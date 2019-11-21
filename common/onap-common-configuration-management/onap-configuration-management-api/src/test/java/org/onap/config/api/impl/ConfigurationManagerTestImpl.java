/*-
 * ============LICENSE_START=======================================================
 * ONAP SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 * ===================================================================
 */


package org.onap.config.api.impl;

import org.onap.config.api.ConfigurationManager;
import org.onap.config.api.Hint;

import java.util.Collection;
import java.util.Map;

/**
 * Mocking implementation of ConfigurationManager interface
 */
public class ConfigurationManagerTestImpl implements ConfigurationManager {
    @Override
    public String getConfigurationValue(Map<String, Object> queryData) {
        return null;
    }

    @Override
    public Map<String, String> listConfiguration(Map<String, Object> query) {
        return null;
    }

    @Override
    public Collection<String> getTenants() {
        return null;
    }

    @Override
    public Collection<String> getNamespaces() {
        return null;
    }

    @Override
    public Collection<String> getKeys(String tenant, String namespace) {
        return null;
    }

    @Override
    public <T> T get(String tenant, String namespace, String key, Class<T> clazz, Hint... hints) {
        return null;
    }

    @Override
    public <T> Map<String, T> populateMap(String tenantId, String namespace, String key, Class<T> clazz) {
        return null;
    }

    @Override
    public Map generateMap(String tenantId, String namespace, String key) {
        return null;
    }
}
