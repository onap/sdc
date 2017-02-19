package testobjects.yaml;

import java.util.Map;

public class YamlFile {
  String heat_template_version;
  String description;
  Map<String, Parameter> parameters;


  public YamlFile() {
  }

  public String getHeat_template_version() {
    return heat_template_version;
  }

  public void setHeat_template_version(String heat_template_version) {
    this.heat_template_version = heat_template_version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, Parameter> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, Parameter> parameters) {
    this.parameters = parameters;
  }
}
