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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for handling all operations for the TOSCA artifact types
 */
@Component("artifact-type-operation")
public class ArtifactTypeOperation implements IArtifactTypeOperation {

    private static final Logger LOGGER = Logger.getLogger(ArtifactTypeOperation.class.getName());

    private final JanusGraphGenericDao janusGraphGenericDao;
    private final PropertyOperation propertyOperation;
    private final DerivedFromOperation derivedFromOperation;
    private final ModelOperation modelOperation;

    @Autowired
    public ArtifactTypeOperation(final JanusGraphGenericDao janusGraphGenericDao,
                                 final PropertyOperation propertyOperation,
                                 final DerivedFromOperation derivedFromOperation,
                                 final ModelOperation modelOperation) {
        this.janusGraphGenericDao = janusGraphGenericDao;
        this.propertyOperation = propertyOperation;
        this.derivedFromOperation = derivedFromOperation;
        this.modelOperation = modelOperation;
    }

    /**
     * Creates a TOSCA artifact types
     * @param artifactType the TOSCA artifact types definition to be created
     * @return the created TOSCA artifact types definition
     */
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
                    LOGGER.error(EcompLoggerErrorCode.DATA_ERROR,
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
                LOGGER.debug("Artifact type already exist {}", artifactType);
                return convertArtifactDataDefinition(existArtifact.left().value());
            }
        } finally {
            if (!inTransaction) {
                if (createNodeResult == null || createNodeResult.isRight()) {
                    LOGGER.debug("Rollback on graph.");
                    janusGraphGenericDao.rollback();
                } else {
                    janusGraphGenericDao.commit();
                }
            }
        }
    }

    /**
     * Finds all TOSCA artifact types applicable to the given model name
     * @param model model name
     * @return all the TOSCA artifact types found
     */
    @Override
    public Map<String, ArtifactTypeDefinition> getAllArtifactTypes(final String model) {
        final Either<List<ArtifactTypeData>, JanusGraphOperationStatus> artifactTypes =
            janusGraphGenericDao.getByCriteriaForModel(NodeTypeEnum.ArtifactType, Collections.emptyMap(), model, ArtifactTypeData.class);
        if (artifactTypes.isRight()) {
            if (JanusGraphOperationStatus.NOT_FOUND == artifactTypes.right().value()) {
                return Collections.emptyMap();
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

    /**
     * Coverts the given artifact type data into a TOSCA artifact types definition
     * @param artifactTypeData artifact type data representation
     * @return the TOSCA artifact types definition
     */
    private ArtifactTypeDefinition convertArtifactDataDefinition(final ArtifactTypeData artifactTypeData) {
        LOGGER.debug("The object returned after create tosca artifact type is {}", artifactTypeData);
        final ArtifactTypeDefinition artifactType = new ArtifactTypeDefinition(artifactTypeData.getArtifactTypeDataDefinition());
        final var modelAssociatedToArtifactOptional = getModelAssociatedToArtifact(artifactTypeData.getUniqueId());
        if (!modelAssociatedToArtifactOptional.isEmpty()) {
            artifactType.setModel(modelAssociatedToArtifactOptional.get());
        }
        artifactType.setType(artifactTypeData.getArtifactTypeDataDefinition().getType());
        final ArtifactTypeData derivedFromNode = fillDerivedFrom(artifactType);
        fillProperties(artifactType, derivedFromNode);
        return artifactType;
    }

    /**
     * Finds an artifact type data on JanusGraph based on the given parameters
     * @param type the artifact type derived from
     * @param model the model name
     * @return the optional artifact type data found
     */
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

    /**
     * Creates the derivedFrom relation for the given TOSCA artifact types
     * @param artifactType the TOSCA artifact types definition
     */
    private void addDerivedFromRelation(final ArtifactTypeDefinition artifactType) {
        final String derivedFrom = artifactType.getDerivedFrom();
        final String artifactId = artifactType.getUniqueId();
        if (derivedFrom == null || derivedFrom.isEmpty()) {
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

    /**
     * Adds a property definition to the given TOSCA artifact types definition
     * @param artifactType the TOSCA artifact types
     */
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

    /**
     * Creates an edge between the given TOSCA artifact types and it`s model
     * @param artifactType the TOSCA artifact types
     */
    private void addModelRelation(final ArtifactTypeDefinition artifactType) {
        final String model = artifactType.getModel();
        if (StringUtils.isNotEmpty(model)) {
            final GraphNode from = new UniqueIdData(NodeTypeEnum.Model, UniqueIdBuilder.buildModelUid(model));
            final GraphNode to = new UniqueIdData(NodeTypeEnum.ArtifactType, artifactType.getUniqueId());
            LOGGER.info("Connecting model {} to type {}", from, to);
            final Either<GraphRelation, JanusGraphOperationStatus> status = janusGraphGenericDao.createRelation(from,
                to, GraphEdgeLabels.MODEL_ELEMENT, Collections.emptyMap());
            if (status.isRight()) {
                throw new OperationException(ActionStatus.GENERAL_ERROR,
                    String.format("Failed creating relation between model %s and artifact type %s with status %s",
                        model, artifactType.getType(), DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status.right().value())));
            }
        }
    }

    /**
     * Finds a model associated to the given artifact type unique id
     * @param uid the TOSCA artifact types unique id
     * @return
     */
    private Optional<String> getModelAssociatedToArtifact(final String uid) {
        final Either<ImmutablePair<ModelData, GraphEdge>, JanusGraphOperationStatus> model =
            janusGraphGenericDao.getParentNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Interface), uid,
                GraphEdgeLabels.MODEL_ELEMENT, NodeTypeEnum.Model, ModelData.class);
        if (model.isLeft()) {
            return Optional.ofNullable(model.left().value().getLeft().getName());
        }
        return Optional.empty();
    }

    /**
     * Finds the derived from for teh given TOSCA artifact types
     * @param artifactType
     * @return
     */
    private ArtifactTypeData fillDerivedFrom(final ArtifactTypeDefinition artifactType) {
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

    private Either<ArtifactTypeData, StorageOperationStatus> handleDerivedFromNotExist(final StorageOperationStatus storageOperationStatus) {
        if (storageOperationStatus == StorageOperationStatus.NOT_FOUND) {
            return Either.left(null);
        }
        return Either.right(storageOperationStatus);
    }

    private ArtifactTypeData setDerivedFrom(final ArtifactTypeDefinition artifactType, final ArtifactTypeData derivedFrom) {
        if (derivedFrom != null) {
            artifactType.setDerivedFrom(derivedFrom.getArtifactTypeDataDefinition().getType());
        }
        return derivedFrom;
    }

    /**
     * Finds all properties for the given TOSCA artifact types
     * @param artifactType the TOSCA artifact types
     * @param derivedFromNode the TOSCA artifact types derived from
     */
    private void fillProperties(final ArtifactTypeDefinition artifactType, final ArtifactTypeData derivedFromNode) {
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
        final JanusGraphOperationStatus janusGraphOperationStatus) {
        if (janusGraphOperationStatus == JanusGraphOperationStatus.NOT_FOUND) {
            return Either.left(new HashMap<>());
        }
        return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(janusGraphOperationStatus));
    }

    private Either<List<PropertyDefinition>, StorageOperationStatus> fillDerivedFromProperties(final ArtifactTypeDefinition artifactType,
                                                                                               final ArtifactTypeData derivedFromNode,
                                                                                               final List<PropertyDefinition> artifactTypeProperties)
    {
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
            LOGGER.error(EcompLoggerErrorCode.DATA_ERROR,"Could not find model name {}", modelName);
            throw ModelOperationExceptionSupplier.invalidModel(modelName).get();
        }
    }
}
