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

import fj.data.Either;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.openecomp.sdc.be.components.kafka.KafkaHandler;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class DistributionEngineInitTask implements Runnable {

    public static final String INIT_DISTRIBUTION_ENGINE_FLOW = "initDistributionEngine";
    public static final String ALREADY_EXISTS = "ALREADY_EXISTS";
    public static final String CONSUMER = "CONSUMER";
    public static final String PRODUCER = "PRODUCER";
    public static final String CREATED = "CREATED";
    public static final String FAILED = "FAILED";
    public static final Integer HTTP_OK = 200;
    private static final Logger logger = Logger.getLogger(DistributionEngineInitTask.class.getName());
    boolean maximumRetryInterval = false;
    ComponentsUtils componentsUtils = null;
    DistributionEnginePollingTask distributionEnginePollingTask = null;
    ScheduledFuture<?> scheduledFuture = null;
    private Long delayBeforeStartFlow = 0l;
    private DistributionEngineConfiguration deConfiguration;
    private String envName;
    private long retryInterval;
    private long currentRetryInterval;
    private long maxInterval;
    private AtomicBoolean status = null;
    private OperationalEnvironmentEntry environmentEntry;
    private CambriaHandler cambriaHandler = new CambriaHandler();
    private KafkaHandler kafkaHandler = new KafkaHandler();
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public DistributionEngineInitTask(Long delayBeforeStartFlow, DistributionEngineConfiguration deConfiguration, String envName,
                                      AtomicBoolean status, ComponentsUtils componentsUtils,
                                      DistributionEnginePollingTask distributionEnginePollingTask, OperationalEnvironmentEntry environmentEntry) {
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
        this.environmentEntry = environmentEntry;
    }

    public static String buildTopicName(String topicName, String environment) {
        return topicName + "-" + environment.toUpperCase();
    }

    public void startTask() {
        if (scheduledExecutorService != null) {
            Integer retryInterval = deConfiguration.getInitRetryIntervalSec();
            logger.debug("Start Distribution Engine init task. retry interval {} seconds, delay before first run {} seconds", retryInterval,
                delayBeforeStartFlow);
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
            if (!result) {
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
        if (initFlow()) {
            this.stopTask();
            this.status.set(true);
            if (this.distributionEnginePollingTask != null) {
                String topicName = buildTopicName(deConfiguration.getDistributionStatusTopicName(), envName);
                logger.debug("start polling distribution status topic {}", topicName);
                this.distributionEnginePollingTask.startTask(topicName);
            }
        } else {
            if (!maximumRetryInterval) {
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
        logger.info("Start init flow for environment {}", this.envName);
        if (Boolean.FALSE.equals(kafkaHandler.isKafkaActive())) {
            Set<String> topicsList;
            Either<Set<String>, CambriaErrorResponse> getTopicsRes;
            getTopicsRes = cambriaHandler.getTopics(new ArrayList<>(environmentEntry.getDmaapUebAddress()));
            if (getTopicsRes.isRight()) {
                CambriaErrorResponse cambriaErrorResponse = getTopicsRes.right().value();
                if (cambriaErrorResponse.getOperationStatus() == CambriaOperationStatus.NOT_FOUND) {
                    topicsList = new HashSet<>();
                } else {
                    BeEcompErrorManager.getInstance().logBeUebSystemError(INIT_DISTRIBUTION_ENGINE_FLOW,
                        "try retrieve list of topics from U-EB server");
                    return false;
                }
            } else {
                topicsList = getTopicsRes.left().value();
            }
            String notificationTopic = buildTopicName(deConfiguration.getDistributionNotifTopicName(), this.envName);
            logger.debug("Going to handle topic {}", notificationTopic);
            if (!createNotificationTopicIfNotExists(topicsList, notificationTopic)) {
                return false;
            }
            CambriaErrorResponse registerProducerStatus = registerToTopic(notificationTopic,
                SubscriberTypeEnum.PRODUCER);
            CambriaOperationStatus createStatus = registerProducerStatus.getOperationStatus();
            if (createStatus != CambriaOperationStatus.OK) {
                return false;
            }
            String statusTopic = buildTopicName(deConfiguration.getDistributionStatusTopicName(), this.envName);
            logger.debug("Going to handle topic {}", statusTopic);
            if (!createStatusTopicIfNotExists(topicsList, statusTopic)) {
                return false;
            }
            CambriaErrorResponse registerConcumerStatus = registerToTopic(statusTopic, SubscriberTypeEnum.CONSUMER);
            return registerConcumerStatus.getOperationStatus() == CambriaOperationStatus.OK;
        } else {
            logger.info("Skipping DisributionEngineInitTask flow to use kafka native for distribution messaging");
            return true;
        }
    }

    private CambriaErrorResponse registerToTopic(String topicName, SubscriberTypeEnum subscriberType) {
        CambriaErrorResponse registerStatus = cambriaHandler
            .registerToTopic(environmentEntry.getDmaapUebAddress(), environmentEntry.getUebApikey(), environmentEntry.getUebSecretKey(),
                environmentEntry.getUebApikey(), subscriberType, topicName);

        if (CambriaOperationStatus.AUTHENTICATION_ERROR.equals(registerStatus.getOperationStatus())
                || CambriaOperationStatus.CONNNECTION_ERROR.equals(registerStatus.getOperationStatus())){
            registerStatus = cambriaHandler
                    .registerToTopic(environmentEntry.getDmaapUebAddress(), deConfiguration.getUebPublicKey(), deConfiguration.getUebSecretKey(),
                            environmentEntry.getUebApikey(), subscriberType, topicName);
        }

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
            this.componentsUtils.auditDistributionEngine(AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL, this.envName,
                DistributionTopicData.newBuilder().notificationTopic(notificationTopic).build(), role, environmentEntry.getUebApikey(), httpCodeStr);
        }
    }

    private boolean createStatusTopicIfNotExists(Set<String> topicsList, String topicName) {
        DistributionTopicData distributionTopicData = DistributionTopicData.newBuilder().statusTopic(topicName).build();
        return createDistributionTopic(topicsList, topicName, distributionTopicData);
    }

    private boolean createNotificationTopicIfNotExists(Set<String> topicsList, String topicName) {
        DistributionTopicData distributionTopicData = DistributionTopicData.newBuilder().notificationTopic(topicName).build();
        return createDistributionTopic(topicsList, topicName, distributionTopicData);
    }

    private boolean createDistributionTopic(Set<String> topicsList, String topicName, DistributionTopicData distributionTopicData) {
        boolean isSucceeded = true;
        if (topicsList.contains(topicName)) {
            if (componentsUtils != null) {
                componentsUtils
                    .auditDistributionEngine(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC, this.envName, distributionTopicData, ALREADY_EXISTS);
            }
            return isSucceeded;
        }
        CambriaErrorResponse createDistribTopicStatus = cambriaHandler
            .createTopic(environmentEntry.getDmaapUebAddress(), environmentEntry.getUebApikey(), environmentEntry.getUebSecretKey(), topicName,
                deConfiguration.getCreateTopic().getPartitionCount(), deConfiguration.getCreateTopic().getReplicationCount());
        CambriaOperationStatus status = createDistribTopicStatus.getOperationStatus();
        switch (status) {
            case OK:
                if (componentsUtils != null) {
                    componentsUtils
                        .auditDistributionEngine(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC, this.envName, distributionTopicData, CREATED);
                }
                break;
            case TOPIC_ALREADY_EXIST:
                if (componentsUtils != null) {
                    componentsUtils
                        .auditDistributionEngine(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC, this.envName, distributionTopicData, ALREADY_EXISTS);
                }
                break;
            default:
                if (componentsUtils != null) {
                    componentsUtils
                        .auditDistributionEngine(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC, this.envName, distributionTopicData, FAILED);
                }
                BeEcompErrorManager.getInstance().logBeUebSystemError(INIT_DISTRIBUTION_ENGINE_FLOW, "try to create topic " + topicName);
                isSucceeded = false;
                break;
        }
        return isSucceeded;
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

    protected void setKafkaHandler(KafkaHandler kafkaHandler) {
        this.kafkaHandler = kafkaHandler;
    }
}
