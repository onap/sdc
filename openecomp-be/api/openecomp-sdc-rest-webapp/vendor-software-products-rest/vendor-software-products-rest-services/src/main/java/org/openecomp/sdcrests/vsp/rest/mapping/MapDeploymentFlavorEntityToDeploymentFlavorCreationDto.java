package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorCreationDto;


public class MapDeploymentFlavorEntityToDeploymentFlavorCreationDto extends MappingBase<DeploymentFlavorEntity,
    DeploymentFlavorCreationDto> {

  @Override
  public void doMapping(DeploymentFlavorEntity source,
                        DeploymentFlavorCreationDto target) {
    target.setDeploymentFlavorId(source.getId());
  }
}
