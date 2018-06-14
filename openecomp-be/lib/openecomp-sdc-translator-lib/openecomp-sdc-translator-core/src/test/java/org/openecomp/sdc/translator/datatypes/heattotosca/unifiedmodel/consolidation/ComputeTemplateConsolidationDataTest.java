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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;

public class ComputeTemplateConsolidationDataTest {

    private static final String COMPUTE_NODE_TEMPLATE_ID_1 = "computeNodeTemplateId1";
    private static final String COMPUTE_NODE_TEMPLATE_ID_2 = "computeNodeTemplateId2";
    private static final String REQUIREMENT_ID_1 = "requirementId1";
    private static final String REQUIREMENT_ID_2 = "requirementId2";

    private static final String PORT_TYPE_1 = "portType1";
    private static final String PORT_TYPE_2 = "portType2";
    private static final String PORT_NODE_TEMPLATE_ID_1 = "portNodeTemplateId1";
    private static final String PORT_NODE_TEMPLATE_ID_2 = "portNodeTemplateId2";


    private final ComputeTemplateConsolidationData consolidationData
            = new ComputeTemplateConsolidationData();

    @Test
    public void testAddPort_SamePortType() {
        Map<String, String[]> expectedPorts = new HashMap<>();
        expectedPorts.put(PORT_TYPE_1, new String[]{PORT_NODE_TEMPLATE_ID_1});
        addAndCheckPorts(PORT_TYPE_1, PORT_NODE_TEMPLATE_ID_1, expectedPorts);

        expectedPorts.put(PORT_TYPE_1, new String[]{PORT_NODE_TEMPLATE_ID_1, PORT_NODE_TEMPLATE_ID_2});
        addAndCheckPorts(PORT_TYPE_1, PORT_NODE_TEMPLATE_ID_2, expectedPorts);
    }

    @Test
    public void testAddPort_DiffPortType() {
        Map<String, String[]> expectedPorts = new HashMap<>();
        expectedPorts.put(PORT_TYPE_1, new String[]{PORT_NODE_TEMPLATE_ID_1});
        addAndCheckPorts(PORT_TYPE_1, PORT_NODE_TEMPLATE_ID_1, expectedPorts);

        expectedPorts.put(PORT_TYPE_2, new String[]{PORT_NODE_TEMPLATE_ID_2});
        addAndCheckPorts(PORT_TYPE_2, PORT_NODE_TEMPLATE_ID_2, expectedPorts);
    }

    @Test
    public void testAddVolume_SameComputeNode() {
        Map<String, String[]> expectedVolumes = new HashMap<>();
        expectedVolumes.put(COMPUTE_NODE_TEMPLATE_ID_1, new String[]{REQUIREMENT_ID_1});
        addAndCheckVolume(REQUIREMENT_ID_1, COMPUTE_NODE_TEMPLATE_ID_1, expectedVolumes);

        expectedVolumes.put(COMPUTE_NODE_TEMPLATE_ID_1, new String[]{REQUIREMENT_ID_1, REQUIREMENT_ID_2});
        addAndCheckVolume(REQUIREMENT_ID_2, COMPUTE_NODE_TEMPLATE_ID_1, expectedVolumes);
    }

    @Test
    public void testAddVolume_DiffComputeNode() {
        Map<String, String[]> expectedVolumes = new HashMap<>();
        expectedVolumes.put(COMPUTE_NODE_TEMPLATE_ID_1, new String[]{REQUIREMENT_ID_1});
        addAndCheckVolume(REQUIREMENT_ID_1, COMPUTE_NODE_TEMPLATE_ID_1, expectedVolumes);

        expectedVolumes.put(COMPUTE_NODE_TEMPLATE_ID_2, new String[]{REQUIREMENT_ID_2});
        addAndCheckVolume(REQUIREMENT_ID_2, COMPUTE_NODE_TEMPLATE_ID_2, expectedVolumes);
    }

    private void addAndCheckPorts(String portType, String portNodeTemplateId,
            Map<String, String[]> expectedPorts) {
        consolidationData.addPort(portType, portNodeTemplateId);
        checkPorts(consolidationData.getPorts(), expectedPorts);
    }

    private void checkPorts(Map<String, List<String>> actualAllPorts,
            Map<String, String[]> expectedAllPorts) {
        Assert.assertNotNull(actualAllPorts);
        expectedAllPorts.keySet().forEach(expectedPortType -> {
            Assert.assertTrue(actualAllPorts.containsKey(expectedPortType));
            Assert.assertEquals(expectedAllPorts.size(), actualAllPorts.size());
            checkPortsPerType(actualAllPorts, expectedAllPorts, expectedPortType);
        });
    }

    private void checkPortsPerType(Map<String, List<String>>  actualAllPorts,  Map<String, String[]> expectedAllPorts,
                                   String expectedPortType) {
        List<String> actualPorts = actualAllPorts.get(expectedPortType);
        List<String> expectedPortList = Arrays.asList(expectedAllPorts.get(expectedPortType));
        Assert.assertEquals(expectedPortList.size(), actualPorts.size());
        actualPorts.forEach(actualPort ->
                Assert.assertTrue(expectedPortList.contains(actualPort)));
    }

    private void addAndCheckVolume(String requirementId, String computeNodeTemplateId,
                                          Map<String, String[]> expectedVolumes) {
        RequirementAssignment requirementAssignment1 = createRequirement(computeNodeTemplateId);
        consolidationData.addVolume(requirementId, requirementAssignment1);
        checkVolumes(consolidationData.getVolumes(), expectedVolumes);
    }

    private void checkVolumes(Map<String, List<RequirementAssignmentData>> actualVolumes,
            Map<String, String[]> expectedVolumes) {
        Assert.assertNotNull(actualVolumes);
        expectedVolumes.keySet().forEach(nodeTemplateId -> {
            Assert.assertTrue(actualVolumes.containsKey(nodeTemplateId));
            Assert.assertEquals(expectedVolumes.size(), actualVolumes.size());
            checkVolumesPerType(actualVolumes, expectedVolumes, nodeTemplateId);
        });
    }

    private void checkVolumesPerType(Map<String, List<RequirementAssignmentData>>
            actualVolumes,  Map<String, String[]> expectedVolumes, String nodeTemplateId) {
        List<RequirementAssignmentData> actualRequirementAssignmentData = actualVolumes.get(nodeTemplateId);
        List<String> requirementIds = Arrays.asList(expectedVolumes.get(nodeTemplateId));
        Assert.assertEquals(requirementIds.size(), actualRequirementAssignmentData.size());
        actualRequirementAssignmentData.forEach(actualRequirementAssignment ->
                Assert.assertTrue(requirementIds.contains(actualRequirementAssignment.getRequirementId())));
    }

    private RequirementAssignment createRequirement(String nodeTemplateId) {
        RequirementAssignment requirementAssignment = new RequirementAssignment();
        requirementAssignment.setNode(nodeTemplateId);
        return requirementAssignment;
    }
}
