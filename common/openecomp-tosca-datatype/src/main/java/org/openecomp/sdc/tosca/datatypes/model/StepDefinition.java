package org.openecomp.sdc.tosca.datatypes.model;

import java.util.List;

public class StepDefinition {

  private String target;
  private String target_relationship;
  private String operation_host;
  private List<Constraint> filter;
  private List<ActivityDefinition> activities;
  private String on_success;
  private String on_failure;


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

  public String getOperation_host() {
    return operation_host;
  }

  public void setOperation_host(String operation_host) {
    this.operation_host = operation_host;
  }

  public List<Constraint> getFilter() {
    return filter;
  }

  public void setFilter(List<Constraint> filter) {
    this.filter = filter;
  }

  public List<ActivityDefinition> getActivities() {
    return activities;
  }

  public void setActivities(List<ActivityDefinition> activities) {
    this.activities = activities;
  }

  public String getOn_success() {
    return on_success;
  }

  public void setOn_success(String on_success) {
    this.on_success = on_success;
  }

  public String getOn_failure() {
    return on_failure;
  }

  public void setOn_failure(String on_failure) {
    this.on_failure = on_failure;
  }
}
