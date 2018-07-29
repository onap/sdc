/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.sdc.activityspec.errors;

import org.codehaus.jackson.map.JsonMappingException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Set;

public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        Response response;
        if (exception instanceof CoreException) {
            response = transform(CoreException.class.cast(exception));
        } else if (exception instanceof ActivitySpecNotFoundException) {
            response = transform(ActivitySpecNotFoundException.class.cast(exception));
        } else if (exception instanceof ConstraintViolationException) {
            response = transform(ConstraintViolationException.class.cast(exception));
        } else if (exception instanceof JsonMappingException) {
            response = transform(JsonMappingException.class.cast(exception));
        } else {
            response = transform(exception);
        }

        return response;
    }

    private Response transform(ActivitySpecNotFoundException notFoundException) {
        LOGGER.error("Error while fetching Activity Spec :", notFoundException);
        return generateResponse(Status.NOT_FOUND, new ActivitySpecErrorResponse(notFoundException.getMessage()));
    }

    private Response transform(CoreException coreException) {
        LOGGER.error(coreException.getMessage(), coreException);
        return generateResponse(Status.BAD_REQUEST, new ActivitySpecErrorResponse(coreException.getMessage()));
    }

    private Response transform(ConstraintViolationException validationException) {
        LOGGER.error("ConstraintViolationException Occurred :", validationException);
        Set<ConstraintViolation<?>> constraintViolationSet = validationException.getConstraintViolations();
        String message;

        String fieldName = null;
        if (constraintViolationSet != null) {
            // getting the first violation message for the output response.
            ConstraintViolation<?> constraintViolation = constraintViolationSet.iterator().next();
            message = constraintViolation.getMessage();
            fieldName = ((PathImpl) constraintViolation.getPropertyPath()).getLeafNode().toString();

        } else {
            message = validationException.getMessage();
        }
        return generateResponse(Status.BAD_REQUEST, new ActivitySpecErrorResponse(String.format(message, fieldName)));
    }

    private Response transform(Exception exception) {
        LOGGER.error("Error occurred :", exception);
        return generateResponse(Status.INTERNAL_SERVER_ERROR, new ActivitySpecErrorResponse(exception.getMessage()));
    }

    private Response transform(JsonMappingException jsonMappingException) {
        LOGGER.error("Error Occurred during JSON mapping :", jsonMappingException);
        return generateResponse(Status.BAD_REQUEST, new ActivitySpecErrorResponse("Invalid Json Input"));
    }

    private Response generateResponse(Response.Status status, ActivitySpecErrorResponse activitySpecErrorResponse) {
        return Response.status(status).entity(activitySpecErrorResponse).type(MediaType.APPLICATION_JSON).build();
    }
}
