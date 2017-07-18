package org.openecomp.sdcrests.vsp.rest.mapping;


import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorDto;

public class MapDeploymentFlavorEntityToDeploymentFlavorDto  extends
    MappingBase<DeploymentFlavorEntity, DeploymentFlavorDto> {
  @Override
  public void doMapping(DeploymentFlavorEntity source, DeploymentFlavorDto target) {
    target.setId(source.getId());
    DeploymentFlavor deploymentFlavor = source.getDeploymentFlavorCompositionData();
    if (deploymentFlavor != null) {
      //new MapNetworkToNetworkDto().doMapping(deploymentFlavor, target);
    }
  }
}
