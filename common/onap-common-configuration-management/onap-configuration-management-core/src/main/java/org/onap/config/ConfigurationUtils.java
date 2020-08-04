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
 *
 * Modifications Copyright (c) 2019 Samsung
 *
 */

package org.onap.config;

import com.virtlink.commons.configuration2.jackson.JsonConfiguration;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.onap.config.api.Config;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.impl.YamlConfiguration;
import org.onap.config.type.ConfigurationMode;
import org.onap.config.type.ConfigurationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.onap.config.api.Hint.EXTERNAL_LOOKUP;
import static org.onap.config.api.Hint.LATEST_LOOKUP;
import static org.onap.config.api.Hint.NODE_SPECIFIC;

public class ConfigurationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationUtils.class);

    private static final String CONFIGURATION_TYPE_NOT_SUPPORTED = "Configuration type not supported:";

    private static final Map<Class<?>, Class<?>> ARRAY_CLASS_MAP;

    private static final String CONFIG_REGEX_TPL_OPT_1 = "CONFIG(-\\w*){0,1}(-(%s|%s|%s)){0,1}\\.(%s|%s|%s|%s)$";

    private static final String CONFIG_REGEX_TPL_OPT_2 = "CONFIG(.)*\\.(%s|%s|%s|%s)$";

    static {
        Map<Class<?>, Class<?>> arrayTypes = new HashMap<>();
        arrayTypes.put(Byte.class, Byte[].class);
        arrayTypes.put(Short.class, Short[].class);
        arrayTypes.put(Integer.class, Integer[].class);
        arrayTypes.put(Long.class, Long[].class);
        arrayTypes.put(Float.class, Float[].class);
        arrayTypes.put(Double.class, Double[].class);
        arrayTypes.put(Boolean.class, Boolean[].class);
        arrayTypes.put(Character.class, Character[].class);
        arrayTypes.put(String.class, String[].class);
        ARRAY_CLASS_MAP = Collections.unmodifiableMap(arrayTypes);
    }

    private ConfigurationUtils() {
        // prevent instantiation
    }

    public static Collection<File> getAllFiles(File file, boolean recursive, boolean onlyDirectory) {
        ArrayList<File> collection = new ArrayList<>();
        Path rootPath = file.toPath();
        try {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    super.preVisitDirectory(dir,attrs);
                    if (rootPath.equals(dir)) {
                        return FileVisitResult.CONTINUE;
                    }
                    collection.add(dir.toFile());
                    return recursive? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    super.visitFile(file, attrs);
                    if (!onlyDirectory) {
                        collection.add(file.toFile());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOGGER.error("Failed to walk through directories starting from: {}.", file.toString(), e);
        }
        return collection;
    }

    public static String getCommaSeparatedList(String[] list) {
        return (list == null) || (list.length == 0) ? "" : getCommaSeparatedList(Arrays.asList(list));
    }

    public static String getCommaSeparatedList(List<?> list) {

        if ((list == null) || list.isEmpty()) {
            return "";
        }

        return list.stream().filter(o -> o != null && !o.toString().trim().isEmpty())
                .map(o -> o.toString().trim()).collect(Collectors.joining(","));
    }

    public static boolean isConfig(URL url) {
        return isConfig(url.getFile());
    }

    public static boolean isConfig(String file) {
        file = file.toUpperCase().substring(file.lastIndexOf('!') + 1);
        file = file.substring(file.lastIndexOf('/') + 1);
        return file.matches(
                String.format(
                        CONFIG_REGEX_TPL_OPT_1,
                        ConfigurationMode.OVERRIDE,
                        ConfigurationMode.MERGE,
                        ConfigurationMode.UNION,
                        ConfigurationType.PROPERTIES.name(),
                        ConfigurationType.XML.name(),
                        ConfigurationType.JSON.name(),
                        ConfigurationType.YAML.name()
                )
        ) || file.matches(
                String.format(
                        CONFIG_REGEX_TPL_OPT_2,
                        ConfigurationType.PROPERTIES.name(),
                        ConfigurationType.XML.name(),
                        ConfigurationType.JSON.name(),
                        ConfigurationType.YAML.name()
                )
        );
    }

    public static boolean isConfig(File file) {
        return file != null && file.exists() && isConfig(file.getName());
    }

    private static Optional<String> readNamespace(Configuration config) {
        return ofNullable(config).flatMap(configuration -> ofNullable(configuration.getString(Constants.NAMESPACE_KEY)))
                .map(String::toUpperCase);
    }

    private static Optional<String> readMergeStrategy(Configuration config) {
        return ofNullable(config).flatMap(configuration -> ofNullable(configuration.getString(Constants.MODE_KEY)))
                .map(String::toUpperCase);
    }

    public static ConfigurationMode getMergeStrategy(File file) {
        Optional<ConfigurationMode> configurationMode =
                getConfiguration(file).flatMap(ConfigurationUtils::readMergeStrategy)
                        .flatMap(ConfigurationUtils::convertConfigurationMode);
        return configurationMode.orElseGet(() -> getMergeStrategy(file.getName().toUpperCase()));
    }

    public static ConfigurationMode getMergeStrategy(URL url) {
        Optional<ConfigurationMode> configurationMode =
                getConfiguration(url).flatMap(ConfigurationUtils::readMergeStrategy)
                        .flatMap(ConfigurationUtils::convertConfigurationMode);
        return configurationMode.orElseGet(() -> getMergeStrategy(url.getFile().toUpperCase()));
    }

    public static ConfigurationMode getMergeStrategy(String file) {

        file = file.toUpperCase().substring(file.lastIndexOf('!') + 1);
        file = file.substring(file.lastIndexOf('/') + 1);
        Pattern pattern = Pattern.compile(
                String.format(
                        CONFIG_REGEX_TPL_OPT_1,
                        ConfigurationMode.OVERRIDE,
                        ConfigurationMode.MERGE,
                        ConfigurationMode.UNION,
                        ConfigurationType.PROPERTIES.name(),
                        ConfigurationType.XML.name(),
                        ConfigurationType.JSON.name(),
                        ConfigurationType.YAML.name()
                )
        );
        Matcher matcher = pattern.matcher(file);
        boolean b1 = matcher.matches();
        if (b1) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String modeName = matcher.group(i);
                if (modeName != null) {
                    modeName = modeName.substring(1);
                }
                try {
                    return Enum.valueOf(ConfigurationMode.class, modeName);
                } catch (Exception exception) {
                    LOGGER.debug("Configuration mode for merge strategy '{}' not found", modeName, exception);
                }
            }
        }

        return null;
    }

    public static Optional<FileBasedConfiguration> getConfiguration(URL url) {

        try {

            ConfigurationType configType = ConfigurationUtils.getConfigType(url);
            switch (configType) {
                case PROPERTIES:
                    return Optional.of(new Configurations().fileBased(PropertiesConfiguration.class, url));
                case XML:
                    return Optional.of(new Configurations().fileBased(XMLConfiguration.class, url));
                case JSON:
                    return Optional.of(new Configurations().fileBased(JsonConfiguration.class, url));
                case YAML:
                    return Optional.of(new Configurations().fileBased(YamlConfiguration.class, url));
                default:
                    throw new ConfigurationException(CONFIGURATION_TYPE_NOT_SUPPORTED + configType);
            }
        } catch (ConfigurationException exception) {
            LOGGER.error("Error reading configuration at {}.", url.toString(), exception);
        }

        return Optional.empty();
    }

    public static Optional<FileBasedConfiguration> getConfiguration(File file) {

        try {
            return getConfiguration(file.getAbsoluteFile().toURI().toURL());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Malformed URL: " + file.getAbsolutePath());
        }
    }

    public static ConfigurationType getConfigType(File file) {
        Objects.requireNonNull(file, "File cannot be null");
        return Enum.valueOf(ConfigurationType.class,
                file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('.') + 1).toUpperCase());
    }

    public static ConfigurationType getConfigType(URL url) {
        Objects.requireNonNull(url, "URL cannot be null");
        return Enum.valueOf(ConfigurationType.class,
                url.getFile().substring(url.getFile().lastIndexOf('.') + 1).toUpperCase());
    }

    private static Optional<ConfigurationMode> convertConfigurationMode(String configMode) {
        ConfigurationMode configurationMode = null;
        try {
            configurationMode = ConfigurationMode.valueOf(configMode);
        } catch (Exception exception) {
            LOGGER.error("Could not find convert {} into configuration mode.", configMode, exception);
        }
        return Optional.ofNullable(configurationMode);
    }

    public static Class<?> getCollectionGenericType(Field field) {
        Type type = field.getGenericType();

        if (type instanceof ParameterizedType) {

            ParameterizedType paramType = (ParameterizedType) type;
            Type[] arr = paramType.getActualTypeArguments();
            if (arr.length > 0) {
                Class<?> clazz = (Class<?>) arr[0];
                if (isWrapperClass(clazz)) {
                    return clazz;
                } else {
                    throw new IllegalArgumentException("Collection of type " + clazz.getName() + " not supported.");
                }
            }
        }

        return String[].class;
    }

    public static boolean isWrapperClass(Class<?> clazz) {
        Predicate<Class<?>> predicateWrapper = type -> type == String.class || type == Boolean.class || type == Character.class
                || Number.class.isAssignableFrom(type);
        return isA(predicateWrapper, clazz);
    }

    public static boolean isAPrimitive(Class<?> clazz) {
        return isA(Class::isPrimitive, clazz);
    }

    /**
     * Check if clazz implementing Map iface
     */
    public static boolean isAMap(Class<?> clazz) {
        Predicate<Class<?>> predicateMap = Map.class::isAssignableFrom;
        return isA(predicateMap, clazz);
    }

    /**
     * Check if clazz implementing Collection iface
     */
    public static boolean isACollection(Class<?> clazz) {
        Predicate<Class<?>> predicateCollection = Collection.class::isAssignableFrom;
        return isA(predicateCollection, clazz);
    }

    /**
     * Check if clazz is a primitive or primitive wrapper
     */
    public static boolean isAPrimitiveOrWrapper(Class<?> clazz) {
        Predicate<Class<?>> predicatePrimitive = Class::isPrimitive;
        Predicate<Class<?>> predicateWrapper = ConfigurationUtils::isWrapperClass;
        return isA(predicatePrimitive.or(predicateWrapper), clazz);
    }

    /**
     * Check if clazz is array of primitives or array of primitives wrappers
     */
    public static boolean isAPrimitivesOrWrappersArray(Class<?> clazz) {
        Predicate<Class<?>> predicatePrimitivesOrWrappersArray =
                type -> ConfigurationUtils.isAWrappersArray(type) || ConfigurationUtils.isAPrimitivesArray(type);
        return isA(predicatePrimitivesOrWrappersArray, clazz);

    }

    /**
     * Check is clazz is array of primitives
     */
    public static boolean isAPrimitivesArray(Class<?> clazz) {
        Predicate<Class<?>> predicateArray = Class::isArray;
        Predicate<Class<?>> predicateComponentPrimitive = type -> type.getComponentType().isPrimitive();
        return isA(predicateArray.and(predicateComponentPrimitive), clazz);
    }

    /**
     * Check is clazz is array of primitives wrappers
     */
    public static boolean isAWrappersArray(Class<?> clazz) {
        Predicate<Class<?>> predicateArray = Class::isArray;
        Predicate<Class<?>> predicateComponentWrapper = type -> isWrapperClass(type.getComponentType());
        return isA(predicateArray.and(predicateComponentWrapper), clazz);
    }

    private static boolean isA(Predicate<Class<?>> predicate, Class<?> clazz) {
        return predicate.test(clazz);
    }

    public static Class<?> getArrayClass(Class<?> clazz) {
        return ARRAY_CLASS_MAP.getOrDefault(clazz, null);
    }

    public static List<URL> getAllClassPathResources() {
        try (ScanResult scanResult = new ClassGraph().scan()) {
            return scanResult.getAllResources().getURLs();
        }
    }

    public static BasicConfigurationBuilder<FileBasedConfiguration> getConfigurationBuilder(File file) {
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder;
        ConfigurationType configType = ConfigurationUtils.getConfigType(file);
        builder = getFileBasedConfigurationBuilder(configType);
        builder.configure(new Parameters().fileBased().setFile(file)
                .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        return builder;
    }

    public static BasicConfigurationBuilder<FileBasedConfiguration> getConfigurationBuilder(URL url) {
        ConfigurationType configType = ConfigurationUtils.getConfigType(url);
        ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                getFileBasedConfigurationBuilder(configType);
        builder.configure(
                new Parameters().fileBased().setURL(url).setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        return builder;
    }

    private static ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> getFileBasedConfigurationBuilder(
            ConfigurationType configType) {

        ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> builder;
        switch (configType) {
            case PROPERTIES:
                builder = new ReloadingFileBasedConfigurationBuilder<>(PropertiesConfiguration.class);
                break;
            case XML:
                builder = new ReloadingFileBasedConfigurationBuilder<>(XMLConfiguration.class);
                break;
            case JSON:
                builder = new ReloadingFileBasedConfigurationBuilder<>(JsonConfiguration.class);
                break;
            case YAML:
                builder = new ReloadingFileBasedConfigurationBuilder<>(YamlConfiguration.class);
                break;
            default:
                throw new IllegalArgumentException(CONFIGURATION_TYPE_NOT_SUPPORTED + configType);
        }
        return builder;
    }

    public static <T> T read(Configuration config, Class<T> clazz, String keyPrefix) throws Exception {
        Config confAnnotation = clazz.getAnnotation(Config.class);
        if (confAnnotation != null) {
            keyPrefix += (confAnnotation.key() + ".");
        }
        T objToReturn = clazz.newInstance();
        for (Field field : clazz.getDeclaredFields()) {
            Config fieldAnnotation = field.getAnnotation(Config.class);
            if (fieldAnnotation != null) {
                field.setAccessible(true);
                field.set(objToReturn, config.getProperty(keyPrefix + fieldAnnotation.key()));
            } else if (field.getType().getAnnotation(Config.class) != null) {
                field.set(objToReturn, read(config, field.getType(), keyPrefix));
            }
        }
        return objToReturn;
    }

    public static Object getPrimitiveArray(Collection<?> collection, Class<?> clazz) {
        switch (clazz.getName()) {
            case "int":
                return getIntsPrimitiveArray(collection);
            case "byte":
                return getBytesPrimitiveArray(collection);
            case "short":
                return getShortsPrimitiveArray(collection);
            case "long":
                return getLongsPrimitiveArray(collection);
            case "float":
                return getFloatsPrimitiveArray(collection);
            case "double":
                return getDoublesPrimitiveArray(collection);
            case "boolean":
                return getBooleansPrimitiveArray(collection);
            case "char":
                return getCharsPrimitiveArray(collection);
            default:
                return null;
        }
    }

    public static Object getWrappersArray(Collection<?> collection, Class<?> clazz) {
        Object array = null;
        if (isWrapperClass(clazz)){
            int collectionSize = collection.size();
            array = Array.newInstance(clazz, collection.size());
            Object[] objArray = collection.toArray();
            System.arraycopy(objArray, 0, array, 0, collectionSize);
        }
        return array;
    }

    public static String getCollectionString(String input) {
        Pattern pattern = Pattern.compile("^\\[(.*)\\]$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) {
            input = matcher.group(1);
        }
        return input;
    }

    public static String processVariablesIfPresent(String tenant, String namespace, String data) {
        Pattern pattern = Pattern.compile("^.*\\$\\{(.*)\\}.*");
        Matcher matcher = pattern.matcher(data);
        if (matcher.matches()) {
            final int substringStartIndex = 4;
            String key = matcher.group(1);
            String value;
            if (key.toUpperCase().startsWith("ENV:")) {
                value = System.getenv(key.substring(substringStartIndex));
            } else if (key.toUpperCase().startsWith("SYS:")) {
                value = System.getProperty(key.substring(substringStartIndex));
            } else {
                value = ConfigurationUtils.getCollectionString(
                        ConfigurationManager.lookup().getAsStringValues(tenant, namespace, key).toString());
            }
            return processVariablesIfPresent(tenant, namespace, data.replaceAll("\\$\\{" + key + "}",
                    value == null ? "" : value.replace("\\", "\\\\")));
        } else {
            return data;
        }
    }

    public static String getFileContents(String path) {
        try {
            if (path != null) {
                return IOUtils.toString(new URL(path), Charset.defaultCharset());
            }
        } catch (Exception exception) {
            LOGGER.error("Error while getting '{}' content", path, exception);
        }
        return null;
    }

    public static String getFileContents(Path path) {
        try {
            if (path != null) {
                return new String(Files.readAllBytes(path));
            }
        } catch (Exception exception) {
            LOGGER.error("Error while getting '{}' content", path.toString(), exception);
        }
        return null;
    }

    public static Object getDefaultFor(Class<?> clazz) {
        if (byte.class == clazz) {
            return new Byte("0");
        } else if (short.class == clazz) {
            return new Short("0");
        } else if (int.class == clazz) {
            return new Integer("0");
        } else if (float.class == clazz) {
            return new Float("0");
        } else if (long.class == clazz) {
            return new Long("0");
        } else if (double.class == clazz) {
            return new Double("0");
        } else if (boolean.class == clazz) {
            return Boolean.FALSE;
        }
        return (char) 0;
    }

    public static Collection getCompatibleCollectionForAbstractDef(Class<?> clazz) {
        if (TransferQueue.class.isAssignableFrom(clazz)) {
            return getConcreteCollection(TransferQueue.class);
        }
        if (BlockingQueue.class.isAssignableFrom(clazz)) {
            return getConcreteCollection(BlockingQueue.class);
        }
        if (Deque.class.isAssignableFrom(clazz)) {
            return getConcreteCollection(Deque.class);
        }
        if (Queue.class.isAssignableFrom(clazz)) {
            return getConcreteCollection(Queue.class);
        }
        if (SortedSet.class.isAssignableFrom(clazz)) {
            return getConcreteCollection(SortedSet.class);
        }
        if (Set.class.isAssignableFrom(clazz)) {
            return getConcreteCollection(Set.class);
        }
        if (List.class.isAssignableFrom(clazz)) {
            return getConcreteCollection(List.class);
        }
        throw new IllegalArgumentException("Only corresponding array classes and any are allowed as argument."
                + "assignable from TransferQueue, BlockingQueue, Deque, Queue, SortedSet, Set, List class");
    }

    public static Collection getConcreteCollection(Class<?> clazz) {
        switch (clazz.getName()) {
            case "java.util.Collection":
            case "java.util.List":
                return new ArrayList<>();
            case "java.util.Set":
                return new HashSet<>();
            case "java.util.SortedSet":
                return new TreeSet<>();
            case "java.util.Queue":
                return new ConcurrentLinkedQueue<>();
            case "java.util.Deque":
                return new ArrayDeque<>();
            case "java.util.concurrent.TransferQueue":
                return new LinkedTransferQueue<>();
            case "java.util.concurrent.BlockingQueue":
                return new LinkedBlockingQueue<>();
            default:
                throw new IllegalArgumentException("Only corresponding array classes and any are allowed as argument."
                        + "assignable from TransferQueue, BlockingQueue, Deque, Queue, SortedSet, Set, List class");
        }
    }

    public static String getConfigurationRepositoryKey(File file) {
        return getConfigurationRepositoryKey(
                ConfigurationUtils.getNamespace(file).split(Constants.TENANT_NAMESPACE_SEPARATOR));
    }

    public static String getConfigurationRepositoryKey(URL url) {
        return getConfigurationRepositoryKey(
                ConfigurationUtils.getNamespace(url).split(Constants.TENANT_NAMESPACE_SEPARATOR));
    }

    public static String getConfigurationRepositoryKey(String[] array) {
        Deque<String> stack = new ArrayDeque<>();
        stack.push(Constants.DEFAULT_TENANT);
        for (String element : array) {
            stack.push(element);
        }
        String toReturn = stack.pop();
        return stack.pop() + Constants.KEY_ELEMENTS_DELIMITER + toReturn;
    }

    public static String getNamespace(File file) {
        Optional<String> namespace =
                getConfiguration(file).flatMap(ConfigurationUtils::readNamespace).map(String::toUpperCase);
        return namespace.orElseGet(() -> getNamespace(file.getName().toUpperCase()));
    }

    public static String getNamespace(String file) {
        file = file.toUpperCase().substring(file.lastIndexOf('!') + 1);
        file = file.substring(file.lastIndexOf('/') + 1);
        Pattern pattern = Pattern.compile(
                String.format(
                        CONFIG_REGEX_TPL_OPT_1,
                        ConfigurationMode.OVERRIDE,
                        ConfigurationMode.MERGE,
                        ConfigurationMode.UNION,
                        ConfigurationType.PROPERTIES.name(),
                        ConfigurationType.XML.name(),
                        ConfigurationType.JSON.name(),
                        ConfigurationType.YAML.name()
                )
        );
        Matcher matcher = pattern.matcher(file);
        boolean b1 = matcher.matches();
        if (b1) {
            if (matcher.group(1) != null) {
                String moduleName = matcher.group(1).substring(1);
                return moduleName.equalsIgnoreCase(ConfigurationMode.OVERRIDE.name()) || moduleName.equalsIgnoreCase(
                        ConfigurationMode.UNION.name()) || moduleName.equalsIgnoreCase(ConfigurationMode.MERGE.name())
                        ? Constants.DEFAULT_NAMESPACE : moduleName;
            } else {
                return Constants.DEFAULT_NAMESPACE;
            }
        } else if (isConfig(file)) {
            return Constants.DEFAULT_NAMESPACE;
        }

        return null;
    }

    public static String getNamespace(URL url) {

        Optional<String> namespace =
                getConfiguration(url).flatMap(ConfigurationUtils::readNamespace).map(String::toUpperCase);

        return namespace.orElseGet(() -> getNamespace(url.getFile().toUpperCase()));
    }

    public static Object getProperty(Configuration config, String key, int processingHints) {
        if (!isDirectLookup(processingHints) && isNodeSpecific(processingHints) && (config instanceof CompositeConfiguration)) {
            CompositeConfiguration conf = (CompositeConfiguration) config;
            for (int i = 0; i < conf.getNumberOfConfigurations(); i++) {
                Object obj = conf.getConfiguration(i).getProperty(key);
                if (obj != null) {
                    return obj;
                }
            }
        }
        return config.getProperty(key);
    }

    public static boolean isCollection(String input) {
        Pattern pattern = Pattern.compile("^\\[(.*)\\]$");
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public static boolean isDirectLookup(int hints) {
        return (hints & LATEST_LOOKUP.value()) == LATEST_LOOKUP.value();
    }

    public static boolean isNodeSpecific(int hints) {
        return (hints & NODE_SPECIFIC.value()) == NODE_SPECIFIC.value();
    }

    public static boolean isExternalLookup(int hints) {
        return (hints & EXTERNAL_LOOKUP.value()) == EXTERNAL_LOOKUP.value();
    }

    public static boolean isZeroLengthArray(Class<?> clazz, Object obj) {
        if (isAPrimitivesArray(clazz)) {
            if (clazz.getComponentType() == int.class) {
                return ((int[]) obj).length == 0;
            } else if (clazz.getComponentType() == byte.class) {
                return ((byte[]) obj).length == 0;
            } else if (clazz.getComponentType() == short.class) {
                return ((short[]) obj).length == 0;
            } else if (clazz.getComponentType() == float.class) {
                return ((float[]) obj).length == 0;
            } else if (clazz.getComponentType() == boolean.class) {
                return ((boolean[]) obj).length == 0;
            } else if (clazz.getComponentType() == double.class) {
                return ((double[]) obj).length == 0;
            } else if (clazz.getComponentType() == long.class) {
                return ((long[]) obj).length == 0;
            } else {
                return ((Object[]) obj).length == 0;
            }
        }

        return false;
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // private methods section starts here

    private static int[] getIntsPrimitiveArray(Collection<?> collection) {
        int collectionSize = collection.size();
        int[] array = new int[collectionSize];
        Object[] objArray = collection.toArray();
        for (int i = 0; i < collectionSize; i++) {
            array[i] = (int) objArray[i];
        }
        return array;
    }

    private static byte[] getBytesPrimitiveArray(Collection<?> collection) {
        int collectionSize = collection.size();
        byte[] array = new byte[collectionSize];
        Object[] objArray = collection.toArray();
        for (int i = 0; i < collectionSize; i++) {
            array[i] = (byte) objArray[i];
        }
        return array;
    }

    private static short[] getShortsPrimitiveArray(Collection<?> collection) {
        int collectionSize = collection.size();
        short[] array = new short[collectionSize];
        Object[] objArray = collection.toArray();
        for (int i = 0; i < collectionSize; i++) {
            array[i] = (short) objArray[i];
        }
        return array;
    }

    private static long[] getLongsPrimitiveArray(Collection<?> collection) {
        int collectionSize = collection.size();
        long[] array = new long[collectionSize];
        Object[] objArray = collection.toArray();
        for (int i = 0; i < collectionSize; i++) {
            array[i] = (long) objArray[i];
        }
        return array;
    }

    private static float[] getFloatsPrimitiveArray(Collection<?> collection) {
        int collectionSize = collection.size();
        float[] array = new float[collectionSize];
        Object[] objArray = collection.toArray();
        for (int i = 0; i < collectionSize; i++) {
            array[i] = (float) objArray[i];
        }
        return array;
    }

    private static double[] getDoublesPrimitiveArray(Collection<?> collection) {
        int collectionSize = collection.size();
        double[] array = new double[collectionSize];
        Object[] objArray = collection.toArray();
        for (int i = 0; i < collectionSize; i++) {
            array[i] = (double) objArray[i];
        }
        return array;
    }

    private static boolean[] getBooleansPrimitiveArray(Collection<?> collection) {
        int collectionSize = collection.size();
        boolean[] array = new boolean[collectionSize];
        Object[] objArray = collection.toArray();
        for (int i = 0; i < collectionSize; i++) {
            array[i] = (boolean) objArray[i];
        }
        return array;
    }

    private static char[] getCharsPrimitiveArray(Collection<?> collection) {
        int collectionSize = collection.size();
        char[] array = new char[collectionSize];
        Object[] objArray = collection.toArray();
        for (int i = 0; i < collectionSize; i++) {
            array[i] = (char) objArray[i];
        }
        return array;
    }

}
