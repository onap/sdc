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

package org.openecomp.sdc.common.test;

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import java.io.File;

public class BaseConfDependent {
	protected static ConfigurationManager configurationManager;
	protected static String componentName;
	protected static String confPath;
	
	
	protected static void setUp(){
		ExternalConfiguration.setAppName(componentName);
		ExternalConfiguration.setConfigDir(confPath);
		ExternalConfiguration.listenForChanges();
		ExternalConfiguration.setAppVersion("1806.666");

		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), ExternalConfiguration.getConfigDir() + File.separator + ExternalConfiguration.getAppName());

		configurationManager = new ConfigurationManager(configurationSource);

		configurationManager.getConfiguration().setJanusGraphInMemoryGraph(true);
	
	}
	
}
