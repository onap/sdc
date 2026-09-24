/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.openecomp.sdc.common.tenant;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.openecomp.sdc.be.config.Configuration.MultitenancyConfig;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Reads the {@code multitenancy} section of the YAML file named by {@code -Dconfiguration.yaml}, for services that do not load
 * that file into a typed {@link org.openecomp.sdc.be.config.Configuration}.
 */
public final class MultitenancyConfigLoader {

    static final String CONFIG_FILE_PROPERTY = "configuration.yaml";
    static final String CONFIG_SECTION = "multitenancy";
    private static final Logger log = Logger.getLogger(MultitenancyConfigLoader.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final Map<String, MultitenancyConfig> BY_PATH = new ConcurrentHashMap<>();

    private MultitenancyConfigLoader() {
    }

    public static MultitenancyConfig fromConfigurationFileProperty() {
        String file = System.getProperty(CONFIG_FILE_PROPERTY);
        return file == null ? disabled() : BY_PATH.computeIfAbsent(file, MultitenancyConfigLoader::fromFile);
    }

    public static MultitenancyConfig disabled() {
        return new MultitenancyConfig();
    }

    static MultitenancyConfig fromFile(String file) {
        Object root;
        try (InputStream in = Files.newInputStream(Paths.get(file))) {
            root = new Yaml(new SafeConstructor(new LoaderOptions())).load(in);
        } catch (NoSuchFileException e) {
            log.warn("Configuration file {} not found; multitenancy is disabled", file);
            return disabled();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + file, e);
        }
        Object section = root instanceof Map ? ((Map<?, ?>) root).get(CONFIG_SECTION) : null;
        if (section == null) {
            return disabled();
        }
        try {
            return MAPPER.convertValue(section, MultitenancyConfig.class);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid '" + CONFIG_SECTION + "' section in " + file, e);
        }
    }
}
