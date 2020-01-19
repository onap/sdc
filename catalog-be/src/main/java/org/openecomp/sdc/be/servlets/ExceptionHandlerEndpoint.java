package org.openecomp.sdc.be.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcabi.aspects.Loggable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "ExceptionHandling Endpoint")
@Produces(MediaType.APPLICATION_JSON)
@Controller
public class ExceptionHandlerEndpoint {
    private static final Logger log = Logger.getLogger(ExceptionHandlerEndpoint.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ComponentsUtils componentsUtils;

    ExceptionHandlerEndpoint(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    @GET
    @Path("/handleException")
    @ApiOperation(value = "Handle exception", httpMethod = "GET", response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 500, message = "Internal Error")})
    public Response sendError() {
        log.debug("Request is received");

        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
        return Response.status(responseFormat.getStatus())
                .entity(gson.toJson(responseFormat.getRequestError()))
                .build();
    }

}
