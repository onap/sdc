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

package org.openecomp.sdc.vendorlicense.licenseartifacts.impl;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.vendorlicense.HealingServiceFactory;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;
import org.openecomp.sdc.vendorlicense.healing.HealingService;
import org.openecomp.sdc.vendorlicense.licenseartifacts.VendorLicenseArtifactsService;
import org.openecomp.sdc.vendorlicense.licenseartifacts.impl.types.VendorLicenseArtifact;
import org.openecomp.sdc.vendorlicense.licenseartifacts.impl.types.VnfLicenseArtifact;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VendorLicenseArtifactsServiceImpl implements VendorLicenseArtifactsService {

  private static final VendorLicenseFacade vendorLicenseFacade = VendorLicenseFacadeFactory
          .getInstance().createInterface();
  private static final HealingService healingService = HealingServiceFactory
          .getInstance().createInterface();

  /**
   * Create License Artifacts.
   *
   * @param vspId         the vsp id
   * @param vlmId         the vlm id
   * @param vlmVersion    the vlm version
   * @param featureGroups the feature groups
   * @param user          the user
   * @return FileContentHandler
   */
  public FileContentHandler createLicenseArtifacts(String vspId, String vlmId,
                                                   Version vlmVersion,
                                                   List<String> featureGroups, String user) {
    FileContentHandler artifacts = new FileContentHandler();
    String vendorName = getVendorName(vlmId, user);

    artifacts.addFile(VendorLicenseConstants.VNF_ARTIFACT_NAME_WITH_PATH,
            createVnfArtifact(vspId, vlmId, vlmVersion, vendorName, featureGroups, user));
    artifacts.addFile(VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_NAME_WITH_PATH,
            createVendorLicenseArtifact(vlmId, vendorName, user));

    return artifacts;
  }

  static byte[] createVnfArtifact(
      String vspId, String vlmId, Version vlmVersion,
      String vendorName, List<String> featureGroups,
      String user) {
    VnfLicenseArtifact artifact = new VnfLicenseArtifact();

    artifact.setVspId(vspId);
    artifact.setVendorName(vendorName);
    for (String featureGroupId : featureGroups) {
      FeatureGroupModel featureGroupModel =
          vendorLicenseFacade.getFeatureGroupModel(new FeatureGroupEntity(
              vlmId, vlmVersion, featureGroupId), user);
      Set<EntitlementPoolEntity> entitlementPoolEntities = featureGroupModel.getEntitlementPools();
      Set<LicenseKeyGroupEntity> licenseKeyGroupEntities = featureGroupModel.getLicenseKeyGroups();

      featureGroupModel.setEntitlementPools(
          entitlementPoolEntities.stream()
              .map(entitlementPoolEntity -> (EntitlementPoolEntity) healingService
                  .heal(entitlementPoolEntity, user))
              .collect(Collectors.toSet()));
      featureGroupModel.setLicenseKeyGroups(
          licenseKeyGroupEntities.stream()
              .map(licenseKeyGroupEntity -> (LicenseKeyGroupEntity) healingService
                  .heal(licenseKeyGroupEntity, user))
              .collect(Collectors.toSet()));
      artifact.getFeatureGroups().add(featureGroupModel);
    }

    return artifact.toXml().getBytes();
  }


  static byte[] createVendorLicenseArtifact(String vlmId, String vendorName, String user) {
    VendorLicenseArtifact vendorLicenseArtifact = new VendorLicenseArtifact();
    vendorLicenseArtifact.setVendorName(vendorName);
    Set<EntitlementPoolEntity> entitlementPoolEntities = new HashSet<>();
    Set<LicenseKeyGroupEntity> licenseKeyGroupEntities = new HashSet<>();

    List<Version> finalVersions = getFinalVersionsForVlm(vlmId);
    for (Version finalVersion : finalVersions) {
      entitlementPoolEntities.addAll(
          vendorLicenseFacade.listEntitlementPools(vlmId, finalVersion, user));
      licenseKeyGroupEntities.addAll(
          vendorLicenseFacade.listLicenseKeyGroups(vlmId, finalVersion, user));
    }


    entitlementPoolEntities = healEPs(user, filterChangedEntities(entitlementPoolEntities));
    licenseKeyGroupEntities = healLkgs(user, filterChangedEntities(licenseKeyGroupEntities));

    vendorLicenseArtifact.setEntitlementPoolEntities(entitlementPoolEntities);
    vendorLicenseArtifact.setLicenseKeyGroupEntities(licenseKeyGroupEntities);
    return vendorLicenseArtifact.toXml().getBytes();
  }

  private static List<VersionableEntity> filterChangedEntities(
      Collection<? extends VersionableEntity> versionableEntities) {
    MultiValuedMap<String, VersionableEntity> entitiesById = mapById(versionableEntities);
    Map<String, VersionableEntity> entitiesByVersionUuId = new HashMap<>();
    List<VersionableEntity> changedOnly = new ArrayList<>();

    for (String epId : entitiesById.keySet()) {
      Collection<VersionableEntity> versionableEntitiesForId = entitiesById.get(epId);
      for (VersionableEntity ep : versionableEntitiesForId) {
        entitiesByVersionUuId.put(ep.getVersionUuId(), ep);
      }
    }

    changedOnly.addAll(entitiesByVersionUuId.values());

    return changedOnly;
  }

  private static MultiValuedMap<String, VersionableEntity> mapById(
      Collection<? extends VersionableEntity> versionableEntities) {
    MultiValuedMap<String, VersionableEntity> mappedById = new ArrayListValuedHashMap<>();
    for (VersionableEntity ve : versionableEntities) {
      mappedById.put(ve.getId(), ve);
    }
    return mappedById;
  }


  private static Set<LicenseKeyGroupEntity> healLkgs(
      String user, Collection<? extends VersionableEntity> licenseKeyGroupEntities) {
    Set<LicenseKeyGroupEntity> healed = new HashSet<>();
    for (VersionableEntity licenseKeyGroupEntity : licenseKeyGroupEntities) {
      healed.add((LicenseKeyGroupEntity) healingService.heal(licenseKeyGroupEntity, user));
    }

    return healed;
  }

  private static Set<EntitlementPoolEntity> healEPs(
      String user, Collection<? extends VersionableEntity> entitlementPoolEntities) {
    Set<EntitlementPoolEntity> healed = new HashSet<>();
    for (VersionableEntity entitlementPoolEntity : entitlementPoolEntities) {
      healed.add((EntitlementPoolEntity) healingService.heal(entitlementPoolEntity, user));
    }

    return healed;
  }

  private static List<Version> getFinalVersionsForVlm(String vlmId) {
    VersionInfo versionInfo = vendorLicenseFacade
        .getVersionInfo(vlmId, VersionableEntityAction.Read, "");
    return versionInfo.getFinalVersions();

  }


  private static String getVendorName(String vendorLicenseModelId, String user) {
    return vendorLicenseFacade
        .getVendorLicenseModel(vendorLicenseModelId, null, user)
        .getVendorLicenseModel().getVendorName();
  }

}
