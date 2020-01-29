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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.common.impl;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.exception.LoadConfigurationException;
import org.openecomp.sdc.common.util.YamlToObjectConverter;
import org.openecomp.sdc.exception.YamlConversionException;

/**
 * Read configuration from file system
 * 
 * @author esofer
 *
 */
public class FSConfigurationSource implements ConfigurationSource {

    private final YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();

    private final ConfigFileChangeListener changeListener;
    private final String appConfigDir;

	public FSConfigurationSource(ConfigFileChangeListener changeListener, String appConfigDir) {
		super();
		this.changeListener = changeListener;
		this.appConfigDir = appConfigDir;
	}

	/*
	 * get and watch configuration changes. The file name we looking for is the lower case of the class name separated by "-".
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.openecomp.sdc.common.api.ConfigurationSource#getAndWatchConfiguration (java.lang.Class, org.openecomp.sdc.common.api.ConfigurationListener)
	 */
	public <T> T getAndWatchConfiguration(final Class<T> className, final ConfigurationListener configurationListener) {

		final String configFileName = calculateFileName(className);

		T object;
		try {
			object = yamlToObjectConverter.convert(this.appConfigDir, className, configFileName);
		} catch (final YamlConversionException e) {
			final String errorMsg =
				String.format("Could not load '%s' in '%s' for class '%s'", configFileName, appConfigDir, className);
			throw new LoadConfigurationException(errorMsg, e);
		}

		if (configurationListener != null && changeListener != null) {
			changeListener.register(configFileName, configurationListener);
		}

		return object;
	}

	public <T> void addWatchConfiguration(Class<T> className, ConfigurationListener configurationListener) {

		String configFileName = calculateFileName(className);

		if (configurationListener != null) {
			changeListener.register(configFileName, configurationListener);
		}

	}

	/**
	 * convert camel case string to list of words separated by "-" where each word is in lower case format. For example, MyClass will be calculated to be my-class.yaml .
	 * 
	 * @param className
	 * @return file name based on the class name
	 */
    static <T> String calculateFileName(Class<T> className) {
        String[] words = className.getSimpleName().split("(?=\\p{Upper})");

        return Arrays.stream(words)
            .map(String::toLowerCase)
            .collect(Collectors.collectingAndThen(
                Collectors.joining("-"),
                str -> str + Constants.YAML_SUFFIX)
            );
    }

}
