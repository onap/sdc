package org.openecomp.sdc.vendorsoftwareproduct;


import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public interface DeploymentFlavorManager {


  DeploymentFlavorEntity createDeploymentFlavor(DeploymentFlavorEntity deploymentFlavorEntity);

  Collection<DeploymentFlavorEntity> listDeploymentFlavors(String vspId, Version version);

  CompositionEntityResponse<DeploymentFlavor> getDeploymentFlavor(String vspId, Version version,
                                                                  String deploymentFlavorId);

  CompositionEntityResponse<DeploymentFlavor> getDeploymentFlavorSchema(String vspId,
                                                                        Version version);

  void deleteDeploymentFlavor(String vspId, Version version, String deploymentFlavorId);

  CompositionEntityValidationData updateDeploymentFlavor(
      DeploymentFlavorEntity deploymentFlavorEntity);

}
