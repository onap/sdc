/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (c) 2019 Samsung
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

package org.openecomp.sdc.fe.listen;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.listener.AppContextListener;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.impl.PluginStatusBL;
import org.openecomp.sdc.fe.monitoring.FeMonitoringService;
import org.openecomp.sdc.fe.servlets.HealthCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FEAppContextListener extends AppContextListener implements ServletContextListener {

    private static final int HEALTH_CHECK_INTERVAL = 5;
    private static final int PROBE_INTERVAL = 15;
    private static Logger log = LoggerFactory.getLogger(FEAppContextListener.class.getName());

    public void contextInitialized(ServletContextEvent context) {

        super.contextInitialized(context);

        ConfigurationManager configurationManager = new ConfigurationManager(
                ExternalConfiguration.getConfigurationSource());
        log.debug("loading configuration from configDir:{} appName:{}", ExternalConfiguration.getConfigDir(),
                ExternalConfiguration.getAppName());
        context.getServletContext().setAttribute(Constants.CONFIGURATION_MANAGER_ATTR, configurationManager);

        try {
            PluginStatusBL pbl = new PluginStatusBL(buildRestClient());
            context.getServletContext().setAttribute(Constants.PLUGIN_BL_COMPONENT, pbl);
        } catch (SSLException e) {
            log.debug("ERROR: Build rest client failed because ", e);
            return;
        }

        // Health Check service
        HealthCheckService hcs = new HealthCheckService(context.getServletContext());
        hcs.start(configurationManager.getConfiguration().getHealthCheckIntervalInSeconds(HEALTH_CHECK_INTERVAL));
        context.getServletContext().setAttribute(Constants.HEALTH_CHECK_SERVICE_ATTR, hcs);

        // Monitoring service
        FeMonitoringService fms = new FeMonitoringService(context.getServletContext());
        fms.start(configurationManager.getConfiguration().getSystemMonitoring().getProbeIntervalInSeconds(PROBE_INTERVAL));

        if (configurationManager.getConfiguration() == null) {
            log.debug("ERROR: configuration was not properly loaded");
            return;
        }

        ExecutorService executorPool = Executors
                .newFixedThreadPool(configurationManager.getConfiguration().getThreadpoolSize());
        context.getServletContext().setAttribute(Constants.THREAD_EXECUTOR_ATTR, executorPool);

        log.debug("After executing {}", this.getClass());
    }

    public void contextDestroyed(ServletContextEvent context) {

        ExecutorService executorPool = (ExecutorService) context.getServletContext()
                .getAttribute(Constants.THREAD_EXECUTOR_ATTR);
        if (executorPool != null) {
            executorPool.shutdown();
        }

        super.contextDestroyed(context);

    }

    private CloseableHttpClient buildRestClient() throws SSLException {
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                SSLContext.getDefault(), NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> registry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", new PlainConnectionSocketFactory()).register("https", sslsf)
                    .build();
            PoolingHttpClientConnectionManager cm =
                new PoolingHttpClientConnectionManager(registry);
            return HttpClients.custom().setSSLSocketFactory(sslsf).setConnectionManager(cm).build();
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new SSLException(e);
        }
    }
}
