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
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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

import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.CapReqDef;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.aspects.Loggable;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import fj.data.Either;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Component Servlet", description = "Component Servlet")
@Singleton
public class ComponentServlet extends BeGenericServlet {
	private static Logger log = LoggerFactory.getLogger(ComponentServlet.class.getName());

	@GET
	@Path("/{componentType}/{componentId}/requirmentsCapabilities")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Component Requirments And Capabilities", httpMethod = "GET", notes = "Returns Requirments And Capabilities according to componentId", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
	public Response getRequirementAndCapabilities(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
		Response response;
		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
		if (componentTypeEnum != null) {
			ComponentBusinessLogic compBL = getComponentBL(componentTypeEnum, context);
			Either<CapReqDef, ResponseFormat> eitherRequirementsAndCapabilities = compBL.getRequirementsAndCapabilities(componentId, componentTypeEnum, userId);
			if (eitherRequirementsAndCapabilities.isRight()) {
				response = buildErrorResponse(eitherRequirementsAndCapabilities.right().value());
			} else {
				response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), gson.toJson(eitherRequirementsAndCapabilities.left().value()));
			}
		} else {
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
		}

		return response;
	}

	@GET
	@Path("/{componentType}/latestversion/notabstract")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Component Requirments And Capabilities", httpMethod = "GET", notes = "Returns Requirments And Capabilities according to componentId", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
	public Response getLatestVersionNotAbstractCheckoutComponents(@PathParam("componentType") final String componentType, @Context final HttpServletRequest request, @QueryParam("internalComponentType") String internalComponentType,
			@QueryParam("componentUids") List<String> componentUids, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("(get) Start handle request of {}", url);
		Response response = null;

		try {

			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
			ComponentBusinessLogic businessLogic = getComponentBL(componentTypeEnum, context);

			log.debug("Received componentUids size is {}", (componentUids == null ? 0 : componentUids.size()));

			Either<List<Component>, ResponseFormat> actionResponse = businessLogic.getLatestVersionNotAbstractComponents(false, HighestFilterEnum.HIGHEST_ONLY, componentTypeEnum, internalComponentType, componentUids, userId);

			if (actionResponse.isRight()) {
				log.debug("failed to get all non abstract {}", componentType);
				return buildErrorResponse(actionResponse.right().value());
			}
			Object components = RepresentationUtils.toRepresentation(actionResponse.left().value());
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), components);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get Certified Non Abstract" + componentType);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Certified Non Abstract" + componentType);
			log.debug("getCertifiedNotAbstractComponents failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}

	}

	@POST
	@Path("/{componentType}/latestversion/notabstract")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Component Requirments And Capabilities", httpMethod = "GET", notes = "Returns Requirments And Capabilities according to componentId", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
	public Response getLatestVersionNotAbstractCheckoutComponentsByBody(@PathParam("componentType") final String componentType, @Context final HttpServletRequest request, @QueryParam("internalComponentType") String internalComponentType,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId, @ApiParam(value = "Consumer Object to be created", required = true) List<String> data) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		if (log.isDebugEnabled())
			log.debug("(get) Start handle request of {}", url);
		Response response = null;

		try {

			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
			ComponentBusinessLogic businessLogic = getComponentBL(componentTypeEnum, context);
			List<String> componentUids = data;
			if (log.isDebugEnabled())
				log.debug("Received componentUids size is {}", (componentUids == null ? 0 : componentUids.size()));

			// long start = System.currentTimeMillis();

			Either<List<Component>, ResponseFormat> actionResponse = businessLogic.getLatestVersionNotAbstractComponents(false, HighestFilterEnum.HIGHEST_ONLY, componentTypeEnum, internalComponentType, componentUids, userId);

			// long endBl = System.currentTimeMillis();

			if (actionResponse.isRight()) {
				if (log.isDebugEnabled())
					log.debug("failed to get all non abstract {}", componentType);
				return buildErrorResponse(actionResponse.right().value());

			}
			Object components = RepresentationUtils.toRepresentation(actionResponse.left().value());
			Response responseToReturn = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), components);

			long endResp = System.currentTimeMillis();

			// log.info("********** Time calculation in ms: BL {} , Response {},
			// Total {}", (endBl - start ), (endResp - endBl), (endResp -
			// start));
			return responseToReturn;

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get Certified Non Abstract" + componentType);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Certified Non Abstract" + componentType);
			log.debug("getCertifiedNotAbstractComponents failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}

	}

	@GET
	@Path("/{componentType}/latestversion/notabstract/uidonly")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Component uid only", httpMethod = "GET", notes = "Returns componentId", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
	public Response getLatestVersionNotAbstractCheckoutComponentsIdesOnly(@PathParam("componentType") final String componentType, @Context final HttpServletRequest request, @QueryParam("internalComponentType") String internalComponentType,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId, @ApiParam(value = "uid list", required = true) String data) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("(get) Start handle request of {}", url);
		Response response = null;
		try {
			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
			ComponentBusinessLogic businessLogic = getComponentBL(componentTypeEnum, context);

			Either<List<Map<String, String>>, ResponseFormat> actionResponse = businessLogic.getLatestVersionNotAbstractComponentsUidOnly(false, HighestFilterEnum.HIGHEST_ONLY, componentTypeEnum, internalComponentType, userId);
			if (actionResponse.isRight()) {
				log.debug("failed to get all non abstract {}", componentType);
				return buildErrorResponse(actionResponse.right().value());
			}
			Object components = RepresentationUtils.toRepresentation(actionResponse.left().value());
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), components);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get Certified Non Abstract" + componentType);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Certified Non Abstract" + componentType);
			log.debug("getCertifiedNotAbstractComponents failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}

	}

	@GET
	@Path("/{componentType}/{componentId}/componentInstances")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Component instances", httpMethod = "GET", notes = "Returns component instances", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
	public Response getComponentInstancesFilteredByPropertiesAndInputs(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @Context final HttpServletRequest request,
			@QueryParam("searchText") String searchText, @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @ApiParam(value = "uid" + "" + "list", required = true) String data) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("(get) Start handle request of {}", url);
		Response response = null;
		try {
			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
			ComponentBusinessLogic businessLogic = getComponentBL(componentTypeEnum, context);

			Either<List<ComponentInstance>, ResponseFormat> actionResponse = businessLogic.getComponentInstancesFilteredByPropertiesAndInputs(componentId, componentTypeEnum, userId, searchText);
			if (actionResponse.isRight()) {
				log.debug("failed to get all component instances filtered by properties and inputs {}", componentType);
				return buildErrorResponse(actionResponse.right().value());
			}
			Object components = RepresentationUtils.toRepresentation(actionResponse.left().value());
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), components);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get Component Instances filtered by properties & inputs" + componentType);
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Component Instances filtered by properties & inputs" + componentType);
			log.debug("getComponentInstancesFilteredByPropertiesAndInputs failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;
		}
	}
}
