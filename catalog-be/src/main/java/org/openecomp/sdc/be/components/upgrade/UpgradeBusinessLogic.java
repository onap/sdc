package org.openecomp.sdc.be.components.upgrade;

import fj.data.Either;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.UpgradeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component("upgradeBusinessLogic")
public class UpgradeBusinessLogic {

    private final LifecycleBusinessLogic lifecycleBusinessLogic;
    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private final UserValidations userValidations;
    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentsUtils;
    private final UpgradeOperation upgradeOperation;
    private final JanusGraphDao janusGraphDao;
    private LifecycleChangeInfoWithAction changeInfo = new LifecycleChangeInfoWithAction("automated upgrade");

    private static final List<String> UUID_PROPS_NAMES = Arrays.asList("depending_service_uuid", "providing_service_uuid");
    private static final List<String> INV_UUID_PROPS_NAMES = Arrays.asList("depending_service_invariant_uuid", "providing_service_invariant_uuid");
    private static final List<String> NAME_PROPS_NAMES = Arrays.asList("depending_service_name", "providing_service_name");

    private static final Logger LOGGER = Logger.getLogger(UpgradeBusinessLogic.class);

    public UpgradeBusinessLogic(LifecycleBusinessLogic lifecycleBusinessLogic, ComponentInstanceBusinessLogic componentInstanceBusinessLogic, UserValidations userValidations, ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils,
                                UpgradeOperation upgradeOperation, JanusGraphDao janusGraphDao) {
        this.lifecycleBusinessLogic = lifecycleBusinessLogic;
        this.componentInstanceBusinessLogic = componentInstanceBusinessLogic;
        this.userValidations = userValidations;
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
        this.upgradeOperation = upgradeOperation;
        this.janusGraphDao = janusGraphDao;
    }


    /**
     * 
     * @param componentId
     * @param userId
     * @return
     */
    public UpgradeStatus automatedUpgrade(String componentId, List<UpgradeRequest> upgradeRequest, String userId) {
        UpgradeStatus status = new UpgradeStatus();
        User user = userValidations.validateUserExists(userId, "automated upgrade", false);

        Either<Component, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaFullElement(componentId);
        if (storageStatus.isRight()) {
            status.setError(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(storageStatus.right().value()), componentId));
            return status;
        }
        Component component = storageStatus.left().value();
        if (!component.isHighestVersion() || component.getLifecycleState() != LifecycleStateEnum.CERTIFIED) {
            LOGGER.debug("automated Upgrade failed - target is not higest certified component {} state {} version {} ", component.getName(), component.getLifecycleState(), component.getVersion());
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_IS_NOT_HIHGEST_CERTIFIED, component.getName());
            status.setError(responseFormat);
            componentsUtils.auditComponentAdmin(responseFormat, user, component, getAuditTypeByComponent(component), component.getComponentType());

            return status;
        }
        if ( component.isArchived() ){
            LOGGER.debug("automated Upgrade failed - target is archived component {}  version {} ", component.getName(), component.getVersion());
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_IS_ARCHIVED, component.getName());
            status.setError(responseFormat);
            componentsUtils.auditComponentAdmin(responseFormat, user, component, getAuditTypeByComponent(component), component.getComponentType());

            return status;
        }
        switch (component.getComponentType()) {
        case RESOURCE:
            hadnleUpgradeVFInService(component, upgradeRequest, user, status);
            break;
        case SERVICE:
            hadnleUpgradeService(component, upgradeRequest, user, status);
            break;
        default:
            LOGGER.debug("automated Upgrade failed - Not supported type {} for component {} ", component.getComponentType(), component.getName());
            status.setError(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_ERROR));
        }
        return status;
    }

    /**
     * 
     * @param componentId
     * @param userId
     * @return
     */
    public Either<List<ComponentDependency>, ResponseFormat> getComponentDependencies(String componentId, String userId) {

        User user = userValidations.validateUserExists(userId, "get Component Dependencies for automated upgrade", false);
        try {
            return upgradeOperation.getComponentDependencies(componentId)
                    .right()
                    .map(rf -> componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(rf)));
        } finally {
            // all operation were read only. no commit needed
            janusGraphDao.rollback();
        }

    }

    private UpgradeStatus hadnleUpgradeVFInService(Component component, List<UpgradeRequest> componentUids, User user, UpgradeStatus upgradeStatus) {
        Resource vfResource = (Resource) component;
        if (vfResource.getResourceType() != ResourceTypeEnum.VF) {
            LOGGER.debug("automated Upgrade failed - target is not VF resource {} {} ", vfResource.getName(), vfResource.getResourceType());
            upgradeStatus.setStatus(ActionStatus.GENERAL_ERROR);
            componentsUtils.auditComponentAdmin(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR), user, component, getAuditTypeByComponent(component), component.getComponentType());
            return upgradeStatus;
        }
        componentUids.forEach(request -> upgradeInSingleService(request, vfResource, user, upgradeStatus));
        upgradeStatus.setStatus(ActionStatus.OK);
        componentsUtils.auditComponentAdmin(componentsUtils.getResponseFormat(ActionStatus.OK), user, component, AuditingActionEnum.VF_UPGRADE_SERVICES, component.getComponentType());

        return upgradeStatus;
    }

    private UpgradeStatus hadnleUpgradeService(Component component, List<UpgradeRequest> upgradeRequest, User user, UpgradeStatus upgradeStatus) {
        if ( Role.TESTER.name().equals(user.getRole()) ){
            user.setRole(Role.DESIGNER.name());
            LOGGER.debug("Change temporary for update service reference user role from TESTER to DESINGER");
        }
        Service service = (Service) component;
        upgradeRequest.forEach(request -> upgradeSingleService(request, service, user, upgradeStatus));
        upgradeStatus.setStatus(ActionStatus.OK);
        componentsUtils.auditComponentAdmin(componentsUtils.getResponseFormat(ActionStatus.OK), user, component, AuditingActionEnum.UPDATE_SERVICE_REFERENCE, component.getComponentType());
       return upgradeStatus;
    }

    private ActionStatus upgradeSingleService(UpgradeRequest request, Service service, User user, UpgradeStatus upgradeStatus) {
        if (request.getResourceId() == null) {
            // upgrade proxy version
            return upgradeInSingleService(request, service, user, upgradeStatus);
        } else {
            // upgrade allotted resource -> service
            return upgradeChainResourceService(request, service, user, upgradeStatus);
        }
    }

    private ActionStatus upgradeInSingleService(UpgradeRequest request, Component newVersionComponent, User user, UpgradeStatus upgradeStatus) {
        String serviceId = request.getServiceId();
        return toscaOperationFacade.getToscaFullElement(serviceId)
                .either(l -> handleService(l, newVersionComponent, user, upgradeStatus), err -> {
            LOGGER.debug("Failed to fetch service by id {} error {}", serviceId, err);
            ActionStatus errS = componentsUtils.convertFromStorageResponse(err);
            upgradeStatus.addServiceStatus(serviceId, errS);
            return errS;
        });
    }

    private ActionStatus upgradeChainResourceService(UpgradeRequest request, Service service, User user, UpgradeStatus upgradeStatus) {
        Component resource;
        Either<? extends Component, ActionStatus> upgradeAllottedResource = upgradeAllottedResource(request, user, upgradeStatus, service);
        if (upgradeAllottedResource.isRight()) {
            return upgradeAllottedResource.right().value();
        }

        resource = upgradeAllottedResource.left().value();
        // update VF instance in service

        Either<Component, StorageOperationStatus> serviceContainer = toscaOperationFacade.getToscaFullElement(request.getServiceId());
        if (serviceContainer.isRight()) {
            LOGGER.debug("Failed to fetch resource by id {} error {}", request.getServiceId(), serviceContainer.right().value());
            ActionStatus errS = componentsUtils.convertFromStorageResponse(serviceContainer.right().value());
            upgradeStatus.addServiceStatus(request.getServiceId(), errS);
            return errS;
        }
        return handleService(serviceContainer.left().value(), resource, user, upgradeStatus);

    }

    private Either<? extends Component, ActionStatus> upgradeAllottedResource(UpgradeRequest request, User user, UpgradeStatus upgradeStatus, Service service) {
        return getElement(request.getResourceId(), upgradeStatus, request)
                .left()
                .bind(l -> upgradeStateAlloted(request, user, upgradeStatus, service, l));
    }

    private Either<Component, ActionStatus> getElement(String id, UpgradeStatus upgradeStatus, UpgradeRequest request) {
        return toscaOperationFacade.getToscaElement(id)
                .right()
                .map(err -> {
            ActionStatus errS = componentsUtils.convertFromStorageResponse(err);
            upgradeStatus.addServiceStatus(request.getServiceId(), errS);
            return errS;
        });
    }

    private Either<? extends Component, ActionStatus> upgradeStateAlloted(UpgradeRequest request, User user, UpgradeStatus upgradeStatus, Service service, Component resource) {
        if (resource.getLifecycleState() == LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT) {
            LOGGER.debug("Automated upgrade failedd. Alloted vf {} is in state NOT_CERTIFIED_CHECKOUT", request.getResourceId());
            upgradeStatus.addServiceStatus(request.getServiceId(), ActionStatus.RESOURCE_LIFECYCLE_STATE_NOT_VALID);
            return Either.right(ActionStatus.RESOURCE_LIFECYCLE_STATE_NOT_VALID);
        }
        // check out VF
        // update properties-reference to service in VF on VFCI
        return changeComponentState(resource, LifeCycleTransitionEnum.CHECKOUT, user,  upgradeStatus, request.getServiceId())
                .left()
                .bind(l -> updateAllottedPropsAndCertify(request, user, upgradeStatus, service, l));
    }

    private Either<? extends Component, ActionStatus> updateAllottedPropsAndCertify(UpgradeRequest request, User user, UpgradeStatus upgradeStatus, Service service, Component resource) {
        Either<? extends Component, ActionStatus> result = null;
        try {
            List<String> instanceIds = upgradeOperation.getInstanceIdFromAllottedEdge(resource.getUniqueId(), service.getInvariantUUID());
            if (instanceIds != null) {
                Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = resource.getComponentInstancesProperties();
                Map<String, List<ComponentInstanceProperty>> propertiesToUpdate = new HashMap<>();

                instanceIds.forEach(id -> findPropertiesToUpdate(id, componentInstancesProperties, propertiesToUpdate, service));

                Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> updatePropsResult = toscaOperationFacade.updateComponentInstancePropsToComponent(propertiesToUpdate, resource.getUniqueId());
                if (updatePropsResult.isRight()) {
                    LOGGER.debug("Failed to update properties in  Allotted resource {} {}, Error {}. ", resource.getName(), resource.getUniqueId(), updatePropsResult.right().value());

                    result = Either.right(ActionStatus.GENERAL_ERROR);
                    return result;
                }

                // certify VF
                result =  changeComponentState(resource, LifeCycleTransitionEnum.CERTIFY, user,  upgradeStatus, request.getServiceId());
            } else {
                // nothing to update
                LOGGER.debug("No Instances to update in allotted resource {} ", resource.getName());
                result = Either.right(ActionStatus.NO_INSTANCES_TO_UPGRADE);
            }
            return result;
        } finally  {
            if ( result.isRight() ){
                // undo checkout resource in case of failure
                LOGGER.debug("Failed to update Allotted resource {} {}, Error {}. UNDOCHEKOUT our resource", resource.getName(), resource.getUniqueId(), result.right().value());
                       
                upgradeStatus.addServiceStatus(request.getServiceId(), ActionStatus.GENERAL_ERROR);
            }
        }
    }

    private void undocheckoutComponent(User user, Component resource) {
        Either<? extends Component, ResponseFormat> changeComponentState = lifecycleBusinessLogic.changeComponentState(resource.getComponentType(), resource.getUniqueId(), user, LifeCycleTransitionEnum.UNDO_CHECKOUT, changeInfo, false, true);
        if (changeComponentState.isRight()) {
            LOGGER.debug("Failed to UNDOCHECKOUT Service {} {}, Error {}", resource.getName(), resource.getUniqueId(), changeComponentState.right().value());
        }
    }

    private void findPropertiesToUpdate(String id, Map<String, List<ComponentInstanceProperty>> componentInstancesProperties, Map<String, List<ComponentInstanceProperty>> propertiesToUpdate, Service service) {
        List<ComponentInstanceProperty> list = componentInstancesProperties.get(id);
        List<ComponentInstanceProperty> propsPerInstance = new ArrayList<>();
        list.forEach(p -> {
            if (UUID_PROPS_NAMES.contains(p.getName())) {
                p.setValue(service.getUUID());
                propsPerInstance.add(p);
            }
            if (INV_UUID_PROPS_NAMES.contains(p.getName())) {
                p.setValue(service.getInvariantUUID());
                propsPerInstance.add(p);
            }
            if (NAME_PROPS_NAMES.contains(p.getName())) {
                p.setValue(service.getName());
                propsPerInstance.add(p);
            }
        });
        propertiesToUpdate.put(id, propsPerInstance);
    }

    private ActionStatus handleService(Component component, Component newVersionComponent, User user, UpgradeStatus upgradeStatus) {
        if (component.getComponentType() != ComponentTypeEnum.SERVICE) {
            LOGGER.debug("component with id  {} and name {} isn't SERVICE.  type{} ", component.getName(), component.getUniqueId(), component.getComponentType());
            upgradeStatus.addServiceStatus(component, ActionStatus.GENERAL_ERROR);
            return ActionStatus.GENERAL_ERROR;
        }
        
        Service service = (Service) component;
        if (component.getLifecycleState() != LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT) {
            LOGGER.debug("Service {} {} is not in CHECKOUT state . Try to checkout it", component.getName(), component.getUniqueId());
            Either<? extends Component, ActionStatus> changeComponentState = changeComponentState(component, LifeCycleTransitionEnum.CHECKOUT, user,  upgradeStatus, null);
            if ( changeComponentState.isRight() ){
                return changeComponentState.right().value();
            }
            service = (Service) changeComponentState.left().value();
            //need to fetch service again with capability properties
            Either<Component, StorageOperationStatus> toscaFullElement = toscaOperationFacade.getToscaFullElement(service.getUniqueId());
            if ( toscaFullElement.isRight() ){
                return componentsUtils.convertFromStorageResponse(toscaFullElement.right().value());
            }
            service = (Service) toscaFullElement.left().value();
        }else{
            LOGGER.debug("Service {} {} is  in CHECKOUT state . Restricted update operation", component.getName(), component.getUniqueId());
            upgradeStatus.addServiceStatus(component, ActionStatus.COMPONENT_IN_CHECKOUT_STATE);
            return ActionStatus.COMPONENT_IN_CHECKOUT_STATE;          
        }
        ActionStatus status = ActionStatus.GENERAL_ERROR;
        try {
            status = handleInstances(newVersionComponent, user, upgradeStatus, service);
        } finally {
            if (status != ActionStatus.OK) {
                LOGGER.debug("Failed to upgrade instance for service {} status {}. Undocheckout service", service.getName(), status);
                undocheckoutComponent(user, service);

                upgradeStatus.addServiceStatus(component, status);
            }
        }
        return status;
    }
    
    private Either<? extends Component,ActionStatus> changeComponentState(Component component, LifeCycleTransitionEnum nextState, User user, UpgradeStatus upgradeStatus, String idForStatus){
        if ( component.isArchived() ){
            LOGGER.debug("Component  {} from type {} id {} is archived, Error {}", nextState, component.getName(), component.getComponentType(), component.getUniqueId());
            setUpgradeStatus(component, upgradeStatus, idForStatus);
            return Either.right(ActionStatus.COMPONENT_IS_ARCHIVED);
        }
        return lifecycleBusinessLogic.changeComponentState(component.getComponentType(), component.getUniqueId(), user, nextState, changeInfo, false, true)
                .right()
                .map(err-> {
                    LOGGER.debug("Failed to {} Component  {} from type {} id {}, Error {}", nextState, component.getName(), component.getComponentType(), component.getUniqueId(), err);
                    setUpgradeStatus(component, upgradeStatus, idForStatus);
                    return ActionStatus.GENERAL_ERROR;
                         
                });
    }


    private void setUpgradeStatus(Component component, UpgradeStatus upgradeStatus, String idForStatus) {
        if ( idForStatus == null ){ 
            upgradeStatus.addServiceStatus(component, ActionStatus.GENERAL_ERROR);
        }else{
            upgradeStatus.addServiceStatus(idForStatus, ActionStatus.GENERAL_ERROR);
        }
    }

    private ActionStatus handleInstances(Component newVersionComponent, User user, UpgradeStatus upgradeStatus, Service service) {
        List<ComponentInstance> componentInstances = service.getComponentInstances();
        if (componentInstances != null) {
            List<ComponentInstance> instanceToChange = componentInstances
                    .stream()
                    .filter(ci -> matchInstance(ci, newVersionComponent))
                    .collect(Collectors.toList());
            if (instanceToChange != null && !instanceToChange.isEmpty()) {
                return changeInstances(newVersionComponent, user, upgradeStatus, service, instanceToChange);
            } else {
                LOGGER.debug("No instances for change version");
                return ActionStatus.NO_INSTANCES_TO_UPGRADE;
            }
        }
        return ActionStatus.OK;
    }

    private ActionStatus changeInstances(Component newVersionComponent, User user, UpgradeStatus upgradeStatus, Service service, List<ComponentInstance> instanceToChange) {
        Component serviceToUpgrade = service;
        for (ComponentInstance ci : instanceToChange) {
            Either<Component, ActionStatus> fetchService = fetchService(service.getUniqueId(),service.getName());
            if ( fetchService.isRight()){
                upgradeStatus.addServiceStatus(service, fetchService.right().value());
                return fetchService.right().value();
            }
            serviceToUpgrade = fetchService.left().value();
            ActionStatus status = changeVersionOfInstance(serviceToUpgrade, ci, newVersionComponent, user);
            if (status != ActionStatus.OK) {
                LOGGER.debug("Failed to change for instance {} version in service {}", ci.getName(), service.getName());
                upgradeStatus.addServiceStatus(service, status);
                return status;
            }
        }
        Either<Component, ActionStatus> fetchService = fetchService(service.getUniqueId(),service.getName());
        if ( fetchService.isRight()){
            upgradeStatus.addServiceStatus(service, fetchService.right().value());
            return fetchService.right().value();
        }
        serviceToUpgrade = fetchService.left().value();
        
        Either<? extends Component, ActionStatus> changeComponentState = changeComponentState(serviceToUpgrade, LifeCycleTransitionEnum.CHECKIN, user,  upgradeStatus, null);
        if ( changeComponentState.isRight() ){
            return changeComponentState.right().value();
        }
        upgradeStatus.addServiceStatus(serviceToUpgrade, ActionStatus.OK);
        return ActionStatus.OK;
    }


    private Either<Component, ActionStatus> fetchService(String uniqueId, String name) {
        return toscaOperationFacade.getToscaFullElement(uniqueId)
                .right()
                .map(r->{
                    LOGGER.debug("Failed to fetch service {} id {} error {}", name, uniqueId, r);
                    return ActionStatus.GENERAL_ERROR;
                });
    }

    private ActionStatus changeVersionOfInstance(Component service, ComponentInstance ci, Component newVersionComponent, User user) {
        LOGGER.debug("In Service {} change instance version {} to version {}", service.getName(), ci.getName(), newVersionComponent.getVersion());
        ComponentInstance newComponentInstance = new ComponentInstance();
        newComponentInstance.setComponentUid(newVersionComponent.getUniqueId());
        Either<ComponentInstance, ResponseFormat> changeInstanceVersion = componentInstanceBusinessLogic.changeInstanceVersion(service, ci, newComponentInstance, user, service.getComponentType());
        if (changeInstanceVersion.isLeft()) {
            return ActionStatus.OK;
        } else {
            return ActionStatus.GENERAL_ERROR;
        }
    }

    private boolean matchInstance(ComponentInstance ci, Component newVersionComponent) {
        Either<Component, StorageOperationStatus> toscaElement;
        ComponentParametersView filters = new ComponentParametersView(true);
        if (newVersionComponent.getComponentType() == ComponentTypeEnum.SERVICE) {
            if (ci.getIsProxy()) {
                toscaElement = toscaOperationFacade.getToscaElement(ci.getSourceModelUid(), filters);
            } else {
                return false;
            }
        } else {
            toscaElement = toscaOperationFacade.getToscaElement(ci.getComponentUid(), filters);
        }
        if (toscaElement.isLeft()) {
            Component origin = toscaElement.left().value();
            if (newVersionComponent.getInvariantUUID().equals(origin.getInvariantUUID()) && !newVersionComponent.getVersion().equals(origin.getVersion())) {
                // only for same invariant UUID (same component) but different versions
                return true;
            }
        }
        return false;
    }
    private AuditingActionEnum getAuditTypeByComponent(Component component){
        if ( ComponentTypeEnum.RESOURCE == component.getComponentType()){
            return AuditingActionEnum.VF_UPGRADE_SERVICES;
        }
        return AuditingActionEnum.UPDATE_SERVICE_REFERENCE;
    }

}
