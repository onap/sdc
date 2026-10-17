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

import com.att.nsa.cambria.client.CambriaConsumer;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import fj.data.Either;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.openecomp.sdc.be.components.distribution.engine.report.DistributionCompleteReporter;
import org.openecomp.sdc.be.components.kafka.KafkaHandler;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.log.wrappers.LoggerSdcAudit;

public class DistributionEnginePollingTask implements Runnable {

    public static final String DISTRIBUTION_STATUS_POLLING = "distributionEngineStatusPolling";
    private static final String PARTNER_NAME = "UNKNOWN";
    private static final Logger logger = Logger.getLogger(DistributionEnginePollingTask.class.getName());
    private static LoggerSdcAudit audit = new LoggerSdcAudit(DistributionEnginePollingTask.class);
    ScheduledFuture<?> scheduledFuture = null;
    private String topicName;
    private ComponentsUtils componentUtils;
    private int fetchTimeoutInSec = 15;
    private int pollingIntervalInSec;
    private String consumerId;
    private String consumerGroup;
    private CambriaHandler cambriaHandler = new CambriaHandler();
    private final KafkaHandler kafkaHandler;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private DistributionCompleteReporter distributionCompleteReporter;
    private ScheduledExecutorService scheduledPollingService = Executors
        .newScheduledThreadPool(1, new BasicThreadFactory.Builder().namingPattern("TopicPollingThread-%d").build());
    private CambriaConsumer cambriaConsumer = null;
    private DistributionEngineClusterHealth distributionEngineClusterHealth = null;
    private OperationalEnvironmentEntry environmentEntry;

    public DistributionEnginePollingTask(DistributionEngineConfiguration distributionEngineConfiguration,
                                         DistributionCompleteReporter distributionCompleteReporter, ComponentsUtils componentUtils,
                                         DistributionEngineClusterHealth distributionEngineClusterHealth,
                                         OperationalEnvironmentEntry environmentEntry) {
        this(distributionEngineConfiguration, distributionCompleteReporter, componentUtils, distributionEngineClusterHealth, environmentEntry,
            new KafkaHandler());
    }

    @VisibleForTesting
    DistributionEnginePollingTask(DistributionEngineConfiguration distributionEngineConfiguration,
                                  DistributionCompleteReporter distributionCompleteReporter, ComponentsUtils componentUtils,
                                  DistributionEngineClusterHealth distributionEngineClusterHealth,
                                  OperationalEnvironmentEntry environmentEntry, KafkaHandler kafkaHandler) {
        this.kafkaHandler = kafkaHandler;
        this.componentUtils = componentUtils;
        DistributionStatusTopicConfig statusConfig = distributionEngineConfiguration.getDistributionStatusTopic();
        this.pollingIntervalInSec = statusConfig.getPollingIntervalSec();
        this.fetchTimeoutInSec = statusConfig.getFetchTimeSec();
        this.consumerGroup = statusConfig.getConsumerGroup();
        this.consumerId = statusConfig.getConsumerId();
        this.distributionEngineClusterHealth = distributionEngineClusterHealth;
        this.environmentEntry = environmentEntry;
        this.distributionCompleteReporter = distributionCompleteReporter;
    }

    public void startTask(String topicName) {
        this.topicName = topicName;
        logger.debug("start task for polling topic {}", topicName);
        if (fetchTimeoutInSec < 15) {
            logger.warn("fetchTimeout value should be greater or equal to 15 sec. use default");
            fetchTimeoutInSec = 15;
        }
        try {
            if (Boolean.FALSE.equals(kafkaHandler.isKafkaActive())) {
                cambriaConsumer = cambriaHandler
                    .createConsumer(environmentEntry.getDmaapUebAddress(), topicName, environmentEntry.getUebApikey(),
                        environmentEntry.getUebSecretKey(),
                        consumerId, consumerGroup, fetchTimeoutInSec * 1000);
            }
            if (scheduledPollingService != null) {
                logger.debug("Start Distribution Engine polling task. polling interval {} seconds", pollingIntervalInSec);
                scheduledFuture = scheduledPollingService.scheduleAtFixedRate(this, 0, pollingIntervalInSec, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.debug("unexpected error occured", e);
            BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError("startTask", e.getMessage());
        }
    }

    public void stopTask() {
        if (scheduledFuture != null) {
            boolean result = scheduledFuture.cancel(true);
            logger.debug("Stop polling task. result = {}", result);
            if (!result) {
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
        // scheduleAtFixedRate cancels every further run once one run throws, so nothing may escape this method
        try {
            pollTopic();
        } catch (Throwable e) {
            logger.error("unexpected error while polling topic {}", topicName, e);
            BeEcompErrorManager.getInstance().logBeDistributionEngineSystemError(DISTRIBUTION_STATUS_POLLING, String.valueOf(e));
            if (Boolean.TRUE.equals(kafkaHandler.isKafkaActive())) {
                distributionEngineClusterHealth.setHealthCheckUnavailableAndReportInCaseLastStateIsUp();
            }
        }
    }

    private void pollTopic() {
        Either<Iterable<String>, CambriaErrorResponse> fetchResult;
        boolean kafkaActive = Boolean.TRUE.equals(kafkaHandler.isKafkaActive());
        if (!kafkaActive) {
            if (cambriaConsumer == null) {
                BeEcompErrorManager.getInstance()
                    .logBeUebSystemError(DISTRIBUTION_STATUS_POLLING, "polling task was not initialized properly");
                stopTask();
                return;
            }
            fetchResult = cambriaHandler.fetchFromTopic(cambriaConsumer);
        } else {
            fetchResult = kafkaHandler.fetchFromTopic(topicName);
        }
        if (fetchResult.isRight()) {
            CambriaErrorResponse errorResponse = fetchResult.right().value();
            BeEcompErrorManager.getInstance().logBeUebSystemError(DISTRIBUTION_STATUS_POLLING,
                "failed to fetch messages from topic " + topicName + " error: " + errorResponse);
            if (kafkaActive) {
                distributionEngineClusterHealth.setHealthCheckUnavailableAndReportInCaseLastStateIsUp();
            }
            return;
        }
        if (kafkaActive) {
            distributionEngineClusterHealth.setHealthCheckOkAndReportInCaseLastStateIsDown();
        }
        for (String message : fetchResult.left().value()) {
            logger.trace("received message {}", message);
            try {
                handleMessage(message);
            } catch (Throwable e) {
                logger.error("failed to handle message {} from topic {}", message, topicName, e);
                BeEcompErrorManager.getInstance().logBeUebSystemError(DISTRIBUTION_STATUS_POLLING,
                    "failed to handle message " + message + " from topic " + topicName + " error: " + e);
            }
        }
    }

    private void handleMessage(String message) {
        DistributionStatusNotification notification;
        try {
            notification = gson.fromJson(message, DistributionStatusNotification.class);
        } catch (JsonParseException e) {
            skipInvalidMessage(message, "not a valid distribution status notification: " + e.getMessage());
            return;
        }
        if (notification == null || StringUtils.isBlank(notification.getDistributionID())) {
            skipInvalidMessage(message, "missing distributionID");
            return;
        }
        if (notification.getStatus() == null) {
            skipInvalidMessage(message, "missing or unknown status");
            return;
        }
        audit.startAuditFetchLog(PARTNER_NAME, DistributionEnginePollingTask.class.getName());
        handleDistributionNotificationMsg(notification, audit);
        distributionEngineClusterHealth.setHealthCheckOkAndReportInCaseLastStateIsDown();
    }

    private void skipInvalidMessage(String message, String reason) {
        logger.warn("skipping invalid message {} from topic {}: {}", message, topicName, reason);
        BeEcompErrorManager.getInstance().logBeUebSystemError(DISTRIBUTION_STATUS_POLLING,
            "skipping invalid message " + message + " from topic " + topicName + ": " + reason);
    }

    private void handleDistributionNotificationMsg(DistributionStatusNotification notification, LoggerSdcAudit audit) {
        componentUtils.auditDistributionStatusNotification(notification.getDistributionID(), notification.getConsumerID(), topicName,
            notification.getArtifactURL(), String.valueOf(notification.getTimestamp()), notification.getStatus().name(),
            notification.getErrorReason(), audit);
        if (notification.isDistributionCompleteNotification()) {
            distributionCompleteReporter.reportDistributionComplete(notification);
        }
    }

    private void shutdownExecutor() {
        if (scheduledPollingService == null) {
            return;
        }
        scheduledPollingService.shutdown(); // Disable new tasks from being

        // submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!scheduledPollingService.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduledPollingService.shutdownNow(); // Cancel currently

                // executing tasks

                // Wait a while for tasks to respond to being cancelled
                if (!scheduledPollingService.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.debug("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            scheduledPollingService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
