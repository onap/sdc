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

package org.openecomp.sdc.be.components.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.tosca.ToscaFunctions;
import org.openecomp.sdc.be.tosca.utils.InterfacesOperationsToscaUtil;

public class InterfaceOperationUtils {

    private InterfaceOperationUtils() {
    }

    public static Optional<InterfaceDefinition> getInterfaceDefinitionFromComponentByInterfaceType(Component component,
            String interfaceType) {
        if (MapUtils.isEmpty(component.getInterfaces())) {
            return Optional.empty();
        }
        return component.getInterfaces().values().stream()
                       .filter(interfaceDefinition -> interfaceDefinition.getType() != null && interfaceDefinition
                                                                                                       .getType()
                                                                                                       .equals(interfaceType))
                       .findAny();
    }

    public static Optional<InterfaceDefinition> getInterfaceDefinitionFromComponentByInterfaceId(Component component,
            String interfaceId) {
        if (MapUtils.isEmpty(component.getInterfaces())) {
            return Optional.empty();
        }
        return component.getInterfaces().values().stream()
                       .filter(interfaceDefinition -> interfaceDefinition.getUniqueId() != null && interfaceDefinition
                                                                                                           .getUniqueId()
                                                                                                           .equals(interfaceId))
                       .findAny();
    }

    public static Optional<Map.Entry<String, Operation>> getOperationFromInterfaceDefinition(
            InterfaceDefinition interfaceDefinition, String operationId) {
        if (MapUtils.isEmpty(interfaceDefinition.getOperationsMap())) {
            return Optional.empty();
        }
        return interfaceDefinition.getOperationsMap().entrySet().stream()
                       .filter(entry -> entry.getValue().getUniqueId().equals(operationId)).findAny();
    }

    public static Optional<InterfaceDefinition> getInterfaceDefinitionFromOperationId(List<InterfaceDefinition> interfaces,
            String operationId) {
        if (CollectionUtils.isEmpty(interfaces)) {
            return Optional.empty();
        }
        return interfaces.stream()
                       .filter(interfaceDefinition -> interfaceDefinition.getOperationsMap().containsKey(operationId))
                       .findAny();
    }

    public static boolean isOperationInputMappedToComponentInput(OperationInputDefinition input,
                                                                                    List<InputDefinition> inputs) {
        if (CollectionUtils.isEmpty(inputs)) {
            return false;
        }

        boolean matchedInput = inputs.stream().anyMatch(inp -> inp.getUniqueId().equals(input.getInputId()));
        if (!matchedInput && input.getInputId().contains(".")) {
            return inputs.stream()
                    .anyMatch(inp -> inp.getUniqueId()
                            .equals(input.getInputId().substring(0, input.getInputId().lastIndexOf('.'))));
        }
        return matchedInput;
    }

    public static boolean isOperationInputMappedToOtherOperationOutput(String outputName,
                                                                       List<OperationOutputDefinition>
                                                                               otherOperationOutputs) {
        if (CollectionUtils.isEmpty(otherOperationOutputs)) {
            return false;
        }
        return otherOperationOutputs.stream()
                .anyMatch(output -> output.getName().equals(outputName));

    }

    public static Map<String, List<String>> createMappedInputPropertyDefaultValue(String propertyName) {
        Map<String, List<String>> getPropertyMap = new HashMap<>();
        List<String> values = new ArrayList<>();
        values.add(InterfacesOperationsToscaUtil.SELF);
        if (Objects.nonNull(propertyName) && !propertyName.isEmpty()) {
            values.addAll(Arrays.asList(propertyName.split("\\.")));
        }
        getPropertyMap.put(ToscaFunctions.GET_PROPERTY.getFunctionName(), values);
        return getPropertyMap;
    }

    public static Map<String, List<String>> createMappedCapabilityPropertyDefaultValue(String capabilityName,
                                                                                       String propertyName) {
        Map<String, List<String>> getPropertyMap = new HashMap<>();
        List<String> values = new ArrayList<>();
        values.add(InterfacesOperationsToscaUtil.SELF);
        values.add(capabilityName);

        if (Objects.nonNull(propertyName) && !propertyName.isEmpty()) {
            values.addAll(Arrays.asList(propertyName.split("\\.")));
        }
        getPropertyMap.put(ToscaFunctions.GET_PROPERTY.getFunctionName(), values);
        return getPropertyMap;
    }

    /**
     * Get the list of outputs of other operations of all the interfaces in the component.
     * @param currentOperationIdentifier Fully qualified operation name e.g. org.test.interfaces.node.lifecycle.Abc.stop
     * @param componentInterfaces VF or service interfaces
     */

    public static ListDataDefinition<OperationOutputDefinition> getOtherOperationOutputsOfComponent(
            String currentOperationIdentifier, Map<String, ? extends InterfaceDataDefinition> componentInterfaces) {
        ListDataDefinition<OperationOutputDefinition> componentOutputs = new ListDataDefinition<>();
        if (MapUtils.isEmpty(componentInterfaces)) {
            return componentOutputs;
        }
        for (Map.Entry<String, ? extends InterfaceDataDefinition> interfaceDefinitionEntry :
                componentInterfaces.entrySet()) {
            String interfaceName = interfaceDefinitionEntry.getKey();
            final Map<String, OperationDataDefinition> operations = interfaceDefinitionEntry.getValue().getOperations();
            if (MapUtils.isEmpty(operations)) {
                continue;
            }
            for (Map.Entry<String, OperationDataDefinition> operationEntry : operations.entrySet()) {
                ListDataDefinition<OperationOutputDefinition> outputs = operationEntry.getValue().getOutputs();
                String expectedOperationIdentifier = interfaceName + "." + operationEntry.getKey();
                if (!currentOperationIdentifier.equals(expectedOperationIdentifier) && !outputs.isEmpty()) {
                    outputs.getListToscaDataDefinition().forEach(componentOutputs::add);
                }
            }
        }
        return componentOutputs;
    }

    /**
     * Create the value for operation input mapped to an operation output.
     * @param propertyName the mapped other operation output full name
     * @return input map for tosca
     */
    public static Map<String, List<String>> createMappedOutputDefaultValue(String componentName, String propertyName) {
        Map<String, List<String>> getOperationOutputMap = new HashMap<>();
        //For operation input mapped to other operation output parameter, the mapped property value
        // should be of the format <interface name>.<operation name>.<output parameter name>
        // Operation name and output param name should not contain "."
        List<String> defaultMappedOperationOutputValue = new ArrayList<>();
        String[] tokens = propertyName.split("\\.");
        if (tokens.length > 2) {
            defaultMappedOperationOutputValue.add(componentName);
            String outputPropertyName = tokens[tokens.length - 1];
            String operationName = tokens[tokens.length - 2];
            String mappedPropertyInterfaceType =
                    propertyName.substring(0, propertyName.indexOf(operationName + '.' + outputPropertyName) - 1);
            defaultMappedOperationOutputValue.addAll(Arrays.asList(mappedPropertyInterfaceType, operationName,
                    outputPropertyName));
            getOperationOutputMap.put(ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName(),
                    defaultMappedOperationOutputValue);
        }
        return getOperationOutputMap;
    }

    public static String getOperationOutputName(String fullOutputIdentifier) {
        return fullOutputIdentifier.contains(".")
                ? fullOutputIdentifier.substring(fullOutputIdentifier.lastIndexOf('.') + 1)
                : fullOutputIdentifier;
    }

    public static boolean isArtifactInUse(Component component, String operationId, String artifactUniqueId) {
        return MapUtils.emptyIfNull(component.getInterfaces()).values().stream()
                       .filter(o -> MapUtils.isNotEmpty(o.getOperations()) && !o.getOperations().containsKey(operationId))
                       .flatMap(o -> o.getOperations().values().stream()).collect(Collectors.toList()).stream()
                       .anyMatch(op -> op.getImplementation().getUniqueId().equals(artifactUniqueId));
    }
}
