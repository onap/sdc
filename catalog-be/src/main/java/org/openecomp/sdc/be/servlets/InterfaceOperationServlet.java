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

import com.google.common.collect.ImmutableMap;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.openecomp.sdc.be.components.impl.InterfaceOperationBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-2 APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Controller
public class InterfaceOperationServlet extends AbstractValidationsServlet {

    private static final Logger log = LoggerFactory.getLogger(InterfaceOperationServlet.class);
    private final InterfaceOperationBusinessLogic interfaceOperationBusinessLogic;

    @Inject
    public InterfaceOperationServlet(ComponentInstanceBusinessLogic componentInstanceBL,
                                     ComponentsUtils componentsUtils, ServletUtils servletUtils, ResourceImportManager resourceImportManager,
                                     InterfaceOperationBusinessLogic interfaceOperationBusinessLogic) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.interfaceOperationBusinessLogic = interfaceOperationBusinessLogic;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/interfaceOperations")
    @Operation(description = "Create Interface Operations on Resource", method = "POST", summary = "Create Interface Operations on Resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = InterfaceDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Create Interface Operations on Resource"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "409", description = "Interface Operation already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createInterfaceOperationsOnResource(@Parameter(description = "Interface Operations to create", required = true) String data,
                                                        @Parameter(description = "Resource Id") @PathParam("resourceId") String resourceId,
                                                        @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                        @Context final HttpServletRequest request) {
        return createOrUpdate(data, ComponentTypeEnum.RESOURCE, resourceId, request, userId, false);
    }

    private Response createOrUpdate(String data, ComponentTypeEnum componentType, String componentId, HttpServletRequest request, String userId,
                                    boolean isUpdate) {
        String url = request.getMethod() + " " + request.getRequestURI();
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("Start create or update request of {} with modifier id {}", url, userId);
        try {
            String componentIdLower = componentId.toLowerCase();
            List<InterfaceDefinition> mappedInterfaceData = getMappedInterfaceData(data, modifier, componentType);
            Either<List<InterfaceDefinition>, ResponseFormat> actionResponse;
            if (isUpdate) {
                actionResponse = interfaceOperationBusinessLogic.updateInterfaceOperation(componentIdLower, mappedInterfaceData, modifier, true);
            } else {
                actionResponse = interfaceOperationBusinessLogic.createInterfaceOperation(componentIdLower, mappedInterfaceData, modifier, true);
            }
            if (actionResponse.isRight()) {
                log.error("failed to create or update interface operation");
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), getFormattedResponse(actionResponse.left().value()));
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Interface Operation Creation or update");
            log.error("create or update interface Operation with an error", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private List<InterfaceDefinition> getMappedInterfaceData(String inputJson, User user, ComponentTypeEnum componentTypeEnum) {
        Either<UiComponentDataTransfer, ResponseFormat> uiComponentEither = getComponentsUtils()
            .convertJsonToObjectUsingObjectMapper(inputJson, user, UiComponentDataTransfer.class, AuditingActionEnum.CREATE_RESOURCE,
                componentTypeEnum);
        return new ArrayList<>(uiComponentEither.left().value().getInterfaces().values());
    }

    private Object getFormattedResponse(List<InterfaceDefinition> interfaceDefinitions) throws IOException {
        Map<String, List<InterfaceDefinition>> allInterfaces = ImmutableMap
            .of(JsonPresentationFields.INTERFACES.getPresentation(), interfaceDefinitions);
        return RepresentationUtils.toFilteredRepresentation(allInterfaces);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/interfaceOperations")
    @Operation(description = "Update Interface Operations on Resource", method = "PUT", summary = "Update Interface Operations on Resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = InterfaceDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Update Interface Operations on Resource"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Resource not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateInterfaceOperationsOnResource(@Parameter(description = "Interface Operations to update", required = true) String data,
                                                        @Parameter(description = "Resource Id") @PathParam("resourceId") String resourceId,
                                                        @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                        @Context final HttpServletRequest request) {
        return createOrUpdate(data, ComponentTypeEnum.RESOURCE, resourceId, request, userId, true);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/interfaces/{interfaceId}/operations/{operationId}")
    @Operation(description = "Delete Interface Operation from Resource", method = "DELETE", summary = "Delete Interface Operation from Resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = InterfaceDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Delete Interface Operation from Resource"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Resource not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteInterfaceOperationsFromResource(@Parameter(description = "Resource Id") @PathParam("resourceId") String resourceId,
                                                          @Parameter(description = "Interface Id") @PathParam("interfaceId") String interfaceId,
                                                          @Parameter(description = "Operation Id") @PathParam("operationId") String operationId,
                                                          @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                          @Context final HttpServletRequest request) {
        return delete(interfaceId, operationId, resourceId, request, userId);
    }

    private Response delete(String interfaceId, String operationId, String componentId, HttpServletRequest request, String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("Start delete request of {} with modifier id {}", url, userId);
        try {
            String componentIdLower = componentId.toLowerCase();
            Either<List<InterfaceDefinition>, ResponseFormat> actionResponse = interfaceOperationBusinessLogic
                .deleteInterfaceOperation(componentIdLower, interfaceId, Collections.singletonList(operationId), modifier, true);
            if (actionResponse.isRight()) {
                log.error("failed to delete interface operation");
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), getFormattedResponse(actionResponse.left().value()));
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Interface Operation");
            log.error("Delete interface operation with an error", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/interfaces/{interfaceId}/operations/{operationId}")
    @Operation(description = "Get Interface Operation from Resource", method = "GET", summary = "GET Interface Operation from Resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = InterfaceDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Delete Interface Operation from Resource"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Resource not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getInterfaceOperationsFromResource(@Parameter(description = "Resource Id") @PathParam("resourceId") String resourceId,
                                                       @Parameter(description = "Interface Id") @PathParam("interfaceId") String interfaceId,
                                                       @Parameter(description = "Operation Id") @PathParam("operationId") String operationId,
                                                       @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                       @Context final HttpServletRequest request) {
        return get(interfaceId, operationId, resourceId, request, userId);
    }

    private Response get(String interfaceId, String operationId, String componentId, HttpServletRequest request, String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("Start get request of {} with modifier id {}", url, userId);
        try {
            String componentIdLower = componentId.toLowerCase();
            Either<List<InterfaceDefinition>, ResponseFormat> actionResponse = interfaceOperationBusinessLogic
                .getInterfaceOperation(componentIdLower, interfaceId, Collections.singletonList(operationId), modifier, true);
            if (actionResponse.isRight()) {
                log.error("failed to get interface operation");
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), getFormattedResponse(actionResponse.left().value()));
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Component interface operations");
            log.error("get component interface operations failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/interfaceOperations")
    @Operation(description = "Create Interface Operations on Service", method = "POST", summary = "Create Interface Operations on Service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = InterfaceDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Create Interface Operations on Service"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Service not found"),
        @ApiResponse(responseCode = "409", description = "Interface Operation already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createInterfaceOperationsOnService(@Parameter(description = "Interface Operations to create", required = true) String data,
                                                       @Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
                                                       @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                       @Context final HttpServletRequest request) {
        return createOrUpdate(data, ComponentTypeEnum.SERVICE, serviceId, request, userId, false);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/interfaceOperations")
    @Operation(description = "Update Interface Operations on Service", method = "PUT", summary = "Update Interface Operations on Service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = InterfaceDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Update Interface Operations on Service"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Service not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateInterfaceOperationsOnService(@Parameter(description = "Interface Operations to update", required = true) String data,
                                                       @Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
                                                       @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                       @Context final HttpServletRequest request) {
        return createOrUpdate(data, ComponentTypeEnum.SERVICE, serviceId, request, userId, true);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/interfaces/{interfaceId}/operations/{operationId}")
    @Operation(description = "Delete Interface Operation from Service", method = "DELETE", summary = "Delete Interface Operation from Service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = InterfaceDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Delete Interface Operation from Service"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Service not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteInterfaceOperationsFromService(@Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
                                                         @Parameter(description = "Interface Id") @PathParam("interfaceId") String interfaceId,
                                                         @Parameter(description = "Operation Id") @PathParam("operationId") String operationId,
                                                         @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                         @Context final HttpServletRequest request) {
        return delete(interfaceId, operationId, serviceId, request, userId);
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/interfaces/{interfaceId}/operations/{operationId}")
    @Operation(description = "Get Interface Operation from Service", method = "GET", summary = "GET Interface Operation from Service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = InterfaceDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Get Interface Operation from Service"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Service not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getInterfaceOperationsFromService(@Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
                                                      @Parameter(description = "Interface Id") @PathParam("interfaceId") String interfaceId,
                                                      @Parameter(description = "Operation Id") @PathParam("operationId") String operationId,
                                                      @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                      @Context final HttpServletRequest request) {
        return get(interfaceId, operationId, serviceId, request, userId);
    }
}
