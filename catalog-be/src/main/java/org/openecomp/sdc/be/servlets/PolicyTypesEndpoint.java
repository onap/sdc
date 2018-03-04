package org.openecomp.sdc.be.servlets;

import com.jcabi.aspects.Loggable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.openecomp.sdc.be.components.impl.PolicyTypeBusinessLogic;
import org.openecomp.sdc.be.mixin.PolicyTypeMixin;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.view.ResponseView;
import org.openecomp.sdc.common.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "policy types resource")
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PolicyTypesEndpoint extends BeGenericServlet{

    private static final Logger log = LoggerFactory.getLogger(PolicyTypesEndpoint.class);

    private final PolicyTypeBusinessLogic policyTypeBusinessLogic;

    public PolicyTypesEndpoint(PolicyTypeBusinessLogic policyTypeBusinessLogic) {
        this.policyTypeBusinessLogic = policyTypeBusinessLogic;
    }

    @GET
    @Path("/policyTypes")
    @ApiOperation(value = "Get policy types ", httpMethod = "GET", notes = "Returns policy types", response = PolicyTypeDefinition.class, responseContainer="List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "policy types found"),
                            @ApiResponse(code = 403, message = "Restricted operation"),
                            @ApiResponse(code = 500, message = "The GET request failed due to internal SDC problem.")})
    @ResponseView(mixin = {PolicyTypeMixin.class})
    public Response getPolicyTypes(@ApiParam(value = "An optional parameter to indicate the type of the container from where this call is executed")
                                   @QueryParam("internalComponentType") String internalComponentType,
                                   @ApiParam(value = "The user id", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        log.debug("(get) Start handle request of GET policyTypes");
        try {
            return policyTypeBusinessLogic.getAllPolicyTypes(userId, internalComponentType)
                    .either(this::buildOkResponse,
                            this::buildErrorResponse);
        } catch (Exception e) {
            log.error("#getPolicyTypes - Exception occurred during get policy types", e);
            return buildGeneralErrorResponse();
        }
    }

}
