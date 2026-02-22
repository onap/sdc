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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
    protected static final String UEB_HEALTH_LOG_CONTEXT = "ueb.healthcheck";
    //TODO use LoggerMetric instead
    private static final Logger healthLogger = Logger.getLogger(UEB_HEALTH_LOG_CONTEXT);
    @Setter
    private boolean isKafkaActive = Boolean.parseBoolean(System.getenv().getOrDefault("USE_KAFKA", "true"));
    boolean lastHealthState = false;
    Object lockOject = new Object();
    ScheduledExecutorService healthCheckScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "UEB-Health-Check-Task");
        }
    });
    HealthCheckScheduledTask healthCheckScheduledTask = null;
    private long reconnectInterval = 5;
    private long healthCheckReadTimeout = 20;
    private List<String> uebServers = null;
    private String publicApiKey = null;
    private HealthCheckInfo healthCheckInfo = HealthCheckInfoResult.UNKNOWN.getHealthCheckInfo();
    private Map<String, AtomicBoolean> envNamePerStatus = null;
    private ScheduledFuture<?> scheduledFuture = null;

    protected void init(final String publicApiKey) {
        logger.trace("Enter init method of DistributionEngineClusterHealth");
        Long reconnectIntervalConfig = ConfigurationManager.getConfigurationManager().getConfiguration()
            .getUebHealthCheckReconnectIntervalInSeconds();
        if (reconnectIntervalConfig != null) {
            reconnectInterval = reconnectIntervalConfig.longValue();
        }
        Long healthCheckReadTimeoutConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getUebHealthCheckReadTimeout();
        if (healthCheckReadTimeoutConfig != null) {
            healthCheckReadTimeout = healthCheckReadTimeoutConfig.longValue();
        }
        DistributionEngineConfiguration distributionEngineConfiguration = ConfigurationManager.getConfigurationManager()
            .getDistributionEngineConfiguration();
        this.uebServers = distributionEngineConfiguration.getUebServers();
        this.publicApiKey = publicApiKey;
        this.healthCheckScheduledTask = new HealthCheckScheduledTask(this.uebServers);
        logger.trace("Exit init method of DistributionEngineClusterHealth");
    }

    @PreDestroy
    protected void destroy() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        if (healthCheckScheduler != null) {
            healthCheckScheduler.shutdown();
        }
    }

    /**
     * Start health check task.
     *
     * @param envNamePerStatus
     * @param startTask
     */
    public void startHealthCheckTask(Map<String, AtomicBoolean> envNamePerStatus, boolean startTask) {
        this.envNamePerStatus = envNamePerStatus;
        if (startTask && this.scheduledFuture == null) {
            this.scheduledFuture = this.healthCheckScheduler.scheduleAtFixedRate(healthCheckScheduledTask, 0, reconnectInterval, TimeUnit.SECONDS);
        }
    }

    public void startHealthCheckTask(Map<String, AtomicBoolean> envNamePerStatus) {
        startHealthCheckTask(envNamePerStatus, true);
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

    /**
     * Health Check Task Scheduler.
     * <p>
     * It schedules a task which send a apiKey get query towards the UEB servers. In case a query to the first UEB server is failed, then a second
     * query is sent to the next UEB server.
     *
     * @author esofer
     */
    public class HealthCheckScheduledTask implements Runnable {

        @Getter
        List<UebHealthCheckCall> healthCheckCalls = new ArrayList<>();
        /**
         * executor for the query itself
         */
        private final ExecutorService healthCheckExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "UEB-Health-Check-Thread");
            }
        });

        public HealthCheckScheduledTask(List<String> localUebServers) {
            logger.debug("Create health check calls for servers {}", localUebServers);
            if (localUebServers != null) {
                for (String server : localUebServers) {
                    healthCheckCalls.add(new UebHealthCheckCall(server, publicApiKey));
                }
            }
        }

        @Override
        public void run() {
            healthLogger.trace("Executing UEB Health Check Task - Start");
            boolean healthStatus = verifyAtLeastOneEnvIsUp();
            if (healthStatus) {
                boolean queryStatus;
                if (isKafkaActive) {
                    // When MSB (Kafka) is active, we skip legacy UEB health checks.
                    // The environment status (set by DistributionEngineInitTask and
                    // DistributionEnginePollingTask) is sufficient to determine health.
                    queryStatus = true;
                    healthLogger.trace("MSB (MSB/Kafka) is active, skipping UEB health check query");
                } else {
                    queryStatus = queryUeb();
                }
                if (queryStatus == lastHealthState) {
                    return;
                }
                synchronized (lockOject) {
                    if (queryStatus != lastHealthState) {
                        logger.trace("UEB Health State Changed to {}. Issuing alarm / recovery alarm...", healthStatus);
                        lastHealthState = queryStatus;
                        logAlarm(lastHealthState);
                        if (queryStatus) {
                            healthCheckInfo = HealthCheckInfoResult.OK.getHealthCheckInfo();
                        } else {
                            healthCheckInfo = HealthCheckInfoResult.UNAVAILABLE.getHealthCheckInfo();
                        }
                    }
                }
            } else {
                healthLogger.trace("Not all UEB Environments are up");
            }
        }

        /**
         * verify that at least one environment is up.
         */
        private boolean verifyAtLeastOneEnvIsUp() {
            boolean healthStatus = false;
            if (envNamePerStatus != null) {
                Collection<AtomicBoolean> values = envNamePerStatus.values();
                if (values != null) {
                    for (AtomicBoolean status : values) {
                        if (status.get()) {
                            healthStatus = true;
                            break;
                        }
                    }
                }
            }
            return healthStatus;
        }

        /**
         * go all UEB servers and send a get apiKeys query. In case a query is succeed, no query is sent to the rest of UEB servers.
         *
         * @return
         */
        private boolean queryUeb() {
            Boolean result = false;
            int retryNumber = 1;
            for (UebHealthCheckCall healthCheckCall : healthCheckCalls) {
                try {
                    healthLogger
                        .debug("Before running Health Check retry query number {} towards UEB server {}", retryNumber, healthCheckCall.getServer());
                    Future<Boolean> future = healthCheckExecutor.submit(healthCheckCall);
                    result = future.get(healthCheckReadTimeout, TimeUnit.SECONDS);
                    healthLogger.debug("After running Health Check retry query number {} towards UEB server {}. Result is {}", retryNumber,
                        healthCheckCall.getServer(), result);
                    if (result != null && result.booleanValue()) {
                        break;
                    }
                } catch (Exception e) {
                    String message = e.getMessage();
                    if (message == null) {
                        message = e.getClass().getName();
                    }
                    healthLogger.debug("Error occured during running Health Check retry query towards UEB server {}. Result is {}",
                        healthCheckCall.getServer(), message);
                    healthLogger.trace("Error occured during running Health Check retry query towards UEB server {}. Result is {}",
                        healthCheckCall.getServer(), message, e);
                }
                retryNumber++;
            }
            return result;
        }

    }
}
