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

package org.openecomp.sdc.be.distribution;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.components.distribution.engine.CambriaErrorResponse;
import org.openecomp.sdc.be.components.distribution.engine.CambriaHandler;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngine;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngineInitTask;
import org.openecomp.sdc.be.components.distribution.engine.SubscriberTypeEnum;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.be.distribution.api.client.RegistrationRequest;
import org.openecomp.sdc.be.distribution.api.client.ServerListResponse;
import org.openecomp.sdc.be.distribution.api.client.TopicRegistrationResponse;
import org.openecomp.sdc.be.distribution.api.client.TopicUnregistrationResponse;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fj.data.Either;

@Component("distributionBusinessLogic")
public class DistributionBusinessLogic {
	public static final String REGISTER_IN_DISTRIBUTION_ENGINE = "registerInDistributionEngine";
	public static final String UN_REGISTER_IN_DISTRIBUTION_ENGINE = "unregisterInDistributionEngine";
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static Logger log = LoggerFactory.getLogger(DistributionBusinessLogic.class.getName());
	@Resource
	private DistributionEngine distributionEngine;

	private ResponseFormatManager responseFormatManager = ResponseFormatManager.getInstance();
	private CambriaHandler cambriaHandler;

	public Either<ServerListResponse, ResponseFormat> getUebServerList() {

		DistributionEngineConfiguration distributionEngineConfiguration = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();

		List<String> serverList = distributionEngineConfiguration.getUebServers();

		if (serverList != null && !serverList.isEmpty()) {

			ServerListResponse serverListResponse = new ServerListResponse();

			serverListResponse.setUebServerList(serverList);

			return Either.left(serverListResponse);
		} else {
			ResponseFormat errorResponseWrapper = getResponseFormatManager().getResponseFormat(ActionStatus.GENERAL_ERROR);
			return Either.right(errorResponseWrapper);
		}

	}

	public void handleRegistration(Wrapper<Response> responseWrapper, RegistrationRequest registrationRequest, AuditHandler auditHandler) {
		CambriaErrorResponse registerResponse = null;
		try {
			registerResponse = registerDistributionClientToTopic(responseWrapper, registrationRequest, SubscriberTypeEnum.PRODUCER);
			auditHandler.auditRegisterACL(registerResponse, SubscriberTypeEnum.PRODUCER);

			if (responseWrapper.isEmpty()) {
				registerResponse = registerDistributionClientToTopic(responseWrapper, registrationRequest, SubscriberTypeEnum.CONSUMER);
				auditHandler.auditRegisterACL(registerResponse, SubscriberTypeEnum.CONSUMER);
				// Second Register failed - unregister the first
				if (!responseWrapper.isEmpty()) {
					CambriaErrorResponse unRegisterResponse = unRegisterDistributionClientFromTopic(registrationRequest, SubscriberTypeEnum.PRODUCER);
					auditHandler.auditUnRegisterACL(unRegisterResponse, SubscriberTypeEnum.PRODUCER);
				}
			}

			if (responseWrapper.isEmpty()) {
				TopicRegistrationResponse okTopicResponse = buildTopicResponse(registrationRequest);
				responseWrapper.setInnerElement(Response.status(HttpStatus.SC_OK).entity(okTopicResponse).build());
			}

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDistributionEngineSystemError, REGISTER_IN_DISTRIBUTION_ENGINE, "registration of subscriber to topic");
			BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError(REGISTER_IN_DISTRIBUTION_ENGINE, "registration of subscriber to topic");
			Response errorResponse = buildErrorResponse(getResponseFormatManager().getResponseFormat(ActionStatus.GENERAL_ERROR));
			responseWrapper.setInnerElement(errorResponse);
		} finally {
			auditHandler.auditRegisterRequest(registerResponse);
		}
	}

	public void handleUnRegistration(Wrapper<Response> responseWrapper, RegistrationRequest unRegistrationRequest, AuditHandler auditHandler) {
		Wrapper<CambriaErrorResponse> cambriaResponseWrapper = new Wrapper<>();
		try {
			CambriaErrorResponse unregisterClientProducerTopicResponse = unRegisterDistributionClientFromTopic(unRegistrationRequest, SubscriberTypeEnum.PRODUCER);
			auditHandler.auditUnRegisterACL(unregisterClientProducerTopicResponse, SubscriberTypeEnum.PRODUCER);
			updateResponseWrapper(cambriaResponseWrapper, unregisterClientProducerTopicResponse);

			CambriaErrorResponse unregisterClientConsumerTopicResponse = unRegisterDistributionClientFromTopic(unRegistrationRequest, SubscriberTypeEnum.CONSUMER);
			auditHandler.auditUnRegisterACL(unregisterClientConsumerTopicResponse, SubscriberTypeEnum.CONSUMER);
			updateResponseWrapper(cambriaResponseWrapper, unregisterClientConsumerTopicResponse);

			// Success unregister both topics
			TopicUnregistrationResponse unregisterResponse = new TopicUnregistrationResponse(getNotificationTopicName(unRegistrationRequest.getDistrEnvName()), getStatusTopicName(unRegistrationRequest.getDistrEnvName()),
					unregisterClientConsumerTopicResponse.getOperationStatus(), unregisterClientProducerTopicResponse.getOperationStatus());

			if (cambriaResponseWrapper.getInnerElement().getOperationStatus() == CambriaOperationStatus.OK) {
				responseWrapper.setInnerElement(Response.status(HttpStatus.SC_OK).entity(unregisterResponse).build());
			} else {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDistributionEngineSystemError, UN_REGISTER_IN_DISTRIBUTION_ENGINE, "unregistration failed");
				BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError(UN_REGISTER_IN_DISTRIBUTION_ENGINE, "unregistration failed");
				responseWrapper.setInnerElement(Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(unregisterResponse).build());
			}
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDistributionEngineSystemError, UN_REGISTER_IN_DISTRIBUTION_ENGINE, "unregistration of subscriber to topic");
			Response errorResponse = buildErrorResponse(getResponseFormatManager().getResponseFormat(ActionStatus.GENERAL_ERROR));
			responseWrapper.setInnerElement(errorResponse);

		} finally {
			auditHandler.auditUnRegisterRequest(cambriaResponseWrapper.getInnerElement());
		}
	}

	private void updateResponseWrapper(Wrapper<CambriaErrorResponse> cambriaResponseWrapper, CambriaErrorResponse currentResponse) {
		if (cambriaResponseWrapper.isEmpty()) {
			cambriaResponseWrapper.setInnerElement(currentResponse);
		} else if (currentResponse.getOperationStatus() != CambriaOperationStatus.OK) {
			cambriaResponseWrapper.setInnerElement(currentResponse);

		}

	}

	public static String getNotificationTopicName(String envName) {
		DistributionEngineConfiguration config = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();
		return DistributionEngineInitTask.buildTopicName(config.getDistributionNotifTopicName(), envName);

	}

	public static String getStatusTopicName(String envName) {
		DistributionEngineConfiguration config = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();
		return DistributionEngineInitTask.buildTopicName(config.getDistributionStatusTopicName(), envName);

	}

	protected CambriaErrorResponse unRegisterDistributionClientFromTopic(RegistrationRequest unRegistrationRequest, SubscriberTypeEnum subscriberType) {
		DistributionEngineConfiguration config = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();
		String topicName;
		if (subscriberType == SubscriberTypeEnum.PRODUCER) {
			topicName = getStatusTopicName(unRegistrationRequest.getDistrEnvName());
		} else {
			topicName = getNotificationTopicName(unRegistrationRequest.getDistrEnvName());

		}
		log.debug("unregistering client as {} , from topic: {}", subscriberType.name(), topicName);
		return getCambriaHandler().unRegisterFromTopic(config.getUebServers(), topicName, config.getUebPublicKey(), config.getUebSecretKey(), unRegistrationRequest.getApiPublicKey(), subscriberType);
	}

	private TopicRegistrationResponse buildTopicResponse(RegistrationRequest registrationRequest) {
		DistributionEngineConfiguration config = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();
		String statusTopicName = DistributionEngineInitTask.buildTopicName(config.getDistributionStatusTopicName(), registrationRequest.getDistrEnvName());
		String notificationTopicName = DistributionEngineInitTask.buildTopicName(config.getDistributionNotifTopicName(), registrationRequest.getDistrEnvName());

		TopicRegistrationResponse topicResponse = new TopicRegistrationResponse();
		topicResponse.setDistrNotificationTopicName(notificationTopicName);
		topicResponse.setDistrStatusTopicName(statusTopicName);
		return topicResponse;
	}

	protected CambriaErrorResponse registerDistributionClientToTopic(Wrapper<Response> responseWrapper, RegistrationRequest registrationRequest, SubscriberTypeEnum subscriberType) {
		DistributionEngineConfiguration config = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();
		String topicName, errorMsg;

		// Register for notifications as consumer
		if (subscriberType == SubscriberTypeEnum.CONSUMER) {
			topicName = DistributionEngineInitTask.buildTopicName(config.getDistributionNotifTopicName(), registrationRequest.getDistrEnvName());
			errorMsg = "registration of subscriber to topic:" + topicName + " as consumer failed";
		}
		// Register for status as producer
		else {
			topicName = DistributionEngineInitTask.buildTopicName(config.getDistributionStatusTopicName(), registrationRequest.getDistrEnvName());
			errorMsg = "registration of subscriber to topic:" + topicName + " as producer failed";
		}
		log.debug("registering client as {} , from topic: {}", subscriberType.name(), topicName);
		CambriaErrorResponse registerToTopic = getCambriaHandler().registerToTopic(config.getUebServers(), topicName, config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(), subscriberType);

		if (registerToTopic.getOperationStatus() != CambriaOperationStatus.OK) {
			Response failedRegistrationResponse = buildErrorResponse(getResponseFormatManager().getResponseFormat(ActionStatus.GENERAL_ERROR));
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDistributionEngineSystemError, REGISTER_IN_DISTRIBUTION_ENGINE, errorMsg);
			BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError(REGISTER_IN_DISTRIBUTION_ENGINE, errorMsg);
			responseWrapper.setInnerElement(failedRegistrationResponse);
		}
		return registerToTopic;
	}

	protected Response buildErrorResponse(ResponseFormat requestErrorWrapper) {
		Response response = Response.status(requestErrorWrapper.getStatus()).entity(gson.toJson(requestErrorWrapper.getRequestError())).build();
		return response;
	}

	public ResponseFormatManager getResponseFormatManager() {
		return responseFormatManager;
	}

	public DistributionEngine getDistributionEngine() {
		return distributionEngine;
	}

	public CambriaHandler getCambriaHandler() {
		if (cambriaHandler == null) {
			cambriaHandler = new CambriaHandler();
		}
		return cambriaHandler;
	}

}
