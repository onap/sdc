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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class TypeComputeConsolidationDataTest {

    private static final String COMPUTE_NODE_TEMPLATE_ID_1 = "computeNodeTemplateId1";
    private static final String COMPUTE_NODE_TEMPLATE_ID_2 = "computeNodeTemplateId2";

    @Test
    public void testAddComputeTemplateConsolidationData() {
        TypeComputeConsolidationData consolidationData = new TypeComputeConsolidationData();
        consolidationData.addComputeTemplateConsolidationData(COMPUTE_NODE_TEMPLATE_ID_1);

        Set<String> expectedNodeTemplateIds =  new HashSet<>();
        expectedNodeTemplateIds.add(COMPUTE_NODE_TEMPLATE_ID_1);
        verifyComputeTemplateConsolidationData(consolidationData, COMPUTE_NODE_TEMPLATE_ID_1, expectedNodeTemplateIds);
    }

    @Test
    public void testAddSameConsolidationDataTwice_noNewCreated() {
        TypeComputeConsolidationData consolidationData = new TypeComputeConsolidationData();
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
        TypeComputeConsolidationData consolidationData = new TypeComputeConsolidationData();

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
