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

package org.openecomp.sdc.heat.datatypes.model;

import org.junit.Test;
import org.openecomp.sdc.tosca.services.YamlUtil;

import java.io.IOException;
import java.io.InputStream;

public class EnvironmentTest {

  @Test
  public void testYamlToServiceTemplateObj() throws IOException {
    YamlUtil yamlUtil = new YamlUtil();
    try (InputStream yamlFile = yamlUtil.loadYamlFileIs("/mock/model/envSettings.env")) {
      Environment envVars = yamlUtil.yamlToObject(yamlFile, Environment.class);
      envVars.toString();
    }
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

      try {
        System.out.println(heatResourceName.substring(0, lastIndexOfUnderscore));
      } catch (NumberFormatException ignored) {
        System.out.println(heatResourceName);
      }
    }
  }
}
