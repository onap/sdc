/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fj.data.Either;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

public class ToscaElementLifecycleOperationTest {

    @Test
    @DisplayName("handleFailureToGetVertices - Golden Path")
    public void handleFailureToGetVertices_GoldenPath() {
        //Given
        Map<JanusGraphOperationStatus, StorageOperationStatus> map = getStatusMap();

        for (JanusGraphOperationStatus janusStatus : map.keySet()) {
            //When
            StorageOperationStatus result = ToscaElementLifecycleOperation
                .handleFailureToPrepareParameters(janusStatus, "testToscaElementId");

            //Then
            assertEquals(map.get(janusStatus), result);
        }
    }

    @Test
    @DisplayName("handleFailureToGetVertices - Null `toscaElementId`")
    public void handleFailureToGetVertices_NullToscaElementId() {
        //Given
        Map<JanusGraphOperationStatus, StorageOperationStatus> map = getStatusMap();

        for (JanusGraphOperationStatus janusStatus : map.keySet()) {
            //When
            StorageOperationStatus result = ToscaElementLifecycleOperation
                .handleFailureToPrepareParameters(janusStatus, "testToscaElementId");

            //Then
            assertEquals(map.get(janusStatus), result);
        }
    }

    @Test
    @DisplayName("handleFailureToGetVertices - Null `janusGraphOperationStatus`")
    public void handleFailureToGetVertices_NullJanusGraphOperationStatus() {
        //Given
        JanusGraphOperationStatus status = null;

        //When
        StorageOperationStatus result = ToscaElementLifecycleOperation
            .handleFailureToPrepareParameters(status, "testToscaElementId");

        //Then
        assertEquals(StorageOperationStatus.GENERAL_ERROR, result);
    }

    @Test
    @DisplayName("getToscaElementFromOperation - should call `operation.getToscaElement`")
    public void getToscaElementFromOperation_GoldenPath() {
        //Given
        String uniqueId = "uniqueId";
        String toscaElementId = "toscaElementId";

        Either<ToscaElement, StorageOperationStatus> expectedLeftResult = Either.left(
            new ToscaElement(ToscaElementTypeEnum.NODE_TYPE) {
            }
        );
        ToscaElementOperation leftResultOperation = getPseudoToscaOperation(expectedLeftResult);

        //When
        Either<ToscaElement, StorageOperationStatus> leftResult = ToscaElementLifecycleOperation
            .getToscaElementFromOperation(leftResultOperation, uniqueId, toscaElementId);

        //Then
        assertTrue(leftResult.isLeft());
        assertEquals(expectedLeftResult, leftResult);

        //Given
        Either<ToscaElement, StorageOperationStatus> expectedRightResult = Either
            .right(StorageOperationStatus.GENERAL_ERROR);
        ToscaElementOperation rightResultOperation = getPseudoToscaOperation(expectedRightResult);

        //When
        Either<ToscaElement, StorageOperationStatus> rightResult = ToscaElementLifecycleOperation
            .getToscaElementFromOperation(rightResultOperation, uniqueId, toscaElementId);

        //Then
        assertTrue(rightResult.isRight());
        assertEquals(expectedRightResult, rightResult);
    }

    ToscaElementOperation getPseudoToscaOperation(
        Either<ToscaElement, StorageOperationStatus> expectedResult) {
        return new ToscaElementOperation() {
            @Override
            public Either<ToscaElement, StorageOperationStatus> getToscaElement(String uniqueId,
                ComponentParametersView componentParametersView) {
                return expectedResult;
            }

            @Override
            public <T extends ToscaElement> Either<T, StorageOperationStatus> getToscaElement(
                GraphVertex toscaElementVertex, ComponentParametersView componentParametersView) {
                return null;
            }

            @Override
            public <T extends ToscaElement> Either<T, StorageOperationStatus> deleteToscaElement(
                GraphVertex toscaElementVertex) {
                return null;
            }

            @Override
            public <T extends ToscaElement> Either<T, StorageOperationStatus> createToscaElement(
                ToscaElement toscaElement) {
                return null;
            }

            @Override
            protected <T extends ToscaElement> JanusGraphOperationStatus setCategoriesFromGraph(
                GraphVertex vertexComponent, T toscaElement) {
                return null;
            }

            @Override
            protected <T extends ToscaElement> JanusGraphOperationStatus setCapabilitiesFromGraph(
                GraphVertex componentV, T toscaElement) {
                return null;
            }

            @Override
            protected <T extends ToscaElement> JanusGraphOperationStatus setRequirementsFromGraph(
                GraphVertex componentV, T toscaElement) {
                return null;
            }

            @Override
            protected <T extends ToscaElement> StorageOperationStatus validateCategories(T toscaElementToUpdate,
                GraphVertex elementV) {
                return null;
            }

            @Override
            protected <T extends ToscaElement> StorageOperationStatus updateDerived(T toscaElementToUpdate,
                GraphVertex updateElementV) {
                return null;
            }

            @Override
            public <T extends ToscaElement> void fillToscaElementVertexData(GraphVertex elementV,
                T toscaElementToUpdate, JsonParseFlagEnum flag) {

            }
        };
    }

    Map<JanusGraphOperationStatus, StorageOperationStatus> getStatusMap() {
        Map<JanusGraphOperationStatus, StorageOperationStatus> map = new HashMap<>();
        map.put(JanusGraphOperationStatus.OK, StorageOperationStatus.OK);
        map.put(JanusGraphOperationStatus.NOT_CONNECTED, StorageOperationStatus.CONNECTION_FAILURE);
        map.put(JanusGraphOperationStatus.NOT_FOUND, StorageOperationStatus.NOT_FOUND);
        map.put(JanusGraphOperationStatus.NOT_CREATED, StorageOperationStatus.SCHEMA_ERROR);
        map.put(JanusGraphOperationStatus.INDEX_CANNOT_BE_CHANGED, StorageOperationStatus.SCHEMA_ERROR);
        map.put(JanusGraphOperationStatus.MISSING_UNIQUE_ID, StorageOperationStatus.BAD_REQUEST);
        map.put(JanusGraphOperationStatus.ALREADY_LOCKED, StorageOperationStatus.FAILED_TO_LOCK_ELEMENT);
        map.put(JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION, StorageOperationStatus.SCHEMA_VIOLATION);
        map.put(JanusGraphOperationStatus.INVALID_ID, StorageOperationStatus.INVALID_ID);
        map.put(JanusGraphOperationStatus.MATCH_NOT_FOUND, StorageOperationStatus.MATCH_NOT_FOUND);
        map.put(JanusGraphOperationStatus.ILLEGAL_ARGUMENT, StorageOperationStatus.BAD_REQUEST);
        map.put(JanusGraphOperationStatus.ALREADY_EXIST, StorageOperationStatus.ENTITY_ALREADY_EXISTS);
        map.put(JanusGraphOperationStatus.PROPERTY_NAME_ALREADY_EXISTS,
            StorageOperationStatus.PROPERTY_NAME_ALREADY_EXISTS);
        map.put(JanusGraphOperationStatus.INVALID_PROPERTY, StorageOperationStatus.INVALID_PROPERTY);
        map.put(JanusGraphOperationStatus.INVALID_QUERY, StorageOperationStatus.GENERAL_ERROR);

        return map;
    }
}
