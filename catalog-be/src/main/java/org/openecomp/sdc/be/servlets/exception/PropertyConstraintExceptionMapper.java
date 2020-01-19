package org.openecomp.sdc.be.servlets.exception;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.tosca.constraints.exception.PropertyConstraintException;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Component
@Provider
public class PropertyConstraintExceptionMapper implements ExceptionMapper<PropertyConstraintException>  {

    private static final Logger log = Logger.getLogger(PropertyConstraintExceptionMapper.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ComponentsUtils componentsUtils;

    public PropertyConstraintExceptionMapper(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @Override
    public Response toResponse(PropertyConstraintException exception) {
        log.debug("#toResponse - An error occurred: ", exception);
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(exception.getActionStatus(), exception.getParams());
        return Response.status(responseFormat.getStatus())
                .entity(gson.toJson(responseFormat.getRequestError()))
                .build();
    }

}
