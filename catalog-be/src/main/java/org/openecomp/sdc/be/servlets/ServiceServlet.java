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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.apache.http.HttpStatus;
import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;
import com.jcabi.aspects.Loggable;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Service Catalog", description = "Service Servlet")
@Singleton
public class ServiceServlet extends AbstractValidationsServlet {

	private static Logger log = LoggerFactory.getLogger(ServiceServlet.class.getName());

	@POST
	@Path("/services")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Service", httpMethod = "POST", notes = "Returns created service", response = Service.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Service created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Service already exist") })
	public Response createService(@ApiParam(value = "Service object to be created", required = true) String data, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;
		try {
			ServiceBusinessLogic businessLogic = getServiceBL(context);
			Either<Service, ResponseFormat> convertResponse = parseToService(data, modifier);
			if (convertResponse.isRight()) {
				log.debug("failed to parse service");
				response = buildErrorResponse(convertResponse.right().value());
				return response;
			}

			Service service = convertResponse.left().value();
			Either<Service, ResponseFormat> actionResponse = businessLogic.createService(service, modifier);

			if (actionResponse.isRight()) {
				log.debug("Failed to create service");
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}

			Object result = RepresentationUtils.toRepresentation(actionResponse.left().value());
			response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), result);
			return response;

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Service");
			log.debug("create service failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;
		}
	}

	public Either<Service, ResponseFormat> parseToService(String serviceJson, User user) {
		return getComponentsUtils().convertJsonToObjectUsingObjectMapper(serviceJson, user, Service.class, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.SERVICE);
	}

	@GET
	@Path("/services/validate-name/{serviceName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "validate service name", httpMethod = "GET", notes = "checks if the chosen service name is available ", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Service found"), @ApiResponse(code = 403, message = "Restricted operation") })
	public Response validateServiceName(@PathParam("serviceName") final String serviceName, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		// get modifier id
		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);
		Response response = null;
		try {
			ServiceBusinessLogic businessLogic = getServiceBL(context);

			Either<Map<String, Boolean>, ResponseFormat> actionResponse = businessLogic.validateServiceNameExists(serviceName, userId);

			if (actionResponse.isRight()) {
				log.debug("failed to get validate service name");
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Validate Service Name");
			log.debug("validate service name failed with exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
	}

	@GET
	@Path("/audit-records/{componentType}/{componentUniqueId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "get component audit records", httpMethod = "GET", notes = "get audit records for a service or a resource", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Service found"), @ApiResponse(code = 403, message = "Restricted operation") })
	public Response getComponentAuditRecords(@PathParam("componentType") final String componentType, @PathParam("componentUniqueId") final String componentUniqueId, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
		init(log);
		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);
		Wrapper<Response> responseWrapper = new Wrapper<Response>();
		Wrapper<String> uuidWrapper = new Wrapper<>();
		Wrapper<String> versionWrapper = new Wrapper<>();
		Wrapper<User> userWrapper = new Wrapper<>();
		Wrapper<ComponentTypeEnum> componentWrapper = new Wrapper<ComponentTypeEnum>();
		try {
			validateUserExist(responseWrapper, userWrapper, userId);

			if (responseWrapper.isEmpty()) {
				validateComponentType(responseWrapper, componentWrapper, componentType);
			}

			if (responseWrapper.isEmpty()) {
				fillUUIDAndVersion(responseWrapper, uuidWrapper, versionWrapper, userWrapper.getInnerElement(), componentWrapper.getInnerElement(), componentUniqueId, context);
			}

			if (responseWrapper.isEmpty()) {
				Either<List<Map<String, Object>>, ResponseFormat> eitherServiceAudit = getServiceBL(context).getComponentAuditRecords(versionWrapper.getInnerElement(), uuidWrapper.getInnerElement(), userId);

				if (eitherServiceAudit.isRight()) {
					Response errorResponse = buildErrorResponse(eitherServiceAudit.right().value());
					responseWrapper.setInnerElement(errorResponse);
				} else {
					List<Map<String, Object>> auditRecords = eitherServiceAudit.left().value();
					Response okResponse = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), auditRecords);
					responseWrapper.setInnerElement(okResponse);

				}
			}

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Validate Service Name");
			log.debug("get Service Audit Records failed with exception", e);
			Response errorResponse = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			responseWrapper.setInnerElement(errorResponse);
		}
		return responseWrapper.getInnerElement();
	}

	private void fillUUIDAndVersion(Wrapper<Response> responseWrapper, Wrapper<String> uuidWrapper, Wrapper<String> versionWrapper, User user, final ComponentTypeEnum componentTypeEnum, final String componentUniqueId, ServletContext context) {

		if (componentTypeEnum == ComponentTypeEnum.RESOURCE) {
			Either<Resource, ResponseFormat> eitherResource = getResourceBL(context).getResource(componentUniqueId, user);
			if (eitherResource.isLeft()) {
				uuidWrapper.setInnerElement(eitherResource.left().value().getUUID());
				versionWrapper.setInnerElement(eitherResource.left().value().getVersion());
			} else {
				responseWrapper.setInnerElement(buildErrorResponse(eitherResource.right().value()));
			}

		} else {
			Either<Service, ResponseFormat> eitherService = getServiceBL(context).getService(componentUniqueId, user);
			if (eitherService.isLeft()) {
				uuidWrapper.setInnerElement(eitherService.left().value().getUUID());
				versionWrapper.setInnerElement(eitherService.left().value().getVersion());
			} else {
				responseWrapper.setInnerElement(buildErrorResponse(eitherService.right().value()));

			}
		}
	}

	@DELETE
	@Path("/services/{serviceId}")
	public Response deleteService(@PathParam("serviceId") final String serviceId, @Context final HttpServletRequest request) {
		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		// get modifier id
		String userId = request.getHeader(Constants.USER_ID_HEADER);
		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;

		try {
			String serviceIdLower = serviceId.toLowerCase();
			ServiceBusinessLogic businessLogic = getServiceBL(context);
			ResponseFormat actionResponse = businessLogic.deleteService(serviceIdLower, modifier);

			if (actionResponse.getStatus() != HttpStatus.SC_NO_CONTENT) {
				log.debug("failed to delete service");
				response = buildErrorResponse(actionResponse);
				return response;
			}
			response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), null);
			return response;

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Service");
			log.debug("delete service failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}

	@DELETE
	@Path("/services/{serviceName}/{version}")
	public Response deleteServiceByNameAndVersion(@PathParam("serviceName") final String serviceName, @PathParam("version") final String version, @Context final HttpServletRequest request) {
		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		// get modifier id
		String userId = request.getHeader(Constants.USER_ID_HEADER);
		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;

		try {
			ServiceBusinessLogic businessLogic = getServiceBL(context);
			ResponseFormat actionResponse = businessLogic.deleteServiceByNameAndVersion(serviceName, version, modifier);

			if (actionResponse.getStatus() != HttpStatus.SC_NO_CONTENT) {
				log.debug("failed to delete service");
				response = buildErrorResponse(actionResponse);
				return response;
			}
			response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), null);
			return response;

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Service");
			log.debug("delete service failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}

	@PUT
	@Path("/services/{serviceId}/metadata")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update Service Metadata", httpMethod = "PUT", notes = "Returns updated service", response = Service.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Service Updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
	public Response updateServiceMetadata(@PathParam("serviceId") final String serviceId, @ApiParam(value = "Service object to be Updated", required = true) String data, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;

		try {
			String serviceIdLower = serviceId.toLowerCase();
			ServiceBusinessLogic businessLogic = getServiceBL(context);

			Either<Service, ResponseFormat> convertResponse = parseToService(data, modifier);
			if (convertResponse.isRight()) {
				log.debug("failed to parse service");
				response = buildErrorResponse(convertResponse.right().value());
				return response;
			}
			Service updatedService = convertResponse.left().value();
			Either<Service, ResponseFormat> actionResponse = businessLogic.updateServiceMetadata(serviceIdLower, updatedService, modifier);

			if (actionResponse.isRight()) {
				log.debug("failed to update service");
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}

			Service service = actionResponse.left().value();
			Object result = RepresentationUtils.toRepresentation(service);
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Service Metadata");
			log.debug("update service metadata failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}
	/**
	 * updates group instance property values
	 * Note, than in case of group instance updated successfully, related resourceInstance and containing component modification time will be updated
	 * @param serviceId
	 * @param componentInstanceId
	 * @param groupInstanceId
	 * @param data
	 * @param request
	 * @param userId
	 * @return
	 */
	@PUT
	@Path("/{containerComponentType}/{serviceId}/resourceInstance/{componentInstanceId}/groupInstance/{groupInstanceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update Group Instance Property Values", httpMethod = "PUT", notes = "Returns updated group instance", response = Service.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Group Instance Property Values Updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
	public Response updateGroupInstancePropertyValues(@PathParam("serviceId") final String serviceId,@PathParam("componentInstanceId") final String componentInstanceId, @PathParam("groupInstanceId") final String groupInstanceId, @ApiParam(value = "Group instance object to be Updated", required = true) String data, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		Response response = null;
		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);
		
		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}",userId);
		
		ServiceBusinessLogic businessLogic;
		Either<List<GroupInstanceProperty>, ResponseFormat> actionResponse = null;
		try {
			List<GroupInstanceProperty> updatedProperties;
			Type listType = new TypeToken<ArrayList<GroupInstanceProperty>>(){}.getType();
			ArrayList<GroupInstanceProperty> newProperties = gson.fromJson(data, listType);
			if (newProperties == null) {
				actionResponse = Either.right(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
			}
			if(actionResponse == null){
				log.debug("Start handle update group instance property values request. Received group instance is {}", groupInstanceId);
				businessLogic = getServiceBL(context);
				actionResponse = businessLogic.updateGroupInstancePropertyValues(modifier, serviceId, componentInstanceId, groupInstanceId, newProperties);
				if(actionResponse.isRight()){
					actionResponse = Either.right(actionResponse.right().value());
				}
			}
			if(actionResponse.isLeft()){
				updatedProperties = actionResponse.left().value();
				ObjectMapper mapper = new ObjectMapper();
				String result = mapper.writeValueAsString(updatedProperties);
				response =  buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
			}
			else{
				response = buildErrorResponse(actionResponse.right().value());
			}
		} catch (Exception e) {
			log.error("Exception occured during update Group Instance property values: {}", e.getMessage(), e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
		return response;
	}

	@GET
	@Path("/services/{serviceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Retrieve Service", httpMethod = "GET", notes = "Returns service according to serviceId", response = Service.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Service found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Service not found") })
	public Response getServiceById(@PathParam("serviceId") final String serviceId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		// get modifier id
		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;
		try {
			String serviceIdLower = serviceId.toLowerCase();
			ServiceBusinessLogic businessLogic = getServiceBL(context);
			log.debug("get service with id {}", serviceId);
			Either<Service, ResponseFormat> actionResponse = businessLogic.getService(serviceIdLower, modifier);

			if (actionResponse.isRight()) {
				log.debug("failed to get service");
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}

			Service service = actionResponse.left().value();
			Object result = RepresentationUtils.toRepresentation(service);

			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Service");
			log.debug("get service failed with exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

		}
	}

	@GET
	@Path("/services/serviceName/{serviceName}/serviceVersion/{serviceVersion}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Retrieve Service", httpMethod = "GET", notes = "Returns service according to name and version", response = Service.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Service found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Service not found") })
	public Response getServiceByNameAndVersion(@PathParam("serviceName") final String serviceName, @PathParam("serviceVersion") final String serviceVersion, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		// get modifier id
		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;
		try {
			ServiceBusinessLogic businessLogic = getServiceBL(context);
			Either<Service, ResponseFormat> actionResponse = businessLogic.getServiceByNameAndVersion(serviceName, serviceVersion, userId);

			if (actionResponse.isRight()) {
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}

			Service service = actionResponse.left().value();
			Object result = RepresentationUtils.toRepresentation(service);

			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Service by name and version");
			log.debug("get service failed with exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

		}
	}

	@POST
	@Path("/services/{serviceId}/distribution-state/{state}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update Service Distribution State", httpMethod = "POST", notes = "service with the changed distribution status")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Service distribution state changed"), @ApiResponse(code = 409, message = "Restricted operation"), @ApiResponse(code = 403, message = "Service is not available for distribution"),
			@ApiResponse(code = 400, message = "Invalid content / Missing content"), @ApiResponse(code = 404, message = "Requested service was not found"), @ApiResponse(code = 500, message = "Internal Server Error. Please try again later.") })
	public Response updateServiceDistributionState(@ApiParam(value = "DistributionChangeInfo - get comment out of body", required = true) LifecycleChangeInfoWithAction jsonChangeInfo, @PathParam("serviceId") final String serviceId,
			@ApiParam(allowableValues = "approve, reject", required = true) @PathParam("state") final String state, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;
		try {
			ServiceBusinessLogic businessLogic = getServiceBL(context);
			Either<Service, ResponseFormat> actionResponse = businessLogic.changeServiceDistributionState(serviceId, state, jsonChangeInfo, modifier);

			if (actionResponse.isRight()) {
				log.debug("failed to Update Service Distribution State");
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}
			Service service = actionResponse.left().value();
			Object result = RepresentationUtils.toRepresentation(service);

			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Service Distribution State");
			log.debug("updateServiceDistributionState failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;
		}
	}

	@POST
	@Path("/services/{serviceId}/distribution/{env}/activate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Activate distribution", httpMethod = "POST", notes = "activate distribution")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 409, message = "Service cannot be distributed due to missing deployment artifacts"), @ApiResponse(code = 404, message = "Requested service was not found"),
			@ApiResponse(code = 500, message = "Internal Server Error. Please try again later.") })
	public Response activateDistribution(@PathParam("serviceId") final String serviceId, @PathParam("env") final String env, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;
		try {
			ServiceBusinessLogic businessLogic = getServiceBL(context);
			Either<Service, ResponseFormat> distResponse = businessLogic.activateDistribution(serviceId, env, modifier, request);

			if (distResponse.isRight()) {
				log.debug("failed to activate service distribution");
				response = buildErrorResponse(distResponse.right().value());
				return response;
			}
			Service service = distResponse.left().value();
			Object result = RepresentationUtils.toRepresentation(service);
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Activate Distribution");
			log.debug("activate distribution failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;
		}
	}

	@POST
	@Path("/services/{serviceId}/distribution/{did}/markDeployed")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Mark distribution as deployed", httpMethod = "POST", notes = "relevant audit record will be created")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Service was marked as deployed"), @ApiResponse(code = 409, message = "Restricted operation"), @ApiResponse(code = 403, message = "Service is not available"),
			@ApiResponse(code = 400, message = "Invalid content / Missing content"), @ApiResponse(code = 404, message = "Requested service was not found"), @ApiResponse(code = 500, message = "Internal Server Error. Please try again later.") })
	public Response markDistributionAsDeployed(@PathParam("serviceId") final String serviceId, @PathParam("did") final String did, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;
		try {
			ServiceBusinessLogic businessLogic = getServiceBL(context);
			Either<Service, ResponseFormat> distResponse = businessLogic.markDistributionAsDeployed(serviceId, did, modifier);

			if (distResponse.isRight()) {
				log.debug("failed to mark distribution as deployed");
				response = buildErrorResponse(distResponse.right().value());
				return response;
			}
			Service service = distResponse.left().value();
			Object result = RepresentationUtils.toRepresentation(service);
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Mark Distribution As Deployed");
			log.debug("mark distribution as deployed failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;
		}
	}

	@POST
	@Path("/services/{serviceId}/tempUrlToBeDeleted")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 500, message = "Internal Server Error. Please try again later.") })
	public Response tempUrlToBeDeleted(@PathParam("serviceId") final String serviceId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;
		try {
			ServiceBusinessLogic businessLogic = getServiceBL(context);
			Service service = (businessLogic.getService(serviceId, modifier)).left().value();
			Either<Service, ResponseFormat> res = (businessLogic.updateDistributionStatusForActivation(service, modifier, DistributionStatusEnum.DISTRIBUTED));

			if (res.isRight()) {
				response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			}
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), null);
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("tempUrlToBeDeleted");
			log.debug("failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;
		}
	}

	@GET
	@Path("/services/toscatoheat/{artifactName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Download service artifact", httpMethod = "GET", notes = "Returns downloaded artifact", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact downloaded"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Artifact not found") })
	public Response downloadServiceArtifact(@PathParam("artifactName") final String artifactName, @Context final HttpServletRequest request) {
		Response response = null;

		try {
			log.debug("artifact name = {}", artifactName);

			Either<byte[], ResponseFormat> executeCommand = executeCommand(artifactName);

			if (executeCommand.isRight()) {
				log.debug("Failed to convert tosca {} to heat", artifactName);
				ResponseFormat responseFormat = executeCommand.right().value();
				response = buildErrorResponse(responseFormat);
			} else {
				log.debug("Succeed to convert tosca {} to heat", artifactName);
				byte[] value = executeCommand.left().value();
				InputStream is = new ByteArrayInputStream(value);

				Map<String, String> headers = new HashMap<>();
				String heatFileName;
				if (artifactName.indexOf(".") > -1) {
					heatFileName = artifactName.substring(0, artifactName.indexOf(".")) + ".heat";
				} else {
					heatFileName = artifactName + ".heat";
				}
				headers.put(Constants.CONTENT_DISPOSITION_HEADER, getContentDispositionValue(heatFileName));
				ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
				response = buildOkResponse(responseFormat, is, headers);
			}
			return response;

		} catch (Exception e) {
			log.error("download artifact failed with exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
	}

	private Either<byte[], ResponseFormat> executeCommand(String artifactName) {

		Configuration configuration = ConfigurationManager.getConfigurationManager().getConfiguration();
		String toscaFilesDir = configuration.getToscaFilesDir();
		if (toscaFilesDir == null) {
			toscaFilesDir = "/apps/jetty/base/be/config/tosca";
		}
		String heatTranslator = configuration.getHeatTranslatorPath();
		if (heatTranslator == null) {
			heatTranslator = "/home/m98835/heat-translator-0.3.0/heat_translator.py";
		}

		log.debug("toscaFilesDir= {} | heatTranslator= {}", toscaFilesDir, heatTranslator);

		StringBuffer output = new StringBuffer();

		String heatHeader = configuration.getHeatEnvArtifactHeader();
		String heatFooter = configuration.getHeatEnvArtifactFooter();

		output.append(heatHeader + "\n");

		MessageFormat mf = new MessageFormat("python {0} --template-file={1}/{2} --template-type=tosca");

		log.debug("After creating message format");

		Object[] objArray = { heatTranslator, toscaFilesDir, artifactName };
		String command = null;
		try {
			command = mf.format(objArray);
		} catch (Exception e) {
			log.debug("Failed to convert message format", e);
		}

		log.debug("Going to run command {}", command);

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			int waitFor = p.waitFor();
			log.debug("waitFor = {}", waitFor);

			if (waitFor != 0) {
				log.error("Failed runnign the command {}", command);
				return Either.right(getComponentsUtils().getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND, artifactName));
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			log.error("Failed runnign the command {}", command, e);
			return Either.right(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}

		output.append(heatFooter);

		return Either.left(output.toString().getBytes());

	}

}
