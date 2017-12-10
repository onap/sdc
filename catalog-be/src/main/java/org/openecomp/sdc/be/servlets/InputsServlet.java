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

import java.util.List;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.sdc.be.components.impl.InputsBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.jcabi.aspects.Loggable;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Input Catalog", description = "Input Servlet")
@Singleton
public class InputsServlet extends AbstractValidationsServlet {

	private static Logger log = LoggerFactory.getLogger(ProductServlet.class.getName());

	@GET
	@Path("/services/{componentId}/inputs")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Inputs only", httpMethod = "GET", notes = "Returns Inputs list", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
	public Response getComponentInputs(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @Context final HttpServletRequest request, @QueryParam("fromId") String fromName,
			@QueryParam("amount") int amount, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("(get) Start handle request of {}", url);
		Response response = null;

		try {

			InputsBusinessLogic businessLogic = getInputBL(context);

			Either<List<InputDefinition>, ResponseFormat> inputsResponse = businessLogic.getInputs(userId, componentId, fromName, amount);
			if (inputsResponse.isRight()) {
				log.debug("failed to get inputs {}", componentType);
				return buildErrorResponse(inputsResponse.right().value());
			}
			Object inputs = RepresentationUtils.toRepresentation(inputsResponse.left().value());
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), inputs);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get Component Instance Inputs" + componentType);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Inputs " + componentType);
			log.debug("getInputs failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}
	

	@POST
	@Path("/{containerComponentType}/{componentId}/update/inputs")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update resource  inputs", httpMethod = "POST", notes = "Returns updated input", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Input updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
	public Response updateComponentInputs(
			@ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
			@PathParam("componentId") final String componentId, 
			@ApiParam(value = "json describe the input", required = true) String data, @Context final HttpServletRequest request) {


		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);
		String userId = request.getHeader(Constants.USER_ID_HEADER);

		try {
			User modifier = new User();
			modifier.setUserId(userId);
			log.debug("modifier id is {}", userId);

			Either<InputDefinition, ResponseFormat> inputEither = getComponentsUtils().convertJsonToObjectUsingObjectMapper(data, modifier, InputDefinition.class, AuditingActionEnum.UPDATE_RESOURCE_METADATA, ComponentTypeEnum.SERVICE);;
			if(inputEither.isRight()){
				log.debug("Failed to convert data to input definition. Status is {}", inputEither.right().value());
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
			}
			InputDefinition input = inputEither.left().value();

			log.debug("Start handle request of updateResourceInstanceProperty. Received property is {}", input);

			ServletContext context = request.getSession().getServletContext();
			ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(containerComponentType);
			
			InputsBusinessLogic businessLogic = getInputBL(context);
			if (businessLogic == null) {
				log.debug("Unsupported component type {}", containerComponentType);
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR));
			}

			Either<InputDefinition, ResponseFormat> actionResponse = businessLogic.updateInputValue(componentType, componentId, input, userId, true, false);

			if (actionResponse.isRight()) {
				return buildErrorResponse(actionResponse.right().value());
			}

			InputDefinition resourceInstanceProperty = actionResponse.left().value();
			ObjectMapper mapper = new ObjectMapper();
			String result = mapper.writeValueAsString(resourceInstanceProperty);
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

		} catch (Exception e) {
			log.error("create and associate RI failed with exception: {}", e.getMessage(), e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
	}


	@GET
	@Path("/{componentType}/{componentId}/componentInstances/{instanceId}/{originComponentUid}/inputs")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Inputs only", httpMethod = "GET", notes = "Returns Inputs list", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
	public Response getComponentInstanceInputs(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @PathParam("instanceId") final String instanceId,
			@PathParam("originComponentUid") final String originComonentUid, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("(get) Start handle request of {}", url);
		Response response = null;

		try {
			InputsBusinessLogic businessLogic = getInputBL(context);

			Either<List<ComponentInstanceInput>, ResponseFormat> inputsResponse = businessLogic.getComponentInstanceInputs(userId, componentId, instanceId);
			if (inputsResponse.isRight()) {
				log.debug("failed to get component instance inputs {}", componentType);
				return buildErrorResponse(inputsResponse.right().value());
			}
			Object inputs = RepresentationUtils.toRepresentation(inputsResponse.left().value());
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), inputs);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get Component Instance Inputs" + componentType);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Inputs " + componentType);
			log.debug("getInputs failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}

	@GET
	@Path("/{componentType}/{componentId}/componentInstances/{instanceId}/{inputId}/properties")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get properties", httpMethod = "GET", notes = "Returns properties list", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
	public Response getInputPropertiesForComponentInstance(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @PathParam("instanceId") final String instanceId,
			@PathParam("inputId") final String inputId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("(GET) Start handle request of {}", url);
		Response response = null;

		try {
			InputsBusinessLogic businessLogic = getInputBL(context);

			Either<List<ComponentInstanceProperty>, ResponseFormat> inputPropertiesRes = businessLogic.getComponentInstancePropertiesByInputId(userId, componentId, instanceId, inputId);
			if (inputPropertiesRes.isRight()) {
				log.debug("failed to get properties of input: {}, with instance id: {}", inputId, instanceId);
				return buildErrorResponse(inputPropertiesRes.right().value());
			}
			Object properties = RepresentationUtils.toRepresentation(inputPropertiesRes.left().value());
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), properties);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get properties by input id {}, for component instance {} ", inputId, instanceId);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Properites by input id: " + inputId + " for instance with id: " + instanceId);
			log.debug("getInputPropertiesForComponentInstance failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}

	@GET
	@Path("/{componentType}/{componentId}/inputs/{inputId}/inputs")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get inputs", httpMethod = "GET", notes = "Returns inputs list", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
	public Response getInputsForComponentInput(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @PathParam("inputId") final String inputId, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("(get) Start handle request of {}", url);
		Response response = null;

		try {
			InputsBusinessLogic businessLogic = getInputBL(context);

			Either<List<ComponentInstanceInput>, ResponseFormat> inputsRes = businessLogic.getInputsForComponentInput(userId, componentId, inputId);
			
			if (inputsRes.isRight()) {
				log.debug("failed to get inputs of input: {}, with instance id: {}", inputId, componentId);
				return buildErrorResponse(inputsRes.right().value());
			}
			Object properties = RepresentationUtils.toRepresentation(inputsRes.left().value());
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), properties);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get inputs by input id {}, for component {} ", inputId, componentId);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get inputs by input id: " + inputId + " for component with id: " + componentId);
			log.debug("getInputsForComponentInput failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}
	
	@GET
	@Path("/{componentType}/{componentId}/inputs/{inputId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get inputs", httpMethod = "GET", notes = "Returns inputs list", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
	public Response getInputsAndPropertiesForComponentInput(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @PathParam("inputId") final String inputId, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("(get) Start handle request of {}", url);
		Response response = null;

		try {
			InputsBusinessLogic businessLogic = getInputBL(context);

			Either<InputDefinition, ResponseFormat> inputsRes = businessLogic.getInputsAndPropertiesForComponentInput(userId, componentId, inputId, false);
			
			if (inputsRes.isRight()) {
				log.debug("failed to get inputs of input: {}, with instance id: {}", inputId, componentId);
				return buildErrorResponse(inputsRes.right().value());
			}
			Object properties = RepresentationUtils.toRepresentation(inputsRes.left().value());
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), properties);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get inputs by input id {}, for component {} ", inputId, componentId);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get inputs by input id: " + inputId + " for component with id: " + componentId);
			log.debug("getInputsForComponentInput failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}

	public Either<ComponentInstInputsMap, ResponseFormat> parseToComponentInstanceMap(String serviceJson, User user) {
		return getComponentsUtils().convertJsonToObjectUsingObjectMapper(serviceJson, user, ComponentInstInputsMap.class, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.SERVICE);
	}

	@POST
	@Path("/{componentType}/{componentId}/create/inputs")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create inputs on service", httpMethod = "POST", notes = "Return inputs list", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
	public Response createMultipleInputs(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId, @ApiParam(value = "ComponentIns Inputs Object to be created", required = true) String componentInstInputsMapObj) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("(get) Start handle request of {}", url);
		Response response = null;

		try {
			InputsBusinessLogic businessLogic = getInputBL(context);

			// get modifier id
			User modifier = new User();
			modifier.setUserId(userId);
			log.debug("modifier id is {}", userId);

			Either<ComponentInstInputsMap, ResponseFormat> componentInstInputsMapRes = parseToComponentInstanceMap(componentInstInputsMapObj, modifier);
			if (componentInstInputsMapRes.isRight()) {
				log.debug("failed to parse componentInstInputsMap");
				response = buildErrorResponse(componentInstInputsMapRes.right().value());
				return response;
			}

			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
			ComponentInstInputsMap componentInstInputsMap = componentInstInputsMapRes.left().value();

			Either<List<InputDefinition>, ResponseFormat> inputPropertiesRes = businessLogic.createMultipleInputs(userId, componentId, componentTypeEnum, componentInstInputsMap, true, false);
			if (inputPropertiesRes.isRight()) {
				log.debug("failed to create inputs  for service: {}", componentId);
				return buildErrorResponse(inputPropertiesRes.right().value());
			}
			Object properties = RepresentationUtils.toRepresentation(inputPropertiesRes.left().value());
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), properties);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Create inputs for service with id: {}", componentId);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create inputs for service with id: " + componentId);
			log.debug("createMultipleInputs failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}
	

	
	@DELETE
	@Path("/{componentType}/{componentId}/delete/{inputId}/input")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Delete input from service", httpMethod = "DELETE", notes = "Delete service input", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Input deleted"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Input not found") })
	public Response deleteInput (
			@PathParam("componentType") final String componentType,
			@PathParam("componentId") final String componentId,
			@PathParam("inputId") final String inputId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @ApiParam(value = "Service Input to be deleted", required = true) String componentInstInputsMapObj) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("(get) Start handle request of {}", url);
		Response response = null;

		try {
			InputsBusinessLogic businessLogic = getInputBL(context);
			Either<InputDefinition, ResponseFormat> deleteInput = businessLogic.deleteInput(componentType, componentId, userId, inputId);
			if (deleteInput.isRight()){
				ResponseFormat deleteResponseFormat = deleteInput.right().value();
				response = buildErrorResponse(deleteResponseFormat);
				return response;
			}		
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), deleteInput.left().value());
		} catch (Exception e){
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Delete input for service {} with id: {}", componentId, inputId);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete input for service + " + componentId + " + with id: " + inputId);
			log.debug("Delete input failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}
	
	
	
	/*@PUT
	@Path("/{componentType}/{componentId}/edit/{inputId}/input")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Delete input from service", httpMethod = "DELETE", notes = "Delete service input", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Input deleted"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Input not found") })
	public Response editInputValue (
			@PathParam("componentType") final String componentType,
			@PathParam("componentId") final String componentId,
			@PathParam("inputId") final String inputId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("(get) Start handle request of {}", url);
		Response response = null;

		try {
			InputsBusinessLogic businessLogic = getInputBL(context);
			Either<InputDefinition, ResponseFormat> deleteInput = businessLogic.deleteInput(componentType, componentId, userId, inputId);
			if (deleteInput.isRight()){
				ResponseFormat deleteResponseFormat = deleteInput.right().value();
				response = buildErrorResponse(deleteResponseFormat);
				return response;
			}		
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), deleteInput.left().value());
		} catch (Exception e){
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Delete input for service {} with id: {}", componentId, inputId);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete input for service + " + componentId + " + with id: " + inputId);
			log.debug("Delete input failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}*/
	
	protected InputsBusinessLogic getInputBL(ServletContext context) {

		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		InputsBusinessLogic inputsBusinessLogic = webApplicationContext.getBean(InputsBusinessLogic.class);
		return inputsBusinessLogic;
	}

}
