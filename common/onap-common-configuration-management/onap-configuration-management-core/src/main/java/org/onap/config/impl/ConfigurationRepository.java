/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.config.impl;

import java.io.File;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.onap.config.ConfigurationUtils;
import org.onap.config.Constants;

public final class ConfigurationRepository {

    private static final ConfigurationRepository repo = new ConfigurationRepository();

    private final Set<String> tenants = Collections.synchronizedSet(new HashSet<>());

    private final Set<String> namespaces = Collections.synchronizedSet(new HashSet<>());

    private final Map<String, ConfigurationHolder> store = Collections.synchronizedMap(

            new LinkedHashMap<String, ConfigurationHolder>(16, 0.75f, true) {

                @Override
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    try {
                        return size() > getConfigurationFor(Constants.DEFAULT_TENANT, Constants.DB_NAMESPACE)
                                                .getInt("config.size.max");
                    } catch (Exception exception) {
                        return false;
                    }
                }
            });


    private ConfigurationRepository() {
        tenants.add(Constants.DEFAULT_TENANT);
        namespaces.add(Constants.DEFAULT_NAMESPACE);
    }

    public static ConfigurationRepository lookup() {
        return repo;
    }

    public Set<String> getTenants() {
        return tenants;
    }

    public Set<String> getNamespaces() {
        return namespaces;
    }


    public boolean isValidTenant(String tenant) {
        return tenant != null && tenants.contains(tenant.toUpperCase());
    }

    public boolean isValidNamespace(String namespace) {
        return namespace != null && namespaces.contains(namespace.toUpperCase());
    }

    public Configuration getConfigurationFor(String tenant, String namespace) throws Exception {
        String module = tenant + Constants.KEY_ELEMENTS_DELIMITER + namespace;
        ConfigurationHolder config = store.get(module);
        return config.getConfiguration(tenant + Constants.KEY_ELEMENTS_DELIMITER + namespace);
    }

    public void populateConfiguration(String key, Configuration builder) {
        store.put(key, new ConfigurationHolder(builder));
        populateTenantsNamespace(key);
    }

    private void populateTenantsNamespace(String key) {
        String[] array = key.split(Constants.KEY_ELEMENTS_DELIMITER);
        if (!array[1].equalsIgnoreCase(Constants.DB_NAMESPACE)) {
            tenants.add(array[0]);
            namespaces.add(array[1]);
        }
    }

    public void populateOverrideConfiguration(String key, File file) {

        ConfigurationHolder holder = store.get(key);

        if (holder == null) {
            holder = new ConfigurationHolder(new CombinedConfiguration());
            store.put(key, holder);
        }

        holder.addOverrideConfiguration(file.getAbsolutePath(), ConfigurationUtils.getConfigurationBuilder(file));
        populateTenantsNamespace(key);
    }

    private class ConfigurationHolder {

        private final Map<String, FileBasedConfigurationBuilder<FileBasedConfiguration>> overrideConfiguration =
                new LinkedHashMap<>();
        BasicConfigurationBuilder<Configuration> builder;
        Timestamp lastConfigurationBuildTime;
        Configuration config;
        Configuration composite;

        public ConfigurationHolder(BasicConfigurationBuilder builder) {
            this.builder = builder;
        }

        public ConfigurationHolder(Configuration builder) {
            this.config = builder;
        }

        public void addOverrideConfiguration(String path, BasicConfigurationBuilder<FileBasedConfiguration> builder) {
            overrideConfiguration.put(path.toUpperCase(), (FileBasedConfigurationBuilder) builder);
            getEffectiveConfiguration(config, overrideConfiguration.values());
        }

        private Configuration getEffectiveConfiguration(Configuration configuration,
                Collection<FileBasedConfigurationBuilder<FileBasedConfiguration>> list) {
            try {
                CompositeConfiguration cc = new CompositeConfiguration();
                for (FileBasedConfigurationBuilder<FileBasedConfiguration> b : list) {
                    cc.addConfiguration(b.getConfiguration());
                }
                cc.addConfiguration(configuration);
                composite = cc;
                return composite;
            } catch (Exception exception) {
                return null;
            }
        }

        public Configuration getConfiguration(String namespace) throws Exception {

            if (config == null) {
                config = builder.getConfiguration();
                lastConfigurationBuildTime = new Timestamp(System.currentTimeMillis());
            } else if (lastConfigurationBuildTime != null
                               && System.currentTimeMillis() - lastConfigurationBuildTime.getTime()
                                          > getConfigurationFor(Constants.DEFAULT_TENANT, Constants.DB_NAMESPACE)
                                                    .getInt("config.refresh.interval")) {
                lastConfigurationBuildTime = new Timestamp(System.currentTimeMillis());
            }

            if (composite == null && overrideConfiguration.size() != 0) {
                composite = getEffectiveConfiguration(config, overrideConfiguration.values());
            }

            return overrideConfiguration.size() == 0 ? config : composite;
        }
    }
}
