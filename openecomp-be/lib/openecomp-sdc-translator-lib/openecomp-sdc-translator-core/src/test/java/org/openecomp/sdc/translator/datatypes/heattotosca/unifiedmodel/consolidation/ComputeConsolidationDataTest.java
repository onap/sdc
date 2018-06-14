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

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ComputeConsolidationDataTest {

    private static final String COMPUTE_NODE_TEMPLATE_ID_1 = "computeNodeTemplateId1";
    private static final String COMPUTE_NODE_TEMPLATE_ID_2 = "computeNodeTemplateId2";
    private static final String COMPUTE_NODE_TYPE_1 = "computeNodeType1";
    private static final String COMPUTE_NODE_TYPE_2 = "computeNodeType2";

    private final EnumMap<ServiceTemplateFileNameEnum, FileComputeConsolidationData> mockMap =
            new EnumMap<>(ServiceTemplateFileNameEnum.class);

    @Mock
    private FileComputeConsolidationData mockFileComputeConsolidationData1;
    @Mock
    private FileComputeConsolidationData mockFileComputeConsolidationData2;

    private final ComputeConsolidationData consolidationData = new ComputeConsolidationData();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        addMocksToMap();
    }

    @Test
    public void testAddComputeTemplateConsolidationData() {
        String serviceTemplate = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        consolidationData.addComputeTemplateConsolidationData(
                serviceTemplate, COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);

        Set<String> expectedServiceTemplateNames = new HashSet<>();
        expectedServiceTemplateNames.add(serviceTemplate);
        checkComputeConsolidationData(consolidationData, serviceTemplate,
                expectedServiceTemplateNames);
    }

    @Test
    public void testAddSameConsolidationDataTwice_noNewCreated() {
        String serviceTemplate = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        consolidationData.addComputeTemplateConsolidationData(serviceTemplate,
                COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        Set<String> expectedServiceTemplateNames = new HashSet<>();
        expectedServiceTemplateNames.add(serviceTemplate);
        checkComputeConsolidationData(consolidationData, serviceTemplate, expectedServiceTemplateNames);

        consolidationData.addComputeTemplateConsolidationData(serviceTemplate,
                COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        checkComputeConsolidationData(consolidationData, serviceTemplate, expectedServiceTemplateNames);
    }

    @Test
    public void testAddDiffConsolidationData_diffNodeTypes() {
        String serviceTemplate = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        consolidationData.addComputeTemplateConsolidationData(serviceTemplate,
                COMPUTE_NODE_TYPE_1, COMPUTE_NODE_TEMPLATE_ID_1);
        Set<String> expectedServiceTemplateNames = new HashSet<>();
        expectedServiceTemplateNames.add(serviceTemplate);
        checkComputeConsolidationData(consolidationData, serviceTemplate, expectedServiceTemplateNames);

        consolidationData.addComputeTemplateConsolidationData(serviceTemplate,
                COMPUTE_NODE_TYPE_2, COMPUTE_NODE_TEMPLATE_ID_2);
        checkComputeConsolidationData(consolidationData, serviceTemplate, expectedServiceTemplateNames);
    }

    @Test
    public void testAddDiffConsolidationData_diffServiceTemplate() {
        String serviceTemplate1 = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        consolidationData.addComputeTemplateConsolidationData(serviceTemplate1, COMPUTE_NODE_TYPE_1,
                COMPUTE_NODE_TEMPLATE_ID_1);
        Set<String> expectedServiceTemplateNames =  new HashSet<>();
        expectedServiceTemplateNames.add(serviceTemplate1);
        checkComputeConsolidationData(consolidationData, serviceTemplate1, expectedServiceTemplateNames);

        String serviceTemplate2 = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_2.name();
        consolidationData.addComputeTemplateConsolidationData(serviceTemplate2, COMPUTE_NODE_TYPE_2,
                COMPUTE_NODE_TEMPLATE_ID_2);
        expectedServiceTemplateNames.add(serviceTemplate2);
        checkComputeConsolidationData(consolidationData, serviceTemplate2, expectedServiceTemplateNames);
    }

    @Test
    public void testAddSameConsolidationDataTwice_testWithMock() throws Exception {
        String serviceTemplate = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        addAndCheckComputeTemplateConsolidationData(serviceTemplate, COMPUTE_NODE_TYPE_1,
                COMPUTE_NODE_TEMPLATE_ID_1);
        addAndCheckComputeTemplateConsolidationData(serviceTemplate, COMPUTE_NODE_TYPE_1,
                COMPUTE_NODE_TEMPLATE_ID_1, 2);
    }

    @Test
    public void testAddDiffConsolidationData_diffNodeTypes_testWithMock() throws Exception {
        String serviceTemplate = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        addAndCheckComputeTemplateConsolidationData(serviceTemplate, COMPUTE_NODE_TYPE_1,
                COMPUTE_NODE_TEMPLATE_ID_1);
        addAndCheckComputeTemplateConsolidationData(serviceTemplate, COMPUTE_NODE_TYPE_2,
                COMPUTE_NODE_TEMPLATE_ID_2);
    }

    @Test
    public void testAddDiffConsolidationData_diffServiceTemplate_testWithMock() throws Exception {
        String serviceTemplate1 = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        addAndCheckComputeTemplateConsolidationData(serviceTemplate1, COMPUTE_NODE_TYPE_1,
                COMPUTE_NODE_TEMPLATE_ID_1);
        String serviceTemplate2 = ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1.name();
        addAndCheckComputeTemplateConsolidationData(serviceTemplate2, COMPUTE_NODE_TYPE_2,
                COMPUTE_NODE_TEMPLATE_ID_2);
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

    private void addAndCheckComputeTemplateConsolidationData(String serviceTemplateFileName,
            String computeNodeType, String computeNodeTemplateId) throws Exception {
        addAndCheckComputeTemplateConsolidationData(serviceTemplateFileName, computeNodeType,
                computeNodeTemplateId, 1);
    }

    private void addAndCheckComputeTemplateConsolidationData(String serviceTemplateFileName,
            String computeNodeType, String computeNodeTemplateId, int expectedTime) throws Exception {
        FileComputeConsolidationData fileComputeConsolidationDataMock =
                setFileComputeConsolidationDataMock(serviceTemplateFileName);
        consolidationData.addComputeTemplateConsolidationData(
                serviceTemplateFileName, computeNodeType, computeNodeTemplateId);

        Mockito.verify(fileComputeConsolidationDataMock, Mockito.times(expectedTime))
               .addComputeTemplateConsolidationData(computeNodeType, computeNodeTemplateId);
    }

    private FileComputeConsolidationData setFileComputeConsolidationDataMock(
            String serviceTemplateName) throws Exception {
        FileComputeConsolidationData mock = getFileComputeConsolidationDataMock(serviceTemplateName);
        consolidationData.setFileComputeConsolidationData(serviceTemplateName, mock);
        return mock;
    }

    private void addMocksToMap() {
        mockMap.put(ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_1, mockFileComputeConsolidationData1);
        mockMap.put(ServiceTemplateFileNameEnum.SERVICE_TEMPLATE_FILE_NAME_2, mockFileComputeConsolidationData2);
    }

    private FileComputeConsolidationData getFileComputeConsolidationDataMock(String serviceTemplateName)
            throws Exception {
        ServiceTemplateFileNameEnum enumValue = ServiceTemplateFileNameEnum.valueOf(serviceTemplateName);
        FileComputeConsolidationData mock = mockMap.get(enumValue);
        if (mock == null) {
            throw new Exception("This service Template File Name doesn't supported. "
                                        + "Please add it to ServiceTemplateFileName enum");
        }
        return mock;
    }

}
