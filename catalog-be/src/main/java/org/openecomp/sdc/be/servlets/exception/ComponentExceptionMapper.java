package org.openecomp.sdc.be.servlets.exception;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Component
@Provider
public class ComponentExceptionMapper implements ExceptionMapper<ComponentException> {

    private static final Logger log = Logger.getLogger(ComponentExceptionMapper.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ComponentsUtils componentsUtils;

    public ComponentExceptionMapper(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Override
    public Response toResponse(ComponentException exception) {
        // TODO log this? BeEcompErrorManager.getInstance().logBeRestApiGeneralError(requestURI);
        log.debug("#toResponse - An error occurred: ", exception);
        ResponseFormat responseFormat = exception.getResponseFormat();
        if (exception.getResponseFormat()==null) {
            responseFormat = componentsUtils.getResponseFormat(exception.getActionStatus(), exception.getParams());
        }

        return Response.status(responseFormat.getStatus())
                .entity(gson.toJson(responseFormat.getRequestError()))
                .build();
    }

}
