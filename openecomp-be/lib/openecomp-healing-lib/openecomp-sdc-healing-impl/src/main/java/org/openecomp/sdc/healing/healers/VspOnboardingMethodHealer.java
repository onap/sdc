package org.openecomp.sdc.healing.healers;


import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Map;
import java.util.Objects;

public class VspOnboardingMethodHealer implements Healer {
  /*private static final VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();*/
  private static final VendorSoftwareProductInfoDao vendorSoftwareProductInfoDao =
      VendorSoftwareProductInfoDaoFactory.getInstance().createInterface();
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  public VspOnboardingMethodHealer(){

  }
  @Override
  public Object heal(Map<String, Object> healingParams) throws Exception {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    String onboardingMethod=null;
    String vspId = (String) healingParams.get(SdcCommon.VSP_ID);
    Version version = (Version) healingParams.get(SdcCommon.VERSION);
    VspDetails vendorSoftwareProductInfo =
        vendorSoftwareProductInfoDao.get(new VspDetails(vspId, version));
    vendorSoftwareProductInfo.getOnboardingMethod();

    if(Objects.isNull(vendorSoftwareProductInfo.getOnboardingMethod())) {
      onboardingMethod="HEAT";
      vendorSoftwareProductInfo.setOnboardingMethod(onboardingMethod);
      vendorSoftwareProductInfoDao.update(vendorSoftwareProductInfo);
      //vendorSoftwareProductDao.updateVendorSoftwareProductInfo(vendorSoftwareProductInfo);
    }
    return onboardingMethod;
  }
}
