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

package org.openecomp.sdc.translator.services.heattotosca.unifiedmodel.commands;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands.UnifiedSubstitutionNodeTemplateIdGenerator;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands.impl.PortNewNodeTemplateIdGenerator;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.to.UnifiedCompositionTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;

import java.util.*;

import static org.openecomp.sdc.translator.TestUtils.createInitServiceTemplate;

public class PortNewNodeTemplateIdGeneratorTest {

  private static final String SERVER_PCM = "server_pcm";
  private static final String PCM_PORT_0 = "pcm_port_0";
  private static final String PCM_PORT_1 = "pcm_port_1";

  private static UnifiedSubstitutionNodeTemplateIdGenerator unifiedSubstitutionNodeTemplateIdGenerator;

  @BeforeClass
  public static void setItUp(){
    unifiedSubstitutionNodeTemplateIdGenerator = new PortNewNodeTemplateIdGenerator();
  }

  @Test
  public void testGenerateNewPortNodeTemplateId() {
    UnifiedCompositionTo unifiedCompositionTo = new UnifiedCompositionTo(getTestPortServiceTemplate(), null,
        getUnifiedCompositionDataListWithOnePort(), null, null);
    Optional<String>
        nodeTemplateId = unifiedSubstitutionNodeTemplateIdGenerator.generate(unifiedCompositionTo, PCM_PORT_0);
    if (nodeTemplateId.isPresent()) {
      Assert.assertEquals(nodeTemplateId.get(), "pcm_server_pcm_port");
    } else {
      Assert.fail();
    }
  }

  @Test
  public void testGeneratePortIdNotBoundToServer() {
    UnifiedCompositionTo unifiedCompositionTo = new UnifiedCompositionTo(getTestInitPortServiceTemplate(), null,
        getInitUnifiedCompositionDataList(), null, null);
    Optional<String>
        nodeTemplateId = unifiedSubstitutionNodeTemplateIdGenerator.generate(unifiedCompositionTo, PCM_PORT_0);
    Assert.assertEquals(false, nodeTemplateId.isPresent());
  }

  @Test
  public void testGeneratePortIdMultiplePortsSameTypeToOneServer() {
    UnifiedCompositionTo unifiedCompositionTo =
        new UnifiedCompositionTo(getTestPortServiceTemplateWithTwoPortsOfSameType(), null,
            getUnifiedCompositionDataListWithTwoPortsOfSameType(), null, null);
    Optional<String>
        nodeTemplateId = unifiedSubstitutionNodeTemplateIdGenerator.generate(unifiedCompositionTo, PCM_PORT_0);
    if (nodeTemplateId.isPresent()) {
      Assert.assertEquals(nodeTemplateId.get(), "pcm_server_pcm_port_0");
    } else {
      Assert.fail();
    }

    nodeTemplateId = unifiedSubstitutionNodeTemplateIdGenerator.generate(unifiedCompositionTo, PCM_PORT_1);
    if (nodeTemplateId.isPresent()) {
      Assert.assertEquals(nodeTemplateId.get(), "pcm_server_pcm_port_1");
    } else {
      Assert.fail();
    }
}

  @Test
  public void testGenerateInvalidOriginalNodeTemplateId() {
    UnifiedCompositionTo unifiedCompositionTo = new UnifiedCompositionTo(getTestPortServiceTemplate(), null,
        getUnifiedCompositionDataListWithOnePort(), null, null);
    Optional<String>
        nodeTemplateId = unifiedSubstitutionNodeTemplateIdGenerator.generate(unifiedCompositionTo,
        PCM_PORT_0 + "_Invalid");
    Assert.assertEquals(nodeTemplateId.isPresent(), false);
  }

  private static ServiceTemplate getTestInitPortServiceTemplate() {
    ServiceTemplate serviceTemplate = createInitServiceTemplate();
    TopologyTemplate topologyTemplate = serviceTemplate.getTopology_template();
    NodeTemplate nodeTemplate = new NodeTemplate();
    nodeTemplate.setType("org.openecomp.resource.vfc.nodes.heat.pcm_server");
    Map<String, NodeTemplate> nodeTemplateMap = new HashMap<>();
    nodeTemplateMap.put(SERVER_PCM, nodeTemplate);
    NodeTemplate portNodeTemplate = new NodeTemplate();
    portNodeTemplate.setType(ToscaNodeType.NEUTRON_PORT);
    nodeTemplateMap.put(PCM_PORT_0, portNodeTemplate);
    topologyTemplate.setNode_templates(nodeTemplateMap);
    return serviceTemplate;
  }

  private static ServiceTemplate getTestPortServiceTemplate() {
    ServiceTemplate serviceTemplate = getTestInitPortServiceTemplate();
    NodeTemplate portNodeTemplate = serviceTemplate.getTopology_template().getNode_templates().get(PCM_PORT_0);
    Map<String, RequirementAssignment> portBindingToServer = new HashMap<>();
    RequirementAssignment binding = new RequirementAssignment();
    binding.setRelationship(ToscaRelationshipType.NATIVE_NETWORK_BINDS_TO);
    binding.setCapability(ToscaCapabilityType.NATIVE_NETWORK_BINDABLE);
    binding.setNode(SERVER_PCM);
    portBindingToServer.put(ToscaConstants.BINDING_REQUIREMENT_ID, binding);
    List<Map<String, RequirementAssignment>> requirements = new ArrayList<>(1);
    requirements.add(portBindingToServer);
    portNodeTemplate.setRequirements(requirements);
    return serviceTemplate;
  }

  private static ServiceTemplate getTestPortServiceTemplateWithTwoPortsOfSameType() {
    ServiceTemplate serviceTemplate = getTestInitPortServiceTemplate();
    Map<String, NodeTemplate> nodeTemplateMap = serviceTemplate.getTopology_template().getNode_templates();
    NodeTemplate anotherPortNodeTemplate = new NodeTemplate();
    anotherPortNodeTemplate.setType(ToscaNodeType.NEUTRON_PORT);
    nodeTemplateMap.put(PCM_PORT_1, anotherPortNodeTemplate);

    NodeTemplate portNodeTemplate = serviceTemplate.getTopology_template().getNode_templates().get(PCM_PORT_0);
    addBindingToServer(portNodeTemplate);
    addBindingToServer(anotherPortNodeTemplate);
    return serviceTemplate;
  }

  private List<UnifiedCompositionData> getInitUnifiedCompositionDataList() {
    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>(1);
    UnifiedCompositionData unifiedCompositionData = new UnifiedCompositionData();
    ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
    computeTemplateConsolidationData.setNodeTemplateId(SERVER_PCM);
    unifiedCompositionData.setComputeTemplateConsolidationData(computeTemplateConsolidationData);
    unifiedCompositionDataList.add(unifiedCompositionData);
    return unifiedCompositionDataList;
  }

  private List<UnifiedCompositionData> getUnifiedCompositionDataListWithOnePort() {
    List<UnifiedCompositionData> unifiedCompositionDataList = getInitUnifiedCompositionDataList();
    ComputeTemplateConsolidationData computeTemplateConsolidationData = unifiedCompositionDataList.get(0)
        .getComputeTemplateConsolidationData();
    List<String> ports = new ArrayList<>(1);
    ports.add(PCM_PORT_0);
    Map<String, List<String>> portMap = new HashMap<>();
    portMap.put("pcm_port", ports);
    computeTemplateConsolidationData.setPorts(portMap);
    return unifiedCompositionDataList;
  }

  private List<UnifiedCompositionData> getUnifiedCompositionDataListWithTwoPortsOfSameType() {
    List<UnifiedCompositionData> unifiedCompositionDataList = getUnifiedCompositionDataListWithOnePort();
    ComputeTemplateConsolidationData computeTemplateConsolidationData = unifiedCompositionDataList.get(0)
        .getComputeTemplateConsolidationData();
    List<String> ports = new ArrayList<>(2);
    ports.add(PCM_PORT_0);
    ports.add(PCM_PORT_1);
    Map<String, List<String>> portMap = new HashMap<>();
    portMap.put("pcm_port", ports);
    computeTemplateConsolidationData.setPorts(portMap);
    return unifiedCompositionDataList;
  }

  private static void addBindingToServer(NodeTemplate portNodeTemplate) {
    Map<String, RequirementAssignment> portBindingToServer = new HashMap<>();
    RequirementAssignment binding = new RequirementAssignment();
    binding.setRelationship(ToscaRelationshipType.NATIVE_NETWORK_BINDS_TO);
    binding.setCapability(ToscaCapabilityType.NATIVE_NETWORK_BINDABLE);
    binding.setNode(SERVER_PCM);
    portBindingToServer.put(ToscaConstants.BINDING_REQUIREMENT_ID, binding);
    List<Map<String, RequirementAssignment>> requirements = new ArrayList<>(1);
    requirements.add(portBindingToServer);
    portNodeTemplate.setRequirements(requirements);
  }
}
