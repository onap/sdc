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

package org.openecomp.sdc.validation.impl.validators;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.heat.services.manifest.ManifestUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.util.ValidationUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SharedResourceGuideLineValidator implements Validator {
  private static final MdcDataDebugMessage MDC_DATA_DEBUG_MESSAGE = new MdcDataDebugMessage();
  private static final Logger LOGGER = LoggerFactory.getLogger(
          SharedResourceGuideLineValidator.class);
  private static final ErrorMessageCode ERROR_CODE_SRG_1 = new ErrorMessageCode("SRG1");
  private static final ErrorMessageCode ERROR_CODE_SRG_2 = new ErrorMessageCode("SRG2");
  private static final ErrorMessageCode ERROR_CODE_SRG_3 = new ErrorMessageCode("SRG3");
  private static final ErrorMessageCode ERROR_CODE_SRG_4 = new ErrorMessageCode("SRG4");
  private static final ErrorMessageCode ERROR_CODE_SRG_5 = new ErrorMessageCode("SRG5");
  private static final ErrorMessageCode ERROR_CODE_SRG_6 = new ErrorMessageCode("SRG6");

  @Override
  public void validate(GlobalValidationContext globalContext) {
    ManifestContent manifestContent;
    try {
      manifestContent = ValidationUtil.validateManifest(globalContext);
    } catch (Exception exception) {
      LOGGER.error("Fialed to check  Validation PreCondition ",exception);
      return;
    }

    Set<String> baseFiles = validateManifest(manifestContent, globalContext);

    Map<String, FileData.Type> fileTypeMap = ManifestUtil.getFileTypeMap(manifestContent);
    globalContext.getFiles().stream()
        .filter(fileName -> FileData
            .isHeatFile(fileTypeMap.get(fileName)))
        .forEach(fileName -> validate(fileName,
            fileTypeMap, baseFiles, globalContext));


  }

  private Set<String> validateManifest(ManifestContent manifestContent,
                                              GlobalValidationContext globalContext) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", SdcCommon.MANIFEST_NAME);
    Set<String> baseFiles = ManifestUtil.getBaseFiles(manifestContent);
    if (baseFiles == null || baseFiles.isEmpty()) {
      globalContext.addMessage(
          SdcCommon.MANIFEST_NAME,
          ErrorLevel.WARNING,
          ErrorMessagesFormatBuilder
              .getErrorWithParameters(ERROR_CODE_SRG_3,Messages
                  .MISSIN_BASE_HEAT_FILE.getErrorMessage()),
          LoggerTragetServiceName.VALIDATE_BASE_FILE,
          LoggerErrorDescription.MISSING_BASE_HEAT);
    } else if (baseFiles.size() > 1) {
      String baseFileList = getElementListAsString(baseFiles);
      globalContext.addMessage(
          SdcCommon.MANIFEST_NAME,
          ErrorLevel.WARNING,
          ErrorMessagesFormatBuilder
              .getErrorWithParameters(ERROR_CODE_SRG_4,Messages
                      .MULTI_BASE_HEAT_FILE.getErrorMessage(),
                  baseFileList),
          LoggerTragetServiceName.VALIDATE_BASE_FILE,
          LoggerErrorDescription.MULTI_BASE_HEAT);
    }
    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", SdcCommon.MANIFEST_NAME);
    return baseFiles;
  }

  private static String getElementListAsString(Set<String> elementCollection) {
    return "["
        + CommonMethods.collectionToCommaSeparatedString(elementCollection)
        +  "]";
  }

  private void validate(String fileName, Map<String, FileData.Type> fileTypeMap,
                        Set<String> baseFiles, GlobalValidationContext globalContext) {
    globalContext.setMessageCode(ERROR_CODE_SRG_5);
    HeatOrchestrationTemplate
        heatOrchestrationTemplate = ValidationUtil
        .checkHeatOrchestrationPreCondition(fileName, globalContext);
    if (heatOrchestrationTemplate == null) {
      return;
    }

    validateBaseFile(fileName, baseFiles, heatOrchestrationTemplate, globalContext);
    validateHeatVolumeFile(fileName, fileTypeMap, heatOrchestrationTemplate, globalContext);
  }


  private void validateBaseFile(String fileName, Set<String> baseFiles,
                                HeatOrchestrationTemplate heatOrchestrationTemplate,
                                GlobalValidationContext globalContext) {


    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);
    //if not base return
    if (baseFiles == null || !baseFiles.contains(fileName)) {
      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
      return;
    }

    //if no resources exist return
    if (heatOrchestrationTemplate.getResources() == null
        || heatOrchestrationTemplate.getResources().size() == 0) {
      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
      return;
    }

    Set<String> expectedExposedResources = new HashSet<>();
    heatOrchestrationTemplate.getResources()
        .entrySet()
        .stream()
        .filter(entry -> ValidationUtil.isExpectedToBeExposed(entry.getValue().getType()))
        .forEach(entry -> expectedExposedResources.add(entry.getKey()));
    Set<String> actualExposedResources = new HashSet<>();

    if (heatOrchestrationTemplate.getOutputs() != null) {
      globalContext.setMessageCode(ERROR_CODE_SRG_6);
      heatOrchestrationTemplate.getOutputs().entrySet()
          .stream()
          .filter(entry -> isPropertyValueGetResource(fileName, entry.getValue().getValue(),
              globalContext))
          .forEach(entry -> actualExposedResources.add(
              getResourceIdFromPropertyValue(fileName, entry.getValue().getValue(),
                  globalContext)));
    }
    ValidationUtil.removeExposedResourcesCalledByGetResource(fileName, actualExposedResources,
        heatOrchestrationTemplate, globalContext);

    actualExposedResources.forEach(expectedExposedResources::remove);

    if (CollectionUtils.isNotEmpty(expectedExposedResources)) {
      expectedExposedResources
          .stream()
          .forEach(name -> globalContext.addMessage(
              fileName,
              ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                  .getErrorWithParameters(ERROR_CODE_SRG_1,Messages
                          .RESOURCE_NOT_DEFINED_IN_OUTPUT.getErrorMessage(),
                      name),
              LoggerTragetServiceName.VALIDATE_BASE_FILE,
              LoggerErrorDescription.RESOURCE_NOT_DEFINED_AS_OUTPUT));
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
  }

  private void validateHeatVolumeFile(String fileName, Map<String, FileData.Type> fileTypeMap,
                                      HeatOrchestrationTemplate heatOrchestrationTemplate,
                                      GlobalValidationContext globalContext) {


    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);

    //if not heat volume return
    if (!fileTypeMap.get(fileName).equals(FileData.Type.HEAT_VOL)) {
      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
      return;
    }

    //if no resources exist return
    if (heatOrchestrationTemplate.getResources() == null
        || heatOrchestrationTemplate.getResources().size() == 0) {
      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
      return;
    }

    Set<String> expectedExposedResources = new HashSet<>();
    Set<String> actualExposedResources = new HashSet<>();
    heatOrchestrationTemplate.getResources()
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue().getType()
            .equals(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource()))
        .forEach(entry -> expectedExposedResources.add(entry.getKey()));

    if (heatOrchestrationTemplate.getOutputs() != null) {
      globalContext.setMessageCode(ERROR_CODE_SRG_6);
      heatOrchestrationTemplate.getOutputs().entrySet()
          .stream()
          .filter(entry -> isPropertyValueGetResource(fileName, entry.getValue().getValue(),
              globalContext))
          .forEach(entry -> actualExposedResources.add(
              getResourceIdFromPropertyValue(fileName, entry.getValue().getValue(),
                  globalContext)));
    }

    actualExposedResources.forEach(expectedExposedResources::remove);

    if (CollectionUtils.isNotEmpty(expectedExposedResources)) {
      expectedExposedResources
          .stream()
          .forEach(name -> globalContext.addMessage(
              fileName,
              ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                  .getErrorWithParameters(ERROR_CODE_SRG_2,Messages
                      .VOLUME_HEAT_NOT_EXPOSED.getErrorMessage(), name),
              LoggerTragetServiceName.VALIDATE_VOLUME_FILE,
              LoggerErrorDescription.VOLUME_FILE_NOT_EXPOSED));
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
  }


  private boolean isPropertyValueGetResource(String filename, Object value,
                                             GlobalValidationContext globalContext) {
    Set<String> referenceValues = HeatStructureUtil.getReferencedValuesByFunctionName(filename,
        ResourceReferenceFunctions.GET_RESOURCE.getFunction(), value, globalContext);
    return referenceValues != null && (CollectionUtils.isNotEmpty(referenceValues));
  }

  private String getResourceIdFromPropertyValue(String filename, Object value,
                                                GlobalValidationContext globalContext) {
    Set<String> referenceValues = HeatStructureUtil.getReferencedValuesByFunctionName(filename,
        ResourceReferenceFunctions.GET_RESOURCE.getFunction(), value, globalContext);
    if (referenceValues != null && CollectionUtils.isNotEmpty(referenceValues)) {
      return (String) referenceValues.toArray()[0];
    }
    return null;
  }

}
