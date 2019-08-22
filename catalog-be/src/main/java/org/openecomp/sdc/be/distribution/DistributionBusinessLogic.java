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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fj.data.Either;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.components.distribution.engine.*;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.distribution.api.client.*;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.apache.commons.lang.BooleanUtils.isTrue;
import static org.openecomp.sdc.be.components.distribution.engine.DistributionEngineInitTask.buildTopicName;
import static org.openecomp.sdc.be.config.ConfigurationManager.getConfigurationManager;

@Component("distributionBusinessLogic")
public class DistributionBusinessLogic {
    public static final String REGISTER_IN_DISTRIBUTION_ENGINE = "registerInDistributionEngine";
    public static final String UN_REGISTER_IN_DISTRIBUTION_ENGINE = "unregisterInDistributionEngine";
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger log = Logger.getLogger(DistributionBusinessLogic.class);
    @Resource
    private IDistributionEngine distributionEngine;

    private ResponseFormatManager responseFormatManager = ResponseFormatManager.getInstance();
    private CambriaHandler cambriaHandler;

    private void initRequestEnvEndPoints(RegistrationRequest registrationRequest, DistributionEngineConfiguration config) {
        if(registrationRequest.getDistEnvEndPoints() == null || registrationRequest.getDistEnvEndPoints().isEmpty()){
            registrationRequest.setDistEnvEndPoints(config.getUebServers());
        }
    }
    public Either<ServerListResponse, ResponseFormat> getUebServerList() {

        DistributionEngineConfiguration distributionEngineConfiguration = ConfigurationManager.getConfigurationManager()
                .getDistributionEngineConfiguration();

        List<String> serverList = distributionEngineConfiguration.getUebServers();

        if (serverList != null && !serverList.isEmpty()) {

            ServerListResponse serverListResponse = new ServerListResponse();

            serverListResponse.setUebServerList(serverList);

            return Either.left(serverListResponse);
        } else {
            ResponseFormat errorResponseWrapper = getResponseFormatManager()
                    .getResponseFormat(ActionStatus.GENERAL_ERROR);
            return Either.right(errorResponseWrapper);
        }

    }

    public void handleRegistration(Wrapper<Response> responseWrapper, RegistrationRequest registrationRequest,
            AuditHandler auditHandler) {
        CambriaErrorResponse registerResponse = null;
        try {
            DistributionEngineConfiguration config = getConfigurationManager().getDistributionEngineConfiguration();
            String statusTopicName = buildTopicName(config.getDistributionStatusTopicName(),
                    registrationRequest.getDistrEnvName());
            registerResponse = registerDistributionClientToTopic(responseWrapper, registrationRequest,
                    SubscriberTypeEnum.PRODUCER, statusTopicName);

            auditHandler.auditRegisterACL(registerResponse, SubscriberTypeEnum.PRODUCER,
                    DistributionTopicData.newBuilder()
                        .statusTopic(statusTopicName)
                        .build());
            boolean isRegisteredAsProducerOnStatusSuccess = responseWrapper.isEmpty();

            // Story [347698] Distribution Client Get Indication from
            // component whether to register as consumer and producer on
            // status topic
            boolean registeredAsConsumerOnStatus = false;
            if (isRegisteredAsProducerOnStatusSuccess && isTrue(registrationRequest.getIsConsumerToSdcDistrStatusTopic())) {
                registerResponse = registerDistributionClientToTopic(responseWrapper, registrationRequest,
                        SubscriberTypeEnum.CONSUMER, statusTopicName);
                auditHandler.auditRegisterACL(registerResponse, SubscriberTypeEnum.CONSUMER,
                        DistributionTopicData.newBuilder()
                                .statusTopic(statusTopicName)
                                .build());
                registeredAsConsumerOnStatus = responseWrapper.isEmpty();

            }

            if (responseWrapper.isEmpty()) {
                String notificationTopicName = buildTopicName(config.getDistributionNotifTopicName(),
                        registrationRequest.getDistrEnvName());
                registerResponse = registerDistributionClientToTopic(responseWrapper, registrationRequest,
                        SubscriberTypeEnum.CONSUMER, notificationTopicName);
                auditHandler.auditRegisterACL(registerResponse, SubscriberTypeEnum.CONSUMER,
                        DistributionTopicData.newBuilder()
                            .notificationTopic(notificationTopicName)
                            .build());
            }
            // Unregister Rollback
            if (!responseWrapper.isEmpty()) {
                if (isRegisteredAsProducerOnStatusSuccess) {
                    CambriaErrorResponse unRegisterResponse = unRegisterDistributionClientFromTopic(registrationRequest,
                            SubscriberTypeEnum.PRODUCER, statusTopicName);
                    auditHandler.auditUnRegisterACL(unRegisterResponse, SubscriberTypeEnum.PRODUCER,
                            DistributionTopicData.newBuilder()
                                    .statusTopic(statusTopicName)
                                    .build());
                }
                if (registeredAsConsumerOnStatus) {
                    CambriaErrorResponse unRegisterResponse = unRegisterDistributionClientFromTopic(registrationRequest,
                            SubscriberTypeEnum.CONSUMER, statusTopicName);
                    auditHandler.auditUnRegisterACL(unRegisterResponse, SubscriberTypeEnum.CONSUMER,
                            DistributionTopicData.newBuilder()
                            .statusTopic(statusTopicName)
                            .build());
                }
            }

            if (responseWrapper.isEmpty()) {
                TopicRegistrationResponse okTopicResponse = buildTopicResponse(registrationRequest);
                responseWrapper.setInnerElement(Response.status(HttpStatus.SC_OK).entity(okTopicResponse).build());
            }

        } catch (Exception e) {
            log.error("registration to topic failed", e);
            BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError(REGISTER_IN_DISTRIBUTION_ENGINE,
                    "registration of subscriber to topic");
            Response errorResponse = buildErrorResponse(
                    getResponseFormatManager().getResponseFormat(ActionStatus.GENERAL_ERROR));
            responseWrapper.setInnerElement(errorResponse);
        } finally {
            auditHandler.auditRegisterRequest(registerResponse);
        }
    }

    public void handleUnRegistration(Wrapper<Response> responseWrapper, RegistrationRequest unRegistrationRequest,
            AuditHandler auditHandler) {
        Wrapper<CambriaErrorResponse> cambriaResponseWrapper = new Wrapper<>();
        try {
            String statusTopicName = getStatusTopicName(unRegistrationRequest.getDistrEnvName());
            CambriaErrorResponse unregisterClientProducerTopicResponse = unRegisterDistributionClientFromTopic(
                    unRegistrationRequest, SubscriberTypeEnum.PRODUCER, statusTopicName);
            auditHandler.auditUnRegisterACL(unregisterClientProducerTopicResponse, SubscriberTypeEnum.PRODUCER,
                    DistributionTopicData.newBuilder()
                            .statusTopic(statusTopicName)
                            .build());
            updateResponseWrapper(cambriaResponseWrapper, unregisterClientProducerTopicResponse);

            String notificationTopicName = getNotificationTopicName(unRegistrationRequest.getDistrEnvName());
            CambriaErrorResponse unregisterClientConsumerTopicResponse = unRegisterDistributionClientFromTopic(
                    unRegistrationRequest, SubscriberTypeEnum.CONSUMER, notificationTopicName);
            auditHandler.auditUnRegisterACL(unregisterClientConsumerTopicResponse, SubscriberTypeEnum.CONSUMER,
                    DistributionTopicData.newBuilder()
                            .notificationTopic(notificationTopicName)
                            .build());
            updateResponseWrapper(cambriaResponseWrapper, unregisterClientConsumerTopicResponse);

            // Success unregister both topics
            TopicUnregistrationResponse unregisterResponse = new TopicUnregistrationResponse(
                    getNotificationTopicName(unRegistrationRequest.getDistrEnvName()),
                    getStatusTopicName(unRegistrationRequest.getDistrEnvName()),
                    unregisterClientConsumerTopicResponse.getOperationStatus(),
                    unregisterClientProducerTopicResponse.getOperationStatus());

            if (cambriaResponseWrapper.getInnerElement().getOperationStatus() == CambriaOperationStatus.OK) {
                responseWrapper.setInnerElement(Response.status(HttpStatus.SC_OK).entity(unregisterResponse).build());
            } else {
                BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError(UN_REGISTER_IN_DISTRIBUTION_ENGINE,
                        "unregistration failed");
                responseWrapper.setInnerElement(
                        Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(unregisterResponse).build());
            }
        } catch (Exception e) {
            log.error("unregistered to topic failed", e);
            Response errorResponse = buildErrorResponse(
                    getResponseFormatManager().getResponseFormat(ActionStatus.GENERAL_ERROR));
            responseWrapper.setInnerElement(errorResponse);

        } finally {
            auditHandler.auditUnRegisterRequest(cambriaResponseWrapper.getInnerElement());
        }
    }

    private void updateResponseWrapper(Wrapper<CambriaErrorResponse> cambriaResponseWrapper,
            CambriaErrorResponse currentResponse) {
        if (cambriaResponseWrapper.isEmpty()) {
            cambriaResponseWrapper.setInnerElement(currentResponse);
        } else if (currentResponse.getOperationStatus() != CambriaOperationStatus.OK) {
            cambriaResponseWrapper.setInnerElement(currentResponse);

        }

    }

    public static String getNotificationTopicName(String envName) {
        DistributionEngineConfiguration config = ConfigurationManager.getConfigurationManager()
                .getDistributionEngineConfiguration();
        return DistributionEngineInitTask.buildTopicName(config.getDistributionNotifTopicName(), envName);

    }

    public static String getStatusTopicName(String envName) {
        DistributionEngineConfiguration config = ConfigurationManager.getConfigurationManager()
                .getDistributionEngineConfiguration();
        return DistributionEngineInitTask.buildTopicName(config.getDistributionStatusTopicName(), envName);

    }

    protected CambriaErrorResponse unRegisterDistributionClientFromTopic(RegistrationRequest unRegistrationRequest,
            SubscriberTypeEnum subscriberType, String topicName) {
        DistributionEngineConfiguration config = ConfigurationManager.getConfigurationManager()
                .getDistributionEngineConfiguration();
        initRequestEnvEndPoints(unRegistrationRequest, config);

        log.debug("unregistering client as {} , from topic: {}, using DistEnvPoints: {}", subscriberType, topicName, unRegistrationRequest.getDistEnvEndPoints());
        return getCambriaHandler().unRegisterFromTopic(unRegistrationRequest.getDistEnvEndPoints(), config.getUebPublicKey(),
                config.getUebSecretKey(), unRegistrationRequest.getApiPublicKey(), subscriberType, topicName);
    }

    private TopicRegistrationResponse buildTopicResponse(RegistrationRequest registrationRequest) {
        DistributionEngineConfiguration config = ConfigurationManager.getConfigurationManager()
                .getDistributionEngineConfiguration();
        String statusTopicName = DistributionEngineInitTask.buildTopicName(config.getDistributionStatusTopicName(),
                registrationRequest.getDistrEnvName());
        String notificationTopicName = DistributionEngineInitTask.buildTopicName(config.getDistributionNotifTopicName(),
                registrationRequest.getDistrEnvName());

        TopicRegistrationResponse topicResponse = new TopicRegistrationResponse();
        topicResponse.setDistrNotificationTopicName(notificationTopicName);
        topicResponse.setDistrStatusTopicName(statusTopicName);
        return topicResponse;
    }

    protected CambriaErrorResponse registerDistributionClientToTopic(Wrapper<Response> responseWrapper,
            RegistrationRequest registrationRequest, SubscriberTypeEnum subscriberType, String topicName) {
        DistributionEngineConfiguration config = ConfigurationManager.getConfigurationManager()
                .getDistributionEngineConfiguration();
        initRequestEnvEndPoints(registrationRequest, config);
        String errorMsg;

        // Register for notifications as consumer
        if (subscriberType == SubscriberTypeEnum.CONSUMER) {
            errorMsg = "registration of subscriber to topic:" + topicName + " as consumer failed";
        }
        // Register for status as producer
        else {
            errorMsg = "registration of subscriber to topic:" + topicName + " as producer failed";
        }
        log.debug("registering client as {} , from topic: {}, using DistEnvPoints: {}", subscriberType, topicName, registrationRequest.getDistEnvEndPoints());
        CambriaErrorResponse registerToTopic = getCambriaHandler().registerToTopic(registrationRequest.getDistEnvEndPoints(),
                config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(),
                subscriberType, topicName);

        if (registerToTopic.getOperationStatus() != CambriaOperationStatus.OK) {
            Response failedRegistrationResponse = buildErrorResponse(
                    getResponseFormatManager().getResponseFormat(ActionStatus.GENERAL_ERROR));
            BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError(REGISTER_IN_DISTRIBUTION_ENGINE,
                    errorMsg);
            responseWrapper.setInnerElement(failedRegistrationResponse);
        }
        return registerToTopic;
    }

    protected Response buildErrorResponse(ResponseFormat requestErrorWrapper) {
        return Response.status(requestErrorWrapper.getStatus())
                .entity(gson.toJson(requestErrorWrapper.getRequestError())).build();
    }

    public ResponseFormatManager getResponseFormatManager() {
        return responseFormatManager;
    }

    public IDistributionEngine getDistributionEngine() {
        return distributionEngine;
    }

    public CambriaHandler getCambriaHandler() {
        if (cambriaHandler == null) {
            cambriaHandler = new CambriaHandler();
        }
        return cambriaHandler;
    }

}
