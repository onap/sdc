package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.openecomp.sdc.vendorsoftwareproduct.ComputeManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComputeManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionEntityDataManagerFactory;

public class ComputeManagerFactoryImpl extends ComputeManagerFactory {

  private static final ComputeManager INSTANCE =
      new ComputeManagerImpl(
          VendorSoftwareProductInfoDaoFactory.getInstance().createInterface(),
          ComputeDaoFactory.getInstance().createInterface(),
          CompositionEntityDataManagerFactory.getInstance().createInterface(),
          DeploymentFlavorDaoFactory.getInstance().createInterface());

  @Override
  public ComputeManager createInterface() {
    return INSTANCE;
  }
}
