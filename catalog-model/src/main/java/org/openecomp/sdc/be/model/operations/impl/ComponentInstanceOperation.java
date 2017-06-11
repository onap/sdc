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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.IComponentInstanceConnectedElement;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RequirementAndRelationshipPair;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.operations.api.IAttributeOperation;
import org.openecomp.sdc.be.model.operations.api.IComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.AttributeData;
import org.openecomp.sdc.be.resources.data.AttributeValueData;
import org.openecomp.sdc.be.resources.data.CapabilityData;
import org.openecomp.sdc.be.resources.data.CapabilityInstData;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.InputValueData;
import org.openecomp.sdc.be.resources.data.InputsData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.RelationshipInstData;
import org.openecomp.sdc.be.resources.data.RelationshipTypeData;
import org.openecomp.sdc.be.resources.data.RequirementData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.TitanVertexQuery;

import fj.data.Either;

@org.springframework.stereotype.Component("component-instance-operation")
public class ComponentInstanceOperation extends AbstractOperation implements IComponentInstanceOperation {

	public ComponentInstanceOperation() {
		super();
	}

	private static Logger log = LoggerFactory.getLogger(ComponentInstanceOperation.class.getName());

	@Autowired
	private ResourceOperation resourceOperation;

	@Autowired
	private ServiceOperation serviceOperation;

	@Autowired
	CapabilityOperation capabilityOperation;

	@Autowired
	private CapabilityInstanceOperation capabilityInstanceOperation;

	@Autowired
	private RequirementOperation requirementOperation;

	@Autowired
	private ArtifactOperation artifactOperation;

	@Autowired
	TitanGenericDao titanGenericDao;

	@Autowired
	PropertyOperation propertyOperation;

	@Autowired
	InputsOperation inputOperation;

	@Autowired
	private IAttributeOperation attributeOperation;

	@Autowired
	private ApplicationDataTypeCache dataTypeCache;

	@Autowired
	protected GroupOperation groupOperation;

	@Autowired
	protected GroupInstanceOperation groupInstanceOperation;

	/**
	 * FOR TEST ONLY
	 * 
	 * @param titanGenericDao
	 */
	public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
	}

	@Override
	public Either<ComponentInstance, StorageOperationStatus> createComponentInstance(String parentComponentId, NodeTypeEnum nodeType, String instanceNumber, ComponentInstance componentInstance, NodeTypeEnum compInstNodeType, boolean inTransaction) {

		return createComponentInstance(parentComponentId, nodeType, instanceNumber, true, componentInstance, compInstNodeType, false, inTransaction);

	}

	private Either<ComponentInstance, StorageOperationStatus> createComponentInstance(String containerComponentId, NodeTypeEnum containerNodeType, String instanceNumber, boolean isCreateLocgicalName, ComponentInstance componentInstance,
			NodeTypeEnum compInstNodeType, boolean allowDeleted, boolean inTransaction) {
		Either<ComponentInstance, StorageOperationStatus> result = null;

		if (!ValidationUtils.validateStringNotEmpty(componentInstance.getCustomizationUUID())) {
			generateCustomizationUUID(componentInstance);
		}
		try {

			Either<ComponentInstance, TitanOperationStatus> addRes = addComponentInstanceToContainerComponent(containerComponentId, containerNodeType, instanceNumber, isCreateLocgicalName, componentInstance, compInstNodeType, allowDeleted);
			if (addRes.isRight()) {
				TitanOperationStatus status = addRes.right().value();
				log.error("Failed to add resource instance {} to service {}. status is {}", componentInstance, containerComponentId, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			ComponentInstance value = addRes.left().value();
			result = Either.left(value);

			return result;

		} finally {
			if (false == inTransaction) {
				commitOrRollback(result);
			}
		}
	}

	private Either<TitanVertex, StorageOperationStatus> createComponentInstance(String containerComponentId, NodeTypeEnum containerNodeType, String instanceNumber, boolean isCreateLocgicalName, ComponentInstance componentInstance,
			NodeTypeEnum compInstNodeType, boolean allowDeleted, boolean inTransaction, TitanVertex metadataVertex) {
		Either<TitanVertex, StorageOperationStatus> result = null;

		try {

			Either<TitanVertex, TitanOperationStatus> addRes = addComponentInstanceToContainerComponent(containerComponentId, containerNodeType, instanceNumber, isCreateLocgicalName, componentInstance, compInstNodeType, allowDeleted, metadataVertex);
			if (addRes.isRight()) {
				TitanOperationStatus status = addRes.right().value();
				log.error("Failed to add resource instance {} to service {}. status is {}", componentInstance, containerComponentId, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			TitanVertex value = addRes.left().value();
			result = Either.left(value);

			return result;

		} finally {
			if (false == inTransaction) {
				commitOrRollback(result);
			}
		}
	}

	@Override
	public Either<ComponentInstance, StorageOperationStatus> createComponentInstance(String containerComponentId, NodeTypeEnum containerNodeType, String instanceNumber, ComponentInstance componentInstance, NodeTypeEnum instNodeType) {

		return createComponentInstance(containerComponentId, containerNodeType, instanceNumber, componentInstance, instNodeType, false);

	}

	@Override
	public Either<ComponentInstance, StorageOperationStatus> deleteComponentInstance(NodeTypeEnum containerNodeType, String containerComponentId, String resourceInstUid, boolean inTransaction) {

		Either<ComponentInstance, StorageOperationStatus> result = null;

		try {

			Either<ComponentInstance, TitanOperationStatus> deleteRes = removeComponentInstanceFromComponent(containerNodeType, containerComponentId, resourceInstUid);

			if (deleteRes.isRight()) {
				TitanOperationStatus status = deleteRes.right().value();
				log.error("Failed to remove resource instance {} from component {}. status is {}", resourceInstUid, containerComponentId, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			ComponentInstance value = deleteRes.left().value();
			result = Either.left(value);

			return result;

		} finally {
			if (false == inTransaction) {
				commitOrRollback(result);
			}
		}

	}

	@Override
	public Either<ComponentInstance, StorageOperationStatus> deleteComponentInstance(NodeTypeEnum containerNodeType, String containerComponentId, String resourceInstUid) {

		return deleteComponentInstance(containerNodeType, containerComponentId, resourceInstUid, false);
	}

	private <T> void commitOrRollback(Either<T, StorageOperationStatus> result) {
		if (result == null || result.isRight()) {
			log.error("Going to execute rollback on graph.");
			titanGenericDao.rollback();
		} else {
			log.debug("Going to execute commit on graph.");
			titanGenericDao.commit();
		}
	}

	@Override
	public Either<Boolean, StorageOperationStatus> validateParent(String parentId, String uniqId, boolean inTransaction) {

		Either<Boolean, StorageOperationStatus> result = null;
		Either<Boolean, TitanOperationStatus> updateRes = validateParentonGraph(parentId, uniqId, inTransaction);

		if (updateRes.isRight()) {
			TitanOperationStatus status = updateRes.right().value();
			log.error("Failed to find resource instance name {}. status is {}", uniqId, status);
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			return result;
		}

		Boolean value = updateRes.left().value();

		result = Either.left(value);

		return result;

	}

	public Either<Boolean, TitanOperationStatus> validateParentonGraph(String parentId, String uniqId, boolean inTransaction) {

		Either<TitanGraph, TitanOperationStatus> graphRes = titanGenericDao.getGraph();
		if (graphRes.isRight()) {
			log.debug("Failed to retrieve graph. status is {}", graphRes);
			return Either.right(graphRes.right().value());
		}
		TitanGraph titanGraph = graphRes.left().value();
		try {
			Iterable<TitanVertex> vertices = titanGraph.query().has(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), uniqId).vertices();
			if (vertices == null || false == vertices.iterator().hasNext()) {
				return Either.right(TitanOperationStatus.INVALID_ID);
			}

			TitanVertex vertex = vertices.iterator().next();

			TitanVertexQuery query = vertex.query();
			query = query.labels(GraphEdgeLabels.RESOURCE_INST.getProperty()).direction(Direction.IN);
			Iterable<Vertex> verts = query.vertices();
			if (verts == null) {
				log.debug("No edges in graph for criteria");
				return Either.right(TitanOperationStatus.INVALID_ID);
			}
			Iterator<Vertex> vIter = verts.iterator();
			if (vIter.hasNext()) {
				Vertex vert = vIter.next();
				// vert.getProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
				String resInstName = vert.value(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
				if (resInstName.equals(parentId))
					return Either.left(Boolean.TRUE);
			}
			return Either.left(Boolean.FALSE);
		} finally {
			if (false == inTransaction) {
				titanGraph.tx().commit();
			}
		}
	}

	public Either<ComponentInstance, TitanOperationStatus> addComponentInstanceToContainerComponent(String containerComponentId, NodeTypeEnum containerNodeType, String instanceNumber, boolean isCreateLogicaName, ComponentInstance componentInstance,
			NodeTypeEnum compInstNodeType, boolean allowDeleted) {
		log.debug("Going to create component instance {} in component {}", componentInstance, containerComponentId);

		Either<TitanVertex, TitanOperationStatus> metadataVertex = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), containerComponentId);
		if (metadataVertex.isRight()) {
			TitanOperationStatus status = metadataVertex.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}
		Either<TitanVertex, TitanOperationStatus> addComponentInstanceToContainerComponent = addComponentInstanceToContainerComponent(containerComponentId, containerNodeType, instanceNumber, isCreateLogicaName, componentInstance, compInstNodeType,
				allowDeleted, metadataVertex.left().value());

		if (addComponentInstanceToContainerComponent.isRight()) {
			TitanOperationStatus status = addComponentInstanceToContainerComponent.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}
		TitanVertex ciVertex = addComponentInstanceToContainerComponent.left().value();
		Map<String, Object> properties = titanGenericDao.getProperties(ciVertex);
		ComponentInstanceData createdComponentInstance = GraphElementFactory.createElement(NodeTypeEnum.ResourceInstance.getName(), GraphElementTypeEnum.Node, properties, ComponentInstanceData.class);

		Either<ComponentInstance, TitanOperationStatus> createdResourceInstanceRes = createGroupInstancesOnComponentInstance(componentInstance, ciVertex, createdComponentInstance);
		return createdResourceInstanceRes;
	}


	public Either<ComponentInstance, TitanOperationStatus> createGroupInstancesOnComponentInstance(ComponentInstance componentInstance, TitanVertex ciVertex, ComponentInstanceData createdComponentInstance) {
		ComponentInstance createdResourceInstance = new ComponentInstance(createdComponentInstance.getComponentInstDataDefinition());
		createdResourceInstance.setGroupInstances(componentInstance.getGroupInstances());
		List<GroupInstance> groupInstancesList = new ArrayList<GroupInstance>();
		List<GroupDefinition> group = null;
		Either<List<GroupDefinition>, TitanOperationStatus> groupEither = groupOperation.getAllGroupsFromGraph(createdResourceInstance.getComponentUid(), NodeTypeEnum.Resource);
		if (groupEither.isRight() && groupEither.right().value() != TitanOperationStatus.OK && groupEither.right().value() != TitanOperationStatus.NOT_FOUND) {
			TitanOperationStatus status = groupEither.right().value();
			log.debug("Failed to associate group instances to component instance {}. Status is {}", componentInstance.getUniqueId(), status);
			return Either.right(status);
		} else {
			if (groupEither.isLeft()) {
				group = groupEither.left().value();
				if (group != null && !group.isEmpty()) {
					List<GroupDefinition> vfGroupsList = group.stream().filter(p -> p.getType().equals("org.openecomp.groups.VfModule")).collect(Collectors.toList());
					for (GroupDefinition groupDefinition : vfGroupsList) {
						Either<GroupInstance, StorageOperationStatus> status = createGroupInstance(ciVertex, groupDefinition, createdResourceInstance);
						if (status.isRight()) {
							log.debug("Failed to associate group instances to component instance {}. Status is {}", componentInstance.getUniqueId(), status);

						} else {
							GroupInstance groupInstance = status.left().value();
							groupInstancesList.add(groupInstance);
						}

					}
					createdResourceInstance.setGroupInstances(groupInstancesList);
				}
			}

		}
		return Either.left(createdResourceInstance);
	}

	public void generateCustomizationUUID(ComponentInstance componentInstance) {
		UUID uuid = UUID.randomUUID();
		componentInstance.setCustomizationUUID(uuid.toString());
	}

	/**
	 * 
	 * @param containerComponentId
	 * @param containerNodeType
	 * @param instanceNumber
	 * @param isCreateLogicaName
	 * @param componentInstance
	 * @param compInstNodeType
	 * @param allowDeleted
	 * @param metadataVertex
	 * @return
	 */
	public Either<TitanVertex, TitanOperationStatus> addComponentInstanceToContainerComponent(String containerComponentId, NodeTypeEnum containerNodeType, String instanceNumber, boolean isCreateLogicaName, ComponentInstance componentInstance,
			NodeTypeEnum compInstNodeType, boolean allowDeleted, TitanVertex metadataVertex) {
		TitanOperationStatus status;
		log.debug("Going to create component instance {} in component {}", componentInstance, containerComponentId);
		String instOriginComponentId = componentInstance.getComponentUid();
		String logicalName = componentInstance.getName();
		if (isCreateLogicaName)
			logicalName = createComponentInstLogicalName(instanceNumber, componentInstance.getName());

		ComponentInstanceData componentInstanceData = buildComponentInstanceData(componentInstance, containerComponentId, logicalName);
		Either<TitanVertex, TitanOperationStatus> originVertexEither = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), instOriginComponentId);
		if (originVertexEither.isRight()) {
			log.debug("Failed to fetch vertex of origin resource for id {} error {}", instOriginComponentId, originVertexEither.right().value());
			return Either.right(originVertexEither.right().value());
		}
		TitanVertex originVertex = originVertexEither.left().value();

		Boolean isDeleted = (Boolean) titanGenericDao.getProperty(metadataVertex, GraphPropertiesDictionary.IS_DELETED.getProperty());

		if (!allowDeleted && (isDeleted != null) && (isDeleted == true)) {
			log.debug("Component {} is already deleted. Cannot add component instance", instOriginComponentId);
			return Either.right(TitanOperationStatus.INVALID_ID);
		}
		String originType = (String) titanGenericDao.getProperty(originVertex, GraphPropertiesDictionary.LABEL.getProperty());
		String resourceType = (String) titanGenericDao.getProperty(originVertex, GraphPropertiesDictionary.RESOURCE_TYPE.getProperty());

		log.trace("Before adding component instance to graph. componentInstanceData = {}", componentInstanceData);

		Either<TitanVertex, TitanOperationStatus> createCIResult = titanGenericDao.createNode(componentInstanceData);

		log.debug("After adding component instance to graph. status is = {}", createCIResult);

		if (createCIResult.isRight()) {
			status = createCIResult.right().value();
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Add Component Instance");
			log.debug("Failed to create component instance node in graph. status is {}", status);
			return Either.right(status);
		}
		TitanVertex createdComponentInstanceVertex = createCIResult.left().value();
		TitanOperationStatus associateContainerRes = associateContainerCompToComponentInstance(metadataVertex, createdComponentInstanceVertex, logicalName);

		String componentInstanceUniqueId = componentInstanceData.getUniqueId();
		if (associateContainerRes != TitanOperationStatus.OK) {
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Add Component Instance");
			log.debug("Failed to associate container component {} to component instance {}. Status is {}", containerComponentId, componentInstanceUniqueId, associateContainerRes);
			return Either.right(associateContainerRes);
		}
		String originId = (String) titanGenericDao.getProperty(createdComponentInstanceVertex, GraphPropertiesDictionary.TYPE.getProperty());

		TitanOperationStatus associateToInstOriginComponent = associateToInstOriginComponent(createdComponentInstanceVertex, originVertex, originId);
		if (associateToInstOriginComponent != TitanOperationStatus.OK) {
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Add Component Instance");
			log.debug("Failed to associate component instance {} to its origin component {}. Status is {}", componentInstanceUniqueId, componentInstanceData.getComponentInstDataDefinition().getComponentUid(), associateToInstOriginComponent);
			return Either.right(associateToInstOriginComponent);
		}

		TitanOperationStatus associateCompInstToRequirements = associateCompInstToRequirements(createdComponentInstanceVertex, containerNodeType, compInstNodeType, originId);
		if (associateCompInstToRequirements != TitanOperationStatus.OK) {
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Add Component Instance");
			log.debug("Failed to associate component instance {} to its origin requirements. Status is {}", componentInstanceUniqueId, associateCompInstToRequirements);
			return Either.right(associateCompInstToRequirements);
		}
		TitanOperationStatus associateCompInstToCapabilities = associateCompInstToCapabilities(createdComponentInstanceVertex, containerNodeType, compInstNodeType, originId);
		if (associateCompInstToCapabilities != TitanOperationStatus.OK) {
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Add Component Instance");
			log.debug("Failed to associate component instance {} to its origin capabilities. Status is {}", componentInstanceUniqueId, associateCompInstToCapabilities);
			return Either.right(associateCompInstToCapabilities);
		}
		// Capability instance with property values implementation
		Either<List<ImmutablePair<TitanVertex, GraphEdge>>, TitanOperationStatus> cloneCapabilityInstancesRes = null;
		Either<List<GraphRelation>, TitanOperationStatus> associateComponentInstanceToCapabilityInstancesRes;
		status = null;
		if (!isCreateLogicaName) {
			// in case of cloning of component instance
			log.debug("Before cloning of capability instances of component instance {}.", componentInstance.getUniqueId());
			cloneCapabilityInstancesRes = cloneCapabilityInstancesOfResourceInstance(createdComponentInstanceVertex, componentInstance);
			if (cloneCapabilityInstancesRes.isRight() && !cloneCapabilityInstancesRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				status = cloneCapabilityInstancesRes.right().value();
				log.debug("Failed to clone capability instances of component instance {}. Status is {}", componentInstance.getUniqueId(), status);
			}
			log.trace("After cloning of capability instances of component instance {}. Status is {}", componentInstance.getUniqueId(), status);
		} else if (containerNodeType.equals(NodeTypeEnum.Resource) && componentInstance.getCapabilities() != null && !componentInstance.getCapabilities().isEmpty()) {
			// in case of creation from scar
			TitanOperationStatus addPropertiesRes = createCapabilityInstancesWithPropertyValues(createdComponentInstanceVertex, componentInstanceUniqueId, componentInstance.getCapabilities(), true);
			if (addPropertiesRes != TitanOperationStatus.OK) {
				status = addPropertiesRes;
				log.debug("Failed to create capability instances with property values for component instance {}. Status is {}", componentInstance.getUniqueId(), status);
			}
		}
		if (status == null && containerNodeType.equals(NodeTypeEnum.Service)) {
			Map<String, Object> properties = titanGenericDao.getProperties(createdComponentInstanceVertex);
			ComponentInstanceData createdComponentInstance = GraphElementFactory.createElement(NodeTypeEnum.ResourceInstance.getName(), GraphElementTypeEnum.Node, properties, ComponentInstanceData.class);
			if (cloneCapabilityInstancesRes == null || cloneCapabilityInstancesRes.isRight()) {
				// in case of creating of service
				log.trace("Before associating component instance {} to capability instances .", componentInstance.getUniqueId());
				associateComponentInstanceToCapabilityInstancesRes = associateComponentInstanceToCapabilityInstancesOfResourceInstance(componentInstance);
				if (associateComponentInstanceToCapabilityInstancesRes.isRight() && !associateComponentInstanceToCapabilityInstancesRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
					status = associateComponentInstanceToCapabilityInstancesRes.right().value();
					log.debug("Failed to associate capability instances to component instance {}. Status is {}", componentInstance.getUniqueId(), status);
				}
				log.trace("After associating component instance {} to capability instances . Status is {}", componentInstance.getUniqueId(), status);
			} else {
				// in case of cloning of service
				log.trace("Before associating created component instance {} to cloned capability instances.", componentInstanceUniqueId);
				TitanOperationStatus associationStatus = associateCreatedComponentInstanceToClonedCapabilityInstances(createdComponentInstanceVertex, componentInstanceUniqueId, cloneCapabilityInstancesRes.left().value());
				if (associationStatus != TitanOperationStatus.OK && associationStatus != TitanOperationStatus.NOT_FOUND) {
					status = associationStatus;
					log.debug("Failed to associate capability instances to component instance {}. Status is {}", componentInstance.getUniqueId(), status);
				}
				log.trace("After associating created component instance {} to cloned capability instances. Status is {}", componentInstanceUniqueId, status);
			}
		}

		if (status == null) {
			// ComponentInstance createdResourceInstance = new
			// ComponentInstance(createdComponentInstance.getComponentInstDataDefinition());
			//
			// String icon = (String) titanGenericDao.getProperty(originVertex,
			// GraphPropertiesDictionary.ICON.getProperty());
			// createdResourceInstance.setIcon(icon);
			return Either.left(createdComponentInstanceVertex);
		}
		return Either.right(status);
	}

	private Either<Map<CapabilityInstData, List<PropertyValueData>>, TitanOperationStatus> createCapabilityInstancesWithPropertyValues(String resourceInstanceId, Map<String, List<CapabilityDefinition>> capabilities,
			boolean isNewlyCreatedResourceInstance) {
		TitanOperationStatus error;
		Map<CapabilityInstData, List<PropertyValueData>> result = new HashMap<>();
		for (Entry<String, List<CapabilityDefinition>> capailityEntry : capabilities.entrySet()) {
			CapabilityDefinition capability = capailityEntry.getValue().get(0);
			if (capability.getProperties() != null && !capability.getProperties().isEmpty()) {
				Either<Map<CapabilityInstData, List<PropertyValueData>>, TitanOperationStatus> addPropertiesRes = addCapabilityPropertyValuesToResourceInstance(resourceInstanceId, capability, isNewlyCreatedResourceInstance);
				if (addPropertiesRes.isRight()) {
					error = addPropertiesRes.right().value();
					log.debug("Failed to add property values to capabilities of component instance {}. Status is {}", resourceInstanceId, error);
					return Either.right(error);
				} else {
					result.putAll(addPropertiesRes.left().value());
				}
			}
		}
		return Either.left(result);
	}

	private TitanOperationStatus createCapabilityInstancesWithPropertyValues(TitanVertex resourceInstanceVertex, String resourceInstanceId, Map<String, List<CapabilityDefinition>> capabilities, boolean isNewlyCreatedResourceInstance) {
		TitanOperationStatus result = TitanOperationStatus.OK;

		for (Entry<String, List<CapabilityDefinition>> capailityEntry : capabilities.entrySet()) {
			CapabilityDefinition capability = capailityEntry.getValue().get(0);
			if (capability.getProperties() != null && !capability.getProperties().isEmpty()) {
				TitanOperationStatus addPropertiesRes = addCapabilityPropertyValuesToResourceInstance(resourceInstanceVertex, resourceInstanceId, capability, isNewlyCreatedResourceInstance);
				if (addPropertiesRes != TitanOperationStatus.OK) {
					result = addPropertiesRes;
					log.debug("Failed to add property values to capabilities of component instance {}. Status is {}", resourceInstanceId, result);
					return result;
				}
			}
		}
		return result;
	}

	private Either<List<GraphRelation>, TitanOperationStatus> associateCreatedComponentInstanceToClonedCapabilityInstances(String newComponentResourceId, List<ImmutablePair<CapabilityInstData, GraphEdge>> capabilityInstances) {
		TitanOperationStatus error = null;
		List<GraphRelation> relationsToCapabilityInstances = new ArrayList<>();
		UniqueIdData componentInstanceIdData = new UniqueIdData(NodeTypeEnum.ResourceInstance, newComponentResourceId);
		for (ImmutablePair<CapabilityInstData, GraphEdge> capInstPair : capabilityInstances) {
			Either<GraphRelation, TitanOperationStatus> associateComponentInstanceToCapabilityinstanceRes = titanGenericDao.createRelation(componentInstanceIdData, capInstPair.getLeft(), GraphEdgeLabels.CAPABILITY_INST,
					capInstPair.getRight().getProperties());
			if (associateComponentInstanceToCapabilityinstanceRes.isRight()) {
				error = associateComponentInstanceToCapabilityinstanceRes.right().value();
				log.debug("Failed to associate capability instance {} to resource instance {} status is {}.", capInstPair.getLeft().getUniqueId(), newComponentResourceId, error);
				break;
			} else {
				relationsToCapabilityInstances.add(associateComponentInstanceToCapabilityinstanceRes.left().value());
			}
		}
		if (error == null) {
			return Either.left(relationsToCapabilityInstances);
		}
		return Either.right(error);
	}

	private TitanOperationStatus associateCreatedComponentInstanceToClonedCapabilityInstances(TitanVertex riVertex, String newComponentResourceId, List<ImmutablePair<TitanVertex, GraphEdge>> capabilityInstances) {
		TitanOperationStatus error = null;
		for (ImmutablePair<TitanVertex, GraphEdge> capInstPair : capabilityInstances) {
			TitanOperationStatus associateComponentInstanceToCapabilityinstanceRes = titanGenericDao.createEdge(riVertex, capInstPair.getLeft(), GraphEdgeLabels.CAPABILITY_INST, capInstPair.getRight().getProperties());
			if (associateComponentInstanceToCapabilityinstanceRes != TitanOperationStatus.OK) {
				error = associateComponentInstanceToCapabilityinstanceRes;
				log.debug("Failed to associate capability instance {} to resource instance {} status is {} .", capInstPair.getLeft(), newComponentResourceId, error);
				break;
			}
		}
		if (error == null) {
			return TitanOperationStatus.OK;
		}
		return error;
	}

	private Either<List<GraphRelation>, TitanOperationStatus> associateComponentInstanceToCapabilityInstancesOfResourceInstance(ComponentInstance componentInstance) {
		TitanOperationStatus error = null;
		String resourceId = componentInstance.getComponentUid();
		String componentResourceId = componentInstance.getUniqueId();
		UniqueIdData componentInstanceIdData = new UniqueIdData(NodeTypeEnum.ResourceInstance, componentResourceId);
		List<ImmutablePair<ComponentInstanceData, GraphEdge>> resourceInstancesPair;
		List<ImmutablePair<CapabilityInstData, GraphEdge>> allCapabilityInstancesList = new ArrayList<>();
		List<GraphRelation> relationsToCapabilityInstances = new ArrayList<>();
		Either<List<ImmutablePair<ComponentInstanceData, GraphEdge>>, TitanOperationStatus> getAllResourceInstanceRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId,
				GraphEdgeLabels.RESOURCE_INST, NodeTypeEnum.ResourceInstance, ComponentInstanceData.class);
		if (getAllResourceInstanceRes.isRight() && !getAllResourceInstanceRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
			error = getAllResourceInstanceRes.right().value();
			log.debug("Failed to retrieve resource instances from resource {} status is {}.", resourceId, error);
		}
		if (getAllResourceInstanceRes.isLeft()) {
			resourceInstancesPair = getAllResourceInstanceRes.left().value();
			ComponentInstanceData ri;
			for (ImmutablePair<ComponentInstanceData, GraphEdge> riPair : resourceInstancesPair) {
				ri = riPair.getLeft();
				Either<List<ImmutablePair<CapabilityInstData, GraphEdge>>, TitanOperationStatus> getCapabilityInstancesRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), ri.getUniqueId(),
						GraphEdgeLabels.CAPABILITY_INST, NodeTypeEnum.CapabilityInst, CapabilityInstData.class);
				if (getCapabilityInstancesRes.isRight() && !getCapabilityInstancesRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
					error = getCapabilityInstancesRes.right().value();
					log.debug("Failed to retrieve capability instances of resource instance {} status is {}.", ri.getUniqueId(), error);
					break;
				}
				if (getCapabilityInstancesRes.isLeft()) {
					allCapabilityInstancesList.addAll(getCapabilityInstancesRes.left().value());
				}
			}
		}
		if (error == null && !allCapabilityInstancesList.isEmpty()) {
			for (ImmutablePair<CapabilityInstData, GraphEdge> capInstPair : allCapabilityInstancesList) {
				Either<GraphRelation, TitanOperationStatus> associateComponentInstanceToCapabilityinstanceRes = titanGenericDao.createRelation(componentInstanceIdData, capInstPair.getLeft(), GraphEdgeLabels.CAPABILITY_INST,
						capInstPair.getRight().getProperties());
				if (associateComponentInstanceToCapabilityinstanceRes.isRight()) {
					error = associateComponentInstanceToCapabilityinstanceRes.right().value();
					log.debug("Failed to associate capability instance {} to resource instance {} status is {}.", capInstPair.getLeft().getUniqueId(), componentResourceId, error);
					break;
				} else {
					relationsToCapabilityInstances.add(associateComponentInstanceToCapabilityinstanceRes.left().value());
				}
			}
		}
		if (error == null) {
			return Either.left(relationsToCapabilityInstances);
		}
		return Either.right(error);
	}

	private NodeTypeEnum detectOriginType(String label, ComponentInstanceData componentInstanceData, String resourceTypeStr) {
		NodeTypeEnum res = null;
		res = NodeTypeEnum.getByName(label);
		switch (res) {
		case Service:
			componentInstanceData.getComponentInstDataDefinition().setOriginType(OriginTypeEnum.SERVICE);
			break;
		case Product:
			componentInstanceData.getComponentInstDataDefinition().setOriginType(OriginTypeEnum.PRODUCT);
			break;
		case Resource:
			ResourceTypeEnum resourceType = ResourceTypeEnum.valueOf(resourceTypeStr);
			switch (resourceType) {
			case VF:
				componentInstanceData.getComponentInstDataDefinition().setOriginType(OriginTypeEnum.VF);
				break;
			case VFC:
				componentInstanceData.getComponentInstDataDefinition().setOriginType(OriginTypeEnum.VFC);
				break;
			case VFCMT:
				componentInstanceData.getComponentInstDataDefinition().setOriginType(OriginTypeEnum.VFCMT);
				break;
			case CP:
				componentInstanceData.getComponentInstDataDefinition().setOriginType(OriginTypeEnum.CP);
				break;
			case VL:
				componentInstanceData.getComponentInstDataDefinition().setOriginType(OriginTypeEnum.VL);
				break;
			}
			break;
		default:
			break;
		}
		return res;
	}

	private Either<GraphRelation, TitanOperationStatus> associateToInstOriginComponent(ComponentInstanceData componentInstanceData, NodeTypeEnum compInstNodeType) {

		UniqueIdData resourceIdData = new UniqueIdData(compInstNodeType, componentInstanceData.getComponentInstDataDefinition().getComponentUid());

		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(componentInstanceData, resourceIdData, GraphEdgeLabels.INSTANCE_OF, null);

		log.debug("After associating resource instance {} to resource {}. status is {}", componentInstanceData.getUniqueId(), componentInstanceData.getComponentInstDataDefinition().getUniqueId(), createRelation);

		return createRelation;
	}

	private TitanOperationStatus associateToInstOriginComponent(TitanVertex componentInstanceVertex, TitanVertex originVertex, String originId) {

		TitanOperationStatus createRelation = titanGenericDao.createEdge(componentInstanceVertex, originVertex, GraphEdgeLabels.INSTANCE_OF, null);

		log.debug("After associating resource instance {} to resource {}. status is {}", componentInstanceVertex, originId, createRelation);

		return createRelation;
	}

	private Either<List<GraphRelation>, TitanOperationStatus> associateCompInstToRequirements(ComponentInstanceData componentInstanceData, NodeTypeEnum containerNodeType, NodeTypeEnum compInstNodeType) {
		log.trace("Starting to copy origin component requirements to its component instance");
		String compInstOriginId = componentInstanceData.getComponentInstDataDefinition().getComponentUid();
		List<GraphRelation> graphRelations = new ArrayList<>();

		// case of VFC / CP / VL
		if (compInstNodeType.equals(NodeTypeEnum.Resource)) {
			createRequirementRelationsFromAtomicResource(componentInstanceData, compInstOriginId, graphRelations);

		}
		// case of VF / Service / Product
		createCalculatedRequirementRelationsFromComponent(componentInstanceData, containerNodeType, compInstNodeType, graphRelations, compInstOriginId);

		log.trace("Finished to copy origin component requirements to its component instance, created {} new calculated requirement relations", graphRelations.size());
		return Either.left(graphRelations);
	}

	private TitanOperationStatus associateCompInstToRequirements(TitanVertex componentInstanceVertex, NodeTypeEnum containerNodeType, NodeTypeEnum compInstNodeType, String originId) {
		log.trace("Starting to copy origin component requirements to its component instance");
		TitanOperationStatus status = TitanOperationStatus.OK;
		// case of VFC / CP / VL
		if (compInstNodeType.equals(NodeTypeEnum.Resource)) {
			status = createRequirementRelationsFromAtomicResource(componentInstanceVertex, originId);
			if (!status.equals(TitanOperationStatus.OK)) {
				log.debug("Failed create relation to requirement of origin {} error {}", originId, status);
				return status;
			}
		}
		// case of VF / Service / Product
		status = createCalculatedRequirementRelationsFromComponent(componentInstanceVertex, containerNodeType, compInstNodeType, originId);

		log.trace("Finished to copy origin component requirements to its component instance with status {}", status);
		return status;
	}

	private void createRequirementRelationsFromAtomicResource(ComponentInstanceData componentInstanceData, String compInstOriginId, List<GraphRelation> graphRelations) {
		Map<String, RequirementDefinition> requirements = new HashMap<String, RequirementDefinition>();
		Set<String> caseInsensitiveReqNames = new HashSet<>();

		TitanOperationStatus status = requirementOperation.findAllRequirementsRecursively(compInstOriginId, requirements, caseInsensitiveReqNames);
		if (status != TitanOperationStatus.OK) {
			log.debug("Couldn't fetch requirements of component {}, error: {}", compInstOriginId, status);
		}

		log.trace("Found {} requirements for component {}, ", requirements.size(), compInstOriginId);
		for (Entry<String, RequirementDefinition> reqPair : requirements.entrySet()) {
			RequirementDefinition requirementDef = reqPair.getValue();
			RequirementData requirementData = new RequirementData();
			requirementData.setUniqueId(requirementDef.getUniqueId());

			log.trace("Creating calculated requirement relation from component instance {} to requirement {}", componentInstanceData.getUniqueId(), requirementDef.getUniqueId());
			Map<String, Object> props = new HashMap<String, Object>();
			props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), reqPair.getKey());

			props.put(GraphEdgePropertiesDictionary.OWNER_ID.getProperty(), componentInstanceData.getUniqueId());
			if (requirementDef.getMinOccurrences() == null) {
				props.put(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty(), RequirementDataDefinition.MIN_OCCURRENCES);
			} else {
				props.put(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty(), requirementDef.getMinOccurrences());
			}
			if (requirementDef.getMaxOccurrences() == null) {
				props.put(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty(), RequirementDataDefinition.MAX_DEFAULT_OCCURRENCES);
			} else {
				props.put(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty(), requirementDef.getMaxOccurrences());
			}

			Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(componentInstanceData, requirementData, GraphEdgeLabels.CALCULATED_REQUIREMENT, props);
			if (createRelation.isRight()) {
				TitanOperationStatus titanOperationStatus = createRelation.right().value();
				log.debug("Failed to create calculated requirement from component instance {} to requirement {}, error: {}", componentInstanceData.getUniqueId(), requirementDef.getUniqueId(), titanOperationStatus);
			}
			graphRelations.add(createRelation.left().value());
		}
	}

	private TitanOperationStatus createRequirementRelationsFromAtomicResource(TitanVertex componentInstanceVertex, String compInstOriginId) {
		Map<String, RequirementDefinition> requirements = new HashMap<String, RequirementDefinition>();
		Set<String> caseInsensitiveReqNames = new HashSet<>();

		TitanOperationStatus status = requirementOperation.findAllRequirementsRecursively(compInstOriginId, requirements, caseInsensitiveReqNames);
		if (status != TitanOperationStatus.OK && status != TitanOperationStatus.NOT_FOUND) {
			log.debug("Couldn't fetch requirements of component {}, error: {}", compInstOriginId, status);
			return status;
		}

		String compoInstId = (String) titanGenericDao.getProperty(componentInstanceVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
		log.trace("Found {} requirements for component {}, ", requirements.size(), compInstOriginId);
		for (Entry<String, RequirementDefinition> reqPair : requirements.entrySet()) {
			RequirementDefinition requirementDef = reqPair.getValue();
			RequirementData requirementData = new RequirementData();
			requirementData.setUniqueId(requirementDef.getUniqueId());

			log.trace("Creating calculated requirement relation from component instance {} to requirement {}", compoInstId, requirementDef.getUniqueId());
			Map<String, Object> props = new HashMap<String, Object>();
			props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), reqPair.getKey());

			props.put(GraphEdgePropertiesDictionary.OWNER_ID.getProperty(), compoInstId);
			if (requirementDef.getMinOccurrences() == null) {
				props.put(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty(), RequirementDataDefinition.MIN_OCCURRENCES);
			} else {
				props.put(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty(), requirementDef.getMinOccurrences());
			}
			if (requirementDef.getMaxOccurrences() == null) {
				props.put(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty(), RequirementDataDefinition.MAX_DEFAULT_OCCURRENCES);
			} else {
				props.put(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty(), requirementDef.getMaxOccurrences());
			}

			TitanOperationStatus createRelation = titanGenericDao.createEdge(componentInstanceVertex, requirementData, GraphEdgeLabels.CALCULATED_REQUIREMENT, props);
			if (!createRelation.equals(TitanOperationStatus.OK)) {
				log.debug("Failed to create calculated requirement from component instance {} to requirement {}, error: {}", compoInstId, requirementDef.getUniqueId(), createRelation);
				return createRelation;
			}
		}
		return TitanOperationStatus.OK;
	}

	private Either<List<GraphRelation>, TitanOperationStatus> associateCompInstToCapabilities(ComponentInstanceData componentInstanceData, NodeTypeEnum containerNodeType, NodeTypeEnum compInstNodeType) {

		log.trace("Starting to copy origin component capabilities to its component instance");
		List<GraphRelation> graphRelations = new ArrayList<>();

		String compInstOriginId = componentInstanceData.getComponentInstDataDefinition().getComponentUid();

		// case of VFC / CP / VL
		if (compInstNodeType.equals(NodeTypeEnum.Resource)) {
			createCaculatedRelationsFromAtomicResource(componentInstanceData, graphRelations, compInstOriginId);
		}

		// case of VF / Service / Product
		createCalculatedCapabilityRelationsFromComponent(componentInstanceData, containerNodeType, compInstNodeType, graphRelations, compInstOriginId);

		log.trace("Finished to copy origin component capabilities to its component instance, created {} new calculated capability relations", graphRelations.size());
		return Either.left(graphRelations);
	}

	private TitanOperationStatus associateCompInstToCapabilities(TitanVertex componentInstanceVertex, NodeTypeEnum containerNodeType, NodeTypeEnum compInstNodeType, String originId) {

		log.trace("Starting to copy origin component capabilities to its component instance");
		TitanOperationStatus status = TitanOperationStatus.OK;

		// case of VFC / CP / VL
		if (compInstNodeType.equals(NodeTypeEnum.Resource)) {
			status = createCaculatedRelationsFromAtomicResource(componentInstanceVertex, originId);
			if (!status.equals(TitanOperationStatus.OK)) {
				return status;
			}
		}

		// case of VF / Service / Product
		status = createCalculatedCapabilityRelationsFromComponent(componentInstanceVertex, containerNodeType, compInstNodeType, originId);

		return status;
	}

	private void createCalculatedRequirementRelationsFromComponent(ComponentInstanceData componentInstanceData, NodeTypeEnum containerNodeType, NodeTypeEnum compInstNodeType, List<GraphRelation> graphRelations, String compInstOriginId) {

		Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, TitanOperationStatus> componentInstancesOfComponent = getComponentInstancesOfComponent(compInstOriginId, containerNodeType, compInstNodeType);
		if (componentInstancesOfComponent.isLeft() && !componentInstancesOfComponent.left().value().left.isEmpty()) {
			List<ComponentInstance> componentInstances = componentInstancesOfComponent.left().value().left;
			for (ComponentInstance componentInstance : componentInstances) {
				Either<List<ImmutablePair<RequirementData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(containerNodeType), componentInstance.getUniqueId(),
						GraphEdgeLabels.CALCULATED_REQUIREMENT, NodeTypeEnum.Requirement, RequirementData.class);

				if (childrenNodes.isLeft() && !childrenNodes.left().value().isEmpty()) {
					List<ImmutablePair<RequirementData, GraphEdge>> list = childrenNodes.left().value();
					for (ImmutablePair<RequirementData, GraphEdge> calculatedReq : list) {

						GraphEdge edge = calculatedReq.right;
						Map<String, Object> properties = edge.getProperties();
						String source = null;
						String occurrences = RequirementDataDefinition.MAX_DEFAULT_OCCURRENCES;
						String minOccurrences = RequirementDataDefinition.MIN_OCCURRENCES;

						if (properties != null && properties.containsKey(GraphEdgePropertiesDictionary.SOURCE.getProperty())) {
							source = (String) properties.get(GraphEdgePropertiesDictionary.SOURCE.getProperty());
						}
						if (properties != null && properties.containsKey(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty())) {
							occurrences = (String) properties.get(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty());
						}
						if (properties != null && properties.containsKey(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty())) {
							minOccurrences = (String) properties.get(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty());
						}

						String capabilityName = (String) properties.get(GraphEdgePropertiesDictionary.NAME.getProperty());
						Either<GraphRelation, TitanOperationStatus> createRelation = createCalculatedRequirementEdge(componentInstanceData, source, capabilityName, calculatedReq.left, componentInstance, occurrences, minOccurrences);
						if (createRelation.isLeft()) {
							graphRelations.add(createRelation.left().value());
						}
					}
				}
			}
		}
	}

	private TitanOperationStatus createCalculatedRequirementRelationsFromComponent(TitanVertex componentInstanceVertex, NodeTypeEnum containerNodeType, NodeTypeEnum compInstNodeType, String compInstOriginId) {

		Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, TitanOperationStatus> componentInstancesOfComponent = getComponentInstancesOfComponent(compInstOriginId, containerNodeType, compInstNodeType);
		if (componentInstancesOfComponent.isLeft() && !componentInstancesOfComponent.left().value().left.isEmpty()) {
			List<ComponentInstance> componentInstances = componentInstancesOfComponent.left().value().left;
			for (ComponentInstance componentInstance : componentInstances) {

				Either<List<ImmutablePair<TitanVertex, Edge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenVertecies(UniqueIdBuilder.getKeyByNodeType(containerNodeType), componentInstance.getUniqueId(),
						GraphEdgeLabels.CALCULATED_REQUIREMENT);

				if (childrenNodes.isLeft() && !childrenNodes.left().value().isEmpty()) {
					List<ImmutablePair<TitanVertex, Edge>> list = childrenNodes.left().value();
					for (ImmutablePair<TitanVertex, Edge> calculatedReq : list) {

						Edge edge = calculatedReq.right;
						Map<String, Object> properties = titanGenericDao.getProperties(edge);
						String source = null;
						String occurrences = RequirementDataDefinition.MAX_DEFAULT_OCCURRENCES;
						String minOccurrences = RequirementDataDefinition.MIN_OCCURRENCES;

						if (properties != null && properties.containsKey(GraphEdgePropertiesDictionary.SOURCE.getProperty())) {
							source = (String) properties.get(GraphEdgePropertiesDictionary.SOURCE.getProperty());
						}
						if (properties != null && properties.containsKey(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty())) {
							occurrences = (String) properties.get(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty());
						}
						if (properties != null && properties.containsKey(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty())) {
							minOccurrences = (String) properties.get(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty());
						}

						String capabilityName = (String) properties.get(GraphEdgePropertiesDictionary.NAME.getProperty());
						TitanOperationStatus createRelation = createCalculatedRequirementEdge(componentInstanceVertex, source, capabilityName, calculatedReq.left, componentInstance, occurrences, minOccurrences);
						if (!createRelation.equals(TitanOperationStatus.OK)) {
							log.debug("Failed to create calculated requirement edge, status ", createRelation);
							return createRelation;
						}
					}
				}
			}
		}
		return TitanOperationStatus.OK;
	}

	private void createCalculatedCapabilityRelationsFromComponent(ComponentInstanceData componentInstanceData, NodeTypeEnum containerNodeType, NodeTypeEnum compInstNodeType, List<GraphRelation> graphRelations, String compInstOriginId) {

		Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, TitanOperationStatus> componentInstancesOfComponent = getComponentInstancesOfComponent(compInstOriginId, containerNodeType, compInstNodeType);
		if (componentInstancesOfComponent.isLeft() && !componentInstancesOfComponent.left().value().left.isEmpty()) {
			List<ComponentInstance> componentInstances = componentInstancesOfComponent.left().value().left;
			for (ComponentInstance componentInstance : componentInstances) {
				Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(compInstNodeType), componentInstance.getUniqueId(),
						GraphEdgeLabels.CALCULATED_CAPABILITY, NodeTypeEnum.Capability, CapabilityData.class);

				if (childrenNodes.isLeft() && !childrenNodes.left().value().isEmpty()) {
					List<ImmutablePair<CapabilityData, GraphEdge>> list = childrenNodes.left().value();
					for (ImmutablePair<CapabilityData, GraphEdge> calculatedCap : list) {

						GraphEdge edge = calculatedCap.right;
						Map<String, Object> properties = edge.getProperties();
						String source = null;
						if (properties != null && properties.containsKey(GraphEdgePropertiesDictionary.SOURCE.getProperty())) {
							source = (String) properties.get(GraphEdgePropertiesDictionary.SOURCE.getProperty());
						}
						String minOccurrences = CapabilityDataDefinition.MIN_OCCURRENCES;
						String occurrences = CapabilityDataDefinition.MAX_OCCURRENCES;
						if (properties != null && properties.containsKey(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty())) {
							minOccurrences = (String) properties.get(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty());
						}
						if (properties != null && properties.containsKey(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty())) {
							occurrences = (String) properties.get(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty());
						}

						String capabilityName = (String) properties.get(GraphEdgePropertiesDictionary.NAME.getProperty());
						Either<GraphRelation, TitanOperationStatus> createRelation = createCalculatedCapabilityEdge(componentInstanceData, source, capabilityName, calculatedCap.left, componentInstance.getUniqueId(), minOccurrences, occurrences);
						if (createRelation.isLeft()) {
							graphRelations.add(createRelation.left().value());
						}
					}
				}
			}
		}
	}

	private TitanOperationStatus createCalculatedCapabilityRelationsFromComponent(TitanVertex componentInstanceVertex, NodeTypeEnum containerNodeType, NodeTypeEnum compInstNodeType, String compInstOriginId) {

		Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, TitanOperationStatus> componentInstancesOfComponent = getComponentInstancesOfComponent(compInstOriginId, containerNodeType, compInstNodeType);
		if (componentInstancesOfComponent.isLeft() && !componentInstancesOfComponent.left().value().left.isEmpty()) {
			List<ComponentInstance> componentInstances = componentInstancesOfComponent.left().value().left;
			for (ComponentInstance componentInstance : componentInstances) {
				Either<List<ImmutablePair<TitanVertex, Edge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenVertecies(UniqueIdBuilder.getKeyByNodeType(compInstNodeType), componentInstance.getUniqueId(),
						GraphEdgeLabels.CALCULATED_CAPABILITY);

				if (childrenNodes.isLeft() && !childrenNodes.left().value().isEmpty()) {
					List<ImmutablePair<TitanVertex, Edge>> list = childrenNodes.left().value();
					for (ImmutablePair<TitanVertex, Edge> calculatedCap : list) {

						Edge edge = calculatedCap.right;
						Map<String, Object> properties = titanGenericDao.getProperties(edge);
						String source = null;
						if (properties != null && properties.containsKey(GraphEdgePropertiesDictionary.SOURCE.getProperty())) {
							source = (String) properties.get(GraphEdgePropertiesDictionary.SOURCE.getProperty());
						}
						String minOccurrences = CapabilityDataDefinition.MIN_OCCURRENCES;
						String occurrences = CapabilityDataDefinition.MAX_OCCURRENCES;
						if (properties != null && properties.containsKey(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty())) {
							minOccurrences = (String) properties.get(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty());
						}
						if (properties != null && properties.containsKey(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty())) {
							occurrences = (String) properties.get(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty());
						}

						String capabilityName = (String) properties.get(GraphEdgePropertiesDictionary.NAME.getProperty());
						TitanOperationStatus createRelation = createCalculatedCapabilityEdge(componentInstanceVertex, source, capabilityName, calculatedCap.left, componentInstance.getUniqueId(), minOccurrences, occurrences);
						if (!createRelation.equals(TitanOperationStatus.OK)) {
							return createRelation;
						}
					}
				}
			}
		}
		return TitanOperationStatus.OK;
	}

	private void createCaculatedRelationsFromAtomicResource(ComponentInstanceData componentInstanceData, List<GraphRelation> graphRelations, String compInstOriginId) {

		Map<String, CapabilityDefinition> capabilities = new HashMap<String, CapabilityDefinition>();
		Set<String> caseInsensitiveCapNames = new HashSet<>();
		TitanOperationStatus getAllCapabilities = capabilityOperation.getAllCapabilitiesRecusive(NodeTypeEnum.Resource, compInstOriginId, true, capabilities, caseInsensitiveCapNames, true);

		if (!getAllCapabilities.equals(TitanOperationStatus.OK)) {
			if (getAllCapabilities != TitanOperationStatus.NOT_FOUND) {
				log.debug("Couldn't fetch capabilities of component {}, error: {}", compInstOriginId, getAllCapabilities);
				return;
			}
		}
		log.trace("Found {} capabilities for component {}, ", capabilities.size(), compInstOriginId);
		for (Entry<String, CapabilityDefinition> capPair : capabilities.entrySet()) {
			CapabilityDefinition capabilityData = capPair.getValue();
			log.trace("Creating calculated capability relation from component instance {} to capability {}", componentInstanceData.getUniqueId(), capabilityData.getUniqueId());
			CapabilityData capabilityDataNode = new CapabilityData();
			capabilityDataNode.setUniqueId(capabilityData.getUniqueId());
			String minOccurrences = CapabilityDataDefinition.MIN_OCCURRENCES;
			String occurrences = CapabilityDataDefinition.MAX_OCCURRENCES;
			if (capabilityData.getMinOccurrences() != null) {
				minOccurrences = capabilityData.getMinOccurrences();
			}
			if (capabilityData.getMinOccurrences() != null) {
				occurrences = capabilityData.getMaxOccurrences();
			}

			Either<GraphRelation, TitanOperationStatus> createRelation = createCalculatedCapabilityEdge(componentInstanceData, compInstOriginId, capPair.getKey(), capabilityDataNode, componentInstanceData.getUniqueId(), minOccurrences, occurrences);
			graphRelations.add(createRelation.left().value());
		}
	}

	private TitanOperationStatus createCaculatedRelationsFromAtomicResource(TitanVertex componentInstanceVertex, String compInstOriginId) {

		Map<String, CapabilityDefinition> capabilities = new HashMap<String, CapabilityDefinition>();
		Set<String> caseInsensitiveCapNames = new HashSet<>();
		TitanOperationStatus getAllCapabilities = capabilityOperation.getAllCapabilitiesRecusive(NodeTypeEnum.Resource, compInstOriginId, true, capabilities, caseInsensitiveCapNames, true);

		if (!getAllCapabilities.equals(TitanOperationStatus.OK)) {
			if (getAllCapabilities != TitanOperationStatus.NOT_FOUND) {
				log.debug("Couldn't fetch capabilities of component {}, error: {}", compInstOriginId, getAllCapabilities);
			}
		}
		log.trace("Found {} capabilities for component {}, ", capabilities.size(), compInstOriginId);
		String compoInstId = (String) titanGenericDao.getProperty(componentInstanceVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());

		for (Entry<String, CapabilityDefinition> capPair : capabilities.entrySet()) {
			CapabilityDefinition capabilityData = capPair.getValue();
			log.trace("Creating calculated capability relation from component instance {} to capability {}", compoInstId, capabilityData.getUniqueId());
			CapabilityData capabilityDataNode = new CapabilityData();
			capabilityDataNode.setUniqueId(capabilityData.getUniqueId());
			String minOccurrences = CapabilityDataDefinition.MIN_OCCURRENCES;
			String occurrences = CapabilityDataDefinition.MAX_OCCURRENCES;
			if (capabilityData.getMinOccurrences() != null) {
				minOccurrences = capabilityData.getMinOccurrences();
			}
			if (capabilityData.getMinOccurrences() != null) {
				occurrences = capabilityData.getMaxOccurrences();
			}

			TitanOperationStatus createRelation = createCalculatedCapabilityEdge(componentInstanceVertex, compInstOriginId, capPair.getKey(), capabilityDataNode, compoInstId, minOccurrences, occurrences);
			if (!createRelation.equals(TitanOperationStatus.OK)) {
				return createRelation;
			}
		}
		return TitanOperationStatus.OK;
	}

	private Either<GraphRelation, TitanOperationStatus> createCalculatedCapabilityEdge(ComponentInstanceData componentInstanceData, String compInstOriginId, String capabilityName, CapabilityData capabilityDataNode, String componentInstanceId,
			String minOccurrences, String occurrences) {
		Map<String, Object> props = prepareEdgeCapabiltyProperites(compInstOriginId, capabilityName, componentInstanceId, minOccurrences, occurrences);

		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(componentInstanceData, capabilityDataNode, GraphEdgeLabels.CALCULATED_CAPABILITY, props);
		if (createRelation.isRight()) {
			TitanOperationStatus titanOperationStatus = createRelation.right().value();
			log.debug("Failed to create calculated capability from component instance {} to capability {}, error: {}", componentInstanceData.getUniqueId(), capabilityDataNode.getUniqueId(), titanOperationStatus);
			return Either.right(titanOperationStatus);
		}
		return createRelation;
	}

	private TitanOperationStatus createCalculatedCapabilityEdge(TitanVertex componentInstanceVertex, String compInstOriginId, String capabilityName, CapabilityData capabilityDataNode, String componentInstanceId, String minOccurrences,
			String occurrences) {
		Map<String, Object> props = prepareEdgeCapabiltyProperites(compInstOriginId, capabilityName, componentInstanceId, minOccurrences, occurrences);

		TitanOperationStatus createRelation = titanGenericDao.createEdge(componentInstanceVertex, capabilityDataNode, GraphEdgeLabels.CALCULATED_CAPABILITY, props);
		if (!createRelation.equals(TitanOperationStatus.OK)) {
			log.debug("Failed to create calculated capability from component instance {} to capability {}, error: {}", componentInstanceId, capabilityDataNode.getUniqueId(), createRelation);
		}
		return createRelation;
	}

	private TitanOperationStatus createCalculatedCapabilityEdge(TitanVertex componentInstanceVertex, String compInstOriginId, String capabilityName, TitanVertex capabilityDataVertex, String componentInstanceId, String minOccurrences,
			String occurrences) {
		Map<String, Object> props = prepareEdgeCapabiltyProperites(compInstOriginId, capabilityName, componentInstanceId, minOccurrences, occurrences);

		TitanOperationStatus createRelation = titanGenericDao.createEdge(componentInstanceVertex, capabilityDataVertex, GraphEdgeLabels.CALCULATED_CAPABILITY, props);
		if (!createRelation.equals(TitanOperationStatus.OK)) {
			log.debug("Failed to create calculated capability from component instance {} to capability {}, error: {}", componentInstanceId, capabilityName, createRelation);
		}
		return createRelation;
	}

	private Map<String, Object> prepareEdgeCapabiltyProperites(String compInstOriginId, String capabilityName, String componentInstanceId, String minOccurrences, String occurrences) {
		Map<String, Object> props = new HashMap<String, Object>();
		if (capabilityName != null)
			props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), capabilityName);
		if (compInstOriginId != null)
			props.put(GraphEdgePropertiesDictionary.SOURCE.getProperty(), compInstOriginId);
		if (componentInstanceId != null) {
			props.put(GraphEdgePropertiesDictionary.OWNER_ID.getProperty(), componentInstanceId);
		}
		props.put(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty(), minOccurrences);
		props.put(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty(), occurrences);
		return props;
	}

	private Either<GraphRelation, TitanOperationStatus> createCalculatedRequirementEdge(ComponentInstanceData componentInstanceData, String compInstOriginId, String capabilityName, RequirementData requirementData, ComponentInstance componentInstance,
			String occurrences, String minOccurrences) {
		Map<String, Object> props = new HashMap<String, Object>();
		if (capabilityName != null)
			props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), capabilityName);
		if (compInstOriginId != null)
			props.put(GraphEdgePropertiesDictionary.SOURCE.getProperty(), compInstOriginId);
		if (componentInstance != null) {
			props.put(GraphEdgePropertiesDictionary.OWNER_ID.getProperty(), componentInstance.getUniqueId());
		}
		props.put(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty(), occurrences);
		props.put(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty(), minOccurrences);

		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(componentInstanceData, requirementData, GraphEdgeLabels.CALCULATED_REQUIREMENT, props);
		if (createRelation.isRight()) {
			TitanOperationStatus titanOperationStatus = createRelation.right().value();
			log.debug("Failed to create calculated requirement from component instance {} to requirement {}, error: {}", componentInstanceData.getUniqueId(), requirementData.getUniqueId(), titanOperationStatus);
			return Either.right(titanOperationStatus);
		}
		return createRelation;
	}

	private TitanOperationStatus createCalculatedRequirementEdge(TitanVertex componentInstanceVertex, String compInstOriginId, String capabilityName, TitanVertex requirementVertex, ComponentInstance componentInstance, String occurrences,
			String minOccurrences) {
		Map<String, Object> props = new HashMap<String, Object>();
		if (capabilityName != null)
			props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), capabilityName);
		if (compInstOriginId != null)
			props.put(GraphEdgePropertiesDictionary.SOURCE.getProperty(), compInstOriginId);
		if (componentInstance != null) {
			props.put(GraphEdgePropertiesDictionary.OWNER_ID.getProperty(), componentInstance.getUniqueId());
		}
		props.put(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty(), occurrences);
		props.put(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty(), minOccurrences);

		TitanOperationStatus createRelation = titanGenericDao.createEdge(componentInstanceVertex, requirementVertex, GraphEdgeLabels.CALCULATED_REQUIREMENT, props);
		if (!createRelation.equals(TitanOperationStatus.OK)) {
			log.debug("Failed to create calculated requirement from component instance {} to requirement {}, error: {}", componentInstance.getUniqueId(), capabilityName, createRelation);
			return createRelation;
		}
		return createRelation;
	}

	/**
	 * Make a relation between service to resource instance.
	 * 
	 * @param containerCompIdData
	 * @param componentInstanceData
	 * @param logicalName
	 * @return
	 */
	private Either<GraphRelation, TitanOperationStatus> associateContainerCompToComponentInstance(UniqueIdData containerCompIdData, ComponentInstanceData componentInstanceData, String logicalName) {
		Map<String, Object> properties = new HashMap<String, Object>();

		properties.put(GraphPropertiesDictionary.NAME.getProperty(), logicalName);
		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(containerCompIdData, componentInstanceData, GraphEdgeLabels.RESOURCE_INST, properties);

		log.debug("After associating container component {} to resource instance {} with logical name {}. Status is {}", containerCompIdData.getUniqueId(), componentInstanceData.getUniqueId(), logicalName, createRelation);

		return createRelation;
	}

	private TitanOperationStatus associateContainerCompToComponentInstance(TitanVertex containerCompVertex, TitanVertex componentInstanceVertex, String logicalName) {
		Map<String, Object> properties = new HashMap<String, Object>();

		properties.put(GraphPropertiesDictionary.NAME.getProperty(), logicalName);
		TitanOperationStatus createRelation = titanGenericDao.createEdge(containerCompVertex, componentInstanceVertex, GraphEdgeLabels.RESOURCE_INST, properties);

		return createRelation;
	}

	@Override
	public String createComponentInstLogicalName(String instanceNumber, String componentInstanceName) {

		String logicalName = buildComponentInstanceLogicalName(instanceNumber, componentInstanceName);

		return logicalName;
	}

	private String buildComponentInstanceLogicalName(String instanceNumber, String lastToken) {
		return lastToken + " " + (instanceNumber == null ? 0 : instanceNumber);
	}

	private ComponentInstanceData buildComponentInstanceData(ComponentInstance resourceInstance, String componentId, String logicalName) {

		String ciOriginComponentUid = resourceInstance.getComponentUid();

		ComponentInstanceDataDefinition dataDefinition = new ComponentInstanceDataDefinition(resourceInstance);

		Long creationDate = resourceInstance.getCreationTime();
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
		dataDefinition.setUniqueId(UniqueIdBuilder.buildResourceInstanceUniuqeId(componentId, ciOriginComponentUid, dataDefinition.getNormalizedName()));

		ComponentInstanceData resourceInstanceData = new ComponentInstanceData(dataDefinition);

		return resourceInstanceData;
	}

	public Either<ComponentInstance, TitanOperationStatus> removeComponentInstanceFromComponent(NodeTypeEnum containerNodeType, String containerComponentId, String componentInstanceUid) {

		log.debug("Going to delete component instance {} under component {}", componentInstanceUid, containerComponentId);

		Either<ComponentInstanceData, TitanOperationStatus> node = findResourceInstance(componentInstanceUid);

		if (node.isRight()) {
			TitanOperationStatus status = node.right().value();
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError, "Remove Component Instance");
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Remove Component Instance");
			log.debug("Failed to delete component instance {}. status is {}", componentInstanceUid, status);
			return Either.right(status);
		}

		TitanOperationStatus isComponentInstOfComponent = verifyResourceInstanceUnderComponent(containerNodeType, containerComponentId, componentInstanceUid);
		if (isComponentInstOfComponent != TitanOperationStatus.OK) {
			return Either.right(isComponentInstOfComponent);
		}

		TitanOperationStatus status = deleteOutgoingRelationships(containerNodeType, containerComponentId, componentInstanceUid);
		if (status != TitanOperationStatus.OK) {
			return Either.right(status);
		}
		status = deleteIncomingRelationships(containerNodeType, containerComponentId, componentInstanceUid);
		if (status != TitanOperationStatus.OK) {
			return Either.right(status);
		}

		// delete associated properties
		status = deleteAssociatedProperties(componentInstanceUid);
		if (status != TitanOperationStatus.OK) {
			return Either.right(status);
		}
		// delete associated properties
		status = deleteAssociatedAttributes(componentInstanceUid);
		if (status != TitanOperationStatus.OK) {
			return Either.right(status);
		}

		// delete associated artifacts
		status = deleteAssociatedArtifacts(componentInstanceUid);
		if (status != TitanOperationStatus.OK) {
			return Either.right(status);
		}

		// delete associated capability instances
		if (containerNodeType.equals(NodeTypeEnum.Resource)) {
			status = deleteAssociatedCapabilityInstances(componentInstanceUid);
			if (status != TitanOperationStatus.OK) {
				return Either.right(status);
			}
		}

		// delete associated properties
		status = deleteAssociatedGroupInstances(componentInstanceUid);
		if (status != TitanOperationStatus.OK) {
			return Either.right(status);
		}

		Either<ComponentInstanceData, TitanOperationStatus> deleteRI = titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), componentInstanceUid, ComponentInstanceData.class);

		if (deleteRI.isRight()) {
			TitanOperationStatus deleteRiStatus = deleteRI.right().value();
			log.error("Failed to delete resource instance {}. status is {}", componentInstanceUid, deleteRiStatus);
			return Either.right(deleteRiStatus);
		}

		ComponentInstanceData deletedResourceInst = deleteRI.left().value();

		ComponentInstance resourceInstance = new ComponentInstance(deletedResourceInst.getComponentInstDataDefinition());

		return Either.left(resourceInstance);
	}

	private TitanOperationStatus deleteAssociatedGroupInstances(String componentInstanceUid) {

		return this.groupInstanceOperation.deleteAllGroupInstances(componentInstanceUid);
	}

	private TitanOperationStatus deleteAssociatedCapabilityInstances(String resourceInstanceId) {
		TitanOperationStatus status = TitanOperationStatus.OK;

		log.debug("Before deleting all capability instances of component istance {}.", resourceInstanceId);
		Either<List<ImmutablePair<CapabilityInstData, GraphEdge>>, TitanOperationStatus> getCapabilityInstancesRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId,
				GraphEdgeLabels.CAPABILITY_INST, NodeTypeEnum.CapabilityInst, CapabilityInstData.class);

		if (getCapabilityInstancesRes.isRight() && !getCapabilityInstancesRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
			status = getCapabilityInstancesRes.right().value();
			log.debug("Failed to retrieve capability Instances of resource instance {}. Status is {}", resourceInstanceId, status);
		}
		if (getCapabilityInstancesRes.isLeft()) {
			for (ImmutablePair<CapabilityInstData, GraphEdge> capabilityInstancePair : getCapabilityInstancesRes.left().value()) {
				Either<CapabilityInstData, TitanOperationStatus> deleteCababilityInstanceRes = capabilityInstanceOperation.deleteCapabilityInstanceFromResourceInstance(resourceInstanceId, capabilityInstancePair.getLeft().getUniqueId());
				if (deleteCababilityInstanceRes.isRight()) {
					status = deleteCababilityInstanceRes.right().value();
				}
			}
		}
		log.debug("After deleting all capability instances of component istance {}. Status is {}", resourceInstanceId, status);
		return status;
	}

	private TitanOperationStatus deleteAssociatedArtifacts(String resourceInstanceUid) {

		Either<List<ImmutablePair<ArtifactData, GraphEdge>>, TitanOperationStatus> artifactRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceUid, GraphEdgeLabels.ARTIFACT_REF,
				NodeTypeEnum.ArtifactRef, ArtifactData.class);

		if (artifactRes.isRight()) {
			TitanOperationStatus status = artifactRes.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				log.error("Failed to find artifacts of resource instance {}. status is {}", resourceInstanceUid, status);
				return status;
			}
		} else {

			List<ImmutablePair<ArtifactData, GraphEdge>> artifactPairs = artifactRes.left().value();
			for (ImmutablePair<ArtifactData, GraphEdge> pair : artifactPairs) {
				String uniqueId = (String) pair.left.getUniqueId();
				Either<ArtifactData, TitanOperationStatus> removeArifactFromGraph = artifactOperation.removeArtifactOnGraph(resourceInstanceUid, uniqueId, NodeTypeEnum.ResourceInstance, true);
				if (removeArifactFromGraph.isRight()) {
					TitanOperationStatus status = removeArifactFromGraph.right().value();
					log.error("Failed to delete artifact of resource instance {}. status is {}", resourceInstanceUid, status);
					return status;
				}

			}
		}

		return TitanOperationStatus.OK;

	}

	private TitanOperationStatus deleteAssociatedProperties(String resourceInstanceUid) {
		final GraphEdgeLabels edgeConectingToRI = GraphEdgeLabels.PROPERTY_VALUE;
		final NodeTypeEnum elementTypeToDelete = NodeTypeEnum.PropertyValue;
		return deleteAssociatedRIElements(elementTypeToDelete, edgeConectingToRI, resourceInstanceUid, () -> PropertyValueData.class);

	}

	private TitanOperationStatus deleteAssociatedAttributes(String resourceInstanceUid) {
		final GraphEdgeLabels edgeConectingToRI = GraphEdgeLabels.ATTRIBUTE_VALUE;
		final NodeTypeEnum elementTypeToDelete = NodeTypeEnum.AttributeValue;
		return deleteAssociatedRIElements(elementTypeToDelete, edgeConectingToRI, resourceInstanceUid, () -> AttributeValueData.class);
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

	/**
	 * delete all relationship instance nodes which has an outgoing edge to a given resource instance
	 * 
	 * @param resourceInstanceUid
	 * @return
	 */
	private TitanOperationStatus deleteIncomingRelationships(NodeTypeEnum componentType, String componentId, String resourceInstanceUid) {

		Either<List<RequirementCapabilityRelDef>, TitanOperationStatus> relationsForTarget = getRelationsForTarget(resourceInstanceUid);
		if (relationsForTarget.isRight()) {
			TitanOperationStatus status = relationsForTarget.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				log.error("Failed to find the relationships of resource instance {}. status is {}", resourceInstanceUid, status);
				return status;
			}
		} else {
			List<RequirementCapabilityRelDef> relList = relationsForTarget.left().value();
			for (RequirementCapabilityRelDef relation : relList) {
				Either<RequirementCapabilityRelDef, StorageOperationStatus> dissociateResourceInstances = dissociateResourceInstances(componentId, componentType, relation, true);
				if (dissociateResourceInstances.isRight()) {
					log.error("failed to diassociate component instance {} and component instance {} under component {}. error is {}", relation.getFromNode(), relation.getToNode(), componentId);
					return TitanOperationStatus.GENERAL_ERROR;
				}
			}
		}
		return TitanOperationStatus.OK;
	}

	/**
	 * delete all relationship instance nodes which has an incoming edge from a given resource instance
	 * 
	 * @param resourceInstanceUid
	 * @return
	 */
	private TitanOperationStatus deleteOutgoingRelationships(NodeTypeEnum componentType, String componentId, String resourceInstanceUid) {

		Either<List<RequirementCapabilityRelDef>, TitanOperationStatus> relationsForSource = getRelationsForSource(resourceInstanceUid);
		if (relationsForSource.isRight()) {
			TitanOperationStatus status = relationsForSource.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				log.error("Failed to find the relationships of resource instance {}. status is {}", resourceInstanceUid, status);
				return status;
			}
		} else {
			List<RequirementCapabilityRelDef> relList = relationsForSource.left().value();
			for (RequirementCapabilityRelDef relation : relList) {
				Either<RequirementCapabilityRelDef, StorageOperationStatus> dissociateResourceInstances = dissociateResourceInstances(componentId, componentType, relation, true);
				if (dissociateResourceInstances.isRight()) {
					log.error("failed to diassociate component instance {} and component instance {} under component {}. error is {}", relation.getFromNode(), relation.getToNode(), componentId);
					return TitanOperationStatus.GENERAL_ERROR;
				}
			}
		}
		return TitanOperationStatus.OK;
	}

	/**
	 * delete relationship instance nodes
	 * 
	 * @param relationshipNodes
	 * @return
	 */
	private TitanOperationStatus deleteRelationshipNodes(List<ImmutablePair<RelationshipInstData, GraphEdge>> relationshipNodes) {

		if (relationshipNodes != null) {
			for (ImmutablePair<RelationshipInstData, GraphEdge> immutablePair : relationshipNodes) {
				RelationshipInstData relationshipTypeImplData = immutablePair.getKey();
				Either<RelationshipInstData, TitanOperationStatus> deleteNode = titanGenericDao.deleteNode(relationshipTypeImplData, RelationshipInstData.class);
				if (deleteNode.isRight()) {
					TitanOperationStatus status = deleteNode.right().value();
					log.error("Failed to delete relationship node {}. status is {}", relationshipTypeImplData, status);
					return status;
				}
			}
		}

		return TitanOperationStatus.OK;
	}

	public Either<List<RelationshipInstData>, TitanOperationStatus> disconnectResourcesInService(String componentId, NodeTypeEnum nodeType, RequirementCapabilityRelDef requirementDef) {

		if (requirementDef.getRelationships() == null) {
			log.debug("No relation pair in request [ {} ]", requirementDef);
			return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
		}

		String fromResInstanceUid = requirementDef.getFromNode();
		String toResInstanceUid = requirementDef.getToNode();

		// DE191707
		TitanOperationStatus isResourceInstOfService = verifyResourceInstanceUnderComponent(nodeType, componentId, fromResInstanceUid);
		if (isResourceInstOfService != TitanOperationStatus.OK) {
			return Either.right(isResourceInstOfService);
		}
		isResourceInstOfService = verifyResourceInstanceUnderComponent(nodeType, componentId, toResInstanceUid);
		if (isResourceInstOfService != TitanOperationStatus.OK) {
			return Either.right(isResourceInstOfService);
		}

		List<RequirementAndRelationshipPair> relationPairList = requirementDef.getRelationships();

		Either<TitanVertex, TitanOperationStatus> riFrom = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), fromResInstanceUid);
		if (riFrom.isRight()) {
			log.debug("Failed to fetch component instance {}. error {}", fromResInstanceUid, riFrom.right().value());
			return Either.right(riFrom.right().value());
		}
		Iterator<Edge> edgeIter = riFrom.left().value().edges(Direction.OUT, GraphEdgeLabels.RELATIONSHIP_INST.getProperty());
		if (edgeIter == null) {
			log.debug("No edges with label {} for owner RI {}", GraphEdgeLabels.CALCULATED_REQUIREMENT_FULLFILLED.getProperty(), fromResInstanceUid);
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}
		List<RelationshipInstData> deletedRelations = new ArrayList<>();
		Set<String> vertexToDelete = new HashSet<String>();
		while (edgeIter.hasNext()) {
			TitanEdge edge = (TitanEdge) edgeIter.next();
			String name = (String) edge.property(GraphEdgePropertiesDictionary.NAME.getProperty()).value();
			for (RequirementAndRelationshipPair relationPair : relationPairList) {
				if (relationPair.getRequirement().equals(name)) {
					TitanVertex inVertex = edge.inVertex();
					String requirementId = (String) titanGenericDao.getProperty(inVertex, GraphPropertiesDictionary.REQUIREMENT_ID.getProperty());
					String capabiltyId = (String) titanGenericDao.getProperty(inVertex, GraphPropertiesDictionary.CAPABILITY_ID.getProperty());
					String requirementOwnerId = (String) titanGenericDao.getProperty(inVertex, GraphPropertiesDictionary.REQUIREMENT_OWNER_ID.getProperty());
					String capabiltyOwnerId = (String) titanGenericDao.getProperty(inVertex, GraphPropertiesDictionary.CAPABILITY_OWNER_ID.getProperty());
					String relationVertexId = (String) titanGenericDao.getProperty(inVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());

					// verify vs requirement id and owner ids. ( for
					// requirements with same name)
					if (requirementId.equals(relationPair.getRequirementUid()) && capabiltyId.equals(relationPair.getCapabilityUid()) && requirementOwnerId.equals(relationPair.getRequirementOwnerId())
							&& capabiltyOwnerId.equals(relationPair.getCapabilityOwnerId())) {
						vertexToDelete.add(relationVertexId);
					}
				}
			}
		}
		log.debug("relation node with ids: {} are going to be deleted", vertexToDelete);
		for (String relationVertexId : vertexToDelete) {
			// remove relation vertex
			Either<RelationshipInstData, TitanOperationStatus> relationNode = titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.RelationshipInst), relationVertexId, RelationshipInstData.class);
			if (relationNode.isRight()) {
				log.debug("Failed to delete relation node with id {}. error {}", relationVertexId, relationNode.right().value());
				return Either.right(relationNode.right().value());
			}
			RelationshipInstData deletedRelation = relationNode.left().value();
			deletedRelations.add(deletedRelation);
		}
		if (deletedRelations.size() > 0) {
			return Either.left(deletedRelations);
		}
		return Either.right(TitanOperationStatus.NOT_FOUND);
	}

	@Override
	public Either<RequirementCapabilityRelDef, StorageOperationStatus> dissociateResourceInstances(String componentId, NodeTypeEnum nodeType, RequirementCapabilityRelDef requirementDef, boolean inTransaction) {

		String fromResInstanceUid = requirementDef.getFromNode();
		String toResInstanceUid = requirementDef.getToNode();
		String requirement = requirementDef.getRelationships().get(0).getRequirement();
		Either<RequirementCapabilityRelDef, StorageOperationStatus> result = null;
		try {

			Either<List<RelationshipInstData>, TitanOperationStatus> dissociateRes = disconnectResourcesInService(componentId, nodeType, requirementDef);
			if (dissociateRes.isRight()) {
				TitanOperationStatus status = dissociateRes.right().value();
				log.error("Failed to dissociate resource instance {} from resource instance {} in service {}. status is {}", fromResInstanceUid, toResInstanceUid, componentId, status);
				BeEcompErrorManager.getInstance().logBeDaoSystemError("dissociateComponentInstances");
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}
			StorageOperationStatus updateCalculatedCapReqResult = updateCalculatedCapReq(requirementDef, false);
			if (!updateCalculatedCapReqResult.equals(StorageOperationStatus.OK)) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError, "dissociateComponentInstances");
				BeEcompErrorManager.getInstance().logBeDaoSystemError("dissociateComponentInstances");
				log.debug("Failed to dissociate component instances. {}. status is {}", requirementDef, updateCalculatedCapReqResult);
				result = Either.right(updateCalculatedCapReqResult);
				return result;
			}

			StorageOperationStatus status;
			status = updateCustomizationUUID(requirementDef.getFromNode());
			if (status != StorageOperationStatus.OK) {
				result = Either.right(status);
				return result;
			}
			status = updateCustomizationUUID(requirementDef.getToNode());
			if (status != StorageOperationStatus.OK) {
				result = Either.right(status);
				return result;
			}

			List<RelationshipInstData> relationshipInstData = dissociateRes.left().value();
			RequirementCapabilityRelDef capabilityRelDef = buildCapabilityResult(fromResInstanceUid, toResInstanceUid, requirement, relationshipInstData);

			result = Either.left(capabilityRelDef);

			return result;

		} finally {
			if (false == inTransaction) {
				commitOrRollback(result);
			}
		}

	}

	private StorageOperationStatus updateCalculatedCapReq(RequirementCapabilityRelDef capabilityRelDef, boolean associate) {
		GraphEdgeLabels requirmentNewLabel = associate ? GraphEdgeLabels.CALCULATED_REQUIREMENT_FULLFILLED : GraphEdgeLabels.CALCULATED_REQUIREMENT;

		GraphEdgeLabels requirmentCurrentLabel = associate ? GraphEdgeLabels.CALCULATED_REQUIREMENT : GraphEdgeLabels.CALCULATED_REQUIREMENT_FULLFILLED;

		GraphEdgeLabels capabilityNewLabel = associate ? GraphEdgeLabels.CALCULATED_CAPABILITY_FULLFILLED : GraphEdgeLabels.CALCULATED_CAPABILITY;

		GraphEdgeLabels capabilityCurrentLabel = associate ? GraphEdgeLabels.CALCULATED_CAPABILITY : GraphEdgeLabels.CALCULATED_CAPABILITY_FULLFILLED;

		List<RequirementAndRelationshipPair> relationships = capabilityRelDef.getRelationships();
		for (RequirementAndRelationshipPair pair : relationships) {
			StorageOperationStatus status = updateRequirementEdges(requirmentNewLabel, requirmentCurrentLabel, pair, capabilityRelDef.getFromNode());
			if (!status.equals(StorageOperationStatus.OK)) {
				return status;
			}
			status = updateCapabiltyEdges(capabilityNewLabel, capabilityCurrentLabel, pair, capabilityRelDef.getToNode());
			if (!status.equals(StorageOperationStatus.OK)) {
				return status;
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus updateRequirementEdges(GraphEdgeLabels requirmentNewLabel, GraphEdgeLabels requirmentCurrentLabel, RequirementAndRelationshipPair pair, String requirementOwnerId) {
		Either<TitanVertex, TitanOperationStatus> reqOwnerRI = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), requirementOwnerId);
		if (reqOwnerRI.isRight()) {
			log.debug("Failed to fetch requirment Owner by Id {}  error {}", requirementOwnerId, reqOwnerRI.right().value());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(reqOwnerRI.right().value());
		}
		Iterator<Edge> edgeIter = reqOwnerRI.left().value().edges(Direction.OUT, requirmentCurrentLabel.name(), requirmentNewLabel.name());
		if (edgeIter == null) {
			log.debug("No edges with label {} for owner RI {}", requirmentCurrentLabel, requirementOwnerId);
			return StorageOperationStatus.GENERAL_ERROR;
		}
		boolean associate = requirmentNewLabel.equals(GraphEdgeLabels.CALCULATED_REQUIREMENT_FULLFILLED) ? true : false;
		while (edgeIter.hasNext()) {
			TitanEdge edge = (TitanEdge) edgeIter.next();
			String name = (String) edge.property(GraphEdgePropertiesDictionary.NAME.getProperty()).value();
			if (pair.getRequirement().equals(name)) {
				TitanVertex reqVertex = edge.inVertex();
				String requirementId = (String) titanGenericDao.getProperty(reqVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
				// verify vs requirement id . ( for requirements with same name)
				if (requirementId.equals(pair.getRequirementUid())) {
					String ownerIdOnEdge = (String) edge.value(GraphEdgePropertiesDictionary.OWNER_ID.getProperty());
					if (ownerIdOnEdge.equals(pair.getRequirementOwnerId())) {
						String requiredOccurrences = (String) titanGenericDao.getProperty(edge, GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty());
						String leftOccurrences = (String) titanGenericDao.getProperty(edge, GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty());

						String requiredOccurrencesNew = "0";
						String leftOccurrencesNew = RequirementDataDefinition.MAX_DEFAULT_OCCURRENCES;
						if (requiredOccurrences != null) {
							Integer iOccurrences = Integer.parseInt(requiredOccurrences);
							if (associate) {
								if (iOccurrences > 0) {
									iOccurrences--;
									requiredOccurrencesNew = iOccurrences.toString();
								}
							} else {
								String reqMinOccurrences = (String) titanGenericDao.getProperty(reqVertex, GraphPropertiesDictionary.MIN_OCCURRENCES.getProperty());
								if (reqMinOccurrences == null) {
									reqMinOccurrences = RequirementDataDefinition.MIN_OCCURRENCES;
								}
								if (Integer.parseInt(reqMinOccurrences) > iOccurrences) {
									iOccurrences++;
									requiredOccurrencesNew = iOccurrences.toString();
								}
							}
						}
						Map<String, Object> properties = titanGenericDao.getProperties(edge);
						properties.put(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty(), requiredOccurrencesNew);

						if (leftOccurrences != null && !leftOccurrences.equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
							Integer iOccurrences = Integer.parseInt(leftOccurrences);
							if (associate) {
								if (iOccurrences > 0) {
									iOccurrences--;
								}
							} else {
								iOccurrences++;
							}
							leftOccurrencesNew = iOccurrences.toString();
							properties.put(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty(), leftOccurrencesNew);
							if ((associate && iOccurrences == 0) || (!associate && iOccurrences == 1)) {
								// move edge to full filled state
								TitanVertex outVertex = edge.outVertex();
								TitanEdge newEdge = outVertex.addEdge(requirmentNewLabel.getProperty(), reqVertex);
								titanGenericDao.setProperties(newEdge, properties);
								edge.remove();
							} else {
								titanGenericDao.setProperties(edge, properties);
							}
						} else {
							leftOccurrencesNew = leftOccurrences;
							properties.put(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty(), leftOccurrencesNew);
							titanGenericDao.setProperties(edge, properties);
						}
						break;
					}
				}
			}
		}
		return StorageOperationStatus.OK;

	}

	private StorageOperationStatus updateCapabiltyEdges(GraphEdgeLabels capabiltyNewLabel, GraphEdgeLabels capabiltyCurrentLabel, RequirementAndRelationshipPair pair, String capabiltyOwnerId) {
		Either<TitanVertex, TitanOperationStatus> capOwnerRI = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), capabiltyOwnerId);
		if (capOwnerRI.isRight()) {
			log.debug("Failed to fetch requirment Owner by Id {}. error {}", capabiltyOwnerId, capOwnerRI.right().value());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(capOwnerRI.right().value());
		}
		Iterator<Edge> edgeIter = capOwnerRI.left().value().edges(Direction.OUT, capabiltyCurrentLabel.name(), capabiltyNewLabel.name());
		if (edgeIter == null) {
			log.debug("No edges with label {} for owner RI {}", capabiltyCurrentLabel, capabiltyOwnerId);
			return StorageOperationStatus.GENERAL_ERROR;
		}
		boolean associate = capabiltyNewLabel.equals(GraphEdgeLabels.CALCULATED_CAPABILITY_FULLFILLED) ? true : false;

		while (edgeIter.hasNext()) {
			TitanEdge edge = (TitanEdge) edgeIter.next();
			TitanVertex capVertex = edge.inVertex();
			// edge.property(GraphEdgePropertiesDictionary.NAME.getProperty()).value();

			String capabiltyId = (String) titanGenericDao.getProperty(capVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
			// verify vs capability id . ( for capabilty with same name)
			if (capabiltyId.equals(pair.getCapabilityUid())) {
				String ownerIdOnEdge = (String) titanGenericDao.getProperty(edge, GraphEdgePropertiesDictionary.OWNER_ID.getProperty());
				if (ownerIdOnEdge.equals(pair.getCapabilityOwnerId())) {

					String requiredOccurrences = (String) titanGenericDao.getProperty(edge, GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty());
					String leftOccurrences = (String) titanGenericDao.getProperty(edge, GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty());

					String requiredOccurrencesNew = "0";
					String leftOccurrencesNew = CapabilityDataDefinition.MAX_OCCURRENCES;
					if (requiredOccurrences != null) {
						Integer iOccurrences = Integer.parseInt(requiredOccurrences);
						if (associate) {
							if (iOccurrences > 0) {
								iOccurrences--;
								requiredOccurrencesNew = iOccurrences.toString();
							}
						} else {
							String reqMinOccurrences = (String) titanGenericDao.getProperty(capVertex, GraphPropertiesDictionary.MIN_OCCURRENCES.getProperty());
							if (reqMinOccurrences == null) {
								reqMinOccurrences = CapabilityDataDefinition.MIN_OCCURRENCES;
							}
							if (Integer.parseInt(reqMinOccurrences) > iOccurrences) {
								iOccurrences++;
								requiredOccurrencesNew = iOccurrences.toString();
							}
						}
					}
					Map<String, Object> properties = titanGenericDao.getProperties(edge);
					properties.put(GraphEdgePropertiesDictionary.REQUIRED_OCCURRENCES.getProperty(), requiredOccurrencesNew);

					if (leftOccurrences != null && !leftOccurrences.equals(CapabilityDataDefinition.MAX_OCCURRENCES)) {
						Integer iOccurrences = Integer.parseInt(leftOccurrences);
						if (associate) {
							if (iOccurrences > 0) {
								iOccurrences--;
							}
						} else {
							iOccurrences++;
						}
						leftOccurrencesNew = iOccurrences.toString();
						properties.put(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty(), leftOccurrencesNew);
						if ((associate && iOccurrences == 0) || (!associate && iOccurrences == 1)) {
							// move edge to full filled state
							TitanVertex outVertex = edge.outVertex();
							TitanEdge newEdge = outVertex.addEdge(capabiltyNewLabel.getProperty(), capVertex);
							titanGenericDao.setProperties(newEdge, properties);
							edge.remove();
						} else {
							titanGenericDao.setProperties(edge, properties);
						}
					} else {
						properties.put(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty(), leftOccurrencesNew);
						titanGenericDao.setProperties(edge, properties);
					}
					break;
				}
			}
		}
		return StorageOperationStatus.OK;
	}

	@Override
	public Either<RequirementCapabilityRelDef, StorageOperationStatus> dissociateResourceInstances(String serviceId, NodeTypeEnum nodeType, RequirementCapabilityRelDef requirementDef) {

		return dissociateResourceInstances(serviceId, nodeType, requirementDef, false);
	}

	private RequirementCapabilityRelDef buildCapabilityResult(String fromResInstanceUid, String toResInstanceUid, String requirement, List<RelationshipInstData> relationshipInstDataList) {

		RequirementCapabilityRelDef capabilityRelDef = new RequirementCapabilityRelDef();
		capabilityRelDef.setFromNode(fromResInstanceUid);
		capabilityRelDef.setToNode(toResInstanceUid);
		List<RequirementAndRelationshipPair> relationships = new ArrayList<RequirementAndRelationshipPair>();
		for (RelationshipInstData relationshipInstData : relationshipInstDataList) {
			RelationshipImpl relationshipImpl = new RelationshipImpl();
			relationshipImpl.setType(relationshipInstData.getType());
			RequirementAndRelationshipPair reqRel = new RequirementAndRelationshipPair(requirement, relationshipImpl);
			capabilityRelDef.setRelationships(relationships);
			reqRel.setCapabilityOwnerId(relationshipInstData.getCapabilityOwnerId());
			reqRel.setCapabilityUid(relationshipInstData.getCapabiltyId());
			reqRel.setRequirementOwnerId(relationshipInstData.getRequirementOwnerId());
			reqRel.setRequirementUid(relationshipInstData.getRequirementId());
			relationships.add(reqRel);
		}
		return capabilityRelDef;

	}

	public Either<RelationshipInstData, TitanOperationStatus> connectResourcesInService(String componentId, NodeTypeEnum nodeType, String fromResInstanceUid, String toResInstanceUid, RequirementAndRelationshipPair relationPair) {
		String relationship = null;
		String requirement = relationPair.getRequirement();
		if (relationPair.getRelationship() != null) {
			relationship = relationPair.getRelationship().getType();
		}

		log.debug("Going to associate resource instance {} to resource instance {} under component {}. Requirement is {}.", fromResInstanceUid, toResInstanceUid, componentId, requirement);

		Either<ComponentInstanceData, TitanOperationStatus> fromResourceInstDataRes = findMandatoryResourceInstData(fromResInstanceUid);
		if (fromResourceInstDataRes.isRight()) {
			TitanOperationStatus status = fromResourceInstDataRes.right().value();
			log.error("Failed to find resource instance {}. status is {}", fromResInstanceUid, status);
			return Either.right(status);
		}
		ComponentInstanceData fromCI = fromResourceInstDataRes.left().value();
		ComponentInstanceData fromResourceInstanceData = fromCI;
		Either<ComponentInstanceData, TitanOperationStatus> toResourceInstDataRes = findMandatoryResourceInstData(toResInstanceUid);
		if (toResourceInstDataRes.isRight()) {
			TitanOperationStatus status = toResourceInstDataRes.right().value();
			log.error("Failed to find resource instance {}. status is {}", toResInstanceUid, status);
			return Either.right(status);
		}
		ComponentInstanceData toCI = toResourceInstDataRes.left().value();
		ComponentInstanceData toResourceInstanceData = toCI;
		// THE component NodeTypeEnum should be sent
		TitanOperationStatus isResourceInstOfService = verifyResourceInstanceUnderComponent(nodeType, componentId, fromResInstanceUid);
		if (isResourceInstOfService != TitanOperationStatus.OK) {
			return Either.right(isResourceInstOfService);
		}
		isResourceInstOfService = verifyResourceInstanceUnderComponent(nodeType, componentId, toResInstanceUid);
		if (isResourceInstOfService != TitanOperationStatus.OK) {
			return Either.right(isResourceInstOfService);
		}

		Either<ImmutablePair<RelationshipTypeData, String>, TitanOperationStatus> isValidRes = validateRequirementVsCapability(fromResourceInstanceData, toResourceInstanceData, requirement, relationship, relationPair);
		if (isValidRes.isRight()) {
			TitanOperationStatus status = isValidRes.right().value();
			log.error("Failed to validate requirement {} between resource instance {} to resource instance {}. status is {}", requirement, fromResInstanceUid, toResInstanceUid, status);
			return Either.right(status);
		}

		RelationshipTypeData relationshipTypeData = isValidRes.left().value().getKey();
		String capabilityName = isValidRes.left().value().getValue();
		RelationshipInstData relationshipInstData = buildRelationshipInstData(fromResInstanceUid, requirement, relationshipTypeData, relationPair);
		Either<RelationshipInstData, TitanOperationStatus> createNode = createRelationshipInstData(fromCI, relationshipInstData, relationshipTypeData, requirement);

		if (createNode.isRight()) {
			return Either.right(createNode.right().value());
		}
		RelationshipInstData createdRelInstData = createNode.left().value();
		Either<GraphRelation, TitanOperationStatus> associateResInst = associateRelationshipInstToTarget(toCI, requirement, capabilityName, createdRelInstData);

		if (associateResInst.isRight()) {
			TitanOperationStatus status = associateResInst.right().value();
			log.error("Failed to associate relationship instance {} to target node {}. status is {}", createdRelInstData.getUniqueId(), toResInstanceUid, status);
			return Either.right(status);
		}

		return Either.left(createNode.left().value());
	}

	private TitanOperationStatus verifyResourceInstanceUnderComponent(NodeTypeEnum containerNodeType, String containerComponentId, String resInstanceUid) {

		Either<ImmutablePair<ComponentMetadataData, GraphEdge>, TitanOperationStatus> parentNode = titanGenericDao.getParentNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resInstanceUid, GraphEdgeLabels.RESOURCE_INST,
				containerNodeType, ComponentMetadataData.class);

		if (parentNode.isRight()) {
			TitanOperationStatus status = parentNode.right().value();
			log.error("Failed to find the service associated to the resource instance {}. status is {}", resInstanceUid, status);
			return status;
		}

		ImmutablePair<ComponentMetadataData, GraphEdge> componentsRes = parentNode.left().value();
		ComponentMetadataData componentMetadataData = componentsRes.getKey();
		String uniqueId = (String) componentMetadataData.getUniqueId();

		if (containerComponentId.equals(uniqueId)) {
			return TitanOperationStatus.OK;
		} else {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeIncorrectServiceError, "Resource Instance - verifyResourceInstanceUnderComponent", containerComponentId);
			BeEcompErrorManager.getInstance().logBeIncorrectComponentError("Resource Instance - verifyResourceInstanceUnderComponent", containerNodeType.getName(), containerComponentId);
			log.debug("The provided component id {} is not equal to the component ({}) which associated to resource instance {}", containerComponentId, uniqueId, resInstanceUid);
			return TitanOperationStatus.INVALID_ID;
		}

	}

	/**
	 * find the resource instance node in graph.
	 * 
	 * @param resInstanceUid
	 * @return
	 */
	private Either<ComponentInstanceData, TitanOperationStatus> findMandatoryResourceInstData(String resInstanceUid) {
		Either<ComponentInstanceData, TitanOperationStatus> resStatus = findResourceInstance(resInstanceUid);
		if (resStatus.isRight()) {
			TitanOperationStatus status = resStatus.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				return Either.right(TitanOperationStatus.INVALID_ID);
			}
			return Either.right(status);
		}
		ComponentInstanceData riData = resStatus.left().value();
		return Either.left(riData);
	}

	/**
	 * associate relationship instance node to the target resource instance node.
	 * 
	 * @param toResInstance
	 * @param requirement
	 * @param relInstData
	 * @return
	 */
	private Either<GraphRelation, TitanOperationStatus> associateRelationshipInstToTarget(ComponentInstanceData toResInstance, String requirement, String capabilityName, RelationshipInstData relInstData) {

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), capabilityName);
		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(relInstData, toResInstance, GraphEdgeLabels.CAPABILITY_NODE, props);
		log.debug("After creatingrelation between relationship instance {} to target node {}", relInstData.getUniqueId(), toResInstance.getUniqueId());

		return createRelation;

	}

	/**
	 * create reslationship instance node and associate the reosurce instance node to it.
	 * 
	 * @param resInstance
	 * @param relationshipInstData
	 * @param relationshipTypeData
	 * @param requirementName
	 * @return
	 */
	private Either<RelationshipInstData, TitanOperationStatus> createRelationshipInstData(ComponentInstanceData resInstance, RelationshipInstData relationshipInstData, RelationshipTypeData relationshipTypeData, String requirementName) {

		Either<RelationshipInstData, TitanOperationStatus> createNode = titanGenericDao.createNode(relationshipInstData, RelationshipInstData.class);
		if (createNode.isRight()) {
			TitanOperationStatus status = createNode.right().value();
			log.error("Failed to create relationship instance node in graph. status is {}", status);
			return Either.right(status);
		}

		RelationshipInstData createdRelationshipInst = createNode.left().value();

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("name", requirementName);
		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(resInstance, createdRelationshipInst, GraphEdgeLabels.RELATIONSHIP_INST, properties);
		if (createRelation.isRight()) {
			TitanOperationStatus status = createRelation.right().value();
			log.error("Failed to associate resource instance {} to relationship instance {}. status is {}", resInstance.getUniqueIdKey(), createdRelationshipInst.getUniqueId(), status);
			return Either.right(status);
		}

		return Either.left(createdRelationshipInst);
	}

	/**
	 * check whether we can associate resource instances for a given requirement.
	 * 
	 * 1. check the source resource instance contains the requirement
	 * 
	 * 2. check the target resource instance contains a capability with the same name as the requirement
	 * 
	 * @param fromResInstance
	 * @param toResInstance
	 * @param requirement
	 * @param relationship
	 * @param relationPair
	 * @return
	 */
	private Either<ImmutablePair<RelationshipTypeData, String>, TitanOperationStatus> validateRequirementVsCapability(ComponentInstanceData fromResInstance, ComponentInstanceData toResInstance, String requirement, String relationship,
			RequirementAndRelationshipPair relationPair) {

		String fromResourceUid = fromResInstance.getComponentInstDataDefinition().getComponentUid();

		String toResourceUid = toResInstance.getComponentInstDataDefinition().getComponentUid();
		Either<CapabilityDefinition, StorageOperationStatus> capabilityDefinitionE = capabilityOperation.getCapability(relationPair.getCapabilityUid(), true);
		if (capabilityDefinitionE.isRight()) {
			log.error("The capability cannot be found {}", relationPair.getCapabilityUid());
			return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
		}
		Either<RequirementDefinition, TitanOperationStatus> requirementDefinitionE = requirementOperation.getRequirement(relationPair.getRequirementUid());
		if (requirementDefinitionE.isRight()) {
			log.error("The requirement   cannot be found {}", relationPair.getRequirementUid());
			return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
		}
		RequirementDefinition requirementDefinition = requirementDefinitionE.left().value();
		String fetchedRequirementRelationship = requirementDefinition.getRelationship();

		String fetchedRequirementCapability = requirementDefinition.getCapability();
		// String fetchedRequirementNodeName = requirementDefinition.getNode();

		TitanOperationStatus status = validateAvailableRequirement(fromResInstance, relationPair);
		if (!status.equals(TitanOperationStatus.OK)) {
			log.error("The requirement isn't available, status {}", status);
			return Either.right(status);
		}
		status = validateAvailableCapabilty(toResInstance, relationPair);
		if (!status.equals(TitanOperationStatus.OK)) {
			log.error("The capabilty isn't available, status {}", status);
			return Either.right(status);
		}
		Either<ComponentInstanceData, TitanOperationStatus> originCapabilty = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), relationPair.getCapabilityOwnerId(), ComponentInstanceData.class);
		if (originCapabilty.isRight()) {
			log.error("Failed to fetch the origin resource for capabilty resource instance with id {}, error {}", relationPair.getCapabilityOwnerId(), originCapabilty.right().value());
			return Either.right(originCapabilty.right().value());
		}
		// String originCapabId =
		// originCapabilty.left().value().getComponentInstDataDefinition().getComponentUid();

		// List<String> capabilitySources = new ArrayList<>();
		// TitanOperationStatus capabiltySourcesResult =
		// resourceOperation.fillResourceDerivedListFromGraph(originCapabId,
		// capabilitySources);
		// if (!TitanOperationStatus.OK.equals(capabiltySourcesResult)) {
		// log.error("Failed to fill capabilty cources for resource with id " +
		// originCapabId + " , error " + capabiltySourcesResult);
		// return Either.right(originCapabilty.right().value());
		// }
		CapabilityDefinition capabilityDefinition = capabilityDefinitionE.left().value();
		String capabilityName = requirement;

		log.debug("The capability {} of resource {} appropriates to requirement {} on resource {}", capabilityDefinition, toResourceUid, requirement, fromResourceUid);
		String capabilityType = capabilityDefinition.getType();

		if (false == fetchedRequirementCapability.equals(capabilityType)) {
			log.error("The capability type in the requirement ({}) does not equal to the capability on the resource {} ({})", fetchedRequirementCapability, toResourceUid, capabilityType);
			return Either.right(TitanOperationStatus.MATCH_NOT_FOUND);
		}

		// if (fetchedRequirementNodeName != null &&
		// !capabilitySources.contains(fetchedRequirementNodeName)) {
		// log.error("The target resource instance " + toResourceUid + " is not
		// of type " + fetchedRequirementNodeName);
		// return Either.right(TitanOperationStatus.MATCH_NOT_FOUND);
		// }

		RelationshipTypeData relationshipTypeData = new RelationshipTypeData();
		relationshipTypeData.getRelationshipTypeDataDefinition().setType(fetchedRequirementRelationship);

		ImmutablePair<RelationshipTypeData, String> result = new ImmutablePair<RelationshipTypeData, String>(relationshipTypeData, capabilityName);
		return Either.left(result);
	}

	private TitanOperationStatus validateAvailableRequirement(ComponentInstanceData fromResInstance, RequirementAndRelationshipPair relationPair) {
		Either<TitanVertex, TitanOperationStatus> fromRi = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), fromResInstance.getUniqueId());
		if (fromRi.isRight()) {
			log.debug("Failed to fetch component instance {}  error {}", fromResInstance.getUniqueId(), fromRi.right().value());
			return fromRi.right().value();
		}
		Iterator<Edge> edgeIter = fromRi.left().value().edges(Direction.OUT, GraphEdgeLabels.CALCULATED_REQUIREMENT.name());
		if (edgeIter == null || !edgeIter.hasNext()) {
			log.debug("No available CALCULATED_REQUIREMENT edges. All full filled for RI {}", fromResInstance.getUniqueId());
			return TitanOperationStatus.MATCH_NOT_FOUND;
		}
		boolean exist = false;
		while (edgeIter.hasNext()) {
			Edge edge = edgeIter.next();
			TitanVertex reqVertex = (TitanVertex) edge.inVertex();
			String reqId = (String) titanGenericDao.getProperty(reqVertex, UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Requirement));
			if (reqId.equals(relationPair.getRequirementUid())) {
				String ownerIdOnEdge = (String) edge.value(GraphEdgePropertiesDictionary.OWNER_ID.getProperty());
				if (ownerIdOnEdge.equals(relationPair.getRequirementOwnerId())) {
					String leftOccurrences = (String) titanGenericDao.getProperty(edge, GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty());
					if (leftOccurrences != null && !leftOccurrences.equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
						Integer leftIntValue = Integer.parseInt(leftOccurrences);
						if (leftIntValue > 0) {
							exist = true;
						}
					} else {
						exist = true;
					}
					break;
				}
			}
		}
		return exist ? TitanOperationStatus.OK : TitanOperationStatus.MATCH_NOT_FOUND;
	}

	private TitanOperationStatus validateAvailableCapabilty(ComponentInstanceData toResInstance, RequirementAndRelationshipPair relationPair) {
		Either<TitanVertex, TitanOperationStatus> fromRi = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), toResInstance.getUniqueId());
		if (fromRi.isRight()) {
			log.debug("Failed to fetch component instance {}. error {}", toResInstance.getUniqueId(), fromRi.right().value());
			return fromRi.right().value();
		}
		Iterator<Edge> edgeIter = fromRi.left().value().edges(Direction.OUT, GraphEdgeLabels.CALCULATED_CAPABILITY.name());
		if (edgeIter == null || !edgeIter.hasNext()) {
			log.debug("No available CALCULATED_CAPABILITY edges. All full filled for RI {}", toResInstance.getUniqueId());
			return TitanOperationStatus.MATCH_NOT_FOUND;
		}
		boolean exist = false;
		while (edgeIter.hasNext()) {
			Edge edge = edgeIter.next();
			TitanVertex reqVertex = (TitanVertex) edge.inVertex();
			String capId = (String) titanGenericDao.getProperty(reqVertex, UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Capability));
			if (capId.equals(relationPair.getCapabilityUid())) {
				String ownerIdOnEdge = (String) edge.value(GraphEdgePropertiesDictionary.OWNER_ID.getProperty());
				if (ownerIdOnEdge.equals(relationPair.getCapabilityOwnerId())) {
					String leftOccurrences = (String) titanGenericDao.getProperty(edge, GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty());
					if (leftOccurrences != null && !leftOccurrences.equals(CapabilityDataDefinition.MAX_OCCURRENCES)) {
						Integer leftIntValue = Integer.parseInt(leftOccurrences);
						if (leftIntValue > 0) {
							exist = true;
						}
					} else {
						exist = true;
					}
					break;
				}
			}
		}
		return exist ? TitanOperationStatus.OK : TitanOperationStatus.NOT_FOUND;
	}

	private List<ImmutablePair<String, CapabilityDefinition>> findCapabilityOfType(Map<String, CapabilityDefinition> capabilities, String fetchedRequirementCapability) {

		List<ImmutablePair<String, CapabilityDefinition>> result = new ArrayList<ImmutablePair<String, CapabilityDefinition>>();

		if (capabilities == null) {
			return null;
		}

		for (Entry<String, CapabilityDefinition> entry : capabilities.entrySet()) {
			CapabilityDefinition capabilityDefinition = entry.getValue();
			String type = capabilityDefinition.getType();
			if (fetchedRequirementCapability.equals(type)) {
				ImmutablePair<String, CapabilityDefinition> pair = new ImmutablePair<String, CapabilityDefinition>(entry.getKey(), capabilityDefinition);
				result.add(pair);
			}
		}

		return result;
	}

	protected TitanOperationStatus validateTheTargetResourceInstance(String fetchedRequirementNodeName, String resourceUid) {

		if (fetchedRequirementNodeName == null) {
			return TitanOperationStatus.OK;
		}

		List<ResourceMetadataData> resourcesPathList = new ArrayList<ResourceMetadataData>();
		TitanOperationStatus status = resourceOperation.findResourcesPathRecursively(resourceUid, resourcesPathList);
		if (status != TitanOperationStatus.OK) {
			log.error("Failed to find the parent list of resource {}. status is {}", resourceUid, status);
			return status;
		}

		boolean found = false;
		if (resourcesPathList != null) {
			for (ResourceMetadataData resourceData : resourcesPathList) {
				String resourceName = resourceData.getMetadataDataDefinition().getName();
				if (fetchedRequirementNodeName.equals(resourceName)) {
					found = true;
					log.debug("The resource {} is of type {}", resourceData.getUniqueId(), fetchedRequirementNodeName);
					break;
				}
			}
		}

		if (true == found) {
			return TitanOperationStatus.OK;
		} else {
			return TitanOperationStatus.MATCH_NOT_FOUND;
		}

	}

	private RelationshipInstData buildRelationshipInstData(String fromResInstanceUid, String requirement, RelationshipTypeData relationshipTypeData, RequirementAndRelationshipPair relationPair) {

		RelationshipInstData relationshipInstData = new RelationshipInstData();
		relationshipInstData.setUniqueId(UniqueIdBuilder.buildRelationsipInstInstanceUid(fromResInstanceUid, requirement));
		String type = null;
		if (relationshipTypeData != null) {
			type = relationshipTypeData.getRelationshipTypeDataDefinition().getType();
		}

		relationshipInstData.setType(type);
		Long creationDate = System.currentTimeMillis();
		relationshipInstData.setCreationTime(creationDate);
		relationshipInstData.setModificationTime(creationDate);
		relationshipInstData.setCapabilityOwnerId(relationPair.getCapabilityOwnerId());
		relationshipInstData.setRequirementOwnerId(relationPair.getRequirementOwnerId());
		relationshipInstData.setCapabiltyId(relationPair.getCapabilityUid());
		relationshipInstData.setRequirementId(relationPair.getRequirementUid());

		return relationshipInstData;
	}

	private Either<ComponentInstanceData, TitanOperationStatus> findResourceInstance(String resInstanceUid) {

		Either<ComponentInstanceData, TitanOperationStatus> node = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resInstanceUid, ComponentInstanceData.class);

		return node;

	}

	@Override
	public Either<ComponentInstance, StorageOperationStatus> updateResourceInstance(String serviceId, NodeTypeEnum nodeType, String resourceInstanceUid, ComponentInstance resourceInstance, boolean inTransaction) {

		Either<ComponentInstance, StorageOperationStatus> result = null;
		try {

			Either<ComponentInstance, TitanOperationStatus> updateRes = updateResourceInstanceInService(serviceId, resourceInstanceUid, resourceInstance);

			if (updateRes.isRight()) {
				TitanOperationStatus status = updateRes.right().value();
				log.error("Failed to update resource instance {}. status is {}", resourceInstanceUid, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			ComponentInstance value = updateRes.left().value();

			result = Either.left(value);

			return result;

		} finally {
			if (false == inTransaction) {
				commitOrRollback(result);
			}
		}

	}

	/**
	 * prepare new resource instance object for update
	 * 
	 * @param resourceInstance
	 * @param currentInst
	 * @return
	 */
	private ComponentInstance normalizeResourceInstanceForUpdate(ComponentInstance resourceInstance, ComponentInstanceData currentInst) {

		ComponentInstance instance = new ComponentInstance();
		instance.setUniqueId((String) currentInst.getUniqueId());
		Long modificationTime = resourceInstance.getModificationTime();
		if (modificationTime == null) {
			modificationTime = System.currentTimeMillis();
		}
		instance.setModificationTime(modificationTime);
		instance.setPosX(resourceInstance.getPosX());
		instance.setPosY(resourceInstance.getPosY());
		instance.setDescription(resourceInstance.getDescription());
		instance.setName(resourceInstance.getName());
		instance.setNormalizedName(resourceInstance.getNormalizedName());
		instance.setPropertyValueCounter(resourceInstance.getPropertyValueCounter());
		instance.setAttributeValueCounter(resourceInstance.getAttributeValueCounter());
		instance.setInputValueCounter(resourceInstance.getInputValueCounter());

		boolean isNeedGenerate = isNeedGenerateCustomizationUUID(resourceInstance, currentInst);
		if (isNeedGenerate) {
			generateCustomizationUUID(instance);
		} else {
			instance.setCustomizationUUID(resourceInstance.getCustomizationUUID());
		}
		return instance;
	}

	private boolean isNeedGenerateCustomizationUUID(ComponentInstance resourceInstance, ComponentInstanceData currentInst) {
		return !currentInst.getComponentInstDataDefinition().getName().equals(resourceInstance.getName());
	}

	private void printDiff(ComponentInstanceData currentInst, ComponentInstance resourceInstance) {

		log.debug("The current Resource Instance details are : {}", currentInst);
		log.debug("The received Resource Instance details for update are :{}", resourceInstance);

	}

	@Override
	public Either<ComponentInstance, StorageOperationStatus> updateResourceInstance(String serviceId, NodeTypeEnum nodeType, String resourceInstanceName, ComponentInstance resourceInstance) {

		return updateResourceInstance(serviceId, nodeType, resourceInstanceName, resourceInstance, false);
	}

	public Either<ComponentInstance, TitanOperationStatus> updateResourceInstanceInService(String serviceId, String resourceInstanceUid, ComponentInstance resourceInstance) {

		log.trace("Going to update resource instance {}. Properies are {}", resourceInstanceUid, resourceInstance);
		Either<ComponentInstanceData, TitanOperationStatus> findInstRes = findResourceInstance(resourceInstanceUid);
		if (findInstRes.isRight()) {
			TitanOperationStatus status = findInstRes.right().value();
			log.error("Failed to find resource instance {}. status is {}", resourceInstanceUid, status);
			return Either.right(status);
		}

		ComponentInstanceData currentInst = findInstRes.left().value();
		if (log.isDebugEnabled()) {
			printDiff(currentInst, resourceInstance);
		}

		ComponentInstance resourceInstanceForUpdate = normalizeResourceInstanceForUpdate(resourceInstance, currentInst);

		ComponentInstanceData resourceInstanceData = new ComponentInstanceData(resourceInstanceForUpdate);

		Either<ComponentInstanceData, TitanOperationStatus> updateNodeRes = titanGenericDao.updateNode(resourceInstanceData, ComponentInstanceData.class);
		if (updateNodeRes.isRight()) {
			TitanOperationStatus status = updateNodeRes.right().value();
			log.error("Failed to update resource instance {}. status is {}", resourceInstanceUid, status);
			return Either.right(status);
		}

		ComponentInstanceData value = updateNodeRes.left().value();

		ComponentInstance instance = new ComponentInstance(value.getComponentInstDataDefinition());

		return Either.left(instance);

	}

	@Override
	public Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, StorageOperationStatus> getAllComponentInstances(String componentId, NodeTypeEnum containerNodeType, NodeTypeEnum compInstNodeType, boolean inTransaction) {

		Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, StorageOperationStatus> result = null;

		try {

			Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, TitanOperationStatus> resInstancesOfService = getComponentInstancesOfComponent(componentId, containerNodeType, compInstNodeType);

			log.trace("After fetching resource instances of component {}. result is {}", componentId, resInstancesOfService);
			if (resInstancesOfService.isRight()) {
				TitanOperationStatus status = resInstancesOfService.right().value();
				if (status != TitanOperationStatus.NOT_FOUND) {
					log.error("Failed to find resource instances of service {}. status is {}", componentId, status);
				}
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>> immutablePair = resInstancesOfService.left().value();
			List<ComponentInstance> nodes = immutablePair.getKey();
			if (nodes == null || nodes.isEmpty()) {
				return Either.right(StorageOperationStatus.NOT_FOUND);
			}

			result = Either.left(immutablePair);
			return result;
		} finally {
			if (false == inTransaction) {
				commitOrRollback(result);
			}
		}
	}

	@Override
	public Either<Boolean, StorageOperationStatus> isComponentInstanceNameExist(String parentComponentId, NodeTypeEnum nodeType, String compInstId, String componentInstName) {

		Either<Boolean, StorageOperationStatus> result = null;
		Either<Boolean, TitanOperationStatus> updateRes = isComponentInstanceNameExistOnGraph(parentComponentId, nodeType, compInstId, componentInstName);

		if (updateRes.isRight()) {
			TitanOperationStatus status = updateRes.right().value();
			log.error("Failed to find component instance name {}. status is {}", componentInstName, status);
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			return result;
		}

		Boolean value = updateRes.left().value();

		result = Either.left(value);

		return result;

	}

	private Either<Boolean, TitanOperationStatus> isComponentInstanceNameExistOnGraph(String parentComponentId, NodeTypeEnum parentNodeType, String compInstId, String componentInstName) {

		Either<TitanGraph, TitanOperationStatus> graphRes = titanGenericDao.getGraph();
		if (graphRes.isRight()) {
			log.debug("Failed to retrieve graph. status is {}", graphRes);
			return Either.right(graphRes.right().value());
		}

		TitanGraph titanGraph = graphRes.left().value();
		Iterable<TitanVertex> vertices = titanGraph.query().has(UniqueIdBuilder.getKeyByNodeType(parentNodeType), parentComponentId).vertices();
		if (vertices == null || false == vertices.iterator().hasNext()) {
			return Either.right(TitanOperationStatus.INVALID_ID);
		}

		TitanVertex serviceVertex = vertices.iterator().next();
		TitanVertexQuery query = serviceVertex.query();
		query = query.labels(GraphEdgeLabels.RESOURCE_INST.getProperty());
		Iterable<Vertex> verts = query.vertices();
		if (verts == null) {
			log.debug("No edges in graph for criteria");
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}
		Iterator<Vertex> vIter = verts.iterator();
		while (vIter.hasNext()) {
			Vertex vert = vIter.next();
			String resInstName = vert.value(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty());
			if (resInstName.equals(componentInstName)) {
				if (compInstId != null) {// will be null if we got here from
											// create
					// Update case - skipping if this is the same component
					// instance we are updating, that is allowing
					// update of the unchanged name on a component instance.
					// This is needed to support position only update, since
					// name should
					// always be passed in update, and in position case, the
					// name will be unchanged.
					String uniqueId = vert.value(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
					if (uniqueId.equals(compInstId)) {
						continue;
					}
				}
				return Either.left(Boolean.TRUE);
			}
		}
		return Either.left(Boolean.FALSE);
	}

	/**
	 * find resource instances and the relationships between the relationships of a given resource
	 * 
	 * @param componentId
	 * @return
	 */
	public Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, TitanOperationStatus> getComponentInstancesOfComponent(String componentId, NodeTypeEnum containerNodeType, NodeTypeEnum compInstNodeType) {

		if (log.isDebugEnabled())
			log.debug("Going to fetch all resource instances under component {}", componentId);

		Either<ComponentMetadataData, TitanOperationStatus> componentRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(containerNodeType), componentId, ComponentMetadataData.class);
		if (componentRes.isRight()) {
			TitanOperationStatus status = componentRes.right().value();
			log.error("Failed to find component {}. status is {}", componentId, status);
			return Either.right(status);
		}

		Either<List<ImmutablePair<ComponentInstanceData, GraphEdge>>, TitanOperationStatus> resourceInstancesRes = getAllComponentInstanceFromGraph(componentId, containerNodeType, true);
		if (resourceInstancesRes.isRight()) {
			TitanOperationStatus status = resourceInstancesRes.right().value();
			log.debug("Resource instance was found under component {}. status is {}", componentId, status);
			return Either.right(status);
		}

		List<ComponentInstance> resourcesResult = new ArrayList<ComponentInstance>();
		List<RequirementCapabilityRelDef> requirementsResult = new ArrayList<RequirementCapabilityRelDef>();

		List<ImmutablePair<ComponentInstanceData, GraphEdge>> resourceInstances = resourceInstancesRes.left().value();
		if (resourceInstances != null && false == resourceInstances.isEmpty()) {
			Map<String, Map<String, CapabilityDefinition>> compInstCapabilities = new HashMap<String, Map<String, CapabilityDefinition>>();
			Map<String, Map<String, RequirementDefinition>> compInstReq = new HashMap<String, Map<String, RequirementDefinition>>();
			Map<String, Map<String, ArtifactDefinition>> compInstDeploymentArtifacts = new HashMap<String, Map<String, ArtifactDefinition>>();
			Map<String, Map<String, ArtifactDefinition>> compInstInformationalArtifacts = new HashMap<String, Map<String, ArtifactDefinition>>();
			Map<String, Component> compInstOriginsMap = new HashMap<String, Component>();

			for (ImmutablePair<ComponentInstanceData, GraphEdge> immutablePair : resourceInstances) {

				ComponentInstanceData resourceInstanceData = immutablePair.getKey();
				if (log.isDebugEnabled())
					log.debug("Going to fetch the relationships of resource instance {}", resourceInstanceData);

				ComponentInstance resourceInstance = new ComponentInstance(resourceInstanceData.getComponentInstDataDefinition());

				TitanOperationStatus status = getFullComponentInstance(compInstCapabilities, compInstReq, compInstDeploymentArtifacts, compInstOriginsMap, resourceInstance, compInstNodeType, compInstInformationalArtifacts);
				if (status != TitanOperationStatus.OK) {
					return Either.right(status);
				}
				resourcesResult.add(resourceInstance);

				Either<List<ImmutablePair<RelationshipInstData, GraphEdge>>, TitanOperationStatus> relationshipsRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance),
						(String) resourceInstanceData.getUniqueId(), GraphEdgeLabels.RELATIONSHIP_INST, NodeTypeEnum.RelationshipInst, RelationshipInstData.class);

				if (relationshipsRes.isRight()) {
					status = relationshipsRes.right().value();
					log.debug("After fetching all reslationships of resource instance {} under component {} . status is {}", resourceInstanceData.getUniqueId(), componentId, status);
					if (status == TitanOperationStatus.NOT_FOUND) {
						continue;
					} else {
						log.error("Failed to find relationhips of resource instance {} under component {}. status is {}", resourceInstanceData.getUniqueId(), componentId, status);
						return Either.right(status);
					}
				}

				String sourceResourceUid = (String) resourceInstanceData.getUniqueId();

				Map<String, List<ImmutablePair<String, RelationshipInstData>>> targetNodeToRelationship = new HashMap<String, List<ImmutablePair<String, RelationshipInstData>>>();

				List<ImmutablePair<RelationshipInstData, GraphEdge>> relationshipsImpl = relationshipsRes.left().value();
				status = populateTargetAndRelationsForGivenSource(targetNodeToRelationship, relationshipsImpl);

				if (status != TitanOperationStatus.OK) {
					return Either.right(status);
				}

				if (targetNodeToRelationship.isEmpty()) {
					log.error("No target found for relationship instances of resource instance {}", resourceInstanceData.getUniqueId());
					return Either.right(TitanOperationStatus.INVALID_ELEMENT);
				}

				buildRelationsForSource(requirementsResult, sourceResourceUid, targetNodeToRelationship);

			}

			return Either.left(new ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>(resourcesResult, requirementsResult));
		} else {
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

	}

	private Either<List<RequirementCapabilityRelDef>, TitanOperationStatus> getRelationsForSource(String resourceInstanceUid) {
		Either<List<ImmutablePair<RelationshipInstData, GraphEdge>>, TitanOperationStatus> relationshipsRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceUid,
				GraphEdgeLabels.RELATIONSHIP_INST, NodeTypeEnum.RelationshipInst, RelationshipInstData.class);

		TitanOperationStatus status;
		List<RequirementCapabilityRelDef> requirementsResult = new ArrayList<RequirementCapabilityRelDef>();

		if (relationshipsRes.isRight()) {
			status = relationshipsRes.right().value();
			log.debug("After fetching all reslationships of resource instance {}. status is {}", resourceInstanceUid, status);
			if (status == TitanOperationStatus.NOT_FOUND) {
				return Either.left(requirementsResult);
			} else {
				log.error("Failed to find relationhips of resource instance {}. status is {}", resourceInstanceUid, status);
				return Either.right(status);
			}
		}

		Map<String, List<ImmutablePair<String, RelationshipInstData>>> targetNodeToRelationship = new HashMap<String, List<ImmutablePair<String, RelationshipInstData>>>();

		List<ImmutablePair<RelationshipInstData, GraphEdge>> relationshipsImpl = relationshipsRes.left().value();
		status = populateTargetAndRelationsForGivenSource(targetNodeToRelationship, relationshipsImpl);

		if (status != TitanOperationStatus.OK) {
			return Either.right(status);
		}

		if (targetNodeToRelationship.isEmpty()) {
			log.error("No target found for relationship instances of resource instance {}", resourceInstanceUid);
			return Either.right(TitanOperationStatus.INVALID_ELEMENT);
		}

		buildRelationsForSource(requirementsResult, resourceInstanceUid, targetNodeToRelationship);
		return Either.left(requirementsResult);
	}

	private Either<List<RequirementCapabilityRelDef>, TitanOperationStatus> getRelationsForTarget(String resourceInstanceUid) {

		TitanOperationStatus status;

		Either<List<ImmutablePair<RelationshipInstData, GraphEdge>>, TitanOperationStatus> relationshipsRes = titanGenericDao.getParentNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceUid,
				GraphEdgeLabels.CAPABILITY_NODE, NodeTypeEnum.RelationshipInst, RelationshipInstData.class);

		List<RequirementCapabilityRelDef> requirementsResult = new ArrayList<RequirementCapabilityRelDef>();

		if (relationshipsRes.isRight()) {
			status = relationshipsRes.right().value();
			log.debug("After fetching all reslationships of resource instance {}. status is {}", resourceInstanceUid, status);
			if (status == TitanOperationStatus.NOT_FOUND) {
				return Either.left(requirementsResult);
			} else {
				log.error("Failed to find relationhips of resource instance {}. status is {}", resourceInstanceUid, status);
				return Either.right(status);
			}
		}

		Map<String, List<ImmutablePair<String, RelationshipInstData>>> sourceNodeToRelationship = new HashMap<String, List<ImmutablePair<String, RelationshipInstData>>>();

		List<ImmutablePair<RelationshipInstData, GraphEdge>> relationshipsImpl = relationshipsRes.left().value();
		status = populateSourceAndRelationsForGivenTarget(sourceNodeToRelationship, relationshipsImpl);

		if (status != TitanOperationStatus.OK) {
			return Either.right(status);
		}

		if (sourceNodeToRelationship.isEmpty()) {
			log.error("No target found for relationship instances of resource instance {}", resourceInstanceUid);
			return Either.right(TitanOperationStatus.INVALID_ELEMENT);
		}

		buildRelationsForTarget(requirementsResult, resourceInstanceUid, sourceNodeToRelationship);
		return Either.left(requirementsResult);
	}

	@Override
	public Either<ComponentInstance, StorageOperationStatus> getFullComponentInstance(ComponentInstance componentInstance, NodeTypeEnum compInstNodeType) {
		Map<String, Map<String, CapabilityDefinition>> compInstCapabilities = new HashMap<String, Map<String, CapabilityDefinition>>();
		Map<String, Map<String, RequirementDefinition>> compInstReq = new HashMap<String, Map<String, RequirementDefinition>>();
		Map<String, Map<String, ArtifactDefinition>> compInstDeploymentArtifacts = new HashMap<String, Map<String, ArtifactDefinition>>();
		Map<String, Map<String, ArtifactDefinition>> compInstInformationalArtifacts = new HashMap<String, Map<String, ArtifactDefinition>>();
		Map<String, Component> compInstOrigins = new HashMap<String, Component>();

		TitanOperationStatus fullResourceInstance = getFullComponentInstance(compInstCapabilities, compInstReq, compInstDeploymentArtifacts, compInstOrigins, componentInstance, compInstNodeType, compInstInformationalArtifacts);
		if (!fullResourceInstance.equals(TitanOperationStatus.OK)) {
			log.debug("failed to get full data of resource instance. error: {}", fullResourceInstance);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(fullResourceInstance));
		}
		return Either.left(componentInstance);
	}

	private TitanOperationStatus getFullComponentInstance(Map<String, Map<String, CapabilityDefinition>> compInstCapabilities, Map<String, Map<String, RequirementDefinition>> compInstReq,
			Map<String, Map<String, ArtifactDefinition>> compInstDeploymentArtifacts, Map<String, Component> compInstOrigins, ComponentInstance compInst, NodeTypeEnum compInstNodeType,
			Map<String, Map<String, ArtifactDefinition>> compInstInformationalArtifacts) {
		Component component = null;
		ComponentOperation componentOperation = getComponentOperation(compInstNodeType);
		String componentUid = compInst.getComponentUid();
		if (compInstOrigins.containsKey(componentUid)) {
			component = compInstOrigins.get(componentUid);
		} else {
			Either<Component, StorageOperationStatus> metadataComponent = componentOperation.getMetadataComponent(componentUid, true);
			if (metadataComponent.isRight()) {
				log.debug("Failed to fetch the origin component for component instance, origin Id {}, error: {}", componentUid, metadataComponent.right().value());
				return TitanOperationStatus.GENERAL_ERROR;
			}
			component = metadataComponent.left().value();
			compInstOrigins.put(componentUid, component);

		}
		String icon = component.getIcon();
		if (log.isDebugEnabled())
			log.debug("Fetch the resource instance icon from the resource itself. icon = {}", icon);
		compInst.setIcon(icon);
		String componentName = component.getName();
		compInst.setComponentName(componentName);
		compInst.setComponentVersion(component.getVersion());
		if (component.getComponentType() == ComponentTypeEnum.RESOURCE) {
			compInst.setToscaComponentName(((Resource) component).getToscaResourceName());
		}

		List<ComponentInstance> componentInstances = new ArrayList<>();
		List<String> derivedFromList = new ArrayList<String>();

		// For VFC/VL/CP
		if (compInstNodeType == NodeTypeEnum.Resource && ((Resource) component).getResourceType() != ResourceTypeEnum.VF) {
			resourceOperation.fillResourceDerivedListFromGraph(component.getUniqueId(), derivedFromList);
		} else {
			// Getting component instances that the origin component of this
			// component instance is their container, so we can use the logic of
			// getting req/cap from them
			// and fill this component instance with those req/cap
			Either<List<ImmutablePair<ComponentInstanceData, GraphEdge>>, TitanOperationStatus> allComponentInstanceFromGraph = getAllComponentInstanceFromGraph(componentUid, compInstNodeType, true);
			if (allComponentInstanceFromGraph.isRight() && allComponentInstanceFromGraph.right().value() != TitanOperationStatus.NOT_FOUND) {
				log.debug("Couldn't fetch component instances for component {} of type {}", componentUid, compInstNodeType);
				return allComponentInstanceFromGraph.right().value();
			}
			List<ImmutablePair<ComponentInstanceData, GraphEdge>> allCIs = allComponentInstanceFromGraph.isLeft() ? allComponentInstanceFromGraph.left().value() : new ArrayList<>();
			for (ImmutablePair<ComponentInstanceData, GraphEdge> entry : allCIs) {
				componentInstances.add(new ComponentInstance(entry.left.getComponentInstDataDefinition()));
			}
			component.setComponentInstances(componentInstances);
		}

		StorageOperationStatus capStatus = setCompInstCapabilitiesFromGraph(compInstCapabilities, component, compInstNodeType, compInst, derivedFromList);
		if (capStatus != StorageOperationStatus.OK) {
			log.debug("Failed to find capability of resource {}. status is {}", componentName, capStatus);

		}
		capStatus = setCompInstRequirementsFromGraph(compInstReq, component, compInstNodeType, compInst);
		if (capStatus != StorageOperationStatus.OK) {
			log.debug("Failed to find requirements of resource {}. status is {}", componentName, capStatus);

		}

		capStatus = setCompInstDeploymentArtifactsFromGraph(compInstDeploymentArtifacts, componentUid, compInst);
		if (capStatus != StorageOperationStatus.OK) {
			log.debug("Failed to find resource deployment artifacts of resource {}. status is {}", componentName, capStatus);

		}

		capStatus = setCompInstInformationalArtifactsResourceFromGraph(compInstInformationalArtifacts, componentUid, compInst);
		if (capStatus != StorageOperationStatus.OK) {
			log.debug("Failed to find resource deployment artifacts of resource {}. status is {}", componentName, capStatus);

		}

		capStatus = setCompInstDeploymentArtifactsFromGraph(compInst);
		if (capStatus != StorageOperationStatus.OK) {
			log.debug("Failed to find resource deployment artifacts of resource instance {} . status is {}", compInst.getName(), capStatus);
		}
		
		capStatus = setCompInstInformationaltArtifactsFromGraph(compInst);
		if (capStatus != StorageOperationStatus.OK) {
			log.debug("Failed to find resource informational artifacts of resource instance {} . status is {}", compInst.getName(), capStatus);
		}

		capStatus = setGroupInstFromGraph(compInst);
		if (capStatus != StorageOperationStatus.OK) {
			log.debug("Failed to find resource groups of resource instance {} . status is {}", compInst.getName(), capStatus);
		}
		return TitanOperationStatus.OK;
	}

	private StorageOperationStatus setCompInstInformationaltArtifactsFromGraph(ComponentInstance resourceInstance) {
		Map<String, ArtifactDefinition> informationalArtifacts = null;
		if (resourceInstance.getArtifacts() == null) {
			informationalArtifacts = new HashMap<String, ArtifactDefinition>();
		} else {
			informationalArtifacts = new HashMap<String, ArtifactDefinition>(resourceInstance.getArtifacts());
		}

		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> result = artifactOperation.getArtifacts(resourceInstance.getUniqueId(), NodeTypeEnum.ResourceInstance, true, ArtifactGroupTypeEnum.INFORMATIONAL.getType());
		if (result.isRight()) {
			StorageOperationStatus status = result.right().value();
			if (status != StorageOperationStatus.NOT_FOUND) {
				return status;
			}
		} else {
			informationalArtifacts.putAll(result.left().value());			
		}
		
		resourceInstance.setArtifacts(informationalArtifacts);
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus setGroupInstFromGraph(ComponentInstance compInst) {
		List<GroupInstance> groupInstances = null;

		Either<List<GroupInstance>, StorageOperationStatus> result = groupInstanceOperation.getAllGroupInstances(compInst.getUniqueId(), NodeTypeEnum.ResourceInstance);
		if (result.isRight()) {
			StorageOperationStatus status = result.right().value();
			if (status != StorageOperationStatus.NOT_FOUND) {
				return status;
			} else {

				return StorageOperationStatus.OK;
			}
		}

		groupInstances = result.left().value();
		compInst.setGroupInstances(groupInstances);

		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus setCompInstInformationalArtifactsResourceFromGraph(Map<String, Map<String, ArtifactDefinition>> resourcesInformationalArtifacts, String componentUid, ComponentInstance resourceInstance) {

		if (resourcesInformationalArtifacts.containsKey(componentUid)) {
			resourceInstance.setArtifacts(resourcesInformationalArtifacts.get(componentUid));
			return StorageOperationStatus.OK;
		}

		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> result = artifactOperation.getArtifacts(componentUid, NodeTypeEnum.Resource, true, ArtifactGroupTypeEnum.INFORMATIONAL.getType());
		if (result.isRight()) {
			StorageOperationStatus status = result.right().value();
			if (status != StorageOperationStatus.NOT_FOUND) {
				return status;
			} else {
				return StorageOperationStatus.OK;
			}
		}
		Map<String, ArtifactDefinition> artifacts = result.left().value();
		if (!artifacts.isEmpty()) {
			Map<String, ArtifactDefinition> tempArtifacts = new HashMap<>(artifacts);
			for (Entry<String, ArtifactDefinition> artifact : artifacts.entrySet()) {
				if (!artifact.getValue().checkEsIdExist()) {
					tempArtifacts.remove(artifact.getKey());
				}
			}
			resourceInstance.setArtifacts(tempArtifacts);
			resourcesInformationalArtifacts.put(componentUid, tempArtifacts);
		}

		return StorageOperationStatus.OK;

	}

	protected StorageOperationStatus setCompInstDeploymentArtifactsFromGraph(ComponentInstance resourceInstance) {

		Map<String, ArtifactDefinition> deploymentArtifacts = null;
		if (resourceInstance.getDeploymentArtifacts() == null) {
			deploymentArtifacts = new HashMap<String, ArtifactDefinition>();
		} else {
			deploymentArtifacts = new HashMap<String, ArtifactDefinition>(resourceInstance.getDeploymentArtifacts());
		}

		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> result = artifactOperation.getArtifacts(resourceInstance.getUniqueId(), NodeTypeEnum.ResourceInstance, true, ArtifactGroupTypeEnum.DEPLOYMENT.getType());
		if (result.isRight()) {
			StorageOperationStatus status = result.right().value();
			if (status != StorageOperationStatus.NOT_FOUND) {
				return status;
			} else {
				resourceInstance.setDeploymentArtifacts(deploymentArtifacts);
				return StorageOperationStatus.OK;
			}
		}

		Map<String, ArtifactDefinition> artifacts = result.left().value();
		if ((artifacts != null) && !artifacts.isEmpty()) {
			for (ArtifactDefinition artifact : artifacts.values()) {
				if (artifact.getArtifactType().equalsIgnoreCase(ArtifactTypeEnum.HEAT_ENV.getType())) {
					Either<List<HeatParameterDefinition>, StorageOperationStatus> heatParamsForEnv = artifactOperation.getHeatParamsForEnv(artifact);
					if (heatParamsForEnv.isRight()) {
						log.debug("failed to get heat parameters values for heat artifact {}", artifact.getUniqueId());
						return heatParamsForEnv.right().value();
					} else {
						artifact.setListHeatParameters(heatParamsForEnv.left().value());
					}
				}
			}

			// add resource instance artifacts to the artifacts inherited from
			// resource
			deploymentArtifacts.putAll(artifacts);
			resourceInstance.setDeploymentArtifacts(deploymentArtifacts);
		}

		return StorageOperationStatus.OK;

	}

	private Either<List<ImmutablePair<ComponentInstanceData, GraphEdge>>, TitanOperationStatus> getAllComponentInstanceFromGraph(String componentId, NodeTypeEnum containerNodeType, boolean withEdges) {
		if (log.isDebugEnabled())
			log.debug("Going to fetch all resource instances nodes in graph associate to component {}", componentId);
		Either<List<ImmutablePair<ComponentInstanceData, GraphEdge>>, TitanOperationStatus> resourceInstancesRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(containerNodeType), componentId, GraphEdgeLabels.RESOURCE_INST,
				NodeTypeEnum.ResourceInstance, ComponentInstanceData.class, withEdges);
		if (log.isDebugEnabled())
			log.debug("After fetching all component instances under component {}", componentId);

		if (resourceInstancesRes.isLeft()) {
			printAllResourceInstancesNames(resourceInstancesRes);
		}
		return resourceInstancesRes;
	}

	private void printAllResourceInstancesNames(Either<List<ImmutablePair<ComponentInstanceData, GraphEdge>>, TitanOperationStatus> resourceInstancesRes) {
		if (log.isTraceEnabled()) {
			StringBuilder builder = new StringBuilder();
			builder.append("Result is ");
			List<ImmutablePair<ComponentInstanceData, GraphEdge>> listResData = resourceInstancesRes.left().value();
			for (ImmutablePair<ComponentInstanceData, GraphEdge> resInstPair : listResData) {
				ComponentInstanceData resdata = resInstPair.getLeft();
				builder.append(resdata.getName()).append(", ");
			}
			log.trace(builder.toString());
		}
	}

	private TitanOperationStatus populateTargetAndRelationsForGivenSource(Map<String, List<ImmutablePair<String, RelationshipInstData>>> targetNodeToRelationship, List<ImmutablePair<RelationshipInstData, GraphEdge>> relationshipsImpl) {
		if (relationshipsImpl != null && false == relationshipsImpl.isEmpty()) {
			for (ImmutablePair<RelationshipInstData, GraphEdge> pair : relationshipsImpl) {
				RelationshipInstData relationshipInstData = pair.getKey();

				GraphEdge requirementEdge = pair.getValue();
				String requirementName = (String) requirementEdge.getProperties().get(GraphPropertiesDictionary.NAME.getProperty());

				Either<ImmutablePair<ComponentInstanceData, GraphEdge>, TitanOperationStatus> targetNodeRes = titanGenericDao.getChild(relationshipInstData.getUniqueIdKey(), relationshipInstData.getUniqueId(), GraphEdgeLabels.CAPABILITY_NODE,
						NodeTypeEnum.ResourceInstance, ComponentInstanceData.class);

				if (targetNodeRes.isRight()) {
					TitanOperationStatus status = targetNodeRes.right().value();
					log.error("Failed to find the target node of relationship inst {}. status is {}", relationshipInstData, status);
					return status;
				}

				addRelationshipInstToTargetMap(targetNodeToRelationship, relationshipInstData, requirementName, targetNodeRes);

			}
		}

		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus populateSourceAndRelationsForGivenTarget(Map<String, List<ImmutablePair<String, RelationshipInstData>>> sourceNodeToRelationship, List<ImmutablePair<RelationshipInstData, GraphEdge>> relationshipsImpl) {
		if (relationshipsImpl != null && false == relationshipsImpl.isEmpty()) {
			for (ImmutablePair<RelationshipInstData, GraphEdge> pair : relationshipsImpl) {
				RelationshipInstData relationshipInstData = pair.getKey();

				GraphEdge requirementEdge = pair.getValue();
				String requirementName = (String) requirementEdge.getProperties().get(GraphPropertiesDictionary.NAME.getProperty());

				Either<ImmutablePair<ComponentInstanceData, GraphEdge>, TitanOperationStatus> sourceNodeRes = titanGenericDao.getParentNode(relationshipInstData.getUniqueIdKey(), relationshipInstData.getUniqueId(), GraphEdgeLabels.RELATIONSHIP_INST,
						NodeTypeEnum.ResourceInstance, ComponentInstanceData.class);

				if (sourceNodeRes.isRight()) {
					TitanOperationStatus status = sourceNodeRes.right().value();
					log.error("Failed to find the source node of relationship inst {}. status is {}", relationshipInstData, status);
					return status;
				}

				addRelationshipInstToTargetMap(sourceNodeToRelationship, relationshipInstData, requirementName, sourceNodeRes);

			}
		}

		return TitanOperationStatus.OK;
	}

	private void buildRelationsForSource(List<RequirementCapabilityRelDef> requirementsResult, String sourceResourceUid, Map<String, List<ImmutablePair<String, RelationshipInstData>>> targetNodeToRelationship) {
		for (Entry<String, List<ImmutablePair<String, RelationshipInstData>>> targetToRel : targetNodeToRelationship.entrySet()) {
			RequirementCapabilityRelDef requirementCapabilityRelDef = new RequirementCapabilityRelDef();
			requirementCapabilityRelDef.setFromNode(sourceResourceUid);
			String targetUid = targetToRel.getKey();
			requirementCapabilityRelDef.setToNode(targetUid);

			List<RequirementAndRelationshipPair> relationships = new ArrayList<RequirementAndRelationshipPair>();

			populateRelationships(targetToRel, relationships);
			requirementCapabilityRelDef.setRelationships(relationships);

			requirementsResult.add(requirementCapabilityRelDef);
		}
	}

	private void buildRelationsForTarget(List<RequirementCapabilityRelDef> requirementsResult, String targetResourceUid, Map<String, List<ImmutablePair<String, RelationshipInstData>>> sourceNodeToRelationship) {
		for (Entry<String, List<ImmutablePair<String, RelationshipInstData>>> sourceToRel : sourceNodeToRelationship.entrySet()) {
			RequirementCapabilityRelDef requirementCapabilityRelDef = new RequirementCapabilityRelDef();
			requirementCapabilityRelDef.setToNode(targetResourceUid);
			String sourceUid = sourceToRel.getKey();
			requirementCapabilityRelDef.setFromNode(sourceUid);
			List<RequirementAndRelationshipPair> relationships = new ArrayList<RequirementAndRelationshipPair>();

			populateRelationships(sourceToRel, relationships);
			requirementCapabilityRelDef.setRelationships(relationships);

			requirementsResult.add(requirementCapabilityRelDef);
		}
	}

	private void addRelationshipInstToTargetMap(Map<String, List<ImmutablePair<String, RelationshipInstData>>> targetNodeToRelationship, RelationshipInstData relationshipInstData, String requirementName,
			Either<ImmutablePair<ComponentInstanceData, GraphEdge>, TitanOperationStatus> targetNodeRes) {

		ImmutablePair<ComponentInstanceData, GraphEdge> targetResourcePair = targetNodeRes.left().value();
		ComponentInstanceData targetResourceData = targetResourcePair.getKey();

		GraphEdge edge = targetResourcePair.right;
		if (edge.getEdgeType().equals(GraphEdgeLabels.RELATIONSHIP_INST)) {
			requirementName = (String) edge.getProperties().get(GraphEdgePropertiesDictionary.NAME.getProperty());
		}

		String targetResourceUid = (String) targetResourceData.getUniqueId();
		List<ImmutablePair<String, RelationshipInstData>> requirementRelationshipPair = targetNodeToRelationship.get(targetResourceUid);
		if (requirementRelationshipPair == null) {
			requirementRelationshipPair = new ArrayList<ImmutablePair<String, RelationshipInstData>>();
			targetNodeToRelationship.put(targetResourceUid, requirementRelationshipPair);
		}
		ImmutablePair<String, RelationshipInstData> reqRelationshipPair = new ImmutablePair<String, RelationshipInstData>(requirementName, relationshipInstData);
		requirementRelationshipPair.add(reqRelationshipPair);
	}

	private void populateRelationships(Entry<String, List<ImmutablePair<String, RelationshipInstData>>> targetToRel, List<RequirementAndRelationshipPair> relationships) {

		List<ImmutablePair<String, RelationshipInstData>> values = targetToRel.getValue();
		for (ImmutablePair<String, RelationshipInstData> value : values) {
			String reqName = value.getKey();
			RelationshipInstData relationshipInstData = value.getValue();
			RelationshipImpl relationshipImpl = new RelationshipImpl();
			relationshipImpl.setType(relationshipInstData.getType());
			RequirementAndRelationshipPair pair = new RequirementAndRelationshipPair(reqName, relationshipImpl);
			pair.setCapabilityOwnerId(relationshipInstData.getCapabilityOwnerId());
			pair.setCapabilityUid(relationshipInstData.getCapabiltyId());
			pair.setRequirementOwnerId(relationshipInstData.getRequirementOwnerId());
			pair.setRequirementUid(relationshipInstData.getRequirementId());
			pair.setId(relationshipInstData.getUniqueId());
			relationships.add(pair);
		}
	}

	/**
	 * FOR TEST ONLY
	 * 
	 * @param resourceOperation
	 */
	public void setResourceOperation(ResourceOperation resourceOperation) {
		this.resourceOperation = resourceOperation;
	}

	@Override
	public Either<RequirementCapabilityRelDef, StorageOperationStatus> associateResourceInstances(String componentId, NodeTypeEnum nodeType, RequirementCapabilityRelDef relation, boolean inTransaction, boolean isClone) {

		Either<RequirementCapabilityRelDef, StorageOperationStatus> result = null;
		try {
			Either<RequirementCapabilityRelDef, TitanOperationStatus> multiRequirements = associateResourceInstancesMultiRequirements(componentId, nodeType, relation, isClone);
			if (multiRequirements.isRight()) {
				TitanOperationStatus status = multiRequirements.right().value();
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError, "associateComponentInstances");
				BeEcompErrorManager.getInstance().logBeDaoSystemError("associateComponentInstances");
				log.debug("Failed to associate component instances {}. status is {}", relation, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}
			StorageOperationStatus updateCalculatedCapReqResult = updateCalculatedCapReq(relation, true);
			if (!updateCalculatedCapReqResult.equals(StorageOperationStatus.OK)) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError, "associateComponentInstances");
				BeEcompErrorManager.getInstance().logBeDaoSystemError("associateComponentInstances");
				log.debug("Failed to associate component instances. {}. status is {}", relation, updateCalculatedCapReqResult);
				result = Either.right(updateCalculatedCapReqResult);
				return result;
			}
			result = Either.left(multiRequirements.left().value());

			return result;

		} finally {
			if (false == inTransaction) {
				commitOrRollback(result);
			}
		}
	}

	private Either<RequirementCapabilityRelDef, TitanOperationStatus> associateResourceInstancesMultiRequirements(String componentId, NodeTypeEnum nodeType, RequirementCapabilityRelDef relation, boolean isClone) {

		String fromNode = relation.getFromNode();
		String toNode = relation.getToNode();
		List<RequirementAndRelationshipPair> relationships = relation.getRelationships();
		if (relationships == null || relationships.isEmpty()) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedAddingResourceInstanceError, "AssociateResourceInstances - missing relationship", fromNode, componentId);
			BeEcompErrorManager.getInstance().logBeFailedAddingResourceInstanceError("AssociateResourceInstances - missing relationship", fromNode, componentId);
			log.debug("No requirement definition sent in order to set the relation between {} to {}", fromNode, toNode);
			return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
		}

		List<RequirementAndRelationshipPair> relationshipsResult = new ArrayList<RequirementAndRelationshipPair>();
		for (RequirementAndRelationshipPair immutablePair : relationships) {
			String requirement = immutablePair.getRequirement();

			Either<RelationshipInstData, TitanOperationStatus> associateRes = connectResourcesInService(componentId, nodeType, fromNode, toNode, immutablePair);

			if (associateRes.isRight()) {
				TitanOperationStatus status = associateRes.right().value();
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedAddingResourceInstanceError, "AssociateResourceInstances", fromNode, componentId);
				BeEcompErrorManager.getInstance().logBeFailedAddingResourceInstanceError("AssociateResourceInstances - missing relationship", fromNode, componentId);
				log.debug("Failed to associate resource instance {} to resource instance {}. status is {}", fromNode, toNode, status);
				return Either.right(status);
			}

			RelationshipInstData relationshipInstData = associateRes.left().value();
			RelationshipImpl relationshipImplResult = new RelationshipImpl();
			relationshipImplResult.setType(relationshipInstData.getType());
			RequirementAndRelationshipPair requirementAndRelationshipPair = new RequirementAndRelationshipPair(requirement, relationshipImplResult);
			requirementAndRelationshipPair.setCapability(immutablePair.getCapability());
			requirementAndRelationshipPair.setCapabilityOwnerId(relationshipInstData.getCapabilityOwnerId());
			requirementAndRelationshipPair.setRequirementOwnerId(relationshipInstData.getRequirementOwnerId());
			requirementAndRelationshipPair.setCapabilityUid(immutablePair.getCapabilityUid());
			requirementAndRelationshipPair.setRequirementUid(immutablePair.getRequirementUid());
			relationshipsResult.add(requirementAndRelationshipPair);
			if (!isClone) {
				log.trace("update customization UUID for from CI {} and to CI {}", relation.getFromNode(), relation.getToNode());
				StorageOperationStatus status;
				status = updateCustomizationUUID(relation.getFromNode());
				if (status != StorageOperationStatus.OK) {
					return Either.right(TitanOperationStatus.GENERAL_ERROR);
				}
				status = updateCustomizationUUID(relation.getToNode());
				if (status != StorageOperationStatus.OK) {
					return Either.right(TitanOperationStatus.GENERAL_ERROR);
				}
			}

		}

		RequirementCapabilityRelDef capabilityRelDef = new RequirementCapabilityRelDef();
		capabilityRelDef.setFromNode(fromNode);
		capabilityRelDef.setToNode(toNode);
		capabilityRelDef.setRelationships(relationshipsResult);

		return Either.left(capabilityRelDef);
	}

	@Override
	public Either<RequirementCapabilityRelDef, StorageOperationStatus> associateResourceInstances(String componentId, NodeTypeEnum nodeType, RequirementCapabilityRelDef relation) {
		return associateResourceInstances(componentId, nodeType, relation, false, false);
	}

	@Override
	public Either<List<ComponentInstance>, StorageOperationStatus> deleteAllComponentInstances(String containerComponentId, NodeTypeEnum containerNodeType, boolean inTransaction) {

		Either<List<ComponentInstance>, StorageOperationStatus> result = null;
		try {
			Either<List<ComponentInstance>, TitanOperationStatus> multiRequirements = deleteAllComponentInstancesInternal(containerComponentId, containerNodeType);
			if (multiRequirements.isRight()) {
				TitanOperationStatus status = multiRequirements.right().value();
				if (multiRequirements.right().value() != TitanOperationStatus.NOT_FOUND) {
					BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeSystemError, "deleteAllResourceInstances - missing relationship");
					BeEcompErrorManager.getInstance().logBeSystemError("deleteAllResourceInstances - missing relationship");
				}
				log.debug("Failed to delete resource instances of service {}. status is {}", containerComponentId, status);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;

			}

			result = Either.left(multiRequirements.left().value());

			return result;

		} finally {
			if (false == inTransaction) {
				commitOrRollback(result);
			}
		}

	}

	@Override
	public Either<List<ComponentInstance>, StorageOperationStatus> deleteAllComponentInstances(String containerComponentId, NodeTypeEnum nodeType) {
		return deleteAllComponentInstances(containerComponentId, nodeType, false);
	}

	public Either<List<ComponentInstance>, TitanOperationStatus> deleteAllComponentInstancesInternal(String componentId, NodeTypeEnum nodeType) {

		log.debug("Going to delete all resource instances and their relatioships from service {}", componentId);

		Either<List<ImmutablePair<ComponentInstanceData, GraphEdge>>, TitanOperationStatus> resourceInstancesRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId, GraphEdgeLabels.RESOURCE_INST,
				NodeTypeEnum.ResourceInstance, ComponentInstanceData.class);

		if (resourceInstancesRes.isRight()) {
			TitanOperationStatus status = resourceInstancesRes.right().value();
			log.debug("After fetching all resource instances of service {}. status is {}", componentId, status);
			return Either.right(status);
		}

		List<ComponentInstance> result = new ArrayList<ComponentInstance>();
		List<ImmutablePair<ComponentInstanceData, GraphEdge>> listOfResInstances = resourceInstancesRes.left().value();
		for (ImmutablePair<ComponentInstanceData, GraphEdge> resInstance : listOfResInstances) {
			ComponentInstanceData resourceInstanceData = resInstance.getKey();
			String resourceInstUid = resourceInstanceData.getUniqueId();
			Either<ComponentInstance, TitanOperationStatus> removeResourceInstanceRes = removeComponentInstanceFromComponent(nodeType, componentId, resourceInstUid);
			log.debug("After removing resource instance {}. Result is {}", resourceInstUid, removeResourceInstanceRes);
			if (removeResourceInstanceRes.isRight()) {
				TitanOperationStatus status = removeResourceInstanceRes.right().value();
				log.error("After removing resource instance {}. status is {}", resourceInstUid, status);
				return Either.right(status);
			}
			ComponentInstance resourceInstance = removeResourceInstanceRes.left().value();
			result.add(resourceInstance);
		}

		log.debug("The following resource instances was deleted from service {} : {}", componentId, result);

		return Either.left(result);
	}

	public Either<ImmutablePair<List<ComponentInstance>, Map<String, String>>, StorageOperationStatus> cloneAllComponentInstancesFromContainerComponent(String componentIdFrom, Component component, NodeTypeEnum containerNodeType,
			NodeTypeEnum compInstNodeType, LifecycleStateEnum targetLifecycle, Map<String, List<ComponentInstanceInput>> inputsValuesMap) {

		List<ComponentInstance> list = new ArrayList<ComponentInstance>();
		Map<String, String> oldCompInstToNew = new HashMap<>();

		ImmutablePair<List<ComponentInstance>, Map<String, String>> result = new ImmutablePair<List<ComponentInstance>, Map<String, String>>(list, oldCompInstToNew);

		Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, StorageOperationStatus> allResourceInstances = getAllComponentInstances(componentIdFrom, containerNodeType, compInstNodeType, true);

		if (allResourceInstances.isRight()) {
			StorageOperationStatus status = allResourceInstances.right().value();
			if (status.equals(StorageOperationStatus.NOT_FOUND)) {

				return Either.left(result);
			} else {
				log.error("failed to get all resource instances for service {}. status={}", componentIdFrom, status);
				return Either.right(status);
			}
		}

		List<ComponentInstance> riList = allResourceInstances.left().value().left;
		Map<String, ComponentInstance> riMapper = new HashMap<>();
		int instanceNumber = 0;
		for (ComponentInstance ri : riList) {
			instanceNumber++;
			String origRiUniqueID = ri.getUniqueId();
			Either<ComponentInstance, StorageOperationStatus> createResourceInstance = createComponentInstance(component.getUniqueId(), containerNodeType, String.valueOf(instanceNumber), false, ri, compInstNodeType, true, true);
			if (createResourceInstance.isRight()) {
				StorageOperationStatus status = createResourceInstance.right().value();
				log.error("failed to clone resource instance {}. status ={}", origRiUniqueID, status);
				return Either.right(status);
			}
			ComponentInstance createdInstance = createResourceInstance.left().value();
			riMapper.put(origRiUniqueID, createdInstance);
			StorageOperationStatus associateArtifactsToResource = cloneResourceInstanceArtifacts(createdInstance, ri, targetLifecycle);
			if (associateArtifactsToResource != StorageOperationStatus.OK) {
				log.debug("failed to clone resource instance {} artifacts. error {} ", ri.getNormalizedName(), associateArtifactsToResource.name());
				return Either.right(associateArtifactsToResource);
			}

			StorageOperationStatus associatePropertyValuesToResource = cloneResourceInstancePropertyValues(createdInstance, ri);
			if (associatePropertyValuesToResource != StorageOperationStatus.OK) {
				log.debug("failed to clone resource instance {} property values. error {} ", ri.getNormalizedName(), associatePropertyValuesToResource.name());
				return Either.right(associatePropertyValuesToResource);
			}

			StorageOperationStatus associateAttributeValuesToResource = cloneResourceInstanceAttributeValues(createdInstance, ri);
			if (associateAttributeValuesToResource != StorageOperationStatus.OK) {
				log.debug("failed to clone resource instance {} attribute values. error {} ", ri.getNormalizedName(), associateAttributeValuesToResource.name());
				return Either.right(associateAttributeValuesToResource);
			}

			StorageOperationStatus associateInputValuesToResource = cloneResourceInstanceInputsValues(createdInstance, ri, component, inputsValuesMap);
			if (associateInputValuesToResource != StorageOperationStatus.OK) {
				log.debug("failed to clone resource instance {} property values. error {} ", ri.getNormalizedName(), associatePropertyValuesToResource.name());
				return Either.right(associatePropertyValuesToResource);
			}

			list.add(createdInstance);
			oldCompInstToNew.put(origRiUniqueID, createdInstance.getUniqueId());
		}

		List<RequirementCapabilityRelDef> relationsList = allResourceInstances.left().value().right;
		for (RequirementCapabilityRelDef relation : relationsList) {
			String origFrom = relation.getFromNode();
			String origTo = relation.getToNode();
			relation.setFromNode(riMapper.get(origFrom).getUniqueId());
			relation.setToNode(riMapper.get(origTo).getUniqueId());
			List<RequirementAndRelationshipPair> relationships = relation.getRelationships();
			for (RequirementAndRelationshipPair pair : relationships) {
				// for all atomic resource instances need to update to relevant
				// new ri ids
				String capOwnerId = pair.getCapabilityOwnerId();
				String reqOwnerId = pair.getRequirementOwnerId();
				if (isAtomicComponentInstance(riMapper.get(origFrom))) {
					reqOwnerId = riMapper.get(reqOwnerId).getUniqueId();
				}
				if (isAtomicComponentInstance(riMapper.get(origTo))) {
					capOwnerId = riMapper.get(capOwnerId).getUniqueId();
				}
				pair.setRequirementOwnerId(reqOwnerId);
				pair.setCapabilityOwnerId(capOwnerId);
			}

			Either<RequirementCapabilityRelDef, StorageOperationStatus> associateInstances = associateResourceInstances(component.getUniqueId(), containerNodeType, relation, true, true);
			if (associateInstances.isRight()) {
				StorageOperationStatus status = associateInstances.right().value();
				log.error("failed to assosiate resource instance {} and resource instance {}. status ={}", relation.getFromNode(), relation.getToNode(), status);
				return Either.right(status);
			}
		}

		return Either.left(result);
	}

	public Either<ImmutablePair<List<ComponentInstance>, Map<String, String>>, StorageOperationStatus> cloneAllComponentInstancesFromContainerComponent(String componentIdFrom, String componentIdTo, NodeTypeEnum containerNodeType,
			NodeTypeEnum compInstNodeType, LifecycleStateEnum targetLifecycle, TitanVertex metadataVertex, Resource prevResource, Resource newResource, Map<String, List<ComponentInstanceProperty>> inputsPropMap) {

		List<ComponentInstance> list = new ArrayList<ComponentInstance>();
		Map<String, String> oldCompInstToNew = new HashMap<>();

		ImmutablePair<List<ComponentInstance>, Map<String, String>> result = new ImmutablePair<List<ComponentInstance>, Map<String, String>>(list, oldCompInstToNew);

		List<ComponentInstance> riList = prevResource.getComponentInstances();
		Map<String, ComponentInstance> riMapper = new HashMap<>();
		int instanceNumber = 0;
		long timeProperties = 0;
		if (riList != null) {
			for (ComponentInstance ri : riList) {
				instanceNumber++;
				String origRiUniqueID = ri.getUniqueId();
				Either<TitanVertex, StorageOperationStatus> createResourceInstance = createComponentInstance(componentIdTo, containerNodeType, String.valueOf(instanceNumber), false, ri, compInstNodeType, true, true, metadataVertex);
				if (createResourceInstance.isRight()) {
					StorageOperationStatus status = createResourceInstance.right().value();
					log.error("failed to clone resource instance {}. status ={}", origRiUniqueID, status);
					return Either.right(status);
				}
				TitanVertex createdInstance = createResourceInstance.left().value();
				String createdInstanceId = (String) titanGenericDao.getProperty(createdInstance, GraphPropertiesDictionary.UNIQUE_ID.getProperty());

				StorageOperationStatus associateArtifactsToResource = cloneResourceInstanceArtifacts(createdInstance, ri, targetLifecycle);
				if (associateArtifactsToResource != StorageOperationStatus.OK) {
					log.debug("failed to clone resource instance {} artifacts. error {} ", ri.getNormalizedName(), associateArtifactsToResource.name());
					return Either.right(associateArtifactsToResource);
				}

				long start = System.currentTimeMillis();
				StorageOperationStatus associatePropertyValuesToResource = cloneResourceInstancePropertyValues(createdInstance, ri, inputsPropMap, newResource);
				if (associatePropertyValuesToResource != StorageOperationStatus.OK) {
					log.debug("failed to clone resource instance {} property values. error {} ", ri.getNormalizedName(), associatePropertyValuesToResource.name());
					return Either.right(associatePropertyValuesToResource);
				}
				long end = System.currentTimeMillis();
				timeProperties += (end - start);

				StorageOperationStatus associateAttributeValuesToResource = cloneResourceInstanceAttributeValues(createdInstance, ri, createdInstanceId);
				if (associateAttributeValuesToResource != StorageOperationStatus.OK) {
					log.debug("failed to clone resource instance {} attribute values. error {} ", ri.getNormalizedName(), associateAttributeValuesToResource.name());
					return Either.right(associateAttributeValuesToResource);
				}

				StorageOperationStatus associateInputValuesToResource = cloneResourceInstanceInputsValues(createdInstance, ri, createdInstanceId, newResource, null);
				if (associateInputValuesToResource != StorageOperationStatus.OK) {
					log.debug("failed to clone resource instance {} property values. error {} ", ri.getNormalizedName(), associatePropertyValuesToResource.name());
					return Either.right(associatePropertyValuesToResource);
				}
				Map<String, Object> properties = titanGenericDao.getProperties(createdInstance);
				ComponentInstanceData createdComponentInstance = GraphElementFactory.createElement(NodeTypeEnum.ResourceInstance.getName(), GraphElementTypeEnum.Node, properties, ComponentInstanceData.class);
				ComponentInstance createdResourceInstance = new ComponentInstance(createdComponentInstance.getComponentInstDataDefinition());
				riMapper.put(origRiUniqueID, createdResourceInstance);

				list.add(createdResourceInstance);
				oldCompInstToNew.put(origRiUniqueID, createdResourceInstance.getUniqueId());
			}
		}
		log.info("*********** total properties in ms {}", timeProperties);

		// List<RequirementCapabilityRelDef> relationsList =
		// instanceRelationPair.right;
		List<RequirementCapabilityRelDef> relationsList = prevResource.getComponentInstancesRelations();
		if (relationsList != null) {
			for (RequirementCapabilityRelDef relation : relationsList) {
				String origFrom = relation.getFromNode();
				String origTo = relation.getToNode();
				relation.setFromNode(riMapper.get(origFrom).getUniqueId());
				relation.setToNode(riMapper.get(origTo).getUniqueId());
				List<RequirementAndRelationshipPair> relationships = relation.getRelationships();
				for (RequirementAndRelationshipPair pair : relationships) {
					// for all atomic resource instances need to update to
					// relevant
					// new ri ids
					String capOwnerId = pair.getCapabilityOwnerId();
					String reqOwnerId = pair.getRequirementOwnerId();
					if (isAtomicComponentInstance(riMapper.get(origFrom))) {
						reqOwnerId = riMapper.get(reqOwnerId).getUniqueId();
					}
					if (isAtomicComponentInstance(riMapper.get(origTo))) {
						capOwnerId = riMapper.get(capOwnerId).getUniqueId();
					}
					pair.setRequirementOwnerId(reqOwnerId);
					pair.setCapabilityOwnerId(capOwnerId);
				}

				Either<RequirementCapabilityRelDef, StorageOperationStatus> associateInstances = associateResourceInstances(componentIdTo, containerNodeType, relation, true, true);
				if (associateInstances.isRight()) {
					StorageOperationStatus status = associateInstances.right().value();
					log.error("failed to assosiate resource instance {} and resource instance {}. status ={}", relation.getFromNode(), relation.getToNode(), status);
					return Either.right(status);
				}
			}
		}
		return Either.left(result);
	}

	private boolean isAtomicComponentInstance(ComponentInstance componentInstance) {
		OriginTypeEnum originType = componentInstance.getOriginType();
		if (originType == OriginTypeEnum.VFC || originType == OriginTypeEnum.VFCMT || originType == OriginTypeEnum.VL || originType == OriginTypeEnum.CP) {
			return true;
		}
		return false;
	}

	private StorageOperationStatus cloneResourceInstanceArtifacts(ComponentInstance toResourceInstance, ComponentInstance fromResourceInstance, LifecycleStateEnum targetLifecycle) {

		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> getArtifactsOfRI = artifactOperation.getArtifacts(fromResourceInstance.getUniqueId(), NodeTypeEnum.ResourceInstance, true);
		if (getArtifactsOfRI.isRight()) {
			StorageOperationStatus status = getArtifactsOfRI.right().value();
			if (status.equals(StorageOperationStatus.NOT_FOUND)) {
				status = StorageOperationStatus.OK;
			}
			return status;
		}

		Map<String, ArtifactDefinition> artifacts = getArtifactsOfRI.left().value();
		List<GroupInstance> groupInstancesFrom = fromResourceInstance.getGroupInstances();
		List<GroupInstance> groupInstancesTo = toResourceInstance.getGroupInstances();
		Map<String, List<String>> groupsInstanceArtifact = new HashMap<String, List<String>>();
		for (Entry<String, ArtifactDefinition> entry : artifacts.entrySet()) {

			ArtifactDefinition artifactDefinition = entry.getValue();
			String generatedFromIdArtifactUid = artifactDefinition.getGeneratedFromId();

			// US687135 Do not Add VF_MODULES_METADATA when checking out
			if (ArtifactTypeEnum.VF_MODULES_METADATA.getType().equals(artifactDefinition.getArtifactType())) {
				// The artifact of type VF_MODULES_METADATA should not be cloned
				// unless we are changing the state to certified.
				if (targetLifecycle != null && targetLifecycle != LifecycleStateEnum.CERTIFIED) {
					continue;
				}
			}
			Either<ArtifactDefinition, StorageOperationStatus> addArifactToResource = Either.left(artifactDefinition);

			addArifactToResource = artifactOperation.addArifactToComponent(artifactDefinition, toResourceInstance.getUniqueId(), NodeTypeEnum.ResourceInstance, false, true);

			if (addArifactToResource.isRight()) {
				return addArifactToResource.right().value();
			}

			if (groupInstancesTo != null) {
				for (GroupInstance groupInstanceTo : groupInstancesTo) {
					Optional<String> op = groupInstanceTo.getArtifacts().stream().filter(p -> p.equals(generatedFromIdArtifactUid)).findAny();
					if (op.isPresent()) {

						List<String> artifactsUid = null;
						if (groupsInstanceArtifact.containsKey(groupInstanceTo.getUniqueId())) {
							artifactsUid = groupsInstanceArtifact.get(groupInstanceTo.getUniqueId());
						} else {
							artifactsUid = new ArrayList<String>();
						}
						artifactsUid.add(addArifactToResource.left().value().getUniqueId());
						groupsInstanceArtifact.put(groupInstanceTo.getUniqueId(), artifactsUid);
						break;
					}
				}

			}
		}
		if (groupsInstanceArtifact != null && !groupsInstanceArtifact.isEmpty()) {
			for (Map.Entry<String, List<String>> groupArtifact : groupsInstanceArtifact.entrySet()) {
				groupInstanceOperation.associateArtifactsToGroupInstance(groupArtifact.getKey(), groupArtifact.getValue());
			}
		}
		Either<List<GroupInstance>, StorageOperationStatus> groupInstanceStatus = groupInstanceOperation.getAllGroupInstances(toResourceInstance.getUniqueId(), NodeTypeEnum.ResourceInstance);
		if (groupInstanceStatus.isRight()) {
			log.debug("failed to get groupinstance for component inatance {}", toResourceInstance.getUniqueId());
			return groupInstanceStatus.right().value();
		}
		toResourceInstance.setGroupInstances(groupInstanceStatus.left().value());
		toResourceInstance.setDeploymentArtifacts(artifacts);
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus cloneResourceInstanceArtifacts(TitanVertex toResourceInstance, ComponentInstance fromResourceInstance, LifecycleStateEnum targetLifecycle) {

		Either<Map<String, TitanVertex>, StorageOperationStatus> getArtifactsOfRI = artifactOperation.getArtifactsVertecies(fromResourceInstance.getUniqueId(), NodeTypeEnum.ResourceInstance, true);
		if (getArtifactsOfRI.isRight()) {
			StorageOperationStatus status = getArtifactsOfRI.right().value();
			if (status.equals(StorageOperationStatus.NOT_FOUND)) {
				status = StorageOperationStatus.OK;
			}
			return status;
		}

		Map<String, TitanVertex> artifacts = getArtifactsOfRI.left().value();
		for (Entry<String, TitanVertex> entry : artifacts.entrySet()) {

			TitanVertex artifactVertex = entry.getValue();
			// US687135 Do not Add VF_MODULES_METADATA when checking out
			String artifactType = (String) titanGenericDao.getProperty(artifactVertex, GraphPropertiesDictionary.ARTIFACT_TYPE.getProperty());
			String label = (String) titanGenericDao.getProperty(artifactVertex, GraphPropertiesDictionary.ARTIFACT_LABEL.getProperty());
			if (ArtifactTypeEnum.VF_MODULES_METADATA.getType().equals(artifactType)) {
				// The artifact of type VF_MODULES_METADATA should not be cloned
				// unless we are changing the state to certified.
				if (targetLifecycle != null && targetLifecycle != LifecycleStateEnum.CERTIFIED) {
					continue;
				}
			}

			StorageOperationStatus addArifactToResource = artifactOperation.addArifactToComponent(artifactVertex, toResourceInstance, label);

			if (!addArifactToResource.equals(StorageOperationStatus.OK)) {
				return addArifactToResource;
			}
		}
		// toResourceInstance.setDeploymentArtifacts(artifacts);
		return StorageOperationStatus.OK;
	}

	public Either<Integer, StorageOperationStatus> increaseAndGetResourceInstanceSpecificCounter(String resourceInstanceId, GraphPropertiesDictionary counterType, boolean inTransaction) {

		Either<Integer, StorageOperationStatus> result = null;
		try {

			Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
			if (graphResult.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graphResult.right().value()));
				return result;
			}
			Either<TitanVertex, TitanOperationStatus> vertexService = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId);
			if (vertexService.isRight()) {
				log.debug("failed to fetch vertex of resource instance for id = {}", resourceInstanceId);
				TitanOperationStatus status = vertexService.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
				}
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(vertexService.right().value()));
				return result;
			}
			Vertex vertex = vertexService.left().value();

			VertexProperty<Object> vertexProperty = vertex.property(counterType.getProperty());
			Integer counter = 0;
			if (vertexProperty.isPresent()) {
				if (vertexProperty.value() != null) {
					counter = (Integer) vertexProperty.value();
				}
			}

			counter++;
			vertex.property(counterType.getProperty(), counter);

			result = Either.left(counter);
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("increaseAndGetResourceInstanceSpecificCounter operation : Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("increaseAndGetResourceInstanceSpecificCounter operation : Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	public Either<Integer, StorageOperationStatus> increaseAndGetResourceInstanceSpecificCounter(TitanVertex resourceInstanceVertex, GraphPropertiesDictionary counterType) {

		Either<Integer, StorageOperationStatus> result = null;

		VertexProperty<Object> vertexProperty = resourceInstanceVertex.property(counterType.getProperty());
		Integer counter = 0;
		if (vertexProperty.isPresent()) {
			if (vertexProperty.value() != null) {
				counter = (Integer) vertexProperty.value();
			}
		}
		counter++;
		resourceInstanceVertex.property(counterType.getProperty(), counter);

		result = Either.left(counter);
		return result;

	}

	@Override
	public Either<List<String>, StorageOperationStatus> getAllComponentInstancesNames(String serviceId, NodeTypeEnum nodeType, boolean inTransaction) {

		Either<List<String>, StorageOperationStatus> result = null;

		try {

			Either<List<String>, TitanOperationStatus> resInstancesOfService = getComponentInstancesNameOfService(serviceId, nodeType);

			log.debug("After fetching resource instances of service {}. result is {}", serviceId, resInstancesOfService);
			if (resInstancesOfService.isRight()) {
				TitanOperationStatus status = resInstancesOfService.right().value();
				if (status != TitanOperationStatus.NOT_FOUND) {
					log.error("Failed to find resource instances of service {}. status is {}", serviceId, status);
				}
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			List<String> names = resInstancesOfService.left().value();

			if (names == null || names.isEmpty()) {
				return Either.right(StorageOperationStatus.NOT_FOUND);
			}

			result = Either.left(names);

		} finally {
			if (false == inTransaction) {
				commitOrRollback(result);
			}
		}
		return result;
	}

	private Either<List<String>, TitanOperationStatus> getComponentInstancesNameOfService(String serviceId, NodeTypeEnum nodeType) {

		List<String> resourcesInstanseName = new ArrayList<String>();
		Either<List<ImmutablePair<ComponentInstanceData, GraphEdge>>, TitanOperationStatus> resourceInstancesRes = getAllComponentInstanceFromGraph(serviceId, nodeType, false);
		if (resourceInstancesRes.isRight()) {
			TitanOperationStatus status = resourceInstancesRes.right().value();
			log.debug("Resource instance was found under service {}. status is {}", serviceId, status);
			return Either.right(status);
		}

		List<ImmutablePair<ComponentInstanceData, GraphEdge>> resourceInstances = resourceInstancesRes.left().value();
		if (resourceInstances != null && false == resourceInstances.isEmpty()) {

			for (ImmutablePair<ComponentInstanceData, GraphEdge> immutablePair : resourceInstances) {
				ComponentInstanceData resourceInstanceData = immutablePair.getKey();
				log.debug("Going to fetch the relationships of resource instance {}", resourceInstanceData);
				resourcesInstanseName.add(resourceInstanceData.getComponentInstDataDefinition().getName());

			}
		}

		return Either.left(resourcesInstanseName);
	}

	@Override
	public Either<List<String>, StorageOperationStatus> getAllComponentInstancesNames(String componentId, NodeTypeEnum nodeType) {

		return getAllComponentInstancesNames(componentId, nodeType, false);
	}

	@Override
	public Either<ComponentInstance, StorageOperationStatus> getResourceInstanceById(String resourceId) {
		Either<ComponentInstanceData, TitanOperationStatus> resourceInstanceData = findResourceInstance(resourceId);

		if (resourceInstanceData.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(resourceInstanceData.right().value()));
		}

		return Either.left(new ComponentInstance(resourceInstanceData.left().value().getComponentInstDataDefinition()));

	}

	private StorageOperationStatus setCompInstDeploymentArtifactsFromGraph(Map<String, Map<String, ArtifactDefinition>> resourcesArtifacts, String uniqueId, ComponentInstance resourceInstance) {

		if (resourcesArtifacts.containsKey(uniqueId)) {
			resourceInstance.setDeploymentArtifacts(resourcesArtifacts.get(uniqueId));
			return StorageOperationStatus.OK;
		}

		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> result = artifactOperation.getArtifacts(uniqueId, NodeTypeEnum.Resource, true, ArtifactGroupTypeEnum.DEPLOYMENT.getType());
		if (result.isRight()) {
			StorageOperationStatus status = result.right().value();
			if (status != StorageOperationStatus.NOT_FOUND) {
				return status;
			} else {
				return StorageOperationStatus.OK;
			}
		}
		Map<String, ArtifactDefinition> artifacts = result.left().value();
		if (!artifacts.isEmpty()) {
			Map<String, ArtifactDefinition> tempArtifacts = new HashMap<String, ArtifactDefinition>(artifacts);
			for (Entry<String, ArtifactDefinition> artifact : artifacts.entrySet()) {
				if (!artifact.getValue().checkEsIdExist()) {
					tempArtifacts.remove(artifact.getKey());
				}
			}
			resourceInstance.setDeploymentArtifacts(tempArtifacts);
			resourcesArtifacts.put(uniqueId, tempArtifacts);
		}

		return StorageOperationStatus.OK;

	}

	private StorageOperationStatus setCompInstCapabilitiesFromGraph(Map<String, Map<String, CapabilityDefinition>> resourcesCapabilities, Component component, NodeTypeEnum compInstType, ComponentInstance resourceInstance,
			List<String> respourceDerivedList) {

		StorageOperationStatus status;
		ComponentOperation componentOperation = getComponentOperation(compInstType);
		Either<Map<String, List<CapabilityDefinition>>, TitanOperationStatus> eitherCapabilities = componentOperation.getCapabilities(component, compInstType, true);
		if (eitherCapabilities.isLeft()) {
			status = StorageOperationStatus.OK;
			Map<String, List<CapabilityDefinition>> capabilities = eitherCapabilities.left().value();
			if (capabilities != null && !capabilities.isEmpty()) {
				capabilities.forEach((type, list) -> {
					if (list != null && !list.isEmpty()) {
						list.forEach((capability) -> {
							// We want to set ownerId only for instances coming
							// from atomic resources, otherwise we don't want
							// to overwrite the existing ownerId of underlying
							// component instances
							if (isAtomicResource(component)) {
								capability.setOwnerId(resourceInstance.getUniqueId());
								capability.setOwnerName(resourceInstance.getName());
								capability.setCapabilitySources(respourceDerivedList);
							}
						});
					}
				});
				resourceInstance.setCapabilities(capabilities);
			}
		} else {
			status = StorageOperationStatus.GENERAL_ERROR;
		}
		return status;

	}

	private StorageOperationStatus setCompInstRequirementsFromGraph(Map<String, Map<String, RequirementDefinition>> resourcesReq, Component component, NodeTypeEnum compInstType, ComponentInstance resourceInstance) {
		StorageOperationStatus status;
		ComponentOperation componentOperation = getComponentOperation(compInstType);
		Either<Map<String, List<RequirementDefinition>>, TitanOperationStatus> eitherCapabilities = componentOperation.getRequirements(component, compInstType, true);
		if (eitherCapabilities.isLeft()) {
			status = StorageOperationStatus.OK;
			Map<String, List<RequirementDefinition>> requirements = eitherCapabilities.left().value();
			if (requirements != null && !requirements.isEmpty()) {
				// We want to set ownerId only for instances coming from atomic
				// resources, otherwise we don't want
				// to overwrite the existing ownerId of underlying component
				// instances
				if (isAtomicResource(component)) {
					requirements.forEach((type, list) -> {
						if (list != null && !list.isEmpty()) {
							list.forEach((requirement) -> {
								requirement.setOwnerId(resourceInstance.getUniqueId());
								requirement.setOwnerName(resourceInstance.getName());
							});
						}
					});
				}
				resourceInstance.setRequirements(requirements);
			}
		} else {
			status = StorageOperationStatus.GENERAL_ERROR;
		}
		return status;
	}

	@Override
	public Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> getCapabilities(ComponentInstance compInstance, NodeTypeEnum nodeTypeEnum) {
		DataNodeCollector<CapabilityData> collector = () -> titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeTypeEnum), compInstance.getUniqueId(), GraphEdgeLabels.CALCULATED_CAPABILITY, NodeTypeEnum.Capability,
				CapabilityData.class);

		return getDataFromGraph(collector);
	}

	@Override
	public Either<List<ImmutablePair<RequirementData, GraphEdge>>, TitanOperationStatus> getRequirements(ComponentInstance compInstance, NodeTypeEnum nodeTypeEnum) {

		DataNodeCollector<RequirementData> collector = () -> titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeTypeEnum), compInstance.getUniqueId(), GraphEdgeLabels.CALCULATED_REQUIREMENT, NodeTypeEnum.Requirement,
				RequirementData.class);

		return getDataFromGraph(collector);

	}

	@Override
	public Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> getFulfilledCapabilities(ComponentInstance compInstance, NodeTypeEnum nodeTypeEnum) {
		DataNodeCollector<CapabilityData> collector = () -> titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeTypeEnum), compInstance.getUniqueId(), GraphEdgeLabels.CALCULATED_CAPABILITY_FULLFILLED, NodeTypeEnum.Capability,
				CapabilityData.class);

		return getDataFromGraph(collector);
	}

	@Override
	public Either<List<ImmutablePair<RequirementData, GraphEdge>>, TitanOperationStatus> getFulfilledRequirements(ComponentInstance compInstance, NodeTypeEnum nodeTypeEnum) {

		DataNodeCollector<RequirementData> collector = () -> titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeTypeEnum), compInstance.getUniqueId(), GraphEdgeLabels.CALCULATED_REQUIREMENT_FULLFILLED, NodeTypeEnum.Requirement,
				RequirementData.class);

		return getDataFromGraph(collector);

	}

	public Either<Boolean, StorageOperationStatus> isAvailableRequirement(ComponentInstance fromResInstance, RequirementAndRelationshipPair relationPair) {
		Either<TitanVertex, TitanOperationStatus> fromRi = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), fromResInstance.getUniqueId());
		if (fromRi.isRight()) {
			log.debug("Failed to fetch component instance {}  error {}", fromResInstance.getUniqueId(), fromRi.right().value());
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}
		Iterator<Edge> edgeIter = fromRi.left().value().edges(Direction.OUT, GraphEdgeLabels.CALCULATED_REQUIREMENT.name());
		if (edgeIter == null || !edgeIter.hasNext()) {
			log.debug("No available CALCULATED_REQUIREMENT edges. All full filled for RI {}", fromResInstance.getUniqueId());
			return Either.left(false);
		}
		boolean exist = false;
		while (edgeIter.hasNext()) {
			Edge edge = edgeIter.next();
			TitanVertex reqVertex = (TitanVertex) edge.inVertex();
			String reqId = (String) titanGenericDao.getProperty(reqVertex, UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Requirement));
			if (reqId.equals(relationPair.getRequirementUid())) {
				String ownerIdOnEdge = (String) edge.value(GraphEdgePropertiesDictionary.OWNER_ID.getProperty());
				if (ownerIdOnEdge.equals(relationPair.getRequirementOwnerId())) {
					String leftOccurrences = (String) edge.value(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty());
					if (leftOccurrences != null && !leftOccurrences.equals(RequirementDataDefinition.MAX_OCCURRENCES)) {
						Integer leftIntValue = Integer.parseInt(leftOccurrences);
						if (leftIntValue > 0) {
							exist = true;
						}
					} else {
						exist = true;
					}
					break;
				}
			}
		}
		return Either.left(exist);
	}

	public Either<Boolean, StorageOperationStatus> isAvailableCapabilty(ComponentInstance toResInstance, RequirementAndRelationshipPair relationPair) {
		Either<TitanVertex, TitanOperationStatus> fromRi = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), toResInstance.getUniqueId());
		if (fromRi.isRight()) {
			log.debug("Failed to fetch component instance {} error {}", toResInstance.getUniqueId(), fromRi.right().value());
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}
		Iterator<Edge> edgeIter = fromRi.left().value().edges(Direction.OUT, GraphEdgeLabels.CALCULATED_CAPABILITY.name());
		if (edgeIter == null || !edgeIter.hasNext()) {
			log.debug("No available CALCULATED_CAPABILITY edges. All full filled for RI {}", toResInstance.getUniqueId());
			return Either.right(StorageOperationStatus.NOT_FOUND);
		}
		boolean exist = false;
		while (edgeIter.hasNext()) {
			Edge edge = edgeIter.next();
			TitanVertex reqVertex = (TitanVertex) edge.inVertex();
			String capId = (String) titanGenericDao.getProperty(reqVertex, UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Capability));
			if (capId.equals(relationPair.getCapabilityUid())) {
				String ownerIdOnEdge = (String) edge.value(GraphEdgePropertiesDictionary.OWNER_ID.getProperty());
				if (ownerIdOnEdge.equals(relationPair.getCapabilityOwnerId())) {
					String leftOccurrences = (String) edge.value(GraphEdgePropertiesDictionary.LEFT_OCCURRENCES.getProperty());
					if (leftOccurrences != null && !leftOccurrences.equals(CapabilityDataDefinition.MAX_OCCURRENCES)) {
						Integer leftIntValue = Integer.parseInt(leftOccurrences);
						if (leftIntValue > 0) {
							exist = true;
						}
					} else {
						exist = true;
					}
					break;
				}
			}
		}
		return Either.left(exist);
	}

	interface DataNodeCollector<Data> {
		Either<List<ImmutablePair<Data, GraphEdge>>, TitanOperationStatus> getDataNodes();
	}

	public <Data> Either<List<ImmutablePair<Data, GraphEdge>>, TitanOperationStatus> getDataFromGraph(DataNodeCollector<Data> dataCollector) {
		Either<List<ImmutablePair<Data, GraphEdge>>, TitanOperationStatus> eitherRet;

		Either<List<ImmutablePair<Data, GraphEdge>>, TitanOperationStatus> childrenNodes = dataCollector.getDataNodes();

		if (childrenNodes.isLeft()) {
			List<ImmutablePair<Data, GraphEdge>> collectedData = childrenNodes.left().value().stream().map(element -> new ImmutablePair<Data, GraphEdge>(element.getLeft(), element.getRight())).collect(Collectors.toList());
			eitherRet = Either.left(collectedData);
		} else {
			eitherRet = Either.right(childrenNodes.right().value());
		}
		return eitherRet;
	}

	public ComponentOperation getComponentOperation(NodeTypeEnum componentType) {
		if (NodeTypeEnum.Service == componentType) {
			return serviceOperation;
		} else if (NodeTypeEnum.Resource == componentType) {
			return resourceOperation;
		}
		return null;
	}

	private boolean isAtomicResource(Component component) {
		// true if component is of type VL/CP/VFC
		boolean isFromAtomicResource = (component.getComponentType() == ComponentTypeEnum.RESOURCE && ((Resource) component).getResourceType() != ResourceTypeEnum.VF);
		return isFromAtomicResource;
	}

	private StorageOperationStatus cloneResourceInstanceAttributeValues(ComponentInstance createdInstance, ComponentInstance resourceInstance) {
		Wrapper<StorageOperationStatus> storageStatusWrapper = new Wrapper<>();
		Wrapper<List<ComponentInstanceProperty>> compInstanceAttList = new Wrapper<>();

		findAllAttributesOfResourceInstance(resourceInstance, compInstanceAttList, storageStatusWrapper);

		if (storageStatusWrapper.isEmpty()) {
			validateListNotEmpty(storageStatusWrapper, compInstanceAttList.getInnerElement());
		}

		if (storageStatusWrapper.isEmpty()) {
			List<ComponentInstanceProperty> attributesOnInstance = compInstanceAttList.getInnerElement();
			for (int i = 0; i < attributesOnInstance.size() && storageStatusWrapper.isEmpty(); i++) {
				cloneSingleAttributeOnResourceInstance(createdInstance, attributesOnInstance.get(i), storageStatusWrapper);
			}
		}

		StorageOperationStatus result = storageStatusWrapper.isEmpty() ? StorageOperationStatus.OK : storageStatusWrapper.getInnerElement();
		return result;

	}

	private StorageOperationStatus cloneResourceInstanceAttributeValues(TitanVertex createdInstanceVertex, ComponentInstance resourceInstance, String instanceId) {
		Wrapper<StorageOperationStatus> storageStatusWrapper = new Wrapper<>();
		Wrapper<List<ComponentInstanceProperty>> compInstanceAttList = new Wrapper<>();

		findAllAttributesOfResourceInstance(resourceInstance, compInstanceAttList, storageStatusWrapper);

		if (storageStatusWrapper.isEmpty()) {
			validateListNotEmpty(storageStatusWrapper, compInstanceAttList.getInnerElement());
		}

		if (storageStatusWrapper.isEmpty()) {
			List<ComponentInstanceProperty> attributesOnInstance = compInstanceAttList.getInnerElement();
			for (int i = 0; i < attributesOnInstance.size() && storageStatusWrapper.isEmpty(); i++) {
				StorageOperationStatus result = cloneSingleAttributeOnResourceInstance(createdInstanceVertex, attributesOnInstance.get(i), instanceId);
				if (result != StorageOperationStatus.OK) {
					log.trace("Failed to clone attribute for instance {} error {}", instanceId, result);
					return result;
				}
			}
		}

		StorageOperationStatus result = storageStatusWrapper.isEmpty() ? StorageOperationStatus.OK : storageStatusWrapper.getInnerElement();
		return result;

	}

	private <T> void validateListNotEmpty(Wrapper<StorageOperationStatus> storageStatusWrapper, List<T> attributesOnInstance) {
		if (attributesOnInstance == null || attributesOnInstance.isEmpty() == true) {
			storageStatusWrapper.setInnerElement(StorageOperationStatus.OK);
		}
	}

	private void findAllAttributesOfResourceInstance(ComponentInstance resourceInstance, Wrapper<List<ComponentInstanceProperty>> compInstanceAttList, Wrapper<StorageOperationStatus> storageStatusWrapper) {

		Either<List<ComponentInstanceProperty>, TitanOperationStatus> allAttributes = attributeOperation.getAllAttributesOfResourceInstance(resourceInstance);
		if (allAttributes.isRight()) {
			TitanOperationStatus status = allAttributes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
			}
			StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
			storageStatusWrapper.setInnerElement(storageStatus);
		} else {
			compInstanceAttList.setInnerElement(allAttributes.left().value());
		}
	}

	private void cloneSingleAttributeOnResourceInstance(ComponentInstance createdInstance, ComponentInstanceProperty attribute, Wrapper<StorageOperationStatus> storageStatusWrapper) {
		// Only if valueUniqueId is not empty, then its belongs to the
		// instance
		if (attribute.getValueUniqueUid() != null) {
			attribute.setValueUniqueUid(null);
			Either<Integer, StorageOperationStatus> counterRes = increaseAndGetResourceInstanceSpecificCounter(createdInstance.getUniqueId(), GraphPropertiesDictionary.ATTRIBUTE_COUNTER, true);
			if (counterRes.isRight()) {
				storageStatusWrapper.setInnerElement(counterRes.right().value());
			} else {
				Either<AttributeValueData, TitanOperationStatus> addAttributeToResourceInstance = addAttributeToResourceInstance(attribute, createdInstance.getUniqueId(), counterRes.left().value());

				if (addAttributeToResourceInstance.isRight()) {
					TitanOperationStatus status = addAttributeToResourceInstance.right().value();
					StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
					storageStatusWrapper.setInnerElement(storageStatus);
				}
			}
		}

	}

	private StorageOperationStatus cloneSingleAttributeOnResourceInstance(TitanVertex createdInstanceVertex, ComponentInstanceProperty attribute, String instanceId) {
		// Only if valueUniqueId is not empty, then its belongs to the
		// instance
		if (attribute.getValueUniqueUid() != null) {
			attribute.setValueUniqueUid(null);
			Either<Integer, StorageOperationStatus> counterRes = increaseAndGetResourceInstanceSpecificCounter(createdInstanceVertex, GraphPropertiesDictionary.ATTRIBUTE_COUNTER);
			if (counterRes.isRight()) {
				return counterRes.right().value();
			} else {
				Either<AttributeValueData, TitanOperationStatus> addAttributeToResourceInstance = addAttributeToResourceInstance(attribute, instanceId, counterRes.left().value());

				if (addAttributeToResourceInstance.isRight()) {
					TitanOperationStatus status = addAttributeToResourceInstance.right().value();
					StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
					return storageStatus;
				}
			}
		}
		return StorageOperationStatus.OK;

	}

	private void connectAttValueDataToComponentInstanceData(Wrapper<TitanOperationStatus> errorWrapper, ComponentInstanceData compIns, AttributeValueData attValueData) {

		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(compIns, attValueData, GraphEdgeLabels.ATTRIBUTE_VALUE, null);

		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			errorWrapper.setInnerElement(operationStatus);
			BeEcompErrorManager.getInstance().logInternalFlowError("connectAttValueDataToComponentInstanceData",
					"Failed to associate resource instance " + compIns.getUniqueId() + " attribute value " + attValueData.getUniqueId() + " in graph. status is " + operationStatus, ErrorSeverity.ERROR);
		}
	}

	private void connectInputValueDataToComponentInstanceData(Wrapper<TitanOperationStatus> errorWrapper, ComponentInstanceData compIns, InputValueData attValueData) {

		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(compIns, attValueData, GraphEdgeLabels.INPUT_VALUE, null);

		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			errorWrapper.setInnerElement(operationStatus);
			BeEcompErrorManager.getInstance().logInternalFlowError("connectInputValueDataToComponentInstanceData",
					"Failed to associate resource instance " + compIns.getUniqueId() + " input value " + attValueData.getUniqueId() + " in graph. status is " + operationStatus, ErrorSeverity.ERROR);
		}
	}

	private void connectAttValueDataToAttData(Wrapper<TitanOperationStatus> errorWrapper, AttributeData attData, AttributeValueData attValueData) {

		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(attValueData, attData, GraphEdgeLabels.ATTRIBUTE_IMPL, null);

		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			BeEcompErrorManager.getInstance().logInternalFlowError("connectAttValueDataToAttData",
					"Failed to associate attribute value " + attValueData.getUniqueId() + " to attribute " + attData.getUniqueId() + " in graph. status is " + operationStatus, ErrorSeverity.ERROR);

			errorWrapper.setInnerElement(operationStatus);
		}
	}

	private void connectInputValueDataToInputData(Wrapper<TitanOperationStatus> errorWrapper, InputsData attData, InputValueData attValueData) {

		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(attValueData, attData, GraphEdgeLabels.INPUT_IMPL, null);

		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			BeEcompErrorManager.getInstance().logInternalFlowError("connectInputValueDataToInputData", "Failed to associate input value " + attValueData.getUniqueId() + " to input " + attData.getUniqueId() + " in graph. status is " + operationStatus,
					ErrorSeverity.ERROR);

			errorWrapper.setInnerElement(operationStatus);
		}
	}

	private void createAttributeValueDataNode(ComponentInstanceProperty attributeInstanceProperty, Integer index, Wrapper<TitanOperationStatus> errorWrapper, ComponentInstanceData resourceInstanceData,
			Wrapper<AttributeValueData> attValueDataWrapper) {
		String valueUniqueUid = attributeInstanceProperty.getValueUniqueUid();
		if (valueUniqueUid == null) {

			String attValueDatauniqueId = UniqueIdBuilder.buildResourceInstanceAttributeValueUid(resourceInstanceData.getUniqueId(), index);
			AttributeValueData attributeValueData = buildAttributeValueDataFromComponentInstanceAttribute(attributeInstanceProperty, attValueDatauniqueId);

			log.debug("Before adding attribute value to graph {}", attributeValueData);
			Either<AttributeValueData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(attributeValueData, AttributeValueData.class);
			log.debug("After adding attribute value to graph {}", attributeValueData);

			if (createNodeResult.isRight()) {
				TitanOperationStatus operationStatus = createNodeResult.right().value();
				errorWrapper.setInnerElement(operationStatus);
			} else {
				attValueDataWrapper.setInnerElement(createNodeResult.left().value());
			}

		} else {
			BeEcompErrorManager.getInstance().logInternalFlowError("CreateAttributeValueDataNode", "attribute value already exists.", ErrorSeverity.ERROR);
			errorWrapper.setInnerElement(TitanOperationStatus.ALREADY_EXIST);
		}
	}

	/*
	 * private void createInputValueDataNode(ComponentInstanceInput inputInstanceProperty, Integer index, Wrapper<TitanOperationStatus> errorWrapper, ComponentInstanceData resourceInstanceData, Wrapper<AttributeValueData> attValueDataWrapper) {
	 * String valueUniqueUid = inputInstanceProperty.getValueUniqueUid(); if (valueUniqueUid == null) {
	 * 
	 * String attValueDatauniqueId = UniqueIdBuilder.buildResourceInstanceInputValueUid(resourceInstanceData. getUniqueId(), index); AttributeValueData attributeValueData = buildAttributeValueDataFromComponentInstanceAttribute( inputInstanceProperty,
	 * attValueDatauniqueId);
	 * 
	 * log.debug("Before adding attribute value to graph {}", attributeValueData); Either<AttributeValueData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(attributeValueData, AttributeValueData.class);
	 * log.debug("After adding attribute value to graph {}", attributeValueData);
	 * 
	 * if (createNodeResult.isRight()) { TitanOperationStatus operationStatus = createNodeResult.right().value(); errorWrapper.setInnerElement(operationStatus); } else { attValueDataWrapper.setInnerElement(createNodeResult.left().value()); }
	 * 
	 * } else { BeEcompErrorManager.getInstance().logInternalFlowError( "CreateAttributeValueDataNode", "attribute value already exists.", ErrorSeverity.ERROR); errorWrapper.setInnerElement(TitanOperationStatus.ALREADY_EXIST); } }
	 */

	private AttributeValueData buildAttributeValueDataFromComponentInstanceAttribute(ComponentInstanceProperty resourceInstanceAttribute, String uniqueId) {
		AttributeValueData attributeValueData = new AttributeValueData();
		attributeValueData.setUniqueId(uniqueId);
		attributeValueData.setHidden(resourceInstanceAttribute.isHidden());
		attributeValueData.setValue(resourceInstanceAttribute.getValue());
		attributeValueData.setType(resourceInstanceAttribute.getType());
		long currentTimeMillis = System.currentTimeMillis();
		attributeValueData.setCreationTime(currentTimeMillis);
		attributeValueData.setModificationTime(currentTimeMillis);
		return attributeValueData;
	}

	private InputValueData buildAttributeValueDataFromComponentInstanceAttribute(ComponentInstanceInput resourceInstanceInput, String uniqueId) {
		InputValueData inputValueData = new InputValueData();
		inputValueData.setUniqueId(uniqueId);
		inputValueData.setHidden(resourceInstanceInput.isHidden());
		inputValueData.setValue(resourceInstanceInput.getValue());
		inputValueData.setType(resourceInstanceInput.getType());
		long currentTimeMillis = System.currentTimeMillis();
		inputValueData.setCreationTime(currentTimeMillis);
		inputValueData.setModificationTime(currentTimeMillis);
		return inputValueData;
	}

	private StorageOperationStatus cloneResourceInstancePropertyValues(ComponentInstance toResourceInstance, ComponentInstance fromResourceInstance) {

		Either<List<ComponentInstanceProperty>, TitanOperationStatus> allProperties = propertyOperation.getAllPropertiesOfResourceInstanceOnlyPropertyDefId(fromResourceInstance.getUniqueId());
		if (allProperties.isRight()) {
			TitanOperationStatus status = allProperties.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
			}
			StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
			return storageStatus;
		}

		List<ComponentInstanceProperty> propertiesOnInstance = allProperties.left().value();
		if (propertiesOnInstance == null || propertiesOnInstance.isEmpty() == true) {
			return StorageOperationStatus.OK;
		}

		for (ComponentInstanceProperty property : propertiesOnInstance) {

			// Only if valueUniqueId is not empty, then its belongs to the
			// instance
			if (property.getValueUniqueUid() != null) {
				property.setValueUniqueUid(null);
				List<PropertyRule> rules = property.getRules();
				if (rules != null) {
					for (PropertyRule propertyRule : rules) {
						propertyRule.replaceFirstToken(toResourceInstance.getUniqueId());
					}
				}

			} else {
				continue;
			}

			String resourceInstanceId = toResourceInstance.getUniqueId();

			Either<Integer, StorageOperationStatus> counterRes = this.increaseAndGetResourceInstanceSpecificCounter(resourceInstanceId, GraphPropertiesDictionary.PROPERTY_COUNTER, true);

			if (counterRes.isRight()) {
				log.debug("increaseAndGetResourcePropertyCounter failed resource instance {} property {}", resourceInstanceId, property);
				StorageOperationStatus status = counterRes.right().value();
				return status;
			}
			Integer index = counterRes.left().value();

			Either<PropertyValueData, TitanOperationStatus> addPropertyToResourceInstance = this.addPropertyToResourceInstance(property, toResourceInstance.getUniqueId(), false, index);

			if (addPropertyToResourceInstance.isRight()) {
				TitanOperationStatus status = addPropertyToResourceInstance.right().value();
				StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
				return storageStatus;
			}
		}

		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus cloneResourceInstancePropertyValues(TitanVertex toResourceInstance, ComponentInstance fromResourceInstance, Map<String, List<ComponentInstanceProperty>> inputsPropMap, Resource newResource) {

		String riId = (String) titanGenericDao.getProperty(toResourceInstance, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
		Either<List<ComponentInstanceProperty>, TitanOperationStatus> allProperties = propertyOperation.getAllPropertiesOfResourceInstanceOnlyPropertyDefId(fromResourceInstance.getUniqueId());
		List<InputDefinition> newInputs = newResource.getInputs();
		//
		if (allProperties.isRight()) {
			TitanOperationStatus status = allProperties.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
			}
			StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
			return storageStatus;
		}

		List<ComponentInstanceProperty> propertiesOnInstance = allProperties.left().value();
		if (propertiesOnInstance == null || propertiesOnInstance.isEmpty() == true) {
			return StorageOperationStatus.OK;
		}

		for (ComponentInstanceProperty property : propertiesOnInstance) {

			// Only if valueUniqueId is not empty, then its belongs to the
			// instance
			if (property.getValueUniqueUid() != null) {
				property.setValueUniqueUid(null);
				List<PropertyRule> rules = property.getRules();
				if (rules != null) {
					for (PropertyRule propertyRule : rules) {
						propertyRule.replaceFirstToken(riId);
					}
				}

			} else {
				continue;
			}

			String resourceInstanceId = riId;

			Either<Integer, StorageOperationStatus> counterRes = this.increaseAndGetResourceInstanceSpecificCounter(toResourceInstance, GraphPropertiesDictionary.PROPERTY_COUNTER);

			if (counterRes.isRight()) {
				log.debug("increaseAndGetResourcePropertyCounter failed resource instance {} property {}", resourceInstanceId, property);
				StorageOperationStatus status = counterRes.right().value();
				return status;
			}
			Integer index = counterRes.left().value();

			Either<ComponentInstanceProperty, TitanOperationStatus> addPropertyToResourceInstance = this.addPropertyToResourceInstance(property, toResourceInstance, false, index, resourceInstanceId);

			if (addPropertyToResourceInstance.isRight() && addPropertyToResourceInstance.right().value() != TitanOperationStatus.OK) {
				StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(addPropertyToResourceInstance.right().value());
				return storageStatus;
			}
			if (addPropertyToResourceInstance.isLeft()) {
				ComponentInstanceProperty newProp = addPropertyToResourceInstance.left().value();
				Set<String> inputsKey = inputsPropMap.keySet();
				String inputToAssName = null;
				GetInputValueDataDefinition getInputInfo = null;
				for (String inputName : inputsKey) {
					List<ComponentInstanceProperty> propsList = inputsPropMap.get(inputName);
					Optional<ComponentInstanceProperty> op = propsList.stream().filter(p -> p.getUniqueId().equals(property.getUniqueId())).findAny();
					if (op.isPresent()) {
						ComponentInstanceProperty inpProp = op.get();
						getInputInfo = new GetInputValueDataDefinition();
						getInputInfo.setPropName(inpProp.getName());
						getInputInfo.setInputName(inputName);
						inputToAssName = inputName;
						break;
					}

				}
				if (inputToAssName != null) {
					for (InputDefinition input1 : newInputs) {
						if (input1.getName().equals(inputToAssName)) {
							this.inputOperation.associatePropertyToInput(riId, input1.getUniqueId(), newProp, getInputInfo);
							break;
						}
					}
				}

			}
		}

		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus cloneResourceInstanceInputsValues(ComponentInstance toResourceInstance, ComponentInstance fromResourceInstance, Component comonentTo, Map<String, List<ComponentInstanceInput>> inputsValuesMap) {

		Either<List<ComponentInstanceInput>, TitanOperationStatus> allProperties = inputOperation.getAllInputsOfResourceInstanceOnlyInputDefId(fromResourceInstance.getUniqueId());
		if (allProperties.isRight()) {
			TitanOperationStatus status = allProperties.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
			}
			StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
			return storageStatus;
		}

		List<ComponentInstanceInput> propertiesOnInstance = allProperties.left().value();
		if (propertiesOnInstance == null || propertiesOnInstance.isEmpty() == true) {
			return StorageOperationStatus.OK;
		}
		List<InputDefinition> newInputs = comonentTo.getInputs();

		for (ComponentInstanceInput property : propertiesOnInstance) {

			List<InputDefinition> inputToAss = new ArrayList<InputDefinition>();
			if (newInputs != null && !inputsValuesMap.isEmpty()) {

				Set<String> inputsName = inputsValuesMap.keySet();
				for (String name : inputsName) {
					List<ComponentInstanceInput> inputsValue = inputsValuesMap.get(name);
					if (inputsValue != null) {
						Optional<ComponentInstanceInput> op = inputsValue.stream().filter(p -> p.getValueUniqueUid().equals(property.getValueUniqueUid())).findAny();
						if (op.isPresent()) {
							Optional<InputDefinition> optional = newInputs.stream().filter(e -> e.getName().equals(name)).findAny();
							if (optional.isPresent()) {
								inputToAss.add(optional.get());
							}
						}
					}
				}
			}

			// Only if valueUniqueId is not empty, then its belongs to the
			// instance
			if (property.getValueUniqueUid() != null) {
				property.setValueUniqueUid(null);
				List<PropertyRule> rules = property.getRules();
				if (rules != null) {
					for (PropertyRule propertyRule : rules) {
						propertyRule.replaceFirstToken(toResourceInstance.getUniqueId());
					}
				}

			} else {
				continue;
			}

			String resourceInstanceId = toResourceInstance.getUniqueId();

			Either<Integer, StorageOperationStatus> counterRes = this.increaseAndGetResourceInstanceSpecificCounter(resourceInstanceId, GraphPropertiesDictionary.INPUT_COUNTER, true);

			if (counterRes.isRight()) {
				log.debug("increaseAndGetResourcePropertyCounter failed resource instance {} property {}", resourceInstanceId, property);
				StorageOperationStatus status = counterRes.right().value();
				return status;
			}
			Integer index = counterRes.left().value();

			Either<InputValueData, TitanOperationStatus> addPropertyToResourceInstance = this.addInputToResourceInstance(property, toResourceInstance.getUniqueId(), index);

			if (addPropertyToResourceInstance.isRight()) {
				TitanOperationStatus status = addPropertyToResourceInstance.right().value();
				StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
				return storageStatus;
			}

			for (InputDefinition input : inputToAss) {
				Map<String, Object> props = new HashMap<String, Object>();
				props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), input.getName());
				props.put(GraphEdgePropertiesDictionary.OWNER_ID.getProperty(), toResourceInstance.getUniqueId());

				GraphNode inputData = new UniqueIdData(NodeTypeEnum.Input, input.getUniqueId());
				GraphNode propertyData = new UniqueIdData(NodeTypeEnum.InputValue, addPropertyToResourceInstance.left().value().getUniqueId());

				Either<GraphRelation, TitanOperationStatus> addPropRefResult = titanGenericDao.createRelation(inputData, propertyData, GraphEdgeLabels.GET_INPUT, props);

				if (addPropRefResult.isRight()) {
					TitanOperationStatus status = addPropRefResult.right().value();
					log.debug("Failed to associate input {}  to input value {} in graph. Status is {}", input.getUniqueId(), propertyData.getUniqueId(), status);

					return DaoStatusConverter.convertTitanStatusToStorageStatus(status);
				}
			}
		}

		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus cloneResourceInstanceInputsValues(TitanVertex toResourceInstanceVertex, ComponentInstance fromResourceInstance, String instanceId, Resource newResource, Map<String, List<ComponentInstanceInput>> inputsValuesMap) {

		Either<List<ComponentInstanceInput>, TitanOperationStatus> allProperties = inputOperation.getAllInputsOfResourceInstanceOnlyInputDefId(fromResourceInstance.getUniqueId());
		if (allProperties.isRight()) {
			TitanOperationStatus status = allProperties.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
			}
			StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
			return storageStatus;
		}

		List<ComponentInstanceInput> propertiesOnInstance = allProperties.left().value();
		if (propertiesOnInstance == null || propertiesOnInstance.isEmpty() == true) {
			return StorageOperationStatus.OK;
		}

		for (ComponentInstanceInput property : propertiesOnInstance) {

			// Only if valueUniqueId is not empty, then its belongs to the
			// instance
			if (property.getValueUniqueUid() != null) {
				property.setValueUniqueUid(null);
				List<PropertyRule> rules = property.getRules();
				if (rules != null) {
					for (PropertyRule propertyRule : rules) {
						propertyRule.replaceFirstToken(instanceId);
					}
				}

			} else {
				continue;
			}

			String resourceInstanceId = instanceId;

			Either<Integer, StorageOperationStatus> counterRes = this.increaseAndGetResourceInstanceSpecificCounter(toResourceInstanceVertex, GraphPropertiesDictionary.INPUT_COUNTER);

			if (counterRes.isRight()) {
				log.debug("increaseAndGetResourcePropertyCounter failed resource instance {} property {}", resourceInstanceId, property);
				StorageOperationStatus status = counterRes.right().value();
				return status;
			}
			Integer index = counterRes.left().value();

			Either<InputValueData, TitanOperationStatus> addPropertyToResourceInstance = this.addInputToResourceInstance(property, resourceInstanceId, index);

			if (addPropertyToResourceInstance.isRight()) {
				TitanOperationStatus status = addPropertyToResourceInstance.right().value();
				StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
				return storageStatus;
			}
		}

		return StorageOperationStatus.OK;
	}

	public Either<ComponentInstanceProperty, StorageOperationStatus> updatePropertyValueInResourceInstance(ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId, boolean inTransaction) {

		Either<ComponentInstanceProperty, StorageOperationStatus> result = null;

		try {
			// TODO: verify validUniqueId exists
			Either<PropertyValueData, TitanOperationStatus> eitherStatus = this.updatePropertyOfResourceInstance(resourceInstanceProperty, resourceInstanceId, true);

			if (eitherStatus.isRight()) {
				log.error("Failed to add property value {} to resource instance {} in Graph. status is {}", resourceInstanceProperty, resourceInstanceId, eitherStatus.right().value().name());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherStatus.right().value()));
				return result;
			} else {
				PropertyValueData propertyValueData = eitherStatus.left().value();

				ComponentInstanceProperty propertyValueResult = propertyOperation.buildResourceInstanceProperty(propertyValueData, resourceInstanceProperty);

				log.debug("The returned ResourceInstanceProperty is {}", propertyValueResult);

				Either<String, TitanOperationStatus> findDefaultValue = propertyOperation.findDefaultValueFromSecondPosition(resourceInstanceProperty.getPath(), propertyValueData.getUniqueId(), resourceInstanceProperty.getDefaultValue());
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

	@Override
	public Either<AttributeValueData, TitanOperationStatus> createOrUpdateAttributeOfResourceInstance(ComponentInstanceProperty attributeInstanceProperty, String resourceInstanceId) {
		Either<AttributeValueData, TitanOperationStatus> result;
		// Create
		if (attributeInstanceProperty.getValueUniqueUid() == null) {
			Either<Integer, StorageOperationStatus> counterRes = increaseAndGetResourceInstanceSpecificCounter(resourceInstanceId, GraphPropertiesDictionary.ATTRIBUTE_COUNTER, true);
			if (counterRes.isRight()) {
				BeEcompErrorManager.getInstance().logInternalFlowError("createOrUpdateAttributeOfResourceInstance", "Failed to get AttributeValueData Counter", ErrorSeverity.ERROR);
				result = Either.right(TitanOperationStatus.GENERAL_ERROR);

			} else {
				result = addAttributeToResourceInstance(attributeInstanceProperty, resourceInstanceId, counterRes.left().value());
			}
		}
		// Update
		else {
			result = updateAttributeOfResourceInstance(attributeInstanceProperty, resourceInstanceId);
		}
		return result;
	}

	/**
	 * update value of attribute on resource instance
	 * 
	 * @param resourceInstanceAttribute
	 * @param resourceInstanceId
	 * @return
	 */
	private Either<AttributeValueData, TitanOperationStatus> updateAttributeOfResourceInstance(ComponentInstanceProperty resourceInstanceAttribute, String resourceInstanceId) {

		Either<AttributeValueData, TitanOperationStatus> result = null;
		Wrapper<TitanOperationStatus> errorWrapper = new Wrapper<>();
		UpdateDataContainer<AttributeData, AttributeValueData> updateDataContainer = new UpdateDataContainer<>(GraphEdgeLabels.ATTRIBUTE_IMPL, (() -> AttributeData.class), (() -> AttributeValueData.class), NodeTypeEnum.Attribute,
				NodeTypeEnum.AttributeValue);
		preUpdateElementOfResourceInstanceValidations(updateDataContainer, resourceInstanceAttribute, resourceInstanceId, errorWrapper);
		if (errorWrapper.isEmpty()) {
			AttributeValueData attributeValueData = updateDataContainer.getValueDataWrapper().getInnerElement();
			attributeValueData.setHidden(resourceInstanceAttribute.isHidden());
			attributeValueData.setValue(resourceInstanceAttribute.getValue());
			Either<AttributeValueData, TitanOperationStatus> updateRes = titanGenericDao.updateNode(attributeValueData, AttributeValueData.class);
			if (updateRes.isRight()) {
				TitanOperationStatus status = updateRes.right().value();
				errorWrapper.setInnerElement(status);
			} else {
				result = Either.left(updateRes.left().value());
			}
		}
		if (!errorWrapper.isEmpty()) {
			result = Either.right(errorWrapper.getInnerElement());
		}
		return result;

	}

	private Either<AttributeValueData, TitanOperationStatus> addAttributeToResourceInstance(ComponentInstanceProperty attributeInstanceProperty, String resourceInstanceId, Integer index) {
		Wrapper<TitanOperationStatus> errorWrapper = new Wrapper<>();
		Wrapper<ComponentInstanceData> compInsWrapper = new Wrapper<>();
		Wrapper<AttributeData> attDataWrapper = new Wrapper<>();
		Wrapper<AttributeValueData> attValueDataWrapper = new Wrapper<>();

		// Verify RI Exist
		validateRIExist(resourceInstanceId, compInsWrapper, errorWrapper);

		if (errorWrapper.isEmpty()) {
			// Verify Attribute Exist
			validateElementExistInGraph(attributeInstanceProperty.getUniqueId(), NodeTypeEnum.Attribute, () -> AttributeData.class, attDataWrapper, errorWrapper);
		}
		if (errorWrapper.isEmpty()) {
			// Create AttributeValueData that is connected to RI
			createAttributeValueDataNode(attributeInstanceProperty, index, errorWrapper, compInsWrapper.getInnerElement(), attValueDataWrapper);
		}
		if (errorWrapper.isEmpty()) {
			// Connect AttributeValueData (Att on RI) to AttData (Att on
			// Resource)
			connectAttValueDataToAttData(errorWrapper, attDataWrapper.getInnerElement(), attValueDataWrapper.getInnerElement());
		}
		if (errorWrapper.isEmpty()) {
			// Connect AttributeValueData to RI
			connectAttValueDataToComponentInstanceData(errorWrapper, compInsWrapper.getInnerElement(), attValueDataWrapper.getInnerElement());
		}

		if (errorWrapper.isEmpty()) {
			return Either.left(attValueDataWrapper.getInnerElement());
		} else {
			return Either.right(errorWrapper.getInnerElement());
		}

	}

	/**
	 * update value of attribute on resource instance
	 * 
	 * @param resourceInstanceProerty
	 * @param resourceInstanceId
	 * @return
	 */
	public Either<PropertyValueData, TitanOperationStatus> updatePropertyOfResourceInstance(ComponentInstanceProperty resourceInstanceProerty, String resourceInstanceId, boolean isValidate) {

		Wrapper<TitanOperationStatus> errorWrapper = new Wrapper<>();
		UpdateDataContainer<PropertyData, PropertyValueData> updateDataContainer = new UpdateDataContainer<>(GraphEdgeLabels.PROPERTY_IMPL, (() -> PropertyData.class), (() -> PropertyValueData.class), NodeTypeEnum.Property,
				NodeTypeEnum.PropertyValue);

		preUpdateElementOfResourceInstanceValidations(updateDataContainer, resourceInstanceProerty, resourceInstanceId, errorWrapper);
		if (!errorWrapper.isEmpty()) {
			return Either.right(errorWrapper.getInnerElement());
		}

		else {
			String value = resourceInstanceProerty.getValue();
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
			Either<Object, Boolean> isValid = propertyOperation.validateAndUpdatePropertyValue(propertyType, value, isValidate, innerType, allDataTypes.left().value());

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

			ImmutablePair<String, Boolean> pair = propertyOperation.validateAndUpdateRules(propertyType, resourceInstanceProerty.getRules(), innerType, allDataTypes.left().value(), isValidate);
			if (pair.getRight() != null && pair.getRight() == false) {
				BeEcompErrorManager.getInstance().logBeInvalidValueError("Add property value", pair.getLeft(), resourceInstanceProerty.getName(), propertyType);
				return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
			}
			propertyOperation.updateRulesInPropertyValue(propertyValueData, resourceInstanceProerty, resourceInstanceId);

			Either<PropertyValueData, TitanOperationStatus> updateRes = titanGenericDao.updateNode(propertyValueData, PropertyValueData.class);
			if (updateRes.isRight()) {
				TitanOperationStatus status = updateRes.right().value();
				return Either.right(status);
			} else {
				return Either.left(updateRes.left().value());
			}
		}

	}

	/**
	 * update value of attribute on resource instance
	 * 
	 * @param resourceInstanceProerty
	 * @param resourceInstanceId
	 * @return
	 */
	public Either<InputValueData, TitanOperationStatus> updateInputOfResourceInstance(ComponentInstanceInput resourceInstanceProerty, String resourceInstanceId) {

		Wrapper<TitanOperationStatus> errorWrapper = new Wrapper<>();
		UpdateDataContainer<PropertyData, InputValueData> updateDataContainer = new UpdateDataContainer<>(GraphEdgeLabels.INPUT_IMPL, (() -> PropertyData.class), (() -> InputValueData.class), NodeTypeEnum.Input, NodeTypeEnum.InputValue);

		preUpdateElementOfResourceInstanceValidations(updateDataContainer, resourceInstanceProerty, resourceInstanceId, errorWrapper);
		if (!errorWrapper.isEmpty()) {
			return Either.right(errorWrapper.getInnerElement());
		}

		else {
			String value = resourceInstanceProerty.getValue();
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
			/*
			 * Either<Object, Boolean> isValid = propertyOperation.validateAndUpdatePropertyValue(propertyType, value, innerType, allDataTypes.left().value());
			 * 
			 * String newValue = value; if (isValid.isRight()) { Boolean res = isValid.right().value(); if (res == false) { return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT); } } else { Object object = isValid.left().value(); if (object !=
			 * null) { newValue = object.toString(); } } InputValueData propertyValueData = updateDataContainer.getValueDataWrapper().getInnerElement(); log.debug("Going to update property value from " + propertyValueData.getValue() + " to " +
			 * newValue); propertyValueData.setValue(newValue);
			 * 
			 * ImmutablePair<String, Boolean> pair = propertyOperation.validateAndUpdateRules(propertyType, resourceInstanceProerty.getRules(), innerType, allDataTypes.left().value()); if (pair.getRight() != null && pair.getRight() == false) {
			 * BeEcompErrorManager.getInstance(). logBeInvalidValueError("Add property value", pair.getLeft(), resourceInstanceProerty.getName(), propertyType); return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT); }
			 * propertyOperation.updateRulesInPropertyValue(propertyValueData, resourceInstanceProerty, resourceInstanceId);
			 * 
			 * Either<PropertyValueData, TitanOperationStatus> updateRes = titanGenericDao.updateNode(propertyValueData, PropertyValueData.class); if (updateRes.isRight()) { TitanOperationStatus status = updateRes.right().value(); return
			 * Either.right(status); } else { return Either.left(updateRes.left().value()); }
			 */
		}
		return null;

	}

	private <SomeData extends GraphNode, SomeValueData extends GraphNode> void preUpdateElementOfResourceInstanceValidations(UpdateDataContainer<SomeData, SomeValueData> updateDataContainer, IComponentInstanceConnectedElement resourceInstanceProerty,
			String resourceInstanceId, Wrapper<TitanOperationStatus> errorWrapper) {

		if (errorWrapper.isEmpty()) {
			// Verify VFC instance Exist
			validateRIExist(resourceInstanceId, errorWrapper);
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

	private void validateRIExist(String resourceInstanceId, Wrapper<TitanOperationStatus> errorWrapper) {
		validateRIExist(resourceInstanceId, null, errorWrapper);
	}

	private void validateRIExist(String resourceInstanceId, Wrapper<ComponentInstanceData> compInsDataWrapper, Wrapper<TitanOperationStatus> errorWrapper) {
		validateElementExistInGraph(resourceInstanceId, NodeTypeEnum.ResourceInstance, () -> ComponentInstanceData.class, compInsDataWrapper, errorWrapper);
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
	 * add property to resource instance
	 * 
	 * @param resourceInstanceProperty
	 * @param resourceInstanceId
	 * @param index
	 * @return
	 */
	public Either<PropertyValueData, TitanOperationStatus> addPropertyToResourceInstance(ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId, boolean isValidate, Integer index) {

		Either<ComponentInstanceData, TitanOperationStatus> findResInstanceRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId, ComponentInstanceData.class);

		if (findResInstanceRes.isRight()) {
			TitanOperationStatus status = findResInstanceRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		String propertyId = resourceInstanceProperty.getUniqueId();
		Either<PropertyData, TitanOperationStatus> findPropertyDefRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property), propertyId, PropertyData.class);

		if (findPropertyDefRes.isRight()) {
			TitanOperationStatus status = findPropertyDefRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		String valueUniqueUid = resourceInstanceProperty.getValueUniqueUid();
		if (valueUniqueUid == null) {

			PropertyData propertyData = findPropertyDefRes.left().value();
			ComponentInstanceData resourceInstanceData = findResInstanceRes.left().value();

			ImmutablePair<TitanOperationStatus, String> isPropertyValueExists = propertyOperation.findPropertyValue(resourceInstanceId, propertyId);
			if (isPropertyValueExists.getLeft() == TitanOperationStatus.ALREADY_EXIST) {
				log.debug("The property {} already added to the resource instance {}", propertyId, resourceInstanceId);
				resourceInstanceProperty.setValueUniqueUid(isPropertyValueExists.getRight());
				Either<PropertyValueData, TitanOperationStatus> updatePropertyOfResourceInstance = updatePropertyOfResourceInstance(resourceInstanceProperty, resourceInstanceId, isValidate);
				if (updatePropertyOfResourceInstance.isRight()) {
					BeEcompErrorManager.getInstance().logInternalFlowError("UpdatePropertyValueOnComponentInstance", "Failed to update property value on instance. Status is " + updatePropertyOfResourceInstance.right().value(), ErrorSeverity.ERROR);
					return Either.right(updatePropertyOfResourceInstance.right().value());
				}
				return Either.left(updatePropertyOfResourceInstance.left().value());
			}

			if (isPropertyValueExists.getLeft() != TitanOperationStatus.NOT_FOUND) {
				log.debug("After finding property value of {} on componenet instance {}", propertyId, resourceInstanceId);
				return Either.right(isPropertyValueExists.getLeft());
			}

			String innerType = null;

			PropertyDataDefinition propDataDef = propertyData.getPropertyDataDefinition();
			String propertyType = propDataDef.getType();
			String value = resourceInstanceProperty.getValue();
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
			Either<Object, Boolean> isValid = propertyOperation.validateAndUpdatePropertyValue(propertyType, value, isValidate, innerType, allDataTypes.left().value());
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
			ImmutablePair<String, Boolean> pair = propertyOperation.validateAndUpdateRules(propertyType, resourceInstanceProperty.getRules(), innerType, allDataTypes.left().value(), isValidate);
			log.debug("After validateAndUpdateRules. pair = {}", pair);
			if (pair.getRight() != null && pair.getRight() == false) {
				BeEcompErrorManager.getInstance().logBeInvalidValueError("Add property value", pair.getLeft(), resourceInstanceProperty.getName(), propertyType);
				return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
			}
			propertyOperation.addRulesToNewPropertyValue(propertyValueData, resourceInstanceProperty, resourceInstanceId);

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
				log.error("Failed to associate resource instance {} property value {} in graph. status is {}", resourceInstanceId, uniqueId, operationStatus);
				return Either.right(operationStatus);
			}

			return Either.left(propertyValueData);
		} else {
			log.error("property value already exists.");
			return Either.right(TitanOperationStatus.ALREADY_EXIST);
		}

	}

	public Either<ComponentInstanceProperty, TitanOperationStatus> addPropertyToResourceInstance(ComponentInstanceProperty resourceInstanceProperty, TitanVertex resourceInstanceVertex, boolean isValidate, Integer index, String resourceInstanceId) {

		String propertyId = resourceInstanceProperty.getUniqueId();
		Either<PropertyData, TitanOperationStatus> findPropertyDefRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property), propertyId, PropertyData.class);

		if (findPropertyDefRes.isRight()) {
			TitanOperationStatus status = findPropertyDefRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		String valueUniqueUid = resourceInstanceProperty.getValueUniqueUid();
		if (valueUniqueUid == null) {

			PropertyData propertyData = findPropertyDefRes.left().value();

			ImmutablePair<TitanOperationStatus, String> isPropertyValueExists = propertyOperation.findPropertyValue(resourceInstanceId, propertyId);
			if (isPropertyValueExists.getLeft() == TitanOperationStatus.ALREADY_EXIST) {
				log.trace("The property {} already added to the resource instance {}", propertyId, resourceInstanceId);
				resourceInstanceProperty.setValueUniqueUid(isPropertyValueExists.getRight());
				Either<PropertyValueData, TitanOperationStatus> updatePropertyOfResourceInstance = updatePropertyOfResourceInstance(resourceInstanceProperty, resourceInstanceId, isValidate);
				if (updatePropertyOfResourceInstance.isRight()) {
					BeEcompErrorManager.getInstance().logInternalFlowError("UpdatePropertyValueOnComponentInstance", "Failed to update property value on instance. Status is " + updatePropertyOfResourceInstance.right().value(), ErrorSeverity.ERROR);
					return Either.right(updatePropertyOfResourceInstance.right().value());
				}
				return Either.right(TitanOperationStatus.OK);
			}

			if (isPropertyValueExists.getLeft() != TitanOperationStatus.NOT_FOUND) {
				log.trace("After finding property value of {} on componenet instance {}", propertyId, resourceInstanceId);
				return Either.right(isPropertyValueExists.getLeft());
			}

			String innerType = null;

			PropertyDataDefinition propDataDef = propertyData.getPropertyDataDefinition();
			String propertyType = propDataDef.getType();
			String value = resourceInstanceProperty.getValue();
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
			Either<Object, Boolean> isValid = propertyOperation.validateAndUpdatePropertyValue(propertyType, value, isValidate, innerType, allDataTypes.left().value());
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

			String uniqueId = UniqueIdBuilder.buildResourceInstancePropertyValueUid(resourceInstanceId, index);
			PropertyValueData propertyValueData = new PropertyValueData();
			propertyValueData.setUniqueId(uniqueId);
			propertyValueData.setValue(newValue);

			log.trace("Before validateAndUpdateRules");
			ImmutablePair<String, Boolean> pair = propertyOperation.validateAndUpdateRules(propertyType, resourceInstanceProperty.getRules(), innerType, allDataTypes.left().value(), isValidate);
			log.debug("After validateAndUpdateRules. pair = {} ", pair);
			if (pair.getRight() != null && pair.getRight() == false) {
				BeEcompErrorManager.getInstance().logBeInvalidValueError("Add property value", pair.getLeft(), resourceInstanceProperty.getName(), propertyType);
				return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
			}
			propertyOperation.addRulesToNewPropertyValue(propertyValueData, resourceInstanceProperty, resourceInstanceId);

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

			TitanOperationStatus edgeResult = titanGenericDao.createEdge(resourceInstanceVertex, propertyValueData, GraphEdgeLabels.PROPERTY_VALUE, null);

			if (edgeResult != TitanOperationStatus.OK) {
				log.error("Failed to associate resource instance {} property value {} in graph. status is {}", resourceInstanceId, uniqueId, edgeResult);
				return Either.right(edgeResult);
			}

			ComponentInstanceProperty propertyValueResult = propertyOperation.buildResourceInstanceProperty(propertyValueData, resourceInstanceProperty);
			log.debug("The returned ResourceInstanceProperty is {} ", propertyValueResult);

			return Either.left(propertyValueResult);
		} else {
			log.debug("property value already exists.");
			return Either.right(TitanOperationStatus.ALREADY_EXIST);
		}

	}

	/**
	 * add property to resource instance
	 * 
	 * @param resourceInstanceProperty
	 * @param resourceInstanceId
	 * @param index
	 * @return
	 */
	public Either<InputValueData, TitanOperationStatus> addInputToResourceInstance(ComponentInstanceInput resourceInstanceInput, String resourceInstanceId, Integer index) {

		Either<ComponentInstanceData, TitanOperationStatus> findResInstanceRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId, ComponentInstanceData.class);

		if (findResInstanceRes.isRight()) {
			TitanOperationStatus status = findResInstanceRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		String propertyId = resourceInstanceInput.getUniqueId();
		Either<InputsData, TitanOperationStatus> findPropertyDefRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Input), propertyId, InputsData.class);

		if (findPropertyDefRes.isRight()) {
			TitanOperationStatus status = findPropertyDefRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		String valueUniqueUid = resourceInstanceInput.getValueUniqueUid();
		if (valueUniqueUid == null) {

			InputsData propertyData = findPropertyDefRes.left().value();

			ComponentInstanceData resourceInstanceData = findResInstanceRes.left().value();

			ImmutablePair<TitanOperationStatus, String> isInputValueExists = inputOperation.findInputValue(resourceInstanceId, propertyId);
			if (isInputValueExists.getLeft() == TitanOperationStatus.ALREADY_EXIST) {
				log.debug("The property {} already added to the resource instance {}", propertyId, resourceInstanceId);
				resourceInstanceInput.setValueUniqueUid(isInputValueExists.getRight());
				/*
				 * Either<InputValueData, TitanOperationStatus> updatePropertyOfResourceInstance = updatePropertyOfResourceInstance(resourceInstanceInput, resourceInstanceId); if (updatePropertyOfResourceInstance.isRight()) {
				 * BeEcompErrorManager.getInstance().logInternalFlowError( "UpdatePropertyValueOnComponentInstance", "Failed to update property value on instance. Status is " + updatePropertyOfResourceInstance.right().value(), ErrorSeverity.ERROR);
				 * return Either.right(updatePropertyOfResourceInstance.right().value() ); } return Either.left(updatePropertyOfResourceInstance.left().value());
				 */
			}

			if (isInputValueExists.getLeft() != TitanOperationStatus.NOT_FOUND) {
				log.debug("After finding input value of {} on componenet instance {}", propertyId, resourceInstanceId);
				return Either.right(isInputValueExists.getLeft());
			}

			String innerType = null;

			PropertyDataDefinition propDataDef = propertyData.getPropertyDataDefinition();
			String propertyType = propDataDef.getType();
			String value = resourceInstanceInput.getValue();
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

			String uniqueId = UniqueIdBuilder.buildResourceInstanceInputValueUid(resourceInstanceData.getUniqueId(), index);
			InputValueData propertyValueData = new InputValueData();
			propertyValueData.setUniqueId(uniqueId);
			propertyValueData.setValue(value);

			log.debug("Before validateAndUpdateRules");
			ImmutablePair<String, Boolean> pair = propertyOperation.validateAndUpdateRules(propertyType, resourceInstanceInput.getRules(), innerType, allDataTypes.left().value(), true);
			log.debug("After validateAndUpdateRules. pair = {} ", pair);
			if (pair.getRight() != null && pair.getRight() == false) {
				BeEcompErrorManager.getInstance().logBeInvalidValueError("Add property value", pair.getLeft(), resourceInstanceInput.getName(), propertyType);
				return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
			}
			// propertyOperation.addRulesToNewPropertyValue(propertyValueData,
			// resourceInstanceInput, resourceInstanceId);

			log.debug("Before adding property value to graph {}", propertyValueData);
			Either<InputValueData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(propertyValueData, InputValueData.class);
			log.debug("After adding property value to graph {}", propertyValueData);

			if (createNodeResult.isRight()) {
				TitanOperationStatus operationStatus = createNodeResult.right().value();
				return Either.right(operationStatus);
			}

			Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(propertyValueData, propertyData, GraphEdgeLabels.INPUT_IMPL, null);

			if (createRelResult.isRight()) {
				TitanOperationStatus operationStatus = createRelResult.right().value();
				log.error("Failed to associate property value {} to property {} in graph. status is {}", uniqueId, propertyId, operationStatus);
				return Either.right(operationStatus);
			}

			Map<String, Object> properties1 = new HashMap<String, Object>();

			properties1.put(GraphEdgePropertiesDictionary.NAME.getProperty(), resourceInstanceData.getComponentInstDataDefinition().getName());
			properties1.put(GraphEdgePropertiesDictionary.OWNER_ID.getProperty(), resourceInstanceData.getComponentInstDataDefinition().getUniqueId());

			createRelResult = titanGenericDao.createRelation(resourceInstanceData, propertyValueData, GraphEdgeLabels.INPUT_VALUE, properties1);

			if (createRelResult.isRight()) {
				TitanOperationStatus operationStatus = createNodeResult.right().value();
				log.error("Failed to associate resource instance {} property value {} in graph. status is {}", resourceInstanceId, uniqueId, operationStatus);
				return Either.right(operationStatus);

			}

			// inputOperation.associatePropertyToInput(resourceInstanceId,
			// resourceInstanceInput.getInputId(), propertyValueData,
			// resourceInstanceInput.getName());

			return Either.left(createNodeResult.left().value());
		} else {
			log.error("property value already exists.");
			return Either.right(TitanOperationStatus.ALREADY_EXIST);
		}

	}

	@Override
	public Either<ComponentInstanceProperty, StorageOperationStatus> addAttributeValueToResourceInstance(ComponentInstanceProperty resourceInstanceAttribute, String resourceInstanceId, Integer index, boolean inTransaction) {
		Either<ComponentInstanceProperty, StorageOperationStatus> result = null;

		try {

			Either<AttributeValueData, TitanOperationStatus> eitherStatus = this.addAttributeToResourceInstance(resourceInstanceAttribute, resourceInstanceId, index);

			if (eitherStatus.isRight()) {
				log.error("Failed to add attribute value {} to resource instance {} in Graph. status is {}", resourceInstanceAttribute, resourceInstanceId, eitherStatus.right().value().name());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherStatus.right().value()));
				return result;
			} else {
				AttributeValueData attributeValueData = eitherStatus.left().value();

				ComponentInstanceProperty attributeValueResult = attributeOperation.buildResourceInstanceAttribute(attributeValueData, resourceInstanceAttribute);
				log.debug("The returned ResourceInstanceAttribute is {}", attributeValueResult);

				result = Either.left(attributeValueResult);
				return result;
			}
		}

		finally {
			handleTransactionCommitRollback(inTransaction, result);
		}
	}

	@Override
	public Either<ComponentInstanceProperty, StorageOperationStatus> updateAttributeValueInResourceInstance(ComponentInstanceProperty resourceInstanceAttribute, String resourceInstanceId, boolean inTransaction) {

		Either<ComponentInstanceProperty, StorageOperationStatus> result = null;

		try {
			Either<AttributeValueData, TitanOperationStatus> eitherAttributeValue = updateAttributeOfResourceInstance(resourceInstanceAttribute, resourceInstanceId);

			if (eitherAttributeValue.isRight()) {
				log.error("Failed to add attribute value {} to resource instance {} in Graph. status is {}", resourceInstanceAttribute, resourceInstanceId, eitherAttributeValue.right().value().name());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherAttributeValue.right().value()));
				return result;
			} else {
				AttributeValueData attributeValueData = eitherAttributeValue.left().value();

				ComponentInstanceProperty attributeValueResult = attributeOperation.buildResourceInstanceAttribute(attributeValueData, resourceInstanceAttribute);
				log.debug("The returned ResourceInstanceAttribute is {}", attributeValueResult);

				result = Either.left(attributeValueResult);
				return result;
			}
		}

		finally {
			handleTransactionCommitRollback(inTransaction, result);
		}

	}

	@Override
	public Either<ComponentInstanceProperty, StorageOperationStatus> addPropertyValueToResourceInstance(ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId, Integer index, boolean inTransaction) {
		return addPropertyValueToResourceInstance(resourceInstanceProperty, resourceInstanceId, true, index, inTransaction);
	}

	@Override
	public Either<ComponentInstanceProperty, StorageOperationStatus> addPropertyValueToResourceInstance(ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId, boolean isValidate, Integer index, boolean inTransaction) {

		/// #RULES SUPPORT
		/// Ignore rules received from client till support
		resourceInstanceProperty.setRules(null);
		///
		///

		Either<ComponentInstanceProperty, StorageOperationStatus> result = null;

		try {

			Either<PropertyValueData, TitanOperationStatus> eitherStatus = addPropertyToResourceInstance(resourceInstanceProperty, resourceInstanceId, isValidate, index);

			if (eitherStatus.isRight()) {
				log.error("Failed to add property value {} to resource instance {} in Graph. status is {}", resourceInstanceProperty, resourceInstanceId, eitherStatus.right().value().name());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherStatus.right().value()));
				return result;
			} else {
				PropertyValueData propertyValueData = eitherStatus.left().value();

				ComponentInstanceProperty propertyValueResult = propertyOperation.buildResourceInstanceProperty(propertyValueData, resourceInstanceProperty);
				log.debug("The returned ResourceInstanceProperty is {}", propertyValueResult);

				Either<String, TitanOperationStatus> findDefaultValue = propertyOperation.findDefaultValueFromSecondPosition(resourceInstanceProperty.getPath(), resourceInstanceProperty.getUniqueId(), resourceInstanceProperty.getDefaultValue());
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
	public Either<ComponentInstanceInput, StorageOperationStatus> addInputValueToResourceInstance(ComponentInstanceInput resourceInstanceInput, String resourceInstanceId, Integer index, boolean inTransaction) {

		/// #RULES SUPPORT
		/// Ignore rules received from client till support
		resourceInstanceInput.setRules(null);
		///
		///

		Either<ComponentInstanceInput, StorageOperationStatus> result = null;

		try {

			Either<InputValueData, TitanOperationStatus> eitherStatus = addInputToResourceInstance(resourceInstanceInput, resourceInstanceId, index);

			if (eitherStatus.isRight()) {
				log.error("Failed to add input value {} to resource instance {} in Graph. status is {}", resourceInstanceInput, resourceInstanceId, eitherStatus.right().value().name());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherStatus.right().value()));
				return result;
			} else {
				InputValueData propertyValueData = eitherStatus.left().value();

				ComponentInstanceInput propertyValueResult = inputOperation.buildResourceInstanceInput(propertyValueData, resourceInstanceInput);
				log.debug("The returned ResourceInstanceProperty is {}", propertyValueResult);

				Either<String, TitanOperationStatus> findDefaultValue = propertyOperation.findDefaultValueFromSecondPosition(resourceInstanceInput.getPath(), resourceInstanceInput.getUniqueId(), resourceInstanceInput.getDefaultValue());
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

	public Either<List<ComponentInstanceProperty>, TitanOperationStatus> getComponentInstancesProperties(List<ComponentInstance> resourceInstances, Map<String, List<PropertyDefinition>> alreadyProcessedResources,
			Map<String, List<ComponentInstanceProperty>> resourceInstancesProperties, Map<String, ImmutablePair<ComponentInstance, Integer>> processedInstances, List<String> path) {

		List<ComponentInstanceProperty> result = new ArrayList<>();

		for (ComponentInstance componentInstance : resourceInstances) {

			path.add(componentInstance.getUniqueId());

			Either<List<ComponentInstanceProperty>, TitanOperationStatus> componentInstancesProperties = getComponentInstanceProperties(componentInstance, alreadyProcessedResources, resourceInstancesProperties, processedInstances, path);
			if (componentInstancesProperties.isRight()) {
				TitanOperationStatus status = componentInstancesProperties.right().value();
				if (status != TitanOperationStatus.OK) {
					return Either.right(status);
				}
			}

			List<ComponentInstanceProperty> compInstancePropertyList = componentInstancesProperties.left().value();
			if (compInstancePropertyList != null) {
				result.addAll(compInstancePropertyList);
			}

			String uniqueId = componentInstance.getUniqueId();
			if (false == processedInstances.containsKey(uniqueId)) {
				processedInstances.put(uniqueId, new ImmutablePair<ComponentInstance, Integer>(componentInstance, path.size()));
			}
			path.remove(path.size() - 1);

		}

		return Either.left(result);
	}

	//US831698
	public Either<List<ComponentInstanceProperty>, StorageOperationStatus> getComponentInstancesPropertiesAndValuesFromGraph(ComponentInstance resourceInstance) {

		Map<String, List<PropertyDefinition>> alreadyProcessedResources = new HashMap<>();
		Map<String, List<ComponentInstanceProperty>> alreadyProcessedInstances = new HashMap<>();
		Map<String, ImmutablePair<ComponentInstance, Integer>> processedInstances = new HashMap<>();
		Map<String, List<ComponentInstanceProperty>> resourceInstancesProperties = new HashMap<>();

		List<String> path = new ArrayList<>();
		path.add(resourceInstance.getUniqueId());
		Either<List<ComponentInstanceProperty>, TitanOperationStatus> componentInstanceProperties = getComponentInstanceProperties(resourceInstance, alreadyProcessedResources, alreadyProcessedInstances, processedInstances, path);

		if (componentInstanceProperties.isRight()) {
			StorageOperationStatus convertTitanStatusToStorageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(componentInstanceProperties.right().value());
			return Either.right(convertTitanStatusToStorageStatus);
		}

		List<ComponentInstanceProperty> listOfProps = componentInstanceProperties.left().value();
		resourceInstancesProperties.put(resourceInstance.getUniqueId(), listOfProps);

		processedInstances.put(resourceInstance.getUniqueId(), new ImmutablePair<ComponentInstance, Integer>(resourceInstance, path.size()));
		path.remove(path.size() - 1);

		Either<Map<String, Map<String, ComponentInstanceProperty>>, TitanOperationStatus> findAllPropertiesValuesOnInstances = findAllPropertyValueOnInstances(processedInstances);
		// 1. check status
		if (findAllPropertiesValuesOnInstances.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(findAllPropertiesValuesOnInstances.right().value()));
		}

		propertyOperation.updatePropertiesByPropertyValues(resourceInstancesProperties, findAllPropertiesValuesOnInstances.left().value());

		return Either.left(resourceInstancesProperties.get(resourceInstance.getUniqueId()));
	}

	public Either<List<ComponentInstanceProperty>, TitanOperationStatus> getComponentInstanceProperties(ComponentInstance resourceInstance, Map<String, List<PropertyDefinition>> alreadyProcessedResources,
			Map<String, List<ComponentInstanceProperty>> alreadyProcessedInstances, Map<String, ImmutablePair<ComponentInstance, Integer>> processedInstances, List<String> path) {

		// 1. Go over each instance
		// 1.1 get all properties of from the parents of the instance
		// 1.2 get all updated properties
		// 1.3 find all instances included in the parent of this instance and
		// run this method on them.
		if (log.isDebugEnabled())
			log.debug("Going to update properties of resource instance {}", resourceInstance.getUniqueId());
		String resourceUid = resourceInstance.getComponentUid();

		List<PropertyDefinition> properties = alreadyProcessedResources.get(resourceUid);
		if (properties == null) {
			properties = new ArrayList<>();
			TitanOperationStatus findAllRes = propertyOperation.findAllResourcePropertiesRecursively(resourceUid, properties);
			if (findAllRes != TitanOperationStatus.OK) {
				return Either.right(findAllRes);
			}
			alreadyProcessedResources.put(resourceUid, properties);
		}

		if (log.isDebugEnabled())
			log.debug("After getting properties of resource {} . Number of properties is {}", resourceUid, (properties == null ? 0 : properties.size()));
		List<ComponentInstanceProperty> resourceInstancePropertyList = new ArrayList<>();
		if (false == properties.isEmpty()) {

			// TODO: WE MAY HAVE INDIRECT PROPERTY VALUE ALSO IN CASE NO
			// PROPERTY ON THIS COMPONENT

			// String resourceInstanceUid = resourceInstance.getUniqueId();

			for (PropertyDefinition propertyDefinition : properties) {

				String defaultValue = propertyDefinition.getDefaultValue();
				String value = defaultValue;
				String valueUid = null;

				// String propertyId = propertyDefinition.getUniqueId();

				ComponentInstanceProperty resourceInstanceProperty = new ComponentInstanceProperty(propertyDefinition, value, valueUid);

				resourceInstanceProperty.setPath(cloneList(path));

				// TODO: currently ignore constraints since they are not inuse
				// and cause to error in convertion to object.
				resourceInstanceProperty.setConstraints(null);

				resourceInstancePropertyList.add(resourceInstanceProperty);

			}
		}

		OriginTypeEnum originType = resourceInstance.getOriginType();

		Either<List<ComponentInstance>, TitanOperationStatus> findInstancesUnderParentOfInstance = findInstancesUnderParentOfInstance(originType, resourceUid);

		if (findInstancesUnderParentOfInstance.isRight()) {
			TitanOperationStatus status = findInstancesUnderParentOfInstance.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				return Either.right(status);
			}
		} else {
			List<ComponentInstance> listOfInstances = findInstancesUnderParentOfInstance.left().value();
			Either<List<ComponentInstanceProperty>, TitanOperationStatus> componentInstancesProperties = getComponentInstancesProperties(listOfInstances, alreadyProcessedResources, alreadyProcessedInstances, processedInstances, path);
			if (componentInstancesProperties.isRight()) {
				TitanOperationStatus status = componentInstancesProperties.right().value();
				if (status != TitanOperationStatus.OK) {
					return Either.right(status);
				}
			}
			List<ComponentInstanceProperty> currentList = componentInstancesProperties.left().value();
			if (currentList != null) {
				resourceInstancePropertyList.addAll(currentList);
			}
		}

		return Either.left(resourceInstancePropertyList);
	}

	public Either<List<ComponentInstance>, TitanOperationStatus> findInstancesUnderParentOfInstance(OriginTypeEnum originType, String resourceUid) {

		NodeTypeEnum containerNodeType = null;
		NodeTypeEnum compInstNodeType = null;

		switch (originType) {

		case VF:
			containerNodeType = NodeTypeEnum.Resource;
			compInstNodeType = NodeTypeEnum.Resource;
			break;
		case SERVICE:
			containerNodeType = NodeTypeEnum.Service;
			compInstNodeType = NodeTypeEnum.Resource;
			break;
		case PRODUCT:
			containerNodeType = NodeTypeEnum.Product;
			compInstNodeType = NodeTypeEnum.Service;
			break;
		case VFC:
		case VFCMT:
		case VL:
		case CP:
			break;
		default:
			break;
		}

		if (containerNodeType == null || compInstNodeType == null) {
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

		Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, TitanOperationStatus> componentInstancesOfComponent = this.getComponentInstancesOfComponent(resourceUid, containerNodeType, compInstNodeType);

		if (componentInstancesOfComponent.isRight()) {
			TitanOperationStatus status = componentInstancesOfComponent.right().value();
			log.debug("After getting instances of {}  from type {}. Status is {}", resourceUid, originType, status);
			return Either.right(status);
		} else {
			List<ComponentInstance> listOfInstances = componentInstancesOfComponent.left().value().getLeft();
			if (log.isDebugEnabled()) {
				String msg = "After getting instances of {}  from type {}   {}.";
				log.debug(msg, resourceUid, originType, (listOfInstances != null ? listOfInstances.size() : 0));
				if (log.isTraceEnabled())
					log.trace(msg, resourceUid, originType, listOfInstances);
			}
			return Either.left(listOfInstances);
		}

	}

	private List<String> cloneList(List<String> list) {

		if (list == null) {
			return null;
		}

		List<String> clonedList = new ArrayList<String>();
		clonedList.addAll(list);

		return clonedList;
	}

	public Either<Map<String, Map<String, ComponentInstanceProperty>>, TitanOperationStatus> findAllPropertyValueOnInstances(Map<String, ImmutablePair<ComponentInstance, Integer>> processedInstances) {

		if (processedInstances == null) {
			return Either.right(TitanOperationStatus.OK);
		}

		Set<Entry<String, ImmutablePair<ComponentInstance, Integer>>> entrySet = processedInstances.entrySet();

		Map<String, Map<String, ComponentInstanceProperty>> propertyToInstanceValue = new HashMap<>();

		for (Entry<String, ImmutablePair<ComponentInstance, Integer>> entry : entrySet) {

			String compInstUniqueId = entry.getKey();

			ImmutablePair<ComponentInstance, Integer> pair = entry.getValue();

			ComponentInstance componentInstance = pair.getLeft();

			Either<List<ComponentInstanceProperty>, TitanOperationStatus> propeprtyValueOnCIResult = findPropertyValueOnComponentInstance(componentInstance);

			if (propeprtyValueOnCIResult.isRight()) {
				TitanOperationStatus status = propeprtyValueOnCIResult.right().value();
				if (status != TitanOperationStatus.OK) {
					return Either.right(status);
				}
				continue;
			}

			List<ComponentInstanceProperty> propertyValuesOnCI = propeprtyValueOnCIResult.left().value();
			if (propertyValuesOnCI != null) {
				for (ComponentInstanceProperty instanceProperty : propertyValuesOnCI) {
					boolean result = addPropertyValue(compInstUniqueId, instanceProperty, propertyToInstanceValue);
					if (!result) {
						return Either.right(TitanOperationStatus.ALREADY_EXIST);
					}
				}
			}

		}

		return Either.left(propertyToInstanceValue);
	}

	private boolean addPropertyValue(String compInstUniqueId, ComponentInstanceProperty instanceProperty, Map<String, Map<String, ComponentInstanceProperty>> propertyToInstanceValue) {

		String propertyUid = instanceProperty.getUniqueId();

		Map<String, ComponentInstanceProperty> map = propertyToInstanceValue.get(propertyUid);
		if (map == null) {
			map = new HashMap<>();
			propertyToInstanceValue.put(propertyUid, map);
		}

		ComponentInstanceProperty putIfAbsent = map.putIfAbsent(compInstUniqueId, instanceProperty);
		if (putIfAbsent != null) {
			BeEcompErrorManager.getInstance().logInternalUnexpectedError("find property value", "Found 2 values on the same instance", ErrorSeverity.ERROR);
			return false;
		}

		return true;

	}

	private boolean addInputValue(String compInstUniqueId, ComponentInstanceInput instanceProperty, Map<String, Map<String, ComponentInstanceInput>> propertyToInstanceValue) {

		String propertyUid = instanceProperty.getUniqueId();

		Map<String, ComponentInstanceInput> map = propertyToInstanceValue.get(propertyUid);
		if (map == null) {
			map = new HashMap<>();
			propertyToInstanceValue.put(propertyUid, map);
		}

		ComponentInstanceInput putIfAbsent = map.putIfAbsent(compInstUniqueId, instanceProperty);
		if (putIfAbsent != null) {
			BeEcompErrorManager.getInstance().logInternalUnexpectedError("find property value", "Found 2 values on the same instance", ErrorSeverity.ERROR);
			return false;
		}

		return true;

	}

	private Either<List<ComponentInstanceProperty>, TitanOperationStatus> findPropertyValueOnComponentInstance(ComponentInstance componentInstance) {
		String resourceInstanceUid = componentInstance.getUniqueId();
		OriginTypeEnum originType = componentInstance.getOriginType();

		NodeTypeEnum instanceNodeType = findInstanceNodeTypeEnumFromOriginType(originType);

		Either<List<ComponentInstanceProperty>, TitanOperationStatus> propertyValuesResult = propertyOperation.getAllPropertiesOfResourceInstanceOnlyPropertyDefId(resourceInstanceUid, instanceNodeType);

		log.debug("After fetching property under resource instance {}", resourceInstanceUid);
		if (propertyValuesResult.isRight()) {
			TitanOperationStatus status = propertyValuesResult.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				return Either.right(status);
			}
			return Either.right(TitanOperationStatus.OK);
		}

		return Either.left(propertyValuesResult.left().value());

	}

	private NodeTypeEnum findInstanceNodeTypeEnumFromOriginType(OriginTypeEnum originType) {
		NodeTypeEnum nodeType = NodeTypeEnum.ResourceInstance;
		switch (originType) {
		case SERVICE:
			nodeType = NodeTypeEnum.ResourceInstance;
			break;
		default:
			break;
		}

		return nodeType;
	}

	/**
	 * add capability property values to resource instance
	 * 
	 * @param resourceInstanceId
	 * @param capability
	 * @param isNewlyCreatedResourceInstance
	 * @return
	 */
	public Either<Map<CapabilityInstData, List<PropertyValueData>>, TitanOperationStatus> addCapabilityPropertyValuesToResourceInstance(String resourceInstanceId, CapabilityDefinition capability, boolean isNewlyCreatedResourceInstance) {
		log.debug("Before adding capability property values to resource instance {}.", resourceInstanceId);
		TitanOperationStatus error = null;

		Either<Map<CapabilityInstData, List<PropertyValueData>>, TitanOperationStatus> addCapInstWithPropertiesRes = capabilityInstanceOperation.createCapabilityInstanceOfCapabilityWithPropertyValuesForResourceInstance(resourceInstanceId,
				capability.getUniqueId(), capability.getName(), capability.getProperties(), !isNewlyCreatedResourceInstance);
		if (addCapInstWithPropertiesRes.isRight()) {
			error = addCapInstWithPropertiesRes.right().value();
			log.debug("Failed to assotiate capability instance to resource instance {}. status is {}", resourceInstanceId, error);
		}
		log.debug("After adding capability property values to resource instance {}. Status is {}", resourceInstanceId, error);
		if (error == null) {
			return Either.left(addCapInstWithPropertiesRes.left().value());
		}
		return Either.right(error);
	}

	public TitanOperationStatus addCapabilityPropertyValuesToResourceInstance(TitanVertex resourceInstanceVertex, String resourceInstanceId, CapabilityDefinition capability, boolean isNewlyCreatedResourceInstance) {
		log.trace("Before adding capability property values to resource instance {}.", resourceInstanceId);
		TitanOperationStatus error = TitanOperationStatus.OK;

		TitanOperationStatus addCapInstWithPropertiesRes = capabilityInstanceOperation.createCapabilityInstanceOfCapabilityWithPropertyValuesForResourceInstance(resourceInstanceVertex, resourceInstanceId, capability.getUniqueId(),
				capability.getName(), capability.getProperties(), !isNewlyCreatedResourceInstance);
		if (addCapInstWithPropertiesRes != TitanOperationStatus.OK) {
			error = addCapInstWithPropertiesRes;
			log.debug("Failed to assotiate capability instance to resource instance {} . status is {}", resourceInstanceId, error);
		}
		log.debug("After adding capability property values to resource instance {}. Status is {}", resourceInstanceId, error);

		return error;
	}

	/**
	 * update capability property values of capability
	 * 
	 * @param resourceInstanceId
	 * @param capabilityId
	 * @param propertyValues
	 * @return
	 */
	public Either<List<PropertyValueData>, TitanOperationStatus> updateCapabilityPropertyValuesOfResourceInstance(String resourceInstanceId, String capabilityId, List<ComponentInstanceProperty> propertyValues) {
		log.debug("Before updating property values of capability {} of resource instance {}.", capabilityId, resourceInstanceId);
		TitanOperationStatus error = null;
		Either<List<PropertyValueData>, TitanOperationStatus> updateCapabilityPropertyValuesRes = capabilityInstanceOperation.updateCapabilityPropertyValues(resourceInstanceId, capabilityId, propertyValues);
		if (updateCapabilityPropertyValuesRes.isRight()) {
			error = updateCapabilityPropertyValuesRes.right().value();
			log.debug("Failed to update property values of capability {} of resource instance {}. status is {}", capabilityId, resourceInstanceId, error);
		}
		log.debug("After updating property values of capability {} of resource instance {}. Status is {}", capabilityId, resourceInstanceId, error);
		if (error == null) {
			return Either.left(updateCapabilityPropertyValuesRes.left().value());
		}
		return Either.right(error);
	}

	/**
	 * delete property values of capability from resource instance
	 * 
	 * @param capabilityId
	 * @param resourceInstanceId
	 * @return
	 */
	public Either<CapabilityInstData, TitanOperationStatus> deletePropertyValuesOfCapabilityFromResourceInstance(String capabilityId, String resourceInstanceId) {
		log.debug("Before deleting property values of capability {} from resource instance {}.", capabilityId, resourceInstanceId);
		TitanOperationStatus error = null;
		Either<CapabilityInstData, TitanOperationStatus> deleteCapInstWithPropertiesRes = null;
		Either<CapabilityInstData, TitanOperationStatus> getCapInstByCapabilityRes = capabilityInstanceOperation.getCapabilityInstanceOfCapabilityOfResourceInstance(resourceInstanceId, capabilityId);
		if (getCapInstByCapabilityRes.isRight()) {
			error = getCapInstByCapabilityRes.right().value();
			log.debug("Failed to retrieve capability instance of capability {} of resource instance {}. status is {}", capabilityId, resourceInstanceId, error);
		}
		if (error == null) {
			String capabilityInstanceId = getCapInstByCapabilityRes.left().value().getUniqueId();
			deleteCapInstWithPropertiesRes = capabilityInstanceOperation.deleteCapabilityInstanceFromResourceInstance(resourceInstanceId, capabilityInstanceId);
			if (deleteCapInstWithPropertiesRes.isRight()) {
				error = deleteCapInstWithPropertiesRes.right().value();
				log.debug("Failed to delete capability instance {} to resource instance {}. status is {}", capabilityInstanceId, resourceInstanceId, error);
			}
		}
		log.debug("After deleting property values of capability {} from resource instance {}. Status is {}", capabilityId, resourceInstanceId, error);
		if (error == null) {
			return Either.left(deleteCapInstWithPropertiesRes.left().value());
		}
		return Either.right(error);
	}

	/**
	 * clone capability instances of resource instance
	 * 
	 * @param createdComponentInstance
	 * @param resourceInstance
	 * @return
	 */
	private Either<Map<ImmutablePair<CapabilityInstData, GraphEdge>, List<PropertyValueData>>, TitanOperationStatus> cloneCapabilityInstancesOfResourceInstance(ComponentInstanceData createdComponentInstance, ComponentInstance resourceInstance) {
		TitanOperationStatus error = null;
		String resourceInstanceId = resourceInstance.getUniqueId();
		log.debug("Before cloning of capability instances of resource instance {}.", resourceInstanceId);

		Map<ImmutablePair<CapabilityInstData, GraphEdge>, List<PropertyValueData>> result = new HashMap<>();
		Either<ImmutablePair<CapabilityInstData, List<PropertyValueData>>, TitanOperationStatus> cloneAssociateCIWithPropertyValuesRes;
		Either<List<ImmutablePair<CapabilityInstData, GraphEdge>>, TitanOperationStatus> getAllCapabilityInstancesRes = capabilityInstanceOperation.getAllCapabilityInstancesOfResourceInstance(resourceInstanceId);
		if (getAllCapabilityInstancesRes.isRight() && !getAllCapabilityInstancesRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
			error = getAllCapabilityInstancesRes.right().value();
			log.debug("Failed to get capability instances of component instance {}. status is {}", resourceInstanceId, error);
		}
		if (getAllCapabilityInstancesRes.isLeft()) {
			List<ImmutablePair<CapabilityInstData, GraphEdge>> capabilityInstances = getAllCapabilityInstancesRes.left().value();
			Map<String, List<CapabilityDefinition>> allCapabilitiesMap = resourceInstance.getCapabilities();
			List<CapabilityDefinition> allCapabilitiesList = new ArrayList<>();
			for (List<CapabilityDefinition> curList : allCapabilitiesMap.values()) {
				allCapabilitiesList.addAll(curList);
			}
			Map<String, CapabilityDefinition> capabilities = allCapabilitiesList.stream().collect(Collectors.toMap(CapabilityDefinition::getUniqueId, Function.identity()));
			String propertyName = GraphPropertiesDictionary.CAPABILITY_ID.getProperty();
			for (ImmutablePair<CapabilityInstData, GraphEdge> capabilityInstPair : capabilityInstances) {
				String capabilityId = (String) capabilityInstPair.getRight().getProperties().get(propertyName);
				CapabilityDefinition relatedCapability = capabilities.get(capabilityId);
				cloneAssociateCIWithPropertyValuesRes = capabilityInstanceOperation.cloneAssociateCapabilityInstanceWithPropertyValues(createdComponentInstance, relatedCapability, capabilityInstPair);
				if (cloneAssociateCIWithPropertyValuesRes.isRight()) {
					error = cloneAssociateCIWithPropertyValuesRes.right().value();
					log.debug("Failed to clone capability instances {} of component instance {}. status is {}", capabilityInstPair.getLeft().getUniqueId(), resourceInstanceId, error);
					break;
				} else {
					result.put(new ImmutablePair<CapabilityInstData, GraphEdge>(cloneAssociateCIWithPropertyValuesRes.left().value().getLeft(), capabilityInstPair.getRight()), cloneAssociateCIWithPropertyValuesRes.left().value().getRight());
				}
			}
		}
		log.debug("After cloning of capability instance of resource instance {}. Status is {}", resourceInstanceId, error);
		if (error == null) {
			return Either.left(result);
		}
		return Either.right(error);
	}

	private Either<List<ImmutablePair<TitanVertex, GraphEdge>>, TitanOperationStatus> cloneCapabilityInstancesOfResourceInstance(TitanVertex componentInstanceVertex, ComponentInstance resourceInstance) {
		TitanOperationStatus error = null;
		String resourceInstanceId = resourceInstance.getUniqueId();
		log.debug("Before cloning of capability instances of resource instance {}.", resourceInstanceId);

		Either<TitanVertex, TitanOperationStatus> cloneAssociateCIWithPropertyValuesRes = null;
		Either<List<ImmutablePair<CapabilityInstData, GraphEdge>>, TitanOperationStatus> getAllCapabilityInstancesRes = capabilityInstanceOperation.getAllCapabilityInstancesOfResourceInstance(resourceInstanceId);
		if (getAllCapabilityInstancesRes.isRight() && getAllCapabilityInstancesRes.right().value() != TitanOperationStatus.NOT_FOUND) {
			error = getAllCapabilityInstancesRes.right().value();
			log.debug("Failed to get capability instances of component instance {}. status is {}", resourceInstanceId, error);
		}
		List<ImmutablePair<TitanVertex, GraphEdge>> list = new ArrayList<>();
		if (getAllCapabilityInstancesRes.isLeft()) {
			List<ImmutablePair<CapabilityInstData, GraphEdge>> capabilityInstances = getAllCapabilityInstancesRes.left().value();
			Map<String, List<CapabilityDefinition>> allCapabilitiesMap = resourceInstance.getCapabilities();
			List<CapabilityDefinition> allCapabilitiesList = new ArrayList<>();
			for (List<CapabilityDefinition> curList : allCapabilitiesMap.values()) {
				allCapabilitiesList.addAll(curList);
			}
			Map<String, CapabilityDefinition> capabilities = allCapabilitiesList.stream().collect(Collectors.toMap(CapabilityDefinition::getUniqueId, Function.identity()));
			String propertyName = GraphPropertiesDictionary.CAPABILITY_ID.getProperty();
			for (ImmutablePair<CapabilityInstData, GraphEdge> capabilityInstPair : capabilityInstances) {
				String capabilityId = (String) capabilityInstPair.getRight().getProperties().get(propertyName);
				CapabilityDefinition relatedCapability = capabilities.get(capabilityId);
				cloneAssociateCIWithPropertyValuesRes = capabilityInstanceOperation.cloneAssociateCapabilityInstanceWithPropertyValues(componentInstanceVertex, relatedCapability, capabilityInstPair);
				if (cloneAssociateCIWithPropertyValuesRes.isRight()) {
					error = cloneAssociateCIWithPropertyValuesRes.right().value();
					log.debug("Failed to clone capability instances {} of component instance {}. status is {}", capabilityInstPair.getLeft().getUniqueId(), resourceInstanceId, error);
					break;
				} else {
					list.add(new ImmutablePair<TitanVertex, GraphEdge>(cloneAssociateCIWithPropertyValuesRes.left().value(), capabilityInstPair.right));
				}
			}
		}
		log.debug("After cloning of capability instance of resource instance {}. Status is {}", resourceInstanceId, error);
		if (error == null) {
			return Either.left(list);
		}
		return Either.right(error);
	}

	public Either<List<ComponentInstance>, StorageOperationStatus> getAllComponentInstancesMetadataOnly(String componentId, NodeTypeEnum containerNodeType) {

		List<ComponentInstance> componentInstancesResult = new ArrayList<ComponentInstance>();
		Either<List<ComponentInstance>, StorageOperationStatus> result = Either.left(componentInstancesResult);

		Either<List<ImmutablePair<ComponentInstanceData, GraphEdge>>, TitanOperationStatus> resourceInstancesRes = getAllComponentInstanceFromGraph(componentId, containerNodeType, false);

		if (resourceInstancesRes.isRight()) {

			if (log.isDebugEnabled()) {
				log.debug("Resource instance was found under service {} . status is {} ", componentId, resourceInstancesRes.right().value());
			}
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(resourceInstancesRes.right().value()));
		}

		List<ImmutablePair<ComponentInstanceData, GraphEdge>> resourceInstances = resourceInstancesRes.left().value();
		if (resourceInstances != null && false == resourceInstances.isEmpty()) {

			for (ImmutablePair<ComponentInstanceData, GraphEdge> immutablePair : resourceInstances) {
				ComponentInstanceData resourceInstanceData = immutablePair.getKey();
				if (log.isDebugEnabled()) {
					log.debug("Going to fetch the relationships of resource instance {}", resourceInstanceData);
				}
				componentInstancesResult.add(new ComponentInstance(resourceInstanceData.getComponentInstDataDefinition()));

			}
		}

		return result;
	}

	public Either<List<CapabilityDefinition>, TitanOperationStatus> updateCapDefPropertyValues(ComponentInstance componentInstance, List<CapabilityDefinition> capabilityDefList) {
		String componentInstanceId = componentInstance.getUniqueId();
		log.debug("Before updating property values of capabilities of component istance {}.", componentInstanceId);
		TitanOperationStatus error = null;
		NodeTypeEnum nodeType = NodeTypeEnum.getByNameIgnoreCase(componentInstance.getOriginType().getInstanceType().trim());

		log.debug("Before getting all capability instances of component istance {}.", componentInstanceId);
		Either<List<ImmutablePair<CapabilityInstData, GraphEdge>>, TitanOperationStatus> getCapabilityInstancesRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeType), componentInstanceId, GraphEdgeLabels.CAPABILITY_INST,
				NodeTypeEnum.CapabilityInst, CapabilityInstData.class);
		if (getCapabilityInstancesRes.isRight() && !getCapabilityInstancesRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
			error = getCapabilityInstancesRes.right().value();
			log.debug("Failed to retrieve capability Instances of resource instance {}. Status is {}", componentInstance.getName(), error);
		}
		log.debug("After getting all capability instances of component istance {}. Status is {}", componentInstanceId, error);
		Map<String, Map<String, PropertyValueData>> overridedCapabilitiesHM = new HashMap<>();
		if (getCapabilityInstancesRes.isLeft()) {
			List<ImmutablePair<CapabilityInstData, GraphEdge>> capabilityInstDataPair = getCapabilityInstancesRes.left().value();

			for (ImmutablePair<CapabilityInstData, GraphEdge> curCapabilityPair : capabilityInstDataPair) {
				CapabilityInstData curCapabilityInst = curCapabilityPair.getLeft();
				String curCapInstUid = curCapabilityInst.getUniqueId();

				log.debug("Before getting all property values of capability instance {} of component istance {}.", curCapInstUid, componentInstanceId);
				Either<List<ImmutablePair<PropertyValueData, GraphEdge>>, TitanOperationStatus> getOverridedPropertyValuesRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.getByName(curCapabilityInst.getLabel())),
						curCapInstUid, GraphEdgeLabels.PROPERTY_VALUE, NodeTypeEnum.PropertyValue, PropertyValueData.class);
				if (getOverridedPropertyValuesRes.isRight()) {
					error = getOverridedPropertyValuesRes.right().value();
					log.debug("Failed to retrieve property values of capability instance {}. Status is {}", curCapInstUid, error);
				} else {				
					log.debug("After getting all property values of capability instance {} of component istance {}. Status is {}", curCapInstUid, componentInstanceId, error);
					Map<String, PropertyValueData> overridedPropertyValuesHM = new HashMap<>();
					List<ImmutablePair<PropertyValueData, GraphEdge>> overridedPropertyValues = getOverridedPropertyValuesRes.left().value();
					for (ImmutablePair<PropertyValueData, GraphEdge> curPropertyValuePair : overridedPropertyValues) {
						PropertyValueData curPropertyValue = curPropertyValuePair.getLeft();
						String propertyValueUid = curPropertyValue.getUniqueId();
						log.debug("Before getting property related to property value {} of capability instance {} of component istance {}.", propertyValueUid, curCapInstUid, componentInstanceId);
						Either<ImmutablePair<PropertyData, GraphEdge>, TitanOperationStatus> getPropertyDataRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.getByName(curPropertyValue.getLabel())), propertyValueUid,
								GraphEdgeLabels.PROPERTY_IMPL, NodeTypeEnum.Property, PropertyData.class);
						if (getPropertyDataRes.isRight()) {
							error = getOverridedPropertyValuesRes.right().value();
							log.debug("Failed to retrieve property of property value {} Status is {}", propertyValueUid, error);
						}
						log.debug("After getting property related to property value {} of capability instance {} of component istance {}. Status is {}", propertyValueUid, curCapInstUid, componentInstanceId, error);
						PropertyData propertyData = getPropertyDataRes.left().value().getLeft();
						overridedPropertyValuesHM.put((String) propertyData.getUniqueId(), curPropertyValue);
					}
					overridedCapabilitiesHM.put((String) curCapabilityPair.getRight().getProperties().get(GraphPropertiesDictionary.CAPABILITY_ID.getProperty()), overridedPropertyValuesHM);
				}
			}
		}
		if (error == null && !overridedCapabilitiesHM.isEmpty()) {
			updateCapabilityPropertyValues(componentInstance.getCapabilities(), capabilityDefList, overridedCapabilitiesHM);
		}
		log.debug("After updating property values of capabilities of component istance {}. Status is {}", componentInstanceId, error);
		if (error == null) {
			return Either.left(capabilityDefList);
		}
		return Either.right(error);
	}

	private void updateCapabilityPropertyValues(Map<String, List<CapabilityDefinition>> capabilitiesOfRI, List<CapabilityDefinition> capabilitiesOfContainer, Map<String, Map<String, PropertyValueData>> overridedCapabilitiesHM) {

		capabilitiesOfContainer.stream().filter(capability -> overridedCapabilitiesHM.containsKey(capability.getUniqueId())).forEach(capability -> {
			boolean updateProperties = false;
			for (ComponentInstanceProperty property : capability.getProperties()) {
				if (overridedCapabilitiesHM.get(capability.getUniqueId()).containsKey(property.getUniqueId())) {
					property.setValue(overridedCapabilitiesHM.get(capability.getUniqueId()).get(property.getUniqueId()).getValue());
					property.setValueUniqueUid(overridedCapabilitiesHM.get(capability.getUniqueId()).get(property.getUniqueId()).getUniqueId());
					updateProperties = true;
				}
			}
			if (updateProperties) {
				capabilitiesOfRI.get(capability.getType()).get(0).setProperties(capability.getProperties());
			}
		});
	}

	@Override
	public Either<ComponentInstanceInput, StorageOperationStatus> updateInputValueInResourceInstance(ComponentInstanceInput input, String resourceInstanceId, boolean b) {
		return null;
	}

	public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> fetchCIEnvArtifacts(String componentInstanceId) {
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> result = artifactOperation.getArtifacts(componentInstanceId, NodeTypeEnum.ResourceInstance, true, ArtifactGroupTypeEnum.DEPLOYMENT.getType());
		if (result.isRight() && result.right().value() == StorageOperationStatus.NOT_FOUND)
			return Either.right(StorageOperationStatus.OK);
		return result;
	}

	@Override
	public StorageOperationStatus updateCustomizationUUID(String componentInstanceId) {
		Either<TitanVertex, TitanOperationStatus> vertexByProperty = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), componentInstanceId);
		if (vertexByProperty.isRight()) {
			log.debug("Failed to fetch component instance by id {} error {}", componentInstanceId, vertexByProperty.right().value());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(vertexByProperty.right().value());
		}
		UUID uuid = UUID.randomUUID();
		TitanVertex ciVertex = vertexByProperty.left().value();
		ciVertex.property(GraphPropertiesDictionary.CUSTOMIZATION_UUID.getProperty(), uuid.toString());

		return StorageOperationStatus.OK;
	}

	private Either<String, Boolean> handleGroupInstanceNameLogic(TitanVertex ciVertex, GroupInstance groupInstance, String componentInstanceId, String componentInstanceName, String groupName) {

		groupInstance.setGroupName(groupName);

		String logicalName = groupInstanceOperation.createGroupInstLogicalName(componentInstanceName, groupName);

		Boolean eitherValidation = validateGroupInstanceName(logicalName, groupInstance, true);
		if (!eitherValidation) {
			return Either.right(false);
		}
		// groupInstance.setName(logicalName);
		return Either.left(logicalName);
	}

	private Boolean validateGroupInstanceName(String groupInstanceName, GroupInstance groupInstance, boolean isCreate) {

		if (!ValidationUtils.validateStringNotEmpty(groupInstanceName)) {
			return false;
		}
		groupInstance.setNormalizedName(ValidationUtils.normalizeComponentInstanceName(groupInstanceName));
		if (!isCreate) {
			if (!ValidationUtils.validateResourceInstanceNameLength(groupInstanceName)) {
				return false;
			}
			if (!ValidationUtils.validateResourceInstanceName(groupInstanceName)) {
				return false;
			}
		}

		return true;

	}
	// Evg: need to be public for reuse code in migration
	public Either<GroupInstance, StorageOperationStatus> createGroupInstance(TitanVertex ciVertex, GroupDefinition groupDefinition, ComponentInstance componentInstance) {
		// create VFC instance on VF
		GroupInstance groupInstance = null;

		boolean isCreateName = false;
		List<GroupInstance> groupInstances = componentInstance.getGroupInstances();
		if (groupInstances != null && !groupInstances.isEmpty()) {
			Optional<GroupInstance> op = groupInstances.stream().filter(p -> p.getGroupUid().equals(groupDefinition.getUniqueId())).findAny();
			if (op.isPresent()) {
				groupInstance = op.get();

			}
		}
		if (groupInstance == null) {
			groupInstance = new GroupInstance();
			groupInstance.setGroupUid(groupDefinition.getUniqueId());

			groupInstance.setArtifacts(groupDefinition.getArtifacts());
			Either<String, Boolean> handleNameLogic = handleGroupInstanceNameLogic(ciVertex, groupInstance, componentInstance.getUniqueId(), componentInstance.getNormalizedName(), groupDefinition.getName());
			if (handleNameLogic.isRight() && !handleNameLogic.right().value()) {

				if (handleNameLogic.isRight()) {
					log.debug("failed to create logical name gor group instance {}", groupInstance.getName());
					return Either.right(StorageOperationStatus.INVALID_ID);

				}
			}
			isCreateName = true;
			// groupInstance.setName(handleNameLogic.left().value());

		}

		return groupInstanceOperation.createGroupInstance(ciVertex, componentInstance.getUniqueId(), groupInstance, isCreateName);

	}

	@Override
	public Either<ComponentInstanceData, StorageOperationStatus> updateComponentInstanceModificationTimeAndCustomizationUuidOnGraph(ComponentInstance componentInstance, NodeTypeEnum componentInstanceType, Long modificationTime, boolean inTransaction) {
		
		log.debug("Going to update modification time of component instance {}. ", componentInstance.getName());
		Either<ComponentInstanceData, StorageOperationStatus> result = null;
		try{
			ComponentInstanceData componentData = new ComponentInstanceData(componentInstance, componentInstance.getGroupInstances().size());
			componentData.getComponentInstDataDefinition().setModificationTime(modificationTime);
			componentData.getComponentInstDataDefinition().setCustomizationUUID(UUID.randomUUID().toString());
			Either<ComponentInstanceData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(componentData, ComponentInstanceData.class);
			if (updateNode.isRight()) {
				log.error("Failed to update resource {}. status is {}", componentInstance.getUniqueId(), updateNode.right().value());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateNode.right().value()));
			}else{
				result = Either.left(updateNode.left().value());
			}
		}catch(Exception e){
			log.error("Exception occured during  update modification date of compomemt instance{}. The message is {}. ", componentInstance.getName(), e.getMessage(), e);
			result = Either.right(StorageOperationStatus.GENERAL_ERROR);
		}finally {
			if(!inTransaction){
				if (result == null || result.isRight()) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
		return result;
	}
}
