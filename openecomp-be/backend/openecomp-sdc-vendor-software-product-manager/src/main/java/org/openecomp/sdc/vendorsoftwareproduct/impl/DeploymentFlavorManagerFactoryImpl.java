package org.openecomp.sdc.vendorsoftwareproduct.impl;


import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManager;
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionEntityDataManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;

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
