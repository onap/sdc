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

import com.google.common.collect.Multimap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;

@SuppressWarnings("Duplicates")
public class EntityConsolidationDataTest {

    private static final String NODE_TEMPLATE_ID_1 = "nodeTemplateId1";
    private static final String NODE_TEMPLATE_ID_2 = "nodeTemplateId2";
    private static final String REQUIREMENT_ID_1 = "requirementId1";
    private static final String REQUIREMENT_ID_2 = "requirementId2";
    private static final String GROUP_ID_1 = "groupId1";
    private static final String GROUP_ID_2 = "groupId2";
    private static final String FIELD_1 = "field1";
    private static final String FIELD_2 = "field2";
    private static final String SERVER_NETWORKROLE_1_PORT = "server_networkrole_1_port";
    private static final String SERVER_0_NETWORKROLE_1_PORT = "server_0_networkrole_1_port";
    private static final String SERVER_0_NETWORKROLE_2_PORT = "server_0_networkrole_2_port";
    private static final String VMAC_ADDRESS = "vmac_address";
    private static final String ACCESS_IPV4 = "accessIPv4";
    private static final String SERVER_1_NETWORKROLE_1_PORT = "server_1_networkrole_1_port";


    private final EntityConsolidationData consolidationData = new EntityConsolidationData();

    @Test
    public void testIsGetAttrOutFromEntityLegal() {
        Map<String, List<String>> ports = new HashMap<>();
        ports.put(SERVER_NETWORKROLE_1_PORT,
                Arrays.asList(SERVER_0_NETWORKROLE_1_PORT, SERVER_1_NETWORKROLE_1_PORT));

        GetAttrFuncData getAttrFuncData = new GetAttrFuncData(VMAC_ADDRESS, ACCESS_IPV4);
        Map<String, List<GetAttrFuncData>> getAttOutMap = new HashMap<>();
        getAttOutMap.put(SERVER_0_NETWORKROLE_1_PORT, Collections.singletonList(getAttrFuncData));

        GetAttrFuncData getAttrFuncData1 = new GetAttrFuncData(VMAC_ADDRESS, ACCESS_IPV4);
        Map<String, List<GetAttrFuncData>> getAttOutMap1 = new HashMap<>();
        getAttOutMap1.put(SERVER_1_NETWORKROLE_1_PORT, Collections.singletonList(getAttrFuncData1));


        EntityConsolidationData entityConsolidationData = new EntityConsolidationData();
        entityConsolidationData.setNodesGetAttrOut(getAttOutMap);

        EntityConsolidationData entityConsolidationData1 = new EntityConsolidationData();
        entityConsolidationData1.setNodesGetAttrOut(getAttOutMap1);

        List<EntityConsolidationData> entityConsolidationDataList =
                Arrays.asList(entityConsolidationData, entityConsolidationData1);

        Assert.assertTrue(entityConsolidationData
                                  .isGetAttrOutFromEntityLegal(entityConsolidationDataList, ports));
    }

    @Test
    public void testIsGetAttrOutFromEntityLegal_EntityConsolidationDataListEmptyOrNull() {
        EntityConsolidationData entityConsolidationData = new EntityConsolidationData();

        Assert.assertTrue(entityConsolidationData.isGetAttrOutFromEntityLegal(null, null));
        Assert.assertTrue(entityConsolidationData.isGetAttrOutFromEntityLegal(
                Collections.emptyList(), Collections.emptyMap()));
    }

    @Test
    public void testIsGetAttrOutFromEntityLegal_PortTypeToIdsEmptyOrNull() {
        EntityConsolidationData entityConsolidationData = new EntityConsolidationData();

        Assert.assertTrue(entityConsolidationData.isGetAttrOutFromEntityLegal(
                Collections.singletonList(entityConsolidationData), null));

        Assert.assertTrue(entityConsolidationData.isGetAttrOutFromEntityLegal(
                Collections.singletonList(entityConsolidationData), Collections.emptyMap()));
    }

    @Test
    public void testIsGetAttrOutFromEntityLegal_Negative() {
        Map<String, List<String>> ports = new HashMap<>();
        ports.put(SERVER_NETWORKROLE_1_PORT,
                Arrays.asList(SERVER_0_NETWORKROLE_1_PORT, SERVER_0_NETWORKROLE_2_PORT));

        GetAttrFuncData getAttrFuncData = new GetAttrFuncData(VMAC_ADDRESS, ACCESS_IPV4);
        Map<String, List<GetAttrFuncData>> getAttOutMap = new HashMap<>();
        getAttOutMap.put("server_0_networkrole_1_port", Collections.singletonList(getAttrFuncData));

        GetAttrFuncData getAttrFuncData1 = new GetAttrFuncData("vmac_address", "accessIPv4");
        getAttOutMap.put("server_0_networkrole_2_port", Collections.singletonList(getAttrFuncData1));


        EntityConsolidationData entityConsolidationData = new EntityConsolidationData();
        entityConsolidationData.setNodesGetAttrOut(getAttOutMap);

        EntityConsolidationData entityConsolidationData1 = new EntityConsolidationData();

        List<EntityConsolidationData> entityConsolidationDataList =
                Arrays.asList(entityConsolidationData, entityConsolidationData1);

        Assert.assertFalse(entityConsolidationData
                                   .isGetAttrOutFromEntityLegal(entityConsolidationDataList, ports));
    }

    @Test
    public void testIsGetAttrOutFromEntityLegal_EmptyList() {
        Map<String, List<String>> ports = new HashMap<>();
        ports.put(SERVER_NETWORKROLE_1_PORT,
                Arrays.asList(SERVER_0_NETWORKROLE_1_PORT, SERVER_0_NETWORKROLE_2_PORT));

        EntityConsolidationData entityConsolidationData = new EntityConsolidationData();

        Assert.assertTrue(entityConsolidationData
                                   .isGetAttrOutFromEntityLegal(Collections.emptyList(), ports));
    }

    @Test
    public void testIsGetAttrOutFromEntityLegal_MultiplePortWithDiffAttr() {
        Map<String, List<String>> ports = new HashMap<>();
        ports.put(SERVER_NETWORKROLE_1_PORT,
                Arrays.asList(SERVER_0_NETWORKROLE_1_PORT, SERVER_1_NETWORKROLE_1_PORT));

        ports.put("server_networkrole_2_port",
                Arrays.asList(SERVER_0_NETWORKROLE_2_PORT, "server_1_networkrole_2_port"));

        GetAttrFuncData getAttrFuncData = new GetAttrFuncData(VMAC_ADDRESS, ACCESS_IPV4);
        Map<String, List<GetAttrFuncData>> getAttOutMap = new HashMap<>();
        getAttOutMap.put(SERVER_0_NETWORKROLE_1_PORT, Collections.singletonList(getAttrFuncData));
        getAttOutMap.put(SERVER_0_NETWORKROLE_2_PORT, Collections.singletonList(getAttrFuncData));

        GetAttrFuncData getAttrFuncData1 = new GetAttrFuncData(VMAC_ADDRESS, ACCESS_IPV4);
        Map<String, List<GetAttrFuncData>> getAttOutMap1 = new HashMap<>();
        getAttOutMap1.put(SERVER_0_NETWORKROLE_1_PORT, Collections.singletonList(getAttrFuncData1));


        EntityConsolidationData entityConsolidationData = new EntityConsolidationData();
        entityConsolidationData.setNodesGetAttrOut(getAttOutMap);

        EntityConsolidationData entityConsolidationData1 = new EntityConsolidationData();
        entityConsolidationData1.setNodesGetAttrOut(getAttOutMap1);

        List<EntityConsolidationData> entityConsolidationDataList =
                Arrays.asList(entityConsolidationData, entityConsolidationData1);

        Assert.assertFalse(entityConsolidationData
                                   .isGetAttrOutFromEntityLegal(entityConsolidationDataList, ports));
    }

    @Test
    public void testAddNodesConnectedIn_SameNodeTemplateIds() {
        Map<String, String[]> expectedNodesConnectedData = new HashMap<>();
        addNodesConnectedIn(consolidationData,NODE_TEMPLATE_ID_1, REQUIREMENT_ID_1);
        expectedNodesConnectedData.put(NODE_TEMPLATE_ID_1, new String[]{REQUIREMENT_ID_1});
        checkNodesConnected(consolidationData.getNodesConnectedIn(), expectedNodesConnectedData);

        addNodesConnectedIn(consolidationData,NODE_TEMPLATE_ID_1, REQUIREMENT_ID_2);
        expectedNodesConnectedData.put(NODE_TEMPLATE_ID_1, new String[]{REQUIREMENT_ID_1, REQUIREMENT_ID_2});
        checkNodesConnected(consolidationData.getNodesConnectedIn(), expectedNodesConnectedData);
    }

    @Test
    public void testAddNodesConnectedIn_DiffNodeTemplateIds() {
        Map<String, String[]> expectedNodesConnectedData = new HashMap<>();
        addNodesConnectedIn(consolidationData, NODE_TEMPLATE_ID_1, REQUIREMENT_ID_1);
        expectedNodesConnectedData.put(NODE_TEMPLATE_ID_1, new String[]{REQUIREMENT_ID_1});
        checkNodesConnected(consolidationData.getNodesConnectedIn(), expectedNodesConnectedData);

        addNodesConnectedIn(consolidationData, NODE_TEMPLATE_ID_2, REQUIREMENT_ID_2);
        expectedNodesConnectedData.put(NODE_TEMPLATE_ID_2, new String[]{REQUIREMENT_ID_2});
        checkNodesConnected(consolidationData.getNodesConnectedIn(), expectedNodesConnectedData);
    }

    @Test
    public void testAddNodesConnectedOut_SameNodeTemplateIds() {
        Map<String, String[]> expectedNodesConnectedData = new HashMap<>();
        addNodesConnectedOut(consolidationData, NODE_TEMPLATE_ID_1, REQUIREMENT_ID_1);
        expectedNodesConnectedData.put(NODE_TEMPLATE_ID_1, new String[]{REQUIREMENT_ID_1});
        checkNodesConnected(consolidationData.getNodesConnectedOut(), expectedNodesConnectedData);

        addNodesConnectedOut(consolidationData, NODE_TEMPLATE_ID_1, REQUIREMENT_ID_2);
        expectedNodesConnectedData.put(NODE_TEMPLATE_ID_1, new String[]{REQUIREMENT_ID_1, REQUIREMENT_ID_2});
        checkNodesConnected(consolidationData.getNodesConnectedOut(), expectedNodesConnectedData);
    }

    @Test
    public void testAddNodesConnectedOut_DiffNodeTemplateIds() {
        Map<String, String[]> expectedNodesConnectedData = new HashMap<>();
        addNodesConnectedOut(consolidationData, NODE_TEMPLATE_ID_1, REQUIREMENT_ID_1);
        expectedNodesConnectedData.put(NODE_TEMPLATE_ID_1, new String[]{REQUIREMENT_ID_1});
        checkNodesConnected(consolidationData.getNodesConnectedOut(), expectedNodesConnectedData);

        addNodesConnectedOut(consolidationData, NODE_TEMPLATE_ID_2, REQUIREMENT_ID_2);
        expectedNodesConnectedData.put(NODE_TEMPLATE_ID_2, new String[]{REQUIREMENT_ID_2});
        checkNodesConnected(consolidationData.getNodesConnectedOut(), expectedNodesConnectedData);
    }

    @Test
    public void testAddOutputParamGetAttrIn() {
        GetAttrFuncData getAttrFuncData1 = createGetAttrFuncData(FIELD_1);
        consolidationData.addOutputParamGetAttrIn(getAttrFuncData1);
        List<GetAttrFuncData> outputParametersGetAttrIn = consolidationData.getOutputParametersGetAttrIn();
        Assert.assertEquals(1, outputParametersGetAttrIn.size());
        Assert.assertTrue(outputParametersGetAttrIn.contains(getAttrFuncData1));

        GetAttrFuncData getAttrFuncData2 = createGetAttrFuncData(FIELD_2);
        consolidationData.addOutputParamGetAttrIn(getAttrFuncData2);
        Assert.assertEquals(2,outputParametersGetAttrIn.size());
        Assert.assertTrue(outputParametersGetAttrIn.contains(getAttrFuncData1));
        Assert.assertTrue(outputParametersGetAttrIn.contains(getAttrFuncData2));
    }

    @Test
    public void testRemoveParamNameFromAttrFuncList() {
        GetAttrFuncData getAttrFuncData1 = createGetAttrFuncData(FIELD_1);
        consolidationData.addOutputParamGetAttrIn(getAttrFuncData1);
        // verify that getAttrFuncData was added
        List<GetAttrFuncData> outputParametersGetAttrIn = consolidationData.getOutputParametersGetAttrIn();
        Assert.assertEquals(1, outputParametersGetAttrIn.size());

        consolidationData.removeParamNameFromAttrFuncList(FIELD_2);
        //verify that not existing getAttrFuncData parameter wasn't removed and no Exception
        outputParametersGetAttrIn = consolidationData.getOutputParametersGetAttrIn();
        Assert.assertEquals(1, outputParametersGetAttrIn.size());

        consolidationData.removeParamNameFromAttrFuncList("field1");
        //verify that existing getAttrFuncData parameter was removed
        outputParametersGetAttrIn = consolidationData.getOutputParametersGetAttrIn();
        Assert.assertEquals(0, outputParametersGetAttrIn.size());
    }

    @Test
    public void testAddGroupId() {
        consolidationData.addGroupId(GROUP_ID_1);
        List<String> groupIds = consolidationData.getGroupIds();
        Assert.assertNotNull(groupIds);
        Assert.assertTrue(groupIds.contains(GROUP_ID_1));
        Assert.assertEquals(1, consolidationData.getGroupIds().size());

        consolidationData.addGroupId(GROUP_ID_2);
        Assert.assertEquals(2, consolidationData.getGroupIds().size());
        Assert.assertTrue(groupIds.contains(GROUP_ID_2));
    }

    private GetAttrFuncData createGetAttrFuncData(String field) {
        GetAttrFuncData getAttrFuncData = new GetAttrFuncData();
        getAttrFuncData.setFieldName(field);
        getAttrFuncData.setAttributeName("attribute");
        return  getAttrFuncData;
    }

    private void checkNodesConnected(Multimap<String, RequirementAssignmentData> actualNodesConnected,
                                              Map<String, String[]> expectedNodesConnected) {
        Assert.assertNotNull(actualNodesConnected);

        expectedNodesConnected.keySet().forEach(expectedNodeTemplateId -> {
            Assert.assertTrue(actualNodesConnected.containsKey(expectedNodeTemplateId));
            Assert.assertEquals(expectedNodesConnected.size(), actualNodesConnected.keySet().size());

            Collection<RequirementAssignmentData> actualRequirementAssignmentData =
                    actualNodesConnected.get(expectedNodeTemplateId);
            List<String> expectedRequirementIdList =
                    Arrays.asList(expectedNodesConnected.get(expectedNodeTemplateId));
            Assert.assertEquals(expectedRequirementIdList.size(), actualRequirementAssignmentData.size());

            actualRequirementAssignmentData.forEach(actualRequirementAssignment ->
                    Assert.assertTrue(expectedRequirementIdList
                            .contains(actualRequirementAssignment.getRequirementId())));
        });

    }

    private void addNodesConnectedIn(EntityConsolidationData consolidationData,
            String nodeTemplateId, String requirementId) {
        RequirementAssignment requirementAssignment = new RequirementAssignment();
        consolidationData.addNodesConnectedIn(nodeTemplateId, requirementId, requirementAssignment);
    }

    private void addNodesConnectedOut(EntityConsolidationData consolidationData,
            String nodeTemplateId, String requirementId) {
        RequirementAssignment requirementAssignment = new RequirementAssignment();
        consolidationData.addNodesConnectedOut(nodeTemplateId, requirementId, requirementAssignment);
    }
}
