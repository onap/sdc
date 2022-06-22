/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021 Nordix Foundation.
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
package org.openecomp.sdc.be.components.impl;

import static java.util.stream.Collectors.toMap;
import static org.openecomp.sdc.be.components.validation.PolicyUtils.getExcludedPolicyTypesByComponent;
import static org.openecomp.sdc.be.components.validation.PolicyUtils.getNextPolicyCounter;
import static org.openecomp.sdc.be.components.validation.PolicyUtils.validatePolicyFields;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.property.PropertyDeclarationOrchestrator;
import org.openecomp.sdc.be.components.validation.PolicyUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.GetPolicyValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.PromoteVersionEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides specified business logic to create, retrieve, update, delete a policy
 */
@org.springframework.stereotype.Component("policyBusinessLogic")
public class PolicyBusinessLogic extends BaseBusinessLogic {

    private static final String DECLARE_PROPERTIES_TO_POLICIES = "declare properties to policies";
    private static final Logger log = Logger.getLogger(PolicyBusinessLogic.class);
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(PolicyBusinessLogic.class.getName());
    private PropertyDeclarationOrchestrator propertyDeclarationOrchestrator;

    @Autowired
    public PolicyBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation, IGroupInstanceOperation groupInstanceOperation,
                               IGroupTypeOperation groupTypeOperation, InterfaceOperation interfaceOperation,
                               InterfaceLifecycleOperation interfaceLifecycleTypeOperation, ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
    }

    @Autowired
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
    public PolicyDefinition createPolicy(ComponentTypeEnum componentType, String componentId, String policyTypeName, String userId,
                                         boolean shouldLock) {
        log.trace("#createPolicy - starting to create policy of the type {} on the component {}. ", policyTypeName, componentId);
        Component component = null;
        boolean failed = false;
        try {
            component = validateAndLockComponentAndUserBeforeWriteOperation(componentType, componentId, userId, shouldLock);
            return createPolicy(policyTypeName, component);
        } catch (ComponentException e) {
            failed = true;
            throw e;
        } finally {
            unlockComponent(shouldLock, failed, component);
        }
    }

    public Map<String, PolicyDefinition> createPolicies(final Component component,
                                                        final Map<String, PolicyDefinition> incomingPolicyDefinitions) {
        if (MapUtils.isEmpty(incomingPolicyDefinitions)) {
            return Collections.emptyMap();
        }
        final Map<String, PolicyDefinition> createdPolicies = new HashMap<>();
        for (final PolicyDefinition incomingPolicyDefinition : incomingPolicyDefinitions.values()) {
            final String policyName = incomingPolicyDefinition.getName();
            log.trace("Going to create policy {}", incomingPolicyDefinition);
            loggerSupportability
                .log(LoggerSupportabilityActions.CREATE_GROUP_POLICY, component.getComponentMetadataForSupportLog(), StatusCode.STARTED,
                    "Start to create policy: {} for component {}", policyName, component.getName());
            final String policyType = incomingPolicyDefinition.getType();
            if (StringUtils.isEmpty(policyType)) {
                log.debug("Policy type '{}' for policy '{}' not found.", policyType, policyName);
                throw new ByActionStatusComponentException(ActionStatus.POLICY_MISSING_POLICY_TYPE, policyName);
            }
            // create policyDefinition
            final String policyTypeName = incomingPolicyDefinition.getPolicyTypeName();
            PolicyDefinition createdPolicyDefinition = createPolicy(policyTypeName, component);
            // set isFromCsar
            createdPolicyDefinition.setToscaPresentationValue(JsonPresentationFields.IS_FROM_CSAR, true);
            // link policy to component
            component.addPolicy(createdPolicyDefinition);
            // process targets
            final Map<PolicyTargetType, List<String>> policyTargets = incomingPolicyDefinition.getTargets();
            createdPolicyDefinition = setUpdatePolicyTargets(component, createdPolicyDefinition, policyTargets);
            // process policy properties
            List<PropertyDataDefinition> properties = incomingPolicyDefinition.getProperties();
            createdPolicyDefinition = setUpdatePolicyProperties(component, createdPolicyDefinition, properties);
            createdPolicies.put(policyName, createdPolicyDefinition);
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_POLICIES, component.getComponentMetadataForSupportLog(), StatusCode.COMPLETE,
                "policy {} has been created ", policyName);
        }
        return createdPolicies;
    }

    private PolicyDefinition setUpdatePolicyProperties(Component component, PolicyDefinition policyDefinition,
                                                       List<PropertyDataDefinition> properties) {
        if (CollectionUtils.isNotEmpty(properties)) {
            PropertyDataDefinition[] propertiesArray = properties.toArray(new PropertyDataDefinition[properties.size()]);
            List<PropertyDataDefinition> updatedPropertiesList = setComponentValidateUpdatePolicyProperties(policyDefinition.getUniqueId(),
                propertiesArray, component);
            policyDefinition.setProperties(updatedPropertiesList);
        }
        return policyDefinition;
    }

    private PolicyDefinition setUpdatePolicyTargets(Component component, PolicyDefinition policyDefinition,
                                                    Map<PolicyTargetType, List<String>> targets) {
        if (MapUtils.isEmpty(targets)) {
            return policyDefinition;
        }
        final List<String> targetsToUpdate = targets.get(PolicyTargetType.COMPONENT_INSTANCES);
        if (CollectionUtils.isEmpty(targetsToUpdate)) {
            return policyDefinition;
        }
        // update targets to uniqueIds of respective component instance
        final List<String> targetsGroupsUniqueIds = new ArrayList<>();
        final List<String> targetsInstanceUniqueIds = new ArrayList<>();
        for (final String targetName : targetsToUpdate) {
            final String groupsTargetUniqueIdForTargerName = getGroupsTargetUniqueIdForTargerName(component, targetName);
            if (groupsTargetUniqueIdForTargerName != null) {
                targetsGroupsUniqueIds.add(groupsTargetUniqueIdForTargerName);
            }
            final String instanceTargetUniqueIdForTargerName = getInstanceTargetUniqueIdForTargerName(component, targetName);
            if (instanceTargetUniqueIdForTargerName != null) {
                targetsInstanceUniqueIds.add(instanceTargetUniqueIdForTargerName);
            }
        }
        if (targetsGroupsUniqueIds.isEmpty() && targetsInstanceUniqueIds.isEmpty()) {
            throw new ByActionStatusComponentException(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND);
        }
        final EnumMap<PolicyTargetType, List<String>> updatedTargets = new EnumMap<>(PolicyTargetType.class);
        if (!targetsGroupsUniqueIds.isEmpty()) {
            updatedTargets.put(PolicyTargetType.GROUPS, targetsGroupsUniqueIds);
        }
        if (!targetsInstanceUniqueIds.isEmpty()) {
            updatedTargets.put(PolicyTargetType.COMPONENT_INSTANCES, targetsInstanceUniqueIds);
        }
        policyDefinition.setTargets(updatedTargets);
        return validateAndUpdatePolicyTargets(component, policyDefinition.getUniqueId(), policyDefinition.getTargets());
    }

    private String getGroupsTargetUniqueIdForTargerName(final Component component, final String targetName) {
        final Optional<GroupDefinition> groupByInvariantName = component.getGroupByInvariantName(targetName);
        if (groupByInvariantName.isPresent()) {
            return groupByInvariantName.get().getUniqueId();
        }
        return null;
    }

    private String getInstanceTargetUniqueIdForTargerName(final Component component, final String targetName) {
        final Optional<ComponentInstance> componentInstance = component.getComponentInstanceByName(targetName);
        if (componentInstance.isPresent()) {
            return componentInstance.get().getUniqueId();
        }
        return null;
    }

    /**
     * Retrieves the policy of the component by UniqueId
     *
     * @param componentType the type of the component
     * @param componentId   the ID of the component
     * @param policyId      the ID of the policy
     * @param userId        the ID of the user
     * @return either policy or error response
     */
    public PolicyDefinition getPolicy(ComponentTypeEnum componentType, String componentId, String policyId, String userId) {
        log.trace("#getPolicy - starting to retrieve the policy {} of the component {}. ", policyId, componentId);
        Component component = validateContainerComponentAndUserBeforeReadOperation(componentType, componentId, userId);
        return getPolicyById(component, policyId);
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
    public PolicyDefinition updatePolicy(ComponentTypeEnum componentType, String componentId, PolicyDefinition policy, String userId,
                                         boolean shouldLock) {
        Component component = null;
        boolean failed = false;
        log.trace("#updatePolicy - starting to update the policy {} on the component {}. ", policy.getUniqueId(), componentId);
        try {
            component = validateAndLockComponentAndUserBeforeWriteOperation(componentType, componentId, userId, shouldLock);
            return validateAndUpdatePolicy(component, policy);
        } catch (ComponentException e) {
            failed = true;
            log.error("#updatePolicy - the exception occurred upon update of a policy of the type {} for the component {}: ", policy.getUniqueId(),
                componentId, e);
            throw e;
        } finally {
            //TODO Andrey result = boolean
            unlockComponent(shouldLock, failed, component);
        }
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
    public PolicyDefinition deletePolicy(ComponentTypeEnum componentType, String componentId, String policyId, String userId, boolean shouldLock) {
        log.trace("#deletePolicy - starting to update the policy {} on the component {}. ", policyId, componentId);
        Component component = null;
        boolean failed = false;
        try {
            component = validateAndLockComponentAndUserBeforeWriteOperation(componentType, componentId, userId, shouldLock);
            return deletePolicy(component, policyId);
        } catch (ComponentException e) {
            failed = true;
            log.error("#deletePolicy - the exception occurred upon update of a policy of the type {} for the component {}: ", policyId, componentId,
                e);
            throw e;
        } finally {
            unlockComponent(shouldLock, failed, component);
        }
    }

    public Either<PolicyDefinition, ResponseFormat> undeclarePolicy(ComponentTypeEnum componentType, String componentId, String policyId,
                                                                    String userId, boolean shouldLock) {
        Either<PolicyDefinition, ResponseFormat> result = null;
        log.trace("#undeclarePolicy - starting to undeclare policy {} on component {}. ", policyId, componentId);
        Wrapper<Component> component = new Wrapper<>();
        try {
            validateAndLockComponentAndUserBeforeWriteOperation(componentType, componentId, userId, shouldLock);
            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreComponentInstances(false);
            componentParametersView.setIgnoreComponentInstancesProperties(false);
            componentParametersView.setIgnorePolicies(false);
            Either<Component, StorageOperationStatus> componentWithFilters = toscaOperationFacade
                .getToscaElement(componentId, componentParametersView);
            if (componentWithFilters.isRight()) {
                return Either
                    .right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(componentWithFilters.right().value())));
            }
            Component containerComponent = componentWithFilters.left().value();
            Optional<PolicyDefinition> policyCandidate = getPolicyForUndeclaration(policyId, containerComponent);
            if (policyCandidate.isPresent()) {
                result = undeclarePolicy(policyCandidate.get(), containerComponent);
            }
            return result;
        } catch (Exception e) {
            log.error("#undeclarePolicy - the exception occurred upon update of a policy of type {} for component {}: ", policyId, componentId, e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR, e.getMessage()));
        } finally {
            if (result == null || result.isRight()) {
                unlockComponent(shouldLock, true, component);
            } else {
                unlockComponent(shouldLock, false, component);
            }
        }
    }

    private Either<PolicyDefinition, ResponseFormat> undeclarePolicy(PolicyDefinition policyDefinition, Component containerComponent) {
        StorageOperationStatus undeclareStatus = propertyDeclarationOrchestrator.unDeclarePropertiesAsPolicies(containerComponent, policyDefinition);
        if (undeclareStatus != StorageOperationStatus.OK) {
            return Either.right(componentsUtils.getResponseFormat(undeclareStatus));
        } else {
            return Either.left(policyDefinition);
        }
    }

    private Optional<PolicyDefinition> getPolicyForUndeclaration(String policyId, Component component) {
        Map<String, PolicyDefinition> policies = component.getPolicies();
        if (MapUtils.isNotEmpty(policies) && policies.containsKey(policyId)) {
            return Optional.of(policies.get(policyId));
        }
        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties =
            MapUtils.isEmpty(component.getComponentInstancesProperties()) ? new HashMap<>() : component.getComponentInstancesProperties();
        for (Map.Entry<String, List<ComponentInstanceProperty>> instancePropertyEntry : componentInstancesProperties.entrySet()) {
            Optional<ComponentInstanceProperty> propertyCandidate = getPropertyForDeclaredPolicy(policyId, instancePropertyEntry.getValue());
            if (propertyCandidate.isPresent()) {
                return Optional.of(PolicyUtils.getDeclaredPolicyDefinition(instancePropertyEntry.getKey(), propertyCandidate.get()));
            }
        }
        return Optional.empty();
    }

    private Optional<ComponentInstanceProperty> getPropertyForDeclaredPolicy(String policyId,
                                                                             List<ComponentInstanceProperty> componentInstanceProperties) {
        for (ComponentInstanceProperty property : componentInstanceProperties) {
            Optional<GetPolicyValueDataDefinition> getPolicyCandidate = property.safeGetGetPolicyValues().stream()
                .filter(getPolicyValue -> getPolicyValue.getPolicyId().equals(policyId)).findAny();
            if (getPolicyCandidate.isPresent()) {
                return Optional.of(property);
            }
        }
        return Optional.empty();
    }

    public PolicyDefinition updatePolicyTargets(ComponentTypeEnum componentTypeEnum, String componentId, String policyId,
                                                Map<PolicyTargetType, List<String>> targets, String userId) {
        log.debug("updating the policy id {} targets with the components {}. ", policyId, componentId);
        boolean failed = false;
        try {
            //not right error response
            Component component = validateAndLockComponentAndUserBeforeWriteOperation(componentTypeEnum, componentId, userId, true);
            return validateAndUpdatePolicyTargets(component, policyId, targets);
        } catch (ComponentException e) {
            failed = true;
            throw e;
        } finally {
            unlockComponentById(failed, componentId);
        }
    }

    private PolicyDefinition validateAndUpdatePolicyTargets(Component component, String policyId, Map<PolicyTargetType, List<String>> targets) {
        validateTargetsExistAndTypesCorrect(component.getUniqueId(), targets);
        return updateTargets(component.getUniqueId(), component.getPolicyById(policyId), targets, policyId);
    }

    private Component validateTargetsExistAndTypesCorrect(String componentId, Map<PolicyTargetType, List<String>> targets) {
        Either<Component, StorageOperationStatus> componentEither = toscaOperationFacade.getToscaFullElement(componentId);
        if (componentEither.isRight()) {
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(componentEither.right().value()));
        }
        Component parentComponent = componentEither.left().value();
        return validateTargetExists(parentComponent, targets.entrySet());
    }

    private Component validateTargetExists(Component parentComponent, Set<Map.Entry<PolicyTargetType, List<String>>> entries) {
        for (Map.Entry<PolicyTargetType, List<String>> entry : entries) {
            checkTargetNotExistOnComponentByType(parentComponent, entry);
        }
        return parentComponent;
    }

    private Component checkTargetNotExistOnComponentByType(Component parentComponent, Map.Entry<PolicyTargetType, List<String>> targetEntry) {
        for (String id : targetEntry.getValue()) {
            if (checkNotPresenceInComponentByType(parentComponent, id, targetEntry.getKey().getName())) {
                throw new ByActionStatusComponentException(ActionStatus.POLICY_TARGET_DOES_NOT_EXIST, id);
            }
        }
        return parentComponent;
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
    public List<PropertyDataDefinition> getPolicyProperties(ComponentTypeEnum componentType, String componentId, String policyId, String userId) {
        log.debug("#getPolicyProperties - fetching policy properties for component {} and policy {}", componentId, policyId);
        try {
            Component component = validateContainerComponentAndUserBeforeReadOperation(componentType, componentId, userId);
            return getPolicyById(component, policyId).getProperties();
        } finally {
            janusGraphDao.commit();
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
    public List<PropertyDataDefinition> updatePolicyProperties(ComponentTypeEnum componentType, String componentId, String policyId,
                                                               PropertyDataDefinition[] properties, String userId, boolean shouldLock) {
        List<PropertyDataDefinition> result;
        Component component = null;
        log.trace("#updatePolicyProperties - starting to update properties of the policy {} on the component {}. ", policyId, componentId);
        boolean failed = true;
        try {
            component = validateAndLockComponentAndUserBeforeWriteOperation(componentType, componentId, userId, shouldLock);
            failed = false;
            result = setComponentValidateUpdatePolicyProperties(policyId, properties, component);
        } finally {
            if (shouldLock && !failed) {
                unlockComponent(failed, component);
            }
        }
        return result;
    }

    @Override
    public Either<List<PolicyDefinition>, ResponseFormat> declareProperties(String userId, String componentId, ComponentTypeEnum componentTypeEnum,
                                                                            ComponentInstInputsMap componentInstInputsMap) {
        return declarePropertiesToPolicies(userId, componentId, componentTypeEnum, componentInstInputsMap, true, false);
    }

    private Either<List<PolicyDefinition>, ResponseFormat> declarePropertiesToPolicies(String userId, String componentId,
                                                                                       ComponentTypeEnum componentTypeEnum,
                                                                                       ComponentInstInputsMap componentInstInputsMap,
                                                                                       boolean shouldLock, boolean inTransaction) {
        Either<List<PolicyDefinition>, ResponseFormat> result = null;
        org.openecomp.sdc.be.model.Component component = null;
        try {
            validateUserExists(userId);
            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreComponentInstances(false);
            componentParametersView.setIgnoreComponentInstancesProperties(false);
            componentParametersView.setIgnorePolicies(false);
            componentParametersView.setIgnoreUsers(false);
            component = validateComponentExists(componentId, componentTypeEnum, componentParametersView);
            if (shouldLock) {
                lockComponent(component, DECLARE_PROPERTIES_TO_POLICIES);
            }
            validateCanWorkOnComponent(component, userId);
            Either<List<PolicyDefinition>, StorageOperationStatus> declarePropertiesEither = propertyDeclarationOrchestrator
                .declarePropertiesToPolicies(component, componentInstInputsMap);
            if (declarePropertiesEither.isRight()) {
                return Either.right(componentsUtils.getResponseFormat(declarePropertiesEither.right().value()));
            }
            result = Either.left(declarePropertiesEither.left().value());
            return result;
        } finally {
            if (!inTransaction) {
                commitOrRollback(result);
            }
            // unlock resource
            if (shouldLock && component != null) {
                graphLockOperation.unlockComponent(componentId, componentTypeEnum.getNodeType());
            }
        }
    }

    private List<PropertyDataDefinition> setComponentValidateUpdatePolicyProperties(String policyId, PropertyDataDefinition[] properties,
                                                                                    Component component) {
        Set<String> updatedPropertyNames = Arrays.stream(properties).map(PropertyDataDefinition::getName).collect(Collectors.toSet());
        PolicyDefinition policyDefinition = validateAndUpdatePolicyProperties(component, policyId, properties);
        return getFilteredProperties(policyDefinition.getProperties(), updatedPropertyNames);
    }

    private List<PropertyDataDefinition> getFilteredProperties(List<PropertyDataDefinition> all, Set<String> filtered) {
        return all.stream().filter(pd -> filtered.contains(pd.getName())).collect(Collectors.toList());
    }

    private void unlockComponent(boolean shouldLock, boolean result, Component component) {
        if (shouldLock && component != null) {
            unlockComponent(result, component);
        }
    }

    private void unlockComponent(boolean shouldLock, boolean result, Wrapper<Component> component) {
        if (shouldLock && !component.isEmpty()) {
            unlockComponent(result, component.getInnerElement());
        }
    }

    private PolicyDefinition getPolicyById(Component component, String policyId) {
        PolicyDefinition policyById = component.getPolicyById(policyId);
        if (policyById == null) {
            String cmptId = component.getUniqueId();
            log.debug("#getPolicyById - policy with id {} does not exist on component with id {}", policyId, cmptId);
            throw new ByActionStatusComponentException(ActionStatus.POLICY_NOT_FOUND_ON_CONTAINER, policyId, cmptId);
        }
        return policyById;
    }

    private PolicyDefinition createPolicy(String policyTypeName, Component component) {
        PolicyTypeDefinition policyTypeDefinition = validatePolicyTypeOnCreatePolicy(policyTypeName, component);
        return addPolicyToComponent(policyTypeDefinition, component);
    }

    private PolicyDefinition addPolicyToComponent(PolicyTypeDefinition policyType, Component component) {
        Either<PolicyDefinition, StorageOperationStatus> associatePolicyToComponent = toscaOperationFacade
            .associatePolicyToComponent(component.getUniqueId(), new PolicyDefinition(policyType), getNextPolicyCounter(component.getPolicies()));
        if (associatePolicyToComponent.isRight()) {
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(associatePolicyToComponent.right().value()));
        }
        return associatePolicyToComponent.left().value();
    }

    private PolicyTypeDefinition validatePolicyTypeOnCreatePolicy(String policyTypeName, Component component) {
        final var latestPolicyTypeByType = policyTypeOperation.getLatestPolicyTypeByType(policyTypeName, component.getModel());
        if (latestPolicyTypeByType.isRight()) {
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(latestPolicyTypeByType.right().value()));
        }
        return validatePolicyTypeNotExcluded(latestPolicyTypeByType.left().value(), component);
    }

    private PolicyTypeDefinition validatePolicyTypeNotExcluded(PolicyTypeDefinition policyType, Component component) {
        if (getExcludedPolicyTypesByComponent(component).contains(policyType.getType())) {
            throw new ByActionStatusComponentException(ActionStatus.EXCLUDED_POLICY_TYPE, policyType.getType(),
                getComponentOrResourceTypeName(component));
        }
        return policyType;
    }

    private String getComponentOrResourceTypeName(Component component) {
        return component.getComponentType() == ComponentTypeEnum.SERVICE ? ComponentTypeEnum.SERVICE.name()
            : ((Resource) component).getResourceType().name();
    }

    private Component validateAndLockComponentAndUserBeforeWriteOperation(ComponentTypeEnum componentType, String componentId, String userId,
                                                                          boolean shouldLock) {
        Component component = validateContainerComponentAndUserBeforeReadOperation(componentType, componentId, userId);
        validateComponentIsTopologyTemplate(component);
        validateCanWorkOnComponent(component, userId);
        lockComponent(component, shouldLock, "policyWritingOperation");
        return component;
    }

    private Component validateComponentIsTopologyTemplate(Component component) {
        if (!component.isTopologyTemplate()) {
            log.error("#validateComponentIsTopologyTemplate - policy association to a component of Tosca type {} is not allowed. ",
                component.getToscaType());
            throw new ByActionStatusComponentException(ActionStatus.RESOURCE_CANNOT_CONTAIN_POLICIES,
                "#validateAndLockComponentAndUserBeforeWriteOperation", component.getUniqueId(), component.getToscaType());
        }
        return component;
    }

    private Component validateContainerComponentAndUserBeforeReadOperation(ComponentTypeEnum componentType, String componentId, String userId) {
        log.trace("#validateContainerComponentAndUserBeforeReadOperation - starting to validate the user {} before policy processing. ", userId);
        validateUserExists(userId);
        return validateComponentExists(componentType, componentId);
    }

    private Component validateComponentExists(ComponentTypeEnum componentType, String componentId) {
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnorePolicies(false);
        filter.setIgnoreUsers(false);
        filter.setIgnoreComponentInstances(false);
        filter.setIgnoreGroups(false);
        return validateComponentExists(componentId, componentType, filter);
    }

    private PolicyDefinition validateAndUpdatePolicy(Component component, PolicyDefinition policy) {
        PolicyDefinition policyById = getPolicyById(component, policy.getUniqueId());
        PolicyDefinition policyDefinition = validateUpdatePolicyBeforeUpdate(policy, policyById, component.getPolicies());
        return updatePolicyOfComponent(component, policyDefinition);
    }

    private PolicyDefinition validateAndUpdatePolicyProperties(Component component, String policyId, PropertyDataDefinition[] properties) {
        PolicyDefinition policyById = getPolicyById(component, policyId);
        policyById = validateUpdatePolicyPropertiesBeforeUpdate(policyById, properties);
        return updatePolicyOfComponent(component.getUniqueId(), policyById);
    }

    private PolicyDefinition updatePolicyOfComponent(String componentId, PolicyDefinition policy) {
        return toscaOperationFacade.updatePolicyOfComponent(componentId, policy, PromoteVersionEnum.MINOR).left()
            .on(ce -> componentExceptionPolicyDefinition(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(ce))));
    }

    private PolicyDefinition validateUpdatePolicyPropertiesBeforeUpdate(PolicyDefinition policy, PropertyDataDefinition[] newProperties) {
        if (CollectionUtils.isEmpty(policy.getProperties())) {
            log.error(
                "#validateUpdatePolicyPropertiesBeforeUpdate - failed to update properites of the policy. Properties were not found on the policy. ");
            throw new ByActionStatusComponentException(ActionStatus.PROPERTY_NOT_FOUND);
        }
        return updatePropertyValues(policy, newProperties);
    }

    private PolicyDefinition updatePropertyValues(PolicyDefinition policy, PropertyDataDefinition[] newProperties) {
        Map<String, PropertyDataDefinition> oldProperties = policy.getProperties().stream()
            .collect(toMap(PropertyDataDefinition::getName, Function.identity()));
        for (PropertyDataDefinition newProperty : newProperties) {
            if (!oldProperties.containsKey(newProperty.getName())) {
                log.error("#updatePropertyValues - failed to update properites of the policy {}. Properties were not found on the policy. ",
                    policy.getName());
                throw new ByActionStatusComponentException(ActionStatus.PROPERTY_NOT_FOUND, newProperty.getName());
            }
            String newPropertyValueEither = updateInputPropertyObjectValue(newProperty);
            oldProperties.get(newProperty.getName()).setValue(newPropertyValueEither);
        }
        return policy;
    }

    private PolicyDefinition deletePolicy(Component component, String policyId) {
        PolicyDefinition policyById = getPolicyById(component, policyId);
        return removePolicyFromComponent(component, policyById);
    }

    private PolicyDefinition updatePolicyOfComponent(Component component, PolicyDefinition policy) {
        Either<PolicyDefinition, StorageOperationStatus> updatePolicyRes = toscaOperationFacade
            .updatePolicyOfComponent(component.getUniqueId(), policy, PromoteVersionEnum.MINOR);
        if (updatePolicyRes.isRight()) {
            log.error("#updatePolicyOfComponent - failed to update policy {} of the component {}. The status is {}. ", policy.getUniqueId(),
                component.getName(), updatePolicyRes.right().value());
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(updatePolicyRes.right().value()));
        } else {
            log.trace("#updatePolicyOfComponent - the policy with the name {} was updated. ", updatePolicyRes.left().value().getName());
            return updatePolicyRes.left().value();
        }
    }

    private PolicyDefinition removePolicyFromComponent(Component component, PolicyDefinition policy) {
        StorageOperationStatus updatePolicyStatus = toscaOperationFacade.removePolicyFromComponent(component.getUniqueId(), policy.getUniqueId());
        if (updatePolicyStatus != StorageOperationStatus.OK) {
            log.error("#removePolicyFromComponent - failed to remove policy {} from the component {}. The status is {}. ", policy.getUniqueId(),
                component.getName(), updatePolicyStatus);
            throw new ByActionStatusComponentException(componentsUtils.convertFromStorageResponse(updatePolicyStatus));
        } else {
            log.trace("#removePolicyFromComponent - the policy with the name {} was deleted. ", updatePolicyStatus);
            return policy;
        }
    }

    private PolicyDefinition validateUpdatePolicyBeforeUpdate(PolicyDefinition recievedPolicy, PolicyDefinition oldPolicy,
                                                              Map<String, PolicyDefinition> policies) {
        Either<PolicyDefinition, ActionStatus> policyDefinitionActionStatusEither = validatePolicyFields(recievedPolicy,
            new PolicyDefinition(oldPolicy), policies);
        if (policyDefinitionActionStatusEither.isRight()) {
            throw new ByActionStatusComponentException(policyDefinitionActionStatusEither.right().value(), recievedPolicy.getName());
        }
        return policyDefinitionActionStatusEither.left().value();
    }

    private PolicyDefinition updateTargets(String componentId, PolicyDefinition policy, Map<PolicyTargetType, List<String>> targets,
                                           String policyId) {
        if (policy == null) {
            throw new ByActionStatusComponentException(ActionStatus.POLICY_NOT_FOUND_ON_CONTAINER, policyId, componentId);
        }
        PolicyDefinition updatedPolicy = setPolicyTargets(policy, targets);
        return updatePolicyOfComponent(componentId, updatedPolicy);
    }
}
