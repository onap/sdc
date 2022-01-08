/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.dao.janusgraph;

import fj.data.Either;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.utils.DAOConfDependentTest;

class JanusGraphDaoMockTest extends DAOConfDependentTest {

    @InjectMocks
    private JanusGraphDao testSubject;

    @Mock
    private JanusGraphClient janusGraphClient;

    @BeforeEach
    void BeforeEach() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCommit() throws Exception {
        JanusGraphOperationStatus result;

        // default test
        result = testSubject.commit();
    }

    @Test
    void testRollback() throws Exception {
        JanusGraphOperationStatus result;

        // default test
        result = testSubject.rollback();
    }

    @Test
    void testGetGraph() throws Exception {

        Either<JanusGraph, JanusGraphOperationStatus> result;

        // default test

        result = testSubject.getGraph();
    }

    @Test
    void testCreateVertex() throws Exception {

        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setLabel(VertexTypeEnum.ADDITIONAL_INFORMATION);
        Either<GraphVertex, JanusGraphOperationStatus> result;

        JanusGraph tg = Mockito.mock(JanusGraph.class);
        Either<JanusGraph, JanusGraphOperationStatus> value = Either.left(tg);
        // default test
        JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
        Mockito.when(tg.addVertex()).thenReturn(value2);
        Mockito.when(janusGraphClient.getGraph()).thenReturn(value);
        result = testSubject.createVertex(graphVertex);
    }

    @Test
    void testCreateVertexErrorGetGraph() throws Exception {

        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setLabel(VertexTypeEnum.ADDITIONAL_INFORMATION);
        Either<GraphVertex, JanusGraphOperationStatus> result;

        Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
        // default test
        Mockito.when(janusGraphClient.getGraph()).thenReturn(value);
        result = testSubject.createVertex(graphVertex);
    }

    @Test
    void testCreateVertexException() throws Exception {

        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setLabel(VertexTypeEnum.ADDITIONAL_INFORMATION);
        Either<GraphVertex, JanusGraphOperationStatus> result;

        JanusGraph tg = Mockito.mock(JanusGraph.class);
        Either<JanusGraph, JanusGraphOperationStatus> value = Either.left(tg);
        // default test
        Mockito.when(tg.addVertex()).thenThrow(RuntimeException.class);
        Mockito.when(janusGraphClient.getGraph()).thenReturn(value);
        result = testSubject.createVertex(graphVertex);
    }

    @Test
    void testGetVertexByPropertyAndLabel() throws Exception {
        Either<GraphVertex, JanusGraphOperationStatus> result;

        // default test
        Mockito.when(janusGraphClient.getGraph()).thenReturn(Either.right(JanusGraphOperationStatus.GENERAL_ERROR));
        result = testSubject.getVertexByPropertyAndLabel(GraphPropertyEnum.COMPONENT_TYPE, "mock",
            VertexTypeEnum.ADDITIONAL_INFORMATION);
    }

    @Test
    void testGetVertexById_1Exception() throws Exception {

        String id = "mock";
        Either<GraphVertex, JanusGraphOperationStatus> result;

        JanusGraph tg = Mockito.mock(JanusGraph.class);
        Either<JanusGraph, JanusGraphOperationStatus> value = Either.left(tg);
        // default test
        JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
        Mockito.when(tg.addVertex()).thenReturn(value2);
        Mockito.when(janusGraphClient.getGraph()).thenReturn(value);

        // test 1
        result = testSubject.getVertexById(id, JsonParseFlagEnum.NoParse);
        // Assert.assertEquals(null, result);
    }

    @Test
    void testGetVertexById_1GraphClosed() throws Exception {

        String id = "mock";
        Either<GraphVertex, JanusGraphOperationStatus> result;

        Object b;
        Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
        // default test
        JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
        Mockito.when(janusGraphClient.getGraph()).thenReturn(value);

        // test 1
        result = testSubject.getVertexById(id, JsonParseFlagEnum.NoParse);
        // Assert.assertEquals(null, result);
    }

    @Test
    void testSetVertexProperties_1() throws Exception {
        Vertex vertex = Mockito.mock(Vertex.class);
        Map<String, Object> properties = new HashMap<>();
        properties.put("mock", "mock");

        // default test
        testSubject.setVertexProperties(vertex, properties);
    }

    @Test
    void testParseVertexProperties() throws Exception {

        GraphVertex graphVertex = new GraphVertex();
        JanusGraphVertex vertex = Mockito.mock(JanusGraphVertex.class);
        graphVertex.setVertex(vertex);
        JsonParseFlagEnum parseFlag = null;

        // default test

        testSubject.parseVertexProperties(graphVertex, JsonParseFlagEnum.NoParse);
    }

    @Test
    void testCreateEdge() throws Exception {

        GraphVertex from = Mockito.mock(GraphVertex.class);
        GraphVertex to = Mockito.mock(GraphVertex.class);

        JanusGraphVertex value = Mockito.mock(JanusGraphVertex.class);
        Mockito.when(from.getVertex()).thenReturn(value);
        Mockito.when(to.getVertex()).thenReturn(value);
        Map<EdgePropertyEnum, Object> properties = new HashMap<>();
        JanusGraphOperationStatus result;

        // default test

        result = testSubject.createEdge(from, to, EdgeLabelEnum.ADDITIONAL_INFORMATION, properties);
        from = new GraphVertex();
        to = new GraphVertex();
        result = testSubject.createEdge(from, to, EdgeLabelEnum.ADDITIONAL_INFORMATION, properties);
    }

    @Test
    void testSetEdgeProperties() throws Exception {

        Element element = Mockito.mock(Element.class);
        Map<EdgePropertyEnum, Object> properties = new HashMap<>();

        // test 1

        properties.put(EdgePropertyEnum.STATE, "mock");
        testSubject.setEdgeProperties(element, properties);
    }

    @Test
    void testGetByCriteria() throws Exception {
        Map<GraphPropertyEnum, Object> props = new HashMap<>();
        Either<List<GraphVertex>, JanusGraphOperationStatus> result;

        JanusGraph tg = Mockito.mock(JanusGraph.class);
        Either<JanusGraph, JanusGraphOperationStatus> value = Either.left(tg);
        JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
        Mockito.when(tg.addVertex()).thenReturn(value2);
        Mockito.when(janusGraphClient.getGraph()).thenReturn(value);

        // default test
        result = testSubject.getByCriteria(VertexTypeEnum.ADDITIONAL_INFORMATION, props);
    }

    @Test
    void testGetByCriteria_1() throws Exception {

        Map<GraphPropertyEnum, Object> props = new HashMap<>();
        Either<List<GraphVertex>, JanusGraphOperationStatus> result;

        Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
        JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
        Mockito.when(janusGraphClient.getGraph()).thenReturn(value);

        // default test
        result = testSubject.getByCriteria(VertexTypeEnum.ADDITIONAL_INFORMATION, props, JsonParseFlagEnum.NoParse);
    }

    @Test
    void testGetCatalogVerticies() throws Exception {
        Either<Iterator<Vertex>, JanusGraphOperationStatus> result;

        Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
        // default test
        JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
        Mockito.when(janusGraphClient.getGraph()).thenReturn(value);

        // default test
        result = testSubject.getCatalogOrArchiveVerticies(true);
    }

    @Test
    void testGetChildVertex() throws Exception {

        GraphVertex parentVertex = new GraphVertex();
        EdgeLabelEnum edgeLabel = null;
        JsonParseFlagEnum parseFlag = null;
        Either<GraphVertex, JanusGraphOperationStatus> result;

        Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
        JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
        Mockito.when(janusGraphClient.getGraph()).thenReturn(value);

        // default test
        result = testSubject.getChildVertex(parentVertex, EdgeLabelEnum.ADDITIONAL_INFORMATION, JsonParseFlagEnum.NoParse);
    }

    @Test
    void testGetChildVertex_1() throws Exception {

        Vertex parentVertex = null;
        EdgeLabelEnum edgeLabel = null;
        JsonParseFlagEnum parseFlag = null;
        Either<Vertex, JanusGraphOperationStatus> result;

        Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
        JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
        Mockito.when(janusGraphClient.getGraph()).thenReturn(value);

        // default test
        result = testSubject.getChildVertex(parentVertex, edgeLabel, parseFlag);
    }

    @Test
    void testGetParentVertex_1() throws Exception {

        Vertex parentVertex = null;
        EdgeLabelEnum edgeLabel = null;
        JsonParseFlagEnum parseFlag = null;
        Either<Vertex, JanusGraphOperationStatus> result;

        // default test

        result = testSubject.getParentVertex(parentVertex, edgeLabel, parseFlag);
    }

    @Test
    void testGetParentVertecies_1() throws Exception {

        Vertex parentVertex = null;
        EdgeLabelEnum edgeLabel = null;
        JsonParseFlagEnum parseFlag = null;
        Either<List<Vertex>, JanusGraphOperationStatus> result;

        // default test

        result = testSubject.getParentVertices(parentVertex, edgeLabel, parseFlag);
    }

    @Test
    void testGetChildrenVertecies_1() throws Exception {

        Vertex parentVertex = null;
        EdgeLabelEnum edgeLabel = null;
        JsonParseFlagEnum parseFlag = null;
        Either<List<Vertex>, JanusGraphOperationStatus> result;

        // default test

        result = testSubject.getChildrenVertices(parentVertex, edgeLabel, parseFlag);
    }

    @Test
    void testDeleteBelongingEdgeByCriteria() throws Exception {

        GraphVertex vertex = null;
        EdgeLabelEnum label = null;
        Map<GraphPropertyEnum, Object> properties = null;
        Either<Edge, JanusGraphOperationStatus> result;

        // default test

        result = testSubject.deleteBelongingEdgeByCriteria(vertex, label, properties);
    }

    @Test
    void testDeleteEdge() throws Exception {

        GraphVertex fromVertex = new GraphVertex();
        GraphVertex toVertex = new GraphVertex();
        Either<Edge, JanusGraphOperationStatus> result;

        Either<JanusGraph, JanusGraphOperationStatus> value = Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
        JanusGraphVertex value2 = Mockito.mock(JanusGraphVertex.class);
        Mockito.when(janusGraphClient.getGraph()).thenReturn(value);

        // default test
        result = testSubject.deleteEdge(fromVertex, toVertex, EdgeLabelEnum.ADDITIONAL_INFORMATION);
    }

    @Test
    void testDeleteEdgeByDirection() throws Exception {
        GraphVertex fromVertex = new GraphVertex();
        JanusGraphOperationStatus result;

        // default test
        result = testSubject.deleteEdgeByDirection(fromVertex, Direction.BOTH, EdgeLabelEnum.ADDITIONAL_INFORMATION);
    }

    @Test
    void testDeleteEdgeByDirectionMock() throws Exception {
        GraphVertex fromVertex = Mockito.mock(GraphVertex.class);
        JanusGraphOperationStatus result;

        JanusGraphVertex value = Mockito.mock(JanusGraphVertex.class);
        ;
        Mockito.when(fromVertex.getVertex()).thenReturn(value);
        Iterator<Edge> value2 = Mockito.mock(Iterator.class);
        ;
        Mockito.when(value.edges(Mockito.any(), Mockito.any())).thenReturn(value2);
        Mockito.when(value2.hasNext()).thenReturn(true, false);
        Edge value3 = Mockito.mock(Edge.class);
        ;
        Mockito.when(value2.next()).thenReturn(value3);
        // default test
        result = testSubject.deleteEdgeByDirection(fromVertex, Direction.BOTH, EdgeLabelEnum.ADDITIONAL_INFORMATION);
    }

    @Test
    void testUpdateVertex() throws Exception {

        GraphVertex graphVertex = new GraphVertex();
        Either<GraphVertex, JanusGraphOperationStatus> result;

        // default test

        result = testSubject.updateVertex(graphVertex);
    }

    @Test
    void testGetVerticesByUniqueIdAndParseFlag() throws Exception {

        Map<String, ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum>> verticesToGet = new HashMap<>();
        Either<Map<String, GraphVertex>, JanusGraphOperationStatus> result;

        // default test
        result = testSubject.getVerticesByUniqueIdAndParseFlag(verticesToGet);
        ImmutablePair<GraphPropertyEnum, JsonParseFlagEnum> value3 = ImmutablePair.of(GraphPropertyEnum.COMPONENT_TYPE, JsonParseFlagEnum.NoParse);
        verticesToGet.put("mock", value3);
        try {
            result = testSubject.getVerticesByUniqueIdAndParseFlag(verticesToGet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void testCreateEdge_2() throws Exception {

        Vertex from = null;
        Vertex to = null;
        EdgeLabelEnum label = null;
        Edge edgeToCopy = null;
        JanusGraphOperationStatus result;

        // default test

        result = testSubject.createEdge(from, to, label, edgeToCopy);
    }

    @Test
    void testReplaceEdgeLabel() throws Exception {

        Vertex fromVertex = null;
        Vertex toVertex = null;
        Edge prevEdge = null;
        EdgeLabelEnum prevLabel = null;
        EdgeLabelEnum newLabel = null;
        JanusGraphOperationStatus result;

        // default test

        result = testSubject.replaceEdgeLabel(fromVertex, toVertex, prevEdge, prevLabel, newLabel);
    }

    @Test
    void testUpdateVertexMetadataPropertiesWithJson() throws Exception {

        Vertex vertex = Mockito.mock(Vertex.class);
        ;
        Map<GraphPropertyEnum, Object> properties = new HashMap<>();
        properties.put(GraphPropertyEnum.COMPONENT_TYPE, "mock");
        JanusGraphOperationStatus result;

        // default test

        result = testSubject.updateVertexMetadataPropertiesWithJson(vertex, properties);
    }

    //TODO Last
    @Test
    void testDisassociateAndDeleteLast() throws Exception {

        GraphVertex vertex = Mockito.mock(GraphVertex.class);
        JanusGraphOperationStatus result;

        JanusGraphVertex value = Mockito.mock(JanusGraphVertex.class);
        Iterator<Edge> mockiter = Mockito.mock(Iterator.class);
        Edge nextmock = Mockito.mock(Edge.class);
        Mockito.when(vertex.getVertex()).thenReturn(value);
        Mockito.when(value.edges(Mockito.any(), Mockito.any())).thenReturn(mockiter);
        Mockito.when(mockiter.hasNext()).thenReturn(true, false);
        Mockito.when(mockiter.next()).thenReturn(nextmock);
        Vertex secondVertex = Mockito.mock(Vertex.class);
        Mockito.when(nextmock.outVertex()).thenReturn(secondVertex);
        Mockito.when(nextmock.inVertex()).thenReturn(secondVertex);
        Iterator<Edge> restOfEdges = Mockito.mock(Iterator.class);
        Mockito.when(secondVertex.edges(Mockito.any(), Mockito.any())).thenReturn(restOfEdges);
        Mockito.when(restOfEdges.hasNext()).thenReturn(false);

        // default test
        result = testSubject.disassociateAndDeleteLast(vertex, Direction.OUT, EdgeLabelEnum.ADDITIONAL_INFORMATION);
    }

    @Test
    void testDisassociateAndDeleteLastOut() throws Exception {

        GraphVertex vertex = Mockito.mock(GraphVertex.class);
        JanusGraphOperationStatus result;

        JanusGraphVertex value = Mockito.mock(JanusGraphVertex.class);
        Iterator<Edge> mockiter = Mockito.mock(Iterator.class);
        Edge nextmock = Mockito.mock(Edge.class);
        Mockito.when(vertex.getVertex()).thenReturn(value);
        Mockito.when(value.edges(Mockito.any(), Mockito.any())).thenReturn(mockiter);
        Mockito.when(mockiter.hasNext()).thenReturn(true, false);
        Mockito.when(mockiter.next()).thenReturn(nextmock);
        Vertex secondVertex = Mockito.mock(Vertex.class);
        Mockito.when(nextmock.outVertex()).thenReturn(secondVertex);
        Mockito.when(nextmock.inVertex()).thenReturn(secondVertex);
        Iterator<Edge> restOfEdges = Mockito.mock(Iterator.class);
        Mockito.when(secondVertex.edges(Mockito.any(), Mockito.any())).thenReturn(restOfEdges);
        Mockito.when(restOfEdges.hasNext()).thenReturn(false);

        // default test
        result = testSubject.disassociateAndDeleteLast(vertex, Direction.IN, EdgeLabelEnum.ADDITIONAL_INFORMATION);
    }

    @Test
    void testDisassociateAndDeleteLastException() throws Exception {

        GraphVertex vertex = Mockito.mock(GraphVertex.class);
        JanusGraphOperationStatus result;

        Mockito.when(vertex.getVertex()).thenThrow(RuntimeException.class);

        // default test
        result = testSubject.disassociateAndDeleteLast(vertex, Direction.OUT, EdgeLabelEnum.ADDITIONAL_INFORMATION);
    }

    @Test
    void testMoveEdge() throws Exception {

        GraphVertex vertexA = new GraphVertex();
        GraphVertex vertexB = new GraphVertex();
        JanusGraphOperationStatus result;

        // default test

        result = testSubject.moveEdge(vertexA, vertexB, EdgeLabelEnum.ADDITIONAL_INFORMATION, Direction.BOTH);
    }
}
