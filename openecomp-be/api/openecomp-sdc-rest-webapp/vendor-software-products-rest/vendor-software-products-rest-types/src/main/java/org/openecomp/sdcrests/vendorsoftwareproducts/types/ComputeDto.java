package org.openecomp.sdcrests.vendorsoftwareproducts.types;

public class ComputeDto {
  private String name;
  private String id;
  private String description;
  private boolean associatedToDeploymentFlavor;

  public boolean isAssociatedToDeploymentFlavor() {
    return associatedToDeploymentFlavor;
  }

  public void setAssociatedToDeploymentFlavor(boolean associatedToDeploymentFlavor) {
    this.associatedToDeploymentFlavor = associatedToDeploymentFlavor;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
