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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.ICapabilityOperation;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("capability-type-operation")
public class CapabilityTypeOperation extends AbstractOperation implements ICapabilityTypeOperation {
	@Autowired
	private PropertyOperation propertyOperation;
	@Autowired
	private ICapabilityOperation capabilityOperation;

	public CapabilityTypeOperation() {
		super();
	}

	private static Logger log = LoggerFactory.getLogger(CapabilityTypeOperation.class.getName());

	/**
	 * FOR TEST ONLY
	 * 
	 * @param titanGenericDao
	 */
	public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
	}

	@Override
	public Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType(CapabilityTypeDefinition capabilityTypeDefinition, boolean inTransaction) {

		Either<CapabilityTypeDefinition, StorageOperationStatus> result = null;

		try {
			Either<CapabilityTypeDefinition, TitanOperationStatus> validationRes = validateUpdateProperties(capabilityTypeDefinition);
			if (validationRes.isRight()) {
				log.error("One or all properties of capability type {} not valid. status is {}", capabilityTypeDefinition, validationRes.right().value().name());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(validationRes.right().value()));
				return result;
			}
			Either<CapabilityTypeData, TitanOperationStatus> eitherStatus = addCapabilityTypeToGraph(capabilityTypeDefinition);

			if (eitherStatus.isRight()) {
				log.error("Failed to add capability {} to Graph. status is {}", capabilityTypeDefinition, eitherStatus.right().value().name());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherStatus.right().value()));
				return result;
			} else {
				CapabilityTypeData capabilityTypeData = eitherStatus.left().value();

				CapabilityTypeDefinition capabilityTypeDefResult = convertCTDataToCTDefinition(capabilityTypeData);
				log.debug("The returned CapabilityTypeDefinition is {}", capabilityTypeDefResult);
				result = Either.left(capabilityTypeDefResult);
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

	private Either<CapabilityTypeDefinition, TitanOperationStatus> validateUpdateProperties(CapabilityTypeDefinition capabilityTypeDefinition) {
		TitanOperationStatus error = null;
		if (capabilityTypeDefinition.getProperties() != null && !capabilityTypeDefinition.getProperties().isEmpty() && capabilityTypeDefinition.getDerivedFrom() != null) {
			Either<Map<String, PropertyDefinition>, TitanOperationStatus> allPropertiesRes = capabilityOperation.getAllCapabilityTypePropertiesFromAllDerivedFrom(capabilityTypeDefinition.getDerivedFrom());
			if (allPropertiesRes.isRight() && !allPropertiesRes.right().value().equals(TitanOperationStatus.NOT_FOUND)) {
				error = allPropertiesRes.right().value();
				log.debug("Couldn't fetch derived from property nodes for capability type {}, error: {}", capabilityTypeDefinition.getType(), error);
			}
			if (error == null && !allPropertiesRes.left().value().isEmpty()) {
				Map<String, PropertyDefinition> derivedFromProperties = allPropertiesRes.left().value();
				capabilityTypeDefinition.getProperties().entrySet().stream().filter(e -> derivedFromProperties.containsKey(e.getKey()) && e.getValue().getType() == null)
						.forEach(e -> e.getValue().setType(derivedFromProperties.get(e.getKey()).getType()));

				Either<List<PropertyDefinition>, TitanOperationStatus> validatePropertiesRes = capabilityOperation.validatePropertyUniqueness(allPropertiesRes.left().value(),
						capabilityTypeDefinition.getProperties().values().stream().collect(Collectors.toList()));
				if (validatePropertiesRes.isRight()) {
					error = validatePropertiesRes.right().value();
				}
			}
		}
		if (error == null) {
			return Either.left(capabilityTypeDefinition);
		}
		return Either.right(error);
	}

	/**
	 * 
	 * convert between graph Node object to Java object
	 * 
	 * @param capabilityTypeData
	 * @return
	 */
	protected CapabilityTypeDefinition convertCTDataToCTDefinition(CapabilityTypeData capabilityTypeData) {
		log.debug("The object returned after create capability is {}", capabilityTypeData);

		CapabilityTypeDefinition capabilityTypeDefResult = new CapabilityTypeDefinition(capabilityTypeData.getCapabilityTypeDataDefinition());

		return capabilityTypeDefResult;
	}

	/**
	 * 
	 * Add capability type to graph.
	 * 
	 * 1. Add capability type node
	 * 
	 * 2. Add edge between the former node to its parent(if exists)
	 * 
	 * 3. Add property node and associate it to the node created at #1. (per property & if exists)
	 * 
	 * @param capabilityTypeDefinition
	 * @return
	 */
	private Either<CapabilityTypeData, TitanOperationStatus> addCapabilityTypeToGraph(CapabilityTypeDefinition capabilityTypeDefinition) {

		log.debug("Got capability type {}", capabilityTypeDefinition);

		String ctUniqueId = UniqueIdBuilder.buildCapabilityTypeUid(capabilityTypeDefinition.getType());
		// capabilityTypeDefinition.setUniqueId(ctUniqueId);

		CapabilityTypeData capabilityTypeData = buildCapabilityTypeData(capabilityTypeDefinition, ctUniqueId);

		log.debug("Before adding capability type to graph. capabilityTypeData = {}", capabilityTypeData);
		Either<CapabilityTypeData, TitanOperationStatus> createCTResult = titanGenericDao.createNode(capabilityTypeData, CapabilityTypeData.class);
		log.debug("After adding capability type to graph. status is = {}", createCTResult);

		if (createCTResult.isRight()) {
			TitanOperationStatus operationStatus = createCTResult.right().value();
			log.error("Failed to capability type {} to graph. status is {}", capabilityTypeDefinition.getType(), operationStatus);
			return Either.right(operationStatus);
		}

		CapabilityTypeData resultCTD = createCTResult.left().value();
		Map<String, PropertyDefinition> propertiesMap = capabilityTypeDefinition.getProperties();
		Collection<PropertyDefinition> properties = propertiesMap != null ? propertiesMap.values() : null;
		Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesToCapablityType = propertyOperation.addPropertiesToElementType(resultCTD.getUniqueId(), NodeTypeEnum.CapabilityType, propertiesMap);
		if (addPropertiesToCapablityType.isRight()) {
			log.error("Failed add properties {} to capability {}", propertiesMap, capabilityTypeDefinition.getType());
			return Either.right(addPropertiesToCapablityType.right().value());
		}

		String derivedFrom = capabilityTypeDefinition.getDerivedFrom();
		if (derivedFrom != null) {
			log.debug("Before creating relation between capability type {} to its parent {}", ctUniqueId, derivedFrom);
			UniqueIdData from = new UniqueIdData(NodeTypeEnum.CapabilityType, ctUniqueId);
			UniqueIdData to = new UniqueIdData(NodeTypeEnum.CapabilityType, derivedFrom);
			Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(from, to, GraphEdgeLabels.DERIVED_FROM, null);
			log.debug("After create relation between capability type {} to its parent {}. status is {}", ctUniqueId, derivedFrom, createRelation);
			if (createRelation.isRight()) {
				return Either.right(createRelation.right().value());
			}
		}

		return Either.left(createCTResult.left().value());

	}

	private CapabilityTypeData buildCapabilityTypeData(CapabilityTypeDefinition capabilityTypeDefinition, String ctUniqueId) {

		CapabilityTypeData capabilityTypeData = new CapabilityTypeData(capabilityTypeDefinition);

		capabilityTypeData.getCapabilityTypeDataDefinition().setUniqueId(ctUniqueId);
		Long creationDate = capabilityTypeData.getCapabilityTypeDataDefinition().getCreationTime();
		if (creationDate == null) {
			creationDate = System.currentTimeMillis();
		}
		capabilityTypeData.getCapabilityTypeDataDefinition().setCreationTime(creationDate);
		capabilityTypeData.getCapabilityTypeDataDefinition().setModificationTime(creationDate);
		return capabilityTypeData;
	}

	@Override
	public Either<CapabilityTypeDefinition, StorageOperationStatus> getCapabilityType(String uniqueId, boolean inTransaction) {

		Either<CapabilityTypeDefinition, StorageOperationStatus> result = null;
		try {

			Either<CapabilityTypeDefinition, TitanOperationStatus> ctResult = this.getCapabilityTypeByUid(uniqueId);

			if (ctResult.isRight()) {
				TitanOperationStatus status = ctResult.right().value();
				if (status != TitanOperationStatus.NOT_FOUND) {
					log.error("Failed to retrieve information on capability type {}. status is {}", uniqueId, status);
				}
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(ctResult.right().value()));
				return result;
			}

			result = Either.left(ctResult.left().value());

			return result;
		} finally {
			if (false == inTransaction) {
				log.debug("Going to execute commit on graph.");
				titanGenericDao.commit();
			}
		}
	}

	/**
	 * Build Capability type object from graph by unique id
	 * 
	 * @param uniqueId
	 * @return
	 */
	public Either<CapabilityTypeDefinition, TitanOperationStatus> getCapabilityTypeByUid(String uniqueId) {

		Either<CapabilityTypeDefinition, TitanOperationStatus> result = null;

		Either<CapabilityTypeData, TitanOperationStatus> capabilityTypesRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), uniqueId, CapabilityTypeData.class);

		if (capabilityTypesRes.isRight()) {
			TitanOperationStatus status = capabilityTypesRes.right().value();
			log.debug("Capability type {} cannot be found in graph. status is {}", uniqueId, status);
			return Either.right(status);
		}

		CapabilityTypeData ctData = capabilityTypesRes.left().value();
		CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition(ctData.getCapabilityTypeDataDefinition());

		TitanOperationStatus propertiesStatus = fillProperties(uniqueId, capabilityTypeDefinition);
		if (propertiesStatus != TitanOperationStatus.OK) {
			log.error("Failed to fetch properties of capability type {}", uniqueId);
			return Either.right(propertiesStatus);
		}

		Either<ImmutablePair<CapabilityTypeData, GraphEdge>, TitanOperationStatus> parentNode = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), uniqueId, GraphEdgeLabels.DERIVED_FROM,
				NodeTypeEnum.CapabilityType, CapabilityTypeData.class);
		log.debug("After retrieving DERIVED_FROM node of {}. status is {}", uniqueId, parentNode);
		if (parentNode.isRight()) {
			TitanOperationStatus titanOperationStatus = parentNode.right().value();
			if (titanOperationStatus != TitanOperationStatus.NOT_FOUND) {
				log.error("Failed to find the parent capability of capability type {}. status is {}", uniqueId, titanOperationStatus);
				result = Either.right(titanOperationStatus);
				return result;
			}
		} else {
			// derived from node was found
			ImmutablePair<CapabilityTypeData, GraphEdge> immutablePair = parentNode.left().value();
			CapabilityTypeData parentCT = immutablePair.getKey();
			capabilityTypeDefinition.setDerivedFrom(parentCT.getCapabilityTypeDataDefinition().getType());
		}
		result = Either.left(capabilityTypeDefinition);

		return result;
	}

	private TitanOperationStatus fillProperties(String uniqueId, CapabilityTypeDefinition capabilityTypeDefinition) {

		Either<Map<String, PropertyDefinition>, TitanOperationStatus> findPropertiesOfNode = propertyOperation.findPropertiesOfNode(NodeTypeEnum.CapabilityType, uniqueId);
		if (findPropertiesOfNode.isRight()) {
			TitanOperationStatus titanOperationStatus = findPropertiesOfNode.right().value();
			log.debug("After looking for properties of vertex {}. status is {}", uniqueId, titanOperationStatus);
			if (TitanOperationStatus.NOT_FOUND.equals(titanOperationStatus)) {
				return TitanOperationStatus.OK;
			} else {
				return titanOperationStatus;
			}
		} else {
			Map<String, PropertyDefinition> properties = findPropertiesOfNode.left().value();
			capabilityTypeDefinition.setProperties(properties);
			return TitanOperationStatus.OK;
		}
	}

	public Either<Boolean, StorageOperationStatus> isCapabilityTypeDerivedFrom(String childCandidateType, String parentCandidateType) {
		Map<String, Object> propertiesToMatch = new HashMap<String, Object>();
		propertiesToMatch.put(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), childCandidateType);
		Either<List<CapabilityTypeData>, TitanOperationStatus> getResponse = titanGenericDao.getByCriteria(NodeTypeEnum.CapabilityType, propertiesToMatch, CapabilityTypeData.class);
		if (getResponse.isRight()) {
			TitanOperationStatus titanOperationStatus = getResponse.right().value();
			log.debug("Couldn't fetch capability type {}, error: {}", childCandidateType, titanOperationStatus);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(titanOperationStatus));
		}
		String childUniqueId = getResponse.left().value().get(0).getUniqueId();
		Set<String> travelledTypes = new HashSet<>();
		do {
			travelledTypes.add(childUniqueId);
			Either<List<ImmutablePair<CapabilityTypeData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityType), childUniqueId, GraphEdgeLabels.DERIVED_FROM,
					NodeTypeEnum.CapabilityType, CapabilityTypeData.class);
			if (childrenNodes.isRight()) {
				if (childrenNodes.right().value() != TitanOperationStatus.NOT_FOUND) {
					TitanOperationStatus titanOperationStatus = getResponse.right().value();
					log.debug("Couldn't fetch derived from node for capability type {}, error: {}", childCandidateType, titanOperationStatus);
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(titanOperationStatus));
				} else {
					log.debug("Derived from node is not found for type {} - this is OK for root capability.");
					return Either.left(false);
				}
			}
			String derivedFromUniqueId = childrenNodes.left().value().get(0).getLeft().getUniqueId();
			if (derivedFromUniqueId.equals(parentCandidateType)) {
				log.debug("Verified that capability type {} derives from capability type {}", childCandidateType, parentCandidateType);
				return Either.left(true);
			}
			childUniqueId = derivedFromUniqueId;
		} while (!travelledTypes.contains(childUniqueId));
		// this stop condition should never be used, if we use it, we have an
		// illegal cycle in graph - "derived from" hierarchy cannot be cycled.
		// It's here just to avoid infinite loop in case we have such cycle.
		log.error("Detected a cycle of \"derived from\" edges starting at capability type node {}", childUniqueId);
		return Either.right(StorageOperationStatus.GENERAL_ERROR);
	}

	/**
	 * FOR TEST ONLY
	 * 
	 * @param propertyOperation
	 */
	public void setPropertyOperation(PropertyOperation propertyOperation) {
		this.propertyOperation = propertyOperation;
	}

	@Override
	public Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType(CapabilityTypeDefinition capabilityTypeDefinition) {

		return addCapabilityType(capabilityTypeDefinition, false);
	}

	@Override
	public Either<CapabilityTypeDefinition, StorageOperationStatus> getCapabilityType(String uniqueId) {
		return getCapabilityType(uniqueId, false);
	}

}
