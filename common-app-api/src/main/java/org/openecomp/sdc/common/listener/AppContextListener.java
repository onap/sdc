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

package org.openecomp.sdc.common.listener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppContextListener implements ServletContextListener {

	private static Logger log = LoggerFactory.getLogger(AppContextListener.class.getName());

	public void contextInitialized(ServletContextEvent context) {

		log.debug("ServletContextListener initialized ");

		log.debug("After read values from Manifest {}", getManifestInfo(context.getServletContext()));

		Map<String, String> manifestAttr = getManifestInfo(context.getServletContext());

		String appName = setAndGetAttributeInContext(context, manifestAttr, Constants.APPLICATION_NAME);
		String appVersion = setAndGetAttributeInContext(context, manifestAttr, Constants.APPLICATION_VERSION);

		ExternalConfiguration.setAppName(appName);
		ExternalConfiguration.setAppVersion(appVersion);
		String configHome = System.getProperty(Constants.CONFIG_HOME);
		ExternalConfiguration.setConfigDir(configHome);

		String appConfigDir = configHome + File.separator + appName;
		// ChangeListener changeListener = new ChangeListener();
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
				appConfigDir);

		context.getServletContext().setAttribute(Constants.CONFIGURATION_SOURCE_ATTR, configurationSource);

		ExternalConfiguration.setConfigurationSource(configurationSource);

		ExternalConfiguration.listenForChanges();

	}

	public void contextDestroyed(ServletContextEvent context) {

		log.debug("ServletContextListener destroyed");
		ExternalConfiguration.stopListenForFileChanges();
	}

	private String setAndGetAttributeInContext(ServletContextEvent context, Map<String, String> manifestAttr,
			String attr) {

		String name = manifestAttr.get(attr);
		if (name != null) {
			context.getServletContext().setAttribute(attr, name);
		}

		return name;
	}

	public static Map<String, String> getManifestInfo(ServletContext application) {

		Map<String, String> result = new HashMap<String, String>();
		InputStream inputStream = null;
		try {

			inputStream = application.getResourceAsStream("/META-INF/MANIFEST.MF");

			Manifest manifest = new Manifest(inputStream);

			Attributes attr = manifest.getMainAttributes();
			String name = attr.getValue("Implementation-Title");
			if (name != null) {
				result.put(Constants.APPLICATION_NAME, name);
			}

			String version = attr.getValue("Implementation-Version");
			if (version != null) {
				result.put(Constants.APPLICATION_VERSION, version);
			}

		} catch (IOException e) {

		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return result;

	}

}
