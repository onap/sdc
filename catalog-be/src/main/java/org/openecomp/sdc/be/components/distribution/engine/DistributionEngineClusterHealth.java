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
 */
package org.openecomp.sdc.be.components.distribution.engine;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import javax.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openecomp.sdc.be.components.kafka.KafkaClusterProbe;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

@Component("distribution-engine-cluster-health")
public class DistributionEngineClusterHealth {

    private static final String UEB_HEALTH_CHECK_STR = "uebHealthCheck";
    private static final Logger logger = Logger.getLogger(DistributionEngineClusterHealth.class.getName());
    boolean lastHealthState = false;
    Object lockOject = new Object();
    private HealthCheckInfo healthCheckInfo = HealthCheckInfoResult.UNKNOWN.getHealthCheckInfo();
    private final KafkaClusterProbe kafkaClusterProbe;
    private final ScheduledExecutorService healthCheckScheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Kafka-Health-Check-Task");
        thread.setDaemon(true);
        return thread;
    });
    private ScheduledFuture<?> scheduledFuture;

    public DistributionEngineClusterHealth(KafkaClusterProbe kafkaClusterProbe) {
        this.kafkaClusterProbe = kafkaClusterProbe;
    }

    /**
     * Every {@code kafkaHealthCheckIntervalSec}: UP iff the status listener is running and the cluster answers.
     *
     * @param listenerRunning reports whether the status listener is running, restarting it first if it is not
     */
    public synchronized void startHealthCheckTask(BooleanSupplier listenerRunning) {
        if (scheduledFuture != null) {
            return;
        }
        long interval = positiveOrDefault(distributionEngineConfiguration().getKafkaHealthCheckIntervalSec(),
            DistributionEngineConfiguration.DEFAULT_KAFKA_HEALTH_CHECK_INTERVAL_SEC);
        scheduledFuture = healthCheckScheduler.scheduleAtFixedRate(() -> runHealthCheck(listenerRunning), 0, interval, TimeUnit.SECONDS);
    }

    // scheduleAtFixedRate cancels every further run once one run throws, so nothing may escape this method
    protected void runHealthCheck(BooleanSupplier listenerRunning) {
        boolean up;
        try {
            boolean listenerUp = listenerRunning.getAsBoolean();
            up = kafkaClusterProbe.isClusterReachable(probeTimeout()) && listenerUp;
        } catch (RuntimeException e) {
            logger.error("Kafka health check failed", e);
            up = false;
        }
        if (up) {
            setHealthCheckOkAndReportInCaseLastStateIsDown();
        } else {
            setHealthCheckUnavailableAndReportInCaseLastStateIsUp();
        }
    }

    private Duration probeTimeout() {
        return Duration.ofSeconds(positiveOrDefault(distributionEngineConfiguration().getKafkaHealthCheckTimeoutSec(),
            DistributionEngineConfiguration.DEFAULT_KAFKA_HEALTH_CHECK_TIMEOUT_SEC));
    }

    private static int positiveOrDefault(Integer value, int defaultValue) {
        return value != null && value > 0 ? value : defaultValue;
    }

    private static DistributionEngineConfiguration distributionEngineConfiguration() {
        return ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();
    }

    @PreDestroy
    public synchronized void destroy() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        healthCheckScheduler.shutdownNow();
    }

    private void logAlarm(boolean lastHealthState) {
        if (lastHealthState) {
            BeEcompErrorManager.getInstance().logBeHealthCheckUebClusterRecovery(UEB_HEALTH_CHECK_STR);
        } else {
            BeEcompErrorManager.getInstance().logBeHealthCheckUebClusterError(UEB_HEALTH_CHECK_STR);
        }
    }

    public HealthCheckInfo getHealthCheckInfo() {
        return healthCheckInfo;
    }

    /**
     * change the health check to DISABLE
     */
    public void setHealthCheckUebIsDisabled() {
        healthCheckInfo = HealthCheckInfoResult.DISABLED.getHealthCheckInfo();
    }

    /**
     * change the health check to NOT CONFGIURED
     */
    public void setHealthCheckUebConfigurationError() {
        healthCheckInfo = HealthCheckInfoResult.NOT_CONFIGURED.getHealthCheckInfo();
    }

    public void setHealthCheckOkAndReportInCaseLastStateIsDown() {
        if (lastHealthState) {
            return;
        }
        synchronized (lockOject) {
            if (!lastHealthState) {
                logger.debug("Going to update health check state to available");
                lastHealthState = true;
                healthCheckInfo = HealthCheckInfoResult.OK.getHealthCheckInfo();
                logAlarm(lastHealthState);
            }
        }
    }

    public void setHealthCheckUnavailableAndReportInCaseLastStateIsUp() {
        if (healthCheckInfo == HealthCheckInfoResult.UNAVAILABLE.getHealthCheckInfo()) {
            return;
        }
        synchronized (lockOject) {
            if (healthCheckInfo != HealthCheckInfoResult.UNAVAILABLE.getHealthCheckInfo()) {
                logger.debug("Going to update health check state to unavailable");
                lastHealthState = false;
                healthCheckInfo = HealthCheckInfoResult.UNAVAILABLE.getHealthCheckInfo();
                logAlarm(lastHealthState);
            }
        }
    }

    @AllArgsConstructor
    @Getter
    private enum HealthCheckInfoResult {
        // @formatter:off
        OK              (new HealthCheckInfo(Constants.HC_COMPONENT_DISTRIBUTION_ENGINE, HealthCheckStatus.UP, null, ClusterStatusDescription.OK.getDescription())),
        UNAVAILABLE     (new HealthCheckInfo(Constants.HC_COMPONENT_DISTRIBUTION_ENGINE, HealthCheckStatus.DOWN, null, ClusterStatusDescription.UNAVAILABLE.getDescription())),
        NOT_CONFIGURED  (new HealthCheckInfo(Constants.HC_COMPONENT_DISTRIBUTION_ENGINE, HealthCheckStatus.DOWN, null, ClusterStatusDescription.NOT_CONFIGURED.getDescription())),
        DISABLED        (new HealthCheckInfo(Constants.HC_COMPONENT_DISTRIBUTION_ENGINE, HealthCheckStatus.DOWN, null, ClusterStatusDescription.DISABLED.getDescription())),
        UNKNOWN         (new HealthCheckInfo(Constants.HC_COMPONENT_DISTRIBUTION_ENGINE, HealthCheckStatus.UNKNOWN, null, ClusterStatusDescription.UNKNOWN.getDescription()));
        // @formatter:on
        private final HealthCheckInfo healthCheckInfo;
    }

    @AllArgsConstructor
    @Getter
    private enum ClusterStatusDescription {
        OK("OK"),
        UNAVAILABLE("U-EB cluster is not available"),
        NOT_CONFIGURED("U-EB cluster is not configured"),
        DISABLED("DE is disabled in configuration"),
        UNKNOWN("U-EB cluster is currently unknown (try again in few minutes)");
        private final String description;
    }
}
