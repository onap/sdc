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

import fj.data.Either;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.tosca.ToscaUtils;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Arrays;
import java.util.List;

public class CheckoutTransition extends LifeCycleTransition {

    private static final Logger log = Logger.getLogger(CheckoutTransition.class);

    CheckoutTransition(ComponentsUtils componentUtils, ToscaElementLifecycleOperation lifecycleOperation, ToscaOperationFacade toscaOperationFacade, JanusGraphDao janusGraphDao) {
        super(componentUtils, lifecycleOperation, toscaOperationFacade, janusGraphDao);

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

        log.debug("start performing {} for resource {}", getName(), component.getUniqueId());

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
                if(componentType == ComponentTypeEnum.SERVICE) {
                    Service service = (Service)clonedComponent;
                    service.validateAndSetInstantiationType();
                }
                if ( checkoutResourceResult.left().value().getToscaType() == ToscaElementTypeEnum.NODE_TYPE ){
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
                if (upgradeToLatestGeneric.isRight()) {
                    result = Either.right(upgradeToLatestGeneric.right().value());
                } else if (upgradeToLatestGeneric.left().value()  ) {
                    StorageOperationStatus response = upgradeToLatestGenericData(clonedComponent);
                    if (StorageOperationStatus.OK != response) {
                        ActionStatus actionStatus = componentUtils.convertFromStorageResponse(response);
                        ResponseFormat responseFormat = componentUtils.getResponseFormatByComponent(actionStatus, component, componentType);
                        result = Either.right(responseFormat);
                    }
                }
                handleCalculatedCapabilitiesRequirements(clonedComponent);
               updateCapReqPropertiesOwnerId(clonedComponent);
            }

        } finally {
            if (result == null || result.isRight()) {
                BeEcompErrorManager.getInstance().logBeDaoSystemError("Change LifecycleState");
                if (!inTransaction) {
                    log.debug("operation failed. do rollback");
                    janusGraphDao.rollback();
                }
            } else {
                if (!inTransaction) {
                    log.debug("operation success. do commit");
                    janusGraphDao.commit();
                }
            }
        }
        return result;
    }

    private void handleCalculatedCapabilitiesRequirements(Component clonedComponent) {
        if(clonedComponent.isTopologyTemplate() && ToscaUtils.isNotComplexVfc(clonedComponent)){
            toscaOperationFacade.revertNamesOfCalculatedCapabilitiesRequirements(clonedComponent.getUniqueId());
        }
    }

    private void updateCapReqPropertiesOwnerId(Component component) {
        if(component.isTopologyTemplate() && ToscaUtils.isNotComplexVfc(component)) {
            toscaOperationFacade.updateCapReqPropertiesOwnerId(component.getUniqueId());
        }
    }
    private StorageOperationStatus upgradeToLatestGenericData(Component clonedComponent) {

        StorageOperationStatus updateStatus = StorageOperationStatus.OK;
        Either<Component, StorageOperationStatus> updateEither = toscaOperationFacade.updateToscaElement(clonedComponent);
        if (updateEither.isRight()) {
            updateStatus = updateEither.right().value();
        } else if (clonedComponent.shouldGenerateInputs()) {
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
        // check resource is not locked by another user
        if (oldState.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT)) {
            ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_IN_CHECKOUT_STATE, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
            return Either.right(error);
        }
        return Either.left(true);
    }

}
