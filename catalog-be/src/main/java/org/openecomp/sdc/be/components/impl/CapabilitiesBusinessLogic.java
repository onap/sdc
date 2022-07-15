/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.validation.CapabilitiesValidation;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.CapabilitiesOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("capabilitiesBusinessLogic")
public class CapabilitiesBusinessLogic extends BaseBusinessLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(CapabilitiesBusinessLogic.class);
    private static final String FAILED_TO_LOCK_COMPONENT_RESPONSE_IS = "Failed to lock component {}. Response is {}";
    private static final String DELETE_CAPABILITIES = "deleteCapability";
    private static final String GET_CAPABILITIES = "getCapabilities";
    private static final String EXCEPTION_OCCURRED_DURING_CAPABILITIES = "Exception occurred during {}. Response is {}";
    private final ICapabilityTypeOperation capabilityTypeOperation;
    private CapabilitiesOperation capabilitiesOperation;
    private CapabilitiesValidation capabilitiesValidation;

    @Autowired
    public CapabilitiesBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation, IGroupInstanceOperation groupInstanceOperation,
                                     IGroupTypeOperation groupTypeOperation, GroupBusinessLogic groupBusinessLogic,
                                     InterfaceOperation interfaceOperation, InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                     ICapabilityTypeOperation capabilityTypeOperation, ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
        this.capabilityTypeOperation = capabilityTypeOperation;
    }

    @Autowired
    public void setCapabilitiesValidation(CapabilitiesValidation capabilitiesValidation) {
        this.capabilitiesValidation = capabilitiesValidation;
    }

    @Autowired
    public void setCapabilitiesOperation(CapabilitiesOperation capabilitiesOperation) {
        this.capabilitiesOperation = capabilitiesOperation;
    }

    public Either<List<CapabilityDefinition>, ResponseFormat> createCapabilities(String componentId, List<CapabilityDefinition> capabilityDefinitions,
                                                                                 User user, String errorContext, boolean lock) {
        Either<Component, ResponseFormat> validateUserAndCapabilitiesEither = validateUserAndCapabilities(user, componentId, errorContext,
            capabilityDefinitions);
        if (validateUserAndCapabilitiesEither.isRight()) {
            return Either.right(validateUserAndCapabilitiesEither.right().value());
        }
        Component storedComponent = validateUserAndCapabilitiesEither.left().value();
        Either<Boolean, ResponseFormat> lockResult = lockComponentResult(lock, storedComponent, errorContext);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }
        try {
            return createCapability(componentId, capabilityDefinitions, storedComponent);
        } catch (Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(EXCEPTION_OCCURRED_DURING_CAPABILITIES, "addOrUpdate", e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation
                    .unlockComponent(storedComponent.getUniqueId(), NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    private Either<Component, ResponseFormat> validateUserAndCapabilities(User user, String componentId, String errorContext,
                                                                          List<CapabilityDefinition> capabilityDefinitions) {
        validateUserExists(user.getUserId());
        Either<Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        Component storedComponent = componentEither.left().value();
        Either<Boolean, ResponseFormat> capabilitiesValidationEither = capabilitiesValidation
            .validateCapabilities(capabilityDefinitions, storedComponent, false);
        if (capabilitiesValidationEither.isRight()) {
            return Either.right(capabilitiesValidationEither.right().value());
        }
        return Either.left(storedComponent);
    }

    private Either<List<CapabilityDefinition>, ResponseFormat> createCapability(String componentId, List<CapabilityDefinition> capabilityDefinitions,
                                                                                Component storedComponent) {
        Either<List<CapabilityDefinition>, StorageOperationStatus> result;
        List<CapabilityDefinition> capabilitiesListStoredInComponent = null;
        Map<String, List<CapabilityDefinition>> storedComponentCapabilities = storedComponent.getCapabilities();
        if (MapUtils.isNotEmpty(storedComponentCapabilities)) {
            CapabilityDefinition capabilityDefinitionToGetType = capabilityDefinitions.get(0);
            if (Objects.isNull(capabilityDefinitionToGetType)) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
            capabilitiesListStoredInComponent = getCapabilityStoredInComponentByType(capabilityDefinitionToGetType.getType(),
                storedComponentCapabilities);
        }
        List<CapabilityDefinition> capabilitiesDefListToCreate;
        List<CapabilityDefinition> capabilitiesToReturn;
        if (CollectionUtils.isNotEmpty(capabilitiesListStoredInComponent)) {
            capabilitiesDefListToCreate = capabilityDefinitions.stream()
                .map(capabilityDefinition -> initiateNewCapability(storedComponent, capabilityDefinition)).collect(Collectors.toList());
            capabilitiesToReturn = capabilitiesDefListToCreate;
            capabilitiesDefListToCreate.addAll(capabilitiesListStoredInComponent);
            result = capabilitiesOperation.updateCapabilities(componentId, capabilitiesDefListToCreate);
        } else {
            capabilitiesToReturn = capabilityDefinitions.stream()
                .map(capabilityDefinition -> initiateNewCapability(storedComponent, capabilityDefinition)).collect(Collectors.toList());
            result = capabilitiesOperation.addCapabilities(componentId, capabilitiesToReturn);
        }
        if (result.isRight()) {
            janusGraphDao.rollback();
            return Either.right(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(result.right().value(), storedComponent.getComponentType()), ""));
        }
        Map<String, MapPropertiesDataDefinition> propertiesMap = getCapabilitiesPropertiesDataDefinitionMap(capabilityDefinitions);
        if (MapUtils.isNotEmpty(propertiesMap)) {
            StorageOperationStatus storageOperationStatus = capabilitiesOperation.createOrUpdateCapabilityProperties(componentId, storedComponent.isTopologyTemplate(), propertiesMap);
            if (storageOperationStatus != StorageOperationStatus.OK) {
                janusGraphDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(storageOperationStatus));
            }
        }
        janusGraphDao.commit();
        return Either.left(capabilitiesToReturn);
    }

    public Either<List<CapabilityDefinition>, ResponseFormat> updateCapabilities(String componentId, List<CapabilityDefinition> capabilityDefinitions,
                                                                                 User user, String errorContext, boolean lock) {
        validateUserExists(user.getUserId());
        Either<Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        Component storedComponent = componentEither.left().value();
        Either<Boolean, ResponseFormat> capabilitiesValidationEither = capabilitiesValidation
            .validateCapabilities(capabilityDefinitions, storedComponent, true);
        if (capabilitiesValidationEither.isRight()) {
            return Either.right(capabilitiesValidationEither.right().value());
        }
        Either<Boolean, ResponseFormat> lockResult = lockComponentResult(lock, storedComponent, errorContext);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }
        try {
            Either<List<CapabilityDefinition>, StorageOperationStatus> result;
            List<CapabilityDefinition> capabilitiesListStoredInComponent = null;
            Map<String, List<CapabilityDefinition>> storedComponentCapabilities = storedComponent.getCapabilities();
            if (org.apache.commons.collections.MapUtils.isNotEmpty(storedComponentCapabilities)) {
                CapabilityDefinition capabilityDefinitionToGetType = capabilityDefinitions.get(0);
                if (Objects.isNull(capabilityDefinitionToGetType)) {
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
                }
                capabilitiesListStoredInComponent = getCapabilityStoredInComponentByType(capabilityDefinitionToGetType.getType(),
                    storedComponentCapabilities);
            }
            List<CapabilityDefinition> capabilitiesDefListToUpdate = new ArrayList<>();
            List<CapabilityDefinition> capabilitiesToReturn = null;
            if (CollectionUtils.isNotEmpty(capabilitiesListStoredInComponent)) {
                if (capabilityDefinitions.stream()
                    .anyMatch(capabilityDefinition -> isCapabilityUsedInServiceComposition(capabilityDefinition, storedComponent))) {
                    LOGGER.error("Capability can't be edited, since it is" + " used in service composition");
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.CAPABILITY_UPDATE_NOT_ALLOWED_USED_IN_COMPOSITION));
                }
                for (CapabilityDefinition capabilityDefinitionToUpdate : capabilityDefinitions) {
                    capabilitiesToReturn = capabilitiesListStoredInComponent.stream()
                        .filter(capToUpdate -> capToUpdate.getUniqueId().equals(capabilityDefinitionToUpdate.getUniqueId()))
                        .map(capabilityDefinition -> updateCapability(capabilityDefinition, capabilityDefinitionToUpdate, storedComponent))
                        .collect(Collectors.toList());
                    capabilitiesListStoredInComponent
                        .removeIf(capToUpdate -> capToUpdate.getUniqueId().equals(capabilityDefinitionToUpdate.getUniqueId()));
                    if (CollectionUtils.isNotEmpty(capabilitiesToReturn)) {
                        capabilitiesListStoredInComponent.addAll(capabilitiesToReturn);
                        capabilitiesDefListToUpdate.addAll(capabilitiesListStoredInComponent);
                    } else {
                        Either<List<CapabilityDefinition>, ResponseFormat> capTypeUpdateEither = handleCapabilityTypeUpdateWhenNewTypeExist(
                            storedComponent, storedComponent.getCapabilities(), capabilitiesToReturn, capabilityDefinitionToUpdate);
                        if (capTypeUpdateEither.isRight()) {
                            return Either.right(capTypeUpdateEither.right().value());
                        }
                        capabilitiesDefListToUpdate = capTypeUpdateEither.left().value();
                    }
                }
                result = capabilitiesOperation.updateCapabilities(componentId, capabilitiesDefListToUpdate);
            } else {
                Either<List<CapabilityDefinition>, ResponseFormat> capabilityDefinitionToDelete = handleCapabilityTypeUpdateWhenNewTypeNotExist(
                    capabilityDefinitions, storedComponent, storedComponentCapabilities);
                if (capabilityDefinitionToDelete != null) {
                    return capabilityDefinitionToDelete;
                }
                capabilitiesToReturn = capabilityDefinitions.stream()
                    .map(capabilityDefinition -> initiateNewCapability(storedComponent, capabilityDefinition)).collect(Collectors.toList());
                result = capabilitiesOperation.addCapabilities(componentId, capabilitiesToReturn);
            }
            if (result.isRight()) {
                janusGraphDao.rollback();
                return Either.right(componentsUtils
                    .getResponseFormat(componentsUtils.convertFromStorageResponse(result.right().value(), storedComponent.getComponentType()), ""));
            }
            Map<String, MapPropertiesDataDefinition> propertiesMap = getCapabilitiesPropertiesDataDefinitionMap(capabilityDefinitions);
            if (MapUtils.isNotEmpty(propertiesMap)) {
                StorageOperationStatus storageOperationStatus = capabilitiesOperation.createOrUpdateCapabilityProperties(componentId, storedComponent.isTopologyTemplate(), propertiesMap);
                if (storageOperationStatus != StorageOperationStatus.OK) {
                    janusGraphDao.rollback();
                    return Either.right(componentsUtils.getResponseFormat(storageOperationStatus));
                }
            }
            janusGraphDao.commit();
            return Either.left(capabilitiesToReturn);
        } catch (Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(EXCEPTION_OCCURRED_DURING_CAPABILITIES, "addOrUpdate", e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation
                    .unlockComponent(storedComponent.getUniqueId(), NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    private Either<List<CapabilityDefinition>, ResponseFormat> handleCapabilityTypeUpdateWhenNewTypeExist(Component storedComponent,
                                                                                                          Map<String, List<CapabilityDefinition>> storedComponentCapabilities,
                                                                                                          List<CapabilityDefinition> capabilitiesToReturn,
                                                                                                          CapabilityDefinition capabilityDefinitionToUpdate) {
        List<CapabilityDefinition> capabilitiesListStoredInComponent;
        List<CapabilityDefinition> capabilitiesDefsToCreateOrUpdate = new ArrayList<>();
        Optional<CapabilityDefinition> definitionOptional = storedComponentCapabilities.values().stream().flatMap(Collection::stream)
            .filter(capabilityDefinition -> capabilityDefinition.getUniqueId().equals(capabilityDefinitionToUpdate.getUniqueId())).findAny();
        if (!definitionOptional.isPresent()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CAPABILITY_NOT_FOUND, storedComponent.getUniqueId()));
        }
        CapabilityDefinition capabilityDefinitionToDelete = definitionOptional.get();
        capabilitiesListStoredInComponent = getCapabilityStoredInComponentByType(capabilityDefinitionToUpdate.getType(), storedComponentCapabilities);
        Either<List<CapabilityDefinition>, StorageOperationStatus> deleteCapabilityEither = deleteCapability(storedComponent,
            storedComponentCapabilities, capabilityDefinitionToDelete);
        if (deleteCapabilityEither.isRight()) {
            janusGraphDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(deleteCapabilityEither.right().value()));
        }
        StorageOperationStatus deleteStorageOperationStatus = capabilitiesOperation
            .deleteCapabilityProperties(storedComponent, buildCapPropKey(capabilityDefinitionToDelete));
        if (deleteStorageOperationStatus != StorageOperationStatus.OK) {
            janusGraphDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(deleteStorageOperationStatus));
        }
        capabilitiesToReturn.add(initiateNewCapability(storedComponent, capabilityDefinitionToUpdate));
        capabilitiesDefsToCreateOrUpdate.addAll(capabilitiesToReturn);
        capabilitiesDefsToCreateOrUpdate.addAll(capabilitiesListStoredInComponent);
        return Either.left(capabilitiesDefsToCreateOrUpdate);
    }

    private Either<List<CapabilityDefinition>, ResponseFormat> handleCapabilityTypeUpdateWhenNewTypeNotExist(
        List<CapabilityDefinition> capabilityDefinitions, Component storedComponent,
        Map<String, List<CapabilityDefinition>> storedComponentCapabilities) {
        for (CapabilityDefinition capabilityDefinitionToUpdate : capabilityDefinitions) {
            Optional<CapabilityDefinition> definitionOptional = storedComponentCapabilities.values().stream().flatMap(Collection::stream)
                .filter(capabilityDefinition -> capabilityDefinition.getUniqueId().equals(capabilityDefinitionToUpdate.getUniqueId())).findAny();
            if (!definitionOptional.isPresent()) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.CAPABILITY_NOT_FOUND, storedComponent.getUniqueId()));
            }
            CapabilityDefinition capabilityDefinitionToDelete = definitionOptional.get();
            Boolean isCapabilityUsedInServiceComposition = isCapabilityUsedInServiceComposition(capabilityDefinitionToDelete, storedComponent);
            if (isCapabilityUsedInServiceComposition) {
                LOGGER.error("Capability {} can't be edited, since it is used in service composition", capabilityDefinitionToDelete.getUniqueId());
                return Either.right(componentsUtils
                    .getResponseFormat(ActionStatus.CAPABILITY_UPDATE_NOT_ALLOWED_USED_IN_COMPOSITION, capabilityDefinitionToDelete.getName()));
            }
            Either<List<CapabilityDefinition>, StorageOperationStatus> deleteCapabilityEither = deleteCapability(storedComponent,
                storedComponentCapabilities, capabilityDefinitionToDelete);
            if (deleteCapabilityEither.isRight()) {
                janusGraphDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(deleteCapabilityEither.right().value()));
            }
            StorageOperationStatus deleteStorageOperationStatus = capabilitiesOperation
                .deleteCapabilityProperties(storedComponent, buildCapPropKey(capabilityDefinitionToDelete));
            if (deleteStorageOperationStatus != StorageOperationStatus.OK) {
                janusGraphDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(deleteStorageOperationStatus));
            }
        }
        return null;
    }

    public Either<CapabilityDefinition, ResponseFormat> getCapability(String componentId, String capabilityToGet, User user, boolean lock) {
        validateUserExists(user.getUserId());
        Either<Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        Component storedComponent = componentEither.left().value();
        Either<Boolean, ResponseFormat> lockResult = lockComponentResult(lock, storedComponent, GET_CAPABILITIES);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }
        try {
            Either<CapabilityDefinition, ResponseFormat> getCapabilityDefinitionEither = getCapabilityDefinition(capabilityToGet, storedComponent);
            if (getCapabilityDefinitionEither.isRight()) {
                return Either.right(getCapabilityDefinitionEither.right().value());
            }
            return Either.left(getCapabilityDefinitionEither.left().value());
        } catch (Exception e) {
            LOGGER.error(EXCEPTION_OCCURRED_DURING_CAPABILITIES, "get", e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CAPABILITY_NOT_FOUND, componentId));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation
                    .unlockComponent(storedComponent.getUniqueId(), NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    private Either<CapabilityDefinition, ResponseFormat> getCapabilityDefinition(String capabilityIdToGet, Component storedComponent) {
        List<CapabilityDefinition> capabilityDefinitions = storedComponent.getCapabilities().values().stream().flatMap(Collection::stream)
            .collect(Collectors.toList());
        if (capabilityDefinitions.isEmpty()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CAPABILITY_NOT_FOUND, storedComponent.getUniqueId()));
        }
        CapabilityDefinition capabilityDefinitionToReturn;
        Optional<CapabilityDefinition> capabilityDefinitionOptional = capabilityDefinitions.stream()
            .filter(capabilityDefinition -> capabilityDefinition.getUniqueId().equals(capabilityIdToGet)).findAny();
        if (capabilityDefinitionOptional.isPresent()) {
            capabilityDefinitionToReturn = capabilityDefinitionOptional.get();
        } else {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CAPABILITY_NOT_FOUND, storedComponent.getUniqueId()));
        }
        return Either.left(capabilityDefinitionToReturn);
    }

    public Either<CapabilityDefinition, ResponseFormat> deleteCapability(String componentId, String capabilityIdToDelete, User user, boolean lock) {
        validateUserExists(user.getUserId());
        Either<Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        Component storedComponent = componentEither.left().value();
        Either<Boolean, ResponseFormat> lockResult = lockComponentResult(lock, storedComponent, DELETE_CAPABILITIES);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }
        try {
            return deleteCapability(capabilityIdToDelete, storedComponent);
        } catch (Exception e) {
            LOGGER.error(EXCEPTION_OCCURRED_DURING_CAPABILITIES, "delete", e);
            janusGraphDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CAPABILITY_NOT_FOUND));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation
                    .unlockComponent(storedComponent.getUniqueId(), NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    private Either<CapabilityDefinition, ResponseFormat> deleteCapability(String capabilityIdToDelete, Component storedComponent) {
        Map<String, List<CapabilityDefinition>> storedComponentCapabilities = storedComponent.getCapabilities();
        if (storedComponentCapabilities.isEmpty()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CAPABILITY_NOT_FOUND, storedComponent.getUniqueId()));
        }
        Either<CapabilityDefinition, ResponseFormat> capabilityDefinitionToDeleteEither = getAndValidateCapabilitiesToDelete(storedComponent,
            storedComponentCapabilities, capabilityIdToDelete);
        if (capabilityDefinitionToDeleteEither.isRight()) {
            return Either.right(capabilityDefinitionToDeleteEither.right().value());
        }
        Either<List<CapabilityDefinition>, StorageOperationStatus> result = deleteCapability(storedComponent, storedComponentCapabilities,
            capabilityDefinitionToDeleteEither.left().value());
        if (result.isRight()) {
            janusGraphDao.rollback();
            LOGGER.error("Failed to delete capability  from component {}. Response is {}", storedComponent.getName(), result.right().value());
            return Either.right(componentsUtils
                .getResponseFormat(componentsUtils.convertFromStorageResponse(result.right().value(), storedComponent.getComponentType())));
        }
        janusGraphDao.commit();
        return Either.left(capabilityDefinitionToDeleteEither.left().value());
    }

    private Either<CapabilityDefinition, ResponseFormat> getAndValidateCapabilitiesToDelete(Component storedComponent,
                                                                                            Map<String, List<CapabilityDefinition>> storedComponentCapabilities,
                                                                                            String capabilityIdToDelete) {
        Optional<CapabilityDefinition> definitionOptional = storedComponentCapabilities.values().stream().flatMap(Collection::stream)
            .filter(capabilityDefinition -> capabilityDefinition.getUniqueId().equals(capabilityIdToDelete)).findAny();
        if (!definitionOptional.isPresent()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CAPABILITY_NOT_FOUND, storedComponent.getUniqueId()));
        }
        CapabilityDefinition capabilityDefinitionToDelete = definitionOptional.get();
        Boolean isCapabilityUsedInServiceComposition = isCapabilityUsedInServiceComposition(capabilityDefinitionToDelete, storedComponent);
        if (isCapabilityUsedInServiceComposition) {
            LOGGER.error("Capability {} can't be deleted, since it is used in service composition", capabilityDefinitionToDelete.getUniqueId());
            return Either.right(componentsUtils
                .getResponseFormat(ActionStatus.CAPABILITY_DELETION_NOT_ALLOWED_USED_IN_COMPOSITION, capabilityDefinitionToDelete.getName()));
        }
        return Either.left(capabilityDefinitionToDelete);
    }

    private Either<List<CapabilityDefinition>, StorageOperationStatus> deleteCapability(Component storedComponent,
                                                                                        Map<String, List<CapabilityDefinition>> storedComponentCapabilities,
                                                                                        CapabilityDefinition capabilityDefinitionToDelete) {
        List<CapabilityDefinition> capabilitiesListStoredInComponent = getCapabilityStoredInComponentByType(capabilityDefinitionToDelete.getType(),
            storedComponentCapabilities);
        capabilitiesListStoredInComponent
            .removeIf(capabilityDefinition -> capabilityDefinition.getUniqueId().equals(capabilityDefinitionToDelete.getUniqueId()));
        Either<List<CapabilityDefinition>, StorageOperationStatus> result;
        if (capabilitiesListStoredInComponent.isEmpty()) {
            StorageOperationStatus operationStatus = capabilitiesOperation
                .deleteCapabilities(storedComponent, capabilityDefinitionToDelete.getType());
            if (StorageOperationStatus.OK.equals(operationStatus)) {
                result = Either.left(Collections.singletonList(capabilityDefinitionToDelete));
            } else {
                result = Either.right(operationStatus);
            }
        } else {
            result = capabilitiesOperation.updateCapabilities(storedComponent.getUniqueId(), capabilitiesListStoredInComponent);
        }
        if (result.isLeft()) {
            StorageOperationStatus deleteStorageOperationStatus = capabilitiesOperation
                .deleteCapabilityProperties(storedComponent, buildCapPropKey(capabilityDefinitionToDelete));
            if (deleteStorageOperationStatus != StorageOperationStatus.OK) {
                result = Either.right(deleteStorageOperationStatus);
            }
        }
        return result;
    }

    private Either<Component, ResponseFormat> getComponentDetails(String componentId) {
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreCapabilities(false);
        filter.setIgnoreCapabiltyProperties(false);
        Either<Component, StorageOperationStatus> componentStorageOperationStatusEither = toscaOperationFacade.getToscaElement(componentId, filter);
        if (componentStorageOperationStatusEither.isRight()) {
            StorageOperationStatus errorStatus = componentStorageOperationStatusEither.right().value();
            LOGGER.error("Failed to fetch component information by component id {}, Response is {}", componentId, errorStatus);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(errorStatus)));
        }
        return Either.left(componentStorageOperationStatusEither.left().value());
    }

    private Either<Boolean, ResponseFormat> lockComponentResult(boolean lock, Component component, String action) {
        if (lock) {
            try {
                lockComponent(component.getUniqueId(), component, action);
            } catch (ComponentException e) {
                LOGGER.debug(FAILED_TO_LOCK_COMPONENT_RESPONSE_IS, component.getName(), e.getMessage());
                janusGraphDao.rollback();
                throw e;
            }
        }
        return Either.left(true);
    }

    private List<CapabilityDefinition> getCapabilityStoredInComponentByType(String capabilityType,
                                                                            Map<String, List<CapabilityDefinition>> capabilities) {
        Optional<Map.Entry<String, List<CapabilityDefinition>>> entryOptional = capabilities.entrySet().stream()
            .filter(map -> map.getKey().equals(capabilityType)).findFirst();
        return entryOptional.map(Map.Entry::getValue).orElse(Collections.emptyList());
    }

    private CapabilityDefinition initiateNewCapability(Component component, CapabilityDefinition capabilityDefinition) {
        if (StringUtils.isEmpty(capabilityDefinition.getUniqueId())) {
            capabilityDefinition.setUniqueId(UUID.randomUUID().toString());
        }
        if (StringUtils.isEmpty(capabilityDefinition.getOwnerId())) {
            capabilityDefinition.setOwnerId(component.getUniqueId());
        }
        if (StringUtils.isEmpty(capabilityDefinition.getOwnerName())) {
            capabilityDefinition.setOwnerName(component.getName());
        }
        capabilityDefinition.setLeftOccurrences(capabilityDefinition.getMaxOccurrences());
        List<ComponentInstanceProperty> capabilityProperties = capabilityDefinition.getProperties();
        initiateProperties(capabilityDefinition, capabilityProperties);
        return capabilityDefinition;
    }

    private void initiateProperties(CapabilityDefinition capabilityDefinition, List<ComponentInstanceProperty> capabilityProperties) {
        if (CollectionUtils.isNotEmpty(capabilityProperties)) {
            capabilityProperties.stream().filter(prop -> prop != null && StringUtils.isEmpty(prop.getUniqueId())).forEach(propDef -> {
                String uid = UniqueIdBuilder.buildRequirementUid(capabilityDefinition.getUniqueId(), propDef.getName());
                propDef.setUniqueId(uid);
                propDef.setParentUniqueId(capabilityDefinition.getUniqueId());
            });
        }
    }

    private CapabilityDefinition updateCapability(CapabilityDefinition storedCapability, CapabilityDefinition capabilityToUpdate,
                                                  Component component) {
        storedCapability.setName(capabilityToUpdate.getName());
        storedCapability.setDescription(capabilityToUpdate.getDescription());
        storedCapability.setType(capabilityToUpdate.getType());
        storedCapability.setValidSourceTypes(capabilityToUpdate.getValidSourceTypes());
        storedCapability.setMinOccurrences(capabilityToUpdate.getMinOccurrences());
        storedCapability.setMaxOccurrences(capabilityToUpdate.getMaxOccurrences());
        if (!storedCapability.getType().equals(capabilityToUpdate.getType())) {
            List<ComponentInstanceProperty> capabilityProperties = capabilityToUpdate.getProperties();
            initiateProperties(capabilityToUpdate, capabilityProperties);
            storedCapability.setProperties(capabilityToUpdate.getProperties());
        }
        if (!storedCapability.getName().equals(capabilityToUpdate.getName())) {
            StorageOperationStatus deleteStorageOperationStatus = capabilitiesOperation
                .deleteCapabilityProperties(component, buildCapPropKey(storedCapability));
            if (deleteStorageOperationStatus != StorageOperationStatus.OK) {
                janusGraphDao.rollback();
            }
        }
        return storedCapability;
    }

    private Boolean isCapabilityUsedInServiceComposition(CapabilityDefinition capabilityDefinition, Component component) {
        Either<List<Component>, StorageOperationStatus> componentList = toscaOperationFacade.getParentComponents(component.getUniqueId());
        if (componentList.isRight()) {
            return Boolean.FALSE;
        }
        return componentList.left().value().stream().flatMap(parentComponent -> parentComponent.getComponentInstancesRelations().stream())
            .flatMap(requirementCapabilityRelDef -> requirementCapabilityRelDef.getRelationships().stream()).anyMatch(
                capabilityRequirementRelationship -> capabilityRequirementRelationship.getRelation().getCapabilityUid()
                    .equals(capabilityDefinition.getUniqueId()));
    }

    public Either<Map<String, CapabilityTypeDefinition>, ResponseFormat> getAllCapabilityTypes(String modelName) {
        Either<Map<String, CapabilityTypeDefinition>, JanusGraphOperationStatus> capabilityTypeCacheAll = capabilityTypeOperation
            .getAllCapabilityTypes(modelName);
        if (capabilityTypeCacheAll.isRight()) {
            JanusGraphOperationStatus operationStatus = capabilityTypeCacheAll.right().value();
            if (JanusGraphOperationStatus.NOT_FOUND == operationStatus) {
                BeEcompErrorManager.getInstance()
                    .logInternalDataError("FetchCapabilityTypes", "Capability types are not loaded", BeEcompErrorManager.ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.DATA_TYPE_CANNOT_BE_EMPTY));
            } else {
                BeEcompErrorManager.getInstance()
                    .logInternalFlowError("FetchCapabilityTypes", "Failed to fetch capability types", BeEcompErrorManager.ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
        }
        return Either.left(capabilityTypeCacheAll.left().value());
    }

    private Map<String, MapPropertiesDataDefinition> getCapabilitiesPropertiesDataDefinitionMap(List<CapabilityDefinition> capabilityDefinitions) {
        CapabilityDefinition capabilityDefinitionToAddOrUpdateCapProp = capabilityDefinitions.get(0);
        List<ComponentInstanceProperty> componentInstanceProperties = null;
        if (Objects.nonNull(capabilityDefinitionToAddOrUpdateCapProp)) {
            componentInstanceProperties = capabilityDefinitionToAddOrUpdateCapProp.getProperties();
        }
        Map<String, MapPropertiesDataDefinition> propertiesMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(componentInstanceProperties)) {
            MapPropertiesDataDefinition dataToCreate = new MapPropertiesDataDefinition();
            for (ComponentInstanceProperty cip : componentInstanceProperties) {
                dataToCreate.put(cip.getName(), new PropertyDataDefinition(cip));
            }
            propertiesMap.put(buildCapPropKey(capabilityDefinitionToAddOrUpdateCapProp), dataToCreate);
        }
        return propertiesMap;
    }

    private String buildCapPropKey(CapabilityDefinition capabilityDefinitionToAddOrUpdateCapProp) {
        return capabilityDefinitionToAddOrUpdateCapProp.getType() + ModelConverter.CAP_PROP_DELIM + capabilityDefinitionToAddOrUpdateCapProp
            .getName();
    }
}
