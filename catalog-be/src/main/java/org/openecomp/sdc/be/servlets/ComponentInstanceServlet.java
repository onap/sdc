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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.utils.DirectivesUtils;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.ForwardingPaths;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.CreatedFrom;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.info.CreateAndAssotiateInfo;
import org.openecomp.sdc.be.info.GroupDefinitionInfo;
import org.openecomp.sdc.be.mixin.ComponentInstancePropertyMixin;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintDeserialiser;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.be.view.ResponseView;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.stereotype.Controller;

/**
 * Root resource (exposed at "/" path) .json
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@OpenAPIDefinition(info = @Info(title = "Resource Instance Servlet"))
@Controller
public class ComponentInstanceServlet extends AbstractValidationsServlet {

    private static final String FAILED_TO_GET_PROPERTIES_OF_COMPONENT_INSTANCE_ID_IN_WITH_ID = "Failed to get properties of component instance ID: {} in {} with ID: {}";
	private static final String GET_GROUP_ARTIFACT_BY_ID = "getGroupArtifactById";
	private static final String GET_GROUP_ARTIFACT_BY_ID_UNEXPECTED_EXCEPTION = "getGroupArtifactById unexpected exception";
	private static final String GET_START_HANDLE_REQUEST_OF = "(GET) Start handle request of {}";
	private static final String START_HANDLE_REQUEST_OF_UPDATE_RESOURCE_INSTANCE_PROPERTY_RECEIVED_PROPERTY_IS = "Start handle request of updateResourceInstanceProperty. Received property is {}";
	private static final String UPDATE_RESOURCE_INSTANCE = "Update Resource Instance";
	private static final String RESOURCE_INSTANCE_UPDATE_RESOURCE_INSTANCE = "Resource Instance - updateResourceInstance";
	private static final String UPDATE_RESOURCE_INSTANCE_WITH_EXCEPTION = "update resource instance with exception";
	private static final String FAILED_TO_CONVERT_RECEIVED_DATA_TO_BE_FORMAT = "Failed to convert received data to BE format.";
	private static final String EMPTY_BODY_WAS_SENT = "Empty body was sent.";
	private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";
	private static final String UNSUPPORTED_COMPONENT_TYPE = "Unsupported component type {}";
    private static final String CREATE_AND_ASSOCIATE_RI_FAILED_WITH_EXCEPTION = "create and associate RI failed with exception: {}";
	private static final Logger log = Logger.getLogger(ComponentInstanceServlet.class);
    private static final Type PROPERTY_CONSTRAINT_TYPE = new TypeToken<PropertyConstraint>() {}.getType();
    private static final Gson gsonDeserializer = new GsonBuilder().registerTypeAdapter(PROPERTY_CONSTRAINT_TYPE, new PropertyConstraintDeserialiser()).create();
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(ComponentInstanceServlet.class.getName());

    private final GroupBusinessLogic groupBL;
    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private final ServiceBusinessLogic serviceBusinessLogic;


    @Inject
    public ComponentInstanceServlet(UserBusinessLogic userBusinessLogic,
        GroupBusinessLogic groupBL, ComponentInstanceBusinessLogic componentInstanceBL,
        ComponentsUtils componentsUtils, ServletUtils servletUtils,
        ResourceImportManager resourceImportManager,
        ServiceBusinessLogic serviceBusinessLogic) {
        super(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.groupBL = groupBL;
        this.componentInstanceBusinessLogic = componentInstanceBL;
        this.serviceBusinessLogic = serviceBusinessLogic;
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create ComponentInstance", method = "POST", summary = "Returns created ComponentInstance",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Component created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
            @ApiResponse(responseCode = "409", description = "Component instance already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createComponentInstance(@Parameter(description = "RI object to be created", required = true) String data,
            @PathParam("componentId") final String containerComponentId,
            @Parameter(description = "valid values: resources / services",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME ,
                            ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @HeaderParam(value = Constants.USER_ID_HEADER) @Parameter(description = "USER_ID of modifier user",
                    required = true) String userId,
            @Context final HttpServletRequest request) {
        
        validateNotEmptyBody(data);
        ComponentInstance componentInstance = null;
        try {
            componentInstance = RepresentationUtils.fromRepresentation(data, ComponentInstance.class);
            componentInstance.setInvariantName(null);
            componentInstance.setCreatedFrom(CreatedFrom.UI);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Component Instance");
            log.debug("create component instance failed with exception", e);
            throw new ByActionStatusComponentException(ActionStatus.INVALID_CONTENT);
        }
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_INSTANCE, StatusCode.STARTED,"Starting to create component instance by {}",userId);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            ComponentInstance actionResponse = componentInstanceBusinessLogic.createComponentInstance(containerComponentType, containerComponentId, userId, componentInstance);
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_INSTANCE,actionResponse.getComponentMetadataForSupportLog(),StatusCode.COMPLETE,"Ending to create component instance by user {}",userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResponse);

    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update resource instance", method = "POST", summary = "Returns updated resource instance",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Resource instance updated"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateComponentInstanceMetadata(@PathParam("componentId") final String componentId,
            @PathParam("componentInstanceId") final String componentInstanceId,
            @Parameter(description = "valid values: resources / services / products",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME,
                            ComponentTypeEnum.PRODUCT_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @Context final HttpServletRequest request) throws IOException {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_COMPONENT_INSTANCE,StatusCode.STARTED,"update Component Instance Metadata");
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
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
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

            ComponentInstance resourceInstance = convertResponse.left().value();
            Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceBusinessLogic.updateComponentInstanceMetadata(containerComponentType, componentId, componentInstanceId, userId, resourceInstance);
            loggerSupportability.log(LoggerSupportabilityActions.UPDATE_COMPONENT_INSTANCE,actionResponse.left().value().getComponentMetadataForSupportLog(),StatusCode.COMPLETE,"update Component Instance Metadata by {}",userId);
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            ComponentInstance resultValue = actionResponse.left().value();
            if (componentTypeEnum.equals(ComponentTypeEnum.SERVICE)){
                boolean shouldCreateServiceFilter = resourceInstance.getDirectives() != null && resourceInstance.getDirectives().contains(
                        DirectivesUtils.SELECTABLE);

                if(shouldCreateServiceFilter) {
                    Either<CINodeFilterDataDefinition, ResponseFormat> either =
                            serviceBusinessLogic.createIfNotAlreadyExistServiceFilter(componentId, componentInstanceId, userId,
                                    true);
                    if (either.isRight()){
                        BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - updateResourceInstance Failed to create service filter.");
                        log.debug("Failed to create service filter.");
                        return buildErrorResponse(convertResponse.right().value());
                    }
                    resultValue.setNodeFilter(either.left().value());
                } else {
                    Either<String, ResponseFormat> either = serviceBusinessLogic.deleteIfNotAlreadyDeletedServiceFilter(componentId, componentInstanceId,  userId,true);
                    if (either.isRight()){
                        BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - updateResourceInstance Failed to delete service filter.");
                        log.debug("Failed to delete service filter.");
                        return buildErrorResponse(convertResponse.right().value());
                    }
                    resultValue.setNodeFilter(null);
                }
            }

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(UPDATE_RESOURCE_INSTANCE);
            log.debug(UPDATE_RESOURCE_INSTANCE_WITH_EXCEPTION, e);
            throw e;
        }

    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/multipleComponentInstance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update resource instance multiple component", method = "POST",
            summary = "Returns updated resource instance", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Resource instance updated"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateMultipleComponentInstance(@PathParam("componentId") final String componentId, @Parameter(
            description = "valid values: resources / services / products",
            schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                    ComponentTypeEnum.SERVICE_PARAM_NAME,
                    ComponentTypeEnum.PRODUCT_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @Context final HttpServletRequest request, @Parameter(description = "Component Instance JSON Array",
                    required = true) final String componentInstanceJsonArray) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        try {
            log.debug(START_HANDLE_REQUEST_OF, url);

            if (componentInstanceJsonArray == null || componentInstanceJsonArray.length() == 0) {
                log.info("Empty JSON list was sent.");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }

            String userId = request.getHeader(Constants.USER_ID_HEADER);

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
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

            List<ComponentInstance> actionResponse = componentInstanceBusinessLogic.updateComponentInstance(containerComponentType, null, componentId, userId, componentInstanceList, true);
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
    @Operation(description = "Delete ResourceInstance", method = "DELETE", summary = "Returns delete resourceInstance",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "ResourceInstance deleted"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteResourceInstance(@PathParam("componentId") final String componentId,
            @PathParam("resourceInstanceId") final String resourceInstanceId,
            @Parameter(description = "valid values: resources / services / products",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME,
                            ComponentTypeEnum.PRODUCT_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @Context final HttpServletRequest request) {
        
        String url = request.getMethod() + " " + request.getRequestURI();

        try {
            log.debug(START_HANDLE_REQUEST_OF, url);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            String userId = request.getHeader(Constants.USER_ID_HEADER);
            ComponentInstance actionResponse = componentInstanceBusinessLogic.deleteComponentInstance(containerComponentType, componentId, resourceInstanceId, userId);
            loggerSupportability.log(LoggerSupportabilityActions.DELETE_COMPONENT_INSTANCE,actionResponse.getComponentMetadataForSupportLog(),StatusCode.STARTED,"DELETE_COMPONENT_INSTANCE by user {}", userId);
            loggerSupportability.log(LoggerSupportabilityActions.DELETE_COMPONENT_INSTANCE,actionResponse.getComponentMetadataForSupportLog(),StatusCode.COMPLETE,"DELETE_COMPONENT_INSTANCE by user {}", userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Resource Instance");
            log.debug("delete resource instance with exception", e);
            throw e;
        }
    }

    @Parameter(description = "allowed values are resources /services / products",
            schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                    ComponentTypeEnum.SERVICE_PARAM_NAME,
                    ComponentTypeEnum.PRODUCT_PARAM_NAME}),
            required = true)
    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/associate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Associate RI to RI", method = "POST", summary = "Returns created RelationshipInfo",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Relationship created"),
            @ApiResponse(responseCode = "403", description = "Missing information"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
            @ApiResponse(responseCode = "409", description = "Relationship already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response associateRIToRI(@Parameter(
            description = "unique id of the container component") @PathParam("componentId") final String componentId,
            @Parameter(description = "allowed values are resources /services / products",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME, ComponentTypeEnum.PRODUCT_PARAM_NAME}),
                    required = true) @PathParam("containerComponentType") final String containerComponentType,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @Parameter(description = "RelationshipInfo", required = true) String data,
            @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        Response response = null;
        loggerSupportability.log(LoggerSupportabilityActions.ASSOCIATE_RI_TO_RI, StatusCode.STARTED,"Starting to associate RI To RI for component {} ",componentId + " by " +  userId );
        try {

            log.debug(START_HANDLE_REQUEST_OF, url);

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }

            RequirementCapabilityRelDef requirementDef = convertToRequirementCapabilityRelDef(data);
            requirementDef.setOriginUI(true);

            RequirementCapabilityRelDef actionResponse = componentInstanceBusinessLogic.associateRIToRI(componentId, userId, requirementDef, componentTypeEnum);
            loggerSupportability.log(LoggerSupportabilityActions.ASSOCIATE_RI_TO_RI, StatusCode.COMPLETE,"Ended associate RI To RI for component {} ",componentId + " by " +  userId );
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse);

        } catch (Exception e) {
            if(!e.getClass().equals(ComponentException.class)) {
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
    @Operation(description = "Dissociate RI from RI", method = "PUT", summary = "Returns deleted RelationshipInfo",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Relationship deleted"),
            @ApiResponse(responseCode = "403", description = "Missing information"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response dissociateRIFromRI(
            @Parameter(description = "allowed values are resources /services / products",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME, ComponentTypeEnum.PRODUCT_PARAM_NAME}),
                    required = true) @PathParam("containerComponentType") final String containerComponentType,
            @Parameter(
                    description = "unique id of the container component") @PathParam("componentId") final String componentId,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @Parameter(description = "RelationshipInfo", required = true) String data,
            @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        loggerSupportability.log(LoggerSupportabilityActions.UN_ASSOCIATE_RI_TO_RI, StatusCode.STARTED,"Starting to undo associate RI To RI for component {} ",componentId + " by " +  userId );
        try {

            log.debug(START_HANDLE_REQUEST_OF, url);

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }

            RequirementCapabilityRelDef requirementDef = convertToRequirementCapabilityRelDef(data);
            RequirementCapabilityRelDef actionResponse = componentInstanceBusinessLogic.dissociateRIFromRI(componentId, userId, requirementDef, componentTypeEnum);
            loggerSupportability.log(LoggerSupportabilityActions.UN_ASSOCIATE_RI_TO_RI, StatusCode.COMPLETE,"Ended undo associate RI To RI for component {} ",componentId + " by " +  userId );
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
    @Operation(description = "Create RI and associate RI to RI", method = "POST",
            summary = "Returns created RI and RelationshipInfo", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "RI created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
            @ApiResponse(responseCode = "409", description = "Relationship already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createAndAssociateRIToRI(@PathParam("componentId") final String componentId, @Parameter(
            description = "valid values: resources / services",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME, ComponentTypeEnum.PRODUCT_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
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

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }

            Either<CreateAndAssotiateInfo, ActionStatus> convertStatus = convertJsonToObject(data, CreateAndAssotiateInfo.class);
            if (convertStatus.isRight()) {
                BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - createAndAssociateRIToRI");
                log.debug(FAILED_TO_CONVERT_RECEIVED_DATA_TO_BE_FORMAT);
                Either<Object, ResponseFormat> formattedResponse = Either.right(getComponentsUtils().getResponseFormat(convertStatus.right().value()));
                return buildErrorResponse(formattedResponse.right().value());
            }

            CreateAndAssotiateInfo createAndAssotiateInfo = convertStatus.left().value();
            RequirementCapabilityRelDef requirementDef = createAndAssotiateInfo.getAssociate();
            requirementDef.setOriginUI(true);
            Either<CreateAndAssotiateInfo, ResponseFormat> actionResponse = componentInstanceBusinessLogic.createAndAssociateRIToRI(containerComponentType, componentId, userId, createAndAssotiateInfo);

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
    @Operation(description = "Update resource instance property", method = "POST",
            summary = "Returns updated resource instance property", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Resource instance created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateResourceInstanceProperties(
            @Parameter(description = "service id") @PathParam("componentId") final String componentId,
            @Parameter(description = "valid values: resources / services",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @Parameter(
                    description = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId,
            @Parameter(description = "id of user initiating the operation") @HeaderParam(
                    value = Constants.USER_ID_HEADER) String userId,
            @Context final HttpServletRequest request,
            @Parameter(description = "Component Instance Properties JSON Array",
                    required = true) final String componentInstancePropertiesJsonArray) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_COMPONENT_INSTANCE, StatusCode.STARTED,"Starting to update Resource Instance Properties for component {} ",componentId + " by " +  userId );

        Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
        List<ComponentInstanceProperty> propertiesToUpdate = new ArrayList<>();
        if (errorWrapper.isEmpty()) {
                Either<List<ComponentInstanceProperty>, ResponseFormat> propertiesToUpdateEither = convertMultipleProperties(componentInstancePropertiesJsonArray);
                if (propertiesToUpdateEither.isRight()) {
                    errorWrapper.setInnerElement(propertiesToUpdateEither.right().value());
                } else {
                    propertiesToUpdate = propertiesToUpdateEither.left().value();
                }
        }
        if (!errorWrapper.isEmpty()) {
                return buildErrorResponse(errorWrapper.getInnerElement());
        }
        log.debug(START_HANDLE_REQUEST_OF_UPDATE_RESOURCE_INSTANCE_PROPERTY_RECEIVED_PROPERTY_IS, propertiesToUpdate);
        
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
        }
        Either<List<ComponentInstanceProperty>, ResponseFormat> actionResponse = componentInstanceBusinessLogic.createOrUpdatePropertiesValues(componentTypeEnum, componentId, componentInstanceId, propertiesToUpdate, userId);
        if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
        }
        List<ComponentInstanceProperty> resourceInstanceProperties = actionResponse.left().value();
        ObjectMapper mapper = new ObjectMapper();
        String result;
            loggerSupportability.log(LoggerSupportabilityActions.UPDATE_COMPONENT_INSTANCE, StatusCode.COMPLETE,"Ended update Resource Instance Properties for component {} ",componentId + " by " +  userId );
        try {
            result = mapper.writeValueAsString(resourceInstanceProperties);
        } catch (JsonProcessingException e) {
            log.error(UPDATE_RESOURCE_INSTANCE_WITH_EXCEPTION, e.getMessage(), e);
            throw new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR);
        }
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_COMPONENT_INSTANCE, StatusCode.COMPLETE,"Ended update Resource Instance Properties for component {} ",componentId + " by user " +  userId );
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/inputs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update resource instance property", method = "POST",
            summary = "Returns updated resource instance property", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Resource instance created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateResourceInstanceInput(
            @Parameter(description = "service id") @PathParam("componentId") final String componentId,
            @Parameter(description = "valid values: resources / services",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @Parameter(
                    description = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId,
            @Parameter(description = "id of user initiating the operation") @HeaderParam(
                    value = Constants.USER_ID_HEADER) String userId,
            @Context final HttpServletRequest request,
            @Parameter(description = "Component Instance Properties JSON Array",
                    required = true) final String componentInstanceInputsJsonArray) {

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
        Either<List<ComponentInstanceInput>, ResponseFormat> actionResponse =
                componentInstanceBusinessLogic.createOrUpdateInstanceInputValues(componentTypeEnum, componentId, componentInstanceId, inputsToUpdate, userId);
        if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
        }
        List<ComponentInstanceInput> resourceInstanceInput = actionResponse.left().value();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        String result;
            loggerSupportability.log(LoggerSupportabilityActions.UPDATE_PROPERTIES, StatusCode.COMPLETE,"Ending update Resource Instance Input for component {} ",componentId + " by " +  userId );
        try {
            result = mapper.writeValueAsString(resourceInstanceInput);
        } catch (JsonProcessingException e) {
            log.error(UPDATE_RESOURCE_INSTANCE_WITH_EXCEPTION, e.getMessage(), e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_PROPERTIES, StatusCode.COMPLETE, "Ending update Resource Instance Input for component {} ", componentId + " by user " + userId);
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
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/attribute")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update resource instance attribute", method = "POST",
            summary = "Returns updated resource instance attribute", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Resource instance created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateResourceInstanceAttribute(
            @Parameter(description = "service id") @PathParam("componentId") final String componentId,
            @Parameter(description = "valid values: resources / services",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @Parameter(
                    description = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId,
            @Parameter(description = "id of user initiating the operation") @HeaderParam(
                    value = Constants.USER_ID_HEADER) String userId,
            @Context final HttpServletRequest request) throws IOException {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_RESOURCE, StatusCode.STARTED,"Starting to update Resource Instance Attribute for component {} ",componentId + " by " +  userId );
        try {

            Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
            Wrapper<String> dataWrapper = new Wrapper<>();
            Wrapper<ComponentInstanceProperty> attributeWrapper = new Wrapper<>();
            Wrapper<ComponentInstanceBusinessLogic> blWrapper = new Wrapper<>();

            validateInputStream(request, dataWrapper, errorWrapper);

            if (errorWrapper.isEmpty()) {
                validateClassParse(dataWrapper.getInnerElement(), attributeWrapper, () -> ComponentInstanceProperty.class, errorWrapper);
            }

            if (errorWrapper.isEmpty()) {
                validateComponentInstanceBusinessLogic(request, containerComponentType, blWrapper, errorWrapper);
            }

            if (errorWrapper.isEmpty()) {
                ComponentInstanceBusinessLogic componentInstanceLogic = blWrapper.getInnerElement();
                ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
                log.debug("Start handle request of ComponentInstanceAttribute. Received attribute is {}", attributeWrapper.getInnerElement());
                Either<ComponentInstanceProperty, ResponseFormat> eitherAttribute = componentInstanceLogic.createOrUpdateAttributeValue(componentTypeEnum, componentId, componentInstanceId, attributeWrapper.getInnerElement(), userId);
                if (eitherAttribute.isRight()) {
                    errorWrapper.setInnerElement(eitherAttribute.right().value());
                } else {
                    attributeWrapper.setInnerElement(eitherAttribute.left().value());
                }
            }
            loggerSupportability.log(LoggerSupportabilityActions.UPDATE_RESOURCE, StatusCode.COMPLETE,"Ended update Resource Instance Attribute for component {} ",componentId + " by " +  userId );
            return buildResponseFromElement(errorWrapper, attributeWrapper);

        } catch (Exception e) {
            log.error(CREATE_AND_ASSOCIATE_RI_FAILED_WITH_EXCEPTION, e.getMessage(), e);
            throw e;
        }

    }

    @DELETE
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/property/{propertyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update resource instance", method = "DELETE",
            summary = "Returns deleted resource instance property", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Resource instance created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteResourceInstanceProperty(
            @Parameter(description = "service id") @PathParam("componentId") final String componentId,
            @Parameter(description = "valid values: resources / services",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @Parameter(
                    description = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId,
            @Parameter(description = "property id") @PathParam("propertyId") final String propertyId,
            @Parameter(description = "id of user initiating the operation") @HeaderParam(
                    value = Constants.USER_ID_HEADER) String userId,
            @Context final HttpServletRequest request) {

        
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_PROPERTIES, StatusCode.STARTED,"Starting to delete Resource Instance Property for component {} ",componentId + " by " +  userId );
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            loggerSupportability.log(LoggerSupportabilityActions.UPDATE_PROPERTIES, StatusCode.COMPLETE,"Ended delete Resource Instance Property for component {} ",componentId + " by " +  userId );
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }

            Either<ComponentInstanceProperty, ResponseFormat> actionResponse = componentInstanceBusinessLogic.deletePropertyValue(componentTypeEnum, componentId, componentInstanceId, propertyId, userId);
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
    @Operation(description = "Update resource instance", method = "POST", summary = "Returns updated resource instance",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Resource instance created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response changeResourceInstanceVersion(@PathParam("componentId") final String componentId,
            @PathParam("componentInstanceId") final String componentInstanceId,
            @Parameter(description = "valid values: resources / services",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @Context final HttpServletRequest request) throws IOException {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try (
            InputStream inputStream = request.getInputStream()) {

            byte[] bytes = IOUtils.toByteArray(inputStream);

            if (bytes == null || bytes.length == 0) {
                log.info(EMPTY_BODY_WAS_SENT);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }

            String userId = request.getHeader(Constants.USER_ID_HEADER);

            String data = new String(bytes);

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
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
            ComponentInstance actionResponse = componentInstanceBusinessLogic.changeComponentInstanceVersion(containerComponentType, componentId, componentInstanceId, userId, newResourceInstance);

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
    @Operation(description = "Update resource instance property", method = "POST",
            summary = "Returns updated resource instance property", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Resource instance created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateGroupInstanceProperty(
            @Parameter(description = "service id") @PathParam("componentId") final String componentId,
            @Parameter(description = "valid values: resources / services",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @Parameter(
                    description = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId,
            @Parameter(description = "group instance id") @PathParam("groupInstanceId") final String groupInstanceId,
            @Parameter(description = "id of user initiating the operation") @HeaderParam(
                    value = Constants.USER_ID_HEADER) String userId,
            @Context final HttpServletRequest request) throws IOException {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        try {
            loggerSupportability.log(LoggerSupportabilityActions.UPDATE_PROPERTIES, StatusCode.STARTED,"Starting update Group Instance Property for component {} ",componentId + " by " +  userId );
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

            Either<ComponentInstanceProperty, ResponseFormat> actionResponse = componentInstanceBusinessLogic.createOrUpdateGroupInstancePropertyValue(componentTypeEnum, componentId, componentInstanceId, groupInstanceId, property, userId);
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }

            ComponentInstanceProperty resourceInstanceProperty = actionResponse.left().value();
            ObjectMapper mapper = new ObjectMapper();
            String result = mapper.writeValueAsString(resourceInstanceProperty);
            loggerSupportability.log(LoggerSupportabilityActions.UPDATE_PROPERTIES, StatusCode.COMPLETE,"Ended update Group Instance Property for component {} ",componentId + " by " +  userId );
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
    @Operation(description = "Get group artifacts ", method = "GET",
            summary = "Returns artifacts metadata according to groupInstId", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "group found"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
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
            Either<GroupDefinitionInfo, ResponseFormat> actionResponse = groupBL.getGroupInstWithArtifactsById(componentTypeEnum, componentId, componentInstanceId, groupInstId, userId, false);

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
    @Operation(description = "Get component instance properties", method = "GET",
            summary = "Returns component instance properties", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Properties found"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "404", description = "Component/Component Instance - not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getInstancePropertiesById(@PathParam("containerComponentType") final String containerComponentType,
            @PathParam("containerComponentId") final String containerComponentId,
            @PathParam("componentInstanceUniqueId") final String componentInstanceUniqueId,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(GET_START_HANDLE_REQUEST_OF, url);

        List<ComponentInstanceProperty> componentInstancePropertiesById = componentInstanceBusinessLogic.getComponentInstancePropertiesById(containerComponentType, containerComponentId, componentInstanceUniqueId, userId);
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), componentInstancePropertiesById);
    }

    // US330353
    @GET
    @Path("/{containerComponentType}/{containerComponentId}/componentInstances/{componentInstanceUniqueId}/capability/{capabilityType}/capabilityName/{capabilityName}/ownerId/{ownerId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get component instance capability properties", method = "GET",
            summary = "Returns component instance capability properties", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Properties found"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "404", description = "Component/Component Instance/Capability - not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getInstanceCapabilityPropertiesById(
            @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("containerComponentId") final String containerComponentId,
            @PathParam("componentInstanceUniqueId") final String componentInstanceUniqueId,
            @PathParam("capabilityType") final String capabilityType,
            @PathParam("capabilityName") final String capabilityName, @PathParam("ownerId") final String ownerId,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(GET_START_HANDLE_REQUEST_OF, url);

        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);

            List<ComponentInstanceProperty> componentInstancePropertiesById = componentInstanceBusinessLogic.getComponentInstanceCapabilityPropertiesById(containerComponentType, containerComponentId, componentInstanceUniqueId,
                    capabilityType, capabilityName, ownerId, userId);

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
    @Operation(description = "Update Instance Capabilty  Property", method = "PUT",
            summary = "Returns updated property", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(
            value = {@ApiResponse(responseCode = "200", description = "Resource instance capabilty property updated"),
                    @ApiResponse(responseCode = "403", description = "Restricted operation"),
                    @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
                    @ApiResponse(responseCode = "404", description = "Component/Component Instance/Capability - not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateInstanceCapabilityProperty(
            @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("containerComponentId") final String containerComponentId,
            @PathParam("componentInstanceUniqueId") final String componentInstanceUniqueId,
            @PathParam("capabilityType") final String capabilityType,
            @PathParam("capabilityName") final String capabilityName, @PathParam("ownerId") final String ownerId,
            @Parameter(description = "Instance capabilty property to update", required = true) String data,
            @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(PUT) Start handle request of {}", url);
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_INSTANCE_CAPABILITY_PROPERTY, StatusCode.STARTED," Starting to update Instance Capability Property for component instance {} " , componentInstanceUniqueId + " by " + userId);
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

            Either<List<ComponentInstanceProperty>, ResponseFormat> updateCICapProperty = componentInstanceBusinessLogic.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId, componentInstanceUniqueId, capabilityType, capabilityName, propertiesToUpdate, userId);

            if (updateCICapProperty.isRight()) {
                log.debug(FAILED_TO_GET_PROPERTIES_OF_COMPONENT_INSTANCE_ID_IN_WITH_ID, componentInstanceUniqueId, containerComponentType, containerComponentId);
                return buildErrorResponse(updateCICapProperty.right().value());
            }
            loggerSupportability.log(LoggerSupportabilityActions.UPDATE_INSTANCE_CAPABILITY_PROPERTY, StatusCode.COMPLETE," Ended to update Instance Capability Property for component instance {} " , componentInstanceUniqueId + " by " + userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), updateCICapProperty.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(GET_GROUP_ARTIFACT_BY_ID);
            log.debug(GET_GROUP_ARTIFACT_BY_ID_UNEXPECTED_EXCEPTION, e);
            throw e;
        }
    }

    @POST
    @Path("/{containerComponentType}/{containerComponentId}/serviceProxy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create service proxy", method = "POST", summary = "Returns created service proxy",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Service proxy created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
            @ApiResponse(responseCode = "409", description = "Service proxy already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createServiceProxy(@Parameter(description = "RI object to be created", required = true) String data,
            @PathParam("containerComponentId") final String containerComponentId,
            @Parameter(description = "valid values: resources / services",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @HeaderParam(value = Constants.USER_ID_HEADER) @Parameter(description = "USER_ID of modifier user",
                    required = true) String userId,
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
    @Operation(description = "Delete service proxy", method = "DELETE", summary = "Returns delete service proxy",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Service proxy deleted"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteServiceProxy(@PathParam("containerComponentId") final String containerComponentId,
            @PathParam("serviceProxyId") final String serviceProxyId,
            @Parameter(description = "valid values: resources / services / products",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @Context final HttpServletRequest request) {
        
        String url = request.getMethod() + " " + request.getRequestURI();
        try {
            log.debug(START_HANDLE_REQUEST_OF, url);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            String userId = request.getHeader(Constants.USER_ID_HEADER);
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
    @Operation(description = "Update service proxy with new version", method = "POST",
            summary = "Returns updated service proxy", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Service proxy created"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response changeServiceProxyVersion(@PathParam("containerComponentId") final String containerComponentId,
            @PathParam("serviceProxyId") final String serviceProxyId,
            @Parameter(description = "valid values: resources / services",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
            @Context final HttpServletRequest request) {
        

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {

            String userId = request.getHeader(Constants.USER_ID_HEADER);

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
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
     * REST API GET relation by Id
     * Allows to get relation contained in specified component according to received Id
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
    @Operation(description = "Get relation", method = "GET",
            summary = "Returns relation metadata according to relationId",responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "relation found"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
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

            Either<RequirementCapabilityRelDef, ResponseFormat> actionResponse = componentInstanceBusinessLogic.getRelationById(componentId, relationId, userId, componentTypeEnum);
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

        Either<ComponentInstance, ResponseFormat> convertStatus = getComponentsUtils().convertJsonToObjectUsingObjectMapper(data, new User(), ComponentInstance.class, null, ComponentTypeEnum.RESOURCE_INSTANCE);
        if (convertStatus.isRight()) {
            return Either.right(convertStatus.right().value());
        }
        ComponentInstance resourceInstanceInfo = convertStatus.left().value();

        return Either.left(resourceInstanceInfo);
    }

    private Either<List<ComponentInstance>, ResponseFormat> convertToMultipleResourceInstance(String dataList) {

        Either<ComponentInstance[], ResponseFormat> convertStatus = getComponentsUtils().convertJsonToObjectUsingObjectMapper(dataList, new User(), ComponentInstance[].class, null, ComponentTypeEnum.RESOURCE_INSTANCE);
        if (convertStatus.isRight()) {
            return Either.right(convertStatus.right().value());
        }

        return Either.left(Arrays.asList(convertStatus.left().value()));
    }

    private Either<List<ComponentInstanceProperty>, ResponseFormat> convertMultipleProperties(String dataList) {
        if (StringUtils.isEmpty(dataList)) {
            return Either.right(getComponentsUtils().getResponseFormat(ActionStatus.MISSING_BODY));
        }
        Either<ComponentInstanceProperty[], ResponseFormat> convertStatus = getComponentsUtils().convertJsonToObjectUsingObjectMapper(dataList, new User(), ComponentInstanceProperty[].class, null, ComponentTypeEnum.RESOURCE_INSTANCE);
        if (convertStatus.isRight()) {
            return Either.right(convertStatus.right().value());
        }
        return Either.left(Arrays.asList(convertStatus.left().value()));
    }

    private Either<List<ComponentInstanceInput>, ResponseFormat> convertMultipleInputs(String dataList) {
        if (StringUtils.isEmpty(dataList)) {
            return Either.right(getComponentsUtils().getResponseFormat(ActionStatus.MISSING_BODY));
        }
        Either<ComponentInstanceInput[], ResponseFormat> convertStatus = getComponentsUtils().convertJsonToObjectUsingObjectMapper(dataList, new User(), ComponentInstanceInput[].class, null, ComponentTypeEnum.RESOURCE_INSTANCE);
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
        RequirementCapabilityRelDef requirementCapabilityRelDef = convertStatus.left().value();
        return requirementCapabilityRelDef;
    }

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
    @Operation(description = "Check if forwarding path to delete on version change", method = "GET", summary = "Returns forwarding paths to delete",
            responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    public Response changeResourceInstanceVersion( @PathParam("componentId") String componentId,
        @QueryParam("componentInstanceId") final String oldComponentInstanceId,
        @QueryParam("newComponentInstanceId") final String newComponentInstanceId,
        @Parameter(description = "valid values: resources / services",
                schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                        ComponentTypeEnum.SERVICE_PARAM_NAME}))
        @PathParam("containerComponentType") final String containerComponentType,
        @Context final HttpServletRequest request) {
        if (oldComponentInstanceId == null){
            return  buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.MISSING_OLD_COMPONENT_INSTANCE));
        }
        if (newComponentInstanceId == null){
            return  buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.MISSING_NEW_COMPONENT_INSTANCE));
        }
        

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        if (componentInstanceBusinessLogic == null) {
            log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
        }
        ComponentInstance newComponentInstance;
        if(StringUtils.isNotEmpty(newComponentInstanceId)){
            newComponentInstance=new ComponentInstance();
            newComponentInstance.setToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_UID,newComponentInstanceId);
        }else{
            log.error("missing component id");
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.MISSING_DATA));
        }
        Either<Set<String>,ResponseFormat> actionResponse= componentInstanceBusinessLogic.forwardingPathOnVersionChange(
            containerComponentType,componentId,oldComponentInstanceId,newComponentInstance);
        if (actionResponse.isRight()) {
            return buildErrorResponse(actionResponse.right().value());
        }
        ForwardingPaths forwardingPaths=new ForwardingPaths();
        forwardingPaths.setForwardingPathToDelete(actionResponse.left().value());
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), forwardingPaths);

    }

    @POST
    @Path("/services/{componentId}/copyComponentInstance/{componentInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces((MediaType.APPLICATION_JSON))
    @Operation(description = "Copy Component Instance", method = "POST", summary = "Returns updated service information",responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Service.class)))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Copy and Paste Success"),
            @ApiResponse(responseCode = "403", description = "Restricted Operation"),
            @ApiResponse(responseCode = "400", description = "Invalid Content / Missing content")})
    public Response copyComponentInstance(
            @Parameter(description = "service unique id in pasted canvas") @PathParam("componentId") final String containerComponentId,
            @Parameter(description = "Data for copying", required = true) String data, @PathParam("componentInstanceId") final String componentInstanceId,
            @Context final HttpServletRequest request) {
        log.info("Start to copy component instance");

        String userId = request.getHeader(Constants.USER_ID_HEADER);
        final String CNTAINER_CMPT_TYPE = "services";

        try {
            ComponentInstance inputComponentInstance = RepresentationUtils.fromRepresentation(data, ComponentInstance.class);
            inputComponentInstance.setInvariantName(null);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(CNTAINER_CMPT_TYPE);
            if (componentInstanceBusinessLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, componentTypeEnum);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, "services"));
            }
            Either<Map<String, ComponentInstance>, ResponseFormat> copyComponentInstance = componentInstanceBusinessLogic.copyComponentInstance(
                    inputComponentInstance, containerComponentId, componentInstanceId, userId);

            if (copyComponentInstance.isRight()) {
                log.error("Failed to copy ComponentInstance {}", copyComponentInstance.right().value());
                return buildErrorResponse(copyComponentInstance.right().value());
            }

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                    copyComponentInstance.left().value());
        } catch (Exception e) {
            log.error("Failed to convert json to Map { }", data, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.USER_DEFINED,
                    "Failed to get the copied component instance information"));
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/batchDeleteResourceInstances/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Batch Delete ResourceInstances", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "203", description = "ResourceInstances deleted"),
            @ApiResponse(responseCode = "403", description = "Restricted Operation"),
            @ApiResponse(responseCode = "400", description = "Invalid Content / Missing Content")
    })
    public Response batchDeleteResourceInstances(
            @Parameter(description = "valid values: resources / services / products", schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                    ComponentTypeEnum.SERVICE_PARAM_NAME,
                    ComponentTypeEnum.PRODUCT_PARAM_NAME}))
            @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId,
            @Context final HttpServletRequest request,
            @Parameter(description = "Component Instance Id List", required = true) final String componentInstanceIdLisStr) {
        try {
            if (componentInstanceIdLisStr == null || componentInstanceIdLisStr.isEmpty()) {
                log.error("Empty JSON List was sent",componentInstanceIdLisStr);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }


            if (componentInstanceBusinessLogic == null) {
                log.error("Unsupported component type {}", containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }

            Either<List<String>, ResponseFormat> convertResponse = convertToStringList(componentInstanceIdLisStr);

            if (convertResponse.isRight()) {
                BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - batchDeleteResourceInstances");
                log.error("Failed to convert received data to BE format.");
                return buildErrorResponse(convertResponse.right().value());
            }

            String userId = request.getHeader(Constants.USER_ID_HEADER);
            List<String> componentInstanceIdList = convertResponse.left().value();
            log.debug("batchDeleteResourceInstances componentInstanceIdList is {}", componentInstanceIdList);
            Map<String, List<String>> deleteErrorMap = componentInstanceBusinessLogic.batchDeleteComponentInstance(containerComponentType,
                    componentId, componentInstanceIdList, userId);

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), deleteErrorMap);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Batch Delete ResourceInstances");
            log.error("batch delete resource instances with exception" , e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/resourceInstance/batchDissociate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Batch Dissociate RI from RI", method = "PUT",
            summary = "Returns deleted RelationShip Info", responses = @ApiResponse(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Relationship deleted"),
            @ApiResponse(responseCode = "403", description = "Missing Information"),
            @ApiResponse(responseCode = "400", description = "Invalid Content / Missing Content")})
    public Response batchDissociateRIFromRI(
            @Parameter(description = "allowed values are resources/services/products",
                    schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
                            ComponentTypeEnum.SERVICE_PARAM_NAME,
                            ComponentTypeEnum.PRODUCT_PARAM_NAME}),
                    required = true) @PathParam("containerComponentType") final String containerComponentType,
            @Parameter(
                    description = "unique id of the container component") @PathParam("componentId") final String componentId,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @Parameter(description = "RelationshipInfo", required = true) String data,
            @Context final HttpServletRequest request) {
        

        try {
            if (data == null || data.length() == 0) {
                log.info("Empty JSON list was sent");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);

            if (componentInstanceBusinessLogic == null) {
                log.debug("Unsupported component type {}", containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }

            Either<List<RequirementCapabilityRelDef>, ResponseFormat> regInfoWs = convertToRequirementCapabilityRelDefList(data);

            if (regInfoWs.isRight()) {
                BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - batch dissociateRIFromRI");
                log.debug("Failed to convert received data to BE format");
                return buildErrorResponse(regInfoWs.right().value());
            }

            List<RequirementCapabilityRelDef> requirementDefList = regInfoWs.left().value();
            List<RequirementCapabilityRelDef> delOkResult = componentInstanceBusinessLogic.batchDissociateRIFromRI(
                    componentId, userId, requirementDefList, componentTypeEnum);

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), delOkResult);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Batch Dissociate Resource Instance");
            log.debug("batch dissociate resource instance from service failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Either<List<String>, ResponseFormat> convertToStringList(String datalist) {
        Either<String[], ResponseFormat> convertStatus = getComponentsUtils().convertJsonToObjectUsingObjectMapper(datalist, new User(), String[].class, null, null);

        if (convertStatus.isRight()) {
            return Either.right(convertStatus.right().value());
        }

        return Either.left(Arrays.asList(convertStatus.left().value()));
    }

    private Either<List<RequirementCapabilityRelDef>, ResponseFormat> convertToRequirementCapabilityRelDefList(String data) {
        Either<RequirementCapabilityRelDef[], ResponseFormat> convertStatus = getComponentsUtils().convertJsonToObjectUsingObjectMapper(data, new User(), RequirementCapabilityRelDef[].class, null, null);

        if (convertStatus.isRight()) {
            return Either.right(convertStatus.right().value());
        }

        return Either.left(Arrays.asList(convertStatus.left().value()));
    }

}
