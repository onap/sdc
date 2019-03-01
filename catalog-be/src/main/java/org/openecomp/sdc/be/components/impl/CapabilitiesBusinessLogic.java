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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.validation.CapabilitiesValidation;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.CapabilitiesOperation;
import org.openecomp.sdc.be.model.operations.api.ICapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("capabilitiesBusinessLogic")
public class CapabilitiesBusinessLogic extends BaseBusinessLogic {
    private static final Logger LOGGER = LoggerFactory.getLogger(CapabilitiesBusinessLogic.class);
    private static final String FAILED_TO_LOCK_COMPONENT_RESPONSE_IS
            = "Failed to lock component {}. Response is {}";
    private static final String DELETE_CAPABILITIES = "deleteCapability";
    private static final String GET_CAPABILITIES = "getCapabilities";
    private static final String EXCEPTION_OCCURRED_DURING_CAPABILITIES
            = "Exception occurred during {}. Response is {}";

    @Autowired
    private CapabilitiesOperation capabilitiesOperation;
    @Autowired
    private CapabilitiesValidation capabilitiesValidation;
    @Autowired
    private ICapabilityTypeOperation capabilityTypeOperation;


    public void setCapabilitiesValidation(CapabilitiesValidation capabilitiesValidation) {
        this.capabilitiesValidation = capabilitiesValidation;
    }

    public void setCapabilitiesOperation(CapabilitiesOperation capabilitiesOperation) {
        this.capabilitiesOperation = capabilitiesOperation;
    }

    public Either<List<CapabilityDefinition>, ResponseFormat> createCapabilities(
            String componentId, List<CapabilityDefinition> capabilityDefinitions,
            User user, String errorContext, boolean lock) {
        validateUserExists(user.getUserId(), errorContext, true);
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither
                = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();
        Either<Boolean, ResponseFormat> capabilitiesValidationEither = capabilitiesValidation
                .validateCapabilities(capabilityDefinitions, storedComponent, false);
        if (capabilitiesValidationEither.isRight()) {
            return Either.right(capabilitiesValidationEither.right().value());
        }

        Either<Boolean, ResponseFormat> lockResult = lockComponentResult(lock,
                storedComponent, errorContext);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }
        try {
            Either<List<CapabilityDefinition>, StorageOperationStatus> result;
            List<CapabilityDefinition> capabilitiesListStoredInComponent = null;
            Map<String, List<CapabilityDefinition>> storedComponentCapabilities
                    = storedComponent.getCapabilities();
            if (org.apache.commons.collections.MapUtils.isNotEmpty(storedComponentCapabilities)) {
                CapabilityDefinition capabilityDefinitionToGetType = capabilityDefinitions.get(0);
                if(Objects.isNull(capabilityDefinitionToGetType)) {
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
                }
                capabilitiesListStoredInComponent
                        = getCapabilityStoredInComponentByType(capabilityDefinitionToGetType
                        .getType(), storedComponentCapabilities);
            }
            List<CapabilityDefinition> capabilitiesDefListToCreate;
            List<CapabilityDefinition> capabilitiesToReturn;
            if (CollectionUtils.isNotEmpty(capabilitiesListStoredInComponent)) {
                capabilitiesDefListToCreate = capabilityDefinitions.stream()
                        .map(capabilityDefinition ->
                                initiateNewCapability(storedComponent, capabilityDefinition))
                        .collect(Collectors.toList());
                capabilitiesToReturn = capabilitiesDefListToCreate;
                capabilitiesDefListToCreate.addAll(capabilitiesListStoredInComponent);
                result = capabilitiesOperation.updateCapabilities(componentId,
                        capabilitiesDefListToCreate);
            } else {
                capabilitiesToReturn = capabilityDefinitions.stream()
                        .map(capabilityDefinition -> initiateNewCapability(
                                storedComponent, capabilityDefinition))
                        .collect(Collectors.toList());
                result = capabilitiesOperation.addCapabilities(componentId, capabilitiesToReturn);
            }
            if (result.isRight()) {
                titanDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(
                        componentsUtils.convertFromStorageResponse(result.right().value(),
                                storedComponent.getComponentType()), ""));
            }
            titanDao.commit();
            return Either.left(capabilitiesToReturn);
        } catch (Exception e) {
            titanDao.rollback();
            LOGGER.error(EXCEPTION_OCCURRED_DURING_CAPABILITIES, "addOrUpdate", e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(),
                        NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    public Either<List<CapabilityDefinition>, ResponseFormat> updateCapabilities(
            String componentId, List<CapabilityDefinition> capabilityDefinitions,
            User user, String errorContext, boolean lock) {
        validateUserExists(user.getUserId(), errorContext, true);
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither
                = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();
        Either<Boolean, ResponseFormat> capabilitiesValidationEither = capabilitiesValidation
                .validateCapabilities(capabilityDefinitions, storedComponent, true);
        if (capabilitiesValidationEither.isRight()) {
            return Either.right(capabilitiesValidationEither.right().value());
        }

        Either<Boolean, ResponseFormat> lockResult = lockComponentResult(lock,
                storedComponent, errorContext);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }
        try {
            Either<List<CapabilityDefinition>, StorageOperationStatus> result;
            List<CapabilityDefinition> capabilitiesListStoredInComponent = null;
            Map<String, List<CapabilityDefinition>> storedComponentCapabilities
                    = storedComponent.getCapabilities();
            if (org.apache.commons.collections.MapUtils.isNotEmpty(storedComponentCapabilities)) {
                CapabilityDefinition capabilityDefinitionToGetType = capabilityDefinitions.get(0);
                if(Objects.isNull(capabilityDefinitionToGetType)) {
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
                }
                capabilitiesListStoredInComponent
                        = getCapabilityStoredInComponentByType(capabilityDefinitionToGetType
                        .getType(), storedComponentCapabilities);
            }
            List<CapabilityDefinition> capabilitiesDefListToUpdate = new ArrayList<>();
            List<CapabilityDefinition> capabilitiesToReturn = null;
            if (CollectionUtils.isNotEmpty(capabilitiesListStoredInComponent)) {
                if (capabilityDefinitions.stream().anyMatch(capabilityDefinition ->
                        isCapabilityUsedInServiceComposition(capabilityDefinition, storedComponent))) {
                    LOGGER.error("Capability can't be edited, since it is"
                            + " used in service composition");
                    return Either.right(componentsUtils.getResponseFormat(
                            ActionStatus.CAPABILITY_UPDATE_NOT_ALLOWED_USED_IN_COMPOSITION));
                }
                for (CapabilityDefinition capabilityDefinitionToUpdate : capabilityDefinitions) {
                    capabilitiesToReturn = capabilitiesListStoredInComponent.stream()
                            .filter(capToUpdate -> capToUpdate.getUniqueId()
                                    .equals(capabilityDefinitionToUpdate.getUniqueId()))
                            .map(capabilityDefinition -> updateCapability(capabilityDefinition,
                                    capabilityDefinitionToUpdate)).collect(Collectors.toList());
                    capabilitiesListStoredInComponent.removeIf(capToUpdate ->
                            capToUpdate.getUniqueId().equals(capabilityDefinitionToUpdate.getUniqueId()));
                    if (CollectionUtils.isNotEmpty(capabilitiesToReturn)) {
                        capabilitiesListStoredInComponent.addAll(capabilitiesToReturn);
                        capabilitiesDefListToUpdate.addAll(capabilitiesListStoredInComponent);
                    } else {
                        Either<List<CapabilityDefinition>, ResponseFormat> capTypeUpdateEither
                                = handleCapabilityTypeUpdateWhenNewTypeExist(storedComponent,
                                storedComponent.getCapabilities(), capabilitiesToReturn, capabilityDefinitionToUpdate);
                        if (capTypeUpdateEither.isRight()) {
                            return Either.right(capTypeUpdateEither.right().value());
                        }
                        capabilitiesDefListToUpdate = capTypeUpdateEither.left().value();
                    }
                }
                result = capabilitiesOperation.updateCapabilities(componentId,
                        capabilitiesDefListToUpdate);
            } else {
                Either<List<CapabilityDefinition>, ResponseFormat> capabilityDefinitionToDelete
                        = handleCapabilityTypeUpdateWhenNewTypeNotExist(capabilityDefinitions,
                        storedComponent, storedComponentCapabilities);
                if (capabilityDefinitionToDelete != null) {
                    return capabilityDefinitionToDelete;
                }
                capabilitiesToReturn = capabilityDefinitions.stream()
                        .map(capabilityDefinition -> initiateNewCapability(
                                storedComponent, capabilityDefinition))
                        .collect(Collectors.toList());
                result = capabilitiesOperation.addCapabilities(componentId, capabilitiesToReturn);
            }
            if (result.isRight()) {
                titanDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(
                        componentsUtils.convertFromStorageResponse(result.right().value(),
                                storedComponent.getComponentType()), ""));
            }

            titanDao.commit();
            return Either.left(capabilitiesToReturn);
        } catch (Exception e) {
            titanDao.rollback();
            LOGGER.error(EXCEPTION_OCCURRED_DURING_CAPABILITIES, "addOrUpdate", e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(),
                        NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    private Either<List<CapabilityDefinition>, ResponseFormat> handleCapabilityTypeUpdateWhenNewTypeExist(
            org.openecomp.sdc.be.model.Component storedComponent, Map<String,
            List<CapabilityDefinition>> storedComponentCapabilities,
            List<CapabilityDefinition> capabilitiesToReturn,
            CapabilityDefinition capabilityDefinitionToUpdate) {
        List<CapabilityDefinition> capabilitiesListStoredInComponent;
        List<CapabilityDefinition> capabilitiesDefsToCreateOrUpdate = new ArrayList<>();
        Optional<CapabilityDefinition> definitionOptional = storedComponentCapabilities
                .values().stream().flatMap(Collection::stream)
                .filter(capabilityDefinition -> capabilityDefinition.getUniqueId()
                        .equals(capabilityDefinitionToUpdate.getUniqueId())).findAny();
        if (!definitionOptional.isPresent()) {
            return Either.right(componentsUtils.getResponseFormat(
                    ActionStatus.CAPABILITY_NOT_FOUND, storedComponent.getUniqueId()));
        }
        CapabilityDefinition capabilityDefinitionToDelete = definitionOptional.get();

        capabilitiesListStoredInComponent = getCapabilityStoredInComponentByType(
                capabilityDefinitionToUpdate.getType(), storedComponentCapabilities);
        Either<List<CapabilityDefinition>, StorageOperationStatus> deleteCapabilityEither
                = deleteCapability(storedComponent, storedComponentCapabilities, capabilityDefinitionToDelete);
        if (deleteCapabilityEither.isRight()) {
            titanDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(deleteCapabilityEither.right().value()));
        }
        capabilitiesToReturn.add(initiateNewCapability(storedComponent, capabilityDefinitionToUpdate));

        capabilitiesDefsToCreateOrUpdate.addAll(capabilitiesToReturn);
        capabilitiesDefsToCreateOrUpdate.addAll(capabilitiesListStoredInComponent);
        return Either.left(capabilitiesDefsToCreateOrUpdate);
    }

    private Either<List<CapabilityDefinition>, ResponseFormat> handleCapabilityTypeUpdateWhenNewTypeNotExist(
            List<CapabilityDefinition> capabilityDefinitions,
            org.openecomp.sdc.be.model.Component storedComponent,
            Map<String, List<CapabilityDefinition>> storedComponentCapabilities) {
        for (CapabilityDefinition capabilityDefinitionToUpdate : capabilityDefinitions) {

            Optional<CapabilityDefinition> definitionOptional = storedComponentCapabilities.values()
                    .stream().flatMap(Collection::stream)
                    .filter(capabilityDefinition -> capabilityDefinition.getUniqueId()
                            .equals(capabilityDefinitionToUpdate.getUniqueId())).findAny();
            if (!definitionOptional.isPresent()) {
                return Either.right(componentsUtils.getResponseFormat(
                        ActionStatus.CAPABILITY_NOT_FOUND, storedComponent.getUniqueId()));
            }
            CapabilityDefinition capabilityDefinitionToDelete = definitionOptional.get();
            Boolean isCapabilityUsedInServiceComposition = isCapabilityUsedInServiceComposition(
                    capabilityDefinitionToDelete, storedComponent);
            if (isCapabilityUsedInServiceComposition) {
                LOGGER.error("Capability {} can't be edited, since it is used in service composition",
                        capabilityDefinitionToDelete.getUniqueId());
                return Either.right(componentsUtils.getResponseFormat(
                        ActionStatus.CAPABILITY_UPDATE_NOT_ALLOWED_USED_IN_COMPOSITION,
                        capabilityDefinitionToDelete.getName()));
            }
            Either<List<CapabilityDefinition>, StorageOperationStatus> deleteCapabilityEither
                    = deleteCapability(storedComponent, storedComponentCapabilities,
                    capabilityDefinitionToDelete);
            if (deleteCapabilityEither.isRight()) {
                titanDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(deleteCapabilityEither.right().value()));
            }
        }
        return null;
    }

    public Either<CapabilityDefinition, ResponseFormat> getCapability(
            String componentId,
            String capabilityToGet, User user, boolean lock) {
        validateUserExists(user.getUserId(), GET_CAPABILITIES, true);
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither
                = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();

        Either<Boolean, ResponseFormat> lockResult = lockComponentResult(lock,
                storedComponent, GET_CAPABILITIES);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }
        try {
            List<CapabilityDefinition> capabilityDefinitions = storedComponent.getCapabilities()
                    .values().stream()
                    .flatMap(Collection::stream).collect(Collectors.toList());
            if (capabilityDefinitions.isEmpty()) {
                return Either.right(componentsUtils.getResponseFormat(
                        ActionStatus.CAPABILITY_NOT_FOUND, componentId));
            }

            CapabilityDefinition capabilityDefinitionToReturn;
            Optional<CapabilityDefinition> capabilityDefinitionOptional
                    = capabilityDefinitions.stream()
                    .filter(capabilityDefinition -> capabilityDefinition.getUniqueId()
                            .equals(capabilityToGet)).findAny();
            if (capabilityDefinitionOptional.isPresent()) {
                capabilityDefinitionToReturn = capabilityDefinitionOptional.get();
            } else {
                return Either.right(componentsUtils.getResponseFormat(
                        ActionStatus.CAPABILITY_NOT_FOUND, componentId));
            }

            return Either.left(capabilityDefinitionToReturn);
        } catch (Exception e) {
            LOGGER.error(EXCEPTION_OCCURRED_DURING_CAPABILITIES, "get", e);
            return Either.right(componentsUtils.getResponseFormat(
                    ActionStatus.CAPABILITY_NOT_FOUND, componentId));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(),
                        NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType()
                                .getValue()));
            }
        }
    }

    public Either<CapabilityDefinition, ResponseFormat> deleteCapability(
            String componentId, String capabilityIdToDelete, User user,
            boolean lock) {
        validateUserExists(user.getUserId(), DELETE_CAPABILITIES, true);
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither
                = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();

        Either<Boolean, ResponseFormat> lockResult = lockComponentResult(lock,
                storedComponent, DELETE_CAPABILITIES);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }

        try {
            Map<String, List<CapabilityDefinition>> storedComponentCapabilities
                    = storedComponent.getCapabilities();
            if (storedComponentCapabilities.isEmpty()) {
                return Either.right(componentsUtils.getResponseFormat(
                        ActionStatus.CAPABILITY_NOT_FOUND, componentId));
            }

            Optional<CapabilityDefinition> definitionOptional = storedComponentCapabilities
                    .values().stream().flatMap(Collection::stream)
                    .filter(capabilityDefinition -> capabilityDefinition.getUniqueId()
                            .equals(capabilityIdToDelete)).findAny();
            if (!definitionOptional.isPresent()) {
                return Either.right(componentsUtils.getResponseFormat(
                        ActionStatus.CAPABILITY_NOT_FOUND, componentId));
            }
            CapabilityDefinition capabilityDefinitionToDelete = definitionOptional.get();
            Boolean isCapabilityUsedInServiceComposition
                    = isCapabilityUsedInServiceComposition(capabilityDefinitionToDelete, storedComponent);
            if (isCapabilityUsedInServiceComposition) {
                LOGGER.error("Capability {} can't be deleted, since it is used in service composition",
                        capabilityDefinitionToDelete.getUniqueId());
                return Either.right(componentsUtils.getResponseFormat(
                        ActionStatus.CAPABILITY_DELETION_NOT_ALLOWED_USED_IN_COMPOSITION,
                        capabilityDefinitionToDelete.getName()));
            }

            Either<List<CapabilityDefinition>, StorageOperationStatus> result
                    = deleteCapability(storedComponent, storedComponentCapabilities,
                    capabilityDefinitionToDelete);
            if (result.isRight()) {
                titanDao.rollback();
                LOGGER.error("Failed to delete capability  from component {}. Response is {}",
                        storedComponent.getName(), result.right().value());
                return Either.right(componentsUtils.getResponseFormat(
                        componentsUtils.convertFromStorageResponse(result.right().value(),
                                storedComponent.getComponentType())));
            }

            titanDao.commit();
            return Either.left(capabilityDefinitionToDelete);
        } catch (Exception e) {
            LOGGER.error(EXCEPTION_OCCURRED_DURING_CAPABILITIES, "delete", e);
            titanDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CAPABILITY_NOT_FOUND));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(),
                        NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    private Either<List<CapabilityDefinition>, StorageOperationStatus> deleteCapability(
            org.openecomp.sdc.be.model.Component storedComponent,
            Map<String, List<CapabilityDefinition>> storedComponentCapabilities,
            CapabilityDefinition capabilityDefinitionToDelete) {
        List<CapabilityDefinition> capabilitiesListStoredInComponent =
                getCapabilityStoredInComponentByType(capabilityDefinitionToDelete.getType(),
                        storedComponentCapabilities);

        capabilitiesListStoredInComponent.removeIf(capabilityDefinition ->
                capabilityDefinition.getUniqueId().equals(capabilityDefinitionToDelete.getUniqueId()));
        Either<List<CapabilityDefinition>, StorageOperationStatus> result;
        if (capabilitiesListStoredInComponent.isEmpty()) {
            StorageOperationStatus operationStatus = capabilitiesOperation.deleteCapabilities(storedComponent,
                    capabilityDefinitionToDelete.getType());
            if (StorageOperationStatus.OK.equals(operationStatus)) {
                result = Either.left(Collections.singletonList(capabilityDefinitionToDelete));
            } else {
                result = Either.right(operationStatus);
            }
        } else {
            result = capabilitiesOperation.updateCapabilities(storedComponent.getUniqueId(),
                    capabilitiesListStoredInComponent);
        }
        return result;
    }


    private Either<org.openecomp.sdc.be.model.Component, ResponseFormat> getComponentDetails(
            String componentId) {
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreCapabilities(false);
        filter.setIgnoreCapabiltyProperties(false);
        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus>
                componentStorageOperationStatusEither = toscaOperationFacade
                .getToscaElement(componentId, filter);
        if (componentStorageOperationStatusEither.isRight()) {
            StorageOperationStatus errorStatus = componentStorageOperationStatusEither.right().value();
            LOGGER.error("Failed to fetch component information by component id {}, Response is {}",
                    componentId, errorStatus);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils
                    .convertFromStorageResponse(errorStatus)));
        }
        return Either.left(componentStorageOperationStatusEither.left().value());
    }

    private Either<Boolean, ResponseFormat> lockComponentResult(
            boolean lock, org.openecomp.sdc.be.model.Component component,
            String action) {
        if (lock) {
            Either<Boolean, ResponseFormat> lockResult = lockComponent(component.getUniqueId(),
                    component, action);
            if (lockResult.isRight()) {
                LOGGER.debug(FAILED_TO_LOCK_COMPONENT_RESPONSE_IS, component.getName(),
                        lockResult.right().value().getFormattedMessage());
                titanDao.rollback();
                return Either.right(lockResult.right().value());
            }
        }
        return Either.left(true);
    }

    private List<CapabilityDefinition> getCapabilityStoredInComponentByType(
            String capabilityType, Map<String,
            List<CapabilityDefinition>> capabilities) {
        Optional<Map.Entry<String, List<CapabilityDefinition>>> entryOptional
                = capabilities.entrySet().stream().
                filter(map -> map.getKey().equals(capabilityType)).findFirst();
        return entryOptional.map(Map.Entry::getValue).orElse(null);

    }

    private CapabilityDefinition initiateNewCapability(
            org.openecomp.sdc.be.model.Component component,
            CapabilityDefinition capabilityDefinition) {
        if (StringUtils.isEmpty(capabilityDefinition.getUniqueId()))
            capabilityDefinition.setUniqueId(UUID.randomUUID().toString());
        if (StringUtils.isEmpty(capabilityDefinition.getOwnerId()))
            capabilityDefinition.setOwnerId(component.getUniqueId());
        if (StringUtils.isEmpty(capabilityDefinition.getOwnerName()))
            capabilityDefinition.setOwnerName(component.getName());
        capabilityDefinition.setLeftOccurrences(capabilityDefinition.getMaxOccurrences());
        return capabilityDefinition;
    }

    private CapabilityDefinition updateCapability(CapabilityDefinition storedCapability,
                                                  CapabilityDefinition capabilityToUpdate) {
        storedCapability.setName(capabilityToUpdate.getName());
        storedCapability.setDescription(capabilityToUpdate.getDescription());
        storedCapability.setType(capabilityToUpdate.getType());
        storedCapability.setValidSourceTypes(capabilityToUpdate.getValidSourceTypes());
        storedCapability.setMinOccurrences(capabilityToUpdate.getMinOccurrences());
        storedCapability.setMaxOccurrences(capabilityToUpdate.getMaxOccurrences());

        return storedCapability;
    }


    private Boolean isCapabilityUsedInServiceComposition(
            CapabilityDefinition capabilityDefinition,
            org.openecomp.sdc.be.model.Component component) {
        Either<List<org.openecomp.sdc.be.model.Component>, StorageOperationStatus>
                componentList = toscaOperationFacade
                .getParentComponents(component.getUniqueId());
        if (componentList.isRight()) {
            return Boolean.FALSE;
        }
        return componentList.left().value().stream().flatMap(parentComponent -> parentComponent
                .getComponentInstancesRelations().stream())
                .flatMap(requirementCapabilityRelDef -> requirementCapabilityRelDef.getRelationships().stream())
                .anyMatch(capabilityRequirementRelationship -> capabilityRequirementRelationship
                        .getRelation().getCapabilityUid().equals(capabilityDefinition.getUniqueId()));
    }

    public Either<Map<String, CapabilityTypeDefinition>, ResponseFormat> getAllCapabilityTypes() {
        Either<Map<String, CapabilityTypeDefinition>, TitanOperationStatus> capabilityTypeCacheAll =
                capabilityTypeOperation.getAllCapabilityTypes();
        if (capabilityTypeCacheAll.isRight()) {
            TitanOperationStatus operationStatus = capabilityTypeCacheAll.right().value();
            if (TitanOperationStatus.NOT_FOUND == operationStatus) {
                BeEcompErrorManager.getInstance().logInternalDataError("FetchCapabilityTypes", "Capability types are "
                                + "not loaded",
                        BeEcompErrorManager.ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.DATA_TYPE_CANNOT_BE_EMPTY));
            } else {
                BeEcompErrorManager.getInstance().logInternalFlowError("FetchCapabilityTypes", "Failed to fetch capability "
                                + "types",
                        BeEcompErrorManager.ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
        }
        return Either.left(capabilityTypeCacheAll.left().value());
    }
}
