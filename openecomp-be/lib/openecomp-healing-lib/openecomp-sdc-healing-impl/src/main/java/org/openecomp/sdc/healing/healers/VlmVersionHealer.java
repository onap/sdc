package org.openecomp.sdc.healing.healers;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;
import org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by TALIO on 7/3/2017.
 */
public class VlmVersionHealer implements Healer {
  private static final VendorLicenseFacade vendorLicenseFacade =
      VendorLicenseFacadeFactory.getInstance().createInterface();
  private static final VendorSoftwareProductInfoDao vspInfoDao =
      VendorSoftwareProductInfoDaoFactory.getInstance().createInterface();
  private static final LicenseAgreementDao licenseAgreementDao =
      LicenseAgreementDaoFactory.getInstance().createInterface();
  private static final Logger logger =
      LoggerFactory.getLogger(VlmVersionHealer.class);

  @Override
  public Object heal(Map<String, Object> healingParams) throws Exception {
    String vspId = (String) healingParams.get(SdcCommon.VSP_ID);
    Version version = (Version) healingParams.get(SdcCommon.VERSION);
    String user = (String) healingParams.get(SdcCommon.USER);

    VspDetails vspDetails = vspInfoDao.get(new VspDetails(vspId, version));
    VersionedVendorLicenseModel vendorLicenseModel;
    
    if(!Objects.isNull(vspDetails.getVlmVersion())) {
      return Optional.empty();
    }


    try{
      vendorLicenseModel =
          vendorLicenseFacade.getVendorLicenseModel(vspDetails.getVendorId(), null, user);
    } catch (Exception e){
      logger.debug("" + e);
      logger.debug("No Vlm was found for Vsp " + vspDetails.getName());
      return Optional.empty();
    }

    VendorLicenseModelEntity vlm = vendorLicenseModel.getVendorLicenseModel();
    String vlmId = vlm.getId();
    Version vlmVersion = getLatestFinalVlmVersion(vendorLicenseModel.getVersionInfo());

    List<LicenseAgreementEntity> laList =
        new ArrayList<>(
            licenseAgreementDao.list(new LicenseAgreementEntity(vlmId, vlmVersion, null)));


    vspDetails.setVlmVersion(vlmVersion);

    if(CollectionUtils.isNotEmpty(laList)) {
      vspDetails.setLicenseAgreement(laList.get(0).getId());
      vspDetails.setFeatureGroups(new ArrayList<>(laList.get(0).getFeatureGroupIds()));
    }

    vspInfoDao.update(vspDetails);
    return vspDetails;

  }

  private Version getLatestFinalVlmVersion(VersionInfo versionInfo){
    return versionInfo.getActiveVersion().isFinal() ? versionInfo.getActiveVersion()
        : versionInfo.getLatestFinalVersion();
  }
}
