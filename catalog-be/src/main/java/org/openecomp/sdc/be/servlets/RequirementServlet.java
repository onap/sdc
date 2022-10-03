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
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.RequirementBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.RequirementDefinition;
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
public class RequirementServlet extends AbstractValidationsServlet {

    private static final Logger LOGGER = Logger.getLogger(RequirementServlet.class);
    private final RequirementBusinessLogic requirementBusinessLogic;

    @Inject
    public RequirementServlet(ComponentInstanceBusinessLogic componentInstanceBL,
                              ComponentsUtils componentsUtils, ServletUtils servletUtils, ResourceImportManager resourceImportManager,
                              RequirementBusinessLogic requirementBusinessLogic) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.requirementBusinessLogic = requirementBusinessLogic;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/requirements")
    @Operation(description = "Create requirements on resource", method = "POST", summary = "Create requirements on resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Create requirements"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "requirement already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createRequirementsOnResource(@Parameter(description = "Requirement to create", required = true) String data,
                                                 @Parameter(description = "Resource Id") @PathParam("resourceId") String resourceId,
                                                 @Context final HttpServletRequest request,
                                                 @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "resources", resourceId, request, userId, false, "createRequirements");
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/requirements")
    @Operation(description = "Update Requirements on resource", method = "PUT", summary = "Update Requirements on resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = RequirementDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Update Requirements"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateRequirementsOnResource(@Parameter(description = "Requirements to update", required = true) String data,
                                                 @Parameter(description = "Component Id") @PathParam("resourceId") String resourceId,
                                                 @Context final HttpServletRequest request,
                                                 @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "resources", resourceId, request, userId, true, "updateRequirements");
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/requirements/{requirementId}")
    @Operation(description = "Get Requirement from resource", method = "GET", summary = "GET Requirement from resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = RequirementDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "GET requirement"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getRequirementsFromResource(@Parameter(description = "Resource Id") @PathParam("resourceId") String resourceId,
                                                @Parameter(description = "Requirement Id") @PathParam("requirementId") String requirementId,
                                                @Context final HttpServletRequest request,
                                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return get(requirementId, resourceId, request, userId);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/requirements/{requirementId}")
    @Operation(description = "Delete requirements from resource", method = "DELETE", summary = "Delete requirements from resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = RequirementDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Delete requirement"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteRequirementsFromResource(@Parameter(description = "Resource Id") @PathParam("resourceId") String resourceId,
                                                   @Parameter(description = "requirement Id") @PathParam("requirementId") String requirementId,
                                                   @Context final HttpServletRequest request,
                                                   @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return delete(requirementId, resourceId, request, userId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/requirements")
    @Operation(description = "Create requirements on service", method = "POST", summary = "Create requirements on service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Create Requirements"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Requirement already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createRequirementsOnService(@Parameter(description = "Requirements to create", required = true) String data,
                                                @Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
                                                @Context final HttpServletRequest request,
                                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "services", serviceId, request, userId, false, "createRequirements");
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/requirements")
    @Operation(description = "Update requirements on service", method = "PUT", summary = "Update requirements on service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = RequirementDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Update requirements"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateRequirementsOnService(@Parameter(description = "Requirements to update", required = true) String data,
                                                @Parameter(description = "Component Id") @PathParam("serviceId") String serviceId,
                                                @Context final HttpServletRequest request,
                                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createOrUpdate(data, "services", serviceId, request, userId, true, "updateRequirements");
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/requirements/{requirementId}")
    @Operation(description = "Get requirement from service", method = "GET", summary = "GET requirement from service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = RequirementDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "GET Requirements"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getRequirementsOnService(@Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
                                             @Parameter(description = "Requirement Id") @PathParam("requirementId") String requirementId,
                                             @Context final HttpServletRequest request,
                                             @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return get(requirementId, serviceId, request, userId);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/requirements/{requirementId}")
    @Operation(description = "Delete requirement from service", method = "DELETE", summary = "Delete requirement from service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = RequirementDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Delete Requirements"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteRequirementsOnService(@Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
                                                @Parameter(description = "Requirement Id") @PathParam("requirementId") String requirementId,
                                                @Context final HttpServletRequest request,
                                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return delete(requirementId, serviceId, request, userId);
    }

    private Response createOrUpdate(String data, String componentType, String componentId, HttpServletRequest request, String userId,
                                    boolean isUpdate, String errorContext) {
        String url = request.getMethod() + " " + request.getRequestURI();
        User modifier = new User();
        modifier.setUserId(userId);
        LOGGER.debug("Start create or update request of {} with modifier id {}", url, userId);
        try {
            String componentIdLower = componentId.toLowerCase();
            Either<List<RequirementDefinition>, ResponseFormat> mappedRequirementDataEither = getMappedRequirementData(data, modifier,
                ComponentTypeEnum.findByParamName(componentType));
            if (mappedRequirementDataEither.isRight()) {
                LOGGER.error("Failed to create or update requirements");
                return buildErrorResponse(mappedRequirementDataEither.right().value());
            }
            List<RequirementDefinition> mappedRequirementData = mappedRequirementDataEither.left().value();
            Either<List<RequirementDefinition>, ResponseFormat> actionResponse;
            if (isUpdate) {
                actionResponse = requirementBusinessLogic.updateRequirements(componentIdLower, mappedRequirementData, modifier, errorContext, true);
            } else {
                actionResponse = requirementBusinessLogic.createRequirements(componentIdLower, mappedRequirementData, modifier, errorContext, true);
            }
            if (actionResponse.isRight()) {
                LOGGER.error("Failed to create or update requirements");
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("requirements create or update");
            LOGGER.error("Failed to create or update requirements with an error", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Response get(String requirementIdToGet, String componentId, HttpServletRequest request, String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        User modifier = new User();
        modifier.setUserId(userId);
        LOGGER.debug("Start get request of {} with modifier id {}", url, userId);
        try {
            String componentIdLower = componentId.toLowerCase();
            Either<RequirementDefinition, ResponseFormat> actionResponse = requirementBusinessLogic
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

    private Response delete(String requirementId, String componentId, HttpServletRequest request, String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        User modifier = new User();
        modifier.setUserId(userId);
        LOGGER.debug("Start delete request of {} with modifier id {}", url, userId);
        try {
            String componentIdLower = componentId.toLowerCase();
            Either<RequirementDefinition, ResponseFormat> actionResponse = requirementBusinessLogic
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
                                                                                         ComponentTypeEnum componentTypeEnum) {
        Either<UiComponentDataTransfer, ResponseFormat> mappedData = getComponentsUtils()
            .convertJsonToObjectUsingObjectMapper(inputJson, user, UiComponentDataTransfer.class, AuditingActionEnum.CREATE_RESOURCE,
                componentTypeEnum);
        Optional<List<RequirementDefinition>> requirementDefinitionList = mappedData.left().value().getRequirements().values().stream().findFirst();
        return requirementDefinitionList.<Either<List<RequirementDefinition>, ResponseFormat>>map(Either::left)
            .orElseGet(() -> Either.right(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
    }
}
