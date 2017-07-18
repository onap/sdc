package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorRequestDto;

public class MapDeploymentFlavorRequestDtoToDeploymentFlavorEntity
    extends MappingBase<DeploymentFlavorRequestDto, DeploymentFlavorEntity> {

  @Override
  public void doMapping(DeploymentFlavorRequestDto source, DeploymentFlavorEntity target) {
    DeploymentFlavor deploymentFlavor = new DeploymentFlavor();
    deploymentFlavor.setModel(source.getModel());
    deploymentFlavor.setDescription(source.getDescription());
    deploymentFlavor.setFeatureGroupId(source.getFeatureGroupId());
    deploymentFlavor.setComponentComputeAssociations(source.getComponentComputeAssociations());
    target.setDeploymentFlavorCompositionData(deploymentFlavor);
  }
}
