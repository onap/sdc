/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model.operations.impl;

import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.graph.GraphElementFactory;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.operations.api.IArtifactOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.HeatParameterData;
import org.openecomp.sdc.be.resources.data.HeatParameterValueData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("artifact-operation")
public class ArtifactOperation implements IArtifactOperation {

    private static final String THE_RETURNED_ARTIFACT_DEFINTION_IS = "The returned ArtifactDefintion is {}";

	@javax.annotation.Resource
    private JanusGraphGenericDao janusGraphGenericDao;

    @javax.annotation.Resource
    private HeatParametersOperation heatParametersOperation;

    @javax.annotation.Resource
    private GroupOperation groupOperation;
    @javax.annotation.Resource
    private GroupInstanceOperation groupInstanceOperation;

    private static final Logger log = Logger.getLogger(ArtifactOperation.class.getName());

    public ArtifactOperation() {
        super();
    }

    public JanusGraphGenericDao getJanusGraphGenericDao() {
        return janusGraphGenericDao;
    }

    public void setJanusGraphGenericDao(JanusGraphGenericDao janusGraphGenericDao) {
        this.janusGraphGenericDao = janusGraphGenericDao;
    }

    public HeatParametersOperation getHeatParametersOperation() {
        return heatParametersOperation;
    }

    public void setHeatParametersOperation(HeatParametersOperation heatParametersOperation) {
        this.heatParametersOperation = heatParametersOperation;
    }

    @Override
    public Either<ArtifactDefinition, StorageOperationStatus> addArifactToComponent(ArtifactDefinition artifactInfo, String parentId, NodeTypeEnum type, boolean failIfExist, boolean inTransaction) {

        Either<ArtifactData, StorageOperationStatus> status = addArtifactToGraph(artifactInfo, parentId, type, failIfExist);

        if (status.isRight()) {
            if (!inTransaction) {
                janusGraphGenericDao.rollback();
            }
            log.debug("Failed to add artifact {} to {} {}", artifactInfo.getArtifactName(), type , parentId);
            return Either.right(status.right().value());
        } else {
            if (!inTransaction) {
                janusGraphGenericDao.commit();
            }
            ArtifactData artifactData = status.left().value();

            ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(artifactData);

            log.debug(THE_RETURNED_ARTIFACT_DEFINTION_IS, artifactDefResult);
            return Either.left(artifactDefResult);
        }

    }

    @Override
    public StorageOperationStatus addArifactToComponent(ArtifactDefinition artifactInfo, String parentId, NodeTypeEnum type, boolean failIfExist, JanusGraphVertex parentVertex) {

        StorageOperationStatus status = addArtifactToGraph(artifactInfo, parentId, type, failIfExist, parentVertex);

        if (status.equals(StorageOperationStatus.OK)) {
            log.debug("Failed to add artifact {} {} to {}", artifactInfo.getArtifactName(), type, parentId);
        }
        return status;
    }

    private StorageOperationStatus addArtifactToGraph(ArtifactDefinition artifactInfo, String id, NodeTypeEnum type, boolean failIfexist, JanusGraphVertex parentVertex) {

        if (artifactInfo.getUniqueId() == null || artifactInfo.getUniqueId().isEmpty()) {
            String uniqueId = UniqueIdBuilder.buildPropertyUniqueId(id, artifactInfo.getArtifactLabel());
            artifactInfo.setUniqueId(uniqueId);
        }

        if (!validateParentType(type)) {
            return StorageOperationStatus.GENERAL_ERROR;
        }

        ArtifactData artifactData = new ArtifactData(artifactInfo);

        Either<JanusGraphVertex, JanusGraphOperationStatus> existArtifact = janusGraphGenericDao
            .getVertexByProperty(artifactData.getUniqueIdKey(), artifactData.getUniqueId());
        if (existArtifact.isRight()) {
            if (existArtifact.right().value().equals(JanusGraphOperationStatus.NOT_FOUND)) {
                // create new node
                log.debug("Before adding artifact to graph {}", artifactData);
                if (artifactData.getArtifactDataDefinition().getArtifactUUID() == null || artifactData.getArtifactDataDefinition().getArtifactUUID().isEmpty())
                    updateUUID(artifactData.getArtifactDataDefinition(), null, artifactData.getArtifactDataDefinition().getArtifactVersion());
                Either<JanusGraphVertex, JanusGraphOperationStatus> createNodeResult = janusGraphGenericDao.createNode(artifactData);

                if (createNodeResult.isRight()) {
                    JanusGraphOperationStatus operationStatus = createNodeResult.right().value();
                    log.debug("Failed to add artifact {} to graph. status is {}", artifactData.getArtifactDataDefinition().getArtifactName(), operationStatus);
                    BeEcompErrorManager.getInstance().logBeFailedCreateNodeError("Add artifact", artifactData.getArtifactDataDefinition().getArtifactName(), String.valueOf(operationStatus));
                    return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(operationStatus);
                }

                // add heat parameters
                if (artifactInfo.getHeatParameters() != null && !artifactInfo.getHeatParameters().isEmpty() && !artifactInfo.getArtifactType().equals(ArtifactTypeEnum.HEAT_ENV.getType())) {
                    StorageOperationStatus addPropertiesStatus = heatParametersOperation.addPropertiesToGraph(artifactInfo.getListHeatParameters(), artifactData.getUniqueId().toString(), NodeTypeEnum.ArtifactRef);
                    if (addPropertiesStatus != StorageOperationStatus.OK) {
                        log.debug("Failed to create heat parameters on graph for artifact {}", artifactInfo.getArtifactName());
                        return addPropertiesStatus;
                    }
                }

            } else {
                log.debug("Failed to check existance of artifact in graph for id {}", artifactData.getUniqueId());
                return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(existArtifact.right().value());
            }
        } else if (failIfexist) {
            log.debug("Artifact {} already exist", artifactData.getUniqueId());
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(JanusGraphOperationStatus.ALREADY_EXIST);
        }

        // save logical artifact ref name on edge as property
        Map<String, Object> properties = new HashMap<>();
        properties.put(GraphEdgePropertiesDictionary.NAME.getProperty(), artifactInfo.getArtifactLabel());
        if (artifactInfo.getArtifactGroupType() != null)
            properties.put(GraphEdgePropertiesDictionary.GROUP_TYPE.getProperty(), artifactInfo.getArtifactGroupType().getType());
        JanusGraphOperationStatus relation = janusGraphGenericDao
            .createEdge(parentVertex, artifactData, GraphEdgeLabels.ARTIFACT_REF, properties);
        if (!relation.equals(JanusGraphOperationStatus.OK)) {
            log.debug("Failed to create relation in graph for id {} to new artifact", id);
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(relation);
        }

        return StorageOperationStatus.OK;
    }

    private Either<ArtifactData, StorageOperationStatus> addArtifactToGraph(ArtifactDefinition artifactInfo, String id, NodeTypeEnum type, boolean failIfexist) {

        if (artifactInfo.getUniqueId() == null || artifactInfo.getUniqueId().isEmpty()) {
            String uniqueId = UniqueIdBuilder.buildPropertyUniqueId(id, artifactInfo.getArtifactLabel());
            artifactInfo.setUniqueId(uniqueId);
        }

        if (!validateParentType(type)) {
            return Either.right(StorageOperationStatus.GENERAL_ERROR);
        }

        ArtifactData artifactData = new ArtifactData(artifactInfo);

        Either<ArtifactData, JanusGraphOperationStatus> existArtifact = janusGraphGenericDao
            .getNode(artifactData.getUniqueIdKey(), artifactData.getUniqueId(), ArtifactData.class);
        if (existArtifact.isRight()) {
            if (existArtifact.right().value().equals(JanusGraphOperationStatus.NOT_FOUND)) {
                // create new node
                log.debug("Before adding artifact to graph {}" , artifactData);
                if (artifactData.getArtifactDataDefinition().getArtifactUUID() == null || artifactData.getArtifactDataDefinition().getArtifactUUID().isEmpty())
                    updateUUID(artifactData.getArtifactDataDefinition(), null, artifactData.getArtifactDataDefinition().getArtifactVersion());
                Either<ArtifactData, JanusGraphOperationStatus> createNodeResult = janusGraphGenericDao
                    .createNode(artifactData, ArtifactData.class);
                log.debug("After adding artifact to graph {}", artifactData);

                if (createNodeResult.isRight()) {
                    JanusGraphOperationStatus operationStatus = createNodeResult.right().value();
                    log.debug("Failed to add artifact {} to graph. status is {}", artifactData.getArtifactDataDefinition().getArtifactName(), operationStatus);
                    BeEcompErrorManager.getInstance().logBeFailedCreateNodeError("Add artifact", artifactData.getArtifactDataDefinition().getArtifactName(), String.valueOf(operationStatus));
                    return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(operationStatus));
                }
                artifactData = createNodeResult.left().value();

                // add heat parameters
                if (artifactInfo.getHeatParameters() != null && !artifactInfo.getHeatParameters().isEmpty() && !artifactInfo.getArtifactType().equals(ArtifactTypeEnum.HEAT_ENV.getType())) {
                    StorageOperationStatus addPropertiesStatus = heatParametersOperation.addPropertiesToGraph(artifactInfo.getListHeatParameters(), artifactData.getUniqueId().toString(), NodeTypeEnum.ArtifactRef);
                    if (addPropertiesStatus != StorageOperationStatus.OK) {
                        log.debug("Failed to create heat parameters on graph for artifact {}", artifactInfo.getArtifactName());
                        return Either.right(addPropertiesStatus);
                    }
                }

            } else {
                log.debug("Failed to check existance of artifact in graph for id {}", artifactData.getUniqueId());
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(existArtifact.right().value()));
            }
        } else if (failIfexist) {
            log.debug("Artifact {} already exist", artifactData.getUniqueId());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(
                JanusGraphOperationStatus.ALREADY_EXIST));
        } else {
            artifactData = existArtifact.left().value();
        }

        UniqueIdData parent = new UniqueIdData(type, id);

        // save logical artifact ref name on edge as property
        Map<String, Object> properties = new HashMap<>();
        properties.put(GraphEdgePropertiesDictionary.NAME.getProperty(), artifactInfo.getArtifactLabel());
        if (artifactInfo.getArtifactGroupType() != null)
            properties.put(GraphEdgePropertiesDictionary.GROUP_TYPE.getProperty(), artifactInfo.getArtifactGroupType().getType());
        Either<GraphRelation, JanusGraphOperationStatus> relation = janusGraphGenericDao
            .createRelation(parent, artifactData, GraphEdgeLabels.ARTIFACT_REF, properties);
        if (relation.isRight()) {
            log.debug("Failed to create relation in graph fro id {} to new artifact", id);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(relation.right().value()));
        }

        return Either.left(artifactData);
    }

    private boolean validateParentType(NodeTypeEnum type) {
        boolean isValid = false;
        switch (type) {
        case Resource:
        case InterfaceOperation:
        case Service:
        case ResourceInstance:
            isValid = true;
            break;
        default:
            log.debug("Not supported node type for artifact relation : {} ", type);
        }
        return isValid;
    }


    protected ArtifactDefinition convertArtifactDataToArtifactDefinition(ArtifactData artifactDefResult) {
        log.debug("The object returned after create property is {}" ,artifactDefResult);

        ArtifactDefinition propertyDefResult = new ArtifactDefinition(artifactDefResult.getArtifactDataDefinition());
        List<HeatParameterDefinition> parameters = new ArrayList<>();
        StorageOperationStatus heatParametersOfNode = heatParametersOperation.getHeatParametersOfNode(NodeTypeEnum.ArtifactRef, artifactDefResult.getUniqueId().toString(), parameters);
        if ((heatParametersOfNode.equals(StorageOperationStatus.OK)) && !parameters.isEmpty()) {
            propertyDefResult.setListHeatParameters(parameters);
        }
        return propertyDefResult;
    }

    @Override
    public Either<ArtifactDefinition, StorageOperationStatus> updateArifactOnResource(ArtifactDefinition artifactInfo, String id, String artifactId, NodeTypeEnum type, boolean inTransaction) {
        Either<ArtifactData, StorageOperationStatus> status = updateArtifactOnGraph(artifactInfo, artifactId, type, id);

        if (status.isRight()) {
            if (!inTransaction) {
                janusGraphGenericDao.rollback();
            }
            log.debug("Failed to update artifact {} of {} {}. status is {}", artifactId, type.getName(), id, status.right().value());
            BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("Update Artifact", artifactId, String.valueOf(status.right().value()));
            return Either.right(status.right().value());
        } else {
            if (!inTransaction) {
                janusGraphGenericDao.commit();
            }
            ArtifactData artifactData = status.left().value();

            ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(artifactData);
            log.debug(THE_RETURNED_ARTIFACT_DEFINTION_IS, artifactDefResult);
            return Either.left(artifactDefResult);
        }
    }

    @Override
    public Either<ArtifactDefinition, StorageOperationStatus> removeArifactFromResource(String id, String artifactId, NodeTypeEnum type, boolean deleteMandatoryArtifact, boolean inTransaction) {
        Either<ArtifactData, StorageOperationStatus> status = removeArtifactOnGraph(id, artifactId, type, deleteMandatoryArtifact);

        if (status.isRight()) {
            if (!inTransaction) {
                janusGraphGenericDao.rollback();
            }
            log.debug("Failed to delete artifact {} of resource {}", artifactId, id);

            BeEcompErrorManager.getInstance().logBeFailedDeleteNodeError("Delete Artifact", artifactId, String.valueOf(status.right().value()));
            return Either.right(status.right().value());
        } else {
            if (!inTransaction) {
                janusGraphGenericDao.commit();
            }
            ArtifactData artifactData = status.left().value();

            ArtifactDefinition artifactDefResult = convertArtifactDataToArtifactDefinition(artifactData);
            log.debug(THE_RETURNED_ARTIFACT_DEFINTION_IS , artifactDefResult);
            return Either.left(artifactDefResult);
        }
    }

    @SuppressWarnings("null")
    private Either<ArtifactData, StorageOperationStatus> updateArtifactOnGraph(ArtifactDefinition artifactInfo, String artifactId, NodeTypeEnum type, String id) {

        Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphGenericDao.getGraph();
        if (graph.isRight()) {
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(graph.right().value()));
        }

        JanusGraph tGraph = graph.left().value();

        @SuppressWarnings("unchecked")
        Iterable<JanusGraphVertex> verticesArtifact = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef), artifactId).vertices();
        Iterator<JanusGraphVertex> iterator = verticesArtifact.iterator();
        if (!iterator.hasNext()) {
            log.debug("No artifact node for id = {}", artifactId);
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        JanusGraphVertex artifactV = iterator.next();

        Iterator<Edge> iterEdge = artifactV.edges(Direction.IN, GraphEdgeLabels.ARTIFACT_REF.getProperty());

        int edgeCount = 0;
        Edge edgeFromTo = null;
        while (iterEdge.hasNext()) {
            Edge edge = iterEdge.next();
            Vertex vertexFrom = edge.outVertex();
            String vertexId = vertexFrom.value(UniqueIdBuilder.getKeyByNodeType(type));
            if (id.equals(vertexId)) {
                edgeFromTo = edge;
            }
            ++edgeCount;
        }

        if (isNeedUpdateHeatTime(artifactInfo)) {
            artifactInfo.setHeatParamsUpdateDate(System.currentTimeMillis());
        }

        ArtifactData artifactData = new ArtifactData(artifactInfo);
        if (edgeFromTo == null) {
            log.debug("No relation between artifact  = {} and node with id = {}", artifactId, id);
            return Either.right(StorageOperationStatus.GENERAL_ERROR);
        }

        Either<Boolean, StorageOperationStatus> setRelevantHeatParamIdRes = null;
        if (edgeCount > 1) {
            // need to remove relation, create new node
            log.debug("artifactRef have more connection. Need to clone node");
            log.debug("remove edge {}", edgeFromTo);
            edgeFromTo.remove();
            // update resource id in new artifact node
            String uniqueId = UniqueIdBuilder.buildPropertyUniqueId(id, artifactInfo.getArtifactLabel());
            artifactInfo.setUniqueId(uniqueId);
            // update UUID and artifact version
            String oldChecksum = artifactV.valueOrNull(
                janusGraphGenericDao.getGraph().left().value().getPropertyKey(GraphPropertiesDictionary.ARTIFACT_CHECKSUM.getProperty()));
            String oldVersion = artifactV.valueOrNull(
                janusGraphGenericDao.getGraph().left().value().getPropertyKey(GraphPropertiesDictionary.ARTIFACT_VERSION.getProperty()));
            updateUUID(artifactInfo, oldChecksum, oldVersion);
            log.debug("try to create new artifact ref node for id {}", uniqueId);
            Either<ArtifactData, StorageOperationStatus> addedArtifactRes = addArtifactToGraph(artifactInfo, id, type, true);

            if (addedArtifactRes.isLeft()) {
                // remove all relations between groups to the old artifact
                // add relation between the same groups to the new artifact
                StorageOperationStatus reassociateGroupsFromArtifact = groupOperation.dissociateAndAssociateGroupsFromArtifact(id, type, artifactId, addedArtifactRes.left().value(), true);
                if (reassociateGroupsFromArtifact != StorageOperationStatus.OK) {
                    BeEcompErrorManager.getInstance().logInternalFlowError("UpdateArtifact", "Failed to reassociate groups to the new artifact", ErrorSeverity.ERROR);
                    return Either.right(reassociateGroupsFromArtifact);
                }

                StorageOperationStatus reassociateGroupInstancesFromArtifact = groupInstanceOperation.dissociateAndAssociateGroupsInstanceFromArtifact(id, type, artifactId, addedArtifactRes.left().value());
                if (reassociateGroupInstancesFromArtifact != StorageOperationStatus.OK) {
                    BeEcompErrorManager.getInstance().logInternalFlowError("UpdateArtifact", "Failed to reassociate group instances to the new artifact", ErrorSeverity.ERROR);
                    return Either.right(reassociateGroupsFromArtifact);
                }

                // If artifact is heat env
                if (artifactInfo.getArtifactType().equals(ArtifactTypeEnum.HEAT_ENV.getType())) {
                    ArtifactData addedArtifact = addedArtifactRes.left().value();
                    String newArtifactUniqueId = addedArtifact.getUniqueId();
                    Either<HeatParameterValueData, StorageOperationStatus> updateResult = null;

                    setRelevantHeatParamIdRes = setRelevantHeatParamId(artifactV, artifactInfo);
                    if (setRelevantHeatParamIdRes.isRight()) {
                        log.error("Failed to set relevant id to heat parameters for heat env artifact {}. Status is {}", artifactInfo.getUniqueId(), setRelevantHeatParamIdRes.right().value());
                        return Either.right(setRelevantHeatParamIdRes.right().value());
                    }
                    for (HeatParameterDefinition heatEnvParam : artifactInfo.getListHeatParameters()) {
                        updateResult = heatParametersOperation.updateHeatParameterValue(heatEnvParam, newArtifactUniqueId, id, artifactInfo.getArtifactLabel());
                        if (updateResult.isRight()) {
                            log.error("Failed to update heat parameter {}. Status is {}", heatEnvParam.getName(), updateResult.right().value());
                            return Either.right(updateResult.right().value());
                        }
                    }

                    Iterator<Edge> iterEdgeGeneratedFrom = artifactV.edges(Direction.OUT, GraphEdgeLabels.GENERATED_FROM.getProperty());

                    if (!iterEdgeGeneratedFrom.hasNext()) {
                        log.error("No heat artifact node for id = {}", artifactId);
                        return Either.right(StorageOperationStatus.NOT_FOUND);
                    }
                    Edge edgeToHeat = iterEdgeGeneratedFrom.next();
                    Vertex vertexIn = edgeToHeat.inVertex();
                    String generatedFromArtifactId = vertexIn.value(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef));
                    UniqueIdData generatedFromArtifactNode = new UniqueIdData(NodeTypeEnum.ArtifactRef, generatedFromArtifactId);
                    Either<GraphRelation, JanusGraphOperationStatus> createRelationToGeneratedFromArtifactRes = janusGraphGenericDao
                        .createRelation(addedArtifact, generatedFromArtifactNode, GraphEdgeLabels.GENERATED_FROM, null);
                    if (createRelationToGeneratedFromArtifactRes.isRight()) {
                        log.error("Failed to create relation from heat_env {} to heat {}", addedArtifact.getUniqueId(), generatedFromArtifactNode);
                        return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(createRelationToGeneratedFromArtifactRes.right().value()));
                    }
                }
            }
            return addedArtifactRes;

        } else {
            if (edgeCount == 1) {
                String oldChecksum = artifactV.valueOrNull(
                    janusGraphGenericDao.getGraph().left().value().getPropertyKey(GraphPropertiesDictionary.ARTIFACT_CHECKSUM.getProperty()));
                String oldVersion = artifactV.valueOrNull(
                    janusGraphGenericDao.getGraph().left().value().getPropertyKey(GraphPropertiesDictionary.ARTIFACT_VERSION.getProperty()));
                updateUUID(artifactInfo, oldChecksum, oldVersion);
                // update exist
                Either<ArtifactData, JanusGraphOperationStatus> updatedArtifact = janusGraphGenericDao
                    .updateNode(artifactData, ArtifactData.class);
                if (updatedArtifact.isRight()) {
                    log.debug("failed to update artifact node for id {}", artifactData.getUniqueId());
                    return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(updatedArtifact.right().value()));
                }

                if (artifactInfo.getArtifactType().equals(ArtifactTypeEnum.HEAT_ENV.getType())) {
                    Either<HeatParameterValueData, StorageOperationStatus> updateResult = null;
                    String artifactUniqueId = artifactInfo.getUniqueId();
                    setRelevantHeatParamIdRes = setRelevantHeatParamId(artifactV, artifactInfo);
                    if (setRelevantHeatParamIdRes.isRight()) {
                        log.error("Failed to set relevant id to heat parameters for heat env artifact {}. Status is {}", artifactInfo.getUniqueId(), setRelevantHeatParamIdRes.right().value());
                        return Either.right(setRelevantHeatParamIdRes.right().value());
                    }
                    for (HeatParameterDefinition heatEnvParam : artifactInfo.getListHeatParameters()) {
                        updateResult = heatParametersOperation.updateHeatParameterValue(heatEnvParam, artifactUniqueId, id, artifactInfo.getArtifactLabel());
                        if (updateResult.isRight()) {
                            log.error("Failed to update heat parameter {}. Status is {}", heatEnvParam.getName(), updateResult.right().value());
                            return Either.right(updateResult.right().value());
                        }
                    }
                } else {
                    if (artifactData.getArtifactDataDefinition().getArtifactChecksum() == null) {
                        // update heat parameters only if it is not heat env
                        if (artifactInfo.getGeneratedFromId() == null) {
                            StorageOperationStatus operationStatus = heatParametersOperation.updateHeatParameters(artifactInfo.getListHeatParameters());
                            if (operationStatus != StorageOperationStatus.OK) {
                                return Either.right(operationStatus);
                            }
                        }
                    } else {
                        Either<List<HeatParameterDefinition>, StorageOperationStatus> deleteParameters = heatParametersOperation.deleteAllHeatParametersAssociatedToNode(NodeTypeEnum.ArtifactRef, artifactInfo.getUniqueId());
                        if (deleteParameters.isRight()) {
                            log.debug("failed to update heat parameters for artifact id {}", artifactData.getUniqueId());
                            return Either.right(StorageOperationStatus.GENERAL_ERROR);
                        }

                        StorageOperationStatus addParameters = heatParametersOperation.addPropertiesToGraph(artifactInfo.getListHeatParameters(), artifactId, NodeTypeEnum.ArtifactRef);
                        if (!addParameters.equals(StorageOperationStatus.OK)) {
                            log.debug("failed to update heat parameters for artifact id {}", artifactData.getUniqueId());
                            return Either.right(StorageOperationStatus.GENERAL_ERROR);
                        }

                    }
                }

                return Either.left(updatedArtifact.left().value());
            } else {
                log.debug("No relevent edges for artifact = {}", artifactId);
                return Either.right(StorageOperationStatus.GENERAL_ERROR);
            }
        }
    }

    private boolean isNeedUpdateHeatTime(ArtifactDefinition artifactInfo) {
        if (artifactInfo.getArtifactType().equals(ArtifactTypeEnum.HEAT.getType()) || artifactInfo.getArtifactType().equals(ArtifactTypeEnum.HEAT_NET.getType()) || artifactInfo.getArtifactType().equals(ArtifactTypeEnum.HEAT_VOL.getType())) {
            return true;
        }
        return false;
    }

    private Either<Boolean, StorageOperationStatus> setRelevantHeatParamId(JanusGraphVertex artifactV, ArtifactDefinition artifactInfo) {

        Map<String, String> heatParametersHM = new HashMap<>();

        Iterator<Edge> iterHeat = artifactV.edges(Direction.OUT, GraphEdgeLabels.GENERATED_FROM.getProperty());
        if (!iterHeat.hasNext()) {
            log.debug("No edges with label GENERATED_FROM for the node {}" , artifactInfo.getUniqueId());
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        Edge heat = iterHeat.next();
        Vertex heatVertex = heat.inVertex();
        String heatUniqueId = (String) heatVertex.value(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef));

        Either<List<ImmutablePair<HeatParameterData, GraphEdge>>, JanusGraphOperationStatus> getHeatParametersRes = janusGraphGenericDao
            .getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), heatUniqueId, GraphEdgeLabels.HEAT_PARAMETER,
                NodeTypeEnum.HeatParameter, HeatParameterData.class);
        if (getHeatParametersRes.isRight()) {
            log.debug("No heat parameters for heat artifact {}", heatUniqueId);
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        List<ImmutablePair<HeatParameterData, GraphEdge>> heatParameters = getHeatParametersRes.left().value();
        if (heatParameters == null) {
            log.debug("No heat parameters for heat artifact {}", heatUniqueId);
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        for (ImmutablePair<HeatParameterData, GraphEdge> heatParamEdge : heatParameters) {
            HeatParameterData heatParam = heatParamEdge.getLeft();
            heatParametersHM.put(heatParam.getName(), (String) heatParam.getUniqueId());
        }
        String curName = null;
        for (HeatParameterDefinition heatEnvParam : artifactInfo.getListHeatParameters()) {
            curName = heatEnvParam.getName();
            if (heatParametersHM.containsKey(curName)) {
                heatEnvParam.setUniqueId(heatParametersHM.get(curName));
            }
        }
        return Either.left(true);
    }

    private Either<ArtifactData, StorageOperationStatus> removeArtifactOnGraph(String id, String artifactId, NodeTypeEnum type, boolean deleteMandatoryArtifact) {
        Either<JanusGraph, StorageOperationStatus> graph = janusGraphGenericDao.getGraph()
                .right()
                .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
        if (graph.isRight()) {
            return Either.right(graph.right().value());
        }

        JanusGraph tGraph = graph.left().value();
        Either<ArtifactData, StorageOperationStatus> artifactData = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef), artifactId, ArtifactData.class)
                .right()
                .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
        if (artifactData.isRight()) {
            log.debug("Failed to retrieve  artifact for id = {}", artifactId);
            return Either.right(artifactData.right().value());
        }
        ArtifactDataDefinition artifactDefinition = artifactData.left().value().getArtifactDataDefinition();
        boolean isMandatory = false;
        if ((artifactDefinition.getMandatory() || artifactDefinition.getServiceApi()) && !deleteMandatoryArtifact) {
            isMandatory = true;
        }

        @SuppressWarnings("unchecked")
        Iterable<JanusGraphVertex> verticesArtifact = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef), artifactId).vertices();
        Iterator<JanusGraphVertex> iterator = verticesArtifact.iterator();
        if (!iterator.hasNext()) {
            log.debug("No artifact node for id = {}", artifactId);
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        Vertex artifactV = iterator.next();
        Iterator<Edge> iterEdge = artifactV.edges(Direction.IN, GraphEdgeLabels.ARTIFACT_REF.getProperty());
        int edgeCount = 0;
        Edge edgeFromTo = null;
        while (iterEdge.hasNext()) {
            Edge edge = iterEdge.next();
            Vertex vertexFrom = edge.outVertex();
            String vertexId = vertexFrom.value(UniqueIdBuilder.getKeyByNodeType(type));
            if (id.equals(vertexId)) {
                edgeFromTo = edge;
            }
            ++edgeCount;
        }
        if (edgeFromTo == null) {
            log.debug("No relation between artifact  = {} and node with id = {}", artifactId, id);
            return Either.right(StorageOperationStatus.GENERAL_ERROR);
        }

        // need to remove relation from resource/interface

        log.debug("remove edge {}", edgeFromTo);
        if (!isMandatory || (isMandatory && edgeCount > 1)) {
            edgeFromTo.remove();
        }

        // delete edges from all groups under the component id which related to
        // this artifact.
        // Also in case it is a mandatory artifact.
        Either<List<GraphRelation>, StorageOperationStatus> dissociateAllGroups = groupOperation.dissociateAllGroupsFromArtifactOnGraph(id, type, artifactId);
        if (dissociateAllGroups.isRight()) {
            StorageOperationStatus status = dissociateAllGroups.right().value();
            if (status != StorageOperationStatus.NOT_FOUND && status != StorageOperationStatus.OK) {
                return Either.right(status);
            }
        }

        if (edgeCount == 1) {
            // remove artifactRef node
            log.debug("Remove artifactRef node from graph");
            Either<List<HeatParameterDefinition>, StorageOperationStatus> deleteStatus = heatParametersOperation.deleteAllHeatParametersAssociatedToNode(NodeTypeEnum.ArtifactRef, artifactId);
            if (deleteStatus.isRight()) {
                log.error("failed to delete heat parameters of artifact {}", artifactId);
                return Either.right(StorageOperationStatus.GENERAL_ERROR);
            }

            StorageOperationStatus deleteValuesStatus = heatParametersOperation.deleteAllHeatValuesAssociatedToNode(NodeTypeEnum.ArtifactRef, artifactId);
            if (!deleteValuesStatus.equals(StorageOperationStatus.OK)) {
                log.error("failed to delete heat values of artifact {}", artifactId);
                return Either.right(StorageOperationStatus.GENERAL_ERROR);
            }
            if (!isMandatory) {
                artifactV.remove();
            }
        } else {
            log.debug("artifactRef have more connection. ArtifactRef node will not be removed ");
        }

        return Either.left(artifactData.left().value());

    }

    /**
     *
     * @param parentId
     * @param parentType
     * @param inTransaction
     * @return
     */
    public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifacts(String parentId, NodeTypeEnum parentType, boolean inTransaction) {
        Either<Map<String, ArtifactDefinition>, StorageOperationStatus> result = null;
        try {
            Either<JanusGraph, JanusGraphOperationStatus> graph = janusGraphGenericDao.getGraph();
            if (graph.isRight()) {
                log.debug("Failed to work with graph {}", graph.right().value());
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(graph.right().value()));
            }
            JanusGraph tGraph = graph.left().value();
            @SuppressWarnings("unchecked")
            Iterable<JanusGraphVertex> vertices = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(parentType), parentId).vertices();
            if (vertices == null) {
                log.debug("No nodes for type {}  for id = {}", parentType, parentId);
                result = Either.right(StorageOperationStatus.NOT_FOUND);
                return result;
            }
            Iterator<JanusGraphVertex> iterator = vertices.iterator();

            Map<String, ArtifactDefinition> artifactMap = new HashMap<>();
            while (iterator.hasNext()) {
                Vertex vertex = iterator.next();
                Iterator<Edge> iteratorEdge = vertex.edges(Direction.OUT, GraphEdgeLabels.ARTIFACT_REF.getProperty());

                if (iteratorEdge != null) {

                    while (iteratorEdge.hasNext()) {
                        Edge edge = iteratorEdge.next();

                        Vertex artifactV = edge.inVertex();

                        Map<String, Object> properties = this.janusGraphGenericDao.getProperties(artifactV);
                        ArtifactData artifact = GraphElementFactory.createElement(NodeTypeEnum.ArtifactRef.getName(), GraphElementTypeEnum.Node, properties, ArtifactData.class);
                        if (artifact != null) {

                            ArtifactDefinition artifactDefinition = new ArtifactDefinition(artifact.getArtifactDataDefinition());
                            Iterator<Edge> edgesGeneratedFrom = artifactV.edges(Direction.OUT, GraphEdgeLabels.GENERATED_FROM.getProperty());
                            if (edgesGeneratedFrom != null && edgesGeneratedFrom.hasNext()) {
                                JanusGraphVertex inVertex = (JanusGraphVertex) edgesGeneratedFrom.next().inVertex();
                                String artifactIdGeneratedFrom = (String) janusGraphGenericDao
                                    .getProperty(inVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
                                artifactDefinition.setGeneratedFromId(artifactIdGeneratedFrom);
                            }
                            List<HeatParameterDefinition> heatParams = new ArrayList<>();
                            StorageOperationStatus heatParametersStatus = heatParametersOperation.getHeatParametersOfNode(NodeTypeEnum.ArtifactRef, artifactDefinition.getUniqueId(), heatParams);
                            if (!heatParametersStatus.equals(StorageOperationStatus.OK)) {
                                log.debug("failed to get heat parameters for node {}  {}", parentType.getName(), parentId);
                                return Either.right(heatParametersStatus);
                            }
                            if (!heatParams.isEmpty()) {
                                artifactDefinition.setListHeatParameters(heatParams);
                            }
                            artifactMap.put(artifactDefinition.getArtifactLabel(), artifactDefinition);
                            log.debug("Artifact was added to list {}", artifact.getUniqueId());
                        }
                    }
                }
            }
            result = Either.left(artifactMap);
            return result;
        } finally {
            if (!inTransaction) {
                if (result == null || result.isRight()) {
                    this.janusGraphGenericDao.rollback();
                } else {
                    this.janusGraphGenericDao.commit();
                }

            }
        }

    }

    private void updateUUID(ArtifactDataDefinition artifactData, String oldChecksum, String oldVesrion) {
        if (oldVesrion == null || oldVesrion.isEmpty())
            oldVesrion = "0";

        String currentChecksum = artifactData.getArtifactChecksum();
        if (oldChecksum == null || oldChecksum.isEmpty()) {
            if (currentChecksum != null) {
                generateUUID(artifactData, oldVesrion);
            }
        } else if ((currentChecksum != null && !currentChecksum.isEmpty()) && !oldChecksum.equals(currentChecksum)) {
            generateUUID(artifactData, oldVesrion);
        }

    }

    private void generateUUID(ArtifactDataDefinition artifactData, String oldVesrion) {

        UUID uuid = UUID.randomUUID();
        artifactData.setArtifactUUID(uuid.toString());
        MDC.put("serviceInstanceID", uuid.toString());
        updateVersionAndDate(artifactData, oldVesrion);
    }

    private void updateVersionAndDate(ArtifactDataDefinition artifactData, String oldVesrion) {
        long time = System.currentTimeMillis();
        artifactData.setPayloadUpdateDate(time);
        int newVersion = new Integer(oldVesrion).intValue();
        newVersion++;
        artifactData.setArtifactVersion(String.valueOf(newVersion));
    }

}
