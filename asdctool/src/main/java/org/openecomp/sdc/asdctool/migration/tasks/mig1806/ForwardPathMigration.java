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
import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.utils.IdBuilderUtils;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;

@org.springframework.stereotype.Component
public class ForwardPathMigration implements Migration {

    private JanusGraphDao janusGraphDao;
    private UserAdminOperation userAdminOperation;
    private ToscaOperationFacade toscaOperationFacade;
    private User user = null;

    public ForwardPathMigration(JanusGraphDao janusGraphDao,
        UserAdminOperation userAdminOperation, ToscaOperationFacade toscaOperationFacade) {
        this.janusGraphDao = janusGraphDao;
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
        final String userId = ConfigurationManager.getConfigurationManager().getConfiguration().getAutoHealingOwner();

        Either<User, ActionStatus> userData = (Either<User, ActionStatus>) userAdminOperation
            .getUserData(userId, false);
        if (userData.isRight()) {
             return MigrationResult.error(
                "failed to delete unused forwarding paths. Failed to resolve user : " + userId + " error " + userData
                    .right().value());
        } else {
            user = userData.left().value();
        }
        StorageOperationStatus status = cleanAllServices();

        return status == StorageOperationStatus.OK ? MigrationResult.success()
            : MigrationResult.error("failed to remove corrupted forwarding paths . Error : " + status);

    }

    private StorageOperationStatus cleanAllServices() {
        StorageOperationStatus status;

        Map<GraphPropertyEnum, Object> hasProps = new EnumMap<>(GraphPropertyEnum.class);
        hasProps.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        Map<GraphPropertyEnum, Object> hasNotProps = new HashMap<>();
        hasNotProps.put(GraphPropertyEnum.IS_DELETED, true);
        status = janusGraphDao
            .getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, hasProps, hasNotProps, JsonParseFlagEnum.ParseAll)
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
                .getToscaElement(container.getUniqueId(), componentParametersView);
            if (toscaElement.isRight()) {
                return toscaElement.right().value();
            }
            status = fixDataOnGraph(toscaElement.left().value());
            if (status != StorageOperationStatus.OK) {
                break;
            }
        }
        return status;
    }


    private StorageOperationStatus handleError(JanusGraphOperationStatus err) {
        janusGraphDao.rollback();
        return DaoStatusConverter
            .convertJanusGraphStatusToStorageStatus(
                JanusGraphOperationStatus.NOT_FOUND == err ? JanusGraphOperationStatus.OK : err);
    }

    private StorageOperationStatus fixDataOnGraph(Component component) {
        if (!(component instanceof Service)){
            return StorageOperationStatus.OK;
        }
        Service service = (Service) component;
        Either<GraphVertex, JanusGraphOperationStatus> getResponse = janusGraphDao.getVertexById(service.getUniqueId(),
            JsonParseFlagEnum.NoParse);
        if (getResponse.isRight()) {
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getResponse.right().value());

        }
        Set<String> ciNames = new HashSet<>();
        if (service.getComponentInstances() != null && !service.getComponentInstances().isEmpty()) {
            ciNames = service.getComponentInstances().stream().map(ci -> ci.getName())
                .collect(Collectors.toSet());
        }
        GraphVertex componentVertex = getResponse.left().value();

        GraphVertex toscaDataVertex;
        Either<GraphVertex, JanusGraphOperationStatus> groupVertexEither = janusGraphDao.getChildVertex(componentVertex,
            EdgeLabelEnum.FORWARDING_PATH, JsonParseFlagEnum.ParseJson);
        if (groupVertexEither.isRight() && groupVertexEither.right().value() == JanusGraphOperationStatus.NOT_FOUND) {
            return StorageOperationStatus.OK;
        }
        if (groupVertexEither.isRight()) {
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(groupVertexEither.right().value());
        }
        toscaDataVertex = groupVertexEither.left().value();
        Map<String, ForwardingPathDataDefinition> forwardingPaths = new HashMap<>(
            (Map<String, ForwardingPathDataDefinition>) toscaDataVertex.getJson());
        List<String> toBeDeletedFP = new ArrayList<>();
        for (Map.Entry<String, ForwardingPathDataDefinition> forwardingPathDataDefinition : forwardingPaths
            .entrySet()) {
            Set<String> nodeNames = forwardingPathDataDefinition.getValue().getPathElements()
                .getListToscaDataDefinition()
                .stream().map(element -> ImmutableSet.of(element.getFromNode(), element.getToNode()))
                .flatMap(set -> set.stream()).collect(Collectors.toSet());
            if (!ciNames.containsAll(nodeNames)) {
                toBeDeletedFP.add(forwardingPathDataDefinition.getKey());
            }
        }
        if (toBeDeletedFP.isEmpty()) {
            janusGraphDao.rollback();
            return StorageOperationStatus.OK;
        }
        toBeDeletedFP.stream().forEach(fpKey -> forwardingPaths.remove(fpKey));
        toscaDataVertex.setJson(forwardingPaths);
        Either<GraphVertex, JanusGraphOperationStatus> updatevertexEither = updateOrCopyOnUpdate(
             toscaDataVertex, componentVertex);
          if (updatevertexEither.isRight()) {
            janusGraphDao.rollback();
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(updatevertexEither.right().value());
        }
        janusGraphDao.commit();
        return StorageOperationStatus.OK;
    }

    private Either<GraphVertex, JanusGraphOperationStatus> cloneDataVertex(GraphVertex dataVertex, GraphVertex toscaElementVertex, Edge edgeToRemove) {
        EdgeLabelEnum label =  EdgeLabelEnum.FORWARDING_PATH;
        GraphVertex newDataVertex = new GraphVertex(dataVertex.getLabel());
        String id = IdBuilderUtils.generateChildId(toscaElementVertex.getUniqueId(), dataVertex.getLabel());
        newDataVertex.cloneData(dataVertex);
        newDataVertex.setUniqueId(id);

        Either<GraphVertex, JanusGraphOperationStatus> createVertex = janusGraphDao.createVertex(newDataVertex);
        if (createVertex.isRight()) {
            return createVertex;
        }
        newDataVertex = createVertex.left().value();
        JanusGraphOperationStatus
            createEdge = janusGraphDao
            .createEdge(toscaElementVertex, newDataVertex, label, janusGraphDao.getEdgeProperties(edgeToRemove));
        if (createEdge != JanusGraphOperationStatus.OK) {
                return Either.right(createEdge);
        }
        edgeToRemove.remove();
        return Either.left(newDataVertex);
    }

    private Either<GraphVertex, JanusGraphOperationStatus> updateOrCopyOnUpdate(GraphVertex dataVertex, GraphVertex toscaElementVertex ) {
        EdgeLabelEnum label = EdgeLabelEnum.FORWARDING_PATH;
        Iterator<Edge> edges = dataVertex.getVertex().edges(Direction.IN, label.name());
        int edgeCount = 0;
        Edge edgeToRemove = null;
        while (edges.hasNext()) {
            Edge edge = edges.next();
            ++edgeCount;
            Vertex outVertex = edge.outVertex();
            String outId = (String) janusGraphDao
                .getProperty((JanusGraphVertex) outVertex, GraphPropertyEnum.UNIQUE_ID.getProperty());
            if (toscaElementVertex.getUniqueId().equals(outId)) {
                edgeToRemove = edge;
            }
        }
        if (edgeToRemove == null) {
            return Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
        }
        switch (edgeCount) {
            case 0:
                // error
                 return Either.right(JanusGraphOperationStatus.GENERAL_ERROR);
            case 1:
                // update
                return janusGraphDao.updateVertex(dataVertex);
            default:
                // copy on update
                return cloneDataVertex(dataVertex, toscaElementVertex,  edgeToRemove);
        }
    }
}
