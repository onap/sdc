package org.openecomp.config.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The interface Configuration.
 */
public interface Configuration {
  /**
   * The constant tenant.
   */
  public static ThreadLocal<String> tenant = new ThreadLocal<String>();

  /**
   * Sets tenant id.
   *
   * @param id the id
   */
  public static void setTenantId(String id) {
    if (id != null && id.trim().length() > 0) {
      tenant.set(id);
    }
  }

  /**
   * Gets as string.
   *
   * @param key the key
   * @return the as string
   */
  public default String getAsString(String key) {
    return getAsString(null, key);
  }

  /**
   * Gets as string.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as string
   */
  public default String getAsString(String namespace, String key) {
    return getAsString(tenant.get(), namespace, key);
  }

  /**
   * Gets as string.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as string
   */
  public default String getAsString(String tenantId, String namespace, String key) {
    return get(tenantId, namespace, key, String.class);
  }

  /**
   * Gets as byte value.
   *
   * @param key the key
   * @return the as byte value
   */
  public default Byte getAsByteValue(String key) {
    return getAsByteValue(null, key);
  }

  /**
   * Gets as byte value.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as byte value
   */
  public default Byte getAsByteValue(String namespace, String key) {
    return getAsByteValue(tenant.get(), namespace, key);
  }

  /**
   * Gets as byte value.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as byte value
   */
  public default Byte getAsByteValue(String tenantId, String namespace, String key) {
    return get(tenantId, namespace, key, Byte.class);
  }

  /**
   * Gets as short value.
   *
   * @param key the key
   * @return the as short value
   */
  public default Short getAsShortValue(String key) {
    return getAsShortValue(null, key);
  }

  /**
   * Gets as short value.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as short value
   */
  public default Short getAsShortValue(String namespace, String key) {
    return getAsShortValue(tenant.get(), namespace, key);
  }

  /**
   * Gets as short value.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as short value
   */
  public default Short getAsShortValue(String tenantId, String namespace, String key) {
    return get(tenantId, namespace, key, Short.class);
  }

  /**
   * Gets as integer value.
   *
   * @param key the key
   * @return the as integer value
   */
  public default Integer getAsIntegerValue(String key) {
    return getAsIntegerValue(null, key);
  }

  /**
   * Gets as integer value.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as integer value
   */
  public default Integer getAsIntegerValue(String namespace, String key) {
    return getAsIntegerValue(tenant.get(), namespace, key);
  }

  /**
   * Gets as integer value.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as integer value
   */
  public default Integer getAsIntegerValue(String tenantId, String namespace, String key) {
    return get(tenantId, namespace, key, Integer.class);
  }

  /**
   * Gets as long value.
   *
   * @param key the key
   * @return the as long value
   */
  public default Long getAsLongValue(String key) {
    return getAsLongValue(null, key);
  }

  /**
   * Gets as long value.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as long value
   */
  public default Long getAsLongValue(String namespace, String key) {
    return getAsLongValue(tenant.get(), namespace, key);
  }

  /**
   * Gets as long value.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as long value
   */
  public default Long getAsLongValue(String tenantId, String namespace, String key) {
    return get(tenantId, namespace, key, Long.class);
  }

  /**
   * Gets as float value.
   *
   * @param key the key
   * @return the as float value
   */
  public default Float getAsFloatValue(String key) {
    return getAsFloatValue(null, key);
  }

  /**
   * Gets as float value.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as float value
   */
  public default Float getAsFloatValue(String namespace, String key) {
    return getAsFloatValue(tenant.get(), namespace, key);
  }

  /**
   * Gets as float value.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as float value
   */
  public default Float getAsFloatValue(String tenantId, String namespace, String key) {
    return get(tenantId, namespace, key, Float.class);
  }

  /**
   * Gets as double value.
   *
   * @param key the key
   * @return the as double value
   */
  public default Double getAsDoubleValue(String key) {
    return getAsDoubleValue(null, key);
  }

  /**
   * Gets as double value.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as double value
   */
  public default Double getAsDoubleValue(String namespace, String key) {
    return getAsDoubleValue(tenant.get(), namespace, key);
  }

  /**
   * Gets as double value.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as double value
   */
  public default Double getAsDoubleValue(String tenantId, String namespace, String key) {
    return get(tenantId, namespace, key, Double.class);
  }

  /**
   * Gets as boolean value.
   *
   * @param key the key
   * @return the as boolean value
   */
  public default Boolean getAsBooleanValue(String key) {
    return getAsBooleanValue(null, key);
  }

  /**
   * Gets as boolean value.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as boolean value
   */
  public default Boolean getAsBooleanValue(String namespace, String key) {
    return getAsBooleanValue(tenant.get(), namespace, key);
  }

  /**
   * Gets as boolean value.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as boolean value
   */
  public default Boolean getAsBooleanValue(String tenantId, String namespace, String key) {
    return get(tenantId, namespace, key, Boolean.class);
  }

  /**
   * Gets as char value.
   *
   * @param key the key
   * @return the as char value
   */
  public default Character getAsCharValue(String key) {
    return getAsCharValue(null, key);
  }

  /**
   * Gets as char value.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as char value
   */
  public default Character getAsCharValue(String namespace, String key) {
    return getAsCharValue(tenant.get(), namespace, key);
  }

  /**
   * Gets as char value.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as char value
   */
  public default Character getAsCharValue(String tenantId, String namespace, String key) {
    return get(tenantId, namespace, key, Character.class);
  }

  /**
   * Populate configuration t.
   *
   * @param <T>   the type parameter
   * @param clazz the clazz
   * @return the t
   */
  public default <T> T populateConfiguration(Class<T> clazz) {
    return populateConfiguration(null, clazz);
  }

  /**
   * Populate configuration t.
   *
   * @param <T>       the type parameter
   * @param namespace the namespace
   * @param clazz     the clazz
   * @return the t
   */
  public default <T> T populateConfiguration(String namespace, Class<T> clazz) {
    return populateConfiguration(tenant.get(), namespace, clazz);
  }

  /**
   * Populate configuration t.
   *
   * @param <T>       the type parameter
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param clazz     the clazz
   * @return the t
   */
  public default <T> T populateConfiguration(String tenantId, String namespace, Class<T> clazz) {
    return get(tenantId, namespace, null, clazz, Hint.EXTERNAL_LOOKUP);
  }

  /**
   * Gets dynamic configuration.
   *
   * @param <T>          the type parameter
   * @param key          the key
   * @param clazz        the clazz
   * @param defaultValue the default value
   * @return the dynamic configuration
   */
  public default <T> DynamicConfiguration<T> getDynamicConfiguration(String key, Class<T> clazz,
                                                                     T defaultValue) {
    return getDynamicConfiguration(null, key, clazz, defaultValue);
  }

  /**
   * Gets dynamic configuration.
   *
   * @param <T>          the type parameter
   * @param namespace    the namespace
   * @param key          the key
   * @param clazz        the clazz
   * @param defaultValue the default value
   * @return the dynamic configuration
   */
  public default <T> DynamicConfiguration<T> getDynamicConfiguration(String namespace, String key,
                                                                     Class<T> clazz,
                                                                     T defaultValue) {
    return getDynamicConfiguration(tenant.get(), namespace, key, clazz, defaultValue);
  }

  /**
   * Gets dynamic configuration.
   *
   * @param <T>          the type parameter
   * @param tenant       the tenant
   * @param namespace    the namespace
   * @param key          the key
   * @param clazz        the clazz
   * @param defaultValue the default value
   * @return the dynamic configuration
   */
  public default <T> DynamicConfiguration<T> getDynamicConfiguration(String tenant,
                                                                     String namespace, String key,
                                                                     Class<T> clazz,
                                                                     T defaultValue) {
    return DynamicConfiguration
        .getDynamicConfiguration(tenant, namespace, key, clazz, defaultValue, this);
  }

  /**
   * Gets dynamic configuration values.
   *
   * @param <T>          the type parameter
   * @param key          the key
   * @param clazz        the clazz
   * @param defaultValue the default value
   * @return the dynamic configuration values
   */
  public default <T> DynamicConfiguration<List<T>> getDynamicConfigurationValues(String key,
                                                                                 Class<T> clazz,
                                                                                 T defaultValue) {
    return getDynamicConfigurationValues(null, key, clazz, defaultValue);
  }

  /**
   * Gets dynamic configuration values.
   *
   * @param <T>          the type parameter
   * @param namespace    the namespace
   * @param key          the key
   * @param clazz        the clazz
   * @param defaultValue the default value
   * @return the dynamic configuration values
   */
  public default <T> DynamicConfiguration<List<T>> getDynamicConfigurationValues(String namespace,
                                                                                 String key,
                                                                                 Class<T> clazz,
                                                                                 T defaultValue) {
    return getDynamicConfigurationValues(tenant.get(), namespace, key, clazz, defaultValue);
  }

  /**
   * Gets dynamic configuration values.
   *
   * @param <T>          the type parameter
   * @param tenant       the tenant
   * @param namespace    the namespace
   * @param key          the key
   * @param clazz        the clazz
   * @param defaultValue the default value
   * @return the dynamic configuration values
   */
  public default <T> DynamicConfiguration<List<T>> getDynamicConfigurationValues(String tenant,
                                                                                 String namespace,
                                                                                 String key,
                                                                                 Class<T> clazz,
                                                                                 T defaultValue) {
    return DynamicConfiguration
        .getDynConfiguration(tenant, namespace, key, clazz, defaultValue, this);
  }

  /**
   * Gets as string values.
   *
   * @param key the key
   * @return the as string values
   */
  public default List<String> getAsStringValues(String key) {
    return getAsStringValues(null, key);
  }

  /**
   * Gets as string values.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as string values
   */
  public default List<String> getAsStringValues(String namespace, String key) {
    return getAsStringValues(tenant.get(), namespace, key);
  }

  /**
   * Gets as string values.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as string values
   */
  public default List<String> getAsStringValues(String tenantId, String namespace, String key) {
    String[] tempArray = get(tenantId, namespace, key, String[].class);
    return tempArray == null ? Arrays.asList() : Arrays.asList(tempArray);
  }

  /**
   * Gets as byte values.
   *
   * @param key the key
   * @return the as byte values
   */
  public default List<Byte> getAsByteValues(String key) {
    return getAsByteValues(null, key);
  }

  /**
   * Gets as byte values.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as byte values
   */
  public default List<Byte> getAsByteValues(String namespace, String key) {
    return getAsByteValues(tenant.get(), namespace, key);
  }

  /**
   * Gets as byte values.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as byte values
   */
  public default List<Byte> getAsByteValues(String tenantId, String namespace, String key) {
    Byte[] tempArray = get(tenantId, namespace, key, Byte[].class);
    return tempArray == null ? Arrays.asList() : Arrays.asList(tempArray);
  }

  /**
   * Gets as short values.
   *
   * @param key the key
   * @return the as short values
   */
  public default List<Short> getAsShortValues(String key) {
    return getAsShortValues(null, key);
  }

  /**
   * Gets as short values.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as short values
   */
  public default List<Short> getAsShortValues(String namespace, String key) {
    return getAsShortValues(tenant.get(), namespace, key);
  }

  /**
   * Gets as short values.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as short values
   */
  public default List<Short> getAsShortValues(String tenantId, String namespace, String key) {
    Short[] tempArray = get(tenantId, namespace, key, Short[].class);
    return tempArray == null ? Arrays.asList() : Arrays.asList(tempArray);
  }

  /**
   * Gets as integer values.
   *
   * @param key the key
   * @return the as integer values
   */
  public default List<Integer> getAsIntegerValues(String key) {
    return getAsIntegerValues(null, key);
  }

  /**
   * Gets as integer values.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as integer values
   */
  public default List<Integer> getAsIntegerValues(String namespace, String key) {
    return getAsIntegerValues(tenant.get(), namespace, key);
  }

  /**
   * Gets as integer values.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as integer values
   */
  public default List<Integer> getAsIntegerValues(String tenantId, String namespace, String key) {
    Integer[] tempArray = get(tenantId, namespace, key, Integer[].class);
    return tempArray == null ? Arrays.asList() : Arrays.asList(tempArray);
  }

  /**
   * Gets as double values.
   *
   * @param key the key
   * @return the as double values
   */
  public default List<Double> getAsDoubleValues(String key) {
    return getAsDoubleValues(null, key);
  }

  /**
   * Gets as double values.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as double values
   */
  public default List<Double> getAsDoubleValues(String namespace, String key) {
    return getAsDoubleValues(tenant.get(), namespace, key);
  }

  /**
   * Gets as double values.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as double values
   */
  public default List<Double> getAsDoubleValues(String tenantId, String namespace, String key) {
    Double[] tempArray = get(tenantId, namespace, key, Double[].class);
    return tempArray == null ? Arrays.asList() : Arrays.asList(tempArray);
  }

  /**
   * Gets as float values.
   *
   * @param key the key
   * @return the as float values
   */
  public default List<Float> getAsFloatValues(String key) {
    return getAsFloatValues(null, key);
  }

  /**
   * Gets as float values.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as float values
   */
  public default List<Float> getAsFloatValues(String namespace, String key) {
    return getAsFloatValues(tenant.get(), namespace, key);
  }

  /**
   * Gets as float values.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as float values
   */
  public default List<Float> getAsFloatValues(String tenantId, String namespace, String key) {
    Float[] tempArray = get(tenantId, namespace, key, Float[].class);
    return tempArray == null ? Arrays.asList() : Arrays.asList(tempArray);
  }

  /**
   * Gets as boolean values.
   *
   * @param key the key
   * @return the as boolean values
   */
  public default List<Boolean> getAsBooleanValues(String key) {
    return getAsBooleanValues(null, key);
  }

  /**
   * Gets as boolean values.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as boolean values
   */
  public default List<Boolean> getAsBooleanValues(String namespace, String key) {
    return getAsBooleanValues(tenant.get(), namespace, key);
  }

  /**
   * Gets as boolean values.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as boolean values
   */
  public default List<Boolean> getAsBooleanValues(String tenantId, String namespace, String key) {
    Boolean[] tempArray = get(tenantId, namespace, key, Boolean[].class);
    return tempArray == null ? Arrays.asList() : Arrays.asList(tempArray);
  }

  /**
   * Gets as character values.
   *
   * @param key the key
   * @return the as character values
   */
  public default List<Character> getAsCharacterValues(String key) {
    return getAsCharacterValues(null, key);
  }

  /**
   * Gets as character values.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the as character values
   */
  public default List<Character> getAsCharacterValues(String namespace, String key) {
    return getAsCharacterValues(tenant.get(), namespace, key);
  }

  /**
   * Gets as character values.
   *
   * @param tenantId  the tenant id
   * @param namespace the namespace
   * @param key       the key
   * @return the as character values
   */
  public default List<Character> getAsCharacterValues(String tenantId, String namespace,
                                                      String key) {
    Character[] tempArray = get(tenantId, namespace, key, Character[].class);
    return tempArray == null ? Arrays.asList() : Arrays.asList(tempArray);
  }

  /**
   * Get t.
   *
   * @param <T>       the type parameter
   * @param tenant    the tenant
   * @param namespace the namespace
   * @param key       the key
   * @param clazz     the clazz
   * @param hints     the hints
   * @return the t
   */
  public <T> T get(String tenant, String namespace, String key, Class<T> clazz, Hint... hints);

  /**
   * Add configuration change listener.
   *
   * @param key    the key
   * @param myself the myself
   */
  public default void addConfigurationChangeListener(String key,
                                                     ConfigurationChangeListener myself) {
    addConfigurationChangeListener(null, key, myself);
  }

  /**
   * Add configuration change listener.
   *
   * @param namespace the namespace
   * @param key       the key
   * @param myself    the myself
   */
  public default void addConfigurationChangeListener(String namespace, String key,
                                                     ConfigurationChangeListener myself) {
    addConfigurationChangeListener(tenant.get(), namespace, key, myself);
  }

  /**
   * Add configuration change listener.
   *
   * @param tenant    the tenant
   * @param namespace the namespace
   * @param key       the key
   * @param myself    the myself
   */
  public void addConfigurationChangeListener(String tenant, String namespace, String key,
                                             ConfigurationChangeListener myself);

  /**
   * Remove configuration change listener.
   *
   * @param key    the key
   * @param myself the myself
   */
  public default void removeConfigurationChangeListener(String key,
                                                        ConfigurationChangeListener myself) {
    removeConfigurationChangeListener(null, key, myself);
  }

  /**
   * Remove configuration change listener.
   *
   * @param namespace the namespace
   * @param key       the key
   * @param myself    the myself
   */
  public default void removeConfigurationChangeListener(String namespace, String key,
                                                        ConfigurationChangeListener myself) {
    removeConfigurationChangeListener(tenant.get(), namespace, key, myself);
  }

  /**
   * Remove configuration change listener.
   *
   * @param tenant    the tenant
   * @param namespace the namespace
   * @param key       the key
   * @param myself    the myself
   */
  public void removeConfigurationChangeListener(String tenant, String namespace, String key,
                                                ConfigurationChangeListener myself);

  public default <T> Map<String, T> populateMap(String key, Class<T> clazz){
    return populateMap(null, key, clazz);
  }
  public default <T> Map<String, T> populateMap(String namespace, String key, Class<T> clazz){
    return populateMap(tenant.get(), namespace, key, clazz);
  }
  public <T> Map<String, T> populateMap(String tenantId, String namespace, String key, Class<T> clazz);

  public default Map generateMap(String key){
    return generateMap(null, key);
  }
  public default Map generateMap(String namespace, String key){
    return generateMap(tenant.get(), namespace, key);
  }
  public Map generateMap(String tenantId, String namespace, String key);

}
