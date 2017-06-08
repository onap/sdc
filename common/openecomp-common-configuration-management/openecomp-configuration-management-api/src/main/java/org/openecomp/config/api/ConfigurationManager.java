package org.openecomp.config.api;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * The interface Configuration manager.
 */
public interface ConfigurationManager extends Configuration {

  /**
   * The constant config.
   */
  public static final Configuration config = lookup();

  /**
   * Lookup configuration.
   *
   * @return the configuration
   */
  public static Configuration lookup() {
    if (config == null) {
      ServiceLoader<ConfigurationManager> loader = ServiceLoader.load(ConfigurationManager.class);
      for (ConfigurationManager configuration : loader) {
        return (Configuration) Proxy.newProxyInstance(ConfigurationManager.class.getClassLoader(),
            new Class[]{Configuration.class}, (object, method, args) -> {
              try {
                return method.invoke(configuration, args);
              } catch (InvocationTargetException ite) {
                throw ite.getTargetException();
              }
            });
      }
    }
    return config;
  }

  /**
   * Gets configuration value.
   *
   * @param queryData the query data
   * @return the configuration value
   */
  public String getConfigurationValue(Map<String, Object> queryData);

  /**
   * Update configuration value.
   *
   * @param updateData the update data
   */
  public void updateConfigurationValue(Map<String, Object> updateData);

  /**
   * List configuration map.
   *
   * @param query the query
   * @return the map
   */
  public Map<String, String> listConfiguration(Map<String, Object> query);

  /**
   * Update configuration values boolean.
   *
   * @param tenant              the tenant
   * @param namespace           the namespace
   * @param configKeyValueStore the config key value store
   * @return the boolean
   */
  public boolean updateConfigurationValues(String tenant, String namespace,
                                           Map configKeyValueStore);

  /**
   * Gets tenants.
   *
   * @return the tenants
   */
  public Collection<String> getTenants();

  /**
   * Gets namespaces.
   *
   * @return the namespaces
   */
  public Collection<String> getNamespaces();

  /**
   * Gets keys.
   *
   * @param tenant    the tenant
   * @param namespace the namespace
   * @return the keys
   */
  public Collection<String> getKeys(String tenant, String namespace);
}
