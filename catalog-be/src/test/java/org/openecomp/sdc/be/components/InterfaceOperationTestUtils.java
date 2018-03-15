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

package org.openecomp.sdc.be.components;

import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.WorkflowOperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.WorkflowOperationParamDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;

import java.util.Map;

public interface InterfaceOperationTestUtils {

    default InterfaceDefinition createInterface(String uniqueID, String description, String type,
                                                String toscaResourceName, Map<String, Operation> op) {
        InterfaceDefinition id = new InterfaceDefinition();
        id.setType(type);
        id.setDescription(description);
        id.setUniqueId(uniqueID);
        id.setToscaResourceName(toscaResourceName);
        id.setOperationsMap(op);
        return id;
    }


    default Operation createInterfaceOperation(String uniqueID, String description, ArtifactDefinition artifactDefinition,
                                               ListDataDefinition<OperationInputDefinition> inputs, String name) {
        Operation operation = new Operation();

        operation.setUniqueId(uniqueID);
        operation.setDescription(description);
        operation.setImplementation(artifactDefinition);
        operation.setInputs(inputs);
        operation.setName(name);

        return operation;
    }


}
