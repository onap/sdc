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

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;

import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.GroupData;
import org.openecomp.sdc.be.resources.data.GroupTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.log.wrappers.Logger;

@org.springframework.stereotype.Component
public class GroupOperation extends AbstractOperation implements IGroupOperation {

    private static final Logger log = Logger.getLogger(GroupOperation.class.getName());
    private final JanusGraphDao janusGraphDao;
    private final TopologyTemplateOperation topologyTemplateOperation;
    private final PropertyOperation propertyOperation;
    private final GroupTypeOperation groupTypeOperation;
    private final ApplicationDataTypeCache applicationDataTypeCache;

    public GroupOperation(JanusGraphDao janusGraphDao, TopologyTemplateOperation topologyTemplateOperation, PropertyOperation propertyOperation,
                          GroupTypeOperation groupTypeOperation, ApplicationDataTypeCache applicationDataTypeCache) {
        this.janusGraphDao = janusGraphDao;
        this.topologyTemplateOperation = topologyTemplateOperation;
        this.propertyOperation = propertyOperation;
        this.groupTypeOperation = groupTypeOperation;
        this.applicationDataTypeCache = applicationDataTypeCache;
    }

    private GroupDefinition convertGroupDataToGroupDefinition(GroupData groupData) {
        return new GroupDefinition(groupData.getGroupDataDefinition());
    }

    /**
     * get members of group
     *
     * @param groupUniqueId
     * @return
     */
    private Either<Map<String, String>, JanusGraphOperationStatus> getGroupMembers(String groupUniqueId) {
        Either<Map<String, String>, JanusGraphOperationStatus> result = null;
        Either<List<ImmutablePair<ComponentInstanceData, GraphEdge>>, JanusGraphOperationStatus> childrenNodes = janusGraphGenericDao
            .getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), groupUniqueId, GraphEdgeLabels.GROUP_MEMBER,
                NodeTypeEnum.ResourceInstance, ComponentInstanceData.class);
        if (childrenNodes.isRight()) {
            JanusGraphOperationStatus status = childrenNodes.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.OK;
            }
            result = Either.right(status);
        } else {
            Map<String, String> compInstaMap = new HashMap<>();
            List<ImmutablePair<ComponentInstanceData, GraphEdge>> list = childrenNodes.left().value();
            if (list != null) {
                for (ImmutablePair<ComponentInstanceData, GraphEdge> pair : list) {
                    ComponentInstanceData componentInstanceData = pair.getKey();
                    String compInstUniqueId = componentInstanceData.getComponentInstDataDefinition().getUniqueId();
                    String compInstName = componentInstanceData.getName();
                    compInstaMap.put(compInstName, compInstUniqueId);
                }
            }
            result = Either.left(compInstaMap);
        }
        return result;
    }

    private Either<GroupTypeDefinition, StorageOperationStatus> getGroupTypeOfGroup(String groupUniqueId) {
        Either<ImmutablePair<GroupTypeData, GraphEdge>, StorageOperationStatus> groupTypeRes = janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), groupUniqueId, GraphEdgeLabels.TYPE_OF, NodeTypeEnum.GroupType,
                GroupTypeData.class).right().map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
        if (groupTypeRes.isRight()) {
            StorageOperationStatus status = groupTypeRes.right().value();
            log.debug("Cannot find group type associated with capability {}. Status is {}", groupUniqueId, status);
            BeEcompErrorManager.getInstance()
                .logBeFailedFindAssociationError("Fetch Group type", NodeTypeEnum.GroupType.getName(), groupUniqueId, String.valueOf(status));
            return Either.right(groupTypeRes.right().value());
        }
        GroupTypeData groupTypeData = groupTypeRes.left().value().getKey();
        return groupTypeOperation.getGroupTypeByUid(groupTypeData.getGroupTypeDataDefinition().getUniqueId());
    }

    /**
     * get all properties of the group.
     * <p>
     * the propert definition is taken from the group type.
     *
     * @param groupUid
     * @return
     */
    private Either<List<GroupProperty>, StorageOperationStatus> getGroupProperties(String groupUid) {
        List<GroupProperty> groupPropertiesList = new ArrayList<>();
        Either<GroupTypeDefinition, StorageOperationStatus> groupTypeOfGroupRes = getGroupTypeOfGroup(groupUid);
        if (groupTypeOfGroupRes.isRight()) {
            StorageOperationStatus status = groupTypeOfGroupRes.right().value();
            return Either.right(status);
        }
        GroupTypeDefinition groupTypeDefinition = groupTypeOfGroupRes.left().value();
        // Get the properties on the group type of this group
        List<PropertyDefinition> groupTypeProperties = groupTypeDefinition.getProperties();
        if (isEmpty(groupTypeProperties)) {
            return Either.right(StorageOperationStatus.OK);
        }
        Map<String, PropertyDefinition> uidToPropDefMap = groupTypeProperties.stream()
            .collect(Collectors.toMap(PropertyDefinition::getUniqueId, Function.identity()));
        // Find all properties values on the group
        Either<List<ImmutablePair<PropertyValueData, GraphEdge>>, StorageOperationStatus> propertyImplNodes = janusGraphGenericDao
            .getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), groupUid, GraphEdgeLabels.PROPERTY_VALUE,
                NodeTypeEnum.PropertyValue, PropertyValueData.class).right().map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
        if (propertyImplNodes.isRight()) {
            StorageOperationStatus status = propertyImplNodes.right().value();
            if (status == StorageOperationStatus.NOT_FOUND) {
                groupPropertiesList = groupTypeProperties.stream().map(p -> new GroupProperty(p, p.getDefaultValue(), null))
                    .collect(Collectors.toList());
                return Either.left(groupPropertiesList);
            } else {
                return Either.right(status);
            }
        }
        List<ImmutablePair<PropertyValueData, GraphEdge>> list = propertyImplNodes.left().value();
        if (isEmpty(list)) {
            return Either.right(StorageOperationStatus.OK);
        }
        List<String> processedProps = new ArrayList<>();
        for (ImmutablePair<PropertyValueData, GraphEdge> propertyValue : list) {
            PropertyValueData propertyValueData = propertyValue.getLeft();
            String propertyValueUid = propertyValueData.getUniqueId();
            String value = propertyValueData.getValue();
            Either<ImmutablePair<PropertyData, GraphEdge>, StorageOperationStatus> propertyDefRes = janusGraphGenericDao
                .getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.PropertyValue), propertyValueUid, GraphEdgeLabels.PROPERTY_IMPL,
                    NodeTypeEnum.Property, PropertyData.class).right().map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
            if (propertyDefRes.isRight()) {
                StorageOperationStatus status = propertyDefRes.right().value();
                if (status == StorageOperationStatus.NOT_FOUND) {
                    status = StorageOperationStatus.INVALID_ID;
                }
                return Either.right(status);
            }
            ImmutablePair<PropertyData, GraphEdge> propertyDefPair = propertyDefRes.left().value();
            PropertyData propertyData = propertyDefPair.left;
            String propertyUniqueId = propertyData.getPropertyDataDefinition().getUniqueId();
            PropertyDefinition propertyDefinition = uidToPropDefMap.get(propertyUniqueId);
            GroupProperty groupProperty = new GroupProperty(propertyDefinition, value, propertyValueUid);
            processedProps.add(propertyUniqueId);
            groupPropertiesList.add(groupProperty);
        }
        // Find all properties which does not have property value on the group.
        List<GroupProperty> leftProps = groupTypeProperties.stream()
            // filter out the group type properties which already processed
            .filter(p -> !processedProps.contains(p.getUniqueId())).map(p -> new GroupProperty(p, p.getDefaultValue(), null))
            .collect(Collectors.toList());
        if (leftProps != null) {
            groupPropertiesList.addAll(leftProps);
        }
        return Either.left(groupPropertiesList);
    }

    public Either<List<GraphRelation>, StorageOperationStatus> dissociateAllGroupsFromArtifactOnGraph(String componentId,
                                                                                                      NodeTypeEnum componentTypeEnum,
                                                                                                      String artifactId) {
        List<GraphRelation> relations = new ArrayList<>();
        Either<List<GraphRelation>, StorageOperationStatus> result = Either.left(relations);
        Either<List<GroupDefinition>, StorageOperationStatus> allGroupsFromGraph = getAllGroupsFromGraph(componentId, componentTypeEnum, true, true,
            false);
        if (allGroupsFromGraph.isRight()) {
            StorageOperationStatus status = allGroupsFromGraph.right().value();
            return Either.right(status);
        }
        List<GroupDefinition> allGroups = allGroupsFromGraph.left().value();
        if (isEmpty(allGroups)) {
            return Either.right(StorageOperationStatus.OK);
        }
        // Find all groups which contains this artifact id
        List<GroupDefinition> associatedGroups = allGroups.stream().filter(p -> p.getArtifacts() != null && p.getArtifacts().contains(artifactId))
            .collect(Collectors.toList());
        if (isNotEmpty(associatedGroups)) {
            log.debug("The groups {} contains the artifact {}",
                associatedGroups.stream().map(GroupDataDefinition::getName).collect(Collectors.toList()), artifactId);
            UniqueIdData artifactData = new UniqueIdData(NodeTypeEnum.ArtifactRef, artifactId);
            for (GroupDefinition groupDefinition : associatedGroups) {
                UniqueIdData groupData = new UniqueIdData(NodeTypeEnum.Group, groupDefinition.getUniqueId());
                Either<GraphRelation, StorageOperationStatus> deleteRelation = janusGraphGenericDao
                    .deleteRelation(groupData, artifactData, GraphEdgeLabels.GROUP_ARTIFACT_REF).right()
                    .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
                if (deleteRelation.isRight()) {
                    StorageOperationStatus status = deleteRelation.right().value();
                    if (status == StorageOperationStatus.NOT_FOUND) {
                        status = StorageOperationStatus.INVALID_ID;
                    }
                    return Either.right(status);
                }
                relations.add(deleteRelation.left().value());
            }
            return result;
        } else {
            log.debug("No group under component id {} is associated to artifact {}", componentId, artifactId);
            return Either.right(StorageOperationStatus.OK);
        }
    }

    public Either<GroupDefinition, StorageOperationStatus> getGroupFromGraph(String uniqueId, boolean skipProperties, boolean skipMembers,
                                                                             boolean skipArtifacts) {
        Either<GroupDefinition, StorageOperationStatus> result = null;
        Either<GroupData, StorageOperationStatus> groupRes = janusGraphGenericDao
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), uniqueId, GroupData.class).right()
            .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
        if (groupRes.isRight()) {
            StorageOperationStatus status = groupRes.right().value();
            log.debug("Failed to retrieve group {}  from graph. Status is {}", uniqueId, status);
            BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("Fetch Group", uniqueId, String.valueOf(status));
            result = Either.right(status);
            return result;
        }
        GroupData groupData = groupRes.left().value();
        GroupDefinition groupDefinition = convertGroupDataToGroupDefinition(groupData);
        Either<GroupTypeDefinition, StorageOperationStatus> groupTypeOfGroup = getGroupTypeOfGroup(uniqueId);
        if (groupTypeOfGroup.isRight()) {
            StorageOperationStatus status = groupTypeOfGroup.right().value();
            log.debug("Failed to retrieve capability type of capability {}. Status is {}", uniqueId, status);
            result = Either.right(status);
            return result;
        }
        GroupTypeDefinition groupTypeDefinition = groupTypeOfGroup.left().value();
        groupDefinition.setTypeUid(groupTypeDefinition.getUniqueId());
        if (!skipMembers) {
            Either<Map<String, String>, StorageOperationStatus> membersRes = getGroupMembers(uniqueId).right()
                .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
            if (membersRes.isRight()) {
                StorageOperationStatus status = membersRes.right().value();
                if (status != StorageOperationStatus.OK) {
                    result = Either.right(status);
                    return result;
                }
            } else {
                Map<String, String> members = membersRes.left().value();
                groupDefinition.setMembers(members);
            }
        }
        if (!skipProperties) {
            Either<List<GroupProperty>, StorageOperationStatus> propertiesRes = getGroupProperties(uniqueId);
            if (propertiesRes.isRight()) {
                StorageOperationStatus status = propertiesRes.right().value();
                if (status != StorageOperationStatus.OK) {
                    result = Either.right(status);
                    return result;
                }
            } else {
                List<GroupProperty> properties = propertiesRes.left().value();
                groupDefinition.convertFromGroupProperties(properties);
            }
        }
        if (!skipArtifacts) {
            Either<List<ImmutablePair<String, String>>, StorageOperationStatus> artifactsRes = getGroupArtifactsPairs(uniqueId).right()
                .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
            if (artifactsRes.isRight()) {
                StorageOperationStatus status = artifactsRes.right().value();
                if (status != StorageOperationStatus.OK) {
                    result = Either.right(status);
                    return result;
                }
            } else {
                List<String> artifactsUid = new ArrayList<>();
                List<String> artifactsUUID = new ArrayList<>();
                List<ImmutablePair<String, String>> list = artifactsRes.left().value();
                if (list != null) {
                    for (ImmutablePair<String, String> pair : list) {
                        String uid = pair.left;
                        String UUID = pair.right;
                        artifactsUid.add(uid);
                        artifactsUUID.add(UUID);
                    }
                    groupDefinition.setArtifacts(artifactsUid);
                    groupDefinition.setArtifactsUuid(artifactsUUID);
                }
            }
        }
        result = Either.left(groupDefinition);
        return result;
    }

    public boolean isGroupExist(String groupName, boolean inTransaction) {
        Either<List<GroupData>, JanusGraphOperationStatus> eitherGroup = null;
        try {
            Map<String, Object> properties = new HashMap<>();
            properties.put(GraphPropertiesDictionary.NAME.getProperty(), groupName);
            eitherGroup = janusGraphGenericDao.getByCriteria(NodeTypeEnum.Group, properties, GroupData.class);
            return eitherGroup.isLeft() && !eitherGroup.left().value().isEmpty();
        } finally {
            handleTransactionCommitRollback(inTransaction, eitherGroup);
        }
    }

    protected Either<List<GroupDefinition>, StorageOperationStatus> getAllGroupsFromGraph(String componentId, NodeTypeEnum componentTypeEnum,
                                                                                          boolean skipProperties, boolean skipMembers,
                                                                                          boolean skipArtifacts) {
        List<GroupDefinition> groups = new ArrayList<>();
        Either<List<ImmutablePair<GroupData, GraphEdge>>, StorageOperationStatus> childrenNodes = janusGraphGenericDao
            .getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(componentTypeEnum), componentId, GraphEdgeLabels.GROUP, NodeTypeEnum.Group,
                GroupData.class).right().map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
        if (childrenNodes.isRight()) {
            StorageOperationStatus status = childrenNodes.right().value();
            return Either.right(status);
        }
        List<ImmutablePair<GroupData, GraphEdge>> graphGroups = childrenNodes.left().value();
        if (isEmpty(graphGroups)) {
            return Either.right(StorageOperationStatus.NOT_FOUND);
        }
        for (ImmutablePair<GroupData, GraphEdge> pair : graphGroups) {
            String groupUniqueId = pair.left.getGroupDataDefinition().getUniqueId();
            Either<GroupDefinition, StorageOperationStatus> groupRes = this
                .getGroupFromGraph(groupUniqueId, skipProperties, skipMembers, skipArtifacts);
            if (groupRes.isRight()) {
                StorageOperationStatus status = groupRes.right().value();
                if (status == StorageOperationStatus.NOT_FOUND) {
                    status = StorageOperationStatus.INVALID_ID;
                }
                return Either.right(status);
            } else {
                groups.add(groupRes.left().value());
            }
        }
        return Either.left(groups);
    }

    private StorageOperationStatus dissociateAndAssociateGroupsFromArtifactOnGraph(String componentId, NodeTypeEnum componentTypeEnum,
                                                                                   String oldArtifactId, ArtifactData newArtifact) {
        Either<List<GroupDefinition>, StorageOperationStatus> allGroupsFromGraph = getAllGroupsFromGraph(componentId, componentTypeEnum, true, true,
            false);
        if (allGroupsFromGraph.isRight()) {
            return allGroupsFromGraph.right().value();
        }
        List<GroupDefinition> allGroups = allGroupsFromGraph.left().value();
        if (isEmpty(allGroups)) {
            return StorageOperationStatus.OK;
        }
        // Find all groups which contains this artifact id
        List<GroupDefinition> associatedGroups = allGroups.stream().filter(p -> p.getArtifacts() != null && p.getArtifacts().contains(oldArtifactId))
            .collect(Collectors.toList());
        if (isNotEmpty(associatedGroups)) {
            log.debug("The groups {} contains the artifact {}",
                associatedGroups.stream().map(GroupDataDefinition::getName).collect(Collectors.toList()), oldArtifactId);
            UniqueIdData oldArtifactData = new UniqueIdData(NodeTypeEnum.ArtifactRef, oldArtifactId);
            UniqueIdData newArtifactData = new UniqueIdData(NodeTypeEnum.ArtifactRef, newArtifact.getArtifactDataDefinition().getUniqueId());
            Map<String, Object> props = new HashMap<>();
            props.put(GraphPropertiesDictionary.NAME.getProperty(), newArtifactData.getLabel());
            for (GroupDefinition groupDefinition : associatedGroups) {
                UniqueIdData groupData = new UniqueIdData(NodeTypeEnum.Group, groupDefinition.getUniqueId());
                Either<GraphRelation, StorageOperationStatus> deleteRelation = janusGraphGenericDao
                    .deleteRelation(groupData, oldArtifactData, GraphEdgeLabels.GROUP_ARTIFACT_REF).right()
                    .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
                log.trace("After dissociate group {} from artifact {}", groupDefinition.getName(), oldArtifactId);
                if (deleteRelation.isRight()) {
                    StorageOperationStatus status = deleteRelation.right().value();
                    if (status == StorageOperationStatus.NOT_FOUND) {
                        status = StorageOperationStatus.INVALID_ID;
                    }
                    return status;
                }
                Either<GraphRelation, StorageOperationStatus> createRelation = janusGraphGenericDao
                    .createRelation(groupData, newArtifactData, GraphEdgeLabels.GROUP_ARTIFACT_REF, props).right()
                    .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
                log.trace("After associate group {} to artifact {}", groupDefinition.getName(), newArtifact.getUniqueIdKey());
                if (createRelation.isRight()) {
                    StorageOperationStatus status = createRelation.right().value();
                    if (status == StorageOperationStatus.NOT_FOUND) {
                        status = StorageOperationStatus.INVALID_ID;
                    }
                    return status;
                }
            }
        }
        return StorageOperationStatus.OK;
    }

    public StorageOperationStatus dissociateAndAssociateGroupsFromArtifact(String componentId, NodeTypeEnum componentTypeEnum, String oldArtifactId,
                                                                           ArtifactData newArtifact, boolean inTransaction) {
        StorageOperationStatus result = null;
        try {
            StorageOperationStatus status = this
                .dissociateAndAssociateGroupsFromArtifactOnGraph(componentId, componentTypeEnum, oldArtifactId, newArtifact);
            if (status != StorageOperationStatus.OK && status != StorageOperationStatus.NOT_FOUND) {
                return status;
            }
            result = StorageOperationStatus.OK;
            return result;
        } finally {
            if (!inTransaction) {
                if (result == null || result != StorageOperationStatus.OK) {
                    log.debug("Going to execute rollback on graph.");
                    BeEcompErrorManager.getInstance().logBeExecuteRollbackError("Rollback on graph");
                    janusGraphGenericDao.rollback();
                } else {
                    log.debug("Going to execute commit on graph.");
                    janusGraphGenericDao.commit();
                }
            }
        }
    }

    private Either<List<ImmutablePair<String, String>>, JanusGraphOperationStatus> getGroupArtifactsPairs(String groupUniqueId) {
        Either<List<ImmutablePair<String, String>>, JanusGraphOperationStatus> result = null;
        Either<List<ImmutablePair<ArtifactData, GraphEdge>>, JanusGraphOperationStatus> childrenNodes = janusGraphGenericDao
            .getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), groupUniqueId, GraphEdgeLabels.GROUP_ARTIFACT_REF,
                NodeTypeEnum.ArtifactRef, ArtifactData.class);
        if (childrenNodes.isRight()) {
            JanusGraphOperationStatus status = childrenNodes.right().value();
            if (status == JanusGraphOperationStatus.NOT_FOUND) {
                status = JanusGraphOperationStatus.OK;
            }
            result = Either.right(status);
        } else {
            List<ImmutablePair<String, String>> artifactsList = new ArrayList<>();
            List<ImmutablePair<ArtifactData, GraphEdge>> list = childrenNodes.left().value();
            if (list != null) {
                for (ImmutablePair<ArtifactData, GraphEdge> pair : list) {
                    ArtifactData artifactData = pair.getKey();
                    String uniqueId = artifactData.getArtifactDataDefinition().getUniqueId();
                    String UUID = artifactData.getArtifactDataDefinition().getArtifactUUID();
                    ImmutablePair<String, String> artifact = new ImmutablePair<>(uniqueId, UUID);
                    artifactsList.add(artifact);
                }
            }
            log.debug("The artifacts list related to group {} is {}", groupUniqueId, artifactsList);
            result = Either.left(artifactsList);
        }
        return result;
    }

    public StorageOperationStatus validateAndUpdatePropertyValue(final Component groupOwner, final GroupProperty property) {
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = applicationDataTypeCache.getAll(property.getModel());
        if (allDataTypes.isRight()) {
            JanusGraphOperationStatus status = allDataTypes.right().value();
            log.debug("Failed to fetch data types from cache. Status is {}. ", status);
            return DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status);
        }

        Either<Object, Boolean> isValid = propertyOperation.validateAndUpdatePropertyValue(groupOwner, property,  allDataTypes.left().value());
        if (isValid.isRight()) {
            log.debug("Failed to validate property value {}. Status is {}. ", property.getValue(), StorageOperationStatus.INVALID_PROPERTY);
            return StorageOperationStatus.INVALID_PROPERTY;
        }
        return StorageOperationStatus.OK;
    }

    public StorageOperationStatus updateGroupProperties(org.openecomp.sdc.be.model.Component containerComponent, String groupId,
                                                        List<PropertyDataDefinition> propertiesToUpdate) {
        log.debug("#updateGroupProperties - updating the properties of group {} in component {}", groupId, containerComponent.getUniqueId());
        Optional<GroupDefinition> group = containerComponent.getGroupById(groupId);
        if (group.isPresent()) {
            return janusGraphDao.getVertexById(containerComponent.getUniqueId(), JsonParseFlagEnum.NoParse)
                .either(containerVertex -> updateGroupProperties(containerVertex, group.get(), propertiesToUpdate),
                    DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
        } else {
            throw new StorageException(StorageOperationStatus.NOT_FOUND, groupId);
        }
    }

    private StorageOperationStatus updateGroupProperties(GraphVertex container, GroupDefinition group,
                                                         List<PropertyDataDefinition> propertiesToUpdate) {
        List<PropertyDataDefinition> groupProperties = group.getProperties();
        List<PropertyDataDefinition> updatedGroupProperties = updateGroupProperties(groupProperties, propertiesToUpdate);
        group.setProperties(updatedGroupProperties);
        return topologyTemplateOperation.updateGroupOfToscaElement(container, group);
    }

    private List<PropertyDataDefinition> updateGroupProperties(List<PropertyDataDefinition> currentGroupProperties,
                                                               List<PropertyDataDefinition> toBeUpdatedProperties) {
        Map<String, PropertyDataDefinition> currPropsByName = MapUtil.toMap(currentGroupProperties, PropertyDataDefinition::getName);
        overrideCurrentPropertiesWithUpdatedProperties(currPropsByName, toBeUpdatedProperties);
        return new ArrayList<>(currPropsByName.values());
    }

    private void overrideCurrentPropertiesWithUpdatedProperties(Map<String, PropertyDataDefinition> currPropsByName,
                                                                List<PropertyDataDefinition> toBeUpdatedProperties) {
        toBeUpdatedProperties.forEach(prop -> currPropsByName.put(prop.getName(), prop));
    }
}
