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

package org.openecomp.sdc.be.monitoring;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.monitoring.MonitoringEvent;
import org.openecomp.sdc.common.monitoring.MonitoringMetricsFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BeMonitoringService {

	private static final String URL = "%s://%s:%s/sdc2/rest/monitoring";
	private static Logger monitoringLogger = LoggerFactory.getLogger("asdc.be.monitoring.service");
	private static Logger log = LoggerFactory.getLogger(BeMonitoringService.class.getName());
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private class MonitoringScheduledTask implements Runnable {
		@Override
		public void run() {
			monitoringLogger.trace("Executing BE Monitoring Task - Start");
			MonitoringEvent monitoringMetrics = MonitoringMetricsFetcher.getInstance().getMonitoringMetrics();
			processMonitoringEvent(monitoringMetrics);
			monitoringLogger.trace("Executing BE Monitoring Task - Status = {}", monitoringMetrics.toString());
		}
	}

	/**
	 * This executor will execute the Monitoring task.
	 */
	ScheduledExecutorService monitoringExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "BE-Monitoring-Thread");
		}
	});
	private ServletContext context;

	public BeMonitoringService(ServletContext context) {
		this.context = context;
	}

	public void start(int interval) {
		Configuration config = ((ConfigurationManager) context.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR))
				.getConfiguration();
		if (config.getSystemMonitoring().getEnabled()) {
			log.info("BE monitoring service is enabled, interval is {} seconds", interval);
			this.monitoringExecutor.scheduleAtFixedRate(new MonitoringScheduledTask(), 0, interval, TimeUnit.SECONDS);
		} else {
			log.info("BE monitoring service is disabled");
		}
	}

	private void processMonitoringEvent(MonitoringEvent monitoringMetrics) {
		CloseableHttpClient httpClient = null;
		try {
			Configuration config = ((ConfigurationManager) context.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR))
					.getConfiguration();
			String redirectedUrl = String.format(URL, config.getBeProtocol(), config.getBeFqdn(),
					config.getBeHttpPort());
			httpClient = getHttpClient(config);
			HttpPost httpPost = new HttpPost(redirectedUrl);
			String monitoringMetricsJson = gson.toJson(monitoringMetrics);
			HttpEntity myEntity = new StringEntity(monitoringMetricsJson);
			httpPost.setEntity(myEntity);
			httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
			int beResponseStatus;
			CloseableHttpResponse beResponse;
			try {
				beResponse = httpClient.execute(httpPost);
				beResponseStatus = beResponse.getStatusLine().getStatusCode();
				if (beResponseStatus != HttpStatus.SC_OK) {
					monitoringLogger.error("Unexpected HTTP response from BE : {}", beResponseStatus);
				}
			} catch (Exception e) {
				monitoringLogger.error("Monitoring error when trying to connect to BE", e);
			}
		} catch (Exception e) {
			monitoringLogger.error("Unexpected monitoring error", e);
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private CloseableHttpClient getHttpClient(Configuration config) {
		int timeout = 3000;
		RequestConfig.Builder requestBuilder = RequestConfig.custom();
		requestBuilder.setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout);

		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setDefaultRequestConfig(requestBuilder.build());
		return builder.build();
	}
}
