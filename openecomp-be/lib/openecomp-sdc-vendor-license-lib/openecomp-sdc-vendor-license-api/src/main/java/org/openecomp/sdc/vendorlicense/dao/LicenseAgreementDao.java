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
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.versioning.dao.VersionableDao;

import java.util.Set;

public interface LicenseAgreementDao extends VersionableDao, BaseDao<LicenseAgreementEntity> {

  long count(LicenseAgreementEntity entity);

  void deleteAll(LicenseAgreementEntity entity);

  void removeFeatureGroup(LicenseAgreementEntity licenseAgreement, String featureGroupId);

  void updateColumnsAndDeltaFeatureGroupIds(LicenseAgreementEntity licenseAgreement,
                                            Set<String> addedFeatureGroupIds,
                                            Set<String> removedFeatureGroupIds);
}
