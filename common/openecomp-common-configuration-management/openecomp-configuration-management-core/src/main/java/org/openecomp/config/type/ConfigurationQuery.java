package org.openecomp.config.type;

import org.openecomp.config.Constants;

public class ConfigurationQuery {

  String tenant = Constants.DEFAULT_TENANT;
  String namespace = Constants.DEFAULT_NAMESPACE;
  String key;
  boolean fallback;
  boolean externalLookup;
  boolean latest;
  private boolean nodeSpecific;

  public ConfigurationQuery fallback(boolean fallback) {
    this.fallback = fallback;
    return this;
  }

  public ConfigurationQuery latest(boolean val) {
    this.latest = val;
    return this;
  }

  public ConfigurationQuery nodeSpecific(boolean val) {
    this.nodeSpecific = val;
    return this;
  }

  public ConfigurationQuery externalLookup(boolean val) {
    this.externalLookup = val;
    return this;
  }

  /**
   * Tenant configuration query.
   *
   * @param id the id
   * @return the configuration query
   */
  public ConfigurationQuery tenant(String id) {
    if (id != null) {
      tenant = id;
    }
    return this;
  }


  /**
   * Namespace configuration query.
   *
   * @param id the id
   * @return the configuration query
   */
  public ConfigurationQuery namespace(String id) {
    if (id != null) {
      namespace = id;
    }
    return this;
  }

  public ConfigurationQuery key(String id) {
    key = id;
    return this;
  }

  public String getTenant() {
    return tenant.toUpperCase();
  }

  public String getNamespace() {
    return namespace.toUpperCase();
  }

  public String getKey() {
    return key;
  }

  public boolean isFallback() {
    return fallback;
  }

  public boolean isNodeSpecific() {
    return nodeSpecific;
  }

  public boolean isExternalLookup() {
    return externalLookup;
  }

  public boolean isLatest() {
    return latest;
  }
}
