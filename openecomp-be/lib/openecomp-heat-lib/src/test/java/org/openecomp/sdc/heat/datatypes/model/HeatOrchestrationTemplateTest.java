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

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.services.YamlUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeatOrchestrationTemplateTest {

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

  @Test
  public void testYamlToServiceTemplateObj() throws IOException {
    YamlUtil yamlUtil = new YamlUtil();
    try (InputStream yamlFile = yamlUtil.loadYamlFileIs("/mock/model/testHeat.yml")) {
      HeatOrchestrationTemplate heatOrchestrationTemplate =
              yamlUtil.yamlToObject(yamlFile, HeatOrchestrationTemplate.class);
      heatOrchestrationTemplate.toString();
    }
  }

  @Test
  public void createHotTemplate() {
    HeatOrchestrationTemplate template = new HeatOrchestrationTemplate();
    template.setHeat_template_version("2016-04-14");
    template.setDescription("test description for hot template");
    Map<String, Parameter> params = createParameters();
    template.setParameters(params);
    List<ParameterGroup> parameterGroup = new ArrayList<>();
    ParameterGroup paramsGroup = new ParameterGroup();
    paramsGroup.setDescription("params group test description");
    paramsGroup.setLabel("params group test label");
    String paramName = params.get("param1").getLabel();
    List<String> paramsNames = new ArrayList<>();
    paramsNames.add(paramName);
    paramsGroup.setParameters(paramsNames);
    parameterGroup.add(paramsGroup);
    template.setParameter_groups(parameterGroup);
    Map<String, Object> conditions = new HashMap<>();
    conditions.put("key1", "val1");
    HashMap<String, Object> mapValue = new HashMap<>();
    mapValue.put("innerKey", "innerVal");
    conditions.put("key2", mapValue);
    template.setConditions(conditions);

    Map<String, Resource> resources = new HashMap<>();
    Resource resource = new Resource();
    resource.setMetadata("resource metadata");
    resource.setType("resource type");
    //Map<String, String> resourceProps = new ;
    Map<String, Object> resourceProps = new HashMap<>();
    resourceProps.put("aaa", "bbb");
    //resourceProps.add(resourceProp);
    resource.setProperties(resourceProps);
    resources.put("R1", resource);
    resource = new Resource();
    resource.setMetadata("resource2 metadata");
    resource.setType("resource2 type");
    //resourceProps = new ArrayList<>();
    resourceProps = new HashMap<>();
    resourceProps.put("aaa2", "bbb2");
    //resourceProps.add(resourceProp);
    resource.setProperties(resourceProps);
    List<String> dependsOn = new ArrayList<>();
    dependsOn.add("R1");
    resource.setDepends_on(dependsOn);
    resource.setDeletion_policy("all");
    resource.setUpdate_policy("once");
    resources.put("R2", resource);
    template.setResources(resources);

    YamlUtil yamlUtil = new YamlUtil();
    String yml = yamlUtil.objectToYaml(template);
    Assert.assertNotNull(yml);
    try {
      HeatOrchestrationTemplate heatOrchestrationTemplate =
          yamlUtil.yamlToObject(yml, HeatOrchestrationTemplate.class);
      Assert.assertNotNull(heatOrchestrationTemplate);
    } catch (Exception ignored) {
      log.debug("",ignored);
    }
  }

  private Map<String, Parameter> createParameters() {
    Map<String, Parameter> params = new HashMap<>();
    Parameter param;
    for (int i = 0; i < 2; i++) {
      param = new Parameter();
      param.setDescription("param " + i + " desc");
      param.setLabel("param " + i + " label");
      param.set_default("PARAM " + i + " default");
      param.setHidden(i % 2 == 0);
      param.setImmutable(i % 2 == 0);
      param.setType(i % 2 == 0 ? ParameterType.STRING.getDisplayName()
          : ParameterType.BOOLEAN.getDisplayName());
      params.put("param" + i, param);
    }

    return params;
  }

  private List<Constraint> createConstraints() {
    List<Constraint> constraints = new ArrayList<>();
    Constraint constraint = new Constraint();
    constraint.setLength(new Integer[]{2, 4});
    constraints.add(constraint);
    constraint = new Constraint();
    constraint.setPattern("some regex");
    constraints.add(constraint);
    constraint = new Constraint();
    constraint.setRange(new Integer[]{5, 8});
    constraints.add(constraint);
    constraint = new Constraint();
    List<Object> validValues = new ArrayList<>();
    validValues.add("abc");
    validValues.add("def");
    constraint.setValidValues(validValues);
    constraints.add(constraint);
    return constraints;
  }
}
