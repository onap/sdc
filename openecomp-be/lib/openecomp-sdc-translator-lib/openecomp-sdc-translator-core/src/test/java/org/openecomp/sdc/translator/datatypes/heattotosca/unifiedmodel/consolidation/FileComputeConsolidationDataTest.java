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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class FileComputeConsolidationDataTest {

    private static final String COMPUTE_NODE_TEMPLATE_ID_1 = "computeNodeTemplateId1";
    private static final String COMPUTE_NODE_TEMPLATE_ID_2 = "computeNodeTemplateId2";
    private final FileComputeConsolidationData consolidationData = new FileComputeConsolidationData();

    private enum ComputeNodeTypeEnum {
        COMPUTE_NODE_TYPE_1,
        COMPUTE_NODE_TYPE_2
    }

    private final EnumMap<ComputeNodeTypeEnum, TypeComputeConsolidationData> mockMap =
            new EnumMap<>(ComputeNodeTypeEnum.class);

    @Mock
    private TypeComputeConsolidationData mockTypeComputeConsolidationData1;
    @Mock
    private TypeComputeConsolidationData mockTypeComputeConsolidationData2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        addMocksToMap();
    }

    @Test
    public void testAddComputeTemplateConsolidationData() {
        String computeNodeType = ComputeNodeTypeEnum.COMPUTE_NODE_TYPE_1.name();
        consolidationData.addComputeTemplateConsolidationData(computeNodeType, COMPUTE_NODE_TEMPLATE_ID_1);
        Set<String> expectedComputeNodeTypes = new HashSet<>();
        expectedComputeNodeTypes.add(computeNodeType);
        checkComputeConsolidationData(consolidationData, computeNodeType, expectedComputeNodeTypes);
    }

    @Test
    public void testAddSameConsolidationDataTwice_noNewCreated() {
        String computeNodeType = ComputeNodeTypeEnum.COMPUTE_NODE_TYPE_1.name();
        consolidationData.addComputeTemplateConsolidationData(computeNodeType, COMPUTE_NODE_TEMPLATE_ID_1);
        TypeComputeConsolidationData firstTypeComputeConsolidationData =
                consolidationData.getTypeComputeConsolidationData(computeNodeType);

        consolidationData.addComputeTemplateConsolidationData(computeNodeType, COMPUTE_NODE_TEMPLATE_ID_1);
        TypeComputeConsolidationData secondTypeComputeConsolidationData =
                consolidationData.getTypeComputeConsolidationData(computeNodeType);
        Assert.assertEquals(firstTypeComputeConsolidationData, secondTypeComputeConsolidationData);
    }

    @Test
    public void testAddConsolidationData_diffNodeType() {
        String computeNodeType1 = ComputeNodeTypeEnum.COMPUTE_NODE_TYPE_1.name();
        consolidationData.addComputeTemplateConsolidationData(computeNodeType1, COMPUTE_NODE_TEMPLATE_ID_1);
        Set<String> expectedComputeNodeTypes = new HashSet<>();
        expectedComputeNodeTypes.add(computeNodeType1);
        checkComputeConsolidationData(consolidationData, computeNodeType1, expectedComputeNodeTypes);

        String computeNodeType2 = ComputeNodeTypeEnum.COMPUTE_NODE_TYPE_2.name();
        consolidationData.addComputeTemplateConsolidationData(computeNodeType2, COMPUTE_NODE_TEMPLATE_ID_2);
        expectedComputeNodeTypes.add(computeNodeType2);
        checkComputeConsolidationData(consolidationData, computeNodeType2, expectedComputeNodeTypes);
    }

    @Test
    public void testAddConsolidationData_diffNodeTemplateId() {
        String computeNodeType = ComputeNodeTypeEnum.COMPUTE_NODE_TYPE_1.name();
        consolidationData.addComputeTemplateConsolidationData(computeNodeType, COMPUTE_NODE_TEMPLATE_ID_1);
        Set<String> expectedComputeNodeTypes = new HashSet<>();
        expectedComputeNodeTypes.add(computeNodeType);
        checkComputeConsolidationData(consolidationData, computeNodeType, expectedComputeNodeTypes);

        consolidationData.addComputeTemplateConsolidationData(computeNodeType, COMPUTE_NODE_TEMPLATE_ID_2);
        checkComputeConsolidationData(consolidationData, computeNodeType, expectedComputeNodeTypes);
    }

    @Test
    public void testAddSameConsolidationDataTwice_testWithMock() throws Exception {
        String computeNodeType = ComputeNodeTypeEnum.COMPUTE_NODE_TYPE_1.name();
        addAndCheckComputeTemplateConsolidationData(computeNodeType, COMPUTE_NODE_TEMPLATE_ID_1);
        addAndCheckComputeTemplateConsolidationData(computeNodeType, COMPUTE_NODE_TEMPLATE_ID_1, 2);
    }

    @Test
    public void testAddConsolidationData_diffNodeType_testWithMock() throws Exception {
        String computeNodeType = ComputeNodeTypeEnum.COMPUTE_NODE_TYPE_1.name();
        addAndCheckComputeTemplateConsolidationData(computeNodeType, COMPUTE_NODE_TEMPLATE_ID_1);
        addAndCheckComputeTemplateConsolidationData(computeNodeType, COMPUTE_NODE_TEMPLATE_ID_2);
    }

    @Test
    public void testAddConsolidationData_diffNodeTemplateId_testWithMock() throws Exception {
        String computeNodeType = ComputeNodeTypeEnum.COMPUTE_NODE_TYPE_1.name();
        addAndCheckComputeTemplateConsolidationData(computeNodeType, COMPUTE_NODE_TEMPLATE_ID_1);
        addAndCheckComputeTemplateConsolidationData(computeNodeType, COMPUTE_NODE_TEMPLATE_ID_2);
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

    private void addAndCheckComputeTemplateConsolidationData(
            String computeNodeType, String computeNodeTemplateId) throws Exception {
        addAndCheckComputeTemplateConsolidationData(computeNodeType,
                computeNodeTemplateId, 1);
    }

    private void addAndCheckComputeTemplateConsolidationData(String computeNodeType,
            String computeNodeTemplateId, int expectedTime) throws Exception {
        TypeComputeConsolidationData fileComputeConsolidationDataMock =
                setTypeComputeConsolidationDataMock(computeNodeType);

        consolidationData.addComputeTemplateConsolidationData(
                computeNodeType, computeNodeTemplateId);

        Mockito.verify(fileComputeConsolidationDataMock, Mockito.times(expectedTime))
               .addComputeTemplateConsolidationData(computeNodeTemplateId);
    }

    private void addMocksToMap() {
        mockMap.put(ComputeNodeTypeEnum.COMPUTE_NODE_TYPE_1, mockTypeComputeConsolidationData1);
        mockMap.put(ComputeNodeTypeEnum.COMPUTE_NODE_TYPE_2, mockTypeComputeConsolidationData2);
    }

    private TypeComputeConsolidationData setTypeComputeConsolidationDataMock(
            String computeType) throws Exception {
        TypeComputeConsolidationData typeComputeConsolidationDataMock =
                getFileComputeConsolidationDataMock(computeType);
        consolidationData.setTypeComputeConsolidationData(computeType, typeComputeConsolidationDataMock);
        return typeComputeConsolidationDataMock;
    }

    private TypeComputeConsolidationData getFileComputeConsolidationDataMock(String computeType) throws Exception {
        ComputeNodeTypeEnum enumValue = ComputeNodeTypeEnum.valueOf(computeType);
        TypeComputeConsolidationData mock = mockMap.get(enumValue);
        if (mock == null) {
            throw new Exception("This compute Type doesn't support. "
                                        + "Please add it to ComputeNodeTypeEnum enum");
        }
        return mock;
    }

}
