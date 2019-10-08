/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.onap.sdc.tosca.parser.utils.YamlToObjectConverter;
import org.openecomp.sdc.be.config.exception.LoadConfigurationException;

/**
 * Singleton that loads and stores the Non Mano configuration
 */
public class NonManoConfigurationManager {

    private static NonManoConfigurationManager nonManoConfigurationManager = null;
    private NonManoConfiguration nonManoConfiguration;

    private NonManoConfigurationManager() {
        loadConfiguration();
    }

    public static NonManoConfigurationManager getInstance() {
        if (nonManoConfigurationManager == null) {
            nonManoConfigurationManager = new NonManoConfigurationManager();
        }

        return nonManoConfigurationManager;
    }

    /**
     * Loads the configuration yaml from the resources.
     */
    private void loadConfiguration() {
        final InputStream configYamlAsStream = getClass().getClassLoader()
            .getResourceAsStream("config/nonManoConfig.yaml");
        if (configYamlAsStream == null) {
            throw new LoadConfigurationException(
                "Expected non-mano configuration file 'config/nonManoConfig.yaml' not found in resources");
        }
        final String data;
        try {
            data = IOUtils.toString(configYamlAsStream, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new LoadConfigurationException(
                "Could not parse non-mano configuration file 'config/nonManoConfig.yaml' to string", e);
        }
        nonManoConfiguration = new YamlToObjectConverter().convertFromString(data, NonManoConfiguration.class);
    }

    public NonManoConfiguration getNonManoConfiguration() {
        return nonManoConfiguration;
    }
}
