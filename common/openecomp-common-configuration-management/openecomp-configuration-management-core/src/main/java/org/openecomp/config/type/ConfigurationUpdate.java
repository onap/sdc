package org.openecomp.config.type;

public class ConfigurationUpdate extends ConfigurationQuery {
  private String value;
  private boolean nodeOverride;

  public ConfigurationUpdate value(String val) {
    value = val;
    return this;
  }

  public ConfigurationUpdate nodeOverride(boolean val) {
    nodeOverride = val;
    return this;
  }

  /**
   * Gets value.
   *
   * @return the value
   */
  public String getValue() {
    if (value != null && value.split(",").length > 1 && !value.matches("^\\[.*\\]$")) {
      return "[" + value + "]";
    }
    return value;
  }

  public boolean isNodeOverride() {
    return nodeOverride;
  }

}
