package org.openecomp.sdc.vendorsoftwareproduct;


import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public interface DeploymentFlavorManager {


  DeploymentFlavorEntity createDeploymentFlavor(DeploymentFlavorEntity deploymentFlavorEntity,
                                                String user);

  Collection<DeploymentFlavorEntity> listDeploymentFlavors(String vspId, Version version,
                                                           String user);

  CompositionEntityResponse<DeploymentFlavor> getDeploymentFlavor(String vspId, Version version,
                                                                  String deploymentFlavorId,
                                                                  String user);

  CompositionEntityResponse<DeploymentFlavor> getDeploymentFlavorSchema(String vspId, Version
      version, String user);

  void deleteDeploymentFlavor(String vspId, Version version, String deploymentFlavorId, String
      user);

  CompositionEntityValidationData updateDeploymentFlavor(DeploymentFlavorEntity
                                                             deploymentFlavorEntity, String user);

}
