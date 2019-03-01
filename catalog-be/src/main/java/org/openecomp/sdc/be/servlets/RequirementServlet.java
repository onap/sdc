/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.servlets;

import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.openecomp.sdc.be.components.impl.RequirementBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Requirement Servlet", description = "Requirement Servlet")
@Singleton
public class RequirementServlet extends AbstractValidationsServlet {
    private static final Logger LOGGER = Logger.getLogger(RequirementServlet.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/requirements")
    @ApiOperation(value = "Create requirements on resource", httpMethod = "POST",
            notes = "Create requirements on resource", response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Create requirements"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "requirement already exist")})
    public Response createRequirementsOnResource(
            @ApiParam(value = "Requirement to create", required = true) String data,
            @ApiParam(value = "Resource Id") @PathParam("resourceId") String resourceId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "resources" , resourceId, request,
                userId, false, "createRequirements");
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/requirements")
    @ApiOperation(value = "Update Requirements on resource", httpMethod = "PUT",
            notes = "Update Requirements on resource", response = RequirementDefinition.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Update Requirements"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response updateRequirementsOnResource(
            @ApiParam(value = "Requirements to update", required = true) String data,
            @ApiParam(value = "Component Id") @PathParam("resourceId") String resourceId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "resources", resourceId, request,
                userId, true, "updateRequirements");
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/requirements/{requirementId}")
    @ApiOperation(value = "Get Requirement from resource", httpMethod = "GET",
            notes = "GET Requirement from resource", response = RequirementDefinition.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "GET requirement"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response getRequirementsFromResource(
            @ApiParam(value = "Resource Id") @PathParam("resourceId") String resourceId,
            @ApiParam(value = "Requirement Id") @PathParam("requirementId") String requirementId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        return get(requirementId, resourceId, request, userId);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/requirements/{requirementId}")
    @ApiOperation(value = "Delete requirements from resource", httpMethod = "DELETE",
            notes = "Delete requirements from resource", response = RequirementDefinition.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Delete requirement"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response deleteRequirementsFromResource(
            @ApiParam(value = "Resource Id") @PathParam("resourceId") String resourceId,
            @ApiParam(value = "requirement Id") @PathParam("requirementId") String requirementId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return delete(requirementId, resourceId, request, userId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/requirements")
    @ApiOperation(value = "Create requirements on service", httpMethod = "POST",
            notes = "Create requirements on service", response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Create Requirements"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "Requirement already exist")})
    public Response createRequirementsOnService(
            @ApiParam(value = "Requirements to create", required = true) String data,
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "services" , serviceId, request, userId,
                false , "createRequirements");
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/requirements")
    @ApiOperation(value = "Update requirements on service", httpMethod = "PUT",
            notes = "Update requirements on service", response = RequirementDefinition.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Update requirements"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response updateRequirementsOnService(
            @ApiParam(value = "Requirements to update", required = true) String data,
            @ApiParam(value = "Component Id") @PathParam("serviceId") String serviceId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "services", serviceId, request, userId,
                true, "updateRequirements");
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/requirements/{requirementId}")
    @ApiOperation(value = "Get requirement from service", httpMethod = "GET",
            notes = "GET requirement from service", response = RequirementDefinition.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "GET Requirements"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response getRequirementsOnService(
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @ApiParam(value = "Requirement Id") @PathParam("requirementId") String requirementId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        return get(requirementId, serviceId, request, userId);
    }


    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/requirements/{requirementId}")
    @ApiOperation(value = "Delete requirement from service", httpMethod = "DELETE",
            notes = "Delete requirement from service", response = RequirementDefinition.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Delete Requirements"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response deleteRequirementsOnService(
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @ApiParam(value = "Requirement Id") @PathParam("requirementId") String requirementId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return delete(requirementId, serviceId, request, userId);
    }


    private Response createOrUpdate (String data, String componentType, String componentId,
                                     HttpServletRequest request, String userId,
                                     boolean isUpdate, String errorContext) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();

        User modifier = new User();
        modifier.setUserId(userId);
        LOGGER.debug("Start create or update request of {} with modifier id {}", url, userId);

        try {
            String componentIdLower = componentId.toLowerCase();
            RequirementBusinessLogic businessLogic = getRequirementBL(context);

            Either<List<RequirementDefinition>, ResponseFormat> mappedRequirementDataEither =
                    getMappedRequirementData(data, modifier, ComponentTypeEnum.findByParamName(componentType));
            if(mappedRequirementDataEither.isRight()) {
                LOGGER.error("Failed to create or update requirements");
                return buildErrorResponse(mappedRequirementDataEither.right().value());
            }
            List<RequirementDefinition> mappedRequirementData = mappedRequirementDataEither.left().value();
            Either<List<RequirementDefinition>, ResponseFormat> actionResponse;
            if(isUpdate) {
                actionResponse = businessLogic.updateRequirements(componentIdLower, mappedRequirementData, modifier,
                        errorContext, true);
            } else {
                actionResponse = businessLogic.createRequirements(componentIdLower, mappedRequirementData, modifier,
                        errorContext, true);
            }

            if (actionResponse.isRight()) {
                LOGGER.error("Failed to create or update requirements");
                return buildErrorResponse(actionResponse.right().value());
            }

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                    actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("requirements create or update");
            LOGGER.error("Failed to create or update requirements with an error", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Response get (String requirementIdToGet,  String componentId,
                          HttpServletRequest request, String userId){
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();

        User modifier = new User();
        modifier.setUserId(userId);
        LOGGER.debug("Start get request of {} with modifier id {}", url, userId);

        try {
            String componentIdLower = componentId.toLowerCase();
            RequirementBusinessLogic businessLogic = getRequirementBL(context);

            Either<RequirementDefinition, ResponseFormat> actionResponse = businessLogic
                    .getRequirement(componentIdLower, requirementIdToGet, modifier, true);
            if (actionResponse.isRight()) {
                LOGGER.error("failed to get requirements");
                return buildErrorResponse(actionResponse.right().value());
            }
            Object result = RepresentationUtils.toFilteredRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get requirements");
            LOGGER.error("get requirements failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Response delete (String requirementId, String componentId, HttpServletRequest
                                        request, String userId){

        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();

        User modifier = new User();
        modifier.setUserId(userId);
        LOGGER.debug("Start delete request of {} with modifier id {}", url, userId);

        try {
            String componentIdLower = componentId.toLowerCase();
            RequirementBusinessLogic businessLogic = getRequirementBL(context);

            Either<RequirementDefinition, ResponseFormat> actionResponse = businessLogic
                    .deleteRequirement(componentIdLower, requirementId, modifier, true);
            if (actionResponse.isRight()) {
                LOGGER.error("failed to delete requirements");
                return buildErrorResponse(actionResponse.right().value());
            }
            Object result = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete requirements");
            LOGGER.error("Delete requirements failed with an error", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Either<List<RequirementDefinition>, ResponseFormat> getMappedRequirementData(String inputJson, User user,
                                                                 ComponentTypeEnum componentTypeEnum){
        Either<UiComponentDataTransfer, ResponseFormat> mappedData = getComponentsUtils()
                .convertJsonToObjectUsingObjectMapper(inputJson, user, UiComponentDataTransfer.class,
                        AuditingActionEnum.CREATE_RESOURCE, componentTypeEnum);
        Optional<List<RequirementDefinition>> requirementDefinitionList = mappedData.left().value()
                .getRequirements().values().stream().findFirst();
        return requirementDefinitionList.<Either<List<RequirementDefinition>, ResponseFormat>>
                map(Either::left).orElseGet(() -> Either.right(getComponentsUtils()
                .getResponseFormat(ActionStatus.GENERAL_ERROR)));
    }
}
