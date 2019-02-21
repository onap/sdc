/*
 * Copyright © 2019 iconectiv
 *
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
 */

package org.openecomp.core.externaltesting.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.onap.sdc.tosca.services.YamlUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ConfigurationTests {

  @Test
  public void testClientConfig() {
    // a brain dead test of the setter and getter.
    // future tests for more complex config to come.
    ClientConfiguration cc = new ClientConfiguration();
    cc.setEnabled(true);
    Assert.assertTrue("client configuration setter", cc.isEnabled());
    cc.setEnabled(false);
    Assert.assertFalse("client configuration setter", cc.isEnabled());
  }

  @Test
  public void testConfig() throws Exception {
    try (InputStream fileInput = new FileInputStream(new File("src/test/data/testconfiguration.yaml"))) {
      YamlUtil yamlUtil = new YamlUtil();
      Object raw = yamlUtil.yamlToMap(fileInput);
      TestingAccessConfig accessConfig = new ObjectMapper().convertValue(raw, TestingAccessConfig.class);
      Assert.assertNotNull("client config available", accessConfig.getClient());
    }
  }

  @Test
  public void testEndpointDefinition() {
    RemoteTestingEndpointDefinition def = new RemoteTestingEndpointDefinition();
    def.setId("vtp");
    def.setPostStyle("application/json");
    def.setEnabled(true);
    def.setTitle("VTP");
    def.setUrl("http://ec2-34-237-35-152.compute-1.amazonaws.com");
    def.setApiKey("FOOBARBAZ");


    RemoteTestingEndpointDefinition def2 = new RemoteTestingEndpointDefinition();
    def2.setId("vtp");
    def2.setPostStyle("application/json");
    def2.setEnabled(true);
    def2.setTitle("VTP");
    def2.setUrl("http://ec2-34-237-35-152.compute-1.amazonaws.com");
    def2.setApiKey("FOOBARBAZ");

    Assert.assertEquals("hashcode", def.hashCode(), def2.hashCode());
    Assert.assertEquals("equals", def, def2);
    Assert.assertEquals("equals same object", def, def);

    Assert.assertEquals("API keys equals", def.getApiKey(), def2.getApiKey());
    Assert.assertEquals("title equals", def.getTitle(), def2.getTitle());

    Assert.assertNotEquals("diff object type check", def, null);


  }
}
