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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.components.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import fj.data.Either;
import org.openecomp.sdc.be.catalog.enums.ChangeTypeEnum;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ProductBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.version.VesionUpdateHandler;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction.LifecycleChanceActionEnum;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.facade.operations.CatalogOperation;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Component("lifecycleBusinessLogic")
public class LifecycleBusinessLogic {

    private static final String COMMENT = "comment";

    @Autowired
    private IGraphLockOperation graphLockOperation = null;

    @Autowired
    private JanusGraphDao janusGraphDao;

    private static final Logger log = Logger.getLogger(LifecycleBusinessLogic.class);

    @javax.annotation.Resource
    private ComponentsUtils componentUtils;

    @javax.annotation.Resource
    private ToscaElementLifecycleOperation lifecycleOperation;

    @Autowired
    @Lazy
    private ServiceBusinessLogic serviceBusinessLogic;

    @Autowired
    @Lazy
    private ResourceBusinessLogic resourceBusinessLogic;

    @Autowired
    @Lazy
    private ProductBusinessLogic productBusinessLogic;

    @Autowired
    ToscaOperationFacade toscaOperationFacade;
    
    @Autowired
    NodeTemplateOperation nodeTemplateOperation;

    @Autowired
    CatalogOperation catalogOperations;

    @Autowired
    VesionUpdateHandler groupUpdateHandler;

    private Map<String, LifeCycleTransition> stateTransitions;

    @PostConstruct
    public void init() {
       initStateOperations();
    }

    private void initStateOperations() {
        stateTransitions = new HashMap<>();

        LifeCycleTransition checkoutOp = new CheckoutTransition(componentUtils, lifecycleOperation, toscaOperationFacade,
            janusGraphDao);
        stateTransitions.put(checkoutOp.getName().name(), checkoutOp);

        UndoCheckoutTransition undoCheckoutOp = new UndoCheckoutTransition(componentUtils, lifecycleOperation, toscaOperationFacade, janusGraphDao);
        undoCheckoutOp.setCatalogOperations(catalogOperations);
        stateTransitions.put(undoCheckoutOp.getName().name(), undoCheckoutOp);

        LifeCycleTransition checkinOp = new CheckinTransition(componentUtils, lifecycleOperation, toscaOperationFacade, janusGraphDao, groupUpdateHandler);
        stateTransitions.put(checkinOp.getName().name(), checkinOp);

        CertificationChangeTransition successCertification = new CertificationChangeTransition(serviceBusinessLogic, LifeCycleTransitionEnum.CERTIFY, componentUtils, lifecycleOperation, toscaOperationFacade, janusGraphDao);
        successCertification.setNodeTemplateOperation(nodeTemplateOperation);
        stateTransitions.put(successCertification.getName().name(), successCertification);
    }

    @VisibleForTesting
    Map<String, LifeCycleTransition> getStartTransition() {
        return stateTransitions;
    }

    // TODO: rhalili - should use changeComponentState when possible
    public Either<Resource, ResponseFormat> changeState(String resourceId, User modifier, LifeCycleTransitionEnum transitionEnum, LifecycleChangeInfoWithAction changeInfo, boolean inTransaction, boolean needLock) {
        return (Either<Resource, ResponseFormat>) changeComponentState(ComponentTypeEnum.RESOURCE, resourceId, modifier, transitionEnum, changeInfo, inTransaction, needLock);
    }

    private boolean isComponentVFCMT(Component component, ComponentTypeEnum componentType) {
        if (componentType.equals(ComponentTypeEnum.RESOURCE)) {
            ResourceTypeEnum resourceType = ((ResourceMetadataDataDefinition) component.getComponentMetadataDefinition().getMetadataDataDefinition()).getResourceType();
            if (resourceType.equals(ResourceTypeEnum.VFCMT)) {
                return true;
            }
        }
        return false;
    }

    public Either<? extends Component, ResponseFormat> changeComponentState(ComponentTypeEnum componentType, String componentId, User modifier, LifeCycleTransitionEnum transitionEnum, LifecycleChangeInfoWithAction changeInfo, boolean inTransaction,
            boolean needLock) {

        LifeCycleTransition lifeCycleTransition = stateTransitions.get(transitionEnum.name());
        if (lifeCycleTransition == null) {
            log.debug("state operation is not valid. operations allowed are: {}", LifeCycleTransitionEnum.valuesAsString());
            ResponseFormat error = componentUtils.getInvalidContentErrorAndAudit(modifier, componentId, AuditingActionEnum.CHECKOUT_RESOURCE);
            return Either.right(error);
        }
        Component component;
        log.debug("get resource from graph");
        ResponseFormat errorResponse;

        Either<? extends Component, ResponseFormat> eitherResourceResponse = getComponentForChange(componentType, componentId, modifier, lifeCycleTransition, changeInfo);
        if (eitherResourceResponse.isRight()) {
            return eitherResourceResponse;
        }
        component = eitherResourceResponse.left().value();
        String resourceCurrVersion = component.getVersion();
        LifecycleStateEnum resourceCurrState = component.getLifecycleState();

        // lock resource
        if (!inTransaction && needLock) {
            log.debug("lock component {}", componentId);
            try {
                lockComponent(componentType, component);
            }catch (ComponentException e){
                errorResponse = e.getResponseFormat();
                componentUtils.auditComponent(errorResponse, modifier, component, lifeCycleTransition.getAuditingAction(),
                        new ResourceCommonInfo(componentType.getValue()),
                        ResourceVersionInfo.newBuilder()
                                .state(resourceCurrState.name())
                                .version(resourceCurrVersion)
                                .build());


                log.error("lock component {} failed", componentId);
                return Either.right(errorResponse);
            }
            log.debug("after lock component {}", componentId);
        }
        try {
            Either<String, ResponseFormat> commentValidationResult = validateComment(changeInfo, transitionEnum);
            if (commentValidationResult.isRight()) {
                errorResponse = commentValidationResult.right().value();
                componentUtils.auditComponent(errorResponse, modifier, component, lifeCycleTransition.getAuditingAction(),
                        new ResourceCommonInfo(componentType.getValue()),
                        ResourceVersionInfo.newBuilder()
                                .state(resourceCurrState.name())
                                .version(resourceCurrVersion)
                                .build(),
                        changeInfo.getUserRemarks());
                return Either.right(errorResponse);
            }
            changeInfo.setUserRemarks(commentValidationResult.left().value());
            log.debug("after validate component");
            Either<Boolean, ResponseFormat> validateHighestVersion = validateHighestVersion(modifier, lifeCycleTransition, component, resourceCurrVersion, componentType);
            if (validateHighestVersion.isRight()) {
                return Either.right(validateHighestVersion.right().value());
            }
            log.debug("after validate Highest Version");
            final Component oldComponent = component;
            Either<? extends Component, ResponseFormat> checkedInComponentEither = checkInBeforeCertifyIfNeeded(componentType, modifier, transitionEnum, changeInfo, inTransaction, component);
            if(checkedInComponentEither.isRight()) {
                return Either.right(checkedInComponentEither.right().value());
            }
            component = checkedInComponentEither.left().value();
            return changeState(component, lifeCycleTransition, componentType, modifier, changeInfo, inTransaction)
                                        .left()
                                        .bind(c -> updateCatalog(c, oldComponent, ChangeTypeEnum.LIFECYCLE));


        } finally {
            component.setUniqueId(componentId);
            if (!inTransaction && needLock) {
                log.info("unlock component {}", componentId);
                NodeTypeEnum nodeType = componentType.getNodeType();
                log.info("During change state, another component {} has been created/updated", componentId);
                graphLockOperation.unlockComponent(componentId, nodeType);

            }
        }

    }

    private Either<Component, ResponseFormat>  updateCatalog(Component component,  Component oldComponent, ChangeTypeEnum changeStatus){

        log.debug("updateCatalog start");
        Component result = component == null? oldComponent : component;
            if(component != null){
                ActionStatus status =  catalogOperations.updateCatalog(changeStatus,component);
                if(status != ActionStatus.OK){
                    return Either.right( componentUtils.getResponseFormat(status));
                }
        }

       return Either.left(result);
    }

    private Either<? extends Component, ResponseFormat> checkInBeforeCertifyIfNeeded(ComponentTypeEnum componentType, User modifier, LifeCycleTransitionEnum transitionEnum, LifecycleChangeInfoWithAction changeInfo, boolean inTransaction,
                                              Component component) {

        LifecycleStateEnum oldState = component.getLifecycleState();
        Component updatedComponent = component;
        log.debug("Certification request for resource {} ", component.getUniqueId());
        if (oldState == LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT && transitionEnum == LifeCycleTransitionEnum.CERTIFY) {
            log.debug("Resource {} is in Checkout state perform checkin", component.getUniqueId());
            Either<? extends Component, ResponseFormat> actionResponse = changeState(component, stateTransitions.get(LifeCycleTransitionEnum.CHECKIN.name()), componentType, modifier, changeInfo, inTransaction);
            if (actionResponse.isRight()) {
                log.debug("Failed to check in Resource {} error {}", component.getUniqueId(), actionResponse.right().value());
            }
            return actionResponse;
        }

        return Either.left(updatedComponent);
    }

    private Either<? extends Component, ResponseFormat> changeState(Component component, LifeCycleTransition lifeCycleTransition, ComponentTypeEnum componentType, User modifier, LifecycleChangeInfoWithAction changeInfo, boolean inTransaction) {
        ResponseFormat errorResponse;

        LifecycleStateEnum oldState = component.getLifecycleState();
        String resourceCurrVersion = component.getVersion();
        ComponentBusinessLogic bl = getComponentBL(componentType);

        Either<User, ResponseFormat> ownerResult = lifeCycleTransition.getComponentOwner(component, componentType);
        if (ownerResult.isRight()) {
            return Either.right(ownerResult.right().value());
        }
        User owner = ownerResult.left().value();
        log.info("owner of resource {} is {}", component.getUniqueId(), owner.getUserId());

        Either<Boolean, ResponseFormat> stateValidationResult = lifeCycleTransition.validateBeforeTransition(component, componentType, modifier, owner, oldState, changeInfo);
        if (stateValidationResult.isRight()) {
            log.error("Failed to validateBeforeTransition");
            errorResponse = stateValidationResult.right().value();
            componentUtils.auditComponent(errorResponse, modifier, component, lifeCycleTransition.getAuditingAction(),
                    new ResourceCommonInfo(componentType.getValue()),
                    ResourceVersionInfo.newBuilder()
                            .version(resourceCurrVersion)
                            .state(oldState.name())
                            .build(),
                    changeInfo.getUserRemarks());
            return Either.right(errorResponse);
        }
        
        Either<? extends Component, ResponseFormat> operationResult = lifeCycleTransition.changeState(componentType, component, bl, modifier, owner, false, inTransaction);

        if (operationResult.isRight()) {
            errorResponse = operationResult.right().value();
            log.info("audit before sending error response");
            componentUtils.auditComponentAdmin(errorResponse, modifier, component, lifeCycleTransition.getAuditingAction(), componentType,
                     ResourceVersionInfo.newBuilder()
                             .state(oldState.name())
                             .version(resourceCurrVersion)
                             .build());

            return Either.right(errorResponse);
        }
        Component resourceAfterOperation = operationResult.left().value() == null? component: operationResult.left().value() ;
        componentUtils.auditComponent(componentUtils.getResponseFormat(ActionStatus.OK), modifier, resourceAfterOperation,
                lifeCycleTransition.getAuditingAction(), new ResourceCommonInfo(componentType.getValue()),
                ResourceVersionInfo.newBuilder()
                        .state(oldState.name())
                        .version(resourceCurrVersion)
                        .build(),
                changeInfo.getUserRemarks());
        return operationResult;

    }


	private Either<? extends Component, ResponseFormat> getComponentForChange(ComponentTypeEnum componentType, String componentId, User modifier, LifeCycleTransition lifeCycleTransition, LifecycleChangeInfoWithAction changeInfo) {

        Either<? extends Component, StorageOperationStatus> eitherResourceResponse = toscaOperationFacade.getToscaElement(componentId);

        ResponseFormat errorResponse;
        if (eitherResourceResponse.isRight()) {
            ActionStatus actionStatus = componentUtils.convertFromStorageResponse(eitherResourceResponse.right().value(), componentType);
            errorResponse = componentUtils.getResponseFormat(actionStatus, Constants.EMPTY_STRING);
            log.debug("audit before sending response");
            componentUtils.auditComponent(errorResponse, modifier, lifeCycleTransition.getAuditingAction(),
                    new ResourceCommonInfo(componentId, componentType.getValue()), changeInfo.getUserRemarks());

            return Either.right(errorResponse);
        }
        return Either.left(eitherResourceResponse.left().value());
    }

    private Either<Boolean, ResponseFormat> validateHighestVersion(User modifier, LifeCycleTransition lifeCycleTransition, Component component, String resourceCurrVersion, ComponentTypeEnum componentType) {
        ResponseFormat errorResponse;
        if (!component.isHighestVersion()) {
            log.debug("Component version {} is not the last version of component {}", component.getComponentMetadataDefinition().getMetadataDataDefinition().getVersion(),
                    component.getComponentMetadataDefinition().getMetadataDataDefinition().getName());
            errorResponse = componentUtils.getResponseFormat(ActionStatus.COMPONENT_HAS_NEWER_VERSION, component.getComponentMetadataDefinition().getMetadataDataDefinition().getName(), componentType.getValue().toLowerCase());
            componentUtils.auditComponentAdmin(errorResponse, modifier, component, lifeCycleTransition.getAuditingAction(), componentType,
                    ResourceVersionInfo.newBuilder()
                            .state(component.getLifecycleState().name())
                            .version(resourceCurrVersion)
                            .build());
            return Either.right(errorResponse);
        }
        return Either.left(true);
    }

    private Boolean lockComponent(ComponentTypeEnum componentType, Component component) {
        NodeTypeEnum nodeType = componentType.getNodeType();
        StorageOperationStatus lockResourceStatus = graphLockOperation.lockComponent(component.getUniqueId(), nodeType);

        if (lockResourceStatus.equals(StorageOperationStatus.OK)) {
            return true;
        } else {
            ActionStatus actionStatus = componentUtils.convertFromStorageResponse(lockResourceStatus);
            throw new ByActionStatusComponentException(actionStatus, component.getComponentMetadataDefinition().getMetadataDataDefinition().getName());
        }
    }

    private Either<String, ResponseFormat> validateComment(LifecycleChangeInfoWithAction changeInfo, LifeCycleTransitionEnum transitionEnum) {
        String comment = changeInfo.getUserRemarks();
        if (LifeCycleTransitionEnum.CERTIFY == transitionEnum || LifeCycleTransitionEnum.CHECKIN == transitionEnum
        // import?
        ) {

            if (!ValidationUtils.validateStringNotEmpty(comment)) {
                log.debug("user comment cannot be empty or null.");
                ResponseFormat errorResponse = componentUtils.getResponseFormat(ActionStatus.MISSING_DATA, COMMENT);
                return Either.right(errorResponse);
            }

            comment = ValidationUtils.removeNoneUtf8Chars(comment);
            comment = ValidationUtils.removeHtmlTags(comment);
            comment = ValidationUtils.normaliseWhitespace(comment);
            comment = ValidationUtils.stripOctets(comment);

            if (!ValidationUtils.validateLength(comment, ValidationUtils.COMMENT_MAX_LENGTH)) {
                log.debug("user comment exceeds limit.");
                return Either.right(componentUtils.getResponseFormat(ActionStatus.EXCEEDS_LIMIT, COMMENT, String.valueOf(ValidationUtils.COMMENT_MAX_LENGTH)));
            }
            if (!ValidationUtils.validateIsEnglish(comment)) {
                return Either.right(componentUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
        }
        return Either.left(comment);
    }

    private ComponentBusinessLogic getComponentBL(ComponentTypeEnum componentTypeEnum) {
        ComponentBusinessLogic businessLogic;
        switch (componentTypeEnum) {
        case RESOURCE:
            businessLogic = this.resourceBusinessLogic;
            break;
        case SERVICE:
            businessLogic = this.serviceBusinessLogic;
            break;
        case PRODUCT:
            businessLogic = this.productBusinessLogic;
            break;
        default:
            throw new IllegalArgumentException("Illegal component type:" + componentTypeEnum.getValue());
        }
        return businessLogic;
    }

    public Either<Component, ResponseFormat> getLatestComponentByUuid(ComponentTypeEnum componentTypeEnum, String uuid) {

        Either<Component, StorageOperationStatus> latestVersionEither = toscaOperationFacade.getLatestComponentByUuid(uuid);

        if (latestVersionEither.isRight()) {

            return Either.right(componentUtils.getResponseFormat(componentUtils.convertFromStorageResponse(latestVersionEither.right().value(), componentTypeEnum), uuid));
        }

        Component latestComponent = latestVersionEither.left().value();

        return Either.left(latestComponent);
    }

    /**
     * Performs Force certification. Note that a Force certification is allowed for the first certification only, as only a state and a version is promoted due a Force certification, skipping other actions required if a previous certified version
     * exists.
     * 
     * @param resource
     * @param user
     * @param lifecycleChangeInfo
     * @param inTransaction
     * @param needLock
     * @return
     */
    public Resource forceResourceCertification(Resource resource, User user, LifecycleChangeInfoWithAction lifecycleChangeInfo, boolean inTransaction, boolean needLock) {
        Resource result = null;
        Either<ToscaElement, StorageOperationStatus> certifyResourceRes = null;
        if (lifecycleChangeInfo.getAction() != LifecycleChanceActionEnum.CREATE_FROM_CSAR) {
            log.debug("Force certification is not allowed for the action {}. ", lifecycleChangeInfo.getAction());
            throw new ByActionStatusComponentException(ActionStatus.NOT_ALLOWED);
        }
        if (!isFirstCertification(resource.getVersion())) {
            log.debug("Failed to perform a force certification of resource{}. Force certification is allowed for the first certification only. ", resource.getName());
            throw new ByActionStatusComponentException(ActionStatus.NOT_ALLOWED);
        }
        // lock resource
        if (!inTransaction && needLock) {
            log.info("lock component {}", resource.getUniqueId());
            lockComponent(resource.getComponentType(), resource);
            log.info("after lock component {}", resource.getUniqueId());
        }
        try {
            certifyResourceRes = lifecycleOperation.forceCerificationOfToscaElement(resource.getUniqueId(), user.getUserId(), user.getUserId(), resource.getVersion());
            if (certifyResourceRes.isRight()) {
                StorageOperationStatus status = certifyResourceRes.right().value();
                log.debug("Failed to perform a force certification of resource {}. The status is {}. ", resource.getName(), status);
                throw new ByResponseFormatComponentException(componentUtils.getResponseFormatByResource(componentUtils.convertFromStorageResponse(status), resource));
            }
            result = ModelConverter.convertFromToscaElement(certifyResourceRes.left().value());
            resource.setMetadataDefinition(result.getComponentMetadataDefinition());
        } finally {
            log.info("unlock component {}", resource.getUniqueId());
            if (!inTransaction) {
                if (result != null) {
                    janusGraphDao.commit();
                } else {
                    janusGraphDao.rollback();
                }
                if (needLock) {
                    NodeTypeEnum nodeType = resource.getComponentType().getNodeType();
                    log.info("During change state, another component {} has been created/updated", resource.getUniqueId());
                    graphLockOperation.unlockComponent(resource.getUniqueId(), nodeType);
                }
            }
        }
        return result;
    }

    public boolean isFirstCertification(String previousVersion) {
        return previousVersion.split("\\.")[0].equals("0");
    }

}
