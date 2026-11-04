/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.openecomp.sdc.be.distribution.servlet;

import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.Servers;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.distribution.DistributionBusinessLogic;
import org.openecomp.sdc.be.distribution.api.client.KafkaDataResponse;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.BeGenericServlet;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

/**
 * This Servlet serves external users for distribution purposes.
 *
 * @author tgitelman
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1")
@Tags({@Tag(name = "SDCE-6 APIs")})
@Servers({@Server(url = "/sdc")})
@Controller
public class DistributionServlet extends BeGenericServlet {

    private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";
    private static final Logger log = Logger.getLogger(DistributionServlet.class);
    private final DistributionBusinessLogic distributionLogic;
    @Context
    private HttpServletRequest request;

    @Inject
    public DistributionServlet(ComponentsUtils componentsUtils, DistributionBusinessLogic distributionLogic) {
        super(componentsUtils);
        this.distributionLogic = distributionLogic;
    }

    /**
     * @param requestId UUID to track the incoming request
     * @param instanceId UUID to identify the requesting instance
     * @param accept Determines the format of the body of the response
     * @param authorization Username and password auth towards SDC
     * @return KafkaDataResponse (Kafka bootstrap server and topic list to be used by clients)
     */
    @GET
    @Path("/distributionKafkaData")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Kafka data", method = "GET", summary = "return the kafka cluster and topic list", responses = {
        @ApiResponse(responseCode = "200", description = "ECOMP component is authenticated and kafka endpoint and topic list is returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = KafkaDataResponse.class)))),
        @ApiResponse(responseCode = "400", description = "Missing 'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its credentials  for  Basic Authentication - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed: Invalid HTTP method type used ( PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000")})
    public Response getKafkaData(
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) String instanceId,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        ResponseFormat responseFormat;
        if (instanceId == null) {
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            getComponentsUtils().auditGetUebCluster(null, responseFormat.getStatus().toString(), responseFormat.getFormattedMessage());
            return buildErrorResponse(responseFormat);
        }
        try {
            Response response;
            Either<KafkaDataResponse, ResponseFormat> actionResponse = distributionLogic.getKafkaData();
            if (actionResponse.isRight()) {
                responseFormat = actionResponse.right().value();
                response = buildErrorResponse(responseFormat);
            } else {
                responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
                response = buildOkResponse(responseFormat, actionResponse.left().value());
            }
            getComponentsUtils().auditGetUebCluster(instanceId, responseFormat.getStatus().toString(), responseFormat.getFormattedMessage());
            return response;
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("failed to get kafka cluster and topic list from configuration");
            log.debug("failed to get kafka cluster and topic list from configuration", e);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            getComponentsUtils().auditGetUebCluster(instanceId, responseFormat.getStatus().toString(), responseFormat.getFormattedMessage());
            return buildErrorResponse(responseFormat);
        }
    }

    /**
     * Returns list of valid artifact types for validation done in the distribution client.<br> The list is the representation of the values of the
     * enum ArtifactTypeEnum.
     */
    @GET
    @Path("/artifactTypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Artifact types list", method = "GET", summary = "Fetches available artifact types list", responses = {
        @ApiResponse(responseCode = "200", description = "Artifact types list fetched successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed  :  Invalid HTTP method type used to  register for  distribution ( POST,PUT,DELETE  will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The registration failed due to internal SDC problem or Cambria Service failure ECOMP Component  should  continue the attempts to  register for  distribution - POL5000")})
    public Response getValidArtifactTypes(
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) String instanceId,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        Wrapper<Response> responseWrapper = new Wrapper<>();
        //TODO check if in use
        validateHeaders(responseWrapper, request, AuditingActionEnum.GET_VALID_ARTIFACT_TYPES);
        if (responseWrapper.isEmpty()) {
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), ArtifactTypeEnum.values());
        } else {
            return responseWrapper.getInnerElement();
        }
    }

    private void validateHeaders(Wrapper<Response> responseWrapper, HttpServletRequest request, AuditingActionEnum auditingAction) {
        if (request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER) == null) {
            Response missingHeaderResponse = buildErrorResponse(
                distributionLogic.getResponseFormatManager().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID));
            responseWrapper.setInnerElement(missingHeaderResponse);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            getComponentsUtils().auditMissingInstanceIdAsDistributionEngineEvent(auditingAction, responseFormat.getStatus().toString());
        }
    }
}
