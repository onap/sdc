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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.MapUtils;
import org.junit.Assert;
import org.junit.Test;

public class TypeComputeConsolidationDataTest {

    private static final String COMPUTE_NODE_TEMPLATE_ID_1 = "computeNodeTemplateId1";
    private static final String COMPUTE_NODE_TEMPLATE_ID_2 = "computeNodeTemplateId2";
    private static final String SERVER_NETWORK_ROLE_1_PORT = "server_network_role_1_port";
    private static final String SERVER_NETWORK_ROLE_2_PORT = "server_network_role_2_port";

    private final TypeComputeConsolidationData consolidationData = new TypeComputeConsolidationData();

    @Test
    public void testCollectAllPortsOfEachTypeFromComputesNoPorts() {
        ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
        consolidationData.setComputeTemplateConsolidationData("server_type",
                computeTemplateConsolidationData);

        Map<String, List<String>> stringListMap = consolidationData.collectAllPortsOfEachTypeFromComputes();
        Assert.assertTrue(Objects.nonNull(stringListMap) && MapUtils.isEmpty(stringListMap));
    }

    @Test
    public void testCollectAllPortsOfEachTypeFromComputes() {
        Map<String, List<String>> ports = new HashMap<>();
        ports.put(SERVER_NETWORK_ROLE_1_PORT,
                Arrays.asList("server_0_network_role_1_port", "server_1_network_role_1_port"));

        ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
        computeTemplateConsolidationData.setPorts(ports);

        consolidationData.setComputeTemplateConsolidationData("server_type",
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

        consolidationData.setComputeTemplateConsolidationData("server_type",
                computeTemplateConsolidationData);

        consolidationData.setComputeTemplateConsolidationData("server_type1",
                computeTemplateConsolidationData1);

        Map<String, List<String>> stringListMap = consolidationData.collectAllPortsOfEachTypeFromComputes();
        Assert.assertTrue(stringListMap.size() == 2);
        Assert.assertTrue(stringListMap.get(SERVER_NETWORK_ROLE_1_PORT).size() == 2);
        Assert.assertTrue(stringListMap.get(SERVER_NETWORK_ROLE_1_PORT).contains("server_0_network_role_1_port_1")
                && stringListMap.get(SERVER_NETWORK_ROLE_1_PORT).contains("server_1_network_role_1_port_2"));

        Assert.assertTrue(stringListMap.get(SERVER_NETWORK_ROLE_2_PORT).size() == 2);
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
