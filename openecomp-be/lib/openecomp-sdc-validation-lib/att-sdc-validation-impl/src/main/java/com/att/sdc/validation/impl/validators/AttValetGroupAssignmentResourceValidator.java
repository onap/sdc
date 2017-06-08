package com.att.sdc.validation.impl.validators;

import com.att.sdc.validation.datatypes.AttLoggerErrorDescription;
import com.att.sdc.validation.datatypes.AttLoggerTargetServiceName;
import com.att.sdc.validation.datatypes.AttValetGroupTypeValues;
import com.att.sdc.validation.messages.Messages;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;

import java.util.Map;
import java.util.Objects;

/**
 * Created by TALIO on 2/26/2017.
 */
public class AttValetGroupAssignmentResourceValidator implements ResourceValidator {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  @Override
  public void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                       GlobalValidationContext globalContext, ValidationContext validationContext) {
    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    Resource resource = resourceEntry.getValue();

    Map<String, Object> propertiesMap = resource.getProperties();
    if (MapUtils.isEmpty(propertiesMap)) {
      return;
    }

    Object groupTypeValue = propertiesMap.get("group_type");
    if (Objects.isNull(groupTypeValue)) {
      return;
    }

    validateGroupTypeValue(fileName, resourceEntry.getKey(), groupTypeValue, globalContext);
    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

  private static void validateGroupTypeValue(String fileName, String resourceId,
                                             Object groupTypeValue,
                                             GlobalValidationContext globalContext) {
    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    if (!AttValetGroupTypeValues.isGroupTypeValid(groupTypeValue)) {
      globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
              .getErrorWithParameters(Messages.UNEXPECTED_GROUP_TYPE_ATT_VALET.getErrorMessage(),
                  resourceId), AttLoggerTargetServiceName.VALIDATE_ATT_VALET_TYPE,
          AttLoggerErrorDescription.ATT_VALET_IN_USE);
    }

    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }
}
