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

public class UndoCheckoutTransition extends LifeCycleTransition {
	private static Logger log = LoggerFactory.getLogger(CheckoutTransition.class.getName());
	private ArtifactsBusinessLogic artifactsManager;

	public UndoCheckoutTransition(ComponentsUtils componentUtils, ToscaElementLifecycleOperation lifecycleOperation, ToscaOperationFacade toscaOperationFacade, TitanDao titanDao) {
		super(componentUtils, lifecycleOperation, toscaOperationFacade,  titanDao);

		// authorized roles
		Role[] resourceServiceCheckoutRoles = { Role.ADMIN, Role.DESIGNER };
		Role[] productCheckoutRoles = { Role.ADMIN, Role.PRODUCT_MANAGER };
		addAuthorizedRoles(ComponentTypeEnum.RESOURCE, Arrays.asList(resourceServiceCheckoutRoles));
		addAuthorizedRoles(ComponentTypeEnum.SERVICE, Arrays.asList(resourceServiceCheckoutRoles));
		addAuthorizedRoles(ComponentTypeEnum.PRODUCT, Arrays.asList(productCheckoutRoles));

	}

	@Override
	public LifeCycleTransitionEnum getName() {
		return LifeCycleTransitionEnum.UNDO_CHECKOUT;
	}

	@Override
	public AuditingActionEnum getAuditingAction() {
		return AuditingActionEnum.UNDO_CHECKOUT_RESOURCE;
	}

	public ArtifactsBusinessLogic getArtifactsBusinessLogic() {
		return artifactsManager;
	}

	public void setArtifactsBusinessLogic(ArtifactsBusinessLogic artifactsBusinessLogic) {
		this.artifactsManager = artifactsBusinessLogic;
	}

	@Override
	public Either<Boolean, ResponseFormat> validateBeforeTransition(Component component, ComponentTypeEnum componentType, User modifier, User owner, LifecycleStateEnum oldState, LifecycleChangeInfoWithAction lifecycleChangeInfo) {
		String componentName = component.getComponentMetadataDefinition().getMetadataDataDefinition().getName();
		log.debug("validate before undo checkout. resource name={}, oldState={}, owner userId={}", componentName, oldState, owner.getUserId());

		// validate user
		Either<Boolean, ResponseFormat> userValidationResponse = userRoleValidation(modifier,component, componentType, lifecycleChangeInfo);
		if (userValidationResponse.isRight()) {
			return userValidationResponse;
		}

		// check resource is not locked by another user
		if (!oldState.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT)) {
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_ALREADY_CHECKED_IN, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
			return Either.right(error);
		}

		if (!modifier.equals(owner) && !modifier.getRole().equals(Role.ADMIN.name())) {
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_CHECKOUT_BY_ANOTHER_USER, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
			return Either.right(error);
		}

		return Either.left(true);
	}

	@Override
	public Either<? extends Component, ResponseFormat> changeState(ComponentTypeEnum componentType, Component component, ComponentBusinessLogic componentBl, User modifier, User owner, boolean shouldLock, boolean inTransaction) {

		Either<? extends Component, ResponseFormat> result = null;
		log.debug("start performing undo-checkout for resource {}", component.getUniqueId());

		try {
			Either<ToscaElement, StorageOperationStatus> undoCheckoutResourceResult = lifeCycleOperation.undoCheckout(component.getUniqueId());

			if (undoCheckoutResourceResult.isRight()) {
				log.debug("checkout failed on graph");
				StorageOperationStatus response = undoCheckoutResourceResult.right().value();
				ActionStatus actionStatus = componentUtils.convertFromStorageResponse(response);
				ResponseFormat responseFormat = componentUtils.getResponseFormatByComponent(actionStatus, component, componentType);
				result =  Either.right(responseFormat);
			}
			else {
				result =  Either.left(ModelConverter.convertFromToscaElement(undoCheckoutResourceResult.left().value()));
			}
		} finally {
			if (result == null || result.isRight()) {
				BeEcompErrorManager.getInstance().logBeDaoSystemError("Change LifecycleState - Undo Checkout failed on graph");
				log.debug("operation failed. do rollback");
				titanDao.rollback();
			} else {
				log.debug("operation success. do commit");
				titanDao.commit();
			}
		}
		return result;
	}

}
