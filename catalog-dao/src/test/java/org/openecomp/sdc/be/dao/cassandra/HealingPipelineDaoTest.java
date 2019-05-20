/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.dao.cassandra;

import com.google.common.collect.ImmutableListMultimap;
import org.janusgraph.core.JanusGraphVertex;
import org.janusgraph.graphdb.relations.StandardVertexProperty;
import org.janusgraph.graphdb.types.system.EmptyVertex;
import org.janusgraph.graphdb.types.system.ImplicitKey;
import java.util.HashMap;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.heal.*;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class HealingPipelineDaoTest {


    @Test
    public void shouldUpgrade() {
        HealingPipelineDao healingPipelineDao = new HealingPipelineDao();
        healingPipelineDao.setHealVersion(3);
        healingPipelineDao.initHealVersion();
        healingPipelineDao.initGraphHealers();
        final HealVersion<Integer> version3 = HealVersionBuilder.build(3);
        assertFalse(healingPipelineDao.shouldHeal(HealVersionBuilder.build(2), version3));
        assertFalse(healingPipelineDao.shouldHeal(version3, version3));
        assertTrue(healingPipelineDao.shouldHeal(HealVersionBuilder.build(2), HealVersionBuilder.build(1)));
    }


    @Test
    public void testPipelineFilter3Attributes() {
        // init data
        HealingPipelineDao healingPipelineDao = new HealingPipelineDao();
        healingPipelineDao.setHealVersion(7);
        healingPipelineDao.initHealVersion();
        healingPipelineDao.initGraphHealers();
        healingPipelineDao.setHealingPipeline(createPipelineMap());

        assertEquals(2,
                healingPipelineDao.getHealersForVertex(EdgeLabelEnum.ATTRIBUTES.name(), HealVersionBuilder.build(5)).size());

        GraphVertex graphVertex = new GraphVertex();
        final int version = 5;
        graphVertex.addMetadataProperty(GraphPropertyEnum.HEALING_VERSION, Integer.valueOf(version));

        // perform test

        Optional optional = healingPipelineDao.performGraphReadHealing(graphVertex, EdgeLabelEnum.ATTRIBUTES);
        assertTrue(optional.isPresent());
        final GraphVertex changedVertex = (GraphVertex) optional.get();

        //validate result
        final Object healVersion = changedVertex.getMetadataProperties().get(GraphPropertyEnum.HEALING_VERSION);
        assertNotNull(healVersion);
        assertTrue(healVersion instanceof Integer);
        assertEquals(healingPipelineDao.getCurrentHealVersion().getVersion().intValue(), ((Integer) healVersion).intValue());
    }

    @Test
    public void testPipelineFilter3AttributesJanusGraphVertex() {
        // init data
        HealingPipelineDao healingPipelineDao = new HealingPipelineDao();
        healingPipelineDao.setHealVersion(7);
        healingPipelineDao.initHealVersion();
        healingPipelineDao.initGraphHealers();
        healingPipelineDao.setHealingPipeline(createPipelineMap());

        assertEquals(2,
                healingPipelineDao.getHealersForVertex(GraphEdgeLabels.CAPABILITY.getProperty(), HealVersionBuilder.build(5)).size());
        JanusGraphVertex janusGraphVertex = Mockito.mock(JanusGraphVertex.class);
        final int version = 5;
        StandardVertexProperty vertexProperty = new StandardVertexProperty(1, ImplicitKey.ID, new EmptyVertex(), version, (byte) 1);
        Mockito.when(janusGraphVertex.property(GraphPropertyEnum.HEALING_VERSION.getProperty())).thenReturn(vertexProperty);

        // perform test

        Optional optional = healingPipelineDao.performGraphReadHealing(janusGraphVertex, GraphEdgeLabels.CAPABILITY);
        assertTrue(optional.isPresent());
        final JanusGraphVertex changedVertex = (JanusGraphVertex) optional.get();

        //validate result
        assertNotNull(changedVertex);

    }

    @Test
    public void testPipelineFilterGenericJanusGraphDao() {
        // init data
        HealingPipelineDao healingPipelineDao = new HealingPipelineDao();
        healingPipelineDao.setHealVersion(7);
        healingPipelineDao.initHealVersion();
        healingPipelineDao.initGraphHealers();
        healingPipelineDao.setHealingPipeline(createPipelineMap());
        assertEquals(1,

                healingPipelineDao.getHealersForVertex(GraphEdgeLabels.ATTRIBUTE.getProperty(), HealVersionBuilder.build(6)).size());

        GraphNode mockGraphNode = new MockGraphNode(NodeTypeEnum.Attribute);
        final int version = 5;
        mockGraphNode.setHealingVersion(Integer.valueOf(version));

        // perform test

        Optional optional = healingPipelineDao.performGraphReadHealing(mockGraphNode,createGraphEdge(GraphEdgeLabels.ATTRIBUTE));
        assertTrue(optional.isPresent());
        final GraphNode changedVertex = (GraphNode) optional.get();

        //validate result
        final Integer healVersion = changedVertex.getHealingVersion();
        assertNotNull(healVersion);
        assertEquals(healingPipelineDao.getCurrentHealVersion().getVersion(), healVersion);
    }

    @Test
    public void testPipelineFilterJanusGraph1Attributes() {
        // init data
        HealingPipelineDao healingPipelineDao = new HealingPipelineDao();
        healingPipelineDao.setHealVersion(7);
        healingPipelineDao.initHealVersion();
        healingPipelineDao.initGraphHealers();
        healingPipelineDao.setHealingPipeline(createPipelineMap());

        assertEquals(2,
                healingPipelineDao.getHealersForVertex(GraphEdgeLabels.ATTRIBUTE.getProperty(), HealVersionBuilder.build(5)).size());

    }

    @Test
    public void healTest() {
        HealingPipelineDao healingPipelineDao = new HealingPipelineDao();
        healingPipelineDao.setHealVersion(3);
        healingPipelineDao.initHealVersion();
        healingPipelineDao.initGraphHealers();
        final HealVersion<Integer> version3 = HealVersionBuilder.build(3);
        assertFalse(healingPipelineDao.shouldHeal(HealVersionBuilder.build(2), version3));
        assertFalse(healingPipelineDao.shouldHeal(version3, version3));
        assertTrue(healingPipelineDao.shouldHeal(HealVersionBuilder.build(2), HealVersionBuilder.build(1)));
    }


    @Test
    public void setCurrentVersion() {
        //init data
        GraphVertex graphVertex = new GraphVertex();
        HealingPipelineDao healingPipelineDao = new HealingPipelineDao();
        final int healVersion = 7;
        healingPipelineDao.setHealVersion(healVersion);
        healingPipelineDao.initHealVersion();
        healingPipelineDao.initGraphHealers();

        //execute code
        healingPipelineDao.setHealingVersion(graphVertex);

        //validate result
        final Object currentVersion = graphVertex.getMetadataProperties().get(GraphPropertyEnum.HEALING_VERSION);
        assertNotNull(currentVersion);
        assertTrue(currentVersion instanceof Integer);
        assertEquals(healingPipelineDao.getCurrentHealVersion().getVersion().intValue(), ((Integer) currentVersion).intValue());
    }

    @Test(expected = IllegalStateException.class)
    public void testMultilistValidation() {
        // init data
        HealingPipelineDao healingPipelineDao = new HealingPipelineDao();
        healingPipelineDao.setHealVersion(7);
        healingPipelineDao.initHealVersion();
        healingPipelineDao.initGraphHealers();

        ImmutableListMultimap<String, Heal> shouldFail = ImmutableListMultimap.<String, Heal>builder().put(EdgeLabelEnum.ATTRIBUTES.name(), new GraphVertexHealTestMock(3))
                .put(EdgeLabelEnum.ATTRIBUTES.name(), new GraphVertexHealTestMock(4))
                .put(EdgeLabelEnum.ATTRIBUTES.name(), new GraphVertexHealTestMock(5))
                .put(EdgeLabelEnum.ATTRIBUTES.name(), new GraphVertexHealTestMock(6))
                .put(EdgeLabelEnum.CAPABILITIES.name(), new GraphVertexHealTestMock(3))
                .put(EdgeLabelEnum.CAPABILITIES.name(), new GraphVertexHealTestMock(3)) // this should cause exception
                .put(EdgeLabelEnum.CAPABILITIES.name(), new GraphVertexHealTestMock(69)).build();

        //performTest
        healingPipelineDao.setHealingPipeline(shouldFail);
    }

    private ImmutableListMultimap<String, Heal> createPipelineMap() {
        return ImmutableListMultimap.<String, Heal>builder().put(EdgeLabelEnum.ATTRIBUTES.name(), new GraphVertexHealTestMock(3))
                .put(EdgeLabelEnum.ATTRIBUTES.name(), new GraphVertexHealTestMock(4))
                .put(EdgeLabelEnum.ATTRIBUTES.name(), new GraphVertexHealTestMock(5))
                .put(EdgeLabelEnum.ATTRIBUTES.name(), new GraphVertexHealTestMock(6))
                .put(EdgeLabelEnum.CAPABILITIES.name(), new GraphVertexHealTestMock(3))
                .put(EdgeLabelEnum.CAPABILITIES.name(), new GraphVertexHealTestMock(6))
                .put(EdgeLabelEnum.CAPABILITIES.name(), new GraphVertexHealTestMock(69))
                .put(GraphEdgeLabels.ATTRIBUTE.getProperty(), new GraphNodeHealTestMock(4))
                .put(GraphEdgeLabels.ATTRIBUTE.getProperty(), new GraphNodeHealTestMock(5))
                .put(GraphEdgeLabels.ATTRIBUTE.getProperty(), new GraphNodeHealTestMock(6))
                .put(GraphEdgeLabels.CAPABILITY.getProperty(), new JanusGraphVertexHealTestMock(4))
                .put(GraphEdgeLabels.CAPABILITY.getProperty(), new JanusGraphVertexHealTestMock(5))
                .put(GraphEdgeLabels.CAPABILITY.getProperty(), new JanusGraphVertexHealTestMock(6)).build();
    }

    public GraphEdge createGraphEdge(GraphEdgeLabels graphEdgeLabels){
        return new GraphEdge(graphEdgeLabels, new HashMap<>());
    }


    private class GraphVertexHealTestMock extends AbstractGraphVertexHeal {

        private HealVersion healVersion;

        public GraphVertexHealTestMock(int i) {
            healVersion = HealVersionBuilder.build(i);
        }

        @Override
        public HealVersion fromVersion() {
            return healVersion;
        }

        @Override
        public void healData(GraphVertex parentVertex) {

        }

    }

    private class GraphNodeHealTestMock extends AbstractJanusGraphVertexHeal {
        private HealVersion healVersion;

        public GraphNodeHealTestMock(int i) {
            healVersion = HealVersionBuilder.build(i);
        }

        @Override
        public HealVersion fromVersion() {
            return healVersion;
        }

        @Override
        public void healData(GraphNode parentV) {

        }
    }


    private class JanusGraphVertexHealTestMock implements Heal<JanusGraphVertex> {
        private HealVersion healVersion;

        public JanusGraphVertexHealTestMock(int i) {
            healVersion = HealVersionBuilder.build(i);
        }

        @Override
        public HealVersion fromVersion() {
            return healVersion;
        }

        @Override
        public void healData(JanusGraphVertex parentV) {

        }
    }

    private class MockGraphNode extends GraphNode {
        private int healVersion;

        public MockGraphNode(NodeTypeEnum label) {
            super(label);
        }

        @Override
        public String getUniqueId() {
            return null;
        }

        @Override
        public Map<String, Object> toGraphMap() {
            return null;
        }

        @Override
        public Integer getHealingVersion() {
            return healVersion;
        }

        @Override
        public void setHealingVersion(Integer version) {
            this.healVersion = version;
        }
    }

}
