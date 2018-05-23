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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class EntityConsolidationDataTest {
    @Test
    public void testIsGetAttrOutFromEntityLegal() {
        Map<String, List<String>> ports = new HashMap<>();
        ports.put("server_networkrole_port",
                Arrays.asList("server_0_networkrole_1_port", "server_0_networkrole_2_port"));

        GetAttrFuncData getAttrFuncData = new GetAttrFuncData("vmac_address", "accessIPv4");
        Map<String, List<GetAttrFuncData>> getAttOutMap = new HashMap<>();
        getAttOutMap.put("server_0_networkrole_1_port", Collections.singletonList(getAttrFuncData));

        GetAttrFuncData getAttrFuncData1 = new GetAttrFuncData("vmac_address", "accessIPv4");
        Map<String, List<GetAttrFuncData>> getAttOutMap1 = new HashMap<>();
        getAttOutMap.put("server_0_networkrole_2_port", Collections.singletonList(getAttrFuncData1));


        ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
        computeTemplateConsolidationData.setNodesGetAttrOut(getAttOutMap);

        ComputeTemplateConsolidationData computeTemplateConsolidationData1 = new ComputeTemplateConsolidationData();
        computeTemplateConsolidationData1.setNodesGetAttrOut(getAttOutMap1);

        List<ComputeTemplateConsolidationData> computeTemplateConsolidationDataList =
                Arrays.asList(computeTemplateConsolidationData, computeTemplateConsolidationData1);

        Assert.assertTrue(computeTemplateConsolidationData
                                  .isGetAttrOutFromEntityLegal(computeTemplateConsolidationDataList, ports));
    }
}
