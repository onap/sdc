package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

public abstract class ImageManagerFactory extends AbstractComponentFactory<ImageManager> {

  public static ImageManagerFactory getInstance() {
    return AbstractFactory.getInstance(ImageManagerFactory.class);
  }
}

