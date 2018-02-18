package org.openecomp.sdc.tosca.datatypes.model;

import java.util.List;
import java.util.Map;

public class WorkflowDefinition {
  private String description;
  private Map<String, String> metadata;
  private Map<String, PropertyDefinition> inputs;
  private List<PreconditionDefinition> preconditions;
  private Map<String, StepDefinition> steps;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public Map<String, PropertyDefinition> getInputs() {
    return inputs;
  }

  public void setInputs(
      Map<String, PropertyDefinition> inputs) {
    this.inputs = inputs;
  }

  public List<PreconditionDefinition> getPreconditions() {
    return preconditions;
  }

  public void setPreconditions(
      List<PreconditionDefinition> preconditions) {
    this.preconditions = preconditions;
  }

  public Map<String, StepDefinition> getSteps() {
    return steps;
  }

  public void setSteps(
      Map<String, StepDefinition> steps) {
    this.steps = steps;
  }
}
