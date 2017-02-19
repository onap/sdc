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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.misc.Graph;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.graph.GraphElementFactory;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.ICapabilityOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.CapabilityData;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.annotation.JsonAppend.Prop;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@Component("capability-operation")
public class CapabilityOperation extends AbstractOperation implements ICapabilityOperation {

	public CapabilityOperation() {
		super();
	}

	private static Logger log = LoggerFactory.getLogger(CapabilityOperation.class.getName());

	@Autowired
	private PropertyOperation propertyOperation;

	@Autowired
	private TitanGenericDao titanGenericDao;

	/**
	 * FOR TEST ONLY
	 * 
	 * @param titanGenericDao
	 */
	public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
	}

	@Override
	public Either<CapabilityDefinition, StorageOperationStatus> addCapability(String resourceId, String capabilityName, CapabilityDefinition capabilityDefinition, boolean inTransaction) {

		Either<CapabilityDefinition, StorageOperationStatus> result = null;

		try {

			Either<CapabilityData, TitanOperationStatus> addCapStatus = addCapabilityToResource(resourceId, capabilityName, capabilityDefinition);

			if (addCapStatus.isRight()) {
				log.debug("Failed to add capability {} [ {} ] to graph", capabilityName, capabilityDefinition);
				BeEcompErrorManager.getInstance().logBeFailedCreateNodeError("Add Capability", capabilityName, String.valueOf(addCapStatus.right().value()));
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(addCapStatus.right().value()));
				return result;
			} else {
				CapabilityData capabilityData = addCapStatus.left().value();

				String capabilityUid = capabilityData.getUniqueId();
				Either<CapabilityDefinition, StorageOperationStatus> capabilityRes = getCapability(capabilityUid, true);
				log.debug("After fetching capability {} with uid {}. Status is {}", capabilityName, capabilityUid, capabilityRes);

				if (capabilityRes.isRight()) {
					StorageOperationStatus status = capabilityRes.right().value();
					log.debug("Failed to fetch capability {] with uid {}. Status is {}", capabilityName, capabilityUid, status);
					result = Either.right(status);
					return result;
				}

				CapabilityDefinition value = capabilityRes.left().value();
				log.debug("The returned CapabilityDefinition is {}", value);
				result = Either.left(value);

				return result;
			}
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
	public StorageOperationStatus addCapability(TitanVertex metadataVertex, String resourceId, String capabilityName, CapabilityDefinition capabilityDefinition, boolean inTransaction) {

		StorageOperationStatus result = StorageOperationStatus.OK;
		try {

			TitanOperationStatus addCapStatus = addCapabilityToResource(metadataVertex, resourceId, capabilityName, capabilityDefinition);

			if (!addCapStatus.equals(TitanOperationStatus.OK)) {
				log.debug("Failed to add capability {} [ {} ]", capabilityName, capabilityDefinition);
				BeEcompErrorManager.getInstance().logBeFailedCreateNodeError("Add Capability", capabilityName, String.valueOf(addCapStatus));
				result = DaoStatusConverter.convertTitanStatusToStorageStatus(addCapStatus);
			}
		} finally {
			if (false == inTransaction) {
				if (result == null || !result.equals(TitanOperationStatus.OK)) {
					log.debug("Going to execute rollback on graph.");
					BeEcompErrorManager.getInstance().logBeExecuteRollbackError("Rollback on graph");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
		return result;
	}

	private CapabilityDefinition convertCDataToCDefinition(CapabilityData capabilityData) {

		CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
		capabilityDefinition.setType(capabilityData.getType());

		// TODO esofer do something

		return capabilityDefinition;
	}

	@Override
	public Either<CapabilityDefinition, StorageOperationStatus> getCapability(String uniqueId) {

		return getCapability(uniqueId, false);
	}

	public Either<Map<String, CapabilityDefinition>, StorageOperationStatus> getAllCapabilitiesOfResource(String resourceId, boolean recursively, boolean inTransaction) {

		Map<String, CapabilityDefinition> capabilities = new HashMap<>();
		Either<Map<String, CapabilityDefinition>, StorageOperationStatus> result = null;
		Set<String> caseInsensitiveCapabilityNames = new HashSet<>();

		try {
			TitanOperationStatus status = getAllCapabilitiesRecusive(NodeTypeEnum.Resource, resourceId, recursively, capabilities, caseInsensitiveCapabilityNames, inTransaction);
			if (!status.equals(TitanOperationStatus.OK)) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}
			result = Either.left(capabilities);
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

	public TitanOperationStatus getAllCapabilitiesRecusive(NodeTypeEnum nodeType, String resourceId, boolean recursively, Map<String, CapabilityDefinition> capabilities, Set<String> caseInsensitiveCapabilityNames, boolean inTransaction) {

		TitanOperationStatus findStatus;

		if (recursively) {
			findStatus = findAllCapabilitiesRecursively(resourceId, capabilities, caseInsensitiveCapabilityNames);

		} else {
			findStatus = getCapabilitisOfResourceOnly(resourceId, capabilities, caseInsensitiveCapabilityNames);
		}
		if (!findStatus.equals(TitanOperationStatus.OK)) {
			return findStatus;
		}

		List<String> derivedFromList = new ArrayList<>();
		TitanOperationStatus fillResourceDerivedListFromGraph = fillResourceDerivedListFromGraph(resourceId, derivedFromList);
		if (!fillResourceDerivedListFromGraph.equals(TitanOperationStatus.OK)) {
			log.debug("fail to find all valid sources of capability. status = {}", fillResourceDerivedListFromGraph.name());
			return fillResourceDerivedListFromGraph;
		}
		capabilities.forEach((name, capability) -> capability.setCapabilitySources(derivedFromList));
		return TitanOperationStatus.OK;
	}

	protected TitanOperationStatus findAllCapabilitiesRecursively(String resourceId, Map<String, CapabilityDefinition> capabilities, Set<String> caseInsensitiveCapabilityNames) {

		TitanOperationStatus resourceCapabilitiesStatus = getCapabilitisOfResourceOnly(resourceId, capabilities, caseInsensitiveCapabilityNames);

		if (!resourceCapabilitiesStatus.equals(TitanOperationStatus.OK)) {
			return resourceCapabilitiesStatus;
		}

		Either<ImmutablePair<ResourceMetadataData, GraphEdge>, TitanOperationStatus> parentNodes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Resource,
				ResourceMetadataData.class);

		if (parentNodes.isRight()) {
			TitanOperationStatus parentNodesStatus = parentNodes.right().value();
			if (!parentNodesStatus.equals(TitanOperationStatus.NOT_FOUND)) {
				log.debug("Failed to find parent capabilities of resource {}. status is {}", resourceId, parentNodesStatus);
				BeEcompErrorManager.getInstance().logBeFailedFindParentError("Fetch parent capabilities", resourceId, String.valueOf(parentNodesStatus));
				return parentNodesStatus;
			}
		}
		if (parentNodes.isLeft()) {
			ImmutablePair<ResourceMetadataData, GraphEdge> parnetNodePair = parentNodes.left().value();
			String parentUniqueId = parnetNodePair.getKey().getMetadataDataDefinition().getUniqueId();
			TitanOperationStatus addParentIntStatus = findAllCapabilitiesRecursively(parentUniqueId, capabilities, caseInsensitiveCapabilityNames);

			if (addParentIntStatus != TitanOperationStatus.OK) {
				log.debug("Failed to fetch all capabilities of resource {}", parentUniqueId);
				return addParentIntStatus;
			}
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus getCapabilitisOfResourceOnly(String resourceId, Map<String, CapabilityDefinition> capabilities, Set<String> caseInsensitiveCapabilityNames) {
		Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> allCapabilitiesRes = getAllCapabilitiesPairs(resourceId);
		if (allCapabilitiesRes.isRight()) {
			TitanOperationStatus status = allCapabilitiesRes.right().value();
			log.debug("After fetching all capabilities of resource {}. status is {}", resourceId, status);
			if (status.equals(TitanOperationStatus.NOT_FOUND)) {
				status = TitanOperationStatus.OK;
			}
			return status;
		}

		List<ImmutablePair<CapabilityData, GraphEdge>> capabilityPairs = allCapabilitiesRes.left().value();

		if (capabilityPairs != null) {
			for (ImmutablePair<CapabilityData, GraphEdge> capabilityPair : capabilityPairs) {
				CapabilityData capabilityData = capabilityPair.getKey();
				GraphEdge graphEdge = capabilityPair.getValue();
				Map<String, Object> edgeProps = graphEdge.getProperties();
				if (edgeProps != null) {
					String capabilityName = (String) edgeProps.get(GraphPropertiesDictionary.NAME.getProperty());
					if (capabilityName == null) {
						log.error("Capability name was not found for capability {}", capabilityData.getUniqueId());
						return TitanOperationStatus.INVALID_ELEMENT;
					}
					Either<CapabilityDefinition, TitanOperationStatus> capabilityDefRes = getCapabilityByCapabilityData(capabilityData);
					if (capabilityDefRes.isRight()) {
						TitanOperationStatus status = capabilityDefRes.right().value();
						return status;
					}
					CapabilityDefinition capabilityDefinition = capabilityDefRes.left().value();
					capabilityDefinition.setOwnerId(resourceId);
					log.debug("Before adding capability {} with definition {} to result.", capabilityName, capabilityDefinition);
					// US631462
					if (caseInsensitiveCapabilityNames.contains(capabilityName.toLowerCase())) {
						log.debug("The capability {} was already defined in derived resource (case insensitive). Ignore {} from resource {}", capabilityName, capabilityName, resourceId);
					} else {
						capabilities.put(capabilityName, capabilityDefinition);
						caseInsensitiveCapabilityNames.add(capabilityName.toLowerCase());
					}
				} else {
					log.debug("Capability name was not found for capability {}", capabilityData.getUniqueId());
					BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("Fetch Capability", capabilityData.getUniqueId(), String.valueOf(TitanOperationStatus.INVALID_ELEMENT));
					return TitanOperationStatus.INVALID_ELEMENT;
				}

			}
		}
		return TitanOperationStatus.OK;
	}

	@Override
	public Either<CapabilityDefinition, StorageOperationStatus> getCapability(String uniqueId, boolean inTransaction) {

		Either<CapabilityDefinition, StorageOperationStatus> result = null;

		try {
			Either<CapabilityData, TitanOperationStatus> capabiltyRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Capability), uniqueId, CapabilityData.class);
			if (capabiltyRes.isRight()) {
				TitanOperationStatus status = capabiltyRes.right().value();
				log.debug("Failed to retrieve capability {} from graph. Status is {}", uniqueId, status);

				BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("Fetch Capability", uniqueId, String.valueOf(status));
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}
			CapabilityData capabilityData = capabiltyRes.left().value();
			CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
			capabilityDefinition.setDescription(capabilityData.getDescription());
			capabilityDefinition.setUniqueId(capabilityData.getUniqueId());
			capabilityDefinition.setValidSourceTypes(capabilityData.getValidSourceTypes());
			capabilityDefinition.setMinOccurrences(capabilityData.getMinOccurrences());
			capabilityDefinition.setMaxOccurrences(capabilityData.getMaxOccurrences());

			Either<CapabilityTypeData, TitanOperationStatus> capabilityTypeRes = getCapabilityTypeOfCapability(uniqueId);
			if (capabilityTypeRes.isRight()) {
				TitanOperationStatus status = capabilityTypeRes.right().value();
				log.debug("Failed to retrieve capability type of capability {}. Status is {}", uniqueId, status);
				BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("Fetch Capability", uniqueId, String.valueOf(status));

				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			CapabilityTypeData capabilityTypeData = capabilityTypeRes.left().value();
			capabilityDefinition.setType(capabilityTypeData.getCapabilityTypeDataDefinition().getType());

			Either<ImmutablePair<ResourceMetadataData, GraphEdge>, TitanOperationStatus> parentNode = titanGenericDao.getParentNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Capability), uniqueId, GraphEdgeLabels.CAPABILITY,
					NodeTypeEnum.Resource, ResourceMetadataData.class);
			if (parentNode.isRight()) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(parentNode.right().value()));
			} else {
				ImmutablePair<ResourceMetadataData, GraphEdge> pair = parentNode.left().value();
				capabilityDefinition.setOwnerId(pair.left.getMetadataDataDefinition().getUniqueId());
				List<String> derivedFromList = new ArrayList<>();
				// derivedFromList.add(pair.left.getMetadataDataDefinition().getName());
				TitanOperationStatus fillResourceDerivedListFromGraph = fillResourceDerivedListFromGraph(pair.left.getMetadataDataDefinition().getUniqueId(), derivedFromList);
				if (fillResourceDerivedListFromGraph.equals(TitanOperationStatus.OK)) {
					capabilityDefinition.setCapabilitySources(derivedFromList);
				}
			}

			Either<List<PropertyDefinition>, TitanOperationStatus> getPropertiesRes = getPropertiesOfCapability(uniqueId, capabilityTypeData.getCapabilityTypeDataDefinition().getType());
			if (getPropertiesRes.isRight() && !getPropertiesRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				TitanOperationStatus status = getPropertiesRes.right().value();
				log.debug("Failed to retrieve properties of capability {}. Status is {}", uniqueId, status);
				BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("Fetch Properties of Capability", uniqueId, String.valueOf(status));

				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}
			if (getPropertiesRes.isLeft()) {
				List<ComponentInstanceProperty> properties = new ArrayList<>();
				for (PropertyDefinition property : getPropertiesRes.left().value()) {
					properties.add(new ComponentInstanceProperty(property, null, null));
				}
				capabilityDefinition.setProperties(properties);
			}
			result = Either.left(capabilityDefinition);

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

	private TitanOperationStatus fillResourceDerivedListFromGraph(String uniqueId, List<String> derivedFromList) {

		Either<ResourceMetadataData, TitanOperationStatus> resourceNode = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), uniqueId, ResourceMetadataData.class);

		if (resourceNode.isRight()) {
			TitanOperationStatus parentNodesStatus = resourceNode.right().value();
			if (!parentNodesStatus.equals(TitanOperationStatus.NOT_FOUND)) {
				log.debug("Failed to find resource {} . status is {}", uniqueId, parentNodesStatus);
				return parentNodesStatus;
			}
		}

		derivedFromList.add(((ResourceMetadataDataDefinition) resourceNode.left().value().getMetadataDataDefinition()).getToscaResourceName());
		Either<List<ImmutablePair<ResourceMetadataData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), uniqueId, GraphEdgeLabels.DERIVED_FROM,
				NodeTypeEnum.Resource, ResourceMetadataData.class);

		if (childrenNodes.isRight() && (childrenNodes.right().value() != TitanOperationStatus.NOT_FOUND)) {
			return childrenNodes.right().value();
		} else if (childrenNodes.isLeft()) {

			List<ImmutablePair<ResourceMetadataData, GraphEdge>> pairList = childrenNodes.left().value();
			for (ImmutablePair<ResourceMetadataData, GraphEdge> pair : pairList) {
				return fillResourceDerivedListFromGraph(pair.left.getMetadataDataDefinition().getUniqueId(), derivedFromList);
			}
		}
		return TitanOperationStatus.OK;
	}

	public Either<CapabilityDefinition, TitanOperationStatus> getCapabilityByCapabilityData(CapabilityData capabilityData) {

		Either<CapabilityDefinition, TitanOperationStatus> result;

		CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
		capabilityDefinition.setDescription(capabilityData.getDescription());
		capabilityDefinition.setUniqueId(capabilityData.getUniqueId());
		capabilityDefinition.setValidSourceTypes(capabilityData.getValidSourceTypes());
		capabilityDefinition.setMinOccurrences(capabilityData.getMinOccurrences());
		capabilityDefinition.setMaxOccurrences(capabilityData.getMaxOccurrences());

		String capabilityUid = capabilityData.getUniqueId();
		Either<CapabilityTypeData, TitanOperationStatus> capabilityTypeRes = getCapabilityTypeOfCapability(capabilityUid);
		if (capabilityTypeRes.isRight()) {
			TitanOperationStatus status = capabilityTypeRes.right().value();
			log.debug("Failed to retrieve capability type of capability {} . status is {}", capabilityUid, status);
			BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("Fetch Capability", capabilityUid, String.valueOf(status));

			return Either.right(status);
		}

		CapabilityTypeData capabilityTypeData = capabilityTypeRes.left().value();
		capabilityDefinition.setType(capabilityTypeData.getCapabilityTypeDataDefinition().getType());

		Either<List<PropertyDefinition>, TitanOperationStatus> capabilityPropertiesRes = getPropertiesOfCapability(capabilityUid, capabilityDefinition.getType());
		if (capabilityPropertiesRes.isRight() && !capabilityPropertiesRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
			TitanOperationStatus status = capabilityPropertiesRes.right().value();
			log.debug("Failed to retrieve properties of capability {} . status is {}", capabilityUid, status);
			BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("Fetch Capability", capabilityUid, String.valueOf(status));

			result = Either.right(status);
			return result;
		}
		if (capabilityPropertiesRes.isLeft()) {
			List<ComponentInstanceProperty> properties = new ArrayList<>();
			for (PropertyDefinition property : capabilityPropertiesRes.left().value()) {
				properties.add(new ComponentInstanceProperty(property, null, null));
			}
			capabilityDefinition.setProperties(properties);
		}
		result = Either.left(capabilityDefinition);
		return result;
	}

	public Either<CapabilityDefinition, TitanOperationStatus> getCapabilityByCapabilityData(TitanVertex capabilityDataVertex) {

		Either<CapabilityDefinition, TitanOperationStatus> result;

		CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
		Map<String, Object> props = titanGenericDao.getProperties(capabilityDataVertex);
		CapabilityData capabilityData = GraphElementFactory.createElement((String) props.get(GraphPropertiesDictionary.LABEL.getProperty()), GraphElementTypeEnum.Node, props, CapabilityData.class);
		capabilityDefinition.setDescription(capabilityData.getDescription());
		capabilityDefinition.setUniqueId(capabilityData.getUniqueId());
		capabilityDefinition.setValidSourceTypes(capabilityData.getValidSourceTypes());
		capabilityDefinition.setMinOccurrences(capabilityData.getMinOccurrences());
		capabilityDefinition.setMaxOccurrences(capabilityData.getMaxOccurrences());

		String capabilityUid = capabilityData.getUniqueId();
		Either<CapabilityTypeData, TitanOperationStatus> capabilityTypeRes = getCapabilityTypeOfCapability(capabilityUid);
		if (capabilityTypeRes.isRight()) {
			TitanOperationStatus status = capabilityTypeRes.right().value();
			log.debug("Failed to retrieve capability type of capability {} . status is {}", capabilityUid, status);
			BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("Fetch Capability", capabilityUid, String.valueOf(status));

			return Either.right(status);
		}

		CapabilityTypeData capabilityTypeData = capabilityTypeRes.left().value();
		capabilityDefinition.setType(capabilityTypeData.getCapabilityTypeDataDefinition().getType());

		Either<List<PropertyDefinition>, TitanOperationStatus> capabilityPropertiesRes = getPropertiesOfCapability(capabilityUid, capabilityDefinition.getType());
		if (capabilityPropertiesRes.isRight() && !capabilityPropertiesRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
			TitanOperationStatus status = capabilityPropertiesRes.right().value();
			log.debug("Failed to retrieve properties of capability {} . status is {}", capabilityUid, status);
			BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError("Fetch Capability", capabilityUid, String.valueOf(status));

			result = Either.right(status);
			return result;
		}
		if (capabilityPropertiesRes.isLeft()) {
			List<ComponentInstanceProperty> properties = new ArrayList<>();
			for (PropertyDefinition property : capabilityPropertiesRes.left().value()) {
				properties.add(new ComponentInstanceProperty(property, null, null));
			}
			capabilityDefinition.setProperties(properties);
		}
		result = Either.left(capabilityDefinition);
		return result;
	}

	public Either<List<PropertyDefinition>, TitanOperationStatus> getPropertiesOfCapability(String capabilityUid, String capabilityType) {
		log.debug("Before getting properties of capability {} from graph ", capabilityUid);

		List<PropertyDefinition> properties;
		Either<List<PropertyDefinition>, TitanOperationStatus> result = null;
		Either<Map<String, PropertyDefinition>, TitanOperationStatus> getPropertiesOfCapabilityTypeRes = null;
		Either<List<ImmutablePair<PropertyData, GraphEdge>>, TitanOperationStatus> getPropertiesOfCapabilityRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Capability), capabilityUid, GraphEdgeLabels.PROPERTY,
				NodeTypeEnum.Property, PropertyData.class);
		if (getPropertiesOfCapabilityRes.isRight() && !getPropertiesOfCapabilityRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
			TitanOperationStatus status = getPropertiesOfCapabilityRes.right().value();
			log.debug("failed to get properties of capability with id {}. status={}", capabilityUid, status);
			result = Either.right(status);
		}
		if (result == null) {
			String capabilityTypeUid = UniqueIdBuilder.buildCapabilityTypeUid(capabilityType);
			getPropertiesOfCapabilityTypeRes = getAllCapabilityTypePropertiesFromAllDerivedFrom(capabilityTypeUid);
			if (getPropertiesOfCapabilityTypeRes.isRight() && !getPropertiesOfCapabilityTypeRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				TitanOperationStatus status = getPropertiesOfCapabilityTypeRes.right().value();
				log.error("Failed to retrieve properties for capability type {} from graph. Status is {}", capabilityType, status);
				result = Either.right(status);
			}
		}
		if (result == null) {
			result = getPropertiesOfCapabilityTypeRes.isRight()
					? (getPropertiesOfCapabilityRes.isRight() ? Either.right(TitanOperationStatus.NOT_FOUND)
							: Either.left(getPropertiesOfCapabilityRes.left().value().stream().map(p -> propertyOperation.convertPropertyDataToPropertyDefinition(p.getKey(), null, capabilityUid)).collect(Collectors.toList())))
					: (getPropertiesOfCapabilityRes.isRight() ? Either.left(getPropertiesOfCapabilityTypeRes.left().value().values().stream().collect(Collectors.toList())) : null);
		}
		if (result == null) {
			Map<String, PropertyDefinition> propertiesOfCapabilityType = getPropertiesOfCapabilityTypeRes.left().value();
			properties = getPropertiesOfCapabilityRes.left().value().stream()
					.map(p -> propertyOperation.convertPropertyDataToPropertyDefinition(p.getKey(), (String) p.getRight().getProperties().get(GraphPropertiesDictionary.NAME.getProperty()), capabilityUid)).collect(Collectors.toList());
			properties.stream().forEach(p -> propertiesOfCapabilityType.remove(p.getName()));
			properties.addAll(propertiesOfCapabilityType.values());
			result = Either.left(properties);
		}
		return result;
	}

	protected Either<CapabilityTypeData, TitanOperationStatus> getCapabilityTypeOfCapability(String uniqueId) {

		Either<ImmutablePair<CapabilityTypeData, GraphEdge>, TitanOperationStatus> capabilityTypeRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Capability), uniqueId, GraphEdgeLabels.TYPE_OF, NodeTypeEnum.CapabilityType,
				CapabilityTypeData.class);

		if (capabilityTypeRes.isRight()) {
			TitanOperationStatus status = capabilityTypeRes.right().value();//
			log.debug("Cannot find capability type associated with capability {}. Status is {}", uniqueId, status);
			BeEcompErrorManager.getInstance().logBeFailedFindAssociationError("Fetch Capability type", NodeTypeEnum.CapabilityType.getName(), uniqueId, String.valueOf(status));
			return Either.right(capabilityTypeRes.right().value());
		}

		CapabilityTypeData capabilityTypeData = capabilityTypeRes.left().value().getKey();

		return Either.left(capabilityTypeData);

	}

	@Override
	public Either<CapabilityDefinition, StorageOperationStatus> getCapability(String capabilityName, String resourceId) {
		return getCapability(UniqueIdBuilder.buildCapabilityUid(resourceId, capabilityName));
	}

	@Override
	public Either<CapabilityDefinition, StorageOperationStatus> getCapability(String capabilityName, String resourceId, boolean inTransaction) {
		return getCapability(UniqueIdBuilder.buildCapabilityUid(resourceId, capabilityName), inTransaction);
	}

	@Override
	public Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> getAllCapabilitiesPairs(String resourceId) {

		Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> capabilitiesNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.CAPABILITY,
				NodeTypeEnum.Capability, CapabilityData.class);

		log.debug("After looking for all capabilities under resource {}. Status is {}", resourceId, capabilitiesNodes);
		if (capabilitiesNodes.isRight()) {
			TitanOperationStatus status = capabilitiesNodes.right().value();
			return Either.right(status);
		}

		List<ImmutablePair<CapabilityData, GraphEdge>> capabilities = capabilitiesNodes.left().value();
		if (capabilities == null || true == capabilities.isEmpty()) {
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

		return Either.left(capabilitiesNodes.left().value());
	}

	private Either<CapabilityData, TitanOperationStatus> addCapabilityToResource(String resourceId, String capabilityName, CapabilityDefinition capabilityDefinition) {

		log.debug("Going to add capability {} [ {} ] to resource uid {}", capabilityName, capabilityDefinition, resourceId);

		Either<CapabilityData, TitanOperationStatus> createCapRes = createCapability(resourceId, capabilityName, capabilityDefinition);

		log.debug("After creating capability node in graph. status is {}", createCapRes);
		if (createCapRes.isRight()) {
			TitanOperationStatus status = createCapRes.right().value();
			log.error("Failed to create capability data node in graph. status is {}", status);
			return Either.right(status);
		}
		CapabilityData capabilityData = createCapRes.left().value();

		String capabilityType = capabilityDefinition.getType();

		log.debug("Going to associate capability {} to its capabilityType {}", capabilityName, capabilityType);

		Either<GraphRelation, TitanOperationStatus> associateCapabilityTypeRes = associateCapabilityToCapabilityType(capabilityData, capabilityType);
		log.debug("After associating capability {} to its capabilityType {}. status is {}", capabilityName, capabilityType, associateCapabilityTypeRes);
		if (associateCapabilityTypeRes.isRight()) {
			TitanOperationStatus status = associateCapabilityTypeRes.right().value();
			log.error("Failed to associate capability {} to its capabilityType {} in graph. status is {} ", capabilityName, capabilityType, status);

			return Either.right(status);
		}
		List<ComponentInstanceProperty> ciProperties = capabilityDefinition.getProperties();
		if (ciProperties != null && !ciProperties.isEmpty()) {
			List<PropertyDefinition> properties = ciProperties.stream().map(prop -> new PropertyDefinition(prop)).collect(Collectors.toList());
			Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesRes = addPropertiesToCapability(capabilityData, capabilityType, properties);
			if (addPropertiesRes.isRight()) {
				TitanOperationStatus operationStatus = addPropertiesRes.right().value();
				return Either.right(operationStatus);
			}
		}

		Either<GraphRelation, TitanOperationStatus> associateResourceRes = associateResourceToCapability(resourceId, capabilityName, capabilityData);
		if (associateResourceRes.isRight()) {
			TitanOperationStatus status = associateResourceRes.right().value();
			log.error("Failed to associate resource " + resourceId + " to capability " + capabilityData + ". status is " + status);
			return Either.right(status);
		}

		return Either.left(capabilityData);

	}

	private TitanOperationStatus addCapabilityToResource(TitanVertex metadataVertex, String resourceId, String capabilityName, CapabilityDefinition capabilityDefinition) {

		log.debug("Going to add capability {} [ {} ] to resource uid {}", capabilityName, capabilityDefinition, resourceId);

		Either<TitanVertex, TitanOperationStatus> createCapRes = createCapabilityVertex(resourceId, capabilityName, capabilityDefinition);

		log.debug("After creating capability node in graph. status is {}", createCapRes);
		if (createCapRes.isRight()) {
			TitanOperationStatus status = createCapRes.right().value();
			log.error("Failed to create capability data node in graph. status is {}", status);
			return status;
		}
		TitanVertex capabilityVertex = createCapRes.left().value();

		String capabilityType = capabilityDefinition.getType();

		log.debug("Going to associate capability {} to its capabilityType {}", capabilityName, capabilityType);

		TitanOperationStatus associateCapabilityTypeRes = associateCapabilityToCapabilityType(capabilityVertex, capabilityType);
		log.debug("After associating capability {} to its capabilityType {}. status is {}", capabilityName, capabilityType, associateCapabilityTypeRes);
		if (!associateCapabilityTypeRes.equals(TitanOperationStatus.OK)) {
			log.error("Failed to associate capability {} to its capabilityType {} in graph. status is {} ", capabilityName, capabilityType, associateCapabilityTypeRes);
			return associateCapabilityTypeRes;
		}
		List<ComponentInstanceProperty> ciProperties = capabilityDefinition.getProperties();
		if (ciProperties != null && !ciProperties.isEmpty()) {
			String capabiltyId = (String) titanGenericDao.getProperty(capabilityVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
			List<PropertyDefinition> properties = ciProperties.stream().map(prop -> new PropertyDefinition(prop)).collect(Collectors.toList());
			TitanOperationStatus addPropertiesRes = addPropertiesToCapability(capabilityVertex, capabilityType, properties, capabiltyId);
			if (!addPropertiesRes.equals(TitanOperationStatus.OK)) {
				return addPropertiesRes;
			}
		}

		TitanOperationStatus associateResourceRes = associateResourceToCapability(resourceId, capabilityName, capabilityVertex, metadataVertex);
		if (!associateResourceRes.equals(TitanOperationStatus.OK)) {
			log.error("Failed to associate resource{} to capability {}. status is {} ", resourceId, capabilityName, associateResourceRes);
		}

		return associateResourceRes;

	}

	private Either<GraphRelation, TitanOperationStatus> associateCapabilityToCapabilityType(CapabilityData capabilityData, String capabilityType) {
		UniqueIdData capabilityTypeIdData = new UniqueIdData(NodeTypeEnum.CapabilityType, UniqueIdBuilder.buildCapabilityTypeUid(capabilityType));
		log.debug("Before associating {} to capability type {}.", capabilityData, capabilityType);
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(capabilityData, capabilityTypeIdData, GraphEdgeLabels.TYPE_OF, null);
		log.debug("After associating {} to capability type {}. status is {}", capabilityData, capabilityType, createRelResult);
		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			log.error("Failed to associate capability {} to capability type {} in graph. Status is {}",capabilityData, capabilityTypeIdData, operationStatus);
			return Either.right(operationStatus);
		}
		return Either.left(createRelResult.left().value());

	}

	private TitanOperationStatus associateCapabilityToCapabilityType(TitanVertex capabilityVertex, String capabilityType) {

		UniqueIdData capabilityTypeIdData = new UniqueIdData(NodeTypeEnum.CapabilityType, UniqueIdBuilder.buildCapabilityTypeUid(capabilityType));

		log.debug("Before associating {} to capability type {}.", capabilityVertex, capabilityType);
		TitanOperationStatus createRelResult = titanGenericDao.createEdge(capabilityVertex, capabilityTypeIdData, GraphEdgeLabels.TYPE_OF, null);
		log.trace("After associating {} to capability type {}. status is {}", capabilityVertex, capabilityType, createRelResult);
		if (!createRelResult.equals(TitanOperationStatus.OK)) {
			log.error("Failed to associate capability {} to capability type {} in graph. status is {}", capabilityVertex, capabilityTypeIdData, createRelResult);
		}
		return createRelResult;
	}

	private Either<GraphRelation, TitanOperationStatus> associateResourceToCapability(String resourceId, String capabilityName, CapabilityData capabilityData) {

		UniqueIdData resourceIdData = new UniqueIdData(NodeTypeEnum.Resource, resourceId);

		log.debug("Before associating resource {} to capability {}.", resourceId, capabilityData);
		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), capabilityName);
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(resourceIdData, capabilityData, GraphEdgeLabels.CAPABILITY, props);
		log.debug("After associating resource {} to capability {}. Status is {}", resourceId, capabilityData, createRelResult);
		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			log.error("Failed to associate resource {} to capability {} in graph. Status is {}", resourceId, capabilityData, operationStatus);
			return Either.right(operationStatus);
		}

		return Either.left(createRelResult.left().value());

	}

	private TitanOperationStatus associateResourceToCapability(String resourceId, String capabilityName, TitanVertex capabilityVertex, TitanVertex resourceVertex) {

		log.debug("Before associating resource {} to capability {}.", resourceId, capabilityName);
		Map<String, Object> props = new HashMap<>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), capabilityName);
		TitanOperationStatus createRelResult = titanGenericDao.createEdge(resourceVertex, capabilityVertex, GraphEdgeLabels.CAPABILITY, props);
		log.debug("After associating resource {} to capability {}. status is {}", resourceId, capabilityName, createRelResult);
		if (!createRelResult.equals(TitanOperationStatus.OK)) {
			log.error("Failed to associate resource {} to capability {} in graph. status is {}", resourceId, capabilityName, createRelResult);
		}

		return createRelResult;

	}

	/**
	 * 
	 * create capability node in the graph
	 * 
	 * @param resourceId
	 * @param capabilityName
	 * @param capabilityDefinition
	 * @return
	 */
	private Either<CapabilityData, TitanOperationStatus> createCapability(String resourceId, String capabilityName, CapabilityDefinition capabilityDefinition) {

		CapabilityData capabilityData = new CapabilityData();
		String uid = UniqueIdBuilder.buildCapabilityUid(resourceId, capabilityName);
		capabilityData.setUniqueId(uid);
		Long creationTime = System.currentTimeMillis();
		capabilityData.setCreationTime(creationTime);
		capabilityData.setModificationTime(creationTime);
		capabilityData.setValidSourceTypes(capabilityDefinition.getValidSourceTypes());
		capabilityData.setMinOccurrences(capabilityDefinition.getMinOccurrences());
		capabilityData.setMaxOccurrences(capabilityDefinition.getMaxOccurrences());
		capabilityData.setDescription(capabilityDefinition.getDescription());

		Either<CapabilityData, TitanOperationStatus> createNode = titanGenericDao.createNode(capabilityData, CapabilityData.class);

		log.debug("After creating capability node in the graph. status is {}", createNode);

		return createNode;
	}

	private Either<TitanVertex, TitanOperationStatus> createCapabilityVertex(String resourceId, String capabilityName, CapabilityDefinition capabilityDefinition) {

		CapabilityData capabilityData = new CapabilityData();
		String uid = UniqueIdBuilder.buildCapabilityUid(resourceId, capabilityName);
		capabilityData.setUniqueId(uid);
		Long creationTime = System.currentTimeMillis();
		capabilityData.setCreationTime(creationTime);
		capabilityData.setModificationTime(creationTime);
		capabilityData.setValidSourceTypes(capabilityDefinition.getValidSourceTypes());
		capabilityData.setMinOccurrences(capabilityDefinition.getMinOccurrences());
		capabilityData.setMaxOccurrences(capabilityDefinition.getMaxOccurrences());
		capabilityData.setDescription(capabilityDefinition.getDescription());

		Either<TitanVertex, TitanOperationStatus> createNode = titanGenericDao.createNode(capabilityData);

		log.debug("After creating capability node in the graph. status is {}", createNode);

		return createNode;
	}

	@Override
	public Either<CapabilityDefinition, StorageOperationStatus> addCapability(String resourceId, String capabilityName, CapabilityDefinition capabilityDefinition) {

		return addCapability(resourceId, capabilityName, capabilityDefinition, false);

	}

	public StorageOperationStatus deleteCapabilityFromGraph(String capabilityUid) {

		TitanOperationStatus resultStatus = null;

		Either<List<ImmutablePair<PropertyData, GraphEdge>>, TitanOperationStatus> deletePropertiesStatus = deletePropertiesOfCapability(capabilityUid);

		if (deletePropertiesStatus.isRight() && !deletePropertiesStatus.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
			resultStatus = deletePropertiesStatus.right().value();
		}
		if (resultStatus == null) {
			log.debug("Before deleting capability from graph {}", capabilityUid);
			Either<CapabilityData, TitanOperationStatus> deleteNodeStatus = titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Capability), capabilityUid, CapabilityData.class);
			if (deleteNodeStatus.isRight()) {
				resultStatus = deleteNodeStatus.right().value();
			}
		}
		if (resultStatus != null) {
			log.debug("failed to delete capability with id {}. status={}", capabilityUid, resultStatus);
			BeEcompErrorManager.getInstance().logBeFailedDeleteNodeError("Delete capability", capabilityUid, String.valueOf(resultStatus));
			return DaoStatusConverter.convertTitanStatusToStorageStatus(resultStatus);
		}
		return StorageOperationStatus.OK;
	}

	public Either<Map<String, CapabilityDefinition>, StorageOperationStatus> deleteAllCapabilities(String resourceId, boolean inTransaction) {

		Either<Map<String, CapabilityDefinition>, StorageOperationStatus> result = null;
		try {

			Either<Map<String, CapabilityDefinition>, TitanOperationStatus> deleteAllRes = deleteAllCapabilitiesOfResource(resourceId);
			if (deleteAllRes.isRight()) {
				TitanOperationStatus status = deleteAllRes.right().value();
				if (status != TitanOperationStatus.NOT_FOUND) {
					log.debug("Failed to delete capabilities of resource {}. Status is {}", resourceId, status);
				}
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			Map<String, CapabilityDefinition> value = deleteAllRes.left().value();
			result = Either.left(value);
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

	public Either<Map<String, CapabilityDefinition>, StorageOperationStatus> deleteAllCapabilities(String resourceId) {

		return deleteAllCapabilities(resourceId, false);

	}

	private Either<Map<String, CapabilityDefinition>, TitanOperationStatus> deleteAllCapabilitiesOfResource(String resourceId) {
		TitanOperationStatus resultStatus = null;
		Map<String, CapabilityDefinition> capabilities = new HashMap<>();
		Set<String> caseInsensitiveCapabilityNames = new HashSet<>();
		TitanOperationStatus capabilitisRes = getCapabilitisOfResourceOnly(resourceId, capabilities, caseInsensitiveCapabilityNames);
		if (capabilitisRes != TitanOperationStatus.OK) {
			return Either.right(capabilitisRes);
		}

		if (capabilities.isEmpty()) {
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

		for (Entry<String, CapabilityDefinition> entry : capabilities.entrySet()) {
			CapabilityDefinition capabilityDefinition = entry.getValue();
			String capabilityUid = capabilityDefinition.getUniqueId();

			log.debug("Before deleting properties of capability {} from graph", capabilityUid);

			Either<List<ImmutablePair<PropertyData, GraphEdge>>, TitanOperationStatus> deletePropertiesStatus = deletePropertiesOfCapability(capabilityUid);
			if (deletePropertiesStatus.isRight() && !deletePropertiesStatus.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				resultStatus = deletePropertiesStatus.right().value();
			}
			if (resultStatus == null) {
				Either<CapabilityData, TitanOperationStatus> deleteNodeRes = titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Capability), capabilityUid, CapabilityData.class);
				if (deleteNodeRes.isRight()) {
					resultStatus = deleteNodeRes.right().value();
				}
			}
			if (resultStatus != null) {
				log.debug("Failed to delete capability {} of resource {}", capabilityUid, resourceId);
				BeEcompErrorManager.getInstance().logBeFailedDeleteNodeError("Delete capability", capabilityUid, String.valueOf(resultStatus));
				return Either.right(resultStatus);
			}
		}

		return Either.left(capabilities);

	}

	public Map<String, List<CapabilityDefinition>> convertCapabilityMap(Map<String, CapabilityDefinition> capabilityMap, String ownerId, String ownerName) {

		Map<String, List<CapabilityDefinition>> typeToRequirementMap = new HashMap<>();
		capabilityMap.forEach((capabilityName, capability) -> {
			capability.setName(capabilityName);
			if (typeToRequirementMap.containsKey(capability.getType())) {
				typeToRequirementMap.get(capability.getType()).add(capability);
			} else {
				List<CapabilityDefinition> list = new ArrayList<>();
				list.add(capability);
				typeToRequirementMap.put(capability.getType(), list);
			}
		});
		return typeToRequirementMap;
	}

	public TitanOperationStatus getCapabilitySourcesList(String resourceId, List<String> derivedFromList) {
		Map<String, Object> propertiesToMatch = new HashMap<>();
		propertiesToMatch.put(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId);
		Either<List<ResourceMetadataData>, TitanOperationStatus> getResponse = titanGenericDao.getByCriteria(NodeTypeEnum.Resource, propertiesToMatch, ResourceMetadataData.class);
		if (getResponse.isRight()) {
			return getResponse.right().value();
		} else {
			String toscaResourceName = ((ResourceMetadataDataDefinition) getResponse.left().value().get(0).getMetadataDataDefinition()).getToscaResourceName();
			derivedFromList.add(toscaResourceName);
		}

		Either<List<ImmutablePair<ResourceMetadataData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.DERIVED_FROM,
				NodeTypeEnum.Resource, ResourceMetadataData.class);

		while (childrenNodes.isLeft()) {

			List<ImmutablePair<ResourceMetadataData, GraphEdge>> pairList = childrenNodes.left().value();
			ResourceMetadataData left = pairList.get(0).left;
			derivedFromList.add(((ResourceMetadataDataDefinition) left.getMetadataDataDefinition()).getToscaResourceName());
			String id = left.getMetadataDataDefinition().getUniqueId();
			childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), id, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Resource, ResourceMetadataData.class);
		}
		return TitanOperationStatus.OK;
	}

	private Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesToCapability(CapabilityData capabilityData, String capabilityType, List<PropertyDefinition> properties) {
		String capabilityTypeUid = UniqueIdBuilder.buildCapabilityTypeUid(capabilityType);
		Either<Map<String, PropertyDefinition>, TitanOperationStatus> allPropertiesOfCapabilityTypeRes = getAllCapabilityTypePropertiesFromAllDerivedFrom(capabilityTypeUid);
		if (allPropertiesOfCapabilityTypeRes.isRight() && !allPropertiesOfCapabilityTypeRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
			TitanOperationStatus operationStatus = allPropertiesOfCapabilityTypeRes.right().value();
			log.error("Failed to retrieve properties for capability type " + capabilityType + " from graph. status is " + operationStatus);
			return Either.right(operationStatus);
		}

		Map<String, PropertyDefinition> propertiesOfCapabilityType = null;

		if (allPropertiesOfCapabilityTypeRes.isLeft() && allPropertiesOfCapabilityTypeRes.left() != null && !allPropertiesOfCapabilityTypeRes.left().value().isEmpty()) {

			propertiesOfCapabilityType = allPropertiesOfCapabilityTypeRes.left().value();
			Either<List<PropertyDefinition>, TitanOperationStatus> validateAndReducePropertiesRes = validatePropertyUniqueness(propertiesOfCapabilityType, properties);
			if (validateAndReducePropertiesRes.isRight()) {
				TitanOperationStatus operationStatus = validateAndReducePropertiesRes.right().value();
				log.error("Failed to add properties to capability {} in graph. Status is {}", capabilityData.getUniqueId(), operationStatus);
				return Either.right(operationStatus);
			}
		}

		Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesToCapabilityRes = propertyOperation.addPropertiesToElementType(capabilityData.getUniqueId(), NodeTypeEnum.Capability, properties);
		if (addPropertiesToCapabilityRes.isRight()) {
			TitanOperationStatus operationStatus = addPropertiesToCapabilityRes.right().value();
			log.error("Failed to add properties to capability {} in graph. Status is {}", capabilityData.getUniqueId(), operationStatus);
			return Either.right(operationStatus);
		}
		return Either.left(addPropertiesToCapabilityRes.left().value());
	}

	private TitanOperationStatus addPropertiesToCapability(TitanVertex capabilityVertex, String capabilityType, List<PropertyDefinition> properties, String uniqueId) {
		String capabilityTypeUid = UniqueIdBuilder.buildCapabilityTypeUid(capabilityType);
		Either<Map<String, PropertyDefinition>, TitanOperationStatus> allPropertiesOfCapabilityTypeRes = getAllCapabilityTypePropertiesFromAllDerivedFrom(capabilityTypeUid);
		if (allPropertiesOfCapabilityTypeRes.isRight() && !allPropertiesOfCapabilityTypeRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
			TitanOperationStatus operationStatus = allPropertiesOfCapabilityTypeRes.right().value();
			log.error("Failed to retrieve properties for capability type {} from graph. status is {}", capabilityType, operationStatus);
			return operationStatus;
		}
		Map<String, PropertyDefinition> propertiesOfCapabilityType = null;

		if (allPropertiesOfCapabilityTypeRes.isLeft() && allPropertiesOfCapabilityTypeRes.left() != null && !allPropertiesOfCapabilityTypeRes.left().value().isEmpty()) {

			propertiesOfCapabilityType = allPropertiesOfCapabilityTypeRes.left().value();
			Either<List<PropertyDefinition>, TitanOperationStatus> validateAndReducePropertiesRes = validatePropertyUniqueness(propertiesOfCapabilityType, properties);
			if (validateAndReducePropertiesRes.isRight()) {
				TitanOperationStatus operationStatus = validateAndReducePropertiesRes.right().value();
				log.error("Failed to add properties to capability {} in graph. status is {}", capabilityVertex, operationStatus);
				return operationStatus;
			}
		}

		String capabiltyId = (String) titanGenericDao.getProperty(capabilityVertex, GraphPropertiesDictionary.UNIQUE_ID.getProperty());
		TitanOperationStatus addPropertiesToCapabilityRes = propertyOperation.addPropertiesToElementType(capabilityVertex, capabiltyId, NodeTypeEnum.Capability, properties);
		if (!addPropertiesToCapabilityRes.equals(TitanOperationStatus.OK)) {
			log.error("Failed to add properties to capability {} in graph. status is {}", capabiltyId, addPropertiesToCapabilityRes);
		}
		return addPropertiesToCapabilityRes;
	}

	public Either<List<PropertyDefinition>, TitanOperationStatus> validatePropertyUniqueness(Map<String, PropertyDefinition> propertiesOfCapabilityType, List<PropertyDefinition> properties) {
		Either<List<PropertyDefinition>, TitanOperationStatus> result = Either.left(properties);

		for (PropertyDefinition property : properties) {
			String propertyName = property.getName();
			String propertyType = property.getType();
			PropertyDefinition defaultProperty = null;

			if (propertiesOfCapabilityType.containsKey(propertyName)) {
				defaultProperty = propertiesOfCapabilityType.get(propertyName);
				if (propertyType != null && defaultProperty.getType() != null && !defaultProperty.getType().equals(propertyType)) {
					log.error(" Property with name {} and different type already exists.", propertyName);
					result = Either.right(TitanOperationStatus.PROPERTY_NAME_ALREADY_EXISTS);
				} else {
					property.setType(defaultProperty.getType());
					String innerType = defaultProperty.getSchema() == null ? null : defaultProperty.getSchema().getProperty() == null ? null : defaultProperty.getSchema().getProperty().getType();

					if (property.getSchema() != null && property.getSchema().getProperty() != null) {
						property.getSchema().getProperty().setType(innerType);
					}
				}
			}
		}
		return result;
	}

	public StorageOperationStatus validateUpdateCapabilityProperty(PropertyDefinition property) {
		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = applicationDataTypeCache.getAll();
		if (allDataTypes.isRight()) {
			TitanOperationStatus status = allDataTypes.right().value();
			BeEcompErrorManager.getInstance().logInternalFlowError("UpdatePropertyValueOnComponentInstance", "Failed to update property value on instance. Status is " + status, ErrorSeverity.ERROR);
			return DaoStatusConverter.convertTitanStatusToStorageStatus(allDataTypes.right().value());
		}
		return propertyOperation.validateAndUpdateProperty(property, allDataTypes.left().value());
	}

	public StorageOperationStatus validateCapabilityProperties(List<PropertyDefinition> properties) {
		StorageOperationStatus result = StorageOperationStatus.OK;
		for (PropertyDefinition property : properties) {
			result = validateUpdateCapabilityProperty(property);
			if (!result.equals(StorageOperationStatus.OK))
				break;
		}
		return result;
	}

	public Either<List<ImmutablePair<PropertyData, GraphEdge>>, TitanOperationStatus> deletePropertiesOfCapability(String capabilityUid) {
		log.debug("Before deleting properties of capability {} from graph ", capabilityUid);

		Either<List<ImmutablePair<PropertyData, GraphEdge>>, TitanOperationStatus> deletePropertiesStatus = titanGenericDao.deleteChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Capability), capabilityUid, GraphEdgeLabels.PROPERTY,
				NodeTypeEnum.Property, PropertyData.class);
		if (deletePropertiesStatus.isRight()) {
			TitanOperationStatus status = deletePropertiesStatus.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				log.debug("failed to delete properties of capability with id {}. status={}", capabilityUid, status);
			} else {
				log.debug("The Capability with id {} have no Properties. status={}", capabilityUid, status);
			}
			return Either.right(status);
		}
		return Either.left(deletePropertiesStatus.left().value());
	}

	@Override
	public Either<Map<String, PropertyData>, StorageOperationStatus> updatePropertiesOfCapability(String uniqueId, String capabilityType, List<PropertyDefinition> newProperties) {
		return updatePropertiesOfCapability(uniqueId, capabilityType, newProperties, false);
	}

	@Override
	public Either<Map<String, PropertyData>, StorageOperationStatus> updatePropertiesOfCapability(String uniqueId, String capabilityType, List<PropertyDefinition> newProperties, boolean inTransaction) {

		Either<Map<String, PropertyData>, StorageOperationStatus> result = null;
		try {
			Either<CapabilityData, TitanOperationStatus> capabiltyRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Capability), uniqueId, CapabilityData.class);
			if (capabiltyRes.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(capabiltyRes.right().value()));
			}
			CapabilityData capabilityData = capabiltyRes.left().value();
			if (result == null) {
				StorageOperationStatus propertiesValidationRes = validateCapabilityProperties(newProperties);
				if (!propertiesValidationRes.equals(StorageOperationStatus.OK)) {
					result = Either.right(propertiesValidationRes);
				}
			}
			if (result == null) {
				Either<List<ImmutablePair<PropertyData, GraphEdge>>, TitanOperationStatus> deletePropertiesRes = deletePropertiesOfCapability(uniqueId);
				if (deletePropertiesRes.isRight() && !deletePropertiesRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(deletePropertiesRes.right().value()));
				}
			}
			if (result == null) {
				Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesRes = addPropertiesToCapability(capabilityData, capabilityType, newProperties);
				if (addPropertiesRes.isRight()) {
					result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(addPropertiesRes.right().value()));
				} else {
					result = Either.left(addPropertiesRes.left().value());
				}
			}
			if (result.isRight()) {
				log.debug("Failed to update properties of capability {}. Status is {}", uniqueId, result);
			}
			return result;
		} finally {
			if (!inTransaction) {
				if (result == null || result.isRight()) {
					log.debug("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
	}

	public Either<CapabilityData, TitanOperationStatus> getCapabilityRelatedToResourceInstance(String resourceInstanceId, String capabilityUid) {
		TitanOperationStatus error = null;
		CapabilityData capability = null;
		Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> getCapabilitiesRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), resourceInstanceId,
				GraphEdgeLabels.CALCULATED_CAPABILITY, NodeTypeEnum.Capability, CapabilityData.class);
		if (getCapabilitiesRes.isRight()) {
			error = getCapabilitiesRes.right().value();
			log.debug("Failed to retrieve capabilities for resource instance {}. Status is {}", resourceInstanceId, error);
		} else {
			List<ImmutablePair<CapabilityData, GraphEdge>> capabilityPairsList = getCapabilitiesRes.left().value();
			List<CapabilityData> capabilityPair = capabilityPairsList.stream().filter(pair -> pair.getLeft().getUniqueId().equals(capabilityUid)).map(pair -> pair.getLeft()).collect(Collectors.toList());
			if (capabilityPair.isEmpty()) {
				error = TitanOperationStatus.NOT_FOUND;
				log.debug("Failed to retrieve capability {} for resource instance {}. Status is {}", capabilityUid, resourceInstanceId, error);
			} else {
				capability = capabilityPair.get(0);
			}
		}
		if (error == null) {
			return Either.left(capability);
		}
		return Either.right(error);
	}

	public Either<Map<String, PropertyDefinition>, TitanOperationStatus> getAllCapabilityTypePropertiesFromAllDerivedFrom(String firstParentType) {
		Map<String, PropertyDefinition> allProperies = new HashMap<>();
		return getCapabilityTypePropertiesFromDerivedFromRecursively(firstParentType, allProperies);
	}

	private Either<Map<String, PropertyDefinition>, TitanOperationStatus> getCapabilityTypePropertiesFromDerivedFromRecursively(String nextParentType, Map<String, PropertyDefinition> allProperies) {
		TitanOperationStatus error;
		Either<List<ImmutablePair<CapabilityTypeData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), nextParentType, GraphEdgeLabels.DERIVED_FROM,
				NodeTypeEnum.CapabilityType, CapabilityTypeData.class);
		if (childrenNodes.isRight()) {
			if (childrenNodes.right().value() != TitanOperationStatus.NOT_FOUND) {
				error = childrenNodes.right().value();
				log.debug("Couldn't fetch derived from node for capability type {}, error: {}", nextParentType, error);
				return Either.right(error);
			} else {
				log.debug("Derived from node is not found for type {} - this is OK for root capability.");
				return Either.left(allProperies);
			}
		} else {

			Either<Map<String, PropertyDefinition>, TitanOperationStatus> allPropertiesOfCapabilityTypeRes = propertyOperation.findPropertiesOfNode(NodeTypeEnum.CapabilityType, nextParentType);
			if (allPropertiesOfCapabilityTypeRes.isRight() && !allPropertiesOfCapabilityTypeRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				error = allPropertiesOfCapabilityTypeRes.right().value();
				log.error("Failed to retrieve properties for capability type {} from graph. Status is {}", nextParentType, error);
				return Either.right(error);
			} else if (allPropertiesOfCapabilityTypeRes.isLeft()) {
				if (allProperies.isEmpty()) {
					allProperies.putAll(allPropertiesOfCapabilityTypeRes.left().value());
				} else {
					allProperies.putAll(allPropertiesOfCapabilityTypeRes.left().value().entrySet().stream().filter(e -> !allProperies.containsKey(e.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
				}
			}
			return getCapabilityTypePropertiesFromDerivedFromRecursively(childrenNodes.left().value().get(0).getLeft().getUniqueId(), allProperies);
		}
	}
}
