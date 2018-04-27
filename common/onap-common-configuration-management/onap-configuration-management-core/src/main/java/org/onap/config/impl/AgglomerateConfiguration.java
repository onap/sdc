package org.onap.config.impl;

import org.apache.commons.configuration2.DatabaseConfiguration;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The type Agglomerate configuration.
 */
public class AgglomerateConfiguration extends DatabaseConfiguration {

  private final Map<String, Object> store =
      Collections.synchronizedMap(new WeakHashMap<String, Object>());

  /**
   * Gets property value.
   *
   * @param key the key
   * @return the property value
   */
  public Object getPropertyValue(String key) {
    Object objToReturn;
    objToReturn = store.get(key);
    if (objToReturn == null && !store.containsKey(key)) {
      objToReturn = super.getProperty(key);
      store.put(key, objToReturn);
    }
    return objToReturn;
  }

}
