package org.openecomp.sdc.vendorsoftwareproduct.dao;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

public abstract class ImageDaoFactory extends AbstractComponentFactory<ImageDao> {

  public static ImageDaoFactory getInstance() {
    return AbstractFactory.getInstance(ImageDaoFactory.class);
  }
}
