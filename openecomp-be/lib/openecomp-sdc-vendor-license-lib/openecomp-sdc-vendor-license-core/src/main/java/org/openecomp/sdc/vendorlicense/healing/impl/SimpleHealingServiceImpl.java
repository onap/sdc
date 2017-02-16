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

package org.openecomp.sdc.vendorlicense.healing.impl;

import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDao;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.healing.HealingService;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

public class SimpleHealingServiceImpl implements HealingService {
  private static final EntitlementPoolDao entitlementPoolDao =
      EntitlementPoolDaoFactory.getInstance().createInterface();
  private static final LicenseKeyGroupDao licenseKeyGroupDao =
      LicenseKeyGroupDaoFactory.getInstance().createInterface();

  @Override
  public VersionableEntity heal(VersionableEntity toHeal, String user) {
    return handleMissingVersionId(toHeal, user);
  }

  private VersionableEntity handleMissingVersionId(VersionableEntity toHeal, String user) {
    if (toHeal.getVersionUuId() != null) {
      return toHeal;
    }


    if (toHeal instanceof EntitlementPoolEntity) {
      toHeal.setVersionUuId(toHeal.getId());
      entitlementPoolDao.update((EntitlementPoolEntity) toHeal);
    } else if (toHeal instanceof LicenseKeyGroupEntity) {
      toHeal.setVersionUuId(toHeal.getId());
      licenseKeyGroupDao.update((LicenseKeyGroupEntity) toHeal);
    } else {
      throw new UnsupportedOperationException(
          "Unsupported operation for 1610 release/1607->1610 migration.");
      //todo maybe errorbuilder?
    }
    return toHeal;
  }

}
