package org.openecomp.sdc.vendorlicense.impl;

import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorlicense.VendorLicenseManagerFactory;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LimitDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDaoFactory;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;

/**
 * Created by ayalaben on 8/3/2017
 */
public class VendorLicenseManagerFactoryImpl extends VendorLicenseManagerFactory {
  private static final VendorLicenseManager INSTANCE =
      new VendorLicenseManagerImpl(
          VendorLicenseFacadeFactory.getInstance().createInterface(),
          VendorLicenseModelDaoFactory.getInstance().createInterface(),
          LicenseAgreementDaoFactory.getInstance().createInterface(),
          FeatureGroupDaoFactory.getInstance().createInterface(),
          EntitlementPoolDaoFactory.getInstance().createInterface(),
          LicenseKeyGroupDaoFactory.getInstance().createInterface(),
          LimitDaoFactory.getInstance().createInterface());

  @Override
  public VendorLicenseManager createInterface() {
    return INSTANCE;
  }

}
