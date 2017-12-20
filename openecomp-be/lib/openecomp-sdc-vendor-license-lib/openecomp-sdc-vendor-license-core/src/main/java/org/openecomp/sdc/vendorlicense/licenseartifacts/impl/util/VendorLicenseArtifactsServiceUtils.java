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
import org.openecomp.sdc.versioning.ItemManagerFactory;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
  private static MultiValuedMap<String, VersionableEntity> mapById(
      Collection<? extends VersionableEntity> versionableEntities) {
    MultiValuedMap<String, VersionableEntity> mappedById = new ArrayListValuedHashMap<>();
    for (VersionableEntity ve : versionableEntities) {
      mappedById.put(ve.getId(), ve);
    }
    return mappedById;
  }

  /**
   * For all entities with same id, only entities that differ from one another will be returned.
   * If no change has occured, the entity with the earlier VLM version will be returned.
   * If only one version of said entities exists it will be returned
   *
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

  public static Set<LicenseKeyGroupEntity> healLkgs(
      Collection<? extends VersionableEntity> licenseKeyGroupEntities) {
    Set<LicenseKeyGroupEntity> healed = new HashSet<>();
    for (VersionableEntity licenseKeyGroupEntity : licenseKeyGroupEntities) {
      healed.add((LicenseKeyGroupEntity) VendorLicenseArtifactsServiceImpl.healingService
          .heal(licenseKeyGroupEntity));
    }

    return healed;
  }

  public static Set<EntitlementPoolEntity> healEPs(
      Collection<? extends VersionableEntity> entitlementPoolEntities) {
    Set<EntitlementPoolEntity> healed = new HashSet<>();
    for (VersionableEntity entitlementPoolEntity : entitlementPoolEntities) {
      healed.add((EntitlementPoolEntity) VendorLicenseArtifactsServiceImpl.healingService
          .heal(entitlementPoolEntity));
    }

    return healed;
  }

  public static List<Version> getFinalVersionsForVlm(String vlmId) {
    VersioningManager versioningManager = VersioningManagerFactory.getInstance().createInterface();
    return versioningManager.list(vlmId).stream()
        .filter(version -> VersionStatus.Certified == version.getStatus())
        .map(certifiedVersion -> versioningManager.get(vlmId, certifiedVersion)) //sync to private
        .collect(Collectors.toList());
  }

  public static String getVendorName(String vendorLicenseModelId) {
    return ItemManagerFactory.getInstance().createInterface().get(vendorLicenseModelId).getName();
  }


  /**
   * Written to handle the consequences of ATTASDC-4780 where version_uuid was not saved or
   * retrieved correctly by DAO for EPs and LKGs. Performs a healing of sorts according to the
   * following : 1. all versions of a specific entity (EP or LKG that have the same invariant_uuid)
   * are ordered by their VLM version 2. first element is sent to healing (which will set a
   * versionUUID for it IF it doesnt exist) 3. each subsequent element is compared to previous . If
   * same, UUID is copied from the previous element , if they differ - the current element is sent
   * to healing as before. For VLMs created post-bugfix this code should not update any element
   */
  public static Collection<? extends VersionableEntity> prepareForFiltering(Collection<? extends
      VersionableEntity> versionableEntities, boolean isEP) {
    MultiValuedMap<String, VersionableEntity> entitiesById = mapById(
        versionableEntities);

    for (String epId : entitiesById.keySet()) {
      List<VersionableEntity> versionableEntitiesForId = new ArrayList<>();
      versionableEntitiesForId.addAll(entitiesById.get(epId));
      versionableEntitiesForId.sort(new VersionableEntitySortByVlmMajorVersion());
      healingService.heal(versionableEntitiesForId.get(0));
      for (int i = 1; i < versionableEntitiesForId.size(); i++) {
        if (isEP) {
          EntitlementPoolEntity current = (EntitlementPoolEntity) versionableEntitiesForId.get(i);
          EntitlementPoolEntity previous = (EntitlementPoolEntity) versionableEntitiesForId
              .get(i - 1);
          if (current.equals(previous) && current.getVersionUuId() == null) {
            current.setVersionUuId(previous.getVersionUuId());
            healingService.persistNoHealing(current);
          } else {
            versionableEntitiesForId.set(i, healingService.heal(versionableEntitiesForId.get(i)));
          }

        } else {
          LicenseKeyGroupEntity current = (LicenseKeyGroupEntity) versionableEntitiesForId.get(i);
          LicenseKeyGroupEntity previous = (LicenseKeyGroupEntity) versionableEntitiesForId
              .get(i - 1);
          if (current.equals(previous) && current.getVersionUuId() == null) {
            current.setVersionUuId(previous.getVersionUuId());
            healingService.persistNoHealing(current);
          } else {
            versionableEntitiesForId.set(i, healingService.heal(versionableEntitiesForId.get(i)));
          }


        }
      }
    }
    return versionableEntities;
  }


}
