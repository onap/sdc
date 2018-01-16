/*
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

package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.generator.core.services.ManualVspToscaGenerationService;
import org.openecomp.sdc.generator.datatypes.tosca.DeploymentFlavorModel;
import org.openecomp.sdc.generator.datatypes.tosca.MultiFlavorVfcImage;
import org.openecomp.sdc.generator.datatypes.tosca.VspModelInfo;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.vendorsoftwareproduct.ManualVspToscaManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.ManualVspDataCollectionService;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ManualVspToscaManagerImpl implements ManualVspToscaManager {

  private final ManualVspDataCollectionService
      manualVspDataCollectionService = new ManualVspDataCollectionService();

  @Override
  public VspModelInfo gatherVspInformation(String vspId, Version version) {
    VspModelInfo vspModelInfo = new VspModelInfo();
    //Get Release Vendor Name
    Optional<String> releaseVendor;
    try {
      releaseVendor = manualVspDataCollectionService.getReleaseVendor(vspId, version);
    } catch (Exception ex) {
      releaseVendor = Optional.empty();
    }
    releaseVendor.ifPresent(vspModelInfo::setReleaseVendor);

    //Get Allowed Deployment flavors information
    Map<String, DeploymentFlavorModel> allowedFlavors;
    try {
      allowedFlavors = manualVspDataCollectionService.getAllowedFlavors(vspId, version);
    } catch (Exception ex) {
      allowedFlavors = null;
    }
    if (MapUtils.isNotEmpty(allowedFlavors)) {
      vspModelInfo.setAllowedFlavors(allowedFlavors);
    }

    //Get VFC Image information
    Map<String, List<MultiFlavorVfcImage>> vspComponentImages;
    try {
      vspComponentImages =
          manualVspDataCollectionService.getVspComponentImages(vspId, version);
    } catch (Exception ex) {
      vspComponentImages = null;
    }
    if (MapUtils.isNotEmpty(vspComponentImages)) {
      vspModelInfo.setMultiFlavorVfcImages(vspComponentImages);
    }

    //Get VFC component information
    Map<String, String> vspComponents;
    try {
      vspComponents = manualVspDataCollectionService.getVspComponents(vspId, version);
    } catch (Exception ex) {
      vspComponents = null;
    }
    if (MapUtils.isNotEmpty(vspComponents)) {
      vspModelInfo.setComponents(vspComponents);
    }

    //Get VSP component nic information
    Map<String, List<Nic>> vspComponentNics;
    try {
      vspComponentNics = manualVspDataCollectionService.getVspComponentNics(vspId, version);
    } catch (Exception ex) {
      vspComponentNics = null;
    }
    if (MapUtils.isNotEmpty(vspComponentNics)) {
      vspModelInfo.setNics(vspComponentNics);
    }
    return vspModelInfo;
  }

  @Override
  public ToscaServiceModel generateToscaModel(VspModelInfo vspModelInfo) {
    ManualVspToscaGenerationService vspToscaGenerator = new ManualVspToscaGenerationService();
    return vspToscaGenerator.createManualVspToscaServiceModel(vspModelInfo);
  }
}
