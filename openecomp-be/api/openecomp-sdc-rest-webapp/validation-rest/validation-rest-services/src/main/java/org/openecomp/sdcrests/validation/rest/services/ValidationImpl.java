/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdcrests.validation.rest.services;


import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.validation.UploadValidationManager;
import org.openecomp.sdc.validation.types.ValidationFileResponse;
import org.openecomp.sdcrests.validation.rest.Validation;
import org.openecomp.sdcrests.validation.rest.mapping.MapValidationFileResponseToValidationFileResponseDto;
import org.openecomp.sdcrests.validation.types.ValidationFileResponseDto;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

@Named
@Service("validation")
@Scope(value = "prototype")
public class ValidationImpl implements Validation {

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  @Autowired
  private UploadValidationManager uploadValidationManager;

  @Override
  public Response validateFile(String type, InputStream fileToValidate) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    MDC.put(LoggerConstants.SERVICE_NAME,
        LoggerServiceName.Validate.toString());
    ValidationFileResponse validationFileResponse = null;
    try {
      validationFileResponse = uploadValidationManager.validateFile(type, fileToValidate);
    } catch (IOException exception) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
          LoggerTragetServiceName.VALIDATE_FILE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.VALIDATE_FILE);
      throw new RuntimeException(exception);
    }

    ValidationFileResponseDto validationFileResponseDto =
        new MapValidationFileResponseToValidationFileResponseDto()
            .applyMapping(validationFileResponse, ValidationFileResponseDto.class);

    mdcDataDebugMessage.debugExitMessage(null, null);

    return Response.ok(validationFileResponseDto).build();
  }


}
