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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.utils.DirectivesUtils;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.ForwardingPaths;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.info.CreateAndAssotiateInfo;
import org.openecomp.sdc.be.info.GroupDefinitionInfo;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintDeserialiser;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * Root resource (exposed at "/" path) .json
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Resource Instance Servlet")
@Singleton
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
	private static final Logger log = Logger.getLogger(ComponentInstanceServlet.class);
    private static final Type PROPERTY_CONSTRAINT_TYPE = new TypeToken<PropertyConstraint>() {}.getType();
    private static final Gson gsonDeserializer = new GsonBuilder().registerTypeAdapter(PROPERTY_CONSTRAINT_TYPE, new PropertyConstraintDeserialiser()).create();

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create ComponentInstance", httpMethod = "POST", notes = "Returns created ComponentInstance", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Component created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "Component instance already exist") })
    public Response createComponentInstance(@ApiParam(value = "RI object to be created", required = true) String data, @PathParam("componentId") final String containerComponentId,
            @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @HeaderParam(value = Constants.USER_ID_HEADER) @ApiParam(value = "USER_ID of modifier user", required = true) String userId, @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();

        try {

            ComponentInstance componentInstance = RepresentationUtils.fromRepresentation(data, ComponentInstance.class);
            componentInstance.setInvariantName(null);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceLogic.createComponentInstance(containerComponentType, containerComponentId, userId, componentInstance);

            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResponse.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Component Instance");
            log.debug("create component instance failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update resource instance", httpMethod = "POST", notes = "Returns updated resource instance", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Resource instance updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response updateComponentInstanceMetadata(@PathParam("componentId") final String componentId, @PathParam("componentInstanceId") final String componentInstanceId,
            @ApiParam(value = "valid values: resources / services / products", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME + ","
                    + ComponentTypeEnum.PRODUCT_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();

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
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
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
            Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceLogic.updateComponentInstanceMetadata(containerComponentType, componentId, componentInstanceId, userId, resourceInstance);

            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            ComponentInstance resultValue = actionResponse.left().value();
            if (componentTypeEnum.equals(ComponentTypeEnum.SERVICE)){
                boolean shouldCreateServiceFilter = resourceInstance.getDirectives() != null && resourceInstance.getDirectives().contains(
                        DirectivesUtils.SELECTABLE);
                ServiceBusinessLogic
                        serviceBusinessLogic = (ServiceBusinessLogic) getComponentBL(componentTypeEnum, context);

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
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/multipleComponentInstance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update resource instance multiple component", httpMethod = "POST", notes = "Returns updated resource instance", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Resource instance updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response updateMultipleComponentInstance(@PathParam("componentId") final String componentId,
            @ApiParam(value = "valid values: resources / services / products", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME + ","
                    + ComponentTypeEnum.PRODUCT_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @Context final HttpServletRequest request, @ApiParam(value = "Component Instance JSON Array", required = true) final String componentInstanceJsonArray) {

        ServletContext context = request.getSession().getServletContext();
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
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
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

            Either<List<ComponentInstance>, ResponseFormat> actionResponse = componentInstanceLogic.updateComponentInstance(containerComponentType, componentId, userId, componentInstanceList, true);

            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(UPDATE_RESOURCE_INSTANCE);
            log.debug(UPDATE_RESOURCE_INSTANCE_WITH_EXCEPTION, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    @DELETE
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{resourceInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete ResourceInstance", httpMethod = "DELETE", notes = "Returns delete resourceInstance", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "ResourceInstance deleted"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response deleteResourceInstance(@PathParam("componentId") final String componentId, @PathParam("resourceInstanceId") final String resourceInstanceId,
            @ApiParam(value = "valid values: resources / services / products", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME + ","
                    + ComponentTypeEnum.PRODUCT_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        Response response = null;
        try {
            log.debug(START_HANDLE_REQUEST_OF, url);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            String userId = request.getHeader(Constants.USER_ID_HEADER);
            Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceLogic.deleteComponentInstance(containerComponentType, componentId, resourceInstanceId, userId);

            if (actionResponse.isRight()) {
                response = buildErrorResponse(actionResponse.right().value());
            } else {
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), null);
            }
            return response;
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Resource Instance");
            log.debug("delete resource instance with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @ApiParam(value = "allowed values are resources /services / products", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME + "," + ComponentTypeEnum.PRODUCT_PARAM_NAME, required = true)
    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/associate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Associate RI to RI", httpMethod = "POST", notes = "Returns created RelationshipInfo", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Relationship created"), @ApiResponse(code = 403, message = "Missing information"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "Relationship already exist") })
    public Response associateRIToRI(@ApiParam(value = "unique id of the container component") @PathParam("componentId") final String componentId,
            @ApiParam(value = "allowed values are resources /services / products", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME + ","
                    + ComponentTypeEnum.PRODUCT_PARAM_NAME, required = true) @PathParam("containerComponentType") final String containerComponentType,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @ApiParam(value = "RelationshipInfo", required = true) String data, @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        Response response = null;

        try {

            log.debug(START_HANDLE_REQUEST_OF, url);

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }

            Either<RequirementCapabilityRelDef, ResponseFormat> regInfoW = convertToRequirementCapabilityRelDef(data);

            Either<RequirementCapabilityRelDef, ResponseFormat> resultOp;
            if (regInfoW.isRight()) {
                BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - associateRIToRI");
                log.debug(FAILED_TO_CONVERT_RECEIVED_DATA_TO_BE_FORMAT);
                resultOp = Either.right(regInfoW.right().value());
            } else {
                RequirementCapabilityRelDef requirementDef = regInfoW.left().value();
                requirementDef.setOriginUI(true);
                resultOp = componentInstanceLogic.associateRIToRI(componentId, userId, requirementDef, componentTypeEnum);
            }

            Either<RequirementCapabilityRelDef, ResponseFormat> actionResponse = resultOp;

            if (actionResponse.isRight()) {
                response = buildErrorResponse(actionResponse.right().value());
            } else {
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());
            }
            return response;

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Associate Resource Instance");
            log.debug("associate resource instance to another RI with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/resourceInstance/dissociate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Dissociate RI from RI", httpMethod = "PUT", notes = "Returns deleted RelationshipInfo", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Relationship deleted"), @ApiResponse(code = 403, message = "Missing information"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response dissociateRIFromRI(
            @ApiParam(value = "allowed values are resources /services / products", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME + ","
                    + ComponentTypeEnum.PRODUCT_PARAM_NAME, required = true) @PathParam("containerComponentType") final String containerComponentType,
            @ApiParam(value = "unique id of the container component") @PathParam("componentId") final String componentId, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @ApiParam(value = "RelationshipInfo", required = true) String data, @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        try {

            log.debug(START_HANDLE_REQUEST_OF, url);

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }

            Either<RequirementCapabilityRelDef, ResponseFormat> regInfoW = convertToRequirementCapabilityRelDef(data);
            if (regInfoW.isRight()) {
                BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - dissociateRIFromRI");
                log.debug(FAILED_TO_CONVERT_RECEIVED_DATA_TO_BE_FORMAT);
                return buildErrorResponse(regInfoW.right().value());
            }

            RequirementCapabilityRelDef requirementDef = regInfoW.left().value();
            Either<RequirementCapabilityRelDef, ResponseFormat> actionResponse = componentInstanceLogic.dissociateRIFromRI(componentId, userId, requirementDef, componentTypeEnum);

            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Dissociate Resource Instance");
            log.debug("dissociate resource instance from service failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/createAndAssociate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create RI and associate RI to RI", httpMethod = "POST", notes = "Returns created RI and RelationshipInfo", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "RI created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "Relationship already exist") })
    public Response createAndAssociateRIToRI(@PathParam("componentId") final String componentId,
            @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();

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
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
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
            Either<CreateAndAssotiateInfo, ResponseFormat> actionResponse = componentInstanceLogic.createAndAssociateRIToRI(containerComponentType, componentId, userId, createAndAssotiateInfo);

            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create and Associate Resource Instance");
            log.debug("create and associate RI failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update resource instance property", httpMethod = "POST", notes = "Returns updated resource instance property", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Resource instance created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response updateResourceInstanceProperties(@ApiParam(value = "service id") @PathParam("componentId") final String componentId,
            @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @ApiParam(value = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId, @ApiParam(value = "id of user initiating the operation") @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @Context final HttpServletRequest request, @ApiParam(value = "Component Instance Properties JSON Array", required = true) final String componentInstancePropertiesJsonArray) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        try {
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

            ServletContext context = request.getSession().getServletContext();

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }

            Either<List<ComponentInstanceProperty>, ResponseFormat> actionResponse = componentInstanceLogic.createOrUpdatePropertiesValues(componentTypeEnum, componentId, componentInstanceId, propertiesToUpdate, userId);

            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }

            List<ComponentInstanceProperty> resourceInstanceProperties = actionResponse.left().value();
            ObjectMapper mapper = new ObjectMapper();
            String result = mapper.writeValueAsString(resourceInstanceProperties);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

        } catch (Exception e) {
            log.error("create and associate RI failed with exception: {}", e.getMessage(), e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/inputs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update resource instance property", httpMethod = "POST", notes = "Returns updated resource instance property", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Resource instance created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response updateResourceInstanceInput(@ApiParam(value = "service id") @PathParam("componentId") final String componentId,
            @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @ApiParam(value = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId, @ApiParam(value = "id of user initiating the operation") @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @Context final HttpServletRequest request, @ApiParam(value = "Component Instance Properties JSON Array", required = true) final String componentInstanceInputsJsonArray) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        try {
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

            ServletContext context = request.getSession().getServletContext();

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }

            Either<List<ComponentInstanceInput>, ResponseFormat> actionResponse =
                    componentInstanceLogic.createOrUpdateInstanceInputValues(componentTypeEnum, componentId, componentInstanceId, inputsToUpdate, userId);

            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }

            List<ComponentInstanceInput> resourceInstanceInput = actionResponse.left().value();
            ObjectMapper mapper = new ObjectMapper();
            String result = mapper.writeValueAsString(resourceInstanceInput);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

        } catch (Exception e) {
            log.error("create and associate RI failed with exception: {}", e.getMessage(), e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

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
    @ApiOperation(value = "Update resource instance attribute", httpMethod = "POST", notes = "Returns updated resource instance attribute", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Resource instance created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response updateResourceInstanceAttribute(@ApiParam(value = "service id") @PathParam("componentId") final String componentId,
            @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @ApiParam(value = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId, @ApiParam(value = "id of user initiating the operation") @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

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

            return buildResponseFromElement(errorWrapper, attributeWrapper);

        } catch (Exception e) {
            log.error("create and associate RI failed with exception: {}", e.getMessage(), e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    @DELETE
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/property/{propertyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update resource instance", httpMethod = "DELETE", notes = "Returns deleted resource instance property", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Resource instance created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response deleteResourceInstanceProperty(@ApiParam(value = "service id") @PathParam("componentId") final String componentId,
            @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @ApiParam(value = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId, @ApiParam(value = "property id") @PathParam("propertyId") final String propertyId,
            @ApiParam(value = "id of user initiating the operation") @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Context final HttpServletRequest request) {

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }

            Either<ComponentInstanceProperty, ResponseFormat> actionResponse = componentInstanceLogic.deletePropertyValue(componentTypeEnum, componentId, componentInstanceId, propertyId, userId);
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), null);
        } catch (Exception e) {
            log.error("create and associate RI failed with exception: {}", e.getMessage(), e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/changeVersion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update resource instance", httpMethod = "POST", notes = "Returns updated resource instance", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Resource instance created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response changeResourceInstanceVersion(@PathParam("componentId") final String componentId, @PathParam("componentInstanceId") final String componentInstanceId,
            @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try (    InputStream inputStream = request.getInputStream()) {

            byte[] bytes = IOUtils.toByteArray(inputStream);

            if (bytes == null || bytes.length == 0) {
                log.info(EMPTY_BODY_WAS_SENT);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }

            String userId = request.getHeader(Constants.USER_ID_HEADER);

            String data = new String(bytes);

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
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
            Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceLogic.changeComponentInstanceVersion(containerComponentType, componentId, componentInstanceId, userId, newResourceInstance);

            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(UPDATE_RESOURCE_INSTANCE);
            log.debug(UPDATE_RESOURCE_INSTANCE_WITH_EXCEPTION, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    @POST
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/groupInstance/{groupInstanceId}/property")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update resource instance property", httpMethod = "POST", notes = "Returns updated resource instance property", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Resource instance created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response updateGroupInstanceProperty(@ApiParam(value = "service id") @PathParam("componentId") final String componentId,
            @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @ApiParam(value = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId, @ApiParam(value = "group instance id") @PathParam("groupInstanceId") final String groupInstanceId,
            @ApiParam(value = "id of user initiating the operation") @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);

        try {
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

            ServletContext context = request.getSession().getServletContext();

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }

            Either<ComponentInstanceProperty, ResponseFormat> actionResponse = componentInstanceLogic.createOrUpdateGroupInstancePropertyValue(componentTypeEnum, componentId, componentInstanceId, groupInstanceId, property, userId);

            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }

            ComponentInstanceProperty resourceInstanceProperty = actionResponse.left().value();
            ObjectMapper mapper = new ObjectMapper();
            String result = mapper.writeValueAsString(resourceInstanceProperty);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

        } catch (Exception e) {
            log.error("create and associate RI failed with exception: {}", e.getMessage(), e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    @GET
    @Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/groupInstance/{groupInstId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get group artifacts ", httpMethod = "GET", notes = "Returns artifacts metadata according to groupInstId", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "group found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Group not found") })
    public Response getGroupArtifactById(@PathParam("containerComponentType") final String containerComponentType, @PathParam("componentId") final String componentId, @PathParam("componentInstanceId") final String componentInstanceId,
            @PathParam("groupInstId") final String groupInstId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(GET_START_HANDLE_REQUEST_OF, url);

        try {

            GroupBusinessLogic businessLogic = this.getGroupBL(context);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            Either<GroupDefinitionInfo, ResponseFormat> actionResponse = businessLogic.getGroupInstWithArtifactsById(componentTypeEnum, componentId, componentInstanceId, groupInstId, userId, false);

            if (actionResponse.isRight()) {
                log.debug("failed to get all non abstract {}", containerComponentType);
                return buildErrorResponse(actionResponse.right().value());
            }

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(GET_GROUP_ARTIFACT_BY_ID);
            log.debug(GET_GROUP_ARTIFACT_BY_ID_UNEXPECTED_EXCEPTION, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    // US831698
    @GET
    @Path("/{containerComponentType}/{containerComponentId}/componentInstances/{componentInstanceUniqueId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get component instance properties", httpMethod = "GET", notes = "Returns component instance properties", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Properties found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component/Component Instance - not found") })
    public Response getInstancePropertiesById(@PathParam("containerComponentType") final String containerComponentType, @PathParam("containerComponentId") final String containerComponentId,
            @PathParam("componentInstanceUniqueId") final String componentInstanceUniqueId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(GET_START_HANDLE_REQUEST_OF, url);

        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            ComponentInstanceBusinessLogic componentInstanceBL = getComponentInstanceBL(context);

            Either<List<ComponentInstanceProperty>, ResponseFormat> componentInstancePropertiesById = componentInstanceBL.getComponentInstancePropertiesById(containerComponentType, containerComponentId, componentInstanceUniqueId, userId);

            if (componentInstancePropertiesById.isRight()) {
                log.debug(FAILED_TO_GET_PROPERTIES_OF_COMPONENT_INSTANCE_ID_IN_WITH_ID, componentInstanceUniqueId, containerComponentType, containerComponentId);
                return buildErrorResponse(componentInstancePropertiesById.right().value());
            }

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), componentInstancePropertiesById.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(GET_GROUP_ARTIFACT_BY_ID);
            log.debug(GET_GROUP_ARTIFACT_BY_ID_UNEXPECTED_EXCEPTION, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    // US330353
    @GET
    @Path("/{containerComponentType}/{containerComponentId}/componentInstances/{componentInstanceUniqueId}/capability/{capabilityType}/capabilityName/{capabilityName}/ownerId/{ownerId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get component instance capability properties", httpMethod = "GET", notes = "Returns component instance capability properties", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Properties found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component/Component Instance/Capability - not found") })
    public Response getInstanceCapabilityPropertiesById(@PathParam("containerComponentType") final String containerComponentType, @PathParam("containerComponentId") final String containerComponentId,
            @PathParam("componentInstanceUniqueId") final String componentInstanceUniqueId, @PathParam("capabilityType") final String capabilityType, @PathParam("capabilityName") final String capabilityName,  @PathParam("ownerId") final String ownerId, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(GET_START_HANDLE_REQUEST_OF, url);

        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            ComponentInstanceBusinessLogic componentInstanceBL = getComponentInstanceBL(context);

            Either<List<ComponentInstanceProperty>, ResponseFormat> componentInstancePropertiesById = componentInstanceBL.getComponentInstanceCapabilityPropertiesById(containerComponentType, containerComponentId, componentInstanceUniqueId,
                    capabilityType, capabilityName, ownerId, userId);

            if (componentInstancePropertiesById.isRight()) {
                log.debug(FAILED_TO_GET_PROPERTIES_OF_COMPONENT_INSTANCE_ID_IN_WITH_ID, componentInstanceUniqueId, containerComponentType, containerComponentId);
                return buildErrorResponse(componentInstancePropertiesById.right().value());
            }

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), componentInstancePropertiesById.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(GET_GROUP_ARTIFACT_BY_ID);
            log.debug(GET_GROUP_ARTIFACT_BY_ID_UNEXPECTED_EXCEPTION, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    //US 331281
    @PUT
    @Path("/{containerComponentType}/{containerComponentId}/componentInstances/{componentInstanceUniqueId}/capability/{capabilityType}/capabilityName/{capabilityName}/ownerId/{ownerId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update Instance Capabilty  Property", httpMethod = "PUT", notes = "Returns updated property", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Resource instance capabilty property updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 404, message = "Component/Component Instance/Capability - not found") })
    public Response updateInstanceCapabilityProperty(@PathParam("containerComponentType") final String containerComponentType, @PathParam("containerComponentId") final String containerComponentId,
            @PathParam("componentInstanceUniqueId") final String componentInstanceUniqueId, @PathParam("capabilityType") final String capabilityType, @PathParam("capabilityName") final String capabilityName, @PathParam("ownerId") final String ownerId,
            @ApiParam(value = "Instance capabilty property to update", required = true) String data, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(PUT) Start handle request of {}", url);
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
            ComponentInstanceBusinessLogic componentInstanceBL = getComponentInstanceBL(context);

            Either<List<ComponentInstanceProperty>, ResponseFormat> updateCICapProperty = componentInstanceBL.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId, componentInstanceUniqueId, capabilityType, capabilityName, propertiesToUpdate, userId);

            if (updateCICapProperty.isRight()) {
                log.debug(FAILED_TO_GET_PROPERTIES_OF_COMPONENT_INSTANCE_ID_IN_WITH_ID, componentInstanceUniqueId, containerComponentType, containerComponentId);
                return buildErrorResponse(updateCICapProperty.right().value());
            }

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), updateCICapProperty.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(GET_GROUP_ARTIFACT_BY_ID);
            log.debug(GET_GROUP_ARTIFACT_BY_ID_UNEXPECTED_EXCEPTION, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/{containerComponentType}/{containerComponentId}/serviceProxy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create service proxy", httpMethod = "POST", notes = "Returns created service proxy", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Service proxy created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "Service proxy already exist") })
    public Response createServiceProxy(@ApiParam(value = "RI object to be created", required = true) String data, @PathParam("containerComponentId") final String containerComponentId,
            @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @HeaderParam(value = Constants.USER_ID_HEADER) @ApiParam(value = "USER_ID of modifier user", required = true) String userId, @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();

        try {

            ComponentInstance componentInstance = RepresentationUtils.fromRepresentation(data, ComponentInstance.class);
            componentInstance.setInvariantName(null);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentTypeEnum != ComponentTypeEnum.SERVICE) {
                log.debug("Unsupported container component type {}", containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceLogic.createServiceProxy();

            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResponse.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create service proxy");
            log.debug("Create service proxy failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @DELETE
    @Path("/{containerComponentType}/{containerComponentId}/serviceProxy/{serviceProxyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete service proxy", httpMethod = "DELETE", notes = "Returns delete service proxy", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Service proxy deleted"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response deleteServiceProxy(@PathParam("containerComponentId") final String containerComponentId, @PathParam("serviceProxyId") final String serviceProxyId,
            @ApiParam(value = "valid values: resources / services / products", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME + ","
                    + ComponentTypeEnum.PRODUCT_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        Response response = null;
        try {
            log.debug(START_HANDLE_REQUEST_OF, url);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            String userId = request.getHeader(Constants.USER_ID_HEADER);
            Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceLogic.deleteServiceProxy();

            if (actionResponse.isRight()) {
                response = buildErrorResponse(actionResponse.right().value());
            } else {
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), null);
            }
            return response;
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete service proxy");
            log.debug("Delete service proxy failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/{containerComponentType}/{containerComponentId}/serviceProxy/{serviceProxyId}/changeVersion/{newServiceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update service proxy with new version", httpMethod = "POST", notes = "Returns updated service proxy", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Service proxy created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response changeServiceProxyVersion(@PathParam("containerComponentId") final String containerComponentId, @PathParam("serviceProxyId") final String serviceProxyId,
            @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {

            String userId = request.getHeader(Constants.USER_ID_HEADER);

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceLogic.changeServiceProxyVersion();

            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update service proxy with new version");
            log.debug("Update service proxy with new version failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
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
    @ApiOperation(value = "Get relation", httpMethod = "GET", notes = "Returns relation metadata according to relationId", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "relation found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Relation not found") })
    public Response getRelationById(@PathParam("containerComponentType") final String containerComponentType, @PathParam("componentId") final String componentId,
            @PathParam("relationId") final String relationId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(GET_START_HANDLE_REQUEST_OF, url);
        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentTypeEnum == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);

            Either<RequirementCapabilityRelDef, ResponseFormat> actionResponse = componentInstanceLogic.getRelationById(componentId, relationId, userId, componentTypeEnum);
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("getRelationById");
            log.debug("getRelationById unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
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
            return Either.right(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        Either<ComponentInstanceProperty[], ResponseFormat> convertStatus = getComponentsUtils().convertJsonToObjectUsingObjectMapper(dataList, new User(), ComponentInstanceProperty[].class, null, ComponentTypeEnum.RESOURCE_INSTANCE);
        if (convertStatus.isRight()) {
            return Either.right(convertStatus.right().value());
        }
        return Either.left(Arrays.asList(convertStatus.left().value()));
    }

    private Either<List<ComponentInstanceInput>, ResponseFormat> convertMultipleInputs(String dataList) {
        if (StringUtils.isEmpty(dataList)) {
            return Either.right(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        Either<ComponentInstanceInput[], ResponseFormat> convertStatus = getComponentsUtils().convertJsonToObjectUsingObjectMapper(dataList, new User(), ComponentInstanceInput[].class, null, ComponentTypeEnum.RESOURCE_INSTANCE);
        if (convertStatus.isRight()) {
            return Either.right(convertStatus.right().value());
        }
        return Either.left(Arrays.asList(convertStatus.left().value()));
    }


    private Either<RequirementCapabilityRelDef, ResponseFormat> convertToRequirementCapabilityRelDef(String data) {

        Either<RequirementCapabilityRelDef, ActionStatus> convertStatus = convertJsonToObject(data, RequirementCapabilityRelDef.class);
        if (convertStatus.isRight()) {
            return Either.right(getComponentsUtils().getResponseFormat(convertStatus.right().value()));
        }
        RequirementCapabilityRelDef requirementCapabilityRelDef = convertStatus.left().value();
        return Either.left(requirementCapabilityRelDef);
    }

    public  <T> Either<T, ActionStatus> convertJsonToObject(String data, Class<T> clazz) {
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
    @ApiOperation(value = "Check if forwarding path to delete on version change", httpMethod = "GET", notes = "Returns forwarding paths to delete",
        response = Response.class)
    public Response changeResourceInstanceVersion( @PathParam("componentId") String componentId,
        @QueryParam("componentInstanceId") final String oldComponentInstanceId,
        @QueryParam("newComponentInstanceId") final String newComponentInstanceId,
        @ApiParam(value = "valid values: resources / services",
            allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME)
        @PathParam("containerComponentType") final String containerComponentType,
        @Context final HttpServletRequest request) {
        if (oldComponentInstanceId == null){
            return  buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.MISSING_OLD_COMPONENT_INSTANCE));
        }
        if (newComponentInstanceId == null){
            return  buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.MISSING_NEW_COMPONENT_INSTANCE));
        }
        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
        if (componentInstanceLogic == null) {
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
        Either<Set<String>,ResponseFormat> actionResponse= componentInstanceLogic.forwardingPathOnVersionChange(
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
    @ApiOperation(value = "Copy Component Instance", httpMethod = "POST", notes = "Returns updated service information", response = Service.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Copy and Paste Success"),
            @ApiResponse(code = 403, message = "Restricted Operation"),
            @ApiResponse(code = 400, message = "Invalid Content / Missing content")})
    public Response copyComponentInstance(
            @ApiParam(value = "service unique id in pasted canvas") @PathParam("componentId") final String containerComponentId,
            @ApiParam(value = "Data for copying", required = true) String data, @PathParam("componentInstanceId") final String componentInstanceId,
            @Context final HttpServletRequest request) {
        log.info("Start to copy component instance");
        ServletContext context = request.getSession().getServletContext();
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        final String CNTAINER_CMPT_TYPE = "services";

        try {
            ComponentInstance inputComponentInstance = RepresentationUtils.fromRepresentation(data, ComponentInstance.class);
            inputComponentInstance.setInvariantName(null);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(CNTAINER_CMPT_TYPE);
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
                log.debug(UNSUPPORTED_COMPONENT_TYPE, componentTypeEnum);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, "services"));
            }
            Either<Map<String, ComponentInstance>, ResponseFormat> copyComponentInstance = componentInstanceLogic.copyComponentInstance(
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
    @ApiOperation(value = "Batch Delete ResourceInstances", httpMethod = "POST")
    @ApiResponses(value = {
            @ApiResponse(code = 203, message = "ResourceInstances deleted"),
            @ApiResponse(code = 403, message = "Restricted Operation"),
            @ApiResponse(code = 400, message = "Invalid Content / Missing Content")
    })
    public Response batchDeleteResourceInstances(
            @ApiParam(value = "valid values: resources / services / products", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME + "," +
                    ComponentTypeEnum.PRODUCT_PARAM_NAME)
            @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId,
            @Context final HttpServletRequest request,
            @ApiParam(value = "Component Instance Id List", required = true) final String componentInstanceIdLisStr) {
        ServletContext context = request.getSession().getServletContext();
        try {
            if (componentInstanceIdLisStr == null || componentInstanceIdLisStr.isEmpty()) {
                log.error("Empty JSON List was sent",componentInstanceIdLisStr);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }


            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);
            if (componentInstanceLogic == null) {
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
            Map<String, List<String>> deleteErrorMap = componentInstanceLogic.batchDeleteComponentInstance(containerComponentType,
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
    @ApiOperation(value = "Batch Dissociate RI from RI", httpMethod = "PUT", notes = "Returns deleted RelationShip Info", response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Relationship deleted"),
            @ApiResponse(code = 403, message = "Missing Information"),
            @ApiResponse(code = 400, message = "Invalid Content / Missing Content")
    })
    public Response batchDissociateRIFromRI(
            @ApiParam(value = "allowed values are resources/services/products", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME + "," + ComponentTypeEnum.PRODUCT_PARAM_NAME, required = true)
            @PathParam("containerComponentType") final String containerComponentType,
            @ApiParam(value = "unique id of the container component")
            @PathParam("componentId") final String componentId,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @ApiParam(value = "RelationshipInfo", required = true) String data,
            @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();

        try {
            if (data == null || data.length() == 0) {
                log.info("Empty JSON list was sent");
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context);

            if (componentInstanceLogic == null) {
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
            List<RequirementCapabilityRelDef> delOkResult = componentInstanceLogic.batchDissociateRIFromRI(
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
