package org.openecomp.sdc.heat.datatypes.model;

import org.openecomp.core.utilities.yaml.YamlUtil;
import org.junit.Test;

import java.io.InputStream;

public class EnvironmentTest {

  @Test
  public void testYamlToServiceTemplateObj() {
    YamlUtil yamlUtil = new YamlUtil();
    InputStream yamlFile = yamlUtil.loadYamlFileIs("/mock/model/envSettings.env");
    Environment envVars = yamlUtil.yamlToObject(yamlFile, Environment.class);
    envVars.toString();
  }

  @Test
  public void test() {
    String heatResourceName = "server_abc_0u";
    String novaServerPrefix = "server_";
    if (heatResourceName.startsWith(novaServerPrefix)) {
      heatResourceName = heatResourceName.substring(novaServerPrefix.length());
    }
    int lastIndexOfUnderscore = heatResourceName.lastIndexOf("_");
    if (heatResourceName.length() == lastIndexOfUnderscore) {
      System.out.println(heatResourceName);
    } else {
      String heatResourceNameSuffix = heatResourceName.substring(lastIndexOfUnderscore + 1);
      try {
        Integer.parseInt(heatResourceNameSuffix);
        System.out.println(heatResourceName.substring(0, lastIndexOfUnderscore));
      } catch (NumberFormatException ignored) {
        System.out.println(heatResourceName);
      }
    }
  }
}