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

package org.openecomp.sdc.asdctool.migration.tasks.mig1806;

import com.google.common.collect.ImmutableSet;
import fj.data.Either;
import java.math.BigInteger;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

@org.springframework.stereotype.Component
public class ForwardPathMigration implements Migration {

    private static final Logger logger = Logger.getLogger(ForwardPathMigration.class);

    private TitanDao titanDao;
    private ServiceBusinessLogic serviceBusinessLogic;
    private UserAdminOperation userAdminOperation;
    private ToscaOperationFacade toscaOperationFacade;
    private User user = null;

    public ForwardPathMigration(TitanDao titanDao, ServiceBusinessLogic serviceBusinessLogic,
            UserAdminOperation userAdminOperation, ToscaOperationFacade toscaOperationFacade) {
        this.titanDao = titanDao;
        this.serviceBusinessLogic = serviceBusinessLogic;
        this.userAdminOperation = userAdminOperation;
        this.toscaOperationFacade = toscaOperationFacade;
    }

    @Override
    public String description() {
        return "remove corrupted forwarding paths ";
    }

    @Override
    public DBVersion getVersion() {
        return DBVersion.from(BigInteger.valueOf(1806), BigInteger.valueOf(0));
    }

    @Override
    public MigrationResult migrate() {
        logger.info("start remove corrupted forwarding path ");
        final String userId = ConfigurationManager.getConfigurationManager().getConfiguration().getAutoHealingOwner();

        Either<User, ActionStatus> userData = (Either<User, ActionStatus>) userAdminOperation
                                                                                   .getUserData(userId, false);
        if (userData.isRight()) {
            logger.error("Upgrade migration failed. User {} resolve failed: {} ", userId, userData.right().value());
            return MigrationResult.error(
                    "failed to delete unused forwarding paths. Failed to resolve user : " + userId + " error " + userData
                                                                                                                         .right().value());
        } else {
            user = userData.left().value();
            logger.info("User {} will perform upgrade operation with role {}", user.getUserId(), user.getRole());
        }
        StorageOperationStatus status = cleanAllServices();

        return status == StorageOperationStatus.OK ? MigrationResult.success()
                       : MigrationResult.error("failed to remove corrupted forwarding paths . Error : " + status);

    }

    private StorageOperationStatus cleanAllServices() {
        StorageOperationStatus status;

        Map<GraphPropertyEnum, Object> hasProps = new EnumMap<>(GraphPropertyEnum.class);
        hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        status = titanDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps, null, JsonParseFlagEnum.ParseAll)
                         .either(this::cleanServices, this::handleError);
        return status;
    }

    private StorageOperationStatus cleanServices(List<GraphVertex> containersV) {
        StorageOperationStatus status = StorageOperationStatus.OK;
        for (GraphVertex container : containersV) {
            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.setIgnoreComponentInstances(false);
            componentParametersView.setIgnoreCapabilities(false);
            componentParametersView.setIgnoreRequirements(false);
            componentParametersView.setIgnoreForwardingPath(false);
            Either<Component, StorageOperationStatus> toscaElement = toscaOperationFacade
                                                                             .getToscaElement(container.getUniqueId() , componentParametersView);
            if (toscaElement.isRight()) {
                return toscaElement.right().value();
            }
            status = cleanService(toscaElement.left().value());
            if (status != StorageOperationStatus.OK) {
                break;
            }
        }
        return status;
    }

    private StorageOperationStatus cleanService(Component component) {
        if (!(component instanceof Service)) {
            titanDao.rollback();
            return StorageOperationStatus.OK;
        }
        Service service = (Service) component;
        Set<String> ciNames = service.getComponentInstances().stream().map(ci -> ci.getName())
                                     .collect(Collectors.toSet());

        Map<String, ForwardingPathDataDefinition> forwardingPaths = service.getForwardingPaths();
        Set<String> toBeDeletedFP = new HashSet<>();
        for (ForwardingPathDataDefinition forwardingPathDataDefinition : forwardingPaths.values()) {
            Set<String> nodeNames = forwardingPathDataDefinition.getPathElements().getListToscaDataDefinition()
                                                                .stream().map(element -> ImmutableSet.of(element.getFromNode(), element.getToNode()))
                                                                .flatMap(set -> set.stream()).collect(Collectors.toSet());
            if (!ciNames.containsAll(nodeNames)) {
                toBeDeletedFP.add(forwardingPathDataDefinition.getUniqueId());
            }
        }
        if (toBeDeletedFP.isEmpty()) {
            titanDao.rollback();
            return StorageOperationStatus.OK;
        }
        final Either<Set<String>, ResponseFormat> deleteEither = serviceBusinessLogic
                                                                         .deleteForwardingPaths(service.getUniqueId(), toBeDeletedFP, user, false);
        final String fpString = toBeDeletedFP.stream().collect(Collectors.joining(",", "[", "]"));
        if (deleteEither.isRight()) {
            logger.info("User : {} , Service: {} removed corrupted forwarding paths named : {} failed with error {}",
                    user.getUserId(), service.getName(),
                    fpString, deleteEither.right().value().getFormattedMessage());
            return StorageOperationStatus.GENERAL_ERROR;
        }
        logger.info("User : {} , Service: {} removed corrupted forwarding paths named : {}", user.getUserId(),
                service.getName(),
                fpString);
        return StorageOperationStatus.OK;
    }

    private StorageOperationStatus handleError(TitanOperationStatus err) {
        titanDao.rollback();
        return DaoStatusConverter
                       .convertTitanStatusToStorageStatus(TitanOperationStatus.NOT_FOUND == err ? TitanOperationStatus.OK : err);
    }
}
