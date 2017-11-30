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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.IComponentInstanceConnectedElement;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.GroupInstanceData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@org.springframework.stereotype.Component("group-instance-operation")
public class GroupInstanceOperation extends AbstractOperation implements IGroupInstanceOperation {

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
	public Either<ComponentInstanceProperty, StorageOperationStatus> updatePropertyValueInGroupInstance(ComponentInstanceProperty gropuInstanceProperty, String groupInstanceId, boolean inTransaction) {
		// TODO Auto-generated method stub
		// change Propety class
		return null;
	}

	public void generateCustomizationUUID(GroupInstance groupInstance) {
		UUID uuid = UUID.randomUUID();
		groupInstance.setCustomizationUUID(uuid.toString());
	}

	/**
	 * add property to resource instance
	 * 
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

	/**
	 * update value of attribute on resource instance
	 * 
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

	private Either<GroupInstance, TitanOperationStatus> getGroupInstanceFromGraph(String uniqueId, boolean skipProperties, boolean skipArtifacts) {

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
				.stream().map(p -> getUpdatedConvertedProperty(p, groupInstancePropertyValues)).collect(Collectors.toList());
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

		return this.dissociateAndAssociateGroupsInstanceFromArtifactOnGraph(componentId, componentTypeEnum, oldArtifactId, newArtifact);

	}

	private StorageOperationStatus dissociateAndAssociateGroupsInstanceFromArtifactOnGraph(String componentId, NodeTypeEnum componentTypeEnum, String oldArtifactId, ArtifactData newArtifact) {

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
}
