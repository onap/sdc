package org.openecomp.sdc.tosca.datatypes.model;

public class PreconditionDefinition {

  private String target;
  private String target_relationship;
  private Constraint condition;

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getTarget_relationship() {
    return target_relationship;
  }

  public void setTarget_relationship(String target_relationship) {
    this.target_relationship = target_relationship;
  }

  public Constraint getCondition() {
    return condition;
  }

  public void setCondition(Constraint condition) {
    this.condition = condition;
  }
}
