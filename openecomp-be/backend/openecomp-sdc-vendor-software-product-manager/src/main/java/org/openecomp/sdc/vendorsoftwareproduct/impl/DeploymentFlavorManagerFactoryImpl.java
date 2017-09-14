package org.openecomp.sdc.vendorsoftwareproduct.impl;


import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManager;
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionEntityDataManagerFactory;

public class DeploymentFlavorManagerFactoryImpl extends DeploymentFlavorManagerFactory {

  private static final DeploymentFlavorManager INSTANCE = new DeploymentFlavorManagerImpl(
      VendorSoftwareProductInfoDaoFactory.getInstance().createInterface(),
      DeploymentFlavorDaoFactory.getInstance().createInterface(),
      CompositionEntityDataManagerFactory.getInstance().createInterface(),
      ComponentDaoFactory.getInstance().createInterface(),
      ComputeDaoFactory.getInstance().createInterface()
  );

  @Override
  public DeploymentFlavorManager createInterface() {
    return INSTANCE;
  }
}
