/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.asdctool.migration.tasks.mig2002;

import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.asdctool.enums.LifecycleStateEnum;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.asdctool.migration.tasks.InstanceMigrationBase;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class SdcCollapsingRolesCIPstateMigration extends InstanceMigrationBase implements Migration {

    private static final Logger log = LoggerFactory.getLogger(SdcCollapsingRolesCIPstateMigration.class);

    public SdcCollapsingRolesCIPstateMigration(JanusGraphDao janusGraphDao) {
        super(janusGraphDao);
    }

    @Override
    public String description() {
        return "update Service state from CERTIFICATION_IN_PROGRES to NOT_CERTIFIED_CHECKOUT state ";
    }

    @Override
    public DBVersion getVersion() {
        return DBVersion.from(BigInteger.valueOf(2002), BigInteger.valueOf(0));
    }

    @Override
    public MigrationResult migrate() {
        StorageOperationStatus status = updateServiceLifeCycleState();
        return status == StorageOperationStatus.OK ?
                MigrationResult.success() : MigrationResult.error("failed to service state. Error : " + status);
    }

    protected StorageOperationStatus updateServiceLifeCycleState() {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        propertiesToMatch.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.name());
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        Either<List<GraphVertex>, JanusGraphOperationStatus> byCriteria = janusGraphDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll);
        return byCriteria.either(this::proceed, this::handleError);
    }

    @Override
    protected StorageOperationStatus handleOneContainer(GraphVertex containerVorig) {
        StorageOperationStatus status = StorageOperationStatus.NOT_FOUND;
        GraphVertex containerV = getVertexById(containerVorig.getUniqueId());
        try {

            // update vertex state property from READY_FOR_CERTIFICATION to NOT_CERTIFIED_CHECKIN state

            Map<GraphPropertyEnum, Object> metadataProperties = containerV.getMetadataProperties();
            metadataProperties.put(GraphPropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.name());
            containerV.setMetadataProperties(metadataProperties);

             //update edges to meet above change
            // remove STATE and LAST_MODIFIER edges
            removeEdges(getVertexEdge(containerV, Direction.IN, EdgeLabelEnum.STATE));
            removeEdges(getVertexEdge(containerV, Direction.IN, EdgeLabelEnum.LAST_MODIFIER));

            //find designer with LS = NOT_CERTIFIED_CHECKIN
            Vertex relevantDesigner = findRelevantDesigner(getVertexEdge(containerV, Direction.IN, EdgeLabelEnum.LAST_STATE));
            removeEdges(getVertexEdge(containerV, Direction.IN, EdgeLabelEnum.LAST_STATE));
            Map<EdgePropertyEnum, Object> edgeProperties = new HashMap<>();
            edgeProperties.put(EdgePropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.name());
            JanusGraphOperationStatus createSTedgeStatus = janusGraphDao.createEdge(relevantDesigner, containerV.getVertex(), EdgeLabelEnum.STATE, edgeProperties);
            JanusGraphOperationStatus createLMedgeStatus = janusGraphDao.createEdge(relevantDesigner, containerV.getVertex(), EdgeLabelEnum.LAST_MODIFIER, new HashMap<>());

            status = updateVertexAndCommit(containerV);

        } catch (NullPointerException e) {
            log.error("Null Pointer Exception occurred - this mean we have zombie vertex, migration task will continue anyway", e);
            status = StorageOperationStatus.EXEUCTION_FAILED;
        } catch (Exception e) {
            //it is happy flow as well
            log.error("Exception occurred:", e);
            log.error("Migration task will continue anyway, please find below vertex details related to this exception", e);
            if (containerV != null) {
                log.error("containerV.getUniqueId() ---> {}  ", containerV.getUniqueId());
            }

        } finally {
            if (status != StorageOperationStatus.OK) {
                janusGraphDao.rollback();
                log.info("failed to update vertex ID {} ", containerV.getUniqueId());
                log.info("Storage Operation Status {}", status.toString());
            } else {
                log.info("vertex ID {} successfully updated", containerV.getUniqueId());
            }

        }
        return status;
    }

    private Vertex findRelevantDesigner(Iterator<Edge> edges) {
        Vertex vertex = null;
        while (edges.hasNext()) {
            Edge edge = edges.next();
            String state = (String) janusGraphDao.getProperty(edge, EdgePropertyEnum.STATE);
            if (state.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.name())) {
                vertex = edge.outVertex();
            }
        }
        return vertex;
    }

}
