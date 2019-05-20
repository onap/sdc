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

import com.google.common.base.Strings;
import org.janusgraph.graphdb.query.JanusGraphPredicate;
import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.GroupTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.DerivedFromOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.api.TypeOperations;
import org.openecomp.sdc.be.model.utils.TypeCompareUtils;
import org.openecomp.sdc.be.resources.data.*;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openecomp.sdc.be.dao.janusgraph.JanusGraphUtils.buildNotInPredicate;

@Component("group-type-operation")
public class GroupTypeOperation implements IGroupTypeOperation {

    private static final Logger log = Logger.getLogger(GroupTypeOperation.class.getName());
    private static final String CREATE_FLOW_CONTEXT = "CreateGroupType";

    private final PropertyOperation propertyOperation;
    private final JanusGraphGenericDao janusGraphGenericDao;
    private final CapabilityTypeOperation capabilityTypeOperation;
    private final CapabilityOperation capabilityOperation;
    private final DerivedFromOperation derivedFromOperation;
    private final OperationUtils operationUtils;


    public GroupTypeOperation(JanusGraphGenericDao janusGraphGenericDao,
                              PropertyOperation propertyOperation,
                              CapabilityTypeOperation capabilityTypeOperation,
                              CapabilityOperation capabilityOperation,
                              DerivedFromOperation derivedFromOperation, OperationUtils operationUtils) {
        this.janusGraphGenericDao = janusGraphGenericDao;
        this.propertyOperation = propertyOperation;
        this.capabilityTypeOperation = capabilityTypeOperation;
        this.capabilityOperation = capabilityOperation;
        this.derivedFromOperation = derivedFromOperation;
        this.operationUtils = operationUtils;
    }

    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> addGroupType(GroupTypeDefinition groupTypeDefinition) {
        Either<GroupTypeDefinition, StorageOperationStatus> validationRes = validateUpdateProperties(groupTypeDefinition);
        if (validationRes.isRight()) {
            log.error("#addGroupType - One or all properties of group type {} not valid. status is {}", groupTypeDefinition, validationRes.right().value());
            return validationRes;
        }
        
        return addGroupType(groupTypeDefinition, true);
    }

    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> addGroupType(GroupTypeDefinition groupTypeDefinition, boolean inTransaction) {

        Either<GroupTypeDefinition, StorageOperationStatus> result = null;

        try {

            Either<GroupTypeData, JanusGraphOperationStatus> eitherStatus = addGroupTypeToGraph(groupTypeDefinition);

            if (eitherStatus.isRight()) {
                BeEcompErrorManager.getInstance().logBeFailedCreateNodeError(CREATE_FLOW_CONTEXT, groupTypeDefinition.getType(), eitherStatus.right().value().name());
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(eitherStatus.right().value()));
            }
            else {
                result = getGroupType(eitherStatus.left().value().getUniqueId(), inTransaction);
            }

            return result;

        } finally {
            janusGraphGenericDao.handleTransactionCommitRollback(inTransaction, result);
        }

    }

    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> updateGroupType(GroupTypeDefinition updatedGroupType, GroupTypeDefinition currGroupType) {
        log.debug("updating group type {}", updatedGroupType.getType());
        return updateGroupTypeOnGraph(updatedGroupType, currGroupType);
    }
    
    
    public Either<GroupTypeDefinition, StorageOperationStatus> validateUpdateProperties(GroupTypeDefinition groupTypeDefinition) {
        JanusGraphOperationStatus error = null;
        if (CollectionUtils.isNotEmpty(groupTypeDefinition.getProperties()) && !Strings.isNullOrEmpty(groupTypeDefinition.getDerivedFrom())) {
            Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> allPropertiesRes =
                                        getAllGroupTypePropertiesFromAllDerivedFrom(groupTypeDefinition.getDerivedFrom());
            if (allPropertiesRes.isRight() && !allPropertiesRes.right().value().equals(
                JanusGraphOperationStatus.NOT_FOUND)) {
                error = allPropertiesRes.right().value();
                log.debug("Couldn't fetch derived from property nodes for group type {}, error: {}", groupTypeDefinition.getType(), error);
            }
            if (error == null && !allPropertiesRes.left().value().isEmpty()) {
                Either<List<PropertyDefinition>, JanusGraphOperationStatus> validatePropertiesRes = propertyOperation.validatePropertiesUniqueness(allPropertiesRes.left().value(),
                        groupTypeDefinition.getProperties());
                if (validatePropertiesRes.isRight()) {
                    error = validatePropertiesRes.right().value();
                }
            }
        }
        if (error == null) {
            return Either.left(groupTypeDefinition);
        }
        return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(error));
    }
    
    private Either<Map<String, PropertyDefinition>, JanusGraphOperationStatus> getAllGroupTypePropertiesFromAllDerivedFrom(String firstParentType) {
        return janusGraphGenericDao
            .getNode(GraphPropertiesDictionary.TYPE.getProperty(), firstParentType, GroupTypeData.class)
                    .left()
                    .bind(parentGroup -> propertyOperation.getAllTypePropertiesFromAllDerivedFrom(parentGroup.getUniqueId(), NodeTypeEnum.GroupType, GroupTypeData.class));
    }


    private StorageOperationStatus mergeCapabilities(GroupTypeDefinition groupTypeDef) {
        Map<String, CapabilityDefinition> updatedGroupTypeCapabilities = groupTypeDef.getCapabilities();
        Map<String, CapabilityDefinition> newGroupTypeCapabilities;
        Either<List<CapabilityDefinition>, StorageOperationStatus> oldCapabilitiesRes = getCapablities(groupTypeDef.getUniqueId());
        if (oldCapabilitiesRes.isRight()) {
            StorageOperationStatus status = oldCapabilitiesRes.right().value();
            if (status == StorageOperationStatus.NOT_FOUND) {
                newGroupTypeCapabilities = updatedGroupTypeCapabilities;
            }
            else {
                return status;
            }
        }
        else {
            Map<String, CapabilityDefinition> oldCapabilities = asCapabilitiesMap(oldCapabilitiesRes.left().value());
            newGroupTypeCapabilities = collectNewCapabilities(updatedGroupTypeCapabilities, oldCapabilities);

            for(Map.Entry<String, CapabilityDefinition> oldEntry: oldCapabilities.entrySet()) {
                String key = oldEntry.getKey();
                CapabilityDefinition newCapDef = updatedGroupTypeCapabilities != null? updatedGroupTypeCapabilities.get(key): null;
                CapabilityDefinition oldCapDef = oldEntry.getValue();

                StorageOperationStatus deleteCapResult = deleteOutdatedCapability(newGroupTypeCapabilities, newCapDef, oldCapDef);
                if(deleteCapResult != StorageOperationStatus.OK) {
                    return deleteCapResult;
                }
            }
        }

        JanusGraphOperationStatus createCapResult = createCapabilities(new GroupTypeData(groupTypeDef), newGroupTypeCapabilities);
        return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(createCapResult);
    }

    /**
     * @param newGroupTypeCapabilities
     * @param newCapDef
     * @param oldCapDef
     * @return 
     */
    private StorageOperationStatus deleteOutdatedCapability(Map<String, CapabilityDefinition> newGroupTypeCapabilities, CapabilityDefinition newCapDef, CapabilityDefinition oldCapDef) {
        if(!isUpdateAllowed(newCapDef, oldCapDef)) {
            return StorageOperationStatus.MATCH_NOT_FOUND;
        }

        if (!TypeCompareUtils.capabilityEquals(oldCapDef, newCapDef)) {
            StorageOperationStatus deleteCapResult = capabilityOperation.deleteCapability(oldCapDef);

            if(deleteCapResult == StorageOperationStatus.OK) {
                newGroupTypeCapabilities.put(newCapDef.getName(), newCapDef);
            }
            else {
                return deleteCapResult;
            }
        }
        
        return StorageOperationStatus.OK;
    }

    private boolean isUpdateAllowed(CapabilityDefinition newCapDef, CapabilityDefinition oldCapDef) {
        if (newCapDef == null) {
            log.error("#upsertCapabilities - Failed due to attempt to delete the capability with id {}", oldCapDef.getUniqueId());
            return false;
        }

        if (newCapDef.getType() == null || !newCapDef.getType().equals(oldCapDef.getType())) {
            log.error("#upsertCapabilities - Failed due to attempt to change type of the capability with id {}", oldCapDef.getUniqueId());
            return false;
        }
        
        return true;
    }

    /**
     * @param updatedGroupTypeCapabilities
     * @param oldCapabilities
     * @return
     */
    private Map<String, CapabilityDefinition> collectNewCapabilities(Map<String, CapabilityDefinition> updatedGroupTypeCapabilities, Map<String, CapabilityDefinition> oldCapabilities) {
        return updatedGroupTypeCapabilities != null? updatedGroupTypeCapabilities.entrySet().stream()
                .filter(entry -> !oldCapabilities.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue) ): null;
    }

    private JanusGraphOperationStatus createCapabilities(GroupTypeData groupTypeData, Map<String, CapabilityDefinition> groupCapabilities) {
        if (MapUtils.isEmpty(groupCapabilities)) {
            return JanusGraphOperationStatus.OK;
        }
        
        return groupCapabilities.values().stream()
                .map(v -> createCapability(groupTypeData, v))
                .filter(Either::isRight)
                .findFirst()
                .map(either -> either.right().value())
                .orElse(JanusGraphOperationStatus.OK);
    }

    private Either<GraphRelation, JanusGraphOperationStatus> createCapability(GroupTypeData groupTypeData, CapabilityDefinition  capabilityDef) {
        Either<CapabilityTypeDefinition, JanusGraphOperationStatus> eitherCapData = capabilityTypeOperation.getCapabilityTypeByType(capabilityDef.getType());
        return eitherCapData
                .left()
                .map(CapabilityTypeData::new)
                .left()
                .bind(capTypeData -> capabilityOperation.addCapabilityToGraph(groupTypeData.getUniqueId(), capTypeData, capabilityDef))
                .left()
                .bind(capData -> connectToCapability(groupTypeData, capData, capabilityDef.getName()));
    }


    /**
     * Get capability with all relevant properties
     * @param groupTypeId
     * @return
     */
    private Either<List<CapabilityDefinition>, StorageOperationStatus> getCapablities(String groupTypeId) {
        Either<List<ImmutablePair<CapabilityData, GraphEdge>>, JanusGraphOperationStatus> groupCapabilitiesOnGraph =
                janusGraphGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.GroupType), groupTypeId, GraphEdgeLabels.GROUP_TYPE_CAPABILITY, NodeTypeEnum.Capability, CapabilityData.class, true);

        if (groupCapabilitiesOnGraph.isRight()) {
            JanusGraphOperationStatus capabilityStatus = groupCapabilitiesOnGraph.right().value();
            if (capabilityStatus == JanusGraphOperationStatus.NOT_FOUND) {
                return Either.left(Collections.emptyList());
            }
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(capabilityStatus));
        }

        List<ImmutablePair<CapabilityData, GraphEdge>> groupCapabilites = groupCapabilitiesOnGraph.left().value();
        groupCapabilites.forEach(this::fillCapabilityName);

        return capabilityOperation.getCapabilitiesWithProps(groupCapabilites)
                .right()
                .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    private void fillCapabilityName(ImmutablePair<CapabilityData, GraphEdge> pair) {
        pair.getLeft().getCapabilityDataDefinition().setName((String)pair.getRight().getProperties().get(GraphEdgePropertiesDictionary.NAME.getProperty()));
    }

    private Either<GraphRelation, JanusGraphOperationStatus> connectToCapability(GroupTypeData groupTypeData, CapabilityData capabilityData, String capabilityName) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(GraphEdgePropertiesDictionary.NAME.getProperty(), capabilityName);

        return janusGraphGenericDao.createRelation(groupTypeData, capabilityData, GraphEdgeLabels.GROUP_TYPE_CAPABILITY, properties);
    }


    public List<GroupTypeDefinition> getAllGroupTypes(Set<String> excludedGroupTypes) {
        Map<String, Map.Entry<JanusGraphPredicate, Object>> predicateCriteria = buildNotInPredicate(GraphPropertiesDictionary.TYPE.getProperty(), excludedGroupTypes);
        List<GroupTypeData> groupTypes = janusGraphGenericDao
            .getByCriteriaWithPredicate(NodeTypeEnum.GroupType, predicateCriteria, GroupTypeData.class)
                .left()
                .on(operationUtils::onJanusGraphOperationFailure);
        return convertGroupTypesToDefinition(groupTypes);
    }


    private List<GroupTypeDefinition> convertGroupTypesToDefinition(List<GroupTypeData> groupTypes) {
        return groupTypes.stream()
                .map(type -> new GroupTypeDefinition(type.getGroupTypeDataDefinition()))
                .collect(Collectors.toList());
    }


    public Either<GroupTypeDefinition, StorageOperationStatus> getGroupTypeByUid(String uniqueId) {
        log.debug("#getGroupTypeByUid - fetching group type with id {}", uniqueId);
        return janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.GroupType), uniqueId, GroupTypeData.class)
                .right()
                .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus)
                .left()
                .bind(groupType -> buildGroupTypeDefinition(uniqueId, groupType));
    }

    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> getGroupType(String uniqueId, boolean inTransaction) {
        Either<GroupTypeDefinition, StorageOperationStatus> result = null;
        try {

            Either<GroupTypeDefinition, StorageOperationStatus> ctResult = getGroupTypeByUid(uniqueId);

            if (ctResult.isRight()) {
                StorageOperationStatus status = ctResult.right().value();
                if (status != StorageOperationStatus.NOT_FOUND) {
                    log.error("Failed to retrieve information on element uniqueId: {}. status is {}", uniqueId, status);
                }
                result = Either.right(ctResult.right().value());
                return result;
            }

            result = Either.left(ctResult.left().value());

            return result;
        } finally {
            janusGraphGenericDao.handleTransactionCommitRollback(inTransaction, result);
        }

    }

    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> getLatestGroupTypeByType(String type) {
        return getLatestGroupTypeByType(type, true);
    }

    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> getLatestGroupTypeByType(String type, boolean inTransaction) {
        Map<String, Object> mapCriteria = new HashMap<>();
        mapCriteria.put(GraphPropertiesDictionary.TYPE.getProperty(), type);
        mapCriteria.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);

        return getGroupTypeByCriteria(type, mapCriteria, inTransaction);

    }

    public Either<GroupTypeDefinition, StorageOperationStatus> getGroupTypeByCriteria(String type, Map<String, Object> properties, boolean inTransaction) {
        Either<GroupTypeDefinition, StorageOperationStatus> result = null;
        try {
            if (type == null || type.isEmpty()) {
                log.error("type is empty");
                result = Either.right(StorageOperationStatus.INVALID_ID);
                return result;
            }

            Either<List<GroupTypeData>, StorageOperationStatus> groupTypeEither = janusGraphGenericDao
                .getByCriteria(NodeTypeEnum.GroupType, properties, GroupTypeData.class)
                    .right()
                    .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
            if (groupTypeEither.isRight()) {
                result = Either.right(groupTypeEither.right().value());
            } else {
                GroupTypeDataDefinition dataDefinition = groupTypeEither.left().value().stream()
                        .map(GroupTypeData::getGroupTypeDataDefinition)
                        .findFirst()
                        .get();
                result = getGroupTypeByUid(dataDefinition.getUniqueId());

            }
            return result;

        } finally {
            janusGraphGenericDao.handleTransactionCommitRollback(inTransaction, result);
        }
    }

    private Either<GroupTypeDefinition, StorageOperationStatus> buildGroupTypeDefinition(String uniqueId, GroupTypeData groupTypeNode) {
        GroupTypeDefinition groupType = new GroupTypeDefinition(groupTypeNode.getGroupTypeDataDefinition());
        return fillDerivedFrom(uniqueId, groupType)
                .left()
                .map(derivedFrom -> fillProperties(uniqueId, groupType, derivedFrom))
                .left()
                .bind(props -> fillCapabilities(uniqueId, groupType));
    }
    
    private Either<GroupTypeDefinition, StorageOperationStatus> fillCapabilities(String uniqueId, GroupTypeDefinition groupType) {
        return getCapablities(uniqueId)
                .left()
                .map(capabilities -> {
                    groupType.setCapabilities(asCapabilitiesMap(capabilities));
                    return groupType;
                });
    }

    private Either<GroupTypeData, StorageOperationStatus> fillDerivedFrom(String uniqueId, GroupTypeDefinition groupType) {
        log.debug("#fillDerivedFrom - fetching group type {} derived node", groupType.getType());
        return derivedFromOperation.getDerivedFromChild(uniqueId, NodeTypeEnum.GroupType, GroupTypeData.class)
                .right()
                .bind(this::handleDerivedFromNotExist)
                .left()
                .map(derivedFrom -> setDerivedFrom(groupType, derivedFrom));

    }

    private Either<List<PropertyDefinition>, StorageOperationStatus> fillProperties(String uniqueId, GroupTypeDefinition groupType, GroupTypeData derivedFromNode) {
        log.debug("#fillProperties - fetching all properties for group type {}", groupType.getType());
        return propertyOperation.findPropertiesOfNode(NodeTypeEnum.GroupType, uniqueId)
                .right()
                .bind(this::handleGroupTypeHasNoProperties)
                .left()
                .bind(propsMap -> fillDerivedFromProperties(groupType, derivedFromNode, new ArrayList<>(propsMap.values())));
    }

    Either<Map<String, PropertyDefinition>, StorageOperationStatus> handleGroupTypeHasNoProperties(JanusGraphOperationStatus err) {
        if (err == JanusGraphOperationStatus.NOT_FOUND) {
            return Either.left(new HashMap<>());
        }
        return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(err));
    }

    private Either<List<PropertyDefinition>, StorageOperationStatus> fillDerivedFromProperties(GroupTypeDefinition groupType, GroupTypeData derivedFromNode, List<PropertyDefinition> groupTypeDirectProperties) {
        if (derivedFromNode == null) {
            groupType.setProperties(groupTypeDirectProperties);
            return Either.left(groupTypeDirectProperties);
        }
        log.debug("#fillDerivedFromProperties - fetching all properties of derived from chain for group type {}", groupType.getType());
        return propertyOperation.getAllPropertiesRec(derivedFromNode.getUniqueId(), NodeTypeEnum.GroupType, GroupTypeData.class)
                .left()
                .map(derivedFromProps -> {groupTypeDirectProperties.addAll(derivedFromProps); return groupTypeDirectProperties;})
                .left()
                .map(allProps -> {groupType.setProperties(allProps);return allProps;});
    }

    private Either<GroupTypeData, StorageOperationStatus> handleDerivedFromNotExist(StorageOperationStatus err) {
        if (err == StorageOperationStatus.NOT_FOUND) {
            return Either.left(null);
        }
        return Either.right(err);
    }

    private GroupTypeData setDerivedFrom(GroupTypeDefinition groupTypeDefinition, GroupTypeData derivedFrom) {
        if (derivedFrom != null) {
            groupTypeDefinition.setDerivedFrom(derivedFrom.getGroupTypeDataDefinition().getType());
        }
        return derivedFrom;
    }


    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> getGroupTypeByTypeAndVersion(String type, String version) {
        return getGroupTypeByTypeAndVersion(type, version, false);
    }

    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> getGroupTypeByTypeAndVersion(String type, String version, boolean inTransaction) {
        Map<String, Object> mapCriteria = new HashMap<>();
        mapCriteria.put(GraphPropertiesDictionary.TYPE.getProperty(), type);
        mapCriteria.put(GraphPropertiesDictionary.VERSION.getProperty(), version);

        return getGroupTypeByCriteria(type, mapCriteria, inTransaction);
    }

    /**
     * Add group type to graph.
     * <p>
     * 1. Add group type node
     * <p>
     * 2. Add edge between the former node to its parent(if exists)
     * <p>
     * 3. Add property node and associate it to the node created at #1. (per property & if exists)
     *
     * @param groupTypeDefinition
     * @return
     */
    private Either<GroupTypeData, JanusGraphOperationStatus> addGroupTypeToGraph(GroupTypeDefinition groupTypeDefinition) {

        log.debug("Got group type {}", groupTypeDefinition);

        String ctUniqueId = UniqueIdBuilder.buildGroupTypeUid(groupTypeDefinition.getType(), groupTypeDefinition.getVersion(), "grouptype");

        GroupTypeData groupTypeData = buildGroupTypeData(groupTypeDefinition, ctUniqueId);

        log.debug("Before adding group type to graph. groupTypeData = {}", groupTypeData);

        Either<GroupTypeData, JanusGraphOperationStatus> createGTResult = janusGraphGenericDao
            .createNode(groupTypeData, GroupTypeData.class);
        log.debug("After adding group type to graph. status is = {}", createGTResult);

        if (createGTResult.isRight()) {
            JanusGraphOperationStatus operationStatus = createGTResult.right().value();
            log.error("Failed to add group type {} to graph. status is {}", groupTypeDefinition.getType(), operationStatus);
            return Either.right(operationStatus);
        }

        GroupTypeData resultCTD = createGTResult.left().value();
        List<PropertyDefinition> properties = groupTypeDefinition.getProperties();
        Either<Map<String, PropertyData>, JanusGraphOperationStatus> addPropertiesToCapablityType = propertyOperation.addPropertiesToElementType(resultCTD.getUniqueId(), NodeTypeEnum.GroupType, properties);
        if (addPropertiesToCapablityType.isRight()) {
            log.error("Failed add properties {} to capability {}", properties, groupTypeDefinition.getType());
            return Either.right(addPropertiesToCapablityType.right().value());
        }

        String derivedFrom = groupTypeDefinition.getDerivedFrom();
        if (derivedFrom != null) {
            Either<GraphRelation, JanusGraphOperationStatus> createRelation = connectToDerivedFrom(ctUniqueId, derivedFrom);
            if (createRelation.isRight()) {
                return Either.right(createRelation.right().value());
            }
        }
        
        Map<String, CapabilityDefinition> groupCapTypes = groupTypeDefinition.getCapabilities();
        if (!MapUtils.isEmpty(groupCapTypes)) {
            JanusGraphOperationStatus status = createCapabilities(groupTypeData, groupCapTypes);
            if (status != JanusGraphOperationStatus.OK) {
                return Either.right(status);
            }
        }

        return Either.left(createGTResult.left().value());

    }


    private Either<GraphRelation, JanusGraphOperationStatus> connectToDerivedFrom(String ctUniqueId, String derivedFrom) {
        log.debug("Before creating relation between Group Type with id {} to its parent {}", ctUniqueId, derivedFrom);

        Either<GroupTypeData, JanusGraphOperationStatus> derivedFromGroupTypeResult =
                janusGraphGenericDao
                    .getNode(GraphPropertiesDictionary.TYPE.getProperty(), derivedFrom, GroupTypeData.class);

        if (derivedFromGroupTypeResult.isLeft()) {
            UniqueIdData from = new UniqueIdData(NodeTypeEnum.GroupType, ctUniqueId);
            GroupTypeData to = derivedFromGroupTypeResult.left().value();

            Either<GraphRelation, JanusGraphOperationStatus> createRelation = janusGraphGenericDao
                .createRelation(from, to, GraphEdgeLabels.DERIVED_FROM, null);
            log.debug("After create relation between Group Type with id {} to its parent {}, status is {}.", ctUniqueId, derivedFrom, createRelation);
            return createRelation;
        } else {
            JanusGraphOperationStatus status = derivedFromGroupTypeResult.right().value();
            log.debug("Failed to found parent Group Type {}, stauts is {}.", derivedFrom, status);
            return Either.right(status);
        }
    }

    private GroupTypeData buildGroupTypeData(GroupTypeDefinition groupTypeDefinition, String ctUniqueId) {

        GroupTypeData groupTypeData = new GroupTypeData(groupTypeDefinition);

        groupTypeData.getGroupTypeDataDefinition().setUniqueId(ctUniqueId);
        Long creationDate = groupTypeData.getGroupTypeDataDefinition().getCreationTime();
        if (creationDate == null) {
            creationDate = System.currentTimeMillis();
        }
        groupTypeData.getGroupTypeDataDefinition().setCreationTime(creationDate);
        groupTypeData.getGroupTypeDataDefinition().setModificationTime(creationDate);

        return groupTypeData;
    }

    public Either<Boolean, StorageOperationStatus> isCapabilityTypeDerivedFrom(String childCandidateType, String parentCandidateType) {
        Map<String, Object> propertiesToMatch = new HashMap<>();
        propertiesToMatch.put(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), childCandidateType);
        Either<List<CapabilityTypeData>, JanusGraphOperationStatus> getResponse = janusGraphGenericDao
            .getByCriteria(NodeTypeEnum.CapabilityType, propertiesToMatch, CapabilityTypeData.class);
        if (getResponse.isRight()) {
            JanusGraphOperationStatus janusGraphOperationStatus = getResponse.right().value();
            log.debug("Couldn't fetch capability type {}, error: {}", childCandidateType,
                janusGraphOperationStatus);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(
                janusGraphOperationStatus));
        }
        String childUniqueId = getResponse.left().value().get(0).getUniqueId();
        Set<String> travelledTypes = new HashSet<>();
        do {
            travelledTypes.add(childUniqueId);
            Either<List<ImmutablePair<CapabilityTypeData, GraphEdge>>, JanusGraphOperationStatus> childrenNodes = janusGraphGenericDao
                .getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), childUniqueId, GraphEdgeLabels.DERIVED_FROM,
                    NodeTypeEnum.CapabilityType, CapabilityTypeData.class);
            if (childrenNodes.isRight()) {
                if (childrenNodes.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                    JanusGraphOperationStatus janusGraphOperationStatus = getResponse.right().value();
                    log.debug("Couldn't fetch derived from node for capability type {}, error: {}", childCandidateType,
                        janusGraphOperationStatus);
                    return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(
                        janusGraphOperationStatus));
                } else {
                    log.debug("Derived from node is not found for type {} - this is OK for root capability.", childCandidateType);
                    return Either.left(false);
                }
            }
            String derivedFromUniqueId = childrenNodes.left().value().get(0).getLeft().getUniqueId();
            if (derivedFromUniqueId.equals(parentCandidateType)) {
                log.debug("Verified that capability type {} derives from capability type {}", childCandidateType, parentCandidateType);
                return Either.left(true);
            }
            childUniqueId = derivedFromUniqueId;
        } while (!travelledTypes.contains(childUniqueId));
        // this stop condition should never be used, if we use it, we have an
        // illegal cycle in graph - "derived from" hierarchy cannot be cycled.
        // It's here just to avoid infinite loop in case we have such cycle.
        log.error("Detected a cycle of \"derived from\" edges starting at capability type node {}", childUniqueId);
        return Either.right(StorageOperationStatus.GENERAL_ERROR);
    }
    
    /**
     * @param list
     * @return
     */
    private Map<String, CapabilityDefinition> asCapabilitiesMap(List<CapabilityDefinition> list) {
        return list.stream()
                .collect(Collectors.toMap(CapabilityDefinition::getName, Function.identity()));
    }


    private Either<GroupTypeDefinition, StorageOperationStatus> updateGroupTypeOnGraph(GroupTypeDefinition updatedGroupType, GroupTypeDefinition currGroupType) {
        updateGroupTypeData(updatedGroupType, currGroupType);
        return janusGraphGenericDao.updateNode(new GroupTypeData(updatedGroupType), GroupTypeData.class)
                .right()
                .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus)
                .left()
                .bind(updatedNode -> updateGroupProperties(updatedGroupType.getUniqueId(), updatedGroupType.getProperties()))
                .left()
                .bind(updatedProperties -> updateGroupDerivedFrom(updatedGroupType, currGroupType.getDerivedFrom()))
                .right()
                .bind(result -> TypeOperations.mapOkStatus(result, null))
                .left()
                .bind(updatedDerivedFrom -> TypeOperations.mapOkStatus(mergeCapabilities(updatedGroupType), updatedGroupType))
                .left()
                .bind(def -> getGroupTypeByUid(def.getUniqueId()));
    }
    

    private Either<Map<String, PropertyData>, StorageOperationStatus> updateGroupProperties(String groupId, List<PropertyDefinition> properties) {
        log.debug("#updateGroupProperties - updating group type properties for group type with id {}", groupId);
        Map<String, PropertyDefinition> mapProperties = properties != null? properties.stream()
                .collect(Collectors.toMap(PropertyDefinition::getName, Function.identity())): null;
        return propertyOperation.mergePropertiesAssociatedToNode(NodeTypeEnum.GroupType, groupId, mapProperties)
                .right()
                .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }



    private Either<GraphRelation, StorageOperationStatus> updateGroupDerivedFrom(GroupTypeDefinition updatedGroupType, String currDerivedFromGroupType) {
        
        String groupTypeId = updatedGroupType.getUniqueId();
        if (StringUtils.equals(updatedGroupType.getDerivedFrom(), currDerivedFromGroupType)) {
            return Strings.isNullOrEmpty(currDerivedFromGroupType)? 
                    Either.right(StorageOperationStatus.OK):
                        getLatestGroupTypeByType(currDerivedFromGroupType, true)
                        .left()
                        .map(def -> null);
        }
        
        StorageOperationStatus status = isLegalToReplaceParent(currDerivedFromGroupType, updatedGroupType.getDerivedFrom(), updatedGroupType.getType());
        if ( status != StorageOperationStatus.OK) {
            return Either.right(status);
        }

        log.debug("#updateGroupDerivedFrom - updating group derived from relation for group type with id {}. old derived type {}. new derived type {}", groupTypeId, currDerivedFromGroupType, updatedGroupType.getDerivedFrom());
        StorageOperationStatus deleteDerivedRelationStatus = deleteDerivedFromGroupType(groupTypeId, currDerivedFromGroupType);
        if (deleteDerivedRelationStatus != StorageOperationStatus.OK) {
            return Either.right(deleteDerivedRelationStatus);
        }
        return addDerivedFromRelation(updatedGroupType, groupTypeId);
    }

    private StorageOperationStatus isLegalToReplaceParent(String oldTypeParent, String newTypeParent, String childType) {
        return derivedFromOperation.isUpdateParentAllowed(oldTypeParent, newTypeParent, childType, NodeTypeEnum.GroupType, GroupTypeData.class, t -> t.getGroupTypeDataDefinition().getType());
    }
    
    private Either<GraphRelation, StorageOperationStatus> addDerivedFromRelation(GroupTypeDataDefinition groupTypeDef, String gtUniqueId) {
        String derivedFrom = groupTypeDef.getDerivedFrom();
        if (derivedFrom == null) {
            return Either.left(null);
        }
        log.debug("#addDerivedFromRelationBefore - adding derived from relation between group type {} to its parent {}", groupTypeDef.getType(), derivedFrom);
        return this.getLatestGroupTypeByType(derivedFrom, true)
                .left()
                .bind(derivedFromGroup -> derivedFromOperation.addDerivedFromRelation(gtUniqueId, derivedFromGroup.getUniqueId(), NodeTypeEnum.GroupType));
    }

    private StorageOperationStatus deleteDerivedFromGroupType(String groupTypeId, String derivedFromType) {
        if (derivedFromType == null) {
            return StorageOperationStatus.OK;
        }
        log.debug("#deleteDerivedFromGroupType - deleting derivedFrom relation for group type with id {} and its derived type {}", groupTypeId, derivedFromType);
        return getLatestGroupTypeByType(derivedFromType, true)
                .either(derivedFromNode -> derivedFromOperation.removeDerivedFromRelation(groupTypeId, derivedFromNode.getUniqueId(), NodeTypeEnum.GroupType),
                        err -> err);
    }

    private void updateGroupTypeData(GroupTypeDefinition updatedTypeDefinition, GroupTypeDefinition currTypeDefinition) {
        updatedTypeDefinition.setUniqueId(currTypeDefinition.getUniqueId());
        updatedTypeDefinition.setCreationTime(currTypeDefinition.getCreationTime());
        updatedTypeDefinition.setModificationTime(System.currentTimeMillis());
    }

}
