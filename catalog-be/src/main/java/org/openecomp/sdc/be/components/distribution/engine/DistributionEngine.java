/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
 * Modifications copyright (c) 2025 Deutsche Telekom.
 * ================================================================================
 */
package org.openecomp.sdc.be.components.distribution.engine;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.kafka.KafkaCommonConfig;
import org.openecomp.sdc.be.components.validation.ServiceDistributionValidation;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Component("distributionEngine")
public class DistributionEngine implements IDistributionEngine {

    public static final String INIT_DISTRIBUTION_ENGINE_FLOW = "initDistributionEngine";
    private static final Logger logger = Logger.getLogger(DistributionEngine.class.getName());
    @Autowired
    private EnvironmentsEngine environmentsEngine;
    @Resource
    private DistributionNotificationSender distributionNotificationSender;
    @Resource
    private ServiceDistributionArtifactsBuilder serviceDistributionArtifactsBuilder;
    @Resource
    private DistributionEngineClusterHealth distributionEngineClusterHealth;
    @Resource
    private ServiceDistributionValidation serviceDistributionValidation;
    @Resource
    private DistributionDeleteNotificationSender distributionDeleteNotificationSender;
    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    @Autowired
    private KafkaCommonConfig kafkaCommonConfig;

    private Map<String, AtomicBoolean> envNamePerStatus = new HashMap<>();
    private boolean engineEnabledAndValid;

    @Override
    public boolean isActive() {
        if (envNamePerStatus.isEmpty()) {
            return false;
        }
        for (AtomicBoolean status : envNamePerStatus.values()) {
            if (!status.get()) {
                return false;
            }
        }
        return true;
    }

    @PostConstruct
    void init() {
        logger.trace("Enter init method of DistributionEngine");
        DistributionEngineConfiguration distributionEngineConfiguration = ConfigurationManager.getConfigurationManager()
                .getDistributionEngineConfiguration();
        boolean startDistributionEngine = distributionEngineConfiguration.isStartDistributionEngine();
        logger.debug("Distribution engine activation parameter is {}", startDistributionEngine);
        if (!startDistributionEngine) {
            logger.info("The distribution engine is disabled");
            this.distributionEngineClusterHealth.setHealthCheckUebIsDisabled();
            return;
        }
        boolean isValidConfig = validateConfiguration(distributionEngineConfiguration);
        if (!isValidConfig) {
            BeEcompErrorManager.getInstance()
                    .logBeUebSystemError(INIT_DISTRIBUTION_ENGINE_FLOW, "validate distribution configuration in init phase");
            this.distributionEngineClusterHealth.setHealthCheckUebConfigurationError();
            return;
        }
        for (String envName : distributionEngineConfiguration.getEnvironments()) {
            envNamePerStatus.put(envName, new AtomicBoolean(false));
        }
        engineEnabledAndValid = true;
        logger.trace("Exit init method of DistributionEngine");
    }

    /**
     * First start of the status listener. The health task started here keeps restarting it while it is not running.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void startStatusListener() {
        if (!engineEnabledAndValid) {
            return;
        }
        Optional<String> kafkaError = kafkaCommonConfig.configurationError();
        if (kafkaError.isPresent()) {
            BeEcompErrorManager.getInstance().logBeUebSystemError(INIT_DISTRIBUTION_ENGINE_FLOW, kafkaError.get());
            distributionEngineClusterHealth.setHealthCheckUebConfigurationError();
            return;
        }
        refreshStatusListener();
        distributionEngineClusterHealth.startHealthCheckTask(this::refreshStatusListener);
    }

    /**
     * Starts the status listener if it is not running and marks every environment ready iff it is running afterwards.
     *
     * @return whether the status listener is running
     */
    synchronized boolean refreshStatusListener() {
        MessageListenerContainer container = kafkaListenerEndpointRegistry.getListenerContainer(DistributionStatusListener.LISTENER_ID);
        boolean running = container != null && startIfNotRunning(container);
        envNamePerStatus.values().forEach(status -> status.set(running));
        return running;
    }

    private boolean startIfNotRunning(MessageListenerContainer container) {
        if (isRunning(container)) {
            return true;
        }
        try {
            // a consumer that stopped on a fatal error leaves its parent container reporting running, and start() is then a no-op
            if (container.isRunning()) {
                container.stop();
            }
            container.start();
        } catch (RuntimeException e) {
            logger.error("failed to start the distribution status listener", e);
            BeEcompErrorManager.getInstance().logBeUebSystemError(INIT_DISTRIBUTION_ENGINE_FLOW, e.getMessage());
            distributionEngineClusterHealth.setHealthCheckUnavailableAndReportInCaseLastStateIsUp();
        }
        return isRunning(container);
    }

    private static boolean isRunning(MessageListenerContainer container) {
        return container.isRunning() && container.isChildRunning();
    }

    /**
     * validate mandatory configuration parameters received
     *
     * @param deConfiguration: distribution engine configuration
     * @return boolean result: true of false
     */
    protected boolean validateConfiguration(DistributionEngineConfiguration deConfiguration) {
        String methodName = "validateConfiguration";
        boolean result = isValidParam(deConfiguration.getEnvironments(), methodName, "environments");
        result = isValidParam(deConfiguration.getDistributionNotifTopicName(), methodName, "distributionNotifTopicName") && result;
        result = isValidParam(deConfiguration.getDistributionStatusTopicName(), methodName, "distributionStatusTopicName") && result;
        result = isValidObject(deConfiguration.getDistributionStatusTopic(), methodName, "distributionStatusTopic") && result;
        result = isValidParam(deConfiguration.getDistributionDeleteTopicName(), methodName,
                "distributionDeleteTopicName") && result;
        result = isValidParam(deConfiguration.getDistributionStatusTopic().getConsumerId(), methodName, "consumerId") && result;
        result = isValidParam(deConfiguration.getDistributionStatusTopic().getConsumerGroup(), methodName, "consumerGroup") && result;
        result = isValidObject(deConfiguration.getDistributionStatusTopic().getFetchTimeSec(), methodName, "fetchTimeSec") && result;
        result = isValidObject(deConfiguration.getDistributionStatusTopic().getPollingIntervalSec(), methodName, "pollingIntervalSec") && result;
        return result;
    }

    private boolean isValidParam(String paramValue, String methodName, String paramName) {
        if (StringUtils.isEmpty(paramValue)) {
            BeEcompErrorManager.getInstance().logBeMissingConfigurationError(methodName, paramName);
            return false;
        }
        return true;
    }

    private boolean isValidParam(List<String> paramValue, String methodName, String paramName) {
        if (CollectionUtils.isEmpty(paramValue)) {
            BeEcompErrorManager.getInstance().logBeMissingConfigurationError(methodName, paramName);
            return false;
        }
        return true;
    }

    private boolean isValidObject(Object paramValue, String methodName, String paramName) {
        if (paramValue == null) {
            BeEcompErrorManager.getInstance().logBeMissingConfigurationError(methodName, paramName);
            return false;
        }
        return true;
    }

    private String getEnvironmentErrorDescription(StorageOperationStatus status) {
        switch (status) {
            case DISTR_ENVIRONMENT_NOT_AVAILABLE:
                return "environment is unavailable";
            case DISTR_ENVIRONMENT_NOT_FOUND:
                return "environment is not configured in our system";
            case DISTR_ENVIRONMENT_SENT_IS_INVALID:
                return "environment name is invalid";
            default:
                return "unkhown";
        }
    }

    @Override
    public StorageOperationStatus isEnvironmentAvailable(String envName) {
        if (envName == null || envName.isEmpty()) {
            return StorageOperationStatus.DISTR_ENVIRONMENT_SENT_IS_INVALID;
        }
        AtomicBoolean status = envNamePerStatus.get(envName);
        if (status == null) {
            return StorageOperationStatus.DISTR_ENVIRONMENT_NOT_FOUND;
        }
        if (!status.get()) {
            return StorageOperationStatus.DISTR_ENVIRONMENT_NOT_AVAILABLE;
        }
        return StorageOperationStatus.OK;
    }

    @Override
    public StorageOperationStatus isEnvironmentAvailable() {
        String envName = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().getEnvironments().get(0);
        return isEnvironmentAvailable(envName);
    }

    @Override
    public void disableEnvironment(String envName) {
        AtomicBoolean status = envNamePerStatus.get(envName);
        status.set(false);
    }

    @Override
    public ActionStatus notifyService(String distributionId, Service service, INotificationData notificationData, String envName, User modifier) {
        return notifyService(distributionId, service, notificationData, envName, envName, modifier);
    }

    @Override
    public ActionStatus notifyService(String distributionId, Service service, INotificationData notificationData, String envId, String envName,
                                      User modifier) {
        logger.debug(
                "Received notify service request. distributionId = {}, serviceUuid = {} serviceUid = {}, envName = {}, userId = {}, modifierName {}",
                distributionId, service.getUUID(), service.getUniqueId(), envName, service.getLastUpdaterUserId(), modifier);
        String topicName = buildTopicName(envName);
        ActionStatus notifyServiceStatus = Optional.ofNullable(environmentsEngine.getEnvironmentById(envId)).map(EnvironmentMessageBusData::new).map(
                        messageBusData -> distributionNotificationSender
                                .sendNotification(topicName, distributionId, messageBusData, notificationData, service, modifier))
                .orElse(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_AVAILABLE);
        logger.debug("Finish notifyService. status is {}", notifyServiceStatus);
        return notifyServiceStatus;
    }

    private String buildTopicName(String envName) {
        DistributionEngineConfiguration deConfiguration = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();
        String distributionNotifTopicName = deConfiguration.getDistributionNotifTopicName();
        return DistributionTopics.buildTopicName(distributionNotifTopicName, envName);
    }

    @Override
    public StorageOperationStatus isReadyForDistribution(String envName) {
        StorageOperationStatus status = isEnvironmentAvailable(envName);
        if (status != StorageOperationStatus.OK) {
            String envErrorDec = getEnvironmentErrorDescription(status);
            BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError(DistributionNotificationSender.DISTRIBUTION_NOTIFICATION_SENDING,
                    "Environment name " + envName + " is not available. Reason : " + envErrorDec);
        }
        return status;
    }

    @Override
    public OperationalEnvironmentEntry getEnvironmentById(String opEnvId) {
        return environmentsEngine.getEnvironmentById(opEnvId);
    }

    @Override
    public INotificationData buildServiceForDistribution(Service service, String distributionId, String workloadContext) {
        INotificationData value = serviceDistributionArtifactsBuilder.buildResourceInstanceForDistribution(service, distributionId, workloadContext);
        value = serviceDistributionArtifactsBuilder.buildServiceForDistribution(value, service);
        return value;
    }

    /**
     * This method is used to build the notification payload for delete service
     * 
     * @param service        service to be deleted
     * @param distributionId random UUID generated for the notification
     * @return notification payload of INotificationData type.
     */
    @Override
    public INotificationData buildServiceForDeleteNotification(Service service, String distributionId) {
        INotificationData value = serviceDistributionArtifactsBuilder.buildResourceInstanceForDistribution(service,
                distributionId, null);
        logger.debug("Payload for delete kafka notification is -  {}", value);
        return value;
    }

    /**
     * This method is used to notify the service for delete operation.
     * 
     * @param distributionId   random UUID generated for the notification
     * @param notificationData the payload data to be sent in the notification
     * @param service          the service to be deleted
     * @param envName          the environment name
     * @param user             the user performing the delete operation
     * @return the action status of the notification.
     */
    @Override
    public ActionStatus notifyServiceForDelete(String distributionId, INotificationData notificationData,
            Service service,
            String envName, User user) {
        logger.debug(
                "Received notify service request for delete. distributionId = {}, serviceUuid = {} serviceUid = {}, envName = {}, userId = {}, modifierName {}",
                distributionId, service.getUUID(), service.getUniqueId(), envName, service.getLastUpdaterUserId(),
                user);

        String topicName = buildDeleteNotificationTopicName(envName);
        logger.debug("topic name for delete - {}", topicName);

        ActionStatus deleteServiceNotifStatus = Optional
                .ofNullable(environmentsEngine.getEnvironmentById(envName)).map(
                        environment -> distributionDeleteNotificationSender
                                .sendNotificationForDeleteService(topicName, distributionId, notificationData))
                .orElse(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_AVAILABLE);
        logger.debug("Finish delete notification service. notification status is {}", deleteServiceNotifStatus);
        return deleteServiceNotifStatus;
    }

    /**
     * This method is used to create topic name for delete operation based on
     * environment name.
     * 
     * @param envName the environment name
     * @return the delete topic name
     */
    private String buildDeleteNotificationTopicName(String envName) {
        String deleteTopicName = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration()
                .getDistributionDeleteTopicName();
        return DistributionTopics.buildTopicName(deleteTopicName, envName);
    }
}
