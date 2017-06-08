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

package org.openecomp.sdc.vendorlicense.licenseartifacts.impl.util;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.openecomp.sdc.vendorlicense.HealingServiceFactory;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.healing.HealingService;
import org.openecomp.sdc.vendorlicense.licenseartifacts.impl.VendorLicenseArtifactsServiceImpl;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author katyr
 * @since January 10, 2017
 */

public class VendorLicenseArtifactsServiceUtils {
  private static final HealingService healingService =
      HealingServiceFactory.getInstance().createInterface();

  /**
   * maps the entities by id
   *
   * @return a Map of id -> list of versionable entities with that id
   */
  static MultiValuedMap<String, VersionableEntity> mapById(
      Collection<? extends VersionableEntity> versionableEntities) {
    MultiValuedMap<String, VersionableEntity> mappedById = new ArrayListValuedHashMap<>();
    for (VersionableEntity ve : versionableEntities) {
      mappedById.put(ve.getId(), ve);
    }
    return mappedById;
  }

  /**
   *  For all entities with same id, only entities that differ from one another will be returned.
   *  If no change has occured, the entity with the earlier VLM version will be returned.
   *  If only one version of said entitity exists it will be returned
   * @param versionableEntities
   * @return a list of entities that has been changed
   */
  public static List<VersionableEntity> filterChangedEntities(
      Collection<? extends VersionableEntity> versionableEntities) {
    MultiValuedMap<String, VersionableEntity> entitiesById = mapById(
        versionableEntities);
    MultiValuedMap<String, VersionableEntity> entitiesByVersionUuId =
        new ArrayListValuedHashMap<>();
    List<VersionableEntity> changedOnly = new ArrayList<>();

    for (String epId : entitiesById.keySet()) {
      Collection<VersionableEntity> versionableEntitiesForId = entitiesById.get(epId);
      for (VersionableEntity ep : versionableEntitiesForId) {
        entitiesByVersionUuId.put(ep.getVersionUuId(), ep);
      }
    }

    //for every list of eps which have the same uuid, get the one with the earliest vlm version.
    for (String versionUid : entitiesByVersionUuId.keySet()) {
      List<VersionableEntity> versionableEntitiesForUuid =
          (List<VersionableEntity>) entitiesByVersionUuId.get(versionUid);
      versionableEntitiesForUuid.sort(new VersionableEntitySortByVlmMajorVersion());
      changedOnly.add(versionableEntitiesForUuid.get(0));
    }

    return changedOnly;
  }

  public static Set<LicenseKeyGroupEntity> healLkgs(String user,
                                                    Collection<? extends VersionableEntity> licenseKeyGroupEntities) {
    Set<LicenseKeyGroupEntity> healed = new HashSet<>();
    for (VersionableEntity licenseKeyGroupEntity : licenseKeyGroupEntities) {
      healed.add((LicenseKeyGroupEntity) VendorLicenseArtifactsServiceImpl.healingService
          .heal(licenseKeyGroupEntity, user));
    }

    return healed;
  }

  public static Set<EntitlementPoolEntity> healEPs(String user,
                                                   Collection<? extends VersionableEntity> entitlementPoolEntities) {
    Set<EntitlementPoolEntity> healed = new HashSet<>();
    for (VersionableEntity entitlementPoolEntity : entitlementPoolEntities) {
      healed.add((EntitlementPoolEntity) VendorLicenseArtifactsServiceImpl.healingService
          .heal(entitlementPoolEntity, user));
    }

    return healed;
  }

  public static List<Version> getFinalVersionsForVlm(String vlmId) {
    VersionInfo versionInfo =
        VendorLicenseArtifactsServiceImpl.vendorLicenseFacade
            .getVersionInfo(vlmId, VersionableEntityAction.Read, "");
    return versionInfo.getFinalVersions();

  }

  public static String getVendorName(String vendorLicenseModelId, String user) {
    return VendorLicenseArtifactsServiceImpl.vendorLicenseFacade
        .getVendorLicenseModel(vendorLicenseModelId, null, user)
        .getVendorLicenseModel().getVendorName();
  }
}
