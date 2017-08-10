package org.openecomp.sdc.vendorlicense;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

/**
 * Created by ayalaben on 8/3/2017
 */
public abstract class  VendorLicenseManagerFactory extends
    AbstractComponentFactory<VendorLicenseManager> {

  public static VendorLicenseManagerFactory getInstance() {
    return AbstractFactory.getInstance(VendorLicenseManagerFactory.class);
  }
}
