package org.openecomp.config.impl;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.openecomp.config.ConfigurationUtils;
import org.openecomp.config.Constants;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The type Configuration repository.
 */
public final class ConfigurationRepository {

  /**
   * The Repo.
   */
  static ConfigurationRepository repo;
  private static Set<String> validCallers = Collections.unmodifiableSet(new HashSet<>(Arrays
      .asList(ConfigurationChangeNotifier.NotificationData.class.getName(),
          ConfigurationUtils.class.getName(), CliConfigurationImpl.class.getName(),
          ConfigurationChangeNotifier.class.getName(), ConfigurationDataSource.class.getName(),
          ConfigurationImpl.class.getName())));

  static {
    repo = new ConfigurationRepository();
  }

  private boolean dbAccessible = true;
  private Set<String> tenants = new HashSet<>();
  private Set<String> namespaces = new HashSet<>();
  private LinkedHashMap<String, ConfigurationHolder> store =
      new LinkedHashMap<String, ConfigurationHolder>(16, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry eldest) {
          try {
            return size() > getConfigurationFor(Constants.DEFAULT_TENANT, Constants.DB_NAMESPACE)
                .getInt("config.size.max");
          } catch (Exception exception) {
            return false;
          }
        }
      };

  private ConfigurationRepository() {
    if (repo != null) {
      throw new RuntimeException("Illegal access to configuration.");
    }
    tenants.add(Constants.DEFAULT_TENANT);
    namespaces.add(Constants.DEFAULT_NAMESPACE);
  }

  /**
   * Lookup configuration repository.
   *
   * @return the configuration repository
   */
  public static ConfigurationRepository lookup() {
    if (validCallers.contains(Thread.currentThread().getStackTrace()[2].getClassName())) {
      return repo;
    }
    return null;
  }

  /**
   * Gets tenants.
   *
   * @return the tenants
   */
  public Set<String> getTenants() {
    return tenants;
  }

  /**
   * Gets namespaces.
   *
   * @return the namespaces
   */
  public Set<String> getNamespaces() {
    return namespaces;
  }

  private void populateTenantsNamespace(String key, boolean sourcedFromDb) {
    String[] array = key.split(Constants.KEY_ELEMENTS_DELEMETER);
    if (!array[1].toUpperCase().equals(Constants.DB_NAMESPACE)) {
      if (!sourcedFromDb) {
        dbAccessible = false;
      }
      tenants.add(array[0]);
      namespaces.add(array[1]);
    }
  }

  /**
   * Init tenants and namespaces.
   */
  public void initTenantsAndNamespaces() {
    try {
      Collection<String> collection = ConfigurationUtils.executeSelectSql(
          getConfigurationFor(Constants.DEFAULT_TENANT, Constants.DB_NAMESPACE)
              .getString("fetchnamescql"), new String[]{});
      Iterator<String> iterator = collection.iterator();
      while (iterator.hasNext()) {
        populateTenantsNamespace(iterator.next(), true);
      }
    } catch (Exception exception) {
      //exception.printStackTrace();
    }
  }

  /**
   * Is valid tenant boolean.
   *
   * @param tenant the tenant
   * @return the boolean
   */
  public boolean isValidTenant(String tenant) {
    return tenant == null ? false : tenants.contains(tenant.toUpperCase());
  }

  /**
   * Is valid namespace boolean.
   *
   * @param namespace the namespace
   * @return the boolean
   */
  public boolean isValidNamespace(String namespace) {
    return namespace == null ? false : namespaces.contains(namespace.toUpperCase());
  }

  /**
   * Gets configuration for.
   *
   * @param tenant    the tenant
   * @param namespace the namespace
   * @return the configuration for
   * @throws Exception the exception
   */
  public Configuration getConfigurationFor(String tenant, String namespace) throws Exception {
    ConfigurationHolder config = null;
    String module = tenant + Constants.KEY_ELEMENTS_DELEMETER + namespace;
    config = store.get(module);
    if (config == null) {
      config = new ConfigurationHolder(ConfigurationUtils
          .getDbConfigurationBuilder(tenant + Constants.KEY_ELEMENTS_DELEMETER + namespace));
      store.put(module, config);
    }
    return config.getConfiguration(tenant + Constants.KEY_ELEMENTS_DELEMETER + namespace);
  }

  /**
   * Populate configurtaion.
   *
   * @param key     the key
   * @param builder the builder
   */
  public void populateConfigurtaion(String key, Configuration builder) {
    store.put(key, new ConfigurationHolder(builder));
    populateTenantsNamespace(key, false);
  }

  /**
   * Populate configurtaion.
   *
   * @param key     the key
   * @param builder the builder
   * @throws Exception the exception
   */
  public void populateConfigurtaion(String key, BasicConfigurationBuilder builder)
      throws Exception {
    store.put(key, new ConfigurationHolder(builder));
  }

  /**
   * Populate override configurtaion.
   *
   * @param key  the key
   * @param file the file
   * @throws Exception the exception
   */
  public void populateOverrideConfigurtaion(String key, File file) throws Exception {
    ConfigurationHolder holder = store.get(key);
    if (holder == null) {
      if (dbAccessible) {
        store.put(key,
            holder = new ConfigurationHolder(ConfigurationUtils.getDbConfigurationBuilder(key)));
      } else {
        store.put(key, holder = new ConfigurationHolder(new CombinedConfiguration()));
      }
    }
    holder.addOverrideConfiguration(file.getAbsolutePath(),
        ConfigurationUtils.getConfigurationBuilder(file, true));
    populateTenantsNamespace(key, true);
  }

  /**
   * Refresh override configurtaion for.
   *
   * @param key   the key
   * @param index the index
   * @throws Exception the exception
   */
  public void refreshOverrideConfigurtaionFor(String key, int index) throws Exception {
    ConfigurationHolder holder = store.get(key);
    if (holder != null) {
      holder.refreshOverrideConfiguration(index);
    }
  }

  /**
   * Remove override configurtaion.
   *
   * @param file the file
   * @throws Exception the exception
   */
  public void removeOverrideConfigurtaion(File file) throws Exception {
    Iterator<String> iterator = new ArrayList(store.keySet()).iterator();
    while (iterator.hasNext()) {
      ConfigurationHolder holder = store.get(iterator.next());
      if (holder.containsOverrideConfiguration(file.getAbsolutePath())) {
        holder.removeOverrideConfiguration(file.getAbsolutePath());
      }
    }

  }

  private class ConfigurationHolder {

    /**
     * The Builder.
     */
    BasicConfigurationBuilder<Configuration> builder;
    /**
     * The Last configuration build time.
     */
    Timestamp lastConfigurationBuildTime;
    /**
     * The Config.
     */
    Configuration config;
    /**
     * The Composite.
     */
    Configuration composite;
    /**
     * The Last config change timestamp.
     */
    Timestamp lastConfigChangeTimestamp;
    private Map<String, FileBasedConfigurationBuilder<FileBasedConfiguration>>
        overrideConfiguration = new LinkedHashMap<>();


    /**
     * Instantiates a new Configuration holder.
     *
     * @param builder the builder
     */
    public ConfigurationHolder(BasicConfigurationBuilder builder) {
      this.builder = builder;
    }

    /**
     * Instantiates a new Configuration holder.
     *
     * @param builder the builder
     */
    public ConfigurationHolder(Configuration builder) {
      this.config = builder;
    }

    /**
     * Refresh override configuration.
     *
     * @param index the index
     */
    public void refreshOverrideConfiguration(int index) {
      int count = -1;
      for (FileBasedConfigurationBuilder overrides : overrideConfiguration.values()) {
        try {
          if (++count == index) {
            overrides.save();
            overrides.resetResult();
          }
        } catch (ConfigurationException exception) {
          //do nothing
        }
      }
    }

    /**
     * Add override configuration.
     *
     * @param path    the path
     * @param builder the builder
     */
    public void addOverrideConfiguration(String path,
                                     BasicConfigurationBuilder<FileBasedConfiguration> builder) {
      overrideConfiguration.put(path.toUpperCase(), (FileBasedConfigurationBuilder) builder);
      getEffectiveConfiguration(config, overrideConfiguration.values());
    }

    /**
     * Remove override configuration.
     *
     * @param path the path
     */
    public void removeOverrideConfiguration(String path) {
      overrideConfiguration.remove(path.toUpperCase());
      getEffectiveConfiguration(config, overrideConfiguration.values());
    }

    /**
     * Contains override configuration boolean.
     *
     * @param path the path
     * @return the boolean
     */
    public boolean containsOverrideConfiguration(String path) {
      return overrideConfiguration.containsKey(path.toUpperCase());
    }

    /**
     * Gets configuration.
     *
     * @param namespace the namespace
     * @return the configuration
     * @throws Exception the exception
     */
    public Configuration getConfiguration(String namespace) throws Exception {
      if (config == null) {
        config = builder.getConfiguration();
        lastConfigurationBuildTime = new Timestamp(System.currentTimeMillis());
      } else if (lastConfigurationBuildTime != null
          && System.currentTimeMillis() - lastConfigurationBuildTime.getTime()
          > getConfigurationFor(Constants.DEFAULT_TENANT, Constants.DB_NAMESPACE)
                  .getInt("config.refresh.interval")) {
        Timestamp temp = getLastUpdateTimestampFor(namespace);
        if (temp != null) {
          if (lastConfigChangeTimestamp == null
              || temp.getTime() > lastConfigChangeTimestamp.getTime()) {
            builder.resetResult();
            config = builder.getConfiguration();
            lastConfigChangeTimestamp = temp;
            getEffectiveConfiguration(config, overrideConfiguration.values());
          }
        }
        lastConfigurationBuildTime = new Timestamp(System.currentTimeMillis());
      }
      if (composite == null && overrideConfiguration.size() != 0) {
        composite = getEffectiveConfiguration(config, overrideConfiguration.values());
      }
      return overrideConfiguration.size() == 0 ? config : composite;
    }

    private Configuration getEffectiveConfiguration(Configuration configuration,
                    Collection<FileBasedConfigurationBuilder<FileBasedConfiguration>> list) {
      try {
        CompositeConfiguration cc = new CompositeConfiguration();
        for (FileBasedConfigurationBuilder<FileBasedConfiguration> b : list) {
          cc.addConfiguration(b.getConfiguration());
        }
        cc.addConfiguration(configuration);
        return composite = cc;
      } catch (Exception exception) {
        exception.printStackTrace();
        return null;
      }
    }

    /**
     * Gets last update timestamp for.
     *
     * @param namespace the namespace
     * @return the last update timestamp for
     */
    public Timestamp getLastUpdateTimestampFor(String namespace) {
      Timestamp timestamp = null;

      try {
        Collection<String> collection = ConfigurationUtils.executeSelectSql(
            getConfigurationFor(Constants.DEFAULT_TENANT, Constants.DB_NAMESPACE)
                .getString("fetchlastchangecql"), new String[]{namespace});
        if (!collection.isEmpty()) {
          timestamp = new Timestamp(Long.valueOf(((ArrayList) collection).get(0).toString()));
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }

      return timestamp;
    }


  }

  public boolean isDBAccessible(){
    return dbAccessible;
  }


}
