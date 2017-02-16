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

package org.openecomp.sdcrests.errors;

import org.codehaus.jackson.map.JsonMappingException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.ErrorCodeAndMessage;
import org.openecomp.sdc.common.errors.GeneralErrorBuilder;
import org.openecomp.sdc.common.errors.JsonMappingErrorBuilder;
import org.openecomp.sdc.common.errors.ValidationErrorBuilder;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class DefaultExceptionMapper implements ExceptionMapper<Exception> {
  private static final String ERROR_CODES_TO_RESPONSE_STATUS_MAPPING_FILE =
      "errorCodesToResponseStatusMapping.json";
  private static Map<String, String> errorCodeToResponseStatus = JsonUtil
      .json2Object(FileUtils.getFileInputStream(ERROR_CODES_TO_RESPONSE_STATUS_MAPPING_FILE),
          Map.class);
  private static org.slf4j.Logger logger = LoggerFactory.getLogger(DefaultExceptionMapper.class);

  @Override
  public Response toResponse(Exception e0) {
    Response response;
    if (e0 instanceof CoreException) {
      response = transform(CoreException.class.cast(e0));
    } else if (e0 instanceof ConstraintViolationException) {
      response = transform(ConstraintViolationException.class.cast(e0));

    } else if (e0 instanceof JsonMappingException) {
      response = transform(JsonMappingException.class.cast(e0));

    } else {
      response = transform(e0);
    }

    List<Object> contentTypes = new ArrayList<>();
    contentTypes.add(MediaType.APPLICATION_JSON);
    response.getMetadata().put("Content-Type", contentTypes);
    return response;
  }

  private Response transform(CoreException coreException) {
    Response response;
    ErrorCode code = coreException.code();
    logger.error(code.message(), coreException);

    if (coreException.code().category().equals(ErrorCategory.APPLICATION)) {
      if (Response.Status.NOT_FOUND.name().equals(errorCodeToResponseStatus.get(code.id()))) {
        response = Response
            .status(Response.Status.NOT_FOUND)
            .entity(toEntity(Response.Status.NOT_FOUND, code))
            .build();
      } else if (Response.Status.BAD_REQUEST.name()
          .equals(errorCodeToResponseStatus.get(code.id()))) {
        response = Response
            .status(Response.Status.BAD_REQUEST)
            .entity(toEntity(Response.Status.BAD_REQUEST, code))
            .build();
      } else {
        response = Response
            .status(Response.Status.EXPECTATION_FAILED)
            .entity(toEntity(Response.Status.EXPECTATION_FAILED, code))
            .build();
      }
    } else {
      response = Response
          .status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(toEntity(Response.Status.INTERNAL_SERVER_ERROR, code))
          .build();
    }


    return response;
  }

  private Response transform(ConstraintViolationException validationException) {
    Set<ConstraintViolation<?>> constraintViolationSet =
        validationException.getConstraintViolations();
    String message;

    String fieldName = null;
    if (!CommonMethods.isEmpty(constraintViolationSet)) {
      // getting the first violation message for the output response.
      ConstraintViolation<?> constraintViolation = constraintViolationSet.iterator().next();
      message = constraintViolation.getMessage();
      fieldName = getFieldName(constraintViolation.getPropertyPath());

    } else {
      message = validationException.getMessage();
    }

    ErrorCode validationErrorCode = new ValidationErrorBuilder(message, fieldName).build();

    logger.error(validationErrorCode.message(), validationException);
    return Response
        .status(Response.Status.EXPECTATION_FAILED) //error 417
        .entity(toEntity(Response.Status.EXPECTATION_FAILED, validationErrorCode))
        .build();
  }


  private Response transform(JsonMappingException jsonMappingException) {
    ErrorCode jsonMappingErrorCode = new JsonMappingErrorBuilder().build();
    logger.error(jsonMappingErrorCode.message(), jsonMappingException);
    return Response
        .status(Response.Status.EXPECTATION_FAILED) //error 417
        .entity(toEntity(Response.Status.EXPECTATION_FAILED, jsonMappingErrorCode))
        .build();
  }

  private Response transform(Exception e0) {
    ErrorCode generalErrorCode = new GeneralErrorBuilder(e0.getMessage()).build();
    logger.error(generalErrorCode.message(), e0);
    return Response
        .status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(toEntity(Response.Status.INTERNAL_SERVER_ERROR, generalErrorCode))
        .build();
  }

  private String getFieldName(Path propertyPath) {
    return ((PathImpl) propertyPath).getLeafNode().toString();
  }

  private Object toEntity(Response.Status status, ErrorCode code) {
    return new ErrorCodeAndMessage(status, code);
  }

}
