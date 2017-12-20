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

package org.openecomp.sdc.vendorlicense;

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
import java.util.Set;

public interface VendorLicenseManager {

  void validate(String vendorLicenseModelId, Version version);

  VendorLicenseModelEntity createVendorLicenseModel(VendorLicenseModelEntity licenseModel);

  void updateVendorLicenseModel(VendorLicenseModelEntity licenseModel);

  VendorLicenseModelEntity getVendorLicenseModel(String vlmId, Version version);

  void deleteVendorLicenseModel(String vlmId, Version version);


  Collection<LicenseAgreementEntity> listLicenseAgreements(String vlmId, Version version);

  LicenseAgreementEntity createLicenseAgreement(LicenseAgreementEntity licenseAgreement);

  void updateLicenseAgreement(LicenseAgreementEntity licenseAgreement,
                              Set<String> addedFeatureGroupIds, Set<String> removedFeatureGroupIds);

  LicenseAgreementModel getLicenseAgreementModel(String vlmId, Version version,
                                                 String licenseAgreementId);

  void deleteLicenseAgreement(String vlmId, Version version, String licenseAgreementId);


  Collection<FeatureGroupEntity> listFeatureGroups(String vlmId, Version version);

  FeatureGroupEntity createFeatureGroup(FeatureGroupEntity fg);

  void updateFeatureGroup(FeatureGroupEntity featureGroup,
                          Set<String> addedLicenseKeyGroups, Set<String> removedLicenseKeyGroups,
                          Set<String> addedEntitlementPools, Set<String> removedEntitlementPools);

  FeatureGroupModel getFeatureGroupModel(FeatureGroupEntity featureGroup);

  void deleteFeatureGroup(FeatureGroupEntity featureGroup);


  Collection<EntitlementPoolEntity> listEntitlementPools(String vlmId, Version version);

  EntitlementPoolEntity createEntitlementPool(EntitlementPoolEntity entitlementPool);

  void updateEntitlementPool(EntitlementPoolEntity entitlementPool);

  EntitlementPoolEntity getEntitlementPool(EntitlementPoolEntity entitlementPool);

  void deleteEntitlementPool(EntitlementPoolEntity entitlementPool);


  Collection<LicenseKeyGroupEntity> listLicenseKeyGroups(String vlmId, Version version);

  LicenseKeyGroupEntity createLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup);

  void updateLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup);

  LicenseKeyGroupEntity getLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup);

  void deleteLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup);

  LimitEntity createLimit(LimitEntity limitEntity);

  Collection<LimitEntity> listLimits(String vlmId, Version version, String epLkgId);

  void deleteLimit(LimitEntity limitEntity);

  void updateLimit(LimitEntity limitEntity);

  LimitEntity getLimit(LimitEntity entitlementPool);
}
