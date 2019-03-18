package org.openecomp.sdc.be.externalapi.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.externalapi.servlet.representation.ServiceDistributionReqInfo;
import org.openecomp.sdc.be.externalapi.servlet.representation.ServiceDistributionRespInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.servlets.AbstractValidationsServlet;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by chaya on 10/17/2017.
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Service Activation External Servlet", description = "This Servlet serves external users for activating a specific service")
@Singleton
public class ServiceActivationServlet extends AbstractValidationsServlet {

    @Context
    private HttpServletRequest request;

    private static final Logger log = Logger.getLogger(ServiceActivationServlet.class);

    /**
     * Activates a service on a specific environment
     *
     * @param serviceUUID
     * @param opEnvId
     * @param userId
     * @param instanceIdHeader
     * @return
     */
    @POST
    @Path("/services/{serviceUUID}/distribution/{opEnvId}/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "activate a service", httpMethod = "POST", notes = "Activates a service")
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "ECOMP component is authenticated and required service may be distributed"),
            @ApiResponse(code = 400, message = "Missing  X-ECOMP-InstanceID  HTTP header - POL5001"),
            @ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic Authentication credentials - POL5002"),
            @ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
            @ApiResponse(code = 404, message = "Error: Requested '%1' (uuid) resource was not found - SVC4063"),
            @ApiResponse(code = 405, message = "Method  Not Allowed  :  Invalid HTTP method type used ( PUT,DELETE,POST will be rejected) - POL4050"),
            @ApiResponse(code = 500, message = "The request failed either due to internal SDC problem. ECOMP Component should continue the attempts to get the needed information - POL5000"),
            @ApiResponse(code = 400, message = "Invalid field format. One of the provided fields does not comply with the field rules - SVC4126"),
            @ApiResponse(code = 400, message = "Missing request body. The post request did not contain the expected body - SVC4500"),
            @ApiResponse(code = 400, message = "The resource name is missing in the request body - SVC4062"),
            @ApiResponse(code = 409, message = "Service state is invalid for this action"),
            @ApiResponse(code = 502, message = "The server was acting as a gateway or proxy and received an invalid response from the upstream server")})
    public Response activateServiceExternal(
            @ApiParam(value = "Determines the format of the body of the request", required = true) @HeaderParam(value = Constants.CONTENT_TYPE_HEADER) String contentType,
            @ApiParam(value = "The user id", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
            @ApiParam(value = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
            @ApiParam(value = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
            @ApiParam(value = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
            @ApiParam(value = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
            @ApiParam(value = "The serviceUUid to activate", required = true) @PathParam("serviceUUID") final String serviceUUID,
            @ApiParam(value = "The operational environment on which to activate the service on", required = true) @PathParam("opEnvId") final String opEnvId,
            String data) {

        init();

        ResponseFormat responseFormat = null;
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug("Start handle request of {}", url);

        ServletContext context = request.getSession().getServletContext();
        User modifier = new User();

        try {

            Wrapper<ResponseFormat> responseWrapper = validateRequestHeaders(instanceIdHeader, userId);

            if (responseWrapper.isEmpty()) {
                modifier.setUserId(userId);
                log.debug("modifier id is {}", userId);

                ServiceBusinessLogic businessLogic = getServiceBL(context);
                ServiceDistributionReqInfo reqMetadata = convertJsonToActivationMetadata(data);
                Either<String, ResponseFormat> distResponse = businessLogic.activateServiceOnTenantEnvironment(serviceUUID, opEnvId, modifier, reqMetadata);

                if (distResponse.isRight()) {
                    log.debug("failed to activate service distribution");
                    responseFormat = distResponse.right().value();
                    return buildErrorResponse(responseFormat);
                }
                String distributionId = distResponse.left().value();
                Object result = RepresentationUtils.toRepresentation(new ServiceDistributionRespInfo(distributionId));
                responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.ACCEPTED);
                return buildOkResponse(responseFormat, result);
            } else {
                log.debug("request instanceId/userId header validation failed");
                responseFormat = responseWrapper.getInnerElement();
                return buildErrorResponse(responseFormat);
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Activate Distribution");
            log.debug("activate distribution failed with exception", e);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        } finally {
            getComponentsUtils().auditExternalActivateService(responseFormat,
                    new DistributionData(instanceIdHeader, requestURI), requestId, serviceUUID, modifier);
        }
    }

    private Wrapper<ResponseFormat> validateRequestHeaders(String instanceIdHeader, String userId) {
        Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
        if (responseWrapper.isEmpty()) {
            validateXECOMPInstanceIDHeader(instanceIdHeader, responseWrapper);
        }
        if (responseWrapper.isEmpty()) {
            validateHttpCspUserIdHeader(userId, responseWrapper);
        }
        return responseWrapper;
    }

    private ServiceDistributionReqInfo convertJsonToActivationMetadata(String data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(data, ServiceDistributionReqInfo.class);
        } catch (IOException e) {
            log.error("#convertJsonToActivationMetadata - json deserialization failed with error: ", e);
            return new ServiceDistributionReqInfo(null);
        }
    }

    @Override
    protected void validateHttpCspUserIdHeader(String header, Wrapper<ResponseFormat> responseWrapper) {
        ResponseFormat responseFormat;
        if( StringUtils.isEmpty(header)){
            log.debug("MissingUSER_ID");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.AUTH_FAILED);
            responseWrapper.setInnerElement(responseFormat);
        }
    }
}



