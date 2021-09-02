/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.model.Model;

class ModelElementOperationTest {

    @Mock
    private JanusGraphGenericDao janusGraphGenericDao;
    @Mock
    private DataTypeOperation dataTypeOperation;
    @Mock
    private PolicyTypeOperation policyTypeOperation;
    @InjectMocks
    private ModelElementOperation modelElementOperation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deleteModelElements_noTransactionSuccessTest() {
        var model = new Model("name");
        final String modelId = UniqueIdBuilder.buildModelUid(model.getName());
        modelElementOperation.deleteModelElements(model, false);
        verify(dataTypeOperation).deleteDataTypesByModelId(modelId);
        verify(policyTypeOperation).deletePolicyTypesByModelId(modelId);
        verify(janusGraphGenericDao).commit();
        verify(janusGraphGenericDao, times(0)).rollback();
    }

    @Test
    void deleteModelElements_inTransactionSuccessTest() {
        var model = new Model("name");
        final String modelId = UniqueIdBuilder.buildModelUid(model.getName());
        modelElementOperation.deleteModelElements(model, true);
        verify(dataTypeOperation).deleteDataTypesByModelId(modelId);
        verify(policyTypeOperation).deletePolicyTypesByModelId(modelId);
        verify(janusGraphGenericDao, times(0)).commit();
        verify(janusGraphGenericDao, times(0)).rollback();
    }

    @Test
    void deleteModelElements_noTransactionErrorTest() {
        var model = new Model("name");
        final String modelId = UniqueIdBuilder.buildModelUid(model.getName());
        doThrow(new RuntimeException()).when(dataTypeOperation).deleteDataTypesByModelId(modelId);
        assertThrows(RuntimeException.class, () -> modelElementOperation.deleteModelElements(model, false));
        verify(dataTypeOperation).deleteDataTypesByModelId(modelId);
        verify(janusGraphGenericDao).rollback();
        verify(janusGraphGenericDao, times(0)).commit();
    }

    @Test
    void deleteModelElements_inTransactionErrorTest() {
        var model = new Model("name");
        final String modelId = UniqueIdBuilder.buildModelUid(model.getName());
        doThrow(new RuntimeException()).when(dataTypeOperation).deleteDataTypesByModelId(modelId);
        assertThrows(RuntimeException.class, () -> modelElementOperation.deleteModelElements(model, true));
        verify(dataTypeOperation).deleteDataTypesByModelId(modelId);
        verify(janusGraphGenericDao, times(0)).commit();
        verify(janusGraphGenericDao, times(0)).rollback();
    }
}