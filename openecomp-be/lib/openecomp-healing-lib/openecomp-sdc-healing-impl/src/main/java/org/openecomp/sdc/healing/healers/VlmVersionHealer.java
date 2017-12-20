package org.openecomp.sdc.healing.healers;

import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by TALIO on 7/3/2017.
 */
public class VlmVersionHealer implements Healer {
  private VersioningManager versioningManager =
      VersioningManagerFactory.getInstance().createInterface();
  private static final VendorSoftwareProductInfoDao vspInfoDao =
      VendorSoftwareProductInfoDaoFactory.getInstance().createInterface();
  private static final LicenseAgreementDao licenseAgreementDao =
      LicenseAgreementDaoFactory.getInstance().createInterface();
  private static final Logger logger =
      LoggerFactory.getLogger(VlmVersionHealer.class);

  @Override
  public Object heal(String vspId, Version version) throws Exception {
    VspDetails vspDetails = vspInfoDao.get(new VspDetails(vspId, version));

    if (!Objects.isNull(vspDetails.getVlmVersion())) {
      return Optional.empty();
    }

    // get the certified vlm version with the highest number in its name
    Optional<Version> certifiedVlmVersion =
        versioningManager.list(vspDetails.getVendorId()).stream()
            .filter(ver -> VersionStatus.Certified == ver.getStatus())
            .max((o1, o2) -> ((Double) Double.parseDouble(o1.getName()))
                .compareTo(Double.parseDouble(o2.getName())));
    if (!certifiedVlmVersion.isPresent()) {
      logger.debug("No Vlm was found for Vsp " + vspDetails.getName());
      return Optional.empty();
    }
    vspDetails.setVlmVersion(certifiedVlmVersion.get());

    Collection<LicenseAgreementEntity> licenseAgreements = licenseAgreementDao.list(
        new LicenseAgreementEntity(vspDetails.getVendorId(), certifiedVlmVersion.get(), null));
    if (!licenseAgreements.isEmpty()) {
      LicenseAgreementEntity licenseAgreement = licenseAgreements.iterator().next();
      vspDetails.setLicenseAgreement(licenseAgreement.getId());
      vspDetails.setFeatureGroups(new ArrayList<>(licenseAgreement.getFeatureGroupIds()));
    }

    vspInfoDao.update(vspDetails);

    return vspDetails;
  }
}
