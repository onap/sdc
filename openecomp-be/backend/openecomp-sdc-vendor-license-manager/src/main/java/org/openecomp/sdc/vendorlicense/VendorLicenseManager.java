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
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.Set;

public interface VendorLicenseManager {

  void checkout(String vendorLicenseModelId, String user);

  void undoCheckout(String vendorLicenseModelId, String user);

  void checkin(String vendorLicenseModelId, String user);

  void submit(String vendorLicenseModelId, String user);

  Collection<VersionedVendorLicenseModel> listVendorLicenseModels(String versionFilter,
                                                                  String user);

  VendorLicenseModelEntity createVendorLicenseModel(VendorLicenseModelEntity licenseModel,
                                                    String user);

  void updateVendorLicenseModel(VendorLicenseModelEntity licenseModel, String user);

  VersionedVendorLicenseModel getVendorLicenseModel(String vlmId, Version version, String user);

  void deleteVendorLicenseModel(String vlmId, String user);


  Collection<LicenseAgreementEntity> listLicenseAgreements(String vlmId, Version version,
                                                           String user);

  LicenseAgreementEntity createLicenseAgreement(LicenseAgreementEntity licenseAgreement,
                                                String user);

  void updateLicenseAgreement(LicenseAgreementEntity licenseAgreement,
                              Set<String> addedFeatureGroupIds, Set<String> removedFeatureGroupIds,
                              String user);

  LicenseAgreementModel getLicenseAgreementModel(String vlmId, Version version,
                                                 String licenseAgreementId, String user);

  void deleteLicenseAgreement(String vlmId, String licenseAgreementId, String user);


  Collection<org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity> listFeatureGroups(
      String vlmId, Version version, String user);

  org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity createFeatureGroup(
      org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity fg, String user);

  void updateFeatureGroup(org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity featureGroup,
                          Set<String> addedLicenseKeyGroups, Set<String> removedLicenseKeyGroups,
                          Set<String> addedEntitlementPools, Set<String> removedEntitlementPools,
                          String user);

  FeatureGroupModel getFeatureGroupModel(
      org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity featureGroup, String user);

  void deleteFeatureGroup(org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity featureGroup,
                          String user);


  Collection<EntitlementPoolEntity> listEntitlementPools(String vlmId, Version version,
                                                         String user);

  EntitlementPoolEntity createEntitlementPool(EntitlementPoolEntity entitlementPool, String user);

  void updateEntitlementPool(EntitlementPoolEntity entitlementPool, String user);

  EntitlementPoolEntity getEntitlementPool(EntitlementPoolEntity entitlementPool, String user);

  void deleteEntitlementPool(EntitlementPoolEntity entitlementPool, String user);


  Collection<LicenseKeyGroupEntity> listLicenseKeyGroups(String vlmId, Version version,
                                                         String user);

  LicenseKeyGroupEntity createLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup, String user);

  void updateLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup, String user);

  LicenseKeyGroupEntity getLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup, String user);

  void deleteLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup, String user);

}
