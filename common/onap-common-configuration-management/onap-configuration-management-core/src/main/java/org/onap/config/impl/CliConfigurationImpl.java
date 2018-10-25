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

import static org.onap.config.Constants.DB_NAMESPACE;
import static org.onap.config.Constants.DEFAULT_NAMESPACE;
import static org.onap.config.Constants.DEFAULT_TENANT;
import static org.onap.config.Constants.KEY_ELEMENTS_DELIMETER;
import static org.onap.config.Constants.LOAD_ORDER_KEY;
import static org.onap.config.Constants.MBEAN_NAME;
import static org.onap.config.Constants.MODE_KEY;
import static org.onap.config.Constants.NAMESPACE_KEY;

import java.io.File;
import java.io.PrintWriter;
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
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.onap.config.ConfigurationUtils;
import org.onap.config.Constants;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.api.Hint;
import org.onap.config.type.ConfigurationQuery;
import org.onap.config.type.ConfigurationUpdate;

public final class CliConfigurationImpl extends ConfigurationImpl implements ConfigurationManager {

    public CliConfigurationImpl() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName(MBEAN_NAME);
        if (mbs.isRegistered(name)) {
            mbs.unregisterMBean(name);
        }
        mbs.registerMBean(new StandardMBean(this, ConfigurationManager.class), name);
        mbs.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME,
                (notification, handback) -> handleNotification(notification), null, null);
    }

    public void handleNotification(Notification notification) {
        if (notification instanceof MBeanServerNotification) {
            MBeanServerNotification mbs = (MBeanServerNotification) notification;
            if (MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(mbs.getType())) {
                try {
                    String mbean = ConfigurationRepository.lookup().getConfigurationFor(DEFAULT_TENANT, DB_NAMESPACE)
                                           .getString("shutdown.mbean");
                    if (mbs.getMBeanName().equals(mbean == null ? new ObjectName(MBEAN_NAME) : new ObjectName(mbean))) {
                        changeNotifier.shutdown();
                    }
                } catch (Exception exception) {
                    //do nothing.
                }
            } else if (MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(mbs.getType())) {
                mbs.getMBeanName();
            }
        }
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

    public void updateConfigurationValue(Map<String, Object> input) {
        updateConfigurationValue((ConfigurationUpdate) getInput(input));
    }

    private void updateConfigurationValue(ConfigurationUpdate updateData) {

        try {
            if (!ConfigurationRepository.lookup().isValidTenant(updateData.getTenant())) {
                throw new RuntimeException("Invalid tenantId.");
            }
            if (!ConfigurationRepository.lookup().isValidNamespace(updateData.getNamespace())) {
                throw new RuntimeException("Invalid Namespace.");
            }
        } catch (NullPointerException e1) {
            e1.printStackTrace();
        }

        try {
            boolean keyPresent = isKeyExists(updateData.getTenant(), updateData.getNamespace(), updateData.getKey());
            if (keyPresent) {
                boolean isUpdated = false;
                Object[] paramArray =
                        new Object[] {updateData.getTenant() + KEY_ELEMENTS_DELIMETER + updateData.getNamespace(),
                                System.currentTimeMillis(), updateData.getKey(), getConfigurationValue(updateData),
                                updateData.getValue()};
                Configuration config = ConfigurationRepository.lookup()
                                               .getConfigurationFor(updateData.getTenant(), updateData.getNamespace());
                if (config instanceof AgglomerateConfiguration || config instanceof CombinedConfiguration) {
                    CompositeConfiguration cc = new CompositeConfiguration();
                    cc.addConfiguration(config);
                    config = cc;
                }
                CompositeConfiguration configuration = (CompositeConfiguration) config;
                int overrideIndex = -1;
                for (int i = 0; i < configuration.getNumberOfConfigurations(); i++) {
                    if (!updateData.isNodeOverride() && (
                            configuration.getConfiguration(i) instanceof AgglomerateConfiguration
                                    || configuration.getConfiguration(i) instanceof CombinedConfiguration)) {
                        configuration.getConfiguration(i).setProperty(updateData.getKey(), updateData.getValue());
                        isUpdated = true;
                        break;
                    } else if (updateData.isNodeOverride() && configuration.getConfiguration(
                            i) instanceof FileBasedConfiguration) {
                        configuration.getConfiguration(i).setProperty(updateData.getKey(), updateData.getValue());
                        isUpdated = true;
                        overrideIndex = i;
                        break;
                    }
                }
                if (!isUpdated) {
                    if (updateData.isNodeOverride()) {
                        PropertiesConfiguration pc = new PropertiesConfiguration();
                        pc.setProperty(NAMESPACE_KEY,
                                updateData.getTenant() + Constants.TENANT_NAMESPACE_SEPARATOR
                                        + updateData.getNamespace());
                        pc.setProperty(MODE_KEY, "OVERRIDE");
                        pc.setProperty(updateData.getKey(), updateData.getValue());
                        String nodeConfigLocation = System.getProperty("node.config.location");
                        if (nodeConfigLocation != null && nodeConfigLocation.trim().length() > 0) {
                            File file = new File(nodeConfigLocation,
                                    updateData.getTenant() + File.separator + updateData.getNamespace() + File.separator
                                            + "config.properties");
                            file.getParentFile().mkdirs();
                            PrintWriter out = new PrintWriter(file);
                            pc.write(out);
                            out.close();
                            ConfigurationRepository.lookup().populateOverrideConfiguration(
                                    updateData.getTenant() + KEY_ELEMENTS_DELIMETER + updateData.getNamespace(), file);
                        }
                    } else {
                        configuration.getConfiguration(0).setProperty(updateData.getKey(), updateData.getValue());
                    }
                }
                if (updateData.isNodeOverride()) {
                    ConfigurationRepository.lookup().refreshOverrideConfigurationFor(
                            updateData.getTenant() + KEY_ELEMENTS_DELIMETER + updateData.getNamespace(), overrideIndex);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private boolean isKeyExists(String tenant, String namespace, String key) {
        boolean keyExist = false;
        try {
            keyExist = ConfigurationRepository.lookup().getConfigurationFor(tenant, namespace).containsKey(key);
            if (!keyExist && !DEFAULT_TENANT.equals(tenant)) {
                keyExist = ConfigurationRepository.lookup().getConfigurationFor(DEFAULT_TENANT, namespace)
                                   .containsKey(key);
            }
            if (!keyExist && !DEFAULT_NAMESPACE.equals(namespace)) {
                keyExist = ConfigurationRepository.lookup().getConfigurationFor(tenant, DEFAULT_NAMESPACE)
                                   .containsKey(key);
            }
            if (!keyExist && !DEFAULT_TENANT.equals(tenant) && !DEFAULT_NAMESPACE.equals(namespace)) {
                keyExist = ConfigurationRepository.lookup().getConfigurationFor(DEFAULT_TENANT, DEFAULT_NAMESPACE)
                                   .containsKey(key);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return keyExist;
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
    public boolean updateConfigurationValues(String tenant, String namespace, Map configKeyValueStore) {
        boolean valueToReturn = true;
        for (String s : (Iterable<String>) configKeyValueStore.keySet()) {
            try {
                String key = s;
                ConfigurationUpdate updateData = new ConfigurationUpdate();
                updateData.tenant(tenant).namespace(namespace).key(key);
                updateData.value(configKeyValueStore.get(key).toString());
                updateConfigurationValue(updateData);
            } catch (Exception exception) {
                exception.printStackTrace();
                valueToReturn = false;
            }
        }
        return valueToReturn;
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
