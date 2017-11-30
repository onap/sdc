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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.IComponentInstanceConnectedElement;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.operations.api.IComponentInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IInputsOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.AttributeData;
import org.openecomp.sdc.be.resources.data.AttributeValueData;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.InputValueData;
import org.openecomp.sdc.be.resources.data.InputsData;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@org.springframework.stereotype.Component("component-instance-operation")
public class ComponentInstanceOperation extends AbstractOperation implements IComponentInstanceOperation {

	public ComponentInstanceOperation() {
		super();
	}

	private static Logger log = LoggerFactory.getLogger(ComponentInstanceOperation.class.getName());

	@Autowired
	TitanGenericDao titanGenericDao;

	@Autowired
	PropertyOperation propertyOperation;

	@Autowired
	private IInputsOperation inputOperation;

	@Autowired
	private ApplicationDataTypeCache dataTypeCache;

	/**
	 * FOR TEST ONLY
	 * 
	 * @param titanGenericDao
	 */
	public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
	}

	@Override
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

	private void connectAttValueDataToComponentInstanceData(Wrapper<TitanOperationStatus> errorWrapper, ComponentInstanceData compIns, AttributeValueData attValueData) {

		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(compIns, attValueData, GraphEdgeLabels.ATTRIBUTE_VALUE, null);

		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			errorWrapper.setInnerElement(operationStatus);
			BeEcompErrorManager.getInstance().logInternalFlowError("connectAttValueDataToComponentInstanceData",
					"Failed to associate resource instance " + compIns.getUniqueId() + " attribute value " + attValueData.getUniqueId() + " in graph. status is " + operationStatus, ErrorSeverity.ERROR);
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
	 * @param resourceInstanceId
	 * @param index
	 * @return
	 */
	private Either<InputValueData, TitanOperationStatus> addInputToResourceInstance(ComponentInstanceInput resourceInstanceInput, String resourceInstanceId, Integer index) {

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

				ComponentInstanceProperty attributeValueResult = buildResourceInstanceAttribute(attributeValueData, resourceInstanceAttribute);
				log.debug("The returned ResourceInstanceAttribute is {}", attributeValueResult);

				result = Either.left(attributeValueResult);
				return result;
			}
		}

		finally {
			handleTransactionCommitRollback(inTransaction, result);
		}
	}

	private ComponentInstanceProperty buildResourceInstanceAttribute(AttributeValueData attributeValueData, ComponentInstanceProperty resourceInstanceAttribute) {
		Boolean hidden = attributeValueData.isHidden();
		String uid = attributeValueData.getUniqueId();
		return new ComponentInstanceProperty(hidden, resourceInstanceAttribute, uid);
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

				ComponentInstanceProperty attributeValueResult = buildResourceInstanceAttribute(attributeValueData, resourceInstanceAttribute);
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

	@Override
	public Either<ComponentInstanceInput, StorageOperationStatus> updateInputValueInResourceInstance(ComponentInstanceInput input, String resourceInstanceId, boolean b) {
		return null;
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
