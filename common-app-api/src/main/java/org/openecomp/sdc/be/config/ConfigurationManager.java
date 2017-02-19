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

package org.openecomp.sdc.be.config;

import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.common.api.BasicConfiguration;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.FileChangeCallback;
import org.openecomp.sdc.common.config.EcompErrorConfiguration;
import org.openecomp.sdc.common.config.IEcompConfigurationManager;

public class ConfigurationManager implements FileChangeCallback, IEcompConfigurationManager {

	ConfigurationSource configurationSource = null;
	private static ConfigurationManager instance;

	public ConfigurationManager(ConfigurationSource configurationSource) {
		super();
		this.configurationSource = configurationSource;
		loadConfigurationFiles();
		instance = this;
	}

	Map<String, Object> configurations = new HashMap<String, Object>();

	private void loadConfigurationFiles() {

		loadConfigurationClass(Configuration.class);
		loadConfigurationClass(ErrorConfiguration.class);
		loadConfigurationClass(Neo4jErrorsConfiguration.class);
		loadConfigurationClass(EcompErrorConfiguration.class);
		loadConfigurationClass(DistributionEngineConfiguration.class);
	}

	private <T extends BasicConfiguration> void loadConfigurationClass(Class<T> clazz) {
		ConfigurationListener configurationListener = new ConfigurationListener(clazz, this);

		T object = configurationSource.getAndWatchConfiguration(clazz, configurationListener);

		configurations.put(getKey(clazz), object);
	}

	private <T> String getKey(Class<T> class1) {

		return class1.getSimpleName();

	}

	public Configuration getConfiguration() {

		return (Configuration) configurations.get(getKey(Configuration.class));

	}

	public void setConfiguration(Configuration configuration) {

		configurations.put(getKey(Configuration.class), configuration);

	}

	public void setErrorConfiguration(ErrorConfiguration configuration) {

		configurations.put(getKey(ErrorConfiguration.class), configuration);

	}

	public ErrorConfiguration getErrorConfiguration() {

		return (ErrorConfiguration) configurations.get(getKey(ErrorConfiguration.class));

	}

	public Neo4jErrorsConfiguration getNeo4jErrorsConfiguration() {
		return (Neo4jErrorsConfiguration) configurations.get(getKey(Neo4jErrorsConfiguration.class));
	}

	@Override
	public EcompErrorConfiguration getEcompErrorConfiguration() {

		return (EcompErrorConfiguration) configurations.get(getKey(EcompErrorConfiguration.class));

	}

	public Configuration getConfigurationAndWatch(ConfigurationListener configurationListener) {

		if (configurationListener != null) {

			configurationSource.addWatchConfiguration(Configuration.class, configurationListener);

		}
		return (Configuration) configurations.get(getKey(Configuration.class));

	}

	public static ConfigurationManager getConfigurationManager() {
		return instance;
	}

	public void reconfigure(BasicConfiguration obj) {

		// if (obj != null) {

		// if (obj instanceof Configuration) {
		// configurations.put(getKey(Configuration.class), obj);
		// }
		// if (obj instanceof ErrorConfiguration) {
		// configurations.put(getKey(ErrorConfiguration.class), obj);
		// }
		//
		// if (obj instanceof EcompErrorConfiguration) {
		// configurations.put(getKey(EcompErrorConfiguration.class), obj);
		// }
		// }

	}

	/**
	 * FOR TEST ONLY
	 * 
	 * @param ecompErrorConfiguration
	 */
	public void setEcompErrorConfiguration(EcompErrorConfiguration ecompErrorConfiguration) {

		configurations.put(getKey(EcompErrorConfiguration.class), ecompErrorConfiguration);

	}

	public DistributionEngineConfiguration getDistributionEngineConfiguration() {

		return (DistributionEngineConfiguration) configurations.get(getKey(DistributionEngineConfiguration.class));

	}
}
