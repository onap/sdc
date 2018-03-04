package org.openecomp.sdc.be.servlets;

import org.eclipse.jetty.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Component
@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .entity(exception.getMessage())
                .build();
    }

}
