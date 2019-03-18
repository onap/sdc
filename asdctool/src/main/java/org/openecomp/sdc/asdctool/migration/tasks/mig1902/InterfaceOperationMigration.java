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

package org.openecomp.sdc.asdctool.migration.tasks.mig1902;

import fj.data.Either;
import java.math.BigInteger;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapInterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component
public class InterfaceOperationMigration implements Migration {

    private static final Logger LOGGER = Logger.getLogger(InterfaceOperationMigration.class);

    @Autowired
    private TitanDao titanDao;
    @Autowired
    private UserAdminOperation userAdminOperation;
    @Autowired
    private InterfaceOperation interfaceOperation;

    @Override
    public String description() {
        return "Update interface operation data to latest data model";
    }

    @Override
    public DBVersion getVersion() {
        return DBVersion.from(BigInteger.valueOf(1902), BigInteger.valueOf(0));
    }

    @Override
    public MigrationResult migrate() {
        final String userId = ConfigurationManager.getConfigurationManager().getConfiguration().getAutoHealingOwner();

        Either<User, ActionStatus> userData = userAdminOperation.getUserData(userId, false);
        if (userData.isRight()) {
            return MigrationResult.error(
                    "failed to update interface operation data. Failed to resolve user : "
                            + userId + " error " + userData.right().value());
        }

        StorageOperationStatus status = getAndUpdateAllComponents();
        return status == StorageOperationStatus.OK ? MigrationResult.success()
                : MigrationResult.error("failed to update interface operation data . Error : " + status);
    }

    private StorageOperationStatus getAndUpdateAllComponents(){
        Map<GraphPropertyEnum, Object> hasNotProps = new EnumMap<>(GraphPropertyEnum.class);
        hasNotProps.put(GraphPropertyEnum.IS_DELETED, true);
        return titanDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, null, hasNotProps, JsonParseFlagEnum.ParseAll)
                .either(this::updateComponentVertices, this::handleError);
    }

    private StorageOperationStatus updateComponentVertices(List<GraphVertex> containersV) {
        StorageOperationStatus status = StorageOperationStatus.OK;
        for (GraphVertex container : containersV) {
            status = updateDataOnGraph(container);
            if (status != StorageOperationStatus.OK) {
                break;
            }
        }
        return status;
    }

    private StorageOperationStatus handleError(TitanOperationStatus err) {
        titanDao.rollback();
        return DaoStatusConverter.convertTitanStatusToStorageStatus(
                TitanOperationStatus.NOT_FOUND == err ? TitanOperationStatus.OK : err);
    }

    private StorageOperationStatus updateDataOnGraph(GraphVertex componentVertex) {
        try {
            Either<GraphVertex, TitanOperationStatus> interfaceVertexEither =
                    titanDao.getChildVertex(componentVertex, EdgeLabelEnum.INTERFACE, JsonParseFlagEnum.ParseJson);
            if (interfaceVertexEither.isLeft()) {
                GraphVertex interfaceVertex = interfaceVertexEither.left().value();
                Map<String, InterfaceDataDefinition> interfaceDefinitions = (Map<String, InterfaceDataDefinition>) interfaceVertex.getJson();
                if(MapUtils.isNotEmpty(interfaceDefinitions)){
                    for (Map.Entry<String, InterfaceDataDefinition> interfaceDefinition : interfaceDefinitions.entrySet()) {
                        if (StringUtils.isEmpty(interfaceDefinition.getValue().getType())) {
                            interfaceDefinition.getValue().setType(interfaceDefinition.getValue().getToscaResourceName());
                        }
                    }
                    interfaceVertex.setJson(interfaceDefinitions);
                    Either<GraphVertex, TitanOperationStatus> updateInterfaceVertexEither = titanDao.updateVertex(interfaceVertex);
                    if(updateInterfaceVertexEither.isRight()){
                        return DaoStatusConverter.convertTitanStatusToStorageStatus(updateInterfaceVertexEither.right().value());
                    }
                }

                StorageOperationStatus statusRes = interfaceOperation.removeToscaDataVertex(
                        interfaceVertex, EdgeLabelEnum.INTERFACE_OPERATION, VertexTypeEnum.INTERFACE_OPERATION);
                if (statusRes != StorageOperationStatus.NOT_FOUND && statusRes != StorageOperationStatus.OK) {
                    return statusRes;
                }
            }

            Either<GraphVertex, TitanOperationStatus> instInterfaceVertexEither =
                    titanDao.getChildVertex(componentVertex, EdgeLabelEnum.INST_INTERFACES, JsonParseFlagEnum.ParseJson);
            if (instInterfaceVertexEither.isLeft()) {
                GraphVertex instInterfaceVertex = instInterfaceVertexEither.left().value();
                Map<String, MapInterfaceDataDefinition> instInterfaceDefinitions = (Map<String, MapInterfaceDataDefinition>) instInterfaceVertex.getJson();
                if(MapUtils.isNotEmpty(instInterfaceDefinitions)){
                    for (Map.Entry<String, MapInterfaceDataDefinition> mapInstInterfaceDataDefinitions : instInterfaceDefinitions.entrySet()) {
                        for (Map.Entry<String, InterfaceDataDefinition> instInterfaceDataDefinitions : mapInstInterfaceDataDefinitions.getValue().getMapToscaDataDefinition().entrySet()) {
                            if (StringUtils.isEmpty(instInterfaceDataDefinitions.getValue().getType())) {
                                instInterfaceDataDefinitions.getValue().setType(instInterfaceDataDefinitions.getValue().getToscaResourceName());
                            }
                        }
                    }
                    instInterfaceVertex.setJson(instInterfaceDefinitions);
                    Either<GraphVertex, TitanOperationStatus> updateInstInterfaceVertexEither = titanDao.updateVertex(instInterfaceVertex);
                    if(updateInstInterfaceVertexEither.isRight()){
                        return DaoStatusConverter.convertTitanStatusToStorageStatus(updateInstInterfaceVertexEither.right().value());
                    }
                }
            }

            titanDao.commit();
        } catch (Exception e) {
            LOGGER.debug("Interface operation migration failed with error : ", e);
            titanDao.rollback();
            return StorageOperationStatus.GENERAL_ERROR;
        }

        return StorageOperationStatus.OK;
    }
}