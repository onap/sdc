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
import org.openecomp.sdc.be.components.impl.CapabilitiesBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
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
@Api(value = "Capability Servlet", description = "Capability Servlet")
@Singleton
public class CapabilityServlet extends AbstractValidationsServlet {
    private static final Logger LOGGER = Logger.getLogger(CapabilityServlet.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/capabilities")
    @ApiOperation(value = "Create Capabilities on resource", httpMethod = "POST",
            notes = "Create Capabilities on resource", response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Create Capabilities"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "Capability already exist")})
    public Response createCapabilitiesOnResource(
            @ApiParam(value = "Capability to create", required = true) String data,
            @ApiParam(value = "Resource Id") @PathParam("resourceId") String resourceId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "resources" , resourceId,
                request, userId, false, "createCapabilities");
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/capabilities")
    @ApiOperation(value = "Update Capabilities on resource", httpMethod = "PUT",
            notes = "Update Capabilities on resource", response = CapabilityDefinition.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Update Capabilities"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response updateCapabilitiesOnResource(
            @ApiParam(value = "Capabilities to update", required = true) String data,
            @ApiParam(value = "Component Id") @PathParam("resourceId") String resourceId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "resources", resourceId,
                request, userId, true, "updateCapabilities");
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/capabilities/{capabilityId}")
    @ApiOperation(value = "Get Capability from resource", httpMethod = "GET",
            notes = "GET Capability from resource", response = CapabilityDefinition.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "GET Capability"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response getCapabilityOnResource(
            @ApiParam(value = "Resource Id") @PathParam("resourceId") String resourceId,
            @ApiParam(value = "Capability Id") @PathParam("capabilityId") String capabilityId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        return get(capabilityId, resourceId, request, userId);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/capabilities/{capabilityId}")
    @ApiOperation(value = "Delete capability from resource", httpMethod = "DELETE",
            notes = "Delete capability from resource", response = CapabilityDefinition.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Delete capability"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response deleteCapabilityOnResource(
            @ApiParam(value = "capability Id") @PathParam("capabilityId") String capabilityId,
            @ApiParam(value = "Resource Id") @PathParam("resourceId") String resourceId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return delete(capabilityId, resourceId, request, userId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/capabilities")
    @ApiOperation(value = "Create Capabilities on service", httpMethod = "POST",
            notes = "Create Capabilities on service", response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Create Capabilities"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "Capability already exist")})
    public Response createCapabilitiesOnService(
            @ApiParam(value = "Capability to create", required = true) String data,
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "services" , serviceId,
                request, userId, false, "createCapabilities");
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/capabilities")
    @ApiOperation(value = "Update Capabilities on service", httpMethod = "PUT",
            notes = "Update Capabilities on service", response = CapabilityDefinition.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Update Capabilities"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response updateCapabilitiesOnService(
            @ApiParam(value = "Capabilities to update", required = true) String data,
            @ApiParam(value = "Component Id") @PathParam("serviceId") String serviceId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "services", serviceId,
                request, userId, true, "updateCapabilities");
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/capabilities/{capabilityId}")
    @ApiOperation(value = "Get Capability from service", httpMethod = "GET",
            notes = "GET Capability from service", response = CapabilityDefinition.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "GET Capability"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response getCapabilityOnService(
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @ApiParam(value = "Capability Id") @PathParam("capabilityId") String capabilityId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        return get(capabilityId, serviceId, request, userId);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/capabilities/{capabilityId}")
    @ApiOperation(value = "Delete capability from service", httpMethod = "DELETE",
            notes = "Delete capability from service", response = CapabilityDefinition.class)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Delete capability"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content")})
    public Response deleteCapabilityOnService(
            @ApiParam(value = "capability Id") @PathParam("capabilityId") String capabilityId,
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return delete(capabilityId, serviceId, request, userId);
    }

    private Response createOrUpdate (String data, String componentType, String componentId,
                                     HttpServletRequest request,
                                     String userId,
                                     boolean isUpdate,
                                     String errorContext) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();

        User modifier = new User();
        modifier.setUserId(userId);
        LOGGER.debug("Start create or update request of {} with modifier id {}", url, userId);
        try {
            String componentIdLower = componentId.toLowerCase();
            CapabilitiesBusinessLogic businessLogic = getCapabilitiesBL(context);

            Either<List<CapabilityDefinition>, ResponseFormat> mappedCapabilitiesDataEither
                    = getMappedCapabilitiesData(data, modifier, ComponentTypeEnum.findByParamName(componentType));
            if(mappedCapabilitiesDataEither.isRight()) {
                LOGGER.error("Failed to create or update capabilities");
                buildErrorResponse(mappedCapabilitiesDataEither.right().value());
            }
            List<CapabilityDefinition> mappedCapabilitiesData = mappedCapabilitiesDataEither.left().value();
            Either<List<CapabilityDefinition>, ResponseFormat> actionResponse;
            if(isUpdate) {
                actionResponse = businessLogic.updateCapabilities(componentIdLower,
                        mappedCapabilitiesData, modifier, errorContext, true);
            } else {
                actionResponse = businessLogic.createCapabilities(componentIdLower,
                        mappedCapabilitiesData, modifier, errorContext, true);
            }
            if (actionResponse.isRight()) {
                LOGGER.error("Failed to create or update capabilities");
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                    actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Capabilities create or update");
            LOGGER.error("Failed to create or update capabilities with an error", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Response get (String capabilityIdToGet,  String componentId,
                          HttpServletRequest request, String userId){
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();

        User modifier = new User();
        modifier.setUserId(userId);
        LOGGER.debug("Start get request of {} with modifier id {}", url, userId);

        try {
            String componentIdLower = componentId.toLowerCase();
            CapabilitiesBusinessLogic businessLogic = getCapabilitiesBL(context);

            Either<CapabilityDefinition, ResponseFormat> actionResponse = businessLogic
                    .getCapability(componentIdLower, capabilityIdToGet, modifier, true);
            if (actionResponse.isRight()) {
                LOGGER.error("failed to get capability");
                return buildErrorResponse(actionResponse.right().value());
            }
            Object result = RepresentationUtils.toFilteredRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get capability");
            LOGGER.error("get capabilities failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Response delete (String capabilityId, String componentId, HttpServletRequest
            request, String userId){

        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();

        User modifier = new User();
        modifier.setUserId(userId);
        LOGGER.debug("Start delete request of {} with modifier id {}", url, userId);

        try {
            String componentIdLower = componentId.toLowerCase();
            CapabilitiesBusinessLogic businessLogic = getCapabilitiesBL(context);

            Either<CapabilityDefinition, ResponseFormat> actionResponse = businessLogic
                    .deleteCapability(componentIdLower, capabilityId, modifier, true);
            if (actionResponse.isRight()) {
                LOGGER.error("failed to delete capability");
                return buildErrorResponse(actionResponse.right().value());
            }
            Object result = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete capability");
            LOGGER.error("Delete capability failed with an error", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Either<List<CapabilityDefinition>, ResponseFormat> getMappedCapabilitiesData(String inputJson, User user,
                                                                 ComponentTypeEnum componentTypeEnum){
        Either<UiComponentDataTransfer, ResponseFormat> mappedData = getComponentsUtils()
                .convertJsonToObjectUsingObjectMapper(inputJson, user, UiComponentDataTransfer.class,
                        AuditingActionEnum.CREATE_RESOURCE, componentTypeEnum);
        Optional<List<CapabilityDefinition>> capabilityDefinitionList =
                mappedData.left().value().getCapabilities().values().stream().findFirst();
        return capabilityDefinitionList.<Either<List<CapabilityDefinition>, ResponseFormat>>
                map(Either::left).orElseGet(() -> Either.right(getComponentsUtils()
                .getResponseFormat(ActionStatus.GENERAL_ERROR)));
    }
}
