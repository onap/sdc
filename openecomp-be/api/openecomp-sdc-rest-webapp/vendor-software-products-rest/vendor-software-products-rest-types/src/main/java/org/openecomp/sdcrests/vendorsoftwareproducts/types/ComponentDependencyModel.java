package org.openecomp.sdcrests.vendorsoftwareproducts.types;


import org.hibernate.validator.constraints.NotBlank;

public class ComponentDependencyModel {

  private String sourceId;
  private String targetId;
  private String relationType;

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public String getTargetId() {
    return targetId;
  }

  public void setTargetId(String targetId) {
    this.targetId = targetId;
  }

  public String getRelationType() {
    return relationType;
  }

  public void setRelationType(String relationType) {
    this.relationType = relationType;
  }
}
