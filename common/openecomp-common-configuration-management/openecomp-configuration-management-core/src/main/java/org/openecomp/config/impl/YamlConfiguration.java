package org.openecomp.config.impl;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

public class YamlConfiguration extends JacksonConfiguration {

  protected YamlConfiguration(HierarchicalConfiguration<ImmutableNode> config) {
    super(new YAMLFactory(), config);
  }

  public YamlConfiguration() {
    super(new YAMLFactory());
  }

}
