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
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.json.Json;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
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
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;

import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.IInputsOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.AttributeData;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.InputValueData;
import org.openecomp.sdc.be.resources.data.InputsData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.TitanVertexQuery;
import com.thinkaurelius.titan.core.attribute.Cmp;

import fj.data.Either;

@Component("input-operation")
public class InputsOperation extends AbstractOperation implements IInputsOperation {

	private static final String GET_INPUT = "get_input";

	private static String ASSOCIATING_INPUT_TO_PROP = "AssociatingInputToComponentInstanceProperty";

	private static Logger log = LoggerFactory.getLogger(InputsOperation.class.getName());
	@Autowired
	PropertyOperation propertyOperation;

	@Autowired
	private ComponentInstanceOperation componentInstanceOperation;
	Gson gson = new Gson();

	/**
	 * Delete specific input from component Although inputId is unique, pass also componentId as all other methods, and also check that the inputId is inside that componentId.
	 */
	@Override
	public Either<InputDefinition, StorageOperationStatus> deleteInput(String inputId) {
		log.debug(String.format("Before deleting input: %s from graph", inputId));
		List<ComponentInstanceInput> inputsValueList = null;
		List<ComponentInstanceProperty> propertyList = new ArrayList<>();
		
		Either<TitanVertex, TitanOperationStatus> vertexService = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), inputId);

		if (vertexService.isRight()) {
			log.debug("failed to fetch vertex of resource input for id = {}", inputId);
			TitanOperationStatus status = vertexService.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}

			StorageOperationStatus convertTitanStatusToStorageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
			return Either.right(convertTitanStatusToStorageStatus);
		}
		TitanVertex vertex = vertexService.left().value();
		Iterator<Edge> edgeIter = vertex.edges(Direction.IN, GraphEdgeLabels.INPUT.getProperty());
		
		if (edgeIter == null) {
			log.debug("No edges in graph for criteria");
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.NOT_FOUND));
		}
		String inputName = "";
		if (edgeIter != null) {
			while (edgeIter.hasNext()) {
				Edge edge = edgeIter.next();
				GraphEdge graphEdge = null;

				Map<String, Object> edgeProps = titanGenericDao.getProperties(edge);
				GraphEdgeLabels edgeTypeFromGraph = GraphEdgeLabels.getByName(edge.label());
				graphEdge = new GraphEdge(edgeTypeFromGraph, edgeProps);
				
				inputName = (String) graphEdge.getProperties().get(GraphEdgePropertiesDictionary.NAME.getProperty());
				
			}
		}
		
		
		Either<List<ComponentInstanceInput>, TitanOperationStatus> inputsValueStatus = this.getComponentInstanceInputsByInputId(inputId);
		if(inputsValueStatus.isLeft()){
			inputsValueList = inputsValueStatus.left().value();
			if(!inputsValueList.isEmpty()){	
				for(ComponentInstanceInput inputValue: inputsValueList){					
					Either<InputValueData, TitanOperationStatus> deleteNode = titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.InputValue), inputValue.getValueUniqueUid(), InputValueData.class);
					if (deleteNode.isRight()) {
						StorageOperationStatus convertTitanStatusToStorageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(deleteNode.right().value());
						return Either.right(convertTitanStatusToStorageStatus);
					} 		
				}
			}
		// US848813 delete service input that relates to VL / CP property
		} else {
			Either<List<ComponentInstanceProperty>, TitanOperationStatus> propertyValueStatus = getComponentInstancePropertiesByInputId(inputId);
			if(propertyValueStatus.isLeft() && !propertyValueStatus.left().value().isEmpty()){
				//propertyList = propertyValueStatus.left().value();	
				for(ComponentInstanceProperty propertyValue: propertyValueStatus.left().value()){
			
					String value = propertyValue.getValue();
					Map<String, Object> mappedToscaTemplate = (Map<String, Object>) new Yaml().load(value);
					
					resetInputName(mappedToscaTemplate, inputName);
					
					value = gson.toJson(mappedToscaTemplate);
					propertyValue.setValue(value);
					String compInstId = propertyValue.getComponentInstanceId();
					propertyValue.setRules(null);
				
					Either<PropertyValueData, TitanOperationStatus> eitherStatus = componentInstanceOperation.updatePropertyOfResourceInstance(propertyValue, compInstId, false);

					if (eitherStatus.isRight()) {
						log.error("Failed to add property value {} to resource instance {} in Graph. status is {}", propertyValue, compInstId, eitherStatus.right().value().name());
						return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherStatus.right().value()));
						
					} else {
						PropertyValueData propertyValueData = eitherStatus.left().value();

						ComponentInstanceProperty propertyValueResult = propertyOperation.buildResourceInstanceProperty(propertyValueData, propertyValue);

						log.debug("The returned ResourceInstanceProperty is {}", propertyValueResult);

						Either<String, TitanOperationStatus> findDefaultValue = propertyOperation.findDefaultValueFromSecondPosition(propertyValue.getPath(), propertyValueData.getUniqueId(), propertyValue.getDefaultValue());
						if (findDefaultValue.isRight()) {
							return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(findDefaultValue.right().value()));
							
						}
						String defaultValue = findDefaultValue.left().value();
						propertyValueResult.setDefaultValue(defaultValue);
						log.debug("The returned default value in ResourceInstanceProperty is {}", defaultValue);
						
						propertyValueResult.setComponentInstanceId(compInstId);
						propertyList.add(propertyValueResult);

						
					}
					
				}
			}
		}
		Either<InputsData, TitanOperationStatus> deleteNode = titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Input), inputId, InputsData.class);
		if (deleteNode.isRight()) {
			StorageOperationStatus convertTitanStatusToStorageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(deleteNode.right().value());
			return Either.right(convertTitanStatusToStorageStatus);
		} else {
			InputDefinition inputDefinition = this.convertInputDataToInputDefinition(deleteNode.left().value());
			inputDefinition.setInputs(inputsValueList);
			inputDefinition.setProperties(propertyList);
			inputDefinition.setName(inputName);
			return Either.left(inputDefinition);
		}
	}

	@Override
	public Either<List<InputDefinition>, TitanOperationStatus> addInputsToGraph(String componentId, NodeTypeEnum nodeType, Map<String, InputDefinition> inputs, Map<String, DataTypeDefinition> dataTypes) {

		List<InputDefinition> newInputs = new ArrayList<InputDefinition>();
		if (inputs != null) {
			for (Entry<String, InputDefinition> entry : inputs.entrySet()) {

				String inputName = entry.getKey();
				InputDefinition propertyDefinition = entry.getValue();

				StorageOperationStatus validateAndUpdateProperty = validateAndUpdateProperty(propertyDefinition, dataTypes);
				if (validateAndUpdateProperty != StorageOperationStatus.OK) {
					log.error("Property {} is invalid. Status is {}", propertyDefinition, validateAndUpdateProperty);
					return Either.right(TitanOperationStatus.INVALID_PROPERTY);
				}

				Either<InputsData, TitanOperationStatus> addPropertyToGraph = addInputToGraph(inputName, propertyDefinition, componentId, nodeType);

				if (addPropertyToGraph.isRight()) {
					return Either.right(addPropertyToGraph.right().value());
				}
				InputDefinition createdInputyDefinition = convertInputDataToInputDefinition(addPropertyToGraph.left().value());
				createdInputyDefinition.setName(inputName);
				createdInputyDefinition.setParentUniqueId(componentId);
				newInputs.add(createdInputyDefinition);
			}
		}

		return Either.left(newInputs);
	}

	@Override
	public TitanOperationStatus addInputsToGraph(TitanVertex metadata, String componentId, Map<String, InputDefinition> inputs, Map<String, DataTypeDefinition> dataTypes) {

		if (inputs != null) {
			for (Entry<String, InputDefinition> entry : inputs.entrySet()) {

				String inputName = entry.getKey();
				InputDefinition propertyDefinition = entry.getValue();

				StorageOperationStatus validateAndUpdateProperty = validateAndUpdateProperty(propertyDefinition, dataTypes);
				if (validateAndUpdateProperty != StorageOperationStatus.OK) {
					log.error("Property {} is invalid. Status is {} ", propertyDefinition, validateAndUpdateProperty);
					return TitanOperationStatus.INVALID_PROPERTY;
				}

				TitanOperationStatus addPropertyToGraph = addInputToGraph(metadata, inputName, propertyDefinition, componentId);

				if (!addPropertyToGraph.equals(TitanOperationStatus.OK)) {
					return addPropertyToGraph;
				}

			}
		}

		return TitanOperationStatus.OK;
	}

	@Override
	public Either<List<InputDefinition>, StorageOperationStatus> getInputsOfComponent(String compId, String fromName, int amount) {
		List<InputDefinition> inputs = new ArrayList<>();
		if ((fromName == null || fromName.isEmpty()) && amount == 0) {

			TitanOperationStatus status = findAllResourceInputs(compId, inputs);

			if (status != TitanOperationStatus.OK) {
				log.error("Failed to set inputs of resource {}. status is {}", compId, status);
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			}

		} else {

			Either<TitanGraph, TitanOperationStatus> graphRes = titanGenericDao.getGraph();
			if (graphRes.isRight()) {
				log.error("Failed to retrieve graph. status is {}", graphRes);
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(graphRes.right().value()));
			}

			TitanGraph titanGraph = graphRes.left().value();
			@SuppressWarnings("unchecked")
			Iterable<TitanVertex> vertices = titanGraph.query().has(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), compId).vertices();
			if (vertices == null || false == vertices.iterator().hasNext()) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.INVALID_ID));
			}

			TitanVertex rootVertex = vertices.iterator().next();
			TitanVertexQuery<?> query;
			if (fromName == null || fromName.isEmpty())
				query = rootVertex.query().direction(Direction.OUT).labels(GraphEdgeLabels.INPUT.getProperty()).orderBy(GraphEdgePropertiesDictionary.NAME.getProperty(), Order.incr).limit(amount);
			else
				query = rootVertex.query().direction(Direction.OUT).labels(GraphEdgeLabels.INPUT.getProperty()).orderBy(GraphEdgePropertiesDictionary.NAME.getProperty(), Order.incr).has(GraphEdgePropertiesDictionary.NAME.getProperty(), Cmp.GREATER_THAN, fromName).limit(amount);

			Iterable<TitanEdge> edgesCreatorEges = query.edges();

			if (edgesCreatorEges == null) {
				log.debug("No edges in graph for criteria");
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.NOT_FOUND));
			}
			Iterator<TitanEdge> edgesCreatorIterator = edgesCreatorEges.iterator();

			if (edgesCreatorIterator != null) {
				while (edgesCreatorIterator.hasNext()) {
					Edge edge = edgesCreatorIterator.next();
					GraphEdge graphEdge = null;

					Map<String, Object> edgeProps = titanGenericDao.getProperties(edge);
					GraphEdgeLabels edgeTypeFromGraph = GraphEdgeLabels.getByName(edge.label());
					graphEdge = new GraphEdge(edgeTypeFromGraph, edgeProps);

					Vertex outgoingVertex = edge.inVertex();
					Map<String, Object> properties = titanGenericDao.getProperties(outgoingVertex);
					InputsData data = GraphElementFactory.createElement(NodeTypeEnum.Input.getName(), GraphElementTypeEnum.Node, properties, InputsData.class);
					String inputName = (String) graphEdge.getProperties().get(GraphEdgePropertiesDictionary.NAME.getProperty());
					InputDefinition inputDefinition = this.convertInputDataToInputDefinition(data);
					inputDefinition.setName(inputName);
					inputDefinition.setParentUniqueId(compId);

					inputs.add(inputDefinition);

				}
			}

		}
		if (true == inputs.isEmpty()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.NOT_FOUND));
		}

		return Either.left(inputs);

	}

	@Override
	public Either<Map<String, InputDefinition>, StorageOperationStatus> deleteAllInputsAssociatedToNode(NodeTypeEnum nodeType, String uniqueId) {

		Wrapper<TitanOperationStatus> errorWrapper;
		List<InputDefinition> inputs = new ArrayList<>();
		TitanOperationStatus findAllResourceAttribues = this.findNodeNonInheretedInputs(uniqueId, inputs);
		errorWrapper = (findAllResourceAttribues != TitanOperationStatus.OK) ? new Wrapper<>(findAllResourceAttribues) : new Wrapper<>();

		if (errorWrapper.isEmpty()) {
			for (InputDefinition inDef : inputs) {
				log.debug("Before deleting inputs from graph {}", inDef.getUniqueId());
				Either<InputsData, TitanOperationStatus> deleteNode = titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Input), inDef.getUniqueId(), InputsData.class);
				if (deleteNode.isRight()) {
					errorWrapper.setInnerElement(deleteNode.right().value());
					break;
				}
			}
		}

		if (errorWrapper.isEmpty()) {
			Map<String, InputDefinition> inputsMap = inputs.stream().collect(Collectors.toMap(e -> e.getName(), e -> e));
			return Either.left(inputsMap);
		} else {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(errorWrapper.getInnerElement()));
		}

	}

	@Override
	public Either<InputsData, StorageOperationStatus> addInput(String inputName, InputDefinition inputDefinition, String resourceId, NodeTypeEnum nodeType) {

		ComponentMetadataData componentMetadata = null;

		Either<InputsData, TitanOperationStatus> either = addInputToGraph(inputName, inputDefinition, resourceId, nodeType);
		if (either.isRight()) {
			StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(either.right().value());
			return Either.right(storageStatus);
		}
		return Either.left(either.left().value());
	}

	@Override
	public Either<AttributeData, StorageOperationStatus> updateInput(String inputId, InputDefinition newInDef, Map<String, DataTypeDefinition> dataTypes) {
		// TODO Auto-generated method stub
		return null;
	}

	public Either<InputsData, TitanOperationStatus> addInputToGraph(String propertyName, InputDefinition inputDefinition, String componentId, NodeTypeEnum nodeType) {

		UniqueIdData from = new UniqueIdData(nodeType, componentId);

		List<PropertyConstraint> constraints = inputDefinition.getConstraints();

		inputDefinition.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(componentId, propertyName));
		InputsData inputData = new InputsData(inputDefinition, convertConstraintsToString(constraints));

		log.debug("Before adding property to graph {}", inputData);
		Either<InputsData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(inputData, InputsData.class);
		log.debug("After adding input to graph {}", inputData);
		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to add input {} to graph. status is {}", propertyName, operationStatus);
			if(operationStatus == TitanOperationStatus.TITAN_SCHEMA_VIOLATION )
				return Either.right(TitanOperationStatus.ALREADY_EXIST);			
			return Either.right(operationStatus);
		}

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), propertyName);

		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(from, inputData, GraphEdgeLabels.INPUT, props);
		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to associate resource {} to property {} in graph. status is {}", componentId, propertyName, operationStatus);
			return Either.right(operationStatus);
		}

		return Either.left(createNodeResult.left().value());

	}

	public TitanOperationStatus addInputToGraph(TitanVertex vertex, String propertyName, InputDefinition inputDefinition, String componentId) {

		List<PropertyConstraint> constraints = inputDefinition.getConstraints();

		inputDefinition.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(componentId, propertyName));
		InputsData inputData = new InputsData(inputDefinition, convertConstraintsToString(constraints));

		log.debug("Before adding property to graph {}", inputData);
		Either<TitanVertex, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(inputData);
		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to add input {} to graph. status is {}", propertyName, operationStatus);
			return operationStatus;
		}

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), propertyName);
		TitanVertex inputVertex = createNodeResult.left().value();
		TitanOperationStatus createRelResult = titanGenericDao.createEdge(vertex, inputVertex, GraphEdgeLabels.INPUT, props);
		if (!createRelResult.equals(TitanOperationStatus.OK)) {
			TitanOperationStatus operationStatus = createRelResult;
			log.error("Failed to associate resource {} to property {} in graph. status is {}", componentId, propertyName, operationStatus);
			return operationStatus;
		}

		return createRelResult;

	}

	public InputDefinition convertInputDataToInputDefinition(InputsData inputDataResult) {
		if (log.isDebugEnabled())
			log.debug("The object returned after create property is {}", inputDataResult);

		InputDefinition propertyDefResult = new InputDefinition(inputDataResult.getPropertyDataDefinition());
		propertyDefResult.setConstraints(convertConstraints(inputDataResult.getConstraints()));

		return propertyDefResult;
	}

	public boolean isInputExist(List<InputDefinition> inputs, String resourceUid, String inputName) {

		if (inputs == null) {
			return false;
		}

		for (InputDefinition propertyDefinition : inputs) {
			String parentUniqueId = propertyDefinition.getParentUniqueId();
			String name = propertyDefinition.getName();

			if (parentUniqueId.equals(resourceUid) && name.equals(inputName)) {
				return true;
			}
		}

		return false;

	}

	@Override
	public TitanOperationStatus findAllResourceInputs(String uniqueId, List<InputDefinition> inputs) {
		// final NodeElementFetcher<InputDefinition> singleNodeFetcher =
		// (resourceIdParam, attributesParam) ->
		// findNodeNonInheretedInputs(resourceIdParam, componentType,
		// attributesParam);
		// return findAllResourceElementsDefinitionRecursively(uniqueId, inputs,
		// singleNodeFetcher);
		return findNodeNonInheretedInputs(uniqueId, inputs);
	}

	@Override
	public TitanOperationStatus findNodeNonInheretedInputs(String uniqueId, List<InputDefinition> inputs) {
		Either<List<ImmutablePair<InputsData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), uniqueId, GraphEdgeLabels.INPUT, NodeTypeEnum.Input, InputsData.class);

		if (childrenNodes.isRight()) {
			TitanOperationStatus status = childrenNodes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
			}
			return status;
		}

		List<ImmutablePair<InputsData, GraphEdge>> values = childrenNodes.left().value();
		if (values != null) {

			for (ImmutablePair<InputsData, GraphEdge> immutablePair : values) {
				GraphEdge edge = immutablePair.getValue();
				String inputName = (String) edge.getProperties().get(GraphEdgePropertiesDictionary.NAME.getProperty());
				log.debug("Input {}  is associated to node {}", inputName, uniqueId);
				InputsData inputData = immutablePair.getKey();
				InputDefinition inputDefinition = this.convertInputDataToInputDefinition(inputData);

				inputDefinition.setName(inputName);
				inputDefinition.setParentUniqueId(uniqueId);

				inputs.add(inputDefinition);

				log.trace("findInputsOfNode - input {}  associated to node {}", inputDefinition, uniqueId);
			}

		}

		return TitanOperationStatus.OK;
	}

	public Either<InputDefinition, StorageOperationStatus> getInputById(String uniqueId, boolean skipProperties, boolean skipInputsvalue) {
		Either<InputDefinition, TitanOperationStatus> status = getInputFromGraph(uniqueId, skipProperties, skipInputsvalue);

		if (status.isRight()) {
			log.error("Failed to get input {} from graph {}. status is {}", uniqueId, status);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status.right().value()));
		}

		return Either.left(status.left().value());

	}

	public <ElementDefinition> TitanOperationStatus findAllResourceElementsDefinitionRecursively(String resourceId, List<ElementDefinition> elements, NodeElementFetcher<ElementDefinition> singleNodeFetcher) {

		log.trace("Going to fetch elements under resource {}" , resourceId);
		TitanOperationStatus resourceAttributesStatus = singleNodeFetcher.findAllNodeElements(resourceId, elements);

		if (resourceAttributesStatus != TitanOperationStatus.OK) {
			return resourceAttributesStatus;
		}

		Either<ImmutablePair<ResourceMetadataData, GraphEdge>, TitanOperationStatus> parentNodes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Resource, ResourceMetadataData.class);

		if (parentNodes.isRight()) {
			TitanOperationStatus parentNodesStatus = parentNodes.right().value();
			if (parentNodesStatus != TitanOperationStatus.NOT_FOUND) {
				BeEcompErrorManager.getInstance().logInternalFlowError("findAllResourceElementsDefinitionRecursively", "Failed to find parent elements of resource " + resourceId + ". status is " + parentNodesStatus, ErrorSeverity.ERROR);
				return parentNodesStatus;
			}
		}

		if (parentNodes.isLeft()) {
			ImmutablePair<ResourceMetadataData, GraphEdge> parnetNodePair = parentNodes.left().value();
			String parentUniqueId = parnetNodePair.getKey().getMetadataDataDefinition().getUniqueId();
			TitanOperationStatus addParentIntStatus = findAllResourceElementsDefinitionRecursively(parentUniqueId, elements, singleNodeFetcher);

			if (addParentIntStatus != TitanOperationStatus.OK) {
				BeEcompErrorManager.getInstance().logInternalFlowError("findAllResourceElementsDefinitionRecursively", "Failed to find all resource elements of resource " + parentUniqueId, ErrorSeverity.ERROR);

				return addParentIntStatus;
			}
		}
		return TitanOperationStatus.OK;
	}

	public TitanOperationStatus associatePropertyToInput(String riId, String inputId, InputValueData property, String name) {
		TitanOperationStatus status = TitanOperationStatus.OK;
		Either<TitanGraph, TitanOperationStatus> graphRes = titanGenericDao.getGraph();
		if (graphRes.isRight()) {
			log.error("Failed to retrieve graph. status is {}", graphRes);
			return graphRes.right().value();
		}

		TitanGraph titanGraph = graphRes.left().value();
		@SuppressWarnings("unchecked")
		Iterable<TitanVertex> vertices = titanGraph.query().has(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), property.getUniqueId()).vertices();
		if (vertices == null || false == vertices.iterator().hasNext()) {
			return TitanOperationStatus.INVALID_ID;
		}
		// Either<PropertyData, TitanOperationStatus> findPropertyDefRes =
		// titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property),
		// propertyId, PropertyData.class);

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), name);
		props.put(GraphEdgePropertiesDictionary.OWNER_ID.getProperty(), riId);

		GraphNode inputData = new UniqueIdData(NodeTypeEnum.Input, inputId);
		GraphNode propertyData = new UniqueIdData(NodeTypeEnum.InputValue, property.getUniqueId());
		Either<GraphRelation, TitanOperationStatus> addPropRefResult = titanGenericDao.createRelation(inputData, propertyData, GraphEdgeLabels.GET_INPUT, props);

		if (addPropRefResult.isRight()) {
			status = addPropRefResult.right().value();
			String description = "Failed to associate input " + inputData.getUniqueId() + " to property " + property.getUniqueId() + " in graph. Status is " + status;
			BeEcompErrorManager.getInstance().logInternalFlowError(ASSOCIATING_INPUT_TO_PROP, description, ErrorSeverity.ERROR);
			return status;
		}
		return status;

	}

	public TitanOperationStatus associatePropertyToInput(String riId, String inputId, ComponentInstanceProperty property, GetInputValueDataDefinition getInput) {
		TitanOperationStatus status = TitanOperationStatus.OK;
		Either<TitanGraph, TitanOperationStatus> graphRes = titanGenericDao.getGraph();
		if (graphRes.isRight()) {
			log.error("Failed to retrieve graph. status is {}", graphRes);
			return graphRes.right().value();
		}

		TitanGraph titanGraph = graphRes.left().value();
		@SuppressWarnings("unchecked")
		Iterable<TitanVertex> vertices = titanGraph.query().has(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), property.getUniqueId()).vertices();
		if (vertices == null || false == vertices.iterator().hasNext()) {
			return TitanOperationStatus.INVALID_ID;
		}
		// Either<PropertyData, TitanOperationStatus> findPropertyDefRes =
		// titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Property),
		// propertyId, PropertyData.class);

		Map<String, Object> props = new HashMap<String, Object>();
		if(getInput!=null){
			props.put(GraphEdgePropertiesDictionary.NAME.getProperty(), getInput.getPropName());
			if (getInput.isList()) {
				String index = "";
				if(getInput.getIndexValue()!= null ){
					index = getInput.getIndexValue().toString();
				}else{
					if (getInput.getGetInputIndex() != null) {
						index = getInput.getGetInputIndex().getInputName();
		
					}
				}
				props.put(GraphEdgePropertiesDictionary.GET_INPUT_INDEX.getProperty(), index);
			}
		}
		props.put(GraphEdgePropertiesDictionary.OWNER_ID.getProperty(), riId);

		GraphNode inputData = new UniqueIdData(NodeTypeEnum.Input, inputId);
		GraphNode propertyData = new UniqueIdData(NodeTypeEnum.PropertyValue, property.getValueUniqueUid());
		Either<GraphRelation, TitanOperationStatus> addPropRefResult = titanGenericDao.createRelation(inputData, propertyData, GraphEdgeLabels.GET_INPUT, props);

		if (addPropRefResult.isRight()) {
			status = addPropRefResult.right().value();
			String description = "Failed to associate input " + inputData.getUniqueId() + " to property " + property.getUniqueId() + " in graph. Status is " + status;
			BeEcompErrorManager.getInstance().logInternalFlowError(ASSOCIATING_INPUT_TO_PROP, description, ErrorSeverity.ERROR);
			return status;
		}
		return status;

	}

	private Either<InputDefinition, TitanOperationStatus> getInputFromGraph(String uniqueId, boolean skipProperties, boolean skipInputsValue) {

		Either<InputDefinition, TitanOperationStatus> result = null;

		Either<InputsData, TitanOperationStatus> inputRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Input), uniqueId, InputsData.class);
		if (inputRes.isRight()) {
			TitanOperationStatus status = inputRes.right().value();
			log.debug("Failed to retrieve group {}  from graph. Status is {}", uniqueId, status);
			BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("Fetch Group", uniqueId, String.valueOf(status));
			result = Either.right(status);
			return result;
		}

		InputsData inputData = inputRes.left().value();

		InputDefinition groupDefinition = this.convertInputDataToInputDefinition(inputData);

		if (false == skipInputsValue) {
			List<ComponentInstanceInput> propsList = new ArrayList<ComponentInstanceInput>();

			Either<List<ImmutablePair<InputValueData, GraphEdge>>, TitanOperationStatus> propertyImplNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Input), uniqueId, GraphEdgeLabels.GET_INPUT, NodeTypeEnum.InputValue, InputValueData.class);

			if (propertyImplNodes.isRight()) {
				TitanOperationStatus status = propertyImplNodes.right().value();
				if (status != TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
					return Either.right(status);
				}
				
			}
			if(propertyImplNodes.isLeft()){
				List<ImmutablePair<InputValueData, GraphEdge>> propertyDataPairList = propertyImplNodes.left().value();
				for (ImmutablePair<InputValueData, GraphEdge> propertyValue : propertyDataPairList) {
	
					InputValueData propertyValueData = propertyValue.getLeft();
					String propertyValueUid = propertyValueData.getUniqueId();
					String value = propertyValueData.getValue();
	
					Either<ImmutablePair<PropertyData, GraphEdge>, TitanOperationStatus> propertyDefRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.InputValue), propertyValueUid, GraphEdgeLabels.INPUT_IMPL, NodeTypeEnum.Property, PropertyData.class);
					if (propertyDefRes.isRight()) {
						TitanOperationStatus status = propertyDefRes.right().value();
						if (status == TitanOperationStatus.NOT_FOUND) {
							status = TitanOperationStatus.INVALID_ID;
						}
						return Either.right(status);
					}
	
					ImmutablePair<PropertyData, GraphEdge> propertyDefPair = propertyDefRes.left().value();
					String propertyUniqueId = (String) propertyDefPair.left.getUniqueId();
	
					ComponentInstanceInput resourceInstanceProperty = new ComponentInstanceInput();
					// set property original unique id
					resourceInstanceProperty.setUniqueId(propertyUniqueId);
					// set resource id
					// TODO: esofer add resource id
					resourceInstanceProperty.setParentUniqueId(null);
					// set value
					resourceInstanceProperty.setValue(value);
					// set property value unique id
					resourceInstanceProperty.setValueUniqueUid(propertyValueUid);
					// set rules
					// resourceInstanceProperty.setRules(propertyValueData.getRules());
	
					propsList.add(resourceInstanceProperty);
	
				}
	
				groupDefinition.setInputs(propsList);
			}

		}

		if (false == skipProperties) {
			Either<List<ImmutablePair<PropertyValueData, GraphEdge>>, TitanOperationStatus> propertyImplNodes = titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), uniqueId, GraphEdgeLabels.GET_INPUT, NodeTypeEnum.PropertyValue, PropertyValueData.class);

			if (propertyImplNodes.isRight()) {
				TitanOperationStatus status = propertyImplNodes.right().value();
				return Either.right(status);
			}

			List<ImmutablePair<PropertyValueData, GraphEdge>> list = propertyImplNodes.left().value();

			if (list == null || true == list.isEmpty()) {
				return Either.right(TitanOperationStatus.NOT_FOUND);
			}

			List<ComponentInstanceProperty> propsRresult = new ArrayList<>();
			for (ImmutablePair<PropertyValueData, GraphEdge> propertyValueDataPair : list) {
				PropertyValueData propertyValueData = propertyValueDataPair.left;
				String propertyValueUid = propertyValueData.getUniqueId();
				String value = propertyValueData.getValue();

				Either<ImmutablePair<PropertyData, GraphEdge>, TitanOperationStatus> propertyDefRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.PropertyValue), propertyValueUid, GraphEdgeLabels.PROPERTY_IMPL, NodeTypeEnum.Property, PropertyData.class);
				if (propertyDefRes.isRight()) {
					TitanOperationStatus status = propertyDefRes.right().value();
					if (status != TitanOperationStatus.NOT_FOUND) {
						status = TitanOperationStatus.INVALID_ID;
						return Either.right(status);
					}
					
				}
				if(propertyDefRes.isLeft()){

					ImmutablePair<PropertyData, GraphEdge> propertyDefPair = propertyDefRes.left().value();
					PropertyData propertyData = propertyDefPair.left;
					String propertyUniqueId = (String) propertyData.getPropertyDataDefinition().getUniqueId();
	
					ComponentInstanceProperty resourceInstanceProperty = new ComponentInstanceProperty();
					// set property original unique id
					resourceInstanceProperty.setUniqueId(propertyUniqueId);
					// set resource id
					// TODO: esofer add resource id
					resourceInstanceProperty.setParentUniqueId(null);
					// set value
					resourceInstanceProperty.setValue(value);
					// set property value unique id
					resourceInstanceProperty.setValueUniqueUid(propertyValueData.getUniqueId());
					// set rules
					resourceInstanceProperty.setRules(propertyValueData.getRules());
					resourceInstanceProperty.setType(propertyData.getPropertyDataDefinition().getType());
					resourceInstanceProperty.setSchema(propertyData.getPropertyDataDefinition().getSchema());
					resourceInstanceProperty.setName((String) propertyValueDataPair.right.getProperties().get(GraphPropertiesDictionary.NAME.getProperty()));
	
					propsRresult.add(resourceInstanceProperty);
				}
	
				groupDefinition.setProperties(propsRresult);
			}

		}

		result = Either.left(groupDefinition);

		return result;

	}

	public ImmutablePair<TitanOperationStatus, String> findInputValue(String resourceInstanceId, String propertyId) {

		log.debug("Going to check whether the property {} already added to resource instance {}", propertyId, resourceInstanceId);

		Either<List<ComponentInstanceInput>, TitanOperationStatus> getAllRes = getAllInputsOfResourceInstanceOnlyInputDefId(resourceInstanceId);
		if (getAllRes.isRight()) {
			TitanOperationStatus status = getAllRes.right().value();
			log.trace("After fetching all properties of resource instance {}. Status is {}" ,resourceInstanceId, status);
			return new ImmutablePair<TitanOperationStatus, String>(status, null);
		}

		List<ComponentInstanceInput> list = getAllRes.left().value();
		if (list != null) {
			for (ComponentInstanceInput instanceProperty : list) {
				String propertyUniqueId = instanceProperty.getUniqueId();
				String valueUniqueUid = instanceProperty.getValueUniqueUid();
				log.trace("Go over property {} under resource instance {}. valueUniqueId = {}" ,propertyUniqueId, resourceInstanceId, valueUniqueUid);
				if (propertyId.equals(propertyUniqueId) && valueUniqueUid != null) {
					log.debug("The property {} already created under resource instance {}", propertyId, resourceInstanceId);
					return new ImmutablePair<TitanOperationStatus, String>(TitanOperationStatus.ALREADY_EXIST, valueUniqueUid);
				}
			}
		}

		return new ImmutablePair<TitanOperationStatus, String>(TitanOperationStatus.NOT_FOUND, null);
	}

	/**
	 * return all properties associated to resource instance. The result does contains the property unique id but not its type, default value...
	 * 
	 * @param resourceInstanceUid
	 * @return
	 */
	public Either<List<ComponentInstanceInput>, TitanOperationStatus> getAllInputsOfResourceInstanceOnlyInputDefId(String resourceInstanceUid) {

		return getAllInputsOfResourceInstanceOnlyInputDefId(resourceInstanceUid, NodeTypeEnum.ResourceInstance);

	}

	/**
	 * return all properties associated to resource instance. The result does contains the property unique id but not its type, default value...
	 * 
	 * @param resourceInstanceUid
	 * @return
	 */
	public Either<List<ComponentInstanceInput>, StorageOperationStatus> getComponentInstanceInputsByInputId(String resourceInstanceUid, String inputId) {

		Either<List<ComponentInstanceInput>, TitanOperationStatus> status = getComponentInstanceInputsByInputId(inputId);
		if (status.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status.right().value()));
		}

		return Either.left(status.left().value());

	}

	/**
	 * return all properties associated to resource instance. The result does contains the property unique id but not its type, default value...
	 * 
	 * @param resourceInstanceUid
	 * @return
	 */
	public Either<List<ComponentInstanceProperty>, StorageOperationStatus> getComponentInstancePropertiesByInputId(String resourceInstanceUid, String inputId) {

		Either<List<ComponentInstanceProperty>, TitanOperationStatus> status = getComponentInstancePropertiesByInputId(inputId);
		if (status.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status.right().value()));
		}

		return Either.left(status.left().value());

	}

	public Either<List<ComponentInstanceInput>, TitanOperationStatus> getAllInputsOfResourceInstanceOnlyInputDefId(String resourceInstanceUid, NodeTypeEnum instanceNodeType) {

		Either<ComponentInstanceData, TitanOperationStatus> findResInstanceRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(instanceNodeType), resourceInstanceUid, ComponentInstanceData.class);

		if (findResInstanceRes.isRight()) {
			TitanOperationStatus status = findResInstanceRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		Either<List<ImmutablePair<InputValueData, GraphEdge>>, TitanOperationStatus> propertyImplNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(instanceNodeType), resourceInstanceUid, GraphEdgeLabels.INPUT_VALUE, NodeTypeEnum.InputValue, InputValueData.class);

		if (propertyImplNodes.isRight()) {
			TitanOperationStatus status = propertyImplNodes.right().value();
			return Either.right(status);
		}

		List<ImmutablePair<InputValueData, GraphEdge>> list = propertyImplNodes.left().value();
		if (list == null || true == list.isEmpty()) {
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

		List<ComponentInstanceInput> result = new ArrayList<>();
	
		
		for (ImmutablePair<InputValueData, GraphEdge> propertyValueDataPair : list) {
			
			InputValueData propertyValueData = propertyValueDataPair.getLeft();
			String propertyValueUid = propertyValueData.getUniqueId();
			String value = propertyValueData.getValue();

			Either<ImmutablePair<InputsData, GraphEdge>, TitanOperationStatus> inputNodes = titanGenericDao.getParentNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), propertyValueData.getUniqueId(), GraphEdgeLabels.GET_INPUT, NodeTypeEnum.Input, InputsData.class);

			if (inputNodes.isRight()) {

				return Either.right(inputNodes.right().value());
			}

			InputsData input = inputNodes.left().value().left;
			String inputId = input.getPropertyDataDefinition().getUniqueId();

			Either<ImmutablePair<PropertyData, GraphEdge>, TitanOperationStatus> propertyDefRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.InputValue), propertyValueUid, GraphEdgeLabels.INPUT_IMPL, NodeTypeEnum.Property, PropertyData.class);
			if (propertyDefRes.isRight()) {
				TitanOperationStatus status = propertyDefRes.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
				}
				return Either.right(status);
			}

			ImmutablePair<PropertyData, GraphEdge> propertyDefPair = propertyDefRes.left().value();
			PropertyData propertyData = propertyDefPair.left;
			Either<Edge, TitanOperationStatus> inputsEges = titanGenericDao.getIncomingEdgeByCriteria(propertyData, GraphEdgeLabels.INPUT, null);
			if (inputsEges.isRight()) {
				TitanOperationStatus status = inputsEges.right().value();

				return Either.right(status);
			}
			Edge edge = inputsEges.left().value();
			String inputName = (String) titanGenericDao.getProperty(edge, GraphEdgePropertiesDictionary.NAME.getProperty());

			String propertyUniqueId = (String) propertyData.getPropertyDataDefinition().getUniqueId();
			

			ComponentInstanceInput resourceInstanceProperty = new ComponentInstanceInput(propertyData.getPropertyDataDefinition(), inputId, value, propertyValueUid);
			
			//resourceInstanceProperty.setName(inputName);
			// set resource id
			// TODO: esofer add resource id
			resourceInstanceProperty.setName(inputName);
			resourceInstanceProperty.setParentUniqueId(inputId);
			// set value
			resourceInstanceProperty.setValue(value);
			// set property value unique id
			resourceInstanceProperty.setValueUniqueUid(propertyValueData.getUniqueId());
			// set rules
			// resourceInstanceProperty.setRules(propertyValueData.getRules());
			resourceInstanceProperty.setType(propertyData.getPropertyDataDefinition().getType());
			resourceInstanceProperty.setSchema(propertyData.getPropertyDataDefinition().getSchema());
			//resourceInstanceProperty.setComponentInstanceName(componentInsName);
			resourceInstanceProperty.setComponentInstanceId(resourceInstanceUid);

			result.add(resourceInstanceProperty);
		}


		return Either.left(result);
	}

	public Either<List<ComponentInstanceInput>, TitanOperationStatus> getComponentInstanceInputsByInputId(String inputId) {

		Either<InputsData, TitanOperationStatus> findResInputRes = titanGenericDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), inputId, InputsData.class);

		if (findResInputRes.isRight()) {
			TitanOperationStatus status = findResInputRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		

		// Either<List<InputValueData>, TitanOperationStatus> propertyImplNodes
		// = titanGenericDao.getByCriteria(NodeTypeEnum.InputValue, props,
		// InputValueData.class);
		Either<TitanVertex, TitanOperationStatus> vertexService = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), inputId);

		if (vertexService.isRight()) {
			log.debug("failed to fetch vertex of resource input for id = {}", inputId);
			TitanOperationStatus status = vertexService.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}

			return Either.right(status);
		}
		TitanVertex vertex = vertexService.left().value();

		Either<List<ImmutablePair<InputValueData, GraphEdge>>, TitanOperationStatus> propertyImplNodes = titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), inputId, GraphEdgeLabels.GET_INPUT, NodeTypeEnum.InputValue, InputValueData.class);

		//////////////////////////////////////////////////////////////////////////////////////////////////////////////

		if (propertyImplNodes.isRight()) {
			TitanOperationStatus status = propertyImplNodes.right().value();
			return Either.right(status);
		}

		List<ImmutablePair<InputValueData, GraphEdge>> list = propertyImplNodes.left().value();

		if (list == null || true == list.isEmpty()) {
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

		List<ComponentInstanceInput> result = new ArrayList<>();
		for (ImmutablePair<InputValueData, GraphEdge> propertyValueDataPair : list) {
			InputValueData propertyValueData = propertyValueDataPair.left;
			String propertyValueUid = propertyValueData.getUniqueId();
			String value = propertyValueData.getValue();
			// Either<List<Edge>, TitanOperationStatus> out =
			// titanGenericDao.getEdgesForNode(propertyValueData,
			// Direction.OUT);
			// Either<List<Edge>, TitanOperationStatus> in =
			// titanGenericDao.getEdgesForNode(propertyValueData, Direction.IN);
			Either<Edge, TitanOperationStatus> inputsvalueEges = titanGenericDao.getIncomingEdgeByCriteria(propertyValueData, GraphEdgeLabels.INPUT_VALUE, null);
			if (inputsvalueEges.isRight()) {
				TitanOperationStatus status = inputsvalueEges.right().value();

				return Either.right(status);
			}
			Edge edge = inputsvalueEges.left().value();
			String componentInsName = (String) titanGenericDao.getProperty(edge, GraphEdgePropertiesDictionary.NAME.getProperty());
			String componentInsId = (String) titanGenericDao.getProperty(edge, GraphEdgePropertiesDictionary.OWNER_ID.getProperty());

			Either<ImmutablePair<InputsData, GraphEdge>, TitanOperationStatus> propertyDefRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.InputValue), propertyValueUid, GraphEdgeLabels.INPUT_IMPL, NodeTypeEnum.Input, InputsData.class);

			if (propertyDefRes.isRight()) {
				TitanOperationStatus status = propertyDefRes.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
				}
				return Either.right(status);
			}

			ImmutablePair<InputsData, GraphEdge> propertyDefPair = propertyDefRes.left().value();

			InputsData propertyData = propertyDefPair.left;

			Either<Edge, TitanOperationStatus> inputsEges = titanGenericDao.getIncomingEdgeByCriteria(propertyData, GraphEdgeLabels.INPUT, null);
			if (inputsEges.isRight()) {
				TitanOperationStatus status = inputsEges.right().value();

				return Either.right(status);
			}
			edge = inputsEges.left().value();
			String inputName = (String) titanGenericDao.getProperty(edge, GraphEdgePropertiesDictionary.NAME.getProperty());

			String propertyUniqueId = (String) propertyData.getPropertyDataDefinition().getUniqueId();

			ComponentInstanceInput resourceInstanceProperty = new ComponentInstanceInput(propertyData.getPropertyDataDefinition(), inputId, value, propertyValueUid);
			// set property original unique id
			resourceInstanceProperty.setUniqueId(propertyUniqueId);
			resourceInstanceProperty.setName(inputName);
			// set resource id
			// TODO: esofer add resource id
			resourceInstanceProperty.setParentUniqueId(null);
			// set value
			resourceInstanceProperty.setValue(value);
			// set property value unique id
			resourceInstanceProperty.setValueUniqueUid(propertyValueData.getUniqueId());
			// set rules
			// resourceInstanceProperty.setRules(propertyValueData.getRules());
			resourceInstanceProperty.setType(propertyData.getPropertyDataDefinition().getType());
			resourceInstanceProperty.setSchema(propertyData.getPropertyDataDefinition().getSchema());
			resourceInstanceProperty.setComponentInstanceName(componentInsName);
			resourceInstanceProperty.setComponentInstanceId(componentInsId);

			result.add(resourceInstanceProperty);
		}

		return Either.left(result);

	}

	public Either<List<ComponentInstanceProperty>, TitanOperationStatus> getComponentInstancePropertiesByInputId(String inputId) {

		Either<InputsData, TitanOperationStatus> findResInputRes = titanGenericDao.getNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), inputId, InputsData.class);

		if (findResInputRes.isRight()) {
			TitanOperationStatus status = findResInputRes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return Either.right(status);
		}

		//Map<String, Object> props = new HashMap<String, Object>();
		//props.put(GraphEdgePropertiesDictionary.OWNER_ID.getProperty(), resourceInstanceUid);

		// Either<List<PropertyValueData>, TitanOperationStatus>
		// propertyImplNodes =
		// titanGenericDao.getByCriteria(NodeTypeEnum.PropertyValue, props,
		// PropertyValueData.class);
		Either<TitanVertex, TitanOperationStatus> vertexService = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), inputId);

		if (vertexService.isRight()) {
			log.debug("failed to fetch vertex of resource input for id = {}", inputId);
			TitanOperationStatus status = vertexService.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}

			return Either.right(status);
		}
		TitanVertex vertex = vertexService.left().value();

		// Either<List<ImmutablePair<PropertyValueData, GraphEdge>>,
		// TitanOperationStatus> propertyImplNodes =
		// titanGenericDao.getChildrenByEdgeCriteria(vertex, inputId,
		// GraphEdgeLabels.GET_INPUT, NodeTypeEnum.PropertyValue,
		// PropertyValueData.class, props);
		Either<List<ImmutablePair<PropertyValueData, GraphEdge>>, TitanOperationStatus> propertyImplNodes = titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), inputId, GraphEdgeLabels.GET_INPUT, NodeTypeEnum.PropertyValue, PropertyValueData.class);

		if (propertyImplNodes.isRight()) {
			TitanOperationStatus status = propertyImplNodes.right().value();
			return Either.right(status);
		}

		List<ImmutablePair<PropertyValueData, GraphEdge>> list = propertyImplNodes.left().value();

		if (list == null || true == list.isEmpty()) {
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

		List<ComponentInstanceProperty> result = new ArrayList<>();
		for (ImmutablePair<PropertyValueData, GraphEdge> propertyValueDataPair : list) {
			PropertyValueData propertyValueData = propertyValueDataPair.left;
			String propertyValueUid = propertyValueData.getUniqueId();
			String value = propertyValueData.getValue();
			
			String componentInstanceId = (String) propertyValueDataPair.right.getProperties().get(GraphEdgePropertiesDictionary.OWNER_ID.getProperty());

			Either<ImmutablePair<PropertyData, GraphEdge>, TitanOperationStatus> propertyDefRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.PropertyValue), propertyValueUid, GraphEdgeLabels.PROPERTY_IMPL, NodeTypeEnum.Property, PropertyData.class);
			if (propertyDefRes.isRight()) {
				TitanOperationStatus status = propertyDefRes.right().value();
				return Either.right(status);
			}

			ImmutablePair<PropertyData, GraphEdge> propertyDefPair = propertyDefRes.left().value();
			PropertyData propertyData = propertyDefPair.left;
			String propertyUniqueId = (String) propertyData.getPropertyDataDefinition().getUniqueId();
			
			Either<TitanVertex, TitanOperationStatus> originVertexEither = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), propertyUniqueId);
			if (originVertexEither.isRight()) {
				log.debug("Failed to fetch vertex of property for id {} error {}", propertyUniqueId, originVertexEither.right().value());
				return Either.right(originVertexEither.right().value());
			}
			TitanVertex originVertex = originVertexEither.left().value();
			Iterator<Edge> edgeIter = originVertex.edges(Direction.IN, GraphEdgeLabels.PROPERTY.getProperty());
			if (edgeIter == null) {		
				return Either.right(TitanOperationStatus.NOT_FOUND);
			}
		
			String propertyName = "";
			
			while (edgeIter.hasNext()) {
				TitanEdge edge = (TitanEdge) edgeIter.next();
				propertyName = (String) edge.property(GraphEdgePropertiesDictionary.NAME.getProperty()).value();
			}

			ComponentInstanceProperty resourceInstanceProperty = new ComponentInstanceProperty();
			// set property original unique id
			resourceInstanceProperty.setUniqueId(propertyUniqueId);
			// set resource id
			// TODO: esofer add resource id
			resourceInstanceProperty.setParentUniqueId(null);
			// set value
			resourceInstanceProperty.setValue(value);
			// set property value unique id
			resourceInstanceProperty.setValueUniqueUid(propertyValueData.getUniqueId());
			// set rules
			resourceInstanceProperty.setRules(propertyValueData.getRules());
			resourceInstanceProperty.setType(propertyData.getPropertyDataDefinition().getType());
			resourceInstanceProperty.setSchema(propertyData.getPropertyDataDefinition().getSchema());
			resourceInstanceProperty.setName(propertyName);
			resourceInstanceProperty.setComponentInstanceId(componentInstanceId);


			result.add(resourceInstanceProperty);
		}

		return Either.left(result);
	}

	public ComponentInstanceInput buildResourceInstanceInput(InputValueData propertyValueData, ComponentInstanceInput resourceInstanceInput) {

		String value = propertyValueData.getValue();
		String uid = propertyValueData.getUniqueId();
		ComponentInstanceInput instanceProperty = new ComponentInstanceInput(resourceInstanceInput, value, uid);
		instanceProperty.setPath(resourceInstanceInput.getPath());

		return instanceProperty;
	}

	public Either<List<ComponentInstanceInput>, TitanOperationStatus> getAllInputsOfResourceInstance(ComponentInstance compInstance) {

		Either<List<ComponentInstanceInput>, TitanOperationStatus> result;

		return getAllInputsOfResourceInstanceOnlyInputDefId(compInstance.getUniqueId());

	}



	private Either<List<InputDefinition>, StorageOperationStatus> createInputsFromProperty(String resourceId, NodeTypeEnum nodeType, Map<String, DataTypeDefinition> dataTypes, List<InputDefinition> resList, Map<String, List<ComponentInstancePropInput>> newInputsPropsMap) {
		for (Entry<String, List<ComponentInstancePropInput>> entry : newInputsPropsMap.entrySet()) {
			String compInstId = entry.getKey();
			List<ComponentInstancePropInput> properties = entry.getValue();
			
			Either<TitanVertex, TitanOperationStatus> ciVertexEither = titanGenericDao.getVertexByProperty(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), compInstId);
			if (ciVertexEither.isRight()) {
				log.debug("Failed to fetch vertex of resource instance for id {} error {}", compInstId, ciVertexEither.right().value());
				return Either.right( DaoStatusConverter.convertTitanStatusToStorageStatus(ciVertexEither.right().value()));
			}
			TitanVertex ciVertex = ciVertexEither.left().value();

			
			//String originType = (String) titanGenericDao.getProperty(originVertex, GraphPropertiesDictionary.LABEL.getProperty());
			String compInstname = (String) titanGenericDao.getProperty(ciVertex, GraphPropertiesDictionary.NORMALIZED_NAME.getProperty());
			String inputName = compInstname;
			
			if (properties != null && !properties.isEmpty()) {
				for (ComponentInstancePropInput propInput : properties) {
					Either<InputDefinition, StorageOperationStatus> createInputRes = createInputForComponentInstance(resourceId, nodeType, dataTypes, compInstId, inputName, propInput);
					
					if (createInputRes.isRight()) {
						log.debug("Failed to create input  of resource instance for id {} error {}", compInstId, createInputRes.right().value());
						return Either.right(createInputRes.right().value());
					}
					
					resList.add(createInputRes.left().value());
				
				}
			}
			
		}
		return Either.left(resList);
	}

	private Either<InputDefinition, StorageOperationStatus> createInputForComponentInstance(String resourceId, NodeTypeEnum nodeType, Map<String, DataTypeDefinition> dataTypes,  String compInstId, String inputName, ComponentInstancePropInput propInput) {
		String propertiesName = propInput.getPropertiesName() ;
		PropertyDefinition selectedProp = propInput.getInput();
		String[] parsedPropNames = propInput.getParsedPropNames();
		if(parsedPropNames != null){
			for(String str: parsedPropNames){
				inputName += "_"  + str;
			}
		} else {
			inputName += "_"  + propInput.getName();
		}
		
		InputDefinition input = null;
		ComponentInstanceProperty prop = propInput;	
		
		if(propertiesName != null && !propertiesName.isEmpty() && selectedProp != null){
			input = new InputDefinition(selectedProp);
		}else{
			input = new InputDefinition(prop);
			input.setName(inputName + "_" + prop.getName());
			
		}
		input.setName(inputName);	
		
		JSONObject jobject = new JSONObject();
							
		
		if(prop.getValueUniqueUid() == null || prop.getValueUniqueUid().isEmpty()){
			if(propertiesName != null && !propertiesName.isEmpty() && selectedProp != null){
					
				jobject = createJSONValueForProperty(parsedPropNames.length -1, parsedPropNames, jobject, inputName);	
				prop.setValue(jobject.toJSONString());	
				
			}else{
				
				jobject.put(GET_INPUT, input.getName());
				prop.setValue(jobject.toJSONString());
				
				
			}
			Either<Integer, StorageOperationStatus> increaseCounterRes = componentInstanceOperation.increaseAndGetResourceInstanceSpecificCounter(compInstId, GraphPropertiesDictionary.PROPERTY_COUNTER, true);
			if (increaseCounterRes.isRight()) {
				log.debug("Failed to increase resource property counter {} to resource instance {}", prop, compInstId);
				
				return Either.right( increaseCounterRes.right().value());
			}
			Integer index = increaseCounterRes.left().value();							
			Either<ComponentInstanceProperty, StorageOperationStatus> result = componentInstanceOperation.addPropertyValueToResourceInstance(prop, compInstId, false, index, true);
			if (result.isRight()) {
				log.debug("Failed to add property value {} to resource instance {}", prop, compInstId);								
				return  Either.right( result.right().value());
			}
			prop = result.left().value();
			
		}else{
			
			String value = prop.getValue();
			if(value != null){
				Object objValue =  new Yaml().load(value);
				if( objValue instanceof Map || objValue  instanceof List ){
					if(propertiesName == null ||propertiesName.isEmpty()){
						jobject.put(GET_INPUT, input.getName());
						prop.setValue(jobject.toJSONString());
						prop.setRules(null);
						
					}else{
						Map<String, Object> mappedToscaTemplate = (Map<String, Object>) objValue;
						createInputValue(mappedToscaTemplate, 1, parsedPropNames, inputName);
						Gson gson = new Gson(); 
						String json = gson.toJson(mappedToscaTemplate);								
						prop.setValue(json);
						prop.setRules(null);
					}
					
				}else{
					jobject.put(GET_INPUT, input.getName());
					prop.setValue(jobject.toJSONString());
					prop.setRules(null);
				}
			}else{
				jobject.put(GET_INPUT, input.getName());
				prop.setValue(jobject.toJSONString());
				prop.setRules(null);
			}
			
			Either<PropertyValueData, TitanOperationStatus> eitherStatus = componentInstanceOperation.updatePropertyOfResourceInstance(prop, compInstId, false);

			if (eitherStatus.isRight()) {
				log.error("Failed to add property value {} to resource instance {} in Graph. status is {}", prop, compInstId, eitherStatus.right().value().name());
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherStatus.right().value()));
				
			} else {
				PropertyValueData propertyValueData = eitherStatus.left().value();

				prop  = propertyOperation.buildResourceInstanceProperty(propertyValueData, prop);

				log.debug("The returned ResourceInstanceProperty is {}", prop);

				Either<String, TitanOperationStatus> findDefaultValue = propertyOperation.findDefaultValueFromSecondPosition(prop.getPath(), propertyValueData.getUniqueId(), prop.getDefaultValue());
				if (findDefaultValue.isRight()) {
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(findDefaultValue.right().value()));
					
				}
				String defaultValue = findDefaultValue.left().value();
				prop.setDefaultValue(defaultValue);
				log.debug("The returned default value in ResourceInstanceProperty is {}", defaultValue);
				
				prop.setComponentInstanceId(compInstId);
				

				
			}

			
		}
		
		StorageOperationStatus validateAndUpdateProperty = validateAndUpdateProperty(input, dataTypes);
		if (validateAndUpdateProperty != StorageOperationStatus.OK) {
			log.error("Property {} is invalid. Status is {}", input, validateAndUpdateProperty);
			return Either.right(validateAndUpdateProperty);
		}

		Either<InputsData, TitanOperationStatus> addPropertyToGraph = addInputToGraph(input.getName(), input, resourceId, nodeType);

		if (addPropertyToGraph.isRight()) {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(addPropertyToGraph.right().value()));
		}
		InputDefinition createdInputyDefinition = convertInputDataToInputDefinition(addPropertyToGraph.left().value());
		createdInputyDefinition.setName(input.getName());
		createdInputyDefinition.setParentUniqueId(resourceId);
		
		TitanOperationStatus status = associatePropertyToInput(compInstId, createdInputyDefinition.getUniqueId(), prop, null);
		if (status != TitanOperationStatus.OK) {
			log.debug("Failed to associate input {} tp property  value{} ", createdInputyDefinition.getName(), prop.getValueUniqueUid());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		}
		
		return Either.left(createdInputyDefinition);
		
	}
	
	private  JSONObject createJSONValueForProperty (int i, String [] parsedPropNames, JSONObject ooj, String inputName){
		
		while(i >= 1){
			if( i == parsedPropNames.length -1){				
				JSONObject jobProp = new JSONObject();
				jobProp.put(GET_INPUT, inputName);
				ooj.put(parsedPropNames[i], jobProp);
				i--;
				return createJSONValueForProperty (i, parsedPropNames, ooj, inputName);
			}else{
				JSONObject res = new JSONObject();
				res.put(parsedPropNames[i], ooj);
				i --;
				res =  createJSONValueForProperty (i, parsedPropNames, res, inputName);
				return res;
			}
		}
		
		return ooj;
	}
	
	public void resetInputName(Map<String, Object> lhm1, String inputName){
	    for (Map.Entry<String, Object> entry : lhm1.entrySet()) {
	        String key = entry.getKey();
	        Object value = entry.getValue();
	        if (value instanceof String && ((String) value).equalsIgnoreCase(inputName) && key.equals(GET_INPUT)) {
	        	value = "";
	        	lhm1.remove(key);	        	
	        } else if (value instanceof Map) {
	            Map<String, Object> subMap = (Map<String, Object>)value;
	            resetInputName(subMap, inputName);
	        } else {
	             continue;
	        }

	    }
	}
	
	private  Map<String, Object> createInputValue(Map<String, Object> lhm1, int index, String[] inputNames, String inputName){
		while(index < inputNames.length){
			if(lhm1.containsKey(inputNames[index])){
				Object value = lhm1.get(inputNames[index]);
				if (value instanceof Map){
					if(index == inputNames.length -1){
						((Map) value).put(GET_INPUT, inputName);
						return ((Map) value);
						
					}else{
						index++;
						return  createInputValue((Map)value, index, inputNames, inputName);
					}
				}else{
					Map<String, Object> jobProp = new HashMap<>();
					if(index == inputNames.length -1){
						jobProp.put(GET_INPUT, inputName);
						lhm1.put(inputNames[index], jobProp);
						return lhm1;						
					}else{						
						lhm1.put(inputNames[index], jobProp);
						index++;
						return  createInputValue(jobProp, index, inputNames, inputName);
					}
				}
			}else{				
				Map<String, Object> jobProp = new HashMap<>();
				lhm1.put(inputNames[index], jobProp);
				if(index == inputNames.length -1){
					jobProp.put(GET_INPUT, inputName);
					return jobProp;
				}else{
					index++;
					return  createInputValue(jobProp, index, inputNames, inputName);
				}
			}
		}
		return lhm1;
	}




}
