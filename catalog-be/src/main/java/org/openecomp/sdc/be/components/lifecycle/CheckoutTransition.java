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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

public class CheckoutTransition extends LifeCycleTransition {

	private static Logger log = LoggerFactory.getLogger(CheckoutTransition.class.getName());

	public CheckoutTransition(ComponentsUtils componentUtils, ToscaElementLifecycleOperation lifecycleOperation, ToscaOperationFacade toscaOperationFacade, TitanDao titanDao) {
		super(componentUtils, lifecycleOperation, toscaOperationFacade, titanDao);

		// authorized roles
		Role[] resourceServiceCheckoutRoles = { Role.ADMIN, Role.DESIGNER };
		Role[] productCheckoutRoles = { Role.ADMIN, Role.PRODUCT_MANAGER };
		addAuthorizedRoles(ComponentTypeEnum.RESOURCE, Arrays.asList(resourceServiceCheckoutRoles));
		addAuthorizedRoles(ComponentTypeEnum.SERVICE, Arrays.asList(resourceServiceCheckoutRoles));
		addAuthorizedRoles(ComponentTypeEnum.PRODUCT, Arrays.asList(productCheckoutRoles));

	}

	@Override
	public LifeCycleTransitionEnum getName() {
		return LifeCycleTransitionEnum.CHECKOUT;
	}

	@Override
	public AuditingActionEnum getAuditingAction() {
		return AuditingActionEnum.CHECKOUT_RESOURCE;
	}

	@Override
	public Either<? extends Component, ResponseFormat> changeState(ComponentTypeEnum componentType, Component component, ComponentBusinessLogic componentBl, User modifier, User owner, boolean shouldLock, boolean inTransaction) {

		log.debug("start performing {} for resource {}", getName().name(), component.getUniqueId());

		Either<? extends Component, ResponseFormat> result = null;
		try {

			Either<ToscaElement, StorageOperationStatus> checkoutResourceResult = lifeCycleOperation.checkoutToscaElement(component.getUniqueId(), modifier.getUserId(), owner.getUserId());

			if (checkoutResourceResult.isRight()) {
				log.debug("checkout failed on graph");
				StorageOperationStatus response = checkoutResourceResult.right().value();
				ActionStatus actionStatus = componentUtils.convertFromStorageResponse(response);

				if (response.equals(StorageOperationStatus.ENTITY_ALREADY_EXISTS)) {
					actionStatus = ActionStatus.COMPONENT_VERSION_ALREADY_EXIST;
				}
				ResponseFormat responseFormat = componentUtils.getResponseFormatByComponent(actionStatus, component, componentType);
				result = Either.right(responseFormat);
			} else {
			
				Component clonedComponent = ModelConverter.convertFromToscaElement(checkoutResourceResult.left().value());
				if ( checkoutResourceResult.left().value().getToscaType() == ToscaElementTypeEnum.NodeType ){
					Either<Component, ActionStatus> upgradeToLatestDerived = componentBl.shouldUpgradeToLatestDerived(clonedComponent);
					if (upgradeToLatestDerived.isRight() && ActionStatus.OK != upgradeToLatestDerived.right().value()){
						result = Either.right(componentUtils.getResponseFormat(upgradeToLatestDerived.right().value()));
						return result;
					}
					if ( upgradeToLatestDerived.isLeft() ){
						//get resource after update derived 
						clonedComponent = upgradeToLatestDerived.left().value();
					}
				}
				result = Either.left(clonedComponent); 
				Either<Boolean, ResponseFormat> upgradeToLatestGeneric = componentBl.shouldUpgradeToLatestGeneric(clonedComponent);
				if (upgradeToLatestGeneric.isRight())
					result = Either.right(upgradeToLatestGeneric.right().value());
				else if (upgradeToLatestGeneric.left().value()  ) {
					StorageOperationStatus response = upgradeToLatestGenericData(clonedComponent);
					if (StorageOperationStatus.OK != response) {
						ActionStatus actionStatus = componentUtils.convertFromStorageResponse(response);
						ResponseFormat responseFormat = componentUtils.getResponseFormatByComponent(actionStatus, component, componentType);
						result = Either.right(responseFormat);
					}
				}

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

	private StorageOperationStatus upgradeToLatestGenericData(Component clonedComponent) {
		
		StorageOperationStatus updateStatus = StorageOperationStatus.OK;
		Either<Component, StorageOperationStatus> updateEither = toscaOperationFacade.updateToscaElement(clonedComponent);
		if (updateEither.isRight())
			updateStatus = updateEither.right().value();  
		else if (clonedComponent.shouldGenerateInputs()) {
			List<InputDefinition> newInputs = clonedComponent.getInputs();
			updateStatus = lifeCycleOperation.updateToscaDataOfToscaElement(clonedComponent.getUniqueId(), EdgeLabelEnum.INPUTS, VertexTypeEnum.INPUTS, newInputs, JsonPresentationFields.NAME);
		}
		return updateStatus;
	}

	@Override
	public Either<Boolean, ResponseFormat> validateBeforeTransition(Component component, ComponentTypeEnum componentType, User modifier, User owner, LifecycleStateEnum oldState, LifecycleChangeInfoWithAction lifecycleChangeInfo) {
		String componentName = component.getComponentMetadataDefinition().getMetadataDataDefinition().getName();
		log.debug("validate before checkout. resource name={}, oldState={}, owner userId={}", componentName, oldState, owner.getUserId());

		// validate user
		Either<Boolean, ResponseFormat> userValidationResponse = userRoleValidation(modifier, component, componentType, lifecycleChangeInfo);
		if (userValidationResponse.isRight()) {
			return userValidationResponse;
		}

		// Disabled as of 1604 patch after discussing with Ella/Eli/Michael

		/*
		 * if (componentType == ComponentTypeEnum.PRODUCT){ Either<Boolean, ResponseFormat> productContactsEither = productContactsValidation((Product)component, modifier); if (productContactsEither.isRight()){ return productContactsEither; } }
		 */

		// check resource is not locked by another user
		if (oldState.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT)) {
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_IN_CHECKOUT_STATE, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
			return Either.right(error);
		}

		if (oldState.equals(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS)) {
			ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
			return Either.right(error);
		}

		if (oldState.equals(LifecycleStateEnum.READY_FOR_CERTIFICATION)) {
			if (!modifier.getRole().equals(Role.DESIGNER.name()) && !modifier.getRole().equals(Role.ADMIN.name())) {
				ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_SENT_FOR_CERTIFICATION, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
				return Either.right(error);
			}
		}
		return Either.left(true);
	}

}
