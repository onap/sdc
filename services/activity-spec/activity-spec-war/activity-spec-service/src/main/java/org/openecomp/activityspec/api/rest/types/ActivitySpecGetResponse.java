package org.openecomp.activityspec.api.rest.types;

import java.util.List;

public class ActivitySpecGetResponse  {
  private String name;
  private String description;
  private List<String> categoryList;
  private List<ActivitySpecParameterDto> inputParameters;
  private List<ActivitySpecParameterDto> outputParameters;
  private String status;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getCategoryList() {
    return categoryList;
  }

  public void setCategoryList(List<String> categoryList) {
    this.categoryList = categoryList;
  }

  public List<ActivitySpecParameterDto> getInputParameters() {
    return inputParameters;
  }

  public void setInputParameters(List<ActivitySpecParameterDto> inputParameters) {
    this.inputParameters = inputParameters;
  }

  public List<ActivitySpecParameterDto> getOutputParameters() {
    return outputParameters;
  }

  public void setOutputParameters(List<ActivitySpecParameterDto> outputParameters) {
    this.outputParameters = outputParameters;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
