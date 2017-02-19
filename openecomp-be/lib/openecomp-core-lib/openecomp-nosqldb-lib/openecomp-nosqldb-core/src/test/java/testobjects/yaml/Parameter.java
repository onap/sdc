package testobjects.yaml;

import java.util.Map;

public class Parameter {
  String name;
  String label;
  String description;
  String paramDefault;
  boolean hidden;
  Map<String, InnerP> inner;

  public Map<String, InnerP> getInner() {
    return inner;
  }

  public void setInner(Map<String, InnerP> inner) {
    this.inner = inner;
  }

  public String getParamDefault() {
    return paramDefault;
  }

  public void setParamDefault(String paramDefault) {
    this.paramDefault = paramDefault;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDefault() {
    return paramDefault;
  }

  public void setDefault(String paramDefault) {
    this.paramDefault = paramDefault;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }
}
