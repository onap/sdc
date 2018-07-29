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
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.translator.TestUtils;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands.UnifiedSubstitutionNodeTemplateIdGenerator;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands.impl.SubInterfaceNewNodeTemplateIdGenerator;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.to.UnifiedCompositionTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class SubInterfaceNewNodeTemplateIdGeneratorTest {

  private static final String VDBE_UNTR_1_PORT = "vdbe_untr_1_port";
  private static final String VDBE_UNTR_1_SUBPORTS = "vdbe_untr_1_subports";
  private static final String MAIN_SERVICE_TEMPLATE_YAML = "MainServiceTemplate.yaml";
  private static final String VDBE_UNTR_2_SUBPORTS = "vdbe_untr_2_subports";
  private static final String SUBINTERFACE_TYPE_NESTED =
      "org.openecomp.resource.abstract.nodes.heat.subinterface.nested";

  private static UnifiedSubstitutionNodeTemplateIdGenerator unifiedSubstitutionNodeTemplateIdGenerator;

  @BeforeClass
  public static void setItUp(){
    unifiedSubstitutionNodeTemplateIdGenerator = new SubInterfaceNewNodeTemplateIdGenerator();
  }

  @Test
  public void testGenerateNewSubInterfaceNodeTemplateId() throws IOException, URISyntaxException {
    UnifiedCompositionTo unifiedCompositionTo = new UnifiedCompositionTo(getTestInitSubInterfaceServiceTemplate(), null,
        getUnifiedCompositionDataListWithOnePortAndSubInterface(), getContext(false), null);
    Optional<String>
        nodeTemplateId = unifiedSubstitutionNodeTemplateIdGenerator.generate(unifiedCompositionTo, VDBE_UNTR_1_SUBPORTS);
    if (nodeTemplateId.isPresent()) {
      Assert.assertEquals(nodeTemplateId.get(), "vdbe_vdbe_untr_1_port_nested");
    } else {
      Assert.fail();
    }
  }

  @Test
  public void testGeneratePortIdMultipleSubInterfacesOfSameTypeToOnePort() throws IOException, URISyntaxException  {
    UnifiedCompositionTo unifiedCompositionTo =
        new UnifiedCompositionTo(getTestSubInterfaceServiceTemplateMultipleVlan(), null,
            getUnifiedCompositionDataListWithTwoSubInterfacesOfSameType(), getContext(true), null);
    Optional<String>
        nodeTemplateId = unifiedSubstitutionNodeTemplateIdGenerator.generate(unifiedCompositionTo, VDBE_UNTR_1_SUBPORTS);
    if (nodeTemplateId.isPresent()) {
      Assert.assertEquals(nodeTemplateId.get(), "vdbe_vdbe_untr_1_port_vdbe_untr_1_subports");
    } else {
      Assert.fail();
    }

    nodeTemplateId = unifiedSubstitutionNodeTemplateIdGenerator.generate(unifiedCompositionTo, VDBE_UNTR_2_SUBPORTS);
    if (nodeTemplateId.isPresent()) {
      Assert.assertEquals(nodeTemplateId.get(), "vdbe_vdbe_untr_1_port_vdbe_untr_2_subports");
    } else {
      Assert.fail();
    }
  }

  @Test
  public void testGenerateInvalidOriginalNodeTemplateId() throws IOException, URISyntaxException  {
    UnifiedCompositionTo unifiedCompositionTo = new UnifiedCompositionTo(getTestInitSubInterfaceServiceTemplate(), null,
        getUnifiedCompositionDataListWithOnePortAndSubInterface(), getContext(false), null);
    Optional<String>
        nodeTemplateId = unifiedSubstitutionNodeTemplateIdGenerator.generate(unifiedCompositionTo,
        VDBE_UNTR_1_SUBPORTS + "_Invalid");
    Assert.assertEquals(nodeTemplateId.isPresent(), false);
  }

  private ServiceTemplate getTestInitSubInterfaceServiceTemplate() throws IOException, URISyntaxException  {
    ToscaServiceModel serviceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/unifiedComposition/commands/newnodetemplateidgenerator/oneportsubinterfacetype",
            null, null);
    Optional<ServiceTemplate> serviceTemplate = serviceModel.getServiceTemplate("MainServiceTemplate.yaml");
    return serviceTemplate.get();
  }

  private ServiceTemplate getTestSubInterfaceServiceTemplateMultipleVlan() throws IOException, URISyntaxException  {
    ToscaServiceModel serviceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/unifiedComposition/commands/newnodetemplateidgenerator/multiplevlansametype",
            null, null);
    Optional<ServiceTemplate> serviceTemplate = serviceModel.getServiceTemplate("MainServiceTemplate.yaml");
    return serviceTemplate.get();
  }

  private List<UnifiedCompositionData> getInitUnifiedCompositionDataList() {
    List<UnifiedCompositionData> unifiedCompositionDataList = new ArrayList<>(1);
    UnifiedCompositionData unifiedCompositionData = new UnifiedCompositionData();
    ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
    computeTemplateConsolidationData.setNodeTemplateId("vdbe_node_1");
    unifiedCompositionData.setComputeTemplateConsolidationData(computeTemplateConsolidationData);
    unifiedCompositionDataList.add(unifiedCompositionData);
    return unifiedCompositionDataList;
  }

  private List<UnifiedCompositionData> getUnifiedCompositionDataListWithOnePortAndSubInterface() {
    List<UnifiedCompositionData> unifiedCompositionDataList = getInitUnifiedCompositionDataList();
    UnifiedCompositionData unifiedCompositionData = unifiedCompositionDataList.get(0);
    ComputeTemplateConsolidationData computeTemplateConsolidationData = unifiedCompositionData
        .getComputeTemplateConsolidationData();
    List<String> ports = new ArrayList<>(1);
    ports.add(VDBE_UNTR_1_PORT);
    Map<String, List<String>> portMap = new HashMap<>();
    portMap.put(VDBE_UNTR_1_PORT, ports);
    computeTemplateConsolidationData.setPorts(portMap);


    PortTemplateConsolidationData portTemplateConsolidationData = new PortTemplateConsolidationData();
    portTemplateConsolidationData.setNodeTemplateId(VDBE_UNTR_1_PORT);
    SubInterfaceTemplateConsolidationData subInterfaceTemplateConsolidationData = new
        SubInterfaceTemplateConsolidationData();
    subInterfaceTemplateConsolidationData.setNodeTemplateId(VDBE_UNTR_1_SUBPORTS);
    subInterfaceTemplateConsolidationData.setParentPortNodeTemplateId(VDBE_UNTR_1_PORT);
    List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList = new ArrayList<>(1);
    subInterfaceTemplateConsolidationDataList.add(subInterfaceTemplateConsolidationData);
    portTemplateConsolidationData.addSubInterfaceConsolidationData(
        SUBINTERFACE_TYPE_NESTED,
        subInterfaceTemplateConsolidationData);

    List<PortTemplateConsolidationData> portTemplateConsolidationDataList = new ArrayList<>(1);
    portTemplateConsolidationDataList.add(portTemplateConsolidationData);
    unifiedCompositionData.setPortTemplateConsolidationDataList(portTemplateConsolidationDataList);
    unifiedCompositionData.setSubInterfaceTemplateConsolidationDataList(subInterfaceTemplateConsolidationDataList);


    return unifiedCompositionDataList;
  }

  private List<UnifiedCompositionData> getUnifiedCompositionDataListWithTwoSubInterfacesOfSameType() {
    List<UnifiedCompositionData> unifiedCompositionDataList = getUnifiedCompositionDataListWithOnePortAndSubInterface();
    List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList = unifiedCompositionDataList
        .get(0).getSubInterfaceTemplateConsolidationDataList();
    SubInterfaceTemplateConsolidationData anotherSubInterfaceData = new SubInterfaceTemplateConsolidationData();
    anotherSubInterfaceData.setNodeTemplateId(VDBE_UNTR_2_SUBPORTS);
    anotherSubInterfaceData.setParentPortNodeTemplateId(VDBE_UNTR_1_PORT);
    subInterfaceTemplateConsolidationDataList.add(anotherSubInterfaceData);
    unifiedCompositionDataList.get(0).getPortTemplateConsolidationDataList().get(0).addSubInterfaceConsolidationData
        (SUBINTERFACE_TYPE_NESTED, anotherSubInterfaceData);
    return unifiedCompositionDataList;
  }

  private TranslationContext getContext(boolean isMultipleSubInterfaceTest) {
    FilePortConsolidationData filePortConsolidationData = new FilePortConsolidationData();
    PortTemplateConsolidationData portTemplateConsolidationData = isMultipleSubInterfaceTest
        ? getUnifiedCompositionDataListWithTwoSubInterfacesOfSameType().get(0).getPortTemplateConsolidationDataList().get(0)
        : getUnifiedCompositionDataListWithOnePortAndSubInterface().get(0).getPortTemplateConsolidationDataList().get(0);
    filePortConsolidationData.setPortTemplateConsolidationData(portTemplateConsolidationData
        .getNodeTemplateId(), portTemplateConsolidationData);
    TranslationContext context = new TranslationContext();
    context.setConsolidationData(new ConsolidationData());
    context.getConsolidationData().getPortConsolidationData()
        .setFilePortConsolidationData(MAIN_SERVICE_TEMPLATE_YAML, filePortConsolidationData);
    context.getConsolidationData().getPortConsolidationData().getFilePortConsolidationData(MAIN_SERVICE_TEMPLATE_YAML);
    return context;
  }

}
