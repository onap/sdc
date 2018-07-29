package org.openecomp.sdc.be.servlets;

import com.jcabi.aspects.Loggable;
import io.swagger.annotations.*;
import org.openecomp.sdc.be.components.upgrade.UpgradeBusinessLogic;
import org.openecomp.sdc.be.components.upgrade.UpgradeRequest;
import org.openecomp.sdc.be.components.upgrade.UpgradeStatus;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.utils.JsonParserUtils;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "policy types resource")
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AutomatedUpgradeEndpoint extends BeGenericServlet {
    private static final Logger log = Logger.getLogger(PolicyTypesEndpoint.class);

    private final UpgradeBusinessLogic businessLogic;

    public AutomatedUpgradeEndpoint(UpgradeBusinessLogic businessLogic) {
        this.businessLogic = businessLogic;
    }
    
    
    @POST
    @Path("/{componentType}/{componentId}/automatedupgrade")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Autometed upgrade", httpMethod = "POST", notes = "....", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response autometedUpgrade(@PathParam("componentType") final String componentType, @Context final HttpServletRequest request, @PathParam("componentId") final String componentId, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @ApiParam(value = "json describes upgrade request", required = true) String data) {

     
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(POST) Start handle request of {}", url);

        try {
            
            List<UpgradeRequest> inputsToUpdate = JsonParserUtils.toList(data, UpgradeRequest.class);
            
            if (log.isDebugEnabled()) {
                log.debug("Received upgrade requests size is {}", inputsToUpdate == null ? 0 : inputsToUpdate.size());
            }
            UpgradeStatus actionResponse = businessLogic.automatedUpgrade(componentId, inputsToUpdate, userId);
            
            return actionResponse.getStatus() == ActionStatus.OK ? buildOkResponse(actionResponse) : buildErrorResponse(actionResponse.getError());

        } catch (Exception e) {
            log.error("#autometedUpgrade - Exception occurred during autometed Upgrade", e);
             return buildGeneralErrorResponse();
        }
    }
    
    @GET
    @Path("/{componentType}/{componentId}/dependencies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Autometed upgrade", httpMethod = "POST", notes = "....", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response getComponentDependencies(@PathParam("componentType") final String componentType, @Context final HttpServletRequest request, @PathParam("componentId") final String componentId, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @ApiParam(value = "Consumer Object to be created", required = true) List<String> data) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(GET) Start handle request of {}", url);

        try {
            return  businessLogic.getComponentDependencies(componentId, userId)
                    .either(this::buildOkResponse, this::buildErrorResponse);  
        } catch (Exception e) {
            log.error("#getServicesForComponent - Exception occurred during autometed Upgrade", e);
            return buildGeneralErrorResponse();
        }
     
        
    }
}
