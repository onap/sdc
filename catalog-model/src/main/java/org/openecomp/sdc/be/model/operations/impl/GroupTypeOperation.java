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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.GroupTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;
import org.openecomp.sdc.be.resources.data.GroupTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("group-type-operation")
public class GroupTypeOperation extends AbstractOperation implements IGroupTypeOperation {

	String CREATE_FLOW_CONTEXT = "CreateGroupType";
	String GET_FLOW_CONTEXT = "GetGroupType";

	private PropertyOperation propertyOperation;
	
	private TitanGenericDao titanGenericDao;

	public GroupTypeOperation(@Qualifier("titan-generic-dao") TitanGenericDao titanGenericDao, @Qualifier("property-operation")PropertyOperation propertyOperation) {
		super();
		this.propertyOperation = propertyOperation;
		this.titanGenericDao = titanGenericDao;
	}

	private static Logger log = LoggerFactory.getLogger(GroupTypeOperation.class.getName());

	/**
	 * FOR TEST ONLY
	 * 
	 * @param titanGenericDao
	 */
	public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
	}

	@Override
	public Either<GroupTypeDefinition, StorageOperationStatus> addGroupType(GroupTypeDefinition groupTypeDefinition) {

		return addGroupType(groupTypeDefinition, false);
	}

	@Override
	public Either<GroupTypeDefinition, StorageOperationStatus> addGroupType(GroupTypeDefinition groupTypeDefinition, boolean inTransaction) {

		Either<GroupTypeDefinition, StorageOperationStatus> result = null;

		try {

			Either<GroupTypeData, TitanOperationStatus> eitherStatus = addGroupTypeToGraph(groupTypeDefinition);

			if (eitherStatus.isRight()) {
				BeEcompErrorManager.getInstance().logBeFailedCreateNodeError(CREATE_FLOW_CONTEXT, groupTypeDefinition.getType(), eitherStatus.right().value().name());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(eitherStatus.right().value()));

			} else {
				GroupTypeData groupTypeData = eitherStatus.left().value();

				String uniqueId = groupTypeData.getUniqueId();
				Either<GroupTypeDefinition, StorageOperationStatus> groupTypeRes = this.getGroupType(uniqueId, true);

				if (groupTypeRes.isRight()) {
					BeEcompErrorManager.getInstance().logBeFailedRetrieveNodeError(GET_FLOW_CONTEXT, groupTypeDefinition.getType(), eitherStatus.right().value().name());
				}

				result = groupTypeRes;

			}

			return result;

		} finally {
			handleTransactionCommitRollback(inTransaction, result);
		}

	}

	public Either<GroupTypeDefinition, TitanOperationStatus> getGroupTypeByUid(String uniqueId) {

		Either<GroupTypeDefinition, TitanOperationStatus> result = null;

		Either<GroupTypeData, TitanOperationStatus> groupTypesRes = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.GroupType), uniqueId, GroupTypeData.class);

		if (groupTypesRes.isRight()) {
			TitanOperationStatus status = groupTypesRes.right().value();
			log.debug("Group type {} cannot be found in graph. status is {}", uniqueId, status);
			return Either.right(status);
		}

		GroupTypeData gtData = groupTypesRes.left().value();
		GroupTypeDefinition groupTypeDefinition = new GroupTypeDefinition(gtData.getGroupTypeDataDefinition());

		TitanOperationStatus propertiesStatus = propertyOperation.fillProperties(uniqueId, properList -> groupTypeDefinition.setProperties(properList));

		if (propertiesStatus != TitanOperationStatus.OK) {
			log.error("Failed to fetch properties of capability type {}", uniqueId);
			return Either.right(propertiesStatus);
		}

		result = Either.left(groupTypeDefinition);

		return result;
	}

	@Override
	public Either<GroupTypeDefinition, StorageOperationStatus> getGroupType(String uniqueId) {

		return getGroupType(uniqueId, false);

	}

	@Override
	public Either<GroupTypeDefinition, StorageOperationStatus> getGroupType(String uniqueId, boolean inTransaction) {
		return getElementType(this::getGroupTypeByUid, uniqueId, inTransaction);

	}

	@Override
	public Either<GroupTypeDefinition, StorageOperationStatus> getLatestGroupTypeByType(String type) {
		return getLatestGroupTypeByType(type, false);
	}

	@Override
	public Either<GroupTypeDefinition, StorageOperationStatus> getLatestGroupTypeByType(String type, boolean inTransaction) {
		Map<String, Object> mapCriteria = new HashMap<>();
		mapCriteria.put(GraphPropertiesDictionary.TYPE.getProperty(), type);
		mapCriteria.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);

		return getGroupTypeByCriteria(type, mapCriteria, inTransaction);

	}

	public Either<GroupTypeDefinition, StorageOperationStatus> getGroupTypeByCriteria(String type, Map<String, Object> properties, boolean inTransaction) {
		Either<GroupTypeDefinition, StorageOperationStatus> result = null;
		try {
			if (type == null || type.isEmpty()) {
				log.error("type is empty");
				result = Either.right(StorageOperationStatus.INVALID_ID);
				return result;
			}

			Either<List<GroupTypeData>, TitanOperationStatus> groupTypeEither = titanGenericDao.getByCriteria(NodeTypeEnum.GroupType, properties, GroupTypeData.class);
			if (groupTypeEither.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(groupTypeEither.right().value()));
			} else {
				GroupTypeDataDefinition dataDefinition = groupTypeEither.left().value().stream().map(e -> e.getGroupTypeDataDefinition()).findFirst().get();
				result = getGroupType(dataDefinition.getUniqueId(), inTransaction);
			}

			return result;

		} finally {
			handleTransactionCommitRollback(inTransaction, result);
		}
	}

	@Override
	public Either<GroupTypeDefinition, StorageOperationStatus> getGroupTypeByTypeAndVersion(String type, String version) {
		return getGroupTypeByTypeAndVersion(type, version, false);
	}

	@Override
	public Either<GroupTypeDefinition, StorageOperationStatus> getGroupTypeByTypeAndVersion(String type, String version, boolean inTransaction) {
		Map<String, Object> mapCriteria = new HashMap<>();
		mapCriteria.put(GraphPropertiesDictionary.TYPE.getProperty(), type);
		mapCriteria.put(GraphPropertiesDictionary.VERSION.getProperty(), version);

		return getGroupTypeByCriteria(type, mapCriteria, inTransaction);
	}

	/**
	 * 
	 * Add group type to graph.
	 * 
	 * 1. Add group type node
	 * 
	 * 2. Add edge between the former node to its parent(if exists)
	 * 
	 * 3. Add property node and associate it to the node created at #1. (per property & if exists)
	 * 
	 * @param groupTypeDefinition
	 * @return
	 */
	private Either<GroupTypeData, TitanOperationStatus> addGroupTypeToGraph(GroupTypeDefinition groupTypeDefinition) {

		log.debug("Got group type {}", groupTypeDefinition);

		String ctUniqueId = UniqueIdBuilder.buildGroupTypeUid(groupTypeDefinition.getType(), groupTypeDefinition.getVersion());
		// capabilityTypeDefinition.setUniqueId(ctUniqueId);

		GroupTypeData groupTypeData = buildGroupTypeData(groupTypeDefinition, ctUniqueId);

		log.debug("Before adding group type to graph. groupTypeData = {}", groupTypeData);

		Either<GroupTypeData, TitanOperationStatus> createGTResult = titanGenericDao.createNode(groupTypeData, GroupTypeData.class);
		log.debug("After adding group type to graph. status is = {}", createGTResult);

		if (createGTResult.isRight()) {
			TitanOperationStatus operationStatus = createGTResult.right().value();
			log.error("Failed to add group type {} to graph. status is {}", groupTypeDefinition.getType(), operationStatus);
			return Either.right(operationStatus);
		}

		GroupTypeData resultCTD = createGTResult.left().value();
		List<PropertyDefinition> properties = groupTypeDefinition.getProperties();
		Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesToCapablityType = propertyOperation.addPropertiesToElementType(resultCTD.getUniqueId(), NodeTypeEnum.GroupType, properties);
		if (addPropertiesToCapablityType.isRight()) {
			log.error("Failed add properties {} to capability {}", properties, groupTypeDefinition.getType());
			return Either.right(addPropertiesToCapablityType.right().value());
		}

		String derivedFrom = groupTypeDefinition.getDerivedFrom();
		if (derivedFrom != null) {

			// TODO: Need to find the parent. need to take the latest one since
			// we may have many versions of the same type
			/*
			 * log.debug("Before creating relation between group type {} to its parent {}", ctUniqueId, derivedFrom); UniqueIdData from = new UniqueIdData(NodeTypeEnum.CapabilityType, ctUniqueId); UniqueIdData to = new
			 * UniqueIdData(NodeTypeEnum.CapabilityType, derivedFrom); Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao .createRelation(from, to, GraphEdgeLabels.DERIVED_FROM, null);
			 * log.debug("After create relation between capability type {} to its parent {}. status is {}", ctUniqueId, derivedFrom, createRelation); if (createRelation.isRight()) { return Either.right(createRelation.right().value()); }
			 * 
			 */
		}

		return Either.left(createGTResult.left().value());

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

	private GroupTypeData buildGroupTypeData(GroupTypeDefinition groupTypeDefinition, String ctUniqueId) {

		GroupTypeData groupTypeData = new GroupTypeData(groupTypeDefinition);

		groupTypeData.getGroupTypeDataDefinition().setUniqueId(ctUniqueId);
		Long creationDate = groupTypeData.getGroupTypeDataDefinition().getCreationTime();
		if (creationDate == null) {
			creationDate = System.currentTimeMillis();
		}
		groupTypeData.getGroupTypeDataDefinition().setCreationTime(creationDate);
		groupTypeData.getGroupTypeDataDefinition().setModificationTime(creationDate);

		return groupTypeData;
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
	public Either<GroupTypeData, TitanOperationStatus> getLatestGroupTypeByNameFromGraph(String name) {

		return null;
	}

}
