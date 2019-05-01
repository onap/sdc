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
import org.openecomp.core.externaltesting.api.ClientConfiguration;
import org.openecomp.core.externaltesting.api.RemoteTestingEndpointDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class TestingAccessConfigTest {

  @Test
  public void testConfig() throws Exception {
    try (InputStream fileInput = new FileInputStream(new File("src/test/data/externaltesting-configuration.yaml"))) {
      YamlUtil yamlUtil = new YamlUtil();
      Object raw = yamlUtil.yamlToMap(fileInput);
      TestingAccessConfig accessConfig = new ObjectMapper().convertValue(raw, TestingAccessConfig.class);
      Assert.assertNotNull("client config available", accessConfig.getClient());
    }
  }

}
