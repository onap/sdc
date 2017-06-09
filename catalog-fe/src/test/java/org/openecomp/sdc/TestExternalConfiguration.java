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

import java.io.IOException;

import org.openecomp.sdc.common.api.BasicConfiguration;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.FileChangeCallback;
import org.openecomp.sdc.common.impl.ConfigFileChangeListener;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.fe.config.Configuration;

public class TestExternalConfiguration<T extends Object> {

	public static void main(String[] args) throws IOException {

		ExternalConfiguration.setAppName("catalog-server");
		ExternalConfiguration
				.setConfigDir("C:\\Users\\esofer\\workspaceLuna\\catalog-server\\src\\test\\resources\\config");
		ExternalConfiguration.listenForChanges();

		ConfigurationListener configurationListener = new ConfigurationListener(Configuration.class,
				new FileChangeCallback() {

					@Override
					public void reconfigure(BasicConfiguration obj) {
						// TODO Auto-generated method stub

					}
				});

		ConfigurationSource configurationSource1 = new FSConfigurationSource(new ConfigFileChangeListener(),
				ExternalConfiguration.getConfigDir());
		configurationSource1.getAndWatchConfiguration(Configuration.class, configurationListener);

		try {
			Thread.currentThread().sleep(100 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
