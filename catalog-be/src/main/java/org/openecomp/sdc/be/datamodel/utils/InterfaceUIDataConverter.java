package org.openecomp.sdc.be.datamodel.utils;

import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.Operation;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class InterfaceUIDataConverter {

  public static Operation convertInterfaceDataToOperationData(InterfaceOperationDataDefinition interfaceOperation){

    ListDataDefinition<InterfaceOperationParamDataDefinition> inputParams = interfaceOperation.getInputParams();
    ListDataDefinition<OperationInputDefinition> inputs = new ListDataDefinition<>();
    if (inputParams != null) {
      List<OperationInputDefinition> inputList = inputParams.getListToscaDataDefinition().stream()
          .map(a -> new OperationInputDefinition(a.getParamName(),
              new InputDataDefinition(new HashMap<String, Object>() {
                {
                  put(
                      JsonPresentationFields.UNIQUE_ID.getPresentation(), a.getParamId());
                }

                ;
              }))).collect(
              Collectors.toList());
      inputList.forEach(input -> inputs.add(input));
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
    List<InterfaceOperationParamDataDefinition> inputParamList = inputs.getListToscaDataDefinition().stream().map(a -> new InterfaceOperationParamDataDefinition(a.getName(), a.getUniqueId())).collect(
        Collectors.toList());
    ListDataDefinition<InterfaceOperationParamDataDefinition> inputParams = new ListDataDefinition<>();
    inputParamList.forEach(input -> inputParams.add(input));

    InterfaceOperationDataDefinition interfaceOperationDataDefinition = new InterfaceOperationDataDefinition();
    interfaceOperationDataDefinition.setUniqueId(operationData.getUniqueId());
    interfaceOperationDataDefinition.setOperationType(operationData.getName());
    interfaceOperationDataDefinition.setDescription(operationData.getDescription());
    interfaceOperationDataDefinition.setInputParams(inputParams);

    return interfaceOperationDataDefinition;
  }

}
