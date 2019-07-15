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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.inject.Inject;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.DistributionMonitoringBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.DistributionStatusListResponse;
import org.openecomp.sdc.be.info.DistributionStatusOfServiceListResponce;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Root resource (exposed at "/" path)
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Distribution Service Servlet", description = "Distribution Service Servlet")
@Singleton
public class DistributionServiceServlet extends BeGenericServlet {
    private static final Logger log = Logger.getLogger(DistributionServiceServlet.class);

    @Inject
    public DistributionServiceServlet(UserBusinessLogic userBusinessLogic,
        ComponentsUtils componentsUtils,
        DistributionMonitoringBusinessLogic distributionMonitoringLogic) {
        super(userBusinessLogic, componentsUtils);
        this.distributionMonitoringLogic = distributionMonitoringLogic;
    }

    private DistributionMonitoringBusinessLogic distributionMonitoringLogic;

    @GET
    @Path("/services/{serviceUUID}/distribution")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve Distributions", httpMethod = "GET", notes = "Returns list  bases on the  information extracted from  Auditing Records according to service uuid", response = DistributionStatusListResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Service found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Service not found") })
    public Response getServiceById(@PathParam("serviceUUID") final String serviceUUID, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        Response response = null;
        ResponseFormat responseFormat = null;

        try {
            Either<DistributionStatusOfServiceListResponce, ResponseFormat> actionResponse = distributionMonitoringLogic.getListOfDistributionServiceStatus(serviceUUID, userId);

            if (actionResponse.isRight()) {

                responseFormat = actionResponse.right().value();
                response = buildErrorResponse(responseFormat);
            } else {
                responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
                response = buildOkResponse(responseFormat, actionResponse.left().value());

            }

            return response;

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Distribution list for Service");
            log.debug("failed to get service distribution statuses", e);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);

            response = buildErrorResponse(responseFormat);
            return response;
        }

    }

    @GET
    @Path("/services/distribution/{did}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve Distributions", httpMethod = "GET", notes = "Return  the  list  of  distribution status objects", response = DistributionStatusListResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Service found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Status not found") })
    public Response getListOfDistributionStatuses(@PathParam("did") final String did, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        Response response = null;
        ResponseFormat responseFormat = null;

        try {
            Either<DistributionStatusListResponse, ResponseFormat> actionResponse = distributionMonitoringLogic.getListOfDistributionStatus(did, userId);

            if (actionResponse.isRight()) {

                responseFormat = actionResponse.right().value();
                log.debug("failed to fount statuses for did {} {}", did, responseFormat);
                response = buildErrorResponse(responseFormat);
            } else {

                responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
                log.debug("success to fount statuses for did {} {}", did, actionResponse.left().value());
                response = buildOkResponse(responseFormat, actionResponse.left().value());

            }

            return response;

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Distribution Status");
            log.debug("failed to get distribution status ", e);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);

            response = buildErrorResponse(responseFormat);
            return response;
        }

    }

}
