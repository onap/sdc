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
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
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
import org.openecomp.sdc.be.components.impl.CapabilitiesBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-2 APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Controller
public class CapabilityServlet extends AbstractValidationsServlet {

    private static final Logger LOGGER = Logger.getLogger(CapabilityServlet.class);
    private final CapabilitiesBusinessLogic capabilitiesBusinessLogic;

    @Inject
    public CapabilityServlet(ComponentInstanceBusinessLogic componentInstanceBL, ComponentsUtils componentsUtils,
                             ServletUtils servletUtils, ResourceImportManager resourceImportManager,
                             CapabilitiesBusinessLogic capabilitiesBusinessLogic) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.capabilitiesBusinessLogic = capabilitiesBusinessLogic;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/capabilities")
    @Operation(description = "Create Capabilities on resource", method = "POST", summary = "Create Capabilities on resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Create Capabilities"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Capability already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createCapabilitiesOnResource(@Parameter(description = "Capability to create", required = true) String data,
                                                 @Parameter(description = "Resource Id") @PathParam("resourceId") String resourceId,
                                                 @Context final HttpServletRequest request,
                                                 @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "resources", resourceId, request, userId, false, "createCapabilities");
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/capabilities")
    @Operation(description = "Update Capabilities on resource", method = "PUT", summary = "Update Capabilities on resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = CapabilityDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Update Capabilities"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateCapabilitiesOnResource(@Parameter(description = "Capabilities to update", required = true) String data,
                                                 @Parameter(description = "Component Id") @PathParam("resourceId") String resourceId,
                                                 @Context final HttpServletRequest request,
                                                 @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "resources", resourceId, request, userId, true, "updateCapabilities");
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/capabilities/{capabilityId}")
    @Operation(description = "Get Capability from resource", method = "GET", summary = "GET Capability from resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = CapabilityDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "GET Capability"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getCapabilityOnResource(@Parameter(description = "Resource Id") @PathParam("resourceId") String resourceId,
                                            @Parameter(description = "Capability Id") @PathParam("capabilityId") String capabilityId,
                                            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return get(capabilityId, resourceId, request, userId);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/capabilities/{capabilityId}")
    @Operation(description = "Delete capability from resource", method = "DELETE", summary = "Delete capability from resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = CapabilityDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Delete capability"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteCapabilityOnResource(@Parameter(description = "capability Id") @PathParam("capabilityId") String capabilityId,
                                               @Parameter(description = "Resource Id") @PathParam("resourceId") String resourceId,
                                               @Context final HttpServletRequest request,
                                               @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return delete(capabilityId, resourceId, request, userId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/capabilities")
    @Operation(description = "Create Capabilities on service", method = "POST", summary = "Create Capabilities on service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Create Capabilities"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Capability already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createCapabilitiesOnService(@Parameter(description = "Capability to create", required = true) String data,
                                                @Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
                                                @Context final HttpServletRequest request,
                                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "services", serviceId, request, userId, false, "createCapabilities");
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/capabilities")
    @Operation(description = "Update Capabilities on service", method = "PUT", summary = "Update Capabilities on service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = CapabilityDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Update Capabilities"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateCapabilitiesOnService(@Parameter(description = "Capabilities to update", required = true) String data,
                                                @Parameter(description = "Component Id") @PathParam("serviceId") String serviceId,
                                                @Context final HttpServletRequest request,
                                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "services", serviceId, request, userId, true, "updateCapabilities");
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/capabilities/{capabilityId}")
    @Operation(description = "Get Capability from service", method = "GET", summary = "GET Capability from service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = CapabilityDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "GET Capability"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getCapabilityOnService(@Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
                                           @Parameter(description = "Capability Id") @PathParam("capabilityId") String capabilityId,
                                           @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return get(capabilityId, serviceId, request, userId);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/capabilities/{capabilityId}")
    @Operation(description = "Delete capability from service", method = "DELETE", summary = "Delete capability from service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = CapabilityDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Delete capability"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteCapabilityOnService(@Parameter(description = "capability Id") @PathParam("capabilityId") String capabilityId,
                                              @Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
                                              @Context final HttpServletRequest request,
                                              @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return delete(capabilityId, serviceId, request, userId);
    }

    private Response createOrUpdate(String data, String componentType, String componentId, HttpServletRequest request, String userId,
                                    boolean isUpdate, String errorContext) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        User modifier = new User();
        modifier.setUserId(userId);
        LOGGER.debug("Start create or update request of {} with modifier id {}", url, userId);
        try {
            String componentIdLower = componentId.toLowerCase();
            Either<List<CapabilityDefinition>, ResponseFormat> mappedCapabilitiesDataEither = getMappedCapabilitiesData(data, modifier,
                ComponentTypeEnum.findByParamName(componentType));
            if (mappedCapabilitiesDataEither.isRight()) {
                LOGGER.error("Failed to create or update capabilities");
                return buildErrorResponse(mappedCapabilitiesDataEither.right().value());
            }
            List<CapabilityDefinition> mappedCapabilitiesData = mappedCapabilitiesDataEither.left().value();
            Either<List<CapabilityDefinition>, ResponseFormat> actionResponse;
            if (isUpdate) {
                actionResponse = capabilitiesBusinessLogic.updateCapabilities(componentIdLower, mappedCapabilitiesData, modifier, errorContext, true);
            } else {
                actionResponse = capabilitiesBusinessLogic.createCapabilities(componentIdLower, mappedCapabilitiesData, modifier, errorContext, true);
            }
            if (actionResponse.isRight()) {
                LOGGER.error("Failed to create or update capabilities");
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Capabilities create or update");
            LOGGER.error("Failed to create or update capabilities with an error", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Response get(String capabilityIdToGet, String componentId, HttpServletRequest request, String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        User modifier = new User();
        modifier.setUserId(userId);
        LOGGER.debug("Start get request of {} with modifier id {}", url, userId);
        try {
            String componentIdLower = componentId.toLowerCase();
            Either<CapabilityDefinition, ResponseFormat> actionResponse = capabilitiesBusinessLogic
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

    private Response delete(String capabilityId, String componentId, HttpServletRequest request, String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        User modifier = new User();
        modifier.setUserId(userId);
        LOGGER.debug("Start delete request of {} with modifier id {}", url, userId);
        try {
            String componentIdLower = componentId.toLowerCase();
            Either<CapabilityDefinition, ResponseFormat> actionResponse = capabilitiesBusinessLogic
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
                                                                                         ComponentTypeEnum componentTypeEnum) {
        Either<UiComponentDataTransfer, ResponseFormat> mappedData = getComponentsUtils()
            .convertJsonToObjectUsingObjectMapper(inputJson, user, UiComponentDataTransfer.class, AuditingActionEnum.CREATE_RESOURCE,
                componentTypeEnum);
        if (mappedData.isRight()) {
            return Either.right(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        Optional<List<CapabilityDefinition>> capabilityDefinitionList = mappedData.left().value().getCapabilities().values().stream().findFirst();
        return capabilityDefinitionList.<Either<List<CapabilityDefinition>, ResponseFormat>>map(Either::left)
            .orElseGet(() -> Either.right(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
    }
}
