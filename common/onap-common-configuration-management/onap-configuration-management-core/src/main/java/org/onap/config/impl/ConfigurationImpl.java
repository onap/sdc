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

import static org.onap.config.ConfigurationUtils.isBlank;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.onap.config.ConfigurationUtils;
import org.onap.config.Constants;
import org.onap.config.NonConfigResource;
import org.onap.config.api.Config;
import org.onap.config.api.Hint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationImpl implements org.onap.config.api.Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationImpl.class);

    private static final String KEY_CANNOT_BE_NULL = "Key can't be null.";
    private static final NonConfigResource NON_CONFIG_RESOURCE = new NonConfigResource();
    private static final Map<String, AggregateConfiguration> MODULE_CONFIG_STORE = new HashMap<>();

    static {
        boolean classPathConfigLoadFailed = loadClassPathConfigurationsAndResources();
        boolean additionalConfLoadFailed = loadAdditionalConfigurationsAndResources();
        boolean tenantConfigLoadFailed = loadTenantConfigurations();
        if (classPathConfigLoadFailed || additionalConfLoadFailed || tenantConfigLoadFailed) {
            throw new IllegalStateException("Failed to initialize configuration");
        }
        populateFinalConfigurationIncrementally(MODULE_CONFIG_STORE);
        loadNodeSpecificConfigurations();
    }

    private static boolean loadClassPathConfigurationsAndResources() {
        List<URL> classpathResources = ConfigurationUtils.getAllClassPathResources();
        Predicate<URL> predicate = ConfigurationUtils::isConfig;
        Map<Boolean, List<URL>> resources = classpathResources.stream().collect(Collectors.partitioningBy(predicate));
        List<URL> configResources = resources.get(true);
        List<URL> nonConfigResources = resources.get(false);
        AtomicReference<Boolean> failedFlagHolder = new AtomicReference<>(false);
        configResources.forEach(url -> {
            String moduleName = ConfigurationUtils.getConfigurationRepositoryKey(url);
            AggregateConfiguration moduleConfig = MODULE_CONFIG_STORE.get(moduleName);
            if (moduleConfig == null) {
                moduleConfig = new AggregateConfiguration();
                MODULE_CONFIG_STORE.put(moduleName, moduleConfig);
            }
            try {
                moduleConfig.addConfig(url);
            } catch (ConfigurationException e) {
                failedFlagHolder.set(true);
            }
        });
        nonConfigResources.forEach(NON_CONFIG_RESOURCE::add);
        return failedFlagHolder.get();
    }

    private static boolean loadAdditionalConfigurationsAndResources() {
        String configLocation = System.getProperty("config.location");
        AtomicReference<Boolean> failedFlagHolder = new AtomicReference<>(false);
        if (!isBlank(configLocation)) {
            File root = new File(configLocation);
            Collection<File> filesystemResources = ConfigurationUtils.getAllFiles(root, true, false);
            Predicate<File> filePredicate = ConfigurationUtils::isConfig;
            Map<Boolean, List<File>> resources = filesystemResources.stream().collect(Collectors.partitioningBy(filePredicate));
            List<File> configResources = resources.get(true);
            List<File> nonConfigResources = resources.get(false);
            configResources.forEach(file -> {
                String moduleName = ConfigurationUtils.getConfigurationRepositoryKey(file);
                AggregateConfiguration moduleConfig = MODULE_CONFIG_STORE.get(moduleName);
                if (moduleConfig == null) {
                    moduleConfig = new AggregateConfiguration();
                    MODULE_CONFIG_STORE.put(moduleName, moduleConfig);
                }
                try {
                    moduleConfig.addConfig(file);
                } catch (ConfigurationException e) {
                    failedFlagHolder.set(true);
                }
            });
            nonConfigResources.forEach(NON_CONFIG_RESOURCE::add);
        }
        return failedFlagHolder.get();
    }

    private static boolean loadTenantConfigurations() {
        String tenantConfigLocation = System.getProperty("tenant.config.location");
        AtomicReference<Boolean> failedFlagHolder = new AtomicReference<>(false);
        if (!isBlank(tenantConfigLocation)) {
            File root = new File(tenantConfigLocation);
            Collection<File> tenantsRoot = ConfigurationUtils.getAllFiles(root, false, true);
            Collection<File> filesystemResources = ConfigurationUtils.getAllFiles(root, true, false);
            filesystemResources.stream().filter(ConfigurationUtils::isConfig).forEach(configFile -> {
                AtomicReference<String> moduleNameHolder = new AtomicReference<>(ConfigurationUtils.getNamespace(configFile));
                tenantsRoot.stream().filter(t -> configFile.getAbsolutePath().startsWith(t.getAbsolutePath())).forEach(
                        f -> moduleNameHolder.set(ConfigurationUtils.getConfigurationRepositoryKey(
                                (f.getName().toUpperCase() + Constants.TENANT_NAMESPACE_SEPARATOR + moduleNameHolder.get())
                                        .split(Constants.TENANT_NAMESPACE_SEPARATOR))
                        ));
                AggregateConfiguration moduleConfig = MODULE_CONFIG_STORE.get(moduleNameHolder.get());
                if (moduleConfig == null) {
                    moduleConfig = new AggregateConfiguration();
                    MODULE_CONFIG_STORE.put(moduleNameHolder.get(), moduleConfig);
                }
                try {
                    moduleConfig.addConfig(configFile);
                } catch (ConfigurationException e) {
                    failedFlagHolder.set(true);
                }
            });
        }
        return failedFlagHolder.get();
    }

    private static void loadNodeSpecificConfigurations() {
        String nodeConfigLocation = System.getProperty("node.config.location");
        if (!isBlank(nodeConfigLocation)) {
            File root = new File(nodeConfigLocation);
            Collection<File> filesystemResources = ConfigurationUtils.getAllFiles(root, true, false);
            filesystemResources.stream().filter(ConfigurationUtils::isConfig).forEach(
                    file -> ConfigurationRepository.lookup().populateOverrideConfiguration(
                            ConfigurationUtils.getConfigurationRepositoryKey(
                                    ConfigurationUtils.getNamespace(file).split(Constants.TENANT_NAMESPACE_SEPARATOR)), file)
            );
        }
    }

    private static void populateFinalConfigurationIncrementally(Map<String, AggregateConfiguration> configs) {

        if (configs.get(Constants.DEFAULT_TENANT + Constants.KEY_ELEMENTS_DELIMITER + Constants.DB_NAMESPACE) != null) {
            ConfigurationRepository.lookup().populateConfiguration(
                    Constants.DEFAULT_TENANT + Constants.KEY_ELEMENTS_DELIMITER + Constants.DB_NAMESPACE,
                    configs.remove(Constants.DEFAULT_TENANT + Constants.KEY_ELEMENTS_DELIMITER + Constants.DB_NAMESPACE)
                            .getFinalConfiguration());
        }
        Set<String> modules = configs.keySet();
        modules.forEach(
                m -> ConfigurationRepository.lookup().populateConfiguration(m, configs.get(m).getFinalConfiguration())
        );
    }

    @Override
    public <T> T get(String tenant, String namespace, String key, Class<T> clazz, Hint... hints) {
        String[] tenantNamespaceArray;
        if (tenant == null && namespace != null) {
            tenantNamespaceArray = namespace.split(Constants.TENANT_NAMESPACE_SEPARATOR);
            if (tenantNamespaceArray.length > 1) {
                tenant = tenantNamespaceArray[0];
                namespace = tenantNamespaceArray[1];
            }
        }

        tenant = ConfigurationRepository.lookup().isValidTenant(tenant) ? tenant.toUpperCase() : Constants.DEFAULT_TENANT;
        namespace = ConfigurationRepository.lookup().isValidNamespace(namespace) ? namespace.toUpperCase() : Constants.DEFAULT_NAMESPACE;
        hints = hints == null || hints.length == 0 ? new Hint[]{Hint.EXTERNAL_LOOKUP, Hint.NODE_SPECIFIC} : hints;
        T returnValue;
        returnValue = getInternal(tenant, namespace, key, clazz, hints);
        if ((returnValue == null || ConfigurationUtils.isZeroLengthArray(clazz, returnValue))
                && !Constants.DEFAULT_TENANT.equals(tenant)) {
            returnValue = getInternal(Constants.DEFAULT_TENANT, namespace, key, clazz, hints);
        }
        if ((returnValue == null || ConfigurationUtils.isZeroLengthArray(clazz, returnValue))
                && !Constants.DEFAULT_NAMESPACE.equals(namespace)) {
            returnValue = getInternal(tenant, Constants.DEFAULT_NAMESPACE, key, clazz, hints);
        }
        if ((returnValue == null || ConfigurationUtils.isZeroLengthArray(clazz, returnValue))
                && !Constants.DEFAULT_NAMESPACE.equals(namespace) && !Constants.DEFAULT_TENANT.equals(tenant)) {
            returnValue = getInternal(Constants.DEFAULT_TENANT, Constants.DEFAULT_NAMESPACE, key, clazz, hints);
        }
        if (returnValue == null && ConfigurationUtils.isAPrimitive(clazz)) {
            returnValue = (T) ConfigurationUtils.getDefaultFor(clazz);
        }
        return returnValue;
    }

    @Override
    public <T> Map<String, T> populateMap(String tenantId, String namespace, String key, Class<T> clazz) {
        final String calculatedTenantId = calculateTenant(tenantId);
        final String calculatedNamespace = calculateNamespace(namespace);
        Map<String, T> map = new HashMap<>();
        Iterator<String> keys;
        try {
            keys = ConfigurationRepository.lookup().getConfigurationFor(calculatedTenantId, calculatedNamespace).getKeys(key);
            keys.forEachRemaining(k -> {
                if (k.startsWith(key + ".")) {
                    k = k.substring(key.length() + 1);
                    String subkey = k.substring(0, k.indexOf('.'));
                    if (!map.containsKey(subkey)) {
                        map.put(subkey, get(calculatedTenantId, calculatedNamespace, key + "." + subkey, clazz));
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.warn(
                    "Couldn't populate map fot tenant: {}, namespace: {}, key: {}, type: {}",
                    tenantId,
                    namespace,
                    key,
                    clazz.getSimpleName(),
                    e
            );
        }
        return map;
    }

    @Override
    public Map<Object, Object> generateMap(String tenantId, String namespace, String key) {
        final String calculatedTenantId = calculateTenant(tenantId);
        final String calculatedNamespace = calculateNamespace(namespace);
        Map<Object, Object> parentMap = new HashMap<>();
        Iterator<String> configKeys;
        try {
            if (isBlank(key)) {
                configKeys = ConfigurationRepository.lookup().getConfigurationFor(calculatedTenantId, calculatedNamespace).getKeys();
            } else {
                configKeys = ConfigurationRepository.lookup().getConfigurationFor(calculatedTenantId, calculatedNamespace).getKeys(key);
            }
            configKeys.forEachRemaining(subKey -> {
                if (!isBlank(key) && !subKey.startsWith(key + ".")) {
                    configKeys.remove();
                }
                parseConfigSubKeys(subKey, key, calculatedTenantId, calculatedNamespace, parentMap);
            });
        } catch (Exception e) {
            LOGGER.warn(
                    "Couldn't generate map fot tenant: {}, namespace: {}, key: {}",
                    tenantId,
                    namespace,
                    key,
                    e
            );
        }
        return parentMap;
    }

    private void parseConfigSubKeys(String subKey, String keyPrefix, String tenantId, String namespace, Map<Object, Object> targetMap) {
        String value = getAsString(tenantId, namespace, subKey);
        if (!isBlank(keyPrefix) && subKey.startsWith(keyPrefix + ".")) {
            subKey = subKey.substring(keyPrefix.trim().length() + 1);
        }
        while (subKey.contains(".")) {
            String subSubKey = subKey.substring(0, subKey.indexOf('.'));
            subKey = subKey.substring(subKey.indexOf('.') + 1);
            if (!targetMap.containsKey(subSubKey)) {
                Map<Object, Object> subMap = new HashMap<>();
                targetMap.put(subSubKey, subMap);
                targetMap= subMap;
            } else {
                targetMap = (Map<Object, Object>) targetMap.get(subSubKey);
            }
        }
        targetMap.put(subKey, value);
    }

    protected <T> T getInternal(String tenant, String namespace, String key, Class<T> clazz, Hint... hints) {
        int processingHints = Hint.DEFAULT.value();
        if (hints != null) {
            for (Hint hint : hints) {
                processingHints = processingHints | hint.value();
            }
        }

        tenant = calculateTenant(tenant);
        namespace = calculateNamespace(namespace);

        if (isBlank(key) && !clazz.isAnnotationPresent(Config.class)) {
            throw new IllegalArgumentException(KEY_CANNOT_BE_NULL);
        }

        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null.");
        }

        if (ConfigurationUtils.isAPrimitive(clazz)) {
            clazz = getWrapperClass(clazz);
        }
        try {
            if (ConfigurationUtils.isWrapperClass(clazz)) {
                return getWrapperTypeValue(tenant, namespace, key, clazz, processingHints);
            } else if (ConfigurationUtils.isAPrimitivesOrWrappersArray(clazz)) {
                return getArrayTypeValue(tenant, namespace, key, clazz, processingHints);
            } else if (clazz.isAnnotationPresent(Config.class)) {
                return getAnnotatedTypeValue(tenant, namespace, clazz, isBlank(key) ? "" : (key + "."), hints);
            } else {
                throw new IllegalArgumentException(
                        "Only primitive classes, wrapper classes, corresponding array classes and any "
                                + "class decorated with @org.openecomp.config.api.Config are allowed as argument.");
            }
        } catch (Exception exception) {
            LOGGER.warn(
                    "Failed to get internal value fot tenant: {}, namespace: {}, key: {}, type: {}",
                    tenant,
                    namespace,
                    key,
                    clazz.getSimpleName(),
                    exception
            );
        }
        return null;
    }

    private <T> T getWrapperTypeValue(String tenant, String namespace, String key, Class<T> clazz, int processingHints) throws Exception {
        Object obj = ConfigurationUtils.getProperty(
                ConfigurationRepository.lookup().getConfigurationFor(tenant, namespace), key, processingHints);
        if (obj != null) {
            if (ConfigurationUtils.isCollection(obj.toString())) {
                obj = ConfigurationUtils.getCollectionString(obj.toString());
            }
            String value = ConfigurationUtils.processVariablesIfPresent(
                    tenant,
                    namespace,
                    obj.toString().split(",")[0]
            );
            return getTypeValue(value, clazz, processingHints);
        }
        return null;
    }

    private <T> T getArrayTypeValue(String tenant, String namespace, String key, Class<T> clazz, int processingHints) throws Exception {
        Object obj = ConfigurationUtils.getProperty(
                ConfigurationRepository.lookup().getConfigurationFor(tenant, namespace), key, processingHints);
        if (obj != null) {
            Class componentType = clazz.getComponentType();
            if (ConfigurationUtils.isAPrimitivesArray(clazz)) {
                componentType = getWrapperClass(componentType);
            }
            String collString = ConfigurationUtils.getCollectionString(obj.toString());
            ArrayList<String> tempCollection = new ArrayList<>();
            for (String itemValue : collString.split(",")) {
                tempCollection.add(ConfigurationUtils.processVariablesIfPresent(tenant, namespace, itemValue));
            }
            Collection<T> collection = convert(
                    ConfigurationUtils.getCollectionString(Arrays.toString(tempCollection.toArray())),
                    (Class<T>) componentType,
                    processingHints
            );
            if (ConfigurationUtils.isAPrimitivesArray(clazz)) {
                return (T) ConfigurationUtils.getPrimitiveArray(collection, componentType);
            } else {
                return (T) collection.toArray(getZeroLengthArrayFor(getWrapperClass(componentType)));
            }
        } else {
            return null;
        }
    }

    private static String calculateNamespace(String namespace) {

        if (isBlank(namespace)) {
            return Constants.DEFAULT_NAMESPACE;
        }

        return namespace.toUpperCase();
    }

    private static String calculateTenant(String tenant) {

        if (isBlank(tenant)) {
            return Constants.DEFAULT_TENANT;
        }

        return tenant.toUpperCase();
    }

    private <T> T getAnnotatedTypeValue(String tenant, String namespace, Class<T> clazz, String keyPrefix, Hint... hints)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Config confAnnotation = clazz.getAnnotation(Config.class);
        if (confAnnotation != null && confAnnotation.key().length() > 0 && !keyPrefix.endsWith(".")) {
            keyPrefix += (confAnnotation.key() + ".");
        }
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        T objToReturn = constructor.newInstance();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Config fieldConfAnnotation = field.getAnnotation(Config.class);
            Class<?> fieldType = field.getType();
            if (fieldConfAnnotation != null) {
                if (ConfigurationUtils.isAPrimitiveOrWrapper(fieldType) ||
                        ConfigurationUtils.isAPrimitivesOrWrappersArray(fieldType)) {
                    setPrimitiveField(field, objToReturn, tenant, namespace, keyPrefix, hints);
                }
                if (ConfigurationUtils.isACollection(fieldType)) {
                    setCollectionField(field, objToReturn, tenant, namespace, keyPrefix, hints);
                }
                if (ConfigurationUtils.isAMap(fieldType)) {
                    setMapField(field, objToReturn, tenant, namespace, keyPrefix);
                }
            }
        }
        return objToReturn;
    }

    private void setPrimitiveField(Field field, Object objToReturn, String tenant, String namespace, String keyPrefix, Hint[] hints)
            throws IllegalAccessException {
        String fieldConfAnnotationKey = field.getAnnotation(Config.class).key();
        Class<?> fieldType = field.getType();
        field.set(objToReturn, get(tenant, namespace, keyPrefix + fieldConfAnnotationKey, fieldType, hints));
    }

    private void setMapField(Field field, Object objToReturn, String tenant, String namespace, String keyPrefix)
            throws IllegalAccessException {
        String fieldConfAnnotationKey = field.getAnnotation(Config.class).key();
        field.set(objToReturn, generateMap(tenant, namespace, keyPrefix + fieldConfAnnotationKey));
    }

    private void setCollectionField(Field field, Object objToReturn, String tenant, String namespace, String keyPrefix, Hint[] hints)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        String fieldConfAnnotationKey = field.getAnnotation(Config.class).key();
        Class<?> fieldType = field.getType();
        Object obj = get(tenant, namespace, keyPrefix + fieldConfAnnotationKey,
                ConfigurationUtils.getArrayClass(ConfigurationUtils.getCollectionGenericType(field)),
                hints);
        if (obj != null) {
            List<Object> list = Arrays.asList((Object[]) obj);
            Class clazzToInstantiate;
            if (fieldType.isInterface()) {
                clazzToInstantiate = ConfigurationUtils.getConcreteCollection(fieldType).getClass();
            } else if (Modifier.isAbstract(fieldType.getModifiers())) {
                clazzToInstantiate =
                        ConfigurationUtils.getCompatibleCollectionForAbstractDef(fieldType)
                                .getClass();
            } else {
                clazzToInstantiate = fieldType;
            }
            Constructor construct = getConstructorWithArguments(clazzToInstantiate, Collection.class);

            if (construct != null) {
                construct.setAccessible(true);
                field.set(objToReturn, construct.newInstance(list));
            } else {
                construct = getConstructorWithArguments(clazzToInstantiate, Integer.class,
                        Boolean.class, Collection.class);
                if (construct != null) {
                    construct.setAccessible(true);
                    field.set(objToReturn, construct.newInstance(list.size(), true, list));
                }
            }
        }
    }

    private Constructor getConstructorWithArguments(Class<?> clazz, Class<?>... classes) {
        try {
            return clazz.getDeclaredConstructor(classes);
        } catch (Exception exception) {
            LOGGER.warn("Failed to get {} constructor.", clazz.getSimpleName(), exception);
            return null;
        }
    }

    private Class getWrapperClass(Class<?> clazz) {
        if (byte.class == clazz) {
            return Byte.class;
        } else if (short.class == clazz) {
            return Short.class;
        } else if (int.class == clazz) {
            return Integer.class;
        } else if (long.class == clazz) {
            return Long.class;
        } else if (float.class == clazz) {
            return Float.class;
        } else if (double.class == clazz) {
            return Double.class;
        } else if (char.class == clazz) {
            return Character.class;
        } else if (boolean.class == clazz) {
            return Boolean.class;
        }
        return clazz;
    }

    private <T> T getTypeValue(Object obj, Class<T> clazz, int processingHint) {
        if (obj == null || obj.toString().trim().length() == 0) {
            return null;
        } else {
            obj = obj.toString().trim();
        }
        if (String.class.equals(clazz)) {
            return getValueForStringType(obj, processingHint);
        }
        if (Number.class.isAssignableFrom(clazz)) {
            return getValueForNumbersType(obj, clazz);
        }
        if (Boolean.class.equals(clazz)) {
            return (T) Boolean.valueOf(obj.toString());
        }
        if (Character.class.equals(clazz)) {
            return (T) Character.valueOf(obj.toString().charAt(0));
        }
        return null;
    }

    private <T> T getValueForStringType(Object obj, int processingHint) {
        if (obj.toString().startsWith("@") && ConfigurationUtils.isExternalLookup(processingHint)) {
            String subString = obj.toString().substring(1).trim();
            String contents = ConfigurationUtils.getFileContents(
                    NON_CONFIG_RESOURCE.locate(subString));
            if (contents == null) {
                contents = ConfigurationUtils.getFileContents(subString);
            }
            if (contents != null) {
                obj = contents;
            }
        }
        return (T) obj.toString();
    }

    private <T> T getValueForNumbersType(Object obj, Class<T> clazz) {
        Double doubleValue = Double.valueOf(obj.toString());
        switch (clazz.getName()) {
            case "java.lang.Byte":
                Byte byteVal = doubleValue.byteValue();
                return (T) byteVal;
            case "java.lang.Short":
                Short shortVal = doubleValue.shortValue();
                return (T) shortVal;
            case "java.lang.Integer":
                Integer intVal = doubleValue.intValue();
                return (T) intVal;
            case "java.lang.Long":
                Long longVal = doubleValue.longValue();
                return (T) longVal;
            case "java.lang.Float":
                Float floatVal = doubleValue.floatValue();
                return (T) floatVal;
            case "java.lang.Double":
                return (T) doubleValue;
            default:
                return null;
        }
    }

    private <T> T[] getZeroLengthArrayFor(Class<T> clazz) {
        Object objToReturn = null;
        if (ConfigurationUtils.isAPrimitive(clazz)) {
            objToReturn = ConfigurationUtils.getPrimitiveArray(Collections.emptyList(), clazz);
        } else if (ConfigurationUtils.isWrapperClass(clazz)) {
            objToReturn = ConfigurationUtils.getWrappersArray(Collections.emptyList(), clazz);
        }
        return (T[]) objToReturn;
    }

    private <T> Collection<T> convert(String commaSeparatedValues, Class<T> clazz, int processingHints) {
        ArrayList<T> collection = new ArrayList<>();
        for (String value : commaSeparatedValues.split(",")) {
            try {
                T type1 = getTypeValue(value, clazz, processingHints);
                if (type1 != null) {
                    collection.add(type1);
                }
            } catch (RuntimeException re) {
                LOGGER.warn("Failed to convert {}", commaSeparatedValues, re);
            }
        }
        return collection;
    }
}
