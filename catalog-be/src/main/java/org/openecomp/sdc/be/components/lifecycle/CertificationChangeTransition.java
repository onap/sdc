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
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.tosca.ToscaUtils;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CertificationChangeTransition extends LifeCycleTransition {
    private static final  String ALLOTTED_CATEGORY = "Allotted Resource";
    private static final  String DEPENDING_SRV_NAME = "depending_service_name";
    private static final  String PROVIDING_SRV_NAME = "providing_service_name";
    private static final  String PROVIDING_SRV_UUID = "providing_service_uuid";
    private static final  String DEPENDING_SRV_UUID = "depending_service_uuid";

    private static final Logger log = Logger.getLogger(CertificationChangeTransition.class);

    private LifecycleStateEnum nextState;
    private LifeCycleTransitionEnum name;
    private AuditingActionEnum auditingAction;
    private NodeTemplateOperation nodeTemplateOperation;
    private ServiceBusinessLogic serviceBusinessLogic;

    public CertificationChangeTransition(ServiceBusinessLogic serviceBusinessLogic, LifeCycleTransitionEnum name, ComponentsUtils componentUtils, ToscaElementLifecycleOperation lifecycleOperation, ToscaOperationFacade toscaOperationFacade, JanusGraphDao janusGraphDao) {
        super(componentUtils, lifecycleOperation, toscaOperationFacade, janusGraphDao);

        this.name = name;
        this.serviceBusinessLogic = serviceBusinessLogic;

        // authorized roles
        Role[] certificationChangeRoles = { Role.ADMIN, Role.DESIGNER };
        Role[] resourceRoles = { Role.ADMIN, Role.DESIGNER};
        addAuthorizedRoles(ComponentTypeEnum.RESOURCE, Arrays.asList(resourceRoles));
        addAuthorizedRoles(ComponentTypeEnum.SERVICE, Arrays.asList(certificationChangeRoles));

        this.auditingAction = AuditingActionEnum.CERTIFICATION_SUCCESS_RESOURCE;
        this.nextState = LifecycleStateEnum.CERTIFIED;
    }

    @Override
    public LifeCycleTransitionEnum getName() {
        return name;
    }

    @Override
    public AuditingActionEnum getAuditingAction() {
        return auditingAction;
    }

    public NodeTemplateOperation getNodeTemplateOperation() {
        return nodeTemplateOperation;
    }

    public void setNodeTemplateOperation(NodeTemplateOperation nodeTemplateOperation) {
        this.nodeTemplateOperation = nodeTemplateOperation;
    }

    private ResponseFormat formatCertificationError(Component component, StorageOperationStatus response, ComponentTypeEnum componentType) {
        BeEcompErrorManager.getInstance().logBeDaoSystemError("Change LifecycleState - Certify failed on graph");
        log.debug("certification change failed on graph");

        return componentUtils.getResponseFormatByComponent(componentUtils.convertFromStorageResponse(response), component, componentType);
    }

    @Override
    public Either<Boolean, ResponseFormat> validateBeforeTransition(Component component, ComponentTypeEnum componentType, User modifier, User owner, LifecycleStateEnum oldState, LifecycleChangeInfoWithAction lifecycleChangeInfo) {
        String componentName = component.getComponentMetadataDefinition().getMetadataDataDefinition().getName();
        log.info("validate before certification change. resource name={}, oldState={}, owner userId={}", componentName, oldState, owner.getUserId());

        // validate user
        Either<Boolean, ResponseFormat> userValidationResponse = userRoleValidation(modifier,component, componentType, lifecycleChangeInfo);
        if (userValidationResponse.isRight()) {
            log.debug("userRoleValidation failed");
            return userValidationResponse;
        }

        if (oldState != LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT && oldState != LifecycleStateEnum.NOT_CERTIFIED_CHECKIN) {
            log.debug("Valid states for certification are NOT_CERTIFIED_CHECKIN and NOT_CERTIFIED_CHECKOUT. {} is invalid state", oldState);
            ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.ILLEGAL_COMPONENT_STATE, componentName, componentType.name().toLowerCase(), oldState.name());
            return Either.right(error);
        }
        return Either.left(true);
    }

    @Override
    public Either<? extends Component, ResponseFormat> changeState(ComponentTypeEnum componentType, Component component, ComponentBusinessLogic componentBl, User modifier, User owner, boolean shouldLock, boolean inTransaction) {

        log.info("start performing certification change for resource {}", component.getUniqueId());
        Either<? extends Component, ResponseFormat> result = null;

        try {
            handleValidationsAndArtifactsGenerationBeforeCertifying(componentType, component, componentBl, modifier, shouldLock, inTransaction);
            Either<ToscaElement, StorageOperationStatus> certificationChangeResult =
                    lifeCycleOperation.certifyToscaElement(component.getUniqueId(), modifier.getUserId(), owner.getUserId());

            if (certificationChangeResult.isRight()) {
                ResponseFormat responseFormat = formatCertificationError(component, certificationChangeResult.right().value(), componentType);
                result = Either.right(responseFormat);
                return result;
            }

            ToscaElement certificationResult = certificationChangeResult.left().value();
            Component componentAfterCertification = ModelConverter.convertFromToscaElement(certificationResult);
            if ( result == null || result.isLeft() ){
                //update edges for allotted resource 
                StorageOperationStatus status = handleConnectionsForAllotted(componentAfterCertification);
                if ( status != StorageOperationStatus.OK){
                    ResponseFormat responseFormat = formatCertificationError(componentAfterCertification, status, componentType);
                    result = Either.right(responseFormat);
                }
            }
            updateCalculatedCapabilitiesRequirements(componentAfterCertification);
            updateCapReqPropertiesOwnerId(componentAfterCertification);
            result = Either.left(componentAfterCertification);
            return result;
        } finally {
            if (result == null || result.isRight()) {
                BeEcompErrorManager.getInstance().logBeDaoSystemError("Change LifecycleState");
                if ( !inTransaction ) {
                    log.debug("operation failed. do rollback");
                    janusGraphDao.rollback();
                }
            } else {
                if ( !inTransaction ) {
                    log.debug("operation success. do commit");
                    janusGraphDao.commit();
                }
            }
        }

    }

    private void updateCapReqPropertiesOwnerId(Component component) {
        if(component.isTopologyTemplate() && ToscaUtils.isNotComplexVfc(component)) {
            toscaOperationFacade.updateCapReqPropertiesOwnerId(component.getUniqueId());
        }
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

    private void handleValidationsAndArtifactsGenerationBeforeCertifying(ComponentTypeEnum componentType, Component component, ComponentBusinessLogic componentBl, User modifier, boolean shouldLock, boolean inTransaction) {
        if (component.isTopologyTemplate()) {
            Either<Boolean, ResponseFormat> statusCert = validateAllResourceInstanceCertified(component);
            if (statusCert.isRight()) {
                throw new ByResponseFormatComponentException(statusCert.right().value());
            }
        }
        if (componentType == ComponentTypeEnum.SERVICE) {

            Either<Service, ResponseFormat> generateHeatEnvResult = serviceBusinessLogic.generateHeatEnvArtifacts((Service) component, modifier, shouldLock, inTransaction);

            if (generateHeatEnvResult.isRight()) {
                throw new ByResponseFormatComponentException(generateHeatEnvResult.right().value());
            }
            Either<Service, ResponseFormat> generateVfModuleResult = serviceBusinessLogic.generateVfModuleArtifacts(generateHeatEnvResult.left().value(), modifier, shouldLock, inTransaction);
            if (generateVfModuleResult.isRight()) {
                throw new ByResponseFormatComponentException(generateVfModuleResult.right().value());
            }
            component = generateVfModuleResult.left().value();
        }

        componentBl.populateToscaArtifacts(component, modifier, true, inTransaction, shouldLock);
    }


    private void updateCalculatedCapabilitiesRequirements(Component certifiedComponent) {
        if(certifiedComponent.getComponentType() == ComponentTypeEnum.SERVICE){
            toscaOperationFacade.updateNamesOfCalculatedCapabilitiesRequirements(certifiedComponent.getUniqueId());
        }
    }

    private StorageOperationStatus handleConnectionsForAllotted(Component component){
        StorageOperationStatus status = StorageOperationStatus.OK;
        if (component.getComponentType() == ComponentTypeEnum.RESOURCE && component.isTopologyTemplate()  ){
            List<CategoryDefinition> categories = component.getCategories();
            Optional<CategoryDefinition> findFirst = categories.stream().filter(c->c.getName().equals(ALLOTTED_CATEGORY)).findFirst();
            if ( findFirst.isPresent() ){
                findInstanceByAllottedProperties(component);
            }else{
                log.debug("Component isn't from allotted category.");
            }
        }
        return status;
    }

    private void findInstanceByAllottedProperties(Component component) {
        log.debug("Component is from alloted category. Remove all previous ALLOTTED_OF connections for all instances");
        nodeTemplateOperation.removeAllAllotedEdges(component.getUniqueId());
        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = component.getComponentInstancesProperties();
        if ( componentInstancesProperties != null ){
            componentInstancesProperties.entrySet().forEach(e->{
                List<ComponentInstanceProperty> props = e.getValue();
                Optional<ComponentInstanceProperty> findProp = props.stream().filter(p -> p.getName().equals(DEPENDING_SRV_NAME) ||  p.getName().equals(PROVIDING_SRV_NAME)).findFirst();
                if ( findProp.isPresent() ){
                    log.debug("Find specific properties [{} or {}]on instance {} ", DEPENDING_SRV_NAME,PROVIDING_SRV_NAME, e.getKey()  );
                    handleAllotedInstance(component.getUniqueId(), e.getKey(), e.getValue() );
                }else{
                    log.debug("Not defined specific properties [{} or {}]on instance {} ", DEPENDING_SRV_NAME,PROVIDING_SRV_NAME, e.getKey()  );
                }
            });
        }
    }

    private StorageOperationStatus handleAllotedInstance(String componentId, String instanceId, List<ComponentInstanceProperty> props) {
        ComponentInstanceProperty serviceUUIDProp = props.stream().filter(p -> p.getName().equals(PROVIDING_SRV_UUID) ||  p.getName().equals(DEPENDING_SRV_UUID)).findFirst().get();
        if ( serviceUUIDProp.getValue() != null && !serviceUUIDProp.getValue().contains("get_input")){
            log.debug("Handle Allotted edge on instance {} for service UUID {} ", instanceId,  serviceUUIDProp.getValue() );
            return  nodeTemplateOperation.createAllottedOfEdge(componentId, instanceId, serviceUUIDProp.getValue());
        }else{
            log.debug("An incorrectly defined service UUID for Allotted instance {} . Skip instance", instanceId,  serviceUUIDProp.getValue() );
            return StorageOperationStatus.OK;
        }
    }
}
