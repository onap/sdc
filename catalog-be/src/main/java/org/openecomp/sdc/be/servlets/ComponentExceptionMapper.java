package org.openecomp.sdc.be.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Component
@Provider
public class ComponentExceptionMapper implements ExceptionMapper<ComponentException> {

    private final ComponentsUtils componentsUtils;
    protected Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ComponentExceptionMapper(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Override
    public Response toResponse(ComponentException componentException) {
        ResponseFormat responseFormat = componentException.getResponseFormat();
        if (componentException.getResponseFormat()==null) {
            responseFormat = componentsUtils.getResponseFormat(componentException.getActionStatus(), componentException.getParams());
        }

        return Response.status(responseFormat.getStatus())
                .entity(gson.toJson(responseFormat.getRequestError()))
                .build();
    }

}
