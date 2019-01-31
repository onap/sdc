package org.openecomp.sdc.be.model;

import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.InterfaceInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InterfaceInstanceDefinition extends InterfaceInstanceDataDefinition {

  public InterfaceInstanceDefinition(InterfaceInstanceDataDefinition inter) {
    super(inter);
  }

  public InterfaceInstanceDefinition(){}

  public Map<String, Object> getInputs() {
    return this.inputs;
  }

  public void setInputs(
      Map<String, Object> inputs) {
    this.inputs = inputs;
  }

  public Map<String, OperationInstance> getOperations() {
    return operations;
  }

  public void addInstanceOperation(String operationName, OperationInstance operation) {
    if(MapUtils.isEmpty(this.operations)) {
      this.operations = new HashMap<>();
    }

    this.operations.put(operationName, operation);
  }
}
