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
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
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
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.GroupData;
import org.openecomp.sdc.be.resources.data.GroupTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("group-operation")
public class GroupOperation extends AbstractOperation implements IGroupOperation {

	private static String ADDING_GROUP = "AddingGroup";
	private static String DELETING_GROUP = "DeletingGroup";
	private static String DELETING_ALL_GROUPS = "DeletingAllGroups";
	private static String ASSOCIATING_GROUP_TO_COMP_INST = "AssociatingGroupToComponentInstance";

	private static Logger log = LoggerFactory.getLogger(GroupOperation.class.getName());

	@javax.annotation.Resource
	private PropertyOperation propertyOperation;

	@javax.annotation.Resource
	private GroupTypeOperation groupTypeOperation;

	@javax.annotation.Resource
	private ApplicationDataTypeCache dataTypeCache;

	@Override
	public Either<GroupData, TitanOperationStatus> addGroupToGraph(NodeTypeEnum nodeTypeEnum, String componentId,
			GroupDefinition groupDefinition) {

		String groupTypeUid = groupDefinition.getTypeUid();

		if (groupTypeUid == null) {
			BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, "Group type id is empty",
					ErrorSeverity.ERROR);
			return Either.right(TitanOperationStatus.INVALID_ID);
		}

		ComponentMetadataData metaData = null;
		if (nodeTypeEnum == NodeTypeEnum.Resource) {
			metaData = new ResourceMetadataData();
		} else {
			metaData = new ServiceMetadataData();
		}
		metaData.getMetadataDataDefinition().setUniqueId(componentId);

		groupDefinition.setUniqueId(UniqueIdBuilder.buildGroupUniqueId(componentId, groupDefinition.getName()));

		int propertiesSize = groupDefinition.getProperties() == null ? 0 : groupDefinition.getProperties().size();
		groupDefinition.setPropertyValueCounter(propertiesSize);

		GroupData groupData = new GroupData(groupDefinition);

		TitanOperationStatus status = null;
		// Adding group data node to graph
		log.debug("Before adding group to graph {}", groupData.toString());
		Either<GroupData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(groupData,
				GroupData.class);
		log.debug("After adding group to graph {}", groupData.toString());
		if (createNodeResult.isRight()) {
			status = createNodeResult.right().value();
			log.error("Failed to add group {} to graph. Status is {}", groupDefinition.getName(), status);
			return Either.right(status);
		}

		// Associate group to group type
		log.debug("Going to associate group {} to its groupType {}", groupDefinition.getName(), groupDefinition.getType());
		Either<GraphRelation, TitanOperationStatus> associateGroupTypeRes = associateGroupToGroupType(groupData,
				groupTypeUid);
		log.debug("After associating group {} to its groupType {}. Status is {}", groupDefinition.getName(), groupDefinition.getType(), associateGroupTypeRes);
		if (associateGroupTypeRes.isRight()) {
			status = associateGroupTypeRes.right().value();
			String description = "Failed to associate group " + groupDefinition.getName() + " to its groupType "
					+ groupDefinition.getType() + " in graph.";
			BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description, ErrorSeverity.ERROR);
			return Either.right(status);
		}

		// Associate group to component RESOURCE/SERVICE/PRODUCT
		Either<GraphRelation, TitanOperationStatus> associateComponentRes = associateGroupToComponent(groupData,
				nodeTypeEnum, componentId);
		if (associateComponentRes.isRight()) {
			status = associateComponentRes.right().value();
			String description = "Failed to associate group " + groupDefinition.getName() + " to "
					+ nodeTypeEnum.getName() + " " + componentId + ". status is " + status;
			BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description, ErrorSeverity.ERROR);
			return Either.right(status);
		}

		Either<GroupTypeDefinition, TitanOperationStatus> groupTypeRes = groupTypeOperation
				.getGroupTypeByUid(groupDefinition.getTypeUid());
		if (groupTypeRes.isRight()) {
			TitanOperationStatus operationStatus = groupTypeRes.right().value();
			log.debug("Failed to find group type {}", groupDefinition.getTypeUid());
			if (operationStatus == TitanOperationStatus.NOT_FOUND) {
				return Either.right(TitanOperationStatus.INVALID_ID);
			}
		}
		GroupTypeDefinition groupTypeDefinition = groupTypeRes.left().value();
		// 1. find properties from group type
		List<PropertyDefinition> groupTypeProperties = groupTypeDefinition.getProperties();

		// 2. check the properties exists in the group type.
		// 3. add parent unique id to the properties
		// 4. add node per group property which the group point to it and it
		// points to the parent unique id

		// Adding properties to group
		List<GroupProperty> properties = groupDefinition.getProperties();

		if (properties != null && false == properties.isEmpty()) {

			if (groupTypeProperties == null || true == groupTypeProperties.isEmpty()) {
				BeEcompErrorManager.getInstance().logInvalidInputError(ADDING_GROUP,
						"group type does not have properties", ErrorSeverity.INFO);
				return Either.right(TitanOperationStatus.MATCH_NOT_FOUND);
			}

			Map<String, PropertyDefinition> groupTypePropertiesMap = groupTypeProperties.stream()
					.collect(Collectors.toMap(p -> p.getName(), p -> p));

			Either<PropertyValueData, TitanOperationStatus> addPropertyResult = null;
			int i = 1;
			for (GroupProperty prop : properties) {
				addPropertyResult = addPropertyToGroup(groupData, prop, groupTypePropertiesMap.get(prop.getName()), i);
				if (addPropertyResult.isRight()) {
					status = addPropertyResult.right().value();
					String description = "Failed to associate group " + groupData.getUniqueId() + " to property "
							+ prop.getName() + " in graph. Status is " + status;
					BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description,
							ErrorSeverity.ERROR);
					return Either.right(status);
				}
				i++;
			}
		}

		// Associate artifacts to group
		List<String> artifacts = groupDefinition.getArtifacts();

		Either<GroupDefinition, TitanOperationStatus> associateArtifactsToGroupOnGraph = associateArtifactsToGroupOnGraph(
				groupData.getGroupDataDefinition().getUniqueId(), artifacts);
		if (associateArtifactsToGroupOnGraph.isRight()
				&& associateArtifactsToGroupOnGraph.right().value() != TitanOperationStatus.OK) {
			return Either.right(status);
		}
		/*
		 * Either<GraphRelation, TitanOperationStatus> addArtifactsRefResult =
		 * null; if (artifacts != null) { for (String artifactId : artifacts) {
		 * Either<ArtifactData, TitanOperationStatus> findArtifactRes =
		 * titanGenericDao .getNode(UniqueIdBuilder
		 * .getKeyByNodeType(NodeTypeEnum.ArtifactRef), artifactId,
		 * ArtifactData.class); if (findArtifactRes.isRight()) { status =
		 * findArtifactRes.right().value(); if (status ==
		 * TitanOperationStatus.NOT_FOUND) { status =
		 * TitanOperationStatus.INVALID_ID; } String description =
		 * "Failed to associate group " + groupData.getUniqueId() +
		 * " to artifact " + artifactId + " in graph. Status is " + status;
		 * BeEcompErrorManager.getInstance().logInternalFlowError( ADDING_GROUP,
		 * description, ErrorSeverity.ERROR); return Either.right(status); }
		 * 
		 * Map<String, Object> props = new HashMap<String, Object>();
		 * props.put(GraphPropertiesDictionary.NAME.getProperty(),
		 * findArtifactRes.left().value().getLabel());
		 * 
		 * addArtifactsRefResult = titanGenericDao.createRelation( groupData,
		 * findArtifactRes.left().value(), GraphEdgeLabels.GROUP_ARTIFACT_REF,
		 * props);
		 * 
		 * if (addArtifactsRefResult.isRight()) { status =
		 * addArtifactsRefResult.right().value(); String description =
		 * "Failed to associate group " + groupData.getUniqueId() +
		 * " to artifact " + artifactId + " in graph. Status is " + status;
		 * BeEcompErrorManager.getInstance().logInternalFlowError( ADDING_GROUP,
		 * description, ErrorSeverity.ERROR); return Either.right(status); } } }
		 */

		// Associate group to members
		// map of componentInstances <name: uniqueId>
		Map<String, String> members = groupDefinition.getMembers();

		if (members != null && false == members.isEmpty()) {
			Either<GraphRelation, TitanOperationStatus> addMembersRefResult = null;
			for (Entry<String, String> member : members.entrySet()) {
				if (member.getValue() == null || member.getValue().isEmpty()) {
					continue;
				}
				Either<ComponentInstanceData, TitanOperationStatus> findComponentInstanceRes = titanGenericDao.getNode(
						UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), member.getValue(),
						ComponentInstanceData.class);
				if (findComponentInstanceRes.isRight()) {
					status = findComponentInstanceRes.right().value();
					if (status == TitanOperationStatus.NOT_FOUND) {
						status = TitanOperationStatus.INVALID_ID;
					}
					String description = "Failed to find to find member of group " + member.getValue()
							+ " in graph. Status is " + status;
					BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description,
							ErrorSeverity.ERROR);
					return Either.right(status);
				}
				Map<String, Object> props = new HashMap<String, Object>();
				props.put(GraphPropertiesDictionary.NAME.getProperty(), member.getKey());
				addMembersRefResult = titanGenericDao.createRelation(groupData, findComponentInstanceRes.left().value(),
						GraphEdgeLabels.GROUP_MEMBER, props);

				if (addMembersRefResult.isRight()) {
					status = addMembersRefResult.right().value();
					String description = "Failed to associate group " + groupData.getUniqueId()
							+ " to component instance " + member.getValue() + " in graph. Status is " + status;
					BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description,
							ErrorSeverity.ERROR);
					return Either.right(status);
				}
			}
		}

		return Either.left(groupData);
	}

	private Either<PropertyValueData, TitanOperationStatus> addPropertyToGroup(GroupData groupData,
			GroupProperty groupProperty, PropertyDefinition prop, Integer index) {

		if (prop == null) {
			return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
		}

		String propertyId = prop.getUniqueId();
		Either<PropertyData, TitanOperationStatus> findPropertyDefRes = titanGenericDao
				.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property), propertyId, PropertyData.class);

		if (findPropertyDefRes.isRight()) {
			TitanOperationStatus status = findPropertyDefRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		PropertyData propertyData = findPropertyDefRes.left().value();

		PropertyDataDefinition propDataDef = propertyData.getPropertyDataDefinition();
		String propertyType = propDataDef.getType();
		String value = groupProperty.getValue();

		Either<String, TitanOperationStatus> checkInnerType = propertyOperation.checkInnerType(propDataDef);
		if (checkInnerType.isRight()) {
			TitanOperationStatus status = checkInnerType.right().value();
			return Either.right(status);
		}

		String innerType = checkInnerType.left().value();

		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = dataTypeCache.getAll();
		if (allDataTypes.isRight()) {
			TitanOperationStatus status = allDataTypes.right().value();
			BeEcompErrorManager.getInstance().logInternalFlowError("AddPropertyToGroup",
					"Failed to add property to group. Status is " + status, ErrorSeverity.ERROR);
			return Either.right(status);

		}

		log.debug("Before validateAndUpdatePropertyValue");
		Either<Object, Boolean> isValid = propertyOperation.validateAndUpdatePropertyValue(propertyType, value,
				innerType, allDataTypes.left().value());
		log.debug("After validateAndUpdatePropertyValue. isValid = {}", isValid);

		String newValue = value;
		if (isValid.isRight()) {
			Boolean res = isValid.right().value();
			if (res == false) {
				return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
			}
		} else {
			Object object = isValid.left().value();
			if (object != null) {
				newValue = object.toString();
			}
		}

		String uniqueId = UniqueIdBuilder.buildGroupPropertyValueUid((String) groupData.getUniqueId(), index);
		PropertyValueData propertyValueData = new PropertyValueData();
		propertyValueData.setUniqueId(uniqueId);
		propertyValueData.setValue(newValue);

		log.debug("Before adding property value to graph {}",propertyValueData);
		Either<PropertyValueData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(propertyValueData,
				PropertyValueData.class);
		log.debug("After adding property value to graph {}", propertyValueData);

		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			return Either.right(operationStatus);
		}

		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(propertyValueData,
				propertyData, GraphEdgeLabels.PROPERTY_IMPL, null);

		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			String description = "Failed to associate property value " + uniqueId + " to property " + propertyId
					+ " in graph. status is " + operationStatus;
			BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description, ErrorSeverity.ERROR);
			return Either.right(operationStatus);
		}

		createRelResult = titanGenericDao.createRelation(groupData, propertyValueData, GraphEdgeLabels.PROPERTY_VALUE,
				null);

		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			String description = "Failed to associate group " + groupData.getGroupDataDefinition().getName()
					+ " to property value " + uniqueId + " in graph. Status is " + operationStatus;
			BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description, ErrorSeverity.ERROR);
			return Either.right(operationStatus);
		}

		return Either.left(createNodeResult.left().value());
	}

	private Either<GraphRelation, TitanOperationStatus> associateGroupToComponent(GroupData groupData,
			NodeTypeEnum nodeTypeEnum, String componentId) {
		UniqueIdData componentIdData = new UniqueIdData(nodeTypeEnum, componentId);

		log.debug("Before associating component {} to group {}.", componentId, groupData);
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), groupData.getGroupDataDefinition().getName());
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(componentIdData,
				groupData, GraphEdgeLabels.GROUP, props);
		log.debug("After associating component {} to group {}. Status is {}", componentId, groupData, createRelResult);
		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			log.debug("Failed to associate component {} to group {} in graph. Status is {}", componentId, groupData, operationStatus);
			return Either.right(operationStatus);
		}

		return Either.left(createRelResult.left().value());
	}

	private Either<GraphRelation, TitanOperationStatus> associateGroupToGroupType(GroupData groupData,
			String groupTypeUid) {

		UniqueIdData groupTypeIdData = new UniqueIdData(NodeTypeEnum.GroupType, groupTypeUid);

		log.debug("Before associating {} to group type {} (uid = {}).", groupData, groupData.getGroupDataDefinition().getType(), groupTypeUid);
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(groupData,
				groupTypeIdData, GraphEdgeLabels.TYPE_OF, null);
		
		if (log.isDebugEnabled()) {
			log.debug("After associating {} to group type {} (uid = {}). Result is {}", groupData, groupData.getGroupDataDefinition().getType(), groupTypeUid, createRelResult);
		}
		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			return Either.right(operationStatus);
		}
		return createRelResult;
	}

	@Override
	public Either<GroupDefinition, StorageOperationStatus> addGroup(NodeTypeEnum nodeTypeEnum, String componentId,
			GroupDefinition groupDefinition) {
		return addGroup(nodeTypeEnum, componentId, groupDefinition, false);
	}

	@Override
	public Either<GroupDefinition, StorageOperationStatus> addGroup(NodeTypeEnum nodeTypeEnum, String componentId,
			GroupDefinition groupDefinition, boolean inTransaction) {

		Either<GroupDefinition, StorageOperationStatus> result = null;
		try {
			Either<GroupData, TitanOperationStatus> addGroupRes = addGroupToGraph(nodeTypeEnum, componentId,
					groupDefinition);
			if (addGroupRes.isRight()) {
				TitanOperationStatus status = addGroupRes.right().value();
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			} else {

				GroupData groupData = addGroupRes.left().value();
				String groupUid = groupData.getGroupDataDefinition().getUniqueId();
				result = this.getGroup(groupUid, true);

			}

			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on graph. Failed to add group {} to {}", groupDefinition.getName(), nodeTypeEnum.toString());
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
	}

	private GroupDefinition convertGroupDataToGroupDefinition(GroupData groupData) {
		GroupDefinition newGroupDefinition = new GroupDefinition(groupData.getGroupDataDefinition());
		return newGroupDefinition;
	}

	public Either<GroupDefinition, StorageOperationStatus> getGroup(String uniqueId) {
		return getGroup(uniqueId, false);
	}

	@Override
	public Either<GroupDefinition, StorageOperationStatus> getGroup(String uniqueId, boolean inTransaction) {

		Either<GroupDefinition, StorageOperationStatus> result = null;

		try {

			Either<GroupDefinition, TitanOperationStatus> groupFromGraph = this.getGroupFromGraph(uniqueId);

			if (groupFromGraph.isRight()) {
				TitanOperationStatus status = groupFromGraph.right().value();
				log.debug("Failed to retrieve group {} from graph. Status is {}", uniqueId, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			} else {
				GroupDefinition groupDefinition = groupFromGraph.left().value();
				result = Either.left(groupDefinition);
			}

			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
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

	@Override
	public Either<GroupDefinition, TitanOperationStatus> getGroupFromGraph(String uniqueId) {

		return getGroupFromGraph(uniqueId, false, false, false);

	}

	/**
	 * get the list of artifacts related to a given group
	 * 
	 * @param groupUniqueId
	 * @return
	 */
	// private Either<List<String>, TitanOperationStatus> getGroupArtifacts(
	// String groupUniqueId) {
	//
	// Either<List<String>, TitanOperationStatus> result = null;
	//
	// Either<List<ImmutablePair<ArtifactData, GraphEdge>>,
	// TitanOperationStatus> childrenNodes = titanGenericDao
	// .getChildrenNodes(
	// UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group),
	// groupUniqueId, GraphEdgeLabels.GROUP_ARTIFACT_REF,
	// NodeTypeEnum.ArtifactRef, ArtifactData.class);
	// if (childrenNodes.isRight()) {
	// TitanOperationStatus status = childrenNodes.right().value();
	// if (status == TitanOperationStatus.NOT_FOUND) {
	// status = TitanOperationStatus.OK;
	// }
	// result = Either.right(status);
	//
	// } else {
	//
	// List<String> artifactsList = new ArrayList<>();
	// List<ImmutablePair<ArtifactData, GraphEdge>> list = childrenNodes
	// .left().value();
	// if (list != null) {
	// for (ImmutablePair<ArtifactData, GraphEdge> pair : list) {
	// ArtifactData artifactData = pair.getKey();
	// String uniqueId = artifactData.getArtifactDataDefinition()
	// .getUniqueId();
	// artifactsList.add(uniqueId);
	// }
	// }
	//
	// log.debug("The artifacts list related to group {} is {}", groupUniqueId, artifactsList);
	// result = Either.left(artifactsList);
	// }
	//
	// return result;
	//
	// }

	/**
	 * get members of group
	 * 
	 * @param groupUniqueId
	 * @return
	 */
	protected Either<Map<String, String>, TitanOperationStatus> getGroupMembers(String groupUniqueId) {

		Either<Map<String, String>, TitanOperationStatus> result = null;

		Either<List<ImmutablePair<ComponentInstanceData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao
				.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), groupUniqueId,
						GraphEdgeLabels.GROUP_MEMBER, NodeTypeEnum.ResourceInstance, ComponentInstanceData.class);

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

	public Either<GroupTypeDefinition, TitanOperationStatus> getGroupTypeOfGroup(String groupUniqueId) {

		Either<ImmutablePair<GroupTypeData, GraphEdge>, TitanOperationStatus> groupTypeRes = titanGenericDao.getChild(
				UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), groupUniqueId, GraphEdgeLabels.TYPE_OF,
				NodeTypeEnum.GroupType, GroupTypeData.class);

		if (groupTypeRes.isRight()) {
			TitanOperationStatus status = groupTypeRes.right().value();
			log.debug("Cannot find group type associated with capability {}. Status is {}", groupUniqueId, status);

			BeEcompErrorManager.getInstance().logBeFailedFindAssociationError("Fetch Group type",
					NodeTypeEnum.GroupType.getName(), groupUniqueId, String.valueOf(status));
			return Either.right(groupTypeRes.right().value());
		}

		GroupTypeData groupTypeData = groupTypeRes.left().value().getKey();

		Either<GroupTypeDefinition, TitanOperationStatus> groupTypeByUid = groupTypeOperation
				.getGroupTypeByUid(groupTypeData.getGroupTypeDataDefinition().getUniqueId());

		return groupTypeByUid;

	}

	/**
	 * get all properties of the group.
	 * 
	 * the propert definition is taken from the group type.
	 * 
	 * @param groupUid
	 * @return
	 */
	public Either<List<GroupProperty>, TitanOperationStatus> getGroupProperties(String groupUid) {

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

		Map<String, PropertyDefinition> uidToPropDefMap = groupTypeProperties.stream()
				.collect(Collectors.toMap(p -> p.getUniqueId(), p -> p));

		// Find all properties values on the group
		Either<List<ImmutablePair<PropertyValueData, GraphEdge>>, TitanOperationStatus> propertyImplNodes = titanGenericDao
				.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), groupUid,
						GraphEdgeLabels.PROPERTY_VALUE, NodeTypeEnum.PropertyValue, PropertyValueData.class);

		if (propertyImplNodes.isRight()) {
			TitanOperationStatus status = propertyImplNodes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				groupPropertiesList = groupTypeProperties.stream()
						.map(p -> new GroupProperty(p, p.getDefaultValue(), null)).collect(Collectors.toList());
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

			Either<ImmutablePair<PropertyData, GraphEdge>, TitanOperationStatus> propertyDefRes = titanGenericDao
					.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.PropertyValue), propertyValueUid,
							GraphEdgeLabels.PROPERTY_IMPL, NodeTypeEnum.Property, PropertyData.class);
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
				.filter(p -> false == processedProps.contains(p.getUniqueId()))
				.map(p -> new GroupProperty(p, p.getDefaultValue(), null)).collect(Collectors.toList());
		if (leftProps != null) {
			groupPropertiesList.addAll(leftProps);
		}

		return Either.left(groupPropertiesList);
	}

	public Either<List<GroupDefinition>, TitanOperationStatus> getAllGroupsFromGraph(String componentId,
			NodeTypeEnum componentTypeEnum) {

		return getAllGroupsFromGraph(componentId, componentTypeEnum, false, false, false);

	}

	@Override
	public Either<List<GroupDefinition>, StorageOperationStatus> getAllGroups(String componentId,
			NodeTypeEnum compTypeEnum, boolean inTransaction) {

		Either<List<GroupDefinition>, StorageOperationStatus> result = null;

		try {

			Either<List<GroupDefinition>, TitanOperationStatus> allGroups = this.getAllGroupsFromGraph(componentId,
					compTypeEnum);

			if (allGroups.isRight()) {
				TitanOperationStatus status = allGroups.right().value();
				log.debug("Failed to retrieve all groups of component {} from graph. Status is {}", componentId,
						status);
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.OK;
				}
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			} else {
				List<GroupDefinition> groupsDefinition = allGroups.left().value();
				result = Either.left(groupsDefinition);
			}

			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
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

	@Override
	public Either<List<GroupDefinition>, StorageOperationStatus> getAllGroups(String componentId,
			NodeTypeEnum compTypeEnum) {
		return getAllGroups(componentId, compTypeEnum, false);
	}

	public Either<GroupData, TitanOperationStatus> deleteGroupFromGraph(String groupUniqueId) {

		Either<GroupDefinition, TitanOperationStatus> groupFromGraph = this.getGroupFromGraph(groupUniqueId);
		if (groupFromGraph.isRight()) {
			TitanOperationStatus status = groupFromGraph.right().value();
			log.debug("Cannot find group {} on graph. Status is {}", groupUniqueId, status);
			return Either.right(status);
		}

		GroupDefinition groupDefinition = groupFromGraph.left().value();
		// 1. delete all properties values nodes
		List<GroupProperty> properties = groupDefinition.getProperties();
		if (properties != null) {
			for (GroupProperty groupProperty : properties) {
				String propValueUniqueId = groupProperty.getValueUniqueUid();

				if (propValueUniqueId != null) {
					UniqueIdData uniqueIdData = new UniqueIdData(NodeTypeEnum.PropertyValue, propValueUniqueId);
					Either<PropertyValueData, TitanOperationStatus> deleteNode = titanGenericDao
							.deleteNode(uniqueIdData, PropertyValueData.class);
					if (deleteNode.isRight()) {
						TitanOperationStatus status = groupFromGraph.right().value();
						String description = String.format(
								"Failed to delete property {} under group {}" + groupUniqueId
										+ " on graph. Status is {}",
								propValueUniqueId, groupDefinition.getName(), status.name());
						log.debug(description);
						BeEcompErrorManager.getInstance().logBeFailedDeleteNodeError(DELETING_GROUP, propValueUniqueId,
								status.name());
						return Either.right(status);
					} else {
						log.trace("Property {} was deleted from geoup {}", propValueUniqueId, groupDefinition.getName());
					}
				}
			}
		}

		// 2. delete the group node
		UniqueIdData uniqueIdData = new UniqueIdData(NodeTypeEnum.Group, groupUniqueId);
		Either<GroupData, TitanOperationStatus> deleteNode = titanGenericDao.deleteNode(uniqueIdData, GroupData.class);
		if (deleteNode.isRight()) {
			TitanOperationStatus status = groupFromGraph.right().value();
			String description = String.format(
					"Failed to delete group {} with uid " + groupUniqueId + " on graph. Status is {}",
					groupDefinition.getName(), groupUniqueId, status.name());
			log.debug(description);
			BeEcompErrorManager.getInstance().logBeFailedDeleteNodeError(DELETING_GROUP, groupUniqueId, status.name());
			return Either.right(status);
		} else {
			log.trace("Group {} was deleted from group", groupUniqueId);
		}

		GroupData groupData = deleteNode.left().value();
		return Either.left(groupData);
	}

	public Either<GroupDefinition, StorageOperationStatus> deleteGroup(String groupUniqueId) {
		return deleteGroup(groupUniqueId, false);
	}

	public Either<GroupDefinition, StorageOperationStatus> deleteGroup(String groupUniqueId, boolean inTransaction) {

		Either<GroupDefinition, StorageOperationStatus> result = null;

		try {

			Either<GroupData, TitanOperationStatus> deleteGroup = this.deleteGroupFromGraph(groupUniqueId);

			if (deleteGroup.isRight()) {
				TitanOperationStatus status = deleteGroup.right().value();
				log.debug("Failed to delete group {} from graph. Status is ", groupUniqueId, status.name());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			} else {
				GroupData groupData = deleteGroup.left().value();
				GroupDefinition groupDefinition = convertGroupDataToGroupDefinition(groupData);
				result = Either.left(groupDefinition);
			}

			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
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

	@Override
	public Either<List<GroupDefinition>, TitanOperationStatus> deleteAllGroupsFromGraph(String componentId,
			NodeTypeEnum componentTypeEnum) {

		Either<List<ImmutablePair<GroupData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao
				.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(componentTypeEnum), componentId,
						GraphEdgeLabels.GROUP, NodeTypeEnum.Group, GroupData.class);

		if (childrenNodes.isRight()) {
			TitanOperationStatus status = childrenNodes.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				BeEcompErrorManager.getInstance().logBeFailedFindAllNodesError(DELETING_ALL_GROUPS,
						NodeTypeEnum.Group.name(), componentId, status.name());
			}
			return Either.right(status);
		}

		List<GroupDefinition> result = new ArrayList<>();

		List<ImmutablePair<GroupData, GraphEdge>> list = childrenNodes.left().value();
		if (list != null) {
			for (ImmutablePair<GroupData, GraphEdge> pair : list) {
				String uniqueId = pair.left.getGroupDataDefinition().getUniqueId();
				Either<GroupData, TitanOperationStatus> deleteGroupFromGraph = deleteGroupFromGraph(uniqueId);
				if (deleteGroupFromGraph.isRight()) {
					TitanOperationStatus status = deleteGroupFromGraph.right().value();
					BeEcompErrorManager.getInstance().logBeFailedDeleteNodeError(DELETING_ALL_GROUPS, uniqueId,
							status.name());
					return Either.right(status);
				}
				GroupData groupData = deleteGroupFromGraph.left().value();
				GroupDefinition groupDefinition = convertGroupDataToGroupDefinition(groupData);
				result.add(groupDefinition);
			}
		}

		return Either.left(result);
	}

	@Override
	public Either<List<GroupDefinition>, StorageOperationStatus> deleteAllGroups(String componentId,
			NodeTypeEnum compTypeEnum, boolean inTransaction) {

		Either<List<GroupDefinition>, StorageOperationStatus> result = null;

		try {

			Either<List<GroupDefinition>, TitanOperationStatus> allGroups = this.deleteAllGroupsFromGraph(componentId,
					compTypeEnum);

			if (allGroups.isRight()) {
				TitanOperationStatus status = allGroups.right().value();
				log.debug("Failed to delete all groups of component {} from graph. Status is {}", componentId, status);
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.OK;
				}
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			} else {
				List<GroupDefinition> groupsDefinition = allGroups.left().value();
				result = Either.left(groupsDefinition);
			}

			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
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

	@Override
	public Either<List<GroupDefinition>, StorageOperationStatus> deleteAllGroups(String componentId,
			NodeTypeEnum compTypeEnum) {
		return deleteAllGroups(componentId, compTypeEnum, false);
	}

	public Either<List<GroupDefinition>, StorageOperationStatus> prepareGroupsForCloning(
			org.openecomp.sdc.be.model.Component origResource,
			ImmutablePair<List<ComponentInstance>, Map<String, String>> cloneInstances) {

		List<GroupDefinition> groupsToCreate = new ArrayList<>();
		Either<List<GroupDefinition>, StorageOperationStatus> result = Either.left(groupsToCreate);

		List<GroupDefinition> groups = origResource.getGroups();

		if (groups != null) {
			// keep typeUid
			// keep artifacts uids
			// remove properties without valueUniqueId
			for (GroupDefinition groupDefinition : groups) {

				GroupDefinition gdToCreate = new GroupDefinition(groupDefinition);
				gdToCreate.setUniqueId(null);
				gdToCreate.setMembers(null);

				List<GroupProperty> properties = groupDefinition.getProperties();
				if (properties != null) {
					// Take properties which was updated in the
					// group(getValueUniqueUid != null),
					// Then set null instead of the value(prepare for the
					// creation).
					List<GroupProperty> propertiesToUpdate = properties.stream()
							.filter(p -> p.getValueUniqueUid() != null).map(p -> {
								p.setValueUniqueUid(null);
								return p;
							}).collect(Collectors.toList());

					gdToCreate.setProperties(propertiesToUpdate);

				}

				Map<String, String> members = groupDefinition.getMembers();
				if (cloneInstances != null) {
					List<ComponentInstance> createdInstances = cloneInstances.left;
					Map<String, String> oldCompUidToNew = cloneInstances.right;
					if (members != null && createdInstances != null) {

						Map<String, String> compInstIdToName = createdInstances.stream()
								.collect(Collectors.toMap(p -> p.getUniqueId(), p -> p.getName()));

						Map<String, String> membersToCreate = new HashMap<>();

						for (String oldCompInstId : members.values()) {
							String newCompInstUid = oldCompUidToNew.get(oldCompInstId);
							if (newCompInstUid == null) {
								result = Either.right(StorageOperationStatus.MATCH_NOT_FOUND);
								return result;
							}
							String newCompInstName = compInstIdToName.get(newCompInstUid);
							membersToCreate.put(newCompInstName, newCompInstUid);
						}

						gdToCreate.setMembers(membersToCreate);
					}
				}

				log.debug("The group definition for creation is {}", gdToCreate);

				groupsToCreate.add(gdToCreate);
			}

		}

		return result;
	}

	@Override
	public Either<List<GroupDefinition>, StorageOperationStatus> addGroups(NodeTypeEnum nodeTypeEnum,
			String componentId, List<GroupDefinition> groups, boolean inTransaction) {

		List<GroupDefinition> createdGroups = new ArrayList<>();

		Either<List<GroupDefinition>, StorageOperationStatus> result = null;

		try {

			if (groups != null) {
				for (GroupDefinition groupDefinition : groups) {
					Either<GroupDefinition, StorageOperationStatus> addGroup = this.addGroup(nodeTypeEnum, componentId,
							groupDefinition, true);
					if (addGroup.isRight()) {
						StorageOperationStatus status = addGroup.right().value();
						result = Either.right(status);
						return result;
					}

					createdGroups.add(addGroup.left().value());
				}
			}

			result = Either.left(createdGroups);
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
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

	@Override
	public Either<List<String>, TitanOperationStatus> getAssociatedGroupsToComponentInstanceFromGraph(
			String componentInstanceId) {

		List<String> groups = new ArrayList<>();
		Either<List<String>, TitanOperationStatus> result = Either.left(groups);

		Either<List<ImmutablePair<GroupData, GraphEdge>>, TitanOperationStatus> parentNodes = titanGenericDao
				.getParentNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), componentInstanceId,
						GraphEdgeLabels.GROUP_MEMBER, NodeTypeEnum.Group, GroupData.class);

		if (parentNodes.isRight()) {
			TitanOperationStatus status = parentNodes.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				BeEcompErrorManager.getInstance().logBeFailedFindParentError("FetchGroupMembers", componentInstanceId,
						status.name());
			}
			return Either.right(status);
		}

		List<ImmutablePair<GroupData, GraphEdge>> fetchedGroups = parentNodes.left().value();
		if (fetchedGroups != null) {
			List<String> list = fetchedGroups.stream().map(p -> p.left.getGroupDataDefinition().getUniqueId())
					.collect(Collectors.toList());
			groups.addAll(list);
		}

		return result;

	}

	@Override
	public Either<List<String>, StorageOperationStatus> getAssociatedGroupsToComponentInstance(
			String componentInstanceId, boolean inTransaction) {

		Either<List<String>, StorageOperationStatus> result = null;

		try {

			Either<List<String>, TitanOperationStatus> groups = this
					.getAssociatedGroupsToComponentInstanceFromGraph(componentInstanceId);

			if (groups.isRight()) {
				TitanOperationStatus status = groups.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.OK;
				}
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			List<String> list = groups.left().value();

			return Either.left(list);

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
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

	@Override
	public Either<List<String>, StorageOperationStatus> getAssociatedGroupsToComponentInstance(
			String componentInstanceId) {
		return getAssociatedGroupsToComponentInstance(componentInstanceId, false);
	}

	@Override
	public Either<List<GraphRelation>, TitanOperationStatus> associateGroupsToComponentInstanceOnGraph(
			List<String> groups, String componentInstanceId, String compInstName) {

		List<GraphRelation> relations = new ArrayList<>();
		Either<List<GraphRelation>, TitanOperationStatus> result = Either.left(relations);

		if (groups != null && false == groups.isEmpty()) {

			UniqueIdData compInstData = new UniqueIdData(NodeTypeEnum.ResourceInstance, componentInstanceId);

			for (String groupId : groups) {
				UniqueIdData groupData = new UniqueIdData(NodeTypeEnum.Group, groupId);

				Map<String, Object> props = new HashMap<String, Object>();
				props.put(GraphPropertiesDictionary.NAME.getProperty(), compInstName);
				Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(groupData,
						compInstData, GraphEdgeLabels.GROUP_MEMBER, props);
				if (createRelation.isRight()) {
					TitanOperationStatus status = createRelation.right().value();
					String description = "Failed to associate group " + groupData.getUniqueId()
							+ " to component instance " + compInstName + " in graph. Status is " + status;
					BeEcompErrorManager.getInstance().logInternalFlowError(ASSOCIATING_GROUP_TO_COMP_INST, description,
							ErrorSeverity.ERROR);
					result = Either.right(status);
					break;
				}
				GraphRelation graphRelation = createRelation.left().value();
				relations.add(graphRelation);
			}
		} else {
			result = Either.right(TitanOperationStatus.OK);
		}

		return result;
	}

	public StorageOperationStatus associateGroupsToComponentInstance(List<String> groups, String componentInstanceId,
			String compInstName) {

		return associateGroupsToComponentInstance(groups, componentInstanceId, compInstName, false);
	}

	@Override
	public StorageOperationStatus associateGroupsToComponentInstance(List<String> groups, String componentInstanceId,
			String compInstName, boolean inTransaction) {

		StorageOperationStatus result = null;

		try {
			Either<List<GraphRelation>, TitanOperationStatus> either = this
					.associateGroupsToComponentInstanceOnGraph(groups, componentInstanceId, compInstName);

			if (either.isRight()) {
				TitanOperationStatus status = either.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.OK;
				}
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

	@Override
	public Either<List<GraphRelation>, TitanOperationStatus> dissociateAllGroupsFromArtifactOnGraph(String componentId,
			NodeTypeEnum componentTypeEnum, String artifactId) {

		List<GraphRelation> relations = new ArrayList<>();
		Either<List<GraphRelation>, TitanOperationStatus> result = Either.left(relations);

		Either<List<GroupDefinition>, TitanOperationStatus> allGroupsFromGraph = getAllGroupsFromGraph(componentId,
				componentTypeEnum, true, true, false);
		if (allGroupsFromGraph.isRight()) {
			TitanOperationStatus status = allGroupsFromGraph.right().value();
			return Either.right(status);
		}

		List<GroupDefinition> allGroups = allGroupsFromGraph.left().value();
		if (allGroups == null || allGroups.isEmpty()) {
			return Either.right(TitanOperationStatus.OK);
		}

		// Find all groups which contains this artifact id
		List<GroupDefinition> associatedGroups = allGroups.stream()
				.filter(p -> p.getArtifacts() != null && p.getArtifacts().contains(artifactId))
				.collect(Collectors.toList());

		if (associatedGroups != null && false == associatedGroups.isEmpty()) {
			log.debug("The groups {} contains the artifact {}", associatedGroups.stream().map(p -> p.getName()).collect(Collectors.toList()), artifactId);

			UniqueIdData artifactData = new UniqueIdData(NodeTypeEnum.ArtifactRef, artifactId);
			for (GroupDefinition groupDefinition : associatedGroups) {
				UniqueIdData groupData = new UniqueIdData(NodeTypeEnum.Group, groupDefinition.getUniqueId());
				Either<GraphRelation, TitanOperationStatus> deleteRelation = titanGenericDao.deleteRelation(groupData,
						artifactData, GraphEdgeLabels.GROUP_ARTIFACT_REF);
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

	public Either<GroupDefinition, TitanOperationStatus> getGroupFromGraph(String uniqueId, boolean skipProperties,
			boolean skipMembers, boolean skipArtifacts) {

		Either<GroupDefinition, TitanOperationStatus> result = null;

		Either<GroupData, TitanOperationStatus> groupRes = titanGenericDao
				.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), uniqueId, GroupData.class);
		if (groupRes.isRight()) {
			TitanOperationStatus status = groupRes.right().value();
			log.debug("Failed to retrieve group {}  from graph. Status is {}", uniqueId, status);
			BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("Fetch Group", uniqueId,
					String.valueOf(status));
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
				groupDefinition.setProperties(properties);
			}
		}

		if (false == skipArtifacts) {
			Either<List<ImmutablePair<String, String>>, TitanOperationStatus> artifactsRes = getGroupArtifactsPairs(
					uniqueId);
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

	protected Either<List<GroupDefinition>, TitanOperationStatus> getAllGroupsFromGraph(String componentId,
			NodeTypeEnum componentTypeEnum, boolean skipProperties, boolean skipMembers, boolean skipArtifacts) {

		List<GroupDefinition> groups = new ArrayList<GroupDefinition>();

		Either<List<ImmutablePair<GroupData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao
				.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(componentTypeEnum), componentId,
						GraphEdgeLabels.GROUP, NodeTypeEnum.Group, GroupData.class);

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
			Either<GroupDefinition, TitanOperationStatus> groupRes = this.getGroupFromGraph(groupUniqueId,
					skipProperties, skipMembers, skipArtifacts);

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

	@Override
	public StorageOperationStatus dissociateAllGroupsFromArtifact(String componentId, NodeTypeEnum componentTypeEnum,
			String artifactId, boolean inTransaction) {

		StorageOperationStatus result = null;

		try {
			Either<List<GraphRelation>, TitanOperationStatus> either = this
					.dissociateAllGroupsFromArtifactOnGraph(componentId, componentTypeEnum, artifactId);

			if (either.isRight()) {
				TitanOperationStatus status = either.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.OK;
				}
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

	@Override
	public StorageOperationStatus dissociateAllGroupsFromArtifact(String componentId, NodeTypeEnum componentTypeEnum,
			String artifactId) {

		return dissociateAllGroupsFromArtifact(componentId, componentTypeEnum, artifactId, false);
	}

	@Override
	public TitanOperationStatus dissociateAndAssociateGroupsFromArtifactOnGraph(String componentId,
			NodeTypeEnum componentTypeEnum, String oldArtifactId, ArtifactData newArtifact) {

		Either<List<GroupDefinition>, TitanOperationStatus> allGroupsFromGraph = getAllGroupsFromGraph(componentId,
				componentTypeEnum, true, true, false);
		if (allGroupsFromGraph.isRight()) {
			TitanOperationStatus status = allGroupsFromGraph.right().value();
			return status;
		}

		List<GroupDefinition> allGroups = allGroupsFromGraph.left().value();
		if (allGroups == null || allGroups.isEmpty()) {
			return TitanOperationStatus.OK;
		}

		// Find all groups which contains this artifact id
		List<GroupDefinition> associatedGroups = allGroups.stream()
				.filter(p -> p.getArtifacts() != null && p.getArtifacts().contains(oldArtifactId))
				.collect(Collectors.toList());

		if (associatedGroups != null && false == associatedGroups.isEmpty()) {

			log.debug("The groups {} contains the artifact {}", associatedGroups.stream().map(p -> p.getName()).collect(Collectors.toList()), oldArtifactId);

			UniqueIdData oldArtifactData = new UniqueIdData(NodeTypeEnum.ArtifactRef, oldArtifactId);
			UniqueIdData newArtifactData = new UniqueIdData(NodeTypeEnum.ArtifactRef,
					newArtifact.getArtifactDataDefinition().getUniqueId());
			Map<String, Object> props = new HashMap<String, Object>();
			props.put(GraphPropertiesDictionary.NAME.getProperty(), newArtifactData.getLabel());

			for (GroupDefinition groupDefinition : associatedGroups) {
				UniqueIdData groupData = new UniqueIdData(NodeTypeEnum.Group, groupDefinition.getUniqueId());

				Either<GraphRelation, TitanOperationStatus> deleteRelation = titanGenericDao.deleteRelation(groupData,
						oldArtifactData, GraphEdgeLabels.GROUP_ARTIFACT_REF);
				log.trace("After dissociate group {} from artifac {}", groupDefinition.getName(), oldArtifactId);
				if (deleteRelation.isRight()) {
					TitanOperationStatus status = deleteRelation.right().value();
					if (status == TitanOperationStatus.NOT_FOUND) {
						status = TitanOperationStatus.INVALID_ID;
					}
					return status;
				}

				Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(groupData,
						newArtifactData, GraphEdgeLabels.GROUP_ARTIFACT_REF, props);
				log.trace("After associate group {} to artifact {}", groupDefinition.getName(), newArtifact.getUniqueIdKey());
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
	public StorageOperationStatus dissociateAndAssociateGroupsFromArtifact(String componentId,
			NodeTypeEnum componentTypeEnum, String oldArtifactId, ArtifactData newArtifact, boolean inTransaction) {

		StorageOperationStatus result = null;

		try {
			TitanOperationStatus status = this.dissociateAndAssociateGroupsFromArtifactOnGraph(componentId,
					componentTypeEnum, oldArtifactId, newArtifact);

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

	@Override
	public StorageOperationStatus dissociateAndAssociateGroupsFromArtifact(String componentId,
			NodeTypeEnum componentTypeEnum, String oldArtifactId, ArtifactData newArtifact) {
		return dissociateAndAssociateGroupsFromArtifact(componentId, componentTypeEnum, oldArtifactId, newArtifact,
				false);
	}

	private Either<List<ImmutablePair<String, String>>, TitanOperationStatus> getGroupArtifactsPairs(
			String groupUniqueId) {

		Either<List<ImmutablePair<String, String>>, TitanOperationStatus> result = null;

		Either<List<ImmutablePair<ArtifactData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao
				.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Group), groupUniqueId,
						GraphEdgeLabels.GROUP_ARTIFACT_REF, NodeTypeEnum.ArtifactRef, ArtifactData.class);
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

	public Either<List<GroupDefinition>, TitanOperationStatus> updateGroupVersionOnGraph(List<String> groupsUniqueId) {

		if (groupsUniqueId != null) {

			List<GroupDefinition> groups = new ArrayList<>();
			for (String groupUid : groupsUniqueId) {
				Either<GroupDefinition, TitanOperationStatus> either = updateGroupVersionOnGraph(groupUid);
				if (either.isRight()) {
					log.debug("Failed to update version of group {}", groupUid);
					return Either.right(either.right().value());
				}
				groups.add(either.left().value());
			}
			return Either.left(groups);
		}

		return Either.right(TitanOperationStatus.OK);

	}

	/**
	 * update the group version of a given group. It also creates a new UUID.
	 * 
	 * @param groupUniqueId
	 * @return
	 */
	public Either<GroupDefinition, TitanOperationStatus> updateGroupVersionOnGraph(String groupUniqueId) {

		Either<GroupDefinition, TitanOperationStatus> groupFromGraph = this.getGroupFromGraph(groupUniqueId, false,
				false, false);

		if (groupFromGraph.isRight()) {
			TitanOperationStatus status = groupFromGraph.right().value();
			return Either.right(status);
		} else {
			GroupDefinition groupDefinition = groupFromGraph.left().value();
			String version = groupDefinition.getVersion();
			String newVersion = increaseMajorVersion(version);
			Integer pvCounter = groupDefinition.getPropertyValueCounter();

			GroupData groupData = new GroupData();
			groupData.getGroupDataDefinition().setUniqueId(groupUniqueId);
			groupData.getGroupDataDefinition().setVersion(newVersion);
			groupData.getGroupDataDefinition().setPropertyValueCounter(pvCounter);

			String groupUUID = UniqueIdBuilder.generateUUID();
			groupData.getGroupDataDefinition().setGroupUUID(groupUUID);

			Either<GroupData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(groupData, GroupData.class);
			if (updateNode.isRight()) {
				return Either.right(updateNode.right().value());
			} else {
				groupFromGraph = this.getGroupFromGraph(groupUniqueId, false, false, false);
				return groupFromGraph;
			}

		}
	}

	/**
	 * The version of the group is an integer. In order to support BC, we might
	 * get a version in a float format.
	 * 
	 * @param version
	 * @return
	 */
	private String increaseMajorVersion(String version) {

		String[] versionParts = version.split(LifecycleOperation.VERSION_DELIMETER_REGEXP);
		Integer majorVersion = Integer.parseInt(versionParts[0]);

		majorVersion++;

		return String.valueOf(majorVersion);

	}

	public Either<GroupDefinition, TitanOperationStatus> associateArtifactsToGroupOnGraph(String groupId,
			List<String> artifactsId) {

		if (artifactsId == null || artifactsId.isEmpty()) {
			return Either.right(TitanOperationStatus.OK);
		}

		for (String artifactId : artifactsId) {
			Either<ArtifactData, TitanOperationStatus> findArtifactRes = titanGenericDao.getNode(
					UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef), artifactId, ArtifactData.class);
			if (findArtifactRes.isRight()) {
				TitanOperationStatus status = findArtifactRes.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
				}
				String description = "Failed to associate group " + groupId + " to artifact " + artifactId
						+ " in graph. Status is " + status;
				BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description, ErrorSeverity.ERROR);
				return Either.right(status);
			}

			Map<String, Object> props = new HashMap<String, Object>();
			props.put(GraphPropertiesDictionary.NAME.getProperty(), findArtifactRes.left().value().getLabel());

			GraphNode groupData = new UniqueIdData(NodeTypeEnum.Group, groupId);
			Either<GraphRelation, TitanOperationStatus> addArtifactsRefResult = titanGenericDao.createRelation(
					groupData, findArtifactRes.left().value(), GraphEdgeLabels.GROUP_ARTIFACT_REF, props);

			if (addArtifactsRefResult.isRight()) {
				TitanOperationStatus status = addArtifactsRefResult.right().value();
				String description = "Failed to associate group " + groupData.getUniqueId() + " to artifact "
						+ artifactId + " in graph. Status is " + status;
				BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description, ErrorSeverity.ERROR);
				return Either.right(status);
			}
		}

		Either<GroupDefinition, TitanOperationStatus> groupFromGraph = this.getGroupFromGraph(groupId, true, true,
				false);

		return groupFromGraph;
	}

	public Either<GroupDefinition, TitanOperationStatus> associateMembersToGroupOnGraph(String groupId,
			Map<String, String> members) {

		if (members != null && false == members.isEmpty()) {
			Either<GraphRelation, TitanOperationStatus> addMembersRefResult = null;
			for (Entry<String, String> member : members.entrySet()) {
				Either<ComponentInstanceData, TitanOperationStatus> findComponentInstanceRes = titanGenericDao.getNode(
						UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), member.getValue(),
						ComponentInstanceData.class);
				if (findComponentInstanceRes.isRight()) {

					TitanOperationStatus status = findComponentInstanceRes.right().value();
					if (status == TitanOperationStatus.NOT_FOUND) {
						status = TitanOperationStatus.INVALID_ID;
					}
					String description = "Failed to find to find component instance group " + member.getValue()
							+ " in graph. Status is " + status;
					BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description,
							ErrorSeverity.ERROR);
					return Either.right(status);
				}
				Map<String, Object> props = new HashMap<String, Object>();
				props.put(GraphPropertiesDictionary.NAME.getProperty(), member.getKey());
				GraphNode groupData = new UniqueIdData(NodeTypeEnum.Group, groupId);
				addMembersRefResult = titanGenericDao.createRelation(groupData, findComponentInstanceRes.left().value(),
						GraphEdgeLabels.GROUP_MEMBER, props);
				if (addMembersRefResult.isRight()) {
					TitanOperationStatus status = addMembersRefResult.right().value();
					String description = "Failed to associate group " + groupData.getUniqueId()
							+ " to component instance " + member.getValue() + " in graph. Status is " + status;
					BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description,
							ErrorSeverity.ERROR);
					return Either.right(status);
				}
			}
		}

		Either<GroupDefinition, TitanOperationStatus> groupFromGraph = this.getGroupFromGraph(groupId, true, false,
				true);

		return groupFromGraph;
	}

	public Either<GroupDefinition, TitanOperationStatus> dissociateArtifactsFromGroupOnGraph(String groupId,
			List<String> artifactsId) {

		if (artifactsId == null || artifactsId.isEmpty()) {
			return Either.right(TitanOperationStatus.OK);
		}

		UniqueIdData groupData = new UniqueIdData(NodeTypeEnum.Group, groupId);
		for (String artifactId : artifactsId) {

			UniqueIdData artifactData = new UniqueIdData(NodeTypeEnum.Group, artifactId);
			Either<GraphRelation, TitanOperationStatus> deleteRelation = titanGenericDao.deleteRelation(groupData,
					artifactData, GraphEdgeLabels.GROUP_ARTIFACT_REF);
			log.trace("After dissociate group {} from artifact {}", groupId, artifactId);

			if (deleteRelation.isRight()) {
				TitanOperationStatus status = deleteRelation.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
				}
				String description = "Failed to diassociate group " + groupId + " from artifact " + artifactId
						+ " in graph. Status is " + status;
				BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description, ErrorSeverity.ERROR);
				return Either.right(status);
			}

		}

		Either<GroupDefinition, TitanOperationStatus> groupFromGraph = this.getGroupFromGraph(groupId, true, true,
				false);

		return groupFromGraph;

	}

	public Either<GroupDefinition, TitanOperationStatus> dissociateMembersFromGroupOnGraph(String groupId,
			Map<String, String> members) {

		if (members == null || members.isEmpty()) {
			return Either.right(TitanOperationStatus.OK);
		}

		UniqueIdData groupData = new UniqueIdData(NodeTypeEnum.Group, groupId);
		for (Entry<String, String> member : members.entrySet()) {

			UniqueIdData artifactData = new UniqueIdData(NodeTypeEnum.Group, member.getValue());
			Either<GraphRelation, TitanOperationStatus> deleteRelation = titanGenericDao.deleteRelation(groupData,
					artifactData, GraphEdgeLabels.GROUP_MEMBER);
			log.trace("After dissociate group {} from members", groupId, member.getValue());

			if (deleteRelation.isRight()) {
				TitanOperationStatus status = deleteRelation.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
				}
				String description = "Failed to diassociate group " + groupId + " from member " + member.getValue()
						+ " in graph. Status is " + status;
				BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description, ErrorSeverity.ERROR);
				return Either.right(status);
			}

		}

		Either<GroupDefinition, TitanOperationStatus> groupFromGraph = this.getGroupFromGraph(groupId, true, true,
				false);

		return groupFromGraph;

	}

	/**
	 * dissociate artifacts from a group. It do not delete the artifacts !!!
	 * 
	 * @param groupId
	 * @param artifactsId
	 * @param inTransaction
	 * @return
	 */
	public Either<GroupDefinition, StorageOperationStatus> dissociateArtifactsFromGroup(String groupId,
			List<String> artifactsId, boolean inTransaction) {

		Either<GroupDefinition, StorageOperationStatus> result = null;

		try {
			Either<GroupDefinition, TitanOperationStatus> titanRes = this.dissociateArtifactsFromGroupOnGraph(groupId,
					artifactsId);

			if (titanRes.isRight()) {
				StorageOperationStatus storageOperationStatus = DaoStatusConverter
						.convertTitanStatusToStorageStatus(titanRes.right().value());
				result = Either.right(storageOperationStatus);
				return result;
			}

			result = Either.left(titanRes.left().value());
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
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

	public Either<GroupDefinition, StorageOperationStatus> dissociateMembersFromGroup(String groupId,
			Map<String, String> members, boolean inTransaction) {

		Either<GroupDefinition, StorageOperationStatus> result = null;

		try {
			Either<GroupDefinition, TitanOperationStatus> titanRes = this.dissociateMembersFromGroupOnGraph(groupId,
					members);

			if (titanRes.isRight()) {
				StorageOperationStatus storageOperationStatus = DaoStatusConverter
						.convertTitanStatusToStorageStatus(titanRes.right().value());
				result = Either.right(storageOperationStatus);
				return result;
			}

			result = Either.left(titanRes.left().value());
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
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

	/**
	 * Associate artifacts to a given group
	 * 
	 * @param groupId
	 * @param artifactsId
	 * @param inTransaction
	 * @return
	 */
	public Either<GroupDefinition, StorageOperationStatus> associateArtifactsToGroup(String groupId,
			List<String> artifactsId, boolean inTransaction) {

		Either<GroupDefinition, StorageOperationStatus> result = null;

		try {

			Either<GroupDefinition, TitanOperationStatus> titanRes = this.associateArtifactsToGroupOnGraph(groupId,
					artifactsId);

			if (titanRes.isRight()) {
				StorageOperationStatus status = DaoStatusConverter
						.convertTitanStatusToStorageStatus(titanRes.right().value());
				result = Either.right(status);
			}

			result = Either.left(titanRes.left().value());
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
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

	/**
	 * Associate artifacts to a given group
	 * 
	 * @param groupId
	 * @param artifactsId
	 * @param inTransaction
	 * @return
	 */
	public Either<GroupDefinition, StorageOperationStatus> associateMembersToGroup(String groupId,
			Map<String, String> members, boolean inTransaction) {

		Either<GroupDefinition, StorageOperationStatus> result = null;

		try {

			Either<GroupDefinition, TitanOperationStatus> titanRes = this.associateMembersToGroupOnGraph(groupId,
					members);

			if (titanRes.isRight()) {
				StorageOperationStatus status = DaoStatusConverter
						.convertTitanStatusToStorageStatus(titanRes.right().value());
				result = Either.right(status);
				return result;
			}

			result = Either.left(titanRes.left().value());
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
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

	public Either<List<GroupDefinition>, StorageOperationStatus> updateGroupVersion(List<String> groupsId,
			boolean inTransaction) {

		Either<List<GroupDefinition>, StorageOperationStatus> result = null;

		try {
			Either<List<GroupDefinition>, TitanOperationStatus> updateGroupVersionOnGraph = this
					.updateGroupVersionOnGraph(groupsId);

			if (updateGroupVersionOnGraph.isRight()) {
				result = Either.right(DaoStatusConverter
						.convertTitanStatusToStorageStatus(updateGroupVersionOnGraph.right().value()));
				return result;
			}

			result = Either.left(updateGroupVersionOnGraph.left().value());
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
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

	public Either<GroupDefinition, StorageOperationStatus> updateGroupName(String uniqueId, String newName,
			boolean inTransaction) {
		Either<GroupDefinition, StorageOperationStatus> result = null;

		try {
			Either<GroupDefinition, TitanOperationStatus> updateGroupNameOnGraph = this.updateGroupNameOnGraph(uniqueId,
					newName);

			if (updateGroupNameOnGraph.isRight()) {
				result = Either.right(
						DaoStatusConverter.convertTitanStatusToStorageStatus(updateGroupNameOnGraph.right().value()));
				return result;
			}

			result = Either.left(updateGroupNameOnGraph.left().value());
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
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

	private Either<GroupDefinition, TitanOperationStatus> updateGroupNameOnGraph(String uniqueId, String newName) {

		Either<GroupDefinition, TitanOperationStatus> groupFromGraph = this.getGroupFromGraph(uniqueId, false, false,
				false);

		if (groupFromGraph.isRight()) {
			TitanOperationStatus status = groupFromGraph.right().value();
			return Either.right(status);
		} else {
			GroupDefinition groupDefinition = groupFromGraph.left().value();
			String version = groupDefinition.getVersion();
			String newVersion = increaseMajorVersion(version);
			Integer pvCounter = groupDefinition.getPropertyValueCounter();

			GroupData groupData = new GroupData();
			groupData.getGroupDataDefinition().setUniqueId(uniqueId);
			groupData.getGroupDataDefinition().setVersion(newVersion);
			groupData.getGroupDataDefinition().setName(newName);
			groupData.getGroupDataDefinition().setPropertyValueCounter(pvCounter);

			Either<GroupData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(groupData, GroupData.class);
			if (updateNode.isRight()) {
				return Either.right(updateNode.right().value());
			} else {
				groupFromGraph = this.getGroupFromGraph(uniqueId, false, false, false);
				return groupFromGraph;
			}
		}
	}
}
