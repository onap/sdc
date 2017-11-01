package org.openecomp.sdc.validation.impl.validators.namingconvention;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.ErrorCode;
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

import static java.util.Objects.nonNull;

/**
 * Created by TALIO on 2/24/2017.
 */
public class ContrailServiceTemplateNamingConventionValidator implements ResourceValidator {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static final ErrorCode ERROR_CODE_NST1 = new ErrorCode("NST1");
  private static final ErrorCode ERROR_CODE_NST2 = new ErrorCode("NST2");
  private static final ErrorCode ERROR_CODE_NST3 = new ErrorCode("NST3");
  @Override
  public void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                       GlobalValidationContext globalContext, ValidationContext validationContext) {
    validateServiceTemplateImageAndFlavor(fileName, resourceEntry, globalContext);
  }

  private void validateServiceTemplateImageAndFlavor(String fileName,
                                                     Map.Entry<String, Resource> entry,
                                                     GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

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

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private void validateServiceTemplatePropertiesValuesVmtypesAreIdentical(String fileName,
                                                                          Map.Entry<String, Resource> entry,
                                                                          GlobalValidationContext globalContext,
                                                                          Map<String, Object> propertiesMap) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Pair<String, String> vmTypeImagePair = new ImmutablePair<>("image_name", "\\_image\\_name");
    Pair<String, String> vmTypeFlavorPair = new ImmutablePair<>("flavor", "\\_flavor\\_name");
    validatePropertiesValuesVmtypesAreIdentical(Arrays.asList(vmTypeImagePair, vmTypeFlavorPair),
        fileName, entry, propertiesMap, globalContext);

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private void validatePropertiesValuesVmtypesAreIdentical(List<Pair> propertiesToMatch,
                                                           String fileName,
                                                           Map.Entry<String, Resource> resourceEntry,
                                                           Map<String, Object> propertiesMap,
                                                           GlobalValidationContext globalContext) {


    mdcDataDebugMessage.debugEntryMessage("file", fileName);

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
          mdcDataDebugMessage.debugExitMessage("file", fileName);
          return;
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private boolean addWarningIfCurrentVmTypeIsDifferentFromPrevious(String fileName,
                                                                   Map.Entry<String, Resource> resourceEntry,
                                                                   GlobalValidationContext globalContext,
                                                                   String previousPropertyValueValue,
                                                                   String currentPropVmType) {
    if (!Objects.equals(previousPropertyValueValue, currentPropVmType)) {
        ERROR_CODE_NST1.setMessage(Messages.CONTRAIL_VM_TYPE_NAME_NOT_ALIGNED_WITH_NAMING_CONVENSION
                .getErrorMessage());
      globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
              .getErrorWithParameters(
                      ERROR_CODE_NST1,
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
    Object nameValue =
        propertiesMap.get(propertyName) == null ? null : propertiesMap.get(propertyName);
    String[] regexList = new String[]{propertyNameAndRegex.getValue()};
    if (nonNull(nameValue)) {
      if (nameValue instanceof Map) {
          globalContext.setErrorCode(ERROR_CODE_NST3);
        if (ValidationUtil.validateMapPropertyValue(fileName, resourceEntry, globalContext,
            propertyName,
            nameValue, regexList)) {
          return true;
        }
      } else {
          ERROR_CODE_NST2.setMessage(Messages.MISSING_GET_PARAM.getErrorMessage());
        globalContext.addMessage(
            fileName,
            ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                .getErrorWithParameters(
                        ERROR_CODE_NST2, propertyName,
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
          ERROR_CODE_NST2.setMessage(Messages.MISSING_GET_PARAM.getErrorMessage());
        globalContext.addMessage(
            fileName,
            ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                .getErrorWithParameters(
                        ERROR_CODE_NST3, propertyName,
                    resourceEntry.getKey()),
            LoggerTragetServiceName.VALIDATE_VM_SYNC_IN_IMAGE_FLAVOR,
            LoggerErrorDescription.MISSING_GET_PARAM);
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  private String handleFirstIteration(String previousPropertyValueValue, String currentPropVmType) {
    if (Objects.isNull(previousPropertyValueValue)) {
      previousPropertyValueValue = currentPropVmType;
    }

    return previousPropertyValueValue;
  }
}
