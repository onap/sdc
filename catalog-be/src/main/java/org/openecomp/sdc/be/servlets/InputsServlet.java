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
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import fj.data.Either;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Input Catalog", description = "Input Servlet")
@Singleton
public class InputsServlet extends BeGenericServlet {

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
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Inputs" + componentType);
			log.debug("getInputs failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}

	@GET
	@Path("/{componentType}/{componentId}/componentInstances/{instanceId}/{originComonentUid}/inputs")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Inputs only", httpMethod = "GET", notes = "Returns Inputs list", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
	public Response getComponentInstanceInputs(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @PathParam("instanceId") final String instanceId,
			@PathParam("originComonentUid") final String originComonentUid, @Context final HttpServletRequest request, @QueryParam("fromName") String fromName, @QueryParam("amount") int amount,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("(get) Start handle request of {}", url);
		Response response = null;

		try {
			InputsBusinessLogic businessLogic = getInputBL(context);

			Either<List<InputDefinition>, ResponseFormat> inputsResponse = businessLogic.getInputs(userId, originComonentUid, fromName, amount);
			if (inputsResponse.isRight()) {
				log.debug("failed to get component instance inputs {}", componentType);
				return buildErrorResponse(inputsResponse.right().value());
			}
			Object inputs = RepresentationUtils.toRepresentation(inputsResponse.left().value());
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), inputs);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get Component Instance Inputs" + componentType);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Inputs" + componentType);
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
		log.debug("(get) Start handle request of {}", url);
		Response response = null;

		try {
			InputsBusinessLogic businessLogic = getInputBL(context);

			Either<List<ComponentInstanceProperty>, ResponseFormat> inputPropertiesRes = businessLogic.getComponentInstancePropertiesByInputId(userId, instanceId, inputId);
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
			Either<InputDefinition, ResponseFormat> deleteInput = businessLogic.deleteInput(componentType, componentId, userId, inputId, true);
			if (deleteInput.isRight()){
				ResponseFormat deleteResponseFormat = deleteInput.right().value();
				response = buildErrorResponse(deleteResponseFormat);
				return response;
			}		
			InputDefinition inputDefinition = deleteInput.left().value();
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), inputDefinition);
		} catch (Exception e){
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Delete input for service {} with id: {}", componentId, inputId);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete input for service + " + componentId + " + with id: " + inputId);
			log.debug("Delete input failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}

	protected InputsBusinessLogic getInputBL(ServletContext context) {

		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		InputsBusinessLogic inputsBusinessLogic = webApplicationContext.getBean(InputsBusinessLogic.class);
		return inputsBusinessLogic;
	}

}
