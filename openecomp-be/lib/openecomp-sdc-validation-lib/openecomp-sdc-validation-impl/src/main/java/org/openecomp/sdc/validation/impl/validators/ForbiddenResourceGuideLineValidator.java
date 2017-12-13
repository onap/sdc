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

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
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
import java.util.Objects;
import java.util.Set;

public class ForbiddenResourceGuideLineValidator implements Validator {
  private static final MdcDataDebugMessage MDC_DATA_DEBUG_MESSAGE = new MdcDataDebugMessage();
  private static Set<String> forbiddenResources = new HashSet<>();
  private static final ErrorMessageCode ERROR_CODE_FRG_1 = new ErrorMessageCode("FRG1");
  private static final ErrorMessageCode ERROR_CODE_FRG_2 = new ErrorMessageCode("FRG2");
  private static final ErrorMessageCode ERROR_CODE_FRG_3 = new ErrorMessageCode("FRG3");

  private static final Logger LOGGER =  LoggerFactory
          .getLogger(ForbiddenResourceGuideLineValidator.class);

  @Override
  public void init(Map<String, Object> properties) {
    Map<String, Map<String, Object>> forbiddenResourcesMap =
        (Map<String, Map<String, Object>>) properties.get("forbiddenResourceTypes");

    forbiddenResourcesMap.entrySet().stream()
        .filter(entry -> isResourceEnabled(entry.getValue().get("enable")))
        .forEach(entry -> forbiddenResources.add(entry.getKey()));
  }

  private boolean isResourceEnabled(Object enableValue) {
    if (Objects.isNull(enableValue)) {
      return true;
    }

    if (enableValue instanceof Boolean) {
      return (Boolean)enableValue;
    }

    return Boolean.valueOf((String) enableValue);
  }


  @Override
  public void validate(GlobalValidationContext globalContext) {
    ManifestContent manifestContent;
    try {
      manifestContent = ValidationUtil.checkValidationPreCondition(globalContext);
    } catch (Exception exception) {
      LOGGER.debug("",exception);
      return;
    }

    Map<String, FileData.Type> fileTypeMap = ManifestUtil.getFileTypeMap(manifestContent);

    globalContext.getFiles().stream()
        .filter(fileName -> FileData
            .isHeatFile(fileTypeMap.get(fileName)))
        .forEach(fileName -> validate(fileName, globalContext));
  }

  private void validate(String fileName, GlobalValidationContext globalContext) {
    globalContext.setMessageCode(ERROR_CODE_FRG_3);
    HeatOrchestrationTemplate
        heatOrchestrationTemplate = ValidationUtil.checkHeatOrchestrationPreCondition(
        fileName, globalContext);
    if (heatOrchestrationTemplate == null) {
      return;
    }

    validateResourceTypeIsForbidden(fileName, heatOrchestrationTemplate, globalContext);
  }

  private void validateResourceTypeIsForbidden(String fileName,
                                               HeatOrchestrationTemplate heatOrchestrationTemplate,
                                               GlobalValidationContext globalContext) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);

    Map<String, Resource> resourcesMap = heatOrchestrationTemplate.getResources();
    if (MapUtils.isEmpty(resourcesMap)) {
      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
      return;
    }

    for (Map.Entry<String, Resource> resourceEntry : resourcesMap.entrySet()) {
      String resourceType = resourceEntry.getValue().getType();
      if (Objects.isNull(resourceType)) {
        globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                .getErrorWithParameters(ERROR_CODE_FRG_1,
                        Messages.INVALID_RESOURCE_TYPE.getErrorMessage(),"null",
                    resourceEntry.getKey()), LoggerTragetServiceName.VALIDATE_RESOURCE_TYPE,
            LoggerErrorDescription.INVALID_RESOURCE_TYPE);
      } else {
        if (isResourceForbidden(resourceType)) {
           globalContext.addMessage(
              fileName,
              ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(ERROR_CODE_FRG_2, Messages.FORBIDDEN_RESOURCE_IN_USE
                          .getErrorMessage(),
                      resourceType, resourceEntry.getKey()),
              LoggerTragetServiceName.VALIDATE_FORBIDDEN_RESOURCE,
              LoggerErrorDescription.FLOATING_IP_IN_USE);
        }
      }
    }
    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
  }

  private boolean isResourceForbidden(String resourceType) {
    return forbiddenResources.contains(resourceType);
  }
}
