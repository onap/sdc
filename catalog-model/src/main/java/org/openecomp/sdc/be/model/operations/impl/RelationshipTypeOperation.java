/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.DerivedFromOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ModelData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.RelationshipTypeData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import fj.data.Either;

@Component("relationship-type-operation")
public class RelationshipTypeOperation extends AbstractOperation {

    private static final Logger logger = Logger.getLogger(RelationshipTypeOperation.class.getName());
    private static final String RELATIONSHIP_TYPE_CANNOT_BE_FOUND_IN_GRAPH_STATUS_IS =
        "Relationship type {} cannot be " + "found in " + "graph status is {}";
    private static final String FAILED_TO_FETCH_PROPERTIES_OF_RELATIONSHIP_TYPE = "Failed to fetch properties of " + "relationship type {}";
    @Autowired
    private PropertyOperation propertyOperation;
    @Autowired
    private DerivedFromOperation derivedFromOperation;

    public Either<RelationshipTypeDefinition, JanusGraphOperationStatus> getRelationshipTypeByUid(String uniqueId) {
        Either<RelationshipTypeDefinition, JanusGraphOperationStatus> result;
        Either<RelationshipTypeData, JanusGraphOperationStatus> relationshipTypesRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.RelationshipType), uniqueId, RelationshipTypeData.class);
        if (relationshipTypesRes.isRight()) {
            JanusGraphOperationStatus status = relationshipTypesRes.right().value();
            logger.debug("Relationship type {} cannot be found in graph. status is {}", uniqueId, status);
            return Either.right(status);
        }
        return getRelationshipTypeDefinition(relationshipTypesRes.left().value());
    }
    
    private Either<RelationshipTypeDefinition, JanusGraphOperationStatus> getRelationshipTypeDefinition(final RelationshipTypeData relationshipTypeData) {
        RelationshipTypeDefinition relationshipTypeDefinition = new RelationshipTypeDefinition(
            relationshipTypeData.getRelationshipTypeDataDefinition());
        Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> propertiesStatus = OperationUtils
            .fillProperties(relationshipTypeData.getUniqueId(), propertyOperation, NodeTypeEnum.RelationshipType);
        if (propertiesStatus.isRight() && propertiesStatus.right().value() != JanusGraphOperationStatus.OK) {
            logger.error(BUSINESS_PROCESS_ERROR, "Failed to fetch properties of relationship type {}", relationshipTypeData.getUniqueId());
            return Either.right(propertiesStatus.right().value());
        }
        if (propertiesStatus.isLeft()) {
            relationshipTypeDefinition.setProperties(propertiesStatus.left().value());
        }
        Either<ImmutablePair<RelationshipTypeData, GraphEdge>, JanusGraphOperationStatus> parentNode = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.RelationshipType), relationshipTypeData.getUniqueId(), GraphEdgeLabels.DERIVED_FROM,
                NodeTypeEnum.RelationshipType, RelationshipTypeData.class);
        logger.debug("After retrieving DERIVED_FROM node of {}. status is {}", relationshipTypeData.getUniqueId(), parentNode);
        if (parentNode.isRight()) {
            JanusGraphOperationStatus janusGraphOperationStatus = parentNode.right().value();
            if (janusGraphOperationStatus != JanusGraphOperationStatus.NOT_FOUND) {
                logger.error(BUSINESS_PROCESS_ERROR, "Failed to find the parent relationship of relationship type {}. status is {}", relationshipTypeData.getUniqueId(), janusGraphOperationStatus);
                return Either.right(janusGraphOperationStatus);
            }
        } else {
            // derived from node was found
            ImmutablePair<RelationshipTypeData, GraphEdge> immutablePair = parentNode.left().value();
            RelationshipTypeData parentCT = immutablePair.getKey();
            relationshipTypeDefinition.setDerivedFrom(parentCT.getRelationshipTypeDataDefinition().getType());
        }
        
        final Either<ImmutablePair<ModelData, GraphEdge>, JanusGraphOperationStatus> model = janusGraphGenericDao.getParentNode(
            UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.RelationshipType), relationshipTypeData.getUniqueId(), GraphEdgeLabels.MODEL_ELEMENT, 
            NodeTypeEnum.Model, ModelData.class);
        if (model.isLeft()) {
            relationshipTypeDefinition.setModel(model.left().value().getLeft().getName());
        }
        return Either.left(relationshipTypeDefinition);
    }

    private Either<RelationshipTypeDefinition, StorageOperationStatus> validateUpdateProperties(
        RelationshipTypeDefinition relationshipTypeDefinition) {
        JanusGraphOperationStatus error = null;
        if (MapUtils.isNotEmpty(relationshipTypeDefinition.getProperties()) && relationshipTypeDefinition.getDerivedFrom() != null) {
            final Either<RelationshipTypeData, JanusGraphOperationStatus> derivedFromNode = janusGraphGenericDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), 
                relationshipTypeDefinition.getDerivedFrom(), RelationshipTypeData.class, relationshipTypeDefinition.getModel());
            if (derivedFromNode.isRight()) {
                logger.error(BUSINESS_PROCESS_ERROR, "Failed to find the derived from type for  {}. status is {}", relationshipTypeDefinition.getUniqueId(), derivedFromNode.right().value());
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(derivedFromNode.right().value()));
            }
            Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> allPropertiesRes = getAllRelationshipTypePropertiesFromAllDerivedFrom(
                derivedFromNode.left().value().getUniqueId());
            if (allPropertiesRes.isRight() && !JanusGraphOperationStatus.NOT_FOUND.equals(allPropertiesRes.right().value())) {
                error = allPropertiesRes.right().value();
                logger.debug("Couldn't fetch derived from property nodes for relationship type {}, error: {}", relationshipTypeDefinition.getType(),
                    error);
            }
            error = getJanusGraphOperationStatus(relationshipTypeDefinition, error, allPropertiesRes);
        }
        if (error == null) {
            return Either.left(relationshipTypeDefinition);
        }
        return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(error));
    }

    private JanusGraphOperationStatus getJanusGraphOperationStatus(RelationshipTypeDefinition relationshipTypeDefinition,
                                                                   JanusGraphOperationStatus error,
                                                                   Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> allPropertiesRes) {
        if (error == null && !allPropertiesRes.left().value().isEmpty()) {
            Map<String, PropertyDefinition> derivedFromProperties = allPropertiesRes.left().value();
            relationshipTypeDefinition.getProperties().entrySet().stream()
                .filter(e -> derivedFromProperties.containsKey(e.getKey()) && e.getValue().getType() == null)
                .forEach(e -> e.getValue().setType(derivedFromProperties.get(e.getKey()).getType()));
            List<PropertyDefinition> properties = new ArrayList<>(relationshipTypeDefinition.getProperties().values());
            Either<List<PropertyDefinition>, JanusGraphOperationStatus> validatePropertiesRes = propertyOperation
                .validatePropertiesUniqueness(allPropertiesRes.left().value(), properties);
            if (validatePropertiesRes.isRight()) {
                error = validatePropertiesRes.right().value();
            }
        }
        return error;
    }

    private Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> getAllRelationshipTypePropertiesFromAllDerivedFrom(
        String firstParentUid) {
        return propertyOperation.getAllTypePropertiesFromAllDerivedFrom(firstParentUid, NodeTypeEnum.RelationshipType, RelationshipTypeData.class);
    }

    public Either<RelationshipTypeDefinition, StorageOperationStatus> addRelationshipType(RelationshipTypeDefinition relationshipTypeDefinition,
                                                                                          boolean inTransaction) {
        Either<RelationshipTypeDefinition, StorageOperationStatus> result = null;
        try {
            Either<RelationshipTypeDefinition, StorageOperationStatus> validationRes = validateUpdateProperties(relationshipTypeDefinition);
            if (validationRes.isRight()) {
                logger
                    .error("#addRelationshipType - One or all properties of relationship type {} not valid. status is {}", relationshipTypeDefinition,
                        validationRes.right().value());
                return validationRes;
            }
            Either<RelationshipTypeData, StorageOperationStatus> eitherStatus = addRelationshipTypeToGraph(relationshipTypeDefinition);
            result = eitherStatus.left().map(RelationshipTypeData::getUniqueId).left().bind(uniqueId -> getRelationshipType(uniqueId, inTransaction));
            if (result.isLeft()) {
                logger.debug("#addRelationshipType - The returned RelationshipTypeDefinition is {}", result.left().value());
            }
            return result;
        } finally {
            if (!inTransaction) {
                if (result == null || result.isRight()) {
                    logger.error("#addRelationshipType - Going to execute rollback on graph.");
                    janusGraphGenericDao.rollback();
                } else {
                    logger.debug("#addRelationshipType - Going to execute commit on graph.");
                    janusGraphGenericDao.commit();
                }
            }
        }
    }

    public Either<RelationshipTypeDefinition, StorageOperationStatus> getRelationshipType(String uniqueId, boolean inTransaction) {
        Either<RelationshipTypeDefinition, StorageOperationStatus> result;
        try {
            Either<RelationshipTypeDefinition, JanusGraphOperationStatus> ctResult = this.getRelationshipTypeByUid(uniqueId);
            if (ctResult.isRight()) {
                JanusGraphOperationStatus status = ctResult.right().value();
                if (status != JanusGraphOperationStatus.NOT_FOUND) {
                    logger.error("Failed to retrieve information on relationship type {}. status is {}", uniqueId, status);
                }
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(ctResult.right().value()));
                return result;
            }
            result = Either.left(ctResult.left().value());
            return result;
        } finally {
            if (!inTransaction) {
                logger.debug("Going to execute commit on graph.");
                janusGraphGenericDao.commit();
            }
        }
    }

    private Either<RelationshipTypeData, StorageOperationStatus> addRelationshipTypeToGraph(RelationshipTypeDefinition relationshipTypeDefinition) {
        logger.debug("Got relationship type {}", relationshipTypeDefinition);
        String ctUniqueId = UniqueIdBuilder.buildRelationshipTypeUid(relationshipTypeDefinition.getModel(), relationshipTypeDefinition.getType());
        RelationshipTypeData relationshipTypeData = buildRelationshipTypeData(relationshipTypeDefinition, ctUniqueId);
        logger.debug("Before adding relationship type to graph. relationshipTypeData = {}", relationshipTypeData);
        Either<RelationshipTypeData, JanusGraphOperationStatus> createCTResult = janusGraphGenericDao
            .createNode(relationshipTypeData, RelationshipTypeData.class);
        logger.debug("After adding relationship type to graph. status is = {}", createCTResult);
        if (createCTResult.isRight()) {
            JanusGraphOperationStatus operationStatus = createCTResult.right().value();
            logger.error("Failed to relationship type {} to graph. status is {}", relationshipTypeDefinition.getType(), operationStatus);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(operationStatus));
        }
        RelationshipTypeData resultCTD = createCTResult.left().value();
        Map<String, PropertyDefinition> propertiesMap = relationshipTypeDefinition.getProperties();
        Either<Map<String, PropertyData>, JanusGraphOperationStatus> addPropertiesToRelationshipType = propertyOperation
            .addPropertiesToElementType(resultCTD.getUniqueId(), NodeTypeEnum.RelationshipType, propertiesMap);
        if (addPropertiesToRelationshipType.isRight()) {
            logger.error("Failed add properties {} to relationship {}", propertiesMap, relationshipTypeDefinition.getType());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(addPropertiesToRelationshipType.right().value()));
        }
        final Either<GraphRelation, StorageOperationStatus> modelRelationship = addRelationshipToModel(relationshipTypeDefinition);
        if (modelRelationship.isRight()) {
            return Either.right(modelRelationship.right().value());
        }

        return addDerivedFromRelation(relationshipTypeDefinition, ctUniqueId).left().map(updatedDerivedFrom -> createCTResult.left().value());
    }

    private RelationshipTypeData buildRelationshipTypeData(RelationshipTypeDefinition relationshipTypeDefinition, String ctUniqueId) {
        RelationshipTypeData relationshipTypeData = new RelationshipTypeData(relationshipTypeDefinition);
        relationshipTypeData.getRelationshipTypeDataDefinition().setUniqueId(ctUniqueId);
        Long creationDate = relationshipTypeData.getRelationshipTypeDataDefinition().getCreationTime();
        if (creationDate == null) {
            creationDate = System.currentTimeMillis();
        }
        relationshipTypeData.getRelationshipTypeDataDefinition().setCreationTime(creationDate);
        relationshipTypeData.getRelationshipTypeDataDefinition().setModificationTime(creationDate);
        return relationshipTypeData;
    }

    private Either<GraphRelation, StorageOperationStatus> addDerivedFromRelation(RelationshipTypeDefinition relationshipTypeDefinition,
                                                                                 String relationshipTypeUniqueId) {
        String derivedFrom = relationshipTypeDefinition.getDerivedFrom();
        if (derivedFrom == null) {
            return Either.left(null);
        }
        logger.debug("#addDerivedFromRelation - adding derived from relation between relationship type {} to its parent " + "{}",
            relationshipTypeDefinition.getType(), derivedFrom);
        return getRelationshipTypeByTypeAndModel(derivedFrom, relationshipTypeDefinition.getModel()).right().map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus).left().bind(
            derivedFromRelationship -> derivedFromOperation
                .addDerivedFromRelation(relationshipTypeUniqueId, derivedFromRelationship.getUniqueId(), NodeTypeEnum.RelationshipType));
    }
    
    private Either<GraphRelation, StorageOperationStatus> addRelationshipToModel(final RelationshipTypeDefinition relationshipTypeDefinition) {
        final String model = relationshipTypeDefinition.getModel();
        if (model == null) {
            return Either.left(null);
        }
        final GraphNode from = new UniqueIdData(NodeTypeEnum.Model, UniqueIdBuilder.buildModelUid(model));
        final GraphNode to = new UniqueIdData(NodeTypeEnum.RelationshipType, relationshipTypeDefinition.getUniqueId());
        logger.info("Connecting model {} to type {}", from, to);
        return janusGraphGenericDao.createRelation(from , to, GraphEdgeLabels.MODEL_ELEMENT, Collections.emptyMap()).right().map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    private Either<RelationshipTypeDefinition, JanusGraphOperationStatus> getRelationshipTypeByTypeAndModel(final String relationshipType, final String model) {
        final Either<RelationshipTypeData, JanusGraphOperationStatus> relationshipTypesRes = janusGraphGenericDao
            .getNode(GraphPropertiesDictionary.TYPE.getProperty(), relationshipType, RelationshipTypeData.class, model);
        if (relationshipTypesRes.isRight()) {
            final JanusGraphOperationStatus status = relationshipTypesRes.right().value();
            logger.debug("Relationship type {} cannot be found in graph. status is {}", relationshipType, status);
            return Either.right(status);
        }
        return getRelationshipTypeDefinition(relationshipTypesRes.left().value());
    }

    public Either<RelationshipTypeDefinition, StorageOperationStatus> updateRelationshipType(RelationshipTypeDefinition newRelationshipTypeDefinition,
                                                                                             RelationshipTypeDefinition oldRelationshipTypeDefinition,
                                                                                             boolean inTransaction) {
        logger.debug("updating relationship type {}", newRelationshipTypeDefinition.getType());
        Either<RelationshipTypeDefinition, StorageOperationStatus> updateRelationshipEither = null;
        try {
            updateRelationshipEither = updateRelationshipTypeOnGraph(newRelationshipTypeDefinition, oldRelationshipTypeDefinition);
        } finally {
            if (!inTransaction) {
                if (updateRelationshipEither == null || updateRelationshipEither.isRight()) {
                    janusGraphGenericDao.rollback();
                } else {
                    janusGraphGenericDao.commit();
                }
            }
        }
        return updateRelationshipEither;
    }

    private Either<RelationshipTypeDefinition, StorageOperationStatus> updateRelationshipTypeOnGraph(
        RelationshipTypeDefinition newRelationshipTypeDefinition, RelationshipTypeDefinition oldRelationshipTypeDefinition) {
        updateRelationshipTypeData(newRelationshipTypeDefinition, oldRelationshipTypeDefinition);
        return janusGraphGenericDao.updateNode(new RelationshipTypeData(newRelationshipTypeDefinition), RelationshipTypeData.class).right()
            .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus).left().bind(
                updatedNode -> updateRelationshipTypeProperties(newRelationshipTypeDefinition.getUniqueId(),
                    newRelationshipTypeDefinition.getProperties())).left().bind(
                updatedProperties -> updateRelationshipTypeDerivedFrom(newRelationshipTypeDefinition, oldRelationshipTypeDefinition.getDerivedFrom()))
            .left().map(updatedDerivedFrom -> newRelationshipTypeDefinition);
    }

    private Either<Map<String, PropertyData>, StorageOperationStatus> updateRelationshipTypeProperties(String relationshipTypeId,
                                                                                                       Map<String, PropertyDefinition> properties) {
        logger.debug("#updateRelationshipTypeProperties - updating relationship type properties for relationship type with " + "id {}",
            relationshipTypeId);
        return propertyOperation.deletePropertiesAssociatedToNode(NodeTypeEnum.RelationshipType, relationshipTypeId).left()
            .bind(deleteProps -> addPropertiesToRelationshipType(relationshipTypeId, properties));
    }

    private Either<GraphRelation, StorageOperationStatus> updateRelationshipTypeDerivedFrom(RelationshipTypeDefinition newRelationshipTypeDefinition,
                                                                                            String currDerivedFromRelationshipType) {
        String relationshipTypeId = newRelationshipTypeDefinition.getUniqueId();
        logger.debug("#updateRelationshipTypeDerivedFrom - updating relationship derived from relation for relationship "
                + "type with id {}. old derived type {}. new derived type {}", relationshipTypeId, currDerivedFromRelationshipType,
            newRelationshipTypeDefinition.getDerivedFrom());
        StorageOperationStatus deleteDerivedRelationStatus = deleteDerivedFromRelationshipType(relationshipTypeId, newRelationshipTypeDefinition.getModel(), currDerivedFromRelationshipType);
        if (deleteDerivedRelationStatus != StorageOperationStatus.OK) {
            return Either.right(deleteDerivedRelationStatus);
        }
        return addDerivedFromRelation(newRelationshipTypeDefinition, relationshipTypeId);
    }

    private void updateRelationshipTypeData(RelationshipTypeDefinition newRelationshipTypeDefinition,
                                            RelationshipTypeDefinition oldRelationshipTypeDefinition) {
        newRelationshipTypeDefinition.setUniqueId(oldRelationshipTypeDefinition.getUniqueId());
        newRelationshipTypeDefinition.setCreationTime(oldRelationshipTypeDefinition.getCreationTime());
        newRelationshipTypeDefinition.setModificationTime(System.currentTimeMillis());
    }

    private Either<Map<String, PropertyData>, StorageOperationStatus> addPropertiesToRelationshipType(String relationshipTypeId,
                                                                                                      Map<String, PropertyDefinition> properties) {
        logger.debug("#addPropertiesToRelationshipType - adding relationship type properties for relationship type with " + "id {}",
            relationshipTypeId);
        return propertyOperation.addPropertiesToElementType(relationshipTypeId, NodeTypeEnum.RelationshipType, properties).right()
            .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    private StorageOperationStatus deleteDerivedFromRelationshipType(final String relationshipTypeId, final String modelName, final String derivedFromType) {
        if (derivedFromType == null) {
            return StorageOperationStatus.OK;
        }
        logger
            .debug("#deleteDerivedFromRelationshipType - deleting derivedFrom relation for relationship type with id " + "{} and its derived type {}",
                relationshipTypeId, derivedFromType);
        return getRelationshipTypeByTypeAndModel(derivedFromType, modelName).either(derivedFromNode -> derivedFromOperation
                .removeDerivedFromRelation(relationshipTypeId, derivedFromNode.getUniqueId(), NodeTypeEnum.RelationshipType),
            DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    public Either<Map<String, RelationshipTypeDefinition>, JanusGraphOperationStatus> getAllRelationshipTypes(final String model) {
        Map<String, RelationshipTypeDefinition> relationshipTypeDefinitionMap = new HashMap<>();
        Either<Map<String, RelationshipTypeDefinition>, JanusGraphOperationStatus> result = Either.left(relationshipTypeDefinitionMap);
        Either<List<RelationshipTypeData>, JanusGraphOperationStatus> getAllRelationshipTypes = janusGraphGenericDao
            .getByCriteriaForModel(NodeTypeEnum.RelationshipType, null, model, RelationshipTypeData.class);
        if (getAllRelationshipTypes.isRight()) {
            JanusGraphOperationStatus status = getAllRelationshipTypes.right().value();
            if (status != JanusGraphOperationStatus.NOT_FOUND) {
                return Either.right(status);
            } else {
                return result;
            }
        }
        List<RelationshipTypeData> list = getAllRelationshipTypes.left().value();
        if (list != null) {
            logger.trace("Number of relationship types to load is {}", list.size());
            //Set properties
            Either<Map<String, RelationshipTypeDefinition>, JanusGraphOperationStatus> status = getMapJanusGraphOperationStatusEither(
                relationshipTypeDefinitionMap, list);
            if (status != null) {
                return status;
            }
        }
        return result;
    }

    private Either<Map<String, RelationshipTypeDefinition>, JanusGraphOperationStatus> getMapJanusGraphOperationStatusEither(
        Map<String, RelationshipTypeDefinition> relationshipTypeDefinitionMap, List<RelationshipTypeData> list) {
        for (RelationshipTypeData relationshipTypeData : list) {
            logger.trace("Going to fetch relationship type {}. uid is {}", relationshipTypeData.getRelationshipTypeDataDefinition().getType(),
                relationshipTypeData.getUniqueId());
            Either<RelationshipTypeDefinition, JanusGraphOperationStatus> relationshipTypesByUid = getAndAddPropertiesANdDerivedFrom(
                relationshipTypeData.getUniqueId(), relationshipTypeDefinitionMap);
            if (relationshipTypesByUid.isRight()) {
                JanusGraphOperationStatus status = relationshipTypesByUid.right().value();
                if (status == JanusGraphOperationStatus.NOT_FOUND) {
                    status = JanusGraphOperationStatus.INVALID_ID;
                }
                return Either.right(status);
            }
        }
        return null;
    }

    private Either<RelationshipTypeDefinition, JanusGraphOperationStatus> getAndAddPropertiesANdDerivedFrom(String uniqueId,
                                                                                                            Map<String, RelationshipTypeDefinition> relationshipTypeDefinitionMap) {
        if (relationshipTypeDefinitionMap.containsKey(uniqueId)) {
            return Either.left(relationshipTypeDefinitionMap.get(uniqueId));
        }
        Either<RelationshipTypeData, JanusGraphOperationStatus> relationshipTypesRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.RelationshipType), uniqueId, RelationshipTypeData.class);
        if (relationshipTypesRes.isRight()) {
            JanusGraphOperationStatus status = relationshipTypesRes.right().value();
            logger.debug(RELATIONSHIP_TYPE_CANNOT_BE_FOUND_IN_GRAPH_STATUS_IS, uniqueId, status);
            return Either.right(status);
        }
        RelationshipTypeData ctData = relationshipTypesRes.left().value();
        RelationshipTypeDefinition relationshipTypeDefinition = new RelationshipTypeDefinition(ctData.getRelationshipTypeDataDefinition());
        Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> propertiesStatus = OperationUtils
            .fillProperties(uniqueId, propertyOperation, NodeTypeEnum.RelationshipType);
        if (propertiesStatus.isRight() && propertiesStatus.right().value() != JanusGraphOperationStatus.OK) {
            logger.error(FAILED_TO_FETCH_PROPERTIES_OF_RELATIONSHIP_TYPE, uniqueId);
            return Either.right(propertiesStatus.right().value());
        }
        if (propertiesStatus.isLeft()) {
            relationshipTypeDefinition.setProperties(propertiesStatus.left().value());
        }
        fillDerivedFrom(uniqueId, relationshipTypeDefinition);
        relationshipTypeDefinitionMap.put(relationshipTypeDefinition.getType(), relationshipTypeDefinition);
        return Either.left(relationshipTypeDefinition);
    }

    private void fillDerivedFrom(String uniqueId, RelationshipTypeDefinition relationshipType) {
        logger.debug("#fillDerivedFrom - fetching relationship type {} derived node", relationshipType.getType());
        derivedFromOperation.getDerivedFromChild(uniqueId, NodeTypeEnum.RelationshipType, RelationshipTypeData.class).right()
            .bind(this::handleDerivedFromNotExist).left().map(derivedFrom -> setDerivedFrom(relationshipType, derivedFrom));
    }

    private Either<RelationshipTypeData, StorageOperationStatus> handleDerivedFromNotExist(StorageOperationStatus err) {
        if (err == StorageOperationStatus.NOT_FOUND) {
            return Either.left(null);
        }
        return Either.right(err);
    }

    private RelationshipTypeData setDerivedFrom(RelationshipTypeDefinition relationshipTypeDefinition, RelationshipTypeData derivedFrom) {
        if (derivedFrom != null) {
            relationshipTypeDefinition.setDerivedFrom(derivedFrom.getRelationshipTypeDataDefinition().getType());
        }
        return derivedFrom;
    }
}
