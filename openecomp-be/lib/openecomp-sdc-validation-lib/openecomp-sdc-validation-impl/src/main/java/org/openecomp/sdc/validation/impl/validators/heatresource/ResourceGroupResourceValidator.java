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

package org.openecomp.sdc.validation.impl.validators.heatresource;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.heat.services.tree.HeatTreeManagerUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.impl.util.HeatValidationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Created by TALIO on 2/22/2017.
 */
public class ResourceGroupResourceValidator implements ResourceValidator {
  private static final MdcDataDebugMessage MDC_DATA_DEBUG_MESSAGE = new MdcDataDebugMessage();

  private static final ErrorMessageCode ERROR_CODE_HRR1 = new ErrorMessageCode("HRR1");
  private static final ErrorMessageCode ERROR_CODE_HRR2 = new ErrorMessageCode("HRR2");
  private static final ErrorMessageCode ERROR_CODE_HRR3 = new ErrorMessageCode("HRR3");
  private static final ErrorMessageCode ERROR_CODE_HRR4 = new ErrorMessageCode("HRR4");
  private static final ErrorMessageCode ERROR_CODE_HRR5 = new ErrorMessageCode("HRR5");
  private static final ErrorMessageCode ERROR_CODE_HRR6 = new ErrorMessageCode("HRR6");
  private static final ErrorMessageCode ERROR_CODE_HRR7 = new ErrorMessageCode("HRR7");
  private static final ErrorMessageCode ERROR_CODE_HRR8 = new ErrorMessageCode("HRR8");

  @Override
  public void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                       GlobalValidationContext globalContext, ValidationContext validationContext) {
    validateResourceGroupType(fileName, resourceEntry, globalContext);
  }

  private static void validateResourceGroupType(String fileName,
                                                Map.Entry<String, Resource> resourceEntry,
                                                GlobalValidationContext globalContext) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);

    globalContext.setMessageCode(ERROR_CODE_HRR6);
    HeatTreeManagerUtil
            .checkResourceTypeValid(fileName, resourceEntry.getKey(), resourceEntry.getValue(),
                    globalContext);
    globalContext.setMessageCode(ERROR_CODE_HRR7);
    HeatTreeManagerUtil
            .checkResourceGroupTypeValid(fileName, resourceEntry.getKey(), resourceEntry.getValue(),
                    globalContext);
    globalContext.setMessageCode(ERROR_CODE_HRR8);
    HeatTreeManagerUtil.checkIfResourceGroupTypeIsNested(fileName, resourceEntry.getKey(),
            resourceEntry.getValue(), globalContext);
    Resource resourceDef = HeatTreeManagerUtil
            .getResourceDef( resourceEntry.getValue());

      if (resourceDef != null  && Objects.nonNull(resourceDef.getType())
              && HeatValidationService.isNestedResource(resourceDef.getType())) {
        Optional<String> indexVarValue =
                getResourceGroupIndexVarValue(resourceEntry, fileName, globalContext);
        handleNestedResourceType(fileName, resourceEntry.getKey(), resourceDef, indexVarValue,
                globalContext);

    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
  }

  private static Optional<String> getResourceGroupIndexVarValue(
          Map.Entry<String, Resource> resourceEntry, String fileName,
          GlobalValidationContext globalContext) {
    Object indexVar =
            resourceEntry.getValue().getProperties().get(HeatConstants.INDEX_PROPERTY_NAME);
    if (indexVar == null) {
      return Optional.of(HeatConstants.RESOURCE_GROUP_INDEX_VAR_DEFAULT_VALUE);
    }

    if (indexVar instanceof String) {
      return Optional.of((String) indexVar);
    } else {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                      .getErrorWithParameters(
                              ERROR_CODE_HRR1, Messages.RESOURCE_GROUP_INVALID_INDEX_VAR.getErrorMessage(),
                              resourceEntry.getKey()),
              LoggerTragetServiceName.VALIDATE_RESOURCE_GROUP_TYPE,
              LoggerErrorDescription.INVALID_INDEX_VAR);
      return Optional.empty();
    }
  }

  private static void handleNestedResourceType(String fileName, String resourceName,
                                               Resource resource, Optional<String> indexVarValue,
                                               GlobalValidationContext globalContext) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);

    validateAllPropertiesMatchNestedParameters(fileName, resourceName, resource, indexVarValue,
            globalContext);
    validateLoopsOfNestingFromFile(fileName, resource.getType(), globalContext);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
  }

  private static void validateAllPropertiesMatchNestedParameters(String fileName,
                                                                 String resourceName,
                                                                 Resource resource,
                                                                 Optional<String> indexVarValue,
                                                                 GlobalValidationContext
                                                                         globalContext) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);

    String resourceType = resource.getType();
    if (globalContext.getFileContextMap().containsKey(resourceType)) {
      Set<String> propertiesNames =
              resource.getProperties() == null ? null : resource.getProperties().keySet();
      if (CollectionUtils.isNotEmpty(propertiesNames)) {
        globalContext.setMessageCode(ERROR_CODE_HRR4);
        HeatValidationService
                .checkNestedParametersNoMissingParameterInNested(fileName, resourceType, resourceName,
                         propertiesNames,
                         globalContext);
        globalContext.setMessageCode(ERROR_CODE_HRR5);
        HeatValidationService
                .checkNestedInputValuesAlignWithType(fileName, resourceType, resourceName, resource,
                        indexVarValue, globalContext);
      }
    } else {
      globalContext.addMessage(resourceType, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                      .getErrorWithParameters(
                              ERROR_CODE_HRR2, Messages.MISSING_NESTED_FILE.getErrorMessage(),
                              resourceType),
              LoggerTragetServiceName.VALIDATE_PROPERTIES_MATCH_NESTED_PARAMETERS,
              LoggerErrorDescription.MISSING_FILE);
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
  }

  private static void validateLoopsOfNestingFromFile(String fileName, String resourceType,
                                                     GlobalValidationContext globalContext) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);

    List<String> filesInLoop = new ArrayList<>(Collections.singletonList(fileName));
    if (HeatValidationService
            .isNestedLoopExistInFile(fileName, resourceType, filesInLoop, globalContext)) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                      .getErrorWithParameters(
                              ERROR_CODE_HRR3, Messages.NESTED_LOOP.getErrorMessage(),
                              HeatValidationService.drawFilesLoop(filesInLoop)),
              LoggerTragetServiceName.VALIDATE_NESTING_LOOPS, LoggerErrorDescription.NESTED_LOOP);
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
  }
}
