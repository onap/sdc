package org.openecomp.sdcrests.item.types;

import org.openecomp.sdc.versioning.types.VersionCreationMethod;

public class VersionRequestDto {
  private String description;
  private VersionCreationMethod creationMethod;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public VersionCreationMethod getCreationMethod() {
    return creationMethod;
  }

  public void setCreationMethod(VersionCreationMethod creationMethod) {
    this.creationMethod = creationMethod;
  }
}
