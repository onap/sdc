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

package org.openecomp.sdc.be.components.lifecycle;

import java.util.Arrays;
import java.util.List;

import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.ILifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

public class CertificationChangeTransition extends LifeCycleTransition {

	private static Logger log = LoggerFactory.getLogger(CertificationChangeTransition.class.getName());

	private LifecycleStateEnum nextState;
	private LifeCycleTransitionEnum name;
	private AuditingActionEnum auditingAction;
	private ArtifactsBusinessLogic artifactsManager;

	public CertificationChangeTransition(LifeCycleTransitionEnum name, ComponentsUtils componentUtils, ILifecycleOperation lifecycleOperation) {
		super(componentUtils, lifecycleOperation);

		this.name = name;

		// authorized roles
		Role[] certificationChangeRoles = { Role.ADMIN, Role.TESTER };
		addAuthorizedRoles(ComponentTypeEnum.RESOURCE, Arrays.asList(certificationChangeRoles));
		addAuthorizedRoles(ComponentTypeEnum.SERVICE, Arrays.asList(certificationChangeRoles));
		// TODO to be later defined for product

		switch (this.name) {
		case CERTIFY:
			this.auditingAction = AuditingActionEnum.CERTIFICATION_SUCCESS_RESOURCE;
			this.nextState = LifecycleStateEnum.CERTIFIED;
			break;
		case FAIL_CERTIFICATION:
			this.auditingAction = AuditingActionEnum.FAIL_CERTIFICATION_RESOURCE;
			nextState = LifecycleStateEnum.NOT_CERTIFIED_CHECKIN;
			break;
		case CANCEL_CERTIFICATION:
			this.auditingAction = AuditingActionEnum.CANCEL_CERTIFICATION_RESOURCE;
			nextState = LifecycleStateEnum.READY_FOR_CERTIFICATION;
			break;
		default:
			break;
		}

	}

	@Override
	public LifeCycleTransitionEnum getName() {
		return name;
	}

	@Override
	public AuditingActionEnum getAuditingAction() {
		return auditingAction;
	}

	public ArtifactsBusinessLogic getArtifactsManager() {
		return artifactsManager;
	}

	public void setArtifactsManager(ArtifactsBusinessLogic artifactsManager) {
		this.artifactsManager = artifactsManager;
	}

	private ResponseFormat formatCertificationError(Component component, StorageOperationStatus response, ComponentTypeEnum componentType) {
		BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError, "Change LifecycleState - Certify failed on graph");
		BeEcompErrorManager.getInstance().logBeDaoSystemError("Change LifecycleState - Certify failed on graph");
		log.debug("certification change failed on graph");

		ActionStatus actionStatus = componentUtils.convertFromStorageResponse(response);
		ResponseFormat responseFormat = componentUtils.getResponseFormatByComponent(actionStatus, component, componentType);
		return responseFormat;
	}

	@Override
	public Either<Boolean, ResponseFormat> validateBeforeTransition(Component component, ComponentTypeEnum componentType, User modifier, User owner, LifecycleStateEnum oldState, LifecycleChangeInfoWithAction lifecycleChangeInfo) {
		String componentName = component.getComponentMetadataDefinition().getMetadataDataDefinition().getName();
		log.debug("validate before certification change. resource name={}, oldState={}, owner userId={}", componentName, oldState, owner.getUserId());

		// validate user
		Either<Boolean, ResponseFormat> userValidationResponse = userRoleValidation(modifier, componentType, lifecycleChangeInfo);
		if (userValidationResponse.isRight()) {
			return userValidationResponse;
		}

		if (!oldState.equals(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS)) {
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, componentName, componentType.name().toLowerCase());
			return Either.right(error);
		}

		if (oldState.equals(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS) && !modifier.equals(owner) && !modifier.getRole().equals(Role.ADMIN.name())) {
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
			return Either.right(error);
		}

		return Either.left(true);
	}

	@Override
	public Either<? extends Component, ResponseFormat> changeState(ComponentTypeEnum componentType, Component component, ComponentBusinessLogic componentBl, User modifier, User owner, boolean shouldLock, boolean inTransaction) {

		log.info("start performing certification change for resource {}", component.getUniqueId());
		Either<? extends Component, ResponseFormat> result = null;
		NodeTypeEnum nodeType = componentType.getNodeType();

		try {
			Either<? extends Component, StorageOperationStatus> certificationChangeResult = Either.right(StorageOperationStatus.GENERAL_ERROR);
			if (nextState.equals(LifecycleStateEnum.CERTIFIED)) {
				certificationChangeResult = lifeCycleOperation.certifyComponent(nodeType, component, modifier, owner, true);
			} else {
				certificationChangeResult = lifeCycleOperation.cancelOrFailCertification(nodeType, component, modifier, owner, nextState, true);
			}

			if (certificationChangeResult.isRight()) {
				ResponseFormat responseFormat = formatCertificationError(component, certificationChangeResult.right().value(), componentType);
				result = Either.right(responseFormat);
				return result;
			}

			if (nextState.equals(LifecycleStateEnum.CERTIFIED)) {
				Either<Boolean, StorageOperationStatus> deleteOldComponentVersions = lifeCycleOperation.deleteOldComponentVersions(nodeType, component.getComponentMetadataDefinition().getMetadataDataDefinition().getName(),
						component.getComponentMetadataDefinition().getMetadataDataDefinition().getUUID(), true);
				if (deleteOldComponentVersions.isRight()) {
					ResponseFormat responseFormat = formatCertificationError(component, deleteOldComponentVersions.right().value(), componentType);
					result = Either.right(responseFormat);
					return result;
				}
			}

			result = Either.left(certificationChangeResult.left().value());
			return result;
		} finally {
			if (result == null || result.isRight()) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeDaoSystemError, "Change LifecycleState");
				BeEcompErrorManager.getInstance().logBeDaoSystemError("Change LifecycleState");
				if (inTransaction == false) {
					log.debug("operation failed. do rollback");
					lifeCycleOperation.getResourceOperation().getTitanGenericDao().rollback();
				}
			} else {
				if (inTransaction == false) {
					log.debug("operation success. do commit");
					lifeCycleOperation.getResourceOperation().getTitanGenericDao().commit();
				}
			}
		}

	}

	public StorageOperationStatus deleteOldVersion(List<ArtifactDefinition> artifactsToDelete, Resource resourceToDelete) {
		ResourceOperation resourceOperation = lifeCycleOperation.getResourceOperation();

		Either<List<ArtifactDefinition>, StorageOperationStatus> artifactsRes = resourceOperation.getComponentArtifactsForDelete(resourceToDelete.getUniqueId(), NodeTypeEnum.Resource, true);
		if (artifactsRes.isRight()) {
			return artifactsRes.right().value();
		}
		Either<Resource, StorageOperationStatus> deleteResourceRes = resourceOperation.deleteResource(resourceToDelete.getUniqueId(), true);
		if (deleteResourceRes.isRight()) {
			return deleteResourceRes.right().value();
		}
		artifactsToDelete.addAll(artifactsRes.left().value());

		return StorageOperationStatus.OK;
	}

}
