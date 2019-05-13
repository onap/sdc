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
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.tosca.ToscaUtils;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.log.wrappers.Logger;
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
    private ArtifactsBusinessLogic artifactsManager;
    private NodeTemplateOperation nodeTemplateOperation;

    public CertificationChangeTransition(LifeCycleTransitionEnum name, ComponentsUtils componentUtils, ToscaElementLifecycleOperation lifecycleOperation, ToscaOperationFacade toscaOperationFacade, TitanDao titanDao) {
        super(componentUtils, lifecycleOperation, toscaOperationFacade, titanDao);

        this.name = name;

        // authorized roles
        Role[] certificationChangeRoles = { Role.ADMIN, Role.TESTER };
        Role[] resourceRoles = { Role.ADMIN, Role.TESTER, Role.DESIGNER};
        addAuthorizedRoles(ComponentTypeEnum.RESOURCE, Arrays.asList(resourceRoles));
        addAuthorizedRoles(ComponentTypeEnum.SERVICE, Arrays.asList(certificationChangeRoles));

        //additional authorized roles for resource type
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

        if ( componentType != ComponentTypeEnum.RESOURCE ){
            if (!oldState.equals(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS)  ) {
                log.debug("oldState={} should be={}",oldState,ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION);
                ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_NOT_READY_FOR_CERTIFICATION, componentName, componentType.name().toLowerCase());
                return Either.right(error);
            }
    
            if (oldState.equals(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS) && !modifier.getUserId().equals(owner.getUserId()) && !modifier.getRole().equals(Role.ADMIN.name())) {
                log.debug("oldState={} should not be={}",oldState,ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE);
                log.debug("&& modifier({})!={}  && modifier.role({})!={}", modifier, owner, modifier.getRole(), owner.getRole());
                ResponseFormat error = componentUtils.getResponseFormat(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, componentName, componentType.name().toLowerCase(), owner.getFirstName(), owner.getLastName(), owner.getUserId());
                return Either.right(error);
            }
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
                    titanDao.rollback();
                }
            } else {
                if ( !inTransaction ) {
                    log.debug("operation success. do commit");
                    titanDao.commit();
                }
            }
        }

    }

    private void updateCapReqPropertiesOwnerId(Component component) {
        if(component.isTopologyTemplate() && ToscaUtils.isNotComplexVfc(component)) {
            toscaOperationFacade.updateCapReqPropertiesOwnerId(component.getUniqueId());
        }
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
