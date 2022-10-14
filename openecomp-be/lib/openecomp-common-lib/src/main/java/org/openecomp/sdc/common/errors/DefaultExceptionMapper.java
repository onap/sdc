/*
 * Copyright © 2016-2018 European Support Limited
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
package org.openecomp.sdc.common.errors;

import com.fasterxml.jackson.databind.JsonMappingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

    private static final String ERROR_CODES_TO_RESPONSE_STATUS_MAPPING_FILE = "errorCodesToResponseStatusMapping.json";
    @SuppressWarnings("unchecked")
    private static final Map<String, String> ERROR_CODE_TO_RESPONSE_STATUS = FileUtils
        .readViaInputStream(ERROR_CODES_TO_RESPONSE_STATUS_MAPPING_FILE, stream -> JsonUtil.json2Object(stream, Map.class));
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        Response response;
        if (exception instanceof CoreException) {
            response = transform((CoreException) exception);
        } else if (exception instanceof ConstraintViolationException) {
            response = transform((ConstraintViolationException) exception);
        } else if (exception instanceof JsonMappingException) {
            response = transform((JsonMappingException) exception);
        } else {
            response = transform(exception);
        }
        List<Object> contentTypes = new ArrayList<>();
        contentTypes.add(MediaType.APPLICATION_JSON);
        response.getMetadata().put("Content-Type", contentTypes);
        return response;
    }

    private Response transform(final CoreException coreException) {
        final ErrorCode code = coreException.code();
        LOGGER.error(code.message(), coreException);
        if (coreException.code().category().equals(ErrorCategory.APPLICATION)) {
            final Status errorStatus = Status.valueOf(ERROR_CODE_TO_RESPONSE_STATUS.get(code.id()));
            if (List.of(Status.BAD_REQUEST, Status.FORBIDDEN, Status.NOT_FOUND, Status.INTERNAL_SERVER_ERROR).contains(errorStatus)) {
                return buildResponse(errorStatus, code);
            }
            return buildResponse(Status.EXPECTATION_FAILED, code);
        }
        return buildResponse(Status.INTERNAL_SERVER_ERROR, code);
    }

    private Response buildResponse(final Status status, final ErrorCode code) {
        return Response.status(status).entity(toEntity(status, code)).build();
    }

    private Response transform(ConstraintViolationException validationException) {
        Set<ConstraintViolation<?>> constraintViolationSet = validationException.getConstraintViolations();
        String message;
        String fieldName = null;
        if (CollectionUtils.isEmpty(constraintViolationSet)) {
            message = validationException.getMessage();
        } else {
            // getting the first violation message for the output response.
            ConstraintViolation<?> constraintViolation = constraintViolationSet.iterator().next();
            message = constraintViolation.getMessage();
            fieldName = getFieldName(constraintViolation.getPropertyPath());
        }
        ErrorCode validationErrorCode = new ValidationErrorBuilder(message, fieldName).build();
        LOGGER.error(validationErrorCode.message(), validationException);
        return buildResponse(Status.EXPECTATION_FAILED, validationErrorCode);
    }

    private Response transform(JsonMappingException jsonMappingException) {
        ErrorCode jsonMappingErrorCode = new JsonMappingErrorBuilder().build();
        LOGGER.error(jsonMappingErrorCode.message(), jsonMappingException);
        return buildResponse(Status.EXPECTATION_FAILED, jsonMappingErrorCode);
    }

    private Response transform(Exception exception) {
        ErrorCode errorCode = new GeneralErrorBuilder().build();
        LOGGER.error(errorCode.message(), exception);
        return buildResponse(Status.INTERNAL_SERVER_ERROR, errorCode);
    }

    private String getFieldName(Path propertyPath) {
        return ((PathImpl) propertyPath).getLeafNode().toString();
    }

    private Object toEntity(final Status status, final ErrorCode code) {
        return new ErrorCodeAndMessage(status, code);
    }
}
