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
package org.openecomp.sdc.be.model.jsontitan.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.InputDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;

public class InterfaceUtils {

    public static final String INTERFACE_TOSCA_RESOURCE_NAME = "org.openecomp.interfaces.node.lifecycle.%s";

    public static final Optional<InterfaceDefinition> getInterfaceDefinitionFromToscaName(
        Collection<InterfaceDefinition> interfaces,
        String resourceName) {
        if (CollectionUtils.isEmpty(interfaces)) {
            return Optional.empty();
        }

        String toscaName = createInterfaceToscaResourceName(resourceName);
        return interfaces.stream().filter(
            interfaceDefinition -> interfaceDefinition.getToscaResourceName() != null && interfaceDefinition
                .getToscaResourceName().equals(toscaName)).findAny();
    }

    public static Collection<InterfaceDefinition> getInterfaceDefinitionListFromToscaName(Collection<InterfaceDefinition> interfaces,
                                                                                          String resourceName) {
        if(CollectionUtils.isEmpty(interfaces)){
            return CollectionUtils.EMPTY_COLLECTION;
        }

        String toscaName = createInterfaceToscaResourceName(resourceName);
        return interfaces.stream().filter(
                interfaceDefinition -> interfaceDefinition.getToscaResourceName() != null && interfaceDefinition
                        .getToscaResourceName().equals(toscaName)).collect(Collectors.toList());
    }

    public static String createInterfaceToscaResourceName(String resourceName) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        return formatter.format(INTERFACE_TOSCA_RESOURCE_NAME, resourceName).toString();
    }

    public static Map<String, Operation> getInterfaceOperationsFromInterfaces(
        Map<String, InterfaceDefinition> interfaces,
        Resource resource) throws IllegalStateException {
        if (MapUtils.isEmpty(interfaces)) {
            return Collections.EMPTY_MAP;
        }
        Optional<InterfaceDefinition> optionalInterface = getInterfaceDefinitionFromToscaName(
            interfaces.values(), resource.getName());
        if (!optionalInterface.isPresent()) {
            return Collections.EMPTY_MAP;
        }
        InterfaceDefinition interfaceDefinition = optionalInterface.get();
        interfaceDefinition.getOperationsMap().values().stream()
            .forEach(operation -> createInputOutput(operation, resource.getInputs()));


        return interfaceDefinition.getOperationsMap();

    }

    private static void createInputOutput(Operation operation, List<InputDefinition> inputs) throws IllegalStateException {
        ListDataDefinition<OperationInputDefinition> inputDefinitionListDataDefinition = operation.getInputs();
        if (inputDefinitionListDataDefinition != null) {
            return;
        }
        List<OperationInputDefinition> listToscaDataDefinition = inputDefinitionListDataDefinition
            .getListToscaDataDefinition();
        List<OperationInputDefinition> convertedInputs = listToscaDataDefinition.stream()
            .map(input -> convertInput(input, inputs))
            .collect(Collectors.toList());

        ListDataDefinition<OperationOutputDefinition> outputDefinitionListDataDefinition = operation.getOutputs();
        if (outputDefinitionListDataDefinition != null) {
            return;
        }
        List<OperationOutputDefinition> outListToscaDefinition = outputDefinitionListDataDefinition
                .getListToscaDataDefinition();
        List<OperationOutputDefinition> convertedOutputs = outListToscaDefinition.stream()
                .map(operationOutputDefinition -> convertOutput(operationOutputDefinition, inputs) )
                .collect(Collectors.toList());
        inputDefinitionListDataDefinition.getListToscaDataDefinition().clear();
        inputDefinitionListDataDefinition.getListToscaDataDefinition().addAll(convertedInputs);
        outputDefinitionListDataDefinition.getListToscaDataDefinition().clear();
        outputDefinitionListDataDefinition.getListToscaDataDefinition().addAll(convertedOutputs);
    }

    private static OperationInputDefinition convertInput(OperationInputDefinition input,
                                                         List<InputDefinition> inputs) throws IllegalStateException {
        Optional<InputDefinition> anyInputDefinition = inputs.stream()
            .filter(inp -> inp.getUniqueId().equals(input.getUniqueId())).findAny();
        if (anyInputDefinition.isPresent()) {
            return new OperationInputDefinition(input.getLabel(),new InputDataDefinition(anyInputDefinition.get()));
        }
        throw new IllegalStateException("Could not find input :"+ input.getLabel());
    }

    private static OperationOutputDefinition convertOutput(OperationOutputDefinition output,
                                                         List<InputDefinition> outputs) throws IllegalStateException {
        Optional<InputDefinition> anyOutputDefinition = outputs.stream()
                .filter(inp -> inp.getUniqueId().equals(output.getUniqueId())).findAny();
        if (anyOutputDefinition.isPresent()) {
            return new OperationOutputDefinition(output.getLabel(),new InputDataDefinition(anyOutputDefinition.get()));
        }
        throw new IllegalStateException("Could not find output :"+ output.getLabel());
    }
    public static List<Operation> getOperationsFromInterface(Map<String, InterfaceDefinition> interfaces) {
        List<Operation> operationData = new ArrayList<>();
        if (!MapUtils.isEmpty(interfaces)) {
            operationData = interfaces.values().stream()
                .filter(a -> MapUtils.isNotEmpty(a.getOperationsMap()))
                .map(a-> new ArrayList<>(a.getOperationsMap().values())).flatMap(List::stream).collect(Collectors.toList());
        }
        return operationData;
    }
}
