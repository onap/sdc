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
package org.openecomp.sdc.be.listen;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.monitoring.BeMonitoringService;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.listener.AppContextListener;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class BEAppContextListener extends AppContextListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger(BEAppContextListener.class);

    public void contextInitialized(ServletContextEvent context) {
        super.contextInitialized(context);
        ConfigurationManager configurationManager = BeConfigurationBootstrap.start(ExternalConfiguration.getConfigurationSource());
        context.getServletContext().setAttribute(Constants.CONFIGURATION_MANAGER_ATTR, configurationManager);
        WebAppContextWrapper webAppContextWrapper = new WebAppContextWrapper();
        context.getServletContext().setAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR, webAppContextWrapper);
        context.getServletContext()
            .setAttribute(Constants.ASDC_RELEASE_VERSION_ATTR, context.getServletContext().getAttribute(Constants.APPLICATION_VERSION));
        // Monitoring service
        BeMonitoringService bms = new BeMonitoringService(context.getServletContext());
        bms.start(configurationManager.getConfiguration().getSystemMonitoring().getProbeIntervalInSeconds(15));
        log.debug("After executing {}", this.getClass());
    }
}
