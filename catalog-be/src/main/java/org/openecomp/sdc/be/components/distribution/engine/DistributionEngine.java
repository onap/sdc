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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.util.YamlToObjectConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("distributionEngine")
public class DistributionEngine implements IDistributionEngine {

	public static final Pattern FQDN_PATTERN = Pattern.compile("^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*(:[0-9]{2,4})*$", Pattern.CASE_INSENSITIVE);

	public static void main(String[] args) {

		List<String> servers = new ArrayList<>();

		servers.add("uebsb91kcdc.it.att.com:3904");
		servers.add("uebsb91kcdc.it.att.com:3904");
		servers.add("uebsb91kcdc.it.att.com:3904");

		YamlToObjectConverter converter = new YamlToObjectConverter();
		DistributionEngineConfiguration distributionEngineConfiguration = converter.convert("src/test/resources/config/catalog-be/distribEngine1/distribution-engine-configuration.yaml", DistributionEngineConfiguration.class);

		DistributionEngineInitTask distributionEngineInitTask = new DistributionEngineInitTask(2l, distributionEngineConfiguration, "PROD", new AtomicBoolean(false), null, null);
		distributionEngineInitTask.startTask();

	}

	@javax.annotation.Resource
	private ComponentsUtils componentUtils;

	@javax.annotation.Resource
	private DistributionNotificationSender distributionNotificationSender;

	@javax.annotation.Resource
	private ServiceDistributionArtifactsBuilder serviceDistributionArtifactsBuilder;

	@javax.annotation.Resource
	private DistributionEngineClusterHealth distributionEngineClusterHealth;

	private static Logger logger = LoggerFactory.getLogger(DistributionEngine.class.getName());

	private Map<String, DistributionEngineInitTask> envNamePerInitTask = new HashMap<String, DistributionEngineInitTask>();
	private Map<String, DistributionEnginePollingTask> envNamePerPollingTask = new HashMap<String, DistributionEnginePollingTask>();

	private Map<String, AtomicBoolean> envNamePerStatus = new HashMap<String, AtomicBoolean>();

	@Override
	public boolean isActive() {

		if (true == envNamePerInitTask.isEmpty()) {
			return false;
		}

		for (DistributionEngineInitTask task : envNamePerInitTask.values()) {
			boolean active = task.isActive();
			if (active == false) {
				return false;
			}
		}
		return true;
	}

	@PostConstruct
	private void init() {

		logger.trace("Enter init method of DistributionEngine");

		DistributionEngineConfiguration distributionEngineConfiguration = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();

		boolean startDistributionEngine = distributionEngineConfiguration.isStartDistributionEngine();
		logger.debug("Distribution engine activation parameter is {}", startDistributionEngine);
		if (false == startDistributionEngine) {
			logger.info("The disribution engine is disabled");

			this.distributionEngineClusterHealth.setHealthCheckUebIsDisabled();

			return;
		}

		boolean isValidConfig = validateConfiguration(distributionEngineConfiguration);

		if (false == isValidConfig) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUebSystemError, DistributionEngineInitTask.INIT_DISTRIBUTION_ENGINE_FLOW, "validate distribution configuration in init phase");
			BeEcompErrorManager.getInstance().logBeUebSystemError(DistributionEngineInitTask.INIT_DISTRIBUTION_ENGINE_FLOW, "validate distribution configuration in init phase");

			this.distributionEngineClusterHealth.setHealthCheckUebConfigurationError();
			return;
		}

		List<String> environments = distributionEngineConfiguration.getEnvironments();

		for (String envName : environments) {

			DistributionEnginePollingTask distributionEnginePollingTask = new DistributionEnginePollingTask(distributionEngineConfiguration, envName, componentUtils, distributionEngineClusterHealth);

			logger.debug("Init task for environment {}", envName);

			AtomicBoolean status = new AtomicBoolean(false);
			envNamePerStatus.put(envName, status);
			DistributionEngineInitTask distributionEngineInitTask = new DistributionEngineInitTask(0l, distributionEngineConfiguration, envName, status, componentUtils, distributionEnginePollingTask);
			distributionEngineInitTask.startTask();
			envNamePerInitTask.put(envName, distributionEngineInitTask);
			envNamePerPollingTask.put(envName, distributionEnginePollingTask);
		}

		logger.debug("Init UEB health check");
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
	 * @param deConfiguration
	 * @return
	 */
	protected boolean validateConfiguration(DistributionEngineConfiguration deConfiguration) {

		String methodName = new Object() {
		}.getClass().getEnclosingMethod().getName();

		boolean result = true;
		result = isValidServers(deConfiguration.getUebServers(), methodName, "uebServers") && result;
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

		if (uebServers == null || uebServers.size() == 0) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeMissingConfigurationError, methodName, paramName);
			BeEcompErrorManager.getInstance().logBeMissingConfigurationError(methodName, paramName);
			return false;
		}

		if (uebServers.size() < 2) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeConfigurationInvalidListSizeError, methodName, paramName, "2");
			BeEcompErrorManager.getInstance().logBeConfigurationInvalidListSizeError(methodName, paramName, 2);
			return false;
		}

		for (String serverFqdn : uebServers) {
			if (false == isValidFqdn(serverFqdn)) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidConfigurationError, methodName, paramName, serverFqdn);
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

	private boolean isEmptyParam(String param) {

		if (param == null || true == param.isEmpty()) {
			return true;
		}

		return false;
	}

	private boolean isValidParam(String paramValue, String methodName, String paramName) {

		if (isEmptyParam(paramValue)) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeMissingConfigurationError, methodName, paramName);
			BeEcompErrorManager.getInstance().logBeMissingConfigurationError(methodName, paramName);
			return false;
		}
		return true;

	}

	private boolean isValidParam(List<String> paramValue, String methodName, String paramName) {

		if (isEmptyList(paramValue)) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeMissingConfigurationError, methodName, paramName);
			BeEcompErrorManager.getInstance().logBeMissingConfigurationError(methodName, paramName);
			return false;
		}
		return true;

	}

	private boolean isValidObject(Object paramValue, String methodName, String paramName) {

		if (paramValue == null) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeMissingConfigurationError, methodName, paramName);
			BeEcompErrorManager.getInstance().logBeMissingConfigurationError(methodName, paramName);
			return false;
		}
		return true;

	}

	private boolean isEmptyList(List<String> list) {
		if (list == null || true == list.isEmpty()) {
			return true;
		}
		return false;
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

	public StorageOperationStatus isEnvironmentAvailable(String envName) {

		if (envName == null || true == envName.isEmpty()) {

			return StorageOperationStatus.DISTR_ENVIRONMENT_SENT_IS_INVALID;
		}

		AtomicBoolean status = envNamePerStatus.get(envName);
		if (status == null) {
			return StorageOperationStatus.DISTR_ENVIRONMENT_NOT_FOUND;
		}

		if (false == status.get()) {
			return StorageOperationStatus.DISTR_ENVIRONMENT_NOT_AVAILABLE;
		}
		return StorageOperationStatus.OK;
	}

	public StorageOperationStatus isEnvironmentAvailable() {

		String envName = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().getEnvironments().get(0);

		return isEnvironmentAvailable(envName);
	}

	@Override
	public void disableEnvironment(String envName) {
		// TODO disable tasks
		AtomicBoolean status = envNamePerStatus.get(envName);
		status.set(false);
	}

	@Override
	public StorageOperationStatus notifyService(String distributionId, Service service, INotificationData notificationData, String envName, String userId, String modifierName) {

		logger.debug("Received notify service request. distributionId = {}, serviceUuid = {} serviceUid = {}, envName = {}, userId = {}, modifierName {}", distributionId, service.getUUID(), service.getUniqueId(), envName, userId, modifierName);

		DistributionEngineConfiguration deConfiguration = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();

		String distributionNotifTopicName = deConfiguration.getDistributionNotifTopicName();
		String topicName = DistributionEngineInitTask.buildTopicName(distributionNotifTopicName, envName);

		StorageOperationStatus sendNotification = distributionNotificationSender.sendNotification(topicName, distributionId, deConfiguration, envName, notificationData, service, userId, modifierName);

		logger.debug("Finish notifyService. status is {}", sendNotification);

		return sendNotification;
	}

	@Override
	public Either<INotificationData, StorageOperationStatus> isReadyForDistribution(Service service, String distributionId, String envName) {
		StorageOperationStatus status = isEnvironmentAvailable(envName);
		if (status != StorageOperationStatus.OK) {
			String envErrorDec = getEnvironmentErrorDescription(status);
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDistributionEngineSystemError, DistributionNotificationSender.DISTRIBUTION_NOTIFICATION_SENDING,
					"Environment name " + envName + " is not available. Reason : " + envErrorDec);
			BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError(DistributionNotificationSender.DISTRIBUTION_NOTIFICATION_SENDING, "Environment name " + envName + " is not available. Reason : " + envErrorDec);
			return Either.right(status);
		}

		Either<Boolean, StorageOperationStatus> isServiceContainsDeploymentArtifactsStatus = serviceDistributionArtifactsBuilder.isServiceContainsDeploymentArtifacts(service);
		if (isServiceContainsDeploymentArtifactsStatus.isRight()) {
			StorageOperationStatus operationStatus = isServiceContainsDeploymentArtifactsStatus.right().value();
			return Either.right(operationStatus);
		} else {
			Boolean isDeploymentArtifactExists = isServiceContainsDeploymentArtifactsStatus.left().value();
			if (isDeploymentArtifactExists == null || isDeploymentArtifactExists.booleanValue() == false) {
				return Either.right(StorageOperationStatus.DISTR_ARTIFACT_NOT_FOUND);
			}
		}

		INotificationData value = serviceDistributionArtifactsBuilder.buildResourceInstanceForDistribution(service, distributionId);
		value = serviceDistributionArtifactsBuilder.buildServiceForDistribution(value, service);

		return Either.left(value);
	}

}
