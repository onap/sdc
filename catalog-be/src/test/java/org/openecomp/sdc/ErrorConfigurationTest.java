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

package org.openecomp.sdc;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.config.ErrorConfiguration;
import org.openecomp.sdc.be.config.ErrorInfo;
import org.openecomp.sdc.common.api.BasicConfiguration;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.FileChangeCallback;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorConfigurationTest {
	ConfigurationSource configurationSource = null;
	private static Logger log = LoggerFactory.getLogger(ErrorConfigurationTest.class.getName());

	@Before
	public void setup() {

		ExternalConfiguration.setAppName("catalog-be");
		ExternalConfiguration.setConfigDir("src/test/resources/config");
		ExternalConfiguration.listenForChanges();

		configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), ExternalConfiguration.getConfigDir() + File.separator + ExternalConfiguration.getAppName());

	}

	@Test
	public void testReadConfigurationFile() {

		ConfigurationListener configurationListener = new ConfigurationListener(ErrorConfiguration.class, new FileChangeCallback() {

			public void reconfigure(BasicConfiguration obj) {
				// TODO Auto-generated method stub
				log.debug("In reconfigure of {}", obj);
			}

		});

		ErrorConfiguration testConfiguration = configurationSource.getAndWatchConfiguration(ErrorConfiguration.class, configurationListener);

		assertTrue(testConfiguration != null);
		ErrorInfo errorInfo = testConfiguration.getErrorInfo("USER_NOT_FOUND");
		assertTrue(errorInfo != null);
		log.debug("{}", testConfiguration);
		log.debug("{}", errorInfo);

	}

}
