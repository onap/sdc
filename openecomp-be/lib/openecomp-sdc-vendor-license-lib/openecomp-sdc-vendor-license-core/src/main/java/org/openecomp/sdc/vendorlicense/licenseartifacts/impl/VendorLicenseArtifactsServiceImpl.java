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

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.utils.CommonUtil;
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
import org.openecomp.sdc.vendorlicense.licenseartifacts.impl.util.VendorLicenseArtifactsServiceUtils;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VENDOR_LICENSE_MODEL_ARTIFACT_NAME_WITH_PATH;
import static org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VNF_ARTIFACT_NAME_WITH_PATH;

public class VendorLicenseArtifactsServiceImpl implements VendorLicenseArtifactsService {

  public static final VendorLicenseFacade vendorLicenseFacade =
      VendorLicenseFacadeFactory.getInstance().createInterface();
  public static final HealingService healingService =
      HealingServiceFactory.getInstance().createInterface();
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();


  static byte[] createVnfArtifact(String vspId, String vlmId, Version vlmVersion, String vendorName,
                                  List<String> featureGroups, String user) {


    mdcDataDebugMessage.debugEntryMessage("VLM name", vendorName);

    VnfLicenseArtifact artifact = new VnfLicenseArtifact();

    artifact.setVspId(vspId);
    artifact.setVendorName(vendorName);
    for (String featureGroupId : featureGroups) {
      FeatureGroupModel featureGroupModel = vendorLicenseFacade
          .getFeatureGroupModel(new FeatureGroupEntity(vlmId, vlmVersion, featureGroupId), user);
      Set<EntitlementPoolEntity> entitlementPoolEntities = featureGroupModel.getEntitlementPools();
      Set<LicenseKeyGroupEntity> licenseKeyGroupEntities = featureGroupModel.getLicenseKeyGroups();

      featureGroupModel.setEntitlementPools(entitlementPoolEntities.stream().map(
          entitlementPoolEntity -> (EntitlementPoolEntity) healingService
              .heal(entitlementPoolEntity, user)).collect(Collectors.toSet()));
      featureGroupModel.setLicenseKeyGroups(licenseKeyGroupEntities.stream().map(
          licenseKeyGroupEntity -> (LicenseKeyGroupEntity) healingService
              .heal(licenseKeyGroupEntity, user)).collect(Collectors.toSet()));
      artifact.getFeatureGroups().add(featureGroupModel);
    }

    mdcDataDebugMessage.debugExitMessage("VLM name", vendorName);
    return artifact.toXml().getBytes();
  }

  static byte[] createVendorLicenseArtifact(String vlmId, String vendorName, String user) {


    mdcDataDebugMessage.debugEntryMessage("VLM name", vendorName);

    VendorLicenseArtifact vendorLicenseArtifact = new VendorLicenseArtifact();
    vendorLicenseArtifact.setVendorName(vendorName);
    Set<EntitlementPoolEntity> entitlementPoolEntities = new HashSet<>();
    Set<LicenseKeyGroupEntity> licenseKeyGroupEntities = new HashSet<>();

    List<Version> finalVersions = VendorLicenseArtifactsServiceUtils.getFinalVersionsForVlm(vlmId);
    for (Version finalVersion : finalVersions) {
      entitlementPoolEntities
          .addAll(vendorLicenseFacade.listEntitlementPools(vlmId, finalVersion, user));
      licenseKeyGroupEntities
          .addAll(vendorLicenseFacade.listLicenseKeyGroups(vlmId, finalVersion, user));
    }


    entitlementPoolEntities = VendorLicenseArtifactsServiceUtils
        .healEPs(user,
            VendorLicenseArtifactsServiceUtils.filterChangedEntities(entitlementPoolEntities));
    licenseKeyGroupEntities = VendorLicenseArtifactsServiceUtils
        .healLkgs(user,
            VendorLicenseArtifactsServiceUtils.filterChangedEntities(licenseKeyGroupEntities));

    vendorLicenseArtifact.setEntitlementPoolEntities(entitlementPoolEntities);
    vendorLicenseArtifact.setLicenseKeyGroupEntities(licenseKeyGroupEntities);

    mdcDataDebugMessage.debugExitMessage("VLM name", vendorName);
    return vendorLicenseArtifact.toXml().getBytes();
  }


  /**
   * Create License Artifacts.
   * @param vspId vspId
   * @param vlmId vlmId
   * @param vlmVersion vlmVersion
   * @param featureGroups featureGroups
   * @param user user
   * @return FileContentHandler
   */
  public FileContentHandler createLicenseArtifacts(String vspId, String vlmId, Version vlmVersion,
                                                   List<String> featureGroups, String user) {


    mdcDataDebugMessage.debugEntryMessage("VSP Id", vspId);

    FileContentHandler artifacts = new FileContentHandler();
    String vendorName = VendorLicenseArtifactsServiceUtils.getVendorName(vlmId, user);

    artifacts.addFile(VNF_ARTIFACT_NAME_WITH_PATH,
        createVnfArtifact(vspId, vlmId, vlmVersion, vendorName, featureGroups, user));
    artifacts.addFile(VENDOR_LICENSE_MODEL_ARTIFACT_NAME_WITH_PATH,
        createVendorLicenseArtifact(vlmId, vendorName, user));

    mdcDataDebugMessage.debugExitMessage("VSP Id", vspId);

    return artifacts;
  }

}
