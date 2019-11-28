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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
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

    static {

        try {
            init();
        } catch (ConfigurationException e) {
            throw new IllegalStateException("Failed to initialize configuration", e);
        }
    }

    private static void init() throws ConfigurationException {

        Map<String, AggregateConfiguration> moduleConfigStore = new HashMap<>();
        List<URL> classpathResources = ConfigurationUtils.getAllClassPathResources();
        Predicate<URL> predicate = ConfigurationUtils::isConfig;
        for (URL url : classpathResources) {
            if (predicate.test(url)) {
                String moduleName = ConfigurationUtils.getConfigurationRepositoryKey(url);
                AggregateConfiguration moduleConfig = moduleConfigStore.get(moduleName);
                if (moduleConfig == null) {
                    moduleConfig = new AggregateConfiguration();
                    moduleConfigStore.put(moduleName, moduleConfig);
                }
                moduleConfig.addConfig(url);
            } else {
                NON_CONFIG_RESOURCE.add(url);
            }
        }
        String configLocation = System.getProperty("config.location");
        if (!isBlank(configLocation)) {
            File root = new File(configLocation);
            Collection<File> filesystemResources = ConfigurationUtils.getAllFiles(root, true, false);
            Predicate<File> filePredicate = ConfigurationUtils::isConfig;
            for (File file : filesystemResources) {
                if (filePredicate.test(file)) {
                    String moduleName = ConfigurationUtils.getConfigurationRepositoryKey(file);
                    AggregateConfiguration moduleConfig = moduleConfigStore.get(moduleName);
                    if (moduleConfig == null) {
                        moduleConfig = new AggregateConfiguration();
                        moduleConfigStore.put(moduleName, moduleConfig);
                    }
                    moduleConfig.addConfig(file);
                } else {
                    NON_CONFIG_RESOURCE.add(file);
                }
            }
        }
        String tenantConfigLocation = System.getProperty("tenant.config.location");
        if (!isBlank(tenantConfigLocation)) {
            File root = new File(tenantConfigLocation);
            Collection<File> tenantsRoot = ConfigurationUtils.getAllFiles(root, false, true);
            Collection<File> filesystemResources = ConfigurationUtils.getAllFiles(root, true, false);
            Predicate<File> filePredicate = ConfigurationUtils::isConfig;
            for (File file : filesystemResources) {
                if (filePredicate.test(file)) {
                    String moduleName = ConfigurationUtils.getNamespace(file);
                    for (File tenantFileRoot : tenantsRoot) {
                        if (file.getAbsolutePath().startsWith(tenantFileRoot.getAbsolutePath())) {
                            moduleName = ConfigurationUtils.getConfigurationRepositoryKey(
                                    (tenantFileRoot.getName().toUpperCase() + Constants.TENANT_NAMESPACE_SEPARATOR
                                             + moduleName).split(Constants.TENANT_NAMESPACE_SEPARATOR));
                        }
                    }
                    AggregateConfiguration moduleConfig = moduleConfigStore.get(moduleName);
                    if (moduleConfig == null) {
                        moduleConfig = new AggregateConfiguration();
                        moduleConfigStore.put(moduleName, moduleConfig);
                    }
                    moduleConfig.addConfig(file);
                }
            }
        }

        populateFinalConfigurationIncrementally(moduleConfigStore);
        String nodeConfigLocation = System.getProperty("node.config.location");
        if (!isBlank(nodeConfigLocation)) {
            File root = new File(nodeConfigLocation);
            Collection<File> filesystemResources = ConfigurationUtils.getAllFiles(root, true, false);
            Predicate<File> filePredicate = ConfigurationUtils::isConfig;
            for (File file : filesystemResources) {
                if (filePredicate.test(file)) {
                    ConfigurationRepository.lookup().populateOverrideConfiguration(
                            ConfigurationUtils.getConfigurationRepositoryKey(
                                    ConfigurationUtils.getNamespace(file).split(Constants.TENANT_NAMESPACE_SEPARATOR)),
                            file);
                }
            }
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
        for (String module : modules) {
            ConfigurationRepository.lookup().populateConfiguration(module, configs.get(module).getFinalConfiguration());
        }
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

        tenant = ConfigurationRepository.lookup().isValidTenant(tenant) ? tenant.toUpperCase()
                         : Constants.DEFAULT_TENANT;
        namespace = ConfigurationRepository.lookup().isValidNamespace(namespace) ? namespace.toUpperCase()
                            : Constants.DEFAULT_NAMESPACE;
        T returnValue;
        returnValue = (T) getInternal(tenant, namespace, key, clazz.isPrimitive() ? getWrapperClass(clazz) : clazz,
                hints == null || hints.length == 0 ? new Hint[] {Hint.EXTERNAL_LOOKUP, Hint.NODE_SPECIFIC} : hints);
        if ((returnValue == null || ConfigurationUtils.isZeroLengthArray(clazz, returnValue))
                    && !Constants.DEFAULT_TENANT.equals(tenant)) {
            returnValue = (T) getInternal(Constants.DEFAULT_TENANT, namespace, key,
                    clazz.isPrimitive() ? getWrapperClass(clazz) : clazz,
                    hints == null || hints.length == 0 ? new Hint[] {Hint.EXTERNAL_LOOKUP, Hint.NODE_SPECIFIC} : hints);
        }
        if ((returnValue == null || ConfigurationUtils.isZeroLengthArray(clazz, returnValue))
                    && !Constants.DEFAULT_NAMESPACE.equals(namespace)) {
            returnValue = (T) getInternal(tenant, Constants.DEFAULT_NAMESPACE, key,
                    clazz.isPrimitive() ? getWrapperClass(clazz) : clazz,
                    hints == null || hints.length == 0 ? new Hint[] {Hint.EXTERNAL_LOOKUP, Hint.NODE_SPECIFIC} : hints);
        }
        if ((returnValue == null || ConfigurationUtils.isZeroLengthArray(clazz, returnValue))
                    && !Constants.DEFAULT_NAMESPACE.equals(namespace) && !Constants.DEFAULT_TENANT.equals(tenant)) {
            returnValue = (T) getInternal(Constants.DEFAULT_TENANT, Constants.DEFAULT_NAMESPACE, key,
                    clazz.isPrimitive() ? getWrapperClass(clazz) : clazz,
                    hints == null || hints.length == 0 ? new Hint[] {Hint.EXTERNAL_LOOKUP, Hint.NODE_SPECIFIC} : hints);
        }
        if (returnValue == null && clazz.isPrimitive()) {
            return (T) ConfigurationUtils.getDefaultFor(clazz);
        } else {
            return returnValue;
        }
    }

    @Override
    public <T> Map<String, T> populateMap(String tenantId, String namespace, String key, Class<T> clazz) {

        tenantId = calculateTenant(tenantId);
        namespace = calculateNamespace(namespace);
        Map<String, T> map = new HashMap<>();
        Iterator<String> keys;
        try {
            keys = ConfigurationRepository.lookup().getConfigurationFor(tenantId, namespace).getKeys(key);
            while (keys.hasNext()) {
                String k = keys.next();
                if (k.startsWith(key + ".")) {
                    k = k.substring(key.length() + 1);
                    String subkey = k.substring(0, k.indexOf('.'));
                    if (!map.containsKey(subkey)) {
                        map.put(subkey, get(tenantId, namespace, key + "." + subkey, clazz));
                    }
                }
            }
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
    public Map generateMap(String tenantId, String namespace, String key) {

        tenantId = calculateTenant(tenantId);
        namespace = calculateNamespace(namespace);

        Map map;
        Map parentMap = new HashMap<>();
        Iterator<String> keys;
        try {
            if (isBlank(key)) {
                keys = ConfigurationRepository.lookup().getConfigurationFor(tenantId, namespace).getKeys();
            } else {
                keys = ConfigurationRepository.lookup().getConfigurationFor(tenantId, namespace).getKeys(key);
            }
            while (keys.hasNext()) {
                map = parentMap;
                String k = keys.next();

                if (!isBlank(key) && !k.startsWith(key + ".")) {
                    continue;
                }
                String value = getAsString(tenantId, namespace, k);
                if (!isBlank(key) && k.startsWith(key + ".")) {
                    k = k.substring(key.trim().length() + 1);
                }

                while (k.contains(".")) {
                    if (k.contains(".")) {
                        String subkey = k.substring(0, k.indexOf('.'));
                        k = k.substring(k.indexOf('.') + 1);
                        if (!map.containsKey(subkey)) {
                            Map tmp = new HashMap();
                            map.put(subkey, tmp);
                            map = tmp;
                        } else {
                            map = (Map) map.get(subkey);
                        }
                    }
                }
                map.put(k, value);
            }
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

        if (clazz.isPrimitive()) {
            clazz = getWrapperClass(clazz);
        }
        try {
            if (ConfigurationUtils.isWrapperClass(clazz) || clazz.isPrimitive()) {
                Object obj = ConfigurationUtils.getProperty(
                        ConfigurationRepository.lookup().getConfigurationFor(tenant, namespace), key, processingHints);
                if (obj != null) {
                    if (ConfigurationUtils.isCollection(obj.toString())) {
                        obj = ConfigurationUtils.getCollectionString(obj.toString());
                    }
                    String value = obj.toString().split(",")[0];
                    value = ConfigurationUtils.processVariablesIfPresent(tenant, namespace, value);
                    return (T) getValue(value, clazz.isPrimitive() ? getWrapperClass(clazz) : clazz, processingHints);
                } else {
                    return null;
                }
            } else if (clazz.isArray() && (clazz.getComponentType().isPrimitive() || ConfigurationUtils.isWrapperClass(
                    clazz.getComponentType()))) {
                Object obj = ConfigurationUtils.getProperty(
                        ConfigurationRepository.lookup().getConfigurationFor(tenant, namespace), key, processingHints);
                if (obj != null) {
                    Class componentClass = clazz.getComponentType();
                    if (clazz.getComponentType().isPrimitive()) {
                        componentClass = getWrapperClass(clazz.getComponentType());
                    }
                    String collString = ConfigurationUtils.getCollectionString(obj.toString());
                    ArrayList<String> tempCollection = new ArrayList<>();
                    for (String itemValue : collString.split(",")) {
                        tempCollection.add(ConfigurationUtils.processVariablesIfPresent(tenant, namespace, itemValue));
                    }
                    Collection<T> collection =
                            convert(ConfigurationUtils.getCollectionString(Arrays.toString(tempCollection.toArray())),
                                    componentClass, processingHints);
                    if (clazz.getComponentType().isPrimitive()) {
                        return (T) ConfigurationUtils.getPrimitiveArray(collection, clazz.getComponentType());
                    } else {
                        return (T) collection.toArray(getZeroLengthArrayFor(getWrapperClass(clazz.getComponentType())));
                    }
                } else {
                    return null;
                }
            } else if (clazz.isAnnotationPresent(Config.class)) {
                return read(tenant, namespace, clazz, isBlank(key) ? "" : (key + "."), hints);
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

    private <T> T read(String tenant, String namespace, Class<T> clazz, String keyPrefix, Hint... hints)
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
            if (fieldConfAnnotation != null) {
                if (field.getType().isPrimitive() || ConfigurationUtils.isWrapperClass(field.getType()) || (
                        field.getType().isArray() && (field.getType().getComponentType().isPrimitive()
                                                              || ConfigurationUtils.isWrapperClass(
                                field.getType().getComponentType())))
                            || field.getType().getAnnotation(Config.class) != null) {
                    field.set(objToReturn,
                            get(tenant, namespace, keyPrefix + fieldConfAnnotation.key(), field.getType(), hints));
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    Object obj = get(tenant, namespace, keyPrefix + fieldConfAnnotation.key(),
                            ConfigurationUtils.getArrayClass(ConfigurationUtils.getCollectionGenericType(field)),
                            hints);
                    if (obj != null) {
                        List list = Arrays.asList((Object[]) obj);
                        Class clazzToInstantiate;
                        if (field.getType().isInterface()) {
                            clazzToInstantiate = ConfigurationUtils.getConcreteCollection(field.getType()).getClass();
                        } else if (Modifier.isAbstract(field.getType().getModifiers())) {
                            clazzToInstantiate =
                                    ConfigurationUtils.getCompatibleCollectionForAbstractDef(field.getType())
                                            .getClass();
                        } else {
                            clazzToInstantiate = field.getType();
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
                } else if (Map.class.isAssignableFrom(field.getType())) {
                    field.set(objToReturn, generateMap(tenant, namespace, keyPrefix + fieldConfAnnotation.key()));
                }
            }
        }
        return objToReturn;
    }

    private Constructor getConstructorWithArguments(Class clazz, Class... classes) {
        try {
            return clazz.getDeclaredConstructor(classes);
        } catch (Exception exception) {
            LOGGER.warn("Failed to get {} constructor.", clazz.getSimpleName(), exception);
            return null;
        }
    }

    private Class getWrapperClass(Class clazz) {
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

    private <T> T getValue(Object obj, Class<T> clazz, int processingHint) {

        if (obj == null || obj.toString().trim().length() == 0) {
            return null;
        } else {
            obj = obj.toString().trim();
        }

        if (String.class.equals(clazz)) {
            if (obj.toString().startsWith("@") && ConfigurationUtils.isExternalLookup(processingHint)) {
                String contents = ConfigurationUtils.getFileContents(
                        NON_CONFIG_RESOURCE.locate(obj.toString().substring(1).trim()));
                if (contents == null) {
                    contents = ConfigurationUtils.getFileContents(obj.toString().substring(1).trim());
                }
                if (contents != null) {
                    obj = contents;
                }
            }
            return (T) obj.toString();
        } else if (Number.class.isAssignableFrom(clazz)) {
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
            }
        } else if (Boolean.class.equals(clazz)) {
            return (T) Boolean.valueOf(obj.toString());
        } else if (Character.class.equals(clazz)) {
            return (T) Character.valueOf(obj.toString().charAt(0));
        }
        return null;
    }

    private <T> T[] getZeroLengthArrayFor(Class<T> clazz) {
        Object obj = null;
        if (clazz == int.class) {
            obj = new int[] {};
        } else if (clazz == byte.class) {
            obj = new byte[] {};
        } else if (clazz == short.class) {
            obj = new short[] {};
        } else if (clazz == long.class) {
            obj = new long[] {};
        } else if (clazz == float.class) {
            obj = new float[] {};
        } else if (clazz == double.class) {
            obj = new double[] {};
        } else if (clazz == boolean.class) {
            obj = new boolean[] {};
        } else if (clazz == char.class) {
            obj = new char[] {};
        } else if (clazz == Byte.class) {
            obj = new Byte[] {};
        } else if (clazz == Short.class) {
            obj = new Short[] {};
        } else if (clazz == Integer.class) {
            obj = new Integer[] {};
        } else if (clazz == Long.class) {
            obj = new Long[] {};
        } else if (clazz == Float.class) {
            obj = new Float[] {};
        } else if (clazz == Double.class) {
            obj = new Double[] {};
        } else if (clazz == Boolean.class) {
            obj = new Boolean[] {};
        } else if (clazz == Character.class) {
            obj = new Character[] {};
        } else if (clazz == String.class) {
            obj = new String[] {};
        }
        return (T[]) obj;
    }

    private <T> Collection<T> convert(String commaSeparatedValues, Class<T> clazz, int processingHints) {
        ArrayList<T> collection = new ArrayList<>();
        for (String value : commaSeparatedValues.split(",")) {
            try {
                T type1 = getValue(value, clazz, processingHints);
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
