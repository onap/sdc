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

package org.openecomp.sdc.be.distribution.servlet;

import javax.annotation.Resource;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.distribution.AuditHandler;
import org.openecomp.sdc.be.distribution.DistributionBusinessLogic;
import org.openecomp.sdc.be.distribution.api.client.RegistrationRequest;
import org.openecomp.sdc.be.distribution.api.client.ServerListResponse;
import org.openecomp.sdc.be.distribution.api.client.TopicRegistrationResponse;
import org.openecomp.sdc.be.distribution.api.client.TopicUnregistrationResponse;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.BeGenericServlet;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.HttpUtil;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.jcabi.aspects.Loggable;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

/**
 * This Servlet serves external users for distribution purposes.
 * 
 * @author tgitelman
 *
 */

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1")
@Api(value = "Distribution Servlet", description = "This Servlet serves external users for distribution purposes.")
@Singleton
public class DistributionServlet extends BeGenericServlet {

	private static Logger log = LoggerFactory.getLogger(DistributionServlet.class.getName());
	@Resource
	private DistributionBusinessLogic distributionLogic;
	@Context
	private HttpServletRequest request;
	
	/**
	 * 
	 * @param requestId
	 * @param instanceId
	 * @param accept
	 * @param authorization
	 * @return
	 */
	@GET
	@Path("/distributionUebCluster")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "UEB Server List", httpMethod = "GET", notes = "return the available UEB Server List",
	//TODO Tal G fix response headers
	responseHeaders = {
			@ResponseHeader(name = Constants.CONTENT_TYPE_HEADER, description = "Determines the format of the response body", response = String.class), 
			@ResponseHeader(name = "Content-Length", description = "Length of  the response body", response = String.class)})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "ECOMP component is authenticated and list of Cambria API server’s FQDNs is returned", response = ServerListResponse.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its credentials  for  Basic Authentication - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 405, message = "Method  Not Allowed: Invalid HTTP method type used ( PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000")})
	public Response getUebServerList( 
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) String instanceId, 
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization) {
		
		init(request);
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);
		Response response = null;
		ResponseFormat responseFormat = null;
		
		if (instanceId == null) {
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			response = buildErrorResponse(responseFormat);
			getComponentsUtils().auditMissingInstanceId(AuditingActionEnum.GET_UEB_CLUSTER, responseFormat.getStatus().toString(), responseFormat.getFormattedMessage());
			return response;
		}
		
		try {
			Either<ServerListResponse, ResponseFormat> actionResponse = distributionLogic.getUebServerList();

			if (actionResponse.isRight()) {
				responseFormat = actionResponse.right().value();
				response = buildErrorResponse(responseFormat);
			} else {
				responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
				response = buildOkResponse(responseFormat, actionResponse.left().value());
			}

			getComponentsUtils().auditGetUebCluster(AuditingActionEnum.GET_UEB_CLUSTER, instanceId, null, responseFormat.getStatus().toString(), responseFormat.getFormattedMessage());
			return response;

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "failed to get ueb serbver list from cofiguration");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("failed to get ueb serbver list from cofiguration");
			log.debug("failed to get ueb serbver list from cofiguration", e);
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			getComponentsUtils().auditGetUebCluster(AuditingActionEnum.GET_UEB_CLUSTER, instanceId, null, responseFormat.getStatus().toString(), responseFormat.getFormattedMessage());
			response = buildErrorResponse(responseFormat);
			return response;
		}

	}
	
	/**
	 * 
	 * @param requestId
	 * @param instanceId
	 * @param accept
	 * @param contenType
	 * @param contenLength
	 * @param authorization
	 * @param requestJson
	 * @return
	 */
	@POST
	@Path("/registerForDistribution")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Subscription status", httpMethod = "POST", notes = "Subscribes for distribution notifications")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "ECOMP component is successfully registered for distribution", response = TopicRegistrationResponse.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 400, message = "Missing  Body - POL4500"),
			@ApiResponse(code = 400, message = "Invalid  Body  : missing mandatory parameter 'apiPublicKey' - POL4501"),
			@ApiResponse(code = 400, message = "Invalid  Body  : missing mandatory parameter 'distrEnvName' - POL4502"),
			@ApiResponse(code = 400, message = "Invalid Body :  Specified 'distrEnvName' doesn’t exist - POL4137"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 405, message = "Method  Not Allowed  :  Invalid HTTP method type used to  register for  distribution ( PUT,DELETE,GET  will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The registration failed due to internal SDC problem or Cambria Service failure ECOMP Component  should  continue the attempts to  register for  distribution - POL5000")})
	//TODO Tal G fix response headers and to check missing header validations with Michael L
			@ApiImplicitParam(required = true, dataType = "org.openecomp.sdc.be.distribution.api.client.RegistrationRequest", paramType = "body", value = "json describe the artifact")
	public Response registerForDistribution(
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) String instanceId, 
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "Determines the format of the body of the request", required = true)@HeaderParam(value = Constants.CONTENT_TYPE_HEADER) String contenType,
			@ApiParam(value = "Length  of  the request body", required = true)@HeaderParam(value = Constants.CONTENT_LENGTH_HEADER) String contenLength,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			String requestJson) {
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);
		init(request);

		Wrapper<Response> responseWrapper = new Wrapper<>();
		Wrapper<RegistrationRequest> registrationRequestWrapper = new Wrapper<>();

		validateHeaders(responseWrapper, request, AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL);

		if (responseWrapper.isEmpty()) {
			validateJson(responseWrapper, registrationRequestWrapper, requestJson);
		}
		if (responseWrapper.isEmpty()) {
			validateEnv(responseWrapper, registrationRequestWrapper.getInnerElement().getDistrEnvName());
		}

		if (responseWrapper.isEmpty()) {
			distributionLogic.handleRegistration(responseWrapper, registrationRequestWrapper.getInnerElement(), buildAuditHandler(request, registrationRequestWrapper.getInnerElement()));
		} else {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDistributionEngineSystemError, DistributionBusinessLogic.REGISTER_IN_DISTRIBUTION_ENGINE, "registration validation failed");
			BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError(DistributionBusinessLogic.REGISTER_IN_DISTRIBUTION_ENGINE, "registration validation failed");
		}

		return responseWrapper.getInnerElement();
	}
	
	/**
	 * Returns list of valid artifact types for validation done in the distribution client.<br>
	 * The list is the representation of the values of the enum ArtifactTypeEnum.
	 * 
	 * @param requestId
	 * @param instanceId
	 * @param authorization
	 * @param accept
	 * @return
	 */
	@GET
	@Path("/artifactTypes")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Artifact types list", httpMethod = "GET", notes = "Fetches available artifact types list")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Artifact types list fetched successfully", response = String.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 405, message = "Method  Not Allowed  :  Invalid HTTP method type used to  register for  distribution ( POST,PUT,DELETE  will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The registration failed due to internal SDC problem or Cambria Service failure ECOMP Component  should  continue the attempts to  register for  distribution - POL5000")})
	public Response getValidArtifactTypes(
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) String instanceId, 
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization, 
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept) {
		init(request);
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);
		Response response = null;

		Wrapper<Response> responseWrapper = new Wrapper<>();

		validateHeaders(responseWrapper, request, AuditingActionEnum.GET_VALID_ARTIFACT_TYPES);
		if (responseWrapper.isEmpty()) {
			response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), ArtifactTypeEnum.values());
		} else {
			response = responseWrapper.getInnerElement();
		}
		return response;
	}
	
	/**
	 * Removes from subscription for distribution notifications
	 * 
	 * @param requestId
	 * @param instanceId
	 * @param accept
	 * @param contenType
	 * @param contenLength
	 * @param authorization
	 * @param requestJson
	 * @return
	 */
	@POST
	@Path("/unRegisterForDistribution")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Subscription status", httpMethod = "POST", notes = "Removes from subscription for distribution notifications")
	//TODO Edit the responses
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "ECOMP component is successfully unregistered", response = TopicUnregistrationResponse.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 400, message = "Missing  Body - POL4500"),
			@ApiResponse(code = 400, message = "Invalid  Body  : missing mandatory parameter 'apiPublicKey' - POL4501"),
			@ApiResponse(code = 400, message = "Invalid  Body  : missing mandatory parameter 'distrEnvName' - SVC4506"),
			@ApiResponse(code = 400, message = "Invalid Body :  Specified 'distrEnvName' doesn’t exist - POL4137"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 405, message = "Method  Not Allowed  :  Invalid HTTP method type used to  register for  distribution ( PUT,DELETE,GET will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The registration failed due to internal SDC problem or Cambria Service failure ECOMP Component  should  continue the attempts to  register for  distribution - POL5000")})
			@ApiImplicitParam(required = true, dataType = "org.openecomp.sdc.be.distribution.api.client.RegistrationRequest", paramType = "body", value = "json describe the artifact")
	public Response unRegisterForDistribution( 
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) String instanceId, 
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "Determines the format of the body of the request", required = true)@HeaderParam(value = Constants.CONTENT_TYPE_HEADER) String contenType,
			@ApiParam(value = "Length  of  the request body", required = true)@HeaderParam(value = Constants.CONTENT_LENGTH_HEADER) String contenLength,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			String requestJson) {
		
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);
		init(request);

		Wrapper<Response> responseWrapper = new Wrapper<>();
		Wrapper<RegistrationRequest> unRegistrationRequestWrapper = new Wrapper<>();

		validateHeaders(responseWrapper, request, AuditingActionEnum.REMOVE_KEY_FROM_TOPIC_ACL);

		if (responseWrapper.isEmpty()) {
			validateJson(responseWrapper, unRegistrationRequestWrapper, requestJson);
		}
		if (responseWrapper.isEmpty()) {
			validateEnv(responseWrapper, unRegistrationRequestWrapper.getInnerElement().getDistrEnvName());
		}
		if (responseWrapper.isEmpty()) {
			distributionLogic.handleUnRegistration(responseWrapper, unRegistrationRequestWrapper.getInnerElement(), buildAuditHandler(request, unRegistrationRequestWrapper.getInnerElement()));
		} else {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDistributionEngineSystemError, DistributionBusinessLogic.UN_REGISTER_IN_DISTRIBUTION_ENGINE, "unregistration validation failed");
			BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError(DistributionBusinessLogic.UN_REGISTER_IN_DISTRIBUTION_ENGINE, "unregistration validation failed");
		}

		return responseWrapper.getInnerElement();
	}

	private void validateEnv(Wrapper<Response> responseWrapper, String distrEnvName) {

		// DE194021
		StorageOperationStatus environmentStatus = distributionLogic.getDistributionEngine().isEnvironmentAvailable();
		// DE194021
		// StorageOperationStatus environmentStatus =
		// distributionLogic.getDistributionEngine().isEnvironmentAvailable(distrEnvName);
		if (environmentStatus != StorageOperationStatus.OK) {
			if (environmentStatus == StorageOperationStatus.DISTR_ENVIRONMENT_NOT_FOUND) {
				Response missingHeaderResponse = buildErrorResponse(distributionLogic.getResponseFormatManager().getResponseFormat(ActionStatus.DISTRIBUTION_ENV_DOES_NOT_EXIST));
				responseWrapper.setInnerElement(missingHeaderResponse);
			} else {
				Response missingHeaderResponse = buildErrorResponse(distributionLogic.getResponseFormatManager().getResponseFormat(ActionStatus.GENERAL_ERROR));
				responseWrapper.setInnerElement(missingHeaderResponse);
			}
		}

	}

	private void init(HttpServletRequest request) {
		if (distributionLogic == null) {
			distributionLogic = getDistributionBL(request.getSession().getServletContext());
		}
	}

	private void validateHeaders(Wrapper<Response> responseWrapper, HttpServletRequest request, AuditingActionEnum auditingAction) {
		if (request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER) == null) {
			Response missingHeaderResponse = buildErrorResponse(distributionLogic.getResponseFormatManager().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID));
			responseWrapper.setInnerElement(missingHeaderResponse);
			// Audit
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditMissingInstanceId(auditingAction, responseFormat.getStatus().toString(), responseFormat.getFormattedMessage());

		}

	}

	private void validateJson(Wrapper<Response> responseWrapper, Wrapper<RegistrationRequest> registrationRequestWrapper, String requestJson) {
		if (requestJson == null || requestJson.isEmpty()) {
			Response missingBodyResponse = buildErrorResponse(distributionLogic.getResponseFormatManager().getResponseFormat(ActionStatus.MISSING_BODY));
			responseWrapper.setInnerElement(missingBodyResponse);
		} else {
			Either<RegistrationRequest, Exception> eitherRegistration = HttpUtil.convertJsonStringToObject(requestJson, RegistrationRequest.class);
			if (eitherRegistration.isLeft()) {
				RegistrationRequest registrationRequest = eitherRegistration.left().value();
				if (registrationRequest.getApiPublicKey() == null) {
					Response missingBodyResponse = buildErrorResponse(distributionLogic.getResponseFormatManager().getResponseFormat(ActionStatus.MISSING_PUBLIC_KEY));
					responseWrapper.setInnerElement(missingBodyResponse);

				} else if (registrationRequest.getDistrEnvName() == null) {
					Response missingBodyResponse = buildErrorResponse(distributionLogic.getResponseFormatManager().getResponseFormat(ActionStatus.MISSING_ENV_NAME));
					responseWrapper.setInnerElement(missingBodyResponse);
				} else {
					registrationRequestWrapper.setInnerElement(registrationRequest);
				}
			} else {
				Response missingBodyResponse = buildErrorResponse(distributionLogic.getResponseFormatManager().getResponseFormat(ActionStatus.MISSING_BODY));
				responseWrapper.setInnerElement(missingBodyResponse);
			}
		}

	}

	private DistributionBusinessLogic getDistributionBL(ServletContext context) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		return webApplicationContext.getBean(DistributionBusinessLogic.class);
	}

	private AuditHandler buildAuditHandler(HttpServletRequest request, RegistrationRequest registrationRequest) {
		return new AuditHandler(getComponentsUtils(), request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER), registrationRequest);
	}
}
