package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.generator.core.services.ManualVspToscaGenerationService;
import org.openecomp.sdc.generator.datatypes.tosca.DeploymentFlavorModel;
import org.openecomp.sdc.generator.datatypes.tosca.MultiFlavorVfcImage;
import org.openecomp.sdc.generator.datatypes.tosca.VspModelInfo;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.vendorsoftwareproduct.ManualVspToscaManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.ManualVspDataCollectionService;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ManualVspToscaManagerImpl implements ManualVspToscaManager {

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private ManualVspDataCollectionService
      manualVspDataCollectionService = new ManualVspDataCollectionService();

  @Override
  public VspModelInfo gatherVspInformation(String vspId, Version version) {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    VspModelInfo vspModelInfo = new VspModelInfo();
    //Get Release Vendor Name
    Optional<String> releaseVendor;
    try {
      releaseVendor = manualVspDataCollectionService.getReleaseVendor(vspId, version);
    } catch (Exception ex) {
      releaseVendor = Optional.empty();
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
          LoggerTragetServiceName.COLLECT_MANUAL_VSP_TOSCA_DATA, ErrorLevel.INFO.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Release Vendor not found : "
              + ex.getMessage());
    }
    releaseVendor.ifPresent(vspModelInfo::setReleaseVendor);

    //Get Allowed Deployment flavors information
    Map<String, DeploymentFlavorModel> allowedFlavors;
    try {
      allowedFlavors = manualVspDataCollectionService.getAllowedFlavors(vspId, version);
    } catch (Exception ex) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
          LoggerTragetServiceName.COLLECT_MANUAL_VSP_TOSCA_DATA, ErrorLevel.INFO.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Unable to collect allowed flavors : "
              + ex.getMessage());
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
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
          LoggerTragetServiceName.COLLECT_MANUAL_VSP_TOSCA_DATA, ErrorLevel.INFO.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Unable to collect vsp component images : "
              + ex.getMessage());
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
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
          LoggerTragetServiceName.COLLECT_MANUAL_VSP_TOSCA_DATA, ErrorLevel.INFO.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Unable to collect vsp components : "
              + ex.getMessage());
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
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
          LoggerTragetServiceName.COLLECT_MANUAL_VSP_TOSCA_DATA, ErrorLevel.INFO.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Unable to collect vsp component nics : "
              + ex.getMessage());
      vspComponentNics = null;
    }
    if (MapUtils.isNotEmpty(vspComponentNics)) {
      vspModelInfo.setNics(vspComponentNics);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return vspModelInfo;
  }

  @Override
  public ToscaServiceModel generateToscaModel(VspModelInfo vspModelInfo) {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    ManualVspToscaGenerationService vspToscaGenerator = new ManualVspToscaGenerationService();
    ToscaServiceModel manualVspToscaServiceModel =
        vspToscaGenerator.createManualVspToscaServiceModel(vspModelInfo);
    mdcDataDebugMessage.debugExitMessage(null, null);
    return manualVspToscaServiceModel;
  }
}
