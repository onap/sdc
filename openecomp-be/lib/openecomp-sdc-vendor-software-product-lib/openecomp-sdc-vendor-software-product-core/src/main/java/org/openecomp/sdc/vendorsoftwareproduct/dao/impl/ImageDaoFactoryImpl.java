package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import org.openecomp.core.zusammen.api.ZusammenAdaptorFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.ImageDaoZusammenImpl;

public class ImageDaoFactoryImpl extends ImageDaoFactory {

  private static final ImageDao INSTANCE = new ImageDaoZusammenImpl(
      ZusammenAdaptorFactory.getInstance().createInterface());

  @Override
  public ImageDao createInterface() {
    return INSTANCE;
  }
}
