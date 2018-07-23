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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.MapUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TypeComputeConsolidationDataTest {

    private static final String COMPUTE_NODE_TEMPLATE_ID_1 = "computeNodeTemplateId1";
    private static final String COMPUTE_NODE_TEMPLATE_ID_2 = "computeNodeTemplateId2";
    private static final String SERVER_NETWORK_ROLE_1_PORT = "server_networkrole_1_port";
    private static final String SERVER_NETWORK_ROLE_2_PORT = "server_networkrole_2_port";
    private static final String SERVER_NETWORKROLE_1_PORT1 = "server_networkrole_1_port";
    private static final String SERVER_NETWORKROLE_2_PORT1 = "server_networkrole_2_port";
    private static final String SERVER_OAM = "server_oam";
    private static final String SERVER_CMAUI = "server_cmaui";
    private static final String SERVER_0_NETWORKROLE_1_PORT = "server_0_networkrole_1_port";
    private static final String VMAC_ADDRESS = "vmac_address";
    private static final String ACCESS_IPV4 = "accessIPv4";
    private static final String SERVER_0_NETWORKROLE_2_PORT = "server_0_networkrole_2_port";
    private static final String SERVER_1_NETWORKROLE_1_PORT = "server_1_networkrole_1_port";
    private static final String SERVER_TYPE = "server_type";

    @Mock
    EntityConsolidationData entityConsolidationDataMock;

    @Mock
    private ComputeTemplateConsolidationData computeTemplateConsolidationDataMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private Map<String, List<String>> multipleNumberOfPortInEachTypeTypeMap = new HashMap<String, List<String>>() {
        {
            put("oam_untr_port", Arrays.asList("oam_1_untr_port_1", "oam_1_untr_port_2"));
            put("cmaui_untr_port", Arrays.asList("cmaui_1_untr_port_1", "cmaui_1_untr_port_2"));
        }
    };

    private Map<String, List<String>> singleNumberOfPortTypeMap = new HashMap<String, List<String>>() {
        {
            put("oam_untr_port", Collections.singletonList("oam_1_untr_port_1"));
        }
    };

    private final TypeComputeConsolidationData consolidationData = new TypeComputeConsolidationData();

    @Test
    public void testCollectAllPortsOfEachTypeFromComputesNoPorts() {
        ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
        consolidationData.setComputeTemplateConsolidationData(SERVER_TYPE,
                computeTemplateConsolidationData);

        Map<String, List<String>> stringListMap = consolidationData.collectAllPortsOfEachTypeFromComputes();
        Assert.assertTrue(Objects.nonNull(stringListMap) && MapUtils.isEmpty(stringListMap));
    }

    @Test
    public void testCollectAllPortsOfEachTypeFromComputes() {
        Map<String, List<String>> ports = new HashMap<>();
        ports.put(SERVER_NETWORK_ROLE_1_PORT,
                Arrays.asList(SERVER_0_NETWORKROLE_1_PORT, SERVER_1_NETWORKROLE_1_PORT));

        ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
        computeTemplateConsolidationData.setPorts(ports);

        consolidationData.setComputeTemplateConsolidationData(SERVER_TYPE,
                computeTemplateConsolidationData);

        Map<String, List<String>> stringListMap = consolidationData.collectAllPortsOfEachTypeFromComputes();
        Assert.assertTrue(stringListMap.containsKey(SERVER_NETWORK_ROLE_1_PORT)
                                  && stringListMap.get(SERVER_NETWORK_ROLE_1_PORT).size() == 2);
    }

    @Test
    public void testCollectAllPortsOfEachTypeFromComputesWithMultipleCompute() {
        Map<String, List<String>> ports = new HashMap<>();
        ports.put(SERVER_NETWORK_ROLE_1_PORT,
                Arrays.asList("server_0_network_role_1_port_1", "server_1_network_role_1_port_2"));

        Map<String, List<String>> ports1 = new HashMap<>();
        ports1.put(SERVER_NETWORK_ROLE_2_PORT,
                Arrays.asList("server_0_network_role_2_port_1", "server_1_network_role_2_port_2"));

        ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
        computeTemplateConsolidationData.setPorts(ports);

        ComputeTemplateConsolidationData computeTemplateConsolidationData1 = new ComputeTemplateConsolidationData();
        computeTemplateConsolidationData1.setPorts(ports1);

        consolidationData.setComputeTemplateConsolidationData(SERVER_TYPE,
                computeTemplateConsolidationData);

        consolidationData.setComputeTemplateConsolidationData("server_type1",
                computeTemplateConsolidationData1);

        Map<String, List<String>> stringListMap = consolidationData.collectAllPortsOfEachTypeFromComputes();
        Assert.assertEquals(2, stringListMap.size());
        Assert.assertEquals(2, stringListMap.get(SERVER_NETWORK_ROLE_1_PORT).size());
        Assert.assertTrue(stringListMap.get(SERVER_NETWORK_ROLE_1_PORT).contains("server_0_network_role_1_port_1")
                && stringListMap.get(SERVER_NETWORK_ROLE_1_PORT).contains("server_1_network_role_1_port_2"));

        Assert.assertEquals(2, stringListMap.get(SERVER_NETWORK_ROLE_2_PORT).size());
        Assert.assertTrue(stringListMap.get(SERVER_NETWORK_ROLE_2_PORT).contains("server_0_network_role_2_port_1")
                && stringListMap.get(SERVER_NETWORK_ROLE_2_PORT).contains("server_1_network_role_2_port_2"));
    }

    @Test
    public void testAddComputeTemplateConsolidationData() {
        consolidationData.addComputeTemplateConsolidationData(COMPUTE_NODE_TEMPLATE_ID_1);

        Set<String> expectedNodeTemplateIds =  new HashSet<>();
        expectedNodeTemplateIds.add(COMPUTE_NODE_TEMPLATE_ID_1);
        verifyComputeTemplateConsolidationData(consolidationData, COMPUTE_NODE_TEMPLATE_ID_1, expectedNodeTemplateIds);
    }

    @Test
    public void testAddSameConsolidationDataTwice_noNewCreated() {
        consolidationData.addComputeTemplateConsolidationData(COMPUTE_NODE_TEMPLATE_ID_1);
        ComputeTemplateConsolidationData firstComputeTemplateConsolidationData =
                consolidationData.getComputeTemplateConsolidationData(COMPUTE_NODE_TEMPLATE_ID_1);

        consolidationData.addComputeTemplateConsolidationData(COMPUTE_NODE_TEMPLATE_ID_1);
        ComputeTemplateConsolidationData secondComputeTemplateConsolidationData =
                consolidationData.getComputeTemplateConsolidationData(COMPUTE_NODE_TEMPLATE_ID_1);
        Assert.assertEquals(firstComputeTemplateConsolidationData, secondComputeTemplateConsolidationData);
    }

    @Test
    public void testAddDiffConsolidationData_DiffNodeTemplateId() {
        final ComputeTemplateConsolidationData firstComputeTemplateConsolidationData = consolidationData
                .addComputeTemplateConsolidationData(COMPUTE_NODE_TEMPLATE_ID_1);
        Set<String> expectedNodeTemplateIds1 =  new HashSet<>();
        expectedNodeTemplateIds1.add(COMPUTE_NODE_TEMPLATE_ID_1);
        verifyComputeTemplateConsolidationData(consolidationData, COMPUTE_NODE_TEMPLATE_ID_1, expectedNodeTemplateIds1);

        final ComputeTemplateConsolidationData secondComputeTemplateConsolidationData = consolidationData
                .addComputeTemplateConsolidationData(COMPUTE_NODE_TEMPLATE_ID_2);
        Set<String> expectedNodeTemplateIds2 =  new HashSet<>();
        expectedNodeTemplateIds2.add(COMPUTE_NODE_TEMPLATE_ID_2);
        verifyComputeTemplateConsolidationData(consolidationData, COMPUTE_NODE_TEMPLATE_ID_2, expectedNodeTemplateIds2);

        Assert.assertNotEquals(firstComputeTemplateConsolidationData, secondComputeTemplateConsolidationData);
    }

    @Test
    public void isThereMoreThanOneComputeTypeInstancePositive() {
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        typeComputeConsolidationData.setComputeTemplateConsolidationData(
                SERVER_OAM, new ComputeTemplateConsolidationData());
        typeComputeConsolidationData.setComputeTemplateConsolidationData(
                "server_mao", new ComputeTemplateConsolidationData());

        Assert.assertTrue(typeComputeConsolidationData.isThereMoreThanOneComputeTypeInstance());
    }

    @Test
    public void isThereMoreThanOneComputeTypeInstanceNegative() {
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        typeComputeConsolidationData.setComputeTemplateConsolidationData(
                "server_mao", new ComputeTemplateConsolidationData());

        Assert.assertFalse(typeComputeConsolidationData.isThereMoreThanOneComputeTypeInstance());
    }

    @Test
    public void isThereMoreThanOneComputeTypeInstanceEmpty() {
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        Assert.assertFalse(typeComputeConsolidationData.isThereMoreThanOneComputeTypeInstance());
    }

    @Test
    public void isNumberOfPortFromEachTypeLegal() {
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        typeComputeConsolidationData.setComputeTemplateConsolidationData(
                SERVER_OAM, computeTemplateConsolidationDataMock);

        typeComputeConsolidationData.isNumberOfPortFromEachTypeLegal();

        Mockito.verify(computeTemplateConsolidationDataMock).isNumberOfPortFromEachTypeLegal();
    }

    @Test
    public void isPortTypesAndNumberOfPortEqualsBetweenComputeNodesPositive() {
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();

        ComputeTemplateConsolidationData firstComputeTemplate = new ComputeTemplateConsolidationData();
        firstComputeTemplate.setPorts(singleNumberOfPortTypeMap);

        ComputeTemplateConsolidationData secondComputeTemplate = new ComputeTemplateConsolidationData();
        secondComputeTemplate.setPorts(singleNumberOfPortTypeMap);

        typeComputeConsolidationData.setComputeTemplateConsolidationData(SERVER_OAM, firstComputeTemplate);
        typeComputeConsolidationData.setComputeTemplateConsolidationData(SERVER_CMAUI, secondComputeTemplate);

        Assert.assertTrue(typeComputeConsolidationData.isPortTypesEqualsBetweenComputeNodes());
        Assert.assertTrue(typeComputeConsolidationData.isNumberOfPortsEqualsBetweenComputeNodes());
    }

    @Test
    public void isPortTypesAndNumberOfPortEqualsBetweenComputeNodesNegative() {
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();

        ComputeTemplateConsolidationData firstComputeTemplate = new ComputeTemplateConsolidationData();
        firstComputeTemplate.setPorts(multipleNumberOfPortInEachTypeTypeMap);
        typeComputeConsolidationData.setComputeTemplateConsolidationData(SERVER_OAM, firstComputeTemplate);

        ComputeTemplateConsolidationData secondComputeTemplate = new ComputeTemplateConsolidationData();
        secondComputeTemplate.setPorts(singleNumberOfPortTypeMap);
        typeComputeConsolidationData.setComputeTemplateConsolidationData(SERVER_CMAUI, secondComputeTemplate);

        Assert.assertFalse(typeComputeConsolidationData.isPortTypesEqualsBetweenComputeNodes());
        Assert.assertFalse(typeComputeConsolidationData.isNumberOfPortsEqualsBetweenComputeNodes());
    }

    @Test
    public void isNumberOfComputeConsolidationDataPerTypeLegalPositive() {
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        typeComputeConsolidationData.setComputeTemplateConsolidationData(
                SERVER_CMAUI, new ComputeTemplateConsolidationData());

        Assert.assertTrue(typeComputeConsolidationData.isNumberOfComputeConsolidationDataPerTypeLegal());
    }

    @Test
    public void isNumberOfComputeConsolidationDataPerTypeLegalNegative() {
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        typeComputeConsolidationData.setComputeTemplateConsolidationData(
                SERVER_CMAUI, new ComputeTemplateConsolidationData());
        typeComputeConsolidationData.setComputeTemplateConsolidationData(
                SERVER_OAM, new ComputeTemplateConsolidationData());

        Assert.assertFalse(typeComputeConsolidationData.isNumberOfComputeConsolidationDataPerTypeLegal());
    }

    @Test
    public void isGetAttrOutFromEntityLegal() {
        Map<String, List<String>> ports = new HashMap<>();
        ports.put(SERVER_NETWORKROLE_1_PORT1,
                Arrays.asList(SERVER_0_NETWORKROLE_1_PORT, SERVER_1_NETWORKROLE_1_PORT));

        Mockito.when(computeTemplateConsolidationDataMock.isGetAttrOutFromEntityLegal(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        typeComputeConsolidationData.setComputeTemplateConsolidationData("server_oam_1", computeTemplateConsolidationDataMock);

        Assert.assertTrue(typeComputeConsolidationData.isGetAttrOutFromEntityLegal(ports));
        Mockito.verify(computeTemplateConsolidationDataMock, Mockito.times(1))
                .isGetAttrOutFromEntityLegal(Mockito.any(), Mockito.any());
    }

    @Test
    public void testIsGetAttrOutFromEntityLegalNegative() {
        GetAttrFuncData getAttrFuncData = new GetAttrFuncData(VMAC_ADDRESS, ACCESS_IPV4);
        Map<String, List<GetAttrFuncData>> getAttOutMap = new HashMap<>();
        getAttOutMap.put(SERVER_0_NETWORKROLE_1_PORT, Collections.singletonList(getAttrFuncData));

        computeTemplateConsolidationDataMock.setNodesGetAttrOut(getAttOutMap);

        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        typeComputeConsolidationData.setComputeTemplateConsolidationData("server_oam_1",
                computeTemplateConsolidationDataMock);

        Mockito.when(computeTemplateConsolidationDataMock.isGetAttrOutFromEntityLegal(Mockito.any(), Mockito.any()))
                .thenReturn(false);

        Assert.assertFalse(computeTemplateConsolidationDataMock
                .isGetAttrOutFromEntityLegal(Mockito.any(), Mockito.any()));

        Mockito.verify(computeTemplateConsolidationDataMock, Mockito.times(1))
                .isGetAttrOutFromEntityLegal(Mockito.any(), Mockito.any());

    }

    private void verifyComputeTemplateConsolidationData(TypeComputeConsolidationData typeComputeConsolidationData,
            String computeNodeTemplateId, Set<String> expectedComputeNodeTemplateIds) {
        ComputeTemplateConsolidationData computeTemplateConsolidationData =
                typeComputeConsolidationData.getComputeTemplateConsolidationData(computeNodeTemplateId);
        Assert.assertNotNull(computeTemplateConsolidationData);
        Assert.assertEquals(computeTemplateConsolidationData.getNodeTemplateId(), computeNodeTemplateId);

        verifyGetAllComputeNodeTemplateId(typeComputeConsolidationData, expectedComputeNodeTemplateIds);
    }

    private void verifyGetAllComputeNodeTemplateId(TypeComputeConsolidationData typeComputeConsolidationData,
            Set<String> expectedComputeNodeTemplateIds) {
        Collection<String> allComputeNodeTemplateIds  = typeComputeConsolidationData.getAllComputeNodeTemplateIds();
        Assert.assertNotNull(allComputeNodeTemplateIds);
        Assert.assertTrue(allComputeNodeTemplateIds.containsAll(expectedComputeNodeTemplateIds));
    }
}
