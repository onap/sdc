package org.openecomp.sdc.healing.healers;


import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Map;
import java.util.Objects;

public class VspOnboardingMethodHealer implements Healer {
  private static VendorSoftwareProductInfoDao vendorSoftwareProductInfoDao;
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  public VspOnboardingMethodHealer(){
    this(VendorSoftwareProductInfoDaoFactory.getInstance().createInterface());
  }

  public VspOnboardingMethodHealer( VendorSoftwareProductInfoDao inVendorSoftwareProductInfoDao){
    vendorSoftwareProductInfoDao = inVendorSoftwareProductInfoDao;
  }


  @Override
  public Object heal(Map<String, Object> healingParams) throws Exception {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    String onboardingMethod=null;
    String vspId = (String) healingParams.get(SdcCommon.VSP_ID);
    Version version = (Version) healingParams.get(SdcCommon.VERSION);
    VspDetails vendorSoftwareProductInfo =
        vendorSoftwareProductInfoDao.get(new VspDetails(vspId, version));
    String onboardingValue = vendorSoftwareProductInfo.getOnboardingMethod();

    if(Objects.isNull(onboardingValue)) {
      onboardingMethod="NetworkPackage";

      updateVSPInfo(OnboardingTypesEnum.ZIP.toString(), onboardingMethod, vendorSoftwareProductInfo);
    } else if (onboardingValue.equals("HEAT")){
      onboardingMethod="NetworkPackage";
      updateVSPInfo(OnboardingTypesEnum.ZIP.toString(),onboardingMethod, vendorSoftwareProductInfo);
    }
    return onboardingMethod;
  }

  private void updateVSPInfo(String onboardingOrigin,  String onboardingMethod, VspDetails vendorSoftwareProductInfo) {
    vendorSoftwareProductInfo.setOnboardingMethod(onboardingMethod);
    vendorSoftwareProductInfo.setOnboardingOrigin(onboardingOrigin);
    vendorSoftwareProductInfoDao.update(vendorSoftwareProductInfo);
  }
}
