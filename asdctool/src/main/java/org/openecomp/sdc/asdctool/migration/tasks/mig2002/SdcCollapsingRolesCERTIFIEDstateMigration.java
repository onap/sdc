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
import org.openecomp.sdc.asdctool.enums.DistributionStatusEnum;
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
import java.util.List;
import java.util.Map;

@Component
public class SdcCollapsingRolesCERTIFIEDstateMigration extends InstanceMigrationBase implements Migration {

    private static final Logger log = LoggerFactory.getLogger(SdcCollapsingRolesCERTIFIEDstateMigration.class);

    public SdcCollapsingRolesCERTIFIEDstateMigration(JanusGraphDao janusGraphDao) {
        super(janusGraphDao);
    }

    @Override
    public String description() {
        return "remove LS=READY_FOR_CERTIFICATION edge from service node + migrate DISTRIBUTION approved/rejected states to <waiting for distribution> state";
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
        propertiesToMatch.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
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

            //update edges to meet above change
            // update LS eges from RFC to  NOT_CERTIFIED_CHECKIN

            updateEdgeProperty(EdgePropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.name(), getVertexEdge(containerV, Direction.IN, EdgeLabelEnum.LAST_STATE));

            if (containerV.getMetadataProperty(GraphPropertyEnum.DISTRIBUTION_STATUS).equals(DistributionStatusEnum.DISTRIBUTION_APPROVED.name()) || containerV.getMetadataProperty(GraphPropertyEnum.DISTRIBUTION_STATUS).equals(DistributionStatusEnum.DISTRIBUTION_REJECTED.name())) {

                // update vertex state property from DISTRIBUTION_APPROVED/REJECTED to DISTRIBUTION_NOT_APPROVED state

                Map<GraphPropertyEnum, Object> metadataProperties = containerV.getMetadataProperties();
                metadataProperties.put(GraphPropertyEnum.DISTRIBUTION_STATUS, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
                containerV.setMetadataProperties(metadataProperties);

                //update edges to meet above change
                //delete LAST_DISTRIBUTION_STATE_MODIFIER edge

                removeEdges(getVertexEdge(containerV, Direction.IN, EdgeLabelEnum.LAST_DISTRIBUTION_STATE_MODIFIER));

            }

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


}
