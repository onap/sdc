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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.graph.GraphElementFactory;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;

import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.dao.utils.Constants;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import org.openecomp.sdc.be.model.ArtifactDefinition;

import org.openecomp.sdc.be.model.ComponentInstance;

import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.IComponentInstanceConnectedElement;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.AttributeValueData;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.GroupData;
import org.openecomp.sdc.be.resources.data.GroupInstanceData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@org.springframework.stereotype.Component("group-instance-operation")
public class GroupInstanceOperation extends AbstractOperation implements IGroupInstanceOperation {

	private static String ADDING_GROUP = "AddingGroupInstance";

	private static Logger log = LoggerFactory.getLogger(GroupInstanceOperation.class.getName());

	@Autowired
	TitanGenericDao titanGenericDao;
	@Autowired
	GroupOperation groupOperation;

	@Autowired
	PropertyOperation propertyOperation;

	@javax.annotation.Resource
	private ApplicationDataTypeCache dataTypeCache;

	@Override
	public Either<GroupInstance, StorageOperationStatus> createGroupInstance(String componentInstId, GroupInstance groupInstance, boolean isCreateLogicalName) {
		Either<GroupInstance, StorageOperationStatus> result = null;

		if (!ValidationUtils.validateStringNotEmpty(groupInstance.getCustomizationUUID())) {
			generateCustomizationUUID(groupInstance);
		}

		Either<GroupInstance, TitanOperationStatus> addRes = addGroupInstanceToComponentInstance(componentInstId,  isCreateLogicalName, groupInstance);
		if (addRes.isRight()) {
			TitanOperationStatus status = addRes.right().value();
			log.error("Failed to add resource instance {} to service {}. status is {}", groupInstance, componentInstId, status);
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			return result;
		}

		GroupInstance value = addRes.left().value();
		result = Either.left(value);

		return result;

	}

	@Override
	public Either<GroupInstance, StorageOperationStatus> createGroupInstance(TitanVertex ciVertex, String componentInstId,  GroupInstance groupInstance, boolean isCreateLogicalName) {
		Either<GroupInstance, StorageOperationStatus> result = null;

		if (!ValidationUtils.validateStringNotEmpty(groupInstance.getCustomizationUUID())) {
			generateCustomizationUUID(groupInstance);
		}

		Either<TitanVertex, TitanOperationStatus> addComponentInstanceToContainerComponent = addGroupInstanceToContainerComponent(ciVertex, componentInstId, isCreateLogicalName, groupInstance);

		if (addComponentInstanceToContainerComponent.isRight()) {
			TitanOperationStatus status = addComponentInstanceToContainerComponent.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		TitanVertex giVertex = addComponentInstanceToContainerComponent.left().value();
		Map<String, Object> properties = titanGenericDao.getProperties(giVertex);
		GroupInstanceData createdGroupInstanceData = GraphElementFactory.createElement(NodeTypeEnum.GroupInstance.getName(), GraphElementTypeEnum.Node, properties, GroupInstanceData.class);

		GroupInstance createdGroupInstance = new GroupInstance(createdGroupInstanceData.getGroupDataDefinition());
		createdGroupInstance.setGroupName(groupInstance.getGroupName());

		createdGroupInstance.setArtifacts(groupInstance.getArtifacts());

		result = Either.left(createdGroupInstance);

		return result;

	}

	@Override
	public Either<GroupInstance, StorageOperationStatus> deleteGroupInstanceInstance(NodeTypeEnum containerNodeType, String containerComponentId, String groupInstUid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<GroupInstance, StorageOperationStatus> updateGroupInstance(String serviceId, NodeTypeEnum nodeType, String resourceInstanceName, ComponentInstance resourceInstance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<List<GroupInstance>, StorageOperationStatus> getAllGroupInstances(String parentId, NodeTypeEnum parentType) {
		Either<List<GroupInstance>, StorageOperationStatus> result = null;
		List<GroupInstance> groupInstanceRes = new ArrayList<>();

		Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
		if (graph.isRight()) {
			log.debug("Failed to work with graph {}", graph.right().value());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graph.right().value()));
		}
		TitanGraph tGraph = graph.left().value();
		@SuppressWarnings("unchecked")
		Iterable<TitanVertex> vertices = tGraph.query().has(UniqueIdBuilder.getKeyByNodeType(parentType), parentId).vertices();
		if (vertices == null || vertices.iterator() == null || false == vertices.iterator().hasNext()) {
			log.debug("No nodes for type {}  for id = {}", parentType, parentId);
			result = Either.right(StorageOperationStatus.NOT_FOUND);
			return result;
		}

		Iterator<TitanVertex> iterator = vertices.iterator();
		Vertex vertex = iterator.next();

		Map<String, Object> edgeProperties = null;

		Either<List<ImmutablePair<GroupInstanceData, GraphEdge>>, TitanOperationStatus> childrenByEdgeCriteria = titanGenericDao.getChildrenByEdgeCriteria(vertex, parentId, GraphEdgeLabels.GROUP_INST, NodeTypeEnum.GroupInstance,
				GroupInstanceData.class, edgeProperties);

		if (childrenByEdgeCriteria.isRight()) {
			TitanOperationStatus status = childrenByEdgeCriteria.right().value();
			log.debug("Failed to find group instance {} on graph", childrenByEdgeCriteria.right().value());

			if (status == TitanOperationStatus.NOT_FOUND) {
				result = Either.left(groupInstanceRes);
				return result;
			}
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

		}

		List<ImmutablePair<GroupInstanceData, GraphEdge>> list = childrenByEdgeCriteria.left().value();

		for (ImmutablePair<GroupInstanceData, GraphEdge> pair : list) {
			GroupInstanceData groupInstData = pair.getLeft();
			GroupInstance groupInstance = new GroupInstance(groupInstData.getGroupDataDefinition());
			String instOriginGroupId = groupInstance.getGroupUid();
			Either<GroupDefinition, TitanOperationStatus> groupRes = groupOperation.getGroupFromGraph(instOriginGroupId, false, true, false);

			if (groupRes.isRight()) {
				TitanOperationStatus status = groupRes.right().value();
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));

			}
			GroupDefinition groupDefinition = groupRes.left().value();
			Either<Map<String, PropertyValueData>, TitanOperationStatus> groupInstancePropertyValuesRes = getAllGroupInstancePropertyValuesData(groupInstData);
			if(groupInstancePropertyValuesRes.isRight()){
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(groupInstancePropertyValuesRes.right().value()));
			}
			buildGroupInstanceFromGroup(groupInstance, groupDefinition, groupInstancePropertyValuesRes.left().value());
			/*
			 * Either<List<GroupProperty>, TitanOperationStatus> groupInsPropStatus = getGroupInstanceProperties(groupInstance, groupDefinition); if (groupInsPropStatus.isRight()) {
			 * 
			 * return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(groupInsPropStatus.right().value())); }
			 */

			Either<List<ImmutablePair<String, String>>, TitanOperationStatus> artifactsRes = getGroupArtifactsPairs(groupInstance.getUniqueId());
			if (artifactsRes.isRight()) {
				TitanOperationStatus status = artifactsRes.right().value();
				if (status != TitanOperationStatus.OK) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
					return result;
				}
			} else {
				List<String> artifactsUid = new ArrayList<>();
				List<String> artifactsUUID = new ArrayList<>();

				List<ImmutablePair<String, String>> list1 = artifactsRes.left().value();
				if (list != null) {
					for (ImmutablePair<String, String> pair1 : list1) {
						String uid = pair1.left;
						String UUID = pair1.right;
						artifactsUid.add(uid);
						artifactsUUID.add(UUID);
					}
					groupInstance.setGroupInstanceArtifacts(artifactsUid);
					groupInstance.setGroupInstanceArtifactsUuid(artifactsUUID);
				}
			}

			groupInstanceRes.add(groupInstance);
			log.debug("GroupInstance {} was added to list ", groupInstance.getUniqueId());
		}

		result = Either.left(groupInstanceRes);
		return result;

	}

	@Override
	public Either<GroupInstance, TitanOperationStatus> getGroupInstanceById(String groupResourceId) {
		// TODO Auto-generated method stub
		return getGroupInstanceFromGraph(groupResourceId, false, false);
	}

	@Override
	public TitanOperationStatus deleteAllGroupInstances(String componentInstId) {

		return deleteAssociatedGroupInstances(componentInstId);
	}

	private TitanOperationStatus deleteAssociatedGroupInstances(String resourceInstanceUid) {
		final GraphEdgeLabels edgeConectingToRI = GraphEdgeLabels.GROUP_INST;
		final NodeTypeEnum elementTypeToDelete = NodeTypeEnum.GroupInstance;
		return deleteAssociatedRIElements(elementTypeToDelete, edgeConectingToRI, resourceInstanceUid, () -> GroupInstanceData.class);
	}

	private <T extends GraphNode> TitanOperationStatus deleteAssociatedRIElements(NodeTypeEnum elementTypeToDelete, GraphEdgeLabels edgeConectingToRI, String resourceInstanceUid, Supplier<Class<T>> classGen) {

		Either<List<ImmutablePair<T, GraphEdge>>, TitanOperationStatus> elementsNodesRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceUid, edgeConectingToRI, elementTypeToDelete,
				classGen.get());

		if (elementsNodesRes.isRight()) {
			TitanOperationStatus status = elementsNodesRes.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				BeEcompErrorManager.getInstance().logInternalFlowError("deleteAssociatedRIElements", "Failed to find the elements of resource instance " + resourceInstanceUid + ". status is " + status, ErrorSeverity.ERROR);
				return status;
			}
		} else {

			List<ImmutablePair<T, GraphEdge>> relationshipNodes = elementsNodesRes.left().value();
			if (relationshipNodes != null) {
				for (ImmutablePair<T, GraphEdge> immutablePair : relationshipNodes) {
					T elementValueDataData = immutablePair.getKey();
					Either<T, TitanOperationStatus> deleteNode = titanGenericDao.deleteNode(elementValueDataData, classGen.get());
					if (deleteNode.isRight()) {
						TitanOperationStatus status = deleteNode.right().value();
						BeEcompErrorManager.getInstance().logInternalFlowError("deleteAssociatedRIElements", "Failed to delete element value node " + elementValueDataData + ". status is " + status, ErrorSeverity.ERROR);
						return status;
					}
				}
			}

		}

		return TitanOperationStatus.OK;
	}

	@Override
	public Either<Integer, StorageOperationStatus> increaseAndGetGroupInstancePropertyCounter(String groupInstanceId) {
		Either<Integer, StorageOperationStatus> result = null;

		Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
		if (graphResult.isRight()) {
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graphResult.right().value()));
			return result;
		}
		Either<TitanVertex, TitanOperationStatus> vertexService = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.GroupInstance), groupInstanceId);
		if (vertexService.isRight()) {
			log.debug("failed to fetch vertex of resource instance for id = {}", groupInstanceId);
			TitanOperationStatus status = vertexService.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(vertexService.right().value()));
			return result;
		}
		Vertex vertex = vertexService.left().value();

		VertexProperty<Object> vertexProperty = vertex.property(GraphPropertiesDictionary.PROPERTY_COUNTER.getProperty());
		Integer counter = 0;
		if (vertexProperty.isPresent()) {
			if (vertexProperty.value() != null) {
				counter = (Integer) vertexProperty.value();
			}
		}

		counter++;
		vertex.property(GraphPropertiesDictionary.PROPERTY_COUNTER.getProperty(), counter);

		result = Either.left(counter);
		return result;

	}

	@Override
	public Either<Boolean, StorageOperationStatus> isGroupInstanceNameExist(String parentComponentId, NodeTypeEnum parentNodeType, String compInstId, String componentInstName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<ComponentInstance, StorageOperationStatus> getFullGroupInstance(ComponentInstance componentInstance, NodeTypeEnum compInstNodeType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<ComponentInstanceProperty, StorageOperationStatus> addPropertyValueToGroupInstance(ComponentInstanceProperty groupInstanceProperty, String groupInstanceId, Integer index, boolean inTransaction) {
		/// #RULES SUPPORT
		/// Ignore rules received from client till support
		groupInstanceProperty.setRules(null);
		///
		///

		Either<ComponentInstanceProperty, StorageOperationStatus> result = null;

		try {

			Either<PropertyValueData, TitanOperationStatus> eitherStatus = addPropertyToGroupInstance(groupInstanceProperty, groupInstanceId, index);

			if (eitherStatus.isRight()) {
				log.error("Failed to add property value {} to resource instance {} in Graph. status is {}", groupInstanceProperty, groupInstanceId, eitherStatus.right().value().name());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherStatus.right().value()));
				return result;
			} else {
				PropertyValueData propertyValueData = eitherStatus.left().value();

				ComponentInstanceProperty propertyValueResult = propertyOperation.buildResourceInstanceProperty(propertyValueData, groupInstanceProperty);
				log.debug("The returned GroupInstanceProperty is {}", propertyValueResult);

				Either<String, TitanOperationStatus> findDefaultValue = propertyOperation.findDefaultValueFromSecondPosition(groupInstanceProperty.getPath(), groupInstanceProperty.getUniqueId(), groupInstanceProperty.getDefaultValue());
				if (findDefaultValue.isRight()) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(findDefaultValue.right().value()));
					return result;
				}
				String defaultValue = findDefaultValue.left().value();
				propertyValueResult.setDefaultValue(defaultValue);
				log.debug("The returned default value in ResourceInstanceProperty is {}", defaultValue);

				result = Either.left(propertyValueResult);
				return result;
			}
		}

		finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	@Override
	public Either<ComponentInstanceProperty, StorageOperationStatus> addPropertyValueToGroupInstance(ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId, boolean isvalidate, Integer index, boolean inTransaction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Either<ComponentInstanceProperty, StorageOperationStatus> updatePropertyValueInGroupInstance(ComponentInstanceProperty gropuInstanceProperty, String groupInstanceId, boolean inTransaction) {
		// TODO Auto-generated method stub
		// change Propety class
		return null;
	}

	@Override
	public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> fetchCIEnvArtifacts(String componentInstanceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StorageOperationStatus updateCustomizationUUID(String groupInstanceId) {
		Either<TitanVertex, TitanOperationStatus> vertexByProperty = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), groupInstanceId);
		if (vertexByProperty.isRight()) {
			log.debug("Failed to fetch component instance by id {} error {}", groupInstanceId, vertexByProperty.right().value());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(vertexByProperty.right().value());
		}
		UUID uuid = UUID.randomUUID();
		TitanVertex ciVertex = vertexByProperty.left().value();
		ciVertex.property(GraphPropertiesDictionary.CUSTOMIZATION_UUID.getProperty(), uuid.toString());

		return StorageOperationStatus.OK;
	}

	public void generateCustomizationUUID(GroupInstance groupInstance) {
		UUID uuid = UUID.randomUUID();
		groupInstance.setCustomizationUUID(uuid.toString());
	}

	/**
	 * add property to resource instance
	 * 
	 * @param resourceInstanceProperty
	 * @param resourceInstanceId
	 * @param index
	 * @return
	 */
	public Either<PropertyValueData, TitanOperationStatus> addPropertyToGroupInstance(ComponentInstanceProperty groupInstanceProperty, String groupInstanceId, Integer index) {

		Either<GroupInstanceData, TitanOperationStatus> findResInstanceRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.GroupInstance), groupInstanceId, GroupInstanceData.class);

		if (findResInstanceRes.isRight()) {
			TitanOperationStatus status = findResInstanceRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		String propertyId = groupInstanceProperty.getUniqueId();
		Either<PropertyData, TitanOperationStatus> findPropertyDefRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property), propertyId, PropertyData.class);

		if (findPropertyDefRes.isRight()) {
			TitanOperationStatus status = findPropertyDefRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		String valueUniqueUid = groupInstanceProperty.getValueUniqueUid();
		if (valueUniqueUid == null) {

			PropertyData propertyData = findPropertyDefRes.left().value();
			GroupInstanceData resourceInstanceData = findResInstanceRes.left().value();

			ImmutablePair<TitanOperationStatus, String> isPropertyValueExists = propertyOperation.findPropertyValue(groupInstanceId, propertyId);
			if (isPropertyValueExists.getLeft() == TitanOperationStatus.ALREADY_EXIST) {
				log.debug("The property {} already added to the resource instance {}", propertyId, groupInstanceId);
				groupInstanceProperty.setValueUniqueUid(isPropertyValueExists.getRight());
				Either<PropertyValueData, TitanOperationStatus> updatePropertyOfResourceInstance = updatePropertyOfGroupInstance(groupInstanceProperty, groupInstanceId);
				if (updatePropertyOfResourceInstance.isRight()) {
					BeEcompErrorManager.getInstance().logInternalFlowError("UpdatePropertyValueOnComponentInstance", "Failed to update property value on instance. Status is " + updatePropertyOfResourceInstance.right().value(), ErrorSeverity.ERROR);
					return Either.right(updatePropertyOfResourceInstance.right().value());
				}
				return Either.left(updatePropertyOfResourceInstance.left().value());
			}

			if (isPropertyValueExists.getLeft() != TitanOperationStatus.NOT_FOUND) {
				log.debug("After finding property value of {} on componenet instance {}", propertyId, groupInstanceId);
				return Either.right(isPropertyValueExists.getLeft());
			}

			String innerType = null;

			PropertyDataDefinition propDataDef = propertyData.getPropertyDataDefinition();
			String propertyType = propDataDef.getType();
			String value = groupInstanceProperty.getValue();
			ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);

			if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
				SchemaDefinition def = propDataDef.getSchema();
				if (def == null) {
					log.debug("Schema doesn't exists for property of type {}", type);
					return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
				}
				PropertyDataDefinition propDef = def.getProperty();
				if (propDef == null) {
					log.debug("Property in Schema Definition inside property of type {} doesn't exist", type);
					return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
				}
				innerType = propDef.getType();
			}

			log.debug("Before validateAndUpdatePropertyValue");
			Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = dataTypeCache.getAll();
			if (allDataTypes.isRight()) {
				TitanOperationStatus status = allDataTypes.right().value();
				BeEcompErrorManager.getInstance().logInternalFlowError("UpdatePropertyValueOnComponentInstance", "Failed to update property value on instance. Status is " + status, ErrorSeverity.ERROR);
				return Either.right(status);
			}
			Either<Object, Boolean> isValid = propertyOperation.validateAndUpdatePropertyValue(propertyType, value, innerType, allDataTypes.left().value());
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

			String uniqueId = UniqueIdBuilder.buildResourceInstancePropertyValueUid(resourceInstanceData.getUniqueId(), index);
			PropertyValueData propertyValueData = new PropertyValueData();
			propertyValueData.setUniqueId(uniqueId);
			propertyValueData.setValue(newValue);

			log.debug("Before validateAndUpdateRules");
			ImmutablePair<String, Boolean> pair = propertyOperation.validateAndUpdateRules(propertyType, groupInstanceProperty.getRules(), innerType, allDataTypes.left().value(), false);
			log.debug("After validateAndUpdateRules. pair = {}", pair);
			if (pair.getRight() != null && pair.getRight() == false) {
				BeEcompErrorManager.getInstance().logBeInvalidValueError("Add property value", pair.getLeft(), groupInstanceProperty.getName(), propertyType);
				return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
			}
			propertyOperation.addRulesToNewPropertyValue(propertyValueData, groupInstanceProperty, groupInstanceId);

			log.debug("Before adding property value to graph {}", propertyValueData);
			Either<PropertyValueData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(propertyValueData, PropertyValueData.class);
			log.debug("After adding property value to graph {}", propertyValueData);

			if (createNodeResult.isRight()) {
				TitanOperationStatus operationStatus = createNodeResult.right().value();
				return Either.right(operationStatus);
			}
			propertyValueData = createNodeResult.left().value();

			Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(propertyValueData, propertyData, GraphEdgeLabels.PROPERTY_IMPL, null);

			if (createRelResult.isRight()) {
				TitanOperationStatus operationStatus = createRelResult.right().value();
				log.error("Failed to associate property value {} to property {} in graph. status is {}", uniqueId, propertyId, operationStatus);
				return Either.right(operationStatus);
			}

			createRelResult = titanGenericDao.createRelation(resourceInstanceData, propertyValueData, GraphEdgeLabels.PROPERTY_VALUE, null);

			if (createRelResult.isRight()) {
				TitanOperationStatus operationStatus = createRelResult.right().value();
				log.error("Failed to associate resource instance {} property value {} in graph. status is {}", groupInstanceId, uniqueId, operationStatus);
				return Either.right(operationStatus);
			}

			return Either.left(propertyValueData);
		} else {
			log.error("property value already exists.");
			return Either.right(TitanOperationStatus.ALREADY_EXIST);
		}

	}

	public Either<ComponentInstanceProperty, TitanOperationStatus> addPropertyToResourceInstance(ComponentInstanceProperty groupInstanceProperty, TitanVertex groupInstanceVertex, Integer index, String groupInstanceId) {

		String propertyId = groupInstanceProperty.getUniqueId();
		Either<PropertyData, TitanOperationStatus> findPropertyDefRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property), propertyId, PropertyData.class);

		if (findPropertyDefRes.isRight()) {
			TitanOperationStatus status = findPropertyDefRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		String valueUniqueUid = groupInstanceProperty.getValueUniqueUid();
		if (valueUniqueUid == null) {

			PropertyData propertyData = findPropertyDefRes.left().value();

			ImmutablePair<TitanOperationStatus, String> isPropertyValueExists = propertyOperation.findPropertyValue(groupInstanceId, propertyId);
			if (isPropertyValueExists.getLeft() == TitanOperationStatus.ALREADY_EXIST) {
				log.trace("The property {} already added to the resource instance {}", propertyId, groupInstanceId);
				groupInstanceProperty.setValueUniqueUid(isPropertyValueExists.getRight());
				Either<PropertyValueData, TitanOperationStatus> updatePropertyOfResourceInstance = updatePropertyOfGroupInstance(groupInstanceProperty, groupInstanceId);
				if (updatePropertyOfResourceInstance.isRight()) {
					BeEcompErrorManager.getInstance().logInternalFlowError("UpdatePropertyValueOnComponentInstance", "Failed to update property value on instance. Status is " + updatePropertyOfResourceInstance.right().value(), ErrorSeverity.ERROR);
					return Either.right(updatePropertyOfResourceInstance.right().value());
				}
				return Either.right(TitanOperationStatus.OK);
			}

			if (isPropertyValueExists.getLeft() != TitanOperationStatus.NOT_FOUND) {
				log.trace("After finding property value of {} on componenet instance {}", propertyId, groupInstanceId);
				return Either.right(isPropertyValueExists.getLeft());
			}

			String innerType = null;

			PropertyDataDefinition propDataDef = propertyData.getPropertyDataDefinition();
			String propertyType = propDataDef.getType();
			String value = groupInstanceProperty.getValue();
			ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);

			if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
				SchemaDefinition def = propDataDef.getSchema();
				if (def == null) {
					log.debug("Schema doesn't exists for property of type {}", type);
					return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
				}
				PropertyDataDefinition propDef = def.getProperty();
				if (propDef == null) {
					log.debug("Property in Schema Definition inside property of type {} doesn't exist", type);
					return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
				}
				innerType = propDef.getType();
			}

			log.trace("Before validateAndUpdatePropertyValue");
			Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = dataTypeCache.getAll();
			if (allDataTypes.isRight()) {
				TitanOperationStatus status = allDataTypes.right().value();
				BeEcompErrorManager.getInstance().logInternalFlowError("UpdatePropertyValueOnComponentInstance", "Failed to update property value on instance. Status is " + status, ErrorSeverity.ERROR);
				return Either.right(status);
			}
			Either<Object, Boolean> isValid = propertyOperation.validateAndUpdatePropertyValue(propertyType, value, innerType, allDataTypes.left().value());
			log.trace("After validateAndUpdatePropertyValue. isValid = {}", isValid);

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

			String uniqueId = UniqueIdBuilder.buildResourceInstancePropertyValueUid(groupInstanceId, index);
			PropertyValueData propertyValueData = new PropertyValueData();
			propertyValueData.setUniqueId(uniqueId);
			propertyValueData.setValue(newValue);

			log.trace("Before validateAndUpdateRules");
			ImmutablePair<String, Boolean> pair = propertyOperation.validateAndUpdateRules(propertyType, groupInstanceProperty.getRules(), innerType, allDataTypes.left().value(), false);
			log.debug("After validateAndUpdateRules. pair = {} ", pair);
			if (pair.getRight() != null && pair.getRight() == false) {
				BeEcompErrorManager.getInstance().logBeInvalidValueError("Add property value", pair.getLeft(), groupInstanceProperty.getName(), propertyType);
				return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
			}
			propertyOperation.addRulesToNewPropertyValue(propertyValueData, groupInstanceProperty, groupInstanceId);

			log.trace("Before adding property value to graph {}", propertyValueData);
			Either<PropertyValueData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(propertyValueData, PropertyValueData.class);
			log.trace("After adding property value to graph {}", propertyValueData);

			if (createNodeResult.isRight()) {
				TitanOperationStatus operationStatus = createNodeResult.right().value();
				return Either.right(operationStatus);
			}
			propertyValueData = createNodeResult.left().value();

			Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(propertyValueData, propertyData, GraphEdgeLabels.PROPERTY_IMPL, null);

			if (createRelResult.isRight()) {
				TitanOperationStatus operationStatus = createRelResult.right().value();
				log.error("Failed to associate property value {} to property {} in graph. status is {}", uniqueId, propertyId, operationStatus);
				return Either.right(operationStatus);
			}

			TitanOperationStatus edgeResult = titanGenericDao.createEdge(groupInstanceVertex, propertyValueData, GraphEdgeLabels.PROPERTY_VALUE, null);

			if (edgeResult != TitanOperationStatus.OK) {
				log.error("Failed to associate resource instance {} property value {} in graph. status is {}", groupInstanceId, uniqueId, edgeResult);
				return Either.right(edgeResult);
			}

			ComponentInstanceProperty propertyValueResult = propertyOperation.buildResourceInstanceProperty(propertyValueData, groupInstanceProperty);
			log.debug("The returned ResourceInstanceProperty is {} ", propertyValueResult);

			return Either.left(propertyValueResult);
		} else {
			log.debug("property value already exists.");
			return Either.right(TitanOperationStatus.ALREADY_EXIST);
		}

	}

	public Either<GroupInstance, TitanOperationStatus> addGroupInstanceToComponentInstance(String componentInstanceId, boolean isCreateLogicaName, GroupInstance groupInstance) {
		log.debug("Going to create group instance {} in componentInstance {}", groupInstance, componentInstanceId);

		Either<TitanVertex, TitanOperationStatus> metadataVertex = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), componentInstanceId);
		if (metadataVertex.isRight()) {
			TitanOperationStatus status = metadataVertex.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}
		Either<TitanVertex, TitanOperationStatus> addComponentInstanceToContainerComponent = addGroupInstanceToContainerComponent(metadataVertex.left().value(), componentInstanceId, isCreateLogicaName, groupInstance);

		if (addComponentInstanceToContainerComponent.isRight()) {
			TitanOperationStatus status = addComponentInstanceToContainerComponent.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}
		TitanVertex ciVertex = addComponentInstanceToContainerComponent.left().value();
		Map<String, Object> properties = titanGenericDao.getProperties(ciVertex);
		GroupInstanceData createdComponentInstance = GraphElementFactory.createElement(NodeTypeEnum.GroupInstance.getName(), GraphElementTypeEnum.Node, properties, GroupInstanceData.class);

		GroupInstance createdResourceInstance = new GroupInstance(createdComponentInstance.getGroupDataDefinition());

		return Either.left(createdResourceInstance);

	}

	/**
	 * 
	 * @param containerComponentId
	 * @param containerNodeType
	 * @param instanceNumber
	 * @param isCreateLogicaName
	 * @param componentInstance
	 * @param compInstNodeType
	 * @param metadataVertex
	 * @return
	 */
	public Either<TitanVertex, TitanOperationStatus> addGroupInstanceToContainerComponent(TitanVertex ciVertex, String componentInstanceId,  boolean isCreateLogicaName, GroupInstance groupInstance) {
		TitanOperationStatus status = null;
		log.debug("Going to create group instance {} in component instance {}", groupInstance, componentInstanceId);
		String instOriginGroupId = groupInstance.getGroupUid();
		String logicalName = groupInstance.getName();
		if (isCreateLogicaName){
			String instanceName = (String) titanGenericDao.getProperty(ciVertex, GraphPropertiesDictionary.NORMALIZED_NAME.getProperty());
			logicalName = createGroupInstLogicalName(instanceName, groupInstance.getGroupName());
		}

		GroupInstanceData groupInstanceData = buildGroupInstanceData(groupInstance, componentInstanceId, logicalName);
		Either<TitanVertex, TitanOperationStatus> originVertexEither = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), instOriginGroupId);
		if (originVertexEither.isRight()) {
			log.debug("Failed to fetch vertex of origin resource for id {} error {}", instOriginGroupId, originVertexEither.right().value());
			return Either.right(originVertexEither.right().value());
		}
		TitanVertex originVertex = originVertexEither.left().value();

		// String originType = (String) titanGenericDao.getProperty(originVertex, GraphPropertiesDictionary.LABEL.getProperty());
		String groupType = (String) titanGenericDao.getProperty(originVertex, GraphPropertiesDictionary.TYPE.getProperty());
		// detectOriginType(originType, groupInstanceData, resourceType);

		log.trace("Before adding component instance to graph. componentInstanceData = {}", groupInstanceData);
		// groupInstanceData.getGroupDataDefinition().setGroupUid(groupType);

		Either<TitanVertex, TitanOperationStatus> createGIResult = titanGenericDao.createNode(groupInstanceData);

		log.debug("After adding component instance to graph. status is = {}", createGIResult);

		if (createGIResult.isRight()) {
			status = createGIResult.right().value();
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Add Component Instance");
			log.debug("Failed to create group instance node in graph. status is {}", status);
			return Either.right(status);
		}
		TitanVertex createdGroupInstanceVertex = createGIResult.left().value();
		TitanOperationStatus associateContainerRes = associateComponentInstanceToGroupInstance(ciVertex, createdGroupInstanceVertex, logicalName);

		String componentInstanceUniqueId = groupInstanceData.getUniqueId();
		if (associateContainerRes != TitanOperationStatus.OK) {
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Add Component Instance");
			log.debug("Failed to associate container component {} to component instance {}. Status is {}", componentInstanceId, componentInstanceUniqueId, associateContainerRes);
			return Either.right(associateContainerRes);
		}
		// String originId = (String) titanGenericDao.getProperty(createdGroupInstanceVertex, GraphPropertiesDictionary.TYPE.getProperty());

		TitanOperationStatus associateToInstOriginComponent = associateToInstOriginGroup(createdGroupInstanceVertex, originVertex, instOriginGroupId);
		if (associateToInstOriginComponent != TitanOperationStatus.OK) {
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Add Component Instance");
			log.debug("Failed to associate component instance {} to its origin component {}. Status is {}", componentInstanceUniqueId, groupInstanceData.getGroupDataDefinition().getGroupUid(), associateToInstOriginComponent);
			return Either.right(associateToInstOriginComponent);
		}

		// Capability instance with property values implementation

		if (status == null) {
			// ComponentInstance createdResourceInstance = new
			// ComponentInstance(createdComponentInstance.getComponentInstDataDefinition());
			//
			// String icon = (String) titanGenericDao.getProperty(originVertex,
			// GraphPropertiesDictionary.ICON.getProperty());
			// createdResourceInstance.setIcon(icon);
			return Either.left(createdGroupInstanceVertex);
		}
		return Either.right(status);
	}

	private GroupInstanceData buildGroupInstanceData(GroupInstance groupInstance, String componentInstanceId, String logicalName) {
		String ciOriginComponentUid = groupInstance.getGroupUid();

		GroupInstanceDataDefinition dataDefinition = new GroupInstanceDataDefinition(groupInstance);

		Long creationDate = groupInstance.getCreationTime();
		if (creationDate == null) {
			creationDate = System.currentTimeMillis();
		}
		dataDefinition.setCreationTime(creationDate);
		dataDefinition.setModificationTime(creationDate);
		// dataDefinition.setResourceUid(resourceUid);
		// String replacmentlogicalName = logicalName.replaceAll(" ",
		// "_").toLowerCase();
		dataDefinition.setName(logicalName);
		if (dataDefinition.getNormalizedName() == null)
			dataDefinition.setNormalizedName(ValidationUtils.normalizeComponentInstanceName(logicalName));
		dataDefinition.setUniqueId(UniqueIdBuilder.buildResourceInstanceUniuqeId(componentInstanceId, ciOriginComponentUid, dataDefinition.getNormalizedName()));

		GroupInstanceData resourceInstanceData = new GroupInstanceData(dataDefinition);

		return resourceInstanceData;
	}

	@Override
	public String createGroupInstLogicalName(String instanceName, String groupName) {

		String logicalName = buildGroupInstanceLogicalName(instanceName, groupName);

		return logicalName;
	}

	private String buildGroupInstanceLogicalName(String instanceName, String groupName) {
		return ValidationUtils.normalizeComponentInstanceName(instanceName) + ".." + groupName;
	}

	/**
	 * Make a relation between service to resource instance.
	 * 
	 * @param containerCompIdData
	 * @param componentInstanceData
	 * @param logicalName
	 * @return
	 */
	private Either<GraphRelation, TitanOperationStatus> associateComponentInstanceToGroupInstance(UniqueIdData compInstIdData, GroupInstanceData groupInstanceData, String logicalName) {
		Map<String, Object> properties = new HashMap<String, Object>();

		properties.put(GraphPropertiesDictionary.NAME.getProperty(), logicalName);
		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(compInstIdData, groupInstanceData, GraphEdgeLabels.GROUP_INST, properties);

		log.debug("After associating container component {} to resource instance {} with logical name {}. Status is {}", compInstIdData.getUniqueId(), groupInstanceData.getUniqueId(), logicalName, createRelation);

		return createRelation;
	}

	private TitanOperationStatus associateComponentInstanceToGroupInstance(TitanVertex componentInstVertex, TitanVertex groupInstanceVertex, String logicalName) {
		Map<String, Object> properties = new HashMap<String, Object>();

		properties.put(GraphPropertiesDictionary.NAME.getProperty(), logicalName);
		TitanOperationStatus createRelation = titanGenericDao.createEdge(componentInstVertex, groupInstanceVertex, GraphEdgeLabels.GROUP_INST, properties);

		return createRelation;
	}

	private Either<GraphRelation, TitanOperationStatus> associateToInstOriginGroup(GroupInstanceData groupInstanceData, NodeTypeEnum compInstNodeType) {

		UniqueIdData groupIdData = new UniqueIdData(compInstNodeType, groupInstanceData.getGroupDataDefinition().getGroupUid());

		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(groupInstanceData, groupIdData, GraphEdgeLabels.INSTANCE_OF, null);

		log.debug("After associating group instance {} to group {}. status is {}", groupInstanceData.getUniqueId(), groupInstanceData.getGroupDataDefinition().getGroupUid(), createRelation);

		return createRelation;
	}

	private TitanOperationStatus associateToInstOriginGroup(TitanVertex groupInstanceVertex, TitanVertex originVertex, String originId) {

		TitanOperationStatus createRelation = titanGenericDao.createEdge(groupInstanceVertex, originVertex, GraphEdgeLabels.INSTANCE_OF, null);

		log.debug("After associating group instance {} to group {}. status is {}", groupInstanceVertex, originId, createRelation);

		return createRelation;
	}

	public Either<List<GroupProperty>, TitanOperationStatus> getGroupInstanceProperties(GroupInstance groupInstance, GroupDefinition groupDefinition) {

		// 1. Go over each instance
		// 1.1 get all properties of from the parents of the instance
		// 1.2 get all updated properties
		// 1.3 find all instances included in the parent of this instance and
		// run this method on them.
		String groupInstanceId = groupInstance.getUniqueId();
		if (log.isDebugEnabled())
			log.debug("Going to update properties of group instance {}", groupInstanceId);
		String groupUid = groupInstance.getGroupUid();
		List<GroupProperty> properties = groupDefinition.convertToGroupProperties();

		if (log.isDebugEnabled())
			log.debug("After getting properties of group {} . Number of properties is {}", groupUid, (properties == null ? 0 : properties.size()));
		List<GroupProperty> resourceInstancePropertyList = new ArrayList<>();
		if (properties != null && false == properties.isEmpty()) {

			// TODO: WE MAY HAVE INDIRECT PROPERTY VALUE ALSO IN CASE NO
			// PROPERTY ON THIS COMPONENT

			// String resourceInstanceUid = resourceInstance.getUniqueId();

			for (GroupProperty propertyDefinition : properties) {

				String defaultValue = propertyDefinition.getDefaultValue();
				String value = defaultValue;
				String valueUid = null;

				// String propertyId = propertyDefinition.getUniqueId();

				GroupProperty resourceInstanceProperty = new GroupProperty(propertyDefinition, value, valueUid);

				// resourceInstanceProperty.setPath(cloneList(path));

				// TODO: currently ignore constraints since they are not inuse
				// and cause to error in convertion to object.
				resourceInstanceProperty.setConstraints(null);

				resourceInstancePropertyList.add(resourceInstanceProperty);

			}

		}

		return Either.left(resourceInstancePropertyList);
	}

	/**
	 * update value of attribute on resource instance
	 * 
	 * @param resourceInstanceProerty
	 * @param resourceInstanceId
	 * @return
	 */
	public Either<PropertyValueData, TitanOperationStatus> updatePropertyOfGroupInstance(ComponentInstanceProperty groupInstanceProerty, String groupInstanceId) {

		Wrapper<TitanOperationStatus> errorWrapper = new Wrapper<>();
		UpdateDataContainer<PropertyData, PropertyValueData> updateDataContainer = new UpdateDataContainer<>(GraphEdgeLabels.PROPERTY_IMPL, (() -> PropertyData.class), (() -> PropertyValueData.class), NodeTypeEnum.Property,
				NodeTypeEnum.PropertyValue);

		preUpdateElementOfResourceInstanceValidations(updateDataContainer, groupInstanceProerty, groupInstanceId, errorWrapper);
		if (!errorWrapper.isEmpty()) {
			return Either.right(errorWrapper.getInnerElement());
		}

		else {
			String value = groupInstanceProerty.getValue();
			// Specific Validation Logic
			PropertyData propertyData = updateDataContainer.getDataWrapper().getInnerElement();

			String innerType = null;

			PropertyDataDefinition propDataDef = propertyData.getPropertyDataDefinition();
			String propertyType = propDataDef.getType();
			ToscaPropertyType type = ToscaPropertyType.isValidType(propertyType);
			log.debug("The type of the property {} is {}", propertyData.getUniqueId(), propertyType);

			if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {
				SchemaDefinition def = propDataDef.getSchema();
				if (def == null) {
					log.debug("Schema doesn't exists for property of type {}", type);
					return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
				}
				PropertyDataDefinition propDef = def.getProperty();
				if (propDef == null) {
					log.debug("Property in Schema Definition inside property of type {} doesn't exist", type);
					return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
				}
				innerType = propDef.getType();
			}
			// Specific Update Logic
			Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = dataTypeCache.getAll();
			if (allDataTypes.isRight()) {
				TitanOperationStatus status = allDataTypes.right().value();
				BeEcompErrorManager.getInstance().logInternalFlowError("UpdatePropertyValueOnComponentInstance", "Failed to update property value on instance. Status is " + status, ErrorSeverity.ERROR);
				return Either.right(status);
			}
			Either<Object, Boolean> isValid = propertyOperation.validateAndUpdatePropertyValue(propertyType, value, innerType, allDataTypes.left().value());

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
			PropertyValueData propertyValueData = updateDataContainer.getValueDataWrapper().getInnerElement();
			log.debug("Going to update property value from {} to {}", propertyValueData.getValue(), newValue);
			propertyValueData.setValue(newValue);

			ImmutablePair<String, Boolean> pair = propertyOperation.validateAndUpdateRules(propertyType, groupInstanceProerty.getRules(), innerType, allDataTypes.left().value(), true);
			if (pair.getRight() != null && pair.getRight() == false) {
				BeEcompErrorManager.getInstance().logBeInvalidValueError("Add property value", pair.getLeft(), groupInstanceProerty.getName(), propertyType);
				return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
			}
			propertyOperation.updateRulesInPropertyValue(propertyValueData, groupInstanceProerty, groupInstanceId);

			Either<PropertyValueData, TitanOperationStatus> updateRes = titanGenericDao.updateNode(propertyValueData, PropertyValueData.class);
			if (updateRes.isRight()) {
				TitanOperationStatus status = updateRes.right().value();
				return Either.right(status);
			} else {
				return Either.left(updateRes.left().value());
			}
		}

	}

	private static final class UpdateDataContainer<SomeData, SomeValueData> {
		final Wrapper<SomeValueData> valueDataWrapper;
		final Wrapper<SomeData> dataWrapper;
		final GraphEdgeLabels graphEdge;
		final Supplier<Class<SomeData>> someDataClassGen;
		final Supplier<Class<SomeValueData>> someValueDataClassGen;
		final NodeTypeEnum nodeType;
		final NodeTypeEnum nodeTypeValue;

		private UpdateDataContainer(GraphEdgeLabels graphEdge, Supplier<Class<SomeData>> someDataClassGen, Supplier<Class<SomeValueData>> someValueDataClassGen, NodeTypeEnum nodeType, NodeTypeEnum nodeTypeValue) {
			super();
			this.valueDataWrapper = new Wrapper<>();
			this.dataWrapper = new Wrapper<>();
			this.graphEdge = graphEdge;
			this.someDataClassGen = someDataClassGen;
			this.someValueDataClassGen = someValueDataClassGen;
			this.nodeType = nodeType;
			this.nodeTypeValue = nodeTypeValue;
		}

		public Wrapper<SomeValueData> getValueDataWrapper() {
			return valueDataWrapper;
		}

		public Wrapper<SomeData> getDataWrapper() {
			return dataWrapper;
		}

		public GraphEdgeLabels getGraphEdge() {
			return graphEdge;
		}

		public Supplier<Class<SomeData>> getSomeDataClassGen() {
			return someDataClassGen;
		}

		public Supplier<Class<SomeValueData>> getSomeValueDataClassGen() {
			return someValueDataClassGen;
		}

		public NodeTypeEnum getNodeType() {
			return nodeType;
		}

		public NodeTypeEnum getNodeTypeValue() {
			return nodeTypeValue;
		}
	}

	private <SomeData extends GraphNode, SomeValueData extends GraphNode> void preUpdateElementOfResourceInstanceValidations(UpdateDataContainer<SomeData, SomeValueData> updateDataContainer, IComponentInstanceConnectedElement resourceInstanceProerty,
			String resourceInstanceId, Wrapper<TitanOperationStatus> errorWrapper) {

		if (errorWrapper.isEmpty()) {
			// Verify VFC instance Exist
			validateGIExist(resourceInstanceId, errorWrapper);
		}

		if (errorWrapper.isEmpty()) {
			// Example: Verify Property connected to VFC exist
			validateElementConnectedToComponentExist(updateDataContainer, resourceInstanceProerty, errorWrapper);
		}

		if (errorWrapper.isEmpty()) {
			// Example: Verify PropertyValue connected to VFC Instance exist
			validateElementConnectedToComponentInstanceExist(updateDataContainer, resourceInstanceProerty, errorWrapper);
		}

		if (errorWrapper.isEmpty()) {
			// Example: Verify PropertyValue connected Property
			validateElementConnectedToInstance(updateDataContainer, resourceInstanceProerty, errorWrapper);
		}
	}

	private <SomeData extends GraphNode, SomeValueData extends GraphNode> void validateElementConnectedToInstance(UpdateDataContainer<SomeData, SomeValueData> updateDataContainer, IComponentInstanceConnectedElement resourceInstanceProerty,
			Wrapper<TitanOperationStatus> errorWrapper) {
		Either<ImmutablePair<SomeData, GraphEdge>, TitanOperationStatus> child = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(updateDataContainer.getNodeTypeValue()), resourceInstanceProerty.getValueUniqueUid(),
				updateDataContainer.getGraphEdge(), updateDataContainer.getNodeType(), updateDataContainer.getSomeDataClassGen().get());

		if (child.isRight()) {
			TitanOperationStatus status = child.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			errorWrapper.setInnerElement(status);

		} else {
			updateDataContainer.getDataWrapper().setInnerElement(child.left().value().left);
		}
	}

	private <SomeValueData extends GraphNode, SomeData extends GraphNode> void validateElementConnectedToComponentInstanceExist(UpdateDataContainer<SomeData, SomeValueData> updateDataContainer,
			IComponentInstanceConnectedElement resourceInstanceProerty, Wrapper<TitanOperationStatus> errorWrapper) {
		String valueUniqueUid = resourceInstanceProerty.getValueUniqueUid();
		if (valueUniqueUid == null) {
			errorWrapper.setInnerElement(TitanOperationStatus.INVALID_ID);
		} else {
			Either<SomeValueData, TitanOperationStatus> findPropertyValueRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(updateDataContainer.getNodeTypeValue()), valueUniqueUid, updateDataContainer.getSomeValueDataClassGen().get());
			if (findPropertyValueRes.isRight()) {
				TitanOperationStatus status = findPropertyValueRes.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
				}
				errorWrapper.setInnerElement(status);
			} else {
				updateDataContainer.getValueDataWrapper().setInnerElement(findPropertyValueRes.left().value());
			}
		}
	}

	private <SomeData extends GraphNode, SomeValueData extends GraphNode> void validateElementConnectedToComponentExist(UpdateDataContainer<SomeData, SomeValueData> updateDataContainer,
			IComponentInstanceConnectedElement resourceInstanceElementConnected, Wrapper<TitanOperationStatus> errorWrapper) {
		String uniqueId = resourceInstanceElementConnected.getUniqueId();
		Either<SomeData, TitanOperationStatus> findPropertyDefRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(updateDataContainer.getNodeType()), uniqueId, updateDataContainer.getSomeDataClassGen().get());

		if (findPropertyDefRes.isRight()) {
			TitanOperationStatus status = findPropertyDefRes.right().value();
			errorWrapper.setInnerElement(status);
		}
	}

	private void validateGIExist(String resourceInstanceId, Wrapper<TitanOperationStatus> errorWrapper) {
		validateGIExist(resourceInstanceId, null, errorWrapper);
	}

	private void validateGIExist(String resourceInstanceId, Wrapper<GroupInstanceData> compInsDataWrapper, Wrapper<TitanOperationStatus> errorWrapper) {
		validateElementExistInGraph(resourceInstanceId, NodeTypeEnum.GroupInstance, () -> GroupInstanceData.class, compInsDataWrapper, errorWrapper);
	}

	public <ElementData extends GraphNode> void validateElementExistInGraph(String elementUniqueId, NodeTypeEnum elementNodeType, Supplier<Class<ElementData>> elementClassGen, Wrapper<ElementData> elementDataWrapper,
			Wrapper<TitanOperationStatus> errorWrapper) {
		Either<ElementData, TitanOperationStatus> findResInstanceRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(elementNodeType), elementUniqueId, elementClassGen.get());
		if (findResInstanceRes.isRight()) {
			TitanOperationStatus status = findResInstanceRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			errorWrapper.setInnerElement(status);
		} else {
			if (elementDataWrapper != null) {
				elementDataWrapper.setInnerElement(findResInstanceRes.left().value());
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
	public Either<GroupInstance, StorageOperationStatus> associateArtifactsToGroupInstance(String groupId, List<String> artifactsId) {

		Either<GroupInstance, StorageOperationStatus> result = null;

		Either<GroupInstance, TitanOperationStatus> titanRes = this.associateArtifactsToGroupInstanceOnGraph(groupId, artifactsId);

		if (titanRes.isRight()) {
			StorageOperationStatus status = DaoStatusConverter.convertTitanStatusToStorageStatus(titanRes.right().value());
			result = Either.right(status);
		}

		result = Either.left(titanRes.left().value());
		return result;

	}

	public Either<GroupInstance, TitanOperationStatus> associateArtifactsToGroupInstanceOnGraph(String groupInstanceId, List<String> artifactsId) {

		if (artifactsId == null || artifactsId.isEmpty()) {
			return Either.right(TitanOperationStatus.OK);
		}

		for (String artifactId : artifactsId) {
			Either<ArtifactData, TitanOperationStatus> findArtifactRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ArtifactRef), artifactId, ArtifactData.class);
			if (findArtifactRes.isRight()) {
				TitanOperationStatus status = findArtifactRes.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
				}
				String description = "Failed to associate group " + groupInstanceId + " to artifact " + artifactId + " in graph. Status is " + status;
				BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description, ErrorSeverity.ERROR);
				return Either.right(status);
			}

			Map<String, Object> props = new HashMap<String, Object>();
			props.put(GraphPropertiesDictionary.NAME.getProperty(), findArtifactRes.left().value().getLabel());

			GraphNode groupData = new UniqueIdData(NodeTypeEnum.GroupInstance, groupInstanceId);
			Either<GraphRelation, TitanOperationStatus> addArtifactsRefResult = titanGenericDao.createRelation(groupData, findArtifactRes.left().value(), GraphEdgeLabels.GROUP_ARTIFACT_REF, props);

			if (addArtifactsRefResult.isRight()) {
				TitanOperationStatus status = addArtifactsRefResult.right().value();
				String description = "Failed to associate group " + groupData.getUniqueId() + " to artifact " + artifactId + " in graph. Status is " + status;
				BeEcompErrorManager.getInstance().logInternalFlowError(ADDING_GROUP, description, ErrorSeverity.ERROR);
				return Either.right(status);
			}
		}

		Either<GroupInstance, TitanOperationStatus> groupFromGraph = this.getGroupInstanceFromGraph(groupInstanceId, true, false);

		return groupFromGraph;
	}

	public Either<GroupInstance, TitanOperationStatus> getGroupInstanceFromGraph(String uniqueId, boolean skipProperties, boolean skipArtifacts) {

		Either<GroupInstance, TitanOperationStatus> result = null;

		Either<GroupInstanceData, TitanOperationStatus> groupInstRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.GroupInstance), uniqueId, GroupInstanceData.class);
		if (groupInstRes.isRight()) {
			TitanOperationStatus status = groupInstRes.right().value();
			log.debug("Failed to retrieve group {}  from graph. Status is {}", uniqueId, status);
			BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("Fetch Group", uniqueId, String.valueOf(status));
			result = Either.right(status);
			return result;
		}

		GroupInstanceData groupInstData = groupInstRes.left().value();

		GroupInstance groupInstance = new GroupInstance(groupInstData.getGroupDataDefinition());
		String instOriginGroupId = groupInstance.getGroupUid();
		Either<GroupDefinition, TitanOperationStatus> groupRes = groupOperation.getGroupFromGraph(instOriginGroupId, false, true, false);

		if (groupRes.isRight()) {
			TitanOperationStatus status = groupRes.right().value();
			result = Either.right(status);

		}
		GroupDefinition groupDefinition = groupRes.left().value();
		Either<Map<String, PropertyValueData>, TitanOperationStatus> groupInstancePropertyValuesRes = getAllGroupInstancePropertyValuesData(groupInstData);
		if(groupInstancePropertyValuesRes.isRight()){
			result = Either.right(groupInstancePropertyValuesRes.right().value());
		}
		buildGroupInstanceFromGroup(groupInstance, groupDefinition, groupInstancePropertyValuesRes.left().value());

		/*
		 * if (false == skipProperties) { Either<List<GroupProperty>, TitanOperationStatus> propertiesRes = getGroupProperties(uniqueId); if (propertiesRes.isRight()) { TitanOperationStatus status = propertiesRes.right().value(); if (status !=
		 * TitanOperationStatus.OK) { result = Either.right(status); return result; } } else { List<GroupProperty> properties = propertiesRes.left().value(); groupDefinition.setProperties(properties); } }
		 */

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
					groupInstance.setGroupInstanceArtifacts(artifactsUid);
					groupInstance.setGroupInstanceArtifactsUuid(artifactsUUID);
				}
			}
		}
		result = Either.left(groupInstance);

		return result;

	}

	private void buildGroupInstanceFromGroup(GroupInstance groupInstance, GroupDefinition groupDefinition, Map<String, PropertyValueData> groupInstancePropertyValues) {

	groupInstance.setGroupName(groupDefinition.getName());
	groupInstance.setInvariantUUID(groupDefinition.getInvariantUUID());
	groupInstance.setDescription(groupDefinition.getDescription());
	groupInstance.setVersion(groupDefinition.getVersion());
	groupInstance.setArtifacts(groupDefinition.getArtifacts());
	groupInstance.setArtifactsUuid(groupDefinition.getArtifactsUuid());
	groupInstance.setType(groupDefinition.getType());
	groupInstance.setGroupUUID(groupDefinition.getGroupUUID());
	
	List<GroupInstanceProperty> groupInstanceProperties = groupDefinition.convertToGroupProperties()
			//converts List of GroupProperties to List of GroupInstanceProperties and updates it with group instance property data
			.stream().map(p->getUpdatedConvertedProperty(p, groupInstancePropertyValues)).collect(Collectors.toList());
	groupInstance.convertFromGroupInstancesProperties(groupInstanceProperties);
}
	
	private GroupInstanceProperty getUpdatedConvertedProperty(GroupProperty groupProperty,  Map<String, PropertyValueData> groupInstancePropertyValues){

		GroupInstanceProperty updatedProperty = new GroupInstanceProperty(groupProperty, groupProperty.getValue());
		if(!MapUtils.isEmpty(groupInstancePropertyValues) && groupInstancePropertyValues.containsKey(groupProperty.getName())){
			PropertyValueData groupInstancePropertyValue = groupInstancePropertyValues.get(groupProperty.getName());
			updatedProperty.setValue(groupInstancePropertyValue.getValue());
			updatedProperty.setValueUniqueUid(groupInstancePropertyValue.getUniqueId());
		}
		return updatedProperty;
	}

	private Either<List<ImmutablePair<String, String>>, TitanOperationStatus> getGroupArtifactsPairs(String groupUniqueId) {

		Either<List<ImmutablePair<String, String>>, TitanOperationStatus> result = null;

		Either<List<ImmutablePair<ArtifactData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.GroupInstance), groupUniqueId, GraphEdgeLabels.GROUP_ARTIFACT_REF,
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

			log.debug("The artifacts list related to group {} is {}",groupUniqueId,artifactsList);
			result = Either.left(artifactsList);
		}

		return result;

	}

	@Override
	public StorageOperationStatus dissociateAndAssociateGroupsInstanceFromArtifact(String componentId, NodeTypeEnum componentTypeEnum, String oldArtifactId, ArtifactData newArtifact) {

		StorageOperationStatus result = null;

		return this.dissociateAndAssociateGroupsInstanceFromArtifactOnGraph(componentId, componentTypeEnum, oldArtifactId, newArtifact);

	}

	@Override
	public StorageOperationStatus dissociateAndAssociateGroupsInstanceFromArtifactOnGraph(String componentId, NodeTypeEnum componentTypeEnum, String oldArtifactId, ArtifactData newArtifact) {

		Either<List<GroupInstance>, StorageOperationStatus> allGroupsFromGraph = getAllGroupInstances(componentId, componentTypeEnum);
		if (allGroupsFromGraph.isRight()) {
			StorageOperationStatus status = allGroupsFromGraph.right().value();
			return status;
		}

		List<GroupInstance> allGroups = allGroupsFromGraph.left().value();
		if (allGroups == null || allGroups.isEmpty()) {
			return StorageOperationStatus.OK;
		}

		// Find all groups which contains this artifact id
		List<GroupInstance> associatedGroups = allGroups.stream().filter(p -> p.getGroupInstanceArtifacts() != null && p.getGroupInstanceArtifacts().contains(oldArtifactId)).collect(Collectors.toList());

		if (associatedGroups != null && false == associatedGroups.isEmpty()) {

			log.debug("The groups {} contains the artifact {}",associatedGroups.stream().map(p -> p.getName()).collect(Collectors.toList()),oldArtifactId);

			UniqueIdData oldArtifactData = new UniqueIdData(NodeTypeEnum.ArtifactRef, oldArtifactId);
			UniqueIdData newArtifactData = new UniqueIdData(NodeTypeEnum.ArtifactRef, newArtifact.getArtifactDataDefinition().getUniqueId());
			Map<String, Object> props = new HashMap<String, Object>();
			props.put(GraphPropertiesDictionary.NAME.getProperty(), newArtifactData.getLabel());

			for (GroupInstance groupDefinition : associatedGroups) {
				UniqueIdData groupData = new UniqueIdData(NodeTypeEnum.GroupInstance, groupDefinition.getUniqueId());

				Either<GraphRelation, TitanOperationStatus> deleteRelation = titanGenericDao.deleteRelation(groupData, oldArtifactData, GraphEdgeLabels.GROUP_ARTIFACT_REF);
				log.trace("After dissociate group {} from artifact {}", groupDefinition.getName(), oldArtifactId);
				if (deleteRelation.isRight()) {
					TitanOperationStatus status = deleteRelation.right().value();
					if (status == TitanOperationStatus.NOT_FOUND) {
						status = TitanOperationStatus.INVALID_ID;
					}
					return DaoStatusConverter.convertTitanStatusToStorageStatus(status);
				}

				Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(groupData, newArtifactData, GraphEdgeLabels.GROUP_ARTIFACT_REF, props);
				log.trace("After associate group {} to artifact {}", groupDefinition.getName(), newArtifact.getUniqueIdKey());
				if (createRelation.isRight()) {
					TitanOperationStatus status = createRelation.right().value();
					if (status == TitanOperationStatus.NOT_FOUND) {
						status = TitanOperationStatus.INVALID_ID;
					}
					return DaoStatusConverter.convertTitanStatusToStorageStatus(status);
				}
			}

		}
		return StorageOperationStatus.OK;
	}

	@Override
	public Either<GroupInstance, StorageOperationStatus> updateGroupInstancePropertyValues(GroupInstance oldGroupInstance, List<GroupInstanceProperty> newProperties, Boolean inTransaction) {
		
		Either<GroupInstance, StorageOperationStatus> updateRes = Either.left(oldGroupInstance);
		try{
			if(!CollectionUtils.isEmpty(newProperties)){
				updateRes = updateGroupInstancePropertyValuesOnGraph(oldGroupInstance, newProperties);
			}
		}catch(Exception e){ 
			log.debug("The Exception occured during update of group instance {} property values. The message is {}. ", oldGroupInstance.getName(), e.getMessage(), e);
			updateRes = Either.right(StorageOperationStatus.GENERAL_ERROR);
		}finally {
			handleTransactionCommitRollback(inTransaction, updateRes);
		}
		return updateRes;
	}

	private Either<GroupInstance, StorageOperationStatus> updateGroupInstancePropertyValuesOnGraph( GroupInstance oldGroupInstance, List<GroupInstanceProperty> newProperties ) {
		Either<GroupInstance, StorageOperationStatus> updateRes = null;
		Either<Integer, StorageOperationStatus> nodeUpdateRes = null;
		Vertex groupInstanceVertex = null;
		Either<Vertex, StorageOperationStatus> groupInstanceVertexRes;
		Map<String, Vertex> existingPropertyValueVertices = new HashMap<>();
		Map<String, Vertex> existingPropertyVertices = new HashMap<>();
		groupInstanceVertexRes = getVertexFromGraph(GraphPropertiesDictionary.UNIQUE_ID.getProperty(),oldGroupInstance.getUniqueId());
		try{
			if (groupInstanceVertexRes.isRight()) {
				log.debug("Failed to fetch group instance vertex {} from graph. ", oldGroupInstance.getName());
				updateRes = Either.right(groupInstanceVertexRes.right().value());
			} else {
				groupInstanceVertex = groupInstanceVertexRes.left().value();
				findExistingPropertyValueVertices(groupInstanceVertex, existingPropertyValueVertices);
				nodeUpdateRes = handlePropertyValues(oldGroupInstance, oldGroupInstance.getPropertyValueCounter(),  newProperties, groupInstanceVertex, existingPropertyValueVertices, existingPropertyVertices);
				if(nodeUpdateRes.isRight()){
					log.debug("Failed to handle property values of group instance {}. ", oldGroupInstance.getName());
					updateRes = Either.right(nodeUpdateRes.right().value());
				} else {
					updateRes = updateGroupInstanceVertexAndGetUpdatedGroupInstance(groupInstanceVertex, nodeUpdateRes.left().value(), oldGroupInstance);
				}
			}
		} catch(Exception e){
			log.debug("The Exception occured during update group instance {} property values on graph. The message is {}. ", oldGroupInstance.getName(), e.getMessage(), e);
			updateRes = Either.right(StorageOperationStatus.GENERAL_ERROR);
		}
		return updateRes;
	}

	private Either<Integer, StorageOperationStatus> handlePropertyValues(GroupInstance oldGroupInstance, Integer propertyValueCounter, List<GroupInstanceProperty> newProperties, Vertex groupInstanceVertex,
			Map<String, Vertex> existingPropertyValueVertices, Map<String, Vertex> existingPropertyVertices) {

		Either<Integer, StorageOperationStatus> nodeHandleRes = null;
		int currCounter = propertyValueCounter;
		for(GroupInstanceProperty currProperty : newProperties){
			nodeHandleRes = handlePropertyValueNode(oldGroupInstance, currCounter,  currProperty, groupInstanceVertex, existingPropertyValueVertices, existingPropertyVertices);
			if(nodeHandleRes.isRight()){
				break;
			}
			currCounter = nodeHandleRes.left().value();
		}
		return nodeHandleRes;
	}

	private Either<GroupInstance, StorageOperationStatus> updateGroupInstanceVertexAndGetUpdatedGroupInstance( Vertex groupInstanceVertex, Integer propertyValueCounter, GroupInstance oldGroupInstance) {
		
		TitanOperationStatus status;
		Either<GroupInstance, StorageOperationStatus> actionResult;
		status = updateGroupInstanceVertex(groupInstanceVertex, propertyValueCounter);
		if(status != TitanOperationStatus.OK){
			log.debug("Failed to update group instance {}. ", oldGroupInstance.getName());
			actionResult = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}else{
			Either<GroupInstance, TitanOperationStatus> updatedGroupInstanceRes = getGroupInstanceFromGraph(oldGroupInstance.getUniqueId(), false, false);
			if(updatedGroupInstanceRes.isRight()){
				status = updatedGroupInstanceRes.right().value();
				log.debug("Failed to get updated group instance {}. Status is {}. ", oldGroupInstance.getName(), status);
				actionResult = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}else{
				actionResult = Either.left(updatedGroupInstanceRes.left().value());
			}
		}
		return actionResult;
	}

	private Either<Integer, StorageOperationStatus> handlePropertyValueNode(GroupInstance oldGroupInstance, Integer propertyValueCounter, GroupInstanceProperty currProperty, Vertex groupInstanceVertex, Map<String, Vertex> existingPropertyValueVertices, Map<String, Vertex> existingPropertyVertices) {
		
		String groupInstanceName = oldGroupInstance.getName();
		TitanOperationStatus updateStatus;
		TitanOperationStatus addStatus;
		Vertex propertyValueVertex;
		String propertyValueId;
		propertyValueId = currProperty.getValueUniqueUid();
		Either<Integer, StorageOperationStatus> actionResult = null;
		if(existingPropertyValueVertices.containsKey(propertyValueId)){
			updateStatus = updatePropertyValueVertex(existingPropertyValueVertices.get(propertyValueId), currProperty);
			if(updateStatus != TitanOperationStatus.OK){
				log.debug("Failed to update property value {} of group instance {}. ", currProperty.getName(), groupInstanceName);
				actionResult = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateStatus));
			}
		}
		else{
			if(MapUtils.isEmpty(existingPropertyVertices)){
				findExistingPropertyVertices(existingPropertyVertices, groupInstanceVertex);
			}
			propertyValueVertex = existingPropertyVertices.get(currProperty.getUniqueId());
			addStatus = addPropertyValueNodeToGroupInstance(currProperty, groupInstanceVertex, propertyValueVertex, oldGroupInstance.getUniqueId(), ++propertyValueCounter);
			if(addStatus != TitanOperationStatus.OK){
				log.debug("Failed to add property value {} to group instance {}. ", currProperty.getName(), groupInstanceName);
				actionResult = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(addStatus));
			}
		}
		if(actionResult == null){
			actionResult = Either.left(propertyValueCounter);
		}
		return actionResult;
	}

	@SuppressWarnings("unchecked")
	private Either<Vertex, StorageOperationStatus> getVertexFromGraph(String uniqueKeyName, String uniqueId) {
	
		Either<Vertex, StorageOperationStatus> actionResult = null;
		try{
			Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
			Iterable<TitanVertex> vertices = null;
			if (graph.isRight()) {
				log.debug("Failed to get graph. Status is {}", graph.right().value());
				actionResult = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graph.right().value()));
			}
			if(actionResult == null){
				TitanGraph tGraph = graph.left().value();
				vertices = tGraph.query().has(uniqueKeyName, uniqueId).vertices();
				if (vertices == null || vertices.iterator() == null || !vertices.iterator().hasNext()) {
					log.debug("Failed to get nodes from graph for type {}  for id = {}", NodeTypeEnum.GroupInstance, uniqueId);
					actionResult = Either.right(StorageOperationStatus.NOT_FOUND);
				}
			}
			if(actionResult == null && vertices != null){
				actionResult = Either.left(vertices.iterator().next());
			}
		} catch(Exception e){
			log.debug("The Exception occured during get vertex {} from graph. The message is {}. ", uniqueId, e.getMessage(), e);
		}
		return actionResult;
	}

	private void findExistingPropertyValueVertices(Vertex groupInstanceVertex,	Map<String, Vertex> existingPropertyValueVertices) {
		Iterator<Edge> propertyValueEdges = groupInstanceVertex.edges(Direction.OUT, GraphEdgeLabels.PROPERTY_VALUE.getProperty());
		Vertex propertyValueVertex;
		while(propertyValueEdges.hasNext()){
			propertyValueVertex = propertyValueEdges.next().inVertex();
			existingPropertyValueVertices.put((String) propertyValueVertex.property(GraphPropertiesDictionary.UNIQUE_ID.getProperty()).value(), propertyValueVertex);
		}
	}

	private void findExistingPropertyVertices(Map<String, Vertex> existingPropertyVertices, Vertex groupInstanceVertex) {
		Vertex groupVertex = groupInstanceVertex.edges(Direction.OUT, GraphEdgeLabels.INSTANCE_OF.getProperty()).next().inVertex();
		Vertex groupTypeVertex = groupVertex.edges(Direction.OUT, GraphEdgeLabels.TYPE_OF.getProperty()).next().inVertex();
		Iterator<Edge> groupTypePropertiesIterator = groupTypeVertex.edges(Direction.OUT, GraphEdgeLabels.PROPERTY.getProperty());
		while(groupTypePropertiesIterator.hasNext()){
			Vertex propertyValueVertex = groupTypePropertiesIterator.next().inVertex();
			existingPropertyVertices.put((String) propertyValueVertex.property(GraphPropertiesDictionary.UNIQUE_ID.getProperty()).value(), propertyValueVertex);
		}
	}

	private TitanOperationStatus addPropertyValueNodeToGroupInstance(GroupInstanceProperty currProperty, Vertex groupInstanceVertex, Vertex propertyVertex, String groupInstanceId, int index) {
		TitanOperationStatus status = null;
		TitanVertex propertyValueVertex = null;
		PropertyValueData newPropertyValue = new PropertyValueData();
		Long creationTime = System.currentTimeMillis();
		newPropertyValue.setModificationTime(creationTime);
		newPropertyValue.setCreationTime(creationTime);
		newPropertyValue.setUniqueId(UniqueIdBuilder.buildGroupPropertyValueUid(groupInstanceId, index));
		newPropertyValue.setValue(currProperty.getValue());
		newPropertyValue.setType(currProperty.getType());
		Either<TitanVertex, TitanOperationStatus> propertyValueNodeRes = titanGenericDao.createNode(newPropertyValue);
		if(propertyValueNodeRes.isRight()){
			status = propertyValueNodeRes.right().value();
		}
		if(status == null){
			propertyValueVertex = propertyValueNodeRes.left().value();
			Map<String, Object> props = new HashMap<>();
			props.put(GraphPropertiesDictionary.PROPERTY_NAME.getProperty(), currProperty.getName());
			status = titanGenericDao.createEdge(groupInstanceVertex, propertyValueVertex, GraphEdgeLabels.PROPERTY_VALUE, props);
		}
		if(status == TitanOperationStatus.OK){
			status = titanGenericDao.createEdge(propertyValueVertex, propertyVertex, GraphEdgeLabels.PROPERTY_IMPL, null);
		}
		return status;
	}

	private TitanOperationStatus updatePropertyValueVertex(Vertex propertyValueVertex, GroupInstanceProperty property) {
		PropertyValueData propertyValue = new PropertyValueData();
		propertyValue.setUniqueId(property.getValue());
		propertyValue.setModificationTime(System.currentTimeMillis());
		propertyValue.setType(property.getType());
		propertyValue.setValue(property.getValue());
		return titanGenericDao.updateVertex(propertyValue, propertyValueVertex);
	}
	
	private TitanOperationStatus updateGroupInstanceVertex(Vertex groupInstanceVertex, int propertyValueCounter) {
		GroupInstanceData groupInstanceData = new GroupInstanceData();
		groupInstanceData.getGroupDataDefinition().setModificationTime(System.currentTimeMillis());
		groupInstanceData.getGroupDataDefinition().setCustomizationUUID(UUID.randomUUID().toString());
		groupInstanceData.getGroupDataDefinition().setPropertyValueCounter(propertyValueCounter);
		return  titanGenericDao.updateVertex(groupInstanceData, groupInstanceVertex);
	}
	
	private Either<Map<String, PropertyValueData>, TitanOperationStatus> getAllGroupInstancePropertyValuesData(GroupInstanceData groupInstData) {
		
		Either<Map<String, PropertyValueData>, TitanOperationStatus> result = null;
		try{
			Either<List<ImmutablePair<PropertyValueData, GraphEdge>>, TitanOperationStatus> getPropertyValueChildrenRes = 
					titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), groupInstData.getUniqueId(), GraphEdgeLabels.PROPERTY_VALUE,
							NodeTypeEnum.PropertyValue, PropertyValueData.class, true);
			if(getPropertyValueChildrenRes.isRight()){
				TitanOperationStatus status = getPropertyValueChildrenRes.right().value();
				log.debug("Failed to fetch property value nodes for group instance {}. Status is {}. ", groupInstData.getName(), status);
				if(status == TitanOperationStatus.NOT_FOUND){
					result = Either.left(null);
				}else{
					result = Either.right(status);
				}
			}else{
				result = Either.left(getPropertyValueChildrenRes.left().value().stream()
						.collect(Collectors.toMap(pair->(String)(pair.getRight().getProperties().get(GraphPropertiesDictionary.PROPERTY_NAME.getProperty())), pair->pair.getLeft())));
			}
		} catch(Exception e){
			log.debug("The Exception occured during fetch group instance () property values. The message is {}. ", groupInstData.getName(), e.getMessage(), e);
			if(result == null){
				result = Either.right(TitanOperationStatus.GENERAL_ERROR);
			}
		}
		return result;
	}
	@Override
	public Either<GroupInstance, StorageOperationStatus> updateGroupInstancePropertyValues(GroupInstance groupInstance, List<GroupInstanceProperty> newProperties) {
		return updateGroupInstancePropertyValues(groupInstance, newProperties, false);
	}
}
