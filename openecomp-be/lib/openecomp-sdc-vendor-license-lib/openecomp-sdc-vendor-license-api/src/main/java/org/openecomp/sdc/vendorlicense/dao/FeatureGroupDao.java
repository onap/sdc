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

package org.openecomp.sdc.vendorlicense.dao;

import org.openecomp.core.dao.BaseDao;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.versioning.dao.VersionableDao;

import java.util.Set;

public interface FeatureGroupDao extends VersionableDao, BaseDao<FeatureGroupEntity> {

  long count(FeatureGroupEntity featureGroup);

  void deleteAll(FeatureGroupEntity featureGroup);

  void updateFeatureGroup(FeatureGroupEntity entity, Set<String> addedEntitlementPools,
                          Set<String> removedEntitlementPools, Set<String> addedLicenseKeyGroups,
                          Set<String> removedLicenseKeyGroups);

  void addReferencingLicenseAgreement(FeatureGroupEntity featureGroup, String licenseAgreementId);

  void removeReferencingLicenseAgreement(FeatureGroupEntity featureGroup,
                                         String licenseAgreementId);

  void removeEntitlementPool(FeatureGroupEntity featureGroup, String entitlementPoolId);

  void removeLicenseKeyGroup(FeatureGroupEntity featureGroup, String licenseKeyGroupId);
}
