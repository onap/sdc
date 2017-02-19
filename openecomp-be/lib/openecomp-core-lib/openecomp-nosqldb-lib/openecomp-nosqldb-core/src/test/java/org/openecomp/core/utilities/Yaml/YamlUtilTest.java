package org.openecomp.core.utilities.Yaml;

import org.openecomp.core.utilities.yaml.YamlUtil;
import org.junit.Before;
import org.junit.Test;
import testobjects.yaml.YamlFile;


public class YamlUtilTest {

  String yamlContent;

  @Before
  public void setup() {
    initYamlFileContent();
  }

  void initYamlFileContent() {
    yamlContent = "heat_template_version: ss\n" +
        "description: ab\n" +
        "parameters:\n" +
        "  jsa_net_name:    \n" +
        "    description: network name of jsa log network\n" +
        "    hidden: true\n" +
        "    inner:\n" +
        "        inner1:\n" +
        "            name: shiri\n" +
        "        inner2:\n" +
        "            name: avi";
  }

  @Test
  public void shouldConvertSimpleYamlToObject() {
    new YamlUtil().yamlToObject(yamlContent, YamlFile.class);
  }


    /*public void loadCassandraParameters(){
        YamlUtil yamlutil = new YamlUtil();
        String cassandraKey = "cassandraConfig";
        String configurationFile = "/configuration.yaml";
        InputStream yamlAsIS = yamlutil.loadYamlFileIs(configurationFile);
        Map<String, LinkedHashMap<String, Object>> configurationMap = yamlutil.yamlToMap(yamlAsIS);
        LinkedHashMap<String, Object> cassandraConfiguration = configurationMap.get(cassandraKey);
        System.out.println(cassandraConfiguration.entrySet());
    }*/
}