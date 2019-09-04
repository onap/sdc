/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fj.data.Either;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.StorageException;

@RunWith(MockitoJUnitRunner.class)
public class OperationUtilsTest {

    public static final String ID = "ID";
    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    private PropertyOperation propertyOperation;

    private JanusGraphOperationStatus status = JanusGraphOperationStatus.OK;

    @Test(expected = StorageException.class)
    public void shouldThrowStorageException() {
        OperationUtils operationUtils = new OperationUtils(janusGraphDao);
        Mockito.when(janusGraphDao.rollback()).thenReturn(status);
        operationUtils.onJanusGraphOperationFailure(status);
    }

    @Test
    public void shouldFillPropertiesAndReceiveLeftProjection() {
        NodeTypeEnum capability = NodeTypeEnum.Capability;
        Map<String, PropertyDefinition> definitions = Collections.emptyMap();
        Mockito.when(propertyOperation.findPropertiesOfNode(capability, ID)).thenReturn(Either.left(definitions));
        Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> result =
                OperationUtils.fillProperties(ID, propertyOperation, capability);
        assertTrue(result.isLeft());
        assertEquals(result.left().value(), definitions);
    }

    @Test
    public void shouldFillPropertiesAndReceiveRightProjectionWithAlreadyExistStatus() {
        NodeTypeEnum capability = NodeTypeEnum.Capability;
        Mockito.when(propertyOperation.findPropertiesOfNode(capability, ID))
                .thenReturn(Either.right(JanusGraphOperationStatus.ALREADY_EXIST));
        Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> result =
                OperationUtils.fillProperties(ID, propertyOperation, capability);
        assertTrue(result.isRight());
        assertEquals(result.right().value(), JanusGraphOperationStatus.ALREADY_EXIST);
    }

    @Test
    public void shouldFillPropertiesAndReceiveRightProjectionWithOKStatus() {
        NodeTypeEnum capability = NodeTypeEnum.Capability;
        Mockito.when(propertyOperation.findPropertiesOfNode(capability, ID))
                .thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> result =
                OperationUtils.fillProperties(ID, propertyOperation, capability);
        assertTrue(result.isRight());
        assertEquals(result.right().value(), JanusGraphOperationStatus.OK);
    }
}