package org.openecomp.sdc.tosca.datatypes.model;

import java.util.Map;
import java.util.Objects;

public class OperationDefinitionType extends OperationDefinition {

  private String implementation;
  private Map<String, PropertyDefinition> inputs;

  public String getImplementation() {
    return implementation;
  }

  public void setImplementation(String implementation) {
    this.implementation = implementation;
  }

  public Map<String, PropertyDefinition> getInputs() {
    return inputs;
  }

  public void setInputs(
      Map<String, PropertyDefinition> inputs) {
    this.inputs = inputs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OperationDefinitionType)) {
      return false;
    }
    OperationDefinitionType that = (OperationDefinitionType) o;
    return Objects.equals(implementation, that.implementation) &&
        Objects.equals(inputs, that.inputs);
  }

  @Override
  public int hashCode() {

    return Objects.hash(implementation, inputs);
  }
}
