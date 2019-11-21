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

package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.config.JanusGraphSpringConfig;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.config.ModelOperationsSpringConfig;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JanusGraphSpringConfig.class, ModelOperationsSpringConfig.class})
public class NodeTemplateOperationGraphTest extends ModelTestBase{
    @Resource
    private HealingJanusGraphDao janusGraphDao;
    @Resource
    private NodeTemplateOperation nodeTemplateOperation;
    
    private JanusGraph graphT;
    private GraphVertex containerVertex; 
    private String containeId;
  
    @BeforeClass
    public static void setupBeforeClass() {

        ModelTestBase.init();
    }
    @Before
    public void before(){
       
        Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphDao.getGraph();
        graphT = graph.left().value();
        
        containerVertex = new GraphVertex(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        containeId = "containerId";
        containerVertex.setUniqueId(containeId);
        Either<GraphVertex, JanusGraphOperationStatus> createVertex = janusGraphDao.createVertex(containerVertex);
        assertTrue(createVertex.isLeft());
    }
    
    @After
    public void after(){
        janusGraphDao.rollback();
        
    }
    
    
    String outputDirectory = "C:\\Output";
   
    @Test
    public void testCreateInstanceEdge(){
 
        Map<String, List<String>> mapOriginToInstId = new HashMap<>();
        createIntancesFromSameResource(mapOriginToInstId, 1, 3);
        createIntancesFromSameResource(mapOriginToInstId, 2, 4);
        createIntancesFromSameResource(mapOriginToInstId, 3, 1);
          
//        exportGraphMl(graphT);
        
        validateOnGraph(mapOriginToInstId, 3);
    }
    
    @Test
    public void testRemoveInstanceEdge(){
        //create 3 instances from same resource orig1
        Map<String, List<String>> mapOriginToInstId = new HashMap<>();
        String originId = createIntancesFromSameResource(mapOriginToInstId, 1, 3);
        validateOnGraph(mapOriginToInstId, 1);
        
        //remove instance 2
        String instanceId = removeInstanceEdge(containerVertex, originId, 1, 1); 
        mapOriginToInstId.get(originId).remove(instanceId);
        validateOnGraph(mapOriginToInstId, 1);
        
        //create new instance from orig1
        instanceId = createInstanceEdge(containerVertex, originId, 1, 4, false, null); 
        mapOriginToInstId.get(originId).add(instanceId);
        validateOnGraph(mapOriginToInstId, 1);
        
        //create 1 instance from same resource orig2
        originId = createIntancesFromSameResource(mapOriginToInstId, 2, 1);
        validateOnGraph(mapOriginToInstId, 2);
        
        //remove instance of orig 2  
        instanceId = removeInstanceEdge(containerVertex, originId, 2, 1); 
        mapOriginToInstId.get(originId).remove(instanceId);
        validateOnGraph(mapOriginToInstId, 1);
        
    }
    
    @Test
    public void testProxyInstanceEdge(){
        Map<String, List<String>> mapOriginToInstId = new HashMap<>();
        String proxyId  = createOrigin(2);
        createIntancesFromSameResource(mapOriginToInstId, 1, 1, true, proxyId);
        
        validateOnGraph(mapOriginToInstId, 1);        
    }
    private void validateOnGraph(Map<String, List<String>> mapOriginToInstId, int expectedEdgeCount) {
        validateOnGraph(mapOriginToInstId, expectedEdgeCount, false);
    }
    private void validateOnGraph(Map<String, List<String>> mapOriginToInstId, int expectedEdgeCount, boolean validateProxy) {
        Iterable vertices = graphT.query().has(GraphPropertyEnum.UNIQUE_ID.getProperty(), containeId).vertices();
        assertNotNull(vertices);
        Iterator<JanusGraphVertex> iterator = vertices.iterator();
        assertTrue(iterator.hasNext());
        Vertex containerV = iterator.next();
        validatePerEdgeType(mapOriginToInstId, expectedEdgeCount, containerV, EdgeLabelEnum.INSTANCE_OF);
        if ( validateProxy ){
            validatePerEdgeType(mapOriginToInstId, expectedEdgeCount, containerV, EdgeLabelEnum.PROXY_OF);
        }
    }
    private void validatePerEdgeType(Map<String, List<String>> mapOriginToInstId, int expectedEdgeCount, Vertex containerV, EdgeLabelEnum edgeLabel) {
        Iterator<Edge> edges = containerV.edges(Direction.OUT, edgeLabel.name());
        assertNotNull(edges);
        
        int counter = 0;
        while (edges.hasNext()){
            Edge edge = edges.next();
            counter++;
            validateEdge(edge, mapOriginToInstId);
        }
        assertEquals("check edge size", expectedEdgeCount, counter);
    }
    
    
    private String createIntancesFromSameResource(Map<String, List<String>> mapOriginToInstId, int originIndex, int countInstances) {
        return createIntancesFromSameResource(mapOriginToInstId,  originIndex,  countInstances, false, null); 
    }
    
    private String createIntancesFromSameResource(Map<String, List<String>> mapOriginToInstId, int originIndex, int countInstances, boolean isProxy, String proxyId) {
        
        List<String> exp = new ArrayList();
        String originId = createOrigin(originIndex);
        
        for ( int i = 0; i < countInstances; i++){
            String instanceId = createInstanceEdge(containerVertex, originId, originIndex, i+1, isProxy, proxyId); 
            exp.add(instanceId);
        }
        mapOriginToInstId.put(originId, exp);
        if ( isProxy ){
            mapOriginToInstId.put(proxyId, exp); 
        }
        return originId;
    }
    
    private String createInstanceEdge(GraphVertex containerVertex, String originId, int originIndex, int insIndex, boolean isProxy, String proxyId) {
        ComponentInstanceDataDefinition componentInstance = new ComponentInstanceDataDefinition();
        componentInstance.setComponentUid(originId);
        String instanceId = buildInstanceId(originIndex, insIndex);
        componentInstance.setUniqueId(instanceId);
        componentInstance.setIsProxy(isProxy);
        componentInstance.setSourceModelUid(proxyId);
        StorageOperationStatus edgeStatus = nodeTemplateOperation.createInstanceEdge(containerVertex, componentInstance);
        assertEquals("assertion createInstanceEdge", StorageOperationStatus.OK, edgeStatus);
        return instanceId;
    }
    
    private String buildInstanceId(int originIndex, int insIndex) {
        StringBuilder sb = new StringBuilder("instanceId_");
        sb.append(originIndex).append("-").append(insIndex);
        return  sb.toString();
    }
    private String removeInstanceEdge(GraphVertex containerVertex, String originId, int originIndex, int insIndex) {
        ComponentInstanceDataDefinition componentInstance = new ComponentInstanceDataDefinition();
        componentInstance.setComponentUid(originId);
        String instanceId = buildInstanceId(originIndex, insIndex);
        componentInstance.setUniqueId(instanceId);
        StorageOperationStatus edgeStatus = nodeTemplateOperation.removeInstanceEdge(containerVertex, componentInstance);
        assertEquals("assertion removeInstanceEdge", StorageOperationStatus.OK, edgeStatus);
        return instanceId;
    }
    
    
    private String createOrigin(int index) {
        Either<GraphVertex, JanusGraphOperationStatus> createVertex;
        GraphVertex originVertex = new GraphVertex(VertexTypeEnum.NODE_TYPE);
        String originId = "originId_" + index;
        originVertex.setUniqueId(originId);
        createVertex = janusGraphDao.createVertex(originVertex);
        assertTrue(createVertex.isLeft());
        return originId;
    }
    private void validateEdge(Edge edge, Map<String, List<String>> mapOriginToInstId) {
        List<String> expextedInList;
        
        Vertex originV = edge.inVertex();
        String id = (String) janusGraphDao.getProperty((JanusGraphVertex)originV, GraphPropertyEnum.UNIQUE_ID.getProperty());
        expextedInList = mapOriginToInstId.get(id);
        
        List<String> list = (List<String>) janusGraphDao.getProperty(edge, EdgePropertyEnum.INSTANCES);
        assertThat(list).hasSameSizeAs(expextedInList);
        assertThat(list).containsOnlyElementsOf(expextedInList);
    }
    
    private String exportGraphMl(JanusGraph graph) {
        String result = null;
        String outputFile = outputDirectory + File.separator + "exportGraph." + System.currentTimeMillis() + ".graphml";
        try {
            try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                graph.io(IoCore.graphml()).writer().normalize(true).create().writeGraph(os, graph);
            }
            result = outputFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }
}
