package org.openecomp.sdc.validation.impl.validators;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.manifest.ManifestUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.validation.util.ValidationUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by TALIO on 2/15/2017.
 */
public class ForbiddenResourceGuideLineValidator implements Validator {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static Set<String> forbiddenResources = new HashSet<>();

  @Override
  public void init(Map<String, Object> properties) {
    Map<String, Map<String, Object>> forbiddenResourcesMap =
        (Map<String, Map<String, Object>>) properties.get("forbiddenResourceTypes");

    forbiddenResourcesMap.entrySet().stream()
        .filter(entry -> isResourceEnabled(entry.getValue().get("enable")))
        .forEach(entry -> forbiddenResources.add(entry.getKey()));




  }

  private boolean isResourceEnabled(Object enableValue){
    if(Objects.isNull(enableValue)){
      return true;
    }

    if(enableValue instanceof Boolean){
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
      return;
    }

    Map<String, FileData.Type> fileTypeMap = ManifestUtil.getFileTypeMap(manifestContent);

    globalContext.getFiles().stream()
        .filter(fileName -> FileData
            .isHeatFile(fileTypeMap.get(fileName)))
        .forEach(fileName -> validate(fileName, globalContext));
  }

  private void validate(String fileName, GlobalValidationContext globalContext) {
    HeatOrchestrationTemplate
        heatOrchestrationTemplate = ValidationUtil.checkHeatOrchestrationPreCondition(fileName, globalContext);
    if (heatOrchestrationTemplate == null) {
      return;
    }

    validateResourceTypeIsForbidden(fileName, heatOrchestrationTemplate, globalContext);
  }

  private void validateResourceTypeIsForbidden(String fileName,
                                               HeatOrchestrationTemplate heatOrchestrationTemplate,
                                               GlobalValidationContext globalContext) {

    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Map<String, Resource> resourcesMap = heatOrchestrationTemplate.getResources();
    if (MapUtils.isEmpty(resourcesMap)) {
      mdcDataDebugMessage.debugExitMessage("file", fileName);
      return;
    }

    for (Map.Entry<String, Resource> resourceEntry : resourcesMap.entrySet()) {
      String resourceType = resourceEntry.getValue().getType();
      if (Objects.isNull(resourceType)) {
        globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                .getErrorWithParameters(Messages.INVALID_RESOURCE_TYPE.getErrorMessage(), "null",
                    resourceEntry.getKey()), LoggerTragetServiceName.VALIDATE_RESOURCE_TYPE,
            LoggerErrorDescription.INVALID_RESOURCE_TYPE);
      } else {
        if(isResourceForbidden(resourceType)){
          globalContext.addMessage(
              fileName,
              ErrorLevel.WARNING,
              ErrorMessagesFormatBuilder
                  .getErrorWithParameters(Messages.FORBIDDEN_RESOURCE_IN_USE.getErrorMessage(),
                      resourceType,
                      resourceEntry.getKey()),
              LoggerTragetServiceName.VALIDATE_FORBIDDEN_RESOURCE,
              LoggerErrorDescription.FLOATING_IP_IN_USE);
        }
      }
    }
    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private boolean isResourceForbidden(String resourceType){
    return forbiddenResources.contains(resourceType);
  }
}
