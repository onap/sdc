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
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public interface VendorLicenseFacade {

  LicenseAgreementEntity getLicenseAgreement(String vlmId, Version version,
                                             String licenseAgreementId);

  LicenseAgreementModel getLicenseAgreementModel(String vlmId, Version version,
                                                 String licenseAgreementId);

  LicenseAgreementEntity createLicenseAgreement(LicenseAgreementEntity licenseAgreement);

  Collection<FeatureGroupEntity> listFeatureGroups(String vlmId, Version version);

  FeatureGroupEntity getFeatureGroup(FeatureGroupEntity featureGroup);

  FeatureGroupModel getFeatureGroupModel(FeatureGroupEntity featureGroup);

  FeatureGroupEntity createFeatureGroup(FeatureGroupEntity featureGroup);

  Collection<EntitlementPoolEntity> listEntitlementPools(String vlmId, Version version);

  EntitlementPoolEntity createEntitlementPool(EntitlementPoolEntity entitlementPool);

  void updateEntitlementPool(EntitlementPoolEntity entitlementPool);

  Collection<LicenseKeyGroupEntity> listLicenseKeyGroups(String vlmId, Version version);

  LicenseKeyGroupEntity createLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup);

  void updateLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup);

  Collection<LimitEntity> listLimits(String vlmId, Version version, String epLkgId);

  LimitEntity createLimit(LimitEntity limit);

  void updateLimit(LimitEntity limit);

  VendorLicenseModelEntity getVendorLicenseModel(String vlmId, Version version);


  Collection<ErrorCode> validateLicensingData(String vlmId, Version vlmVersion,
                                              String licenseAgreementId,
                                              Collection<String> featureGroupIds);

  void validate(String vendorLicenseModelId, Version version);
}
