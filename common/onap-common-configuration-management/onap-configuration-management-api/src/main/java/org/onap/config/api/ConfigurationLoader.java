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

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Loads a Java SPI binding for the configuration service.
 *
 * @author evitaliy
 * @since 29 Oct 2018
 */
class ConfigurationLoader {

    private ConfigurationLoader() {
    }

    static Configuration load() {

        ServiceLoader<ConfigurationManager> loader = ServiceLoader.load(ConfigurationManager.class);
        Iterator<ConfigurationManager> configManagers = loader.iterator();
        while (configManagers.hasNext()) {
            try {
                return configManagers.next();
            } catch (ServiceConfigurationError e) {
                // this provider loading has failed, let's try next one
            }
        }

        throw new IllegalStateException("No binding found for configuration service");
    }
}
