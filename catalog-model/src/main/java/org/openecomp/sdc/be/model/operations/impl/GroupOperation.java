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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.GroupData;
import org.openecomp.sdc.be.resources.data.GroupTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("group-operation")
public class GroupOperation extends AbstractOperation implements IGroupOperation {


	private static Logger log = LoggerFactory.getLogger(GroupOperation.class.getName());

	@javax.annotation.Resource
	private PropertyOperation propertyOperation;

	@javax.annotation.Resource
	private GroupTypeOperation groupTypeOperation;

	@javax.annotation.Resource
	private ApplicationDataTypeCache dataTypeCache;

	private GroupDefinition convertGroupDataToGroupDefinition(GroupData groupData) {
		GroupDefinition newGroupDefinition = new GroupDefinition(groupData.getGroupDataDefinition());
		return newGroupDefinition;
	}

	/**
	 * get members of group
	 * 
	 * @param groupUniqueId
	 * @return
	 */
	private Either<Map<String, String>, TitanOperationStatus> getGroupMembers(String groupUniqueId) {

		Either<Map<String, String>, TitanOperationStatus> result = null;

		Either<List<ImmutablePair<ComponentInstanceData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), groupUniqueId, GraphEdgeLabels.GROUP_MEMBER,
				NodeTypeEnum.ResourceInstance, ComponentInstanceData.class);

		if (childrenNodes.isRight()) {
			TitanOperationStatus status = childrenNodes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
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

	private Either<GroupTypeDefinition, TitanOperationStatus> getGroupTypeOfGroup(String groupUniqueId) {

		Either<ImmutablePair<GroupTypeData, GraphEdge>, TitanOperationStatus> groupTypeRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), groupUniqueId, GraphEdgeLabels.TYPE_OF, NodeTypeEnum.GroupType,
				GroupTypeData.class);

		if (groupTypeRes.isRight()) {
			TitanOperationStatus status = groupTypeRes.right().value();
			log.debug("Cannot find group type associated with capability {}. Status is {}", groupUniqueId, status);

			BeEcompErrorManager.getInstance().logBeFailedFindAssociationError("Fetch Group type", NodeTypeEnum.GroupType.getName(), groupUniqueId, String.valueOf(status));
			return Either.right(groupTypeRes.right().value());
		}

		GroupTypeData groupTypeData = groupTypeRes.left().value().getKey();

		return groupTypeOperation.getGroupTypeByUid(groupTypeData.getGroupTypeDataDefinition().getUniqueId());

	}

	/**
	 * get all properties of the group.
	 * 
	 * the propert definition is taken from the group type.
	 * 
	 * @param groupUid
	 * @return
	 */
	private Either<List<GroupProperty>, TitanOperationStatus> getGroupProperties(String groupUid) {

		List<GroupProperty> groupPropertiesList = new ArrayList<>();

		Either<GroupTypeDefinition, TitanOperationStatus> groupTypeOfGroupRes = getGroupTypeOfGroup(groupUid);

		if (groupTypeOfGroupRes.isRight()) {
			TitanOperationStatus status = groupTypeOfGroupRes.right().value();
			return Either.right(status);
		}

		GroupTypeDefinition groupTypeDefinition = groupTypeOfGroupRes.left().value();

		// Get the properties on the group type of this group
		List<PropertyDefinition> groupTypeProperties = groupTypeDefinition.getProperties();

		if (groupTypeProperties == null || true == groupTypeProperties.isEmpty()) {
			return Either.right(TitanOperationStatus.OK);
		}

		Map<String, PropertyDefinition> uidToPropDefMap = groupTypeProperties.stream().collect(Collectors.toMap(p -> p.getUniqueId(), p -> p));

		// Find all properties values on the group
		Either<List<ImmutablePair<PropertyValueData, GraphEdge>>, TitanOperationStatus> propertyImplNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), groupUid, GraphEdgeLabels.PROPERTY_VALUE,
				NodeTypeEnum.PropertyValue, PropertyValueData.class);

		if (propertyImplNodes.isRight()) {
			TitanOperationStatus status = propertyImplNodes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				groupPropertiesList = groupTypeProperties.stream().map(p -> new GroupProperty(p, p.getDefaultValue(), null)).collect(Collectors.toList());
				return Either.left(groupPropertiesList);
			} else {
				return Either.right(status);
			}
		}

		List<ImmutablePair<PropertyValueData, GraphEdge>> list = propertyImplNodes.left().value();
		if (list == null || true == list.isEmpty()) {
			return Either.right(TitanOperationStatus.OK);
		}

		List<String> processedProps = new ArrayList<>();

		for (ImmutablePair<PropertyValueData, GraphEdge> propertyValue : list) {

			PropertyValueData propertyValueData = propertyValue.getLeft();
			String propertyValueUid = propertyValueData.getUniqueId();
			String value = propertyValueData.getValue();

			Either<ImmutablePair<PropertyData, GraphEdge>, TitanOperationStatus> propertyDefRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.PropertyValue), propertyValueUid, GraphEdgeLabels.PROPERTY_IMPL,
					NodeTypeEnum.Property, PropertyData.class);
			if (propertyDefRes.isRight()) {
				TitanOperationStatus status = propertyDefRes.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
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
				.filter(p -> false == processedProps.contains(p.getUniqueId())).map(p -> new GroupProperty(p, p.getDefaultValue(), null)).collect(Collectors.toList());
		if (leftProps != null) {
			groupPropertiesList.addAll(leftProps);
		}

		return Either.left(groupPropertiesList);
	}

	@Override
	public Either<List<GraphRelation>, TitanOperationStatus> dissociateAllGroupsFromArtifactOnGraph(String componentId, NodeTypeEnum componentTypeEnum, String artifactId) {

		List<GraphRelation> relations = new ArrayList<>();
		Either<List<GraphRelation>, TitanOperationStatus> result = Either.left(relations);

		Either<List<GroupDefinition>, TitanOperationStatus> allGroupsFromGraph = getAllGroupsFromGraph(componentId, componentTypeEnum, true, true, false);
		if (allGroupsFromGraph.isRight()) {
			TitanOperationStatus status = allGroupsFromGraph.right().value();
			return Either.right(status);
		}

		List<GroupDefinition> allGroups = allGroupsFromGraph.left().value();
		if (allGroups == null || allGroups.isEmpty()) {
			return Either.right(TitanOperationStatus.OK);
		}

		// Find all groups which contains this artifact id
		List<GroupDefinition> associatedGroups = allGroups.stream().filter(p -> p.getArtifacts() != null && p.getArtifacts().contains(artifactId)).collect(Collectors.toList());

		if (associatedGroups != null && false == associatedGroups.isEmpty()) {
			log.debug("The groups {} contains the artifact {}", associatedGroups.stream().map(p -> p.getName()).collect(Collectors.toList()), artifactId);

			UniqueIdData artifactData = new UniqueIdData(NodeTypeEnum.ArtifactRef, artifactId);
			for (GroupDefinition groupDefinition : associatedGroups) {
				UniqueIdData groupData = new UniqueIdData(NodeTypeEnum.Group, groupDefinition.getUniqueId());
				Either<GraphRelation, TitanOperationStatus> deleteRelation = titanGenericDao.deleteRelation(groupData, artifactData, GraphEdgeLabels.GROUP_ARTIFACT_REF);
				if (deleteRelation.isRight()) {
					TitanOperationStatus status = deleteRelation.right().value();
					if (status == TitanOperationStatus.NOT_FOUND) {
						status = TitanOperationStatus.INVALID_ID;
					}
					return Either.right(status);
				}

				relations.add(deleteRelation.left().value());
			}

			return result;

		} else {
			log.debug("No group under component id {} is associated to artifact {}", componentId, artifactId);
			return Either.right(TitanOperationStatus.OK);
		}

	}

	public Either<GroupDefinition, TitanOperationStatus> getGroupFromGraph(String uniqueId, boolean skipProperties, boolean skipMembers, boolean skipArtifacts) {

		Either<GroupDefinition, TitanOperationStatus> result = null;

		Either<GroupData, TitanOperationStatus> groupRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), uniqueId, GroupData.class);
		if (groupRes.isRight()) {
			TitanOperationStatus status = groupRes.right().value();
			log.debug("Failed to retrieve group {}  from graph. Status is {}", uniqueId, status);
			BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("Fetch Group", uniqueId, String.valueOf(status));
			result = Either.right(status);
			return result;
		}

		GroupData groupData = groupRes.left().value();

		GroupDefinition groupDefinition = convertGroupDataToGroupDefinition(groupData);

		Either<GroupTypeDefinition, TitanOperationStatus> groupTypeOfGroup = getGroupTypeOfGroup(uniqueId);

		if (groupTypeOfGroup.isRight()) {
			TitanOperationStatus status = groupTypeOfGroup.right().value();
			log.debug("Failed to retrieve capability type of capability {}. Status is {}", uniqueId, status);

			result = Either.right(status);
			return result;
		}

		GroupTypeDefinition groupTypeDefinition = groupTypeOfGroup.left().value();

		groupDefinition.setTypeUid(groupTypeDefinition.getUniqueId());

		if (false == skipMembers) {
			Either<Map<String, String>, TitanOperationStatus> membersRes = getGroupMembers(uniqueId);
			if (membersRes.isRight()) {
				TitanOperationStatus status = membersRes.right().value();
				if (status != TitanOperationStatus.OK) {
					result = Either.right(status);
					return result;
				}
			} else {
				Map<String, String> members = membersRes.left().value();
				groupDefinition.setMembers(members);
			}
		}

		if (false == skipProperties) {
			Either<List<GroupProperty>, TitanOperationStatus> propertiesRes = getGroupProperties(uniqueId);
			if (propertiesRes.isRight()) {
				TitanOperationStatus status = propertiesRes.right().value();
				if (status != TitanOperationStatus.OK) {
					result = Either.right(status);
					return result;
				}
			} else {
				List<GroupProperty> properties = propertiesRes.left().value();
				groupDefinition.convertFromGroupProperties(properties);
			}
		}

		if (false == skipArtifacts) {
			Either<List<ImmutablePair<String, String>>, TitanOperationStatus> artifactsRes = getGroupArtifactsPairs(uniqueId);
			if (artifactsRes.isRight()) {
				TitanOperationStatus status = artifactsRes.right().value();
				if (status != TitanOperationStatus.OK) {
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

	@Override
	public boolean isGroupExist(String groupName, boolean inTransaction) {

		Either<List<GroupData>, TitanOperationStatus> eitherGroup = null;
		try {
			Map<String, Object> properties = new HashMap<>();
			properties.put(GraphPropertiesDictionary.NAME.getProperty(), groupName);

			eitherGroup = titanGenericDao.getByCriteria(NodeTypeEnum.Group, properties, GroupData.class);
			return eitherGroup.isLeft() && !eitherGroup.left().value().isEmpty();

		} finally {
			handleTransactionCommitRollback(inTransaction, eitherGroup);
		}
	}

	protected Either<List<GroupDefinition>, TitanOperationStatus> getAllGroupsFromGraph(String componentId, NodeTypeEnum componentTypeEnum, boolean skipProperties, boolean skipMembers, boolean skipArtifacts) {

		List<GroupDefinition> groups = new ArrayList<GroupDefinition>();

		Either<List<ImmutablePair<GroupData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(componentTypeEnum), componentId, GraphEdgeLabels.GROUP, NodeTypeEnum.Group,
				GroupData.class);

		if (childrenNodes.isRight()) {
			TitanOperationStatus status = childrenNodes.right().value();
			return Either.right(status);
		}

		List<ImmutablePair<GroupData, GraphEdge>> graphGroups = childrenNodes.left().value();

		if (graphGroups == null || true == graphGroups.isEmpty()) {
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

		// Consumer<String> consumer = (x) -> getGroup(x);
		// StreamUtils.takeWhile(graphGroups.stream().map(p ->
		// p.left.getUniqueId()), consumer);

		for (ImmutablePair<GroupData, GraphEdge> pair : graphGroups) {

			String groupUniqueId = pair.left.getGroupDataDefinition().getUniqueId();
			Either<GroupDefinition, TitanOperationStatus> groupRes = this.getGroupFromGraph(groupUniqueId, skipProperties, skipMembers, skipArtifacts);

			if (groupRes.isRight()) {
				TitanOperationStatus status = groupRes.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
				}
				return Either.right(status);
			} else {
				groups.add(groupRes.left().value());
			}

		}

		return Either.left(groups);
	}

	private TitanOperationStatus dissociateAndAssociateGroupsFromArtifactOnGraph(String componentId, NodeTypeEnum componentTypeEnum, String oldArtifactId, ArtifactData newArtifact) {

		Either<List<GroupDefinition>, TitanOperationStatus> allGroupsFromGraph = getAllGroupsFromGraph(componentId, componentTypeEnum, true, true, false);
		if (allGroupsFromGraph.isRight()) {
			TitanOperationStatus status = allGroupsFromGraph.right().value();
			return status;
		}

		List<GroupDefinition> allGroups = allGroupsFromGraph.left().value();
		if (allGroups == null || allGroups.isEmpty()) {
			return TitanOperationStatus.OK;
		}

		// Find all groups which contains this artifact id
		List<GroupDefinition> associatedGroups = allGroups.stream().filter(p -> p.getArtifacts() != null && p.getArtifacts().contains(oldArtifactId)).collect(Collectors.toList());

		if (associatedGroups != null && false == associatedGroups.isEmpty()) {

			log.debug("The groups {} contains the artifact {}", associatedGroups.stream().map(p -> p.getName()).collect(Collectors.toList()), oldArtifactId);

			UniqueIdData oldArtifactData = new UniqueIdData(NodeTypeEnum.ArtifactRef, oldArtifactId);
			UniqueIdData newArtifactData = new UniqueIdData(NodeTypeEnum.ArtifactRef, newArtifact.getArtifactDataDefinition().getUniqueId());
			Map<String, Object> props = new HashMap<String, Object>();
			props.put(GraphPropertiesDictionary.NAME.getProperty(), newArtifactData.getLabel());

			for (GroupDefinition groupDefinition : associatedGroups) {
				UniqueIdData groupData = new UniqueIdData(NodeTypeEnum.Group, groupDefinition.getUniqueId());

				Either<GraphRelation, TitanOperationStatus> deleteRelation = titanGenericDao.deleteRelation(groupData, oldArtifactData, GraphEdgeLabels.GROUP_ARTIFACT_REF);
				log.trace("After dissociate group {} from artifact {}"  , groupDefinition.getName(), oldArtifactId);
				if (deleteRelation.isRight()) {
					TitanOperationStatus status = deleteRelation.right().value();
					if (status == TitanOperationStatus.NOT_FOUND) {
						status = TitanOperationStatus.INVALID_ID;
					}
					return status;
				}

				Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(groupData, newArtifactData, GraphEdgeLabels.GROUP_ARTIFACT_REF, props);
				log.trace("After associate group {} to artifact {}" , groupDefinition.getName(), newArtifact.getUniqueIdKey());
				if (createRelation.isRight()) {
					TitanOperationStatus status = createRelation.right().value();
					if (status == TitanOperationStatus.NOT_FOUND) {
						status = TitanOperationStatus.INVALID_ID;
					}
					return status;
				}
			}

		}
		return TitanOperationStatus.OK;
	}

	@Override
	public StorageOperationStatus dissociateAndAssociateGroupsFromArtifact(String componentId, NodeTypeEnum componentTypeEnum, String oldArtifactId, ArtifactData newArtifact, boolean inTransaction) {

		StorageOperationStatus result = null;

		try {
			TitanOperationStatus status = this.dissociateAndAssociateGroupsFromArtifactOnGraph(componentId, componentTypeEnum, oldArtifactId, newArtifact);

			if (status != TitanOperationStatus.OK && status != TitanOperationStatus.NOT_FOUND) {
				result = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
				return result;
			}

			result = StorageOperationStatus.OK;
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result != StorageOperationStatus.OK) {
					log.debug("Going to execute rollback on graph.");
					BeEcompErrorManager.getInstance().logBeExecuteRollbackError("Rollback on graph");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
	}

	private Either<List<ImmutablePair<String, String>>, TitanOperationStatus> getGroupArtifactsPairs(String groupUniqueId) {

		Either<List<ImmutablePair<String, String>>, TitanOperationStatus> result = null;

		Either<List<ImmutablePair<ArtifactData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), groupUniqueId, GraphEdgeLabels.GROUP_ARTIFACT_REF,
				NodeTypeEnum.ArtifactRef, ArtifactData.class);
		if (childrenNodes.isRight()) {
			TitanOperationStatus status = childrenNodes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
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
					ImmutablePair<String, String> artifact = new ImmutablePair<String, String>(uniqueId, UUID);
					artifactsList.add(artifact);
				}
			}

			log.debug("The artifacts list related to group {} is {}", groupUniqueId, artifactsList);
			result = Either.left(artifactsList);
		}

		return result;

	}

	@Override
	public StorageOperationStatus validateAndUpdatePropertyValue(GroupProperty property) {
		
		StorageOperationStatus result = null;
		String innerType = property.getSchema() == null ? null : property.getSchema().getProperty() == null ? null : property.getSchema().getProperty().getType();
		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = dataTypeCache.getAll();
		Either<Object, Boolean> isValid = null;
		if (allDataTypes.isRight()) {
			TitanOperationStatus status = allDataTypes.right().value();
			log.debug("Failed to fetch data types from cache. Status is {}. ", status);
			result = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
		}
		if(result == null){
			isValid = propertyOperation.validateAndUpdatePropertyValue(property.getType(), property.getValue(), innerType, allDataTypes.left().value());
			if(isValid.isRight()){
				log.debug("Failed to validate property value {}. Status is {}. ", property.getValue(), StorageOperationStatus.INVALID_PROPERTY);
				result =  StorageOperationStatus.INVALID_PROPERTY;
			}
		}
		if(result == null){
			String validValue = String.valueOf(isValid.left().value());
			property.setValue(validValue);
			result = StorageOperationStatus.OK;
		}
		return result;
	}
}
