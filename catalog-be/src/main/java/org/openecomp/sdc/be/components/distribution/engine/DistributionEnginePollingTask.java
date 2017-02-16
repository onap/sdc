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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.nsa.cambria.client.CambriaConsumer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fj.data.Either;

public class DistributionEnginePollingTask implements Runnable {

	public static final String DISTRIBUTION_STATUS_POLLING = "distributionEngineStatusPolling";

	private String topicName;
	private ComponentsUtils componentUtils;
	private int fetchTimeoutInSec = 15;
	private int pollingIntervalInSec;
	private String consumerId;
	private String consumerGroup;
	private DistributionEngineConfiguration distributionEngineConfiguration;

	private CambriaHandler cambriaHandler = new CambriaHandler();
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private ScheduledExecutorService scheduledPollingService = Executors.newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("TopicPollingThread-%d").build());

	private static Logger logger = LoggerFactory.getLogger(DistributionEnginePollingTask.class.getName());

	ScheduledFuture<?> scheduledFuture = null;
	private CambriaConsumer cambriaConsumer = null;

	private DistributionEngineClusterHealth distributionEngineClusterHealth = null;

	public DistributionEnginePollingTask(DistributionEngineConfiguration distributionEngineConfiguration, String envName, ComponentsUtils componentUtils, DistributionEngineClusterHealth distributionEngineClusterHealth) {

		this.componentUtils = componentUtils;
		this.distributionEngineConfiguration = distributionEngineConfiguration;
		DistributionStatusTopicConfig statusConfig = distributionEngineConfiguration.getDistributionStatusTopic();
		this.pollingIntervalInSec = statusConfig.getPollingIntervalSec();
		this.fetchTimeoutInSec = statusConfig.getFetchTimeSec();
		this.consumerGroup = statusConfig.getConsumerGroup();
		this.consumerId = statusConfig.getConsumerId();
		this.distributionEngineClusterHealth = distributionEngineClusterHealth;
	}

	public void startTask(String topicName) {

		this.topicName = topicName;
		logger.debug("start task for polling topic {}", topicName);
		if (fetchTimeoutInSec < 15) {
			logger.warn("fetchTimeout value should be greater or equal to 15 sec. use default");
			fetchTimeoutInSec = 15;
		}
		try {
			cambriaConsumer = cambriaHandler.createConsumer(distributionEngineConfiguration.getUebServers(), topicName, distributionEngineConfiguration.getUebPublicKey(), distributionEngineConfiguration.getUebSecretKey(), consumerId, consumerGroup,
					fetchTimeoutInSec * 1000);

			if (scheduledPollingService != null) {
				logger.debug("Start Distribution Engine polling task. polling interval {} seconds", pollingIntervalInSec);
				scheduledFuture = scheduledPollingService.scheduleAtFixedRate(this, 0, pollingIntervalInSec, TimeUnit.SECONDS);

			}
		} catch (Exception e) {
			logger.debug("unexpected error occured", e);
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDistributionEngineSystemError, methodName, e.getMessage());
			BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError(methodName, e.getMessage());
		}
	}

	public void stopTask() {
		if (scheduledFuture != null) {
			boolean result = scheduledFuture.cancel(true);
			logger.debug("Stop polling task. result = {}", result);
			if (false == result) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUebSystemError, DISTRIBUTION_STATUS_POLLING, "try to stop the polling task");
				BeEcompErrorManager.getInstance().logBeUebSystemError(DISTRIBUTION_STATUS_POLLING, "try to stop the polling task");
			}
			scheduledFuture = null;
		}
		if (cambriaConsumer != null) {
			logger.debug("close consumer");
			cambriaHandler.closeConsumer(cambriaConsumer);
		}

	}

	public void destroy() {
		this.stopTask();
		shutdownExecutor();
	}

	@Override
	public void run() {
		logger.trace("run() method. polling queue {}", topicName);

		try {
			// init error
			if (cambriaConsumer == null) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUebSystemError, DISTRIBUTION_STATUS_POLLING, "polling task was not initialized properly");
				BeEcompErrorManager.getInstance().logBeUebSystemError(DISTRIBUTION_STATUS_POLLING, "polling task was not initialized properly");
				stopTask();
				return;
			}

			Either<Iterable<String>, CambriaErrorResponse> fetchResult = cambriaHandler.fetchFromTopic(cambriaConsumer);
			// fetch error
			if (fetchResult.isRight()) {
				CambriaErrorResponse errorResponse = fetchResult.right().value();
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUebSystemError, DISTRIBUTION_STATUS_POLLING, "failed to fetch messages from topic " + topicName + " error: " + fetchResult.right().value());
				BeEcompErrorManager.getInstance().logBeUebSystemError(DISTRIBUTION_STATUS_POLLING, "failed to fetch messages from topic " + topicName + " error: " + fetchResult.right().value());

				// TODO: if status== internal error (connection problem) change
				// state to inactive
				// in next try, if succeed - change to active
				return;
			}

			// success
			Iterable<String> messages = fetchResult.left().value();
			for (String message : messages) {
				logger.trace("received message {}", message);
				try {
					DistributionStatusNotification notification = gson.fromJson(message, DistributionStatusNotification.class);
					componentUtils.auditDistributionStatusNotification(AuditingActionEnum.DISTRIBUTION_STATUS, notification.getDistributionID(), notification.getConsumerID(), topicName, notification.getArtifactURL(),
							String.valueOf(notification.getTimestamp()), notification.getStatus().name(), notification.getErrorReason());

					distributionEngineClusterHealth.setHealthCheckOkAndReportInCaseLastStateIsDown();

				} catch (Exception e) {
					logger.debug("failed to convert message to object", e);
					BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeUebSystemError, DISTRIBUTION_STATUS_POLLING, "failed to parse message " + message + " from topic " + topicName + " error: " + fetchResult.right().value());
					BeEcompErrorManager.getInstance().logBeUebSystemError(DISTRIBUTION_STATUS_POLLING, "failed to parse message " + message + " from topic " + topicName + " error: " + fetchResult.right().value());
				}

			}
		} catch (Exception e) {
			logger.debug("unexpected error occured", e);
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();

			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDistributionEngineSystemError, methodName, e.getMessage());
			BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError(methodName, e.getMessage());
		}

	}

	private void shutdownExecutor() {
		if (scheduledPollingService == null)
			return;

		scheduledPollingService.shutdown(); // Disable new tasks from being
											// submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!scheduledPollingService.awaitTermination(60, TimeUnit.SECONDS)) {
				scheduledPollingService.shutdownNow(); // Cancel currently
														// executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!scheduledPollingService.awaitTermination(60, TimeUnit.SECONDS))
					logger.debug("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			scheduledPollingService.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

}
