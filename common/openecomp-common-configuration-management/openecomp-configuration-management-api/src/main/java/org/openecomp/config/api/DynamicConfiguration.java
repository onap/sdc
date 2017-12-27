package org.openecomp.config.api;

import java.util.Arrays;
import java.util.List;

/**
 * The type Dynamic configuration.
 *
 * @param <T> the type parameter
 */
public class DynamicConfiguration<T> {

  /**
   * The Tenant.
   */
  String tenant;
  /**
   * The Namespace.
   */
  String namespace;
  /**
   * The Key.
   */
  String key;
  /**
   * The Configuration.
   */
  Configuration configuration;
  /**
   * The Clazz.
   */
  Class clazz;
  /**
   * The Default value.
   */
  T defaultValue;

  /**
   * Gets dynamic configuration.
   *
   * @param <T>           the type parameter
   * @param tenant        the tenant
   * @param namespace     the namespace
   * @param key           the key
   * @param clazz         the clazz
   * @param defaultValue  the default value
   * @param configuration the configuration
   * @return the dynamic configuration
   */
  public static <T> DynamicConfiguration<T> getDynamicConfiguration(String tenant, String namespace,
                                                                    String key, Class<T> clazz,
                                                                    T defaultValue,
                                                                    Configuration configuration) {
    DynamicConfiguration<T> dynamicConfiguration = new DynamicConfiguration<>();
    dynamicConfiguration.tenant = tenant;
    dynamicConfiguration.namespace = namespace;
    dynamicConfiguration.key = key;
    dynamicConfiguration.clazz = clazz;
    dynamicConfiguration.defaultValue = defaultValue;
    dynamicConfiguration.configuration = configuration;
    return dynamicConfiguration;
  }

  /**
   * Gets dyn configuration.
   *
   * @param <K>           the type parameter
   * @param tenant        the tenant
   * @param namespace     the namespace
   * @param key           the key
   * @param clazz         the clazz
   * @param defaultValue  the default value
   * @param configuration the configuration
   * @return the dyn configuration
   */
  public static <K> DynamicConfiguration<List<K>> getDynConfiguration(String tenant,
                                                                      String namespace, String key,
                                                                      Class<K> clazz,
                                                                      K defaultValue,
                                                                      Configuration configuration) {
    if (clazz.isPrimitive()) {
      throw new RuntimeException(
          "Only Wrapper classes like Integer, Long, Double, "
              + "Boolean etc including String are supported.");
    }
    return getDynamicConfiguration(tenant, namespace, key, getArrayClass(clazz),
        Arrays.asList(defaultValue), configuration);
  }

  /**
   * Gets array class.
   *
   * @param clazz the clazz
   * @return the array class
   */
  public static Class getArrayClass(Class clazz) {
    Class arrayClass = null;
    switch (clazz.getName()) {
      case "java.lang.Byte":
        arrayClass = Byte[].class;
        break;
      case "java.lang.Short":
        arrayClass = Short[].class;
        break;
      case "java.lang.Integer":
        arrayClass = Integer[].class;
        break;
      case "java.lang.Long":
        arrayClass = Long[].class;
        break;
      case "java.lang.Float":
        arrayClass = Float[].class;
        break;
      case "java.lang.Double":
        arrayClass = Double[].class;
        break;
      case "java.lang.Boolean":
        arrayClass = Boolean[].class;
        break;
      case "java.lang.Character":
        arrayClass = Character[].class;
        break;
      case "java.lang.String":
        arrayClass = String[].class;
        break;
      default:
    }
    return arrayClass;
  }

  /**
   * Get t.
   *
   * @return the t
   */
  public T get() {
    Object toReturn = configuration
        .get(tenant, namespace, key, clazz, Hint.LATEST_LOOKUP, Hint.EXTERNAL_LOOKUP,
            Hint.NODE_SPECIFIC);
    if (toReturn != null && toReturn.getClass().isArray()) {
      toReturn = Arrays.asList((Object[]) toReturn);
    }
    return toReturn == null ? defaultValue : (T) toReturn;
  }

}
