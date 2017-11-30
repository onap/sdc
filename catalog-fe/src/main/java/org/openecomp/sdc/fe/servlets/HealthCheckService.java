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

package org.openecomp.sdc.fe.servlets;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.common.api.HealthCheckWrapper;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.util.HealthCheckUtil;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.FeEcompErrorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class HealthCheckService {

	private class HealthStatus {
		public String body;
		public int statusCode;

		public HealthStatus(int code, String body) {
			this.body = body;
			this.statusCode = code;
		}
	}

	private static final String URL = "%s://%s:%s/sdc2/rest/healthCheck";
	private static Logger healthLogger = LoggerFactory.getLogger("asdc.fe.healthcheck");
	private static Logger log = LoggerFactory.getLogger(HealthCheckService.class.getName());

	private HealthStatus lastHealthStatus = new HealthStatus(500, "{}");
	private final List<String> healthCheckFeComponents = Arrays.asList(Constants.HC_COMPONENT_ON_BOARDING, Constants.HC_COMPONENT_DCAE);

	private class HealthCheckScheduledTask implements Runnable {
		@Override
		public void run() {
			healthLogger.trace("Executing FE Health Check Task - Start");
			HealthStatus currentHealth = checkHealth();
			int currentHealthStatus = currentHealth.statusCode;
			healthLogger.trace("Executing FE Health Check Task - Status = {}", currentHealthStatus);

			// In case health status was changed, issue alarm/recovery
			if (currentHealthStatus != lastHealthStatus.statusCode) {
				log.trace("FE Health State Changed to {}. Issuing alarm / recovery alarm...", currentHealthStatus);
				logFeAlarm(currentHealthStatus);
			}

			// Anyway, update latest response
			lastHealthStatus = currentHealth;
		}
	}

	/**
	 * This executor will execute the health check task.
	 */
	ScheduledExecutorService healthCheckExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "FE-Health-Check-Thread");
		}
	});
	private ServletContext context;

	public HealthCheckService(ServletContext context) {
		this.context = context;
	}

	public void start(int interval) {
		this.healthCheckExecutor.scheduleAtFixedRate(new HealthCheckScheduledTask(), 0, interval, TimeUnit.SECONDS);
	}

	/**
	 * To be used by the HealthCheckServlet
	 * 
	 * @return
	 */
	public Response getFeHealth() {
		return this.buildResponse(lastHealthStatus.statusCode, lastHealthStatus.body);
	}

	private HealthStatus checkHealth() {
		CloseableHttpClient httpClient = null;
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			Configuration config = ((ConfigurationManager) context.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR))
					.getConfiguration();
			String redirectedUrl = String.format(URL, config.getBeProtocol(), config.getBeHost(),
					config.getBeHttpPort());
			httpClient = getHttpClient(config);
			HttpGet httpGet = new HttpGet(redirectedUrl);
			CloseableHttpResponse beResponse;
			HealthCheckWrapper feAggHealthCheck;
			try {
				beResponse = httpClient.execute(httpGet);
				log.debug("HC call to BE - status code is {}", beResponse.getStatusLine().getStatusCode());
				String beJsonResponse = EntityUtils.toString(beResponse.getEntity());
				feAggHealthCheck = getFeHealthCheckInfos(gson, beJsonResponse);
			} catch (Exception e) {
				log.debug("Health Check error when trying to connect to BE or external FE. Error: {}", e.getMessage());
				log.error("Health Check error when trying to connect to BE or external FE.", e);
				String beDowneResponse = gson.toJson(getBeDownCheckInfos());
				return new HealthStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR, beDowneResponse);
			}
			
			//Getting aggregate FE status
			boolean aggregateFeStatus = HealthCheckUtil.getAggregateStatus(feAggHealthCheck.getComponentsInfo());
			return new HealthStatus(aggregateFeStatus ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR, gson.toJson(feAggHealthCheck));
		} catch (Exception e) {
			FeEcompErrorManager.getInstance().processEcompError(EcompErrorName.FeHealthCheckGeneralError, "Unexpected FE Health check error");
			FeEcompErrorManager.getInstance().logFeHealthCheckGeneralError("Unexpected FE Health check error");
			log.error("Unexpected FE health check error {}", e.getMessage());
			return new HealthStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					log.error("Couldn't close HC HTTP Client: ", e);
				}
			}
		}
	}





	private Response buildResponse(int status, String jsonResponse) {
		healthLogger.trace("FE and BE health check status: {}", jsonResponse);
		return Response.status(status).entity(jsonResponse).build();
	}

	private void logFeAlarm(int lastFeStatus) {

		switch (lastFeStatus) {
		case 200:
			FeEcompErrorManager.getInstance().processEcompError(EcompErrorName.FeHealthCheckRecovery,
					"FE Health Recovered");
			FeEcompErrorManager.getInstance().logFeHealthCheckRecovery("FE Health Recovered");
			break;
		case 500:
			FeEcompErrorManager.getInstance().processEcompError(EcompErrorName.FeHealthCheckConnectionError,
					"Connection with ASDC-BE is probably down");
			FeEcompErrorManager.getInstance().logFeHealthCheckError("Connection with ASDC-BE is probably down");
			break;
		default:
			break;
		}

	}

	private HealthCheckWrapper getFeHealthCheckInfos(Gson gson, String responseString) {
		Type wrapperType = new TypeToken<HealthCheckWrapper>() {
		}.getType();
		HealthCheckWrapper healthCheckWrapper = gson.fromJson(responseString, wrapperType);
		String appVersion = ExternalConfiguration.getAppVersion();
		String description = "OK";
		healthCheckWrapper.getComponentsInfo()
				.add(new HealthCheckInfo(Constants.HC_COMPONENT_FE, HealthCheckStatus.UP, appVersion, description));

		//add hosted components fe component
		for (String component: healthCheckFeComponents) {
			List<HealthCheckInfo> feComponentsInfo = addHostedComponentsFeHealthCheck(component);
			HealthCheckInfo baseComponentHCInfo = healthCheckWrapper.getComponentsInfo().stream().filter(c -> c.getHealthCheckComponent().equals(component)).findFirst().orElse(null);
			if (baseComponentHCInfo != null) {
				if (baseComponentHCInfo.getComponentsInfo() == null) {
					baseComponentHCInfo.setComponentsInfo(new ArrayList<>());
				}
				baseComponentHCInfo.getComponentsInfo().addAll(feComponentsInfo);
				boolean status = HealthCheckUtil.getAggregateStatus(baseComponentHCInfo.getComponentsInfo());
				baseComponentHCInfo.setHealthCheckStatus(status ? HealthCheckStatus.UP : HealthCheckStatus.DOWN);

				String componentsDesc = HealthCheckUtil.getAggregateDescription(baseComponentHCInfo.getComponentsInfo(), baseComponentHCInfo.getDescription());
				if (componentsDesc.length() > 0) { //aggregated description contains all the internal components desc
					baseComponentHCInfo.setDescription(componentsDesc);
				}
			} else {
				log.error("{} not exists in HealthCheck info", component);
			}
		}
		return healthCheckWrapper;
	}

	private List<HealthCheckInfo> addHostedComponentsFeHealthCheck(String baseComponent) {
		Configuration config = ((ConfigurationManager) context.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR))
				.getConfiguration();

		String healthCheckUrl = null;
		switch(baseComponent) {
			case Constants.HC_COMPONENT_ON_BOARDING:
				healthCheckUrl = buildOnboardingHealthCheckUrl(config);
				break;
			case Constants.HC_COMPONENT_DCAE:
				healthCheckUrl = buildDcaeHealthCheckUrl(config);
				break;
		}

		String description;

		if (healthCheckUrl != null) {
			ObjectMapper mapper = new ObjectMapper();
			CloseableHttpClient httpClient = getHttpClient(config);
			HttpGet httpGet = new HttpGet(healthCheckUrl);
			CloseableHttpResponse beResponse;

			try {
				beResponse = httpClient.execute(httpGet);
				int beStatus = beResponse.getStatusLine().getStatusCode();
				if (beStatus == HttpStatus.SC_OK || beStatus == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
					try {
						String beJsonResponse = EntityUtils.toString(beResponse.getEntity());

						Map<String, Object> healthCheckMap = mapper.readValue(beJsonResponse, new TypeReference<Map<String, Object>>(){});
						if (healthCheckMap.containsKey("componentsInfo")) {
							List<HealthCheckInfo> componentsInfo = mapper.convertValue(healthCheckMap.get("componentsInfo"), new TypeReference<List<HealthCheckInfo>>() {});
							return componentsInfo;
						} else {
							description = "Internal components are missing";
						}
					} catch (JsonSyntaxException e) {
						log.error("{} Unexpected response body ", baseComponent, e);
						description = baseComponent + " Unexpected response body. Response code: " + beStatus;
					}
				} else {
					description = "Response code: " + beStatus;
					log.trace("{} Health Check Response code: {}", baseComponent, beStatus);
				}
			} catch (Exception e) {
				log.error("{} Unexpected response ", baseComponent, e);
				description = baseComponent + " Unexpected response: " + e.getMessage();
			}
		} else {
			description = baseComponent + " health check Configuration is missing";
		}

		return Arrays.asList(new HealthCheckInfo(Constants.HC_COMPONENT_FE, HealthCheckStatus.DOWN, null, description));
	}

	private HealthCheckWrapper getBeDownCheckInfos() {
		List<HealthCheckInfo> healthCheckInfos = new ArrayList<HealthCheckInfo>();
		healthCheckInfos.add(new HealthCheckInfo(Constants.HC_COMPONENT_FE, HealthCheckStatus.UP,
				ExternalConfiguration.getAppVersion(), "OK"));
		healthCheckInfos.add(new HealthCheckInfo(Constants.HC_COMPONENT_BE, HealthCheckStatus.DOWN, null, null));
		healthCheckInfos.add(new HealthCheckInfo(Constants.HC_COMPONENT_TITAN, HealthCheckStatus.UNKNOWN, null, null));
		healthCheckInfos.add(new HealthCheckInfo(Constants.HC_COMPONENT_CASSANDRA, HealthCheckStatus.UNKNOWN, null, null));
		healthCheckInfos.add(new HealthCheckInfo(Constants.HC_COMPONENT_DISTRIBUTION_ENGINE, HealthCheckStatus.UNKNOWN, null, null));
		healthCheckInfos.add(new HealthCheckInfo(Constants.HC_COMPONENT_ON_BOARDING, HealthCheckStatus.UNKNOWN, null, null));
		healthCheckInfos.add(new HealthCheckInfo(Constants.HC_COMPONENT_DCAE, HealthCheckStatus.UNKNOWN, null, null));
		HealthCheckWrapper hcWrapper = new HealthCheckWrapper(healthCheckInfos, "UNKNOWN", "UNKNOWN");
		return hcWrapper;
	}

	private CloseableHttpClient getHttpClient(Configuration config) {
		int timeout = 3000;
		int socketTimeout = config.getHealthCheckSocketTimeoutInMs(5000);
		RequestConfig.Builder requestBuilder = RequestConfig.custom();
		requestBuilder.setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(socketTimeout);

		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setDefaultRequestConfig(requestBuilder.build());
		return builder.build();
	}

	private String buildOnboardingHealthCheckUrl(Configuration config) {

		Configuration.OnboardingConfig onboardingConfig = config.getOnboarding();

		if (onboardingConfig != null) {
			String protocol = onboardingConfig.getProtocol();
			String host = onboardingConfig.getHost();
			Integer port = onboardingConfig.getPort();
			String uri = onboardingConfig.getHealthCheckUri();

			return protocol + "://" + host + ":" + port + uri;
		}

		log.error("onboarding health check configuration is missing.");
		return null;
	}

	private String buildDcaeHealthCheckUrl(Configuration config) {

		Configuration.DcaeConfig dcaeConfig = config.getDcae();

		if (dcaeConfig != null) {
			String protocol = dcaeConfig.getProtocol();
			String host = dcaeConfig.getHost();
			Integer port = dcaeConfig.getPort();
			String uri = dcaeConfig.getHealthCheckUri();

			return protocol + "://" + host + ":" + port + uri;
		}

		log.error("dcae health check configuration is missing.");
		return null;
	}
}
