package org.openecomp.config.impl;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.apache.commons.configuration2.tree.UnionCombiner;
import static org.openecomp.config.Constants.LOAD_ORDER_KEY;
import org.openecomp.config.ConfigurationUtils;
import org.openecomp.config.type.ConfigurationMode;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.*;

/**
 * The type Aggregate configuration.
 */
public final class AggregateConfiguration {

  private Map<String, Configuration> rootConfig = new HashMap<>();
  private Map<String, Configuration> unionConfig = new HashMap<>();
  private Map<String, Configuration> mergeConfig = new HashMap<>();
  private Map<String, Configuration> overrideConfig = new LinkedHashMap<>();

  {
    if (!Thread.currentThread().getStackTrace()[2].getClassName()
        .equals(ConfigurationImpl.class.getName())) {
      throw new RuntimeException("Illegal access.");
    }
  }

  private void addConfig(String path, ConfigurationMode configMode, Configuration config) {
    if (configMode != null) {
      switch (configMode) {
        case MERGE:
          mergeConfig.put(path, config);
          break;
        case OVERRIDE:
          overrideConfig.put(path, config);
          break;
        case UNION:
          unionConfig.put(path, config);
          break;
        default:
      }
    } else {
      rootConfig.put(path, config);
    }
  }

  /**
   * Add config.
   *
   * @param file the file
   * @throws Exception the exception
   */
  public void addConfig(File file) throws Exception {
    addConfig(file.getAbsolutePath().toUpperCase(), ConfigurationUtils.getMergeStrategy(file),
        ConfigurationUtils.getConfigurationBuilder(file, false).getConfiguration());
  }

  /**
   * Add config.
   *
   * @param url the url
   * @throws Exception the exception
   */
  public void addConfig(URL url) throws Exception {
    addConfig(url.getFile().toUpperCase(), ConfigurationUtils.getMergeStrategy(url),
        ConfigurationUtils.getConfigurationBuilder(url).getConfiguration());
  }

  /**
   * Remove config.
   *
   * @param file the file
   */
  public void removeConfig(File file) {
    String key = file.getAbsolutePath().toUpperCase();
    if (rootConfig.containsKey(key)) {
      rootConfig.remove(key);
    } else if (mergeConfig.containsKey(key)) {
      mergeConfig.remove(key);
    } else if (unionConfig.containsKey(key)) {
      unionConfig.remove(key);
    } else if (overrideConfig.containsKey(key)) {
      overrideConfig.remove(key);
    }
  }

  /**
   * Contains config boolean.
   *
   * @param file the file
   * @return the boolean
   */
  public boolean containsConfig(File file) {
    String key = file.getAbsolutePath().toUpperCase();
    return rootConfig.containsKey(key) || mergeConfig.containsKey(key)
        || unionConfig.containsKey(key) || overrideConfig.containsKey(key);
  }

  /**
   * Gets final configuration.
   *
   * @return the final configuration
   */
  public Configuration getFinalConfiguration() {
    CombinedConfiguration ccRoot = new CombinedConfiguration(new MergeCombiner());
    ArrayList<Configuration> tempList = new ArrayList<>(rootConfig.values());
    Collections.sort(tempList, this::sortForMerge);
    for (Configuration conf : tempList) {
      ccRoot.addConfiguration(conf);
    }
    CombinedConfiguration ccMergeRoot = new CombinedConfiguration(new MergeCombiner());
    ccMergeRoot.addConfiguration(ccRoot);
    tempList = new ArrayList<>(mergeConfig.values());
    Collections.sort(tempList, this::sortForMerge);
    for (Configuration conf : tempList) {
      ccMergeRoot.addConfiguration(conf);
    }
    CombinedConfiguration ccUnionRoot = new CombinedConfiguration(new UnionCombiner());
    ccUnionRoot.addConfiguration(ccMergeRoot);
    for (Configuration conf : unionConfig.values()) {
      ccUnionRoot.addConfiguration(conf);
    }
    ArrayList<Configuration> tempOverrideConfigs = new ArrayList<>(overrideConfig.values());
    Collections.reverse(tempOverrideConfigs);
    Collections.sort(tempOverrideConfigs, this::sortForOverride);
    CombinedConfiguration ccOverrideRoot = new CombinedConfiguration(new OverrideCombiner());
    for (Configuration conf : tempOverrideConfigs) {
      ccOverrideRoot.addConfiguration(conf);
    }
    ccOverrideRoot.addConfiguration(ccUnionRoot);
    return ccOverrideRoot;
  }

  private int sortForOverride(Configuration conf1, Configuration conf2){
    String order1 = conf1.getString(LOAD_ORDER_KEY);
    String order2 = conf2.getString(LOAD_ORDER_KEY);
    if (ConfigurationUtils.isBlank(order1) || !order1.trim().matches("\\d+")){
      order1 = "0";
    }
    if (ConfigurationUtils.isBlank(order2) || !order2.trim().matches("\\d+")){
      order2 = "0";
    }
    return Integer.parseInt(order2.trim())-Integer.parseInt(order1.trim());
  }

  private int sortForMerge(Configuration conf1, Configuration conf2){
    String order1 = conf1.getString(LOAD_ORDER_KEY);
    String order2 = conf2.getString(LOAD_ORDER_KEY);
    if (ConfigurationUtils.isBlank(order1) || !order1.trim().matches("\\d+")){
      order1 = "0";
    }
    if (ConfigurationUtils.isBlank(order2) || !order2.trim().matches("\\d+")){
      order2 = "0";
    }
    return Integer.parseInt(order1.trim())-Integer.parseInt(order2.trim());
  }

}
