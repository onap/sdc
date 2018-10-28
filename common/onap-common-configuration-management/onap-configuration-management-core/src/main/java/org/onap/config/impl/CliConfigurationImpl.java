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
import static org.onap.config.Constants.MBEAN_NAME;
import static org.onap.config.Constants.MODE_KEY;
import static org.onap.config.Constants.NAMESPACE_KEY;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import org.onap.config.ConfigurationUtils;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.api.Hint;
import org.onap.config.type.ConfigurationQuery;

public final class CliConfigurationImpl extends ConfigurationImpl implements ConfigurationManager {

    public CliConfigurationImpl() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName(MBEAN_NAME);
        if (mbs.isRegistered(name)) {
            mbs.unregisterMBean(name);
        }
        mbs.registerMBean(new StandardMBean(this, ConfigurationManager.class), name);
    }

    public String getConfigurationValue(Map<String, Object> input) {
        return getConfigurationValue((ConfigurationQuery) getInput(input));
    }

    private String getConfigurationValue(ConfigurationQuery queryData) {
        try {
            if (queryData.isFallback()) {
                return ConfigurationUtils.getCommaSeparatedList(
                        get(queryData.getTenant(), queryData.getNamespace(), queryData.getKey(), String[].class,
                                queryData.isLatest() ? Hint.LATEST_LOOKUP : Hint.DEFAULT,
                                queryData.isExternalLookup() ? Hint.EXTERNAL_LOOKUP : Hint.DEFAULT,
                                queryData.isNodeSpecific() ? Hint.NODE_SPECIFIC : Hint.DEFAULT));
            } else {
                String[] list =
                        getInternal(queryData.getTenant(), queryData.getNamespace(), queryData.getKey(), String[].class,
                                queryData.isLatest() ? Hint.LATEST_LOOKUP : Hint.DEFAULT,
                                queryData.isExternalLookup() ? Hint.EXTERNAL_LOOKUP : Hint.DEFAULT,
                                queryData.isNodeSpecific() ? Hint.NODE_SPECIFIC : Hint.DEFAULT);
                return ConfigurationUtils
                               .getCommaSeparatedList(list == null ? Collections.emptyList() : Arrays.asList(list));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
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
            exception.printStackTrace();
        }

        return toReturn;
    }

    public Map<String, String> listConfiguration(Map<String, Object> input) {
        return listConfiguration((ConfigurationQuery) getInput(input));
    }

    private Map<String, String> listConfiguration(ConfigurationQuery query) {
        Map<String, String> map = new HashMap<>();
        try {
            Collection<String> keys = getKeys(query.getTenant(), query.getNamespace());
            for (String key : keys) {
                map.put(key, getConfigurationValue(query.key(key)));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
        return map;
    }

    private ArrayList<String> getInMemoryKeys(String tenant, String namespace) {

        ArrayList<String> keys = new ArrayList<>();

        try {
            Iterator<String> iter = ConfigurationRepository.lookup().getConfigurationFor(tenant, namespace).getKeys();
            while (iter.hasNext()) {
                String key = iter.next();
                if (!(key.equals(NAMESPACE_KEY) || key.equals(MODE_KEY) || key.equals(LOAD_ORDER_KEY))) {
                    keys.add(key);
                }
            }
        } catch (Exception exception) {
            //do nothing
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
