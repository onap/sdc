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

package org.openecomp.sdc.validation.impl.validators.namingconvention;

import static java.util.Objects.nonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.util.ValidationUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;


public class ContrailServiceTemplateNamingConventionValidator implements ResourceValidator {
  private static final MdcDataDebugMessage MDC_DATA_DEBUG_MESSAGE = new MdcDataDebugMessage();
  private static final ErrorMessageCode ERROR_CODE_NST1 = new ErrorMessageCode("NST1");
  private static final ErrorMessageCode ERROR_CODE_NST2 = new ErrorMessageCode("NST2");
  private static final ErrorMessageCode ERROR_CODE_NST3 = new ErrorMessageCode("NST3");

  @Override
  public void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                       GlobalValidationContext globalContext, ValidationContext validationContext) {
    validateServiceTemplateImageAndFlavor(fileName, resourceEntry, globalContext);
  }

  private void validateServiceTemplateImageAndFlavor(String fileName,
                                                     Map.Entry<String, Resource> entry,
                                                     GlobalValidationContext globalContext) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);

    if (MapUtils.isEmpty(entry.getValue().getProperties())) {
      return;
    }

    Pair<String, String> imagePair = new ImmutablePair<>("image_name", ".*_image_name");
    Pair<String, String> flavorPair = new ImmutablePair<>("flavor", ".*_flavor_name");
    List<Pair<String, String>> imageFlavorPairs = Arrays.asList(imagePair, flavorPair);

    Map<String, Object> propertiesMap = entry.getValue().getProperties();

    boolean errorExistValidatingImageOrFlavor = false;
    for (Pair<String, String> imageOrFlavor : imageFlavorPairs) {
      boolean errorExistWhenValidatingImageOrFlavorNames =
              isErrorExistWhenValidatingImageOrFlavorNames(fileName, imageOrFlavor, entry,
                      propertiesMap, globalContext);
      errorExistValidatingImageOrFlavor =
              errorExistValidatingImageOrFlavor || errorExistWhenValidatingImageOrFlavorNames;
    }

    if (!errorExistValidatingImageOrFlavor) {
      validateServiceTemplatePropertiesValuesVmtypesAreIdentical(fileName, entry, globalContext,
              propertiesMap);
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
  }

  private void validateServiceTemplatePropertiesValuesVmtypesAreIdentical(String fileName,
                                                                          Map.Entry<String, Resource> entry,
                                                                          GlobalValidationContext globalContext,
                                                                          Map<String, Object> propertiesMap) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);

    Pair<String, String> vmTypeImagePair = new ImmutablePair<>("image_name", "\\_image\\_name");
    Pair<String, String> vmTypeFlavorPair = new ImmutablePair<>("flavor", "\\_flavor\\_name");
    validatePropertiesValuesVmtypesAreIdentical(Arrays.asList(vmTypeImagePair, vmTypeFlavorPair),
            fileName, entry, propertiesMap, globalContext);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
  }

  private void validatePropertiesValuesVmtypesAreIdentical(List<Pair> propertiesToMatch,
                                                           String fileName,
                                                           Map.Entry<String, Resource> resourceEntry,
                                                           Map<String, Object> propertiesMap,
                                                           GlobalValidationContext globalContext) {


    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);

    if (CollectionUtils.isEmpty(propertiesToMatch)) {
      return;
    }

    String previousPropertyValueValue = null;
    for (Pair propertyToMatch : propertiesToMatch) {
      Optional<String> propertyVmType =
              extractVmTypeFromProperty(fileName, resourceEntry, propertiesMap, globalContext,
                      propertyToMatch);
      if (propertyVmType.isPresent()) {
        String currentPropVmType = propertyVmType.get();
        previousPropertyValueValue =
                handleFirstIteration(previousPropertyValueValue, currentPropVmType);
        if (addWarningIfCurrentVmTypeIsDifferentFromPrevious(fileName, resourceEntry, globalContext,
                previousPropertyValueValue, currentPropVmType)) {
          MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
          return;
        }
      }
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
  }

  private boolean addWarningIfCurrentVmTypeIsDifferentFromPrevious(String fileName,
                                                                   Map.Entry<String, Resource> resourceEntry,
                                                                   GlobalValidationContext globalContext,
                                                                   String previousPropertyValueValue,
                                                                   String currentPropVmType) {
    if (!Objects.equals(previousPropertyValueValue, currentPropVmType)) {
      globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                      .getErrorWithParameters(
                              ERROR_CODE_NST1, Messages.CONTRAIL_VM_TYPE_NAME_NOT_ALIGNED_WITH_NAMING_CONVENSION
                                      .getErrorMessage(),
                              resourceEntry.getKey()),
              LoggerTragetServiceName.VALIDATE_CONTRAIL_VM_NAME,
              LoggerErrorDescription.NAME_NOT_ALIGNED_WITH_GUIDELINES);
      return true;
    }

    return false;
  }

  private boolean isErrorExistWhenValidatingImageOrFlavorNames(String fileName,
                                                               Pair<String, String> propertyNameAndRegex,
                                                               Map.Entry<String, Resource> resourceEntry,
                                                               Map<String, Object> propertiesMap,
                                                               GlobalValidationContext globalContext) {
    String propertyName = propertyNameAndRegex.getKey();
    Object nameValue = propertiesMap.get(propertyName);
    String[] regexList = new String[]{propertyNameAndRegex.getValue()};
    if (nonNull(nameValue)) {
      if (nameValue instanceof Map) {
        globalContext.setMessageCode(ERROR_CODE_NST3);
        if (ValidationUtil.validateMapPropertyValue(fileName, resourceEntry, globalContext,
                propertyName,
                nameValue, regexList)) {
          return true;
        }
      } else {
        globalContext.addMessage(
                fileName,
                ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                        .getErrorWithParameters(
                                ERROR_CODE_NST2, Messages.MISSING_GET_PARAM.getErrorMessage(),
                                propertyName,
                                resourceEntry.getKey()),
                LoggerTragetServiceName.VALIDATE_IMAGE_AND_FLAVOR_NAME,
                LoggerErrorDescription.MISSING_GET_PARAM);
        return true;
      }

      return false;
    }
    return false;
  }


  private Optional<String> extractVmTypeFromProperty(String fileName,
                                                     Map.Entry<String, Resource> resourceEntry,
                                                     Map<String, Object> propertiesMap,
                                                     GlobalValidationContext globalContext,
                                                     Pair propertyKeyRegex) {
    String propertyName = (String) propertyKeyRegex.getKey();
    Object propertyVal = propertiesMap.get(propertyName);
    if (nonNull(propertyVal)) {
      if (propertyVal instanceof Map) {
        String propertyValFromGetParam = ValidationUtil.getWantedNameFromPropertyValueGetParam
                (propertyVal);
        if (nonNull(propertyValFromGetParam)) {
          Pattern pattern = Pattern.compile("" + propertyKeyRegex.getValue());
          return Optional.ofNullable(pattern.split(propertyValFromGetParam)[0]);
        }
      } else {
        globalContext.addMessage(
                fileName,
                ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                        .getErrorWithParameters(
                                ERROR_CODE_NST2, Messages.MISSING_GET_PARAM.getErrorMessage(),
                                propertyName,
                                resourceEntry.getKey()),
                LoggerTragetServiceName.VALIDATE_VM_SYNC_IN_IMAGE_FLAVOR,
                LoggerErrorDescription.MISSING_GET_PARAM);
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  private String handleFirstIteration(String previousPropertyValueValue, String currentPropVmType) {
    String previousPropertyValue;
    if (Objects.isNull(previousPropertyValueValue)) {
      previousPropertyValue = currentPropVmType;
      return previousPropertyValue;
    }

   return previousPropertyValueValue;
  }
}
