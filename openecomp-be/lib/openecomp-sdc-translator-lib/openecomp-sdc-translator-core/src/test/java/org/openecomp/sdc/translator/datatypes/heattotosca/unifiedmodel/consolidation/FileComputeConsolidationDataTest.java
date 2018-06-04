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

public class FileComputeConsolidationDataTest {

    private static final String COMPUTE_NODE_TEMPLATE_ID_1 = "computeNodeTemplateId1";
    private static final String COMPUTE_NODE_TEMPLATE_ID_2 = "computeNodeTemplateId2";
    private static final String COMPUTE_NODE_TYPE_1 = "computeNodeType1";
    private static final String COMPUTE_NODE_TYPE_2 = "computeNodeType2";

    @Test
    public void testAddComputeTemplateConsolidationData() {
        FileComputeConsolidationData consolidationData = new FileComputeConsolidationData();
        consolidationData.addComputeTemplateConsolidationData(COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);

        Set<String> expectedComputeNodeTypes = new HashSet<>();
        expectedComputeNodeTypes.add(COMPUTE_NODE_TYPE_1);
        checkComputeConsolidationData(consolidationData, COMPUTE_NODE_TYPE_1, expectedComputeNodeTypes);
    }

    @Test
    public void testAddSameConsolidationDataTwice_noNewCreated() {
        FileComputeConsolidationData consolidationData = new FileComputeConsolidationData();
        consolidationData.addComputeTemplateConsolidationData(COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        TypeComputeConsolidationData firstTypeComputeConsolidationData =
                consolidationData.getTypeComputeConsolidationData(COMPUTE_NODE_TYPE_1);

        consolidationData.addComputeTemplateConsolidationData(COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        TypeComputeConsolidationData secondTypeComputeConsolidationData =
                consolidationData.getTypeComputeConsolidationData(COMPUTE_NODE_TYPE_1);
        Assert.assertEquals(firstTypeComputeConsolidationData, secondTypeComputeConsolidationData);
    }

    @Test
    public void testAddDiffConsolidationData_DiffNodeType() {
        FileComputeConsolidationData consolidationData = new FileComputeConsolidationData();

        consolidationData.addComputeTemplateConsolidationData(COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        Set<String> expectedComputeNodeTypes =  new HashSet<>();
        expectedComputeNodeTypes.add(COMPUTE_NODE_TYPE_1);
        checkComputeConsolidationData(consolidationData, COMPUTE_NODE_TYPE_1, expectedComputeNodeTypes);

        consolidationData.addComputeTemplateConsolidationData(COMPUTE_NODE_TYPE_2, COMPUTE_NODE_TEMPLATE_ID_2);
        expectedComputeNodeTypes.add(COMPUTE_NODE_TYPE_2);
        checkComputeConsolidationData(consolidationData, COMPUTE_NODE_TYPE_2, expectedComputeNodeTypes);
    }

    @Test
    public void testAddDiffConsolidationData_SameNodeType_MultiNodeTemplateId() {
        FileComputeConsolidationData consolidationData = new FileComputeConsolidationData();

        consolidationData.addComputeTemplateConsolidationData(COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        Set<String> expectedComputeNodeTypes = new HashSet<>();
        expectedComputeNodeTypes.add(COMPUTE_NODE_TYPE_1);
        checkComputeConsolidationData(consolidationData, COMPUTE_NODE_TYPE_1, expectedComputeNodeTypes);

        consolidationData.addComputeTemplateConsolidationData(COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_2);
        checkComputeConsolidationData(consolidationData, COMPUTE_NODE_TYPE_1, expectedComputeNodeTypes);
    }

    @Test
    public void isNumberOfComputeTypesLegalPositive() {
        ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        typeComputeConsolidationData.setComputeTemplateConsolidationData(
                "server_oam_1", computeTemplateConsolidationData);

        FileComputeConsolidationData fileComputeConsolidationData = new FileComputeConsolidationData();
        fileComputeConsolidationData.setTypeComputeConsolidationData("server_oam", typeComputeConsolidationData);

        Assert.assertTrue(fileComputeConsolidationData.isNumberOfComputeTypesLegal());
    }

    @Test
    public void isNumberOfComputeTypesLegalNegative() {
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        typeComputeConsolidationData.setComputeTemplateConsolidationData(
                "server_oam_1", new ComputeTemplateConsolidationData());
        typeComputeConsolidationData.setComputeTemplateConsolidationData(
                "server_oam_2", new ComputeTemplateConsolidationData());


        FileComputeConsolidationData fileComputeConsolidationData = new FileComputeConsolidationData();
        fileComputeConsolidationData.setTypeComputeConsolidationData("server_oam", typeComputeConsolidationData);

        Assert.assertFalse(fileComputeConsolidationData.isNumberOfComputeTypesLegal());
    }

    private void checkComputeConsolidationData(FileComputeConsolidationData fileComputeConsolidationData,
            String computeNodeType, Set<String> expectedComputeNodeTypes) {
        TypeComputeConsolidationData typeComputeConsolidationData =
                fileComputeConsolidationData.getTypeComputeConsolidationData(computeNodeType);
        Assert.assertNotNull(typeComputeConsolidationData);
        checkGetAllComputeTypes(fileComputeConsolidationData, expectedComputeNodeTypes);
    }

    private void checkGetAllComputeTypes(FileComputeConsolidationData fileComputeConsolidationData,
                                                       Set<String> expectedComputeNodeTypes) {
        Collection<String> allComputeTypes = fileComputeConsolidationData.getAllComputeTypes();
        Assert.assertNotNull(allComputeTypes);
        Assert.assertEquals(allComputeTypes.size(), expectedComputeNodeTypes.size());
        Assert.assertTrue(allComputeTypes.containsAll(expectedComputeNodeTypes));
    }
}
