package org.openecomp.sdc.be.servlets.exception;

import org.openecomp.sdc.be.config.ErrorInfo;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Set;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Set<ConstraintViolation<?>> constraintViolations = exception.getConstraintViolations();
        ErrorInfo error = new ErrorInfo();
        error.setCode(500);
        error.setMessage(constraintViolations.toString());
        return Response.status(BAD_REQUEST)
                .entity(error)
                .build();
    }
}
