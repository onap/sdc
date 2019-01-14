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

package org.openecomp.sdc.test.utils;

import java.util.HashMap;
import java.util.Map;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;

public class InterfaceOperationTestUtils {

    public static Map<String, InterfaceDefinition> createMockInterfaceDefinitionMap(String interfaceId,
            String operationId) {
        Map<String, InterfaceDefinition> interfaceDefinitionMap = new HashMap<>();
        interfaceDefinitionMap.put(interfaceId, createMockInterface(interfaceId, operationId));
        return interfaceDefinitionMap;
    }

    public static InterfaceDefinition createMockInterface(String interfaceId, String operationId) {
        return createInterface(interfaceId, interfaceId, interfaceId, interfaceId, createMockOperationMap(operationId));
    }

    private static InterfaceDefinition createInterface(String uniqueId, String description, String type,
            String toscaResourceName, Map<String, Operation> op) {
        InterfaceDefinition id = new InterfaceDefinition();
        id.setType(type);
        id.setDescription(description);
        id.setUniqueId(uniqueId);
        id.setToscaResourceName(toscaResourceName);
        id.setOperationsMap(op);
        return id;
    }

    public static Map<String, Operation> createMockOperationMap(String operationId) {
        Map<String, Operation> operationMap = new HashMap<>();
        operationMap.put(operationId, createMockOperation(operationId));
        return operationMap;
    }

    public static Operation createMockOperation(String operationId) {
        Operation operation = new Operation();
        ListDataDefinition<OperationInputDefinition> operationInputDefinitionList = new ListDataDefinition<>();
        operationInputDefinitionList.add(createMockOperationInputDefinition("inputId"));
        operation.setInputs(operationInputDefinitionList);

        ListDataDefinition<OperationOutputDefinition> operationOutputDefList = new ListDataDefinition<>();
        operationOutputDefList.add(createMockOperationOutputDefinition("outputId"));
        operation.setOutputs(operationOutputDefList);

        operation.setDefinition(false);
        operation.setName(operationId);
        operation.setUniqueId(operationId);
        ArtifactDefinition implementation = new ArtifactDefinition();
        implementation.setUniqueId("uniqId");
        implementation.setArtifactUUID("artifactId");
        operation.setImplementation(implementation);
        operation.setWorkflowId("workflowId");
        operation.setWorkflowVersionId("workflowVersionId");
        return operation;
    }

    public static OperationInputDefinition createMockOperationInputDefinition(String inputId) {
        OperationInputDefinition operationInputDefinition = new OperationInputDefinition();
        operationInputDefinition.setName(inputId);
        operationInputDefinition.setUniqueId(inputId);
        operationInputDefinition.setInputId(inputId);
        operationInputDefinition.setValue(inputId);
        operationInputDefinition.setDefaultValue(inputId);
        return operationInputDefinition;
    }

    public static OperationOutputDefinition createMockOperationOutputDefinition(String outputId) {
        OperationOutputDefinition operationOutputDefinition = new OperationOutputDefinition();
        operationOutputDefinition.setName(outputId);
        operationOutputDefinition.setUniqueId(outputId);
        operationOutputDefinition.setInputId(outputId);
        operationOutputDefinition.setValue(outputId);
        operationOutputDefinition.setDefaultValue(outputId);
        return operationOutputDefinition;
    }

    public static Map<String, InterfaceDefinition> createMockInterfaceTypeMap(String interfaceType,
            String operationType) {
        Map<String, Operation> operationMap = createMockOperationTypeMap(operationType);
        Map<String, InterfaceDefinition> interfaceDefinitionMap = new HashMap<>();
        interfaceDefinitionMap.put(interfaceType,
                createInterface(interfaceType, interfaceType, interfaceType, interfaceType, operationMap));
        return interfaceDefinitionMap;
    }

    private static Map<String, Operation> createMockOperationTypeMap(String operationType) {
        Operation operation = new Operation();
        operation.setUniqueId(operationType);
        Map<String, Operation> operationMap = new HashMap<>();
        operationMap.put(operationType, operation);
        return operationMap;
    }

}
