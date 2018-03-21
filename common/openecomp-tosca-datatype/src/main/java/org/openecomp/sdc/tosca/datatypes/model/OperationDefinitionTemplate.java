package org.openecomp.sdc.tosca.datatypes.model;

import java.util.Map;
import java.util.Objects;

public class OperationDefinitionTemplate extends OperationDefinition{

  private Implementation implementation;
  private Map<String, Object> inputs;

  public Implementation getImplementation() {
    return implementation;
  }

  public void setImplementation(Implementation implementation) {
    this.implementation = implementation;
  }

  public Map<String, Object> getInputs() {
    return inputs;
  }

  public void setInputs(Map<String, Object> inputs) {
    this.inputs = inputs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OperationDefinitionTemplate)) {
      return false;
    }
    OperationDefinitionTemplate that = (OperationDefinitionTemplate) o;
    return Objects.equals(implementation, that.implementation) &&
        Objects.equals(inputs, that.inputs);
  }

  @Override
  public int hashCode() {

    return Objects.hash(implementation, inputs);
  }
}
