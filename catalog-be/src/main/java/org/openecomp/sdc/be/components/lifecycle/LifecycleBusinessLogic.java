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

import fj.data.Either;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.distribution.engine.ServiceDistributionArtifactsBuilder;
import org.openecomp.sdc.be.components.impl.*;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction.LifecycleChanceActionEnum;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.ICacheMangerOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CapabilityOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Component("lifecycleBusinessLogic")
public class LifecycleBusinessLogic {

    private static final String COMMENT = "comment";

    @Autowired
    private IGraphLockOperation graphLockOperation = null;

    @Autowired
    private ArtifactsBusinessLogic artifactsBusinessLogic;

    @Autowired
    private TitanDao titanDao;

    @Autowired
    private CapabilityOperation capabilityOperation;

    private static final Logger log = Logger.getLogger(LifecycleBusinessLogic.class);

    @javax.annotation.Resource
    private ComponentsUtils componentUtils;

    @javax.annotation.Resource
    private ToscaElementLifecycleOperation lifecycleOperation;
    @javax.annotation.Resource
    ArtifactsBusinessLogic artifactsManager;

    @javax.annotation.Resource
    private ServiceDistributionArtifactsBuilder serviceDistributionArtifactsBuilder;

    @javax.annotation.Resource
    private ServiceBusinessLogic serviceBusinessLogic;

    @javax.annotation.Resource
    private ResourceBusinessLogic resourceBusinessLogic;

    @javax.annotation.Resource
    private ProductBusinessLogic productBusinessLogic;

    @Autowired
    private ToscaExportHandler toscaExportUtils;

    @Autowired
    ICacheMangerOperation cacheManagerOperation;

    @Autowired
    ToscaOperationFacade toscaOperationFacade;
    
    @Autowired
    NodeTemplateOperation nodeTemplateOperation;

    private Map<String, LifeCycleTransition> stateTransitions;
    private static volatile boolean isInitialized = false;

    @PostConstruct
    public void init() {
        // init parameters
        if (!isInitialized) {
            synchronized (this) {
                if (!isInitialized) {
                    initStateOperations();
                    isInitialized = true;
                }
            }
        }
    }

    private void initStateOperations() {
        stateTransitions = new HashMap<>();

        LifeCycleTransition checkoutOp = new CheckoutTransition(componentUtils, lifecycleOperation, toscaOperationFacade, titanDao);
        stateTransitions.put(checkoutOp.getName().name(), checkoutOp);

        UndoCheckoutTransition undoCheckoutOp = new UndoCheckoutTransition(componentUtils, lifecycleOperation, toscaOperationFacade, titanDao);
        undoCheckoutOp.setArtifactsBusinessLogic(artifactsBusinessLogic);
        stateTransitions.put(undoCheckoutOp.getName().name(), undoCheckoutOp);

        LifeCycleTransition checkinOp = new CheckinTransition(componentUtils, lifecycleOperation, toscaOperationFacade, titanDao);
        stateTransitions.put(checkinOp.getName().name(), checkinOp);

        LifeCycleTransition certificationRequest = new CertificationRequestTransition(componentUtils, lifecycleOperation, serviceBusinessLogic, toscaOperationFacade,
                titanDao);
        stateTransitions.put(certificationRequest.getName().name(), certificationRequest);

        LifeCycleTransition startCertification = new StartCertificationTransition(componentUtils, lifecycleOperation, toscaOperationFacade, titanDao);
        stateTransitions.put(startCertification.getName().name(), startCertification);

        LifeCycleTransition failCertification = new CertificationChangeTransition(LifeCycleTransitionEnum.FAIL_CERTIFICATION, componentUtils, lifecycleOperation, toscaOperationFacade, titanDao);
        stateTransitions.put(failCertification.getName().name(), failCertification);

        LifeCycleTransition cancelCertification = new CertificationChangeTransition(LifeCycleTransitionEnum.CANCEL_CERTIFICATION, componentUtils, lifecycleOperation, toscaOperationFacade, titanDao);
        stateTransitions.put(cancelCertification.getName().name(), cancelCertification);

        CertificationChangeTransition successCertification = new CertificationChangeTransition(LifeCycleTransitionEnum.CERTIFY, componentUtils, lifecycleOperation, toscaOperationFacade, titanDao);
        successCertification.setArtifactsManager(artifactsBusinessLogic);
        successCertification.setNodeTemplateOperation(nodeTemplateOperation);
        stateTransitions.put(successCertification.getName().name(), successCertification);
    }

    public LifeCycleTransition getLifecycleTransition(LifeCycleTransitionEnum transitionEnum) {
        return stateTransitions.get(transitionEnum.name());
    }

    public Either<Service, ResponseFormat> changeServiceState(String serviceId, User modifier, LifeCycleTransitionEnum transitionEnum, LifecycleChangeInfoWithAction changeInfo, boolean inTransaction, boolean needLock) {
        return (Either<Service, ResponseFormat>) changeComponentState(ComponentTypeEnum.SERVICE, serviceId, modifier, transitionEnum, changeInfo, inTransaction, needLock);
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
        Component component = null;
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
            Either<Boolean, ResponseFormat> eitherLockResource = lockComponent(componentType, component);
            if (eitherLockResource.isRight()) {
                errorResponse = eitherLockResource.right().value();
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
            if (componentType == ComponentTypeEnum.RESOURCE) {
                Either<? extends Component, ResponseFormat> changeResourceResponse = changeResourceState(componentType, modifier, transitionEnum, changeInfo, true, component);
                if (changeResourceResponse.isRight()) {
                    return changeResourceResponse;
                }
                component = changeResourceResponse.left().value();
            }
            return changeState(component, lifeCycleTransition, componentType, modifier, changeInfo, inTransaction);
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

    /*
     * special case for certification of VFCMT - VFCMT can be certified by Designer or Tester right after checkin in case the operation "submit for test" / "start testing" is done to "VFCMT" - please return error 400
     */
    private Either<? extends Component, ResponseFormat> changeResourceState(ComponentTypeEnum componentType, User modifier, LifeCycleTransitionEnum transitionEnum, LifecycleChangeInfoWithAction changeInfo, boolean inTransaction,
            Component component) {
        LifecycleStateEnum oldState = component.getLifecycleState();
        Component updatedComponent = component;
        if (transitionEnum == LifeCycleTransitionEnum.START_CERTIFICATION || transitionEnum == LifeCycleTransitionEnum.CERTIFICATION_REQUEST) {
            //for VFCMT use old error for backward comp. 
            ActionStatus status = isComponentVFCMT(component, componentType) ? ActionStatus.RESOURCE_VFCMT_LIFECYCLE_STATE_NOT_VALID : ActionStatus.RESOURCE_LIFECYCLE_STATE_NOT_VALID;
            return Either.right(componentUtils.getResponseFormat(status, transitionEnum.getDisplayName()));
        } // certify is done directly from checkin
        else if (transitionEnum == LifeCycleTransitionEnum.CERTIFY) {
            log.debug("Certification request for resource {} ", component.getUniqueId());
            if (oldState == LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT) {
                log.debug("Resource {} is in Checkout state perform checkin", component.getUniqueId());
                Either<? extends Component, ResponseFormat> actionResponse = changeState(component, stateTransitions.get(LifeCycleTransitionEnum.CHECKIN.name()), componentType, modifier, changeInfo, inTransaction);
                if (actionResponse.isRight()) {
                    log.debug("Failed to check in Resource {} error {}", component.getUniqueId(), actionResponse.right().value());
                    return actionResponse;
                }
                updatedComponent = actionResponse.left().value();
                oldState = updatedComponent.getLifecycleState();
            }
            if (oldState == LifecycleStateEnum.NOT_CERTIFIED_CHECKIN) {
                // we will call for submit for testing first and then for certify
                Either<? extends Component, ResponseFormat> actionResponse = changeState(updatedComponent, stateTransitions.get(LifeCycleTransitionEnum.CERTIFICATION_REQUEST.name()), componentType, modifier, changeInfo, inTransaction);
                if (actionResponse.isRight()) {
                    return actionResponse;
                }
                updatedComponent = actionResponse.left().value();
                actionResponse = changeState(updatedComponent, stateTransitions.get(LifeCycleTransitionEnum.START_CERTIFICATION.name()), componentType, modifier, changeInfo, inTransaction);
                if (actionResponse.isRight()) {
                    return actionResponse;
                }
                updatedComponent = actionResponse.left().value();
            }
            if(oldState == LifecycleStateEnum.CERTIFIED){
                failOnAlreadyCertifiedResource(component);
            }
        }
        return Either.left(updatedComponent);
    }

    private void failOnAlreadyCertifiedResource(Component component) {
        String firstName = null;
        String lastName = null;
        if(StringUtils.isNotEmpty(component.getLastUpdaterFullName())){
            String[] fullName = component.getLastUpdaterFullName().split(" ");
            if(fullName.length == 2){
                firstName = fullName[0];
                lastName = fullName[1];
            }
        }
        throw new ByActionStatusComponentException(ActionStatus.COMPONENT_ALREADY_CERTIFIED,
                component.getName(),
                component.getComponentType().name().toLowerCase(),
                firstName,
                lastName,
                component.getLastUpdaterUserId());
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
        Component resourceAfterOperation = operationResult.left().value();
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

    private Either<Boolean, ResponseFormat> lockComponent(ComponentTypeEnum componentType, Component component) {
        NodeTypeEnum nodeType = componentType.getNodeType();
        StorageOperationStatus lockResourceStatus = graphLockOperation.lockComponent(component.getUniqueId(), nodeType);

        if (lockResourceStatus.equals(StorageOperationStatus.OK)) {
            return Either.left(true);
        } else {
            ActionStatus actionStatus = componentUtils.convertFromStorageResponse(lockResourceStatus);
            ResponseFormat responseFormat = componentUtils.getResponseFormat(actionStatus, component.getComponentMetadataDefinition().getMetadataDataDefinition().getName());
            return Either.right(responseFormat);
        }

    }

    private Either<String, ResponseFormat> validateComment(LifecycleChangeInfoWithAction changeInfo, LifeCycleTransitionEnum transitionEnum) {
        String comment = changeInfo.getUserRemarks();
        if (LifeCycleTransitionEnum.CANCEL_CERTIFICATION == transitionEnum || LifeCycleTransitionEnum.CERTIFY == transitionEnum || LifeCycleTransitionEnum.FAIL_CERTIFICATION == transitionEnum || LifeCycleTransitionEnum.CHECKIN == transitionEnum
                || LifeCycleTransitionEnum.CERTIFICATION_REQUEST == transitionEnum
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
    public Either<Resource, ResponseFormat> forceResourceCertification(Resource resource, User user, LifecycleChangeInfoWithAction lifecycleChangeInfo, boolean inTransaction, boolean needLock) {
        Either<Resource, ResponseFormat> result = null;
        Either<ToscaElement, StorageOperationStatus> certifyResourceRes = null;
        if (lifecycleChangeInfo.getAction() != LifecycleChanceActionEnum.CREATE_FROM_CSAR) {
            log.debug("Force certification is not allowed for the action {}. ", lifecycleChangeInfo.getAction());
            result = Either.right(componentUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
        }
        if (!isFirstCertification(resource.getVersion())) {
            log.debug("Failed to perform a force certification of resource{}. Force certification is allowed for the first certification only. ", resource.getName());
            result = Either.right(componentUtils.getResponseFormat(ActionStatus.NOT_ALLOWED));
        }
        // lock resource
        if (result == null && !inTransaction && needLock) {
            log.info("lock component {}", resource.getUniqueId());
            Either<Boolean, ResponseFormat> eitherLockResource = lockComponent(resource.getComponentType(), resource);
            if (eitherLockResource.isRight()) {
                log.error("lock component {} failed", resource.getUniqueId());
                result = Either.right(eitherLockResource.right().value());
            }
            log.info("after lock component {}", resource.getUniqueId());
        }
        try {
            if (result == null) {
                certifyResourceRes = lifecycleOperation.forceCerificationOfToscaElement(resource.getUniqueId(), user.getUserId(), user.getUserId(), resource.getVersion());
                if (certifyResourceRes.isRight()) {
                    StorageOperationStatus status = certifyResourceRes.right().value();
                    log.debug("Failed to perform a force certification of resource {}. The status is {}. ", resource.getName(), status);
                    result = Either.right(componentUtils.getResponseFormatByResource(componentUtils.convertFromStorageResponse(status), resource));
                }
            }
            if (result == null) {
                result = Either.left(ModelConverter.convertFromToscaElement(certifyResourceRes.left().value()));
            }
        } finally {
            log.info("unlock component {}", resource.getUniqueId());
            if (!inTransaction) {
                if (result.isLeft()) {
                    titanDao.commit();
                } else {
                    titanDao.rollback();
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
