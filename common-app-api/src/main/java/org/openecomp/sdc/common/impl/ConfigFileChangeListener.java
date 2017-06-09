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

package org.openecomp.sdc.common.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jci.listeners.FileChangeListener;
import org.openecomp.sdc.common.api.BasicConfiguration;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.util.YamlToObjectConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigFileChangeListener extends FileChangeListener {

	private static Logger log = LoggerFactory.getLogger(ConfigFileChangeListener.class.getName());

	private Map<String, List<ConfigurationListener>> fileChangeToCallBack = new HashMap<String, List<ConfigurationListener>>();

	private Object lock = new Object();

	private YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();

	@Override
	public void onFileChange(File pFile) {

		super.onFileChange(pFile);

		if (pFile != null) {

			if (fileChangeToCallBack != null) {

				String id = findIdFromFileName(pFile.getName());

				if (id != null) {

					List<ConfigurationListener> listeners = fileChangeToCallBack.get(id);
					if (listeners != null) {
						for (ConfigurationListener configurationListener : listeners) {

							Class<? extends BasicConfiguration> configClass = configurationListener.getType();

							BasicConfiguration basicConfiguration = yamlToObjectConverter.convert(pFile.getAbsolutePath(), configClass);

							if (basicConfiguration == null) {
								log.warn("Cannot update the listeners for file Change since the file content is invalid");
								continue;
							}
							log.debug("Loaded configuration after converting is {}", basicConfiguration);
							// System.out.println("New configuration is " +
							// basicConfiguration);

							configurationListener.getCallBack().reconfigure(basicConfiguration);

						}
					}
				} else {

					log.warn("Cannot calculate id from file {}", pFile.getName());
				}
			}

		}

		log.debug("File {} was changed.", pFile);
	}

	private String findIdFromFileName(String name) {

		String result = null;
		if (name != null) {
			int startIndex = 0;
			int endIndex = name.length();
			if (name.contains(File.separator)) {
				startIndex = name.lastIndexOf(File.separator);
			}
			// String subNameString = name.substring(startIndex, endIndex);
			// if (subNameString.contains(".")) {
			// endIndex = subNameString.indexOf(".");
			// }

			result = name.substring(startIndex, endIndex);

		}

		return result;
	}

	public void register(String id, ConfigurationListener configurationListener) {

		if (configurationListener != null) {

			synchronized (lock) {

				List<ConfigurationListener> callbacks = fileChangeToCallBack.get(id);
				if (callbacks == null) {
					callbacks = new ArrayList<ConfigurationListener>();
					fileChangeToCallBack.put(id, callbacks);
				}
				callbacks.add(configurationListener);

			}

		}

	}

	// public void notify(String id, BasicConfiguration object) {
	//
	// if (fileChangeToCallBack != null) {
	// List<ConfigurationListener> listeners = fileChangeToCallBack
	// .get(id);
	// if (listeners != null) {
	// for (ConfigurationListener configurationListener : listeners) {
	//
	// configurationListener.getCallBack().reconfigure(object);
	//
	// }
	// }
	// }
	//
	// }

}
