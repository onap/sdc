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
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.onap.config.ConfigurationUtils;
import org.onap.config.Constants;
import org.onap.config.api.ConfigurationChangeListener;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.api.Hint;


public final class ConfigurationChangeNotifier {

    static {
        if (!Thread.currentThread().getStackTrace()[2].getClassName().equals(ConfigurationImpl.class.getName())) {
            throw new RuntimeException("Illegal access.");
        }
    }

    private final HashMap<String, List<NotificationData>> store = new HashMap<>();
    private final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(5, ConfigurationUtils.getThreadFactory());
    private final ExecutorService notificationExecutor =
            Executors.newCachedThreadPool(ConfigurationUtils.getThreadFactory());
    private final Map<String, WatchService> watchServiceCollection = Collections.synchronizedMap(new HashMap<>());

    public ConfigurationChangeNotifier(Map<String, AggregateConfiguration> inMemoryConfig) {
        executor.scheduleWithFixedDelay(() -> this.pollFilesystemAndUpdateConfigurationIfRequired(inMemoryConfig,
                System.getProperty("config.location"), false), 1, 1, TimeUnit.MILLISECONDS);
        executor.scheduleWithFixedDelay(() -> this.pollFilesystemAndUpdateConfigurationIfRequired(inMemoryConfig,
                System.getProperty("tenant.config.location"), true), 1, 1, TimeUnit.MILLISECONDS);
        executor.scheduleWithFixedDelay(() -> this.pollFilesystemAndUpdateNodeSpecificConfigurationIfRequired(
                System.getProperty("node.config.location")), 1, 1, TimeUnit.MILLISECONDS);
    }

    public void pollFilesystemAndUpdateConfigurationIfRequired(Map<String, AggregateConfiguration> inMemoryConfig,
            String location, boolean isTenantLocation) {
        try {
            Set<Path> paths = watchForChange(location);
            if (paths != null) {
                for (Path path : paths) {
                    File file = path.toAbsolutePath().toFile();
                    String repositoryKey = null;
                    if (ConfigurationUtils.isConfig(file) && file.isFile()) {
                        if (isTenantLocation) {
                            Collection<File> tenantsRoot =
                                    ConfigurationUtils.getAllFiles(new File(location), false, true);
                            for (File tenantRoot : tenantsRoot) {
                                if (file.getAbsolutePath().startsWith(tenantRoot.getAbsolutePath())) {
                                    repositoryKey = ConfigurationUtils.getConfigurationRepositoryKey(
                                            (tenantRoot.getName() + Constants.TENANT_NAMESPACE_SEPARATOR
                                                     + ConfigurationUtils.getNamespace(file))
                                                    .split(Constants.TENANT_NAMESPACE_SEPARATOR));
                                }
                            }
                        } else {
                            repositoryKey = ConfigurationUtils.getConfigurationRepositoryKey(file);
                        }
                        AggregateConfiguration config = inMemoryConfig.get(repositoryKey);
                        if (config != null) {
                            LinkedHashMap origConfig = ConfigurationUtils.toMap(config.getFinalConfiguration());
                            config.addConfig(file);
                            LinkedHashMap latestConfig = ConfigurationUtils.toMap(config.getFinalConfiguration());
                            Map map = ConfigurationUtils.diff(origConfig, latestConfig);
                            String[] tenantNamespaceArray = repositoryKey.split(Constants.KEY_ELEMENTS_DELIMETER);
                            updateConfigurationValues(tenantNamespaceArray[0], tenantNamespaceArray[1], map);
                        }
                    } else {
                        for (String configKey : inMemoryConfig.keySet()) {
                            repositoryKey = configKey;
                            AggregateConfiguration config = inMemoryConfig.get(repositoryKey);
                            if (config.containsConfig(file)) {
                                LinkedHashMap origConfig = ConfigurationUtils.toMap(config.getFinalConfiguration());
                                config.removeConfig(file);
                                LinkedHashMap latestConfig = ConfigurationUtils.toMap(config.getFinalConfiguration());
                                Map map = ConfigurationUtils.diff(origConfig, latestConfig);
                                String[] tenantNamespaceArray = repositoryKey.split(Constants.KEY_ELEMENTS_DELIMETER);
                                updateConfigurationValues(tenantNamespaceArray[0], tenantNamespaceArray[1], map);
                            }
                        }
                    }
                }
            }
        } catch (ClosedWatchServiceException exception) {
            // do nothing.
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void pollFilesystemAndUpdateNodeSpecificConfigurationIfRequired(String location) {
        try {
            Set<Path> paths = watchForChange(location);
            if (paths != null) {
                for (Path path : paths) {
                    File file = path.toAbsolutePath().toFile();

                    if (ConfigurationUtils.isConfig(file)) {
                        String repositoryKey = ConfigurationUtils.getConfigurationRepositoryKey(file);
                        ConfigurationRepository.lookup().populateOverrideConfiguration(repositoryKey, file);
                    } else {
                        ConfigurationRepository.lookup().removeOverrideConfiguration(file);
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Set<Path> watchForChange(String location) throws Exception {
        if (location == null || location.trim().length() == 0) {
            return Collections.emptySet();
        }
        File file = new File(location);
        if (!file.exists()) {
            return Collections.emptySet();
        }
        Path path = file.toPath();
        Set<Path> toReturn = new HashSet<>();
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            watchServiceCollection.put(location, watchService);
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            for (File dir : ConfigurationUtils.getAllFiles(file, true, true)) {
                dir.toPath().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
            }
            while (true) {
                final WatchKey wk = watchService.take();
                Thread.sleep(ConfigurationRepository.lookup()
                                     .getConfigurationFor(Constants.DEFAULT_TENANT, Constants.DB_NAMESPACE)
                                     .getLong("event.fetch.delay"));
                for (WatchEvent<?> event : wk.pollEvents()) {
                    Object context = event.context();
                    if (context instanceof Path) {
                        File newFile = new File(((Path) wk.watchable()).toFile(), context.toString());
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            if (newFile.isDirectory()) {
                                newFile.toPath().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,
                                        StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
                                continue;
                            }
                        } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            if (newFile.isDirectory()) {
                                continue;
                            }
                        }
                        toReturn.add(newFile.toPath());
                    }
                }
                if (toReturn.isEmpty()) {
                    continue;
                }
                break;
            }
        }
        return toReturn;
    }

    private void updateConfigurationValues(String tenant, String namespace, Map map) throws Exception {
        MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
        ObjectName mbeanName = new ObjectName(Constants.MBEAN_NAME);
        ConfigurationManager conf = JMX.newMBeanProxy(mbsc, mbeanName, ConfigurationManager.class, true);
        conf.updateConfigurationValues(tenant, namespace, map);
    }

    public void shutdown() {
        for (WatchService watch : watchServiceCollection.values()) {
            try {
                watch.close();
            } catch (IOException exception) {
                //do nothing
            }
        }
        executor.shutdownNow();
    }

    public void notifyChangesTowards(String tenant, String component, String key, ConfigurationChangeListener myself)
            throws Exception {
        List<NotificationData> notificationList = store.get(tenant + Constants.KEY_ELEMENTS_DELIMETER + component);
        if (notificationList == null) {
            notificationList = Collections.synchronizedList(new ArrayList<>());
            store.put(tenant + Constants.KEY_ELEMENTS_DELIMETER + component, notificationList);
            executor.scheduleWithFixedDelay(
                    () -> triggerScanning(tenant + Constants.KEY_ELEMENTS_DELIMETER + component), 1, 30000,
                    TimeUnit.MILLISECONDS);
        }
        notificationList.add(new NotificationData(tenant, component, key, myself));
    }

    private void triggerScanning(String key) {
        if (store.get(key) != null) {
            notificationExecutor.submit(() -> scanForChanges(key));
        } else {
            throw new IllegalArgumentException("Notification service for " + key + " is suspended.");
        }
    }

    private void scanForChanges(String key) {
        List<NotificationData> list = store.get(key);
        if (list != null) {
            list.stream().filter(NotificationData::isChanged)
                    .forEach(notificationData -> notificationExecutor.submit(() -> sendNotification(notificationData)));
        }
    }

    private void sendNotification(NotificationData notificationData) {
        try {
            notificationData.dispatchNotification();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void stopNotificationTowards(String tenant, String component, String key, ConfigurationChangeListener myself)
            throws Exception {
        List<NotificationData> notificationList = store.get(tenant + Constants.KEY_ELEMENTS_DELIMETER + component);
        if (notificationList != null) {
            boolean removed = notificationList.remove(new NotificationData(tenant, component, key, myself));
            if (removed && notificationList.isEmpty()) {
                store.remove(tenant + Constants.KEY_ELEMENTS_DELIMETER + component);
            }
        }

    }

    static class NotificationData {

        final String tenant;

        final String namespace;

        final String key;

        final ConfigurationChangeListener myself;

        Object currentValue;

        boolean isArray;

        public NotificationData(String tenant, String component, String key, ConfigurationChangeListener myself)
                throws Exception {
            this.tenant = tenant;
            this.namespace = component;
            this.key = key;
            this.myself = myself;
            if (!ConfigurationRepository.lookup().getConfigurationFor(tenant, component).containsKey(key)) {
                throw new RuntimeException("Key[" + key + "] not found.");
            }
            isArray = ConfigurationUtils.isArray(tenant, component, key, Hint.DEFAULT.value());
            if (isArray) {
                currentValue = ConfigurationManager.lookup().getAsStringValues(tenant, component, key);
            } else {
                currentValue = ConfigurationManager.lookup().getAsString(tenant, component, key);
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(tenant, namespace, key, myself, currentValue, isArray);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof NotificationData)) {
                return false;
            }
            NotificationData nd = (NotificationData) obj;
            return Objects.equals(tenant, nd.tenant) && Objects.equals(namespace, nd.namespace) && Objects.equals(key,
                    nd.key) && Objects.equals(myself, nd.myself) && Objects.equals(currentValue, nd.currentValue)
                           // it's either String or List<String>
                           && isArray == nd.isArray;
        }

        public boolean isChanged() {
            Object latestValue;
            try {
                if (isArray) {
                    latestValue = ConfigurationManager.lookup().getAsStringValues(tenant, namespace, key);
                } else {
                    latestValue = ConfigurationManager.lookup().getAsString(tenant, namespace, key);
                }
                if (!isArray) {
                    return !currentValue.equals(latestValue);
                } else {
                    Collection<String> oldCollection = (Collection<String>) currentValue;
                    Collection<String> newCollection = (Collection<String>) latestValue;
                    for (String val : oldCollection) {
                        if (!newCollection.remove(val)) {
                            return true;
                        }
                    }
                    return !newCollection.isEmpty();
                }
            } catch (Exception exception) {
                return false;
            }
        }

        public void dispatchNotification() throws Exception {
            Method method = null;
            Vector<Object> parameters = null;
            try {
                Object latestValue;
                if (isArray) {
                    latestValue = ConfigurationManager.lookup().getAsStringValues(tenant, namespace, key);
                } else {
                    latestValue = ConfigurationManager.lookup().getAsString(tenant, namespace, key);
                }
                Method[] methods = myself.getClass().getDeclaredMethods();
                if (methods != null && methods.length > 0) {
                    method = methods[0];
                    int paramCount = method.getParameterCount();
                    parameters = new Vector<>();
                    if (paramCount > 4) {
                        if (tenant.equals(Constants.DEFAULT_TENANT)) {
                            parameters.add(null);
                        } else {
                            parameters.add(tenant);
                        }
                    }
                    if (paramCount > 3) {
                        if (namespace.equals(Constants.DEFAULT_NAMESPACE)) {
                            parameters.add(null);
                        } else {
                            parameters.add(namespace);
                        }
                    }
                    parameters.add(key);
                    parameters.add(currentValue);
                    parameters.add(latestValue);
                    method.setAccessible(true);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                isArray = ConfigurationUtils.isArray(tenant, namespace, key, Hint.DEFAULT.value());
                if (isArray) {
                    currentValue = ConfigurationManager.lookup().getAsStringValues(tenant, namespace, key);
                } else {
                    currentValue = ConfigurationManager.lookup().getAsString(tenant, namespace, key);
                }
                if (method != null && parameters != null) {
                    method.invoke(myself, parameters.toArray());
                }
            }
        }
    }
}
