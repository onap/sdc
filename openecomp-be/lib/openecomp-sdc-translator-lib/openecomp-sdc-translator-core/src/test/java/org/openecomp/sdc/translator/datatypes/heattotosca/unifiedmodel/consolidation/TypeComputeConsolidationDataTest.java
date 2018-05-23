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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeComputeConsolidationDataTest {
    @Test
    public void testCollectAllPortsOfEachTypeFromComputes() {
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        Map<String, List<String>> ports = new HashMap<>();
        ports.put("server_networkrole_port",
                Arrays.asList("server_0_networkrole_1_port", "server_0_networkrole_2_port"));

        ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
        computeTemplateConsolidationData.setPorts(ports);

        typeComputeConsolidationData.setComputeTemplateConsolidationData("server_type",
                computeTemplateConsolidationData);

        Map<String, List<String>> stringListMap = typeComputeConsolidationData.collectAllPortsOfEachTypeFromComputes();
        Assert.assertTrue(stringListMap.containsKey("server_networkrole_port")
                                  && stringListMap.get("server_networkrole_port").size() == 2);

    }
}
