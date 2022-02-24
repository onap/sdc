/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017, 2021 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

/**
 * This is a common class that can access the config file given in input to the JVM with the parameter -Dconfiguration.yaml=file.yaml.
 */
public class CommonConfigurationManager {

    public static final String JVM_PARAM_CONFIGURATION_FILE = "configuration.yaml";
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonConfigurationManager.class);
    private static CommonConfigurationManager singletonInstance;
    private Map<String, LinkedHashMap<String, Object>> configuration;
    private String configFilename;
    private String yamlSection;

    protected CommonConfigurationManager() {
        initConfiguration();
    }

    protected CommonConfigurationManager(String yamlSection) {
        this();
        this.yamlSection = yamlSection;
    }

    public static synchronized CommonConfigurationManager getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new CommonConfigurationManager();
        }
        return singletonInstance;
    }

    public void reload() {
        initConfiguration();
    }

    private void initConfiguration() {
        YamlUtil yamlUtil = new YamlUtil();
        readConfigurationFromStream(yamlUtil, (filename, stream) -> {
            this.configFilename = filename;
            if (stream == null) {
                LOGGER.warn("Configuration not found: " + filename + ". Using defaults");
                return;
            }
            Map<String, LinkedHashMap<String, Object>> configurationMap = yamlUtil.yamlToMap(stream);
            if (configurationMap == null) {
                LOGGER.warn("Configuration cannot be parsed: " + filename + ". Using defaults");
                return;
            } else {
                this.configuration = configurationMap;
            }
        });
    }

    private void readConfigurationFromStream(YamlUtil yamlUtil, BiConsumer<String, InputStream> reader) {
        String configurationYamlFile = System.getProperty(JVM_PARAM_CONFIGURATION_FILE);
        try {
            if (configurationYamlFile == null) {
                try (InputStream inputStream = yamlUtil.loadYamlFileIs("/" + JVM_PARAM_CONFIGURATION_FILE)) {
                    reader.accept(JVM_PARAM_CONFIGURATION_FILE, inputStream);
                }
            } else {
                try (InputStream inputStream = new FileInputStream(configurationYamlFile)) {
                    reader.accept(configurationYamlFile, inputStream);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read configuration " + configurationYamlFile, e);
            throw new RuntimeException("Failed to read configuration " + configurationYamlFile, e);
        }
    }

    /**
     * This method can be used to access any yaml section configuration.
     *
     * @param yamlSection  The yaml section that must be accessed
     * @param name         The configuration name inside that yaml section
     * @param defaultValue A default value
     * @param <T>          The type of value to be returned
     * @return The value found or the default value if not found
     */
    public <T> T getConfigValue(String yamlSection, String name, T defaultValue) {
        Map<String, Object> section = this.configuration.get(yamlSection);
        if (section == null) {
            LOGGER.error("Section " + yamlSection + " is missing in configuration file '" + configFilename +
                "'. Using defaults");
            return defaultValue;
        }
        Object value = section.get(name);
        try {
            return value == null ? defaultValue : (T) value;
        } catch (ClassCastException e) {
            LOGGER.warn(
                String.format("Failed to read configuration property '%s' as requested type. Using default '%s'",
                    name, defaultValue), e);
            return defaultValue;
        }
    }

    /**
     * This method can be used to access a specific configuration parameter in the configuration in the yamlSection predefined in the constructor.
     *
     * @param name         The name of the config
     * @param defaultValue A default value
     * @param <T>          The type of value to be returned
     * @return The value found or the default value if not found
     */
    public <T> T getConfigValue(String name, T defaultValue) {
        return this.getConfigValue(yamlSection, name, defaultValue);
    }
}
