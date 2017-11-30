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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("distribution-engine-cluster-health")
public class DistributionEngineClusterHealth {

	protected static String UEB_HEALTH_LOG_CONTEXT = "ueb.healthcheck";

	private static Logger healthLogger = LoggerFactory.getLogger(UEB_HEALTH_LOG_CONTEXT);

	private static final String UEB_HEALTH_CHECK_STR = "uebHealthCheck";

	boolean lastHealthState = false;

	Object lockOject = new Object();

	private long reconnectInterval = 5;

	private long healthCheckReadTimeout = 20;

	private static Logger logger = LoggerFactory.getLogger(DistributionEngineClusterHealth.class.getName());

	private List<String> uebServers = null;

	private String publicApiKey = null;

	public enum HealthCheckInfoResult {

		OK(new HealthCheckInfo(Constants.HC_COMPONENT_DISTRIBUTION_ENGINE, HealthCheckStatus.UP, null, ClusterStatusDescription.OK.getDescription())), 
		UNAVAILABLE(new HealthCheckInfo(Constants.HC_COMPONENT_DISTRIBUTION_ENGINE, HealthCheckStatus.DOWN, null, ClusterStatusDescription.UNAVAILABLE.getDescription())), 
		NOT_CONFIGURED(new HealthCheckInfo(Constants.HC_COMPONENT_DISTRIBUTION_ENGINE, HealthCheckStatus.DOWN, null, ClusterStatusDescription.NOT_CONFIGURED.getDescription())), 
		DISABLED(new HealthCheckInfo(Constants.HC_COMPONENT_DISTRIBUTION_ENGINE, HealthCheckStatus.DOWN, null, ClusterStatusDescription.DISABLED.getDescription()));

		private HealthCheckInfo healthCheckInfo;

		HealthCheckInfoResult(HealthCheckInfo healthCheckInfo) {
			this.healthCheckInfo = healthCheckInfo;
		}

		public HealthCheckInfo getHealthCheckInfo() {
			return healthCheckInfo;
		}

	}

	private HealthCheckInfo healthCheckInfo = HealthCheckInfoResult.UNAVAILABLE.getHealthCheckInfo();

	private Map<String, AtomicBoolean> envNamePerStatus = null;

	private ScheduledFuture<?> scheduledFuture = null;

	ScheduledExecutorService healthCheckScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "UEB-Health-Check-Task");
		}
	});

	HealthCheckScheduledTask healthCheckScheduledTask = null;

	public enum ClusterStatusDescription {

		OK("OK"), UNAVAILABLE("U-EB cluster is not available"), NOT_CONFIGURED("U-EB cluster is not configured"), DISABLED("DE is disabled in configuration");

		private String desc;

		ClusterStatusDescription(String desc) {
			this.desc = desc;
		}

		public String getDescription() {
			return desc;
		}

	}

	/**
	 * Health Check Task Scheduler.
	 * 
	 * It schedules a task which send a apiKey get query towards the UEB servers. In case a query to the first UEB server is failed, then a second query is sent to the next UEB server.
	 * 
	 * 
	 * @author esofer
	 *
	 */
	public class HealthCheckScheduledTask implements Runnable {

		List<UebHealthCheckCall> healthCheckCalls = new ArrayList<>();

		public HealthCheckScheduledTask(List<String> uebServers) {

			logger.debug("Create health check calls for servers {}", uebServers);
			if (uebServers != null) {
				for (String server : uebServers) {
					healthCheckCalls.add(new UebHealthCheckCall(server, publicApiKey));
				}
			}
		}

		@Override
		public void run() {

			healthLogger.trace("Executing UEB Health Check Task - Start");

			boolean healthStatus = verifyAtLeastOneEnvIsUp();

			if (true == healthStatus) {
				boolean queryUebStatus = queryUeb();
				if (queryUebStatus == lastHealthState) {
					return;
				}

				synchronized (lockOject) {
					if (queryUebStatus != lastHealthState) {
						logger.trace("UEB Health State Changed to {}. Issuing alarm / recovery alarm...", healthStatus);
						lastHealthState = queryUebStatus;
						logAlarm(lastHealthState);
						if (true == queryUebStatus) {
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
		 * 
		 */
		private boolean verifyAtLeastOneEnvIsUp() {

			boolean healthStatus = false;

			if (envNamePerStatus != null) {
				Collection<AtomicBoolean> values = envNamePerStatus.values();
				if (values != null) {
					for (AtomicBoolean status : values) {
						if (true == status.get()) {
							healthStatus = true;
							break;
						}
					}
				}
			}

			return healthStatus;
		}

		/**
		 * executor for the query itself
		 */
		ExecutorService healthCheckExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "UEB-Health-Check-Thread");
			}
		});

		/**
		 * go all UEB servers and send a get apiKeys query. In case a query is succeed, no query is sent to the rest of UEB servers.
		 * 
		 * 
		 * @return
		 */
		private boolean queryUeb() {

			Boolean result = false;
			int retryNumber = 1;
			for (UebHealthCheckCall healthCheckCall : healthCheckCalls) {
				try {

					healthLogger.debug("Before running Health Check retry query number {} towards UEB server {}", retryNumber, healthCheckCall.getServer());

					Future<Boolean> future = healthCheckExecutor.submit(healthCheckCall);
					result = future.get(healthCheckReadTimeout, TimeUnit.SECONDS);

					healthLogger.debug("After running Health Check retry query number {} towards UEB server {}. Result is {}", retryNumber, healthCheckCall.getServer(), result);

					if (result != null && true == result.booleanValue()) {
						break;
					}

				} catch (Exception e) {
					String message = e.getMessage();
					if (message == null) {
						message = e.getClass().getName();
					}
					healthLogger.debug("Error occured during running Health Check retry query towards UEB server {}. Result is {}", healthCheckCall.getServer(), message);
					healthLogger.trace("Error occured during running Health Check retry query towards UEB server {}. Result is {}", healthCheckCall.getServer(), message, e);
				}
				retryNumber++;

			}

			return result;

		}

		public List<UebHealthCheckCall> getHealthCheckCalls() {
			return healthCheckCalls;
		}

	}

	@PostConstruct
	private void init() {

		logger.trace("Enter init method of DistributionEngineClusterHealth");

		Long reconnectIntervalConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getUebHealthCheckReconnectIntervalInSeconds();
		if (reconnectIntervalConfig != null) {
			reconnectInterval = reconnectIntervalConfig.longValue();
		}
		Long healthCheckReadTimeoutConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getUebHealthCheckReadTimeout();
		if (healthCheckReadTimeoutConfig != null) {
			healthCheckReadTimeout = healthCheckReadTimeoutConfig.longValue();
		}

		DistributionEngineConfiguration distributionEngineConfiguration = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration();

		this.uebServers = distributionEngineConfiguration.getUebServers();
		this.publicApiKey = distributionEngineConfiguration.getUebPublicKey();

		this.healthCheckScheduledTask = new HealthCheckScheduledTask(this.uebServers);

		logger.trace("Exit init method of DistributionEngineClusterHealth");

	}

	@PreDestroy
	private void destroy() {

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

		if (startTask == true && this.scheduledFuture == null) {
			this.scheduledFuture = this.healthCheckScheduler.scheduleAtFixedRate(healthCheckScheduledTask, 0, reconnectInterval, TimeUnit.SECONDS);
		}
	}

	public void startHealthCheckTask(Map<String, AtomicBoolean> envNamePerStatus) {
		startHealthCheckTask(envNamePerStatus, true);
	}

	private void logAlarm(boolean lastHealthState) {
		if (lastHealthState == true) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeHealthCheckRecovery, UEB_HEALTH_CHECK_STR);
			BeEcompErrorManager.getInstance().logBeHealthCheckUebClusterRecovery(UEB_HEALTH_CHECK_STR);
		} else {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeHealthCheckError, UEB_HEALTH_CHECK_STR);
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

		if (lastHealthState == true) {
			return;
		}
		synchronized (lockOject) {
			if (lastHealthState == false) {
				logger.debug("Going to update health check state to available");
				lastHealthState = true;
				healthCheckInfo = HealthCheckInfoResult.OK.getHealthCheckInfo();
				logAlarm(lastHealthState);
			}
		}

	}

}
