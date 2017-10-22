package org.openecomp.config.impl;

import static org.openecomp.config.ConfigurationUtils.isArray;

import org.openecomp.config.ConfigurationUtils;
import org.openecomp.config.Constants;
import org.openecomp.config.api.ConfigurationChangeListener;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.config.api.Hint;

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


/**
 * The type Configuration change notifier.
 */
public final class ConfigurationChangeNotifier {

  private final HashMap<String, List<NotificationData>> store = new HashMap<>();
  private final ScheduledExecutorService executor =
      Executors.newScheduledThreadPool(5, ConfigurationUtils.getThreadFactory());
  private final ExecutorService notificationExecutor =
      Executors.newCachedThreadPool(ConfigurationUtils.getThreadFactory());
  private final Map<String, WatchService> watchServiceCollection =
      Collections.synchronizedMap(new HashMap<>());

  static {
    if (!Thread.currentThread().getStackTrace()[2].getClassName()
        .equals(ConfigurationImpl.class.getName())) {
      throw new RuntimeException("Illegal access.");
    }
  }

  /**
   * Instantiates a new Configuration change notifier.
   *
   * @param inMemoryConfig the in memory config
   */
  public ConfigurationChangeNotifier(Map<String, AggregateConfiguration> inMemoryConfig) {
    executor.scheduleWithFixedDelay(() -> this
        .pollFilesystemAndUpdateConfigurationIfRequired(inMemoryConfig,
            System.getProperty("config.location"), false), 1, 1, TimeUnit.MILLISECONDS);
    executor.scheduleWithFixedDelay(() -> this
        .pollFilesystemAndUpdateConfigurationIfRequired(inMemoryConfig,
            System.getProperty("tenant.config.location"), true), 1, 1, TimeUnit.MILLISECONDS);
    executor.scheduleWithFixedDelay(() -> this
        .pollFilesystemAndUpdateNodeSpecificConfigurationIfRequired(
            System.getProperty("node.config.location")), 1, 1, TimeUnit.MILLISECONDS);
  }

  /**
   * Shutdown.
   */
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

  /**
   * Poll filesystem and update configuration if required.
   *
   * @param inMemoryConfig   the in memory config
   * @param location         the location
   * @param isTenantLocation the is tenant location
   */
  public void pollFilesystemAndUpdateConfigurationIfRequired(
      Map<String, AggregateConfiguration> inMemoryConfig, String location,
      boolean isTenantLocation) {
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
                      (tenantRoot.getName() + Constants.TENANT_NAMESPACE_SAPERATOR
                          + ConfigurationUtils.getNamespace(file))
                          .split(Constants.TENANT_NAMESPACE_SAPERATOR));
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
              String[] tenantNamespaceArray =
                  repositoryKey.split(Constants.KEY_ELEMENTS_DELEMETER);
              updateConfigurationValues(tenantNamespaceArray[0], tenantNamespaceArray[1], map);
            }
          } else {
            for (String s : inMemoryConfig.keySet()) {
              repositoryKey = s;
              AggregateConfiguration config = inMemoryConfig.get(repositoryKey);
              if (config.containsConfig(file)) {
                LinkedHashMap origConfig = ConfigurationUtils.toMap(config.getFinalConfiguration());
                config.removeConfig(file);
                LinkedHashMap latestConfig =
                        ConfigurationUtils.toMap(config.getFinalConfiguration());
                Map map = ConfigurationUtils.diff(origConfig, latestConfig);
                String[] tenantNamespaceArray =
                        repositoryKey.split(Constants.KEY_ELEMENTS_DELEMETER);
                updateConfigurationValues(tenantNamespaceArray[0], tenantNamespaceArray[1],
                        map);
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

  private void updateConfigurationValues(String tenant, String namespace, Map map)
      throws Exception {
    MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
    ObjectName mbeanName = new ObjectName(Constants.MBEAN_NAME);
    ConfigurationManager conf =
        JMX.newMBeanProxy(mbsc, mbeanName, org.openecomp.config.api.ConfigurationManager.class,
            true);
    conf.updateConfigurationValues(tenant, namespace, map);
  }

  /**
   * Poll filesystem and update node specific configuration if required.
   *
   * @param location the location
   */
  public void pollFilesystemAndUpdateNodeSpecificConfigurationIfRequired(String location) {
    try {
      Set<Path> paths = watchForChange(location);
      if (paths != null) {
        for (Path path : paths) {
          File file = path.toAbsolutePath().toFile();
          String repositoryKey;
          if (ConfigurationUtils.isConfig(file)) {
            repositoryKey = ConfigurationUtils.getConfigurationRepositoryKey(file);
            ConfigurationRepository.lookup().populateOverrideConfigurtaion(repositoryKey, file);
          } else {
            ConfigurationRepository.lookup().removeOverrideConfigurtaion(file);
          }
        }
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  /**
   * Notify changes towards.
   *
   * @param tenant    the tenant
   * @param component the component
   * @param key       the key
   * @param myself    the myself
   * @throws Exception the exception
   */
  public void notifyChangesTowards(String tenant, String component, String key,
                                   ConfigurationChangeListener myself) throws Exception {
    List<NotificationData> notificationList =
        store.get(tenant + Constants.KEY_ELEMENTS_DELEMETER + component);
    if (notificationList == null) {
      notificationList = Collections.synchronizedList(new ArrayList<NotificationData>());
      store.put(tenant + Constants.KEY_ELEMENTS_DELEMETER + component, notificationList);
      executor.scheduleWithFixedDelay(
          () -> triggerScanning(tenant + Constants.KEY_ELEMENTS_DELEMETER + component), 1, 30000,
          TimeUnit.MILLISECONDS);
    }
    notificationList.add(new NotificationData(tenant, component, key, myself));
  }

  /**
   * Stop notification towards.
   *
   * @param tenant    the tenant
   * @param component the component
   * @param key       the key
   * @param myself    the myself
   * @throws Exception the exception
   */
  public void stopNotificationTowards(String tenant, String component, String key,
                                      ConfigurationChangeListener myself) throws Exception {
    List<NotificationData> notificationList =
        store.get(tenant + Constants.KEY_ELEMENTS_DELEMETER + component);
    if (notificationList != null) {
      boolean removed =
          notificationList.remove(new NotificationData(tenant, component, key, myself));
      if (removed && notificationList.isEmpty()) {
        store.remove(tenant + Constants.KEY_ELEMENTS_DELEMETER + component);
      }
    }

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
      for (NotificationData notificationData : list) {
        if (notificationData.isChanged()) {
          notificationExecutor.submit(() -> sendNotification(notificationData));
        }
      }
    }
  }

  private void sendNotification(NotificationData notificationData) {
    try {
      notificationData.dispatchNotification();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private Set<Path> watchForChange(String location) throws Exception {
    if (location == null || location.trim().length() == 0) {
      return null;
    }
    File file = new File(location);
    if (!file.exists()) {
      return null;
    }
    Path path = file.toPath();
    Set<Path> toReturn = new HashSet<>();
    try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
      watchServiceCollection.put(location, watchService);
      path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,
          StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
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

  /**
   * The type Notification data.
   */
  static class NotificationData {

    /**
     * The Tenant.
     */
    final String tenant;
    /**
     * The Namespace.
     */
    final String namespace;
    /**
     * The Key.
     */
    final String key;
    /**
     * The Myself.
     */
    final ConfigurationChangeListener myself;
    /**
     * The Current value.
     */
    Object currentValue;
    /**
     * The Is array.
     */
    boolean isArray;

    /**
     * Instantiates a new Notification data.
     *
     * @param tenant    the tenant
     * @param component the component
     * @param key       the key
     * @param myself    the myself
     * @throws Exception the exception
     */
    public NotificationData(String tenant, String component, String key,
                            ConfigurationChangeListener myself) throws Exception {
      this.tenant = tenant;
      this.namespace = component;
      this.key = key;
      this.myself = myself;
      if (!ConfigurationRepository.lookup().getConfigurationFor(tenant, component)
          .containsKey(key)) {
        throw new RuntimeException("Key[" + key + "] not found.");
      }
      isArray = isArray(tenant, component, key, Hint.DEFAULT.value());
      if (isArray) {
        currentValue = ConfigurationManager.lookup().getAsStringValues(tenant, component, key);
      } else {
        currentValue = ConfigurationManager.lookup().getAsString(tenant, component, key);
      }
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof NotificationData)) {
        return false;
      }
      NotificationData nd = (NotificationData) obj;
      return Objects.equals(tenant, nd.tenant)
              && Objects.equals(namespace, nd.namespace)
              && Objects.equals(key, nd.key)
              && Objects.equals(myself, nd.myself)
              && Objects.equals(currentValue, nd.currentValue) // it's either String or List<String>
              && isArray == nd.isArray;
    }

    @Override
    public int hashCode() {
      return Objects.hash(tenant, namespace, key, myself, currentValue, isArray);
    }

    /**
     * Is changed boolean.
     *
     * @return the boolean
     */
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

    /**
     * Dispatch notification.
     *
     * @throws Exception the exception
     */
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
        isArray = isArray(tenant, namespace, key, Hint.DEFAULT.value());
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
