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

package org.openecomp.sdc.be.datamodel.utils;

import org.openecomp.sdc.be.datatypes.elements.InterfaceOperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceOperationParamDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.model.Operation;

import java.util.List;
import java.util.stream.Collectors;

public class InterfaceUIDataConverter {

  private InterfaceUIDataConverter () {

  }
  public static Operation convertInterfaceDataToOperationData(InterfaceOperationDataDefinition interfaceOperation){

    ListDataDefinition<InterfaceOperationParamDataDefinition> inputParams = interfaceOperation.getInputParams();
    ListDataDefinition<OperationInputDefinition> inputs = new ListDataDefinition<>();
    if (inputParams != null) {
      List<OperationInputDefinition> inputList = inputParams.getListToscaDataDefinition().stream()
              .map(a -> new OperationInputDefinition(a.getParamName(), a.getParamId())).collect(Collectors.toList());
      inputList.forEach(inputs::add);
    }
    Operation operationData = new Operation();
    operationData.setDescription(interfaceOperation.getDescription());
    operationData.setName(interfaceOperation.getOperationType());
    operationData.setUniqueId(interfaceOperation.getUniqueId());
    operationData.setInputs(inputs);

    return operationData;
  }

  public static InterfaceOperationDataDefinition convertOperationDataToInterfaceData(Operation operationData){

    ListDataDefinition<OperationInputDefinition> inputs = operationData.getInputs();
    List<InterfaceOperationParamDataDefinition> inputParamList = inputs.getListToscaDataDefinition().stream().map(a -> new InterfaceOperationParamDataDefinition(a.getName(), a.getInputId())).collect(
            Collectors.toList());
    ListDataDefinition<InterfaceOperationParamDataDefinition> inputParams = new ListDataDefinition<>();
    inputParamList.forEach(inputParams::add);

    InterfaceOperationDataDefinition interfaceOperationDataDefinition = new InterfaceOperationDataDefinition();
    interfaceOperationDataDefinition.setUniqueId(operationData.getUniqueId());
    interfaceOperationDataDefinition.setOperationType(operationData.getName());
    interfaceOperationDataDefinition.setDescription(operationData.getDescription());
    interfaceOperationDataDefinition.setInputParams(inputParams);
    interfaceOperationDataDefinition.setWorkflowId(operationData.getImplementation().getArtifactUUID());

    return interfaceOperationDataDefinition;
  }

}
