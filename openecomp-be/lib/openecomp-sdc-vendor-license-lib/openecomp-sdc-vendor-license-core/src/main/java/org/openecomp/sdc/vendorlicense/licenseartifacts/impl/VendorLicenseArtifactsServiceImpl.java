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

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorlicense.HealingServiceFactory;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_NAME_WITH_PATH;
import static org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VNF_ARTIFACT_NAME_WITH_PATH;
import static org.openecomp.sdc.vendorlicense.licenseartifacts.impl.util.VendorLicenseArtifactsServiceUtils.filterChangedEntities;
import static org.openecomp.sdc.vendorlicense.licenseartifacts.impl.util.VendorLicenseArtifactsServiceUtils.getFinalVersionsForVlm;
import static org.openecomp.sdc.vendorlicense.licenseartifacts.impl.util.VendorLicenseArtifactsServiceUtils.getVendorName;
import static org.openecomp.sdc.vendorlicense.licenseartifacts.impl.util.VendorLicenseArtifactsServiceUtils.healEPs;
import static org.openecomp.sdc.vendorlicense.licenseartifacts.impl.util.VendorLicenseArtifactsServiceUtils.healLkgs;
import static org.openecomp.sdc.vendorlicense.licenseartifacts.impl.util.VendorLicenseArtifactsServiceUtils.prepareForFiltering;

public class VendorLicenseArtifactsServiceImpl implements VendorLicenseArtifactsService {

  public static final VendorLicenseFacade vendorLicenseFacade =
      VendorLicenseFacadeFactory.getInstance().createInterface();
  public static final HealingService healingService =
      HealingServiceFactory.getInstance().createInterface();
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();


  private static byte[] createVnfArtifact(String vspId, String vlmId, Version vlmVersion, String vendorName,
                                  List<String> featureGroups) {


    mdcDataDebugMessage.debugEntryMessage("VLM name", vendorName);

    VnfLicenseArtifact artifact = new VnfLicenseArtifact();

    artifact.setVspId(vspId);
    artifact.setVendorName(vendorName);
    if (featureGroups != null) {
      for (String featureGroupId : featureGroups) {
        FeatureGroupModel featureGroupModel = vendorLicenseFacade
            .getFeatureGroupModel(new FeatureGroupEntity(vlmId, vlmVersion, featureGroupId));
        Set<EntitlementPoolEntity> entitlementPoolEntities =
            featureGroupModel.getEntitlementPools();
        for (EntitlementPoolEntity entitlementPoolEntity : entitlementPoolEntities) {
          entitlementPoolEntity.setLimits(vendorLicenseFacade.listLimits(vlmId, vlmVersion,
              entitlementPoolEntity.getId()));
          entitlementPoolEntity.setManufacturerReferenceNumber(featureGroupModel.
              getEntityManufacturerReferenceNumber());
        }

        Set<LicenseKeyGroupEntity> licenseKeyGroupEntities =
            featureGroupModel.getLicenseKeyGroups();
        for (LicenseKeyGroupEntity licenseKeyGroupEntity : licenseKeyGroupEntities) {
          licenseKeyGroupEntity.setLimits(vendorLicenseFacade.listLimits(vlmId, vlmVersion,
              licenseKeyGroupEntity.getId()));
          licenseKeyGroupEntity.setManufacturerReferenceNumber(featureGroupModel.
              getEntityManufacturerReferenceNumber());
        }

        featureGroupModel.setEntitlementPools(entitlementPoolEntities.stream().map(
            entitlementPoolEntity -> (EntitlementPoolEntity) healingService
                .heal(entitlementPoolEntity)).collect(Collectors.toSet()));
        featureGroupModel.setLicenseKeyGroups(licenseKeyGroupEntities.stream().map(
            licenseKeyGroupEntity -> (LicenseKeyGroupEntity) healingService
                .heal(licenseKeyGroupEntity)).collect(Collectors.toSet()));
        artifact.getFeatureGroups().add(featureGroupModel);
      }
    }

    mdcDataDebugMessage.debugExitMessage("VLM name", vendorName);
    return artifact.toXml().getBytes();
  }

  private static byte[] createVendorLicenseArtifact(String vlmId, String vendorName) {


    mdcDataDebugMessage.debugEntryMessage("VLM name", vendorName);

    VendorLicenseArtifact vendorLicenseArtifact = new VendorLicenseArtifact();
    vendorLicenseArtifact.setVendorName(vendorName);
    Set<EntitlementPoolEntity> entitlementPoolEntities = new HashSet<>();
    Set<LicenseKeyGroupEntity> licenseKeyGroupEntities = new HashSet<>();

    List<Version> finalVersions = getFinalVersionsForVlm(vlmId);
    for (Version finalVersion : finalVersions) {
      Collection<EntitlementPoolEntity> eps =
          vendorLicenseFacade.listEntitlementPools(vlmId, finalVersion);
      eps.forEach(entitlementPoolEntity -> {
        entitlementPoolEntity.setLimits(vendorLicenseFacade.listLimits(vlmId, finalVersion,
            entitlementPoolEntity.getId()));
        Optional<String> manufacturerReferenceNumber = getFeatureGroupManufactureRefNumber
            (entitlementPoolEntity.getReferencingFeatureGroups(), vlmId, finalVersion);
        manufacturerReferenceNumber
            .ifPresent(entitlementPoolEntity::setManufacturerReferenceNumber);
      });
      entitlementPoolEntities.addAll(eps);

      Collection<LicenseKeyGroupEntity> lkgs =
          vendorLicenseFacade.listLicenseKeyGroups(vlmId, finalVersion);

      lkgs.forEach(licenseKeyGroupEntity -> {
        licenseKeyGroupEntity.setLimits(vendorLicenseFacade.listLimits(vlmId, finalVersion,
            licenseKeyGroupEntity.getId()));
        Optional<String> manufacturerReferenceNumber = getFeatureGroupManufactureRefNumber
            (licenseKeyGroupEntity.getReferencingFeatureGroups(), vlmId, finalVersion);
        manufacturerReferenceNumber
            .ifPresent(licenseKeyGroupEntity::setManufacturerReferenceNumber);
      });
      licenseKeyGroupEntities.addAll(lkgs);
    }


    entitlementPoolEntities =
        healEPs(filterChangedEntities(prepareForFiltering(entitlementPoolEntities, true)));
    licenseKeyGroupEntities =
        healLkgs(filterChangedEntities(prepareForFiltering(licenseKeyGroupEntities, false)));
    vendorLicenseArtifact.setEntitlementPoolEntities(entitlementPoolEntities);
    vendorLicenseArtifact.setLicenseKeyGroupEntities(licenseKeyGroupEntities);

    mdcDataDebugMessage.debugExitMessage("VLM name", vendorName);
    return vendorLicenseArtifact.toXml().getBytes();
  }

  private static Optional<String> getFeatureGroupManufactureRefNumber(Set<String> featureGroupIds,
                                                                      String vlmId,
                                                                      Version finalVersion) {
    String manufactureReferenceNumber = null;
    if (CollectionUtils.isNotEmpty(featureGroupIds)) {
      Object[] featureGroupIdsList = featureGroupIds.toArray();
      if (featureGroupIdsList.length > 0) {
        FeatureGroupEntity featureGroup =
            vendorLicenseFacade.getFeatureGroup(new FeatureGroupEntity(vlmId, finalVersion,
                featureGroupIdsList[0].toString()));
        manufactureReferenceNumber = featureGroup != null ? featureGroup
            .getManufacturerReferenceNumber() : null;
      }
    }
    return manufactureReferenceNumber != null ? Optional.of(manufactureReferenceNumber) :
        Optional.empty();
  }


  /**
   * Create License Artifacts.
   *
   * @param vspId         vspId
   * @param vlmId         vlmId
   * @param vlmVersion    vlmVersion
   * @param featureGroups featureGroups
   * @return FileContentHandler
   */
  public FileContentHandler createLicenseArtifacts(String vspId, String vlmId, Version vlmVersion,
                                                   List<String> featureGroups) {


    mdcDataDebugMessage.debugEntryMessage("VSP Id", vspId);

    FileContentHandler artifacts = new FileContentHandler();
    String vendorName = getVendorName(vlmId);

    artifacts.addFile(VNF_ARTIFACT_NAME_WITH_PATH,
        createVnfArtifact(vspId, vlmId, vlmVersion, vendorName, featureGroups));
    artifacts.addFile(VENDOR_LICENSE_MODEL_ARTIFACT_NAME_WITH_PATH,
        createVendorLicenseArtifact(vlmId, vendorName));

    mdcDataDebugMessage.debugExitMessage("VSP Id", vspId);

    return artifacts;
  }

}
