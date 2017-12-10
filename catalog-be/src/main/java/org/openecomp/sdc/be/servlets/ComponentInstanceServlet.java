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

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.info.CreateAndAssotiateInfo;
import org.openecomp.sdc.be.info.GroupDefinitionInfo;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintDeserialiser;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jcabi.aspects.Loggable;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Root resource (exposed at "/" path) .json
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Resource Instance Servlet")
@Singleton
public class ComponentInstanceServlet extends AbstractValidationsServlet {

	private static Logger log = LoggerFactory.getLogger(ComponentInstanceServlet.class.getName());

	Type constraintType = new TypeToken<PropertyConstraint>() {
	}.getType();

	Gson gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyConstraintDeserialiser()).create();

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
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
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
		log.debug("Start handle request of {}", url);
		try {

			log.debug("Start handle request of {}", url);

			InputStream inputStream = request.getInputStream();

			byte[] bytes = IOUtils.toByteArray(inputStream);

			if (bytes == null || bytes.length == 0) {
				log.info("Empty body was sent.");
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
			}

			String userId = request.getHeader(Constants.USER_ID_HEADER);

			String data = new String(bytes);
			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
			}
			Either<ComponentInstance, ResponseFormat> convertResponse = convertToResourceInstance(data);

			if (convertResponse.isRight()) {
				BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - updateResourceInstance");
				log.debug("Failed to convert received data to BE format.");
				return buildErrorResponse(convertResponse.right().value());
			}

			ComponentInstance resourceInstance = convertResponse.left().value();
			Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceLogic.updateComponentInstanceMetadata(containerComponentType, componentId, componentInstanceId, userId, resourceInstance);

			if (actionResponse.isRight()) {
				return buildErrorResponse(actionResponse.right().value());
			}
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Resource Instance");
			log.debug("update resource instance with exception", e);
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
		log.debug("Start handle request of {}", url);

		try {
			log.debug("Start handle request of {}", url);

			if (componentInstanceJsonArray == null || componentInstanceJsonArray.length() == 0) {
				log.info("Empty JSON list was sent.");
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
			}

			String userId = request.getHeader(Constants.USER_ID_HEADER);

			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
			}

			Either<List<ComponentInstance>, ResponseFormat> convertResponse = convertToMultipleResourceInstance(componentInstanceJsonArray);

			if (convertResponse.isRight()) {
				// Using both ECOMP error methods, show to Sofer
				BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - updateResourceInstance");
				log.debug("Failed to convert received data to BE format.");
				return buildErrorResponse(convertResponse.right().value());
			}

			List<ComponentInstance> componentInstanceList = convertResponse.left().value();

			Either<List<ComponentInstance>, ResponseFormat> actionResponse = componentInstanceLogic.updateComponentInstance(containerComponentType, componentId, userId, componentInstanceList, true, true);

			if (actionResponse.isRight()) {
				return buildErrorResponse(actionResponse.right().value());
			}

			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());

		} catch (Exception e) {
			/*
			 * BeEcompErrorManager.getInstance().processEcompError( EcompErrorName.BeRestApiGeneralError, "Update Resource Instance" );
			 */
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Resource Instance");
			log.debug("update resource instance with exception", e);
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
			log.debug("Start handle request of {}", url);
			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
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
		log.debug("Start handle request of {}", url);
		Response response = null;

		try {

			log.debug("Start handle request of {}", url);

			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
			}

			Either<RequirementCapabilityRelDef, ResponseFormat> regInfoW = convertToRequirementCapabilityRelDef(data);

			Either<RequirementCapabilityRelDef, ResponseFormat> resultOp;
			if (regInfoW.isRight()) {
				BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - associateRIToRI");
				log.debug("Failed to convert received data to BE format.");
				resultOp = Either.right(regInfoW.right().value());
			} else {
				RequirementCapabilityRelDef requirementDef = regInfoW.left().value();
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
		log.debug("Start handle request of {}", url);

		try {

			log.debug("Start handle request of {}", url);

			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
			}

			Either<RequirementCapabilityRelDef, ResponseFormat> regInfoW = convertToRequirementCapabilityRelDef(data);
			if (regInfoW.isRight()) {
				BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - dissociateRIFromRI");
				log.debug("Failed to convert received data to BE format.");
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
		log.debug("Start handle request of {}", url);
		try {

			log.debug("Start handle request of {}", url);

			InputStream inputStream = request.getInputStream();

			byte[] bytes = IOUtils.toByteArray(inputStream);

			if (bytes == null || bytes.length == 0) {
				log.info("Empty body was sent.");
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
			}

			String userId = request.getHeader(Constants.USER_ID_HEADER);

			String data = new String(bytes);

			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
			}

			Either<CreateAndAssotiateInfo, ActionStatus> convertStatus = convertJsonToObject(data, CreateAndAssotiateInfo.class);
			if (convertStatus.isRight()) {
				BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - createAndAssociateRIToRI");
				log.debug("Failed to convert received data to BE format.");
				Either<Object, ResponseFormat> formattedResponse = Either.right(getComponentsUtils().getResponseFormat(convertStatus.right().value()));
				return buildErrorResponse(formattedResponse.right().value());
			}

			CreateAndAssotiateInfo createAndAssotiateInfo = convertStatus.left().value();
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
	@Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/property")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update resource instance property", httpMethod = "POST", notes = "Returns updated resource instance property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Resource instance created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
	public Response updateResourceInstanceProperty(@ApiParam(value = "service id") @PathParam("componentId") final String componentId,
			@ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
			@ApiParam(value = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId, @ApiParam(value = "id of user initiating the operation") @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
			@Context final HttpServletRequest request) {

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

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

			log.debug("Start handle request of updateResourceInstanceProperty. Received property is {}", property);

			ServletContext context = request.getSession().getServletContext();

			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
			}

			Either<ComponentInstanceProperty, ResponseFormat> actionResponse = componentInstanceLogic.createOrUpdatePropertyValue(componentTypeEnum, componentId, componentInstanceId, property, userId);

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

	@POST
	@Path("/{containerComponentType}/{componentId}/resourceInstance/{componentInstanceId}/input")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update resource instance property", httpMethod = "POST", notes = "Returns updated resource instance property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Resource instance created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
	public Response updateResourceInstanceInput(@ApiParam(value = "service id") @PathParam("componentId") final String componentId,
			@ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
			@ApiParam(value = "resource instance id") @PathParam("componentInstanceId") final String componentInstanceId, @ApiParam(value = "id of user initiating the operation") @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
			@Context final HttpServletRequest request) {

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		try {
			Wrapper<String> dataWrapper = new Wrapper<>();
			Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();

			validateInputStream(request, dataWrapper, errorWrapper);
			ComponentInstanceInput property = null;

			if (errorWrapper.isEmpty()) {
				User modifier = new User();
				modifier.setUserId(userId);
				log.debug("modifier id is {}", userId);

				Either<ComponentInstanceInput, ResponseFormat> inputEither = getComponentsUtils().convertJsonToObjectUsingObjectMapper(dataWrapper.getInnerElement(), modifier, ComponentInstanceInput.class, AuditingActionEnum.UPDATE_RESOURCE_METADATA,
						ComponentTypeEnum.SERVICE);
				;
				if (inputEither.isRight()) {
					log.debug("Failed to convert data to input definition. Status is {}", inputEither.right().value());
					return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
				}
				property = inputEither.left().value();

			}

			if (property == null) {
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
			}

			log.debug("Start handle request of updateResourceInstanceProperty. Received property is {}", property);

			ServletContext context = request.getSession().getServletContext();

			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
			}

			Either<ComponentInstanceInput, ResponseFormat> actionResponse = componentInstanceLogic.createOrUpdateInstanceInputValue(componentTypeEnum, componentId, componentInstanceId, property, userId);

			if (actionResponse.isRight()) {
				return buildErrorResponse(actionResponse.right().value());
			}

			ComponentInstanceInput resourceInstanceProperty = actionResponse.left().value();
			ObjectMapper mapper = new ObjectMapper();
			String result = mapper.writeValueAsString(resourceInstanceProperty);
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
		log.debug("Start handle request of {}", url);

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
		log.debug("Start handle request of {}", url);
		try {

			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
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
		log.debug("Start handle request of {}", url);
		try {
			InputStream inputStream = request.getInputStream();

			byte[] bytes = IOUtils.toByteArray(inputStream);

			if (bytes == null || bytes.length == 0) {
				log.info("Empty body was sent.");
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
			}

			String userId = request.getHeader(Constants.USER_ID_HEADER);

			String data = new String(bytes);

			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
			}

			Either<ComponentInstance, ResponseFormat> convertResponse = convertToResourceInstance(data);

			if (convertResponse.isRight()) {
				BeEcompErrorManager.getInstance().logBeSystemError("Resource Instance - updateResourceInstance");
				log.debug("Failed to convert received data to BE format.");
				return buildErrorResponse(convertResponse.right().value());
			}

			ComponentInstance newResourceInstance = convertResponse.left().value();
			Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceLogic.changeComponentInstanceVersion(containerComponentType, componentId, componentInstanceId, userId, newResourceInstance);

			if (actionResponse.isRight()) {
				return buildErrorResponse(actionResponse.right().value());
			}
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Resource Instance");
			log.debug("update resource instance with exception", e);
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
		log.debug("Start handle request of {}", url);

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

			log.debug("Start handle request of updateResourceInstanceProperty. Received property is {}", property);

			ServletContext context = request.getSession().getServletContext();

			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
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
		log.debug("(GET) Start handle request of {}", url);

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
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("getGroupArtifactById");
			log.debug("getGroupArtifactById unexpected exception", e);
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
		log.debug("(GET) Start handle request of {}", url);

		try {
			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceBL = getComponentInstanceBL(context, componentTypeEnum);

			Either<List<ComponentInstanceProperty>, ResponseFormat> componentInstancePropertiesById = componentInstanceBL.getComponentInstancePropertiesById(containerComponentType, containerComponentId, componentInstanceUniqueId, userId);

			if (componentInstancePropertiesById.isRight()) {
				log.debug("Failed to get properties of component instance ID: {} in {} with ID: {}", componentInstanceUniqueId, containerComponentType, containerComponentId);
				return buildErrorResponse(componentInstancePropertiesById.right().value());
			}

			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), componentInstancePropertiesById.left().value());
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("getGroupArtifactById");
			log.debug("getGroupArtifactById unexpected exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}

	}

	// US330353
	@GET
	@Path("/{containerComponentType}/{containerComponentId}/componentInstances/{componentInstanceUniqueId}/capability/{capabilityType}/capabilityName/{capabilityName}/properties")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get component instance capability properties", httpMethod = "GET", notes = "Returns component instance capability properties", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Properties found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component/Component Instance/Capability - not found") })
	public Response getInstanceCapabilityPropertiesById(@PathParam("containerComponentType") final String containerComponentType, @PathParam("containerComponentId") final String containerComponentId,
			@PathParam("componentInstanceUniqueId") final String componentInstanceUniqueId, @PathParam("capabilityType") final String capabilityType, @PathParam("capabilityName") final String capabilityName, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("(GET) Start handle request of {}", url);

		try {
			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceBL = getComponentInstanceBL(context, componentTypeEnum);

			Either<List<ComponentInstanceProperty>, ResponseFormat> componentInstancePropertiesById = componentInstanceBL.getComponentInstanceCapabilityPropertiesById(containerComponentType, containerComponentId, componentInstanceUniqueId,
					capabilityType, capabilityName, userId);

			if (componentInstancePropertiesById.isRight()) {
				log.debug("Failed to get properties of component instance ID: {} in {} with ID: {}", componentInstanceUniqueId, containerComponentType, containerComponentId);
				return buildErrorResponse(componentInstancePropertiesById.right().value());
			}

			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), componentInstancePropertiesById.left().value());
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("getGroupArtifactById");
			log.debug("getGroupArtifactById unexpected exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}

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

    //US 331281
    @PUT
    @Path("/{containerComponentType}/{containerComponentId}/componentInstances/{componentInstanceUniqueId}/capability/{capabilityType}/capabilityName/{capabilityName}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update Instance Capabilty  Property", httpMethod = "PUT", notes = "Returns updated property", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Resource instance capabilty property updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 404, message = "Component/Component Instance/Capability - not found") })
    public Response updateInstanceCapabilityProperty(@PathParam("containerComponentType") final String containerComponentType, @PathParam("containerComponentId") final String containerComponentId,
                                                     @PathParam("componentInstanceUniqueId") final String componentInstanceUniqueId, @PathParam("capabilityType") final String capabilityType, @PathParam("capabilityName") final String capabilityName,
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
            ComponentInstanceBusinessLogic componentInstanceBL = getComponentInstanceBL(context, componentTypeEnum);

            Either<List<ComponentInstanceProperty>, ResponseFormat> updateCICapProperty = componentInstanceBL.updateInstanceCapabilityProperties(componentTypeEnum, containerComponentId, componentInstanceUniqueId, capabilityType, capabilityName, propertiesToUpdate, userId);

            if (updateCICapProperty.isRight()) {
                log.debug("Failed to get properties of component instance ID: {} in {} with ID: {}", componentInstanceUniqueId, containerComponentType, containerComponentId);
                return buildErrorResponse(updateCICapProperty.right().value());
            }

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), updateCICapProperty.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("getGroupArtifactById");
            log.debug("getGroupArtifactById unexpected exception", e);
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
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
			}
			Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceLogic.createServiceProxy(containerComponentType, containerComponentId, userId, componentInstance);

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
			log.debug("Start handle request of {}", url);
			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
			}
			String userId = request.getHeader(Constants.USER_ID_HEADER);
			Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceLogic.deleteServiceProxy(containerComponentType, containerComponentId, serviceProxyId, userId);

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
		log.debug("Start handle request of {}", url);
		try {

			String userId = request.getHeader(Constants.USER_ID_HEADER);

			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
			}
			Either<ComponentInstance, ResponseFormat> actionResponse = componentInstanceLogic.changeServiceProxyVersion(containerComponentType, containerComponentId, serviceProxyId, userId);

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
		log.debug("(GET) Start handle request of {}", url);
		try {
			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			ComponentInstanceBusinessLogic componentInstanceLogic = getComponentInstanceBL(context, componentTypeEnum);
			if (componentInstanceLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
			}
			
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

	private Either<RequirementCapabilityRelDef, ResponseFormat> convertToRequirementCapabilityRelDef(String data) {

		Either<RequirementCapabilityRelDef, ActionStatus> convertStatus = convertJsonToObject(data, RequirementCapabilityRelDef.class);
		if (convertStatus.isRight()) {
			return Either.right(getComponentsUtils().getResponseFormat(convertStatus.right().value()));
		}
		RequirementCapabilityRelDef requirementCapabilityRelDef = convertStatus.left().value();
		return Either.left(requirementCapabilityRelDef);
	}

	private <T> Either<T, ActionStatus> convertJsonToObject(String data, Class<T> clazz) {
		try {
			log.trace("convert json to object. json=\n {}", data);
			T t = null;
			t = gson.fromJson(data, clazz);
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
}
