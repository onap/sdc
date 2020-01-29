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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.jci.listeners.FileChangeListener;
import org.openecomp.sdc.common.api.BasicConfiguration;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.YamlToObjectConverter;
import org.openecomp.sdc.exception.YamlConversionException;

public class ConfigFileChangeListener extends FileChangeListener {

	private static final Logger LOGGER = Logger.getLogger(ConfigFileChangeListener.class.getName());

	private Map<String, List<ConfigurationListener>> fileChangeToCallBack = new HashMap<>();

	private Object lock = new Object();

	private YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();

	@Override
	public void onFileChange(File pFile) {
		super.onFileChange(pFile);

		if (pFile == null) {
			LOGGER.debug("Invalid file '{}'.", pFile);
			return;
		}

		if (fileChangeToCallBack == null) {
			LOGGER.debug("File '{}' callback is null.", pFile);
			return;
		}

		final String id = findIdFromFileName(pFile.getName());
		if (id == null) {
			LOGGER.warn(EcompLoggerErrorCode.UNKNOWN_ERROR,"","",
				"Cannot calculate id from file {}", pFile.getName());
			return;
		}

		final List<ConfigurationListener> listeners = fileChangeToCallBack.get(id);
		if (CollectionUtils.isEmpty(listeners)) {
			LOGGER.debug("No file listeners for file '{}', id '{}'.", pFile, id);
			return;
		}
		for (final ConfigurationListener configurationListener : listeners) {
			final Class<? extends BasicConfiguration> configClass = configurationListener.getType();
			final BasicConfiguration basicConfiguration;
			try {
				basicConfiguration = yamlToObjectConverter.convert(pFile.getAbsolutePath(), configClass);
			} catch (final YamlConversionException e) {
				LOGGER.warn(EcompLoggerErrorCode.SCHEMA_ERROR,
					"Configuration", "Configuration",
					"Cannot update the listeners for file Change since the file content is invalid: {}",
					e.getLocalizedMessage());
				continue;
			}
			LOGGER.debug("Loaded configuration after converting is {}", basicConfiguration);
			configurationListener.getCallBack().reconfigure(basicConfiguration);
		}
		LOGGER.debug("File {} was changed.", pFile);
	}

	private String findIdFromFileName(String name) {

		String result = null;
		if (name != null) {
			int startIndex = 0;
			int endIndex = name.length();
			if (name.contains(File.separator)) {
				startIndex = name.lastIndexOf(File.separator);
			}

			result = name.substring(startIndex, endIndex);

		}

		return result;
	}

	public void register(String id, ConfigurationListener configurationListener) {

		if (configurationListener != null) {

			synchronized (lock) {

				List<ConfigurationListener> callbacks = fileChangeToCallBack.get(id);
				if (callbacks == null) {
					callbacks = new ArrayList<>();
					fileChangeToCallBack.put(id, callbacks);
				}
				callbacks.add(configurationListener);

			}

		}

	}

}
