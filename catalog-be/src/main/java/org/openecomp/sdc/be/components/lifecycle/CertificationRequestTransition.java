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
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.*;

public class CertificationRequestTransition extends LifeCycleTransition {

    private static final Logger log = Logger.getLogger(CertificationRequestTransition.class);

    private ServiceBusinessLogic serviceBusinessLogic;

    public CertificationRequestTransition(ComponentsUtils componentUtils, ToscaElementLifecycleOperation lifecycleOperation, ServiceBusinessLogic serviceBusinessLogic,
                                          ToscaOperationFacade toscaOperationFacade, TitanDao titanDao) {
        super(componentUtils, lifecycleOperation, toscaOperationFacade, titanDao);

        // authorized roles
        Role[] resourceServiceCheckoutRoles = { Role.ADMIN, Role.DESIGNER };
        addAuthorizedRoles(ComponentTypeEnum.RESOURCE, Arrays.asList(resourceServiceCheckoutRoles));
        addAuthorizedRoles(ComponentTypeEnum.SERVICE, Arrays.asList(resourceServiceCheckoutRoles));
        Role[] resourceRoles = { Role.TESTER};
        addResouceAuthorizedRoles(ResourceTypeEnum.VFCMT, Arrays.asList(resourceRoles));

        this.serviceBusinessLogic = serviceBusinessLogic;
    }

    @Override
    public LifeCycleTransitionEnum getName() {
        return LifeCycleTransitionEnum.CERTIFICATION_REQUEST;
    }

    @Override
    public AuditingActionEnum getAuditingAction() {
        return AuditingActionEnum.CERTIFICATION_REQUEST_RESOURCE;
    }

    Either<Boolean, ResponseFormat> validateAllResourceInstanceCertified(Component component) {
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);

        if (component.isVspArchived()){
            return Either.right(componentUtils.getResponseFormat(ActionStatus.ARCHIVED_ORIGINS_FOUND, component.getComponentType().name(), component.getName()));
        }

        List<ComponentInstance> resourceInstance = component.getComponentInstances();
        if (resourceInstance != null) {

            //Filter components instances with archived origins
            Optional<ComponentInstance> archivedRIOptional = resourceInstance.stream().filter(ComponentInstanceDataDefinition::isOriginArchived).findAny();

            //RIs with archived origins found, return relevant error
            if (archivedRIOptional.isPresent()){
                return Either.right(componentUtils.getResponseFormat(ActionStatus.ARCHIVED_ORIGINS_FOUND, component.getComponentType().name(), component.getName()));
            }

            //Continue with searching for non certified RIs
            Optional<ComponentInstance> nonCertifiedRIOptional = resourceInstance.stream().filter(p -> !ValidationUtils.validateCertifiedVersion(p.getComponentVersion())).findAny();
            // Uncertified Resource Found
            if (nonCertifiedRIOptional.isPresent()) {
                ComponentInstance nonCertifiedRI = nonCertifiedRIOptional.get();
                ResponseFormat resFormat = getRelevantResponseFormatUncertifiedRI(nonCertifiedRI, component.getComponentType());
                eitherResult = Either.right(resFormat);
            }

        }
        return eitherResult;
    }

    private ResponseFormat getRelevantResponseFormatUncertifiedRI(ComponentInstance nonCertifiedRI, ComponentTypeEnum componentType) {

        Either<Resource, StorageOperationStatus> eitherResource = toscaOperationFacade.getToscaElement(nonCertifiedRI.getComponentUid());
        if (eitherResource.isRight()) {
            return componentUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
        }
        ActionStatus actionStatus;
        Resource resource = eitherResource.left().value();
        Either<Resource, StorageOperationStatus> status = toscaOperationFacade.findLastCertifiedToscaElementByUUID(resource);

        if (ValidationUtils.validateMinorVersion(nonCertifiedRI.getComponentVersion())) {
            if (status.isRight() || status.left().value() == null) {
                actionStatus = ActionStatus.VALIDATED_RESOURCE_NOT_FOUND;
            } else {
                actionStatus = ActionStatus.FOUND_ALREADY_VALIDATED_RESOURCE;
            }
        } else {
            if (status.isRight() || status.left().value() == null) {
                actionStatus = ActionStatus.FOUND_LIST_VALIDATED_RESOURCES;
            } else {
                actionStatus = ActionStatus.FOUND_ALREADY_VALIDATED_RESOURCE;
            }
        }
        return componentUtils.getResponseFormat(actionStatus, componentType == ComponentTypeEnum.RESOURCE ? "VF" : "service", resource.getName());
    }

    @Override
    public Either<? extends Component, ResponseFormat> changeState(ComponentTypeEnum componentType, Component component, ComponentBusinessLogic componentBl, User modifier, User owner, boolean shouldLock, boolean inTransaction) {
        log.debug("start performing certification request for resource {}", component.getUniqueId());

        ActionStatus actionStatus;
        ResponseFormat responseFormat;
        Either<? extends Component, ResponseFormat> result = null;
        try{
            if (component.isTopologyTemplate()) {

                Either<Boolean, ResponseFormat> statusCert = validateAllResourceInstanceCertified(component);
                if (statusCert.isRight()) {
                    return Either.right(statusCert.right().value());
                }

                statusCert = validateConfiguredAtomicReqCapSatisfied(component);
                if (statusCert.isRight()) {
                    return Either.right(statusCert.right().value());
                }
            }
            if (componentType == ComponentTypeEnum.SERVICE) {

                Either<Service, ResponseFormat> generateHeatEnvResult = serviceBusinessLogic.generateHeatEnvArtifacts((Service) component, modifier, shouldLock, inTransaction);

                if (generateHeatEnvResult.isRight()) {
                        return Either.right(generateHeatEnvResult.right().value());
                    }
                Either<Service, ResponseFormat> generateVfModuleResult = serviceBusinessLogic.generateVfModuleArtifacts(generateHeatEnvResult.left().value(), modifier, shouldLock, inTransaction);
                if (generateVfModuleResult.isRight()) {
                        return Either.right(generateVfModuleResult.right().value());
                }
                component = generateVfModuleResult.left().value();

            }

            Either<Either<ArtifactDefinition, Operation>, ResponseFormat> eitherPopulated = componentBl.populateToscaArtifacts(component, modifier, true, inTransaction, shouldLock);
            if (eitherPopulated != null && eitherPopulated.isRight()) {
                return Either.right(eitherPopulated.right().value());
            }

            Either<ToscaElement, StorageOperationStatus> certificationRequestResult = lifeCycleOperation.requestCertificationToscaElement(component.getUniqueId(), modifier.getUserId(), owner.getUserId());
            if (certificationRequestResult.isRight()) {
                log.debug("checkout failed on graph");
                StorageOperationStatus response = certificationRequestResult.right().value();
                actionStatus = componentUtils.convertFromStorageResponse(response);

                if (response.equals(StorageOperationStatus.ENTITY_ALREADY_EXISTS)) {
                    actionStatus = ActionStatus.COMPONENT_VERSION_ALREADY_EXIST;
                }
                responseFormat = componentUtils.getResponseFormatByComponent(actionStatus, component, componentType);
                result =  Either.right(responseFormat);
            }
            else {
                result =  Either.left(ModelConverter.convertFromToscaElement(certificationRequestResult.left().value()));
            }
    } finally {
        if (result == null || result.isRight()) {
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Change LifecycleState");
            if (!inTransaction) {
                log.debug("operation failed. do rollback");
                titanDao.rollback();
            }
        } else {
            if (!inTransaction) {
                log.debug("operation success. do commit");
                titanDao.commit();
            }
        }
    }
        return result;
    }

    private Either<Boolean, ResponseFormat> validateConfiguredAtomicReqCapSatisfied(Component component) {
        log.debug("Submit for testing validation - Start validating configured req/cap satisfied for inner atomic instances, component id:{}", component.getUniqueId());
        List<ComponentInstance> componentInstances = component.getComponentInstances();
        if (componentInstances != null) {
            // Prepare relationships data structures
            // Better make it list than set in case we need to count req/cap
            // occurrences in the future
            Map<String, List<String>> reqName2Ids = new HashMap<>();
            Map<String, List<String>> capName2Ids = new HashMap<>();
            Map<String, Set<String>> requirementsToFulfillBeforeCert = configurationManager.getConfiguration().getRequirementsToFulfillBeforeCert();
            Map<String, Set<String>> capabilitiesToConsumeBeforeCert = configurationManager.getConfiguration().getCapabilitiesToConsumeBeforeCert();
            for (ComponentInstance compInst : componentInstances) {
                String compInstId = compInst.getUniqueId();
                OriginTypeEnum originType = compInst.getOriginType();
                if (originType == null) {
                    log.debug("Origin type is not set for component instance {} - it shouldn't happen. Skipping this component instance...", compInst.getUniqueId());
                    continue;
                }
                String compInstType = originType.getValue();
                // Validating configured requirements fulfilled
                if (null != requirementsToFulfillBeforeCert) {
                    Set<String> reqToFulfillForType = requirementsToFulfillBeforeCert.get(compInstType);
                    if (reqToFulfillForType != null) {
                        for (String reqNameToFulfill : reqToFulfillForType) {
                            List<String> reqNameList = reqName2Ids.get(reqNameToFulfill);
                            if (reqNameList == null || !reqNameList.contains(compInstId)) {
                                log.debug("Requirement {} wasn't fulfilled for component instance {} of type {}", reqNameToFulfill, compInstId, compInstType);
                                ComponentTypeEnum componentType = component.getComponentType();
                                String compParam = (componentType == ComponentTypeEnum.RESOURCE) ? "VF" : componentType.getValue().toLowerCase();
                                ResponseFormat responseFormat = componentUtils.getResponseFormat(ActionStatus.REQ_CAP_NOT_SATISFIED_BEFORE_CERTIFICATION, component.getName(), compParam, originType.getDisplayValue(), compInst.getName(), "requirement",
                                        reqNameToFulfill, "fulfilled");
                                return Either.right(responseFormat);
                            }
                        }
                    }
                }
                // Validating configured capabilities consumed
                if (null != capabilitiesToConsumeBeforeCert) {
                    Set<String> capToConsumeForType = capabilitiesToConsumeBeforeCert.get(compInstType);
                    if (capToConsumeForType != null) {
                        for (String capNameToConsume : capToConsumeForType) {
                            List<String> capNameList = capName2Ids.get(capNameToConsume);
                            if (capNameList == null || !capNameList.contains(compInstId)) {
                                log.debug("Capability {} wasn't consumed for component instance {} of type {}", capNameToConsume, compInstId, compInstType);
                                ComponentTypeEnum componentType = component.getComponentType();
                                String compParam = (componentType == ComponentTypeEnum.RESOURCE) ? "VF" : componentType.getValue().toLowerCase();
                                ResponseFormat responseFormat = componentUtils.getResponseFormat(ActionStatus.REQ_CAP_NOT_SATISFIED_BEFORE_CERTIFICATION, component.getName(), compParam, originType.getDisplayValue(), compInst.getName(), "capability",
                                        capNameToConsume, "consumed");
                                return Either.right(responseFormat);
                            }
                        }
                    }
                }
            }
        }
        log.debug("Submit for testing validation - validating configured req/cap satisfied for inner atomic instances finished successfully, component id:{}", component.getUniqueId());
        return Either.left(true);
    }

    @Override
    public Either<Boolean, ResponseFormat> validateBeforeTransition(Component component, ComponentTypeEnum componentType, User modifier, User owner, LifecycleStateEnum oldState, LifecycleChangeInfoWithAction lifecycleChangeInfo) {
        String componentName = component.getComponentMetadataDefinition().getMetadataDataDefinition().getName();
        log.debug("validate before certification request. resource name={}, oldState={}, owner userId={}", componentName, oldState, owner.getUserId());

        // validate user
        Either<Boolean, ResponseFormat> userValidationResponse = userRoleValidation(modifier,component, componentType, lifecycleChangeInfo);
        if (userValidationResponse.isRight()) {
            return userValidationResponse;
        }

        // case of "atomic" checkin and certification request - modifier must be
        // the owner
        if (oldState.equals(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT) && !modifier.equals(owner) && !modifier.getRole().equals(Role.ADMIN.name())) {
            ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_CHECKOUT_BY_ANOTHER_USER, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
            return Either.right(error);
        }

        // other states
        if (oldState.equals(LifecycleStateEnum.CERTIFIED)) {
            ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_ALREADY_CERTIFIED, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
            return Either.right(error);
        }
        if (oldState.equals(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS)) {
            ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
            return Either.right(error);
        }
        if (oldState.equals(LifecycleStateEnum.READY_FOR_CERTIFICATION)) {
            ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_SENT_FOR_CERTIFICATION, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
            return Either.right(error);
        }

        return Either.left(true);
    }
}
