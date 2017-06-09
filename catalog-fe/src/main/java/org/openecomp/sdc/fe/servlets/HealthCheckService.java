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
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckWrapper;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckComponent;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.FeEcompErrorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
			int beStatus;
			String feAggHealthCheck;
			try {
				beResponse = httpClient.execute(httpGet);
				beStatus = beResponse.getStatusLine().getStatusCode();
				String beJsonResponse = EntityUtils.toString(beResponse.getEntity());
				feAggHealthCheck = getFeHealthCheckInfos(gson, beJsonResponse);
			} catch (Exception e) {
				log.error("Health Check error when trying to connect to BE", e);
				String beDowneResponse = gson.toJson(getBeDownCheckInfos());
				return new HealthStatus(500, beDowneResponse);
			}
			return new HealthStatus(beStatus, feAggHealthCheck);
		} catch (Exception e) {
			FeEcompErrorManager.getInstance().processEcompError(EcompErrorName.FeHealthCheckGeneralError, "Unexpected FE Health check error");
			FeEcompErrorManager.getInstance().logFeHealthCheckGeneralError("Unexpected FE Health check error");
			log.error("Unexpected FE health check error {}", e.getMessage());
			return new HealthStatus(500, e.getMessage());
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
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

	private String getFeHealthCheckInfos(Gson gson, String responseString) {
		Type wrapperType = new TypeToken<HealthCheckWrapper>() {
		}.getType();
		HealthCheckWrapper healthCheckWrapper = gson.fromJson(responseString, wrapperType);
		String appVersion = ExternalConfiguration.getAppVersion();
		String description = "OK";
		healthCheckWrapper.getComponentsInfo()
				.add(new HealthCheckInfo(HealthCheckComponent.FE, HealthCheckStatus.UP, appVersion, description));
		return gson.toJson(healthCheckWrapper);
	}

	private HealthCheckWrapper getBeDownCheckInfos() {
		List<HealthCheckInfo> healthCheckInfos = new ArrayList<HealthCheckInfo>();
		healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.FE, HealthCheckStatus.UP,
				ExternalConfiguration.getAppVersion(), "OK"));
		healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.BE, HealthCheckStatus.DOWN, null, null));
		healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.ES, HealthCheckStatus.UNKNOWN, null, null));
		healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.TITAN, HealthCheckStatus.UNKNOWN, null, null));
		healthCheckInfos.add(new HealthCheckInfo(HealthCheckComponent.DE, HealthCheckStatus.UNKNOWN, null, null));
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
}
