/*
 * Copyright © 2016-2020 European Support Limited
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

package org.openecomp.sdc.be.tosca;

import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DEFAULT;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.INPUTS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.OPERATIONS;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.InputDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.tosca.PropertyConvertor.PropertyType;
import org.openecomp.sdc.be.tosca.model.ToscaInput;
import org.openecomp.sdc.be.tosca.model.ToscaInterfaceDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaInterfaceNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaLifecycleOperationDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.utils.OperationArtifactUtil;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InterfacesOperationsConverter {


    private static final String DERIVED_FROM_STANDARD_INTERFACE = "tosca.interfaces.node.lifecycle.Standard";
    private static final String DERIVED_FROM_BASE_DEFAULT = "org.openecomp.interfaces.node.lifecycle.";

    private static final String DEFAULT_HAS_UNDERSCORE = "_default";
    private static final String DOT = ".";
    private static final String DEFAULTP = "defaultp";

    public static final String SELF = "SELF";
    private static final String LOCAL_INTERFACE_TYPE = "Local";

    private final PropertyConvertor propertyConvertor;

    @Autowired
    public InterfacesOperationsConverter(final PropertyConvertor propertyConvertor) {
        this.propertyConvertor = propertyConvertor;
    }

    /**
     * Creates the interface_types element.
     *
     * @param component to work on
     * @return the added element
     */
    public static Map<String, Object> addInterfaceTypeElement(Component component, List<String> allInterfaceTypes) {
        if (component instanceof Product) {
            return null;
        }
        final Map<String, InterfaceDefinition> interfaces = component.getInterfaces();
        if (MapUtils.isEmpty(interfaces)) {
            return null;
        }

        Map<String, Object> toscaInterfaceTypes = new HashMap<>();
        for (InterfaceDefinition interfaceDefinition : interfaces.values()) {
            boolean isInterfaceTypeExistInGlobalType =
                    allInterfaceTypes.stream().anyMatch(type -> type.equalsIgnoreCase(interfaceDefinition.getType()));
            if (!isInterfaceTypeExistInGlobalType) {
                ToscaInterfaceNodeType toscaInterfaceType = new ToscaInterfaceNodeType();
                toscaInterfaceType.setDerived_from(DERIVED_FROM_STANDARD_INTERFACE);

                final Map<String, OperationDataDefinition> operations = interfaceDefinition.getOperations();
                Map<String, Object> toscaOperations = new HashMap<>();

                for (Map.Entry<String, OperationDataDefinition> operationEntry : operations.entrySet()) {
                    toscaOperations.put(operationEntry.getValue().getName(), null);
                }
                toscaInterfaceType.setOperations(toscaOperations);
                Map<String, Object> interfacesAsMap = getObjectAsMap(toscaInterfaceType);
                Map<String, Object> operationsMap = (Map<String, Object>) interfacesAsMap.remove(OPERATIONS.getElementName());
                interfacesAsMap.putAll(operationsMap);

                toscaInterfaceTypes.put(getInterfaceType(component, LOCAL_INTERFACE_TYPE), interfacesAsMap);
            }
        }
        return MapUtils.isNotEmpty(toscaInterfaceTypes) ? toscaInterfaceTypes : null;
    }

    /**
     * Adds the 'interfaces' element to the node type provided.
     *
     * @param component to work on
     * @param nodeType  to which the interfaces element will be added
     */
    public void addInterfaceDefinitionElement(Component component, ToscaNodeType nodeType,
                                                     Map<String, DataTypeDefinition> dataTypes,
                                                     boolean isAssociatedComponent) {
        if (component instanceof Product) {
            return;
        }
        final Map<String, InterfaceDefinition> interfaces = component.getInterfaces();
        if (MapUtils.isEmpty(interfaces)) {
            return;
        }
        Map<String, Object> toscaInterfaceDefinitions = getInterfacesMap(component, dataTypes,
                isAssociatedComponent);
        if (MapUtils.isNotEmpty(toscaInterfaceDefinitions)) {
            nodeType.setInterfaces(toscaInterfaceDefinitions);
        }
    }

    private Map<String, Object> getInterfacesMap(Component component,
                                                        Map<String, DataTypeDefinition> dataTypes,
                                                        boolean isAssociatedComponent) {
        return getInterfacesMap(component, null, component.getInterfaces(), dataTypes, isAssociatedComponent, false);
    }

    public Map<String, Object> getInterfacesMap(final Component component,
                                                final ComponentInstance componentInstance,
                                                final Map<String, InterfaceDefinition> interfaces,
                                                final Map<String, DataTypeDefinition> dataTypes,
                                                final boolean isAssociatedComponent,
                                                final boolean isServiceProxyInterface) {
        if(MapUtils.isEmpty(interfaces)) {
            return null;
        }

        Map<String, Object> toscaInterfaceDefinitions = new HashMap<>();
        for (InterfaceDefinition interfaceDefinition : interfaces.values()) {
            ToscaInterfaceDefinition toscaInterfaceDefinition = new ToscaInterfaceDefinition();
            final String interfaceType;
            if(componentInstance != null && LOCAL_INTERFACE_TYPE.equals(interfaceDefinition.getType())) {
                interfaceType = DERIVED_FROM_BASE_DEFAULT + componentInstance.getSourceModelName();
            } else {
                interfaceType = getInterfaceType(component, interfaceDefinition.getType());
            }
            if (componentInstance == null) {
                toscaInterfaceDefinition.setType(interfaceType);
            }
            toscaInterfaceDefinition.setType(interfaceType);
            final Map<String, OperationDataDefinition> operations = interfaceDefinition.getOperations();
            Map<String, Object> toscaOperationMap = new HashMap<>();

            String operationArtifactPath;
            for (Map.Entry<String, OperationDataDefinition> operationEntry : operations.entrySet()) {
                ToscaLifecycleOperationDefinition toscaOperation = new ToscaLifecycleOperationDefinition();
                if (isArtifactPresent(operationEntry)) {
                    operationArtifactPath = OperationArtifactUtil
                            .createOperationArtifactPath(component, componentInstance, operationEntry.getValue(),
                                    isAssociatedComponent);
                    toscaOperation.setImplementation(operationArtifactPath);
                }
                toscaOperation.setDescription(operationEntry.getValue().getDescription());
                fillToscaOperationInputs(operationEntry.getValue(), dataTypes, toscaOperation, isServiceProxyInterface);

                toscaOperationMap.put(operationEntry.getValue().getName(), toscaOperation);
            }
            toscaInterfaceDefinition.setOperations(toscaOperationMap);

            final Map<String, Object> interfaceInputMap = createInterfaceInputMap(interfaceDefinition, dataTypes);
            if (!interfaceInputMap.isEmpty()) {
                toscaInterfaceDefinition.setInputs(interfaceInputMap);
            }

            Map<String, Object> interfaceDefAsMap = getObjectAsMap(toscaInterfaceDefinition);
            if (interfaceDefAsMap.containsKey(INPUTS.getElementName())) {
                handleDefaults((Map<String, Object>) interfaceDefAsMap.get(INPUTS.getElementName()));
            }
            Map<String, Object> operationsMap = (Map<String, Object>) interfaceDefAsMap.remove(OPERATIONS.getElementName());
            if (isServiceProxyInterface) {
                //Remove input type and copy default value directly into the proxy node template from the node type
                handleServiceProxyOperationInputValue(operationsMap, interfaceType);
            } else {
                handleDefaults(operationsMap);
            }
            interfaceDefAsMap.putAll(operationsMap);
            toscaInterfaceDefinitions.put(getLastPartOfName(interfaceType), interfaceDefAsMap);
        }

        return toscaInterfaceDefinitions;
    }

    public void removeInterfacesWithoutOperations(final Map<String, Object> interfaceMap) {
        if (MapUtils.isEmpty(interfaceMap)) {
            return;
        }
        final Set<String> emptyInterfaces = interfaceMap.entrySet().stream()
            .filter(entry -> {
                final Object value = entry.getValue();
                if (value instanceof ToscaInterfaceDefinition) {
                    final ToscaInterfaceDefinition interfaceDefinition = (ToscaInterfaceDefinition) value;
                    return MapUtils.isEmpty(interfaceDefinition.getOperations());
                } else if (value instanceof Map) {
                    final Map<String, Object> interfaceDefMap = (Map<String, Object>) value;
                    return MapUtils.isEmpty(interfaceDefMap);
                }
                return false;
            }).map(Entry::getKey).collect(Collectors.toSet());
        emptyInterfaces.forEach(interfaceMap::remove);
    }

    private Map<String, Object> createInterfaceInputMap(final InterfaceDefinition interfaceDefinition,
                                                        final Map<String, DataTypeDefinition> allDataTypeMap) {
        final Map<String, InputDataDefinition> inputMap = interfaceDefinition.getInputs();
        if (MapUtils.isEmpty(inputMap)) {
            return Collections.emptyMap();
        }
        final Map<String, Object> toscaInterfaceInputMap = new HashMap<>();
        for (final Entry<String, InputDataDefinition> inputEntry : inputMap.entrySet()) {
            final InputDataDefinition inputDataDefinition = inputEntry.getValue();
            final ToscaProperty toscaProperty = propertyConvertor
                .convertProperty(allDataTypeMap, new PropertyDefinition(inputDataDefinition), PropertyType.INPUT);
            toscaInterfaceInputMap.put(inputEntry.getKey(), new ToscaInput(toscaProperty));
        }
        return toscaInterfaceInputMap;
    }

    private static void handleServiceProxyOperationInputValue(Map<String, Object> operationsMap, String parentKey) {
        for (Map.Entry<String, Object> operationEntry : operationsMap.entrySet()) {
            final Object value = operationEntry.getValue();
            final String key = operationEntry.getKey();
            if (value instanceof Map) {
                if ("inputs".equals(parentKey)) {
                    Object defaultValue = getDefaultValue((Map<String, Object>) value);
                    operationsMap.put(key, defaultValue);
                } else {
                    handleServiceProxyOperationInputValue((Map<String, Object>) value, key);
                }
            }
        }
    }

    private static Object getDefaultValue(Map<String, Object> inputValueMap) {
        Object defaultValue = null;
        for (Map.Entry<String, Object> operationEntry : inputValueMap.entrySet()) {
            final Object value = operationEntry.getValue();
            if (value instanceof Map) {
                getDefaultValue((Map<String, Object>) value);
            }
            final String key = operationEntry.getKey();
            if (key.equals(DEFAULTP)) {
                defaultValue = inputValueMap.remove(key);
            }
        }
        return defaultValue;
    }

    /*
     * workaround for : currently "defaultp" is not being converted to "default" by the relevant code in
     * ToscaExportHandler so, any string Map key named "defaultp" will have its named changed to "default"
     * @param operationsMap the map to update
     */
    private void handleDefaults(Map<String, Object> operationsMap) {
        for (Map.Entry<String, Object> operationEntry : operationsMap.entrySet()) {
            final Object value = operationEntry.getValue();
            if (value instanceof Map) {
                handleDefaults((Map<String, Object>) value);
            }
            final String key = operationEntry.getKey();
            if (key.equals(DEFAULTP)) {
                Object removed = operationsMap.remove(key);
                operationsMap.put(ToscaTagNamesEnum.DEFAULT.getElementName(), removed);
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

    private void fillToscaOperationInputs(OperationDataDefinition operation,
                                                 Map<String, DataTypeDefinition> dataTypes,
                                                 ToscaLifecycleOperationDefinition toscaOperation,
                                                 boolean isServiceProxyInterface) {
        if (Objects.isNull(operation.getInputs()) || operation.getInputs().isEmpty()) {
            toscaOperation.setInputs(null);
            return;
        }
        Map<String, ToscaProperty> toscaInputs = new HashMap<>();

        for (OperationInputDefinition input : operation.getInputs().getListToscaDataDefinition()) {
            ToscaProperty toscaInput = new ToscaProperty();
            toscaInput.setDescription(input.getDescription());
            toscaInput.setType(input.getType());
            toscaInput.setRequired(input.isRequired());
            if (isServiceProxyInterface) {
                String inputValue = Objects.nonNull(input.getValue()) ? getInputValue(input.getValue()) :
                        getInputValue(input.getToscaDefaultValue());
                toscaInput.setDefaultp(propertyConvertor.convertToToscaObject(input, inputValue, dataTypes, false));
            } else {
                toscaInput.setDefaultp(propertyConvertor
                                               .convertToToscaObject(input, getInputValue(input.getToscaDefaultValue()),
                                                       dataTypes, false));
            }
            toscaInputs.put(input.getName(), toscaInput);
        }
        toscaOperation.setInputs(toscaInputs);
    }

    private static String getInputValue(String inputValue) {
        String toscaInputValue = inputValue;
        if (Objects.nonNull(inputValue) && inputValue.contains(ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName())) {
            Gson gson = new Gson();
            Map<String, List<String>> consumptionValue = gson.fromJson(inputValue, Map.class);
            List<String> mappedOutputValue =
                    consumptionValue.get(ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName());
            //Extract the interface name from the interface type
            String interfaceType = mappedOutputValue.get(1);
            String interfaceName = interfaceType.substring(interfaceType.lastIndexOf('.') + 1);
            mappedOutputValue.remove(1);
            mappedOutputValue.add(1, interfaceName);
            toscaInputValue = gson.toJson(consumptionValue);
        }
        return toscaInputValue;
    }

    private static String getInterfaceType(Component component, String interfaceType) {
        if (LOCAL_INTERFACE_TYPE.equals(interfaceType)) {
            return DERIVED_FROM_BASE_DEFAULT
                    + component.getComponentMetadataDefinition()
                    .getMetadataDataDefinition().getSystemName();
        }

        return interfaceType;
    }

    private static Map<String, Object> getObjectAsMap(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (obj instanceof ToscaInterfaceDefinition) {
            //Prevent empty field serialization in interface definition
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        Map<String, Object> objectAsMap =
                obj instanceof Map ? (Map<String, Object>) obj : objectMapper.convertValue(obj, Map.class);

        final String defaultEntry = DEFAULT.getElementName();
        if (objectAsMap.containsKey(defaultEntry)) {
            objectAsMap.put(DEFAULT_HAS_UNDERSCORE, objectAsMap.remove(defaultEntry));
        }
        return objectAsMap;
    }
}
