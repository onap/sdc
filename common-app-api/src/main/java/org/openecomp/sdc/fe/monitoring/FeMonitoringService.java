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

package org.openecomp.sdc.fe.monitoring;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.http.client.api.HttpRequest;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.common.http.config.HttpClientConfig;
import org.openecomp.sdc.common.http.config.Timeouts;
import org.openecomp.sdc.common.monitoring.MonitoringEvent;
import org.openecomp.sdc.common.monitoring.MonitoringMetricsFetcher;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.common.log.wrappers.Logger;

import javax.servlet.ServletContext;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class FeMonitoringService {

	private static final String URL = "%s://%s:%s/sdc2/rest/monitoring";
    private static Logger monitoringLogger = Logger.getLogger("asdc.fe.monitoring.service");
    private static Logger log = Logger.getLogger(FeMonitoringService.class.getName());
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private class MonitoringScheduledTask implements Runnable {
		@Override
		public void run() {
			monitoringLogger.trace("Executing FE Monitoring Task - Start");
			MonitoringEvent monitoringMetrics = MonitoringMetricsFetcher.getInstance().getMonitoringMetrics();
			processMonitoringEvent(monitoringMetrics);
			monitoringLogger.trace("Executing FE Monitoring Task - Status = {}", monitoringMetrics.toString());
		}
	}

	/**
	 * This executor will execute the Monitoring task.
	 */
	ScheduledExecutorService monitoringExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "FE-Monitoring-Thread");
		}
	});
	private ServletContext context;

	public FeMonitoringService(ServletContext context) {
		this.context = context;
	}

	public void start(int interval) {
		Configuration config = ((ConfigurationManager) context.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR))
				.getConfiguration();
		if (config.getSystemMonitoring().getEnabled()) {
			log.info("FE monitoring service enabled, interval is {} seconds", interval);
			this.monitoringExecutor.scheduleAtFixedRate(new MonitoringScheduledTask(), 0, interval, TimeUnit.SECONDS);
		} else {
			log.info("FE monitoring service is disabled");
		}
	}

	private void processMonitoringEvent(MonitoringEvent monitoringMetrics) {
		try {
			Configuration config = ((ConfigurationManager) context.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR))
					.getConfiguration();
			String redirectedUrl = String.format(URL, config.getBeProtocol(), config.getBeHost(),
			        Constants.HTTPS.equals(config.getBeProtocol()) ? config.getBeSslPort() : config.getBeHttpPort());

			int timeout = 3000;
			String monitoringMetricsJson = gson.toJson(monitoringMetrics);
			HttpEntity myEntity = new StringEntity(monitoringMetricsJson, ContentType.APPLICATION_JSON);
			HttpResponse<String> resposne = HttpRequest.post(redirectedUrl, myEntity, new HttpClientConfig(new Timeouts(timeout, timeout)));
            int beResponseStatus = resposne.getStatusCode();
            if (beResponseStatus != HttpStatus.SC_OK) {
                monitoringLogger.error("Unexpected HTTP response from BE : {}", beResponseStatus);
            }
		} catch (Exception e) {
		    monitoringLogger.error("Monitoring BE failed with exception ", e);
		}
	}
}
