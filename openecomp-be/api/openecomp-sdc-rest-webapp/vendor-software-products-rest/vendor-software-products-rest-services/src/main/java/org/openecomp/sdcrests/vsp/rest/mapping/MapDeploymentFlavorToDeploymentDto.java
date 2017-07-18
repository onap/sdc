package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorDto;


public class MapDeploymentFlavorToDeploymentDto extends MappingBase<DeploymentFlavor, DeploymentFlavorDto> {
  @Override
  public void doMapping(DeploymentFlavor source, DeploymentFlavorDto target) {
    target.setModel(source.getModel());
    target.setDescription(source.getDescription());
    target.setFeatureGroupId(source.getFeatureGroupId());
    target.setComponentComputeAssociations(source.getComponentComputeAssociations());
  }
}
