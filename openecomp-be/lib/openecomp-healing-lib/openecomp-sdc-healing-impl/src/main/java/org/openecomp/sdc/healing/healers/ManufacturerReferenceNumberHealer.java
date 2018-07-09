/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.healing.healers;

import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDao;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class ManufacturerReferenceNumberHealer implements Healer {

  private static final String manufacturerReferenceNumber = "MRN";
  private VendorLicenseFacade vendorLicenseFacade = VendorLicenseFacadeFactory.getInstance()
      .createInterface();
  private static final FeatureGroupDao featureGroupDao =
      FeatureGroupDaoFactory.getInstance().createInterface();

  @Override
  public boolean isHealingNeeded(String itemId, Version version) {
    return Objects.isNull(vendorLicenseFacade.listEntitlementPools
        (itemId, version).iterator().next().getManufacturerReferenceNumber()) ? true :
        false;
  }

  @Override
  public void heal(String itemId, Version version) throws Exception {

    healEntitlementPools(itemId, version);
    healLicenseKeyGroups(itemId, version);
    healFeatureGroups(itemId, version);
  }

  private void healEntitlementPools(String itemId, Version version) {
    Collection<EntitlementPoolEntity> entitlementPoolEntities = vendorLicenseFacade
        .listEntitlementPools(itemId, version);

    for (EntitlementPoolEntity entitlementPoolEntity : entitlementPoolEntities) {
      Set<String> referencingFeatureGroup = entitlementPoolEntity.getReferencingFeatureGroups();

      if (referencingFeatureGroup.size() == 1) {
        entitlementPoolEntity.setManufacturerReferenceNumber(getMRN(itemId, version,
            referencingFeatureGroup));
      } else {
        entitlementPoolEntity.setManufacturerReferenceNumber(manufacturerReferenceNumber);
      }
      vendorLicenseFacade.updateEntitlementPool(entitlementPoolEntity);
    }
  }

  private void healLicenseKeyGroups(String itemId, Version version) {
    Collection<LicenseKeyGroupEntity> licenseKeyGroupEntities = vendorLicenseFacade
        .listLicenseKeyGroups(itemId, version);

    for (LicenseKeyGroupEntity licenseKeyGroupEntity : licenseKeyGroupEntities) {
      Set<String> referencingFeatureGroup = licenseKeyGroupEntity.getReferencingFeatureGroups();
      if (referencingFeatureGroup.size() == 1) {
        licenseKeyGroupEntity.setManufacturerReferenceNumber(getMRN(itemId, version,
            referencingFeatureGroup));
      } else {
        licenseKeyGroupEntity.setManufacturerReferenceNumber(manufacturerReferenceNumber);
      }
      vendorLicenseFacade.updateLicenseKeyGroup(licenseKeyGroupEntity);
    }
  }

  private String getMRN(String itemId, Version
      version, Set<String> referencingFeatureGroup) {
    FeatureGroupEntity featureGroupEntity = vendorLicenseFacade.getFeatureGroup(new
        FeatureGroupEntity(itemId, version, referencingFeatureGroup.iterator().next()));
    return featureGroupEntity.getManufacturerReferenceNumber();
  }

  private void healFeatureGroups(String itemId, Version version) {

    Collection<FeatureGroupEntity> featureGroupEntities = vendorLicenseFacade.listFeatureGroups
        (itemId, version);

    for (FeatureGroupEntity featureGroupEntity : featureGroupEntities) {
      featureGroupEntity.setManufacturerReferenceNumber("");
      featureGroupDao.update(featureGroupEntity);
    }
  }
}
