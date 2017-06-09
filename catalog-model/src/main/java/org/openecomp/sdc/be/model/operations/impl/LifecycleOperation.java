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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.graph.datatype.RelationEndPoint;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.ILifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.ServiceMetadataData;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

@org.springframework.stereotype.Component("lifecycle-operation")
public class LifecycleOperation implements ILifecycleOperation {

	public static final String VERSION_DELIMETER = ".";
	public static final String VERSION_DELIMETER_REGEXP = "\\.";

	public LifecycleOperation() {
		super();
	}

	private static Logger log = LoggerFactory.getLogger(LifecycleOperation.class.getName());

	@javax.annotation.Resource
	private ResourceOperation resourceOperation;

	@javax.annotation.Resource
	private ServiceOperation serviceOperation;

	@javax.annotation.Resource
	private ProductOperation productOperation;

	@javax.annotation.Resource
	private TitanGenericDao titanGenericDao;

	public ResourceOperation getResourceOperation() {
		return resourceOperation;
	}

	public void setResourceOperation(ResourceOperation resourceOperation) {
		this.resourceOperation = resourceOperation;
	}

	public ServiceOperation getServiceOperation() {
		return serviceOperation;
	}

	public ComponentOperation getComponentOperation(NodeTypeEnum componentType) {
		if (NodeTypeEnum.Service.equals(componentType)) {
			return serviceOperation;
		} else if (NodeTypeEnum.Resource.equals(componentType)) {
			return resourceOperation;
		} else if (NodeTypeEnum.Product.equals(componentType)) {
			return productOperation;
		}
		return null;
	}

	public void setServiceOperation(ServiceOperation serviceOperation) {
		this.serviceOperation = serviceOperation;
	}

	public TitanGenericDao getTitanGenericDao() {
		return titanGenericDao;
	}

	public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
	}

	@Override
	public Either<User, StorageOperationStatus> getComponentOwner(String resourceId, NodeTypeEnum nodeType, boolean inTransaction) {

		Either<User, StorageOperationStatus> result = Either.right(StorageOperationStatus.GENERAL_ERROR);
		try {

			Either<ImmutablePair<UserData, GraphEdge>, TitanOperationStatus> parentNode = titanGenericDao.getParentNode(UniqueIdBuilder.getKeyByNodeType(nodeType), resourceId, GraphEdgeLabels.STATE, NodeTypeEnum.User, UserData.class);

			if (parentNode.isRight()) {
				return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(parentNode.right().value()));
			}

			ImmutablePair<UserData, GraphEdge> value = parentNode.left().value();

			User owner = new User(value.left);
			result = Either.left(owner);

		} finally {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}
		}
		return result;
	}

	@Override
	public Either<? extends Component, StorageOperationStatus> checkoutComponent(NodeTypeEnum nodeType, Component component, User modifier, User currentOwner, boolean inTransaction) {
		Either<? extends Component, StorageOperationStatus> result = null;

		try {
			// update old component
			if (!component.getLifecycleState().equals(LifecycleStateEnum.CERTIFIED)) {
				component.setHighestVersion(false);
				ComponentOperation componentOperation = getComponentOperation(nodeType);
				Either<? extends Component, StorageOperationStatus> updateComponent = componentOperation.updateComponent(component, inTransaction, titanGenericDao, component.getClass(), nodeType);
				if (updateComponent.isRight()) {
					StorageOperationStatus error = updateComponent.right().value();
					log.debug("Couldn't set lifecycle for component {} to state {}, error: {}", component.getUniqueId(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, error);
					return Either.right(error);
				}

				StorageOperationStatus changeStateToLastState = changeStateRelation(nodeType, component.getUniqueId(), currentOwner, GraphEdgeLabels.STATE, GraphEdgeLabels.LAST_STATE);
				if (!changeStateToLastState.equals(StorageOperationStatus.OK)) {
					result = Either.right(changeStateToLastState);
					return result;
				}
			}

			// clone the component
			result = cloneComponentForCheckout(component, nodeType, modifier);
			if (result.isRight()) {
				log.debug("Couldn't set lifecycle for component {} to state {}, error: {}", component.getUniqueId(), LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, result.right().value());
				return result;
			}

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

	private Either<? extends Component, StorageOperationStatus> cloneComponentForCertified(Component component, User modifier, Integer majorVersion) {

		// set new version
		String certifiedVersion = (majorVersion + 1) + VERSION_DELIMETER + "0";
		component.setVersion(certifiedVersion);
		component.setLifecycleState(LifecycleStateEnum.CERTIFIED);
		component.setLastUpdateDate(null);
		component.setLastUpdaterUserId(modifier.getUserId());
		component.setHighestVersion(true);

		ComponentOperation componentOperation = getComponentOperation(component.getComponentType().getNodeType());
		Either<? extends Component, StorageOperationStatus> cloneComponentResult = componentOperation.cloneComponent(component, certifiedVersion, LifecycleStateEnum.CERTIFIED, true);

		return cloneComponentResult;
	}

	@Override
	public Either<? extends Component, StorageOperationStatus> undoCheckout(NodeTypeEnum nodeType, Component component, User modifier, User currentOwner, boolean inTransaction) {
		Either<? extends Component, StorageOperationStatus> result = null;
		ComponentOperation componentOperation = getComponentOperation(nodeType);

		// this is in case prevVersion is 0.0 - returning OOTB component
		Component prevComponent = componentOperation.getDefaultComponent();
		try {
			// find previous version
			String[] versionParts = component.getVersion().split(VERSION_DELIMETER_REGEXP);
			Integer minorVersion = Integer.parseInt(versionParts[1]) - 1;
			String previousVersion = versionParts[0] + VERSION_DELIMETER + minorVersion;

			if (!previousVersion.equals("0.0")) {
				Either<? extends Component, StorageOperationStatus> updateOldResourceResult = updateOldComponentBeforeUndoCheckout(componentOperation, prevComponent, component, previousVersion, nodeType, true);
				if (updateOldResourceResult.isRight()) {
					result = updateOldResourceResult;
					return result;
				}
				prevComponent = updateOldResourceResult.left().value();
			}

			// delete the component
			Either<? extends Component, StorageOperationStatus> deleteResourceResult = componentOperation.deleteComponent(component.getUniqueId(), true);
			if (deleteResourceResult.isRight()) {
				result = deleteResourceResult;
				return result;
			}

			// return the deleted resource
			result = Either.left(prevComponent);

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
	public Either<? extends Component, StorageOperationStatus> checkinComponent(NodeTypeEnum nodeType, Component component, User modifier, User owner, boolean inTransaction) {
		Either<? extends Component, StorageOperationStatus> result = null;
		try {
			StorageOperationStatus updateCheckinInGraph = updateCheckinInGraph(nodeType, component.getUniqueId(), component.getLifecycleState(), modifier, owner);
			if (!updateCheckinInGraph.equals(StorageOperationStatus.OK)) {
				log.error("failed to update state of resource {}. status={}", component.getUniqueId(), updateCheckinInGraph);
				return Either.right(updateCheckinInGraph);
			}
			LifecycleStateEnum state = LifecycleStateEnum.NOT_CERTIFIED_CHECKIN;
			ComponentParametersView componentParametersView = buildFilterForFetchComponentAfterChangeState();
			result = updateComponentMD(component, modifier, state, nodeType, componentParametersView);
			if (result.isRight()) {
				log.debug("Couldn't set lifecycle for component {} to state {}, error: {}", component.getUniqueId(), state, result.right().value());
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

	private ComponentParametersView buildFilterForFetchComponentAfterChangeState() {
		ComponentParametersView componentParametersView = new ComponentParametersView();
		componentParametersView.disableAll();
		componentParametersView.setIgnoreUsers(false);
		// Used when we running multiple change states and want to use the
		// result from another change
		// state(LifecycleOperationTest.certificationStatusChange)
		componentParametersView.setIgnoreCategories(false);
		return componentParametersView;
	}

	private StorageOperationStatus updateCheckinInGraph(NodeTypeEnum componentType, String componentId, LifecycleStateEnum state, User modifier, User owner) {

		// check if we cancel rfc
		if (state.equals(LifecycleStateEnum.READY_FOR_CERTIFICATION)) {

			// remove last checkin
			Map<String, Object> props = new HashMap<String, Object>();
			props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
			UniqueIdData resourceData = new UniqueIdData(componentType, componentId);
			Either<GraphRelation, TitanOperationStatus> deleteResult = titanGenericDao.deleteIncomingRelationByCriteria(resourceData, GraphEdgeLabels.LAST_STATE, props);
			if (deleteResult.isRight()) {
				log.debug("failed to update last state relation");
				return StorageOperationStatus.INCONSISTENCY;
			}
		}

		// remove CHECKOUT relation
		StorageOperationStatus removeUserToResourceRelation = removeUserToResourceRelation(componentType, owner.getUserId(), componentId, GraphEdgeLabels.STATE);
		if (!removeUserToResourceRelation.equals(StorageOperationStatus.OK)) {
			return removeUserToResourceRelation;
		}

		// create CHECKIN relation
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		StorageOperationStatus createUserToResourceRelation = createUserToResourceRelation(componentType, modifier.getUserId(), componentId, GraphEdgeLabels.STATE, props);
		if (!createUserToResourceRelation.equals(StorageOperationStatus.OK)) {
			return createUserToResourceRelation;
		}

		return StorageOperationStatus.OK;
	}

	@Override
	public Either<? extends Component, StorageOperationStatus> requestCertificationComponent(NodeTypeEnum nodeType, Component component, User modifier, User owner, boolean inTransaction) {
		Either<? extends Component, StorageOperationStatus> result = null;
		try {
			StorageOperationStatus updateRfcOnGraph = updateRfcOnGraph(nodeType, component.getUniqueId(), component.getLifecycleState(), modifier, owner);
			if (!updateRfcOnGraph.equals(StorageOperationStatus.OK)) {
				log.error("failed to update state of resource {}. status={}", component.getUniqueId(), updateRfcOnGraph);
				return Either.right(updateRfcOnGraph);
			}

			LifecycleStateEnum state = LifecycleStateEnum.READY_FOR_CERTIFICATION;

			ComponentParametersView componentParametersView = buildFilterForFetchComponentAfterChangeState();

			result = updateComponentMD(component, modifier, state, nodeType, componentParametersView);
			if (result.isRight()) {
				log.debug("Couldn't set lifecycle for component {} to state {}, error: {}", component.getUniqueId(), state, result.right().value());
				return result;
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

	private StorageOperationStatus updateRfcOnGraph(NodeTypeEnum componentType, String componentId, LifecycleStateEnum state, User modifier, User owner) {

		if (state.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT)) {
			// if this is atomic checkin + RFC: create checkin relation

			// remove CHECKOUT relation
			StorageOperationStatus relationStatus = removeUserToResourceRelation(componentType, owner.getUserId(), componentId, GraphEdgeLabels.STATE);
			if (!relationStatus.equals(StorageOperationStatus.OK)) {
				return relationStatus;
			}

			// create CHECKIN relation
			Map<String, Object> props = new HashMap<String, Object>();
			props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
			relationStatus = createUserToResourceRelation(componentType, modifier.getUserId(), componentId, GraphEdgeLabels.LAST_STATE, props);
			if (!relationStatus.equals(StorageOperationStatus.OK)) {
				return relationStatus;
			}
		} else {
			StorageOperationStatus changeStatus = changeRelationLabel(componentType, componentId, owner, GraphEdgeLabels.STATE, GraphEdgeLabels.LAST_STATE);
			if (!changeStatus.equals(StorageOperationStatus.OK)) {
				return changeStatus;
			}
		}

		// create RFC relation
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		StorageOperationStatus changeRelationLabel = createUserToResourceRelation(componentType, modifier.getUserId(), componentId, GraphEdgeLabels.STATE, props);
		if (!changeRelationLabel.equals(StorageOperationStatus.OK)) {
			return changeRelationLabel;
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus changeRelationLabel(NodeTypeEnum componentType, String componentId, User owner, GraphEdgeLabels prevLabel, GraphEdgeLabels toLabel) {
		UniqueIdData resourceV = new UniqueIdData(componentType, componentId);
		UserData userV = new UserData();
		userV.setUserId(owner.getUserId());
		Either<GraphRelation, TitanOperationStatus> replaceRelationLabelResult = titanGenericDao.replaceRelationLabel(userV, resourceV, prevLabel, toLabel);
		if (replaceRelationLabelResult.isRight()) {
			log.error("failed to replace label from last state to state");
			return DaoStatusConverter.convertTitanStatusToStorageStatus(replaceRelationLabelResult.right().value());
		}
		return StorageOperationStatus.OK;
	}

	@Override
	public Either<? extends Component, StorageOperationStatus> startComponentCertification(NodeTypeEnum nodeType, Component component, User modifier, User owner, boolean inTransaction) {
		Either<? extends Component, StorageOperationStatus> result = null;
		try {
			StorageOperationStatus updateOnGraph = updateStartCertificationOnGraph(nodeType, component.getUniqueId(), modifier, owner);
			if (!updateOnGraph.equals(StorageOperationStatus.OK)) {
				log.error("failed to update state of resource {}. status={}", component.getUniqueId(), updateOnGraph);
				return Either.right(updateOnGraph);
			}

			LifecycleStateEnum state = LifecycleStateEnum.CERTIFICATION_IN_PROGRESS;
			ComponentParametersView componentParametersView = buildFilterForFetchComponentAfterChangeState();

			result = updateComponentMD(component, modifier, state, nodeType, componentParametersView);
			if (result.isRight()) {
				log.debug("Couldn't set lifecycle for component {} to state {}, error: {}", component.getUniqueId(), state, result.right().value());
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

	private StorageOperationStatus updateStartCertificationOnGraph(NodeTypeEnum componentType, String componentId, User modifier, User owner) {
		StorageOperationStatus changeRelationLabel = changeRelationLabel(componentType, componentId, owner, GraphEdgeLabels.STATE, GraphEdgeLabels.LAST_STATE);
		if (!changeRelationLabel.equals(StorageOperationStatus.OK)) {
			return changeRelationLabel;
		}

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);

		StorageOperationStatus createUserToResourceRelation = createUserToResourceRelation(componentType, modifier.getUserId(), componentId, GraphEdgeLabels.STATE, props);
		if (!createUserToResourceRelation.equals(StorageOperationStatus.OK)) {
			return createUserToResourceRelation;
		}
		return StorageOperationStatus.OK;
	}

	@Override
	public Either<? extends Component, StorageOperationStatus> certifyComponent(NodeTypeEnum nodeType, Component component, User modifier, User currentOwner, boolean inTransaction) {
		Either<? extends Component, StorageOperationStatus> result = null;

		try {
			String resourceIdBeforeCertify = component.getUniqueId();
			String[] versionParts = component.getVersion().split(VERSION_DELIMETER_REGEXP);
			Integer majorVersion = Integer.parseInt(versionParts[0]);

			// update old certified resource
			if (majorVersion > 0) {
				StorageOperationStatus updateLastCertifiedResource = StorageOperationStatus.OK;
				updateLastCertifiedResource = updateLastCertifiedComponent(component, majorVersion);
				if (!updateLastCertifiedResource.equals(StorageOperationStatus.OK)) {
					return Either.right(updateLastCertifiedResource);
				}
			}

			// clone the resource
			Either<? extends Component, StorageOperationStatus> createResourceResult = Either.right(StorageOperationStatus.GENERAL_ERROR);
			switch (nodeType) {
			case Service:
			case Resource:
				createResourceResult = cloneComponentForCertified(component, modifier, majorVersion);
				break;
			default:
				log.error("component object is with type {} . It's not supported type");
				result = Either.right(StorageOperationStatus.BAD_REQUEST);
				return result;
			}

			if (createResourceResult.isRight()) {
				log.error("failed to create new resource version for checkout.");
				result = createResourceResult;
				return createResourceResult;
			}

			Component certifiedResource = createResourceResult.left().value();

			// add rfc relation to preserve follower information
			StorageOperationStatus addRfcRelation = addRfcRelationToCertfiedComponent(nodeType, resourceIdBeforeCertify, certifiedResource.getUniqueId());
			if (!addRfcRelation.equals(StorageOperationStatus.OK)) {
				result = Either.right(addRfcRelation);
				return result;
			}

			result = Either.left(certifiedResource);

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

	@Override
	public Either<Boolean, StorageOperationStatus> deleteOldComponentVersions(NodeTypeEnum nodeType, String componentName, String uuid, boolean inTransaction) {

		Either<Boolean, StorageOperationStatus> result = null;
		ComponentOperation componentOperation = getComponentOperation(nodeType);

		try {
			Either<List<Component>, StorageOperationStatus> oldVersionsToDelete = getComponentTempVersions(nodeType, uuid);

			if (oldVersionsToDelete.isRight()) {
				result = Either.right(oldVersionsToDelete.right().value());
				return result;
			}

			for (Component resourceToDelete : oldVersionsToDelete.left().value()) {

				Either<Component, StorageOperationStatus> updateResource = componentOperation.markComponentToDelete(resourceToDelete, inTransaction);
				if (updateResource.isRight()) {
					result = Either.right(updateResource.right().value());
					return result;
				}
			}
			result = Either.left(true);
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

	private StorageOperationStatus addRfcRelationToCertfiedComponent(NodeTypeEnum componentType, String resourceIdBeforeCertify, String uniqueId) {

		// get user of certification request
		UniqueIdData componentV = new UniqueIdData(componentType, resourceIdBeforeCertify);
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);
		Either<GraphRelation, TitanOperationStatus> rfcRelationResponse = titanGenericDao.getIncomingRelationByCriteria(componentV, GraphEdgeLabels.LAST_STATE, props);
		if (rfcRelationResponse.isRight()) {
			TitanOperationStatus status = rfcRelationResponse.right().value();
			log.error("failed to find rfc relation for component {}. status=", resourceIdBeforeCertify, status);
			return DaoStatusConverter.convertTitanStatusToStorageStatus(status);
		}
		GraphRelation rfcRelation = rfcRelationResponse.left().value();
		rfcRelation.setTo(new RelationEndPoint(componentType, GraphPropertiesDictionary.UNIQUE_ID.getProperty(), uniqueId));

		Either<GraphRelation, TitanOperationStatus> createRelationResponse = titanGenericDao.createRelation(rfcRelation);
		if (createRelationResponse.isRight()) {
			TitanOperationStatus status = createRelationResponse.right().value();
			log.error("failed to create rfc relation for component {}. status=", uniqueId, status);
			return DaoStatusConverter.convertTitanStatusToStorageStatus(status);
		}
		return StorageOperationStatus.OK;

	}

	private StorageOperationStatus updateLastCertifiedComponent(Component component, Integer majorVersion) {

		NodeTypeEnum nodeType = component.getComponentType().getNodeType();
		ComponentOperation componentOperation = getComponentOperation(nodeType);
		Map<String, Object> additionalQueryParams = null;
		if (nodeType == NodeTypeEnum.Resource) {
			ResourceTypeEnum resourceType = ((Resource) component).getResourceType();
			additionalQueryParams = new HashMap<String, Object>();
			additionalQueryParams.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), resourceType.name());
		}
		Either<? extends Component, StorageOperationStatus> getLastCertifiedResponse = componentOperation.getComponentByNameAndVersion(component.getName(), majorVersion + VERSION_DELIMETER + "0", additionalQueryParams, true);

		if (getLastCertifiedResponse.isRight()) {
			log.error("failed to update last certified resource. status={}", getLastCertifiedResponse.right().value());
			return getLastCertifiedResponse.right().value();
		}

		Component lastCertified = getLastCertifiedResponse.left().value();
		lastCertified.setHighestVersion(false);
		Either<Component, StorageOperationStatus> updateResource = componentOperation.updateComponent(lastCertified, true);
		if (updateResource.isRight()) {
			log.error("failed to update last certified resource. status={}", updateResource.right().value());
			return updateResource.right().value();
		}
		return StorageOperationStatus.OK;
	}

	private Either<Component, StorageOperationStatus> cloneComponentForCheckout(Component component, NodeTypeEnum nodeType, User modifier) {

		ComponentOperation componentOperation = getComponentOperation(nodeType);
		String prevId = component.getUniqueId();
		// set new version
		Either<String, StorageOperationStatus> nextVersion = getNextVersion(component.getVersion());
		if (nextVersion.isRight()) {
			return Either.right(nextVersion.right().value());
		}

		// if checkout on certified service - init distribution status back
		if (nodeType == NodeTypeEnum.Service && component.getLifecycleState().equals(LifecycleStateEnum.CERTIFIED)) {
			((Service) component).setDistributionStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);
		}

		String version = nextVersion.left().value();
		component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		component.setLastUpdateDate(null);
		component.setLastUpdaterUserId(modifier.getUserId());
		component.setHighestVersion(true);

		// check version of resource does not exist. Note that resource type VF
		// can have same name as resource type VFC
		Map<String, Object> additionalQueryParams = null;
		if (nodeType == NodeTypeEnum.Resource) {
			ResourceTypeEnum resourceType = ((Resource) component).getResourceType();
			additionalQueryParams = new HashMap<String, Object>();
			additionalQueryParams.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), resourceType.name());
		}
		String name = component.getComponentMetadataDefinition().getMetadataDataDefinition().getName();
		Either<Component, StorageOperationStatus> alreadyExistResult = componentOperation.getComponentByNameAndVersion(name, version, additionalQueryParams, true);
		if (alreadyExistResult.isLeft()) {
			log.debug("Component with name {} and version {} already exist", name, version);
			return Either.right(StorageOperationStatus.ENTITY_ALREADY_EXISTS);

		}

		StorageOperationStatus storageOperationStatus = alreadyExistResult.right().value();
		if (storageOperationStatus != StorageOperationStatus.NOT_FOUND) {
			log.debug("Unexpected error when checking if component with name {} and version {} already exist, error: {}", name, version, storageOperationStatus);
			return Either.right(storageOperationStatus);
		}

		Either<Component, StorageOperationStatus> cloneComponentResponse = componentOperation.cloneComponent(component, version, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, true);

		return cloneComponentResponse;
	}

	private Either<String, StorageOperationStatus> getNextVersion(String currVersion) {
		String[] versionParts = currVersion.split(VERSION_DELIMETER_REGEXP);
		if (versionParts == null || versionParts.length != 2) {
			log.error("invalid version {}", currVersion);
			return Either.right(StorageOperationStatus.BAD_REQUEST);
		}

		Integer minorVersion = Integer.parseInt(versionParts[1]) + 1;
		String newVersion = versionParts[0] + VERSION_DELIMETER + minorVersion;
		return Either.left(newVersion);
	}

	private StorageOperationStatus setRelationForCancelCertification(LifecycleStateEnum nextState, NodeTypeEnum componentType, String componentId) {

		StorageOperationStatus result = StorageOperationStatus.GENERAL_ERROR;
		Map<String, Object> props = new HashMap<String, Object>();
		UniqueIdData componentData = new UniqueIdData(componentType, componentId);

		// delete relation CERTIFICATION_IN_PROGRESS
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);

		Either<GraphRelation, TitanOperationStatus> deleteResult = titanGenericDao.deleteIncomingRelationByCriteria(componentData, GraphEdgeLabels.STATE, props);
		if (deleteResult.isRight()) {
			log.debug("failed to update last state relation");
			result = StorageOperationStatus.INCONSISTENCY;
			return result;
		}

		// delete relation READY_FOR_CERTIFICATION (LAST_STATE)
		props.put(GraphPropertiesDictionary.STATE.getProperty(), nextState);

		deleteResult = titanGenericDao.deleteIncomingRelationByCriteria(componentData, GraphEdgeLabels.LAST_STATE, props);
		if (deleteResult.isRight()) {
			log.debug("failed to update last state relation");
			result = StorageOperationStatus.INCONSISTENCY;
			return result;
		}
		GraphRelation origRelation = deleteResult.left().value();

		// create relation READY_FOR_CERTIFICATION (STATE)
		UserData user = new UserData();
		user.setUserId((String) origRelation.getFrom().getIdValue());
		Either<GraphRelation, TitanOperationStatus> createRelationResult = titanGenericDao.createRelation(user, componentData, GraphEdgeLabels.STATE, origRelation.toGraphMap());

		if (createRelationResult.isRight()) {
			log.error("failed to update last state relation. status={}", createRelationResult.right().value());
			result = StorageOperationStatus.INCONSISTENCY;
			return result;
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus setRelationForFailCertification(LifecycleStateEnum nextState, NodeTypeEnum componentType, String componentId) {

		StorageOperationStatus result = null;
		Map<String, Object> props = new HashMap<String, Object>();
		UniqueIdData componentData = new UniqueIdData(componentType, componentId);

		// delete relation CERTIFICATION_IN_PROGRESS
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);

		Either<GraphRelation, TitanOperationStatus> deleteResult = titanGenericDao.deleteIncomingRelationByCriteria(componentData, GraphEdgeLabels.STATE, props);
		if (deleteResult.isRight()) {
			log.debug("failed to update last state relation");
			result = StorageOperationStatus.INCONSISTENCY;
			return result;
		}

		// delete relation READY_FOR_CERTIFICATION
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.READY_FOR_CERTIFICATION);

		deleteResult = titanGenericDao.deleteIncomingRelationByCriteria(componentData, GraphEdgeLabels.LAST_STATE, props);
		if (deleteResult.isRight()) {
			log.debug("failed to update last state relation");
			result = StorageOperationStatus.INCONSISTENCY;
			return result;
		}

		// delete relation NOT_CERTIFIED_CHECKIN (in order to change to STATE)
		props.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		deleteResult = titanGenericDao.deleteIncomingRelationByCriteria(componentData, GraphEdgeLabels.LAST_STATE, props);
		if (deleteResult.isRight()) {
			log.debug("failed to update last state relation");
			result = StorageOperationStatus.INCONSISTENCY;
			return result;
		}

		// create new STATE relation NOT_CERTIFIED_CHECKIN
		GraphRelation origRelation = deleteResult.left().value();
		UserData user = new UserData();
		user.setUserId((String) origRelation.getFrom().getIdValue());
		Either<GraphRelation, TitanOperationStatus> createRelationResult = titanGenericDao.createRelation(user, componentData, GraphEdgeLabels.STATE, origRelation.toGraphMap());

		if (createRelationResult.isRight()) {
			log.debug("failed to update last state relation");
			result = StorageOperationStatus.INCONSISTENCY;
			return result;
		}
		
		// delete relation LAST_MODIFIER (in order to change tester to designer)
		deleteResult = titanGenericDao.deleteIncomingRelationByCriteria(componentData, GraphEdgeLabels.LAST_MODIFIER, null);
		if (deleteResult.isRight()) {
			log.debug("failed to update last modifier relation");
			result = StorageOperationStatus.INCONSISTENCY;
			return result;
		}
		
		// create new LAST_MODIFIER relation
		origRelation = deleteResult.left().value();
		createRelationResult = titanGenericDao.createRelation(user, componentData, GraphEdgeLabels.LAST_MODIFIER, origRelation.toGraphMap());
		if (createRelationResult.isRight()) {
			log.debug("failed to update last state relation");
			result = StorageOperationStatus.INCONSISTENCY;
			return result;
		}
		
		return StorageOperationStatus.OK;
	}

	/**
	 * update service metadata - lastUpdater and state
	 * 
	 * @param component
	 * @param modifier
	 * @param nextState
	 * @return
	 */
	private Either<Component, StorageOperationStatus> updateComponentMD(Component component, User modifier, LifecycleStateEnum nextState, NodeTypeEnum nodeType, ComponentParametersView returnedComponentParametersViewFilter) {

		if (returnedComponentParametersViewFilter == null) {
			returnedComponentParametersViewFilter = new ComponentParametersView();
		}

		Either<Component, StorageOperationStatus> result = Either.right(StorageOperationStatus.GENERAL_ERROR);
		component.setLastUpdateDate(null);
		component.setLastUpdaterUserId(modifier.getUserId());

		ComponentOperation componentOperation = getComponentOperation(nodeType);
		ComponentParametersView filterParametersView = buildFilterForFetchComponentAfterChangeState();
		log.debug("updateComponentMD::updateComponentFilterResult start");
		result = componentOperation.updateComponentFilterResult(component, true, filterParametersView);
		log.debug("updateComponentMD::updateComponentFilterResult end");
		if (result.isRight()) {
			log.debug("Failed to update component for certification request, error: {}", result.right().value());
			return result;
		}
		log.debug("updateComponentMD::getAndUpdateMetadata start");
		// get service MD
		Either<ComponentMetadataData, TitanOperationStatus> componentDataResult = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), component.getUniqueId(), ComponentMetadataData.class);
		if (componentDataResult.isRight()) {
			log.debug("failed to get service data from graph");
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(componentDataResult.right().value()));
		}

		// set state on resource
		ComponentMetadataData componentData = componentDataResult.left().value();
		componentData.getMetadataDataDefinition().setState(nextState.name());
		component.setLifecycleState(nextState);
		Either<ComponentMetadataData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(componentData, ComponentMetadataData.class);
		log.debug("updateComponentMD::getAndUpdateMetadata end");
		if (updateNode.isRight()) {
			log.error("Failed to update component {}. status is {}", component.getUniqueId(), updateNode.right().value());
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateNode.right().value()));
			return result;
		}
		log.debug("updateComponentMD::getAndUpdateMetadata start");
		Either<Object, StorageOperationStatus> serviceAfterChange = componentOperation.getComponent(component.getUniqueId(), returnedComponentParametersViewFilter, true);
		log.debug("updateComponentMD::getAndUpdateMetadata end");
		if (serviceAfterChange.isRight()) {
			log.error("Failed to get component {} after change. status is {}", component.getUniqueId(), updateNode.right().value());
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateNode.right().value()));
			return result;
		}
		return Either.left((Component) serviceAfterChange.left().value());
	}

	/**
	 * update resouce metadata - lastUpdater and state
	 * 
	 * @param resource
	 * @param modifier
	 * @param nextState
	 * @return
	 */
	private Either<Resource, StorageOperationStatus> updateResourceMD(Resource resource, User modifier, LifecycleStateEnum nextState) {

		Either<Resource, StorageOperationStatus> result;
		resource.setLastUpdateDate(null);
		resource.setLastUpdaterUserId(modifier.getUserId());

		result = resourceOperation.updateResource(resource, true);
		if (result.isRight()) {
			log.debug("failed to update resource for certification request.");
			return result;
		}
		// get resource MD
		Either<ResourceMetadataData, TitanOperationStatus> resourceDataResult = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resource.getUniqueId(), ResourceMetadataData.class);
		if (resourceDataResult.isRight()) {
			log.debug("failed to get resource data from graph");
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(resourceDataResult.right().value()));
		}

		// set state on resource
		ResourceMetadataData resourceData = resourceDataResult.left().value();
		resourceData.getMetadataDataDefinition().setState(nextState.name());
		resource.setLifecycleState(nextState);
		Either<ResourceMetadataData, TitanOperationStatus> updateNode = titanGenericDao.updateNode(resourceData, ResourceMetadataData.class);

		if (updateNode.isRight()) {
			log.error("Failed to update resource {}. status is {}", resource.getUniqueId(), updateNode.right().value());
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateNode.right().value()));
			return result;
		}
		return Either.left(resource);
	}

	private Either<List<Component>, StorageOperationStatus> getComponentTempVersions(NodeTypeEnum nodeType, String uuid) {

		Either<List<Component>, StorageOperationStatus> result = Either.right(StorageOperationStatus.GENERAL_ERROR);
		List<Component> componentList = new ArrayList<Component>();
		ComponentOperation componentOperation = getComponentOperation(nodeType);

		Map<String, Object> hasProps = new HashMap<String, Object>();
		Map<String, Object> hasNotProps = new HashMap<String, Object>();

		createOldVersionsCriteria(nodeType, uuid, hasProps, hasNotProps);

		Either<List<ComponentMetadataData>, TitanOperationStatus> getByCriteria = titanGenericDao.getByCriteria(nodeType, hasProps, hasNotProps, ComponentMetadataData.class);

		if (getByCriteria.isRight()) {
			log.error("failed to get old versions for component, type:{}, id: {}", nodeType, uuid);
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getByCriteria.right().value()));
			return result;
		}

		List<ComponentMetadataData> oldVersionComponents = getByCriteria.left().value();
		for (ComponentMetadataData component : oldVersionComponents) {
			Either<Component, StorageOperationStatus> resourceRes = componentOperation.getComponent(component.getMetadataDataDefinition().getUniqueId(), true);
			if (resourceRes.isRight()) {
				result = Either.right(resourceRes.right().value());
				return result;
			} else {
				componentList.add(resourceRes.left().value());
			}
		}
		result = Either.left(componentList);
		return result;
	}

	/*
	 * private Either<List<Service>, StorageOperationStatus> getServiceTempVersions(NodeTypeEnum nodeType, String uuid) {
	 * 
	 * Either<List<Service>, StorageOperationStatus> result = Either.right(StorageOperationStatus.GENERAL_ERROR); List<Service> resourceList = new ArrayList<Service>();
	 * 
	 * Map<String, Object> hasProps = new HashMap<String, Object>(); Map<String, Object> hasNotProps = new HashMap<String, Object>();
	 * 
	 * createOldVersionsCriteria(nodeType, uuid, hasProps, hasNotProps);
	 * 
	 * Either<List<ServiceMetadataData>, TitanOperationStatus> getByCriteria = titanGenericDao.getByCriteria(NodeTypeEnum.Service, hasProps, hasNotProps, ServiceMetadataData.class);
	 * 
	 * if (getByCriteria.isRight()) { log.error("failed to get old versions for {}", uuid); result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus( getByCriteria.right().value())); return result; }
	 * 
	 * List<ServiceMetadataData> oldVersionResources = getByCriteria.left().value(); for (ServiceMetadataData resource : oldVersionResources) { Either<Service, StorageOperationStatus> resourceRes = serviceOperation.getService((String)
	 * resource.getUniqueId(), true); if (resourceRes.isRight()) { result = Either.right(resourceRes.right().value()); return result; } else { resourceList.add(resourceRes.left().value()); } } result = Either.left(resourceList); return result; }
	 */
	private void createOldVersionsCriteria(NodeTypeEnum nodeType, String uuid, Map<String, Object> hasProps, Map<String, Object> hasNotProps) {

		hasProps.put(GraphPropertiesDictionary.UUID.getProperty(), uuid);
		hasProps.put(GraphPropertiesDictionary.LABEL.getProperty(), nodeType.name().toLowerCase());
		hasNotProps.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED.name());
	}

	private Either<? extends Component, StorageOperationStatus> updateOldComponentBeforeUndoCheckout(ComponentOperation componentOperation, Component prevComponent, Component currentComponent, String previousVersion, NodeTypeEnum nodeType,
			boolean inTransaction) {

		log.debug("update previous version of component");
		Map<String, Object> additionalQueryParams = new HashMap<String, Object>();

		if (nodeType == NodeTypeEnum.Resource) {
			ResourceTypeEnum resourceType = ((Resource) currentComponent).getResourceType();

			additionalQueryParams.put(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty(), resourceType.name());
		}
		ComponentMetadataDataDefinition metadataDataDefinition = currentComponent.getComponentMetadataDefinition().getMetadataDataDefinition();
		Either<? extends Component, StorageOperationStatus> getOlderCompResult = componentOperation.getComponentByNameAndVersion(metadataDataDefinition.getName(), previousVersion, additionalQueryParams, true);

		// if previous version exist - set it as current version
		if (getOlderCompResult.isRight()) {
			if (StorageOperationStatus.NOT_FOUND.equals(getOlderCompResult.right().value())) {
				log.debug("No components by name and version: {} - {}", metadataDataDefinition.getName(), previousVersion);
				log.debug("Name may have changed, since the version isn't certified  try to fetch by UUID {}", metadataDataDefinition.getUUID());
				additionalQueryParams.clear();
				additionalQueryParams.put(GraphPropertiesDictionary.UUID.getProperty(), metadataDataDefinition.getUUID());
				additionalQueryParams.put(GraphPropertiesDictionary.VERSION.getProperty(), previousVersion);

				Either<List<ComponentMetadataData>, TitanOperationStatus> byUUID = titanGenericDao.getByCriteria(nodeType, additionalQueryParams, ComponentMetadataData.class);
				if (byUUID.isRight()) {
					log.debug("Failed to fetch by UUID {}", metadataDataDefinition.getUUID());
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(byUUID.right().value()));
				}
				String prevVersionId = (String) byUUID.left().value().get(0).getUniqueId();
				Either<? extends Component, StorageOperationStatus> component = componentOperation.getComponent(prevVersionId, inTransaction);
				if (component.isRight()) {
					log.debug("Failed to fetch previous component by ID {}", prevVersionId);
					return Either.right(component.right().value());
				}
				prevComponent = component.left().value();
			} else {
				log.error("failed to find previous version. status={} ", getOlderCompResult.right().value());
				return getOlderCompResult;
			}
		} else {
			prevComponent = getOlderCompResult.left().value();
		}

		// if last resource is certified - don't touch it.
		if (prevComponent.getVersion().endsWith(".0")) {
			return Either.left(prevComponent);
		}

		prevComponent.setHighestVersion(true);
		Either<Component, StorageOperationStatus> updateCompResult = componentOperation.updateComponent(prevComponent, inTransaction);
		if (updateCompResult.isRight()) {
			log.debug("failed to update prev version of component");
			return updateCompResult;
		}

		User user = new User();
		user.setUserId(prevComponent.getLastUpdaterUserId());
		StorageOperationStatus changeStateRelation = changeStateRelation(nodeType, prevComponent.getUniqueId(), user, GraphEdgeLabels.LAST_STATE, GraphEdgeLabels.STATE);
		if (!changeStateRelation.equals(StorageOperationStatus.OK)) {
			return Either.right(changeStateRelation);
		}

		return Either.left(prevComponent);
	}

	private StorageOperationStatus changeStateRelation(NodeTypeEnum nodeType, String componentId, User currentOwner, GraphEdgeLabels from, GraphEdgeLabels to) {
		UniqueIdData componentData = new UniqueIdData(nodeType, componentId);
		UserData userData = new UserData();
		userData.setUserId(currentOwner.getUserId());
		Either<GraphRelation, TitanOperationStatus> replaceRelationLabelResult = titanGenericDao.replaceRelationLabel(userData, componentData, from, to);
		if (replaceRelationLabelResult.isRight()) {
			TitanOperationStatus titanStatus = replaceRelationLabelResult.right().value();
			log.error("failed to replace label from {} to {}. status = {}", from, to, titanStatus);
			StorageOperationStatus storageStatus = StorageOperationStatus.INCONSISTENCY;
			if (!titanStatus.equals(TitanOperationStatus.INVALID_ID)) {
				storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(titanStatus);
			}
			return storageStatus;
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus removeUserToResourceRelation(NodeTypeEnum componentType, String idFrom, String idTo, GraphEdgeLabels label) {

		UniqueIdData componentV = new UniqueIdData(componentType, idTo);
		UserData userV = new UserData();
		userV.setUserId(idFrom);
		// delete relation
		Either<GraphRelation, TitanOperationStatus> deleteRelationResult = titanGenericDao.deleteRelation(userV, componentV, label);
		if (deleteRelationResult.isRight()) {
			log.error("failed to delete relation. status={}", deleteRelationResult.right().value());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(deleteRelationResult.right().value());
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus createUserToResourceRelation(NodeTypeEnum componentType, String idFrom, String idTo, GraphEdgeLabels label, Map<String, Object> props) {

		UniqueIdData componentV = new UniqueIdData(componentType, idTo);
		UserData userV = new UserData();
		userV.setUserId(idFrom);
		// create relation
		Either<GraphRelation, TitanOperationStatus> createRelationResult = titanGenericDao.createRelation(userV, componentV, label, props);
		if (createRelationResult.isRight()) {
			log.error("failed to create relation. status={}", createRelationResult.right().value());
			return DaoStatusConverter.convertTitanStatusToStorageStatus(createRelationResult.right().value());
		}
		return StorageOperationStatus.OK;
	}

	@Override
	public Either<? extends Component, StorageOperationStatus> cancelOrFailCertification(NodeTypeEnum nodeType, Component component, User modifier, User owner, LifecycleStateEnum nextState, boolean inTransaction) {

		Either<? extends Component, StorageOperationStatus> result = Either.right(StorageOperationStatus.GENERAL_ERROR);
		try {

			ComponentParametersView componentParametersView = buildFilterForFetchComponentAfterChangeState();
			result = updateComponentMD(component, modifier, nextState, nodeType, componentParametersView);
			if (result.isRight()) {
				log.debug("Couldn't set lifecycle for component {} to state {}, error: {}", component.getUniqueId(), nextState, result.right().value());
				return result;
			}
			StorageOperationStatus status = StorageOperationStatus.OK;
			// cancel certification process
			if (nextState.equals(LifecycleStateEnum.READY_FOR_CERTIFICATION)) {
				status = setRelationForCancelCertification(nextState, nodeType, component.getUniqueId());

			} // fail certification
			else if (nextState.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN)) {
				status = setRelationForFailCertification(nextState, nodeType, component.getUniqueId());
			}

			if (!status.equals(StorageOperationStatus.OK)) {
				result = Either.right(status);
			}
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

}
