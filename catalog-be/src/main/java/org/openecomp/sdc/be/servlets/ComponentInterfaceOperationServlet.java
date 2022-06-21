/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 *  Copyright (C) 2021 Nordix Foundation. All rights reserved.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.servlets;

import static org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum.RESOURCE;

import fj.data.Either;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInterfaceOperationBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Path("/v1/catalog")
@Tag(name = "SDCE-2 APIs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Server(url = "/sdc2/rest")
@Controller
public class ComponentInterfaceOperationServlet extends AbstractValidationsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentInterfaceOperationServlet.class);
    private static final String START_HANDLE_REQUEST_OF = "Start handle {} request of {}";
    private static final String MODIFIER_ID_IS = "modifier id is {}";
    private static final String FAILED_TO_UPDATE_INTERFACE_OPERATION = "failed to update Interface Operation on component instance {}";
    private static final String UPDATE_INTERFACE_OPERATION = "Update Interface Operation on Component Instance";
    private static final String FAILED_TO_UPDATE_INTERFACE_OPERATION_WITH_ERROR = "Failed to update Interface Operation with an error";
    private static final String INTERFACE_OPERATION_CONTENT_INVALID = "Interface Operation content is invalid - {}";
    private static final String UNSUPPORTED_COMPONENT_TYPE = "Unsupported component type {}";
    private static final String INTERFACE_OPERATION_SUCCESSFULLY_UPDATED = "Interface Operation successfully updated on component instance with id {}";
    private final ComponentInterfaceOperationBusinessLogic componentInterfaceOperationBusinessLogic;

    @Autowired
    public ComponentInterfaceOperationServlet(final UserBusinessLogic userBusinessLogic, final ComponentInstanceBusinessLogic componentInstanceBL,
                                              final ComponentsUtils componentsUtils, final ServletUtils servletUtils,
                                              final ResourceImportManager resourceImportManager,
                                              final ComponentInterfaceOperationBusinessLogic componentInterfaceOperationBusinessLogic) {
        super(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.componentInterfaceOperationBusinessLogic = componentInterfaceOperationBusinessLogic;
    }

    @PUT
    @Path("/{componentType}/{componentId}/componentInstance/{componentInstanceId}/interfaceOperation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update Interface Operation", method = "PUT", summary = "Update Interface Operation on ComponentInstance", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Update Interface Operation"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateComponentInstanceInterfaceOperation(
        @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
            ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("componentType") String componentType,
        @Parameter(description = "Component Id") @PathParam("componentId") String componentId,
        @Parameter(description = "Component Instance Id") @PathParam("componentInstanceId") String componentInstanceId,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        LOGGER.debug(START_HANDLE_REQUEST_OF, request.getMethod(), request.getRequestURI());
        userId = ValidationUtils.sanitizeInputString(userId);
        componentType = ValidationUtils.sanitizeInputString(componentType);
        componentInstanceId = ValidationUtils.sanitizeInputString(componentInstanceId);
        LOGGER.debug(MODIFIER_ID_IS, userId);
        final User userModifier = componentInterfaceOperationBusinessLogic.validateUser(userId);
        final ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
        if (componentTypeEnum == null) {
            LOGGER.debug(UNSUPPORTED_COMPONENT_TYPE, componentType);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, componentType));
        }
        final byte[] bytes = IOUtils.toByteArray(request.getInputStream());
        if (bytes == null || bytes.length == 0) {
            LOGGER.error(INTERFACE_OPERATION_CONTENT_INVALID, "content is empty");
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        final String data = new String(bytes);
        final Optional<InterfaceDefinition> mappedInterfaceOperationData = getMappedInterfaceData(data, userModifier, componentTypeEnum);
        if (mappedInterfaceOperationData.isEmpty()) {
            LOGGER.error(INTERFACE_OPERATION_CONTENT_INVALID, data);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        final Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        try {
            final Optional<ComponentInstance> actionResponse = componentInterfaceOperationBusinessLogic.updateComponentInstanceInterfaceOperation(
                componentId, componentInstanceId, mappedInterfaceOperationData.get(), componentTypeEnum, errorWrapper, true);
            if (actionResponse.isEmpty()) {
                LOGGER.error(FAILED_TO_UPDATE_INTERFACE_OPERATION, componentInstanceId);
                return buildErrorResponse(errorWrapper.getInnerElement());
            } else {
                LOGGER.debug(INTERFACE_OPERATION_SUCCESSFULLY_UPDATED, componentInstanceId);
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResponse.get());
            }
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(UPDATE_INTERFACE_OPERATION);
            LOGGER.error(FAILED_TO_UPDATE_INTERFACE_OPERATION_WITH_ERROR, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @PUT
    @Path("/resources/{componentId}/interfaceOperation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update Interface Operation", method = "PUT", summary = "Update Interface Operation on ComponentInstance", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Update Interface Operation"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateResourceInterfaceOperation(
        @Parameter(description = "Component Id") @PathParam("componentId") String componentId,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        LOGGER.debug(START_HANDLE_REQUEST_OF, request.getMethod(), request.getRequestURI());
        LOGGER.debug(MODIFIER_ID_IS, userId);
        final User userModifier = componentInterfaceOperationBusinessLogic.validateUser(userId);
        final byte[] bytes = IOUtils.toByteArray(request.getInputStream());
        if (bytes == null || bytes.length == 0) {
            LOGGER.error(INTERFACE_OPERATION_CONTENT_INVALID);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        final ComponentTypeEnum componentType = RESOURCE;
        final String data = new String(bytes);
        final Optional<InterfaceDefinition> mappedInterfaceOperationData = getMappedInterfaceData(data, userModifier, componentType);
        if (mappedInterfaceOperationData.isEmpty()) {
            LOGGER.error(INTERFACE_OPERATION_CONTENT_INVALID, data);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        final Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        try {
            final Optional<Component> actionResponse = componentInterfaceOperationBusinessLogic
                .updateResourceInterfaceOperation(componentId, userId, mappedInterfaceOperationData.get(), componentType,
                    errorWrapper, true);
            if (actionResponse.isEmpty()) {
                LOGGER.error(FAILED_TO_UPDATE_INTERFACE_OPERATION, componentId);
                return buildErrorResponse(errorWrapper.getInnerElement());
            } else {
                LOGGER.debug(INTERFACE_OPERATION_SUCCESSFULLY_UPDATED, componentId);
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResponse.get());
            }
        } catch (final ComponentException e) {
            //let it be handled by org.openecomp.sdc.be.servlets.exception.ComponentExceptionMapper
            throw e;
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(UPDATE_INTERFACE_OPERATION);
            LOGGER.error(FAILED_TO_UPDATE_INTERFACE_OPERATION_WITH_ERROR, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/{componentType}/{componentId}/resource/interfaceOperation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create Interface Operation", method = "POST", summary = "Create Interface Operation on ComponentInstance", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Create Interface Operation"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createInterfaceOperationInResource(
        @Parameter(description = "valid values: resources", schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME}))
        @PathParam("componentType") final String componentType,
        @Parameter(description = "Component Id") @PathParam("componentId") String componentId,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        LOGGER.debug(START_HANDLE_REQUEST_OF, request.getMethod(), request.getRequestURI());
        LOGGER.debug(MODIFIER_ID_IS, userId);
        final User userModifier = componentInterfaceOperationBusinessLogic.validateUser(userId);
        final ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
        if (componentTypeEnum == null) {
            LOGGER.debug(UNSUPPORTED_COMPONENT_TYPE, componentType);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, componentType));
        }
        final byte[] bytes = IOUtils.toByteArray(request.getInputStream());
        if (bytes == null || bytes.length == 0) {
            LOGGER.error(INTERFACE_OPERATION_CONTENT_INVALID);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        final String data = new String(bytes);
        final Optional<InterfaceDefinition> mappedInterfaceOperationData = getMappedInterfaceData(data, userModifier, componentTypeEnum);
        if (mappedInterfaceOperationData.isEmpty()) {
            LOGGER.error(INTERFACE_OPERATION_CONTENT_INVALID, data);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        final Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        try {
            final Optional<Component> actionResponse = componentInterfaceOperationBusinessLogic.createInterfaceOperationInResource(
                componentId, mappedInterfaceOperationData.get(), componentTypeEnum, errorWrapper, true);
            if (actionResponse.isEmpty()) {
                LOGGER.error(FAILED_TO_UPDATE_INTERFACE_OPERATION, componentId);
                return buildErrorResponse(errorWrapper.getInnerElement());
            } else {
                LOGGER.debug(INTERFACE_OPERATION_SUCCESSFULLY_UPDATED, componentId);
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResponse.get());
            }
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(UPDATE_INTERFACE_OPERATION);
            LOGGER.error(FAILED_TO_UPDATE_INTERFACE_OPERATION_WITH_ERROR, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Optional<InterfaceDefinition> getMappedInterfaceData(final String inputJson, final User user, final ComponentTypeEnum componentTypeEnum) {
        final Either<UiComponentDataTransfer, ResponseFormat> uiComponentEither = getComponentsUtils()
            .convertJsonToObjectUsingObjectMapper(inputJson, user, UiComponentDataTransfer.class, AuditingActionEnum.UPDATE_RESOURCE_METADATA,
                componentTypeEnum);
        return uiComponentEither.left().value().getInterfaces().values().stream().findFirst();
    }
}
