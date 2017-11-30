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
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgePropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.operations.api.IInputsOperation;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.InputValueData;
import org.openecomp.sdc.be.resources.data.InputsData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import fj.data.Either;

@Component("input-operation")
public class InputsOperation extends AbstractOperation implements IInputsOperation {

	private static Logger log = LoggerFactory.getLogger(InputsOperation.class.getName());
	@Autowired
	PropertyOperation propertyOperation;

	Gson gson = new Gson();


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


	@Override
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

	@Override
	public ComponentInstanceInput buildResourceInstanceInput(InputValueData propertyValueData, ComponentInstanceInput resourceInstanceInput) {

		String value = propertyValueData.getValue();
		String uid = propertyValueData.getUniqueId();
		ComponentInstanceInput instanceProperty = new ComponentInstanceInput(resourceInstanceInput, value, uid);
		instanceProperty.setPath(resourceInstanceInput.getPath());

		return instanceProperty;
	}


}
