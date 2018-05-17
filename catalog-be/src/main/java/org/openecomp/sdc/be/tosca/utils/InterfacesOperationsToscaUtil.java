/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.be.tosca.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.tosca.model.ToscaInterfaceDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaInterfaceNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaLifecycleOperationDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author KATYR
 * @since March 20, 2018
 */

public class InterfacesOperationsToscaUtil {

    private static final String DERIVED_FROM_STANDARD_INTERFACE = "tosca.interfaces.node.lifecycle.Standard";
    private static final String OPERATIONS_KEY = "operations";

    private static final String DEFAULT = "default";
    private static final String _DEFAULT = "_default";
    private static final String DOT = ".";
    private static final String DEFAULT_INPUT_TYPE = "string";
    private static final String SELF = "SELF";
    private static final String GET_PROPERTY = "get_property";
    public static final String DEFAULTP = "defaultp";

    /**
     * Creates the interface_types element
     *
     * @param component to work on
     * @return the added element
     */
    public static Map<String, Object> addInterfaceTypeElement(Component component) {
        Map<String, Object> toscaInterfaceTypes = new HashMap<>();
        if ((component instanceof Service) || (component instanceof Product)) {
            return null;
        }

        final Map<String, InterfaceDefinition> interfaces = ((Resource) component).getInterfaces();
        for (InterfaceDefinition interfaceDefinition : interfaces.values()) {
            ToscaInterfaceNodeType toscaInterfaceType = new ToscaInterfaceNodeType();
            toscaInterfaceType.setDerived_from(DERIVED_FROM_STANDARD_INTERFACE);

            final Map<String, OperationDataDefinition> operations = interfaceDefinition.getOperations();
            Map<String, Object> toscaOperations = new HashMap<>();

            for (String operationId : operations.keySet()) {
                toscaOperations.put(operations.get(operationId).getName(),
                        null); //currently not initializing any of the operations' fields as it is not needed
            }


            toscaInterfaceType.setOperations(toscaOperations);
            Map<String, Object> interfacesAsMap = getObjectAsMap(toscaInterfaceType);
            Map<String, Object> operationsMap = (Map<String, Object>) interfacesAsMap.remove(OPERATIONS_KEY);
            interfacesAsMap.putAll(operationsMap);

            toscaInterfaceTypes.put(interfaceDefinition.getToscaResourceName(), interfacesAsMap);
        }
        return toscaInterfaceTypes;
    }

    /**
     * Adds the 'interfaces' element to the node type provided
     *
     * @param component to work on
     * @param nodeType  to which the interfaces element will be added
     */
    public static void addInterfaceDefinitionElement(Component component, ToscaNodeType nodeType) {
        Map<String, Object> toscaInterfaceDefinitions = new HashMap<>();

        if ((component instanceof Service) || (component instanceof Product)) {
            return;
        }

        final Map<String, InterfaceDefinition> interfaces = ((Resource) component).getInterfaces();
        if (Objects.isNull(interfaces)) {
            return;
        }
        for (InterfaceDefinition interfaceDefinition : interfaces.values()) {
            ToscaInterfaceDefinition toscaInterfaceDefinition = new ToscaInterfaceDefinition();
            final String toscaResourceName = interfaceDefinition.getToscaResourceName();
            toscaInterfaceDefinition.setType(toscaResourceName);
            final Map<String, OperationDataDefinition> operations = interfaceDefinition.getOperations();
            Map<String, Object> toscaOperations = new HashMap<>();

            String operationArtifactPath;
            for (Map.Entry<String, OperationDataDefinition> operationEntry : operations.entrySet()) {
                ToscaLifecycleOperationDefinition toscaOperation = new ToscaLifecycleOperationDefinition();
                if (isArtifactPresent(operationEntry)) {
                    operationArtifactPath = OperationArtifactUtil
                                                    .createOperationArtifactPath(component.getNormalizedName(),
                                                            interfaceDefinition.getToscaResourceName(),
                                                            operationEntry.getValue());
                    toscaOperation.setImplementation(operationArtifactPath);
                }
                toscaOperation.setDescription(operationEntry.getValue().getDescription());
                fillToscaOperationInputs(operationEntry.getValue(), toscaOperation);

                toscaOperations.put(operationEntry.getValue().getName(), toscaOperation);
            }

            toscaInterfaceDefinition.setOperations(toscaOperations);
            Map<String, Object> interfaceDefAsMap = getObjectAsMap(toscaInterfaceDefinition);
            Map<String, Object> operationsMap = (Map<String, Object>) interfaceDefAsMap.remove(OPERATIONS_KEY);
            handleDefaults(operationsMap);
            interfaceDefAsMap.putAll(operationsMap);
            toscaInterfaceDefinitions.put(getLastPartOfName(toscaResourceName), interfaceDefAsMap);
        }
        nodeType.setInterfaces(toscaInterfaceDefinitions);
    }

    /***
     * workaround for : currently "defaultp" is not being converted to "default" by the relevant code in ToscaExportHandler
     * any string key named "defaultp" will have its named changed to "default"
     * @param operationsMap the map to update
     */
    private static void handleDefaults(Map<String, Object> operationsMap) {
        for (String key : operationsMap.keySet()) {
            Object value = operationsMap.get(key);
            if (value instanceof Map) {
                handleDefaults((Map<String, Object>) value);
            }
            if (key.equals(DEFAULTP)) {
                Object removed = operationsMap.remove(key);
                operationsMap.put(DEFAULT, removed);
            }
        }


    }

    private static String getLastPartOfName(String toscaResourceName) {
        return toscaResourceName.substring(toscaResourceName.lastIndexOf(DOT) + 1);

    }

    private static boolean isArtifactPresent(Map.Entry<String, OperationDataDefinition> operationEntry) {
        final boolean isImplementationPresent = !Objects.isNull(operationEntry.getValue().getImplementation());
        if (isImplementationPresent) {
            return !Objects.isNull(operationEntry.getValue().getImplementation().getArtifactName());
        }
        return false;
    }

    private static void fillToscaOperationInputs(OperationDataDefinition operation,
            ToscaLifecycleOperationDefinition toscaOperation) {
        if (operation.getInputs().isEmpty()) {
            toscaOperation.setInputs(null);
            return;
        }
        Map<String, ToscaProperty> toscaInputs = new HashMap<>();

        for (OperationInputDefinition input : operation.getInputs().getListToscaDataDefinition()) {
            ToscaProperty toscaInput = new ToscaProperty();
            toscaInput.setDescription(input.getDescription());
            toscaInput.setType(DEFAULT_INPUT_TYPE);

            toscaInput.setDefaultp(createDefaultValue(getLastPartOfName(input.getInputId())));
            toscaInputs.put(input.getName(), toscaInput);
        }

        toscaOperation.setInputs(toscaInputs);
    }

    private static Map<String, List<String>> createDefaultValue(String propertyName) {
        Map<String, List<String>> getPropertyMap = new HashMap<>();
        List<String> values = new ArrayList<>();
        values.add(SELF);
        values.add(propertyName);
        getPropertyMap.put(GET_PROPERTY, values);

        return getPropertyMap;
    }


    private static Map<String, Object> getObjectAsMap(Object obj) {
        Map<String, Object> objectAsMap =
                obj instanceof Map ? (Map<String, Object>) obj : new ObjectMapper().convertValue(obj, Map.class);

        if (objectAsMap.containsKey(DEFAULT)) {
            Object defaultValue = objectAsMap.get(DEFAULT);
            objectAsMap.remove(DEFAULT);
            objectAsMap.put(_DEFAULT, defaultValue);
        }
        return objectAsMap;
    }

}
