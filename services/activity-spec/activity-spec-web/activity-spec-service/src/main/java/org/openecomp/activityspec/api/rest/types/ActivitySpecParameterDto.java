package org.openecomp.activityspec.api.rest.types;

public class ActivitySpecParameterDto {

  private String name;
  private String type;
  private String value;

  public void setName(String name) {
    this.name = name;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getValue() {
    return value;
  }
}
