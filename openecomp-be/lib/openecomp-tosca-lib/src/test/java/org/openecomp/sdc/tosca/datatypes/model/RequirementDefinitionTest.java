package org.openecomp.sdc.tosca.datatypes.model;

import org.openecomp.core.utilities.yaml.YamlUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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