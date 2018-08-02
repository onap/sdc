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

    public static InterfaceDefinition createInterface(String uniqueID, String description, String type,
        String toscaResourceName, Map<String, Operation> op) {
        InterfaceDefinition id = new InterfaceDefinition();
        id.setType(type);
        id.setDescription(description);
        id.setUniqueId(uniqueID);
        id.setToscaResourceName(toscaResourceName);
        id.setOperationsMap(op);
        return id;
    }

    public static InterfaceDefinition mockInterfaceDefinitionToReturn(String resourceNamme) {
        Map<String, Operation> operationMap = createMockOperationMap();
        return createInterface("int1", "Interface 1",
            "lifecycle", "org.openecomp.interfaces.node.lifecycle." + resourceNamme, operationMap);
    }

    public static Operation createInterfaceOperation(String uniqueID, String description, ArtifactDefinition artifactDefinition,
        ListDataDefinition<OperationInputDefinition> inputs,
        ListDataDefinition<OperationOutputDefinition> outputs, String name) {
        Operation operation = new Operation();
        operation.setUniqueId(uniqueID);
        operation.setDescription(description);
        operation.setImplementation(artifactDefinition);
        operation.setInputs(inputs);
        operation.setOutputs(outputs);
        operation.setName(name);
        return operation;
    }


    public static Map<String, Operation> createMockOperationMap() {
        Operation operation = new Operation();
        ListDataDefinition<OperationInputDefinition> operationInputDefinitionList = new ListDataDefinition<>();
        operationInputDefinitionList.add(createMockOperationInputDefinition("label1"));
        operation.setInputs(operationInputDefinitionList);

        ListDataDefinition<OperationOutputDefinition> operationOutputDefList = new ListDataDefinition<>();
        operationOutputDefList.add(createMockOperationOutputDefinition("op1"));
        operation.setOutputs(operationOutputDefList);

        operation.setDefinition(false);
        operation.setName("CREATE");
        operation.setUniqueId("uniqueId1");
        ArtifactDefinition implementation = new ArtifactDefinition();
        implementation.setUniqueId("uniqId");
        implementation.setArtifactUUID("artifactId");
        operation.setImplementation(implementation);
        operation.setWorkflowId("workflowId");
        operation.setWorkflowVersionId("workflowVersionId");
        Map<String, Operation> operationMap = new HashMap<>();
        operationMap.put("op1", operation);
        return operationMap;
    }

    public static OperationInputDefinition createMockOperationInputDefinition(String name) {
        OperationInputDefinition operationInputDefinition = new OperationInputDefinition();
        operationInputDefinition.setName(name);
        operationInputDefinition.setUniqueId("uniqueId1");
        return operationInputDefinition;
    }

    public static OperationOutputDefinition createMockOperationOutputDefinition(String name) {
        OperationOutputDefinition operationOutputDefinition = new OperationOutputDefinition();
        operationOutputDefinition.setName(name);
        operationOutputDefinition.setUniqueId("uniqueId1");
        return operationOutputDefinition;
    }

    public static Map<String, InterfaceDefinition> createMockInterfaceDefinition(String resourceName) {
        Map<String, Operation> operationMap = createMockOperationMap();
        Map<String, InterfaceDefinition> interfaceDefinitionMap = new HashMap<>();
        interfaceDefinitionMap.put("int1", createInterface("int1", "Interface 1",
            "lifecycle", "org.openecomp.interfaces.node.lifecycle." + resourceName, operationMap));

        return interfaceDefinitionMap;
    }

}
