/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.model.operations.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ModelTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.resources.data.DataTypeData;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:application-context-test.xml")
class DataTypeOperationTest {

    @InjectMocks
    private DataTypeOperation dataTypeOperation;
    @Mock
    private ModelOperation modelOperation;
    @Mock
    private HealingJanusGraphGenericDao janusGraphGenericDao;

    private final String modelName = "ETSI-SDC-MODEL-TEST";
    private final List<DataTypeData> dataTypesWithoutModel = new ArrayList<>();
    private final List<DataTypeData> dataTypesWithModel = new ArrayList<>();
    final Map<String, Map<String, DataTypeDefinition>> dataTypesMappedByModel = new HashMap<>();
    final Map<String, DataTypeDefinition> allDataTypesFoundDefinitionMap = new HashMap<>();
    private Model model;

    @BeforeEach
    void beforeEachInit() {
        MockitoAnnotations.openMocks(this);
        initTestData();
    }

    @Test
    void getAllDataTypeNodesTest() {
        when(janusGraphGenericDao.getByCriteria(NodeTypeEnum.DataType, null, DataTypeData.class))
            .thenReturn(Either.left(dataTypesWithoutModel));
        when(modelOperation.findAllModels()).thenReturn(Collections.singletonList(model));
        when(janusGraphGenericDao.getByCriteriaForModel(NodeTypeEnum.DataType, null, modelName, DataTypeData.class))
            .thenReturn(Either.left(dataTypesWithModel));
        final var dataTypesFound = dataTypeOperation.getAllDataTypeNodes();
        assertThat(dataTypesFound.size()).isEqualTo(4);
        assertThat(dataTypesFound.containsAll(dataTypesWithoutModel)).isTrue();
        assertThat(dataTypesFound.containsAll(dataTypesWithModel)).isTrue();
    }

    @Test
    void getAllDataTypesWithModelTest() {
        when(janusGraphGenericDao.getByCriteria(NodeTypeEnum.DataType, null, DataTypeData.class))
            .thenReturn(Either.left(Collections.emptyList()));
        when(modelOperation.findAllModels()).thenReturn(Collections.singletonList(model));
        when(janusGraphGenericDao.getByCriteriaForModel(NodeTypeEnum.DataType, null, modelName, DataTypeData.class))
            .thenReturn(Either.left(dataTypesWithModel));
        final var dataTypesFound = dataTypeOperation.getAllDataTypeNodes();
        assertThat(dataTypesFound.size()).isEqualTo(2);
        assertThat(dataTypesFound.containsAll(dataTypesWithModel)).isTrue();
        assertThat(dataTypesFound.containsAll(dataTypesWithoutModel)).isFalse();
    }

    @Test
    void getAllDataTypeNodesWithValidationErrorTest() {
        when(janusGraphGenericDao.getByCriteria(NodeTypeEnum.DataType, null, DataTypeData.class))
            .thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        final var dataTypesFound = dataTypeOperation.getAllDataTypeNodes();
        assertThat(dataTypesFound.isEmpty()).isTrue();
    }

    @Test
    void getAllDataTypesWithModelWithValidationErrorTest() {
        when(janusGraphGenericDao.getByCriteria(NodeTypeEnum.DataType, null, DataTypeData.class))
            .thenReturn(Either.left(Collections.emptyList()));
        when(modelOperation.findAllModels()).thenReturn(Collections.singletonList(model));
        when(janusGraphGenericDao.getByCriteriaForModel(NodeTypeEnum.DataType, null, modelName, DataTypeData.class))
            .thenReturn(Either.right(JanusGraphOperationStatus.GENERAL_ERROR));
        final var dataTypesFound = dataTypeOperation.getAllDataTypeNodes();
        assertThat(dataTypesFound).isEmpty();
    }

    @Test
    void getDataTypeByUidTest_Success() {
        doReturn(Either.left(createDataTypeData("test.data.type99", "test.data.type00099", 888L, 999L, modelName)))
            .when(janusGraphGenericDao).getNode(eq("uid"), eq("dataType"), any());
        final Optional<DataTypeDataDefinition> dataType = dataTypeOperation.getDataTypeByUid("dataType");
        assertTrue(dataType.isPresent());
        assertEquals("test.data.type99", dataType.get().getName());
        assertEquals("test.data.type00099", dataType.get().getUniqueId());
        assertEquals(modelName, dataType.get().getModel());
    }

    @Test
    void getDataTypeByUidTest_Fail() {
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND)).when(janusGraphGenericDao).getNode(eq("uid"), eq("dataType"), any());
        Optional<DataTypeDataDefinition> result = dataTypeOperation.getDataTypeByUid("dataType");
        assertTrue(result.isEmpty());
    }

    private void initTestData() {
        model = new Model(modelName, ModelTypeEnum.NORMATIVE);
        final String TEST_DATA_TYPE_001 = "test.data.type001";
        final String TEST_DATA_TYPE_002 = "test.data.type002";
        final String TEST_DATA_TYPE_003 = "test.data.type003";
        final String TEST_DATA_TYPE_004 = "test.data.type004";
        final DataTypeData dataTypeData1 = createDataTypeData("test.data.type1", TEST_DATA_TYPE_001, 101L,
            101L, null);
        final DataTypeData dataTypeData2 = createDataTypeData("test.data.type2", TEST_DATA_TYPE_002, 101L,
            1002L, null);
        dataTypesWithoutModel.add(dataTypeData1);
        dataTypesWithoutModel.add(dataTypeData2);

        final DataTypeData dataTypeWithModel1 = createDataTypeData("test.data.type1", TEST_DATA_TYPE_003, 101L,
            101L, modelName);
        final DataTypeData dataTypeWithModel2 = createDataTypeData("test.data.type2", TEST_DATA_TYPE_004, 101L,
            1002L, modelName);
        dataTypesWithModel.add(dataTypeWithModel1);
        dataTypesWithModel.add(dataTypeWithModel2);

        allDataTypesFoundDefinitionMap.put(TEST_DATA_TYPE_001, createDataTypeDefinition("test.data.type1", TEST_DATA_TYPE_001,
            101L, 101L, null));
        allDataTypesFoundDefinitionMap.put(TEST_DATA_TYPE_002, createDataTypeDefinition("test.data.type2", TEST_DATA_TYPE_002,
            101L, 101L, null));
        allDataTypesFoundDefinitionMap.put(TEST_DATA_TYPE_003, createDataTypeDefinition("test.data.type1", TEST_DATA_TYPE_003,
            101L, 101L, modelName));
        allDataTypesFoundDefinitionMap.put(TEST_DATA_TYPE_004, createDataTypeDefinition("test.data.type2", TEST_DATA_TYPE_004,
            101L, 101L, modelName));

        dataTypesMappedByModel.put(null, allDataTypesFoundDefinitionMap);
    }

    private DataTypeData createDataTypeData(final String name, final String uniqueId, final long creationTime, final long modificationTime,
                                            final String model) {
        final DataTypeData dataTypeData = new DataTypeData();
        dataTypeData.setDataTypeDataDefinition(createDataTypeDefinition(name, uniqueId, creationTime, modificationTime, model));
        return dataTypeData;
    }

    private DataTypeDefinition createDataTypeDefinition(final String name, final String uniqueId, final long creationTime,
                                                        final long modificationTime, String model) {
        final DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
        dataTypeDefinition.setName(name);
        dataTypeDefinition.setUniqueId(uniqueId);
        dataTypeDefinition.setCreationTime(creationTime);
        dataTypeDefinition.setModificationTime(modificationTime);
        dataTypeDefinition.setModel(model);
        return dataTypeDefinition;
    }

}
