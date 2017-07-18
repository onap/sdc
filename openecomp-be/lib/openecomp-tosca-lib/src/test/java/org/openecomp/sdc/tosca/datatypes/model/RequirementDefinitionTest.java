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

package org.openecomp.sdc.tosca.datatypes.model;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.tosca.services.YamlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shiria
 * @since September 07, 2016.
 */
public class RequirementDefinitionTest {

  @Test
  public void cloneTest() {
    RequirementDefinition reqDef1 = new RequirementDefinition();
    reqDef1.setNode("node1");
    reqDef1.setRelationship("my Relationship");
    reqDef1.setCapability("capabilities");
    reqDef1.setOccurrences(new Object[]{1, 1});

    RequirementDefinition reqDef2 = reqDef1.clone();
    NodeType nodeType = new NodeType();

    List<Map<String, RequirementDefinition>> requirements = new ArrayList<>();
    Map<String, RequirementDefinition> reqMap1 = new HashMap<>();
    reqMap1.put("req1", reqDef1);
    requirements.add(reqMap1);
    Map<String, RequirementDefinition> reqMap2 = new HashMap<>();
    reqMap2.put("req2", reqDef2);
    requirements.add(reqMap2);
    nodeType.setRequirements(requirements);

    String yamlString = new YamlUtil().objectToYaml(nodeType);
    Boolean passResult = !yamlString.contains("&") && !yamlString.contains("*");
    Assert.assertEquals(true, passResult);
  }


}
