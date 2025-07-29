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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.onap.sdc.security.RepresentationUtils;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.exception.NotAllowedSpecialCharsException;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.exception.ServiceException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DefaultExceptionMapper {

    private static final String ERROR_CODES_TO_RESPONSE_STATUS_MAPPING_FILE = "errorCodesToResponseStatusMapping.json";
    @SuppressWarnings("unchecked")
    private static final Map<String, String> ERROR_CODE_TO_RESPONSE_STATUS = FileUtils
        .readViaInputStream(ERROR_CODES_TO_RESPONSE_STATUS_MAPPING_FILE, stream -> JsonUtil.json2Object(stream, Map.class));
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ErrorCode> handleCoreException(CoreException exception) {
        return transform((CoreException)exception);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorCode> handleConstraintViolationException(ConstraintViolationException exception) {
        return transform((ConstraintViolationException)exception);
    }

    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<ErrorCode> handleJsonMappingException(JsonMappingException exception) {
        return transform((JsonMappingException)exception);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorCode> handleException(Exception exception) {
        return transform((Exception)exception);
    }

    private ResponseEntity transform(final CoreException coreException) {
        final ErrorCode code = coreException.code();
        LOGGER.error(code.message(), coreException);
        if (coreException.code().category().equals(ErrorCategory.APPLICATION)) {
            final HttpStatus errorStatus = HttpStatus.valueOf(ERROR_CODE_TO_RESPONSE_STATUS.get(code.id()));
            if (List.of(HttpStatus.BAD_REQUEST, HttpStatus.FORBIDDEN, HttpStatus.NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR).contains(errorStatus)) {
                return buildResponse(errorStatus, code);
            }
            return buildResponse(HttpStatus.EXPECTATION_FAILED, code);
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, code);
    }

    private ResponseEntity buildResponse(final HttpStatus status, final ErrorCode code) {
       return ResponseEntity.status(status).body(toEntity(status, code));
      //  ResponseEntity.ok().build();
    }

    private ResponseEntity transform(ConstraintViolationException validationException) {
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
        return buildResponse(HttpStatus.EXPECTATION_FAILED, validationErrorCode);
    }

    private ResponseEntity transform(JsonMappingException jsonMappingException) {
        ErrorCode jsonMappingErrorCode = new JsonMappingErrorBuilder().build();
        LOGGER.error(jsonMappingErrorCode.message(), jsonMappingException);
        return buildResponse(HttpStatus.EXPECTATION_FAILED, jsonMappingErrorCode);
    }

    private ResponseEntity transform(Exception exception) {
        ErrorCode errorCode = new GeneralErrorBuilder().build();
        LOGGER.error(errorCode.message(), exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorCode);
    }

    private String getFieldName(Path propertyPath) {
        return ((PathImpl) propertyPath).getLeafNode().toString();
    }

    private Object toEntity(final HttpStatus status, final ErrorCode code) {
        return new ErrorCodeAndMessage(status, code);
    }

    public void writeToResponse(final NotAllowedSpecialCharsException e, final HttpServletResponse httpResponse) throws IOException {
        final ResponseFormat responseFormat = new ResponseFormat(400);
        responseFormat.setServiceException(new ServiceException(e.getErrorId(), e.getMessage(), new String[0]));
        httpResponse.setStatus(responseFormat.getStatus());
        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.getWriter().write(RepresentationUtils.toRepresentation(responseFormat.getRequestError()));
    }

}
