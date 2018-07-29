package org.openecomp.sdc.asdctool.impl.internal.tool;

import com.thinkaurelius.titan.core.TitanVertex;
import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.asdctool.utils.ConsoleWriter;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementOperation;
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
    private TitanDao titanDao;
    @Autowired
    private NodeTypeOperation nodeTypeOperation;
    @Autowired
    private TopologyTemplateOperation topologyTemplateOperation;
    
  
    private static Logger log = Logger.getLogger(DeleteComponentHandler.class.getName());

    
    public DeleteComponentHandler(){
        super("delete");
    }
    public void deleteComponent(String id, Scanner scanner) {
        TitanOperationStatus status = TitanOperationStatus.OK;
        GraphVertex metadataVertex = titanDao.getVertexById(id).either(l -> l, r -> null);
        if (metadataVertex != null) {
            status = handleComponent(scanner, metadataVertex);
        } else {
            ConsoleWriter.dataLine("No vertex for id", id);
        }
        if (status == TitanOperationStatus.OK) {
            titanDao.commit();
        } else {
            titanDao.rollback();
        }
    }

    private TitanOperationStatus handleComponent(Scanner scanner, GraphVertex metadataVertex) {
        Map<GraphPropertyEnum, Object> metadataProperties = metadataVertex.getMetadataProperties();
        TitanOperationStatus status = TitanOperationStatus.OK;
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

    private TitanOperationStatus handleComponent(GraphVertex metadataVertex) {
        ToscaElementOperation toscaElementOperation = getOperationByLabel(metadataVertex);
        Iterator<Edge> edges = metadataVertex.getVertex().edges(Direction.IN, EdgeLabelEnum.VERSION.name());
        if (edges != null && edges.hasNext()) {
            TitanOperationStatus status = updatePreviousVersion(metadataVertex, edges);
            if ( status != TitanOperationStatus.OK ){
                return status;
            }
        }
        toscaElementOperation.deleteToscaElement(metadataVertex)
             .left()
             .map(l -> {
                 ConsoleWriter.dataLine("\nDeleted");
                 report(metadataVertex);
                 return TitanOperationStatus.OK;
             })
             .right()
             .map(r-> {
                 ConsoleWriter.dataLine("\nFailed to delete. see log file");
                 return r;
             });
        return TitanOperationStatus.OK;
    }

    private TitanOperationStatus updatePreviousVersion(GraphVertex metadataVertex, Iterator<Edge> edges) {
        Edge edge = edges.next();
        TitanVertex prevVersionVertex = (TitanVertex) edge.outVertex();
        // check if previous version is deleted
        Boolean isDeleted = (Boolean) titanDao.getProperty(prevVersionVertex, GraphPropertyEnum.IS_DELETED.getProperty());
        if (isDeleted != null && isDeleted) {
            ConsoleWriter.dataLine("\nPrevoius version is marked as deleted. Component cannot be deleted");
            return TitanOperationStatus.GENERAL_ERROR;
        }
        // update highest property for previous version
        TitanOperationStatus status = updateStateOfPreviuosVersion(prevVersionVertex);
        if ( TitanOperationStatus.OK != status ){
            return status;
        }
        
        // connect to catalog or archive
        return connectToCatalogAndArchive(metadataVertex, prevVersionVertex);
    }

    private TitanOperationStatus updateStateOfPreviuosVersion(TitanVertex prevVersionVertex) {
        String prevId = (String) titanDao.getProperty(prevVersionVertex, GraphPropertyEnum.UNIQUE_ID.getProperty());
        Either<GraphVertex, TitanOperationStatus> prevGraphVertex = titanDao.getVertexById(prevId);
        GraphVertex prevVertex = prevGraphVertex.left().value();
        prevVertex.addMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        titanDao.updateVertex(prevVertex);
  
        Iterator<Edge> edgesIter = prevVersionVertex.edges(Direction.IN, EdgeLabelEnum.LAST_STATE.name());
        if ( edgesIter.hasNext() ) {
            Edge lastStateEdge = edgesIter.next();
            Vertex lastModifier = lastStateEdge.outVertex();
            TitanOperationStatus replaceRes = titanDao.replaceEdgeLabel(lastModifier, prevVersionVertex, lastStateEdge, EdgeLabelEnum.LAST_STATE, EdgeLabelEnum.STATE);
            if (replaceRes != TitanOperationStatus.OK) {
                log.info("Failed to replace label from {} to {}. status = {}", EdgeLabelEnum.LAST_STATE, EdgeLabelEnum.STATE, replaceRes);
                ConsoleWriter.dataLine("\nFailed to replace LAST_STATE edge . Failed to delete");
                return TitanOperationStatus.GENERAL_ERROR;
            }
        }
        return TitanOperationStatus.OK;
    }

   
    private TitanOperationStatus connectToCatalogAndArchive(GraphVertex metadataVertex, TitanVertex prevVersionVertex) {
        
        TitanOperationStatus status = connectByLabel(metadataVertex, prevVersionVertex, EdgeLabelEnum.CATALOG_ELEMENT, VertexTypeEnum.CATALOG_ROOT);
        if ( status == TitanOperationStatus.OK ){
            status = connectByLabel(metadataVertex, prevVersionVertex, EdgeLabelEnum.ARCHIVE_ELEMENT, VertexTypeEnum.ARCHIVE_ROOT);
        }
        return status;
    }

    private TitanOperationStatus connectByLabel(GraphVertex metadataVertex, TitanVertex prevVersionVertex, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexlabel) {
        Iterator<Edge> edgesToCatalog = metadataVertex.getVertex().edges(Direction.IN, edgeLabel.name());
        if ( edgesToCatalog != null && edgesToCatalog.hasNext() ){
            //exist edge move to prev version
            Either<GraphVertex, TitanOperationStatus> catalog = titanDao.getVertexByLabel(vertexlabel);
            if (catalog.isRight()) {
                log.debug("Failed to fetch {} vertex, error {}", vertexlabel, catalog.right().value());
                return catalog.right().value();
            }
            GraphVertex catalogV = catalog.left().value();      
            Edge edge = edgesToCatalog.next();
            return titanDao.createEdge(catalogV.getVertex(), prevVersionVertex, edgeLabel, edge );
        }
        return TitanOperationStatus.OK;
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
