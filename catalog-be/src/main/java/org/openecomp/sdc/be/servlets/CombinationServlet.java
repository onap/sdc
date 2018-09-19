/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.openecomp.sdc.be.components.impl.CombinationBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Combination;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.ui.model.UiCombination;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Combination Catalog", description = "Combination Servlet")
@Singleton
public class CombinationServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(CombinationServlet.class);

    @POST
    @Path("/combination/{serviceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create Combination", httpMethod = "POST", notes = "Returns created combination", response = Combination.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Combination created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "Combination already exist") })
    public Response createCombination(@ApiParam(value = "Combination object to be created", required = true) String data, @PathParam("serviceId") final String serviceId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        userId = (userId != null) ? userId : request.getHeader(Constants.USER_ID_HEADER);
        init();

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);

        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);

        Response response;
        try {

            CombinationBusinessLogic businessLogic = getCombinationBL(context);

            Either<Service, ResponseFormat> getServiceResponse = businessLogic.getService(serviceId.toLowerCase(), modifier);
            if (getServiceResponse.isRight()) {
                log.debug("failed to get service");
                response = buildErrorResponse(getServiceResponse.right().value());
                return response;
            }
            Service service = getServiceResponse.left().value();

            Either<UiCombination, ResponseFormat> convertResponse = parseToCombination(data);
            if (convertResponse.isRight()) {
                log.debug("failed to parse combination");
                response = buildErrorResponse(convertResponse.right().value());
                return response;
            }
            UiCombination uiCombination = convertResponse.left().value();
            Combination combination = new Combination(uiCombination);
            Either<Combination, ResponseFormat> createdCombination = businessLogic.createCombination(combination, service);
            if (createdCombination.isRight()) {
                log.error("Failed to create Combination");
                response = buildErrorResponse(createdCombination.right().value());
                return response;
            }
            Object representation = RepresentationUtils.toRepresentation(createdCombination.left().value());
            response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), representation);
            return response;

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Combination");
            log.debug("create combination failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;
        }
    }

    private Either<UiCombination, ResponseFormat> parseToCombination(String data) {
        try {
            UiCombination combination = gson.fromJson(data, UiCombination.class);
            return Either.left(combination);
        } catch (Exception e) {
            return Either.right(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/combinationInstance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create ComponentInstance", httpMethod = "POST", notes = "Returns created ComponentInstance", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Component created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "Component instance already exist") })
    public Response createCombinationInstance(@ApiParam(value = "RI object to be created", required = true) String data, @PathParam("componentId") final String containerComponentId,
                                              @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
                                              @HeaderParam(value = Constants.USER_ID_HEADER) @ApiParam(value = "USER_ID of modifier user", required = true) String userId, @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();

        try {

            ComponentInstance componentInstance = RepresentationUtils.fromRepresentation(data, ComponentInstance.class);
            if (componentInstance == null) {
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT, containerComponentType));
            }
            CombinationBusinessLogic businessLogic = getCombinationBL(context);
            if (businessLogic == null) {
                log.debug("unsupported type", containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            ResponseFormat actionResponse = businessLogic.createCombinationInstance(containerComponentType, containerComponentId, userId, componentInstance);

            return buildOkResponse(actionResponse);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Component Instance");
            log.debug("create component instance failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @GET
    @Path("/validateCombinations/{combinationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve Combination", httpMethod = "GET", notes = "Returns combination according to combinationId", response = Combination.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Combination found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Combination not found") })
    public Response validateCombinationById(@PathParam("combinationId") final String combinationId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);

        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);

        Response response;
        try {
            CombinationBusinessLogic businessLogic = getCombinationBL(context);
            log.trace("get combination with id {}", combinationId);
            Either<Boolean, ResponseFormat> actionResponse = businessLogic.validateCombinationExists(combinationId);

            if (actionResponse.isRight()) {
                log.debug("failed to get combination");
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }
            Object combination = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), combination);

        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Resource");
            log.debug("get combination failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    @GET
    @Path("/combinations")
    @ApiOperation(value = "Retrieve Resource", httpMethod = "GET", notes = "Returns resource according to resourceId", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Resource found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Resource not found") })
    public Response getCombinations(@PathParam("resourceId") final String resourceId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        Response response;
        ServletContext context = request.getSession().getServletContext();
        try {
            CombinationBusinessLogic businessLogic = getCombinationBL(context);
            Either<List<UiCombination>, ResponseFormat> allCombinations = businessLogic.getAllCombinations();
            if (allCombinations.isRight()) {
                log.error("Failed to get Combinations");
                response = buildOkResponse(allCombinations.left().value());
                return response;
            }
            Object representation = RepresentationUtils.toRepresentation(allCombinations.left().value());
            response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation);
            return response;

        } catch (Exception e) {
            log.error("Exception occurred", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    @GET
    @Path("/combinations/{combinationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve Combination", httpMethod = "GET", notes = "Returns resource according to resourceId", response = Combination.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Combination found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Combination not found") })
    public Response getCombinationById(@PathParam("combinationId") final String combinationId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}" , url);

        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}" , userId);

        Response response;

        try {
            CombinationBusinessLogic businessLogic = getCombinationBL(context);
            log.trace("get combination with id {}", combinationId);
            Either<Combination, ResponseFormat> actionResponse = businessLogic.getCombinationById(combinationId);

            if (actionResponse.isRight()) {
                log.debug("failed to get combination");
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }
            Object combination = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), combination);

        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Combination");
            log.debug("get combination failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }
}
