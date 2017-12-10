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

import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.Role;
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

	public CertificationChangeTransition(LifeCycleTransitionEnum name, ComponentsUtils componentUtils, ToscaElementLifecycleOperation lifecycleOperation, ToscaOperationFacade toscaOperationFacade, TitanDao titanDao) {
		super(componentUtils, lifecycleOperation, toscaOperationFacade, titanDao);

		this.name = name;

		// authorized roles
		Role[] certificationChangeRoles = { Role.ADMIN, Role.TESTER };
		addAuthorizedRoles(ComponentTypeEnum.RESOURCE, Arrays.asList(certificationChangeRoles));
		addAuthorizedRoles(ComponentTypeEnum.SERVICE, Arrays.asList(certificationChangeRoles));
		// TODO to be later defined for product
		
		//additional authorized roles for resource type
		Role[] resourceRoles = { Role.DESIGNER};
		addResouceAuthorizedRoles(ResourceTypeEnum.VFCMT, Arrays.asList(resourceRoles));
		
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
		BeEcompErrorManager.getInstance().logBeDaoSystemError("Change LifecycleState - Certify failed on graph");
		log.debug("certification change failed on graph");

		ActionStatus actionStatus = componentUtils.convertFromStorageResponse(response);
		ResponseFormat responseFormat = componentUtils.getResponseFormatByComponent(actionStatus, component, componentType);
		return responseFormat;
	}

	@Override
	public Either<Boolean, ResponseFormat> validateBeforeTransition(Component component, ComponentTypeEnum componentType, User modifier, User owner, LifecycleStateEnum oldState, LifecycleChangeInfoWithAction lifecycleChangeInfo) {
		String componentName = component.getComponentMetadataDefinition().getMetadataDataDefinition().getName();
		log.info("validate before certification change. resource name={}, oldState={}, owner userId={}", componentName, oldState, owner.getUserId());

		// validate user
		Either<Boolean, ResponseFormat> userValidationResponse = userRoleValidation(modifier,component, componentType, lifecycleChangeInfo);
		if (userValidationResponse.isRight()) {			
			log.error("userRoleValidation failed");
			return userValidationResponse;
		}

		if (!oldState.equals(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS)) {
			log.error("oldState={} should be={}",oldState,ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION);
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, componentName, componentType.name().toLowerCase());
			return Either.right(error);		
		}

		if (oldState.equals(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS) && !modifier.equals(owner) && !modifier.getRole().equals(Role.ADMIN.name())) {
			log.error("oldState={} should not be={}",oldState,ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE);
			log.error("&& modifier({})!={}  && modifier.role({})!={}",modifier,owner);
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
			return Either.right(error);
		}

		return Either.left(true);
	}

	@Override
	public Either<? extends Component, ResponseFormat> changeState(ComponentTypeEnum componentType, Component component, ComponentBusinessLogic componentBl, User modifier, User owner, boolean shouldLock, boolean inTransaction) {

		log.info("start performing certification change for resource {}", component.getUniqueId());
		Either<? extends Component, ResponseFormat> result = null;

		try {
			Either<ToscaElement, StorageOperationStatus> certificationChangeResult = Either.right(StorageOperationStatus.GENERAL_ERROR);
			if (nextState.equals(LifecycleStateEnum.CERTIFIED)) {
				certificationChangeResult = lifeCycleOperation.certifyToscaElement(component.getUniqueId(), modifier.getUserId(), owner.getUserId());
			} else {
				certificationChangeResult = lifeCycleOperation.cancelOrFailCertification(component.getUniqueId(), modifier.getUserId(), owner.getUserId(), nextState);
			}

			if (certificationChangeResult.isRight()) {
				ResponseFormat responseFormat = formatCertificationError(component, certificationChangeResult.right().value(), componentType);
				result = Either.right(responseFormat);
				return result;
			}
			
			if (nextState.equals(LifecycleStateEnum.CERTIFIED)) {
				Either<Boolean, StorageOperationStatus> deleteOldComponentVersions = lifeCycleOperation.deleteOldToscaElementVersions(ModelConverter.getVertexType(component), componentType, component.getComponentMetadataDefinition().getMetadataDataDefinition().getName(),
						component.getComponentMetadataDefinition().getMetadataDataDefinition().getUUID());
				if (deleteOldComponentVersions.isRight()) {
					ResponseFormat responseFormat = formatCertificationError(component, deleteOldComponentVersions.right().value(), componentType);
					result = Either.right(responseFormat);
				}
			}

			result = Either.left(ModelConverter.convertFromToscaElement(certificationChangeResult.left().value()));
			return result;
		} finally {
			if (result == null || result.isRight()) {
				BeEcompErrorManager.getInstance().logBeDaoSystemError("Change LifecycleState");
				if (inTransaction == false) {
					log.debug("operation failed. do rollback");
					titanDao.rollback();
				}
			} else {
				if (inTransaction == false) {
					log.debug("operation success. do commit");
					titanDao.commit();
				}
			}
		}

	}
}
