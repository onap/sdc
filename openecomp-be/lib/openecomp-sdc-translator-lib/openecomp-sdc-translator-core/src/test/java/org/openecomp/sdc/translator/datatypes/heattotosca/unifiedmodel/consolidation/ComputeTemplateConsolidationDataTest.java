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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private static final String PORT_NEUTRON = "port_neutron";
    private static final String PORT_NEUTRON_1 = "port_neutron_1";
    private static final String PORT_NEUTRON_2 = "port_neutron_2";
    private static final String PORT_NEUTRON_3 = "port_neutron_3";
    private ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();

    private Map<String, List<String>> mapToBeModified = new HashMap<String, List<String>>() {
        {
            put(PORT_NEUTRON, new ArrayList<>(Collections.singletonList(PORT_NEUTRON_3)));
        }
    };


    private final ComputeTemplateConsolidationData consolidationData = new ComputeTemplateConsolidationData();

    @Test
    public void testAddPort_SamePortType() {
        Map<String, String[]> expectedPorts = new HashMap<>();
        expectedPorts.put(PORT_TYPE_1, new String[] {PORT_NODE_TEMPLATE_ID_1});
        addAndCheckPorts(PORT_TYPE_1, PORT_NODE_TEMPLATE_ID_1, expectedPorts);

        expectedPorts.put(PORT_TYPE_1, new String[] {PORT_NODE_TEMPLATE_ID_1, PORT_NODE_TEMPLATE_ID_2});
        addAndCheckPorts(PORT_TYPE_1, PORT_NODE_TEMPLATE_ID_2, expectedPorts);
    }

    @Test
    public void testAddPort_DiffPortType() {
        Map<String, String[]> expectedPorts = new HashMap<>();
        expectedPorts.put(PORT_TYPE_1, new String[] {PORT_NODE_TEMPLATE_ID_1});
        addAndCheckPorts(PORT_TYPE_1, PORT_NODE_TEMPLATE_ID_1, expectedPorts);

        expectedPorts.put(PORT_TYPE_2, new String[] {PORT_NODE_TEMPLATE_ID_2});
        addAndCheckPorts(PORT_TYPE_2, PORT_NODE_TEMPLATE_ID_2, expectedPorts);
    }

    @Test
    public void testAddVolume_SameComputeNode() {
        Map<String, String[]> expectedVolumes = new HashMap<>();
        expectedVolumes.put(COMPUTE_NODE_TEMPLATE_ID_1, new String[] {REQUIREMENT_ID_1});
        addAndCheckVolume(REQUIREMENT_ID_1, COMPUTE_NODE_TEMPLATE_ID_1, expectedVolumes);

        expectedVolumes.put(COMPUTE_NODE_TEMPLATE_ID_1, new String[] {REQUIREMENT_ID_1, REQUIREMENT_ID_2});
        addAndCheckVolume(REQUIREMENT_ID_2, COMPUTE_NODE_TEMPLATE_ID_1, expectedVolumes);
    }

    @Test
    public void testAddVolume_DiffComputeNode() {
        Map<String, String[]> expectedVolumes = new HashMap<>();
        expectedVolumes.put(COMPUTE_NODE_TEMPLATE_ID_1, new String[] {REQUIREMENT_ID_1});
        addAndCheckVolume(REQUIREMENT_ID_1, COMPUTE_NODE_TEMPLATE_ID_1, expectedVolumes);

        expectedVolumes.put(COMPUTE_NODE_TEMPLATE_ID_2, new String[] {REQUIREMENT_ID_2});
        addAndCheckVolume(REQUIREMENT_ID_2, COMPUTE_NODE_TEMPLATE_ID_2, expectedVolumes);
    }

    private void addAndCheckPorts(String portType, String portNodeTemplateId, Map<String, String[]> expectedPorts) {
        consolidationData.addPort(portType, portNodeTemplateId);
        checkPorts(consolidationData.getPorts(), expectedPorts);
    }

    private void checkPorts(Map<String, List<String>> actualAllPorts, Map<String, String[]> expectedAllPorts) {
        Assert.assertNotNull(actualAllPorts);
        expectedAllPorts.keySet().forEach(expectedPortType -> {
            Assert.assertTrue(actualAllPorts.containsKey(expectedPortType));
            Assert.assertEquals(expectedAllPorts.size(), actualAllPorts.size());
            checkPortsPerType(actualAllPorts, expectedAllPorts, expectedPortType);
        });
    }

    private void checkPortsPerType(Map<String, List<String>> actualAllPorts, Map<String, String[]> expectedAllPorts,
            String expectedPortType) {
        List<String> actualPorts = actualAllPorts.get(expectedPortType);
        List<String> expectedPortList = Arrays.asList(expectedAllPorts.get(expectedPortType));
        Assert.assertEquals(expectedPortList.size(), actualPorts.size());
        actualPorts.forEach(actualPort -> Assert.assertTrue(expectedPortList.contains(actualPort)));
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

    private void checkVolumesPerType(Map<String, List<RequirementAssignmentData>> actualVolumes,
            Map<String, String[]> expectedVolumes, String nodeTemplateId) {
        List<RequirementAssignmentData> actualRequirementAssignmentData = actualVolumes.get(nodeTemplateId);
        List<String> requirementIds = Arrays.asList(expectedVolumes.get(nodeTemplateId));
        Assert.assertEquals(requirementIds.size(), actualRequirementAssignmentData.size());
        actualRequirementAssignmentData.forEach(actualRequirementAssignment -> Assert.assertTrue(
                requirementIds.contains(actualRequirementAssignment.getRequirementId())));
    }

    private RequirementAssignment createRequirement(String nodeTemplateId) {
        RequirementAssignment requirementAssignment = new RequirementAssignment();
        requirementAssignment.setNode(nodeTemplateId);
        return requirementAssignment;
    }

    @Test
    public void collectAllPortsOfEachTypeFromComputeKeyPresent() {
        Map<String, List<String>> portMap = new HashMap<>();
        portMap.put(PORT_NEUTRON, new ArrayList<>(Arrays.asList(PORT_NEUTRON_1, PORT_NEUTRON_2)));

        computeTemplateConsolidationData.setPorts(portMap);

        computeTemplateConsolidationData.collectAllPortsOfEachTypeFromCompute(mapToBeModified);

        Assert.assertTrue(mapToBeModified.size() == 1 && mapToBeModified.get(PORT_NEUTRON).size() == 3);
        Assert.assertTrue(mapToBeModified.get(PORT_NEUTRON).containsAll(
                Arrays.asList(PORT_NEUTRON_1, PORT_NEUTRON_2, PORT_NEUTRON_3)));

    }

    @Test
    public void collectAllPortsOfEachTypeFromComputeKeyAbsent() {
        Map<String, List<String>> portMap = new HashMap<>();
        portMap.put(PORT_NEUTRON, new ArrayList<>(Arrays.asList(PORT_NEUTRON_1, PORT_NEUTRON_2)));

        computeTemplateConsolidationData.setPorts(portMap);

        Map<String, List<String>> mapToBeModified = new HashMap<>();

        computeTemplateConsolidationData.collectAllPortsOfEachTypeFromCompute(mapToBeModified);

        Assert.assertTrue(mapToBeModified.size() == 1 && Objects.nonNull(mapToBeModified.get(PORT_NEUTRON))
                                  && mapToBeModified.get(PORT_NEUTRON).size() == 2);
        Assert.assertTrue(mapToBeModified.get(PORT_NEUTRON).containsAll(
                Arrays.asList(PORT_NEUTRON_1, PORT_NEUTRON_2)));
    }

    @Test
    public void isNumberOfPortFromEachTypeLegal_Empty() {
        Assert.assertTrue(computeTemplateConsolidationData.isNumberOfPortFromEachTypeLegal());
    }

    @Test
    public void isNumberOfPortFromEachTypeLegal_OnePort() {
        computeTemplateConsolidationData.setPorts(mapToBeModified);

        Assert.assertTrue(computeTemplateConsolidationData.isNumberOfPortFromEachTypeLegal());
    }

    @Test
    public void isNumberOfPortFromEachTypeLegal_MultiplePorts() {
        mapToBeModified.get(PORT_NEUTRON).addAll(Collections.singletonList("port_neutron_4"));
        computeTemplateConsolidationData.setPorts(mapToBeModified);

        Assert.assertFalse(computeTemplateConsolidationData.isNumberOfPortFromEachTypeLegal());
    }

    @Test
    public void getPortsIdsAndSizeEmpty() {
        Assert.assertNotNull(computeTemplateConsolidationData.getPortsIds());
        Assert.assertEquals(0, computeTemplateConsolidationData.getNumberOfPorts());
    }

    @Test
    public void getPortsIdsWithData() {
        computeTemplateConsolidationData.setPorts(mapToBeModified);
        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>("port_neutron", PORT_NEUTRON_3);

        Assert.assertNotNull(computeTemplateConsolidationData.getPortsIds());
        Assert.assertEquals(1, computeTemplateConsolidationData.getNumberOfPorts());
        Assert.assertEquals("port_neutron",
                computeTemplateConsolidationData.getPorts().entrySet().stream().findFirst().get().getKey());
    }
}
