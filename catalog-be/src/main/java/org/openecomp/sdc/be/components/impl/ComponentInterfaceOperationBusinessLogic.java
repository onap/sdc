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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fj.data.Either;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.PropertyValueConstraintValidationUtil;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("componentInterfaceOperationBusinessLogic")
public class ComponentInterfaceOperationBusinessLogic extends BaseBusinessLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentInterfaceOperationBusinessLogic.class);
    private static final String UPDATE_INTERFACE_OPERATION_ON_COMPONENT_INSTANCE =
        "Update Interface Operation on Component instance";
    private static final String EXCEPTION_OCCURRED_WHEN_UPDATING_COMPONENT_INSTANCE_INTERFACES =
        "Exception occurred when updating Component Instance Interfaces {}";
    private final ComponentValidations componentValidations;
    private final PropertyBusinessLogic propertyBusinessLogic;
    private final ArtifactTypeBusinessLogic artifactTypeBusinessLogic;

    @Autowired
    public ComponentInterfaceOperationBusinessLogic(final IElementOperation elementDao, final IGroupOperation groupOperation,
                                                    final IGroupInstanceOperation groupInstanceOperation,
                                                    final IGroupTypeOperation groupTypeOperation, final InterfaceOperation interfaceOperation,
                                                    final InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                                    final ArtifactsOperations artifactToscaOperation,
                                                    final ComponentValidations componentValidations,
                                                    final PropertyBusinessLogic propertyBusinessLogic,
                                                    final ArtifactTypeBusinessLogic artifactTypeBusinessLogic) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
        this.componentValidations = componentValidations;
        this.propertyBusinessLogic = propertyBusinessLogic;
        this.artifactTypeBusinessLogic = artifactTypeBusinessLogic;
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

        final String model = propertyBusinessLogic.getComponentModelByComponentId(componentId);
        PropertyValueConstraintValidationUtil constraintValidatorUtil = new PropertyValueConstraintValidationUtil();
        Either<Boolean, ResponseFormat> constraintValidatorResponse =
            validateOperationInputConstraints(updatedOperationDataDefinition, constraintValidatorUtil, model);
        if (!isConstraintsValidationSucceed(constraintValidatorResponse, errorWrapper, updatedOperationDataDefinition)) {
            return Optional.empty();
        }
        constraintValidatorResponse = validateOperationArtifactPropertyConstraints(
            updatedOperationDataDefinition, constraintValidatorUtil, model);
        if (!isConstraintsValidationSucceed(constraintValidatorResponse, errorWrapper, updatedOperationDataDefinition)) {
            return Optional.empty();
        }

        updateOperationDefinitionImplementation(updatedOperationDataDefinition);
        optionalComponentInstanceInterface.get().getOperations().replace(updatedOperationDataDefinition.getName(), updatedOperationDataDefinition);
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(componentId, component, UPDATE_INTERFACE_OPERATION_ON_COMPONENT_INSTANCE);
                wasLocked = true;
            }
            final StorageOperationStatus status = toscaOperationFacade.updateComponentInstanceInterfaces(component, componentInstanceId);
            if (status != StorageOperationStatus.OK) {
                janusGraphDao.rollback();
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                LOGGER.error(EXCEPTION_OCCURRED_WHEN_UPDATING_COMPONENT_INSTANCE_INTERFACES, responseFormat);
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

    private Either<Boolean, ResponseFormat> validateOperationInputConstraints (
        OperationDataDefinition operationDataDefinition, PropertyValueConstraintValidationUtil constraintValidatorUtil, String model) {
        return constraintValidatorUtil
            .validatePropertyConstraints(convertOperationInputsToPropertyDefinitions(operationDataDefinition), applicationDataTypeCache,
                model);
    }

    private Either<Boolean, ResponseFormat> validateOperationArtifactPropertyConstraints (
        OperationDataDefinition operationDataDefinition, PropertyValueConstraintValidationUtil constraintValidatorUtil, String model) {
        return constraintValidatorUtil
            .validatePropertyConstraints(convertOperationArtifactPropsToPropertyDefinitions(operationDataDefinition, model), applicationDataTypeCache,
                model);
    }

    private boolean isConstraintsValidationSucceed(Either<Boolean, ResponseFormat> constraintValidatorResponse,
                                                   Wrapper<ResponseFormat> errorWrapper,
                                                   OperationDataDefinition updatedOperationDataDefinition) {
        if (constraintValidatorResponse.isRight()) {
            ResponseFormat responseFormat = constraintValidatorResponse.right().value();
            LOGGER.error("Failed constraints validation on inputs for interface operation: {} - {}",
                updatedOperationDataDefinition.getName(),
                constraintValidatorResponse.right().value());
            errorWrapper.setInnerElement(responseFormat);
            return false;
        }
        return true;
    }

    public Optional<Component> updateResourceInterfaceOperation(final String componentId,
                                                                final String user,
                                                                final InterfaceDefinition interfaceDefinition,
                                                                final ComponentTypeEnum componentTypeEnum,
                                                                final Wrapper<ResponseFormat> errorWrapper,
                                                                final boolean shouldLock) throws BusinessLogicException {
        final var component = getComponent(componentId);
        validateCanWorkOnComponent(component, user);
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
                lockComponent(componentId, component, UPDATE_INTERFACE_OPERATION_ON_COMPONENT_INSTANCE);
                wasLocked = true;
            }
            final StorageOperationStatus status = toscaOperationFacade.updateComponentInterfaces(component, interfaceDefinitionType);
            if (status != StorageOperationStatus.OK) {
                janusGraphDao.rollback();
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                LOGGER.error(EXCEPTION_OCCURRED_WHEN_UPDATING_COMPONENT_INSTANCE_INTERFACES, responseFormat);
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
                lockComponent(componentId, component, UPDATE_INTERFACE_OPERATION_ON_COMPONENT_INSTANCE);
                wasLocked = true;
            }
            final Either<InterfaceDefinition, StorageOperationStatus> operationStatusEither =
                toscaOperationFacade.addInterfaceToComponent(componentInterfaceUpdatedKey, interfaceDefinition, component);
            if (operationStatusEither.isRight()) {
                janusGraphDao.rollback();
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
                LOGGER.error(EXCEPTION_OCCURRED_WHEN_UPDATING_COMPONENT_INSTANCE_INTERFACES, responseFormat);
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

    private List<PropertyDefinition> convertOperationInputsToPropertyDefinitions(final OperationDataDefinition operationDataDefinition) {
        List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        ListDataDefinition<OperationInputDefinition> inputsDefinitionListData = operationDataDefinition.getInputs();
        if (null != inputsDefinitionListData && !inputsDefinitionListData.isEmpty()) {
            List<OperationInputDefinition> inputDefinitionList =
                inputsDefinitionListData.getListToscaDataDefinition();
            for (OperationInputDefinition operationInputDefinition : inputDefinitionList) {
                PropertyDefinition propertyDefinition = new PropertyDefinition();
                propertyDefinition.setValue(operationInputDefinition.getValue());
                propertyDefinition.setUniqueId(operationInputDefinition.getUniqueId());
                propertyDefinition.setType(operationInputDefinition.getType());
                propertyDefinition.setName(operationInputDefinition.getName());
                propertyDefinition.setDefaultValue(operationInputDefinition.getDefaultValue());
                propertyDefinition.setInputPath(operationInputDefinition.getInputPath());
                propertyDefinitions.add(propertyDefinition);
            }
        }
        return propertyDefinitions;
    }

    private List<PropertyDefinition> convertOperationArtifactPropsToPropertyDefinitions(final OperationDataDefinition operationDataDefinition,
                                                                         final String model) {
        List<PropertyDefinition> artifactPropertiesToValidateCollection = new ArrayList<>();
        final ArtifactDataDefinition artifactDataDefinition = operationDataDefinition.getImplementation();
        if (null != artifactDataDefinition) {
            final String artifactType = artifactDataDefinition.getArtifactType();
            final String uniqueId = UniqueIdBuilder.buildArtifactTypeUid(model, artifactType);
            ArtifactTypeDefinition retrievedArtifact = artifactTypeBusinessLogic.getArtifactTypeByUid(uniqueId);
            if (retrievedArtifact != null) {
                List<PropertyDataDefinition> artifactPropertiesList = artifactDataDefinition.getProperties();
                if (null != artifactPropertiesList && !artifactPropertiesList.isEmpty()) {
                    for (PropertyDataDefinition propertyDataDefinition : artifactPropertiesList) {
                        PropertyDefinition propertyDefinition = new PropertyDefinition();
                        propertyDefinition.setConstraints(deserializePropertyConstraints(propertyDataDefinition.getPropertyConstraints()));
                        propertyDefinition.setValue(propertyDataDefinition.getValue());
                        propertyDefinition.setType(propertyDataDefinition.getType());
                        propertyDefinition.setName(propertyDataDefinition.getName());
                        propertyDefinition.setDefaultValue(propertyDataDefinition.getDefaultValue());
                        propertyDefinition.setInputPath(propertyDataDefinition.getInputPath());
                        artifactPropertiesToValidateCollection.add(propertyDefinition);
                    }
                }
            }
        }
        return artifactPropertiesToValidateCollection;
    }

    private List<PropertyConstraint> deserializePropertyConstraints(List<String> constraints) {
        if (CollectionUtils.isNotEmpty(constraints)) {
            Type constraintType = new TypeToken<PropertyConstraint>() {
            }.getType();
            Gson gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyOperation.PropertyConstraintDeserialiser()).create();
            return constraints.stream().map(c -> (PropertyConstraint) gson.fromJson(c, constraintType)).collect(
                Collectors.toList());
        }
        return null;
    }
}
