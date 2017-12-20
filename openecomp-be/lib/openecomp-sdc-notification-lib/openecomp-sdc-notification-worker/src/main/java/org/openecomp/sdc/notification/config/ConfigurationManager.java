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

package org.openecomp.sdc.notification.config;

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.services.YamlUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ConfigurationManager {

    private static final String CONFIGURATION_YAML_FILE = "onboarding_configuration.yaml";
    private static final String NOTIFICATIONS_CONFIG = "notifications";

    private LinkedHashMap<String, Object> notificationsConfiguration;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);
    private static final ConfigurationManager SINGLETON = new ConfigurationManager();

    public static ConfigurationManager getInstance() {
        return SINGLETON;
    }

    private ConfigurationManager() {
        initConfiguration();
    }

    private void initConfiguration() {

        YamlUtil yamlUtil = new YamlUtil();
        readConfigurationFromStream(yamlUtil, (filename, stream) -> {

            if (stream == null) {
                LOGGER.warn("Configuration not found: " + filename + ". Using defaults");
                return;
            }

            Map<String, LinkedHashMap<String, Object>> configurationMap = yamlUtil.yamlToMap(stream);
            if (configurationMap == null) {
                LOGGER.warn("Configuration cannot be parsed: " + filename + ". Using defaults");
                return;
            }

            notificationsConfiguration = configurationMap.get(NOTIFICATIONS_CONFIG);
            if (notificationsConfiguration == null) {
                LOGGER.error(NOTIFICATIONS_CONFIG +
                        " is missing in configuration file '" + filename + "'. Using defaults");
            }
        });
    }

    private void readConfigurationFromStream(YamlUtil yamlUtil,
                                             BiConsumer<String, InputStream> reader) {

        String configurationYamlFile = System.getProperty(CONFIGURATION_YAML_FILE);

        try {

            if (configurationYamlFile == null) {

                try (InputStream inputStream =
                             yamlUtil.loadYamlFileIs("/" + CONFIGURATION_YAML_FILE)) {
                    reader.accept(CONFIGURATION_YAML_FILE, inputStream);
                }

            } else {

                try (InputStream inputStream = new FileInputStream(configurationYamlFile)) {
                    reader.accept(configurationYamlFile, inputStream);
                }
            }

        } catch (IOException e) {
            LOGGER.error("Failed to read configuration", e);
        }
    }

    public <T> T getConfigValue(String name, T defaultValue) {

        Object value = notificationsConfiguration.get(name);

        try {
            return value == null ? defaultValue : (T) value;
        } catch (ClassCastException e) {
            LOGGER.warn(String.format("Failed to read configuration property '%s' as requested type. Using default '%s'",
                    name, defaultValue), e);
            return defaultValue;
        }
    }
}
