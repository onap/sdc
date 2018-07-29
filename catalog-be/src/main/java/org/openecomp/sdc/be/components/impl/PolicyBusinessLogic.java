package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.openecomp.sdc.be.components.validation.PolicyUtils.*;

/**
 * Provides specified business logic to create, retrieve, update, delete a policy
 */
@org.springframework.stereotype.Component("policyBusinessLogic")
public class PolicyBusinessLogic extends BaseBusinessLogic {

    private static final String FAILED_TO_VALIDATE_COMPONENT = "#{} - failed to validate the component {} before policy processing. ";
    private static final Logger log = Logger.getLogger(PolicyBusinessLogic.class);

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
            result = validateAndLockComponentAndUserBeforeWriteOperation(componentType, componentId, userId, shouldLock)
                    .left()
                    .bind(c -> {
                        component.setInnerElement(c);
                        return deletePolicy(c, policyId);
                    });
        } catch (Exception e) {
            log.error("#deletePolicy - the exception occurred upon update of a policy of the type {} for the component {}: ", policyId, componentId, e);
        } finally {
            unlockComponent(shouldLock, result, component);
        }
        return result;
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
