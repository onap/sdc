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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import fj.data.Either;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.resources.data.ModelData;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:application-context-test.xml")
@TestInstance(Lifecycle.PER_CLASS)
class ModelOperationTest extends ModelTestBase {

    @InjectMocks
    private ModelOperation modelOperation;
    @Mock
    private JanusGraphGenericDao janusGraphGenericDao;

    private final String modelName = "ETSI-SDC-MODEL-TEST";

    @BeforeAll
    void setup() {
        init();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createModelSuccessTest() {
        final ModelData modelData = new ModelData(modelName,  UniqueIdBuilder.buildModelUid(modelName));
        when(janusGraphGenericDao.createNode(any(),any())).thenReturn(Either.left(modelData));
        final Model createdModel = modelOperation.createModel(new Model(modelName), false);
        assertThat(createdModel).isNotNull();
        assertThat(createdModel.getName()).isEqualTo(modelName);
    }

    @Test
    void createModelFailWithModelAlreadyExistTest() {
        when(janusGraphGenericDao.createNode(any(),any())).thenReturn(Either.right(JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION));
        assertThrows(OperationException.class, () -> modelOperation.createModel(new Model(modelName), false));
    }

    @Test
    void createModelFailTest() {
        when(janusGraphGenericDao.createNode(any(),any())).thenReturn(Either.right(JanusGraphOperationStatus.GRAPH_IS_NOT_AVAILABLE));
        assertThrows(OperationException.class, () -> modelOperation.createModel(new Model(modelName), false));
    }

}
