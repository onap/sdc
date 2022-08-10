/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
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
package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import java.util.List;
import java.util.Map;
import org.janusgraph.core.JanusGraphVertex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterConstraintDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.datatypes.enums.PropertyFilterTargetType;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NodeFilterOperationTest {

    private final NodeFilterOperation nodeFilterOperation = new NodeFilterOperation();

    @Mock
    private JanusGraphDao janusGraphDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        nodeFilterOperation.setJanusGraphDao(janusGraphDao);
    }

    @Test
    void addOrUpdateNodeFilterData() {
        CINodeFilterDataDefinition nodeFilterDataDefinition = new CINodeFilterDataDefinition();
        nodeFilterDataDefinition.setName("new node filter name");
        String prop1 = "property1";
        String prop2 = "property2";
        final var propertyFilterDataDefinition = new PropertyFilterDataDefinition();
        propertyFilterDataDefinition.setName("Name1");
        final var propertyFilterConstraint1 = new PropertyFilterConstraintDataDefinition();
        propertyFilterConstraint1.setPropertyName("mem_size");
        propertyFilterConstraint1.setOperator(ConstraintType.EQUAL);
        propertyFilterConstraint1.setValue(Map.of("get_property", List.of(prop1, "size")));
        propertyFilterConstraint1.setValueType(FilterValueType.GET_PROPERTY);
        propertyFilterConstraint1.setTargetType(PropertyFilterTargetType.PROPERTY);
        propertyFilterDataDefinition.setConstraints(List.of(propertyFilterConstraint1));

        final var propertyFilterDataDefinition2 = new PropertyFilterDataDefinition();
        propertyFilterDataDefinition2.setName("Name2");
        final var propertyFilterConstraint2 = new PropertyFilterConstraintDataDefinition();
        propertyFilterConstraint2.setPropertyName("mem_size");
        propertyFilterConstraint2.setOperator(ConstraintType.EQUAL);
        propertyFilterConstraint2.setValue(Map.of("get_property", List.of("SELF", prop2)));
        propertyFilterConstraint2.setValueType(FilterValueType.GET_PROPERTY);
        propertyFilterConstraint2.setTargetType(PropertyFilterTargetType.PROPERTY);
        propertyFilterDataDefinition2.setConstraints(List.of(propertyFilterConstraint2));

        ListDataDefinition<PropertyFilterDataDefinition> listDataDefinition =
                new ListDataDefinition<>(Arrays.asList(
                    propertyFilterDataDefinition,
                    propertyFilterDataDefinition2));
        nodeFilterDataDefinition.setProperties(listDataDefinition);

        String componentId = "componentId";
        final GraphVertex serviceVertexMock = mock(GraphVertex.class);
        final JanusGraphVertex serviceJanusVertex = mock(JanusGraphVertex.class);
        when(serviceVertexMock.getVertex()).thenReturn(serviceJanusVertex);
        when(serviceVertexMock.getUniqueId()).thenReturn("componentId");
        when(janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(serviceVertexMock));
        when(janusGraphDao.getChildVertex(serviceVertexMock, EdgeLabelEnum.NODE_FILTER_TEMPLATE, JsonParseFlagEnum.ParseJson))
                .thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        when(janusGraphDao.createVertex(any())).thenReturn(Either.left(new GraphVertex()));
        when(janusGraphDao.createEdge(eq(serviceJanusVertex), nullable(JanusGraphVertex.class), eq(EdgeLabelEnum.NODE_FILTER_TEMPLATE), eq(new HashMap<>())))
                .thenReturn(JanusGraphOperationStatus.OK);
        final Either<CINodeFilterDataDefinition, StorageOperationStatus> expectedNodeFilterEither = nodeFilterOperation.updateNodeFilter(
                componentId, "componentInstanceId", nodeFilterDataDefinition);
        assertTrue(expectedNodeFilterEither.isLeft());
        final CINodeFilterDataDefinition expectedNodeFilter = expectedNodeFilterEither.left().value();
        assertEquals("new node filter name", expectedNodeFilter.getName());
        assertEquals(listDataDefinition, expectedNodeFilter.getProperties());
    }
}