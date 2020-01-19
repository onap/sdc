package org.openecomp.sdc.asdctool.migration.tasks;

import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.janusgraph.core.JanusGraphVertex;
import org.openecomp.sdc.asdctool.migration.tasks.mig2002.SdcCollapsingRolesRFCstateMigration;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public abstract class InstanceMigrationBase {

    private static final Logger log = LoggerFactory.getLogger(InstanceMigrationBase.class);
    protected JanusGraphDao janusGraphDao;

    public InstanceMigrationBase(JanusGraphDao janusGraphDao) {
        this.janusGraphDao = janusGraphDao;
    }

    protected StorageOperationStatus upgradeTopologyTemplates() {
        Map<GraphPropertyEnum, Object> hasNotProps = new EnumMap<>(GraphPropertyEnum.class);
        hasNotProps.put(GraphPropertyEnum.IS_DELETED, true);
        hasNotProps.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.CVFC);

        return janusGraphDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, null, hasNotProps, JsonParseFlagEnum.ParseAll)
                .either(this::proceed, this::handleError);
    }

    protected abstract StorageOperationStatus handleOneContainer(GraphVertex containerV);

    protected StorageOperationStatus proceed(List<GraphVertex> containersV) {
        int failureCounter = 0;
        log.info("found {} vertices to migrate ", containersV.size());
        for (GraphVertex container : containersV) {
            StorageOperationStatus storageOperationStatus = handleOneContainer(container);
            if (storageOperationStatus != StorageOperationStatus.OK) {
                failureCounter++;
            }
        }

        if (failureCounter > 0) {
            log.info("Failed to update {} vertices", failureCounter);
        } else {
            log.info("All vertices were successfully updated");
        }

        return StorageOperationStatus.OK;
    }

    protected GraphVertex getVertexById(String vertexId) {
        Either<GraphVertex, JanusGraphOperationStatus> vertexById = janusGraphDao.getVertexById(vertexId);
        if (vertexById.isRight()) {
            log.info("Exception occurred while query vertexId: {} exception: {} " + vertexId + vertexById.right().value());
            return null;
        }
        else return vertexById.left().value();
    }

    protected StorageOperationStatus updateVertexAndCommit(GraphVertex graphVertex) {
        StorageOperationStatus status;
        if ((status = janusGraphDao.updateVertex(graphVertex)
                .either(v -> StorageOperationStatus.OK, this::handleError)) != StorageOperationStatus.OK) {
            return status;
        }
        return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(janusGraphDao.commit());
    }

    protected StorageOperationStatus handleError(JanusGraphOperationStatus err) {
        return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.NOT_FOUND == err ? JanusGraphOperationStatus.OK : err);
    }

    protected void removeEdges(Iterator<Edge> edges) {

        while (edges.hasNext()) {
            Edge edge = edges.next();
            edge.remove();
        }
    }

    protected void removeEdgesInState(Iterator<Edge> edges, String state) {

        while (edges.hasNext()) {
            Edge edge = edges.next();
            String edgeState = (String) janusGraphDao.getProperty(edge, EdgePropertyEnum.STATE);
            if (edgeState.equals(state)) {
                edge.remove();
            }
        }
    }


    protected void updateEdgeProperty(EdgePropertyEnum property, String value, Iterator<Edge> edges) throws IOException {
        while (edges.hasNext()) {
            Edge edge = edges.next();
            Map<EdgePropertyEnum, Object> prop = new HashMap<>();
            prop.put(property, value);
            janusGraphDao.setEdgeProperties(edge, prop);
        }

    }


    // check if user has both edges state and last_state
    protected boolean sameUser(List<JanusGraphVertex> stateList, List<JanusGraphVertex> lastStateList) {

        for (JanusGraphVertex lsVertex : lastStateList) {
            String idLs = (String) janusGraphDao.getProperty(lsVertex, GraphPropertyEnum.USERID.getProperty());
            String idSt = (String) janusGraphDao.getProperty(stateList.get(0), GraphPropertyEnum.USERID.getProperty());
            if (idLs.equals(idSt)) {
                return true;
            }
        }
        return false;
    }

    protected List<JanusGraphVertex> getVertexByEdgeSide(Iterator<Edge> edges, SdcCollapsingRolesRFCstateMigration.EdgeSide side) {
        List<JanusGraphVertex> vertexList = new ArrayList();
        while (edges.hasNext()) {
            Edge edge = edges.next();

            if (side == SdcCollapsingRolesRFCstateMigration.EdgeSide.OUT) {
                vertexList.add((JanusGraphVertex) edge.outVertex());
            } else {
                vertexList.add((JanusGraphVertex) edge.inVertex());
            }
        }

        return vertexList;
    }

    protected Iterator<Edge> getVertexEdge(GraphVertex containerV, Direction direction, EdgeLabelEnum edgeLabel) {
        return containerV.getVertex().edges(direction, edgeLabel.name());
    }

    public enum EdgeSide {
        IN, OUT;
    }
}

