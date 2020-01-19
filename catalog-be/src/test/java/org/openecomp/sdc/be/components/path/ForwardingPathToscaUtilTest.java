/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.be.components.path;

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdc.be.tosca.utils.ForwardingPathToscaUtil.FORWARDER;
import static org.openecomp.sdc.be.tosca.utils.ForwardingPathToscaUtil.FORWARDS_TO_TOSCA_NAME;
import static org.openecomp.sdc.be.tosca.utils.ForwardingPathToscaUtil.PORTS_RANGE;
import static org.openecomp.sdc.be.tosca.utils.ForwardingPathToscaUtil.PROTOCOL;
import static org.openecomp.sdc.be.tosca.utils.ForwardingPathToscaUtil.addForwardingPaths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateRequirement;
import org.openecomp.sdc.be.tosca.utils.ForwardingPathToscaUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author KATYR, ORENK
 * @since November 19, 2017
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/paths/path-context.xml")
public class ForwardingPathToscaUtilTest extends BaseForwardingPathTest {

  private static final String NODE_NAME_1 = "nodeA-name";
  private static final String NODE_NAME_2 = "nodeB-name";
  private static final String PATH_1_PROTOCOL = "protocol-path1";
  private static final String PATH_1_DESC = "path1-desc";
  private static final String PATH_1_PORTS = "8585";
  private static final String PATH_1_NAME = "pathName1";
  private Service service;
  private Map<String, Component> originComponents = new HashMap<>();
  private static final String NODE_ID_1 = "nodeA-id";
  private static final String NODE_ID_2 = "nodeB-id";
  private static final String FORWARDING_PATH_TOSCA_TYPE = "org.openecomp.nodes.ForwardingPath";


  @Before
  public void setUpForwardingPath() {
    service = initForwardPath();
    List<ComponentInstance> componentInstances = new ArrayList<>();
    componentInstances.add(generateComponentInstance(NODE_NAME_1, NODE_ID_1));
    componentInstances.add(generateComponentInstance(NODE_NAME_2, NODE_ID_2));
    service.setComponentInstances(componentInstances);
  }

  private ComponentInstance generateComponentInstance(String name, String uuid) {
    ComponentInstance componentInstance = new ComponentInstance();
    componentInstance.setName(name);
    componentInstance.setUniqueId(uuid);
    componentInstance.setComponentUid(uuid);
    Map<String, List<CapabilityDefinition>> capabiltiesMap = new HashMap<>();
    CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
    capabilityDefinition.setMinOccurrences(Integer.toString(1));
    capabilityDefinition.setMaxOccurrences(Integer.toString(100));
    capabilityDefinition.setType(ForwardingPathToscaUtil.FORWARDS_TO_TOSCA_NAME);
    capabilityDefinition.setName(name);
    capabilityDefinition.setUniqueId(name);
    capabilityDefinition.setPath(Arrays.asList("Just", "something", "to", "read"));
    capabiltiesMap.put(capabilityDefinition.getName(),
        Collections.singletonList(capabilityDefinition));
    componentInstance.setCapabilities(capabiltiesMap);
    Resource resource = new Resource();
    resource.setToscaResourceName("test");
    originComponents.put(uuid, resource);
    return componentInstance;
  }


  @Test
  public void singleElementPath() {
    try {
      createPathSingleElement();
      Map<String, ToscaNodeTemplate> nodeTemplatesRes = new HashMap<>();

      Map<String, ToscaNodeTemplate> expectedNodeTemplatesRes = new HashMap<>();
      ToscaNodeTemplate pathEntry = new ToscaNodeTemplate();
      pathEntry.setType(FORWARDING_PATH_TOSCA_TYPE);
      Map<String, Object> expectedProps = new HashMap<>();
      expectedProps.put(PORTS_RANGE, Collections.singletonList(PATH_1_PORTS));
      expectedProps.put(PROTOCOL, PATH_1_PROTOCOL);
      pathEntry.setProperties(expectedProps);
      List<Map<String, ToscaTemplateRequirement>> requirements = new ArrayList<>();
      ToscaTemplateRequirement firstEntryReq = new ToscaTemplateRequirement();
      ToscaTemplateRequirement secondEntryReq = new ToscaTemplateRequirement();

      firstEntryReq.setCapability("null." + NODE_NAME_1);
      secondEntryReq.setCapability("null." + NODE_NAME_2);

      firstEntryReq.setNode(NODE_NAME_1);
      secondEntryReq.setNode(NODE_NAME_2);

      firstEntryReq.setRelationship(FORWARDS_TO_TOSCA_NAME);
      secondEntryReq.setRelationship(FORWARDS_TO_TOSCA_NAME);

      Map<String, ToscaTemplateRequirement> entryMap1 = new HashMap<>();
      Map<String, ToscaTemplateRequirement> entryMap2 = new HashMap<>();

      entryMap1.put(FORWARDER, firstEntryReq);
      entryMap2.put(FORWARDER, secondEntryReq);

      requirements.add(entryMap1);
      requirements.add(entryMap2);

      pathEntry.setRequirements(requirements);
      expectedNodeTemplatesRes.put(PATH_1_NAME, pathEntry);
      addForwardingPaths(service, nodeTemplatesRes, capabiltyRequirementConvertor, originComponents,
          toscaOperationFacade);

      assertEquals(2, nodeTemplatesRes.get(PATH_1_NAME).getRequirements().size());
      compareToscaPathEntry(expectedNodeTemplatesRes, nodeTemplatesRes);
    } catch (Exception e){
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  private void compareToscaPathEntry(Map<String, ToscaNodeTemplate> expectedNodeTemplatesRes,
                                     Map<String, ToscaNodeTemplate> nodeTemplatesRes) {
    final ToscaNodeTemplate expectedTemplate = expectedNodeTemplatesRes.get(PATH_1_NAME);
    final ToscaNodeTemplate actualTemplate = nodeTemplatesRes.get(PATH_1_NAME);
    assertEquals(expectedTemplate.getProperties(), actualTemplate.getProperties());
    final int size = expectedTemplate.getRequirements().size();
    assertEquals(size,
        actualTemplate.getRequirements().size());
    for (int i = 0; i < size; i++) {
      compareToscaRequirement(expectedTemplate, actualTemplate, i);
    }
  }

  private void compareToscaRequirement(ToscaNodeTemplate expectedTemplate,
                                       ToscaNodeTemplate actualTemplate,
                                       int i) {
    final ToscaTemplateRequirement actualRequirement =
        actualTemplate.getRequirements().get(i).get(FORWARDER);
    final ToscaTemplateRequirement expectedToscaRequirement = expectedTemplate
        .getRequirements()
        .get(i).get(FORWARDER);
    assertEquals(actualRequirement.getCapability(), expectedToscaRequirement.getCapability());
    assertEquals(actualRequirement.getNode(), expectedToscaRequirement.getNode());
  }

  private void createPathSingleElement() {
    ForwardingPathElementDataDefinition element1 = initElement(NODE_NAME_1, NODE_NAME_2, NODE_NAME_1,
        NODE_NAME_2);

    ListDataDefinition<ForwardingPathElementDataDefinition> list = new ListDataDefinition<>();
    list.add(element1);


    ForwardingPathDataDefinition path = new ForwardingPathDataDefinition();
    path.setDescription(PATH_1_DESC);
    path.setProtocol(PATH_1_PROTOCOL);
    path.setDestinationPortNumber(PATH_1_PORTS);
    path.setToscaResourceName(FORWARDING_PATH_TOSCA_TYPE);
    path.setPathElements(list);
    path.setName(PATH_1_NAME);

    Map<String, ForwardingPathDataDefinition> paths = new HashMap<>();
    paths.put(PATH_1_NAME, path);

    service.setForwardingPaths(paths);
  }

  private ForwardingPathElementDataDefinition initElement(String fromId, String toId, String
      fromCP, String toCP) {
    ForwardingPathElementDataDefinition element = new ForwardingPathElementDataDefinition();
    element.setFromCP(fromCP);
    element.setFromNode(fromId);
    element.setToCP(toCP);
    element.setToNode(toId);
    return element;
  }
}