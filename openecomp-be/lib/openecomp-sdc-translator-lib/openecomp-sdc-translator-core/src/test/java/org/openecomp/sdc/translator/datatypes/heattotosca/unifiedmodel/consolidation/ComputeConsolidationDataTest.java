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
import org.junit.Test;

public class ComputeConsolidationDataTest {

    private static final String SERVICE_TEMPLATE_FILE_NAME_1 = "serviceTemplateFileName1";
    private static final String SERVICE_TEMPLATE_FILE_NAME_2 = "serviceTemplateFileName2";
    private static final String COMPUTE_NODE_TEMPLATE_ID_1 = "computeNodeTemplateId1";
    private static final String COMPUTE_NODE_TEMPLATE_ID_2 = "computeNodeTemplateId2";
    private static final String COMPUTE_NODE_TYPE_1 = "computeNodeType1";
    private static final String COMPUTE_NODE_TYPE_2 = "computeNodeType2";
    private static final String MAIN_SERVICE_TEMPLATE = "MainServiceTemplate.yaml";
    private static final String SERVER_OAM = "server_oam";

    @Test
    public void testAddComputeTemplateConsolidationData() {
        ComputeConsolidationData consolidationData = new ComputeConsolidationData();
        consolidationData.addComputeTemplateConsolidationData(SERVICE_TEMPLATE_FILE_NAME_1,
                COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);

        Set<String> expectedServiceTemplateNames = new HashSet<>();
        expectedServiceTemplateNames.add(SERVICE_TEMPLATE_FILE_NAME_1);
        checkComputeConsolidationData(consolidationData, SERVICE_TEMPLATE_FILE_NAME_1, expectedServiceTemplateNames);
    }

    @Test
    public void testAddSameConsolidationDataTwice_noNewCreated() {
        ComputeConsolidationData consolidationData = new ComputeConsolidationData();
        consolidationData.addComputeTemplateConsolidationData(SERVICE_TEMPLATE_FILE_NAME_1,
                COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        consolidationData.getFileComputeConsolidationData(SERVICE_TEMPLATE_FILE_NAME_1);

        Set<String> expectedServiceTemplateNames = new HashSet<>();
        expectedServiceTemplateNames.add(SERVICE_TEMPLATE_FILE_NAME_1);
        checkComputeConsolidationData(consolidationData, SERVICE_TEMPLATE_FILE_NAME_1, expectedServiceTemplateNames);

        consolidationData.addComputeTemplateConsolidationData(SERVICE_TEMPLATE_FILE_NAME_1,
                COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        consolidationData.getFileComputeConsolidationData(SERVICE_TEMPLATE_FILE_NAME_1);
        checkComputeConsolidationData(consolidationData, SERVICE_TEMPLATE_FILE_NAME_1, expectedServiceTemplateNames);
    }

    @Test
    public void testAddDiffConsolidationData_SameServiceTemplate_DiffNodeTypes() {
        ComputeConsolidationData consolidationData = new ComputeConsolidationData();

        consolidationData.addComputeTemplateConsolidationData(SERVICE_TEMPLATE_FILE_NAME_1,
                COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        Set<String> expectedServiceTemplateNames = new HashSet<>();
        expectedServiceTemplateNames.add(SERVICE_TEMPLATE_FILE_NAME_1);
        checkComputeConsolidationData(consolidationData, SERVICE_TEMPLATE_FILE_NAME_1, expectedServiceTemplateNames);

        consolidationData.addComputeTemplateConsolidationData(SERVICE_TEMPLATE_FILE_NAME_1,
                COMPUTE_NODE_TYPE_2, COMPUTE_NODE_TEMPLATE_ID_2);
        checkComputeConsolidationData(consolidationData, SERVICE_TEMPLATE_FILE_NAME_1, expectedServiceTemplateNames);
    }

    @Test
    public void testAddDiffConsolidationData_DiffServiceTemplate() {
        ComputeConsolidationData consolidationData = new ComputeConsolidationData();

        consolidationData.addComputeTemplateConsolidationData(SERVICE_TEMPLATE_FILE_NAME_1,
                COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        Set<String> expectedServiceTemplateNames =  new HashSet<>();
        expectedServiceTemplateNames.add(SERVICE_TEMPLATE_FILE_NAME_1);
        checkComputeConsolidationData(consolidationData, SERVICE_TEMPLATE_FILE_NAME_1, expectedServiceTemplateNames);

        consolidationData.addComputeTemplateConsolidationData(SERVICE_TEMPLATE_FILE_NAME_2,
                COMPUTE_NODE_TYPE_2, COMPUTE_NODE_TEMPLATE_ID_2);
        expectedServiceTemplateNames.add(SERVICE_TEMPLATE_FILE_NAME_2);
        checkComputeConsolidationData(consolidationData, SERVICE_TEMPLATE_FILE_NAME_2, expectedServiceTemplateNames);

    }

    private void checkComputeConsolidationData(ComputeConsolidationData consolidationData,
            String serviceTemplateFileName, Set<String> expectedServiceTemplateNames) {

        FileComputeConsolidationData fileComputeConsolidationData = consolidationData
                .getFileComputeConsolidationData(serviceTemplateFileName);
        Assert.assertNotNull(fileComputeConsolidationData);
        checkGetAllServiceTemplateFileNames(consolidationData, expectedServiceTemplateNames);
    }

    private void checkGetAllServiceTemplateFileNames(ComputeConsolidationData consolidationData,
                Set<String> expectedServiceTemplateNames) {
        Set<String> allServiceTemplateFileNames = consolidationData.getAllServiceTemplateFileNames();
        Assert.assertNotNull(allServiceTemplateFileNames);
        Assert.assertEquals(allServiceTemplateFileNames.size(), expectedServiceTemplateNames.size());
        Assert.assertTrue(allServiceTemplateFileNames.containsAll(expectedServiceTemplateNames));
    }

    @Test
    public void isNumberOfComputeTypesLegalPositive() {
        ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        typeComputeConsolidationData.setComputeTemplateConsolidationData(SERVER_OAM + "_1", computeTemplateConsolidationData);

        FileComputeConsolidationData fileComputeConsolidationData = new FileComputeConsolidationData();
        fileComputeConsolidationData.setTypeComputeConsolidationData(SERVER_OAM, typeComputeConsolidationData);

        ComputeConsolidationData computeConsolidationData = new ComputeConsolidationData();
        computeConsolidationData.setFileComputeConsolidationData(MAIN_SERVICE_TEMPLATE, fileComputeConsolidationData);

        Assert.assertTrue(computeConsolidationData.isNumberOfComputeTypesLegal(MAIN_SERVICE_TEMPLATE));
    }

    @Test
    public void isNumberOfComputeTypesLegalNegative() {
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        typeComputeConsolidationData.setComputeTemplateConsolidationData(SERVER_OAM + "_1", new ComputeTemplateConsolidationData());
        typeComputeConsolidationData.setComputeTemplateConsolidationData(SERVER_OAM + "_2", new ComputeTemplateConsolidationData());


        FileComputeConsolidationData fileComputeConsolidationData = new FileComputeConsolidationData();
        fileComputeConsolidationData.setTypeComputeConsolidationData(SERVER_OAM, typeComputeConsolidationData);

        ComputeConsolidationData computeConsolidationData = new ComputeConsolidationData();
        computeConsolidationData.setFileComputeConsolidationData(MAIN_SERVICE_TEMPLATE, fileComputeConsolidationData);

        Assert.assertFalse(computeConsolidationData.isNumberOfComputeTypesLegal(MAIN_SERVICE_TEMPLATE));
    }

}
