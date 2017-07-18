package org.openecomp.sdc.vendorsoftwareproduct.types;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;

public class ListComputeResponse {

  private ComputeEntity computeEntity;
  private boolean associatedWithDeploymentFlavor;

  public boolean isAssociatedWithDeploymentFlavor() {
    return associatedWithDeploymentFlavor;
  }

  public void setAssociatedWithDeploymentFlavor(boolean associatedWithDeploymentFlavor) {
    this.associatedWithDeploymentFlavor = associatedWithDeploymentFlavor;
  }

  public ComputeEntity getComputeEntity() {
    return computeEntity;
  }

  public void setComputeEntity(
      ComputeEntity computeEntity) {
    this.computeEntity = computeEntity;
  }


}
