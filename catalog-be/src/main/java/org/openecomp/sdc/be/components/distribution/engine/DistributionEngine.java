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
package org.openecomp.sdc.be.components.distribution.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.stereotype.Component;

@Component("distributionEngine")
public class DistributionEngine implements IDistributionEngine {

    private static final Logger logger = Logger.getLogger(DistributionEngine.class.getName());
    private static final Pattern FQDN_PATTERN = Pattern.compile(
        "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*(:[0-9]{2,4})*$",
        Pattern.CASE_INSENSITIVE);
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
    private Map<String, DistributionEngineInitTask> envNamePerInitTask = new HashMap<>();
    private Map<String, DistributionEnginePollingTask> envNamePerPollingTask = new HashMap<>();
    private Map<String, AtomicBoolean> envNamePerStatus = new HashMap<>();

    @Override
    public boolean isActive() {
        if (envNamePerInitTask.isEmpty()) {
            return false;
        }
        for (DistributionEngineInitTask task : envNamePerInitTask.values()) {
            boolean active = task.isActive();
            if (!active) {
                return false;
            }
        }
        return true;
    }

    @PostConstruct
    private void init() {
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
                .logBeUebSystemError(DistributionEngineInitTask.INIT_DISTRIBUTION_ENGINE_FLOW, "validate distribution configuration in init phase");
            this.distributionEngineClusterHealth.setHealthCheckUebConfigurationError();
            return;
        }
        List<String> environments = distributionEngineConfiguration.getEnvironments();
        for (String envName : environments) {
            logger.debug("init task for environment {}", envName);
            AtomicBoolean status = new AtomicBoolean(false);
            envNamePerStatus.put(envName, status);
            environmentsEngine.connectUebTopicForDistributionConfTopic(envName, status, envNamePerInitTask, envNamePerPollingTask);
        }
        logger.debug("init UEB health check");
        distributionEngineClusterHealth.startHealthCheckTask(envNamePerStatus);
        logger.trace("Exit init method of DistributionEngine");
    }

    @PreDestroy
    public void shutdown() {
        logger.info("distribution engine shutdown - start");
        if (envNamePerInitTask != null) {
            for (DistributionEngineInitTask task : envNamePerInitTask.values()) {
                task.destroy();
            }
        }
        if (envNamePerPollingTask != null) {
            for (DistributionEnginePollingTask task : envNamePerPollingTask.values()) {
                task.destroy();
            }
        }
    }

    /**
     * validate mandatory configuration parameters received
     *
     * @param deConfiguration: distribution engine configuration
     * @return boolean result: true of false
     */
    protected boolean validateConfiguration(DistributionEngineConfiguration deConfiguration) {
        String methodName = "validateConfiguration";
        logger.info("\n\n\n\n...........kafka CONFIG: " + deConfiguration.getKafkaBootStrapServers() + "..........\n\n\n");
        boolean result = isValidServers(deConfiguration.getUebServers(), methodName, "uebServers");
        result = isValidParam(deConfiguration.getEnvironments(), methodName, "environments") && result;
        result = isValidParam(deConfiguration.getUebPublicKey(), methodName, "uebPublicKey") && result;
        result = isValidParam(deConfiguration.getUebSecretKey(), methodName, "uebSecretKey") && result;
        result = isValidParam(deConfiguration.getDistributionNotifTopicName(), methodName, "distributionNotifTopicName") && result;
        result = isValidParam(deConfiguration.getDistributionStatusTopicName(), methodName, "distributionStatusTopicName") && result;
        result = isValidObject(deConfiguration.getCreateTopic(), methodName, "createTopic") && result;
        result = isValidObject(deConfiguration.getDistributionStatusTopic(), methodName, "distributionStatusTopic") && result;
        result = isValidObject(deConfiguration.getInitMaxIntervalSec(), methodName, "initMaxIntervalSec") && result;
        result = isValidObject(deConfiguration.getInitRetryIntervalSec(), methodName, "initRetryIntervalSec") && result;
        result = isValidParam(deConfiguration.getDistributionStatusTopic().getConsumerId(), methodName, "consumerId") && result;
        result = isValidParam(deConfiguration.getDistributionStatusTopic().getConsumerGroup(), methodName, "consumerGroup") && result;
        result = isValidObject(deConfiguration.getDistributionStatusTopic().getFetchTimeSec(), methodName, "fetchTimeSec") && result;
        result = isValidObject(deConfiguration.getDistributionStatusTopic().getPollingIntervalSec(), methodName, "pollingIntervalSec") && result;
        return result;
    }

    private boolean isValidServers(List<String> uebServers, String methodName, String paramName) {
        if (uebServers == null || uebServers.isEmpty()) {
            BeEcompErrorManager.getInstance().logBeMissingConfigurationError(methodName, paramName);
            return false;
        }
        if (uebServers.size() < 2) {
            BeEcompErrorManager.getInstance().logBeConfigurationInvalidListSizeError(methodName, paramName, 2);
            return false;
        }
        for (String serverFqdn : uebServers) {
            if (!isValidFqdn(serverFqdn)) {
                BeEcompErrorManager.getInstance().logBeInvalidConfigurationError(methodName, paramName, serverFqdn);
                return false;
            }
        }
        return true;
    }

    private boolean isValidFqdn(String serverFqdn) {
        try {
            Matcher matcher = FQDN_PATTERN.matcher(serverFqdn);
            return matcher.matches();
        } catch (Exception e) {
            logger.debug("Failed to match value of address {}", serverFqdn, e);
            return false;
        }
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
        return DistributionEngineInitTask.buildTopicName(distributionNotifTopicName, envName);
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
    public OperationalEnvironmentEntry getEnvironmentByDmaapUebAddress(List<String> dmaapUebAddress) {
        return environmentsEngine.getEnvironmentByDmaapUebAddress(dmaapUebAddress);
    }

    @Override
    public INotificationData buildServiceForDistribution(Service service, String distributionId, String workloadContext) {
        INotificationData value = serviceDistributionArtifactsBuilder.buildResourceInstanceForDistribution(service, distributionId, workloadContext);
        value = serviceDistributionArtifactsBuilder.buildServiceForDistribution(value, service);
        return value;
    }
}
