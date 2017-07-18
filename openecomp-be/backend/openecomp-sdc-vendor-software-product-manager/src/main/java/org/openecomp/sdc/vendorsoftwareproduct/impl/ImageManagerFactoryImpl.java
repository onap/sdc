package org.openecomp.sdc.vendorsoftwareproduct.impl;


import org.openecomp.sdc.vendorsoftwareproduct.ImageManager;
import org.openecomp.sdc.vendorsoftwareproduct.ImageManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionEntityDataManagerFactory;

public class ImageManagerFactoryImpl extends ImageManagerFactory {

  private static final ImageManager INSTANCE =
      new ImageManagerImpl(
          VendorSoftwareProductInfoDaoFactory.getInstance().createInterface(),
          ImageDaoFactory.getInstance().createInterface(),
          CompositionEntityDataManagerFactory.getInstance().createInterface()
      );

  @Override
  public ImageManager createInterface() {
    return INSTANCE;
  }

}
