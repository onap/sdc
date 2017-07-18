package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorListResponseDto;

public class MapDeploymentFlavorEntityDeploymentFlavorToListResponse extends
    MappingBase<DeploymentFlavorEntity, DeploymentFlavorListResponseDto> {

  @Override
  public void doMapping(DeploymentFlavorEntity source,
                        DeploymentFlavorListResponseDto target) {
    target.setId(source.getId());
    DeploymentFlavor deploymentFlavor = source.getDeploymentFlavorCompositionData();

    if (deploymentFlavor != null) {
      target.setModel(deploymentFlavor.getModel());
      target.setDescription(deploymentFlavor.getDescription());
    }
  }
}
