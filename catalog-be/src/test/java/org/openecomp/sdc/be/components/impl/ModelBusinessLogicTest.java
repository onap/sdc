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
package org.openecomp.sdc.be.components.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.exception.BusinessException;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class ModelBusinessLogicTest {

    @InjectMocks
    private ModelBusinessLogic modelBusinessLogic;
    @Mock
    private ModelOperation modelOperation;
    private Model model;

    @BeforeAll
    void setup() {
        MockitoAnnotations.openMocks(this);
        initTestData();
    }

    private void initTestData() {
        model = new Model("ETSI-SDC-MODEL-TEST");
    }

    @Test
    @Order(1)
    void createModelTest() {
        when(modelOperation.createModel(model, false)).thenReturn(model);
        final Model result = modelBusinessLogic.createModel(model);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(model.getName());
    }

    @Test
    @Order(2)
    void createModelFailTest() {
        when(modelOperation.createModel(model, false))
            .thenThrow(new OperationException(ActionStatus.MODEL_ALREADY_EXISTS, model.getName()));
        final BusinessException exception = assertThrows(BusinessException.class, () -> modelBusinessLogic.createModel(model));
        assertThat(((OperationException) exception).getActionStatus().name()).isEqualTo(ActionStatus.MODEL_ALREADY_EXISTS.name());
        assertThat(((OperationException) exception).getParams()).contains(model.getName());
    }

}