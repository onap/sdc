package org.openecomp.sdc.be.servlets.exception;

import org.eclipse.jetty.http.HttpStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Component
@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger log = Logger.getLogger(DefaultExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        log.debug("#toResponse - An error occurred: ", exception);
        return Response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .entity(exception.getMessage())
                .build();
    }
}
