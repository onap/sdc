/*

 * Copyright (c) 2018 AT&T Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */
package org.openecomp.sdc.asdctool.impl.internal.tool;

import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.asdctool.utils.ConsoleWriter;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaElementOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

@Component("deleteComponentHandler")
public class DeleteComponentHandler extends CommonInternalTool{
    @Autowired
    private JanusGraphDao janusGraphDao;
    @Autowired
    private NodeTypeOperation nodeTypeOperation;
    @Autowired
    private TopologyTemplateOperation topologyTemplateOperation;
    
  
    private static Logger log = Logger.getLogger(DeleteComponentHandler.class.getName());

    
    public DeleteComponentHandler(){
        super("delete");
    }
    public void deleteComponent(String id, Scanner scanner) {
        JanusGraphOperationStatus status = JanusGraphOperationStatus.OK;
        GraphVertex metadataVertex = janusGraphDao.getVertexById(id).either(l -> l, r -> null);
        if (metadataVertex != null) {
            status = handleComponent(scanner, metadataVertex);
        } else {
            ConsoleWriter.dataLine("No vertex for id", id);
        }
        if (status == JanusGraphOperationStatus.OK) {
            janusGraphDao.commit();
        } else {
            janusGraphDao.rollback();
        }
    }

    private JanusGraphOperationStatus handleComponent(Scanner scanner, GraphVertex metadataVertex) {
        Map<GraphPropertyEnum, Object> metadataProperties = metadataVertex.getMetadataProperties();
        JanusGraphOperationStatus status = JanusGraphOperationStatus.OK;
        printComponentInfo(metadataProperties);

        Iterator<Edge> edges = metadataVertex.getVertex().edges(Direction.OUT, EdgeLabelEnum.VERSION.name());
        if (edges != null && edges.hasNext()) {
            ConsoleWriter.dataLine("\ncomponent is not latest version and cannot be deleted");
        } else {
            ConsoleWriter.dataLine("\ncomponent is latest .");
            if (isReferenceExist(metadataVertex)) {
                ConsoleWriter.dataLine("\nExist reference on component ( istance, proxy or allotted). Component cannot be deleted");
            } else {
                ConsoleWriter.dataLine("\nNo references. Try to delete (yes/no)?");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("yes")) {
                    status = handleComponent(metadataVertex);
                }
            }
        }
        return status;
    }

    private JanusGraphOperationStatus handleComponent(GraphVertex metadataVertex) {
        ToscaElementOperation toscaElementOperation = getOperationByLabel(metadataVertex);
        Iterator<Edge> edges = metadataVertex.getVertex().edges(Direction.IN, EdgeLabelEnum.VERSION.name());
        if (edges != null && edges.hasNext()) {
            JanusGraphOperationStatus status = updatePreviousVersion(metadataVertex, edges);
            if ( status != JanusGraphOperationStatus.OK ){
                return status;
            }
        }
        toscaElementOperation.deleteToscaElement(metadataVertex)
             .left()
             .map(l -> {
                 ConsoleWriter.dataLine("\nDeleted");
                 report(metadataVertex);
                 return JanusGraphOperationStatus.OK;
             })
             .right()
             .map(r-> {
                 ConsoleWriter.dataLine("\nFailed to delete. see log file");
                 return r;
             });
        return JanusGraphOperationStatus.OK;
    }

    private JanusGraphOperationStatus updatePreviousVersion(GraphVertex metadataVertex, Iterator<Edge> edges) {
        Edge edge = edges.next();
        JanusGraphVertex prevVersionVertex = (JanusGraphVertex) edge.outVertex();
        // check if previous version is deleted
        Boolean isDeleted = (Boolean) janusGraphDao.getProperty(prevVersionVertex, GraphPropertyEnum.IS_DELETED.getProperty());
        if (isDeleted != null && isDeleted) {
            ConsoleWriter.dataLine("\nPrevoius version is marked as deleted. Component cannot be deleted");
            return JanusGraphOperationStatus.GENERAL_ERROR;
        }
        // update highest property for previous version
        JanusGraphOperationStatus status = updateStateOfPreviuosVersion(prevVersionVertex);
        if ( JanusGraphOperationStatus.OK != status ){
            return status;
        }
        
        // connect to catalog or archive
        return connectToCatalogAndArchive(metadataVertex, prevVersionVertex);
    }

    private JanusGraphOperationStatus updateStateOfPreviuosVersion(JanusGraphVertex prevVersionVertex) {
        String prevId = (String) janusGraphDao.getProperty(prevVersionVertex, GraphPropertyEnum.UNIQUE_ID.getProperty());
        Either<GraphVertex, JanusGraphOperationStatus> prevGraphVertex = janusGraphDao.getVertexById(prevId);
        GraphVertex prevVertex = prevGraphVertex.left().value();
        prevVertex.addMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        janusGraphDao.updateVertex(prevVertex);
  
        Iterator<Edge> edgesIter = prevVersionVertex.edges(Direction.IN, EdgeLabelEnum.LAST_STATE.name());
        if ( edgesIter.hasNext() ) {
            Edge lastStateEdge = edgesIter.next();
            Vertex lastModifier = lastStateEdge.outVertex();
            JanusGraphOperationStatus
                replaceRes = janusGraphDao
                .replaceEdgeLabel(lastModifier, prevVersionVertex, lastStateEdge, EdgeLabelEnum.LAST_STATE, EdgeLabelEnum.STATE);
            if (replaceRes != JanusGraphOperationStatus.OK) {
                log.info("Failed to replace label from {} to {}. status = {}", EdgeLabelEnum.LAST_STATE, EdgeLabelEnum.STATE, replaceRes);
                ConsoleWriter.dataLine("\nFailed to replace LAST_STATE edge . Failed to delete");
                return JanusGraphOperationStatus.GENERAL_ERROR;
            }
        }
        return JanusGraphOperationStatus.OK;
    }

   
    private JanusGraphOperationStatus connectToCatalogAndArchive(GraphVertex metadataVertex, JanusGraphVertex prevVersionVertex) {
        
        JanusGraphOperationStatus
            status = connectByLabel(metadataVertex, prevVersionVertex, EdgeLabelEnum.CATALOG_ELEMENT, VertexTypeEnum.CATALOG_ROOT);
        if ( status == JanusGraphOperationStatus.OK ){
            status = connectByLabel(metadataVertex, prevVersionVertex, EdgeLabelEnum.ARCHIVE_ELEMENT, VertexTypeEnum.ARCHIVE_ROOT);
        }
        return status;
    }

    private JanusGraphOperationStatus connectByLabel(GraphVertex metadataVertex, JanusGraphVertex prevVersionVertex, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexlabel) {
        Iterator<Edge> edgesToCatalog = metadataVertex.getVertex().edges(Direction.IN, edgeLabel.name());
        if ( edgesToCatalog != null && edgesToCatalog.hasNext() ){
            //exist edge move to prev version
            Either<GraphVertex, JanusGraphOperationStatus> catalog = janusGraphDao.getVertexByLabel(vertexlabel);
            if (catalog.isRight()) {
                log.debug("Failed to fetch {} vertex, error {}", vertexlabel, catalog.right().value());
                return catalog.right().value();
            }
            GraphVertex catalogV = catalog.left().value();      
            Edge edge = edgesToCatalog.next();
            return janusGraphDao.createEdge(catalogV.getVertex(), prevVersionVertex, edgeLabel, edge );
        }
        return JanusGraphOperationStatus.OK;
    }

    private boolean isReferenceExist(GraphVertex metadataVertex) {
        return existEdgeByLabel(metadataVertex, EdgeLabelEnum.INSTANCE_OF) || existEdgeByLabel(metadataVertex, EdgeLabelEnum.PROXY_OF) || existEdgeByLabel(metadataVertex, EdgeLabelEnum.ALLOTTED_OF);
    }

    private boolean existEdgeByLabel(GraphVertex metadataVertex, EdgeLabelEnum label) {
        Iterator<Edge> edges = metadataVertex.getVertex().edges(Direction.IN, label.name());
        return (edges != null && edges.hasNext());
    }

    private ToscaElementOperation getOperationByLabel(GraphVertex metadataVertex) {
        VertexTypeEnum label = metadataVertex.getLabel();
        if (label == VertexTypeEnum.NODE_TYPE) {
            return nodeTypeOperation;
        } else {
            return topologyTemplateOperation;
        }
    }
   
    private void report(GraphVertex metadataVertex) {
        try {
            getReportWriter().report(metadataVertex.getMetadataProperties());
        } catch (IOException e) {
            ConsoleWriter.dataLine("\nFailed to created report file.");
        }
    }

 


}
