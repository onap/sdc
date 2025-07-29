/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.onap.config.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Loads a Java SPI binding for the configuration service.
 */
@Component
class ConfigurationLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);

    private ConfigurationLoader() {
    }

    static Configuration load() {
        logger.debug("Starting configuration service loading using ServiceLoader...");

        ServiceLoader<ConfigurationManager> loader = ServiceLoader.load(ConfigurationManager.class);
        Iterator<ConfigurationManager> configManagers = loader.iterator();

        while (configManagers.hasNext()) {
            try {
                ConfigurationManager configManager = configManagers.next();
                logger.debug("Found ConfigurationManager implementation: {}", configManager.getClass().getName());
                return configManager;
            } catch (ServiceConfigurationError e) {
                logger.warn("Failed to load ConfigurationManager implementation", e);
                // continue to next provider
            }
        }

        logger.error("No ConfigurationManager binding found using ServiceLoader");
        throw new IllegalStateException("No binding found for configuration service");
    }
}
