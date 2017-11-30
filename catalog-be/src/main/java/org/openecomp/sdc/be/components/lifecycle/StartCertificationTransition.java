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

public class StartCertificationTransition extends LifeCycleTransition {

	private static Logger log = LoggerFactory.getLogger(StartCertificationTransition.class.getName());

	public StartCertificationTransition(ComponentsUtils componentUtils, ToscaElementLifecycleOperation lifecycleOperation, ToscaOperationFacade toscaOperationFacade, TitanDao titanDao) {
		super(componentUtils, lifecycleOperation, toscaOperationFacade,  titanDao);

		// authorized roles
		Role[] rsrcServiceStartCertificationRoles = { Role.ADMIN, Role.TESTER };
		addAuthorizedRoles(ComponentTypeEnum.RESOURCE, Arrays.asList(rsrcServiceStartCertificationRoles));
		addAuthorizedRoles(ComponentTypeEnum.SERVICE, Arrays.asList(rsrcServiceStartCertificationRoles));
		// TODO to be later defined for product
		
		//additional authorized roles for resource type
		Role[] resourceRoles = { Role.DESIGNER};
		addResouceAuthorizedRoles(ResourceTypeEnum.VFCMT, Arrays.asList(resourceRoles));
	}

	@Override
	public LifeCycleTransitionEnum getName() {
		return LifeCycleTransitionEnum.START_CERTIFICATION;
	}

	@Override
	public AuditingActionEnum getAuditingAction() {
		return AuditingActionEnum.START_CERTIFICATION_RESOURCE;
	}

	@Override
	public Either<? extends Component, ResponseFormat> changeState(ComponentTypeEnum componentType, Component component, ComponentBusinessLogic componentBl, User modifier, User owner, boolean shouldLock, boolean inTransaction) {

		log.debug("start performing certification test for resource {}", component.getUniqueId());
		Either<? extends Component, ResponseFormat> result = null;
		try{
			Either<ToscaElement, StorageOperationStatus> stateChangeResult = lifeCycleOperation.startCertificationToscaElement(component.getUniqueId(), modifier.getUserId(), owner.getUserId());
			if (stateChangeResult.isRight()) {
				log.debug("start certification failed on graph");
				StorageOperationStatus response = stateChangeResult.right().value();
				ActionStatus actionStatus = componentUtils.convertFromStorageResponse(response);
	
				if (response.equals(StorageOperationStatus.ENTITY_ALREADY_EXISTS)) {
					actionStatus = ActionStatus.COMPONENT_VERSION_ALREADY_EXIST;
				}
				ResponseFormat responseFormat = componentUtils.getResponseFormatByComponent(actionStatus, component, componentType);
				result =  Either.right(responseFormat);
			}
			else {
				result =  Either.left(ModelConverter.convertFromToscaElement(stateChangeResult.left().value()));
			}
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
		return result;
	}

	@Override
	public Either<Boolean, ResponseFormat> validateBeforeTransition(Component component, ComponentTypeEnum componentType, User modifier, User owner, LifecycleStateEnum oldState, LifecycleChangeInfoWithAction lifecycleChangeInfo) {
		String componentName = component.getComponentMetadataDefinition().getMetadataDataDefinition().getName();
		log.debug("validate before start certification test. resource name={}, oldState={}, owner userId={}", componentName, oldState, owner.getUserId());

		// validate user
		Either<Boolean, ResponseFormat> userValidationResponse = userRoleValidation(modifier,component, componentType, lifecycleChangeInfo);
		if (userValidationResponse.isRight()) {
			return userValidationResponse;
		}
		
		if (oldState.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN) || oldState.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT)) {
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, componentName, componentType.name().toLowerCase());
			return Either.right(error);
		}

		if (oldState.equals(LifecycleStateEnum.CERTIFIED)) {
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_ALREADY_CERTIFIED, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
			return Either.right(error);
		}

		if (oldState.equals(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS)) {
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
			return Either.right(error);
		}

		return Either.left(true);
	}

}
