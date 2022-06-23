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
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterPropertyDataDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
        RequirementNodeFilterPropertyDataDefinition requirementNodeFilterPropertyDataDefinition = new RequirementNodeFilterPropertyDataDefinition();
        requirementNodeFilterPropertyDataDefinition.setName("Name1");
        requirementNodeFilterPropertyDataDefinition
                .setConstraints(Arrays.asList("mem_size:\n" + "  equal: { get_property : [" + prop1 + ", size]}\n"));
        RequirementNodeFilterPropertyDataDefinition requirementNodeFilterPropertyDataDefinition2 = new RequirementNodeFilterPropertyDataDefinition();
        requirementNodeFilterPropertyDataDefinition2.setName("Name2");
        requirementNodeFilterPropertyDataDefinition2
                .setConstraints(Arrays.asList("mem_size:\n {equal:  { get_property : [SELF, " + prop2 + "]}}\n"));

        ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> listDataDefinition =
                new ListDataDefinition<>(Arrays.asList(
                        requirementNodeFilterPropertyDataDefinition,
                        requirementNodeFilterPropertyDataDefinition2));
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
        assertNotEquals("some other node filter name", expectedNodeFilter.getName());
        assertEquals(listDataDefinition, expectedNodeFilter.getProperties());
        assertNotEquals(null, expectedNodeFilter.getProperties());
    }
}