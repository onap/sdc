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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.graph.GraphElementFactory;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.operations.api.IAdditionalInformationOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.AdditionalInfoParameterData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@Component("additional-information-operation")
public class AdditionalInformationOperation implements IAdditionalInformationOperation {

	public static final String EMPTY_VALUE = null;

	public AdditionalInformationOperation() {
		super();
	}

	private static Logger log = LoggerFactory.getLogger(AdditionalInformationOperation.class.getName());

	@javax.annotation.Resource
	private TitanGenericDao titanGenericDao;

	@Override
	public Either<AdditionalInformationDefinition, TitanOperationStatus> addAdditionalInformationParameter(NodeTypeEnum nodeType, String componentId, String key, String value) {

		TitanOperationStatus verifyNodeTypeVsComponent = verifyNodeTypeVsComponent(nodeType, componentId);
		if (verifyNodeTypeVsComponent != TitanOperationStatus.OK) {
			return Either.right(verifyNodeTypeVsComponent);
		}

		Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, TitanOperationStatus> getResult = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
				NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

		if (getResult.isRight()) {
			TitanOperationStatus status = getResult.right().value();
			return Either.right(status);
		}

		ImmutablePair<AdditionalInfoParameterData, GraphEdge> immutablePair = getResult.left().value();
		AdditionalInfoParameterData parameterData = immutablePair.getLeft();
		Map<String, String> parameters = parameterData.getParameters();
		if (parameters == null) {
			parameters = new HashMap<String, String>();
			parameterData.setParameters(parameters);
		}
		Map<String, String> idToKey = parameterData.getIdToKey();
		if (idToKey == null) {
			idToKey = new HashMap<String, String>();
			parameterData.setIdToKey(idToKey);
		}

		Integer lastCreatedCounter = parameterData.getAdditionalInfoParameterDataDefinition().getLastCreatedCounter();
		lastCreatedCounter++;

		if (parameters.containsKey(key)) {
			log.debug("The key {} already exists under component {}", key, componentId);
			return Either.right(TitanOperationStatus.ALREADY_EXIST);
		}

		idToKey.put(String.valueOf(lastCreatedCounter), key);
		parameters.put(key, value);
		parameterData.getAdditionalInfoParameterDataDefinition().setLastCreatedCounter(lastCreatedCounter);

		Either<AdditionalInfoParameterData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(parameterData, AdditionalInfoParameterData.class);

		if (updateNode.isRight()) {
			TitanOperationStatus status = updateNode.right().value();
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedUpdateNodeError, "UpdateAdditionalInformationParameter", "additional information of " + nodeType.getName() + " " + componentId, String.valueOf(status));
			BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("UpdateAdditionalInformationParameter", "additional information of " + nodeType.getName() + " " + componentId, String.valueOf(status));
			return Either.right(status);
		}

		AdditionalInformationDefinition informationDefinition = createInformationDefinitionFromNode(componentId, parameters, idToKey, updateNode.left().value());

		return Either.left(informationDefinition);

	}

	@Override
	public Either<AdditionalInformationDefinition, TitanOperationStatus> updateAdditionalInformationParameter(NodeTypeEnum nodeType, String componentId, String id, String key, String value) {

		TitanOperationStatus verifyNodeTypeVsComponent = verifyNodeTypeVsComponent(nodeType, componentId);
		if (verifyNodeTypeVsComponent != TitanOperationStatus.OK) {
			return Either.right(verifyNodeTypeVsComponent);
		}

		Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, TitanOperationStatus> getResult = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
				NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

		if (getResult.isRight()) {
			TitanOperationStatus status = getResult.right().value();
			return Either.right(status);
		}

		ImmutablePair<AdditionalInfoParameterData, GraphEdge> immutablePair = getResult.left().value();
		AdditionalInfoParameterData parameterData = immutablePair.getLeft();
		Map<String, String> parameters = parameterData.getParameters();
		Map<String, String> idToKey = parameterData.getIdToKey();
		if (idToKey == null || false == idToKey.containsKey(id)) {
			return Either.right(TitanOperationStatus.INVALID_ID);
		}

		String origKey = idToKey.get(id);

		if (false == origKey.equals(key)) {
			if (parameters.containsKey(key)) {
				log.debug("The key {} already exists", key);
				return Either.right(TitanOperationStatus.ALREADY_EXIST);
			}
			String removed = parameters.remove(origKey);
			log.trace("The key-value {} = {} was removed from additionalInformation", origKey, removed);
		}
		parameters.put(key, value);
		idToKey.put(id, key);

		Either<AdditionalInfoParameterData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(parameterData, AdditionalInfoParameterData.class);

		if (updateNode.isRight()) {
			TitanOperationStatus status = updateNode.right().value();
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedUpdateNodeError, "UpdateAdditionalInformationParameter", "additional information of resource " + componentId, String.valueOf(status));
			BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("UpdateAdditionalInformationParameter", "additional information of resource " + componentId, String.valueOf(status));
			return Either.right(status);
		}

		AdditionalInformationDefinition informationDefinition = createInformationDefinitionFromNode(componentId, parameters, idToKey, updateNode.left().value());

		return Either.left(informationDefinition);

	}

	@Override
	public Either<AdditionalInformationDefinition, TitanOperationStatus> deleteAdditionalInformationParameter(NodeTypeEnum nodeType, String componentId, String id) {

		TitanOperationStatus verifyNodeTypeVsComponent = verifyNodeTypeVsComponent(nodeType, componentId);
		if (verifyNodeTypeVsComponent != TitanOperationStatus.OK) {
			return Either.right(verifyNodeTypeVsComponent);
		}

		Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, TitanOperationStatus> getResult = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
				NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

		if (getResult.isRight()) {
			TitanOperationStatus status = getResult.right().value();
			return Either.right(status);
		}

		ImmutablePair<AdditionalInfoParameterData, GraphEdge> immutablePair = getResult.left().value();
		AdditionalInfoParameterData parameterData = immutablePair.getLeft();
		Map<String, String> parameters = parameterData.getParameters();
		Map<String, String> idToKey = parameterData.getIdToKey();

		if (idToKey == null || false == idToKey.containsKey(id)) {
			return Either.right(TitanOperationStatus.INVALID_ID);
		}

		String key = idToKey.get(id);
		String removedKey = idToKey.remove(id);
		String removedValue = parameters.remove(key);
		log.trace("The key-value {} = {} was removed from additionalInformation", removedKey, removedValue);

		Either<AdditionalInfoParameterData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(parameterData, AdditionalInfoParameterData.class);

		if (updateNode.isRight()) {
			TitanOperationStatus status = updateNode.right().value();
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedUpdateNodeError, "DeleteAdditionalInformationParameter", "additional information of " + nodeType.getName() + " " + componentId, String.valueOf(status));
			BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("DeleteAdditionalInformationParameter", "additional information of " + nodeType.getName() + " " + componentId, String.valueOf(status));
			return Either.right(status);
		}

		AdditionalInformationDefinition informationDefinition = createInformationDefinitionFromNode(componentId, parameters, idToKey, updateNode.left().value());

		return Either.left(informationDefinition);

	}

	private AdditionalInformationDefinition createInformationDefinitionFromNode(String resourceId, Map<String, String> parameters, Map<String, String> idToKey, AdditionalInfoParameterData additionalInfoParameterData) {
		AdditionalInfoParameterDataDefinition dataDefinition = additionalInfoParameterData.getAdditionalInfoParameterDataDefinition();

		AdditionalInformationDefinition informationDefinition = new AdditionalInformationDefinition(dataDefinition, resourceId, convertParameters(parameters, idToKey));
		return informationDefinition;
	}

	private List<AdditionalInfoParameterInfo> convertParameters(Map<String, String> parameters, Map<String, String> idToKey) {

		List<AdditionalInfoParameterInfo> list = new ArrayList<AdditionalInfoParameterInfo>();

		if (parameters != null) {
			for (Entry<String, String> idToKeyEntry : idToKey.entrySet()) {

				String id = idToKeyEntry.getKey();
				String key = idToKeyEntry.getValue();

				String value = parameters.get(key);

				AdditionalInfoParameterInfo parameterInfo = new AdditionalInfoParameterInfo(id, key, value);
				list.add(parameterInfo);
			}

		}

		return list;
	}

	@Override
	public Either<AdditionalInfoParameterData, TitanOperationStatus> addAdditionalInformationNode(NodeTypeEnum nodeType, String componentId) {

		UniqueIdData from = new UniqueIdData(nodeType, componentId);

		String uniqueId = UniqueIdBuilder.buildAdditionalInformationUniqueId(componentId);
		AdditionalInfoParameterDataDefinition additionalInfoParameterDataDefinition = new AdditionalInfoParameterDataDefinition();
		additionalInfoParameterDataDefinition.setUniqueId(uniqueId);

		AdditionalInfoParameterData additionalInfoParameterData = new AdditionalInfoParameterData(additionalInfoParameterDataDefinition, new HashMap<String, String>(), new HashMap<String, String>());

		Either<AdditionalInfoParameterData, TitanOperationStatus> createNode = titanGenericDao.createNode(additionalInfoParameterData, AdditionalInfoParameterData.class);
		if (createNode.isRight()) {
			TitanOperationStatus status = createNode.right().value();
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedCreateNodeError, "AddAdditionalInformationNode", "additional information to " + nodeType.getName() + " " + componentId, String.valueOf(status));
			BeEcompErrorManager.getInstance().logBeFailedCreateNodeError("AddAdditionalInformationNode", uniqueId, String.valueOf(status));
			return Either.right(status);
		}

		AdditionalInfoParameterData to = createNode.left().value();

		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(from, to, GraphEdgeLabels.ADDITIONAL_INFORMATION, null);
		if (createRelation.isRight()) {
			TitanOperationStatus status = createRelation.right().value();
			return Either.right(status);
		}

		return Either.left(to);
	}

	@Override
	public Either<TitanVertex, TitanOperationStatus> addAdditionalInformationNode(NodeTypeEnum nodeType, String componentId, TitanVertex metadataVertex) {

		String uniqueId = UniqueIdBuilder.buildAdditionalInformationUniqueId(componentId);
		AdditionalInfoParameterDataDefinition additionalInfoParameterDataDefinition = new AdditionalInfoParameterDataDefinition();
		additionalInfoParameterDataDefinition.setUniqueId(uniqueId);

		AdditionalInfoParameterData additionalInfoParameterData = new AdditionalInfoParameterData(additionalInfoParameterDataDefinition, new HashMap<String, String>(), new HashMap<String, String>());

		Either<TitanVertex, TitanOperationStatus> createNode = titanGenericDao.createNode(additionalInfoParameterData);
		if (createNode.isRight()) {
			TitanOperationStatus status = createNode.right().value();
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedCreateNodeError, "AddAdditionalInformationNode", "additional information to " + nodeType.getName() + " " + componentId, String.valueOf(status));
			BeEcompErrorManager.getInstance().logBeFailedCreateNodeError("AddAdditionalInformationNode", uniqueId, String.valueOf(status));
			return Either.right(status);
		}

		TitanVertex additionalInfoVertex = createNode.left().value();

		TitanOperationStatus createRelation = titanGenericDao.createEdge(metadataVertex, additionalInfoVertex, GraphEdgeLabels.ADDITIONAL_INFORMATION, null);

		if (!createRelation.equals(TitanOperationStatus.OK)) {
			return Either.right(createRelation);
		}
		return Either.left(additionalInfoVertex);
	}

	public Either<AdditionalInformationDefinition, TitanOperationStatus> addAdditionalInformationNode(NodeTypeEnum nodeType, String componentId, AdditionalInformationDefinition parameters) {

		Either<AdditionalInfoParameterData, TitanOperationStatus> status = this.addAdditionalInformationNode(nodeType, componentId);

		if (status.isRight()) {
			return Either.right(status.right().value());
		}

		AdditionalInfoParameterData parameterData = status.left().value();

		populateParameterNodeWithParameters(parameterData, parameters);

		Either<AdditionalInfoParameterData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(parameterData, AdditionalInfoParameterData.class);

		if (updateNode.isRight()) {
			return Either.right(updateNode.right().value());
		}

		AdditionalInformationDefinition informationDefinition = convertAdditionalInformationDataToDefinition(updateNode.left().value(), componentId);

		return Either.left(informationDefinition);
	}

	public TitanOperationStatus addAdditionalInformationNode(NodeTypeEnum nodeType, String componentId, AdditionalInformationDefinition parameters, TitanVertex metadataVertex) {

		Either<TitanVertex, TitanOperationStatus> status = this.addAdditionalInformationNode(nodeType, componentId, metadataVertex);

		if (status.isRight()) {
			return status.right().value();
		}
		TitanVertex additionalInfoVertex = status.left().value();

		Map<String, Object> newProp = titanGenericDao.getProperties(additionalInfoVertex);
		AdditionalInfoParameterData parameterData = GraphElementFactory.createElement(NodeTypeEnum.AdditionalInfoParameters.getName(), GraphElementTypeEnum.Node, newProp, AdditionalInfoParameterData.class);

		populateParameterNodeWithParameters(parameterData, parameters);

		TitanOperationStatus updateNode = titanGenericDao.updateVertex(parameterData, additionalInfoVertex);

		return updateNode;
	}

	private void populateParameterNodeWithParameters(AdditionalInfoParameterData parameterData, AdditionalInformationDefinition aiDefinition) {

		if (aiDefinition != null) {

			Integer lastCreatedCounter = aiDefinition.getLastCreatedCounter();
			parameterData.getAdditionalInfoParameterDataDefinition().setLastCreatedCounter(lastCreatedCounter);
			log.trace("Set last created counter of additional information to {}", lastCreatedCounter);

			List<AdditionalInfoParameterInfo> parameters = aiDefinition.getParameters();
			if (parameters != null) {

				Map<String, String> idToKey = new HashMap<String, String>();
				Map<String, String> parametersMap = new HashMap<String, String>();
				for (AdditionalInfoParameterInfo additionalInfoParameterInfo : parameters) {
					String uniqueId = additionalInfoParameterInfo.getUniqueId();
					String key = additionalInfoParameterInfo.getKey();
					String value = additionalInfoParameterInfo.getValue();

					if (key != null && false == key.isEmpty()) {
						idToKey.put(uniqueId, key);
						parametersMap.put(key, value);
					}
				}
				parameterData.setIdToKey(idToKey);
				parameterData.setParameters(parametersMap);
			}
		}

	}

	@Override
	public TitanOperationStatus findResourceAllAdditionalInformationRecursively(String uniqueId, List<AdditionalInformationDefinition> properties) {

		log.trace("Going to fetch additional information under resource {}", uniqueId);
		TitanOperationStatus resourceCapabilitiesStatus = findAdditionalInformationOfNode(NodeTypeEnum.Resource, uniqueId, properties);

		if (!resourceCapabilitiesStatus.equals(TitanOperationStatus.OK)) {
			return resourceCapabilitiesStatus;
		}

		Either<ImmutablePair<ResourceMetadataData, GraphEdge>, TitanOperationStatus> parentNodes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), uniqueId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Resource,
				ResourceMetadataData.class);

		if (parentNodes.isRight()) {
			TitanOperationStatus parentNodesStatus = parentNodes.right().value();
			if (false == parentNodesStatus.equals(TitanOperationStatus.NOT_FOUND)) {
				log.error("Failed to find parent additional information of resource {}. status is {}", uniqueId, parentNodesStatus);
				return parentNodesStatus;
			}
		}

		if (parentNodes.isLeft()) {
			ImmutablePair<ResourceMetadataData, GraphEdge> parnetNodePair = parentNodes.left().value();
			String parentUniqueId = parnetNodePair.getKey().getMetadataDataDefinition().getUniqueId();
			TitanOperationStatus addParentIntStatus = findResourceAllAdditionalInformationRecursively(parentUniqueId, properties);

			if (addParentIntStatus != TitanOperationStatus.OK) {
				log.error("Failed to find all resource additional information of resource {}", parentUniqueId);
				return addParentIntStatus;
			}
		}
		return TitanOperationStatus.OK;

	}

	@Override
	public TitanOperationStatus findServiceAllAdditionalInformationRecursively(String uniqueId, List<AdditionalInformationDefinition> properties) {

		log.trace("Going to fetch additional information under service {}", uniqueId);
		TitanOperationStatus resourceCapabilitiesStatus = findAdditionalInformationOfNode(NodeTypeEnum.Service, uniqueId, properties);

		if (!resourceCapabilitiesStatus.equals(TitanOperationStatus.OK)) {
			return resourceCapabilitiesStatus;
		}

		Either<ImmutablePair<ServiceMetadataData, GraphEdge>, TitanOperationStatus> parentNodes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Service), uniqueId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Service,
				ServiceMetadataData.class);

		if (parentNodes.isRight()) {
			TitanOperationStatus parentNodesStatus = parentNodes.right().value();
			if (false == parentNodesStatus.equals(TitanOperationStatus.NOT_FOUND)) {
				log.error("Failed to find parent additional information of resource {}. status is {}", uniqueId, parentNodesStatus);
				return parentNodesStatus;
			}
		}

		if (parentNodes.isLeft()) {
			ImmutablePair<ServiceMetadataData, GraphEdge> parnetNodePair = parentNodes.left().value();
			String parentUniqueId = parnetNodePair.getKey().getMetadataDataDefinition().getUniqueId();
			TitanOperationStatus addParentIntStatus = findServiceAllAdditionalInformationRecursively(parentUniqueId, properties);

			if (addParentIntStatus != TitanOperationStatus.OK) {
				log.error("Failed to find all resource additional information of resource {}", parentUniqueId);
				return addParentIntStatus;
			}
		}
		return TitanOperationStatus.OK;

	}

	private TitanOperationStatus findAdditionalInformationOfNode(NodeTypeEnum nodeType, String uniqueId, List<AdditionalInformationDefinition> properties) {

		Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, TitanOperationStatus> childNode = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
				NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

		if (childNode.isRight()) {
			TitanOperationStatus status = childNode.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
			}
			return status;
		}

		ImmutablePair<AdditionalInfoParameterData, GraphEdge> immutablePair = childNode.left().value();
		AdditionalInfoParameterData propertyData = immutablePair.getKey();

		Map<String, String> parameters = propertyData.getParameters();
		if (parameters != null && false == parameters.isEmpty()) {
			AdditionalInformationDefinition additionalInfoDef = this.convertAdditionalInformationDataToDefinition(propertyData, uniqueId);
			properties.add(additionalInfoDef);
		}

		return TitanOperationStatus.OK;

	}

	private AdditionalInformationDefinition convertAdditionalInformationDataToDefinition(AdditionalInfoParameterData additionalInfoData, String uniqueId) {

		Map<String, String> parameters = additionalInfoData.getParameters();
		Map<String, String> idToKey = additionalInfoData.getIdToKey();

		AdditionalInformationDefinition definition = new AdditionalInformationDefinition(additionalInfoData.getAdditionalInfoParameterDataDefinition(), uniqueId, convertParameters(parameters, idToKey));
		return definition;
	}

	@Override
	public Either<AdditionalInformationDefinition, StorageOperationStatus> createAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String key, String value, boolean inTransaction) {

		Either<AdditionalInformationDefinition, StorageOperationStatus> result = null;

		try {

			Either<AdditionalInformationDefinition, TitanOperationStatus> either = this.addAdditionalInformationParameter(nodeType, resourceId, key, value);

			if (either.isRight()) {
				TitanOperationStatus status = either.right().value();
				log.debug("Failed to add additional information property {} to component {}. Status is {}", key, resourceId, status);
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedUpdateNodeError, "additional information of " + nodeType.getName() + " " + resourceId, String.valueOf(status));
				BeEcompErrorManager.getInstance().logBeFailedUpdateNodeError("CreateAdditionalInformationParameter", "additional information of " + nodeType.getName() + " " + resourceId, String.valueOf(status));
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			} else {
				AdditionalInformationDefinition additionalInformationDefinition = either.left().value();
				result = Either.left(additionalInformationDefinition);
			}

			return result;
		} finally {
			commitOrRollback(inTransaction, result);
		}

	}

	@Override
	public Either<AdditionalInformationDefinition, StorageOperationStatus> updateAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String id, String key, String value, boolean inTransaction) {

		Either<AdditionalInformationDefinition, StorageOperationStatus> result = null;

		try {

			Either<AdditionalInformationDefinition, TitanOperationStatus> either = this.updateAdditionalInformationParameter(nodeType, resourceId, id, key, value);

			if (either.isRight()) {
				log.info("Failed to update additional information property {} to component {}", key, resourceId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(either.right().value()));
			} else {
				AdditionalInformationDefinition additionalInformationDefinition = either.left().value();
				result = Either.left(additionalInformationDefinition);
			}

			return result;

		} finally {
			commitOrRollback(inTransaction, result);
		}

	}

	@Override
	public Either<AdditionalInformationDefinition, StorageOperationStatus> deleteAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String id, boolean inTransaction) {

		Either<AdditionalInformationDefinition, StorageOperationStatus> result = null;

		try {

			Either<AdditionalInformationDefinition, TitanOperationStatus> either = this.deleteAdditionalInformationParameter(nodeType, resourceId, id);

			if (either.isRight()) {
				log.error("Failed to delete additional information id {} to component {}", id, resourceId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(either.right().value()));
			} else {
				AdditionalInformationDefinition additionalInformationDefinition = either.left().value();
				result = Either.left(additionalInformationDefinition);
			}

			return result;

		} finally {
			commitOrRollback(inTransaction, result);
		}

	}

	@Override
	public Either<Integer, StorageOperationStatus> getNumberOfAdditionalInformationParameters(NodeTypeEnum nodeType, String resourceId, boolean inTransaction) {

		Either<Integer, StorageOperationStatus> result = null;

		try {

			Either<Integer, TitanOperationStatus> either = this.getNumberOfParameters(nodeType, resourceId);

			if (either.isRight()) {
				log.error("Failed to get the number of additional information properties in component {}", resourceId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(either.right().value()));
			} else {
				Integer counter = either.left().value();
				result = Either.left(counter);
			}

			return result;
		} finally {
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
	public Either<Integer, TitanOperationStatus> getNumberOfParameters(NodeTypeEnum nodeType, String resourceId) {

		Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, TitanOperationStatus> getResult = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), resourceId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
				NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

		if (getResult.isRight()) {
			TitanOperationStatus status = getResult.right().value();
			return Either.right(status);
		}

		ImmutablePair<AdditionalInfoParameterData, GraphEdge> immutablePair = getResult.left().value();
		AdditionalInfoParameterData parameterData = immutablePair.getLeft();
		Map<String, String> parameters = parameterData.getParameters();

		Integer counter = 0;
		if (parameters != null) {
			counter = parameters.size();
		}

		return Either.left(counter);

	}

	@Override
	public Either<AdditionalInfoParameterInfo, TitanOperationStatus> getAdditionalInformationParameter(NodeTypeEnum nodeType, String componentId, String id) {

		TitanOperationStatus verifyNodeTypeVsComponent = verifyNodeTypeVsComponent(nodeType, componentId);
		if (verifyNodeTypeVsComponent != TitanOperationStatus.OK) {
			return Either.right(verifyNodeTypeVsComponent);
		}

		Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, TitanOperationStatus> getResult = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
				NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

		if (getResult.isRight()) {
			TitanOperationStatus status = getResult.right().value();
			return Either.right(status);
		}

		ImmutablePair<AdditionalInfoParameterData, GraphEdge> immutablePair = getResult.left().value();
		AdditionalInfoParameterData parameterData = immutablePair.getLeft();
		Map<String, String> parameters = parameterData.getParameters();
		Map<String, String> idToKey = parameterData.getIdToKey();

		if (idToKey == null || false == idToKey.containsKey(id)) {
			return Either.right(TitanOperationStatus.INVALID_ID);
		}

		String key = idToKey.get(id);
		String value = parameters.get(key);

		log.trace("The key-value {} = {} was retrieved for id {}", key, value, id);

		Either<AdditionalInfoParameterData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(parameterData, AdditionalInfoParameterData.class);

		if (updateNode.isRight()) {
			TitanOperationStatus status = updateNode.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedRetrieveNodeError, "GetAdditionnalInformationParameter", "additional information of " + nodeType.getName() + " " + componentId, String.valueOf(status));
				BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("GetAdditionnalInformationParameter", "additional information of " + nodeType.getName() + " " + componentId, String.valueOf(status));
			}
			return Either.right(status);
		}

		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo(id, key, value);

		return Either.left(additionalInfoParameterInfo);

	}

	@Override
	public Either<AdditionalInformationDefinition, TitanOperationStatus> getAllAdditionalInformationParameters(NodeTypeEnum nodeType, String componentId, boolean ignoreVerification) {

		if (false == ignoreVerification) {
			TitanOperationStatus verifyNodeTypeVsComponent = verifyNodeTypeVsComponent(nodeType, componentId);
			if (verifyNodeTypeVsComponent != TitanOperationStatus.OK) {
				return Either.right(verifyNodeTypeVsComponent);
			}
		}

		Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, TitanOperationStatus> getResult = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
				NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

		if (getResult.isRight()) {
			TitanOperationStatus status = getResult.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedRetrieveNodeError, "GetAdditionnalInformationParameters", "additional information of " + nodeType.getName() + " " + componentId, String.valueOf(status));
				BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("GetAdditionnalInformationParameters", "additional information of " + nodeType.getName() + " " + componentId, String.valueOf(status));
			}
			return Either.right(status);
		}

		ImmutablePair<AdditionalInfoParameterData, GraphEdge> immutablePair = getResult.left().value();
		AdditionalInfoParameterData parameterData = immutablePair.getLeft();
		Map<String, String> parameters = parameterData.getParameters();
		Map<String, String> idToKey = parameterData.getIdToKey();

		AdditionalInformationDefinition informationDefinition = createInformationDefinitionFromNode(componentId, parameters, idToKey, parameterData);

		return Either.left(informationDefinition);

	}

	@Override
	public Either<AdditionalInformationDefinition, StorageOperationStatus> getAllAdditionalInformationParameters(NodeTypeEnum nodeType, String resourceId, boolean ignoreVerification, boolean inTransaction) {

		Either<AdditionalInformationDefinition, StorageOperationStatus> result = null;

		try {

			Either<AdditionalInformationDefinition, TitanOperationStatus> either = this.getAllAdditionalInformationParameters(nodeType, resourceId, ignoreVerification);

			if (either.isRight()) {
				TitanOperationStatus status = either.right().value();
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			} else {
				AdditionalInformationDefinition additionalInformationDefinition = either.left().value();
				result = Either.left(additionalInformationDefinition);
			}

			return result;

		} finally {
			commitOrRollback(inTransaction, result);
		}

	}

	private void commitOrRollback(boolean inTransaction, Either<? extends Object, StorageOperationStatus> result) {

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

	private void commitOrRollbackTx(TitanTransaction tx, boolean inTransaction, Either<? extends Object, StorageOperationStatus> result) {

		if (false == inTransaction) {
			if (result == null || result.isRight()) {
				log.error("Going to execute rollback on graph.");
				tx.rollback();
			} else {
				log.debug("Going to execute commit on graph.");
				tx.commit();
			}
		}
	}

	@Override
	public Either<AdditionalInfoParameterInfo, StorageOperationStatus> getAdditionalInformationParameter(NodeTypeEnum nodeType, String resourceId, String id, boolean inTransaction) {

		Either<AdditionalInfoParameterInfo, StorageOperationStatus> result = null;

		try {

			Either<AdditionalInfoParameterInfo, TitanOperationStatus> either = this.getAdditionalInformationParameter(nodeType, resourceId, id);

			if (either.isRight()) {
				log.error("Failed to fetch additional information property with id {} of component {}", id, resourceId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(either.right().value()));
			} else {
				AdditionalInfoParameterInfo additionalInformationDefinition = either.left().value();
				result = Either.left(additionalInformationDefinition);
			}

			return result;

		} finally {
			commitOrRollback(inTransaction, result);
		}
	}

	@Override
	public Either<AdditionalInformationDefinition, StorageOperationStatus> deleteAllAdditionalInformationParameters(NodeTypeEnum nodeType, String resourceId, boolean inTransaction) {

		Either<AdditionalInformationDefinition, StorageOperationStatus> result = null;

		try {

			Either<ImmutablePair<AdditionalInfoParameterData, GraphEdge>, TitanOperationStatus> getResult = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), resourceId, GraphEdgeLabels.ADDITIONAL_INFORMATION,
					NodeTypeEnum.AdditionalInfoParameters, AdditionalInfoParameterData.class);

			if (getResult.isRight()) {
				TitanOperationStatus status = getResult.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					return Either.right(StorageOperationStatus.OK);
				} else {
					BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedDeleteNodeError, "DeleteAdditionalInformationNode", "additional information of " + nodeType.getName() + " " + resourceId, String.valueOf(status));
					BeEcompErrorManager.getInstance().logBeFailedDeleteNodeError("DeleteAdditionalInformationNode", "additional information of " + nodeType.getName() + " " + resourceId, String.valueOf(status));
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				}
				return result;
			}

			ImmutablePair<AdditionalInfoParameterData, GraphEdge> value = getResult.left().value();
			AdditionalInfoParameterData parameterData = value.getLeft();

			Either<AdditionalInfoParameterData, TitanOperationStatus> deleteNodeRes = titanGenericDao.deleteNode(parameterData, AdditionalInfoParameterData.class);
			if (deleteNodeRes.isRight()) {
				TitanOperationStatus status = getResult.right().value();
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedDeleteNodeError, "DeleteAdditionalInformationNode", (String) parameterData.getUniqueId(), String.valueOf(status));
				BeEcompErrorManager.getInstance().logBeFailedDeleteNodeError("DeleteAdditionalInformationNode", (String) parameterData.getUniqueId(), String.valueOf(status));
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			AdditionalInformationDefinition informationDefinition = convertAdditionalInformationDataToDefinition(deleteNodeRes.left().value(), resourceId);

			result = Either.left(informationDefinition);

			return result;

		} finally {
			commitOrRollback(inTransaction, result);
		}
	}

	private TitanOperationStatus verifyNodeTypeVsComponent(NodeTypeEnum nodeType, String componentId) {
		Either<TitanVertex, TitanOperationStatus> vertexByProperty = titanGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(nodeType), componentId);
		if (vertexByProperty.isRight()) {
			TitanOperationStatus status = vertexByProperty.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.INVALID_ID;
			}
			return status;
		} else {
			Vertex v = vertexByProperty.left().value();
			String label = (String) v.property(GraphPropertiesDictionary.LABEL.getProperty()).value();
			if (label != null) {
				if (false == label.equals(nodeType.getName())) {
					log.debug("The node type {} is not appropriate to component {}", nodeType, componentId);
					return TitanOperationStatus.INVALID_ID;
				}
			} else {
				log.debug("The node type {}  with id {} does not have a label property.", nodeType, componentId);
				return TitanOperationStatus.INVALID_ID;
			}
		}
		return TitanOperationStatus.OK;
	}

}
