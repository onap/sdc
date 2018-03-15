package org.openecomp.sdc.tosca.datatypes.model;

import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

public class InterfaceDefinition {
  protected Map<String, OperationDefinition> operations;

  public Map<String, OperationDefinition> getOperations() {
    return operations;
  }

  public void setOperations(
      Map<String, OperationDefinition> operations) {
    this.operations = operations;
  }

  public void addOperation(String operationName, OperationDefinition operationDefinition) {
    if (MapUtils.isEmpty(this.operations)) {
      this.operations = new HashMap<>();
    }
    this.operations.put(operationName, operationDefinition);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceDefinition)) {
      return false;
    }

    InterfaceDefinition that = (InterfaceDefinition) o;

    return getOperations() != null ? getOperations().equals(that.getOperations())
        : that.getOperations() == null;
  }

  @Override
  public int hashCode() {
    return getOperations() != null ? getOperations().hashCode() : 0;
  }
}
