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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
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
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.components.impl.InterfaceOperationBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
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
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Interface Operation Servlet", description = "Interface Operation Servlet")
@Singleton
public class InterfaceOperationServlet extends AbstractValidationsServlet {

    private static final Logger log = LoggerFactory.getLogger(InterfaceOperationServlet.class);
    private final InterfaceOperationBusinessLogic interfaceOperationBusinessLogic;

    @Inject
    public InterfaceOperationServlet(UserBusinessLogic userBusinessLogic,
        GroupBusinessLogic groupBL,
        ComponentInstanceBusinessLogic componentInstanceBL,
        ComponentsUtils componentsUtils, ServletUtils servletUtils,
        ResourceImportManager resourceImportManager,
        InterfaceOperationBusinessLogic interfaceOperationBusinessLogic) {
        super(userBusinessLogic, groupBL, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.interfaceOperationBusinessLogic = interfaceOperationBusinessLogic;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/interfaceOperations")
    @ApiOperation(value = "Create Interface Operations on Resource", httpMethod = "POST",
            notes = "Create Interface Operations on Resource", response = InterfaceDefinition.class, responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Create Interface Operations on Resource"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Resource not found"),
            @ApiResponse(code = 409, message = "Interface Operation already exist")})
    public Response createInterfaceOperationsOnResource(
            @ApiParam(value = "Interface Operations to create", required = true) String data,
            @ApiParam(value = "Resource Id") @PathParam("resourceId") String resourceId,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Context final HttpServletRequest request) {
        return createOrUpdate(data, ComponentTypeEnum.RESOURCE, resourceId, request, userId, false);
    }

    private Response createOrUpdate(String data, ComponentTypeEnum componentType, String componentId,
            HttpServletRequest request, String userId, boolean isUpdate) {
        String url = request.getMethod() + " " + request.getRequestURI();

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("Start create or update request of {} with modifier id {}", url, userId);

        try {
            String componentIdLower = componentId.toLowerCase();

            List<InterfaceDefinition> mappedInterfaceData = getMappedInterfaceData(data, modifier, componentType);
            Either<List<InterfaceDefinition>, ResponseFormat> actionResponse;
            if (isUpdate) {
                actionResponse =
                    interfaceOperationBusinessLogic
                        .updateInterfaceOperation(componentIdLower, mappedInterfaceData, modifier, true);
            } else {
                actionResponse =
                    interfaceOperationBusinessLogic
                        .createInterfaceOperation(componentIdLower, mappedInterfaceData, modifier, true);
            }

            if (actionResponse.isRight()) {
                log.error("failed to create or update interface operation");
                return buildErrorResponse(actionResponse.right().value());
            }

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                    getFormattedResponse(actionResponse.left().value()));
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Interface Operation Creation or update");
            log.error("create or update interface Operation with an error", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private List<InterfaceDefinition> getMappedInterfaceData(String inputJson, User user,
            ComponentTypeEnum componentTypeEnum) {
        Either<UiComponentDataTransfer, ResponseFormat> uiComponentEither =
                getComponentsUtils().convertJsonToObjectUsingObjectMapper(inputJson, user,
                        UiComponentDataTransfer.class, AuditingActionEnum.CREATE_RESOURCE, componentTypeEnum);
        return new ArrayList<>(uiComponentEither.left().value().getInterfaces().values());
    }

    private Object getFormattedResponse(List<InterfaceDefinition> interfaceDefinitions) throws IOException {
        Map<String, List<InterfaceDefinition>> allInterfaces =
                ImmutableMap.of(JsonPresentationFields.INTERFACES.getPresentation(), interfaceDefinitions);
        return RepresentationUtils.toFilteredRepresentation(allInterfaces);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/interfaceOperations")
    @ApiOperation(value = "Update Interface Operations on Resource", httpMethod = "PUT",
            notes = "Update Interface Operations on Resource", response = InterfaceDefinition.class, responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Update Interface Operations on Resource"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Resource not found")})
    public Response updateInterfaceOperationsOnResource(
            @ApiParam(value = "Interface Operations to update", required = true) String data,
            @ApiParam(value = "Resource Id") @PathParam("resourceId") String resourceId,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Context final HttpServletRequest request) {
        return createOrUpdate(data, ComponentTypeEnum.RESOURCE, resourceId, request, userId, true);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceId}/interfaces/{interfaceId}/operations/{operationId}")
    @ApiOperation(value = "Delete Interface Operation from Resource", httpMethod = "DELETE",
            notes = "Delete Interface Operation from Resource", response = InterfaceDefinition.class, responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Delete Interface Operation from Resource"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Resource not found")})
    public Response deleteInterfaceOperationsFromResource(
            @ApiParam(value = "Resource Id") @PathParam("resourceId") String resourceId,
            @ApiParam(value = "Interface Id") @PathParam("interfaceId") String interfaceId,
            @ApiParam(value = "Operation Id") @PathParam("operationId") String operationId,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Context final HttpServletRequest request) {
        return delete(interfaceId, operationId, resourceId, request, userId);
    }

    private Response delete(String interfaceId, String operationId, String componentId, HttpServletRequest request,
            String userId) {

        String url = request.getMethod() + " " + request.getRequestURI();

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("Start delete request of {} with modifier id {}", url, userId);

        try {
            String componentIdLower = componentId.toLowerCase();
            Either<List<InterfaceDefinition>, ResponseFormat> actionResponse =
                interfaceOperationBusinessLogic.deleteInterfaceOperation(
                    componentIdLower, interfaceId, Collections.singletonList(operationId), modifier, true);
            if (actionResponse.isRight()) {
                log.error("failed to delete interface operation");
                return buildErrorResponse(actionResponse.right().value());
            }

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                    getFormattedResponse(actionResponse.left().value()));
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
    @ApiOperation(value = "Get Interface Operation from Resource", httpMethod = "GET",
            notes = "GET Interface Operation from Resource", response = InterfaceDefinition.class, responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Delete Interface Operation from Resource"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Resource not found")})
    public Response getInterfaceOperationsFromResource(
            @ApiParam(value = "Resource Id") @PathParam("resourceId") String resourceId,
            @ApiParam(value = "Interface Id") @PathParam("interfaceId") String interfaceId,
            @ApiParam(value = "Operation Id") @PathParam("operationId") String operationId,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Context final HttpServletRequest request) {
        return get(interfaceId, operationId, resourceId, request, userId);
    }

    private Response get(String interfaceId, String operationId, String componentId, HttpServletRequest request,
            String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();

        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("Start get request of {} with modifier id {}", url, userId);

        try {
            String componentIdLower = componentId.toLowerCase();
            Either<List<InterfaceDefinition>, ResponseFormat> actionResponse =
                interfaceOperationBusinessLogic.getInterfaceOperation(
                    componentIdLower, interfaceId, Collections.singletonList(operationId), modifier, true);
            if (actionResponse.isRight()) {
                log.error("failed to get interface operation");
                return buildErrorResponse(actionResponse.right().value());
            }

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                    getFormattedResponse(actionResponse.left().value()));
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
    @ApiOperation(value = "Create Interface Operations on Service", httpMethod = "POST",
            notes = "Create Interface Operations on Service", response = InterfaceDefinition.class, responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Create Interface Operations on Service"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Service not found"),
            @ApiResponse(code = 409, message = "Interface Operation already exist")})
    public Response createInterfaceOperationsOnService(
            @ApiParam(value = "Interface Operations to create", required = true) String data,
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Context final HttpServletRequest request) {
        return createOrUpdate(data, ComponentTypeEnum.SERVICE, serviceId, request, userId, false);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/interfaceOperations")
    @ApiOperation(value = "Update Interface Operations on Service", httpMethod = "PUT",
            notes = "Update Interface Operations on Service", response = InterfaceDefinition.class, responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Update Interface Operations on Service"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Service not found")})
    public Response updateInterfaceOperationsOnService(
            @ApiParam(value = "Interface Operations to update", required = true) String data,
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Context final HttpServletRequest request) {
        return createOrUpdate(data, ComponentTypeEnum.SERVICE, serviceId, request, userId, true);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/interfaces/{interfaceId}/operations/{operationId}")
    @ApiOperation(value = "Delete Interface Operation from Service", httpMethod = "DELETE",
            notes = "Delete Interface Operation from Service", response = InterfaceDefinition.class, responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Delete Interface Operation from Service"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Service not found")})
    public Response deleteInterfaceOperationsFromService(
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @ApiParam(value = "Interface Id") @PathParam("interfaceId") String interfaceId,
            @ApiParam(value = "Operation Id") @PathParam("operationId") String operationId,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Context final HttpServletRequest request) {
        return delete(interfaceId, operationId, serviceId, request, userId);
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/services/{serviceId}/interfaces/{interfaceId}/operations/{operationId}")
    @ApiOperation(value = "Get Interface Operation from Service", httpMethod = "GET",
            notes = "GET Interface Operation from Service", response = InterfaceDefinition.class, responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Get Interface Operation from Service"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Service not found")})
    public Response getInterfaceOperationsFromService(
            @ApiParam(value = "Service Id") @PathParam("serviceId") String serviceId,
            @ApiParam(value = "Interface Id") @PathParam("interfaceId") String interfaceId,
            @ApiParam(value = "Operation Id") @PathParam("operationId") String operationId,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Context final HttpServletRequest request) {
        return get(interfaceId, operationId, serviceId, request, userId);
    }

}

