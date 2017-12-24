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

package org.openecomp.sdc.fe.config;

import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.common.api.BasicConfiguration;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.FileChangeCallback;
import org.openecomp.sdc.common.config.EcompErrorConfiguration;
import org.openecomp.sdc.common.config.IEcompConfigurationManager;
import org.openecomp.sdc.common.rest.api.RestConfigurationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationManager implements FileChangeCallback, IEcompConfigurationManager {

	ConfigurationSource configurationSource = null;
	private static ConfigurationManager instance;
	private static Logger log = LoggerFactory.getLogger(ConfigurationManager.class.getName());

	public ConfigurationManager(ConfigurationSource configurationSource) {
		super();
		this.configurationSource = configurationSource;
		loadConfigurationFiles();
		instance = this;
	}

	Map<String, Object> configurations = new HashMap<String, Object>();

	private void loadConfigurationFiles() {

		loadConfigurationClass(Configuration.class);
		loadConfigurationClass(RestConfigurationInfo.class);
		loadConfigurationClass(EcompErrorConfiguration.class);
		loadConfigurationClass(DesignersConfiguration.class);

	}

	private <T extends BasicConfiguration> void loadConfigurationClass(Class<T> clazz) {
		ConfigurationListener configurationListener = new ConfigurationListener(clazz, this);
		
		log.info("created listener for class {}: {}", clazz.getName(), configurationListener);

		T object = configurationSource.getAndWatchConfiguration(clazz, configurationListener);

		configurations.put(getKey(clazz), object);
	}

	private <T> String getKey(Class<T> class1) {

		return class1.getSimpleName();

	}

	public Configuration getConfiguration() {

		return (Configuration) configurations.get(getKey(Configuration.class));

	}

	public RestConfigurationInfo getRestClientConfiguration() {

		return (RestConfigurationInfo) configurations.get(getKey(RestConfigurationInfo.class));

	}

	@Override
	public EcompErrorConfiguration getEcompErrorConfiguration() {

		return (EcompErrorConfiguration) configurations.get(getKey(EcompErrorConfiguration.class));

	}
	
	public DesignersConfiguration getDesignersConfiguration() {
		
		log.info("requested designers configuration and got this:{}", (DesignersConfiguration) configurations.get(getKey(DesignersConfiguration.class)));
		
		return (DesignersConfiguration) configurations.get(getKey(DesignersConfiguration.class));
	}

	public Configuration getConfigurationAndWatch(ConfigurationListener configurationListener) {

		if (configurationListener != null) {

			configurationSource.addWatchConfiguration(Configuration.class, configurationListener);

		}
		return (Configuration) configurations.get(getKey(Configuration.class));

	}

	public void reconfigure(BasicConfiguration obj) {
		//
		// if (obj != null) {
		//
		// if (obj instanceof Configuration) {
		// configurations.put(getKey(Configuration.class), obj);
		// }
		//
		// if (obj instanceof EcompErrorConfiguration) {
		// configurations.put(getKey(EcompErrorConfiguration.class), obj);
		// }
		// }
	}

	public static ConfigurationManager getConfigurationManager() {
		return instance;
	}
}
