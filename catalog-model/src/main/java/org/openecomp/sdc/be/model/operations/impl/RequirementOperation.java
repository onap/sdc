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

import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.TitanVertex;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.CapabiltyInstance;
import org.openecomp.sdc.be.model.Point;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.RequirementImplDef;
import org.openecomp.sdc.be.model.operations.api.IRequirementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.CapabilityData;
import org.openecomp.sdc.be.resources.data.CapabilityInstData;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;
import org.openecomp.sdc.be.resources.data.PropertyValueData;
import org.openecomp.sdc.be.resources.data.RequirementData;
import org.openecomp.sdc.be.resources.data.RequirementImplData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("requirement-operation")
public class RequirementOperation implements IRequirementOperation {

	private static final String NA = "NA";

	private static final String EQUAL_SIGN = "=";

	private static final String EMPTY_STRING = "";

	public RequirementOperation() {
		super();
	}

	private static Logger log = LoggerFactory.getLogger(RequirementOperation.class.getName());

	@javax.annotation.Resource
	private CapabilityOperation capabilityOperation;

	@javax.annotation.Resource
	private CapabilityTypeOperation capabilityTypeOperation;

	@javax.annotation.Resource
	private TitanGenericDao titanGenericDao;

	/**
	 * FOR TEST ONLY
	 * 
	 * @param titanGenericDao
	 */
	public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
	}

	protected CapabilityTypeDefinition convertCTDataToCTDefinition(CapabilityTypeData capabilityTypeData) {
		log.debug("The object returned after create capability is {}", capabilityTypeData);

		CapabilityTypeDefinition capabilityTypeDefResult = new CapabilityTypeDefinition(capabilityTypeData.getCapabilityTypeDataDefinition());

		return capabilityTypeDefResult;
	}

	/**
	 * FOR TEST ONLY
	 * 
	 * @param capabilityOperation
	 */
	public void setCapabilityOperation(CapabilityOperation capabilityOperation) {
		this.capabilityOperation = capabilityOperation;
	}

	public void setCapabilityTypeOperation(CapabilityTypeOperation capabilityTypeOperation) {
		this.capabilityTypeOperation = capabilityTypeOperation;
	}

	@Override
	public Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource(String reqName, RequirementDefinition reqDefinition, String resourceId) {

		return addRequirementToResource(reqName, reqDefinition, resourceId, false);
	}

	private Either<GraphRelation, TitanOperationStatus> associateRequirementToRelationshipType(RequirementData reqData, RequirementDefinition reqDefinition) {

		String relationship = reqDefinition.getRelationship();

		if (relationship == null) {
			log.debug("The provided relationship is null.");
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}
		UniqueIdData uniqueIdData = new UniqueIdData(NodeTypeEnum.RelationshipType, relationship);
		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(reqData, uniqueIdData, GraphEdgeLabels.RELATIONSHIP_TYPE, null);

		return createRelation;

	}

	/**
	 * Associate the requirement node to its capability type
	 * 
	 * @param reqData
	 * @param reqDefinition
	 * @return
	 */
	private Either<GraphRelation, TitanOperationStatus> associateRequirementToCapabilityType(RequirementData reqData, RequirementDefinition reqDefinition) {

		String capability = reqDefinition.getCapability();

		UniqueIdData uniqueIdData = new UniqueIdData(NodeTypeEnum.CapabilityType, capability);
		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(reqData, uniqueIdData, GraphEdgeLabels.CAPABILITY_TYPE, null);

		log.debug("After associating requirementData {} to capability {}. status is {}", reqData, capability, createRelation);

		return createRelation;
	}

	private TitanOperationStatus associateRequirementToCapabilityType(TitanVertex reqData, RequirementDefinition reqDefinition) {

		String capability = reqDefinition.getCapability();

		UniqueIdData uniqueIdData = new UniqueIdData(NodeTypeEnum.CapabilityType, capability);
		TitanOperationStatus createRelation = titanGenericDao.createEdge(reqData, uniqueIdData, GraphEdgeLabels.CAPABILITY_TYPE, null);

		log.debug("After associating requirementData {} to capability {}. status is {}", reqData, capability, createRelation);

		return createRelation;
	}

	/**
	 * Associate requirement impl node to capability instance node
	 * 
	 * @param reqImplData
	 * @param capabilityInstData
	 * @param capabilityName
	 * @return
	 */
	private Either<GraphRelation, TitanOperationStatus> associateRequirementImplToCapabilityInst(RequirementImplData reqImplData, CapabilityInstData capabilityInstData, String capabilityName) {

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), capabilityName);

		log.debug("Before associating requirement impl {} to capability instance {}", reqImplData, capabilityInstData);
		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(reqImplData, capabilityInstData, GraphEdgeLabels.CAPABILITY_INST, props);
		log.debug("After associating requirement impl {} to capability instance {}.status is {}", reqImplData, capabilityInstData, createRelation);

		return createRelation;

	}

	/**
	 * Add requirement node to graph
	 * 
	 * @param resourceId
	 * @param reqName
	 * @param reqDefinition
	 * @return
	 */
	private Either<RequirementData, TitanOperationStatus> addRequirementData(String resourceId, String reqName, RequirementDefinition reqDefinition) {

		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setUniqueId(resourceId);

		RequirementData requirementData = buildRequirementData(resourceId, reqName, reqDefinition);

		log.debug("Before adding requirement data to graph {}", requirementData);
		Either<RequirementData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(requirementData, RequirementData.class);

		log.debug("After adding requirement to graph {}", requirementData);
		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to add requirement {} [{}]  to graph. status is {}", reqName,  requirementData, operationStatus);
			return Either.right(operationStatus);
		}

		TitanOperationStatus status = associateResourceDataToRequirementData(resourceId, reqName, resourceData, requirementData);
		if (status != TitanOperationStatus.OK) {
			return Either.right(status);
		}

		return Either.left(createNodeResult.left().value());

	}

	private Either<TitanVertex, TitanOperationStatus> addRequirementData(TitanVertex vertex, String resourceId, String reqName, RequirementDefinition reqDefinition) {

		RequirementData requirementData = buildRequirementData(resourceId, reqName, reqDefinition);

		log.debug("Before adding requirement data to graph {}", requirementData);
		Either<TitanVertex, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(requirementData);

		log.debug("After adding requirement to graph {}", requirementData);
		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to add requirement {} [{}]  to graph. status is {}", reqName,  requirementData, operationStatus);
			return Either.right(operationStatus);
		}

		TitanOperationStatus status = associateResourceDataToRequirementData(resourceId, reqName, vertex, createNodeResult.left().value());
		if (!status.equals(TitanOperationStatus.OK)) {
			return Either.right(status);
		}
		return Either.left(createNodeResult.left().value());
	}

	/**
	 * Asssociate resource node to requirement node with REQUIREMENT label and requirement name as property on the edge.
	 * 
	 * @param resourceId
	 * @param reqName
	 * @param resourceData
	 * @param requirementData
	 * @return
	 */
	private TitanOperationStatus associateResourceDataToRequirementData(String resourceId, String reqName, ResourceMetadataData resourceData, RequirementData requirementData) {

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), reqName);
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(resourceData, requirementData, GraphEdgeLabels.REQUIREMENT, props);
		log.debug("After creatin edge between resource {} to requirement {}", resourceId, requirementData);
		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			log.error("Failed to associate resource {} to requirement {} [ {} ] in graph. status is {}", resourceId, reqName, requirementData, operationStatus);
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus associateResourceDataToRequirementData(String resourceId, String reqName, TitanVertex resourceVertex, TitanVertex requirementVertex) {

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), reqName);
		TitanOperationStatus createRelResult = titanGenericDao.createEdge(resourceVertex, requirementVertex, GraphEdgeLabels.REQUIREMENT, props);
		log.debug("After creatin edge between resource {} to requirement {}", resourceId, requirementVertex);
		if (!createRelResult.equals(TitanOperationStatus.OK)) {
			log.error("Failed to associate resource {} to requirement {} in graph. status is {}", resourceId, reqName, createRelResult);
		}
		return TitanOperationStatus.OK;
	}

	private RequirementData buildRequirementData(String resourceId, String reqName, RequirementDefinition reqDefinition) {

		RequirementData requirementData = new RequirementData();
		requirementData.setNode(reqDefinition.getNode());
		requirementData.setUniqueId(UniqueIdBuilder.buildRequirementUid(resourceId, reqName));
		Long creationTime = System.currentTimeMillis();
		requirementData.setCreationTime(creationTime);
		requirementData.setModificationTime(creationTime);
		requirementData.setRelationshipType(reqDefinition.getRelationship());
		requirementData.setMinOccurrences(reqDefinition.getMinOccurrences());
		requirementData.setMaxOccurrences(reqDefinition.getMaxOccurrences());

		return requirementData;
	}

	/**
	 * build requirement impl node associate it to resource, requirement & implementation resource
	 * 
	 * [RESOURCE] --> [REQUIREMENT IMPL] --> [ RESOURCE IMPL ] | V [REQUIREMENT]
	 * 
	 * @param resourceLabel
	 * @param resourceId
	 * @param reqName
	 * @param requirementUid
	 * @param reqImplDefinition
	 * @return
	 */
	private Either<RequirementImplData, TitanOperationStatus> addRequirementImplData(NodeTypeEnum resourceLabel, String resourceId, String reqName, String requirementUid, RequirementImplDef reqImplDefinition) {

		RequirementImplData requirementImplData = buildRequirementImplData(resourceId, reqName, reqImplDefinition);

		log.debug("Before adding requirement impl data to graph {}", requirementImplData);
		Either<RequirementImplData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(requirementImplData, RequirementImplData.class);
		log.debug("After adding requirement to graph {}. status is {}", requirementImplData, createNodeResult);
		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to add requirement {} [ {} ] to graph. status is {}", reqName, requirementImplData, operationStatus);
			return Either.right(operationStatus);
		}

		Either<GraphRelation, TitanOperationStatus> createRelResult = associateReqImplRoResource(resourceLabel, resourceId, reqName, requirementImplData);
		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			log.error("Failed to associate resource {} to requirement impl {} [ {} ] in graph. status is {}", resourceId, requirementImplData,  requirementImplData, operationStatus);
			return Either.right(operationStatus);
		}

		Either<GraphRelation, TitanOperationStatus> associateToResourceImpl = associateReqImplToImplResource(requirementImplData, reqImplDefinition.getNodeId());
		if (associateToResourceImpl.isRight()) {
			TitanOperationStatus operationStatus = associateToResourceImpl.right().value();
			log.error("Failed to associate requirement impl {} to resource impl {} [ {} ] in graph. status is {}", requirementImplData, reqImplDefinition.getNodeId(), requirementImplData, operationStatus);
			return Either.right(operationStatus);
		}

		Either<GraphRelation, TitanOperationStatus> associateToRequirement = associateReqImplToRequirement(requirementImplData, requirementUid);
		if (associateToRequirement.isRight()) {
			TitanOperationStatus operationStatus = associateToRequirement.right().value();
			log.error("Failed to associate requirement impl {} to requirement {} in graph. status is {}", requirementImplData, reqName, operationStatus);
			return Either.right(operationStatus);
		}

		return Either.left(createNodeResult.left().value());

	}

	private RequirementImplData buildRequirementImplData(String resourceId, String reqName, RequirementImplDef reqImplDefinition) {
		String reqImplUid = UniqueIdBuilder.buildRequirementImplUid(resourceId, reqName);
		RequirementImplData requirementImplData = new RequirementImplData();
		requirementImplData.setName(reqName);
		requirementImplData.setUniqueId(reqImplUid);
		Long creationTime = System.currentTimeMillis();
		requirementImplData.setCreationTime(creationTime);
		requirementImplData.setModificationTime(creationTime);
		Point point = reqImplDefinition.getPoint();
		if (point != null) {
			requirementImplData.setPosX(point.getX());
			requirementImplData.setPosY(point.getY());
		}
		return requirementImplData;
	}

	/**
	 * associate requirement impl node to the source requirement. The source requirement maybe belongs to one of parents.
	 * 
	 * @param requirementImplData
	 * @param requirementUid
	 * @return
	 */
	private Either<GraphRelation, TitanOperationStatus> associateReqImplToRequirement(RequirementImplData requirementImplData, String requirementUid) {

		UniqueIdData to = new UniqueIdData(NodeTypeEnum.Requirement, requirementUid);
		log.debug("Before creating edge between requirement impl {} to requirement {}", requirementImplData, requirementUid);
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(requirementImplData, to, GraphEdgeLabels.IMPLEMENTATION_OF, null);
		log.debug("Before creating edge between requirement impl {} to requirement {}. status is {}", requirementImplData, requirementUid, createRelResult);

		return createRelResult;
	}

	/**
	 * Associate requirement impl node to the node which supply this requirement.
	 * 
	 * @param requirementImplData
	 * @param nodeId
	 * @return
	 */
	private Either<GraphRelation, TitanOperationStatus> associateReqImplToImplResource(RequirementImplData requirementImplData, String nodeId) {

		UniqueIdData nodeImpl = new UniqueIdData(NodeTypeEnum.Resource, nodeId);
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), nodeId);
		log.debug("Before creating edge between requirement impl {} to node impl {}", requirementImplData, nodeId);
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(requirementImplData, nodeImpl, GraphEdgeLabels.NODE_IMPL, props);
		log.debug("After creating edge between requirement {} to node impl {}. status is {}", requirementImplData, nodeId, createRelResult);

		return createRelResult;
	}

	/**
	 * create an edge between the requirement impl node to the implementation resource.
	 * 
	 * @param resourceLabel
	 * @param resourceId
	 * @param reqName
	 * @param requirementImplData
	 * @return
	 */
	private Either<GraphRelation, TitanOperationStatus> associateReqImplRoResource(NodeTypeEnum resourceLabel, String resourceId, String reqName, RequirementImplData requirementImplData) {

		UniqueIdData resource = new UniqueIdData(resourceLabel, resourceId);
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), reqName);
		log.debug("Before creating edge between resource {} to requirement impl {}", resourceId, requirementImplData);
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(resource, requirementImplData, GraphEdgeLabels.REQUIREMENT_IMPL, props);
		log.debug("After creating edge between to requirement impl {} to resource {}. status is {}", requirementImplData, resource, createRelResult);

		return createRelResult;
	}

	private void validateNodeExists(String node) {
		// TODO Auto-generated method stub

	}

	@Override
	public Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource(String reqName, RequirementDefinition reqDefinition, String resourceId, boolean inTransaction) {

		Either<RequirementDefinition, StorageOperationStatus> result = null;
		try {

			log.debug("Going to add requirement {} to resource {}. requirement definition is {}", reqName, resourceId, reqDefinition);

			validateNodeExists(reqDefinition.getNode());

			// 1. add requirement node in graph and associate it to the resource
			log.debug("Going to add requirement node in graph and associate it to the resource");
			Either<RequirementData, TitanOperationStatus> addRequirementData = addRequirementData(resourceId, reqName, reqDefinition);
			if (addRequirementData.isRight()) {
				log.error("Failed to add requirement {} node to graph. status is {}", reqName, addRequirementData);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(addRequirementData.right().value()));
				return result;
			}

			RequirementData requirementData = addRequirementData.left().value();

			log.debug("Going to associate the requirement to the appriopriate capability type");
			Either<GraphRelation, TitanOperationStatus> associateReqToCapabilityType = associateRequirementToCapabilityType(requirementData, reqDefinition);
			if (associateReqToCapabilityType.isRight()) {
				log.error("Failed to associate requirement data node {} to the capability type node {}", requirementData, reqDefinition.getCapability());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(associateReqToCapabilityType.right().value()));
				return result;
			}

			// TODO: esofer associate requirement to the relationship type
			/*
			 * Either<GraphRelation, TitanOperationStatus> associateReqToRelshipType = associateRequirementToRelationshipType( requirementData, reqDefinition);
			 * 
			 * if (associateReqToRelshipType.isRight() && associateReqToRelshipType.right().value() != TitanOperationStatus.NOT_FOUND) { log.error("Failed to associate requirement data node " + requirementData + " to the relationship type node " +
			 * reqDefinition.getRelationship()); result = Either .right(TitanStatusConverter .convertTitanStatusToStorageStatus(associateReqToRelshipType .right().value())); return result; }
			 */

			log.debug("Going to fetch the requirement {} from graph", reqName);
			Either<RequirementDefinition, TitanOperationStatus> requirementDefinitionRes = getRequirement(requirementData.getUniqueId());
			if (requirementDefinitionRes.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(requirementDefinitionRes.right().value()));
				return result;
			}

			result = Either.left(requirementDefinitionRes.left().value());

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
	public StorageOperationStatus addRequirementToResource(TitanVertex metadataVertex, String reqName, RequirementDefinition reqDefinition, String resourceId, boolean inTransaction) {

		StorageOperationStatus result = StorageOperationStatus.OK;
		try {

			log.debug("Going to add requirement {} to resource . requirement definition is ", reqName, resourceId, reqDefinition);

			validateNodeExists(reqDefinition.getNode());

			// 1. add requirement node in graph and associate it to the resource
			log.debug("Going to add requirement node in graph and associate it to the resource");
			Either<TitanVertex, TitanOperationStatus> addRequirementData = addRequirementData(metadataVertex, resourceId, reqName, reqDefinition);
			if (addRequirementData.isRight()) {
				log.error("Failed to add requirement {} node to graph. status is {}", reqName, addRequirementData.right().value());
				result = DaoStatusConverter.convertTitanStatusToStorageStatus(addRequirementData.right().value());
				return result;
			}

			log.debug("Going to associate the requirement to the appriopriate capability type");
			TitanOperationStatus associateReqToCapabilityType = associateRequirementToCapabilityType(addRequirementData.left().value(), reqDefinition);
			if (!associateReqToCapabilityType.equals(TitanOperationStatus.OK)) {
				log.error("Failed to associate requirement data node {} to the capability type node {}", reqDefinition.getCapability(), reqDefinition);
				result = DaoStatusConverter.convertTitanStatusToStorageStatus(associateReqToCapabilityType);
				return result;
			}
			return result;

		} finally {
			if (false == inTransaction) {
				if (result == null || !result.equals(TitanOperationStatus.OK)) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	/**
	 * Fetch requirement from graph
	 * 
	 * @param uniqueId
	 *            - the uniqueid of the requirement in the graph
	 * @return
	 */
	public Either<RequirementDefinition, TitanOperationStatus> getRequirement(String uniqueId) {

		log.debug("Going to fetch the requirement {} from graph.", uniqueId);
		Either<RequirementData, TitanOperationStatus> reqDataResult = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Requirement), uniqueId, RequirementData.class);

		if (reqDataResult.isRight()) {
			log.error("Failed to find requirement node in graph {}. status is {}", uniqueId, reqDataResult);
			return Either.right(reqDataResult.right().value());
		}

		log.debug("Going to fetch the capability type associate to requirement {}", uniqueId);
		Either<ImmutablePair<CapabilityTypeData, GraphEdge>, TitanOperationStatus> capabilityTypeRes = titanGenericDao.getChild(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), uniqueId, GraphEdgeLabels.CAPABILITY_TYPE, NodeTypeEnum.CapabilityType,
				CapabilityTypeData.class);

		if (capabilityTypeRes.isRight()) {
			log.error("Cannot find the capability of a given requirement {}. status is {}", uniqueId, capabilityTypeRes);
			return Either.right(capabilityTypeRes.right().value());
		}

		ImmutablePair<CapabilityTypeData, GraphEdge> capability = capabilityTypeRes.left().value();

		String capabilityType = capability.getKey().getCapabilityTypeDataDefinition().getType();

		// TODO: esofer add relationship as edge
		/*
		 * Either<List<ImmutablePair<RelationshipTypeData, GraphEdge>>, TitanOperationStatus> relationshipRes = titanGenericDao .getChildrenNodes( GraphPropertiesDictionary.UNIQUE_ID.getProperty(), uniqueId, GraphEdgeLabels.RELATIONSHIP_TYPE,
		 * NodeTypeEnum.RelationshipType, RelationshipTypeData.class);
		 * 
		 * if (relationshipRes.isRight() && relationshipRes.right().value() != TitanOperationStatus.NOT_FOUND) { 
		 *  return Either.right(relationshipRes.right().value()); }
		 * 
		 * String relationshipType = null; if (relationshipRes.isLeft()) { List<ImmutablePair<RelationshipTypeData, GraphEdge>> rstPairs = relationshipRes .left().value(); if (rstPairs == null || true == rstPairs.isEmpty()) { log.error(
		 * "Cannot find the capability of a given requirement " + uniqueId); return Either.right(TitanOperationStatus.NOT_FOUND); }
		 * 
		 * ImmutablePair<RelationshipTypeData, GraphEdge> relationship = rstPairs .get(0); relationshipType = relationship.getKey().getType(); }
		 */

		log.debug("Going to fetch the capability type associate to requirement {}", uniqueId);
		Either<ImmutablePair<ResourceMetadataData, GraphEdge>, TitanOperationStatus> parentNode = titanGenericDao.getParentNode(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), uniqueId, GraphEdgeLabels.REQUIREMENT, NodeTypeEnum.Resource,
				ResourceMetadataData.class);
		if (parentNode.isRight()) {
			log.error("Cannot find the parent resource for a given requirement {}. status is {}", uniqueId, parentNode.right().value());
			return Either.right(parentNode.right().value());
		}

		RequirementData requirementData = reqDataResult.left().value();

		RequirementDefinition requirementDefinition = new RequirementDefinition();
		requirementDefinition.setOwnerId(parentNode.left().value().getLeft().getMetadataDataDefinition().getUniqueId());
		requirementDefinition.setNode(requirementData.getNode());
		requirementDefinition.setUniqueId(requirementData.getUniqueId());
		requirementDefinition.setCapability(capabilityType);
		requirementDefinition.setRelationship(requirementData.getRelationshipType());
		requirementDefinition.setMinOccurrences(requirementData.getMinOccurrences());
		requirementDefinition.setMaxOccurrences(requirementData.getMaxOccurrences());

		return Either.left(requirementDefinition);

	}

	@Override
	public Either<RequirementDefinition, StorageOperationStatus> getRequirementOfResource(String reqName, String resourceId) {

		return getRequirementOfResource(reqName, resourceId, false);
	}

	@Override
	public Either<RequirementDefinition, StorageOperationStatus> getRequirementOfResource(String reqName, String resourceId, boolean inTransaction) {

		Either<RequirementDefinition, StorageOperationStatus> result = null;

		try {
			String reqUniqueId = UniqueIdBuilder.buildRequirementUid(resourceId, reqName);
			Either<RequirementDefinition, TitanOperationStatus> requirementRes = getRequirement(reqUniqueId);

			if (requirementRes.isRight()) {
				log.debug("Failed to retrieve requirement {} associated to resource {}", reqName, resourceId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(requirementRes.right().value()));
			} else {
				result = Either.left(requirementRes.left().value());
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
	public Either<RequirementDefinition, StorageOperationStatus> addRequirementImplToResource(String reqName, RequirementImplDef reqDefinition, String resourceId, String parentReqUniqueId) {

		return addRequirementImplToResource(reqName, reqDefinition, resourceId, parentReqUniqueId, false);

	}

	@Override
	public Either<RequirementDefinition, StorageOperationStatus> addRequirementImplToResource(String reqName, RequirementImplDef reqImplDefinition, String resourceId, String parentReqUniqueId, boolean inTransaction) {

		Either<RequirementDefinition, StorageOperationStatus> result = null;

		try {

			// find the requirement defined at the resource itself or under one
			// of its parents
			Either<RequirementDefinition, TitanOperationStatus> findReq = getRequirement(parentReqUniqueId);
			log.debug("After looking for requirement {}. status is {}", parentReqUniqueId, findReq);
			if (findReq.isRight()) {
				TitanOperationStatus status = findReq.right().value();
				log.error("The requirment {} was not found in the graph. status is {}", parentReqUniqueId, findReq.right().value());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			RequirementDefinition reqDefinition = findReq.left().value();
			String reqNode = reqDefinition.getNode();
			String reqCapability = reqDefinition.getCapability();

			String nodeIdImpl = reqImplDefinition.getNodeId();

			checkNodeIdImplementsRequirementNode(nodeIdImpl, reqNode);

			Either<RequirementImplData, TitanOperationStatus> addRequirementImplData = addRequirementImplData(NodeTypeEnum.Resource, resourceId, reqName, parentReqUniqueId, reqImplDefinition);

			if (addRequirementImplData.isRight()) {
				TitanOperationStatus status = addRequirementImplData.right().value();
				log.error("Failed to add requirement data impl node in the graph. status is {}", addRequirementImplData.right().value());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}
			RequirementImplData requirementImplData = addRequirementImplData.left().value();

			log.debug("Add the properties of the capabilities of the target node {} to the requirement impl node {} in graph.", nodeIdImpl, requirementImplData.getUniqueId());
			Map<String, CapabiltyInstance> requirementPropertiesPerCapability = reqImplDefinition.getRequirementProperties();
			TitanOperationStatus addPropsResult = addCapabilityPropertiesToReqImpl(requirementImplData, reqCapability, nodeIdImpl, requirementPropertiesPerCapability);

			if (addPropsResult != TitanOperationStatus.OK) {
				log.error("Failed to add capabilities properties to Requirement impl {}", requirementImplData);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(addPropsResult));
				return result;
			}

			result = Either.left(reqDefinition);

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

		return result;
	}

	private Either<RequirementImplDef, TitanOperationStatus> getRequirementImplOfResource(String reqName, String resourceId) {

		RequirementImplDef requirementImplDef = new RequirementImplDef();

		Either<List<ImmutablePair<RequirementImplData, GraphEdge>>, TitanOperationStatus> reqImplNodesRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.REQUIREMENT_IMPL,
				NodeTypeEnum.RequirementImpl, RequirementImplData.class);
		log.debug("After looking for requirement impl edge of resource {}", resourceId);
		if (reqImplNodesRes.isRight()) {
			TitanOperationStatus status = reqImplNodesRes.right().value();
			return Either.right(status);
		}

		boolean found = false;
		List<ImmutablePair<RequirementImplData, GraphEdge>> reqImplNodes = reqImplNodesRes.left().value();
		for (ImmutablePair<RequirementImplData, GraphEdge> entry : reqImplNodes) {
			GraphEdge graphEdge = entry.getValue();
			String edgeType = (String) graphEdge.getProperties().get(GraphPropertiesDictionary.NAME.getProperty());
			if (reqName.equals(edgeType)) {
				found = true;
				RequirementImplData requirementImplData = entry.getKey();

				requirementImplDef.setUniqueId(requirementImplData.getUniqueId());

				Either<ImmutablePair<ResourceMetadataData, GraphEdge>, TitanOperationStatus> nodeImplRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.RequirementImpl), requirementImplData.getUniqueId(),
						GraphEdgeLabels.NODE_IMPL, NodeTypeEnum.Resource, ResourceMetadataData.class);

				if (nodeImplRes.isRight()) {
					TitanOperationStatus status = nodeImplRes.right().value();
					log.debug("No implementation resource was found under requirement impl {}. status is {}", requirementImplData.getUniqueId(), status);

					return Either.right(status);
				}
				String nodeImpl = nodeImplRes.left().value().getKey().getMetadataDataDefinition().getUniqueId();
				requirementImplDef.setNodeId(nodeImpl);

				String posX = requirementImplData.getPosX();
				String posY = requirementImplData.getPosY();
				if (posX != null && posY != null) {
					Point point = new Point(posX, posY);
					requirementImplDef.setPoint(point);
				}

				Either<List<ImmutablePair<CapabilityInstData, GraphEdge>>, TitanOperationStatus> capaInstDataRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.RequirementImpl), requirementImplData.getUniqueId(),
						GraphEdgeLabels.CAPABILITY_INST, NodeTypeEnum.CapabilityInst, CapabilityInstData.class);
				if (capaInstDataRes.isRight()) {
					TitanOperationStatus status = capaInstDataRes.right().value();
					log.debug("No capability instance was found under requirement impl {}. status is {}", requirementImplData.getUniqueId(), status);

					return Either.right(status);
				}

				Map<String, CapabiltyInstance> requirementProperties = new HashMap<String, CapabiltyInstance>();

				List<ImmutablePair<CapabilityInstData, GraphEdge>> list = capaInstDataRes.left().value();
				for (ImmutablePair<CapabilityInstData, GraphEdge> capabilityInst : list) {
					CapabilityInstData capabilityInstData = capabilityInst.getKey();
					GraphEdge edge = capabilityInst.getValue();
					Map<String, Object> properties = edge.getProperties();
					if (properties == null) {
						log.error("Cannot find the property {} on the edge {}", GraphPropertiesDictionary.NAME.getProperty(), edge);
						return Either.right(TitanOperationStatus.INVALID_ELEMENT);
					}
					String capabilityName = (String) properties.get(GraphPropertiesDictionary.NAME.getProperty());
					if (capabilityName == null) {
						log.error("Cannot find the property {} on the edge {}", GraphPropertiesDictionary.NAME.getProperty(), edge);
						return Either.right(TitanOperationStatus.INVALID_ELEMENT);
					}

					// List<String> keyValuePropertiesList = capabilityInstData
					// .getProperties();
					// Map<String, String> actualValues = new HashMap<String,
					// String>();
					// fillMapFromKeyValueList(keyValuePropertiesList,
					// actualValues);
					CapabiltyInstance capabiltyInstance = new CapabiltyInstance();
					capabiltyInstance.setUniqueId(capabilityInstData.getUniqueId());
					// capabiltyInstance.setProperties(actualValues);
					requirementProperties.put(capabilityName, capabiltyInstance);

					Either<List<ImmutablePair<PropertyValueData, GraphEdge>>, TitanOperationStatus> propertyValueNodesRes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.CapabilityInst),
							capabilityInstData.getUniqueId(), GraphEdgeLabels.PROPERTY_VALUE, NodeTypeEnum.PropertyValue, PropertyValueData.class);

					if (propertyValueNodesRes.isRight()) {
						TitanOperationStatus status = propertyValueNodesRes.right().value();
						if (status != TitanOperationStatus.NOT_FOUND) {
							log.error("Failed to find the property values of capability instance {}. status is {}", capabilityInstData, status);
							return Either.right(status);
						}
					} else {
						List<ImmutablePair<PropertyValueData, GraphEdge>> propertyValueNodes = propertyValueNodesRes.left().value();

						if (propertyValueNodes != null) {

							Map<String, String> actualValues = new HashMap<String, String>();
							TitanOperationStatus fillPropertiesResult = fillPropertiesMapFromNodes(propertyValueNodes, actualValues);

							if (fillPropertiesResult != TitanOperationStatus.OK) {
								log.error("Failed to fetch properties of capability {}", capabilityName);
								return Either.right(fillPropertiesResult);
							}

							if (false == actualValues.isEmpty()) {
								capabiltyInstance.setProperties(actualValues);
							}
						}
					}

				}

				requirementImplDef.setRequirementProperties(requirementProperties);

				break;
			} else {
				continue;
			}
		}

		if (false == found) {
			log.debug("Cannot find requirement impl under resource {}", resourceId);
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

		return Either.left(requirementImplDef);
	}

	private void fillMapFromKeyValueList(List<String> keyValuePropertiesList, Map<String, String> actualValues) {

		if (keyValuePropertiesList != null) {
			for (String keyValue : keyValuePropertiesList) {
				int equalSignLocation = keyValue.indexOf(EQUAL_SIGN);
				if (equalSignLocation > -1) {
					String key = keyValue.substring(0, equalSignLocation);
					String value = EMPTY_STRING;
					if (equalSignLocation + 1 < keyValue.length()) {
						value = keyValue.substring(equalSignLocation + 1);
					}
					actualValues.put(key, value);
				}
			}
		}

	}

	private TitanOperationStatus fillPropertiesMapFromNodes(List<ImmutablePair<PropertyValueData, GraphEdge>> propertyValueNodes, Map<String, String> actualValues) {
		if (propertyValueNodes != null) {
			for (ImmutablePair<PropertyValueData, GraphEdge> propertyValuePair : propertyValueNodes) {
				PropertyValueData propertyValueData = propertyValuePair.getKey();
				GraphEdge propertyValueEdge = propertyValuePair.getValue();
				Map<String, Object> propertyEdgeProps = propertyValueEdge.getProperties();
				if (propertyEdgeProps == null) {
					log.error("Cannot find the property {} on the edge {}", GraphPropertiesDictionary.NAME.getProperty(), propertyValueEdge);
					return TitanOperationStatus.INVALID_ELEMENT;
				}
				String paramName = (String) propertyEdgeProps.get(GraphPropertiesDictionary.NAME.getProperty());
				if (paramName == null) {
					log.error("Cannot find the property {} on the edge {}", GraphPropertiesDictionary.NAME.getProperty(), propertyValueEdge);
					return TitanOperationStatus.INVALID_ELEMENT;
				}
				actualValues.put(paramName, propertyValueData.getValue());
			}
		}
		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus addCapabilityPropertiesToReqImpl(RequirementImplData reqImplData, String reqCapability, String nodeIdImpl, Map<String, CapabiltyInstance> propertiesValuePerCapability) {

		TitanOperationStatus result = null;

		Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> allCapabilities = capabilityOperation.getAllCapabilitiesPairs(nodeIdImpl);
		log.trace("Atter looking for the capabilities of resource {}. result is {}", nodeIdImpl, allCapabilities);
		if (allCapabilities.isRight()) {
			TitanOperationStatus status = allCapabilities.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				log.error("Failed to find capabilities of resource {}. status is {}", nodeIdImpl, status);
				return status;
			}
		} else {

			List<ImmutablePair<CapabilityData, GraphEdge>> capabilitiesValue = allCapabilities.left().value();
			checkImplNodeContainsReqCapability(reqCapability, capabilitiesValue);

			for (ImmutablePair<CapabilityData, GraphEdge> entry : capabilitiesValue) {

				CapabilityData capabilityData = entry.getKey();

				GraphEdge graphEdge = entry.getValue();

				Either<String, TitanOperationStatus> capabilityNameResult = findCapabilityName(capabilityData, graphEdge);

				if (capabilityNameResult.isRight()) {
					TitanOperationStatus status = capabilityNameResult.right().value();
					log.error("Failed to find capability name from the edge associated to capability {}", capabilityData);
					return status;
				}

				String capabilityName = capabilityNameResult.left().value();
				log.debug("Going to set properties of capability {}", capabilityName);
				String cabilityDataUid = capabilityData.getUniqueId();

				Either<CapabilityTypeData, TitanOperationStatus> ctDataResult = capabilityOperation.getCapabilityTypeOfCapability(cabilityDataUid);

				if (ctDataResult.isRight()) {
					log.error("Cannot find capability type of capbility {}. status is {}", cabilityDataUid, ctDataResult);
					TitanOperationStatus status = ctDataResult.right().value();
					return status;
				}

				CapabilityTypeData capabilityTypeData = ctDataResult.left().value();

				Either<Map<String, PropertyDefinition>, TitanOperationStatus> propertiesStatus = findPropertiesOfCapability(capabilityTypeData);
				if (propertiesStatus.isRight()) {
					TitanOperationStatus status = propertiesStatus.right().value();
					log.error("Failed to fetch properties definitions from capability. status is {}", status);
					return status;
				}

				Map<String, PropertyDefinition> properties = propertiesStatus.left().value();

				CapabiltyInstance capabiltyInstance = null;
				if (propertiesValuePerCapability != null) {
					capabiltyInstance = propertiesValuePerCapability.get(capabilityName);
				}

				Either<CapabilityInstData, TitanOperationStatus> createCapabilityInstanceNode = createCapabilityInstanceNode(capabilityName, reqImplData);
				if (createCapabilityInstanceNode.isRight()) {
					TitanOperationStatus status = createCapabilityInstanceNode.right().value();
					log.error("Failed to create capability instance node ({}) in graph. status is {}", capabilityName, status);

					return status;
				}
				CapabilityInstData capabilityInstData = createCapabilityInstanceNode.left().value();

				Either<List<GraphRelation>, TitanOperationStatus> instanceProperties = addPropertiesToCapabilityInstance(properties, capabiltyInstance, capabilityInstData);

				if (instanceProperties.isRight()) {
					TitanOperationStatus status = instanceProperties.right().value();
					log.debug("Failed to add properties to capability instance. status is {}", status);
					return status;
				}

				Either<GraphRelation, TitanOperationStatus> associateCapabilityInstToCapabilityType = associateCapabilityInstToCapabilityType(capabilityInstData, capabilityTypeData);
				if (associateCapabilityInstToCapabilityType.isRight()) {
					TitanOperationStatus status = associateCapabilityInstToCapabilityType.right().value();
					log.error("Failed to associate capability instance {} to capability type node {} in graph. status is {}", capabilityInstData, capabilityTypeData, status);

					return status;
				}

				Either<GraphRelation, TitanOperationStatus> associateCapabilityInst = associateRequirementImplToCapabilityInst(reqImplData, capabilityInstData, capabilityName);
				if (associateCapabilityInst.isRight()) {
					TitanOperationStatus status = associateCapabilityInst.right().value();
					log.error("Failed to associate requirement impl {} to capability instance node {} of capability {}) in graph. status is {}", reqImplData, capabilityInstData, capabilityName, status);

					return status;
				}

			}
			result = TitanOperationStatus.OK;
		}
		return result;
	}

	private Either<Map<String, PropertyDefinition>, TitanOperationStatus> findPropertiesOfCapability(CapabilityTypeData capabilityTypeData) {
		String capabilityTypeUid = capabilityTypeData.getUniqueId();

		Either<CapabilityTypeDefinition, TitanOperationStatus> capabilityTypeResult = capabilityTypeOperation.getCapabilityTypeByUid(capabilityTypeUid);

		if (capabilityTypeResult.isRight()) {
			log.error("Failed to find capabilityType {} in the graph. status is {}", capabilityTypeUid, capabilityTypeResult);
			return Either.right(capabilityTypeResult.right().value());
		}

		CapabilityTypeDefinition capabilityTypeDef = capabilityTypeResult.left().value();
		Map<String, PropertyDefinition> properties = capabilityTypeDef.getProperties();

		return Either.left(properties);
	}

	private Either<String, TitanOperationStatus> findCapabilityName(CapabilityData capabilityData, GraphEdge graphEdge) {
		Map<String, Object> edgeProps = graphEdge.getProperties();
		String capabilityName = (String) edgeProps.get(GraphPropertiesDictionary.NAME.getProperty());

		if (capabilityName == null) {
			log.debug("Cannot find the name of the capability associated to node {}", capabilityData);
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}
		return Either.left(capabilityName);
	}

	private Either<GraphRelation, TitanOperationStatus> associateCapabilityInstToCapabilityType(CapabilityInstData capabilityInstData, CapabilityTypeData capabilityTypeData) {

		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(capabilityInstData, capabilityTypeData, GraphEdgeLabels.INSTANCE_OF, null);

		return createRelation;

	}

	/**
	 * add property value node with default value of override value and associate it to the capability instance node
	 * 
	 * @param properties
	 *            - properties definition. old also default value
	 * @param capabilityInstance
	 *            - hold also properties new value(if exists)
	 * @param capabilityInstData
	 *            - the graph node which we associate the properties value node to.
	 * @return
	 */
	private Either<List<GraphRelation>, TitanOperationStatus> addPropertiesToCapabilityInstance(Map<String, PropertyDefinition> properties, CapabiltyInstance capabilityInstance, CapabilityInstData capabilityInstData) {

		List<GraphRelation> relationsResult = new ArrayList<GraphRelation>();

		if (properties != null) {
			for (Entry<String, PropertyDefinition> entry : properties.entrySet()) {

				String paramName = entry.getKey();

				PropertyDefinition propertyDefinition = entry.getValue();

				String propertyValue = setPropertyValue(capabilityInstance, paramName, propertyDefinition);

				PropertyValueData propertyValueData = buildPropertyValueData(capabilityInstData.getUniqueId(), paramName, propertyValue);

				log.debug("Before creating property value data node {} in graph.", propertyValueData);
				Either<PropertyValueData, TitanOperationStatus> createNode = titanGenericDao.createNode(propertyValueData, PropertyValueData.class);
				log.debug("Before creating property value data node {} in graph. status is {}", propertyValueData, createNode);
				if (createNode.isRight()) {
					TitanOperationStatus status = createNode.right().value();
					log.error("Failed to create property value node in graph {}. status is {}", propertyValueData, status);
					return Either.right(status);
				}

				PropertyValueData propertyValueDataCreated = createNode.left().value();

				Either<GraphRelation, TitanOperationStatus> createRelation = associateCapabilityInstToPropertyValue(capabilityInstData, paramName, propertyValueDataCreated);

				if (createRelation.isRight()) {
					TitanOperationStatus status = createNode.right().value();
					log.error("Failed to create relation between capability instance {} to property value {} in graph. status is {}", capabilityInstData.getUniqueId(), propertyValueDataCreated.getUniqueId(), status);
					return Either.right(status);
				}

				relationsResult.add(createRelation.left().value());

			}
		}

		return Either.left(relationsResult);
	}

	private Either<GraphRelation, TitanOperationStatus> associateCapabilityInstToPropertyValue(CapabilityInstData capabilityInstData, String paramName, PropertyValueData propertyValueDataCreated) {

		Map<String, Object> edgeProps = new HashMap<String, Object>();
		edgeProps.put(GraphPropertiesDictionary.NAME.getProperty(), paramName);
		log.debug("Begin creating relation between capability instance {} to property value {} in graph.", capabilityInstData, propertyValueDataCreated);
		Either<GraphRelation, TitanOperationStatus> createRelation = titanGenericDao.createRelation(capabilityInstData, propertyValueDataCreated, GraphEdgeLabels.PROPERTY_VALUE, edgeProps);
		log.debug("After creating relation between capability instance {} to property value {} in graph. status is {}", capabilityInstData, propertyValueDataCreated, createRelation);

		return createRelation;
	}

	private String setPropertyValue(CapabiltyInstance capabilityInstance, String paramName, PropertyDefinition propertyDefinition) {
		String propertyValue = NA;
		if (propertyDefinition.getDefaultValue() != null) {
			propertyValue = propertyDefinition.getDefaultValue();
		}
		Map<String, String> propertiesValue = null;
		if (capabilityInstance != null) {
			propertiesValue = capabilityInstance.getProperties();
			if (propertiesValue != null) {
				String tmpValue = propertiesValue.get(paramName);
				if (tmpValue != null) {
					propertyValue = tmpValue;
				}
			}
		}
		return propertyValue;
	}

	private String buildPropertykeyValue(String paramName, String paramValue) {
		return paramName + EQUAL_SIGN + paramValue;
	}

	private PropertyValueData buildPropertyValueData(String capabilityInstDataUid, String paramName, String propertyValue) {
		PropertyValueData propertyValueData = new PropertyValueData();
		propertyValueData.setValue(propertyValue);
		String uid = UniqueIdBuilder.buildPropertyValueUniqueId(capabilityInstDataUid, paramName);
		propertyValueData.setUniqueId(uid);
		Long creationDate = System.currentTimeMillis();
		propertyValueData.setCreationTime(creationDate);
		propertyValueData.setModificationTime(creationDate);
		return propertyValueData;
	}

	private Either<CapabilityInstData, TitanOperationStatus> createCapabilityInstanceNode(String capabilityName, RequirementImplData reqImplData) {

		CapabilityInstData capabilityInstData = new CapabilityInstData();
		String uniqueId = UniqueIdBuilder.buildCapabilityInstanceUid(reqImplData.getUniqueId(), capabilityName);

		capabilityInstData.setUniqueId(uniqueId);
		// capabilityInstData.setProperties(instanceProperties);
		Long creationDate = System.currentTimeMillis();
		capabilityInstData.setCreationTime(creationDate);
		capabilityInstData.setModificationTime(creationDate);

		log.debug("Before creating capability instance node in graph {}", capabilityInstData);
		Either<CapabilityInstData, TitanOperationStatus> createNode = titanGenericDao.createNode(capabilityInstData, CapabilityInstData.class);
		log.debug("After creating capability instance node in graph {}. status is {}", capabilityInstData, createNode);

		return createNode;
	}

	private void checkNodeIdImplementsRequirementNode(String nodeIdImpl, String reqNode) {
		// TODO Auto-generated method stub

	}

	private void checkImplNodeContainsReqCapability(String reqCapability, List<ImmutablePair<CapabilityData, GraphEdge>> capabilitiesValue) {
		// TODO Auto-generated method stub

	}

	public Either<Map<String, List<RequirementDefinition>>, StorageOperationStatus> getAllRequirementsOfResourceOnly(String resourceId, boolean inTransaction) {

		Either<Map<String, List<RequirementDefinition>>, StorageOperationStatus> result = null;

		try {

			Map<String, RequirementDefinition> requirements = new HashMap<String, RequirementDefinition>();
			Set<String> caseInsensitiveReqNames = new HashSet<>();
			TitanOperationStatus status = findAllRequirementsNonRecursive(resourceId, requirements, caseInsensitiveReqNames);

			if (status != TitanOperationStatus.OK) {
				log.error("Failed to get all requirements of resource {}. status is  {}", resourceId, status);
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
			} else {
				// TODO handle requirementImpl
				result = Either.left(convertRequirementMap(requirements, null, null));
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
	public Either<Map<String, RequirementDefinition>, TitanOperationStatus> getResourceRequirements(String resourceId) {

		Either<Map<String, RequirementDefinition>, TitanOperationStatus> result = null;

		Map<String, RequirementDefinition> requirements = new HashMap<String, RequirementDefinition>();
		Set<String> caseInsensitiveReqNames = new HashSet<>();

		TitanOperationStatus status = findAllRequirementsRecursively(resourceId, requirements, caseInsensitiveReqNames);
		if (status != TitanOperationStatus.OK) {
			log.error("Failed to get all requirements of resource {}. status is  {}", resourceId, status);
			return Either.right(status);
		} else {
			log.debug("The requirements returned for resource {} are {}", resourceId, requirements);

			if (requirements != null) {
				for (Entry<String, RequirementDefinition> entry : requirements.entrySet()) {
					String reqName = entry.getKey();
					Either<RequirementImplDef, TitanOperationStatus> reqImplRes = this.getRequirementImplOfResource(reqName, resourceId);
					if (reqImplRes.isRight()) {

						TitanOperationStatus reqImplResStatus = reqImplRes.right().value();
						if (reqImplResStatus == TitanOperationStatus.NOT_FOUND) {
							log.debug("Cannot find implementation of requirement {} under resource {}", reqName, resourceId);
						} else {
							log.error("Cannot find implementation of requirement {} under resource {}", reqName, resourceId);
							return Either.right(reqImplResStatus);
						}
					} else {
						RequirementDefinition requirementDefinition = entry.getValue();
						// RequirementImplDef requirementImplDef =
						// reqImplRes.left().value();
						// requirementDefinition.setRequirementImpl(requirementImplDef);
					}
				}
			}
			log.debug("The requirements returned for resource {} after fetching requirement impl are {}", resourceId, requirements);

			result = Either.left(requirements);

			return result;
		}

	}

	@Override
	public Either<Map<String, RequirementDefinition>, StorageOperationStatus> getAllResourceRequirements(String resourceId, boolean inTransaction) {

		Either<Map<String, RequirementDefinition>, StorageOperationStatus> result = null;

		try {

			Either<Map<String, RequirementDefinition>, TitanOperationStatus> internalResult = getResourceRequirements(resourceId);
			if (internalResult.isRight()) {
				TitanOperationStatus status = internalResult.right().value();
				if (status != TitanOperationStatus.NOT_FOUND) {
					log.error("Failed to fetch requirements of resource {} . status is {}", resourceId, status);
				}
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			Map<String, RequirementDefinition> value = internalResult.left().value();

			result = Either.left(value);
			return result;
		} finally {
			if (!inTransaction) {
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

	public Either<Map<String, RequirementDefinition>, StorageOperationStatus> getAllResourceRequirements(String resourceId) {

		return getAllResourceRequirements(resourceId, false);

	}

	public TitanOperationStatus findAllRequirementsRecursively(String resourceId, Map<String, RequirementDefinition> requirements, Set<String> caseInsensitiveReqNames) {

		TitanOperationStatus nonRecursiveResult = findAllRequirementsNonRecursive(resourceId, requirements, caseInsensitiveReqNames);
		if (!nonRecursiveResult.equals(TitanOperationStatus.OK) && !nonRecursiveResult.equals(TitanOperationStatus.NOT_FOUND)) {
			return nonRecursiveResult;
		}

		Either<ImmutablePair<ResourceMetadataData, GraphEdge>, TitanOperationStatus> parentNodes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Resource,
				ResourceMetadataData.class);

		if (parentNodes.isRight()) {
			TitanOperationStatus parentNodesStatus = parentNodes.right().value();
			if (parentNodesStatus == TitanOperationStatus.NOT_FOUND) {
				log.debug("Finish to lookup for parnet requirements");
				return TitanOperationStatus.OK;
			} else {
				log.error("Failed to find parent requirements of resource {} . status is {}", resourceId, parentNodesStatus);
				return parentNodesStatus;
			}
		}
		ImmutablePair<ResourceMetadataData, GraphEdge> parnetNodePair = parentNodes.left().value();
		String parentUniqueId = parnetNodePair.getKey().getMetadataDataDefinition().getUniqueId();
		TitanOperationStatus addParentReqStatus = findAllRequirementsRecursively(parentUniqueId, requirements, caseInsensitiveReqNames);

		if (addParentReqStatus != TitanOperationStatus.OK) {
			log.error("Failed to fetch all requirements of resource {}", parentUniqueId);
			return addParentReqStatus;
		}

		return TitanOperationStatus.OK;
	}

	private TitanOperationStatus findAllRequirementsNonRecursive(String resourceId, Map<String, RequirementDefinition> requirements, Set<String> caseInsensitiveReqNames) {
		Either<List<ImmutablePair<RequirementData, GraphEdge>>, TitanOperationStatus> requirementNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.REQUIREMENT,
				NodeTypeEnum.Requirement, RequirementData.class);

		if (requirementNodes.isRight()) {
			TitanOperationStatus status = requirementNodes.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				return status;
			}
		} else {
			List<ImmutablePair<RequirementData, GraphEdge>> requirementList = requirementNodes.left().value();
			if (requirementList != null) {
				for (ImmutablePair<RequirementData, GraphEdge> requirementPair : requirementList) {
					String reqUniqueId = requirementPair.getKey().getUniqueId();
					Map<String, Object> edgeProps = requirementPair.getValue().getProperties();
					String reqName = null;
					if (edgeProps != null) {
						reqName = (String) edgeProps.get(GraphPropertiesDictionary.NAME.getProperty());
						if (reqName == null) {
							log.error("The requirement name is missing on the edge of requirement {}", reqUniqueId);
							return TitanOperationStatus.INVALID_ELEMENT;
						}
					} else {
						log.error("The requirement name is missing on the edge of requirement {}", reqUniqueId);
						return TitanOperationStatus.INVALID_ELEMENT;
					}
					Either<RequirementDefinition, TitanOperationStatus> requirementDefRes = this.getRequirement(reqUniqueId);
					if (requirementDefRes.isRight()) {
						TitanOperationStatus status = requirementDefRes.right().value();
						log.error("Failed to get requirement properties of requirement {}", reqUniqueId);
						return status;
					}

					RequirementDefinition requirementDefinition = requirementDefRes.left().value();
					requirementDefinition.setName(reqName);
					// US631462
					if (caseInsensitiveReqNames.contains(reqName.toLowerCase())) {
						log.debug("The requirement {} was already defined in derived resource (case insensitive). Ignore {} from resource {}", reqName, reqName, resourceId);
					} else {
						requirements.put(reqName, requirementDefinition);
						caseInsensitiveReqNames.add(reqName.toLowerCase());
					}

				}
			}
		}
		return TitanOperationStatus.OK;
	}

	public StorageOperationStatus deleteRequirementFromGraph(String requirementId) {
		log.debug("Before deleting requirement from graph {}", requirementId);
		Either<RequirementData, TitanOperationStatus> deleteNodeStatus = titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Requirement), requirementId, RequirementData.class);
		if (deleteNodeStatus.isRight()) {
			log.error("failed to delete requirement with id {}. status={}", requirementId, deleteNodeStatus.right().value());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(deleteNodeStatus.right().value());
		}
		return StorageOperationStatus.OK;
	}

	public Either<Map<String, RequirementDefinition>, StorageOperationStatus> deleteAllRequirements(String resourceId) {

		return getAllResourceRequirements(resourceId, false);

	}

	public Either<Map<String, RequirementDefinition>, StorageOperationStatus> deleteAllRequirements(String resourceId, boolean inTransaction) {

		Either<Map<String, RequirementDefinition>, StorageOperationStatus> result = null;

		try {
			Either<Map<String, RequirementDefinition>, TitanOperationStatus> deleteAllRes = deleteAllRequirementsOfResource(resourceId);
			if (deleteAllRes.isRight()) {
				TitanOperationStatus status = deleteAllRes.right().value();
				if (status != TitanOperationStatus.NOT_FOUND) {
					log.error("Failed to delete requirements of resource {}. status is {}", resourceId, status);
				}
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
				return result;
			}

			Map<String, RequirementDefinition> value = deleteAllRes.left().value();
			result = Either.left(value);

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

	public Either<Map<String, RequirementDefinition>, TitanOperationStatus> deleteAllRequirementsOfResource(String resourceId) {

		Map<String, RequirementDefinition> requirements = new HashMap<String, RequirementDefinition>();
		Set<String> caseInsensitiveReqNames = new HashSet<>();
		TitanOperationStatus requirementsRes = findAllRequirementsNonRecursive(resourceId, requirements, caseInsensitiveReqNames);
		if (requirementsRes != TitanOperationStatus.OK) {
			return Either.right(requirementsRes);
		}

		if (requirements.isEmpty()) {
			return Either.right(TitanOperationStatus.NOT_FOUND);
		}

		for (Entry<String, RequirementDefinition> entry : requirements.entrySet()) {
			RequirementDefinition requirementDefinition = entry.getValue();

			String requirementUid = requirementDefinition.getUniqueId();

			Either<RequirementData, TitanOperationStatus> deleteNodeRes = titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Requirement), requirementUid, RequirementData.class);
			if (deleteNodeRes.isRight()) {
				TitanOperationStatus status = deleteNodeRes.right().value();
				log.error("Failed to delete requirement {} of resource ", requirementUid, resourceId);
				return Either.right(status);
			}
		}

		return Either.left(requirements);

	}

	public Map<String, List<RequirementDefinition>> convertRequirementMap(Map<String, RequirementDefinition> requirementMap, String ownerId, String ownerName) {

		Map<String, List<RequirementDefinition>> typeToRequirementMap = new HashMap<String, List<RequirementDefinition>>();
		requirementMap.forEach((reqName, requirement) -> {
			// requirement.setOwnerId(ownerId);
			// requirement.setOwnerName(ownerName);
			if (typeToRequirementMap.containsKey(requirement.getCapability())) {
				typeToRequirementMap.get(requirement.getCapability()).add(requirement);
			} else {
				List<RequirementDefinition> list = new ArrayList<RequirementDefinition>();
				list.add(requirement);
				typeToRequirementMap.put(requirement.getCapability(), list);
			}
		});
		return typeToRequirementMap;
	}

}
