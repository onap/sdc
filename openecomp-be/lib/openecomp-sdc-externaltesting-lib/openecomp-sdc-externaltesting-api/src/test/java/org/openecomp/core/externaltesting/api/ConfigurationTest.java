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

package org.openecomp.core.externaltesting.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.onap.sdc.tosca.services.YamlUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ConfigurationTest {

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
  public void testEndpointDefinition() {
    RemoteTestingEndpointDefinition def = new RemoteTestingEndpointDefinition();
    def.setId("vtp");
    def.setEnabled(true);
    def.setTitle("VTP");
    def.setApiKey("FOOBARBAZ");
    def.setUrl("http://example.com/vtptesting");
    def.setScenarioFilter("c.*");
    def.setConfig("vtp,VTP,true,http://example.com/vtptesting,c.*,FOO");

    Assert.assertEquals("code", "VTP", def.getTitle());
    Assert.assertEquals("API keys equals", "FOOBARBAZ", def.getApiKey());
    Assert.assertEquals("code equals", "VTP", def.getTitle());
    Assert.assertEquals("url equals", "http://example.com/vtptesting", def.getUrl());
    Assert.assertEquals("filter equals", "c.*", def.getScenarioFilter());
    Assert.assertTrue("enabled", def.isEnabled());
    Assert.assertEquals("id equals", "vtp", def.getId());
    Assert.assertNotNull("config has val", def.getConfig());


    boolean matches = def.getScenarioFilterPattern().matcher("certification").matches();
    Assert.assertTrue("pattern", matches);

  }
}
