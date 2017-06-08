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
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.versioning.dao.VersionableDao;

import java.util.Collection;


public interface LicenseKeyGroupDao extends VersionableDao, BaseDao<LicenseKeyGroupEntity> {

  void create(LicenseKeyGroupEntity licenseKeyGroup);

  void delete(LicenseKeyGroupEntity licenseKeyGroup);

  LicenseKeyGroupEntity get(LicenseKeyGroupEntity licenseKeyGroup);

  Collection<LicenseKeyGroupEntity> list(LicenseKeyGroupEntity licenseKeyGroup);

  long count(LicenseKeyGroupEntity licenseKeyGroup);

  void deleteAll(LicenseKeyGroupEntity licenseKeyGroup);

  void addReferencingFeatureGroup(LicenseKeyGroupEntity licenseKeyGroup, String featureGroupId);

  void removeReferencingFeatureGroup(LicenseKeyGroupEntity licenseKeyGroup, String featureGroupId);
}
