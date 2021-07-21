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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JanusGraphDaoTest extends DAOConfDependentTest {

    private static final Logger logger = LoggerFactory.getLogger(JanusGraphDaoTest.class);
    private final JanusGraphDao dao = new JanusGraphDao(new JanusGraphClient(new DAOJanusGraphStrategy()));

    @BeforeEach
    void init() {
        dao.janusGraphClient.createGraph();
    }

    @AfterEach
    void end() {
        dao.janusGraphClient.cleanupGraph();
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

        result = dao.getGraph();
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

        result = dao.getCatalogOrArchiveVerticies(true);
    }

    @Test
    void testGetParentVertecies_1() throws Exception {

        Vertex parentVertex = null;
        EdgeLabelEnum edgeLabel = null;
        JsonParseFlagEnum parseFlag = null;
        Either<List<Vertex>, JanusGraphOperationStatus> result;

        // default test

        result = dao.getParentVertices(parentVertex, edgeLabel, parseFlag);
    }

    @Test
    void testGetChildrenVertecies_1() throws Exception {

        Vertex parentVertex = null;
        EdgeLabelEnum edgeLabel = null;
        JsonParseFlagEnum parseFlag = null;
        Either<List<Vertex>, JanusGraphOperationStatus> result;

        // default test

        result = dao.getChildrenVertices(parentVertex, edgeLabel, parseFlag);
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
}
