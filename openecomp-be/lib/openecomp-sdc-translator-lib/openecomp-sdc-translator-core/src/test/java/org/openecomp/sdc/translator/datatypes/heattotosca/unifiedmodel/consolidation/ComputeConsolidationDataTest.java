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

import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class ComputeConsolidationDataTest {

    private final String serviceTemplateFileName1 = "serviceTemplateFileName1";
    private final String serviceTemplateFileName2 = "serviceTemplateFileName2";

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
        ComputeConsolidationData consolidationData = new ComputeConsolidationData();
        consolidationData.addComputeTemplateConsolidationData(serviceTemplateFileName1,
                computeNodeType1, computeNodeTemplateId1);

        Set<String> expectedServiceTemplateNames = new HashSet();
        expectedServiceTemplateNames.add(serviceTemplateFileName1);
        verifyComputeConsolidationData(consolidationData, serviceTemplateFileName1, expectedServiceTemplateNames);

    }


    @Test
    public void testAddSameConsolidationDataTwice_noNewCreated() {

        ComputeConsolidationData consolidationData = new ComputeConsolidationData();
        consolidationData.addComputeTemplateConsolidationData(serviceTemplateFileName1,
                        computeNodeType1, computeNodeTemplateId1);
        consolidationData.getFileComputeConsolidationData(serviceTemplateFileName1);

        Set<String> expectedServiceTemplateNames = new HashSet();
        expectedServiceTemplateNames.add(serviceTemplateFileName1);
        verifyComputeConsolidationData(consolidationData, serviceTemplateFileName1, expectedServiceTemplateNames);

        consolidationData.addComputeTemplateConsolidationData(serviceTemplateFileName1,
                        computeNodeType1, computeNodeTemplateId1);
        consolidationData.getFileComputeConsolidationData(serviceTemplateFileName1);
        verifyComputeConsolidationData(consolidationData, serviceTemplateFileName1, expectedServiceTemplateNames);

    }

    @Test
    public void testAddDiffConsolidationData_DiffServiceTemplate() {
        ComputeConsolidationData consolidationData = new ComputeConsolidationData();

        consolidationData.addComputeTemplateConsolidationData(serviceTemplateFileName1,
                computeNodeType1, computeNodeTemplateId1);
        Set<String> expectedServiceTemplateNames =  new HashSet();
        expectedServiceTemplateNames.add(serviceTemplateFileName1);
        verifyComputeConsolidationData(consolidationData, serviceTemplateFileName1, expectedServiceTemplateNames);

        consolidationData.addComputeTemplateConsolidationData(serviceTemplateFileName2,
                computeNodeType2, computeNodeTemplateId2);
        expectedServiceTemplateNames.add(serviceTemplateFileName2);
        verifyComputeConsolidationData(consolidationData, serviceTemplateFileName2, expectedServiceTemplateNames);

    }

    @Test
    public void testAddDiffConsolidationData_SameServiceTemplate_DiffNodeTypes() {
        ComputeConsolidationData consolidationData = new ComputeConsolidationData();

        consolidationData.addComputeTemplateConsolidationData(serviceTemplateFileName1,
                        computeNodeType1, computeNodeTemplateId1);
        Set<String> expectedServiceTemplateNames = new HashSet();
        expectedServiceTemplateNames.add(serviceTemplateFileName1);
        verifyComputeConsolidationData(consolidationData, serviceTemplateFileName1, expectedServiceTemplateNames);

        consolidationData.addComputeTemplateConsolidationData(serviceTemplateFileName1,
                        computeNodeType2, computeNodeTemplateId2);
        verifyComputeConsolidationData(consolidationData, serviceTemplateFileName1, expectedServiceTemplateNames);

    }

    private void verifyComputeConsolidationData(ComputeConsolidationData consolidationData,
            String serviceTemplateFileName, Set<String> expectedServiceTemplateNames) {

        FileComputeConsolidationData fileComputeConsolidationData = consolidationData
                .getFileComputeConsolidationData(serviceTemplateFileName);
        Assert.assertNotNull(fileComputeConsolidationData);
        verifyGetAllServiceTemplateFileNames(consolidationData, expectedServiceTemplateNames);
    }

    private void verifyGetAllServiceTemplateFileNames(ComputeConsolidationData consolidationData,
                Set<String> expectedServiceTemplateNames) {
        Set<String> allServiceTemplateFileNames = consolidationData.getAllServiceTemplateFileNames();
        Assert.assertNotNull(allServiceTemplateFileNames);
        Assert.assertEquals(allServiceTemplateFileNames.size(), expectedServiceTemplateNames.size());
        Assert.assertTrue(allServiceTemplateFileNames.containsAll(expectedServiceTemplateNames));
    }


}
