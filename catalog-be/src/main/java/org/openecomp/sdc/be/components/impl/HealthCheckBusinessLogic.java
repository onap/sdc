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

package org.openecomp.sdc.be.components.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.openecomp.sdc.be.components.distribution.engine.DistributionEngineClusterHealth;
import org.openecomp.sdc.be.components.distribution.engine.UebHealthCheckCall;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.IEsHealthCheckDao;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.switchover.detector.SwitchoverDetector;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckComponent;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component("healthCheckBusinessLogic")
public class HealthCheckBusinessLogic {

	protected static String BE_HEALTH_LOG_CONTEXT = "be.healthcheck";

	private static Logger healthLogger = LoggerFactory.getLogger(BE_HEALTH_LOG_CONTEXT);

	private static final String BE_HEALTH_CHECK_STR = "beHealthCheck";

	@Resource
	private TitanGenericDao titanGenericDao;

	@Resource
	private IEsHealthCheckDao esHealthCheckDao;

	@Resource
	private DistributionEngineClusterHealth distributionEngineClusterHealth;

	@Autowired
	private SwitchoverDetector switchoverDetector;

	private static Logger log = LoggerFactory.getLogger(HealthCheckBusinessLogic.class.getName());

	private volatile List<HealthCheckInfo> lastBeHealthCheckInfos = null;

	// private static volatile HealthCheckBusinessLogic instance;
	//
	public HealthCheckBusinessLogic() {

	}

	private ScheduledFuture<?> scheduledFuture = null;

	ScheduledExecutorService healthCheckScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "BE-Health-Check-Task");
		}
	});

	HealthCheckScheduledTask healthCheckScheduledTask = null;

	@PostConstruct
	public void init() {

		lastBeHealthCheckInfos = getBeHealthCheckInfos();

		log.debug("After initializing lastBeHealthCheckInfos :{}", lastBeHealthCheckInfos);

		healthCheckScheduledTask = new HealthCheckScheduledTask();

		if (this.scheduledFuture == null) {
			this.scheduledFuture = this.healthCheckScheduler.scheduleAtFixedRate(healthCheckScheduledTask, 0, 3, TimeUnit.SECONDS);
		}

	}

	//
	// public static HealthCheckBusinessLogic getInstance(){
	//// if (instance == null){
	//// instance = init();
	//// }
	// return instance;
	// }

	// private synchronized static HealthCheckBusinessLogic init() {
	// if (instance == null){
	// instance = new HealthCheckBusinessLogic();
	// }
	// return instance;
	// }

	private List<HealthCheckInfo> getBeHealthCheckInfos(ServletContext servletContext) {

		List<HealthCheckInfo> healthCheckInfos = new ArrayList<HealthCheckInfo>();

		// BE
		getBeHealthCheck(servletContext, healthCheckInfos);

		// ES
		getEsHealthCheck(servletContext, healthCheckInfos);

		// Titan
		getTitanHealthCheck(servletContext, healthCheckInfos);

		// Distribution Engine
		getDistributionEngineCheck(servletContext, healthCheckInfos);

		return healthCheckInfos;
	}

	private List<HealthCheckInfo> getBeHealthCheck(ServletContext servletContext, List<HealthCheckInfo> healthCheckInfos) {
		String appVersion = ExternalConfiguration.getAppVersion();
		String description = "OK";
		healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.BE, HealthCheckStatus.UP, appVersion, description));
		return healthCheckInfos;
	}

	public List<HealthCheckInfo> getTitanHealthCheck(ServletContext servletContext, List<HealthCheckInfo> healthCheckInfos) {
		// Titan health check and version
		TitanGenericDao titanStatusDao = (TitanGenericDao) getDao(servletContext, TitanGenericDao.class);
		String description;
		boolean isTitanUp;

		try {
			isTitanUp = titanStatusDao.isGraphOpen();
		} catch (Exception e) {
			description = "Titan error: " + e.getMessage();
			healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.TITAN, HealthCheckStatus.DOWN, null, description));
			return healthCheckInfos;
		}
		if (isTitanUp) {
			description = "OK";
			healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.TITAN, HealthCheckStatus.UP, null, description));
		} else {
			description = "Titan graph is down";
			healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.TITAN, HealthCheckStatus.DOWN, null, description));
		}
		return healthCheckInfos;
	}

	public List<HealthCheckInfo> getEsHealthCheck(ServletContext servletContext, List<HealthCheckInfo> healthCheckInfos) {

		// ES health check and version
		IEsHealthCheckDao esStatusDao = (IEsHealthCheckDao) getDao(servletContext, IEsHealthCheckDao.class);
		HealthCheckStatus healthCheckStatus;
		String description;

		try {
			healthCheckStatus = esStatusDao.getClusterHealthStatus();
		} catch (Exception e) {
			healthCheckStatus = HealthCheckStatus.DOWN;
			description = "ES cluster error: " + e.getMessage();
			healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.ES, healthCheckStatus, null, description));
			return healthCheckInfos;
		}
		if (healthCheckStatus.equals(HealthCheckStatus.DOWN)) {
			description = "ES cluster is down";
		} else {
			description = "OK";
		}
		healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.ES, healthCheckStatus, null, description));
		return healthCheckInfos;
	}

	public Object getDao(ServletContext servletContext, Class<?> clazz) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);

		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(servletContext);

		return webApplicationContext.getBean(clazz);
	}

	private void getDistributionEngineCheck(ServletContext servletContext, List<HealthCheckInfo> healthCheckInfos) {

		DistributionEngineClusterHealth deDao = (DistributionEngineClusterHealth) getDao(servletContext, DistributionEngineClusterHealth.class);
		HealthCheckInfo healthCheckInfo = deDao.getHealthCheckInfo();

		healthCheckInfos.add(healthCheckInfo);

	}

	public boolean isDistributionEngineUp(ServletContext servletContext) {

		DistributionEngineClusterHealth deDao = (DistributionEngineClusterHealth) getDao(servletContext, DistributionEngineClusterHealth.class);
		HealthCheckInfo healthCheckInfo = deDao.getHealthCheckInfo();
		if (healthCheckInfo.getHealthCheckStatus().equals(HealthCheckStatus.DOWN)) {
			return false;
		}
		return true;
	}

	public List<HealthCheckInfo> getBeHealthCheckInfosStatus() {

		return lastBeHealthCheckInfos;

	}

	private List<HealthCheckInfo> getBeHealthCheckInfos() {

		log.trace("In getBeHealthCheckInfos");

		List<HealthCheckInfo> healthCheckInfos = new ArrayList<HealthCheckInfo>();

		// BE
		getBeHealthCheck(healthCheckInfos);

		// ES
		getEsHealthCheck(healthCheckInfos);

		// Titan
		getTitanHealthCheck(healthCheckInfos);

		// Distribution Engine
		getDistributionEngineCheck(healthCheckInfos);

		return healthCheckInfos;
	}

	private List<HealthCheckInfo> getBeHealthCheck(List<HealthCheckInfo> healthCheckInfos) {
		String appVersion = ExternalConfiguration.getAppVersion();
		String description = "OK";
		healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.BE, HealthCheckStatus.UP, appVersion, description));
		return healthCheckInfos;
	}

	public List<HealthCheckInfo> getEsHealthCheck(List<HealthCheckInfo> healthCheckInfos) {

		// ES health check and version
		HealthCheckStatus healthCheckStatus;
		String description;

		try {
			healthCheckStatus = esHealthCheckDao.getClusterHealthStatus();
		} catch (Exception e) {
			healthCheckStatus = HealthCheckStatus.DOWN;
			description = "ES cluster error: " + e.getMessage();
			healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.ES, healthCheckStatus, null, description));
			return healthCheckInfos;
		}
		if (healthCheckStatus.equals(HealthCheckStatus.DOWN)) {
			description = "ES cluster is down";
		} else {
			description = "OK";
		}
		healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.ES, healthCheckStatus, null, description));
		return healthCheckInfos;
	}

	public List<HealthCheckInfo> getTitanHealthCheck(List<HealthCheckInfo> healthCheckInfos) {
		// Titan health check and version
		String description;
		boolean isTitanUp;

		try {
			isTitanUp = titanGenericDao.isGraphOpen();
		} catch (Exception e) {
			description = "Titan error: " + e.getMessage();
			healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.TITAN, HealthCheckStatus.DOWN, null, description));
			return healthCheckInfos;
		}
		if (isTitanUp) {
			description = "OK";
			healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.TITAN, HealthCheckStatus.UP, null, description));
		} else {
			description = "Titan graph is down";
			healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.TITAN, HealthCheckStatus.DOWN, null, description));
		}
		return healthCheckInfos;
	}

	private void getDistributionEngineCheck(List<HealthCheckInfo> healthCheckInfos) {

		HealthCheckInfo healthCheckInfo = distributionEngineClusterHealth.getHealthCheckInfo();

		healthCheckInfos.add(healthCheckInfo);

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

	public class HealthCheckScheduledTask implements Runnable {

		List<UebHealthCheckCall> healthCheckCalls = new ArrayList<>();

		public HealthCheckScheduledTask() {

		}

		@Override
		public void run() {

			healthLogger.trace("Executing BE Health Check Task");

			List<HealthCheckInfo> beHealthCheckInfos = getBeHealthCheckInfos();
			boolean healthStatus = getAggregateBeStatus(beHealthCheckInfos);

			boolean lastHealthStatus = getAggregateBeStatus(lastBeHealthCheckInfos);

			if (lastHealthStatus != healthStatus) {
				log.trace("BE Health State Changed to {}. Issuing alarm / recovery alarm...", healthStatus);

				lastBeHealthCheckInfos = beHealthCheckInfos;
				logAlarm(healthStatus);

			} else {
				// check if we need to update the status's list in case one of
				// the statuses was changed
				if (true == anyStatusChanged(beHealthCheckInfos, lastBeHealthCheckInfos)) {
					lastBeHealthCheckInfos = beHealthCheckInfos;
				}

			}

		}

	}

	private void logAlarm(boolean lastHealthState) {
		if (lastHealthState == true) {
			BeEcompErrorManager.getInstance().logBeHealthCheckRecovery(BE_HEALTH_CHECK_STR);
		} else {
			BeEcompErrorManager.getInstance().logBeHealthCheckError(BE_HEALTH_CHECK_STR);
		}
	}

	private boolean getAggregateBeStatus(List<HealthCheckInfo> beHealthCheckInfos) {

		boolean status = true;

		for (HealthCheckInfo healthCheckInfo : beHealthCheckInfos) {
			if (healthCheckInfo.getHealthCheckStatus().equals(HealthCheckStatus.DOWN) && healthCheckInfo.getHealthCheckComponent() != HealthCheckComponent.DE) {
				status = false;
				break;
			}
		}
		return status;
	}

	public String getSiteMode() {
		return switchoverDetector.getSiteMode();
	}

	public boolean anyStatusChanged(List<HealthCheckInfo> beHealthCheckInfos, List<HealthCheckInfo> lastBeHealthCheckInfos) {

		boolean result = false;

		if (beHealthCheckInfos != null && lastBeHealthCheckInfos != null) {

			Map<HealthCheckComponent, HealthCheckStatus> currentValues = beHealthCheckInfos.stream().collect(Collectors.toMap(p -> p.getHealthCheckComponent(), p -> p.getHealthCheckStatus()));
			Map<HealthCheckComponent, HealthCheckStatus> lastValues = lastBeHealthCheckInfos.stream().collect(Collectors.toMap(p -> p.getHealthCheckComponent(), p -> p.getHealthCheckStatus()));

			if (currentValues != null && lastValues != null) {
				int currentSize = currentValues.size();
				int lastSize = lastValues.size();

				if (currentSize != lastSize) {
					result = true;
				} else {

					for (Entry<HealthCheckComponent, HealthCheckStatus> entry : currentValues.entrySet()) {
						HealthCheckComponent key = entry.getKey();
						HealthCheckStatus value = entry.getValue();

						if (false == lastValues.containsKey(key)) {
							result = true;
							break;
						}

						HealthCheckStatus lastHealthCheckStatus = lastValues.get(key);

						if (value != lastHealthCheckStatus) {
							result = true;
							break;
						}
					}
				}
			} else if (currentValues == null && lastValues == null) {
				result = false;
			} else {
				result = true;
			}

		} else if (beHealthCheckInfos == null && lastBeHealthCheckInfos == null) {
			result = false;
		} else {
			result = true;
		}

		return result;
	}
}
