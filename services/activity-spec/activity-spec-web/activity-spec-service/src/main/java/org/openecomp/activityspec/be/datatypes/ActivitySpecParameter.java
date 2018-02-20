package org.openecomp.activityspec.be.datatypes;

public class ActivitySpecParameter {
  private String name;
  private String type;
  private String value;

  public ActivitySpecParameter(){/*default constructor*/}


  public ActivitySpecParameter(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
