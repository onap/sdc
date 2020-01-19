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

package org.openecomp.sdc.fe.listen;

import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.listener.AppContextListener;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.impl.PluginStatusBL;
import org.openecomp.sdc.fe.monitoring.FeMonitoringService;
import org.openecomp.sdc.fe.impl.HealthCheckService;
import org.openecomp.sdc.common.log.wrappers.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FEAppContextListener extends AppContextListener implements ServletContextListener {

	private static Logger log = Logger.getLogger(FEAppContextListener.class.getName());
    private static final int HEALTH_CHECHK_INTERVALE = 5;
    private static final int PROBE_INTERVALE = 15;

    public void contextInitialized(ServletContextEvent context) {

        super.contextInitialized(context);

        ConfigurationManager configurationManager = new ConfigurationManager(
                ExternalConfiguration.getConfigurationSource());
        log.debug("loading configuration from configDir:{} appName:{}", ExternalConfiguration.getConfigDir(),
                ExternalConfiguration.getAppName());
        context.getServletContext().setAttribute(Constants.CONFIGURATION_MANAGER_ATTR, configurationManager);

        PluginStatusBL pbl = new PluginStatusBL();
        context.getServletContext().setAttribute(Constants.PLUGIN_BL_COMPONENT, pbl);

        // Health Check service
        HealthCheckService hcs = new HealthCheckService(context.getServletContext());
        hcs.start(configurationManager.getConfiguration().getHealthCheckIntervalInSeconds(HEALTH_CHECHK_INTERVALE));
        context.getServletContext().setAttribute(Constants.HEALTH_CHECK_SERVICE_ATTR, hcs);

        // Monitoring service
        FeMonitoringService fms = new FeMonitoringService(context.getServletContext());
        fms.start(configurationManager.getConfiguration().getSystemMonitoring().getProbeIntervalInSeconds(PROBE_INTERVALE));

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

}
