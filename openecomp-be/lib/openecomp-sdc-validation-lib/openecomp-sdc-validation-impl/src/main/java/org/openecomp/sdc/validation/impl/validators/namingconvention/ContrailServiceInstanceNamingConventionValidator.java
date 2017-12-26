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
import org.apache.commons.collections4.MapUtils;
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

import java.util.Map;


public class ContrailServiceInstanceNamingConventionValidator implements ResourceValidator {
  private static final MdcDataDebugMessage MDC_DATA_DEBUG_MESSAGE = new MdcDataDebugMessage();
  private static final String AVAILABILITY_ZONE = "availability_zone";
  private static final ErrorMessageCode ERROR_CODE_NSI1 = new ErrorMessageCode("NSI1");
  private static final ErrorMessageCode ERROR_CODE_NSI2 = new ErrorMessageCode("NSI2");

  @Override
  public void validate(String fileName, Map.Entry<String, Resource> resourceEntry,
                       GlobalValidationContext globalContext, ValidationContext validationContext) {
    validateAvailabilityZoneName(fileName, resourceEntry, globalContext);
  }

  private void validateAvailabilityZoneName(String fileName,
                                            Map.Entry<String, Resource> resourceEntry,
                                            GlobalValidationContext globalContext) {


    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);

    String[] regexList = new String[]{"availability_zone_(\\d+)"};
    if (MapUtils.isEmpty(resourceEntry.getValue().getProperties())) {
      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
      return;
    }

    Object availabilityZoneMap =
            resourceEntry.getValue().getProperties().containsKey(AVAILABILITY_ZONE) ? resourceEntry
                    .getValue().getProperties().get(AVAILABILITY_ZONE) : null;

    if (nonNull(availabilityZoneMap)) {
      if (availabilityZoneMap instanceof Map) {
        String availabilityZoneName = ValidationUtil
                .getWantedNameFromPropertyValueGetParam (availabilityZoneMap);

          if (availabilityZoneName != null && !ValidationUtil
                  .evalPattern(availabilityZoneName, regexList)) {
            globalContext.addMessage(
                    fileName,
                    ErrorLevel.WARNING, ErrorMessagesFormatBuilder.getErrorWithParameters(ERROR_CODE_NSI1,
                            Messages.PARAMETER_NAME_NOT_ALIGNED_WITH_GUIDELINES.getErrorMessage(),
                            ValidationUtil.getMessagePartAccordingToResourceType(resourceEntry),
                            "Availability Zone",
                            availabilityZoneName, resourceEntry.getKey()),
                    LoggerTragetServiceName.VALIDATE_AVAILABILITY_ZONE_NAME,
                    LoggerErrorDescription.NAME_NOT_ALIGNED_WITH_GUIDELINES);
          }
      } else {
        globalContext.addMessage(
                fileName,
                ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                        .getErrorWithParameters(ERROR_CODE_NSI2, Messages.MISSING_GET_PARAM.getErrorMessage(),
                                AVAILABILITY_ZONE, resourceEntry.getKey()),
                LoggerTragetServiceName.VALIDATE_AVAILABILITY_ZONE_NAME,
                LoggerErrorDescription.MISSING_GET_PARAM);
      }
    }
    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
  }

}