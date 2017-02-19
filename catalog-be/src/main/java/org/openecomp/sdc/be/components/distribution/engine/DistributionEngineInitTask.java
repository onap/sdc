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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

public class DistributionEngineInitTask implements Runnable {

	public static final String INIT_DISTRIBUTION_ENGINE_FLOW = "initDistributionEngine";
	public static final String ALREADY_EXISTS = "ALREADY_EXISTS";
	public static final String CONSUMER = "CONSUMER";
	public static final String PRODUCER = "PRODUCER";
	public static final String CREATED = "CREATED";
	public static final String FAILED = "FAILED";
	public static final Integer HTTP_OK = 200;

	private Long delayBeforeStartFlow = 0l;
	private DistributionEngineConfiguration deConfiguration;
	private String envName;
	private long retryInterval;
	private long currentRetryInterval;
	private long maxInterval;
	// private boolean active = false;
	boolean maximumRetryInterval = false;
	private AtomicBoolean status = null;
	ComponentsUtils componentsUtils = null;
	DistributionEnginePollingTask distributionEnginePollingTask = null;

	private CambriaHandler cambriaHandler = new CambriaHandler();

	public DistributionEngineInitTask(Long delayBeforeStartFlow, DistributionEngineConfiguration deConfiguration, String envName, AtomicBoolean status, ComponentsUtils componentsUtils, DistributionEnginePollingTask distributionEnginePollingTask) {
		super();
		this.delayBeforeStartFlow = delayBeforeStartFlow;
		this.deConfiguration = deConfiguration;
		this.envName = envName;
		this.retryInterval = deConfiguration.getInitRetryIntervalSec();
		this.currentRetryInterval = retryInterval;
		this.maxInterval = deConfiguration.getInitMaxIntervalSec();
		this.status = status;
		this.componentsUtils = componentsUtils;
		this.distributionEnginePollingTask = distributionEnginePollingTask;
	}

	private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

	private static Logger logger = LoggerFactory.getLogger(DistributionEngineInitTask.class.getName());

	ScheduledFuture<?> scheduledFuture = null;

	public void startTask() {
		if (scheduledExecutorService != null) {
			Integer retryInterval = deConfiguration.getInitRetryIntervalSec();
			logger.debug("Start Distribution Engine init task. retry interval {} seconds, delay before first run {} seconds", retryInterval, delayBeforeStartFlow);
			this.scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this, delayBeforeStartFlow, retryInterval, TimeUnit.SECONDS);

		}
	}

	public void restartTask() {

		this.stopTask();

		logger.debug("Start Distribution Engine init task. next run in {} seconds", this.currentRetryInterval);

		long lastCurrentInterval = currentRetryInterval;
		incrementRetryInterval();

		this.scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this, lastCurrentInterval, this.currentRetryInterval, TimeUnit.SECONDS);

	}

	protected void incrementRetryInterval() {
		if (currentRetryInterval < maxInterval) {
			currentRetryInterval *= 2;
			if (currentRetryInterval > maxInterval) {
				setMaxRetryInterval();
			}
		} else {
			setMaxRetryInterval();
		}
	}

	private void setMaxRetryInterval() {
		currentRetryInterval = maxInterval;
		maximumRetryInterval = true;
		logger.debug("Set next retry init interval to {}", maxInterval);
	}

	public void stopTask() {
		if (scheduledFuture != null) {
			boolean result = scheduledFuture.cancel(true);
			logger.debug("Stop reinit task. result = {}", result);
			if (false == result) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUebSystemError, INIT_DISTRIBUTION_ENGINE_FLOW, "try to stop the reinit task");
				BeEcompErrorManager.getInstance().logBeUebSystemError(INIT_DISTRIBUTION_ENGINE_FLOW, "try to stop the reinit task");
			}
			scheduledFuture = null;
		}
	}

	public void destroy() {
		this.stopTask();
		if (scheduledExecutorService != null) {
			scheduledExecutorService.shutdown();
		}
	}

	@Override
	public void run() {

		boolean result = false;
		result = initFlow();

		if (true == result) {
			this.stopTask();
			this.status.set(true);
			if (this.distributionEnginePollingTask != null) {
				String topicName = buildTopicName(deConfiguration.getDistributionStatusTopicName(), envName);
				logger.debug("start polling distribution status topic {}", topicName);
				this.distributionEnginePollingTask.startTask(topicName);
			}
		} else {
			if (false == maximumRetryInterval) {
				this.restartTask();
			}
		}
	}

	/**
	 * run initialization flow
	 * 
	 * @return
	 */
	public boolean initFlow() {

		logger.trace("Start init flow for environment {}", this.envName);

		Set<String> topicsList = null;
		Either<Set<String>, CambriaErrorResponse> getTopicsRes = null;

		getTopicsRes = cambriaHandler.getTopics(deConfiguration.getUebServers());
		if (getTopicsRes.isRight()) {
			CambriaErrorResponse status = getTopicsRes.right().value();
			if (status.getOperationStatus() == CambriaOperationStatus.NOT_FOUND) {
				topicsList = new HashSet<>();
			} else {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUebSystemError, INIT_DISTRIBUTION_ENGINE_FLOW, "try retrieve list of topics from U-EB server");

				BeEcompErrorManager.getInstance().logBeUebSystemError(INIT_DISTRIBUTION_ENGINE_FLOW, "try retrieve list of topics from U-EB server");

				return false;
			}
		} else {
			topicsList = getTopicsRes.left().value();
		}

		String notificationTopic = buildTopicName(deConfiguration.getDistributionNotifTopicName(), this.envName);
		logger.debug("Going to handle topic {}", notificationTopic);

		boolean status = createTopicIfNotExists(topicsList, notificationTopic);
		if (false == status) {
			return false;
		}

		CambriaErrorResponse registerProducerStatus = registerToTopic(notificationTopic, SubscriberTypeEnum.PRODUCER);

		CambriaOperationStatus createStatus = registerProducerStatus.getOperationStatus();

		if (createStatus != CambriaOperationStatus.OK) {
			return false;
		}

		String statusTopic = buildTopicName(deConfiguration.getDistributionStatusTopicName(), this.envName);
		logger.debug("Going to handle topic {}", statusTopic);
		status = createTopicIfNotExists(topicsList, statusTopic);
		if (false == status) {
			return false;
		}

		CambriaErrorResponse registerConcumerStatus = registerToTopic(statusTopic, SubscriberTypeEnum.CONSUMER);

		if (registerConcumerStatus.getOperationStatus() != CambriaOperationStatus.OK) {
			return false;
		}

		return true;
	}

	private CambriaErrorResponse registerToTopic(String topicName, SubscriberTypeEnum subscriberType) {
		CambriaErrorResponse registerStatus = cambriaHandler.registerToTopic(deConfiguration.getUebServers(), topicName, deConfiguration.getUebPublicKey(), deConfiguration.getUebSecretKey(), deConfiguration.getUebPublicKey(), subscriberType);

		String role = CONSUMER;
		if (subscriberType == SubscriberTypeEnum.PRODUCER) {
			role = PRODUCER;
		}
		auditRegistration(topicName, registerStatus, role);
		return registerStatus;
	}

	private void auditRegistration(String notificationTopic, CambriaErrorResponse registerProducerStatus, String role) {
		if (componentsUtils != null) {
			Integer httpCode = registerProducerStatus.getHttpCode();
			String httpCodeStr = String.valueOf(httpCode);
			this.componentsUtils.auditDistributionEngine(AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL, this.envName, notificationTopic, role, deConfiguration.getUebPublicKey(), httpCodeStr);
		}
	}

	private boolean createTopicIfNotExists(Set<String> topicsList, String topicName) {

		if (topicsList.contains(topicName)) {
			if (componentsUtils != null) {
				this.componentsUtils.auditDistributionEngine(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC, this.envName, topicName, null, null, ALREADY_EXISTS);
			}
			return true;
		}

		CambriaErrorResponse createDistribTopicStatus = cambriaHandler.createTopic(deConfiguration.getUebServers(), deConfiguration.getUebPublicKey(), deConfiguration.getUebSecretKey(), topicName, deConfiguration.getCreateTopic().getPartitionCount(),
				deConfiguration.getCreateTopic().getReplicationCount());

		CambriaOperationStatus status = createDistribTopicStatus.getOperationStatus();
		if (status == CambriaOperationStatus.TOPIC_ALREADY_EXIST) {
			if (componentsUtils != null) {
				this.componentsUtils.auditDistributionEngine(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC, this.envName, topicName, null, null, ALREADY_EXISTS);
			}
		} else if (status == CambriaOperationStatus.OK) {
			if (componentsUtils != null) {
				this.componentsUtils.auditDistributionEngine(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC, this.envName, topicName, null, null, CREATED);
			}
		} else {
			if (componentsUtils != null) {
				this.componentsUtils.auditDistributionEngine(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC, this.envName, topicName, null, null, FAILED);
			}
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUebSystemError, INIT_DISTRIBUTION_ENGINE_FLOW, "try to create topic " + topicName);

			BeEcompErrorManager.getInstance().logBeUebSystemError(INIT_DISTRIBUTION_ENGINE_FLOW, "try to create topic " + topicName);

			return false;
		}

		return true;
	}

	public static String buildTopicName(String topicName, String environment) {
		return topicName + "-" + environment.toUpperCase();
	}

	public boolean isActive() {
		return this.status.get();
	}

	public long getCurrentRetryInterval() {
		return currentRetryInterval;
	}

	protected void setCambriaHandler(CambriaHandler cambriaHandler) {
		this.cambriaHandler = cambriaHandler;
	}
}
