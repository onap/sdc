package org.openecomp.sdc.tosca.datatypes.model;

import java.sql.Time;

public class Trigger {

  private String description;
  private String event_type;
  private TimeInterval schedule;
  private EventFilter target_filter;
  private Constraint condition;
  private Constraint constraint;
  private String period;
  private int evaluations;
  private String method;
  //action - String or operation?
  private Object action;


  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getEvent_type() {
    return event_type;
  }

  public void setEvent_type(String event_type) {
    this.event_type = event_type;
  }

  public TimeInterval getSchedule() {
    return schedule;
  }

  public void setSchedule(TimeInterval schedule) {
    this.schedule = schedule;
  }

  public EventFilter getTarget_filter() {
    return target_filter;
  }

  public void setTarget_filter(EventFilter target_filter) {
    this.target_filter = target_filter;
  }

  public Constraint getCondition() {
    return condition;
  }

  public void setCondition(Constraint condition) {
    this.condition = condition;
  }

  public Constraint getConstraint() {
    return constraint;
  }

  public void setConstraint(Constraint constraint) {
    this.constraint = constraint;
  }

  public String getPeriod() {
    return period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

  public int getEvaluations() {
    return evaluations;
  }

  public void setEvaluations(int evaluations) {
    this.evaluations = evaluations;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public Object getAction() {
    return action;
  }

  public void setAction(Object action) {
    this.action = action;
  }
}
