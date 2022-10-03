/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.externalapi.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.externalapi.servlet.representation.ServiceDistributionReqInfo;
import org.openecomp.sdc.be.externalapi.servlet.representation.ServiceDistributionRespInfo;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.servlets.AbstractValidationsServlet;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

/**
 * Created by chaya on 10/17/2017.
 */
@SuppressWarnings("ALL")
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tag(name = "SDCE-7 APIs")
@Server(url = "/sdc")
@Controller
public class ServiceActivationServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(ServiceActivationServlet.class);
    private final ServiceBusinessLogic serviceBusinessLogic;
    @Context
    private HttpServletRequest request;

    @Inject
    public ServiceActivationServlet(ComponentInstanceBusinessLogic componentInstanceBL,
                                    ComponentsUtils componentsUtils, ServletUtils servletUtils, ResourceImportManager resourceImportManager,
                                    ServiceBusinessLogic serviceBusinessLogic) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.serviceBusinessLogic = serviceBusinessLogic;
    }

    /**
     * Activates a service on a specific environment
     */
    @POST
    @Path("/services/{serviceUUID}/distribution/{opEnvId}/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "activate a service", method = "POST", summary = "Activates a service", responses = {
        @ApiResponse(responseCode = "202", description = "ECOMP component is authenticated and required service may be distributed"),
        @ApiResponse(responseCode = "400", description = "Missing  X-ECOMP-InstanceID  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Error: Requested '%1' (uuid) resource was not found - SVC4063"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed  :  Invalid HTTP method type used ( PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The request failed either due to internal SDC problem. ECOMP Component should continue the attempts to get the needed information - POL5000"),
        @ApiResponse(responseCode = "400", description = "Invalid field format. One of the provided fields does not comply with the field rules - SVC4126"),
        @ApiResponse(responseCode = "400", description = "Missing request body. The post request did not contain the expected body - SVC4500"),
        @ApiResponse(responseCode = "400", description = "The resource name is missing in the request body - SVC4062"),
        @ApiResponse(responseCode = "409", description = "Service state is invalid for this action"),
        @ApiResponse(responseCode = "502", description = "The server was acting as a gateway or proxy and received an invalid response from the upstream server")})
    @PermissionAllowed({AafPermission.PermNames.WRITE_VALUE})
    public Response activateServiceExternal(
        @Parameter(description = "Determines the format of the body of the request", required = true) @HeaderParam(value = Constants.CONTENT_TYPE_HEADER) String contentType,
        @Parameter(description = "The user id", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Parameter(description = "The serviceUUid to activate", required = true) @PathParam("serviceUUID") final String serviceUUID,
        @Parameter(description = "The operational environment on which to activate the service on", required = true) @PathParam("opEnvId") final String opEnvId,
        String data) throws IOException {
        init();
        ResponseFormat responseFormat = null;
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug("Start handle request of {}", url);
        User modifier = new User();
        try {
            Wrapper<ResponseFormat> responseWrapper = validateRequestHeaders(instanceIdHeader, userId);
            if (responseWrapper.isEmpty()) {
                modifier.setUserId(userId);
                log.debug("modifier id is {}", userId);
                ServiceDistributionReqInfo reqMetadata = convertJsonToActivationMetadata(data);
                Either<String, ResponseFormat> distResponse = serviceBusinessLogic
                    .activateServiceOnTenantEnvironment(serviceUUID, opEnvId, modifier, reqMetadata);
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
            throw e;
        } finally {
            getComponentsUtils()
                .auditExternalActivateService(responseFormat, new DistributionData(instanceIdHeader, requestURI), requestId, serviceUUID, modifier);
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
        if (StringUtils.isEmpty(header)) {
            log.debug("MissingUSER_ID");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.AUTH_FAILED);
            responseWrapper.setInnerElement(responseFormat);
        }
    }
}
