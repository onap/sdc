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

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1")
@Singleton
public class DistributionServlet extends BeGenericServlet {

	private static Logger log = LoggerFactory.getLogger(DistributionServlet.class.getName());
	@Resource
	private DistributionBusinessLogic distributionLogic;

	@GET
	@Path("/distributionUebCluster")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUebServerList(@Context final HttpServletRequest request, @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) String instanceId, @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization, @HeaderParam(value = Constants.ACCEPT_HEADER) String accept) {
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
	 * Returns list of valid artifact types for validation done in the distribution client.<br>
	 * The list is the representation of the values of the enum ArtifactTypeEnum.
	 * 
	 * @param request
	 * @param instanceId
	 * @param requestId
	 * @param authorization
	 * @param accept
	 * @return
	 */
	@GET
	@Path("/artifactTypes")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getValidArtifactTypes(@Context final HttpServletRequest request, @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) String instanceId, @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization, @HeaderParam(value = Constants.ACCEPT_HEADER) String accept) {
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

	@POST
	@Path("/registerForDistribution")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerForDistribution(@Context final HttpServletRequest request, String requestJson) {
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

	@POST
	@Path("/unRegisterForDistribution")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response unRegisterForDistribution(@Context final HttpServletRequest request, String requestJson) {
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
