package org.openecomp.sdc.be.components.impl;

import static java.util.stream.Collectors.toMap;
import static org.openecomp.sdc.be.components.validation.PolicyUtils.getExcludedPolicyTypesByComponent;
import static org.openecomp.sdc.be.components.validation.PolicyUtils.getNextPolicyCounter;
import static org.openecomp.sdc.be.components.validation.PolicyUtils.validatePolicyFields;

import fj.data.Either;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.property.PropertyDeclarationOrchestrator;
import org.openecomp.sdc.be.components.validation.PolicyUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.GetPolicyValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

/**
 * Provides specified business logic to create, retrieve, update, delete a policy
 */
@org.springframework.stereotype.Component("policyBusinessLogic")
public class PolicyBusinessLogic extends BaseBusinessLogic {

    private static final String FAILED_TO_VALIDATE_COMPONENT = "#{} - failed to validate the component {} before policy processing. ";
    private static final String DECLARE_PROPERTIES_TO_POLICIES = "declare properties to policies";
    private static final String EXECUTE_ROLLBACK = "execute rollback";
    private static final String EXECUTE_COMMIT = "execute commit";
    private static final Logger log = Logger.getLogger(PolicyBusinessLogic.class);

    @Inject
    private PropertyDeclarationOrchestrator propertyDeclarationOrchestrator;

    public PolicyBusinessLogic() {
    }

    public PolicyBusinessLogic(PropertyDeclarationOrchestrator propertyDeclarationOrchestrator) {
        this.propertyDeclarationOrchestrator = propertyDeclarationOrchestrator;
    }

    public void setPropertyDeclarationOrchestrator(PropertyDeclarationOrchestrator propertyDeclarationOrchestrator) {
        this.propertyDeclarationOrchestrator = propertyDeclarationOrchestrator;
    }

    /**
     * Adds the newly created policy of the specified type to the component
     *
     * @param componentType  the type of the component
     * @param componentId    the id of the component which the policy resides under
     * @param policyTypeName the name of the policy type
     * @param userId         the user creator id
     * @param shouldLock     the flag defining if the component should be locked
     * @return a policy or an error in a response format
     */

    public Either<PolicyDefinition, ResponseFormat> createPolicy(ComponentTypeEnum componentType, String componentId, String policyTypeName, String userId, boolean shouldLock) {

        Either<PolicyDefinition, ResponseFormat> result = null;
        log.trace("#createPolicy - starting to create policy of the type {} on the component {}. ", policyTypeName, componentId);
        Wrapper<Component> component = new Wrapper<>();
        try {
            result = validateAndLockComponentAndUserBeforeWriteOperation(componentType, componentId, userId, shouldLock)
                    .left()
                    .bind(c -> {
                        component.setInnerElement(c);
                        return createPolicy(policyTypeName, c);
                    });
        } catch (Exception e) {
            log.error("#createPolicy - the exception  occurred upon creation of a policy of the type {} for the component {}: ", policyTypeName, componentId, e);
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {

            unlockComponent(shouldLock, result, component);
        }
        return result;
    }

    public Either<List<PolicyDefinition>, ResponseFormat> getPoliciesList(ComponentTypeEnum componentType, String componentId, String userId) {
        Either<List<PolicyDefinition>, ResponseFormat> result;
        log.trace("#getPolicies - starting to retrieve policies of component {}. ", componentId);
        try {
            result = validateContainerComponentAndUserBeforeReadOperation(componentType, componentId, userId)
                             .left()
                             .bind(c -> Either.left(c.resolvePoliciesList()));
        } catch (Exception e) {
            log.error("#getPolicy - the exception occurred upon retrieving policies list of component {}: ", componentId, e);
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        return result;
    }

    /**
     * Retrieves the policy of the component by UniqueId
     *
     * @param componentType the type of the component
     * @param componentId   the ID of the component
     * @param policyId      the ID of the policy
     * @param userId        the ID of the user
     * @return              either policy or error response
     */
    public Either<PolicyDefinition, ResponseFormat> getPolicy(ComponentTypeEnum componentType, String componentId, String policyId, String userId) {
        Either<PolicyDefinition, ResponseFormat> result;
        log.trace("#getPolicy - starting to retrieve the policy {} of the component {}. ", policyId, componentId);
        try {
            result = validateContainerComponentAndUserBeforeReadOperation(componentType, componentId, userId)
                    .left()
                    .bind(c -> getPolicyById(c, policyId));
        } catch (Exception e) {
            log.error("#getPolicy - the exception occurred upon retrieving the policy {} of the component {}: ", policyId, componentId, e);
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        return result;
    }

    /**
     * Updates the policy of the component
     *
     * @param componentType the type of the component
     * @param componentId   the id of the component which the policy resides under
     * @param policy        the policy to update
     * @param userId        the user modifier id
     * @param shouldLock    the flag defining if the component should be locked
     * @return a policy or an error in a response format
     */
    public Either<PolicyDefinition, ResponseFormat> updatePolicy(ComponentTypeEnum componentType, String componentId, PolicyDefinition policy, String userId, boolean shouldLock) {
        Either<PolicyDefinition, ResponseFormat> result = null;
        log.trace("#updatePolicy - starting to update the policy {} on the component {}. ", policy.getUniqueId(), componentId);
        Wrapper<Component> component = new Wrapper<>();
        try {
            result = validateAndLockComponentAndUserBeforeWriteOperation(componentType, componentId, userId, shouldLock)
                    .left()
                    .bind(c -> {
                        component.setInnerElement(c);
                        return validateAndUpdatePolicy(c, policy);
                    });
        } catch (Exception e) {
            log.error("#updatePolicy - the exception occurred upon update of a policy of the type {} for the component {}: ", policy.getUniqueId(), componentId, e);
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            unlockComponent(shouldLock, result, component);
        }
        return result;
    }

    /**
     * Deletes the policy from the component
     *
     * @param componentType the type of the component
     * @param componentId   the id of the component which the policy resides under
     * @param policyId      the id of the policy which its properties to return
     * @param userId        the user modifier id
     * @param shouldLock    the flag defining if the component should be locked
     * @return a policy or an error in a response format
     */
    public Either<PolicyDefinition, ResponseFormat> deletePolicy(ComponentTypeEnum componentType, String componentId, String policyId, String userId, boolean shouldLock) {
        Either<PolicyDefinition, ResponseFormat> result = null;
        log.trace("#deletePolicy - starting to update the policy {} on the component {}. ", policyId, componentId);
        Wrapper<Component> component = new Wrapper<>();
        try {
            Either<Component, ResponseFormat> componentEither =
                    validateAndLockComponentAndUserBeforeWriteOperation(componentType, componentId, userId, shouldLock);
            if (componentEither.isRight()) {
                return Either.right(componentEither.right().value());
            }

            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreComponentInstances(false);
            componentParametersView.setIgnoreComponentInstancesProperties(false);
            componentParametersView.setIgnorePolicies(false);
            componentParametersView.setIgnoreProperties(false);

            Either<Component, StorageOperationStatus> componentWithFilters =
                    toscaOperationFacade.getToscaElement(componentId, componentParametersView);
            if (componentWithFilters.isRight()) {
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(componentWithFilters.right().value())));
            }

            Component containerComponent = componentWithFilters.left().value();
            component.setInnerElement(containerComponent);
            result = deletePolicy(containerComponent, policyId);

            if(result.isRight()) {
                log.error("#deletePolicy - could not delete policy of the type {} for the component {}: ", policyId, componentId);
                return result;
            }

            PolicyDefinition policyToDelete = result.left().value();

            StorageOperationStatus storageOperationStatus = propertyDeclarationOrchestrator.unDeclarePropertiesAsPolicies(
                    containerComponent, policyToDelete);
            if (storageOperationStatus != StorageOperationStatus.OK) {
                log.debug("Component id: {} update properties declared as policy for policy id: {} failed", componentId, policyId);
                return Either.right(componentsUtils.getResponseFormat(componentsUtils
                                                                                      .convertFromStorageResponse(storageOperationStatus), containerComponent.getName()));
            }

            return result;
        } catch (Exception e) {
            log.error("#deletePolicy - the exception occurred upon update of a policy of the type {} for the component {}: ", policyId, componentId, e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR, e.getMessage()));
        } finally {
            unlockComponent(shouldLock, result, component);
        }
    }

    public Either<PolicyDefinition, ResponseFormat> undeclarePolicy(ComponentTypeEnum componentType, String componentId, String policyId, String userId, boolean shouldLock) {
        Either<PolicyDefinition, ResponseFormat> result = null;
        log.trace("#undeclarePolicy - starting to undeclare policy {} on component {}. ", policyId, componentId);
        Wrapper<Component> component = new Wrapper<>();
        try {
            Either<Component, ResponseFormat> componentEither =
                    validateAndLockComponentAndUserBeforeWriteOperation(componentType, componentId, userId, shouldLock);
            if (componentEither.isRight()) {
                return Either.right(componentEither.right().value());
            }

            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreComponentInstances(false);
            componentParametersView.setIgnoreComponentInstancesProperties(false);
            componentParametersView.setIgnorePolicies(false);

            Either<Component, StorageOperationStatus> componentWithFilters =
                    toscaOperationFacade.getToscaElement(componentId, componentParametersView);
            if (componentWithFilters.isRight()) {
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(componentWithFilters.right().value())));
            }

            Component containerComponent = componentWithFilters.left().value();

            Optional<PolicyDefinition> policyCandidate = getPolicyForUndeclaration(policyId, containerComponent);
            if(policyCandidate.isPresent()) {
                result = undeclarePolicy(policyCandidate.get(), containerComponent);
            }

            return result;
        } catch (Exception e) {
            log.error("#undeclarePolicy - the exception occurred upon update of a policy of type {} for component {}: ", policyId, componentId, e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR, e.getMessage()));
        } finally {
            unlockComponent(shouldLock, result, component);
        }
    }


    private Either<PolicyDefinition, ResponseFormat> undeclarePolicy(PolicyDefinition policyDefinition, Component containerComponent) {
        StorageOperationStatus undeclareStatus = propertyDeclarationOrchestrator
                                                         .unDeclarePropertiesAsPolicies(containerComponent, policyDefinition);
        if(undeclareStatus != StorageOperationStatus.OK){
            return Either.right(componentsUtils.getResponseFormat(undeclareStatus));
        } else {
            return Either.left(policyDefinition);
        }
    }


    private Optional<PolicyDefinition> getPolicyForUndeclaration(String policyId, Component component) {
        Map<String, PolicyDefinition> policies = component.getPolicies();
        if(MapUtils.isNotEmpty(policies) && policies.containsKey(policyId)) {
            return Optional.of(policies.get(policyId));
        }

        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties =
                MapUtils.isEmpty(component.getComponentInstancesProperties()) ? new HashMap<>() : component.getComponentInstancesProperties();

        for(Map.Entry<String, List<ComponentInstanceProperty>> instancePropertyEntry : componentInstancesProperties.entrySet()) {
            Optional<ComponentInstanceProperty> propertyCandidate = getPropertyForDeclaredPolicy(policyId, instancePropertyEntry.getValue());

            if(propertyCandidate.isPresent()) {
                return Optional.of(
                        PolicyUtils.getDeclaredPolicyDefinition(instancePropertyEntry.getKey(), propertyCandidate.get()));
            }
        }

        return Optional.empty();
    }

    private Optional<ComponentInstanceProperty> getPropertyForDeclaredPolicy(String policyId, List<ComponentInstanceProperty> componentInstanceProperties) {
        for(ComponentInstanceProperty property : componentInstanceProperties) {
            Optional<GetPolicyValueDataDefinition> getPolicyCandidate = property.safeGetGetPolicyValues().stream()
                                                                 .filter(getPolicyValue -> getPolicyValue.getPolicyId()
                                                                                                   .equals(policyId))
                                                                 .findAny();

            if(getPolicyCandidate.isPresent()) {
                return Optional.of(property);
            }
        }

        return Optional.empty();
    }

    public Either<PolicyDefinition, ResponseFormat> updatePolicyTargets(ComponentTypeEnum componentTypeEnum, String componentId, String policyId, Map<PolicyTargetType, List<String>> targets, String userId) {

        Either<PolicyDefinition, ResponseFormat> result = null;
        log.debug("updating the policy id {} targets with the components {}. ", policyId, componentId);
        try {
            //not right error response
            result = validateAndLockComponentAndUserBeforeWriteOperation(componentTypeEnum, componentId, userId, true)
                    .left()
                    .bind(cmpt -> validateAndUpdatePolicyTargets(cmpt, policyId, targets));

            return result;
        } finally {

            unlockComponentById(result, componentId);

        }

    }

    private Either<PolicyDefinition, ResponseFormat> validateAndUpdatePolicyTargets(Component component, String policyId, Map<PolicyTargetType, List<String>> targets) {
        return validateTargetsExistAndTypesCorrect(component.getUniqueId(), targets)
                .left()
                .bind(cmp ->updateTargets(component.getUniqueId(), component.getPolicyById(policyId), targets, policyId));

    }

    private Either<Component, ResponseFormat> validateTargetsExistAndTypesCorrect(String componentId, Map<PolicyTargetType, List<String>> targets) {
        Either<Component, StorageOperationStatus> componentEither = toscaOperationFacade.getToscaFullElement(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(componentEither.right().value()));
        }
        Component parentComponent = componentEither.left().value();
        return validateTargetExists(parentComponent, targets.entrySet());
    }



    private Either<Component, ResponseFormat> validateTargetExists(Component parentComponent, Set<Map.Entry<PolicyTargetType, List<String>>> entries) {
        for(Map.Entry<PolicyTargetType, List<String>> entry : entries){
            Either<Component, ResponseFormat> result = checkTargetNotExistOnComponentByType(parentComponent, entry);
            if(result.isRight()){
                return result;
            }
        }
        return Either.left(parentComponent);
    }

    private Either<Component, ResponseFormat> checkTargetNotExistOnComponentByType(Component parentComponent, Map.Entry<PolicyTargetType, List<String>> targetEntry) {

        for(String id : targetEntry.getValue()){
            if(checkNotPresenceInComponentByType(parentComponent, id, targetEntry.getKey().getName())){
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.POLICY_TARGET_DOES_NOT_EXIST, id));
            }
        }
        return Either.left(parentComponent);
    }

    private boolean checkNotPresenceInComponentByType(Component parentComponent, String uniqueId, String type) {
        if (type.equalsIgnoreCase(PolicyTargetType.GROUPS.getName()) && parentComponent.getGroups() != null) {
            return !parentComponent.getGroupById(uniqueId).isPresent();
        } else if (type.equalsIgnoreCase(PolicyTargetType.COMPONENT_INSTANCES.getName()) && parentComponent.getComponentInstances() != null) {
            return !parentComponent.getComponentInstanceById(uniqueId).isPresent();
        }
        return true;
    }

    private PolicyDefinition setPolicyTargets(PolicyDefinition policyDefinition, Map<PolicyTargetType, List<String>> targets) {
        policyDefinition.setTargets(targets);
        return policyDefinition;
    }


    /**
     * @param componentType the type of the component
     * @param componentId   the id of the component which the policy resides under
     * @param policyId      the id of the policy which its properties to return
     * @param userId        the user id
     * @return a list of policy properties or an error in a response format
     */
    public Either<List<PropertyDataDefinition>, ResponseFormat> getPolicyProperties(ComponentTypeEnum componentType, String componentId, String policyId, String userId) {
        log.debug("#getPolicyProperties - fetching policy properties for component {} and policy {}", componentId, policyId);
        try {
            return validateContainerComponentAndUserBeforeReadOperation(componentType, componentId, userId)
                    .left()
                    .bind(cmpt -> getPolicyById(cmpt, policyId)).left().map(PolicyDataDefinition::getProperties);
        } finally {
            titanDao.commit();
        }
    }

    /**
     * Updates the policy properties of the component
     *
     * @param componentType the type of the component
     * @param componentId   the id of the component which the policy resides under
     * @param policyId      the id of the policy which its properties to return
     * @param properties    a list of policy properties containing updated values
     * @param userId        the user modifier id
     * @param shouldLock    the flag defining if the component should be locked
     * @return a list of policy properties or anerrorin a response format
     */
    public Either<List<PropertyDataDefinition>, ResponseFormat> updatePolicyProperties(ComponentTypeEnum componentType, String componentId, String policyId, PropertyDataDefinition[] properties, String userId, boolean shouldLock) {
        Either<List<PropertyDataDefinition>, ResponseFormat> result = null;
        log.trace("#updatePolicyProperties - starting to update properties of the policy {} on the component {}. ", policyId, componentId);
        Wrapper<Component> component = new Wrapper<>();
        try {
            result = validateAndLockComponentAndUserBeforeWriteOperation(componentType, componentId, userId, shouldLock).left()
                    .bind(c -> setComponentValidateUpdatePolicyProperties(policyId, properties, component, c));
        } catch (Exception e) {
            log.error("#updatePolicyProperties - the exception {} occurred upon update properties of the policy {} for the component {}: ", policyId, componentId, e);
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (shouldLock && !component.isEmpty()) {
                unlockComponent(result, component.getInnerElement());
            }
        }
        return result;
    }

    @Override
    public  Either<List<PolicyDefinition>, ResponseFormat> declareProperties(String userId, String componentId,
            ComponentTypeEnum componentTypeEnum, ComponentInstInputsMap componentInstInputsMap) {
        return declarePropertiesToPolicies(userId, componentId, componentTypeEnum, componentInstInputsMap, true, false);

    }

    private Either<List<PolicyDefinition>, ResponseFormat> declarePropertiesToPolicies(String userId, String componentId,
            ComponentTypeEnum componentTypeEnum, ComponentInstInputsMap componentInstInputsMap, boolean shouldLock,
            boolean inTransaction) {
        Either<List<PolicyDefinition>, ResponseFormat> result = null;
        org.openecomp.sdc.be.model.Component component = null;

        try {
            validateUserExists(userId, DECLARE_PROPERTIES_TO_POLICIES, false);

            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreComponentInstances(false);
            componentParametersView.setIgnoreComponentInstancesProperties(false);
            componentParametersView.setIgnorePolicies(false);
            componentParametersView.setIgnoreUsers(false);

            Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> validateComponent = validateComponentExists(componentId, componentTypeEnum, componentParametersView);

            if (validateComponent.isRight()) {
                result = Either.right(validateComponent.right().value());
                return result;
            }
            component = validateComponent.left().value();

            if (shouldLock) {
                Either<Boolean, ResponseFormat> lockComponent = lockComponent(component, DECLARE_PROPERTIES_TO_POLICIES);
                if (lockComponent.isRight()) {
                    result = Either.right(lockComponent.right().value());
                    return result;
                }
            }

            Either<Boolean, ResponseFormat> canWork = validateCanWorkOnComponent(component, userId);
            if (canWork.isRight()) {
                result = Either.right(canWork.right().value());
                return result;
            }

            Either<List<PolicyDefinition>, StorageOperationStatus> declarePropertiesEither =
                    propertyDeclarationOrchestrator.declarePropertiesToPolicies(component, componentInstInputsMap);

            if(declarePropertiesEither.isRight()) {
                return Either.right(componentsUtils.getResponseFormat(declarePropertiesEither.right().value()));
            }

            result = Either.left(declarePropertiesEither.left().value());
            return result;
        } finally {
            if(!inTransaction) {
                commitOrRollback(result);
            }
            // unlock resource
            if (shouldLock && component != null) {
                graphLockOperation.unlockComponent(componentId, componentTypeEnum.getNodeType());
            }
        }
    }

    private Either<List<PropertyDataDefinition>, ResponseFormat> setComponentValidateUpdatePolicyProperties(String policyId, PropertyDataDefinition[] properties, Wrapper<Component> component, Component c) {
        component.setInnerElement(c);
        Set<String> updatedPropertyNames = Arrays.stream(properties).map(PropertyDataDefinition::getName).collect(Collectors.toSet());
        return validateAndUpdatePolicyProperties(c, policyId, properties)
                .left()
                .map(policyDefinition -> getFilteredProperties(policyDefinition.getProperties(), updatedPropertyNames));
    }

    private List<PropertyDataDefinition> getFilteredProperties(List<PropertyDataDefinition> all, Set<String> filtered) {
        return all.stream().filter(pd -> filtered.contains(pd.getName())).collect(Collectors.toList());
    }

    private void unlockComponent(boolean shouldLock, Either<PolicyDefinition, ResponseFormat> result, Wrapper<Component> component) {
        if (shouldLock && !component.isEmpty()) {
            unlockComponent(result, component.getInnerElement());
        }
    }

    private Either<PolicyDefinition, ResponseFormat> getPolicyById(Component component, String policyId) {
        PolicyDefinition policyById = component.getPolicyById(policyId);
        if (policyById == null) {
            String cmptId = component.getUniqueId();
            log.debug("#getPolicyById - policy with id {} does not exist on component with id {}", policyId, cmptId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.POLICY_NOT_FOUND_ON_CONTAINER, policyId, cmptId));
        }
        return Either.left(policyById);
    }

    private Either<PolicyDefinition, ResponseFormat> createPolicy(String policyTypeName, Component component) {
        return validatePolicyTypeOnCreatePolicy(policyTypeName, component).left().bind(type -> addPolicyToComponent(type, component));
    }

    private Either<PolicyDefinition, ResponseFormat> addPolicyToComponent(PolicyTypeDefinition policyType, Component component) {
        return toscaOperationFacade.associatePolicyToComponent(component.getUniqueId(), new PolicyDefinition(policyType), getNextPolicyCounter(component.getPolicies()))
                .either(Either::left, r -> Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(r))));
    }

    private Either<PolicyTypeDefinition, ResponseFormat> validatePolicyTypeOnCreatePolicy(String policyTypeName, Component component) {
        return policyTypeOperation.getLatestPolicyTypeByType(policyTypeName)
                .either(l -> validatePolicyTypeNotExcluded(l, component), r -> Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(r))));
    }

    private Either<PolicyTypeDefinition, ResponseFormat> validatePolicyTypeNotExcluded(PolicyTypeDefinition policyType, Component component) {
        if (getExcludedPolicyTypesByComponent(component).contains(policyType.getType())) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.EXCLUDED_POLICY_TYPE, policyType.getType(), getComponentOrResourceTypeName(component)));
        }
        return Either.left(policyType);
    }

    private String getComponentOrResourceTypeName(Component component) {
        return component.getComponentType() == ComponentTypeEnum.SERVICE ? ComponentTypeEnum.SERVICE.name() : ((Resource) component).getResourceType().name();
    }

    private Either<Component, ResponseFormat> validateAndLockComponentAndUserBeforeWriteOperation(ComponentTypeEnum componentType, String componentId, String userId, boolean shouldLock) {
        Wrapper<Component> component = new Wrapper<>();
        return validateContainerComponentAndUserBeforeReadOperation(componentType, componentId, userId)
                .left()
                .bind(this::validateComponentIsTopologyTemplate)
                .left()
                .bind(c -> {
                    component.setInnerElement(c);
                    return validateCanWorkOnComponent(c, userId);
                })
                .left()
                .bind(l -> lockComponent(component.getInnerElement(), shouldLock, "policyWritingOperation"))
                .either(l -> Either.left(component.getInnerElement()), r -> {
                    log.error(FAILED_TO_VALIDATE_COMPONENT, componentId);
                    return Either.right(r);
                });
    }

    private Either<Component, ResponseFormat> validateComponentIsTopologyTemplate(Component component) {
        if (!component.isTopologyTemplate()) {
            log.error("#validateComponentIsTopologyTemplate - policy association to a component of Tosca type {} is not allowed. ", component.getToscaType());
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_CANNOT_CONTAIN_POLICIES, "#validateAndLockComponentAndUserBeforeWriteOperation", component.getUniqueId(), component.getToscaType()));
        }
        return Either.left(component);
    }

    private Either<Component, ResponseFormat> validateContainerComponentAndUserBeforeReadOperation(ComponentTypeEnum componentType, String componentId, String userId) {
        Either<Component, ResponseFormat> result;
        log.trace("#validateContainerComponentAndUserBeforeReadOperation - starting to validate the user {} before policy processing. ", userId);
        validateUserExists(userId, "create Policy", false);
        result = validateComponentExists(componentType, componentId);
        if (result.isRight()) {
            log.error(FAILED_TO_VALIDATE_COMPONENT, "#validateContainerComponentAndUserBeforeReadOperation", componentId);
        }
        return result;
    }

    private Either<Component, ResponseFormat> validateComponentExists(ComponentTypeEnum componentType, String componentId) {

        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnorePolicies(false);
        filter.setIgnoreUsers(false);
        filter.setIgnoreComponentInstances(false);
        filter.setIgnoreGroups(false);
        return validateComponentExists(componentId, componentType, filter);
    }


    private Either<PolicyDefinition, ResponseFormat> validateAndUpdatePolicy(Component component, PolicyDefinition policy) {
        return getPolicyById(component, policy.getUniqueId())
                .left()
                .bind(np -> validateUpdatePolicyBeforeUpdate(policy, np, component.getPolicies()))
                .left()
                .bind(p -> updatePolicyOfComponent(component, p));
    }

    private Either<PolicyDefinition, ResponseFormat> validateAndUpdatePolicyProperties(Component component, String policyId, PropertyDataDefinition[] properties) {
        return getPolicyById(component, policyId)
                .left()
                .bind(p -> validateUpdatePolicyPropertiesBeforeUpdate(p, properties))
                .left().bind(l -> updatePolicyOfComponent(component.getUniqueId(), l));
    }

    private Either<PolicyDefinition, ResponseFormat> updatePolicyOfComponent(String componentId, PolicyDefinition policy) {
        return toscaOperationFacade.updatePolicyOfComponent(componentId, policy)
                .right()
                .bind(r -> Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(r))));
    }

    private Either<PolicyDefinition, ResponseFormat> validateUpdatePolicyPropertiesBeforeUpdate(PolicyDefinition policy, PropertyDataDefinition[] newProperties) {
        if (CollectionUtils.isEmpty(policy.getProperties())) {
            log.error("#validateUpdatePolicyPropertiesBeforeUpdate - failed to update properites of the policy. Properties were not found on the policy. ");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND));
        }
        return updatePropertyValues(policy, newProperties);
    }

    private Either<PolicyDefinition, ResponseFormat> updatePropertyValues(PolicyDefinition policy, PropertyDataDefinition[] newProperties) {

        Map<String, PropertyDataDefinition> oldProperties = policy.getProperties().stream().collect(toMap(PropertyDataDefinition::getName, Function.identity()));
        for (PropertyDataDefinition newProperty : newProperties) {
            if (!oldProperties.containsKey(newProperty.getName())) {
                log.error("#updatePropertyValues - failed to update properites of the policy {}. Properties were not found on the policy. ", policy.getName());
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, newProperty.getName()));
            }
            Either<String, ResponseFormat> newPropertyValueEither = updateInputPropertyObjectValue(newProperty);
            if (newPropertyValueEither.isRight()) {
                return Either.right(newPropertyValueEither.right().value());
            }
            oldProperties.get(newProperty.getName()).setValue(newPropertyValueEither.left().value());
        }
        return Either.left(policy);
    }

    private Either<PolicyDefinition, ResponseFormat> deletePolicy(Component component, String policyId) {
        return getPolicyById(component, policyId)
                .left()
                .bind(p -> removePolicyFromComponent(component, p));
    }

    private Either<PolicyDefinition, ResponseFormat> updatePolicyOfComponent(Component component, PolicyDefinition policy) {
        Either<PolicyDefinition, StorageOperationStatus> updatePolicyRes = toscaOperationFacade.updatePolicyOfComponent(component.getUniqueId(), policy);
        if (updatePolicyRes.isRight()) {
            log.error("#updatePolicyOfComponent - failed to update policy {} of the component {}. The status is {}. ", policy.getUniqueId(), component.getName(), updatePolicyRes.right().value());
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(updatePolicyRes.right().value())));
        } else {
            log.trace("#updatePolicyOfComponent - the policy with the name {} was updated. ", updatePolicyRes.left().value().getName());
            return Either.left(updatePolicyRes.left().value());
        }
    }

    private Either<PolicyDefinition, ResponseFormat> removePolicyFromComponent(Component component, PolicyDefinition policy) {
        StorageOperationStatus updatePolicyStatus = toscaOperationFacade.removePolicyFromComponent(component.getUniqueId(), policy.getUniqueId());
        if (updatePolicyStatus != StorageOperationStatus.OK) {
            log.error("#removePolicyFromComponent - failed to remove policy {} from the component {}. The status is {}. ", policy.getUniqueId(), component.getName(), updatePolicyStatus);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(updatePolicyStatus)));
        } else {
            log.trace("#removePolicyFromComponent - the policy with the name {} was deleted. ", updatePolicyStatus);
            return Either.left(policy);
        }
    }

    private Either<PolicyDefinition, ResponseFormat> validateUpdatePolicyBeforeUpdate(PolicyDefinition recievedPolicy, PolicyDefinition oldPolicy, Map<String, PolicyDefinition> policies) {
        return validatePolicyFields(recievedPolicy, new PolicyDefinition(oldPolicy), policies)
                .right()
                .bind(r -> Either.right(componentsUtils.getResponseFormat(r, recievedPolicy.getName())));
    }

    private Either<PolicyDefinition, ResponseFormat> updateTargets(String componentId, PolicyDefinition policy, Map<PolicyTargetType, List<String>> targets, String policyId) {
        if(policy == null){
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.POLICY_NOT_FOUND_ON_CONTAINER, policyId, componentId));
        }
        PolicyDefinition updatedPolicy = setPolicyTargets(policy, targets);
        return updatePolicyOfComponent(componentId, updatedPolicy);
    }

}
