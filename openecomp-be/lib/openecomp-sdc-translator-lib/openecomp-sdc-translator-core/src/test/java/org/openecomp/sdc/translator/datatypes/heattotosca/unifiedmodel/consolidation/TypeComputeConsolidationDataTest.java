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
import java.util.Objects;

import org.apache.commons.collections4.MapUtils;
import org.junit.Assert;
import org.junit.Test;

public class TypeComputeConsolidationDataTest {

    private static final String SERVER_NETWORKROLE_1_PORT = "server_networkrole_1_port";
    private static final String SERVER_NETWORKROLE_2_PORT = "server_networkrole_2_port";

    @Test
    public void testCollectAllPortsOfEachTypeFromComputesNoPorts() {
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();

        ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();

        typeComputeConsolidationData.setComputeTemplateConsolidationData("server_type",
                computeTemplateConsolidationData);

        Map<String, List<String>> stringListMap = typeComputeConsolidationData.collectAllPortsOfEachTypeFromComputes();
        Assert.assertTrue(Objects.nonNull(stringListMap) && MapUtils.isEmpty(stringListMap));

    }

    @Test
    public void testCollectAllPortsOfEachTypeFromComputes() {
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        Map<String, List<String>> ports = new HashMap<>();
        ports.put(SERVER_NETWORKROLE_1_PORT,
                Arrays.asList("server_0_networkrole_1_port", "server_1_networkrole_1_port"));

        ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
        computeTemplateConsolidationData.setPorts(ports);

        typeComputeConsolidationData.setComputeTemplateConsolidationData("server_type",
                computeTemplateConsolidationData);

        Map<String, List<String>> stringListMap = typeComputeConsolidationData.collectAllPortsOfEachTypeFromComputes();
        Assert.assertTrue(stringListMap.containsKey(SERVER_NETWORKROLE_1_PORT)
                                  && stringListMap.get(SERVER_NETWORKROLE_1_PORT).size() == 2);

    }

    @Test
    public void testCollectAllPortsOfEachTypeFromComputesWithMultipleCompute() {
        Map<String, List<String>> ports = new HashMap<>();
        ports.put(SERVER_NETWORKROLE_1_PORT,
                Arrays.asList("server_0_networkrole_1_port_1", "server_1_networkrole_1_port_2"));

        Map<String, List<String>> ports1 = new HashMap<>();
        ports1.put(SERVER_NETWORKROLE_2_PORT,
                Arrays.asList("server_0_networkrole_2_port_1", "server_1_networkrole_2_port_2"));

        ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
        computeTemplateConsolidationData.setPorts(ports);

        ComputeTemplateConsolidationData computeTemplateConsolidationData1 = new ComputeTemplateConsolidationData();
        computeTemplateConsolidationData1.setPorts(ports1);

        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        typeComputeConsolidationData.setComputeTemplateConsolidationData("server_type",
                computeTemplateConsolidationData);

        typeComputeConsolidationData.setComputeTemplateConsolidationData("server_type1",
                computeTemplateConsolidationData1);

        Map<String, List<String>> stringListMap = typeComputeConsolidationData.collectAllPortsOfEachTypeFromComputes();
        Assert.assertTrue(stringListMap.size() == 2);
        Assert.assertTrue(stringListMap.get(SERVER_NETWORKROLE_1_PORT).size() == 2);
        Assert.assertTrue(stringListMap.get(SERVER_NETWORKROLE_2_PORT).size() == 2);

    }
}
