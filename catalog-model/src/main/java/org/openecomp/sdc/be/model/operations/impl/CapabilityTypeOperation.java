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

import static org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR;

import fj.data.Either;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.DerivedFromOperation;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.api.TypeOperations;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;
import org.openecomp.sdc.be.resources.data.ModelData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("capability-type-operation")
public class CapabilityTypeOperation extends AbstractOperation implements ICapabilityTypeOperation {

    private static final Logger log = Logger.getLogger(CapabilityTypeOperation.class.getName());
    private static final String DATA_TYPE_CANNOT_BE_FOUND_IN_GRAPH_STATUS_IS = "Data type {} cannot be found in graph." + " status is {}";
    private static final String FAILED_TO_FETCH_PROPERTIES_OF_DATA_TYPE = "Failed to fetch properties of data type {}";
    @Autowired
    private PropertyOperation propertyOperation;
    @Autowired
    private DerivedFromOperation derivedFromOperation;

    public CapabilityTypeOperation() {
        super();
    }

    /**
     * FOR TEST ONLY
     *
     * @param janusGraphGenericDao
     */
    public void setJanusGraphGenericDao(HealingJanusGraphGenericDao janusGraphGenericDao) {
        this.janusGraphGenericDao = janusGraphGenericDao;
    }

    @Override
    public Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType(CapabilityTypeDefinition capabilityTypeDefinition,
                                                                                      boolean inTransaction) {
        Either<CapabilityTypeDefinition, StorageOperationStatus> result = null;
        try {
            Either<CapabilityTypeDefinition, StorageOperationStatus> validationRes = validateUpdateProperties(capabilityTypeDefinition);
            if (validationRes.isRight()) {
                log.error("#addCapabilityType - One or all properties of capability type {} not valid. status is {}", capabilityTypeDefinition,
                    validationRes.right().value());
                return result;
            }
            Either<CapabilityTypeData, StorageOperationStatus> eitherStatus = addCapabilityTypeToGraph(capabilityTypeDefinition);
            result = eitherStatus.left().map(CapabilityTypeData::getUniqueId).left().bind(uniqueId -> getCapabilityType(uniqueId, inTransaction));
            if (result.isLeft()) {
                log.debug("#addCapabilityType - The returned CapabilityTypeDefinition is {}", result.left().value());
            }
            return result;
        } finally {
            if (!inTransaction) {
                if (result == null || result.isRight()) {
                    log.error("#addCapabilityType - Going to execute rollback on graph.");
                    janusGraphGenericDao.rollback();
                } else {
                    log.debug("#addCapabilityType - Going to execute commit on graph.");
                    janusGraphGenericDao.commit();
                }
            }
        }
    }

    public Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> getAllCapabilityTypePropertiesFromAllDerivedFrom(
        final String firstParentUid) {
        return propertyOperation.getAllTypePropertiesFromAllDerivedFrom(firstParentUid, NodeTypeEnum.CapabilityType, CapabilityTypeData.class);
    }

    public Either<CapabilityTypeDefinition, StorageOperationStatus> validateUpdateProperties(CapabilityTypeDefinition capabilityTypeDefinition) {
        JanusGraphOperationStatus error = null;
        if (MapUtils.isNotEmpty(capabilityTypeDefinition.getProperties()) && capabilityTypeDefinition.getDerivedFrom() != null) {
            final Either<CapabilityTypeData, JanusGraphOperationStatus> derivedFromNode = janusGraphGenericDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), 
                capabilityTypeDefinition.getDerivedFrom(), CapabilityTypeData.class, capabilityTypeDefinition.getModel());
            if (derivedFromNode.isRight()) {
                log.error(BUSINESS_PROCESS_ERROR, "Failed to find the derived from type for  {}. status is {}", capabilityTypeDefinition.getType(), derivedFromNode.right().value());
                return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(derivedFromNode.right().value()));
            }
            Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> allPropertiesRes = getAllCapabilityTypePropertiesFromAllDerivedFrom(
                derivedFromNode.left().value().getUniqueId());
            if (allPropertiesRes.isRight() && !allPropertiesRes.right().value().equals(JanusGraphOperationStatus.NOT_FOUND)) {
                error = allPropertiesRes.right().value();
                log.debug("Couldn't fetch derived from property nodes for capability type {}, error: {}", capabilityTypeDefinition.getType(), error);
            }
            if (error == null && !allPropertiesRes.left().value().isEmpty()) {
                Map<String, PropertyDefinition> derivedFromProperties = allPropertiesRes.left().value();
                capabilityTypeDefinition.getProperties().entrySet().stream()
                    .filter(e -> derivedFromProperties.containsKey(e.getKey()) && e.getValue().getType() == null)
                    .forEach(e -> e.getValue().setType(derivedFromProperties.get(e.getKey()).getType()));
                List<PropertyDefinition> properties = capabilityTypeDefinition.getProperties().values().stream().collect(Collectors.toList());
                Either<List<PropertyDefinition>, JanusGraphOperationStatus> validatePropertiesRes = propertyOperation
                    .validatePropertiesUniqueness(allPropertiesRes.left().value(), properties);
                if (validatePropertiesRes.isRight()) {
                    error = validatePropertiesRes.right().value();
                }
            }
        }
        if (error == null) {
            return Either.left(capabilityTypeDefinition);
        }
        return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(error));
    }

    /**
     * Add capability type to graph.
     * <p>
     * 1. Add capability type node
     * <p>
     * 2. Add edge between the former node to its parent(if exists)
     * <p>
     * 3. Add property node and associate it to the node created at #1. (per property & if exists)
     *
     * @param capabilityTypeDefinition
     * @return
     */
    private Either<CapabilityTypeData, StorageOperationStatus> addCapabilityTypeToGraph(CapabilityTypeDefinition capabilityTypeDefinition) {
        log.debug("Got capability type {}", capabilityTypeDefinition);
        String ctUniqueId = UniqueIdBuilder.buildCapabilityTypeUid(capabilityTypeDefinition.getModel(), capabilityTypeDefinition.getType());
        CapabilityTypeData capabilityTypeData = buildCapabilityTypeData(capabilityTypeDefinition, ctUniqueId);
        log.debug("Before adding capability type to graph. capabilityTypeData = {}", capabilityTypeData);
        Either<CapabilityTypeData, JanusGraphOperationStatus> createCTResult = janusGraphGenericDao
            .createNode(capabilityTypeData, CapabilityTypeData.class);
        log.debug("After adding capability type to graph. status is = {}", createCTResult);
        if (createCTResult.isRight()) {
            JanusGraphOperationStatus operationStatus = createCTResult.right().value();
            log.error("Failed to capability type {} to graph. status is {}", capabilityTypeDefinition.getType(), operationStatus);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(operationStatus));
        }
        CapabilityTypeData resultCTD = createCTResult.left().value();
        Map<String, PropertyDefinition> propertiesMap = capabilityTypeDefinition.getProperties();
        Either<Map<String, PropertyData>, JanusGraphOperationStatus> addPropertiesToCapablityType = propertyOperation
            .addPropertiesToElementType(resultCTD.getUniqueId(), NodeTypeEnum.CapabilityType, propertiesMap);
        if (addPropertiesToCapablityType.isRight()) {
            log.error("Failed add properties {} to capability {}", propertiesMap, capabilityTypeDefinition.getType());
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(addPropertiesToCapablityType.right().value()));
        }
        
        final Either<GraphRelation, StorageOperationStatus> modelRelationship = addCapabilityTypeToModel(capabilityTypeDefinition);
        if (modelRelationship.isRight()) {
            return Either.right(modelRelationship.right().value());
        } 
        
        return addDerivedFromRelation(capabilityTypeDefinition, ctUniqueId).left().map(updatedDerivedFrom -> createCTResult.left().value());
    }
    
    private Either<GraphRelation, StorageOperationStatus> addCapabilityTypeToModel(final CapabilityTypeDefinition capabilityTypeDefinition) {
        final String model = capabilityTypeDefinition.getModel();
        if (model == null) {
            return Either.left(null);
        }
        final GraphNode from = new UniqueIdData(NodeTypeEnum.Model, UniqueIdBuilder.buildModelUid(model));
        final GraphNode to = new UniqueIdData(NodeTypeEnum.CapabilityType, capabilityTypeDefinition.getUniqueId());
        log.info("Connecting model {} to type {}", from, to);
        return janusGraphGenericDao.createRelation(from , to, GraphEdgeLabels.MODEL_ELEMENT, Collections.emptyMap()).right().map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    private CapabilityTypeData buildCapabilityTypeData(CapabilityTypeDefinition capabilityTypeDefinition, String ctUniqueId) {
        CapabilityTypeData capabilityTypeData = new CapabilityTypeData(capabilityTypeDefinition);
        capabilityTypeData.getCapabilityTypeDataDefinition().setUniqueId(ctUniqueId);
        Long creationDate = capabilityTypeData.getCapabilityTypeDataDefinition().getCreationTime();
        if (creationDate == null) {
            creationDate = System.currentTimeMillis();
        }
        capabilityTypeData.getCapabilityTypeDataDefinition().setCreationTime(creationDate);
        capabilityTypeData.getCapabilityTypeDataDefinition().setModificationTime(creationDate);
        return capabilityTypeData;
    }

    @Override
    public Either<CapabilityTypeDefinition, StorageOperationStatus> getCapabilityType(String uniqueId, boolean inTransaction) {
        Either<CapabilityTypeDefinition, StorageOperationStatus> result = null;
        try {
            Either<CapabilityTypeDefinition, JanusGraphOperationStatus> ctResult = this.getCapabilityTypeByUid(uniqueId);
            if (ctResult.isRight()) {
                JanusGraphOperationStatus status = ctResult.right().value();
                if (status != JanusGraphOperationStatus.NOT_FOUND) {
                    log.error("Failed to retrieve information on capability type {}. status is {}", uniqueId, status);
                }
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(ctResult.right().value()));
                return result;
            }
            result = Either.left(ctResult.left().value());
            return result;
        } finally {
            if (!inTransaction) {
                log.debug("Going to execute commit on graph.");
                janusGraphGenericDao.commit();
            }
        }
    }

    public Either<CapabilityTypeDefinition, JanusGraphOperationStatus> getCapabilityTypeByType(String capabilityType) {
        // Optimization: In case of Capability Type its unique ID is the same as type
        return getCapabilityTypeByUid(capabilityType);
    }
    
    public Either<CapabilityTypeDefinition, JanusGraphOperationStatus> getCapabilityTypeByType(final String capabilityType, final String model) {
        final Either<CapabilityTypeData, JanusGraphOperationStatus> capabilityTypesRes = janusGraphGenericDao
            .getNode(GraphPropertiesDictionary.TYPE.getProperty(), capabilityType, CapabilityTypeData.class, model);
        if (capabilityTypesRes.isRight()) {
            final JanusGraphOperationStatus status = capabilityTypesRes.right().value();
            log.debug("Capability type {} cannot be found in graph. status is {}", capabilityType, status);
            return Either.right(status);
        }
        return getCapabilityTypeDefinition(capabilityTypesRes.left().value());
    }

    /**
     * Build Capability type object from graph by unique id
     *
     * @param uniqueId
     * @return
     */
    public Either<CapabilityTypeDefinition, JanusGraphOperationStatus> getCapabilityTypeByUid(String uniqueId) {
        Either<CapabilityTypeDefinition, JanusGraphOperationStatus> result = null;
        Either<CapabilityTypeData, JanusGraphOperationStatus> capabilityTypesRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), uniqueId, CapabilityTypeData.class);
        if (capabilityTypesRes.isRight()) {
            JanusGraphOperationStatus status = capabilityTypesRes.right().value();
            log.debug("Capability type {} cannot be found in graph. status is {}", uniqueId, status);
            return Either.right(status);
        }
        return getCapabilityTypeDefinition(capabilityTypesRes.left().value());
    }
    
    private Either<CapabilityTypeDefinition, JanusGraphOperationStatus> getCapabilityTypeDefinition(final CapabilityTypeData ctData) {
        CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition(ctData.getCapabilityTypeDataDefinition());
        Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> propertiesStatus = OperationUtils
            .fillProperties(ctData.getUniqueId(), propertyOperation, NodeTypeEnum.CapabilityType);
        if (propertiesStatus.isRight() && propertiesStatus.right().value() != JanusGraphOperationStatus.OK) {
            log.error(BUSINESS_PROCESS_ERROR, "Failed to fetch properties of capability type {}", ctData.getUniqueId());
            return Either.right(propertiesStatus.right().value());
        }
        if (propertiesStatus.isLeft()) {
            capabilityTypeDefinition.setProperties(propertiesStatus.left().value());
        }
        Either<ImmutablePair<CapabilityTypeData, GraphEdge>, JanusGraphOperationStatus> parentNode = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), ctData.getUniqueId(), GraphEdgeLabels.DERIVED_FROM,
                NodeTypeEnum.CapabilityType, CapabilityTypeData.class);
        log.debug("After retrieving DERIVED_FROM node of {}. status is {}", ctData.getUniqueId(), parentNode);
        if (parentNode.isRight()) {
            JanusGraphOperationStatus janusGraphOperationStatus = parentNode.right().value();
            if (janusGraphOperationStatus != JanusGraphOperationStatus.NOT_FOUND) {
                log.error(BUSINESS_PROCESS_ERROR, "Failed to find the parent capability of capability type {}. status is {}", ctData.getUniqueId(), janusGraphOperationStatus);
                return Either.right(janusGraphOperationStatus);
            }
        } else {
            // derived from node was found
            ImmutablePair<CapabilityTypeData, GraphEdge> immutablePair = parentNode.left().value();
            CapabilityTypeData parentCT = immutablePair.getKey();
            capabilityTypeDefinition.setDerivedFrom(parentCT.getCapabilityTypeDataDefinition().getType());
        }
        
        final Either<ImmutablePair<ModelData, GraphEdge>, JanusGraphOperationStatus> model = janusGraphGenericDao.getParentNode(
            UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), ctData.getUniqueId(), GraphEdgeLabels.MODEL_ELEMENT, 
            NodeTypeEnum.Model, ModelData.class);
        if (model.isLeft()) {
            capabilityTypeDefinition.setModel(model.left().value().getLeft().getName());
        }
        
        return Either.left(capabilityTypeDefinition);
    }

    public Either<Boolean, StorageOperationStatus> isCapabilityTypeDerivedFrom(String childCandidateType, String parentCandidateType) {
        return derivedFromOperation
            .isTypeDerivedFrom(childCandidateType, parentCandidateType, null, NodeTypeEnum.CapabilityType, CapabilityTypeData.class,
                t -> t.getCapabilityTypeDataDefinition().getType());
    }

    @Override
    public Either<CapabilityTypeDefinition, StorageOperationStatus> updateCapabilityType(CapabilityTypeDefinition capabilityTypeDefNew,
                                                                                         CapabilityTypeDefinition capabilityTypeDefOld) {
        log.debug("updating capability type {}", capabilityTypeDefNew.getType());
        updateCapabilityTypeData(capabilityTypeDefNew, capabilityTypeDefOld);
        return updateCapabilityTypeOnGraph(capabilityTypeDefNew, capabilityTypeDefOld);
    }

    private Either<CapabilityTypeDefinition, StorageOperationStatus> updateCapabilityTypeOnGraph(CapabilityTypeDefinition capabilityTypeDefinitionNew,
                                                                                                 CapabilityTypeDefinition capabilityTypeDefinitionOld) {
        return janusGraphGenericDao.updateNode(new CapabilityTypeData(capabilityTypeDefinitionNew), CapabilityTypeData.class).right()
            .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus).left()
            .bind(updatedNode -> updateProperties(capabilityTypeDefinitionNew.getUniqueId(), capabilityTypeDefinitionNew.getProperties())).left()
            .bind(updatedProperties -> updateDerivedFrom(capabilityTypeDefinitionNew, capabilityTypeDefinitionOld.getDerivedFrom())).right()
            .bind(result -> TypeOperations.mapOkStatus(result, null)).left().map(updatedDerivedFrom -> capabilityTypeDefinitionNew);
    }

    private Either<Map<String, PropertyData>, StorageOperationStatus> updateProperties(String capabilityTypeId,
                                                                                       Map<String, PropertyDefinition> properties) {
        log.debug("#updateCapabilityTypeProperties - updating properties for capability type with id {}", capabilityTypeId);
        return propertyOperation.mergePropertiesAssociatedToNode(NodeTypeEnum.CapabilityType, capabilityTypeId, properties).right()
            .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    private Either<GraphRelation, StorageOperationStatus> updateDerivedFrom(CapabilityTypeDefinition updatedCapabilityType,
                                                                            String currDerivedFromCapabilityType) {
        if (StringUtils.equals(updatedCapabilityType.getDerivedFrom(), currDerivedFromCapabilityType)) {
            return Either.right(StorageOperationStatus.OK);
        }
        StorageOperationStatus status = isLegalToReplaceParent(currDerivedFromCapabilityType, updatedCapabilityType.getDerivedFrom(),
            updatedCapabilityType.getType());
        if (status != StorageOperationStatus.OK) {
            return Either.right(status);
        }
        String capabilityTypeId = updatedCapabilityType.getUniqueId();
        log.debug(
            "#updateCapabilityTypeDerivedFrom - updating capability type derived from relation for capability type with id {}. old derived type {}. new derived type {}",
            capabilityTypeId, currDerivedFromCapabilityType, updatedCapabilityType.getDerivedFrom());
        StorageOperationStatus deleteDerivedRelationStatus = deleteDerivedFromCapabilityType(capabilityTypeId, currDerivedFromCapabilityType);
        if (deleteDerivedRelationStatus != StorageOperationStatus.OK) {
            return Either.right(deleteDerivedRelationStatus);
        }
        return addDerivedFromRelation(updatedCapabilityType, capabilityTypeId);
    }

    private StorageOperationStatus isLegalToReplaceParent(String oldTypeParent, String newTypeParent, String childType) {
        return derivedFromOperation
            .isUpdateParentAllowed(oldTypeParent, newTypeParent, childType, NodeTypeEnum.CapabilityType, CapabilityTypeData.class,
                t -> t.getCapabilityTypeDataDefinition().getType());
    }

    private Either<GraphRelation, StorageOperationStatus> addDerivedFromRelation(CapabilityTypeDefinition capabilityTypeDef, String ptUniqueId) {
        String derivedFrom = capabilityTypeDef.getDerivedFrom();
        if (derivedFrom == null) {
            return Either.left(null);
        }
        log.debug("#addDerivedFromRelationBefore - adding derived from relation between capability type {} to its parent {}",
            capabilityTypeDef.getType(), derivedFrom);
        return this.getCapabilityTypeByType(derivedFrom, capabilityTypeDef.getModel())
            .right().map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus)
            .left().bind(derivedFromCapabilityType -> derivedFromOperation
                .addDerivedFromRelation(ptUniqueId, derivedFromCapabilityType.getUniqueId(), NodeTypeEnum.CapabilityType));
    }

    private StorageOperationStatus deleteDerivedFromCapabilityType(String capabilityTypeId, String derivedFromType) {
        if (derivedFromType == null) {
            return StorageOperationStatus.OK;
        }
        log.debug("#deleteDerivedFromCapabilityType - deleting derivedFrom relation for capability type with id {} and its derived type {}",
            capabilityTypeId, derivedFromType);
        return getCapabilityType(derivedFromType, true).either(derivedFromNode -> derivedFromOperation
            .removeDerivedFromRelation(capabilityTypeId, derivedFromNode.getUniqueId(), NodeTypeEnum.CapabilityType), err -> err);
    }

    private void updateCapabilityTypeData(CapabilityTypeDefinition updatedTypeDefinition, CapabilityTypeDefinition currTypeDefinition) {
        updatedTypeDefinition.setUniqueId(currTypeDefinition.getUniqueId());
        updatedTypeDefinition.setCreationTime(currTypeDefinition.getCreationTime());
    }

    /**
     * FOR TEST ONLY
     *
     * @param propertyOperation
     */
    public void setPropertyOperation(PropertyOperation propertyOperation) {
        this.propertyOperation = propertyOperation;
    }

    @Override
    public Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType(CapabilityTypeDefinition capabilityTypeDefinition) {
        return addCapabilityType(capabilityTypeDefinition, true);
    }

    @Override
    public Either<CapabilityTypeDefinition, StorageOperationStatus> getCapabilityType(String uniqueId) {
        return getCapabilityType(uniqueId, true);
    }

    public Either<Map<String, CapabilityTypeDefinition>, JanusGraphOperationStatus> getAllCapabilityTypes(String modelName) {
        Map<String, CapabilityTypeDefinition> capabilityTypes = new HashMap<>();
        Either<Map<String, CapabilityTypeDefinition>, JanusGraphOperationStatus> result = Either.left(capabilityTypes);
        Either<List<CapabilityTypeData>, JanusGraphOperationStatus> getAllCapabilityTypes = janusGraphGenericDao
            .getByCriteriaForModel(NodeTypeEnum.CapabilityType, null, modelName, CapabilityTypeData.class);
        if (getAllCapabilityTypes.isRight()) {
            JanusGraphOperationStatus status = getAllCapabilityTypes.right().value();
            if (status != JanusGraphOperationStatus.NOT_FOUND) {
                return Either.right(status);
            } else {
                return result;
            }
        }
        List<CapabilityTypeData> list = getAllCapabilityTypes.left().value();
        if (list != null) {
            log.trace("Number of data types to load is {}", list.size());
            //Set properties
            for (CapabilityTypeData capabilityTypeData : list) {
                log.trace("Going to fetch data type {}. uid is {}", capabilityTypeData.getCapabilityTypeDataDefinition().getType(),
                    capabilityTypeData.getUniqueId());
                Either<CapabilityTypeDefinition, JanusGraphOperationStatus> capabilityTypesByUid = getAndAddPropertiesANdDerivedFrom(
                    capabilityTypeData.getUniqueId(), capabilityTypes);
                if (capabilityTypesByUid.isRight()) {
                    JanusGraphOperationStatus status = capabilityTypesByUid.right().value();
                    if (status == JanusGraphOperationStatus.NOT_FOUND) {
                        status = JanusGraphOperationStatus.INVALID_ID;
                    }
                    return Either.right(status);
                }
            }
        }
        return result;
    }

    private void fillDerivedFrom(String uniqueId, CapabilityTypeDefinition capabilityType) {
        log.debug("#fillDerivedFrom - fetching capability type {} derived node", capabilityType.getType());
        derivedFromOperation.getDerivedFromChild(uniqueId, NodeTypeEnum.CapabilityType, CapabilityTypeData.class).right()
            .bind(this::handleDerivedFromNotExist).left().map(derivedFrom -> setDerivedFrom(capabilityType, derivedFrom));
    }

    private Either<CapabilityTypeData, StorageOperationStatus> handleDerivedFromNotExist(StorageOperationStatus err) {
        if (err == StorageOperationStatus.NOT_FOUND) {
            return Either.left(null);
        }
        return Either.right(err);
    }

    private CapabilityTypeData setDerivedFrom(CapabilityTypeDefinition capabilityTypeDefinition, CapabilityTypeData derivedFrom) {
        if (derivedFrom != null) {
            capabilityTypeDefinition.setDerivedFrom(derivedFrom.getCapabilityTypeDataDefinition().getType());
        }
        return derivedFrom;
    }

    private Either<CapabilityTypeDefinition, JanusGraphOperationStatus> getAndAddPropertiesANdDerivedFrom(String uniqueId,
                                                                                                          Map<String, CapabilityTypeDefinition> capabilityTypeDefinitionMap) {
        if (capabilityTypeDefinitionMap.containsKey(uniqueId)) {
            return Either.left(capabilityTypeDefinitionMap.get(uniqueId));
        }
        Either<CapabilityTypeData, JanusGraphOperationStatus> capabilityTypesRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), uniqueId, CapabilityTypeData.class);
        if (capabilityTypesRes.isRight()) {
            JanusGraphOperationStatus status = capabilityTypesRes.right().value();
            log.debug(DATA_TYPE_CANNOT_BE_FOUND_IN_GRAPH_STATUS_IS, uniqueId, status);
            return Either.right(status);
        }
        CapabilityTypeData ctData = capabilityTypesRes.left().value();
        CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition(ctData.getCapabilityTypeDataDefinition());
        Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> propertiesStatus = OperationUtils
            .fillProperties(uniqueId, propertyOperation, NodeTypeEnum.CapabilityType);
        if (propertiesStatus.isRight() && propertiesStatus.right().value() != JanusGraphOperationStatus.OK) {
            log.error(FAILED_TO_FETCH_PROPERTIES_OF_DATA_TYPE, uniqueId);
            return Either.right(propertiesStatus.right().value());
        }
        if (propertiesStatus.isLeft()) {
            capabilityTypeDefinition.setProperties(propertiesStatus.left().value());
        }
        fillDerivedFrom(uniqueId, capabilityTypeDefinition);
        capabilityTypeDefinitionMap.put(capabilityTypeDefinition.getType(), capabilityTypeDefinition);
        return Either.left(capabilityTypeDefinition);
    }
}
