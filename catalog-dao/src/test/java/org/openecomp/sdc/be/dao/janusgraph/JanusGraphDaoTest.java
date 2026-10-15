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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fj.data.Either;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.dao.DAOJanusGraphStrategy;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.utils.DAOConfDependentTest;

class JanusGraphDaoTest extends DAOConfDependentTest {

    private final JanusGraphDao dao = new JanusGraphDao(new JanusGraphClient(new DAOJanusGraphStrategy()));

    @BeforeEach
    void init() {
        dao.getJanusGraphClient().createGraph();
    }

    @AfterEach
    void end() {
        dao.getJanusGraphClient().cleanupGraph();
    }

    @Test
    void testCreateVertex() throws Exception {
        Either<GraphVertex, JanusGraphOperationStatus> result;

        // default test
        GraphVertex graphVertex = new GraphVertex(VertexTypeEnum.REQUIREMENTS);
        result = dao.createVertex(graphVertex);

        graphVertex = new GraphVertex();
        result = dao.createVertex(graphVertex);
    }

    @Test
    void testGetVertexByLabel() throws Exception {
        Either<GraphVertex, JanusGraphOperationStatus> result;

        dao.createVertex(new GraphVertex(VertexTypeEnum.ADDITIONAL_INFORMATION));
        // default test
        result = dao.getVertexByLabel(VertexTypeEnum.ADDITIONAL_INFORMATION);
    }

    @Test
    void testCommit() throws Exception {
        JanusGraphOperationStatus result;

        // default test

        result = dao.commit();
    }

    @Test
    void testRollback() throws Exception {

        JanusGraphOperationStatus result;

        // default test

        result = dao.rollback();
    }

    @Test
    void testGetGraph() throws Exception {

        Either<JanusGraph, JanusGraphOperationStatus> result;

        // default test

        result = dao.getJanusGraphClient().getGraph();
    }

    @Test
    void testGetVertexByPropertyAndLabel() throws Exception {

        GraphPropertyEnum name = null;
        Object value = null;
        VertexTypeEnum label = null;
        Either<GraphVertex, JanusGraphOperationStatus> result;

        // default test

        result = dao.getVertexByPropertyAndLabel(name, value, label);

        result = dao.getVertexByPropertyAndLabel(GraphPropertyEnum.COMPONENT_TYPE, new Object(), VertexTypeEnum.ADDITIONAL_INFORMATION);
    }

    @Test
    void testGetVertexByPropertyAndLabel_1() throws Exception {

        GraphPropertyEnum name = null;
        Object value = null;
        VertexTypeEnum label = null;
        JsonParseFlagEnum parseFlag = null;
        Either<GraphVertex, JanusGraphOperationStatus> result;

        // default test

        result = dao.getVertexByPropertyAndLabel(name, value, label, parseFlag);
    }

    @Test
    void testGetVertexById() throws Exception {

        String id = "";
        Either<GraphVertex, JanusGraphOperationStatus> result;

        // default test

        result = dao.getVertexById(id);
    }

    @Test
    void testGetVertexById_1() throws Exception {

        String id = "";
        JsonParseFlagEnum parseFlag = null;
        Either<GraphVertex, JanusGraphOperationStatus> result;

        // test 1

        id = null;
        result = dao.getVertexById(id, parseFlag);

        // test 2

        id = "";
        result = dao.getVertexById(id, parseFlag);
    }

    @Test
    void testGetVertexProperties() throws Exception {

        Element element = null;
        Map<GraphPropertyEnum, Object> result;

        // test 1

        element = null;
        result = dao.getVertexProperties(element);
    }

    @Test
    void testGetEdgeProperties() throws Exception {

        Element element = null;
        Map<EdgePropertyEnum, Object> result;

        // test 1

        element = null;
        result = dao.getEdgeProperties(element);
    }

    @Test
    void testGetByCriteria() throws Exception {

        VertexTypeEnum type = null;
        Map<GraphPropertyEnum, Object> props = null;
        Either<List<GraphVertex>, JanusGraphOperationStatus> result;

        // default test

        result = dao.getByCriteria(type, props);
    }

    @Test
    void testGetByCriteria_1() throws Exception {

        VertexTypeEnum type = null;
        Map<GraphPropertyEnum, Object> props = null;
        JsonParseFlagEnum parseFlag = null;
        Either<List<GraphVertex>, JanusGraphOperationStatus> result;

        // default test

        result = dao.getByCriteria(type, props, parseFlag);
    }

    @Test
    void testGetByCriteria_2() throws Exception {

        VertexTypeEnum type = null;
        Map<GraphPropertyEnum, Object> props = null;
        Map<GraphPropertyEnum, Object> hasNotProps = null;
        JsonParseFlagEnum parseFlag = null;
        Either<List<GraphVertex>, JanusGraphOperationStatus> result;

        // default test

        result = dao.getByCriteria(type, props, hasNotProps, parseFlag);
    }

    @Test
    void testGetCatalogVerticies() throws Exception {

        Either<Iterator<Vertex>, JanusGraphOperationStatus> result;

        // default test

        result = dao.getCatalogOrArchiveVertices(true);
    }

    @Test
    void testGetParentVertecies_1() throws Exception {

        Vertex parentVertex = null;
        EdgeLabelEnum edgeLabel = null;
        JsonParseFlagEnum parseFlag = null;
        Either<List<Vertex>, JanusGraphOperationStatus> result;

        // default test

        result = dao.getParentVertices(parentVertex, edgeLabel);
    }

    @Test
    void testGetChildrenVertecies_1() throws Exception {

        Vertex parentVertex = null;
        EdgeLabelEnum edgeLabel = null;
        JsonParseFlagEnum parseFlag = null;
        Either<List<Vertex>, JanusGraphOperationStatus> result;

        // default test

        result = dao.getChildrenVertices(parentVertex, edgeLabel);
    }

    @Test
    void testUpdateVertexMetadataPropertiesWithJson() throws Exception {

        Vertex vertex = null;
        Map<GraphPropertyEnum, Object> properties = null;
        JanusGraphOperationStatus result;

        // default test

        result = dao.updateVertexMetadataPropertiesWithJson(vertex, properties);
    }

    @Test
    void testGetProperty() throws Exception {
        Edge edge = Mockito.mock(Edge.class);
        ;
        Object result;

        Property<Object> value = Mockito.mock(Property.class);
        Mockito.when(edge.property(Mockito.any())).thenReturn(value);

        // default test
        result = dao.getProperty(edge, EdgePropertyEnum.STATE);
    }

    @Test
    void testGetProperty_1() throws Exception {
        Edge edge = Mockito.mock(Edge.class);
        ;
        Object result;

        // default test
        result = dao.getProperty(edge, EdgePropertyEnum.STATE);
    }

    @Test
    void testGetPropertyexception() throws Exception {
        Edge edge = Mockito.mock(Edge.class);
        ;
        Object result;

        Property<Object> value = Mockito.mock(Property.class);
        Mockito.when(edge.property(Mockito.any())).thenThrow(RuntimeException.class);

        // default test
        result = dao.getProperty(edge, EdgePropertyEnum.STATE);
    }

    @Test
    void testGetBelongingEdgeByCriteria_1() throws Exception {

        String parentId = "";
        EdgeLabelEnum label = null;
        Map<GraphPropertyEnum, Object> properties = null;
        Either<Edge, JanusGraphOperationStatus> result;

        // default test

        result = dao.getBelongingEdgeByCriteria(parentId, label, properties);
    }

    @Test
    void getChildrenVerticesWithPrefetchedPropertiesMatchesGetChildrenVertices() {
        GraphVertex parent = createVertex(VertexTypeEnum.USER, "parent");
        GraphVertex first = createVertex(VertexTypeEnum.NODE_TYPE, "first");
        GraphVertex second = createVertex(VertexTypeEnum.TOPOLOGY_TEMPLATE, "second");
        dao.createEdge(parent, first, EdgeLabelEnum.STATE, new HashMap<>());
        dao.createEdge(parent, second, EdgeLabelEnum.STATE, new HashMap<>());
        dao.commit();

        List<GraphVertex> expected = dao.getChildrenVertices(parent, EdgeLabelEnum.STATE, JsonParseFlagEnum.ParseMetadata).left().value();
        dao.rollback();
        List<GraphVertex> actual = dao.getChildrenVerticesWithPrefetchedProperties(parent, EdgeLabelEnum.STATE, JsonParseFlagEnum.ParseMetadata)
            .left().value();

        assertEquals(expected.stream().map(GraphVertex::getUniqueId).collect(Collectors.toList()),
            actual.stream().map(GraphVertex::getUniqueId).collect(Collectors.toList()));
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getLabel(), actual.get(i).getLabel());
            assertEquals(expected.get(i).getMetadataProperties(), actual.get(i).getMetadataProperties());
            assertEquals(expected.get(i).getMetadataJson(), actual.get(i).getMetadataJson());
        }
        assertEquals(JanusGraphOperationStatus.NOT_FOUND,
            dao.getChildrenVerticesWithPrefetchedProperties(parent, EdgeLabelEnum.LAST_STATE, JsonParseFlagEnum.NoParse).right().value());
    }

    @Test
    void prefetchAdjacentVerticesReturnsDistinctVerticesAtTheOtherEnd() {
        GraphVertex creator = createVertex(VertexTypeEnum.USER, "creator");
        GraphVertex modifier = createVertex(VertexTypeEnum.USER, "modifier");
        GraphVertex category = createVertex(VertexTypeEnum.RESOURCE_SUBCATEGORY, "category");
        GraphVertex first = createVertex(VertexTypeEnum.NODE_TYPE, "first");
        GraphVertex second = createVertex(VertexTypeEnum.NODE_TYPE, "second");
        for (GraphVertex component : List.of(first, second)) {
            dao.createEdge(creator, component, EdgeLabelEnum.CREATOR, new HashMap<>());
            dao.createEdge(modifier, component, EdgeLabelEnum.LAST_MODIFIER, new HashMap<>());
            dao.createEdge(component, category, EdgeLabelEnum.CATEGORY, new HashMap<>());
        }
        dao.commit();
        List<Vertex> components = List.of(first.getVertex(), second.getVertex());

        assertEquals(Set.of("creator", "modifier"), uniqueIds(
            dao.prefetchAdjacentVertices(components, Direction.IN, EdgeLabelEnum.CREATOR, EdgeLabelEnum.LAST_MODIFIER)));
        assertEquals(List.of("category"), dao.prefetchAdjacentVertices(components, Direction.OUT, EdgeLabelEnum.CATEGORY).stream()
            .map(vertex -> vertex.value(GraphPropertyEnum.UNIQUE_ID.getProperty())).collect(Collectors.toList()));
        assertTrue(dao.prefetchAdjacentVertices(components, Direction.IN, EdgeLabelEnum.SUB_CATEGORY).isEmpty());
        assertTrue(dao.prefetchAdjacentVertices(List.of(), Direction.IN, EdgeLabelEnum.CREATOR).isEmpty());
        dao.rollback();
    }

    private Set<Object> uniqueIds(List<Vertex> vertices) {
        return vertices.stream().map(vertex -> vertex.value(GraphPropertyEnum.UNIQUE_ID.getProperty())).collect(Collectors.toSet());
    }

    private GraphVertex createVertex(VertexTypeEnum label, String uniqueId) {
        GraphVertex vertex = new GraphVertex(label);
        vertex.setUniqueId(uniqueId);
        Map<GraphPropertyEnum, Object> metadata = new EnumMap<>(GraphPropertyEnum.class);
        metadata.put(GraphPropertyEnum.UNIQUE_ID, uniqueId);
        metadata.put(GraphPropertyEnum.LABEL, label.getName());
        metadata.put(GraphPropertyEnum.NAME, uniqueId + "-name");
        vertex.setMetadataProperties(metadata);
        vertex.updateMetadataJsonWithCurrentMetadataProperties();
        return dao.createVertex(vertex).left().value();
    }
}
