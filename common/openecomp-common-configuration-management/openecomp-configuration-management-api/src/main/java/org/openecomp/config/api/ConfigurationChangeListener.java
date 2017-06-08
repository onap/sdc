package org.openecomp.config.api;

public interface ConfigurationChangeListener {

  public default void notify(String tenantId, String component, String key, Object oldValue,
                             Object newValue) {
  }

  public default void notify(String component, String key, Object oldValue, Object newValue) {
    System.out.println("HIT");
  }

  public default void notify(String key, Object oldValue, Object newValue) {
  }
}
