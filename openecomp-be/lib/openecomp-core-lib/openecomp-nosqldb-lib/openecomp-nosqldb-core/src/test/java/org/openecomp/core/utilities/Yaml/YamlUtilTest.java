/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.core.utilities.Yaml;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.tosca.services.YamlUtil;
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
        YamlUtil yamlUtil = new YamlUtil();
        String cassandraKey = "cassandraConfig";
        String configurationFile = "/configuration.yaml";
        InputStream yamlAsIS = yamlUtil.loadYamlFileIs(configurationFile);
        Map<String, LinkedHashMap<String, Object>> configurationMap = yamlUtil.yamlToMap(yamlAsIS);
        LinkedHashMap<String, Object> cassandraConfiguration = configurationMap.get(cassandraKey);
        System.out.println(cassandraConfiguration.entrySet());
    }*/
}
