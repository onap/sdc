package org.openecomp.sdc.tosca.datatypes.model;

import java.util.Map;

public class OperationDefinition {

  private String description;
  private String implementation;
  private Map<String, PropertyDefinition> inputs;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

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
    if (!(o instanceof OperationDefinition)) {
      return false;
    }

    OperationDefinition that = (OperationDefinition) o;

    if (getDescription() != null ? !getDescription().equals(that.getDescription())
        : that.getDescription() != null) {
      return false;
    }
    if (getImplementation() != null ? !getImplementation().equals(that.getImplementation())
        : that.getImplementation() != null) {
      return false;
    }
    return getInputs() != null ? getInputs().equals(that.getInputs()) : that.getInputs() == null;
  }

  @Override
  public int hashCode() {
    int result = getDescription() != null ? getDescription().hashCode() : 0;
    result = 31 * result + (getImplementation() != null ? getImplementation().hashCode() : 0);
    result = 31 * result + (getInputs() != null ? getInputs().hashCode() : 0);
    return result;
  }
}
