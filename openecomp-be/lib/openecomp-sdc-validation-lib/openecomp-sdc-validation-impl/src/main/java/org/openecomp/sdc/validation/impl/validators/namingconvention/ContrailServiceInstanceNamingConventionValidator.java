package org.openecomp.sdc.validation.impl.validators.namingconvention;

import org.apache.commons.collections4.MapUtils;
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

import java.util.Map;

import static java.util.Objects.nonNull;

/**
 * Created by TALIO on 2/24/2017.
 */
public class ContrailServiceInstanceNamingConventionValidator implements ResourceValidator {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static final ErrorCode ERROR_CODE_NSI1 = new ErrorCode("NSI1");
  private static final ErrorCode ERROR_CODE_NSI2 = new ErrorCode("NSI2");

  @Override
  public void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                       GlobalValidationContext globalContext, ValidationContext validationContext) {
    validateAvailabilityZoneName(fileName, resourceEntry, globalContext);
  }

  private void validateAvailabilityZoneName(String fileName,
                                            Map.Entry<String, Resource> resourceEntry,
                                            GlobalValidationContext globalContext) {


    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    String[] regexList = new String[]{"availability_zone_(\\d+)"};

    if (MapUtils.isEmpty(resourceEntry.getValue().getProperties())) {
      mdcDataDebugMessage.debugExitMessage("file", fileName);
      return;
    }

    Object availabilityZoneMap =
        resourceEntry.getValue().getProperties().containsKey("availability_zone") ? resourceEntry
            .getValue().getProperties().get("availability_zone") : null;

    if (nonNull(availabilityZoneMap)) {
      if (availabilityZoneMap instanceof Map) {
        String availabilityZoneName = ValidationUtil.getWantedNameFromPropertyValueGetParam
            (availabilityZoneMap);

        if (availabilityZoneName != null) {
          if (!ValidationUtil.evalPattern(availabilityZoneName, regexList)) {
              ERROR_CODE_NSI1.setMessage(Messages.PARAMETER_NAME_NOT_ALIGNED_WITH_GUIDELINES.getErrorMessage());
            globalContext.addMessage(
                fileName,
                ErrorLevel.WARNING, ErrorMessagesFormatBuilder.getErrorWithParameters(ERROR_CODE_NSI1,
                    ValidationUtil.getMessagePartAccordingToResourceType(resourceEntry),
                    "Availability Zone",
                    availabilityZoneName, resourceEntry.getKey()),
                LoggerTragetServiceName.VALIDATE_AVAILABILITY_ZONE_NAME,
                LoggerErrorDescription.NAME_NOT_ALIGNED_WITH_GUIDELINES);
          }
        }
      } else {
          ERROR_CODE_NSI2.setMessage(Messages.MISSING_GET_PARAM.getErrorMessage());
        globalContext.addMessage(
            fileName,
            ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                .getErrorWithParameters(ERROR_CODE_NSI2,
                    "availability_zone", resourceEntry.getKey()),
            LoggerTragetServiceName.VALIDATE_AVAILABILITY_ZONE_NAME,
            LoggerErrorDescription.MISSING_GET_PARAM);
      }
    }
    mdcDataDebugMessage.debugExitMessage("file", fileName);
  }

}
