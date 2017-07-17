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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngineClusterHealth;
import org.openecomp.sdc.be.components.distribution.engine.UebHealthCheckCall;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.IEsHealthCheckDao;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.switchover.detector.SwitchoverDetector;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckComponent;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component("healthCheckBusinessLogic")
public class HealthCheckBusinessLogic {

	protected static String BE_HEALTH_LOG_CONTEXT = "be.healthcheck";

	private static Logger healthLogger = LoggerFactory.getLogger(BE_HEALTH_LOG_CONTEXT);

	private static final String BE_HEALTH_CHECK_STR = "beHealthCheck";
	private static final String COMPONENT_CHANGED_MESSAGE = "BE Component %s state changed from %s to %s";

	@Resource
	private TitanGenericDao titanGenericDao;

	@Resource
	private IEsHealthCheckDao esHealthCheckDao;

	@Resource
	private DistributionEngineClusterHealth distributionEngineClusterHealth;

	@Resource
	private CassandraHealthCheck cassandraHealthCheck;

	@Autowired
	private SwitchoverDetector switchoverDetector;

	private static Logger log = LoggerFactory.getLogger(HealthCheckBusinessLogic.class.getName());

	private volatile List<HealthCheckInfo> prevBeHealthCheckInfos = null;

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

		prevBeHealthCheckInfos = getBeHealthCheckInfos();

		log.debug("After initializing prevBeHealthCheckInfos: {}", prevBeHealthCheckInfos);

		healthCheckScheduledTask = new HealthCheckScheduledTask();

		if (this.scheduledFuture == null) {
			this.scheduledFuture = this.healthCheckScheduler.scheduleAtFixedRate(healthCheckScheduledTask, 0, 3, TimeUnit.SECONDS);
		}

	}

	public boolean isDistributionEngineUp() {

		HealthCheckInfo healthCheckInfo = distributionEngineClusterHealth.getHealthCheckInfo();
		if (healthCheckInfo.getHealthCheckStatus().equals(HealthCheckStatus.DOWN)) {
			return false;
		}
		return true;
	}

	public List<HealthCheckInfo> getBeHealthCheckInfosStatus() {

		return prevBeHealthCheckInfos;

	}

	private List<HealthCheckInfo> getBeHealthCheckInfos() {

		log.trace("In getBeHealthCheckInfos");

		List<HealthCheckInfo> healthCheckInfos = new ArrayList<HealthCheckInfo>();

		// BE
		getBeHealthCheck(healthCheckInfos);

		/*// ES
		getEsHealthCheck(healthCheckInfos);*/

		// Titan
		getTitanHealthCheck(healthCheckInfos);

		// Distribution Engine
		getDistributionEngineCheck(healthCheckInfos);

		//Cassandra
		getCassandraHealthCheck(healthCheckInfos);

		// Amdocs
		getAmdocsHealthCheck(healthCheckInfos);

		return healthCheckInfos;
	}

	private List<HealthCheckInfo> getBeHealthCheck(List<HealthCheckInfo> healthCheckInfos) {
		String appVersion = ExternalConfiguration.getAppVersion();
		String description = "OK";
		healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.BE, HealthCheckStatus.UP, appVersion, description));
		return healthCheckInfos;
	}

	//Removed from aggregate HC - TDP 293490
/*	private List<HealthCheckInfo> getEsHealthCheck(List<HealthCheckInfo> healthCheckInfos) {

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
*/
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

	private List<HealthCheckInfo> getCassandraHealthCheck(List<HealthCheckInfo> healthCheckInfos)  {

		String description;
		boolean isCassandraUp;

		try {
			isCassandraUp = cassandraHealthCheck.getCassandraStatus();
		} catch (Exception e) {
			isCassandraUp = false;
			description = "Cassandra error: " + e.getMessage();
		}
		if (isCassandraUp) {
			description = "OK";
			healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.CASSANDRA, HealthCheckStatus.UP, null, description));
		} else {
			description = "Cassandra is down";
			healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.CASSANDRA, HealthCheckStatus.DOWN, null, description));
		}
		return healthCheckInfos;

	}

	private void getDistributionEngineCheck(List<HealthCheckInfo> healthCheckInfos) {

		HealthCheckInfo healthCheckInfo = distributionEngineClusterHealth.getHealthCheckInfo();

		healthCheckInfos.add(healthCheckInfo);

	}

	private List<HealthCheckInfo> getAmdocsHealthCheck(List<HealthCheckInfo> healthCheckInfos) {
		HealthCheckStatus healthCheckStatus;
		String description;
		Map<String, Object> amdocsHC = null;
		String version = null;
		List<HealthCheckInfo> componentsInfo = null;
		CloseableHttpClient httpClient = getHttpClient();
		String amdocsHealtchCheckUrl = buildHealthCheckUrl();
		HttpGet httpGet = new HttpGet(amdocsHealtchCheckUrl);
		CloseableHttpResponse beResponse;
		int beStatus;
		try {
			beResponse = httpClient.execute(httpGet);
			beStatus = beResponse.getStatusLine().getStatusCode();

			HttpEntity entity = beResponse.getEntity();
			String beJsonResponse = EntityUtils.toString(entity);
			Gson gson = new Gson();
			amdocsHC = gson.fromJson(beJsonResponse, Map.class);
			version = amdocsHC.get("sdcVersion") != null ? amdocsHC.get("sdcVersion").toString() : null;
			Object object = amdocsHC.get("componentsInfo");
			Type listType = new TypeToken<List<HealthCheckInfo>>(){}.getType();
			componentsInfo = gson.fromJson(object.toString(), listType);

			if (beStatus != HttpStatus.SC_OK) {
				healthCheckStatus = HealthCheckStatus.DOWN;
				StringBuilder sb = new StringBuilder();
				componentsInfo.forEach(x -> {
					if (x.getHealthCheckStatus()==HealthCheckStatus.DOWN){
						sb.append("Component "+x.getHealthCheckComponent().name()+" is Down,");
					}
				});
				//Removing the last comma
				description = sb.length()>0 
						? sb.substring(0, sb.length()-1) 
								: "Onboarding is Down, specific reason unknown";//No Amdocs inner component returned DOWN, but the status of Amdocs HC is still DOWN.
			} else {
				healthCheckStatus = HealthCheckStatus.UP;
				description = "OK";


			}

		} catch (Exception e) {
			healthCheckStatus = HealthCheckStatus.DOWN;
			description = "Onboarding unexpected response: " + e.getMessage();
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.ON_BOARDING, healthCheckStatus, version, description, componentsInfo));
		return healthCheckInfos;
	}

	private CloseableHttpClient getHttpClient() {
		int timeout = 3000;
		RequestConfig.Builder requestBuilder = RequestConfig.custom();
		requestBuilder.setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout);

		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setDefaultRequestConfig(requestBuilder.build());
		return builder.build();
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

			List<HealthCheckInfo> currentBeHealthCheckInfos = getBeHealthCheckInfos();
			boolean healthStatus = getAggregateBeStatus(currentBeHealthCheckInfos);

			boolean prevHealthStatus = getAggregateBeStatus(prevBeHealthCheckInfos);

			boolean anyStatusChanged = anyStatusChanged(currentBeHealthCheckInfos, prevBeHealthCheckInfos);

			if (prevHealthStatus != healthStatus || anyStatusChanged) {
				log.trace("BE Health State Changed to {}. Issuing alarm / recovery alarm...", healthStatus);

				prevBeHealthCheckInfos = currentBeHealthCheckInfos;
				logAlarm(healthStatus);
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

	}

	private void logAlarm(boolean prevHealthState) {
		if (prevHealthState) {
			BeEcompErrorManager.getInstance().logBeHealthCheckRecovery(BE_HEALTH_CHECK_STR);
		} else {
			BeEcompErrorManager.getInstance().logBeHealthCheckError(BE_HEALTH_CHECK_STR);
		}
	}

	private void logAlarm(String componentChangedMsg) {
		BeEcompErrorManager.getInstance().logBeHealthCheckRecovery(componentChangedMsg);
	}


	public String getSiteMode() {
		return switchoverDetector.getSiteMode();
	}

	public boolean anyStatusChanged(List<HealthCheckInfo> beHealthCheckInfos, List<HealthCheckInfo> prevBeHealthCheckInfos) {

		boolean result = false;

		if (beHealthCheckInfos != null && prevBeHealthCheckInfos != null) {

			Map<HealthCheckComponent, HealthCheckStatus> currentValues = beHealthCheckInfos.stream().collect(Collectors.toMap(p -> p.getHealthCheckComponent(), p -> p.getHealthCheckStatus()));
			Map<HealthCheckComponent, HealthCheckStatus> prevValues = prevBeHealthCheckInfos.stream().collect(Collectors.toMap(p -> p.getHealthCheckComponent(), p -> p.getHealthCheckStatus()));

			if (currentValues != null && prevValues != null) {
				int currentSize = currentValues.size();
				int prevSize = prevValues.size();

				if (currentSize != prevSize) {

					result = true; //extra/missing component

					Map<HealthCheckComponent, HealthCheckStatus> notPresent = null;
					if (currentValues.keySet().containsAll(prevValues.keySet())) {
						notPresent = new HashMap<>(currentValues);
						notPresent.keySet().removeAll(prevValues.keySet());
					} else {
						notPresent = new HashMap<>(prevValues);
						notPresent.keySet().removeAll(currentValues.keySet());
					}

					for (HealthCheckComponent component : notPresent.keySet()) {
						logAlarm(String.format(COMPONENT_CHANGED_MESSAGE, component, prevValues.get(component), currentValues.get(component)));
					}
					//					HealthCheckComponent changedComponent = notPresent.keySet().iterator().next();

				} else {

					for (Entry<HealthCheckComponent, HealthCheckStatus> entry : currentValues.entrySet()) {
						HealthCheckComponent key = entry.getKey();
						HealthCheckStatus value = entry.getValue();

						if (!prevValues.containsKey(key)) {
							result = true; //component missing
							logAlarm(String.format(COMPONENT_CHANGED_MESSAGE, key, prevValues.get(key), currentValues.get(key)));
							break;
						}

						HealthCheckStatus prevHealthCheckStatus = prevValues.get(key);

						if (value != prevHealthCheckStatus) {
							result = true; //component status changed
							logAlarm(String.format(COMPONENT_CHANGED_MESSAGE, key, prevValues.get(key), currentValues.get(key)));
							break;
						}
					}
				}
			}

		} else if (beHealthCheckInfos == null && prevBeHealthCheckInfos == null) {
			result = false;
		} else {
			logAlarm(String.format(COMPONENT_CHANGED_MESSAGE, "", prevBeHealthCheckInfos == null ? "null" : "true", prevBeHealthCheckInfos == null ? "true" : "null"));
			result = true;
		}

		return result;
	}

	private String buildHealthCheckUrl() {

		Configuration.OnboardingConfig onboardingConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getOnboarding();

		String protocol = onboardingConfig.getProtocol();
		String host = onboardingConfig.getHost();
		Integer port = onboardingConfig.getPort();
		String uri = onboardingConfig.getHealthCheckUri();

		return protocol + "://" + host + ":" + port + uri;
	}
}
