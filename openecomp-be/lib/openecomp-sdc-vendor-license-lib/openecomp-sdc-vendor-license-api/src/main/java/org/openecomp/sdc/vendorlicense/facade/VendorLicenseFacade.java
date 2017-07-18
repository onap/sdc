/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.vendorlicense.facade;

import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;

import java.util.Collection;

public interface VendorLicenseFacade {

  Version checkin(String vendorLicenseModelId, String user);

  Version submit(String vendorLicenseModelId, String user);

  FeatureGroupEntity getFeatureGroup(FeatureGroupEntity featureGroup, String user);

  FeatureGroupModel getFeatureGroupModel(FeatureGroupEntity featureGroup, String user);

  LicenseAgreementEntity getLicenseAgreement(String vlmId, Version version,
                                             String licenseAgreementId, String user);

  LicenseAgreementModel getLicenseAgreementModel(String vlmId, Version version,
                                                 String licenseAgreementId, String user);

  EntitlementPoolEntity createEntitlementPool(EntitlementPoolEntity entitlementPool, String user);

  void updateEntitlementPool(EntitlementPoolEntity entitlementPool, String user);

  Collection<LicenseKeyGroupEntity> listLicenseKeyGroups(String vlmId, Version version,
                                                         String user);

  Collection<EntitlementPoolEntity> listEntitlementPools(String vlmId, Version version,
                                                         String user);

  void updateLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup, String user);

  LicenseKeyGroupEntity createLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup, String user);


  VersionedVendorLicenseModel getVendorLicenseModel(String vlmId, Version version, String user);

  VendorLicenseModelEntity createVendorLicenseModel(
      VendorLicenseModelEntity vendorLicenseModelEntity, String user);


  LicenseAgreementEntity createLicenseAgreement(LicenseAgreementEntity licenseAgreement,
                                                String user);

  FeatureGroupEntity createFeatureGroup(FeatureGroupEntity featureGroup, String user);


  Collection<ErrorCode> validateLicensingData(String vlmId, Version vlmVersion,
                                              String licenseAgreementId,
                                              Collection<String> featureGroupIds);

  VersionInfo getVersionInfo(String vendorLicenseModelId, VersionableEntityAction action,
                             String user);

  void updateVlmLastModificationTime(String vendorLicenseModelId, Version version);

  LimitEntity createLimit(LimitEntity limit, String user);

  Collection<LimitEntity> listLimits(String vlmId, Version version, String epLkgId
                                               ,String user);

  void updateLimit(LimitEntity limit, String user);
}
