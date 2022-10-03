/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.servlets;

import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.v3.oas.annotations.Operation;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.DistributionMonitoringBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.DistributionStatusListResponse;
import org.openecomp.sdc.be.info.DistributionStatusOfServiceListResponce;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

/**
 * Root resource (exposed at "/" path)
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tags({@Tag(name = "SDCE-5 APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Controller
public class DistributionServiceServlet extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(DistributionServiceServlet.class);
    private DistributionMonitoringBusinessLogic distributionMonitoringLogic;

    @Inject
    public DistributionServiceServlet(ComponentsUtils componentsUtils,
                                      DistributionMonitoringBusinessLogic distributionMonitoringLogic) {
        super(componentsUtils);
        this.distributionMonitoringLogic = distributionMonitoringLogic;
    }

    @GET
    @Path("/services/{serviceUUID}/distribution")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve Distributions", method = "GET", summary = "Returns list  bases on the  information extracted from  Auditing Records according to service uuid", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = DistributionStatusListResponse.class)))),
        @ApiResponse(responseCode = "200", description = "Service found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Service not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getServiceById(@PathParam("serviceUUID") final String serviceUUID, @Context final HttpServletRequest request,
                                   @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            Either<DistributionStatusOfServiceListResponce, ResponseFormat> actionResponse = distributionMonitoringLogic
                .getListOfDistributionServiceStatus(serviceUUID, userId);
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            } else {
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Distribution list for Service");
            log.debug("failed to get service distribution statuses", e);
            throw e;
        }
    }

    @GET
    @Path("/services/distribution/{did}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve Distributions", method = "GET", summary = "Return  the  list  of  distribution status objects", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = DistributionStatusListResponse.class)))),
        @ApiResponse(responseCode = "200", description = "Service found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Status not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getListOfDistributionStatuses(@PathParam("did") final String did, @Context final HttpServletRequest request,
                                                  @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        try {
            Either<DistributionStatusListResponse, ResponseFormat> actionResponse = distributionMonitoringLogic
                .getListOfDistributionStatus(did, userId);
            if (actionResponse.isRight()) {
                ResponseFormat responseFormat = actionResponse.right().value();
                log.debug("failed to fount statuses for did {} {}", did, responseFormat);
                return buildErrorResponse(responseFormat);
            } else {
                ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
                log.debug("success to fount statuses for did {} {}", did, actionResponse.left().value());
                return buildOkResponse(responseFormat, actionResponse.left().value());
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Distribution Status");
            log.debug("failed to get distribution status ", e);
            throw e;
        }
    }
}
