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

package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;


public class ComputeConsolidationDataHandlerTest {

    private static final String COMPUTE_NODE_TEMPLATE_ID_1 = "computeNodeTemplateId";
    private static final String COMPUTE_NODE_TEMPLATE_ID_2 = "computeNodeTemplateId2";
    private static final String COMPUTE_NODE_TYPE_1 = "computeNodeType1";

    private static final String PORT_NODE_TYPE_1 = "portType1";
    private static final String PORT_NODE_TEMPLATE_ID_1 = "portNodeTemplateId1";

    private static final String GROUP_ID = "groupId";
    private static final String REQUIREMENT_ID = "requirementId";
    private static final String SERVICE_FILE_NAME_PREFIX = "Main";
    private static final String SERVICE_FILE_NAME = SERVICE_FILE_NAME_PREFIX + "ServiceTemplate.yaml";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        consolidationDataHandler = new ComputeConsolidationDataHandler(computeConsolidationData);
    }

    @Mock
    private static ComputeConsolidationData computeConsolidationData;
    @Mock
    private static TranslateTo translateTo;
    @Mock
    private static ServiceTemplate serviceTemplate;
    @Mock
    private static ComputeTemplateConsolidationData consolidationData;
    @Mock
    private static HeatOrchestrationTemplate heatOrchestrationTemplate;

    private static ComputeConsolidationDataHandler consolidationDataHandler;
    private static final RequirementAssignment requirementAssignment = new RequirementAssignment();

    @Test
    public void testAddNodesConnectedOut() {
        mockEntities(COMPUTE_NODE_TEMPLATE_ID_1);
        consolidationDataHandler.addNodesConnectedOut(
                translateTo, COMPUTE_NODE_TEMPLATE_ID_1, REQUIREMENT_ID, requirementAssignment);
        Mockito.verify(computeConsolidationData).addComputeTemplateConsolidationData(
                SERVICE_FILE_NAME, COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        Mockito.verify(consolidationData).addNodesConnectedOut(
                COMPUTE_NODE_TEMPLATE_ID_1, REQUIREMENT_ID, requirementAssignment);
    }

    @Test
    public void testAddNodesConnectedOut_consolidationDataNotExist() {
        mockEntities_NullConsolidationData(COMPUTE_NODE_TEMPLATE_ID_1);
        consolidationDataHandler.addNodesConnectedOut(
                translateTo, COMPUTE_NODE_TEMPLATE_ID_1, REQUIREMENT_ID, requirementAssignment);
        Mockito.verify(computeConsolidationData).addComputeTemplateConsolidationData(
                SERVICE_FILE_NAME, COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        Mockito.verify(consolidationData, Mockito.times(0)).addNodesConnectedOut(
                Mockito.any(),  Mockito.any(),  Mockito.any());
    }

    @Test
    public void testAddNodesConnectedIn() {
        String sourceNodeTemplateId = COMPUTE_NODE_TEMPLATE_ID_1;
        String dependentNodeTemplateId = COMPUTE_NODE_TEMPLATE_ID_2;
        mockEntities(dependentNodeTemplateId);
        consolidationDataHandler.addNodesConnectedIn(translateTo, sourceNodeTemplateId,
                dependentNodeTemplateId, "targetResourceId", REQUIREMENT_ID, requirementAssignment);
        Mockito.verify(computeConsolidationData).addComputeTemplateConsolidationData(
                SERVICE_FILE_NAME, COMPUTE_NODE_TYPE_1, dependentNodeTemplateId);
        Mockito.verify(consolidationData).addNodesConnectedIn(
                sourceNodeTemplateId, REQUIREMENT_ID, requirementAssignment);
    }

    @Test
    public void testAddNodesConnectedIn_consolidationDataNotExist() {
        String dependentNodeTemplateId = COMPUTE_NODE_TEMPLATE_ID_2;
        mockEntities_NullConsolidationData(dependentNodeTemplateId);
        consolidationDataHandler.addNodesConnectedIn(translateTo, COMPUTE_NODE_TEMPLATE_ID_1,
                dependentNodeTemplateId, "targetResourceId", REQUIREMENT_ID, requirementAssignment);
        Mockito.verify(computeConsolidationData).addComputeTemplateConsolidationData(
                SERVICE_FILE_NAME, COMPUTE_NODE_TYPE_1, dependentNodeTemplateId);
        Mockito.verify(consolidationData, Mockito.times(0)).addNodesConnectedIn(
                Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testRemoveParamNameFromAttrFuncList() {
        mockEntities(COMPUTE_NODE_TEMPLATE_ID_2);
        consolidationDataHandler.removeParamNameFromAttrFuncList(serviceTemplate, heatOrchestrationTemplate,
                        "paramName", COMPUTE_NODE_TEMPLATE_ID_1, COMPUTE_NODE_TEMPLATE_ID_2);
        Mockito.verify(computeConsolidationData).addComputeTemplateConsolidationData(
                SERVICE_FILE_NAME, COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_2);
        Mockito.verify(consolidationData).removeParamNameFromAttrFuncList("paramName");
    }

    @Test
    public void testRemoveParamNameFromAttrFuncList_consolidationDataNotExist() {
        mockEntities_NullConsolidationData(COMPUTE_NODE_TEMPLATE_ID_2);
        consolidationDataHandler.removeParamNameFromAttrFuncList(serviceTemplate, heatOrchestrationTemplate,
                "paramName", COMPUTE_NODE_TEMPLATE_ID_1, COMPUTE_NODE_TEMPLATE_ID_2);
        Mockito.verify(computeConsolidationData).addComputeTemplateConsolidationData(
                SERVICE_FILE_NAME, COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_2);
        Mockito.verify(consolidationData, Mockito.times(0))
                .removeParamNameFromAttrFuncList(Mockito.any());
    }

    @Test
    public void testAddConsolidationData() {
        consolidationDataHandler.addConsolidationData(SERVICE_FILE_NAME,
                COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        Mockito.verify(computeConsolidationData).addComputeTemplateConsolidationData(
                SERVICE_FILE_NAME, COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
    }

    @Test
    public void testAddPortToConsolidationData() {
        mockEntities(COMPUTE_NODE_TEMPLATE_ID_1);
        consolidationDataHandler.addPortToConsolidationData(translateTo, COMPUTE_NODE_TYPE_1,
                COMPUTE_NODE_TEMPLATE_ID_1, PORT_NODE_TYPE_1, PORT_NODE_TEMPLATE_ID_1);
        Mockito.verify(computeConsolidationData).addComputeTemplateConsolidationData(
                SERVICE_FILE_NAME, COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        Mockito.verify(consolidationData).addPort(PORT_NODE_TYPE_1, PORT_NODE_TEMPLATE_ID_1);
    }

    @Test
    public void testAddVolumeToConsolidationData() {
        mockEntities(COMPUTE_NODE_TEMPLATE_ID_1);
        consolidationDataHandler.addVolumeToConsolidationData(
                translateTo, COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1,
                        REQUIREMENT_ID, requirementAssignment);
        Mockito.verify(computeConsolidationData).addComputeTemplateConsolidationData(
                SERVICE_FILE_NAME, COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        Mockito.verify(consolidationData).addVolume(REQUIREMENT_ID, requirementAssignment);
    }


    @Test
    public void testAddGroupIdToConsolidationData() {
        mockEntities(COMPUTE_NODE_TEMPLATE_ID_1);
        consolidationDataHandler.addGroupIdToConsolidationData(
                translateTo, COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1, GROUP_ID);
        Mockito.verify(computeConsolidationData).addComputeTemplateConsolidationData(
                SERVICE_FILE_NAME, COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        Mockito.verify(consolidationData).addGroupId(GROUP_ID);
    }

    private void mockEntities(String nodeTemplateId) {
        mockServiceTemplate(nodeTemplateId);
        mockTranslateTo(nodeTemplateId);
        mockComputeConsolidationData();
    }

    private void mockEntities_NullConsolidationData(String nodeTemplateId) {
        mockServiceTemplate(nodeTemplateId);
        mockTranslateTo(nodeTemplateId);
        mockNullConsolidationData();
    }

    private void mockServiceTemplate(String nodeTemplateId) {
        TopologyTemplate topologyTemplate = createTopologyTemplate(nodeTemplateId);
        Mockito.when(serviceTemplate.getTopology_template()).thenReturn(topologyTemplate);

        Map<String, String> metadata = new HashMap<>();
        metadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, SERVICE_FILE_NAME_PREFIX);
        Mockito.when(serviceTemplate.getMetadata()).thenReturn(metadata);
    }

    private void mockTranslateTo(String nodeTemplateId) {
        Mockito.when(translateTo.getTranslatedId()).thenReturn(nodeTemplateId);
        Mockito.when(translateTo.getServiceTemplate()).thenReturn(serviceTemplate);
    }

    private void mockComputeConsolidationData() {
        Mockito.when(computeConsolidationData
            .addComputeTemplateConsolidationData(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString())).thenReturn(consolidationData);
    }

    private void mockNullConsolidationData() {
        Mockito.when(computeConsolidationData
                             .addComputeTemplateConsolidationData(Mockito.anyString(), Mockito.anyString(),
                                     Mockito.anyString())).thenReturn(null);
    }

    private  TopologyTemplate createTopologyTemplate(String nodeTemplateId) {
        TopologyTemplate topologyTemplate = new TopologyTemplate();
        Map<String, NodeTemplate> nodeTemplates = new HashMap<>();
        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setType(COMPUTE_NODE_TYPE_1);
        nodeTemplates.put(nodeTemplateId, nodeTemplate);
        topologyTemplate.setNode_templates(nodeTemplates);
        return topologyTemplate;
    }

}
