/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 *  Copyright (C) 2021 Nordix Foundation. All rights reserved.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.zone-instance.component.ts
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("componentInterfaceOperationBusinessLogic")
public class ComponentInterfaceOperationBusinessLogic extends BaseBusinessLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentInterfaceOperationBusinessLogic.class);
    private final ComponentValidations componentValidations;

    @Autowired
    public ComponentInterfaceOperationBusinessLogic(final IElementOperation elementDao, final IGroupOperation groupOperation,
                                                    final IGroupInstanceOperation groupInstanceOperation,
                                                    final IGroupTypeOperation groupTypeOperation, final InterfaceOperation interfaceOperation,
                                                    final InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                                    final ArtifactsOperations artifactToscaOperation,
                                                    final ComponentValidations componentValidations) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
        this.componentValidations = componentValidations;
    }

    public Optional<ComponentInstance> updateComponentInstanceInterfaceOperation(final String componentId, final String componentInstanceId,
                                                                                 final InterfaceDefinition interfaceDefinition,
                                                                                 final ComponentTypeEnum componentTypeEnum,
                                                                                 final Wrapper<ResponseFormat> errorWrapper, final boolean shouldLock)
        throws BusinessLogicException {
        final Component component = getComponent(componentId);
        final Optional<ComponentInstance> componentInstanceOptional = componentValidations.getComponentInstance(component, componentInstanceId);
        ResponseFormat responseFormat;
        if (componentInstanceOptional.isEmpty()) {
            responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND);
            LOGGER.debug("Failed to found component instance with id {}, error: {}", componentInstanceId, responseFormat);
            errorWrapper.setInnerElement(responseFormat);
            return Optional.empty();
        }
        Map<String, List<ComponentInstanceInterface>> componentInstancesInterfaceMap = component.getComponentInstancesInterfaces();
        if (MapUtils.isEmpty(componentInstancesInterfaceMap)) {
            componentInstancesInterfaceMap = new HashMap<>();
            component.setComponentInstancesInterfaces(componentInstancesInterfaceMap);
        }
        final List<ComponentInstanceInterface> componentInstanceInterfaceList = componentInstancesInterfaceMap.get(componentInstanceId);
        if (CollectionUtils.isEmpty(componentInstanceInterfaceList)) {
            responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND);
            LOGGER.debug("Failed to found component instance with id {}, error: {}", componentInstanceId, responseFormat);
            errorWrapper.setInnerElement(responseFormat);
            return Optional.empty();
        }
        final Optional<OperationDataDefinition> optionalOperationDataDefinition = interfaceDefinition.getOperations().values().stream().findFirst();
        if (optionalOperationDataDefinition.isEmpty()) {
            responseFormat = componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND);
            LOGGER.debug("Failed to found interface operation on component instance with id {}, error: {}", componentInstanceId, responseFormat);
            errorWrapper.setInnerElement(responseFormat);
            return Optional.empty();
        }
        final OperationDataDefinition updatedOperationDataDefinition = optionalOperationDataDefinition.get();
        final Optional<ComponentInstanceInterface> optionalComponentInstanceInterface = componentInstanceInterfaceList.stream().filter(
                ci -> ci.getOperations().values().stream().anyMatch(
                    operationDataDefinition -> operationDataDefinition.getUniqueId().equalsIgnoreCase(updatedOperationDataDefinition.getUniqueId())))
            .findFirst();
        if (optionalComponentInstanceInterface.isEmpty()) {
            responseFormat = componentsUtils.getResponseFormat(ActionStatus.INTERFACE_NOT_FOUND_IN_COMPONENT);
            LOGGER
                .debug("Failed to found ComponentInstanceInterface on component instance with id {}, error: {}", componentInstanceId, responseFormat);
            errorWrapper.setInnerElement(responseFormat);
            return Optional.empty();
        }
        updateOperationDefinitionImplementation(updatedOperationDataDefinition);
        optionalComponentInstanceInterface.get().getOperations().replace(updatedOperationDataDefinition.getName(), updatedOperationDataDefinition);
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(componentId, component, "Update Interface Operation on Component instance");
                wasLocked = true;
            }
            final StorageOperationStatus status = toscaOperationFacade.updateComponentInstanceInterfaces(component, componentInstanceId);
            if (status != StorageOperationStatus.OK) {
                janusGraphDao.rollback();
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                LOGGER.error("Exception occurred when updating Component Instance Interfaces {}", responseFormat);
                errorWrapper.setInnerElement(responseFormat);
                return Optional.empty();
            }
            final ComponentParametersView componentFilter = new ComponentParametersView();
            componentFilter.disableAll();
            componentFilter.setIgnoreUsers(false);
            componentFilter.setIgnoreComponentInstances(false);
            componentFilter.setIgnoreInterfaces(false);
            componentFilter.setIgnoreComponentInstancesInterfaces(false);
            final Either<Component, StorageOperationStatus> operationStatusEither = toscaOperationFacade
                .updateComponentInstanceMetadataOfTopologyTemplate(component, componentFilter);
            if (operationStatusEither.isRight()) {
                janusGraphDao.rollback();
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                LOGGER.error("Exception occurred when updating Component Instance Topology template {}", responseFormat);
                errorWrapper.setInnerElement(responseFormat);
                return Optional.empty();
            }
            janusGraphDao.commit();
        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER.error("Exception occurred when updating Interface Operation on Component Instance: {}", e.getMessage(), e);
            responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            errorWrapper.setInnerElement(responseFormat);
            throw new BusinessLogicException(responseFormat);
        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }
        return componentInstanceOptional;
    }

    public Optional<Component> updateResourceInterfaceOperation(final String componentId,
                                                                final InterfaceDefinition interfaceDefinition,
                                                                final ComponentTypeEnum componentTypeEnum,
                                                                final Wrapper<ResponseFormat> errorWrapper,
                                                                final boolean shouldLock) throws BusinessLogicException {
        final var component = getComponent(componentId);
        ResponseFormat responseFormat;

        Map<String, InterfaceDefinition> componentInterfaceMap = component.getInterfaces();
        final String interfaceDefinitionType = interfaceDefinition.getType();
        if (MapUtils.isEmpty(componentInterfaceMap)) {
            componentInterfaceMap = new HashMap<>();
            componentInterfaceMap.put(interfaceDefinitionType, interfaceDefinition);
            component.setInterfaces(componentInterfaceMap);
        } else {
            InterfaceDefinition componentInterfaceDefinition = componentInterfaceMap.get(interfaceDefinitionType);
            if (componentInterfaceDefinition == null) {
                componentInterfaceDefinition = interfaceDefinition;
                componentInterfaceMap.put(interfaceDefinitionType, interfaceDefinition);
            }

            final Map<String, OperationDataDefinition> interfaceDefinitionOperations = interfaceDefinition.getOperations();
            final Optional<OperationDataDefinition> optionalOperationDataDefinition = interfaceDefinitionOperations.values().stream().findFirst();
            if (optionalOperationDataDefinition.isEmpty()) {
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND);
                LOGGER.debug("Failed to found interface operation on provided InterfaceDefinition {}, error: {}",
                    interfaceDefinitionType, responseFormat);
                errorWrapper.setInnerElement(responseFormat);
                return Optional.empty();
            }
            final var updatedOperationDataDefinition = optionalOperationDataDefinition.get();
            updateOperationDefinitionImplementation(updatedOperationDataDefinition);
            Map<String, OperationDataDefinition> componentOperationDataDefinitionMap = componentInterfaceDefinition.getOperations();
            if (MapUtils.isEmpty(componentOperationDataDefinitionMap)) {
                componentOperationDataDefinitionMap = new HashMap<>();
                componentInterfaceDefinition.setOperations(componentOperationDataDefinitionMap);
            }
            componentOperationDataDefinitionMap.replace(updatedOperationDataDefinition.getName(), updatedOperationDataDefinition);
        }
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(componentId, component, "Update Interface Operation on Component instance");
                wasLocked = true;
            }
            final StorageOperationStatus status = toscaOperationFacade.updateComponentInterfaces(component, interfaceDefinitionType);
            if (status != StorageOperationStatus.OK) {
                janusGraphDao.rollback();
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                LOGGER.error("Exception occurred when updating Component Instance Interfaces {}", responseFormat);
                errorWrapper.setInnerElement(responseFormat);
                return Optional.empty();
            }
            janusGraphDao.commit();
        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER.error("Exception occurred when updating Interface Operation on Component Instance: ", e);
            responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            errorWrapper.setInnerElement(responseFormat);
            throw new BusinessLogicException(responseFormat);
        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }
        return Optional.of(component);
    }

    public Optional<Component> createInterfaceOperationInResource(final String componentId, final InterfaceDefinition interfaceDefinition,
                                                                  final ComponentTypeEnum componentTypeEnum,
                                                                  final Wrapper<ResponseFormat> errorWrapper, final boolean shouldLock)
        throws BusinessLogicException {
        final Component component = getComponent(componentId);
        ResponseFormat responseFormat;
        final String componentInterfaceUpdatedKey = interfaceDefinition.getType();

        Map<String, InterfaceDefinition> componentInterfaceMap = component.getInterfaces();
        if (MapUtils.isEmpty(componentInterfaceMap)) {
            componentInterfaceMap = new HashMap<>();
            component.setInterfaces(componentInterfaceMap);
        }

        interfaceDefinition.setUniqueId(componentInterfaceUpdatedKey);
        interfaceDefinition.setToscaResourceName(componentInterfaceUpdatedKey);
        interfaceDefinition.setUserCreated(true);

        final Optional<OperationDataDefinition> optionalOperationDataDefinition = interfaceDefinition.getOperations().values().stream().findFirst();
        if (optionalOperationDataDefinition.isEmpty()) {
            responseFormat = componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NOT_FOUND);
            LOGGER.debug("Failed to found interface operation on component instance with id {}, error: {}", componentId, responseFormat);
            errorWrapper.setInnerElement(responseFormat);
            return Optional.empty();
        }

        final OperationDataDefinition updatedOperationDataDefinition = optionalOperationDataDefinition.get();
        updatedOperationDataDefinition.setUniqueId(UUID.randomUUID().toString());

        final InterfaceDefinition interfaceDefinitionFound = componentInterfaceMap.get(componentInterfaceUpdatedKey);
        if (interfaceDefinitionFound != null) {
            final Map<String, OperationDataDefinition> operationsFromComponent = interfaceDefinitionFound.getOperations();
            final String updatedOperationDataDefinitionName = updatedOperationDataDefinition.getName();
            final boolean find = operationsFromComponent.containsKey(updatedOperationDataDefinitionName);
            if (find) {
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.INTERFACE_OPERATION_NAME_ALREADY_IN_USE,
                    updatedOperationDataDefinitionName);
                LOGGER.error("Operation '{}' for Interface '{}' already exist, error: '{}'", updatedOperationDataDefinitionName,
                    componentInterfaceUpdatedKey, responseFormat);
                errorWrapper.setInnerElement(responseFormat);
                return Optional.empty();
            } else {
                operationsFromComponent.put(updatedOperationDataDefinitionName, updatedOperationDataDefinition);
                interfaceDefinition.setOperations(operationsFromComponent);
            }
        }

        componentInterfaceMap.put(componentInterfaceUpdatedKey, interfaceDefinition);

        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(componentId, component, "Update Interface Operation on Component instance");
                wasLocked = true;
            }
            final Either<InterfaceDefinition, StorageOperationStatus> operationStatusEither =
                toscaOperationFacade.addInterfaceToComponent(componentInterfaceUpdatedKey, interfaceDefinition, component);
            if (operationStatusEither.isRight()) {
                janusGraphDao.rollback();
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                LOGGER.error("Exception occurred when updating Component Instance Interfaces {}", responseFormat);
                errorWrapper.setInnerElement(responseFormat);
                return Optional.empty();
            }
            janusGraphDao.commit();
        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER.error("Exception occurred when updating Interface Operation on Component Instance: ", e);
            responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            errorWrapper.setInnerElement(responseFormat);
            throw new BusinessLogicException(responseFormat);
        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }
        return Optional.of(component);
    }

    public User validateUser(final String userId) {
        final User user = userValidations.validateUserExists(userId);
        userValidations.validateUserRole(user, Arrays.asList(Role.DESIGNER, Role.ADMIN));
        return user;
    }

    private void unlockComponent(final String componentUniqueId, final ComponentTypeEnum componentType) {
        graphLockOperation.unlockComponent(componentUniqueId, componentType.getNodeType());
    }

    private void updateOperationDefinitionImplementation(final OperationDataDefinition updatedOperationDataDefinition) {
        final ArtifactDataDefinition artifactInfo = new ArtifactDataDefinition(updatedOperationDataDefinition.getImplementation());
        artifactInfo.setArtifactName(String.format("'%s'", updatedOperationDataDefinition.getImplementation().getArtifactName()));
        updatedOperationDataDefinition.setImplementation(artifactInfo);
    }
}
