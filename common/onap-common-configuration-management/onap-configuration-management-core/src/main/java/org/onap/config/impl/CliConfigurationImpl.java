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

import static org.onap.config.Constants.DEFAULT_NAMESPACE;
import static org.onap.config.Constants.DEFAULT_TENANT;
import static org.onap.config.Constants.LOAD_ORDER_KEY;
import static org.onap.config.Constants.MODE_KEY;
import static org.onap.config.Constants.NAMESPACE_KEY;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.onap.config.ConfigurationUtils;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.api.Hint;
import org.onap.config.type.ConfigurationQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CliConfigurationImpl extends ConfigurationImpl implements ConfigurationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CliConfigurationImpl.class);

    private static final List<String> KEYS_TO_FILTER = Arrays.asList(NAMESPACE_KEY, MODE_KEY, LOAD_ORDER_KEY);

    public CliConfigurationImpl() {
        super();
    }

    public String getConfigurationValue(Map<String, Object> input) {
        return getConfigurationValue((ConfigurationQuery) getInput(input));
    }

    private String getConfigurationValue(ConfigurationQuery queryData) {

        try {

            Hint[] hints = getHints(queryData);

            String[] value;
            if (queryData.isFallback()) {
                value = get(queryData.getTenant(), queryData.getNamespace(), queryData.getKey(), String[].class, hints);
            } else {
                value = getInternal(queryData.getTenant(), queryData.getNamespace(), queryData.getKey(), String[].class,
                                hints);
            }

            return ConfigurationUtils.getCommaSeparatedList(value);

        } catch (Exception exception) {
            LOGGER.warn("Error occurred while processing query: {}", queryData, exception);
        }

        return null;
    }

    private Hint[] getHints(ConfigurationQuery query) {
        List<Hint> hints = new ArrayList<>(Hint.values().length);
        hints.add(query.isLatest() ? Hint.LATEST_LOOKUP : Hint.DEFAULT);
        hints.add(query.isExternalLookup() ? Hint.EXTERNAL_LOOKUP : Hint.DEFAULT);
        hints.add(query.isNodeSpecific() ? Hint.NODE_SPECIFIC : Hint.DEFAULT);
        return hints.toArray(new Hint[0]);
    }

    private Object getInput(Map<String, Object> input) {

        Object toReturn = null;

        try {

            toReturn = Class.forName(input.get("ImplClass").toString()).newInstance();

            Method[] methods = toReturn.getClass().getMethods();
            for (Method method : methods) {
                if (input.containsKey(method.getName())) {
                    method.invoke(toReturn, input.get(method.getName()));
                }
            }

        } catch (Exception exception) {
            LOGGER.warn("Error occurred while processing input: {}", input, exception);
        }

        return toReturn;
    }

    public Map<String, String> listConfiguration(Map<String, Object> input) {
        return listConfiguration((ConfigurationQuery) getInput(input));
    }

    private Map<String, String> listConfiguration(ConfigurationQuery query) {

        Map<String, String> map = new HashMap<>();

        if (query != null) {
            try {

                Collection<String> keys = getKeys(query.getTenant(), query.getNamespace());
                for (String key : keys) {
                    map.put(key, getConfigurationValue(query.key(key)));
                }

            } catch (Exception exception) {
                LOGGER.warn("Error occurred while processing query: {}", query, exception);
                return null;
            }
        }

        return map;
    }

    private ArrayList<String> getInMemoryKeys(String tenant, String namespace) {

        ArrayList<String> keys = new ArrayList<>();

        try {

            Iterator<String> iter = ConfigurationRepository.lookup().getConfigurationFor(tenant, namespace).getKeys();
            while (iter.hasNext()) {

                String key = iter.next();
                if (!KEYS_TO_FILTER.contains(key)) {
                    keys.add(key);
                }
            }

        } catch (Exception exception) {
            LOGGER.warn("Error occurred while searching for in-memory keys for namespace: '{}' and tenant: '{}'",
                    namespace,
                    tenant,
                    exception
            );
        }

        return keys;
    }

    @Override
    public Collection<String> getTenants() {
        return ConfigurationRepository.lookup().getTenants();
    }

    @Override
    public Collection<String> getNamespaces() {
        return ConfigurationRepository.lookup().getNamespaces();
    }

    @Override
    public Collection<String> getKeys(String tenant, String namespace) {
        Set<String> keyCollection = new HashSet<>();
        keyCollection.addAll(getInMemoryKeys(tenant, DEFAULT_NAMESPACE));
        keyCollection.addAll(getInMemoryKeys(tenant, namespace));
        keyCollection.addAll(getInMemoryKeys(DEFAULT_TENANT, namespace));
        keyCollection.addAll(getInMemoryKeys(DEFAULT_TENANT, DEFAULT_NAMESPACE));
        return keyCollection;
    }
}
