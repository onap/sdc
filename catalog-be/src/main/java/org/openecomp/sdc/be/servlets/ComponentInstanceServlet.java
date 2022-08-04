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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jcabi.aspects.Loggable;
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
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogicProvider;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentNodeFilterBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.ForwardingPaths;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.CreatedFrom;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.openecomp.sdc.be.externalapi.servlet.representation.ReplaceVNFInfo;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.info.CreateAndAssotiateInfo;
import org.openecomp.sdc.be.info.GroupDefinitionInfo;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintDeserialiser;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

/**
 * Root resource (exposed at "/" path) .json.
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Controller
public class ComponentInstanceServlet extends AbstractValidationsServlet {

    private static final String FAILED_TO_GET_PROPERTIES_OF_COMPONENT_INSTANCE_ID_IN_WITH_ID = "Failed to get properties of component instance ID: {} in {} with ID: {}";
    private static final String GET_GROUP_ARTIFACT_BY_ID = "getGroupArtifactById";
    private static final String GET_GROUP_ARTIFACT_BY_ID_UNEXPECTED_EXCEPTION = "getGroupArtifactById unexpected exception";
    private static final String GET_START_HANDLE_REQUEST_OF = "(GET) Start handle request of {}";
    private static final String START_HANDLE_REQUEST_OF_UPDATE_RESOURCE_INSTANCE_PROPERTY_RECEIVED_PROPERTY_IS = "Start handle request of updateResourceInstanceProperty. Received property is {}";
    private static final String START_HANDLE_REQUEST_OF_UPDATE_RESOURCE_INSTANCE_ATTRIBUTE_RECEIVED_ATTRIBUTE_IS = "Start handle request of updateResourceInstanceAttribute. Received attribute is {}";
    private static final String UPDATE_RESOURCE_INSTANCE = "Update Resource Instance";
    private static final String RESOURCE_INSTANCE_UPDATE_RESOURCE_INSTANCE = "Resource Instance - updateResourceInstance";
    private static final String UPDATE_RESOURCE_INSTANCE_WITH_EXCEPTION = "update resource instance with exception";
    private static final String FAILED_TO_CONVERT_RECEIVED_DATA_TO_BE_FORMAT = "Failed to convert received data to BE format.";
    private static final String EMPTY_BODY_WAS_SENT = "Empty body was sent.";
    private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";
    private static final String UNSUPPORTED_COMPONENT_TYPE = "Unsupported component type {}";
    private static final String CREATE_AND_ASSOCIATE_RI_FAILED_WITH_EXCEPTION = "create and associate RI failed with exception: {}";
    private static final Logger log = Logger.getLogger(ComponentInstanceServlet.class);
    private static final Type PROPERTY_CONSTRAINT_TYPE = new TypeToken<PropertyConstraint>() {
    }.getType();
    private static final Gson gsonDeserializer = new GsonBuilder().registerTypeAdapter(PROPERTY_CONSTRAINT_TYPE, new PropertyConstraintDeserialiser())
        .create();
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(ComponentInstanceServlet.class.getName());
    private static final String SERVICES = "services";
    private final GroupBusinessLogic groupBL;
    private final ComponentNodeFilterBusinessLogic nodeFilterBusinessLogic;
    private final ComponentBusinessLogicProvider componentBusinessLogicProvider;

    @Inject
    public ComponentInstanceServlet(UserBusinessLogic userBusinessLogic, GroupBusinessLogic groupBL,
                                    ComponentInstanceBusinessLogic componentInstanceBL, ComponentsUtils componentsUtils, ServletUtils servletUtils,
                                    ResourceImportManager resourceImportManager, ComponentNodeFilterBusinessLogic nodeFilterBusinessLogic, ComponentBusinessLogicProvider componentBusinessLogicProvider) {
        super(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.groupBL = groupBL;
        this.nodeFilterBusinessLogic = nodeFilterBusinessLogic;
        this.componentBusinessLogicProvider = componentBusinessLogicProvider;
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create ComponentInstance", method = "POST", summary = "Returns created ComponentInstance", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Component created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Component instance already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createComponentInstance(@Parameter(description = "RI object to be created", required = true) String data,
                                            @PathParam("componentId") final String containerComponentId,
                                            @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                                ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                                ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                            @HeaderParam(value = Constants.USER_ID_HEADER) @Parameter(description = "USER_ID of modifier user", required = true) String userId,
                                            @Context final HttpServletRequest request) {
        validateNotEmptyBody(data);
        final ComponentInstance componentInstance;
        try {
            componentInstance = RepresentationUtils.fromRepresentation(data, ComponentInstance.class);
            componentInstance.setInvariantName(null);
            componentInstance.setCreatedFrom(CreatedFrom.UI);
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Component Instance");
            log.debug("create component instance failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT, containerComponentType));
        }
        loggerSupportability
            .log(LoggerSupportabilityActions.CREATE_INSTANCE, StatusCode.STARTED, "Starting to create component instance by {}", userId);
        if (componentInstanceBusinessLogic == null) {
            log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
        }
        final ComponentInstance actionResponse = componentInstanceBusinessLogic
            .createComponentInstance(containerComponentType, containerComponentId, userId, componentInstance);
        loggerSupportability.log(LoggerSupportabilityActions.CREATE_INSTANCE, actionResponse.getComponentMetadataForSupportLog(), StatusCode.COMPLETE,
            "Ending to create component instance by user {}", userId);
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResponse);
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update resource instance", method = "POST", summary = "Returns updated resource instance", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Resource instance updated"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateComponentInstanceMetadata(@PathParam("componentId") final String componentId,
                                                    @PathParam("componentInstanceId") final String componentInstanceId,
                                                    @Parameter(description = "valid values: resources / services / products", schema = @Schema(allowableValues = {
                                                        ComponentTypeEnum.RESOURCE_PARAM_NAME, ComponentTypeEnum.SERVICE_PARAM_NAME,
                                                        ComponentTypeEnum.PRODUCT_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                                    @Context final HttpServletRequest request) throws IOException, BusinessLogicException {
        final String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_COMPONENT_INSTANCE, StatusCode.STARTED, "update Component Instance Metadata");
        final String userId = request.getHeader(Constants.USER_ID_HEADER);
        try {
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            componentInstanceBusinessLogic.validateUser(userId);
            log.debug(START_HANDLE_REQUEST_OF, url);
            final byte[] bytes = IOUtils.toByteArray(request.getInputStream());
            if (bytes == null || bytes.length == 0) {
                log.info(EMPTY_BODY_WAS_SENT);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
            final String data = new String(bytes);
            final ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            final Either<ComponentInstance, ResponseFormat> convertResponse = convertToResourceInstance(data);
            if (convertResponse.isRight()) {
                BeEcompErrorManager.getInstance().logBeSystemError(RESOURCE_INSTANCE_UPDATE_RESOURCE_INSTANCE);
                log.debug(FAILED_TO_CONVERT_RECEIVED_DATA_TO_BE_FORMAT);
                return buildErrorResponse(convertResponse.right().value());
            }
            final ComponentInstance componentInstance = convertResponse.left().value();
            final Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceBusinessLogic
                .updateComponentInstanceMetadata(containerComponentType, componentId, componentInstanceId, userId, componentInstance);
            loggerSupportability
                .log(LoggerSupportabilityActions.UPDATE_COMPONENT_INSTANCE, actionResponse.left().value().getComponentMetadataForSupportLog(),
                    StatusCode.COMPLETE, "update Component Instance Metadata by {}", userId);
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            final ComponentInstance resultValue = actionResponse.left().value();
            if (ComponentTypeEnum.SERVICE.equals(componentTypeEnum) || ComponentTypeEnum.RESOURCE.equals(componentTypeEnum)) {
                if (CollectionUtils.isNotEmpty(componentInstance.getDirectives())) {
                    final Optional<CINodeFilterDataDefinition> nodeFilterDataDefinition = nodeFilterBusinessLogic
                        .createNodeFilterIfNotExist(componentId, componentInstanceId, true, componentTypeEnum);
                    if (!nodeFilterDataDefinition.isPresent()) {
                        BeEcompErrorManager.getInstance().logBeSystemError("Failed to create node filter.");
                        log.error("Failed to create node filter.");
                        return buildErrorResponse(convertResponse.right().value());
                    }
                    resultValue.setNodeFilter(nodeFilterDataDefinition.get());
                } else {
                    final Optional<String> result = nodeFilterBusinessLogic
                        .deleteNodeFilterIfExists(componentId, componentInstanceId, true, componentTypeEnum);
                    if (!result.isPresent()) {
                        BeEcompErrorManager.getInstance().logBeSystemError("Failed to delete node filter.");
                        log.error("Failed to delete node filter.");
                        return buildErrorResponse(convertResponse.right().value());
                    }
                    resultValue.setNodeFilter(null);
                }
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(UPDATE_RESOURCE_INSTANCE);
            log.debug(UPDATE_RESOURCE_INSTANCE_WITH_EXCEPTION, e);
            throw e;
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/multipleComponentInstance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update resource instance multiple component", method = "POST", summary = "Returns updated resource instance", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Resource instance updated"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateMultipleComponentInstance(@PathParam("componentId") final String componentId,
                                                    @Parameter(description = "valid values: resources / services / products", schema = @Schema(allowableValues = {
                                                        ComponentTypeEnum.RESOURCE_PARAM_NAME, ComponentTypeEnum.SERVICE_PARAM_NAME,
                                                        ComponentTypeEnum.PRODUCT_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                                    @Context final HttpServletRequest request,
                                                    @Parameter(description = "Component Instance JSON Array", required = true) final String componentInstanceJsonArray) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            log.debug(START_HANDLE_REQUEST_OF, url);
            if (componentInstanceJsonArray == null || componentInstanceJsonArray.length() == 0) {
                log.info("Empty JSON list was sent.");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
            String userId = request.getHeader(Constants.USER_ID_HEADER);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<List<ComponentInstance>, ResponseFormat> convertResponse = convertToMultipleResourceInstance(componentInstanceJsonArray);
            if (convertResponse.isRight()) {
                // Using both ECOMP error methods, show to Sofer
                BeEcompErrorManager.getInstance().logBeSystemError(RESOURCE_INSTANCE_UPDATE_RESOURCE_INSTANCE);
                log.debug(FAILED_TO_CONVERT_RECEIVED_DATA_TO_BE_FORMAT);
                return buildErrorResponse(convertResponse.right().value());
            }
            List<ComponentInstance> componentInstanceList = convertResponse.left().value();
            List<ComponentInstance> actionResponse = componentInstanceBusinessLogic
                .updateComponentInstance(containerComponentType, null, componentId, userId, componentInstanceList, true);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(UPDATE_RESOURCE_INSTANCE);
            log.debug(UPDATE_RESOURCE_INSTANCE_WITH_EXCEPTION, e);
            throw e;
        }
    }

    @DELETE
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{resourceInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete ResourceInstance", method = "DELETE", summary = "Returns delete resourceInstance", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "ResourceInstance deleted"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteResourceInstance(@PathParam("componentId") final String componentId,
                                           @PathParam("resourceInstanceId") final String resourceInstanceId,
                                           @Parameter(description = "valid values: resources / services / products", schema = @Schema(allowableValues = {
                                               ComponentTypeEnum.RESOURCE_PARAM_NAME, ComponentTypeEnum.SERVICE_PARAM_NAME,
                                               ComponentTypeEnum.PRODUCT_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                           @Context final HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        try {
            log.debug(START_HANDLE_REQUEST_OF, url);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            String userId = request.getHeader(Constants.USER_ID_HEADER);
            ComponentInstance actionResponse = componentInstanceBusinessLogic
                .deleteComponentInstance(containerComponentType, componentId, resourceInstanceId, userId);
            loggerSupportability
                .log(LoggerSupportabilityActions.DELETE_COMPONENT_INSTANCE, actionResponse.getComponentMetadataForSupportLog(), StatusCode.STARTED,
                    "DELETE_COMPONENT_INSTANCE by user {}", userId);
            loggerSupportability
                .log(LoggerSupportabilityActions.DELETE_COMPONENT_INSTANCE, actionResponse.getComponentMetadataForSupportLog(), StatusCode.COMPLETE,
                    "DELETE_COMPONENT_INSTANCE by user {}", userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Resource Instance");
            log.debug("delete resource instance with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @Parameter(description = "allowed values are resources /services / products", schema = @Schema(allowableValues = {
        ComponentTypeEnum.RESOURCE_PARAM_NAME, ComponentTypeEnum.SERVICE_PARAM_NAME, ComponentTypeEnum.PRODUCT_PARAM_NAME}), required = true)
    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/associate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Associate RI to RI", method = "POST", summary = "Returns created RelationshipInfo", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Relationship created"),
        @ApiResponse(responseCode = "403", description = "Missing information"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Relationship already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response associateRIToRI(
        @Parameter(description = "unique id of the container component") @PathParam("componentId") final String componentId,
        @Parameter(description = "allowed values are resources /services / products", schema = @Schema(allowableValues = {
            ComponentTypeEnum.RESOURCE_PARAM_NAME, ComponentTypeEnum.SERVICE_PARAM_NAME,
            ComponentTypeEnum.PRODUCT_PARAM_NAME}), required = true) @PathParam("containerComponentType") final String containerComponentType,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Parameter(description = "RelationshipInfo", required = true) String data,
        @Context final HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        loggerSupportability
            .log(LoggerSupportabilityActions.ASSOCIATE_RI_TO_RI, StatusCode.STARTED, "Starting to associate RI To RI for component {} ",
                componentId + " by " + userId);
        try {
            log.debug(START_HANDLE_REQUEST_OF, url);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            RequirementCapabilityRelDef requirementDef = convertToRequirementCapabilityRelDef(data);
            requirementDef.setOriginUI(true);
            RequirementCapabilityRelDef actionResponse = componentInstanceBusinessLogic
                .associateRIToRI(componentId, userId, requirementDef, componentTypeEnum);
            loggerSupportability
                .log(LoggerSupportabilityActions.ASSOCIATE_RI_TO_RI, StatusCode.COMPLETE, "Ended associate RI To RI for component {} ",
                    componentId + " by " + userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse);
        } catch (Exception e) {
            if (!e.getClass().equals(ComponentException.class)) {
                BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Associate Resource Instance");
                log.debug("associate resource instance to another RI with exception", e);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
            throw e;
        }
    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/resourceInstance/dissociate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Dissociate RI from RI", method = "PUT", summary = "Returns deleted RelationshipInfo", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Relationship deleted"),
        @ApiResponse(responseCode = "403", description = "Missing information"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response dissociateRIFromRI(
        @Parameter(description = "allowed values are resources /services / products", schema = @Schema(allowableValues = {
            ComponentTypeEnum.RESOURCE_PARAM_NAME, ComponentTypeEnum.SERVICE_PARAM_NAME,
            ComponentTypeEnum.PRODUCT_PARAM_NAME}), required = true) @PathParam("containerComponentType") final String containerComponentType,
        @Parameter(description = "unique id of the container component") @PathParam("componentId") final String componentId,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Parameter(description = "RelationshipInfo", required = true) String data,
        @Context final HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        loggerSupportability
            .log(LoggerSupportabilityActions.UN_ASSOCIATE_RI_TO_RI, StatusCode.STARTED, "Starting to undo associate RI To RI for component {} ",
                componentId + " by " + userId);
        try {
            log.debug(START_HANDLE_REQUEST_OF, url);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            RequirementCapabilityRelDef requirementDef = convertToRequirementCapabilityRelDef(data);
            RequirementCapabilityRelDef actionResponse = componentInstanceBusinessLogic
                .dissociateRIFromRI(componentId, userId, requirementDef, componentTypeEnum);
            loggerSupportability
                .log(LoggerSupportabilityActions.UN_ASSOCIATE_RI_TO_RI, StatusCode.COMPLETE, "Ended undo associate RI To RI for component {} ",
                    componentId + " by " + userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Dissociate Resource Instance");
            log.debug("dissociate resource instance from service failed with exception", e);
            throw e;
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/createAndAssociate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create RI and associate RI to RI", method = "POST", summary = "Returns created RI and RelationshipInfo", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "RI created"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Relationship already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createAndAssociateRIToRI(@PathParam("componentId") final String componentId,
                                             @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                                 ComponentTypeEnum.RESOURCE_PARAM_NAME, ComponentTypeEnum.SERVICE_PARAM_NAME,
                                                 ComponentTypeEnum.PRODUCT_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                             @Context final HttpServletRequest request) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            log.debug(START_HANDLE_REQUEST_OF, url);
            InputStream inputStream = request.getInputStream();
            byte[] bytes = IOUtils.toByteArray(inputStream);
            if (bytes == null || bytes.length == 0) {
                log.info(EMPTY_BODY_WAS_SENT);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
            String userId = request.getHeader(Constants.USER_ID_HEADER);
            String data = new String(bytes);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<CreateAndAssotiateInfo, ActionStatus> convertStatus = convertJsonToObject(data, CreateAndAssotiateInfo.class);
            if (convertStatus.isRight()) {
                BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - createAndAssociateRIToRI");
                log.debug(FAILED_TO_CONVERT_RECEIVED_DATA_TO_BE_FORMAT);
                Either<Object, ResponseFormat> formattedResponse = Either
                    .right(getComponentsUtils().getResponseFormat(convertStatus.right().value()));
                return buildErrorResponse(formattedResponse.right().value());
            }
            CreateAndAssotiateInfo createAndAssotiateInfo = convertStatus.left().value();
            RequirementCapabilityRelDef requirementDef = createAndAssotiateInfo.getAssociate();
            requirementDef.setOriginUI(true);
            Either<CreateAndAssotiateInfo, ResponseFormat> actionResponse = componentInstanceBusinessLogic
                .createAndAssociateRIToRI(containerComponentType, componentId, userId, createAndAssotiateInfo);
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create and Associate Resource Instance");
            log.debug("create and associate RI failed with exception", e);
            throw e;
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update resource instance property", method = "POST", summary = "Returns updated resource instance property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Resource instance created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateResourceInstanceProperties(@Parameter(description = "service id") @PathParam("componentId") final String componentId,
                                                     @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                                         ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                                         ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                                     @Parameter(description = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId,
                                                     @Parameter(description = "id of user initiating the operation") @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                     @Context final HttpServletRequest request,
                                                     @Parameter(description = "Component Instance Properties JSON Array", required = true) final String componentInstancePropertiesJsonArray) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_COMPONENT_INSTANCE, StatusCode.STARTED,
            "Starting to update Resource Instance Properties for component {} ", componentId + " by " + userId);
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        List<ComponentInstanceProperty> propertiesToUpdate = new ArrayList<>();
        if (errorWrapper.isEmpty()) {
            Either<List<ComponentInstanceProperty>, ResponseFormat> propertiesToUpdateEither = convertMultipleProperties(
                componentInstancePropertiesJsonArray);
            if (propertiesToUpdateEither.isRight()) {
                errorWrapper.setInnerElement(propertiesToUpdateEither.right().value());
            } else {
                propertiesToUpdate = propertiesToUpdateEither.left().value();
                handleDeprecatedComponentInstancePropertyStructure(propertiesToUpdate, componentTypeEnum);
            }
        }
        if (!errorWrapper.isEmpty()) {
            return buildErrorResponse(errorWrapper.getInnerElement());
        }
        log.debug(START_HANDLE_REQUEST_OF_UPDATE_RESOURCE_INSTANCE_PROPERTY_RECEIVED_PROPERTY_IS, propertiesToUpdate);
        if (componentInstanceBusinessLogic == null) {
            log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
        }
        Either<List<ComponentInstanceProperty>, ResponseFormat> actionResponse = componentInstanceBusinessLogic
            .createOrUpdatePropertiesValues(componentTypeEnum, componentId, componentInstanceId, propertiesToUpdate, userId);
        if (actionResponse.isRight()) {
            return buildErrorResponse(actionResponse.right().value());
        }
        List<ComponentInstanceProperty> resourceInstanceProperties = actionResponse.left().value();
        ObjectMapper mapper = new ObjectMapper();
        String result;
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_COMPONENT_INSTANCE, StatusCode.COMPLETE,
            "Ended update Resource Instance Properties for component {} ", componentId + " by " + userId);
        try {
            result = mapper.writeValueAsString(resourceInstanceProperties);
        } catch (JsonProcessingException e) {
            log.error(UPDATE_RESOURCE_INSTANCE_WITH_EXCEPTION, e.getMessage(), e);
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        }
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_COMPONENT_INSTANCE, StatusCode.COMPLETE,
            "Ended update Resource Instance Properties for component {} ", componentId + " by user " + userId);
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
    }

    private void handleDeprecatedComponentInstancePropertyStructure(final List<ComponentInstanceProperty> propertiesToUpdate,
            final ComponentTypeEnum componentTypeEnum) {
        propertiesToUpdate.stream().forEach(property -> {
            if (property.getGetInputValues() != null) {
                property.getGetInputValues().stream()
                        .forEach(getInputValue -> property.setToscaFunction(createToscaFunction(getInputValue, componentTypeEnum)));
            }
        });
    }

    private ToscaGetFunctionDataDefinition createToscaFunction(final GetInputValueDataDefinition getInput,
            final ComponentTypeEnum componentTypeEnum) {
        final String[] inputIdSplit = getInput.getInputId().split("\\.");

        ToscaGetFunctionDataDefinition toscaFunction = new ToscaGetFunctionDataDefinition();
        toscaFunction.setFunctionType(ToscaGetFunctionType.GET_INPUT);
        toscaFunction.setPropertyUniqueId(getInput.getInputId());
        toscaFunction.setPropertySource(PropertySource.SELF);
        toscaFunction.setPropertyName(inputIdSplit[1]);
        toscaFunction.setSourceName(getSourceName(inputIdSplit[0], componentTypeEnum));
        toscaFunction.setSourceUniqueId(inputIdSplit[0]);
        toscaFunction.setPropertyPathFromSource(Collections.singletonList(inputIdSplit[1]));

        return toscaFunction;
    }

    private String getSourceName(final String componentId, final ComponentTypeEnum componentTypeEnum) {
        ComponentBusinessLogic compBL = componentBusinessLogicProvider.getInstance(componentTypeEnum);
        final Either<ComponentMetadataData, StorageOperationStatus> componentEither = compBL.getComponentMetadata(componentId);
        if (componentEither.isLeft()) {
            return componentEither.left().value().getMetadataDataDefinition().getName();
        }
        return "";
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/inputs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update resource instance property", method = "POST", summary = "Returns updated resource instance property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Resource instance created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateResourceInstanceInput(@Parameter(description = "service id") @PathParam("componentId") final String componentId,
                                                @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                                    ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                                    ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                                @Parameter(description = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId,
                                                @Parameter(description = "id of user initiating the operation") @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                @Context final HttpServletRequest request,
                                                @Parameter(description = "Component Instance Properties JSON Array", required = true) final String componentInstanceInputsJsonArray) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        List<ComponentInstanceInput> inputsToUpdate = new ArrayList<>();
        if (errorWrapper.isEmpty()) {
            Either<List<ComponentInstanceInput>, ResponseFormat> inputsToUpdateEither = convertMultipleInputs(componentInstanceInputsJsonArray);
            if (inputsToUpdateEither.isRight()) {
                errorWrapper.setInnerElement(inputsToUpdateEither.right().value());
            } else {
                inputsToUpdate = inputsToUpdateEither.left().value();
            }
        }
        if (!errorWrapper.isEmpty()) {
            return buildErrorResponse(errorWrapper.getInnerElement());
        }
        log.debug(START_HANDLE_REQUEST_OF_UPDATE_RESOURCE_INSTANCE_PROPERTY_RECEIVED_PROPERTY_IS, inputsToUpdate);
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        if (componentInstanceBusinessLogic == null) {
            log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
        }
        Either<List<ComponentInstanceInput>, ResponseFormat> actionResponse = componentInstanceBusinessLogic
            .createOrUpdateInstanceInputValues(componentTypeEnum, componentId, componentInstanceId, inputsToUpdate, userId);
        if (actionResponse.isRight()) {
            return buildErrorResponse(actionResponse.right().value());
        }
        List<ComponentInstanceInput> resourceInstanceInput = actionResponse.left().value();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        String result;
        loggerSupportability
            .log(LoggerSupportabilityActions.UPDATE_PROPERTIES, StatusCode.COMPLETE, "Ending update Resource Instance Input for component {} ",
                componentId + " by " + userId);
        try {
            result = mapper.writeValueAsString(resourceInstanceInput);
        } catch (JsonProcessingException e) {
            log.error(UPDATE_RESOURCE_INSTANCE_WITH_EXCEPTION, e.getMessage(), e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        loggerSupportability
            .log(LoggerSupportabilityActions.UPDATE_PROPERTIES, StatusCode.COMPLETE, "Ending update Resource Instance Input for component {} ",
                componentId + " by user " + userId);
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
    }

    /**
     * Updates ResourceInstance Attribute
     *
     * @param componentId
     * @param containerComponentType
     * @param componentInstanceId
     * @param userId
     * @param request
     * @return
     */
    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/attributes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update resource instance attribute", method = "POST", summary = "Returns updated resource instance property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Resource instance created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateResourceInstanceAttribute(@Parameter(description = "service id") @PathParam("componentId") final String componentId,
                                                    @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                                        ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                                        ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                                    @Parameter(description = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId,
                                                    @Parameter(description = "id of user initiating the operation") @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
                                                    @Context final HttpServletRequest request,
                                                    @Parameter(description = "Component Instance Properties JSON Array", required = true) final String componentInstanceAttributesJsonArray) {
        final String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_COMPONENT_INSTANCE, StatusCode.STARTED,
            "Starting to update Resource Instance Attributes for component {} ", componentId + " by " + userId);
        final Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        List<ComponentInstanceAttribute> attributesToUpdate = new ArrayList<>();
        if (errorWrapper.isEmpty()) {
            final Either<List<ComponentInstanceAttribute>, ResponseFormat> attributesToUpdateEither = convertMultipleAttributes(
                componentInstanceAttributesJsonArray);
            if (attributesToUpdateEither.isRight()) {
                errorWrapper.setInnerElement(attributesToUpdateEither.right().value());
            } else {
                attributesToUpdate = attributesToUpdateEither.left().value();
            }
        }
        if (!errorWrapper.isEmpty()) {
            return buildErrorResponse(errorWrapper.getInnerElement());
        }
        log.debug(START_HANDLE_REQUEST_OF_UPDATE_RESOURCE_INSTANCE_ATTRIBUTE_RECEIVED_ATTRIBUTE_IS, attributesToUpdate);
        final ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        if (componentInstanceBusinessLogic == null) {
            log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
        }
        final Either<List<ComponentInstanceAttribute>, ResponseFormat> actionResponse = componentInstanceBusinessLogic
            .createOrUpdateAttributeValues(componentTypeEnum, componentId, componentInstanceId, attributesToUpdate, userId);
        if (actionResponse.isRight()) {
            return buildErrorResponse(actionResponse.right().value());
        }
        final List<ComponentInstanceAttribute> resourceInstanceAttributes = actionResponse.left().value();
        final ObjectMapper mapper = new ObjectMapper();
        String result;
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_COMPONENT_INSTANCE, StatusCode.COMPLETE,
            "Ended update Resource Instance Attributes for component {} ", componentId + " by " + userId);
        try {
            result = mapper.writeValueAsString(resourceInstanceAttributes);
        } catch (JsonProcessingException e) {
            log.error(UPDATE_RESOURCE_INSTANCE_WITH_EXCEPTION, e.getMessage(), e);
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        }
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_COMPONENT_INSTANCE, StatusCode.COMPLETE,
            "Ended update Resource Instance Attributes for component {} ", componentId + " by user " + userId);
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
    }

    @DELETE
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/property/{propertyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update resource instance", method = "DELETE", summary = "Returns deleted resource instance property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Resource instance created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteResourceInstanceProperty(@Parameter(description = "service id") @PathParam("componentId") final String componentId,
                                                   @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                                       ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                                       ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                                   @Parameter(description = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId,
                                                   @Parameter(description = "property id") @PathParam("propertyId") final String propertyId,
                                                   @Parameter(description = "id of user initiating the operation") @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                   @Context final HttpServletRequest request) {
        loggerSupportability
            .log(LoggerSupportabilityActions.UPDATE_PROPERTIES, StatusCode.STARTED, "Starting to delete Resource Instance Property for component {} ",
                componentId + " by " + userId);
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            loggerSupportability
                .log(LoggerSupportabilityActions.UPDATE_PROPERTIES, StatusCode.COMPLETE, "Ended delete Resource Instance Property for component {} ",
                    componentId + " by " + userId);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<ComponentInstanceProperty, ResponseFormat> actionResponse = componentInstanceBusinessLogic
                .deletePropertyValue(componentTypeEnum, componentId, componentInstanceId, propertyId, userId);
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), null);
        } catch (Exception e) {
            log.error(CREATE_AND_ASSOCIATE_RI_FAILED_WITH_EXCEPTION, e.getMessage(), e);
            throw e;
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/changeVersion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update resource instance", method = "POST", summary = "Returns updated resource instance", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Resource instance created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response changeResourceInstanceVersion(@PathParam("componentId") final String componentId,
                                                  @PathParam("componentInstanceId") final String componentInstanceId,
                                                  @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                                      ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                                      ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                                  @Context final HttpServletRequest request) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try (InputStream inputStream = request.getInputStream()) {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            if (bytes == null || bytes.length == 0) {
                log.info(EMPTY_BODY_WAS_SENT);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
            String userId = request.getHeader(Constants.USER_ID_HEADER);
            String data = new String(bytes);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<ComponentInstance, ResponseFormat> convertResponse = convertToResourceInstance(data);
            if (convertResponse.isRight()) {
                BeEcompErrorManager.getInstance().logBeSystemError(RESOURCE_INSTANCE_UPDATE_RESOURCE_INSTANCE);
                log.debug(FAILED_TO_CONVERT_RECEIVED_DATA_TO_BE_FORMAT);
                return buildErrorResponse(convertResponse.right().value());
            }
            ComponentInstance newResourceInstance = convertResponse.left().value();
            ComponentInstance actionResponse = componentInstanceBusinessLogic
                .changeComponentInstanceVersion(containerComponentType, componentId, componentInstanceId, userId, newResourceInstance);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(UPDATE_RESOURCE_INSTANCE);
            log.debug(UPDATE_RESOURCE_INSTANCE_WITH_EXCEPTION, e);
            throw e;
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/groupInstance/{groupInstanceId}/property")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update resource instance property", method = "POST", summary = "Returns updated resource instance property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Resource instance created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateGroupInstanceProperty(@Parameter(description = "service id") @PathParam("componentId") final String componentId,
                                                @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                                    ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                                    ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                                @Parameter(description = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId,
                                                @Parameter(description = "group instance id") @PathParam("groupInstanceId") final String groupInstanceId,
                                                @Parameter(description = "id of user initiating the operation") @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                @Context final HttpServletRequest request) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            loggerSupportability
                .log(LoggerSupportabilityActions.UPDATE_PROPERTIES, StatusCode.STARTED, "Starting update Group Instance Property for component {} ",
                    componentId + " by " + userId);
            Wrapper<String> dataWrapper = new Wrapper<>();
            Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
            Wrapper<ComponentInstanceProperty> propertyWrapper = new Wrapper<>();
            validateInputStream(request, dataWrapper, errorWrapper);
            if (errorWrapper.isEmpty()) {
                validateClassParse(dataWrapper.getInnerElement(), propertyWrapper, () -> ComponentInstanceProperty.class, errorWrapper);
            }
            if (!errorWrapper.isEmpty()) {
                return buildErrorResponse(errorWrapper.getInnerElement());
            }
            ComponentInstanceProperty property = propertyWrapper.getInnerElement();
            log.debug(START_HANDLE_REQUEST_OF_UPDATE_RESOURCE_INSTANCE_PROPERTY_RECEIVED_PROPERTY_IS, property);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<ComponentInstanceProperty, ResponseFormat> actionResponse = componentInstanceBusinessLogic
                .createOrUpdateGroupInstancePropertyValue(componentTypeEnum, componentId, componentInstanceId, groupInstanceId, property, userId);
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            ComponentInstanceProperty resourceInstanceProperty = actionResponse.left().value();
            ObjectMapper mapper = new ObjectMapper();
            String result = mapper.writeValueAsString(resourceInstanceProperty);
            loggerSupportability
                .log(LoggerSupportabilityActions.UPDATE_PROPERTIES, StatusCode.COMPLETE, "Ended update Group Instance Property for component {} ",
                    componentId + " by " + userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
        } catch (Exception e) {
            log.error(CREATE_AND_ASSOCIATE_RI_FAILED_WITH_EXCEPTION, e.getMessage(), e);
            throw e;
        }
    }

    @GET
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/groupInstance/{groupInstId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get group artifacts ", method = "GET", summary = "Returns artifacts metadata according to groupInstId", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "group found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Group not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getGroupArtifactById(@PathParam("containerComponentType") final String containerComponentType,
                                         @PathParam("componentId") final String componentId,
                                         @PathParam("componentInstanceId") final String componentInstanceId,
                                         @PathParam("groupInstId") final String groupInstId, @Context final HttpServletRequest request,
                                         @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(GET_START_HANDLE_REQUEST_OF, url);
        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            Either<GroupDefinitionInfo, ResponseFormat> actionResponse = groupBL
                .getGroupInstWithArtifactsById(componentTypeEnum, componentId, componentInstanceId, groupInstId, userId, false);
            if (actionResponse.isRight()) {
                log.debug("failed to get all non abstract {}", containerComponentType);
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(GET_GROUP_ARTIFACT_BY_ID);
            log.debug(GET_GROUP_ARTIFACT_BY_ID_UNEXPECTED_EXCEPTION, e);
            throw e;
        }
    }

    // US831698
    @GET
    @Path("/{containerComponentType}/{containerComponentId}/componentInstances/{componentInstanceUniqueId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get component instance properties", method = "GET", summary = "Returns component instance properties", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Properties found"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component/Component Instance - not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getInstancePropertiesById(@PathParam("containerComponentType") final String containerComponentType,
                                              @PathParam("containerComponentId") final String containerComponentId,
                                              @PathParam("componentInstanceUniqueId") final String componentInstanceUniqueId,
                                              @Context final HttpServletRequest request,
                                              @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(GET_START_HANDLE_REQUEST_OF, url);
        List<ComponentInstanceProperty> componentInstancePropertiesById = componentInstanceBusinessLogic
            .getComponentInstancePropertiesById(containerComponentType, containerComponentId, componentInstanceUniqueId, userId);
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), componentInstancePropertiesById);
    }

    @GET
    @Path("/{containerComponentType}/{containerComponentId}/componentInstances/{componentInstanceUniqueId}/attributes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get component instance attributes", method = "GET", summary = "Returns component instance attributes", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Attributes found"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component/Component Instance - not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getInstanceAttributesById(@PathParam("containerComponentType") final String containerComponentType,
                                              @PathParam("containerComponentId") final String containerComponentId,
                                              @PathParam("componentInstanceUniqueId") final String componentInstanceUniqueId,
                                              @Context final HttpServletRequest request,
                                              @HeaderParam(value = Constants.USER_ID_HEADER) final String userId) {
        final String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(GET_START_HANDLE_REQUEST_OF, url);
        final List<ComponentInstanceAttribute> componentInstanceAttributesById = componentInstanceBusinessLogic
            .getComponentInstanceAttributesById(containerComponentType, containerComponentId, componentInstanceUniqueId, userId);
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), componentInstanceAttributesById);
    }

    // US330353
    @GET
    @Path("/{containerComponentType}/{containerComponentId}/componentInstances/{componentInstanceUniqueId}/capability/{capabilityType}/capabilityName/{capabilityName}/ownerId/{ownerId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get component instance capability properties", method = "GET", summary = "Returns component instance capability properties", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Properties found"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component/Component Instance/Capability - not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getInstanceCapabilityPropertiesById(@PathParam("containerComponentType") final String containerComponentType,
                                                        @PathParam("containerComponentId") final String containerComponentId,
                                                        @PathParam("componentInstanceUniqueId") final String componentInstanceUniqueId,
                                                        @PathParam("capabilityType") final String capabilityType,
                                                        @PathParam("capabilityName") final String capabilityName,
                                                        @PathParam("ownerId") final String ownerId, @Context final HttpServletRequest request,
                                                        @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(GET_START_HANDLE_REQUEST_OF, url);
        try {
            List<ComponentInstanceProperty> componentInstancePropertiesById = componentInstanceBusinessLogic
                .getComponentInstanceCapabilityPropertiesById(containerComponentType, containerComponentId, componentInstanceUniqueId, capabilityType,
                    capabilityName, ownerId, userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), componentInstancePropertiesById);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(GET_GROUP_ARTIFACT_BY_ID);
            log.debug(GET_GROUP_ARTIFACT_BY_ID_UNEXPECTED_EXCEPTION, e);
            throw e;
        }
    }

    //US 331281
    @PUT
    @Path("/{containerComponentType}/{containerComponentId}/componentInstances/{componentInstanceUniqueId}/capability/{capabilityType}/capabilityName/{capabilityName}/ownerId/{ownerId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update Instance Capabilty  Property", method = "PUT", summary = "Returns updated property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Resource instance capabilty property updated"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Component/Component Instance/Capability - not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateInstanceCapabilityProperty(@PathParam("containerComponentType") final String containerComponentType,
                                                     @PathParam("containerComponentId") final String containerComponentId,
                                                     @PathParam("componentInstanceUniqueId") final String componentInstanceUniqueId,
                                                     @PathParam("capabilityType") final String capabilityType,
                                                     @PathParam("capabilityName") final String capabilityName,
                                                     @PathParam("ownerId") final String ownerId,
                                                     @Parameter(description = "Instance capabilty property to update", required = true) String data,
                                                     @Context final HttpServletRequest request,
                                                     @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(PUT) Start handle request of {}", url);
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_INSTANCE_CAPABILITY_PROPERTY, StatusCode.STARTED,
            " Starting to update Instance Capability Property for component instance {} ", componentInstanceUniqueId + " by " + userId);
        try {
            Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
            List<ComponentInstanceProperty> propertiesToUpdate = new ArrayList<>();
            if (errorWrapper.isEmpty()) {
                Either<List<ComponentInstanceProperty>, ResponseFormat> propertiesToUpdateEither = convertMultipleProperties(data);
                if (propertiesToUpdateEither.isRight()) {
                    errorWrapper.setInnerElement(propertiesToUpdateEither.right().value());
                } else {
                    propertiesToUpdate = propertiesToUpdateEither.left().value();
                }
            }
            if (!errorWrapper.isEmpty()) {
                return buildErrorResponse(errorWrapper.getInnerElement());
            }
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            Either<List<ComponentInstanceProperty>, ResponseFormat> updateCICapProperty = componentInstanceBusinessLogic
                .updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId, componentInstanceUniqueId, capabilityType,
                    capabilityName, propertiesToUpdate, userId);
            if (updateCICapProperty.isRight()) {
                log.debug(FAILED_TO_GET_PROPERTIES_OF_COMPONENT_INSTANCE_ID_IN_WITH_ID, componentInstanceUniqueId, containerComponentType,
                    containerComponentId);
                return buildErrorResponse(updateCICapProperty.right().value());
            }
            loggerSupportability.log(LoggerSupportabilityActions.UPDATE_INSTANCE_CAPABILITY_PROPERTY, StatusCode.COMPLETE,
                " Ended to update Instance Capability Property for component instance {} ", componentInstanceUniqueId + " by " + userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), updateCICapProperty.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(GET_GROUP_ARTIFACT_BY_ID);
            log.debug(GET_GROUP_ARTIFACT_BY_ID_UNEXPECTED_EXCEPTION, e);
            throw e;
        }
    }

    @PUT
    @Path("/{containerComponentType}/{containerComponentId}/componentInstances/{componentInstanceUniqueId}/requirement/{capabilityType}/requirementName/{requirementName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update Instance Requirement", method = "PUT", summary = "Returns updated requirement", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Resource instance requirement updated"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Component/Component Instance/Requirement - not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateInstanceRequirement(@PathParam("containerComponentType") final String containerComponentType,
                                              @PathParam("containerComponentId") final String containerComponentId,
                                              @PathParam("componentInstanceUniqueId") final String componentInstanceUniqueId,
                                              @PathParam("capabilityType") final String capabilityType,
                                              @PathParam("requirementName") final String requirementName,
                                              @Parameter(description = "Instance capabilty requirement to update", required = true) String data,
                                              @Context final HttpServletRequest request,
                                              @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_INSTANCE_REQUIREMENT, StatusCode.STARTED,
            "Starting to update requirement {} in component instance {} by {}", requirementName, componentInstanceUniqueId, userId);
        try {
            log.debug(START_HANDLE_REQUEST_OF, url);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentTypeEnum == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<RequirementDefinition, ResponseFormat> mappedRequirementDataEither = getMappedRequirementData(data, new User(userId),
                componentTypeEnum);
            if (mappedRequirementDataEither.isRight()) {
                log.debug("Failed to update requirements");
                return buildErrorResponse(mappedRequirementDataEither.right().value());
            }
            RequirementDefinition requirementDef = mappedRequirementDataEither.left().value();
            Either<RequirementDefinition, ResponseFormat> response = componentInstanceBusinessLogic
                .updateInstanceRequirement(componentTypeEnum, containerComponentId, componentInstanceUniqueId, requirementDef, userId);
            if (response.isRight()) {
                return buildErrorResponse(response.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), response.left().value());
        } catch (ComponentException e) {
            throw e;
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update component instance requirement");
            log.debug("Update component instance requirement with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Either<RequirementDefinition, ResponseFormat> getMappedRequirementData(String inputJson, User user, ComponentTypeEnum componentTypeEnum) {
        return getComponentsUtils()
            .convertJsonToObjectUsingObjectMapper(inputJson, user, RequirementDefinition.class, AuditingActionEnum.GET_TOSCA_MODEL,
                componentTypeEnum);
    }

    @POST
    @Path("/{containerComponentType}/{containerComponentId}/serviceProxy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create service proxy", method = "POST", summary = "Returns created service proxy", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Service proxy created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Service proxy already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createServiceProxy(@Parameter(description = "RI object to be created", required = true) String data,
                                       @PathParam("containerComponentId") final String containerComponentId,
                                       @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                           ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                           ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                       @HeaderParam(value = Constants.USER_ID_HEADER) @Parameter(description = "USER_ID of modifier user", required = true) String userId,
                                       @Context final HttpServletRequest request) {
        try {
            ComponentInstance componentInstance = RepresentationUtils.fromRepresentation(data, ComponentInstance.class);
            componentInstance.setInvariantName(null);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentTypeEnum != ComponentTypeEnum.SERVICE) {
                log.debug("Unsupported container component type {}", containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceBusinessLogic.createServiceProxy();
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create service proxy");
            log.debug("Create service proxy failed with exception", e);
            throw e;
        }
    }

    @DELETE
    @Path("/{containerComponentType}/{containerComponentId}/serviceProxy/{serviceProxyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete service proxy", method = "DELETE", summary = "Returns delete service proxy", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Service proxy deleted"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteServiceProxy(@PathParam("containerComponentId") final String containerComponentId,
                                       @PathParam("serviceProxyId") final String serviceProxyId,
                                       @Parameter(description = "valid values: resources / services / products", schema = @Schema(allowableValues = {
                                           ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                           ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                       @Context final HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        try {
            log.debug(START_HANDLE_REQUEST_OF, url);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceBusinessLogic.deleteServiceProxy();
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            } else {
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), null);
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete service proxy");
            log.debug("Delete service proxy failed with exception", e);
            throw e;
        }
    }

    @POST
    @Path("/{containerComponentType}/{containerComponentId}/serviceProxy/{serviceProxyId}/changeVersion/{newServiceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update service proxy with new version", method = "POST", summary = "Returns updated service proxy", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Service proxy created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response changeServiceProxyVersion(@PathParam("containerComponentId") final String containerComponentId,
                                              @PathParam("serviceProxyId") final String serviceProxyId,
                                              @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                                  ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                                  ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                              @Context final HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceBusinessLogic.changeServiceProxyVersion();
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update service proxy with new version");
            log.debug("Update service proxy with new version failed with exception", e);
            throw e;
        }
    }

    /**
     * REST API GET relation by Id Allows to get relation contained in specified component according to received Id
     *
     * @param containerComponentType
     * @param componentId
     * @param relationId
     * @param request
     * @param userId
     * @return Response
     */
    @GET
    @Path("/{containerComponentType}/{componentId}/relationId/{relationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get relation", method = "GET", summary = "Returns relation metadata according to relationId", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "relation found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Relation not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getRelationById(@PathParam("containerComponentType") final String containerComponentType,
                                    @PathParam("componentId") final String componentId, @PathParam("relationId") final String relationId,
                                    @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(GET_START_HANDLE_REQUEST_OF, url);
        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentTypeEnum == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<RequirementCapabilityRelDef, ResponseFormat> actionResponse = componentInstanceBusinessLogic
                .getRelationById(componentId, relationId, userId, componentTypeEnum);
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("getRelationById");
            log.debug("getRelationById unexpected exception", e);
            throw e;
        }
    }

    private Either<ComponentInstance, ResponseFormat> convertToResourceInstance(String data) {
        Either<ComponentInstance, ResponseFormat> convertStatus = getComponentsUtils()
            .convertJsonToObjectUsingObjectMapper(data, new User(), ComponentInstance.class, null, ComponentTypeEnum.RESOURCE_INSTANCE);
        if (convertStatus.isRight()) {
            return Either.right(convertStatus.right().value());
        }
        ComponentInstance resourceInstanceInfo = convertStatus.left().value();
        return Either.left(resourceInstanceInfo);
    }

    private Either<List<ComponentInstance>, ResponseFormat> convertToMultipleResourceInstance(String dataList) {
        Either<ComponentInstance[], ResponseFormat> convertStatus = getComponentsUtils()
            .convertJsonToObjectUsingObjectMapper(dataList, new User(), ComponentInstance[].class, null, ComponentTypeEnum.RESOURCE_INSTANCE);
        if (convertStatus.isRight()) {
            return Either.right(convertStatus.right().value());
        }
        return Either.left(Arrays.asList(convertStatus.left().value()));
    }

    private Either<List<ComponentInstanceProperty>, ResponseFormat> convertMultipleProperties(String dataList) {
        if (StringUtils.isEmpty(dataList)) {
            return Either.right(getComponentsUtils().getResponseFormat(ActionStatus.MISSING_BODY));
        }
        Either<ComponentInstanceProperty[], ResponseFormat> convertStatus = getComponentsUtils()
            .convertJsonToObjectUsingObjectMapper(dataList, new User(), ComponentInstanceProperty[].class, null, ComponentTypeEnum.RESOURCE_INSTANCE);
        if (convertStatus.isRight()) {
            return Either.right(convertStatus.right().value());
        }
        return Either.left(Arrays.asList(convertStatus.left().value()));
    }

    private Either<List<ComponentInstanceAttribute>, ResponseFormat> convertMultipleAttributes(final String dataList) {
        if (StringUtils.isEmpty(dataList)) {
            return Either.right(getComponentsUtils().getResponseFormat(ActionStatus.MISSING_BODY));
        }
        final Either<ComponentInstanceAttribute[], ResponseFormat> convertStatus = getComponentsUtils().
            convertJsonToObjectUsingObjectMapper(dataList, new User(), ComponentInstanceAttribute[].class, null, ComponentTypeEnum.RESOURCE_INSTANCE);
        if (convertStatus.isRight()) {
            return Either.right(convertStatus.right().value());
        }
        return Either.left(Arrays.asList(convertStatus.left().value()));
    }

    private Either<List<ComponentInstanceInput>, ResponseFormat> convertMultipleInputs(String dataList) {
        if (StringUtils.isEmpty(dataList)) {
            return Either.right(getComponentsUtils().getResponseFormat(ActionStatus.MISSING_BODY));
        }
        Either<ComponentInstanceInput[], ResponseFormat> convertStatus = getComponentsUtils()
            .convertJsonToObjectUsingObjectMapper(dataList, new User(), ComponentInstanceInput[].class, null, ComponentTypeEnum.RESOURCE_INSTANCE);
        if (convertStatus.isRight()) {
            return Either.right(convertStatus.right().value());
        }
        return Either.left(Arrays.asList(convertStatus.left().value()));
    }

    private RequirementCapabilityRelDef convertToRequirementCapabilityRelDef(String data) {
        Either<RequirementCapabilityRelDef, ActionStatus> convertStatus = convertJsonToObject(data, RequirementCapabilityRelDef.class);
        if (convertStatus.isRight()) {
            throw new ByActionStatusComponentException(convertStatus.right().value());
        }
        return convertStatus.left().value();
    }

    @Override
    public <T> Either<T, ActionStatus> convertJsonToObject(String data, Class<T> clazz) {
        try {
            log.trace("convert json to object. json=\n {}", data);
            T t;
            t = gsonDeserializer.fromJson(data, clazz);
            if (t == null) {
                BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
                log.debug("object is null after converting from json");
                return Either.right(ActionStatus.INVALID_CONTENT);
            }
            return Either.left(t);
        } catch (Exception e) {
            // INVALID JSON
            BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
            log.debug("failed to convert from json", e);
            return Either.right(ActionStatus.INVALID_CONTENT);
        }
    }

    @GET
    @Path("/{containerComponentType}/{componentId}/paths-to-delete")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Check if forwarding path to delete on version change", method = "GET", summary = "Returns forwarding paths to delete", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class))))})
    public Response changeResourceInstanceVersion(@PathParam("componentId") String componentId,
                                                  @QueryParam("componentInstanceId") final String oldComponentInstanceId,
                                                  @QueryParam("newComponentInstanceId") final String newComponentInstanceId,
                                                  @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                                      ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                                      ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
                                                  @Context final HttpServletRequest request) {
        if (oldComponentInstanceId == null) {
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.MISSING_OLD_COMPONENT_INSTANCE));
        }
        if (newComponentInstanceId == null) {
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.MISSING_NEW_COMPONENT_INSTANCE));
        }
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        if (componentInstanceBusinessLogic == null) {
            log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
        }
        ComponentInstance newComponentInstance;
        if (StringUtils.isNotEmpty(newComponentInstanceId)) {
            newComponentInstance = new ComponentInstance();
            newComponentInstance.setToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_UID, newComponentInstanceId);
        } else {
            log.error("missing component id");
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.MISSING_DATA));
        }
        Either<Set<String>, ResponseFormat> actionResponse = componentInstanceBusinessLogic
            .forwardingPathOnVersionChange(containerComponentType, componentId, oldComponentInstanceId, newComponentInstance);
        if (actionResponse.isRight()) {
            return buildErrorResponse(actionResponse.right().value());
        }
        ForwardingPaths forwardingPaths = new ForwardingPaths();
        forwardingPaths.setForwardingPathToDelete(actionResponse.left().value());
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), forwardingPaths);
    }

    @POST
    @Path("/services/{componentId}/copyComponentInstance/{componentInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces((MediaType.APPLICATION_JSON))
    @Operation(description = "Copy Component Instance", method = "POST", summary = "Returns updated service information", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Service.class)))),
        @ApiResponse(responseCode = "201", description = "Copy and Paste Success"),
        @ApiResponse(responseCode = "403", description = "Restricted Operation"),
        @ApiResponse(responseCode = "400", description = "Invalid Content / Missing content")})
    public Response copyComponentInstance(
        @Parameter(description = "service unique id in pasted canvas") @PathParam("componentId") final String containerComponentId,
        @Parameter(description = "Data for copying", required = true) String data, @PathParam("componentInstanceId") final String componentInstanceId,
        @Context final HttpServletRequest request) {
        log.info("Start to copy component instance");
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        final String CNTAINER_CMPT_TYPE = SERVICES;
        try {
            ComponentInstance inputComponentInstance = RepresentationUtils.fromRepresentation(data, ComponentInstance.class);
            inputComponentInstance.setInvariantName(null);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(CNTAINER_CMPT_TYPE);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, componentTypeEnum);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, SERVICES));
            }
            Either<Map<String, ComponentInstance>, ResponseFormat> copyComponentInstance = componentInstanceBusinessLogic
                .copyComponentInstance(inputComponentInstance, containerComponentId, componentInstanceId, userId);
            if (copyComponentInstance.isRight()) {
                log.error("Failed to copy ComponentInstance {}", copyComponentInstance.right().value());
                return buildErrorResponse(copyComponentInstance.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), copyComponentInstance.left().value());
        } catch (Exception e) {
            log.error("Failed to convert json to Map { }", data, e);
            return buildErrorResponse(
                getComponentsUtils().getResponseFormat(ActionStatus.USER_DEFINED, "Failed to get the copied component instance information"));
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/batchDeleteResourceInstances/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Batch Delete ResourceInstances", method = "POST", responses = {
        @ApiResponse(responseCode = "203", description = "ResourceInstances deleted"),
        @ApiResponse(responseCode = "403", description = "Restricted Operation"),
        @ApiResponse(responseCode = "400", description = "Invalid Content / Missing Content")})
    public Response batchDeleteResourceInstances(
        @Parameter(description = "valid values: resources / services / products", schema = @Schema(allowableValues = {
            ComponentTypeEnum.RESOURCE_PARAM_NAME, ComponentTypeEnum.SERVICE_PARAM_NAME,
            ComponentTypeEnum.PRODUCT_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
        @PathParam("componentId") final String componentId, @Context final HttpServletRequest request,
        @Parameter(description = "Component Instance Id List", required = true) final String componentInstanceIdLisStr) {
        try {
            if (componentInstanceIdLisStr == null || componentInstanceIdLisStr.isEmpty()) {
                log.error("Empty JSON List was sent", componentInstanceIdLisStr);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
            if (componentInstanceBusinessLogic == null) {
                log.error(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<List<String>, ResponseFormat> convertResponse = convertToStringList(componentInstanceIdLisStr);
            if (convertResponse.isRight()) {
                BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - batchDeleteResourceInstances");
                log.error(FAILED_TO_CONVERT_RECEIVED_DATA_TO_BE_FORMAT);
                return buildErrorResponse(convertResponse.right().value());
            }
            String userId = request.getHeader(Constants.USER_ID_HEADER);
            List<String> componentInstanceIdList = convertResponse.left().value();
            log.debug("batchDeleteResourceInstances componentInstanceIdList is {}", componentInstanceIdList);
            Map<String, List<String>> deleteErrorMap = componentInstanceBusinessLogic
                .batchDeleteComponentInstance(containerComponentType, componentId, componentInstanceIdList, userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), deleteErrorMap);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Batch Delete ResourceInstances");
            log.error("batch delete resource instances with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/resourceInstance/batchDissociate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Batch Dissociate RI from RI", method = "PUT", summary = "Returns deleted RelationShip Info", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Relationship deleted"),
        @ApiResponse(responseCode = "403", description = "Missing Information"),
        @ApiResponse(responseCode = "400", description = "Invalid Content / Missing Content")})
    public Response batchDissociateRIFromRI(
        @Parameter(description = "allowed values are resources/services/products", schema = @Schema(allowableValues = {
            ComponentTypeEnum.RESOURCE_PARAM_NAME, ComponentTypeEnum.SERVICE_PARAM_NAME,
            ComponentTypeEnum.PRODUCT_PARAM_NAME}), required = true) @PathParam("containerComponentType") final String containerComponentType,
        @Parameter(description = "unique id of the container component") @PathParam("componentId") final String componentId,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Parameter(description = "RelationshipInfo", required = true) String data,
        @Context final HttpServletRequest request) {
        try {
            if (data == null || data.length() == 0) {
                log.info("Empty JSON list was sent");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<List<RequirementCapabilityRelDef>, ResponseFormat> regInfoWs = convertToRequirementCapabilityRelDefList(data);
            if (regInfoWs.isRight()) {
                BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - batch dissociateRIFromRI");
                log.debug("Failed to convert received data to BE format");
                return buildErrorResponse(regInfoWs.right().value());
            }
            List<RequirementCapabilityRelDef> requirementDefList = regInfoWs.left().value();
            List<RequirementCapabilityRelDef> delOkResult = componentInstanceBusinessLogic
                .batchDissociateRIFromRI(componentId, userId, requirementDefList, componentTypeEnum);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), delOkResult);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Batch Dissociate Resource Instance");
            log.debug("batch dissociate resource instance from service failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Either<List<String>, ResponseFormat> convertToStringList(String datalist) {
        Either<String[], ResponseFormat> convertStatus = getComponentsUtils()
            .convertJsonToObjectUsingObjectMapper(datalist, new User(), String[].class, null, null);
        if (convertStatus.isRight()) {
            return Either.right(convertStatus.right().value());
        }
        return Either.left(Arrays.asList(convertStatus.left().value()));
    }

    private Either<List<RequirementCapabilityRelDef>, ResponseFormat> convertToRequirementCapabilityRelDefList(String data) {
        Either<RequirementCapabilityRelDef[], ResponseFormat> convertStatus = getComponentsUtils()
            .convertJsonToObjectUsingObjectMapper(data, new User(), RequirementCapabilityRelDef[].class, null, null);
        if (convertStatus.isRight()) {
            return Either.right(convertStatus.right().value());
        }
        return Either.left(Arrays.asList(convertStatus.left().value()));
    }

    @POST
    @Path("/services/replaceVNF")
    @Operation(description = "Replace new VNF based on the existing VNF", method = "POST", summary = "Return whether the replace VNF is successful", responses = {
        @ApiResponse(responseCode = "200", description = "ECOMP component is authenticated and list of Catalog Assets Metadata is returned", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReplaceVNFInfo.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Error: Requested '%1' (uuid) resource was not found - SVC4063"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed  :  Invalid HTTP method type used ( PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem. ECOMP Component should continue the attempts to get the needed information - POL5000"),
        @ApiResponse(responseCode = "409", description = "Service already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response replaceVNF(@Parameter(description = "The user id", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                               @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
                               @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
                               @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
                               @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
                               @Context final HttpServletRequest request,
                               @Parameter(description = "Resource object to be created", required = true) String data) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("replaceVNF: Start handle request of {}", url);
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("replaceVNF:modifier id is {}", userId);
        validateNotEmptyBody(data);
        Either<ReplaceVNFInfo, ResponseFormat> convertResponse = parseToReplaceVNFInfo(data, modifier);
        if (convertResponse.isRight()) {
            throw new ByResponseFormatComponentException(convertResponse.right().value());
        }
        log.debug("replaceVNF:get ReplaceVNFInfo success");
        String containerComponentType = SERVICES;
        ReplaceVNFInfo replaceVNFInfo = convertResponse.left().value();
        String serviceUniqueId = replaceVNFInfo.getServiceUniqueId();
        String abstractResourceUniqueId = replaceVNFInfo.getAbstractResourceUniqueId();
        ComponentInstance componentInstance = replaceVNFInfo.getRealVNFComponentInstance();
        log.debug("replaceVNF:get ReplaceVNFInfo,serviceUniqueId:{},abstractResourceUniqueId:{}", serviceUniqueId, abstractResourceUniqueId);
        try {
            /**
             * delete vnf
             */
            if (componentInstanceBusinessLogic == null) {
                log.debug("replaceVNF:Unsupported component type {}", containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<ComponentInstance, ResponseFormat> componentInstanceEither = componentInstanceBusinessLogic
                .deleteAbstractComponentInstance(containerComponentType, serviceUniqueId, abstractResourceUniqueId, userId);
            if (componentInstanceEither.isRight()) {
                log.debug("replaceVNF:delete Abstract ComponentInstance field");
                return buildErrorResponse(componentInstanceEither.right().value());
            }
            /**
             * add vnf
             */
            log.debug("replaceVNF,start add vnf");
            componentInstance.setInvariantName(null);
            componentInstance.setCreatedFrom(CreatedFrom.UI);
            Either<ComponentInstance, ResponseFormat> realComponentInstance = componentInstanceBusinessLogic
                .createRealComponentInstance(containerComponentType, serviceUniqueId, userId, componentInstance);
            if (realComponentInstance.isRight()) {
                log.debug("replaceVNF:filed to add vnf");
                return buildErrorResponse(realComponentInstance.right().value());
            }
            ComponentInstance newComponentInstance = realComponentInstance.left().value();
            log.debug("replaceVNF:success to add vnf");
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), newComponentInstance);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("replaceVNF");
            log.debug("replaceVNF with exception", e);
            throw e;
        }
    }

    private Either<ReplaceVNFInfo, ResponseFormat> parseToReplaceVNFInfo(String serviceJson, User user) {
        log.debug("enter parseToReplaceVNFInfo,get serviceJson:{}", serviceJson);
        return getComponentsUtils().convertJsonToObjectUsingObjectMapper(serviceJson, user, ReplaceVNFInfo.class, AuditingActionEnum.CREATE_RESOURCE,
            ComponentTypeEnum.SERVICE);
    }
}
