package org.openecomp.sdc.tosca.datatypes.model;

public class ActivityDefinition {

  private String delegate;
  private String set_state;
  private String call_operation;
  private String inline;

  public String getDelegate() {
    return delegate;
  }

  public void setDelegate(String delegate) {
    this.delegate = delegate;
  }

  public String getSet_state() {
    return set_state;
  }

  public void setSet_state(String set_state) {
    this.set_state = set_state;
  }

  public String getCall_operation() {
    return call_operation;
  }

  public void setCall_operation(String call_operation) {
    this.call_operation = call_operation;
  }

  public String getInline() {
    return inline;
  }

  public void setInline(String inline) {
    this.inline = inline;
  }
}
