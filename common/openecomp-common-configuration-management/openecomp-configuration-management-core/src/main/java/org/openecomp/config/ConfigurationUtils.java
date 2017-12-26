package org.openecomp.config;

import com.virtlink.commons.configuration2.jackson.JsonConfiguration;
import net.sf.corn.cps.CPScanner;
import net.sf.corn.cps.ResourceFilter;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.config.impl.AgglomerateConfiguration;
import org.openecomp.config.impl.ConfigurationDataSource;
import org.openecomp.config.impl.ConfigurationRepository;
import org.openecomp.config.impl.YamlConfiguration;
import org.openecomp.config.type.ConfigurationMode;
import org.openecomp.config.type.ConfigurationType;

import javax.sql.DataSource;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TransferQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openecomp.config.api.Hint.EXTERNAL_LOOKUP;
import static org.openecomp.config.api.Hint.LATEST_LOOKUP;
import static org.openecomp.config.api.Hint.NODE_SPECIFIC;

/**
 * The type Configuration utils.
 */
public class ConfigurationUtils {

    private ConfigurationUtils() {
    }


    /**
     * Gets thread factory.
     *
     * @return the thread factory
     */
    public static ThreadFactory getThreadFactory() {
        return (r1) -> {
            Thread thread = Executors.privilegedThreadFactory().newThread(r1);
            thread.setDaemon(true);
            return thread;
        };
    }

    /**
     * Gets all files.
     *
     * @param file          the file
     * @param recursive     the recursive
     * @param onlyDirectory the only directory
     * @return the all files
     */
    public static Collection<File> getAllFiles(File file, boolean recursive, boolean onlyDirectory) {
        ArrayList<File> collection = new ArrayList<>();
        if (file.isDirectory() && file.exists()) {
            File[] files = file.listFiles();
            for (File innerFile : files) {
                if (innerFile.isFile() && !onlyDirectory) {
                    collection.add(innerFile);
                } else if (innerFile.isDirectory()) {
                    collection.add(innerFile);
                    if (recursive) {
                        collection.addAll(getAllFiles(innerFile, recursive, onlyDirectory));
                    }
                }
            }
        }
        return collection;
    }

    /**
     * Gets comma saperated list.
     *
     * @param list the list
     * @return the comma saperated list
     */
    public static String getCommaSaperatedList(List list) {
        return ((Stream<String>) list.stream().map(o -> o.toString())).collect(Collectors.joining(","));
    }

    /**
     * Gets comma saperated list.
     *
     * @param list the list
     * @return the comma saperated list
     */
    public static String getCommaSaperatedList(String[] list) {
        return getCommaSaperatedList(list == null ? Arrays.asList() : Arrays.asList(list));
    }

    /**
     * Gets config type.
     *
     * @param url the url
     * @return the config type
     */
    public static ConfigurationType getConfigType(URL url) {
        return Enum.valueOf(ConfigurationType.class,
                url.getFile().substring(url.getFile().lastIndexOf('.') + 1).toUpperCase());
    }

    /**
     * Gets config type.
     *
     * @param file the file
     * @return the config type
     */
    public static ConfigurationType getConfigType(File file) {
        return Enum.valueOf(ConfigurationType.class,
                file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('.') + 1)
                        .toUpperCase());
    }

    /**
     * Is config boolean.
     *
     * @param url the url
     * @return the boolean
     */
    public static boolean isConfig(URL url) {
        return isConfig(url.getFile());
    }

    /**
     * Is config boolean.
     *
     * @param file the file
     * @return the boolean
     */
    public static boolean isConfig(File file) {
        return file != null && file.exists() && isConfig(file.getName());
    }

    /**
     * Is config boolean.
     *
     * @param file the file
     * @return the boolean
     */
    public static boolean isConfig(String file) {
        file = file.toUpperCase().substring(file.lastIndexOf('!') + 1);
        file = file.substring(file.lastIndexOf('/') + 1);
        return file.matches(
                "CONFIG(-\\w*){0,1}(-" + "(" + ConfigurationMode.OVERRIDE + "|" + ConfigurationMode.MERGE
                        + "|" + ConfigurationMode.UNION + ")){0,1}" + "\\.("
                        + ConfigurationType.PROPERTIES.name() + "|" + ConfigurationType.XML.name() + "|"
                        + ConfigurationType.JSON.name() + "|" + ConfigurationType.YAML.name() + ")$")
                || file.matches("CONFIG(.)*\\.(" + ConfigurationType.PROPERTIES.name() + "|"
                + ConfigurationType.XML.name() + "|" + ConfigurationType.JSON.name() + "|"
                + ConfigurationType.YAML.name() + ")$");
    }

    /**
     * Gets namespace.
     *
     * @param url the url
     * @return the namespace
     */
    public static String getNamespace(URL url) {
        String namespace = getNamespace(getConfiguration(url));
        if (namespace != null) {
            return namespace.toUpperCase();
        }
        return getNamespace(url.getFile().toUpperCase());
    }

    /**
     * Gets namespace.
     *
     * @param file the file
     * @return the namespace
     */
    public static String getNamespace(File file) {
        String namespace = getNamespace(getConfiguration(file));
        if (namespace != null) {
            return namespace.toUpperCase();
        }
        return getNamespace(file.getName().toUpperCase());
    }

    private static String getNamespace(Configuration config) {
        return config.getString(Constants.NAMESPACE_KEY) == null ? null
                : config.getString(Constants.NAMESPACE_KEY).toUpperCase();
    }

    /**
     * Gets namespace.
     *
     * @param file the file
     * @return the namespace
     */
    public static String getNamespace(String file) {
        file = file.toUpperCase().substring(file.lastIndexOf('!') + 1);
        file = file.substring(file.lastIndexOf('/') + 1);
        Pattern pattern = Pattern.compile(
                "CONFIG(-\\w*){0,1}(-" + "(" + ConfigurationMode.OVERRIDE + "|" + ConfigurationMode.MERGE
                        + "|" + ConfigurationMode.UNION + ")){0,1}" + "\\.("
                        + ConfigurationType.PROPERTIES.name() + "|" + ConfigurationType.XML.name() + "|"
                        + ConfigurationType.JSON.name() + "|" + ConfigurationType.YAML.name() + ")$");
        Matcher matcher = pattern.matcher(file);
        boolean b1 = matcher.matches();
        if (b1) {
            if (matcher.group(1) != null) {
                String moduleName = matcher.group(1).substring(1);
                return moduleName.equalsIgnoreCase(ConfigurationMode.OVERRIDE.name())
                        || moduleName.equalsIgnoreCase(ConfigurationMode.UNION.name())
                        || moduleName.equalsIgnoreCase(ConfigurationMode.MERGE.name())
                        ? Constants.DEFAULT_NAMESPACE : moduleName;
            } else {
                return Constants.DEFAULT_NAMESPACE;
            }
        } else if (isConfig(file)) {
            return Constants.DEFAULT_NAMESPACE;
        }

        return null;
    }

    /**
     * Gets merge strategy.
     *
     * @param url the url
     * @return the merge strategy
     */
    public static ConfigurationMode getMergeStrategy(URL url) {
        String configMode = getMergeStrategy(getConfiguration(url));
        if (configMode != null) {
            try {
                return Enum.valueOf(ConfigurationMode.class, configMode);
            } catch (Exception exception) {
                //do nothing
            }
        }
        return getMergeStrategy(url.getFile().toUpperCase());
    }

    private static String getMergeStrategy(Configuration config) {
        return config.getString(Constants.MODE_KEY) == null ? null
                : config.getString(Constants.MODE_KEY).toUpperCase();
    }

    /**
     * Gets merge strategy.
     *
     * @param file the file
     * @return the merge strategy
     */
    public static ConfigurationMode getMergeStrategy(File file) {
        String configMode = getMergeStrategy(getConfiguration(file));
        if (configMode != null) {
            try {
                return Enum.valueOf(ConfigurationMode.class, configMode);
            } catch (Exception exception) {
                //do nothing
            }
        }
        return getMergeStrategy(file.getName().toUpperCase());
    }

    /**
     * Gets merge strategy.
     *
     * @param file the file
     * @return the merge strategy
     */
    public static ConfigurationMode getMergeStrategy(String file) {
        file = file.toUpperCase().substring(file.lastIndexOf('!') + 1);
        file = file.substring(file.lastIndexOf('/') + 1);
        Pattern pattern = Pattern.compile(
                "CONFIG(-\\w*){0,1}(-" + "(" + ConfigurationMode.OVERRIDE + "|" + ConfigurationMode.MERGE
                        + "|" + ConfigurationMode.UNION + ")){0,1}" + "\\.("
                        + ConfigurationType.PROPERTIES.name() + "|" + ConfigurationType.XML.name() + "|"
                        + ConfigurationType.JSON.name() + "|" + ConfigurationType.YAML.name() + ")$");
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
                    //do nothing
                }
            }
        }

        return null;
    }

    /**
     * Gets configuration.
     *
     * @param url the url
     * @return the configuration
     */
    public static FileBasedConfiguration getConfiguration(URL url) {
        FileBasedConfiguration builder = null;
        try {
            switch (ConfigurationUtils.getConfigType(url)) {
                case PROPERTIES:
                    builder = new Configurations().fileBased(PropertiesConfiguration.class, url);
                    break;
                case XML:
                    builder = new Configurations().fileBased(XMLConfiguration.class, url);
                    break;
                case JSON:
                    builder = new Configurations().fileBased(JsonConfiguration.class, url);
                    break;
                case YAML:
                    builder = new Configurations().fileBased(YamlConfiguration.class, url);
                    break;
                default:
            }
        } catch (ConfigurationException exception) {
            exception.printStackTrace();
        }
        return builder;
    }

    /**
     * Gets configuration.
     *
     * @param url the url
     * @return the configuration
     */
    public static FileBasedConfiguration getConfiguration(File url) {
        FileBasedConfiguration builder = null;
        try {
            switch (ConfigurationUtils.getConfigType(url)) {
                case PROPERTIES:
                    builder = new Configurations().fileBased(PropertiesConfiguration.class, url);
                    break;
                case XML:
                    builder = new Configurations().fileBased(XMLConfiguration.class, url);
                    break;
                case JSON:
                    builder = new Configurations().fileBased(JsonConfiguration.class, url);
                    break;
                case YAML:
                    builder = new Configurations().fileBased(YamlConfiguration.class, url);
                    break;
                default:
            }
        } catch (ConfigurationException exception) {
            exception.printStackTrace();
        }
        return builder;
    }

    /**
     * Gets collection generic type.
     *
     * @param field the field
     * @return the collection generic type
     */
    public static Class getCollectionGenericType(Field field) {
        Type type = field.getGenericType();

        if (type instanceof ParameterizedType) {

            ParameterizedType paramType = (ParameterizedType) type;
            Type[] arr = paramType.getActualTypeArguments();

            for (Type tp : arr) {
                Class<?> clzz = (Class<?>) tp;
                if (isWrapperClass(clzz)) {
                    return clzz;
                } else {
                    throw new RuntimeException("Collection of type " + clzz.getName() + " not supported.");
                }
            }
        }
        return String[].class;
    }

    /**
     * Gets array class.
     *
     * @param clazz the clazz
     * @return the array class
     */
    public static Class getArrayClass(Class clazz) {
        switch (clazz.getName()) {
            case "java.lang.Byte":
                return Byte[].class;
            case "java.lang.Short":
                return Short[].class;
            case "java.lang.Integer":
                return Integer[].class;
            case "java.lang.Long":
                return Long[].class;
            case "java.lang.Float":
                return Float[].class;
            case "java.lang.Double":
                return Double[].class;
            case "java.lang.Boolean":
                return Boolean[].class;
            case "java.lang.Character":
                return Character[].class;
            case "java.lang.String":
                return String[].class;
            default:
        }
        return null;
    }

    /**
     * Gets all class path resources.
     *
     * @return the all class path resources
     */
    public static List<URL> getAllClassPathResources() {
        return CPScanner.scanResources(new ResourceFilter());
    }

    /**
     * Execute ddlsql boolean.
     *
     * @param sql the sql
     * @return the boolean
     * @throws Exception the exception
     */
    public static boolean executeDdlSql(String sql) throws Exception {
        DataSource datasource = ConfigurationDataSource.lookup();
        if (datasource == null) {
            System.err.println("DB configuration not found. Configuration management will be using "
                    + "in-memory persistence.");
            return false;
        }
        try (Connection con = datasource.getConnection(); Statement stmt = con.createStatement()) {
            stmt.executeQuery(sql);
        } catch (Exception exception) {
            System.err.println("Datasource initialization error. Configuration management will be using in-memory persistence.");
            return false;
        }
        return true;
    }

    /**
     * Gets configuration builder.
     *
     * @param url the url
     * @return the configuration builder
     */
    public static BasicConfigurationBuilder<FileBasedConfiguration> getConfigurationBuilder(URL url) {
        ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> builder = null;
        switch (ConfigurationUtils.getConfigType(url)) {
            case PROPERTIES:
                builder = new ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration>(
                        PropertiesConfiguration.class);
                break;
            case XML:
                builder = new ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration>(
                        XMLConfiguration.class);
                break;
            case JSON:
                builder = new ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration>(
                        JsonConfiguration.class);
                break;
            case YAML:
                builder = new ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration>(
                        YamlConfiguration.class);
                break;
            default:
        }
        builder.configure(new Parameters().fileBased().setURL(url)
                .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        return builder;
    }

    /**
     * Gets configuration builder.
     *
     * @param file     the file
     * @param autoSave the auto save
     * @return the configuration builder
     */
    public static BasicConfigurationBuilder<FileBasedConfiguration> getConfigurationBuilder(File file,
                                                                                            boolean autoSave) {
        ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> builder = null;
        switch (ConfigurationUtils.getConfigType(file)) {
            case PROPERTIES:
                builder = new ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration>(
                        PropertiesConfiguration.class);
                break;
            case XML:
                builder = new ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration>(
                        XMLConfiguration.class);
                break;
            case JSON:
                builder = new ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration>(
                        JsonConfiguration.class);
                break;
            case YAML:
                builder = new ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration>(
                        YamlConfiguration.class);
                break;
            default:
        }
        builder.configure(new Parameters().fileBased().setFile(file)
                .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        builder.setAutoSave(autoSave);
        return builder;
    }


    /**
     * Execute select sql collection.
     *
     * @param sql    the sql
     * @param params the params
     * @return the collection
     * @throws Exception the exception
     */
    public static Collection<String> executeSelectSql(String sql, String[] params) throws Exception {
        Collection<String> coll = new ArrayList<>();
        DataSource datasource = ConfigurationDataSource.lookup();
        try (Connection con = datasource.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setString(i + 1, params[i]);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    coll.add(rs.getString(1));
                }
            }

        } catch (Exception exception) {
            //exception.printStackTrace();
            return null;
        }

        return coll;
    }

    /**
     * Execute insert sql boolean.
     *
     * @param sql    the sql
     * @param params the params
     * @return the boolean
     * @throws Exception the exception
     */
    public static boolean executeInsertSql(String sql, Object[] params) throws Exception {
        Collection<String> coll = new ArrayList<>();
        DataSource datasource = ConfigurationDataSource.lookup();
        try (Connection con = datasource.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            if (params != null) {
                int counter = 0;
                for (Object obj : params) {
                    if (obj == null) {
                        obj = "";
                    }
                    switch (obj.getClass().getName()) {
                        case "java.lang.String":
                            stmt.setString(++counter, obj.toString());
                            break;
                        case "java.lang.Integer":
                            stmt.setInt(++counter, ((Integer) obj).intValue());
                            break;
                        case "java.lang.Long":
                            stmt.setLong(++counter, ((Long) obj).longValue());
                            break;
                        default:
                            stmt.setString(++counter, obj.toString());
                            break;
                    }
                }
            }
            stmt.executeUpdate();
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    /**
     * Read t.
     *
     * @param <T>       the type parameter
     * @param config    the config
     * @param clazz     the clazz
     * @param keyPrefix the key prefix
     * @return the t
     * @throws Exception the exception
     */
    public static <T> T read(Configuration config, Class<T> clazz, String keyPrefix)
            throws Exception {
        org.openecomp.config.api.Config confAnnot =
                clazz.getAnnotation(org.openecomp.config.api.Config.class);
        if (confAnnot != null) {
            keyPrefix += (confAnnot.key() + ".");
        }
        T objToReturn = clazz.newInstance();
        for (Field field : clazz.getDeclaredFields()) {
            org.openecomp.config.api.Config fieldConfAnnot =
                    field.getAnnotation(org.openecomp.config.api.Config.class);
            if (fieldConfAnnot != null) {
                field.setAccessible(true);
                field.set(objToReturn, config.getProperty(keyPrefix + fieldConfAnnot.key()));
            } else if (field.getType().getAnnotation(org.openecomp.config.api.Config.class) != null) {
                field.set(objToReturn, read(config, field.getType(), keyPrefix));
            }
        }
        return objToReturn;
    }

    /**
     * Gets db configuration builder.
     *
     * @param configName the config name
     * @return the db configuration builder
     * @throws Exception the exception
     */
    public static BasicConfigurationBuilder<AgglomerateConfiguration> getDbConfigurationBuilder(
            String configName) throws Exception {
        Configuration dbConfig = ConfigurationRepository.lookup()
                .getConfigurationFor(Constants.DEFAULT_TENANT, Constants.DB_NAMESPACE);
        BasicConfigurationBuilder<AgglomerateConfiguration> builder =
                new BasicConfigurationBuilder<AgglomerateConfiguration>(AgglomerateConfiguration.class);
        builder.configure(
                new Parameters().database()
                        .setDataSource(ConfigurationDataSource.lookup())
                        .setTable(dbConfig.getString("config.Table"))
                        .setKeyColumn(dbConfig.getString("configKey"))
                        .setValueColumn(dbConfig.getString("configValue"))
                        .setConfigurationNameColumn(dbConfig.getString("configNameColumn"))
                        .setConfigurationName(configName)
                        .setAutoCommit(true)
        );
        return builder;
    }

    /**
     * Gets property.
     *
     * @param config          the config
     * @param key             the key
     * @param processingHints the processing hints
     * @return the property
     */
    public static Object getProperty(Configuration config, String key, int processingHints) {
        if (!isDirectLookup(processingHints)) {
            if (config instanceof AgglomerateConfiguration) {
                return ((AgglomerateConfiguration) config).getPropertyValue(key);
            } else if (config instanceof CompositeConfiguration) {
                CompositeConfiguration conf = (CompositeConfiguration) config;
                for (int i = 0; i < conf.getNumberOfConfigurations(); i++) {
                    if (conf.getConfiguration(i) instanceof AgglomerateConfiguration) {
                        return ((AgglomerateConfiguration) conf.getConfiguration(i)).getPropertyValue(key);
                    } else if (isNodeSpecific(processingHints)) {
                        Object obj = conf.getConfiguration(i).getProperty(key);
                        if (obj != null) {
                            return obj;
                        }
                    }
                }
            }
        }
        return config.getProperty(key);
    }

    /**
     * Gets primitive array.
     *
     * @param collection the collection
     * @param clazz      the clazz
     * @return the primitive array
     */
    public static Object getPrimitiveArray(Collection collection, Class clazz) {

        if (clazz == int.class) {
            int[] array = new int[collection.size()];
            Object[] objArray = collection.toArray();
            for (int i = 0; i < collection.size(); i++) {
                array[i] = (int) objArray[i];
            }
            return array;
        }
        if (clazz == byte.class) {
            byte[] array = new byte[collection.size()];
            Object[] objArray = collection.toArray();
            for (int i = 0; i < collection.size(); i++) {
                array[i] = (byte) objArray[i];
            }
            return array;
        }
        if (clazz == short.class) {
            short[] array = new short[collection.size()];
            Object[] objArray = collection.toArray();
            for (int i = 0; i < collection.size(); i++) {
                array[i] = (short) objArray[i];
            }
            return array;
        }
        if (clazz == long.class) {
            long[] array = new long[collection.size()];
            Object[] objArray = collection.toArray();
            for (int i = 0; i < collection.size(); i++) {
                array[i] = (long) objArray[i];
            }
            return array;
        }
        if (clazz == float.class) {
            float[] array = new float[collection.size()];
            Object[] objArray = collection.toArray();
            for (int i = 0; i < collection.size(); i++) {
                array[i] = (float) objArray[i];
            }
            return array;
        }
        if (clazz == double.class) {
            double[] array = new double[collection.size()];
            Object[] objArray = collection.toArray();
            for (int i = 0; i < collection.size(); i++) {
                array[i] = (double) objArray[i];
            }
            return array;
        }
        if (clazz == boolean.class) {
            boolean[] array = new boolean[collection.size()];
            Object[] objArray = collection.toArray();
            for (int i = 0; i < collection.size(); i++) {
                array[i] = (boolean) objArray[i];
            }
            return array;
        }
        Object obj = null;
        return obj;
    }

    /**
     * Is wrapper class boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    public static boolean isWrapperClass(Class clazz) {
        return clazz == String.class || clazz == Boolean.class || clazz == Character.class
                || Number.class.isAssignableFrom(clazz);
    }

    /**
     * Gets collection string.
     *
     * @param input the input
     * @return the collection string
     */
    public static String getCollectionString(String input) {
        Pattern pattern = Pattern.compile("^\\[(.*)\\]$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) {
            input = matcher.group(1);
        }
        return input;
    }

    /**
     * Is collection boolean.
     *
     * @param input the input
     * @return the boolean
     */
    public static boolean isCollection(String input) {
        Pattern pattern = Pattern.compile("^\\[(.*)\\]$");
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    /**
     * Process variables if present string.
     *
     * @param tenant    the tenant
     * @param namespace the namespace
     * @param data      the data
     * @return the string
     */
    public static String processVariablesIfPresent(String tenant, String namespace, String data) {
        Pattern pattern = Pattern.compile("^.*\\$\\{(.*)\\}.*");
        Matcher matcher = pattern.matcher(data);
        if (matcher.matches()) {
            String key = matcher.group(1);
            if (key.toUpperCase().startsWith("ENV:")) {
                String envValue = System.getenv(key.substring(4));
                return processVariablesIfPresent(tenant, namespace, data.replaceAll("\\$\\{" + key + "\\}",
                        envValue == null ? "" : envValue.replace("\\", "\\\\")));
            } else if (key.toUpperCase().startsWith("SYS:")) {
                String sysValue = System.getProperty(key.substring(4));
                return processVariablesIfPresent(tenant, namespace, data.replaceAll("\\$\\{" + key + "\\}",
                        sysValue == null ? "" : sysValue.replace("\\", "\\\\")));
            } else {
                String propertyValue = ConfigurationUtils.getCollectionString(
                        ConfigurationManager.lookup().getAsStringValues(tenant, namespace, key).toString());
                return processVariablesIfPresent(tenant, namespace, data.replaceAll("\\$\\{" + key + "\\}",
                        propertyValue == null ? "" : propertyValue.replace("\\", "\\\\")));
            }
        } else {
            return data;
        }
    }

    /**
     * Gets file contents.
     *
     * @param path the path
     * @return the file contents
     */
    public static String getFileContents(String path) {
        try {
            if (path != null) {
                return IOUtils.toString(new URL(path));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * Gets file contents.
     *
     * @param path the path
     * @return the file contents
     */
    public static String getFileContents(Path path) {
        try {
            if (path != null) {
                return new String(Files.readAllBytes(path));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * Gets concrete collection.
     *
     * @param clazz the clazz
     * @return the concrete collection
     */
    public static Collection getConcreteCollection(Class clazz) {
        Collection collection = null;

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
        }

        return collection;
    }

    /**
     * Gets default for.
     *
     * @param clazz the clazz
     * @return the default for
     */
    public static Object getDefaultFor(Class clazz) {
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
        return new Character((char) 0);
    }

    /**
     * Gets compatible collection for abstract def.
     *
     * @param clazz the clazz
     * @return the compatible collection for abstract def
     */
    public static Collection getCompatibleCollectionForAbstractDef(Class clazz) {
        if (BlockingQueue.class.isAssignableFrom(clazz)) {
            return getConcreteCollection(BlockingQueue.class);
        }
        if (TransferQueue.class.isAssignableFrom(clazz)) {
            return getConcreteCollection(TransferQueue.class);
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
        return null;
    }

    /**
     * Gets configuration repository key.
     *
     * @param array the array
     * @return the configuration repository key
     */
    public static String getConfigurationRepositoryKey(String[] array) {
        Stack<String> stack = new Stack<>();
        stack.push(Constants.DEFAULT_TENANT);
        for (String element : array) {
            stack.push(element);
        }
        String toReturn = stack.pop();
        return stack.pop() + Constants.KEY_ELEMENTS_DELEMETER + toReturn;
    }

    /**
     * Gets configuration repository key.
     *
     * @param file the file
     * @return the configuration repository key
     */
    public static String getConfigurationRepositoryKey(File file) {
        return getConfigurationRepositoryKey(
                ConfigurationUtils.getNamespace(file).split(Constants.TENANT_NAMESPACE_SAPERATOR));
    }

    /**
     * Gets configuration repository key.
     *
     * @param url the url
     * @return the configuration repository key
     */
    public static String getConfigurationRepositoryKey(URL url) {
        return getConfigurationRepositoryKey(
                ConfigurationUtils.getNamespace(url).split(Constants.TENANT_NAMESPACE_SAPERATOR));
    }

    /**
     * To map linked hash map.
     *
     * @param config the config
     * @return the linked hash map
     */
    public static LinkedHashMap toMap(Configuration config) {
        Iterator<String> iterator = config.getKeys();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (!(key.equals(Constants.MODE_KEY) || key.equals(Constants.NAMESPACE_KEY)
                    || key.equals(Constants.LOAD_ORDER_KEY))) {
                map.put(key, config.getProperty(key).toString());
            }
        }

        return map;
    }

    /**
     * Diff map.
     *
     * @param orig   the orig
     * @param latest the latest
     * @return the map
     */
    public static Map diff(LinkedHashMap orig, LinkedHashMap latest) {
        orig = new LinkedHashMap<>(orig);
        latest = new LinkedHashMap<>(latest);
        List<String> set = new ArrayList(orig.keySet());
        for (String key : set) {
            if (latest.remove(key, orig.get(key))) {
                orig.remove(key);
            }
        }
        Set<String> keys = latest.keySet();
        for (String key : keys) {
            orig.remove(key);
        }
        set = new ArrayList(orig.keySet());
        for (String key : set) {
            latest.put(key, "");
        }
        return new HashMap<>(latest);
    }

    /**
     * Is array boolean.
     *
     * @param tenant          the tenant
     * @param namespace       the namespace
     * @param key             the key
     * @param processingHints the processing hints
     * @return the boolean
     * @throws Exception the exception
     */
    public static boolean isArray(String tenant, String namespace, String key, int processingHints)
            throws Exception {
        Object obj = ConfigurationUtils
                .getProperty(ConfigurationRepository.lookup().getConfigurationFor(tenant, namespace), key,
                        processingHints);
        return (obj == null) ? false : ConfigurationUtils.isCollection(obj.toString());
    }

    /**
     * Is direct lookup boolean.
     *
     * @param hints the hints
     * @return the boolean
     */
    public static boolean isDirectLookup(int hints) {
        return (hints & LATEST_LOOKUP.value()) == LATEST_LOOKUP.value();
    }

    /**
     * Is external lookup boolean.
     *
     * @param hints the hints
     * @return the boolean
     */
    public static boolean isExternalLookup(int hints) {
        return (hints & EXTERNAL_LOOKUP.value()) == EXTERNAL_LOOKUP.value();
    }

    /**
     * Is node specific boolean.
     *
     * @param hints the hints
     * @return the boolean
     */
    public static boolean isNodeSpecific(int hints) {
        return (hints & NODE_SPECIFIC.value()) == NODE_SPECIFIC.value();
    }

    public static boolean isZeroLengthArray(Class clazz, Object obj) {
        if (clazz.isArray() && clazz.getComponentType().isPrimitive()) {
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

    /**
     * Checks if value is blank
     *
     * @param value
     * @return
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }
}
