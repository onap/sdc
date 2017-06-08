package org.openecomp.config.impl;

import static org.openecomp.config.ConfigurationUtils.getConfigurationRepositoryKey;
import static org.openecomp.config.ConfigurationUtils.getProperty;
import static org.openecomp.config.ConfigurationUtils.isExternalLookup;
import static org.openecomp.config.ConfigurationUtils.isWrapperClass;
import static org.openecomp.config.ConfigurationUtils.isZeroLengthArray;

import static org.openecomp.config.Constants.DB_NAMESPACE;
import static org.openecomp.config.Constants.DEFAULT_NAMESPACE;
import static org.openecomp.config.Constants.DEFAULT_TENANT;
import static org.openecomp.config.Constants.KEY_ELEMENTS_DELEMETER;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.DatabaseConfiguration;
import org.openecomp.config.ConfigurationUtils;
import org.openecomp.config.Constants;
import org.openecomp.config.NonConfigResource;
import org.openecomp.config.api.Config;
import org.openecomp.config.api.ConfigurationChangeListener;
import org.openecomp.config.api.Hint;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

/**
 * The type Configuration.
 */
public class ConfigurationImpl implements org.openecomp.config.api.Configuration {

  private static ThreadLocal<String> tenant = new ThreadLocal<String>() {
    protected String initialValue() {
      return Constants.DEFAULT_TENANT;
    }

    ;
  };
  private static boolean instantiated = false;
  /**
   * The Change notifier.
   */
  ConfigurationChangeNotifier changeNotifier;

  /**
   * Instantiates a new Configuration.
   *
   * @throws Exception the exception
   */
  public ConfigurationImpl() throws Exception {
    if (instantiated || !CliConfigurationImpl.class.isAssignableFrom(this.getClass())) {
      throw new RuntimeException("Illegal access to configuration.");
    }
    Map<String, AggregateConfiguration> moduleConfigStore = new HashMap<>();
    List<URL> classpathResources = ConfigurationUtils.getAllClassPathResources();
    Predicate<URL> predicate = ConfigurationUtils::isConfig;
    for (URL url : classpathResources) {
      if (predicate.test(url)) {
        String moduleName = getConfigurationRepositoryKey(url);
        AggregateConfiguration moduleConfig = moduleConfigStore.get(moduleName);
        if (moduleConfig == null) {
          moduleConfig = new AggregateConfiguration();
          moduleConfigStore.put(moduleName, moduleConfig);
        }
        moduleConfig.addConfig(url);
      } else {
        NonConfigResource.add(url);
      }
    }
    String configLocation = System.getProperty("config.location");
    if (configLocation != null && configLocation.trim().length() > 0) {
      File root = new File(configLocation);
      Collection<File> filesystemResources = ConfigurationUtils.getAllFiles(root, true, false);
      Predicate<File> filePredicate = ConfigurationUtils::isConfig;
      for (File file : filesystemResources) {
        if (filePredicate.test(file)) {
          String moduleName = getConfigurationRepositoryKey(file);
          AggregateConfiguration moduleConfig = moduleConfigStore.get(moduleName);
          if (moduleConfig == null) {
            moduleConfig = new AggregateConfiguration();
            moduleConfigStore.put(moduleName, moduleConfig);
          }
          moduleConfig.addConfig(file);
        } else {
          NonConfigResource.add(file);
        }
      }
    }
    String tenantConfigLocation = System.getProperty("tenant.config.location");
    if (tenantConfigLocation != null && tenantConfigLocation.trim().length() > 0) {
      File root = new File(tenantConfigLocation);
      Collection<File> tenantsRoot = ConfigurationUtils.getAllFiles(root, false, true);
      Collection<File> filesystemResources = ConfigurationUtils.getAllFiles(root, true, false);
      Predicate<File> filePredicate = ConfigurationUtils::isConfig;
      for (File file : filesystemResources) {
        if (filePredicate.test(file)) {
          String moduleName = ConfigurationUtils.getNamespace(file);
          for (File tenanatFileRoot : tenantsRoot) {
            if (file.getAbsolutePath().startsWith(tenanatFileRoot.getAbsolutePath())) {
              moduleName = getConfigurationRepositoryKey(
                  (tenanatFileRoot.getName().toUpperCase() + Constants.TENANT_NAMESPACE_SAPERATOR
                      + moduleName).split(Constants.TENANT_NAMESPACE_SAPERATOR));
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
    ConfigurationRepository.lookup().initTenantsAndNamespaces();
    String nodeConfigLocation = System.getProperty("node.config.location");
    if (nodeConfigLocation != null && nodeConfigLocation.trim().length() > 0) {
      File root = new File(nodeConfigLocation);
      Collection<File> filesystemResources = ConfigurationUtils.getAllFiles(root, true, false);
      Predicate<File> filePredicate = ConfigurationUtils::isConfig;
      for (File file : filesystemResources) {
        if (filePredicate.test(file)) {
          ConfigurationRepository.lookup().populateOverrideConfigurtaion(
              getConfigurationRepositoryKey(ConfigurationUtils.getNamespace(file)
                  .split(Constants.TENANT_NAMESPACE_SAPERATOR)), file);
        }
      }
    }
    instantiated = true;
    changeNotifier = new ConfigurationChangeNotifier(moduleConfigStore);
  }

  @Override
  public void addConfigurationChangeListener(String tenant, String namespace, String key,
                                             ConfigurationChangeListener myself) {
    tenant = ConfigurationRepository.lookup().isValidTenant(tenant) ? tenant.toUpperCase()
        : Constants.DEFAULT_TENANT;
    namespace =
        ConfigurationRepository.lookup().isValidNamespace(namespace) ? namespace.toUpperCase()
            : Constants.DEFAULT_NAMESPACE;
    if (key == null || key.trim().length() == 0) {
      throw new IllegalArgumentException("Key can't be null.");
    }
    if (myself == null) {
      throw new IllegalArgumentException("ConfigurationChangeListener instance is null.");
    }
    try {
      changeNotifier.notifyChangesTowards(tenant, namespace, key, myself);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private void populateFinalConfigurationIncrementally(Map<String, AggregateConfiguration> configs)
      throws Exception {
    boolean isDbAccessible = false;
    if (configs.get(
        Constants.DEFAULT_TENANT + Constants.KEY_ELEMENTS_DELEMETER + Constants.DB_NAMESPACE)
        != null) {
      ConfigurationRepository.lookup().populateConfigurtaion(
          Constants.DEFAULT_TENANT + Constants.KEY_ELEMENTS_DELEMETER + Constants.DB_NAMESPACE,
          configs.remove(
              Constants.DEFAULT_TENANT + Constants.KEY_ELEMENTS_DELEMETER + Constants.DB_NAMESPACE)
              .getFinalConfiguration());
      isDbAccessible = ConfigurationUtils.executeDdlSql(ConfigurationRepository.lookup()
          .getConfigurationFor(Constants.DEFAULT_TENANT, Constants.DB_NAMESPACE)
          .getString("createtablecql"));
      if (isDbAccessible) {
        ConfigurationUtils.executeDdlSql(ConfigurationRepository.lookup()
            .getConfigurationFor(Constants.DEFAULT_TENANT, Constants.DB_NAMESPACE)
            .getString("createmonitoringtablecql"));
      }
    }

    Set<String> modules = configs.keySet();
    for (String module : modules) {
      if (isDbAccessible) {
        DatabaseConfiguration config =
            ConfigurationUtils.getDbConfigurationBuilder(module).getConfiguration();
        Configuration currentConfig = configs.get(module).getFinalConfiguration();
        Iterator<String> keys = currentConfig.getKeys();
        while (keys.hasNext()) {
          String currentKey = keys.next();
          if (!(Constants.MODE_KEY.equals(currentKey)
              || Constants.NAMESPACE_KEY.equals(currentKey)
              || Constants.LOAD_ORDER_KEY.equals(currentKey))) {
            if (!config.containsKey(currentKey)) {
              Object propValue = currentConfig.getProperty(currentKey);
              if (propValue instanceof Collection) {
                config.addProperty(currentKey, propValue.toString());
              } else {
                config.addProperty(currentKey, propValue);
              }
            }
          }
        }
      } else {
        ConfigurationRepository.lookup()
            .populateConfigurtaion(module, configs.get(module).getFinalConfiguration());
      }
    }
  }

  @Override
  public <T> T get(String tenant, String namespace, String key, Class<T> clazz, Hint... hints) {

    String[] tenantNamespaceArrayy = null;
    if (tenant == null && namespace != null
        && (tenantNamespaceArrayy = namespace.split(Constants.TENANT_NAMESPACE_SAPERATOR)).length
        > 1) {
      tenant = tenantNamespaceArrayy[0];
      namespace = tenantNamespaceArrayy[1];
    }

    tenant = ConfigurationRepository.lookup().isValidTenant(tenant) ? tenant.toUpperCase()
        : Constants.DEFAULT_TENANT;
    namespace =
        ConfigurationRepository.lookup().isValidNamespace(namespace) ? namespace.toUpperCase()
            : Constants.DEFAULT_NAMESPACE;
    T returnValue = null;
    returnValue = (T) getInternal(tenant, namespace, key,
        clazz.isPrimitive() ? getWrapperClass(clazz) : clazz,
        hints == null || hints.length == 0 ? new Hint[]{Hint.EXTERNAL_LOOKUP, Hint.NODE_SPECIFIC}
            : hints);
    if ((returnValue == null || isZeroLengthArray(clazz, returnValue))
        && !Constants.DEFAULT_TENANT.equals(tenant)) {
      returnValue = (T) getInternal(Constants.DEFAULT_TENANT, namespace, key,
          clazz.isPrimitive() ? getWrapperClass(clazz) : clazz,
          hints == null || hints.length == 0 ? new Hint[]{Hint.EXTERNAL_LOOKUP, Hint.NODE_SPECIFIC}
              : hints);
    }
    if ((returnValue == null || isZeroLengthArray(clazz, returnValue))
        && !Constants.DEFAULT_NAMESPACE.equals(namespace)) {
      returnValue = (T) getInternal(tenant, Constants.DEFAULT_NAMESPACE, key,
          clazz.isPrimitive() ? getWrapperClass(clazz) : clazz,
          hints == null || hints.length == 0 ? new Hint[]{Hint.EXTERNAL_LOOKUP, Hint.NODE_SPECIFIC}
              : hints);
    }
    if ((returnValue == null ||isZeroLengthArray(clazz, returnValue))
        && !Constants.DEFAULT_NAMESPACE.equals(namespace)
        && !Constants.DEFAULT_TENANT.equals(tenant)) {
      returnValue = (T) getInternal(Constants.DEFAULT_TENANT, Constants.DEFAULT_NAMESPACE, key,
          clazz.isPrimitive() ? getWrapperClass(clazz) : clazz,
          hints == null || hints.length == 0 ? new Hint[]{Hint.EXTERNAL_LOOKUP, Hint.NODE_SPECIFIC}
              : hints);
    }
    if (returnValue == null && clazz.isPrimitive()) {
      return (T) ConfigurationUtils.getDefaultFor(clazz);
    } else {
      return returnValue;
    }
  }


  /**
   * Gets internal.
   *
   * @param <T>       the type parameter
   * @param tenant    the tenant
   * @param namespace the namespace
   * @param key       the key
   * @param clazz     the clazz
   * @param hints     the hints
   * @return the internal
   */
  protected <T> T getInternal(String tenant, String namespace, String key, Class<T> clazz,
                              Hint... hints) {
    int processingHints = Hint.DEFAULT.value();
    if (hints != null) {
      for (Hint hint : hints) {
        processingHints = processingHints | hint.value();
      }
    }

    if (tenant == null || tenant.trim().length() == 0) {
      tenant = this.tenant.get();
    } else {
      tenant = tenant.toUpperCase();
    }
    if (namespace == null || namespace.trim().length() == 0) {
      namespace = Constants.DEFAULT_NAMESPACE;
    } else {
      namespace = namespace.toUpperCase();
    }
    if (key == null || key.trim().length() == 0) {
      if (!clazz.isAnnotationPresent(Config.class)) {
        throw new IllegalArgumentException("Key can't be null.");
      }
    }
    if (clazz == null) {
      throw new IllegalArgumentException("clazz is null.");
    }
    if (clazz.isPrimitive()) {
      clazz = getWrapperClass(clazz);
    }
    try {
      if (isWrapperClass(clazz) || clazz.isPrimitive()) {
        Object obj =
            getProperty(ConfigurationRepository.lookup().getConfigurationFor(tenant, namespace),
                key, processingHints);
        if (obj != null) {
          if (ConfigurationUtils.isCollection(obj.toString())) {
            obj = ConfigurationUtils.getCollectionString(obj.toString());
          }
          String value = obj.toString().split(",")[0];
          value = ConfigurationUtils.processVariablesIfPresent(tenant, namespace, value);
          return (T) getValue(value, clazz.isPrimitive() ? getWrapperClass(clazz) : clazz,
              processingHints);
        } else {
          return null;
        }
      } else if (clazz.isArray()
          && (clazz.getComponentType().isPrimitive() || isWrapperClass(clazz.getComponentType()))) {
        Object obj =
            getProperty(ConfigurationRepository.lookup().getConfigurationFor(tenant, namespace),
                key, processingHints);
        if (obj != null) {
          Class componentClass = clazz.getComponentType();
          if (clazz.getComponentType().isPrimitive()) {
            componentClass = getWrapperClass(clazz.getComponentType());
          }
          String collString = ConfigurationUtils.getCollectionString(obj.toString());
          ArrayList<String> tempCollection = new ArrayList<>();
          for (String itemValue : collString.split(",")) {
            tempCollection
                .add(ConfigurationUtils.processVariablesIfPresent(tenant, namespace, itemValue));
          }
          Collection<T> collection = convert(
              ConfigurationUtils.getCollectionString(Arrays.toString(tempCollection.toArray())),
              componentClass, processingHints);
          if (clazz.getComponentType().isPrimitive()) {
            return (T) ConfigurationUtils.getPrimitiveArray(collection, clazz.getComponentType());
          } else {
            return (T) collection
                .toArray(getZeroLengthArrayFor(getWrapperClass(clazz.getComponentType())));
          }
        } else {
          return null;
        }
      } else if (clazz.isAnnotationPresent(Config.class)) {
        return read(tenant, namespace, clazz,
            (key == null || key.trim().length() == 0) ? "" : (key + "."), hints);
      } else {
        throw new IllegalArgumentException(
            "Only pimitive classes, wrapper classes, corresponding array classes and any "
                + "class decorated with @org.openecomp.config.api.Config are allowed as argument.");
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return null;
  }


  private <T> T read(String tenant, String namespace, Class<T> clazz, String keyPrefix,
                     Hint... hints) throws Exception {
    org.openecomp.config.api.Config confAnnot =
        clazz.getAnnotation(org.openecomp.config.api.Config.class);
    if (confAnnot != null && confAnnot.key().length()>0 && !keyPrefix.endsWith(".")) {
      keyPrefix += (confAnnot.key() + ".");
    }
    Constructor<T> constructor = clazz.getDeclaredConstructor();
    constructor.setAccessible(true);
    T objToReturn = constructor.newInstance();
    for (Field field : clazz.getDeclaredFields()) {
      field.setAccessible(true);
      org.openecomp.config.api.Config fieldConfAnnot =
          field.getAnnotation(org.openecomp.config.api.Config.class);
      if (fieldConfAnnot != null) {
        if (field.getType().isPrimitive() || isWrapperClass(field.getType())
            || (field.getType().isArray() && (field.getType().getComponentType().isPrimitive()
            || isWrapperClass(field.getType().getComponentType())))
            || field.getType().getAnnotation(org.openecomp.config.api.Config.class) != null) {
          field.set(objToReturn,
              get(tenant, namespace, keyPrefix + fieldConfAnnot.key(), field.getType(), hints));
        } else if (Collection.class.isAssignableFrom(field.getType())) {
          Object obj = get(tenant, namespace, keyPrefix + fieldConfAnnot.key(),
              ConfigurationUtils.getArrayClass(ConfigurationUtils.getCollectionGenericType(field)),
              hints);
          if (obj != null) {
            List list = Arrays.asList((Object[]) obj);
            Class clazzToInstantiate = null;
            if (field.getType().isInterface()) {
              clazzToInstantiate =
                  ConfigurationUtils.getConcreteCollection(field.getType()).getClass();
            } else if (Modifier.isAbstract(field.getType().getModifiers())) {
              clazzToInstantiate =
                  ConfigurationUtils.getCompatibleCollectionForAbstractDef(field.getType())
                      .getClass();
            } else {
              clazzToInstantiate = field.getType();
            }
            Constructor construct =
                getConstructorWithArguments(clazzToInstantiate, Collection.class);
            if (construct != null) {
              construct.setAccessible(true);
              field.set(objToReturn, construct.newInstance(list));
            } else if ((construct =
                getConstructorWithArguments(clazzToInstantiate, Integer.class, Boolean.class,
                    Collection.class)) != null) {
              construct.setAccessible(true);
              field.set(objToReturn, construct.newInstance(list.size(), true, list));
            }
          }
        }else if (Map.class.isAssignableFrom(field.getType())){
          field.set(objToReturn, generateMap(tenant, namespace, keyPrefix+fieldConfAnnot.key()));
        }
      }
    }
    return objToReturn;
  }

  private Constructor getConstructorWithArguments(Class clazz, Class... classes) {
    try {
      return clazz.getDeclaredConstructor(classes);
    } catch (Exception exception) {
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
      if (obj.toString().startsWith("@") && isExternalLookup(processingHint)) {
        String contents = ConfigurationUtils
            .getFileContents(NonConfigResource.locate(obj.toString().substring(1).trim()));
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
          Double doubleVal = doubleValue.doubleValue();
          return (T) doubleVal;
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
      obj = new int[]{};
    } else if (clazz == byte.class) {
      obj = new byte[]{};
    } else if (clazz == short.class) {
      obj = new short[]{};
    } else if (clazz == long.class) {
      obj = new long[]{};
    } else if (clazz == float.class) {
      obj = new float[]{};
    } else if (clazz == double.class) {
      obj = new double[]{};
    } else if (clazz == boolean.class) {
      obj = new boolean[]{};
    } else if (clazz == char.class) {
      obj = new char[]{};
    } else if (clazz == Byte.class) {
      obj = new Byte[]{};
    } else if (clazz == Short.class) {
      obj = new Short[]{};
    } else if (clazz == Integer.class) {
      obj = new Integer[]{};
    } else if (clazz == Long.class) {
      obj = new Long[]{};
    } else if (clazz == Float.class) {
      obj = new Float[]{};
    } else if (clazz == Double.class) {
      obj = new Double[]{};
    } else if (clazz == Boolean.class) {
      obj = new Boolean[]{};
    } else if (clazz == Character.class) {
      obj = new Character[]{};
    } else if (clazz == String.class) {
      obj = new String[]{};
    }
    return (T[]) obj;
  }

  private <T> Collection<T> convert(String commaSaperatedValues, Class<T> clazz,
                                    int processingHints) {
    ArrayList<T> collection = new ArrayList<>();
    for (String value : commaSaperatedValues.split(",")) {
      try {
        T type1 = getValue(value, clazz, processingHints);
        if (type1 != null) {
          collection.add(type1);
        }
      } catch (RuntimeException re) {
        // do nothing
      }
    }
    return collection;
  }

  /**
   * Shutdown.
   */
  public void shutdown() {
    if (changeNotifier != null) {
      try {
        changeNotifier.shutdown();
        ConfigurationDataSource.lookup().close();
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  @Override
  public void removeConfigurationChangeListener(String tenant, String namespace, String key,
                                                ConfigurationChangeListener myself) {
    tenant = ConfigurationRepository.lookup().isValidTenant(tenant) ? tenant.toUpperCase()
        : Constants.DEFAULT_TENANT;
    namespace =
        ConfigurationRepository.lookup().isValidNamespace(namespace) ? namespace.toUpperCase()
            : Constants.DEFAULT_NAMESPACE;
    if (key == null || key.trim().length() == 0) {
      throw new IllegalArgumentException("Key can't be null.");
    }
    try {
      changeNotifier.stopNotificationTowards(tenant, namespace, key, myself);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  @Override
  public <T> Map<String, T> populateMap(String tenantId, String namespace, String key, Class<T> clazz){
    if (tenantId==null || tenantId.trim().length()==0){
      tenantId = this.tenant.get();
    }else{
      tenantId = tenantId.toUpperCase();
    }
    if (namespace==null || namespace.trim().length()==0){
      namespace = DEFAULT_NAMESPACE;
    }else{
      namespace = namespace.toUpperCase();
    }
    Map<String, T> map = new HashMap<>();
    Iterator<String> keys ;
    try {
      if (ConfigurationRepository.lookup().isDBAccessible()){
        keys = ConfigurationUtils.executeSelectSql(ConfigurationRepository.lookup().getConfigurationFor(DEFAULT_TENANT, DB_NAMESPACE).getString("fetchkeysql"), new String[]{tenantId+KEY_ELEMENTS_DELEMETER+namespace}).iterator();
      }else{
        keys = ConfigurationRepository.lookup().getConfigurationFor(tenantId, namespace).getKeys(key);
      }
      while(keys.hasNext()){
        String k = keys.next();
        if (k.startsWith(key+".")){
          k = k.substring(key.length()+1);
          String subkey = k.substring(0, k.indexOf("."));
          if (!map.containsKey(subkey)){
            map.put(subkey, get(tenantId, namespace, key+"."+subkey, clazz));
          }
        }
      }
    }catch (Exception e){
      e.printStackTrace();
    }
    return map;
  }

  @Override
  public Map generateMap(String tenantId, String namespace, String key){
    if (tenantId==null || tenantId.trim().length()==0){
      tenantId = this.tenant.get();
    }else{
      tenantId = tenantId.toUpperCase();
    }
    if (namespace==null || namespace.trim().length()==0){
      namespace = DEFAULT_NAMESPACE;
    }else{
      namespace = namespace.toUpperCase();
    }
    Map map, parentMap = new HashMap<>();
    Iterator<String> keys ;
    try {
      if (ConfigurationRepository.lookup().isDBAccessible()){
        keys = ConfigurationUtils.executeSelectSql(ConfigurationRepository.lookup().getConfigurationFor(DEFAULT_TENANT, DB_NAMESPACE).getString("fetchkeysql"), new String[]{tenantId+KEY_ELEMENTS_DELEMETER+namespace}).iterator();
      }else{
        if (key==null || key.trim().length()==0){
          keys = ConfigurationRepository.lookup().getConfigurationFor(tenantId, namespace).getKeys();
        }else{
          keys = ConfigurationRepository.lookup().getConfigurationFor(tenantId, namespace).getKeys(key);
        }
      }
      while(keys.hasNext()){
        map = parentMap;
        String k = keys.next();

        if (key!=null && key.trim().length()!=0 && !k.startsWith(key+".")){
          continue;
        }
        String value = getAsString(tenantId, namespace, k);
        if (key!=null && key.trim().length()!=0 && k.startsWith(key+".")){
          k = k.substring(key.trim().length()+1);
        }

        while(k.contains(".")){
          if (k.contains(".")){
            String subkey = k.substring(0, k.indexOf("."));
            k = k.substring(k.indexOf(".")+1);
            if (!map.containsKey(subkey)){
              map.put(subkey, map=new HashMap<>());
            }else{
              map = (Map)map.get(subkey);
            }
          }
        }
        map.put(k, value);
      }
    }catch (Exception e){
      e.printStackTrace();
    }
    return parentMap;
  }




}
