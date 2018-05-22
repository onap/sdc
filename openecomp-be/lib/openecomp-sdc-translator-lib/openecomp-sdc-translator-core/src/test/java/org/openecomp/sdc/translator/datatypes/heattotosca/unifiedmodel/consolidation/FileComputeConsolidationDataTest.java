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
import org.junit.Before;
import org.junit.Test;

import org.mockito.MockitoAnnotations;


public class FileComputeConsolidationDataTest {

    private final String computeNodeTemplateId1 = "computeNodeTemplateId1";
    private final String computeNodeType1 = "computeNodeType1";

    private final String computeNodeTemplateId2 = "computeNodeTemplateId2";
    private final String computeNodeType2 = "computeNodeType2";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddComputeTemplateConsolidationData() {
        FileComputeConsolidationData consolidationData = new FileComputeConsolidationData();
        consolidationData.addComputeTemplateConsolidationData(computeNodeType1, computeNodeTemplateId1);

        Set<String> expectedComputeNodeTypes = new HashSet<>();
        expectedComputeNodeTypes.add(computeNodeType1);
        verifyComputeConsolidationData(consolidationData, computeNodeType1, expectedComputeNodeTypes);
    }


    @Test
    public void testAddSameConsolidationDataTwice_noNewCreated() {
        FileComputeConsolidationData consolidationData = new FileComputeConsolidationData();
        consolidationData.addComputeTemplateConsolidationData(computeNodeType1, computeNodeTemplateId1);
        TypeComputeConsolidationData firstTypeComputeConsolidationData =
                consolidationData.getTypeComputeConsolidationData(computeNodeType1);

        consolidationData.addComputeTemplateConsolidationData(computeNodeType1, computeNodeTemplateId1);
        TypeComputeConsolidationData secondTypeComputeConsolidationData =
                consolidationData.getTypeComputeConsolidationData(computeNodeType1);
        Assert.assertEquals(firstTypeComputeConsolidationData, secondTypeComputeConsolidationData);
    }

    @Test
    public void testAddDiffConsolidationData_DiffNodeType() {
        FileComputeConsolidationData consolidationData = new FileComputeConsolidationData();

        consolidationData.addComputeTemplateConsolidationData(computeNodeType1, computeNodeTemplateId1);
        Set<String> expectedComputeNodeTypes =  new HashSet<>();
        expectedComputeNodeTypes.add(computeNodeType1);
        verifyComputeConsolidationData(consolidationData, computeNodeType1, expectedComputeNodeTypes);

        consolidationData.addComputeTemplateConsolidationData(computeNodeType2, computeNodeTemplateId2);
        expectedComputeNodeTypes.add(computeNodeType2);
        verifyComputeConsolidationData(consolidationData, computeNodeType2, expectedComputeNodeTypes);
    }

    @Test
    public void testAddDiffConsolidationData_SameNodeType_MultiNodeTemplateId() {
        FileComputeConsolidationData consolidationData = new FileComputeConsolidationData();

        consolidationData.addComputeTemplateConsolidationData(computeNodeType1, computeNodeTemplateId1);
        Set<String> expectedComputeNodeTypes = new HashSet<>();
        expectedComputeNodeTypes.add(computeNodeType1);
        verifyComputeConsolidationData(consolidationData, computeNodeType1, expectedComputeNodeTypes);

        consolidationData.addComputeTemplateConsolidationData(computeNodeType1, computeNodeTemplateId2);
        verifyComputeConsolidationData(consolidationData, computeNodeType1, expectedComputeNodeTypes);
    }

    private void verifyComputeConsolidationData(FileComputeConsolidationData fileComputeConsolidationData,
            String computeNodeType, Set<String> expectedComputeNodeTypes) {
        TypeComputeConsolidationData typeComputeConsolidationData =
                fileComputeConsolidationData.getTypeComputeConsolidationData(computeNodeType);
        Assert.assertNotNull(typeComputeConsolidationData);
        verifyGetAllComputeTypes(fileComputeConsolidationData, expectedComputeNodeTypes);
    }

    private void verifyGetAllComputeTypes(FileComputeConsolidationData fileComputeConsolidationData,
                                                       Set<String> expectedComputeNodeTypes) {
        Collection<String> allComputeTypes = fileComputeConsolidationData.getAllComputeTypes();
        Assert.assertNotNull(allComputeTypes);
        Assert.assertEquals(allComputeTypes.size(), expectedComputeNodeTypes.size());
        Assert.assertTrue(allComputeTypes.containsAll(expectedComputeNodeTypes));
    }
}
