package org.openecomp.sdc.validation.impl.validators.heatresource;

import org.apache.commons.collections4.CollectionUtils;
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
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private static String ERROR_CODE_F1 = "F-1";
  private static String ERROR_CODE_F2 = "F-2";
  private static String ERROR_CODE_F3 = "F-3";

  @Override
  public void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                       GlobalValidationContext globalContext, ValidationContext validationContext) {
    validateResourceGroupType(fileName, resourceEntry, globalContext);
  }

  private static void validateResourceGroupType(String fileName,
                                                Map.Entry<String, Resource> resourceEntry,
                                                GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Resource resourceDef = HeatTreeManagerUtil
        .getResourceDef(fileName, resourceEntry.getKey(), resourceEntry.getValue(), globalContext);
    if (resourceDef != null) {
      if (Objects.nonNull(resourceDef.getType())
          && HeatValidationService.isNestedResource(resourceDef.getType())) {
        Optional<String> indexVarValue =
            getResourceGroupIndexVarValue(resourceEntry, fileName, globalContext);
        handleNestedResourceType(fileName, resourceEntry.getKey(), resourceDef, indexVarValue,
            globalContext);
      }
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
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
              .getErrorWithParameters(Messages.RESOURCE_GROUP_INVALID_INDEX_VAR.getErrorMessage(),
                  ERROR_CODE_F1, resourceEntry.getKey()),
          LoggerTragetServiceName.VALIDATE_RESOURCE_GROUP_TYPE,
          LoggerErrorDescription.INVALID_INDEX_VAR);
      return Optional.empty();
    }
  }

  private static void handleNestedResourceType(String fileName, String resourceName,
                                               Resource resource, Optional<String> indexVarValue,
                                               GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    validateAllPropertiesMatchNestedParameters(fileName, resourceName, resource, indexVarValue,
        globalContext);
    validateLoopsOfNestingFromFile(fileName, resource.getType(), globalContext);

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private static void validateAllPropertiesMatchNestedParameters(String fileName,
                                                                 String resourceName,
                                                                 Resource resource,
                                                                 Optional<String> indexVarValue,
                                                                 GlobalValidationContext
                                                                     globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    String resourceType = resource.getType();
    if (globalContext.getFileContextMap().containsKey(resourceType)) {
      Set<String> propertiesNames =
          resource.getProperties() == null ? null : resource.getProperties().keySet();
      if (CollectionUtils.isNotEmpty(propertiesNames)) {
        HeatValidationService
            .checkNestedParameters(fileName, resourceType, resourceName, resource, propertiesNames,
                indexVarValue, globalContext);
      }
    } else {
      globalContext.addMessage(resourceType, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
              .getErrorWithParameters(Messages.MISSING_NESTED_FILE.getErrorMessage(), ERROR_CODE_F2,
                  resourceType),
          LoggerTragetServiceName.VALIDATE_PROPERTIES_MATCH_NESTED_PARAMETERS,
          LoggerErrorDescription.MISSING_FILE);
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private static void validateLoopsOfNestingFromFile(String fileName, String resourceType,
                                                     GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    List<String> filesInLoop = new ArrayList<>(Collections.singletonList(fileName));
    if (HeatValidationService
        .isNestedLoopExistInFile(fileName, resourceType, filesInLoop, globalContext)) {
      globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
              .getErrorWithParameters(Messages.NESTED_LOOP.getErrorMessage(),
                  ERROR_CODE_F3,
                  HeatValidationService.drawFilesLoop(filesInLoop)),
          LoggerTragetServiceName.VALIDATE_NESTING_LOOPS, LoggerErrorDescription.NESTED_LOOP);
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }
}
