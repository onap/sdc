package org.openecomp.sdcrests.vendorsoftwareproducts.types;

public class VspComputeDto {
  private String name;
  private String componentId;
  private String computeFlavorId;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getComputeFlavorId() {
    return computeFlavorId;
  }

  public void setComputeFlavorId(String computeFlavorId) {
    this.computeFlavorId = computeFlavorId;
  }
}
