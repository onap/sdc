/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.operations.impl;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.ModelOperationExceptionSupplier;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.model.operations.api.DerivedFromOperation;
import org.openecomp.sdc.be.model.operations.api.IArtifactTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ArtifactTypeData;
import org.openecomp.sdc.be.resources.data.ModelData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

@Component("artifact-type-operation")
public class ArtifactTypeOperation implements IArtifactTypeOperation {

    private static final Logger log = Logger.getLogger(ArtifactTypeOperation.class.getName());

    @Resource
    private JanusGraphGenericDao janusGraphGenericDao;
    private final PropertyOperation propertyOperation;
    private final DerivedFromOperation derivedFromOperation;
    private final ModelOperation modelOperation;

    public ArtifactTypeOperation(final PropertyOperation propertyOperation,
                                 final DerivedFromOperation derivedFromOperation,
                                 final ModelOperation modelOperation) {
        this.propertyOperation = propertyOperation;
        this.derivedFromOperation = derivedFromOperation;
        this.modelOperation = modelOperation;
    }

    @Override
    public ArtifactTypeDefinition createArtifactType(final ArtifactTypeDefinition artifactType) {
        return createArtifactType(artifactType, false);
    }

    @Override
    public ArtifactTypeDefinition createArtifactType(final ArtifactTypeDefinition artifactType,
                                                     final boolean inTransaction) {
        Either<ArtifactTypeData, JanusGraphOperationStatus> createNodeResult = null;
        try {
            artifactType.setUniqueId(UniqueIdBuilder.buildArtifactTypeUid(artifactType.getModel(), artifactType.getType()));
            final ArtifactTypeData artifactTypeData = new ArtifactTypeData(artifactType);
            final Either<ArtifactTypeData, JanusGraphOperationStatus> existArtifact = janusGraphGenericDao
                .getNode(artifactTypeData.getUniqueIdKey(), artifactTypeData.getUniqueId(), ArtifactTypeData.class);
            if (!existArtifact.isLeft()) {
                createNodeResult = janusGraphGenericDao.createNode(artifactTypeData, ArtifactTypeData.class);
                if (createNodeResult.isRight()) {
                    final JanusGraphOperationStatus operationStatus = createNodeResult.right().value();
                    log.error(EcompLoggerErrorCode.DATA_ERROR,
                        "Failed to add artifact type {} to graph. Operation status {}", artifactType.getType(), operationStatus);
                    throw new OperationException(ActionStatus.GENERAL_ERROR,
                        String.format("Failed to create artifact type %s with status %s", artifactType.getType(),
                            DaoStatusConverter.convertJanusGraphStatusToStorageStatus(operationStatus)));
                }
                addPropertiesToArtifactType(artifactType);
                addModelRelation(artifactType);
                addDerivedFromRelation(artifactType);
                return convertArtifactDataDefinition(createNodeResult.left().value());
            } else {
                log.debug("Artifact type already exist {}", artifactType);
                return convertArtifactDataDefinition(existArtifact.left().value());
            }
        } finally {
            if (!inTransaction) {
                if (createNodeResult == null || createNodeResult.isRight()) {
                    log.debug("Rollback on graph.");
                    janusGraphGenericDao.rollback();
                } else {
                    janusGraphGenericDao.commit();
                }
            }
        }
    }

    @Override
    public Map<String, ArtifactTypeDefinition> getAllArtifactTypes(final String model) {
        final Either<List<ArtifactTypeData>, JanusGraphOperationStatus> artifactTypes =
            janusGraphGenericDao.getByCriteriaForModel(NodeTypeEnum.ArtifactType, Collections.emptyMap(), model, ArtifactTypeData.class);
        if (artifactTypes.isRight()) {
            if (JanusGraphOperationStatus.NOT_FOUND == artifactTypes.right().value()) {
                return MapUtils.EMPTY_SORTED_MAP;
            }
            throw new OperationException(ActionStatus.GENERAL_ERROR,
                String.format("Failed to find artifact on JanusGraph with status %s",
                    DaoStatusConverter.convertJanusGraphStatusToStorageStatus(artifactTypes.right().value())));
        }
        final Map<String, ArtifactTypeDefinition> artifactTypeDefinitionTypes = new HashMap<>();
        final List<ArtifactTypeDefinition> artifactTypeList = artifactTypes.left().value().stream()
            .map(this::convertArtifactDataDefinition)
            .filter(artifactTypeDefinition -> artifactTypeDefinition.getUniqueId().equalsIgnoreCase(UniqueIdBuilder
                .buildArtifactTypeUid(artifactTypeDefinition.getModel(), artifactTypeDefinition.getType())))
            .collect(Collectors.toList());
        for (final ArtifactTypeDefinition type : artifactTypeList) {
            artifactTypeDefinitionTypes.put(type.getUniqueId(), type);
        }
        return artifactTypeDefinitionTypes;
    }

    private ArtifactTypeDefinition convertArtifactDataDefinition(final ArtifactTypeData artifactTypeData) {
        log.debug("The object returned after create tosca artifact type is {}", artifactTypeData);
        final ArtifactTypeDefinition artifactType = new ArtifactTypeDefinition(artifactTypeData.getArtifactTypeDataDefinition());
        artifactType.setModel(getModelAssociatedToArtifact(artifactTypeData.getUniqueId()));
        artifactType.setType(artifactTypeData.getArtifactTypeDataDefinition().getType());
        final ArtifactTypeData derivedFromNode = fillDerivedFrom(artifactType);
        fillProperties(artifactType, derivedFromNode);
        return artifactType;
    }

    private Optional<ArtifactTypeData> getLatestArtifactTypeByType(final String type, final String model) {
        final Map<String, Object> mapCriteria = new HashMap<>();
        mapCriteria.put(GraphPropertiesDictionary.TYPE.getProperty(), type);
        final Either<List<ArtifactTypeData>, JanusGraphOperationStatus> result = janusGraphGenericDao.getByCriteriaForModel(NodeTypeEnum.ArtifactType,
            mapCriteria, model, ArtifactTypeData.class);
        if (result.isRight()) {
            final JanusGraphOperationStatus status = result.right().value();
            if (JanusGraphOperationStatus.NOT_FOUND == status) {
                return Optional.empty();
            }
            throw new OperationException(ActionStatus.GENERAL_ERROR,
                String.format("Failed to find artifact by type on JanusGraph with status %s",
                    DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status)));
        }
        return Optional.of(result.left().value().get(0));
    }

    private void addDerivedFromRelation(final ArtifactTypeDefinition artifactType) {
        final String derivedFrom = artifactType.getDerivedFrom();
        final String artifactId = artifactType.getUniqueId();
        if (derivedFrom.isEmpty()) {
            return;
        }
        final var getArtifactTypeOptional = getLatestArtifactTypeByType(derivedFrom, artifactType.getModel());
        if (getArtifactTypeOptional.isPresent()) {
            if (derivedFromOperation.addDerivedFromRelation(artifactId, getArtifactTypeOptional.get().getUniqueId(),
                NodeTypeEnum.ArtifactType).isRight()) {
                throw new OperationException(ActionStatus.GENERAL_ERROR,
                    String.format("Failed creating derivedFrom relation for artifact type %s", artifactType.getType()));
            }
        }
    }

    private void addPropertiesToArtifactType(final ArtifactTypeDefinition artifactType) {
        final List<PropertyDefinition> properties = artifactType.getProperties();
        final Either<Map<String, PropertyData>, JanusGraphOperationStatus> addPropertiesToArtifactType =
            propertyOperation.addPropertiesToElementType(artifactType.getUniqueId(), NodeTypeEnum.ArtifactType, properties);
        if (addPropertiesToArtifactType.isRight()) {
            throw new OperationException(ActionStatus.GENERAL_ERROR,
                String.format("Failed creating properties for artifact type %s with status %s",
                    artifactType.getType(), DaoStatusConverter.convertJanusGraphStatusToStorageStatus(addPropertiesToArtifactType.right().value())));
        }
    }

    private void addModelRelation(final ArtifactTypeDefinition artifactType) {
        final String model = artifactType.getModel();
        if (StringUtils.isNotEmpty(model)) {
            final GraphNode from = new UniqueIdData(NodeTypeEnum.Model, UniqueIdBuilder.buildModelUid(model));
            final GraphNode to = new UniqueIdData(NodeTypeEnum.ArtifactType, artifactType.getUniqueId());
            log.info("Connecting model {} to type {}", from, to);
            final Either<GraphRelation, JanusGraphOperationStatus> status = janusGraphGenericDao.createRelation(from,
                to, GraphEdgeLabels.MODEL_ELEMENT, Collections.emptyMap());
            if (status.isRight()) {
                throw new OperationException(ActionStatus.GENERAL_ERROR,
                    String.format("Failed creating relation between model %s and artifact type %s with status %s",
                        model, artifactType.getType(), DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status.right().value())));
            }
        }
    }

    private String getModelAssociatedToArtifact(String uid) {
        final Either<ImmutablePair<ModelData, GraphEdge>, JanusGraphOperationStatus> model =
            janusGraphGenericDao.getParentNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Interface), uid,
                GraphEdgeLabels.MODEL_ELEMENT, NodeTypeEnum.Model, ModelData.class);
        if (model.isLeft()) {
            return model.left().value().getLeft().getName();
        }
        return null;
    }

    private ArtifactTypeData fillDerivedFrom(ArtifactTypeDefinition artifactType) {
        final Either<ArtifactTypeData, StorageOperationStatus> result = derivedFromOperation
            .getDerivedFromChild(artifactType.getUniqueId(), NodeTypeEnum.ArtifactType, ArtifactTypeData.class)
            .right().bind(this::handleDerivedFromNotExist).left()
            .map(derivedFrom -> setDerivedFrom(artifactType, derivedFrom));
        if (result.isRight()) {
            throw new OperationException(ActionStatus.GENERAL_ERROR,
                String.format("Failed fetching derivedFrom for artifact type %s with status %s",
                    artifactType.getType(), result.right().value()));
        }
        return result.left().value();
    }

    private Either<ArtifactTypeData, StorageOperationStatus> handleDerivedFromNotExist(StorageOperationStatus err) {
        if (err == StorageOperationStatus.NOT_FOUND) {
            return Either.left(null);
        }
        return Either.right(err);
    }

    private ArtifactTypeData setDerivedFrom(ArtifactTypeDefinition artifactType, ArtifactTypeData derivedFrom) {
        if (derivedFrom != null) {
            artifactType.setDerivedFrom(derivedFrom.getArtifactTypeDataDefinition().getType());
        }
        return derivedFrom;
    }

    private void fillProperties(ArtifactTypeDefinition artifactType, ArtifactTypeData derivedFromNode) {
        final Either<List<PropertyDefinition>, StorageOperationStatus> result =
            propertyOperation.findPropertiesOfNode(NodeTypeEnum.ArtifactType, artifactType.getUniqueId()).right()
                .bind(this::handleNoProperties).left().bind(propsMap -> fillDerivedFromProperties(artifactType,
                    derivedFromNode, new ArrayList<>(propsMap.values())));
        if (result.isRight()) {
            throw new OperationException(ActionStatus.GENERAL_ERROR,
                String.format("Failed fetching properties for artifact type %s with status %s",
                    artifactType.getType(), result.right().value()));
        }
    }

    private Either<Map<String, PropertyDefinition>, StorageOperationStatus> handleNoProperties(
        JanusGraphOperationStatus err) {
        if (err == JanusGraphOperationStatus.NOT_FOUND) {
            return Either.left(new HashMap<>());
        }
        return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(err));
    }

    private Either<List<PropertyDefinition>, StorageOperationStatus> fillDerivedFromProperties(
        ArtifactTypeDefinition artifactType, ArtifactTypeData derivedFromNode,
        List<PropertyDefinition> artifactTypeProperties) {
        if (derivedFromNode == null) {
            artifactType.setProperties(artifactTypeProperties);
            return Either.left(artifactTypeProperties);
        }
        return propertyOperation
            .getAllPropertiesRec(derivedFromNode.getUniqueId(), NodeTypeEnum.ArtifactType, ArtifactTypeData.class)
            .left().map(derivedFromProps -> {
                artifactTypeProperties.addAll(derivedFromProps);
                return artifactTypeProperties;
            }).left().map(allProps -> {
                artifactType.setProperties(allProps);
                return allProps;
            });
    }

    /**
     * The Model field is an optional entry when uploading a resource. If the field is present, it validates if the Model name exists.
     * @param modelName Model names declared on the resource json representation
     */
    public void validateModel(final String modelName) {
        if (modelOperation.findModelByName(modelName).isEmpty()) {
            log.error(EcompLoggerErrorCode.DATA_ERROR,"Could not find model name {}", modelName);
            throw ModelOperationExceptionSupplier.invalidModel(modelName).get();
        }
    }
}
