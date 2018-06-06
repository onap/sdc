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

import static org.openecomp.sdc.be.dao.titan.TitanUtils.buildNotInPredicate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.GroupTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;
import org.openecomp.sdc.be.resources.data.GroupTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.thinkaurelius.titan.graphdb.query.TitanPredicate;

import fj.data.Either;

@Component("group-type-operation")
public class GroupTypeOperation extends AbstractOperation implements IGroupTypeOperation {
    @Resource
    private CapabilityTypeOperation capabilityTypeOperation;

    private static final Logger log = LoggerFactory.getLogger(GroupTypeOperation.class);

    private static final String CREATE_FLOW_CONTEXT = "CreateGroupType";
    private static final String GET_FLOW_CONTEXT = "GetGroupType";

    private PropertyOperation propertyOperation;

    private TitanGenericDao titanGenericDao;

    public GroupTypeOperation(@Qualifier("titan-generic-dao") TitanGenericDao titanGenericDao, @Qualifier("property-operation") PropertyOperation propertyOperation) {
        super();
        this.propertyOperation = propertyOperation;
        this.titanGenericDao = titanGenericDao;
    }

    /**
     * FOR TEST ONLY
     *
     * @param titanGenericDao
     */
    public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
        this.titanGenericDao = titanGenericDao;
    }

    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> addGroupType(GroupTypeDefinition groupTypeDefinition) {

        return addGroupType(groupTypeDefinition, false);
    }

    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> addGroupType(GroupTypeDefinition groupTypeDefinition, boolean inTransaction) {

        Either<GroupTypeDefinition, StorageOperationStatus> result = null;

        try {

            Either<GroupTypeData, TitanOperationStatus> eitherStatus = addGroupTypeToGraph(groupTypeDefinition);

            if (eitherStatus.isRight()) {
                BeEcompErrorManager.getInstance().logBeFailedCreateNodeError(CREATE_FLOW_CONTEXT, groupTypeDefinition.getType(), eitherStatus.right().value().name());
                result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherStatus.right().value()));

            } else {
                GroupTypeData groupTypeData = eitherStatus.left().value();

                String uniqueId = groupTypeData.getUniqueId();
                Either<GroupTypeDefinition, StorageOperationStatus> groupTypeRes = this.getGroupType(uniqueId, true);

                if (groupTypeRes.isRight()) {
                    BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError(GET_FLOW_CONTEXT, groupTypeDefinition.getType(), eitherStatus.right().value().name());
                } else {
                    List<CapabilityTypeDefinition> groupCapTypes = groupTypeDefinition.getCapabilityTypes();
                    if (!CollectionUtils.isEmpty(groupCapTypes)) {
                        Optional<TitanOperationStatus> firstFailure = connectToCapabilityType(groupTypeData, groupCapTypes);
                        if (firstFailure.isPresent()) {
                            groupTypeRes = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(firstFailure.get()));
                        }
                    }
                }

                result = groupTypeRes;

            }

            return result;

        } finally {
            handleTransactionCommitRollback(inTransaction, result);
        }

    }


    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> upgradeGroupType(GroupTypeDefinition groupTypeDefinitionNew, GroupTypeDefinition groupTypeDefinitionOld) {
        return upgradeGroupType(groupTypeDefinitionOld, groupTypeDefinitionNew, false);
    }

    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> upgradeGroupType(GroupTypeDefinition groupTypeDefinitionNew, GroupTypeDefinition groupTypeDefinitionOld, boolean inTransaction) {
        Either<GroupTypeDefinition, StorageOperationStatus> result = Either.left(groupTypeDefinitionNew);

        try {
            // dr2032:
            // Right now upgrade Group is used only to ensure that already existing group type is connected by DERRIVED_FROM edge with it's parent
            // We don't need to use for a while new node definition since following group type upgrade is not supported.
            if (!Strings.isNullOrEmpty(groupTypeDefinitionOld.getDerivedFrom())) {
                result = ensureExsitanceDerivedFromEdge(groupTypeDefinitionOld);
            }
        } finally {
            handleTransactionCommitRollback(inTransaction, result);
        }

        return result;
    }

    private Optional<TitanOperationStatus> connectToCapabilityType(GroupTypeData groupTypeData, List<CapabilityTypeDefinition> groupCapTypes) {
        return groupCapTypes.stream()
                .map(groupCapTypeDef -> connectTo(groupTypeData, groupCapTypeDef))
                .filter(Either::isRight)
                .findFirst()
                .map(either -> either.right().value());
    }

    private Either<GraphRelation, TitanOperationStatus> connectTo(GroupTypeData groupTypeData, CapabilityTypeDefinition groupCapTypeDef) {
        Either<CapabilityTypeData, TitanOperationStatus> eitherCapData = capabilityTypeOperation.getCapabilityTypeByType(groupCapTypeDef.getType());
        if (eitherCapData.isLeft()) {
            return titanGenericDao.createRelation(groupTypeData, eitherCapData.left().value(), GraphEdgeLabels.GROUP_TYPE_CAPABILITY_TYPE, null);
        }

        return Either.right(eitherCapData.right().value());
    }

    public List<GroupTypeDefinition> getAllGroupTypes(Set<String> excludedGroupTypes) {
        Map<String, Map.Entry<TitanPredicate, Object>> predicateCriteria = buildNotInPredicate(GraphPropertiesDictionary.TYPE.getProperty(), excludedGroupTypes);
        List<GroupTypeData> groupTypes = titanGenericDao.getByCriteriaWithPredicate(NodeTypeEnum.GroupType, predicateCriteria, GroupTypeData.class)
                .left()
                .on(this::onTitanAccessError);

        return convertGroupTypesToDefinition(groupTypes);
    }


    private List<GroupTypeDefinition> convertGroupTypesToDefinition(List<GroupTypeData> groupTypes) {
        return groupTypes.stream()
                .map(type -> new GroupTypeDefinition(type.getGroupTypeDataDefinition()))
                .collect(Collectors.toList());
    }

    private List<GroupTypeData> onTitanAccessError(TitanOperationStatus toe) {
        throw new StorageException(
                DaoStatusConverter.convertTitanStatusToStorageStatus(toe));
    }


    public Either<GroupTypeDefinition, TitanOperationStatus> getGroupTypeByUid(String uniqueId) {

        Either<GroupTypeDefinition, TitanOperationStatus> result = null;

        Either<GroupTypeData, TitanOperationStatus> groupTypesRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.GroupType), uniqueId, GroupTypeData.class);

        if (groupTypesRes.isRight()) {
            TitanOperationStatus status = groupTypesRes.right().value();
            log.debug("Group type {} cannot be found in graph. status is {}", uniqueId, status);
            return Either.right(status);
        }

        GroupTypeData gtData = groupTypesRes.left().value();
        GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition(gtData.getGroupTypeDataDefinition());

        TitanOperationStatus propertiesStatus = propertyOperation.fillProperties(uniqueId, NodeTypeEnum.GroupType, properList -> groupTypeDefinition.setProperties(properList));

        if (propertiesStatus != TitanOperationStatus.OK) {
            log.error("Failed to fetch properties of capability type {}", uniqueId);
            return Either.right(propertiesStatus);
        }

        result = Either.left(groupTypeDefinition);

        return result;
    }

    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> getGroupType(String uniqueId) {

        return getGroupType(uniqueId, false);

    }

    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> getGroupType(String uniqueId, boolean inTransaction) {
        return getElementType(this::getGroupTypeByUid, uniqueId, inTransaction);
    }

    @Override
    public Either<GroupTypeDefinition, StorageOperationStatus> getLatestGroupTypeByType(String type) {
        return getLatestGroupTypeByType(type, false);
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

            Either<List<GroupTypeData>, TitanOperationStatus> groupTypeEither = titanGenericDao.getByCriteria(NodeTypeEnum.GroupType, properties, GroupTypeData.class);
            if (groupTypeEither.isRight()) {
                result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(groupTypeEither.right().value()));
            } else {
                GroupTypeDataDefinition dataDefinition = groupTypeEither.left().value().stream().map(e -> e.getGroupTypeDataDefinition()).findFirst().get();
                result = getGroupType(dataDefinition.getUniqueId(), inTransaction);
            }

            return result;

        } finally {
            handleTransactionCommitRollback(inTransaction, result);
        }
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
    private Either<GroupTypeData, TitanOperationStatus> addGroupTypeToGraph(GroupTypeDefinition groupTypeDefinition) {

        log.debug("Got group type {}", groupTypeDefinition);

        String ctUniqueId = UniqueIdBuilder.buildGroupTypeUid(groupTypeDefinition.getType(), groupTypeDefinition.getVersion());

        GroupTypeData groupTypeData = buildGroupTypeData(groupTypeDefinition, ctUniqueId);

        log.debug("Before adding group type to graph. groupTypeData = {}", groupTypeData);

        Either<GroupTypeData, TitanOperationStatus> createGTResult = titanGenericDao.createNode(groupTypeData, GroupTypeData.class);
        log.debug("After adding group type to graph. status is = {}", createGTResult);

        if (createGTResult.isRight()) {
            TitanOperationStatus operationStatus = createGTResult.right().value();
            log.error("Failed to add group type {} to graph. status is {}", groupTypeDefinition.getType(), operationStatus);
            return Either.right(operationStatus);
        }

        GroupTypeData resultCTD = createGTResult.left().value();
        List<PropertyDefinition> properties = groupTypeDefinition.getProperties();
        Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesToCapablityType = propertyOperation.addPropertiesToElementType(resultCTD.getUniqueId(), NodeTypeEnum.GroupType, properties);
        if (addPropertiesToCapablityType.isRight()) {
            log.error("Failed add properties {} to capability {}", properties, groupTypeDefinition.getType());
            return Either.right(addPropertiesToCapablityType.right().value());
        }

        String derivedFrom = groupTypeDefinition.getDerivedFrom();
        if (derivedFrom != null) {
            Either<GraphRelation, TitanOperationStatus> createRelation = connectToDerivedFrom(ctUniqueId, derivedFrom);
            if (createRelation.isRight()) {
                return Either.right(createRelation.right().value());
            }
        }

        return Either.left(createGTResult.left().value());

    }


    private Either<GraphRelation, TitanOperationStatus> connectToDerivedFrom(String ctUniqueId, String derivedFrom) {
        log.debug("Before creating relation between Group Type with id {} to its parent {}", ctUniqueId, derivedFrom);

        Either<GroupTypeData, TitanOperationStatus> derivedFromGroupTypeResult =
                titanGenericDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), derivedFrom, GroupTypeData.class);

        if (derivedFromGroupTypeResult.isLeft()) {
            UniqueIdData from = new UniqueIdData(NodeTypeEnum.GroupType, ctUniqueId);
            GroupTypeData to = derivedFromGroupTypeResult.left().value();

            Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(from, to, GraphEdgeLabels.DERIVED_FROM, null);
            log.debug("After create relation between Group Type with id {} to its parent {}, status is {}.", ctUniqueId, derivedFrom, createRelation);
            return createRelation;
        } else {
            TitanOperationStatus status = derivedFromGroupTypeResult.right().value();
            log.debug("Failed to found parent Group Type {}, stauts is {}.", derivedFrom, status);
            return Either.right(status);
        }
    }


    private Either<GroupTypeDefinition, StorageOperationStatus> ensureExsitanceDerivedFromEdge(GroupTypeDefinition groupTypeDefinition) {
        Either<GroupTypeDefinition, StorageOperationStatus> result = Either.left(groupTypeDefinition);

        GroupTypeData childGroupType = null;
        GroupTypeData parentGroupType = null;

        Either<GroupTypeData, TitanOperationStatus> childGroupTypeResult =
                titanGenericDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), groupTypeDefinition.getType(), GroupTypeData.class);
        if (childGroupTypeResult.isRight()) {
            result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(childGroupTypeResult.right().value()));
            log.debug("Filed to find GroupType with type {}, status is {}.", groupTypeDefinition.getType(), childGroupTypeResult);
        } else {
            childGroupType = childGroupTypeResult.left().value();
        }


        if (result.isLeft()) {
            Either<GroupTypeData, TitanOperationStatus> parentGroupTypeResult =
                    titanGenericDao.getNode(GraphPropertiesDictionary.TYPE.getProperty(), groupTypeDefinition.getDerivedFrom(), GroupTypeData.class);
            if (parentGroupTypeResult.isRight()) {
                result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(parentGroupTypeResult.right().value()));
                log.debug("Filed to find GroupType with type {}, status is {}.", groupTypeDefinition.getDerivedFrom(), parentGroupTypeResult);
            } else {
                parentGroupType = parentGroupTypeResult.left().value();
            }
        }


        if (childGroupType != null && parentGroupType != null) {
            Either<Edge, TitanOperationStatus> edgeDerivedFromResult = titanGenericDao.getEdgeByNodes(childGroupType, parentGroupType, GraphEdgeLabels.DERIVED_FROM);
            if (edgeDerivedFromResult.isLeft()) {
                log.debug("It was found relation {}. Don't need to create the edge.", edgeDerivedFromResult.left().value());
            } else {
                Either<GraphRelation, TitanOperationStatus> createRelationResult = titanGenericDao.createRelation(childGroupType, parentGroupType, GraphEdgeLabels.DERIVED_FROM, null);
                log.debug("After create relation between Group Type with id {} to its parent with id {}, status is {}.",
                        childGroupType.getKeyValueId().getValue(), parentGroupType.getKeyValueId().getValue(), createRelationResult);
                if (createRelationResult.isRight()) {
                    result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(createRelationResult.right().value()));
                }
            }

        }


        return result;
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
        Map<String, Object> propertiesToMatch = new HashMap<String, Object>();
        propertiesToMatch.put(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), childCandidateType);
        Either<List<CapabilityTypeData>, TitanOperationStatus> getResponse = titanGenericDao.getByCriteria(NodeTypeEnum.CapabilityType, propertiesToMatch, CapabilityTypeData.class);
        if (getResponse.isRight()) {
            TitanOperationStatus titanOperationStatus = getResponse.right().value();
            log.debug("Couldn't fetch capability type {}, error: {}", childCandidateType, titanOperationStatus);
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(titanOperationStatus));
        }
        String childUniqueId = getResponse.left().value().get(0).getUniqueId();
        Set<String> travelledTypes = new HashSet<>();
        do {
            travelledTypes.add(childUniqueId);
            Either<List<ImmutablePair<CapabilityTypeData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), childUniqueId, GraphEdgeLabels.DERIVED_FROM,
                    NodeTypeEnum.CapabilityType, CapabilityTypeData.class);
            if (childrenNodes.isRight()) {
                if (childrenNodes.right().value() != TitanOperationStatus.NOT_FOUND) {
                    TitanOperationStatus titanOperationStatus = getResponse.right().value();
                    log.debug("Couldn't fetch derived from node for capability type {}, error: {}", childCandidateType, titanOperationStatus);
                    return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(titanOperationStatus));
                } else {
                    log.debug("Derived from node is not found for type {} - this is OK for root capability.");
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
     * FOR TEST ONLY
     *
     * @param propertyOperation
     */
    public void setPropertyOperation(PropertyOperation propertyOperation) {
        this.propertyOperation = propertyOperation;
    }

    @Override
    public Either<GroupTypeData, TitanOperationStatus> getLatestGroupTypeByNameFromGraph(String name) {

        return null;
    }

}
