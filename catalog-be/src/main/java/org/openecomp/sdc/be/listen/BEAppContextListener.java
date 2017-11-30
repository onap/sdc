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

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.impl.DownloadArtifactLogic;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.monitoring.BeMonitoringService;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.listener.AppContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BEAppContextListener extends AppContextListener implements ServletContextListener {

	private static final String MANIFEST_FILE_NAME = "/META-INF/MANIFEST.MF";
	private static Logger log = LoggerFactory.getLogger(BEAppContextListener.class.getName());
	
	public void contextInitialized(ServletContextEvent context) {

		super.contextInitialized(context);

		ConfigurationManager configurationManager = new ConfigurationManager(ExternalConfiguration.getConfigurationSource());
		log.debug("loading configuration from configDir: {} appName: {}", ExternalConfiguration.getConfigDir(), ExternalConfiguration.getAppName());

		context.getServletContext().setAttribute(Constants.CONFIGURATION_MANAGER_ATTR, configurationManager);

		WebAppContextWrapper webAppContextWrapper = new WebAppContextWrapper();
		context.getServletContext().setAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR, webAppContextWrapper);

		DownloadArtifactLogic downloadArtifactLogic = new DownloadArtifactLogic();
		context.getServletContext().setAttribute(Constants.DOWNLOAD_ARTIFACT_LOGIC_ATTR, downloadArtifactLogic);

		context.getServletContext().setAttribute(Constants.ASDC_RELEASE_VERSION_ATTR, getVersionFromManifest(context));

		// Monitoring service
		BeMonitoringService bms = new BeMonitoringService(context.getServletContext());
		bms.start(configurationManager.getConfiguration().getSystemMonitoring().getProbeIntervalInSeconds(15));

		log.debug("After executing {}", this.getClass());

	}
	
	private String getVersionFromManifest(ServletContextEvent context) {
		ServletContext servletContext = context.getServletContext();
		InputStream inputStream = servletContext.getResourceAsStream(MANIFEST_FILE_NAME);

		String version = null;
		try {
			Manifest mf = new Manifest(inputStream);
			Attributes atts = mf.getMainAttributes();
			version = atts.getValue(Constants.ASDC_RELEASE_VERSION_ATTR);
			if (version == null || version.isEmpty()) {
				log.warn("failed to read ASDC version from MANIFEST.");
			} else {
				log.info("ASDC version from MANIFEST is {}", version);
			}

		} catch (IOException e) {
			log.warn("failed to read ASDC version from MANIFEST", e.getMessage());
		}

		return version;
	}

}
