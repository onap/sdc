/*-
* Copyright © 2016-2017 European Support Limited
*
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
*/

package org.openecomp.sdc.vendorlicense.dao;

import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.versioning.dao.VersionableDao;

import java.util.Collection;
import java.util.Set;

public interface LicenseAgreementDao extends VersionableDao{

  long count(LicenseAgreementEntity entity);

  void deleteAll(LicenseAgreementEntity entity);

  void removeFeatureGroup(LicenseAgreementEntity licenseAgreement, String featureGroupId);

  void updateColumnsAndDeltaFeatureGroupIds(LicenseAgreementEntity licenseAgreement,
                                            Set<String> addedFeatureGroupIds,
                                            Set<String> removedFeatureGroupIds);
  Collection<LicenseAgreementEntity> list(LicenseAgreementEntity entity);

  void create(LicenseAgreementEntity entity);

  void update(LicenseAgreementEntity entity);

  LicenseAgreementEntity get(LicenseAgreementEntity entity);

  void delete(LicenseAgreementEntity entity);
}
