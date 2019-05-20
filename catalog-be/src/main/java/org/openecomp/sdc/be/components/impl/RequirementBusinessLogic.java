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
import org.openecomp.sdc.be.components.validation.RequirementValidation;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.RequirementOperation;
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

@Component("requirementBusinessLogic")
public class RequirementBusinessLogic extends BaseBusinessLogic {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequirementBusinessLogic.class);
    private static final String FAILED_TO_LOCK_COMPONENT_RESPONSE_IS
            = "Failed to lock component {}. Response is {}";
    private static final String DELETE_REQUIREMENTS = "deleteRequirement";
    private static final String GET_REQUIREMENTS = "getRequirements";
    private static final String EXCEPTION_OCCURRED_DURING_REQUIREMENTS
            = "Exception occurred during {}. Response is {}";

    @Autowired
    private RequirementOperation requirementOperation;
    @Autowired
    private RequirementValidation requirementValidation;


    public void setRequirementOperation(RequirementOperation requirementOperation) {
        this.requirementOperation = requirementOperation;
    }

    public void setRequirementValidation(RequirementValidation requirementValidation) {
        this.requirementValidation = requirementValidation;
    }

    public Either<List<RequirementDefinition>, ResponseFormat> createRequirements(
            String componentId, List<RequirementDefinition> requirementDefinitions,
            User user, String errorContext, boolean lock) {
        validateUserExists(user.getUserId(), errorContext, true);
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither
                = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();

        Either<Boolean, ResponseFormat> requirementsValidationEither = requirementValidation
                .validateRequirements(requirementDefinitions, storedComponent, false);
        if (requirementsValidationEither.isRight()) {
            return Either.right(requirementsValidationEither.right().value());
        }

        Either<Boolean, ResponseFormat> lockResult = lockComponentResult(lock,
                storedComponent, errorContext);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }

        try {
            Either<List<RequirementDefinition>, StorageOperationStatus> result;
            List<RequirementDefinition> requirementsListStoredInComponent = null;
            Map<String, List<RequirementDefinition>> storedComponentRequirements
                    = storedComponent.getRequirements();
            if (org.apache.commons.collections.MapUtils.isNotEmpty(storedComponentRequirements)) {
                RequirementDefinition requirementDefinitionToGetType = requirementDefinitions.get(0);
                if(Objects.isNull(requirementDefinitionToGetType)) {
                     return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
                }
                requirementsListStoredInComponent
                        = getRequirementStoredInComponentByType(requirementDefinitionToGetType
                        .getCapability(), storedComponentRequirements);
            }
            List<RequirementDefinition> requirementsToReturn;
            if (org.apache.commons.collections.CollectionUtils
                    .isNotEmpty(requirementsListStoredInComponent)) {
                List<RequirementDefinition> requirementDefToCreate = requirementDefinitions.stream()
                        .map(requirementDefinition -> initiateNewRequirement(storedComponent, requirementDefinition))
                        .collect(Collectors.toList());
                requirementsToReturn = requirementDefToCreate;
                requirementDefToCreate.addAll(requirementsListStoredInComponent);
                result = requirementOperation.updateRequirement(componentId, requirementDefToCreate);
            } else {
                requirementsToReturn = requirementDefinitions.stream().map(requirementDefinition ->
                        initiateNewRequirement(storedComponent, requirementDefinition))
                        .collect(Collectors.toList());
                result = requirementOperation.addRequirement(componentId, requirementsToReturn);
            }
            if (result.isRight()) {
                janusGraphDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(
                        componentsUtils.convertFromStorageResponse(result.right().value(),
                                storedComponent.getComponentType()), ""));
            }
            janusGraphDao.commit();
            return Either.left(requirementsToReturn);
        } catch (Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(EXCEPTION_OCCURRED_DURING_REQUIREMENTS, "addOrUpdate", e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(),
                        NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    public Either<List<RequirementDefinition>, ResponseFormat> updateRequirements(
            String componentId, List<RequirementDefinition> requirementDefinitions,
            User user, String errorContext, boolean lock) {
        validateUserExists(user.getUserId(), errorContext, true);
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither
                = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();

        Either<Boolean, ResponseFormat> requirementsValidationEither = requirementValidation
                .validateRequirements(requirementDefinitions, storedComponent, true);
        if (requirementsValidationEither.isRight()) {
            return Either.right(requirementsValidationEither.right().value());
        }

        Either<Boolean, ResponseFormat> lockResult = lockComponentResult(lock,
                storedComponent, errorContext);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }
        try {
            Either<List<RequirementDefinition>, StorageOperationStatus> result;
            List<RequirementDefinition> requirementsListStoredInComponent = null;
            Map<String, List<RequirementDefinition>> storedComponentRequirements
                    = storedComponent.getRequirements();
            if (org.apache.commons.collections.MapUtils.isNotEmpty(storedComponentRequirements)) {
                RequirementDefinition requirementDefinitionToGetType = requirementDefinitions.get(0);
                if(Objects.isNull(requirementDefinitionToGetType)) {
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
                }
                requirementsListStoredInComponent
                        = getRequirementStoredInComponentByType(requirementDefinitionToGetType
                        .getCapability(), storedComponentRequirements);
            }
            List<RequirementDefinition> requirementsToReturn = null;
            if (org.apache.commons.collections.CollectionUtils
                    .isNotEmpty(requirementsListStoredInComponent)) {
                List<RequirementDefinition> requirementDefToUpdate = new ArrayList<>();
                if (requirementDefinitions.stream().anyMatch(requirementDefinition ->
                        isRequirementUsedInServiceComposition(requirementDefinition, storedComponent))) {
                    LOGGER.error("Requirement can't be edited, since it is used in service composition");
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus
                            .REQUIREMENT_UPDATE_NOT_ALLOWED_USED_IN_COMPOSITION));
                }
                for (RequirementDefinition requirementDefinitionToUpdate : requirementDefinitions) {
                    requirementsToReturn = requirementsListStoredInComponent.stream()
                            .filter(reqToUpdate -> reqToUpdate.getUniqueId()
                                    .equals(requirementDefinitionToUpdate.getUniqueId()))
                            .map(requirementDefinition -> updateRequirement(requirementDefinition,
                                    requirementDefinitionToUpdate)).collect(Collectors.toList());
                    requirementsListStoredInComponent.removeIf(reqToUpdate ->
                            reqToUpdate.getUniqueId().equals(requirementDefinitionToUpdate.getUniqueId()));

                    if (CollectionUtils.isNotEmpty(requirementsToReturn)) {
                        requirementsListStoredInComponent.addAll(requirementsToReturn);
                        requirementDefToUpdate.addAll(requirementsListStoredInComponent);
                    } else {
                        Either<List<RequirementDefinition>, ResponseFormat> updateCapTypeEither
                                = handleUpdateRequirementCapabilityWhenNewCapabilityExist(storedComponent,
                                storedComponentRequirements,
                                requirementsToReturn, requirementDefinitionToUpdate);
                        if (updateCapTypeEither.isRight()) {
                            return Either.right(updateCapTypeEither.right().value());
                        }
                        requirementDefToUpdate = updateCapTypeEither.left().value();
                    }
                }
                result = requirementOperation.updateRequirement(componentId, requirementDefToUpdate);
            } else {
                Either<List<RequirementDefinition>, ResponseFormat> requirementDefinitionToDelete
                        = handleRequirementCapabilityUpdateWhenNewCapabilityNotExist(requirementDefinitions,
                        storedComponent, storedComponentRequirements);
                if (requirementDefinitionToDelete != null) {
                    return requirementDefinitionToDelete;
                }
                requirementsToReturn = requirementDefinitions.stream().map(requirementDefinition ->
                        initiateNewRequirement(storedComponent, requirementDefinition))
                        .collect(Collectors.toList());
                result = requirementOperation.addRequirement(componentId, requirementsToReturn);
            }
            if (result.isRight()) {
                janusGraphDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(
                        componentsUtils.convertFromStorageResponse(result.right().value(),
                                storedComponent.getComponentType()), ""));
            }
            janusGraphDao.commit();
            return Either.left(requirementsToReturn);
        } catch (Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(EXCEPTION_OCCURRED_DURING_REQUIREMENTS, "addOrUpdate", e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(),
                        NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    private Either<List<RequirementDefinition>, ResponseFormat> handleUpdateRequirementCapabilityWhenNewCapabilityExist(
            org.openecomp.sdc.be.model.Component storedComponent,
            Map<String, List<RequirementDefinition>> storedComponentRequirements,
            List<RequirementDefinition> requirementsToReturn,
            RequirementDefinition requirementDefinitionToUpdate) {
        List<RequirementDefinition> requirementsListStoredInComponent;
        List<RequirementDefinition> requirementDefsToCreateOrUpdate = new ArrayList<>();
        Optional<RequirementDefinition> definitionOptional = storedComponentRequirements
                .values().stream().flatMap(Collection::stream)
                .filter(requirementDefinition -> requirementDefinition.getUniqueId()
                        .equals(requirementDefinitionToUpdate.getUniqueId())).findAny();
        if (!definitionOptional.isPresent()) {
            return Either.right(componentsUtils.getResponseFormat(
                    ActionStatus.REQUIREMENT_NOT_FOUND, storedComponent.getUniqueId()));
        }
        RequirementDefinition requirementDefinitionToDelete = definitionOptional.get();

        requirementsListStoredInComponent = getRequirementStoredInComponentByType(
                requirementDefinitionToUpdate.getCapability(), storedComponentRequirements);
        Either<List<RequirementDefinition>, StorageOperationStatus> deleteRequirementEither
                = deleteRequirement(storedComponent, storedComponentRequirements, requirementDefinitionToDelete);
        if (deleteRequirementEither.isRight()) {
            janusGraphDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(deleteRequirementEither.right().value()));
        }
        requirementsToReturn.add(initiateNewRequirement(storedComponent, requirementDefinitionToUpdate));

        requirementDefsToCreateOrUpdate.addAll(requirementsToReturn);
        requirementDefsToCreateOrUpdate.addAll(requirementsListStoredInComponent);
        return Either.left(requirementDefsToCreateOrUpdate);
    }

    private Either<List<RequirementDefinition>, ResponseFormat> handleRequirementCapabilityUpdateWhenNewCapabilityNotExist(
            List<RequirementDefinition> requirementDefinitions,
            org.openecomp.sdc.be.model.Component storedComponent,
            Map<String, List<RequirementDefinition>> storedComponentRequirements) {
        for (RequirementDefinition requirementDefinitionToUpdate : requirementDefinitions) {

            Optional<RequirementDefinition> definitionOptional = storedComponentRequirements
                    .values().stream().flatMap(Collection::stream)
                    .filter(requirementDefinition -> requirementDefinition.getUniqueId()
                            .equals(requirementDefinitionToUpdate.getUniqueId())).findAny();
            if (!definitionOptional.isPresent()) {
                return Either.right(componentsUtils.getResponseFormat(
                        ActionStatus.REQUIREMENT_NOT_FOUND, storedComponent.getUniqueId()));
            }
            RequirementDefinition requirementDefinitionToDelete = definitionOptional.get();
            Boolean isRequirementUsedInServiceComposition
                    = isRequirementUsedInServiceComposition(requirementDefinitionToDelete, storedComponent);
            if (isRequirementUsedInServiceComposition) {
                LOGGER.error("Requirement {} can't be edited, since it is used in service composition",
                        requirementDefinitionToDelete.getUniqueId());
                return Either.right(componentsUtils.getResponseFormat(ActionStatus
                                .REQUIREMENT_UPDATE_NOT_ALLOWED_USED_IN_COMPOSITION,
                        requirementDefinitionToDelete.getName()));
            }
            Either<List<RequirementDefinition>, StorageOperationStatus> deleteRequirementEither
                    = deleteRequirement(storedComponent, storedComponentRequirements, requirementDefinitionToDelete);
            if (deleteRequirementEither.isRight()) {
                janusGraphDao.rollback();
                return Either.right(componentsUtils.getResponseFormat(deleteRequirementEither.right().value()));
            }
        }
        return null;
    }

    public Either<RequirementDefinition, ResponseFormat> getRequirement(String componentId,
                                                                        String requirementIdToGet, User user, boolean lock) {
        validateUserExists(user.getUserId(), GET_REQUIREMENTS, true);
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();

        Either<Boolean, ResponseFormat> lockResult = lockComponentResult(lock, storedComponent, GET_REQUIREMENTS);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }
        try {

            List<RequirementDefinition> requirementDefinitions = storedComponent.getRequirements().values().stream()
                    .flatMap(Collection::stream).collect(Collectors.toList());
            if (requirementDefinitions.isEmpty()) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.REQUIREMENT_NOT_FOUND, componentId));
            }

            RequirementDefinition requirementDefinitionToReturn;
            Optional<RequirementDefinition> requirementDefinitionOptional = requirementDefinitions.stream()
                    .filter(requirementDefinition -> requirementDefinition.getUniqueId().equals(requirementIdToGet)).findAny();
            if (requirementDefinitionOptional.isPresent()) {
                requirementDefinitionToReturn = requirementDefinitionOptional.get();
            } else {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.REQUIREMENT_NOT_FOUND, componentId));
            }
            return Either.left(requirementDefinitionToReturn);
        } catch (Exception e) {
            LOGGER.error(EXCEPTION_OCCURRED_DURING_REQUIREMENTS, "get", e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.REQUIREMENT_NOT_FOUND, componentId));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(),
                        NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    public Either<RequirementDefinition, ResponseFormat> deleteRequirement(String componentId,
                                                                           String requirementIdToDelete,
                                                                           User user, boolean lock) {
        validateUserExists(user.getUserId(), DELETE_REQUIREMENTS, true);
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> componentEither
                = getComponentDetails(componentId);
        if (componentEither.isRight()) {
            return Either.right(componentEither.right().value());
        }
        org.openecomp.sdc.be.model.Component storedComponent = componentEither.left().value();

        Either<Boolean, ResponseFormat> lockResult = lockComponentResult(lock, storedComponent, DELETE_REQUIREMENTS);
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }

        try {
            Map<String, List<RequirementDefinition>> storedComponentRequirements = storedComponent.getRequirements();
            if (storedComponentRequirements.isEmpty()) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.REQUIREMENT_NOT_FOUND, componentId));
            }

            Optional<RequirementDefinition> definitionOptional = storedComponentRequirements
                    .values().stream().flatMap(Collection::stream)
                    .filter(requirementDefinition -> requirementDefinition.getUniqueId()
                            .equals(requirementIdToDelete)).findAny();
            if (!definitionOptional.isPresent()) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.REQUIREMENT_NOT_FOUND, componentId));
            }
            RequirementDefinition requirementDefinitionToDelete = definitionOptional.get();

            Boolean isRequirementUsedInServiceComposition
                    = isRequirementUsedInServiceComposition(requirementDefinitionToDelete, storedComponent);
            if (isRequirementUsedInServiceComposition) {
                LOGGER.error("Requirement {} can't be deleted, since it is used in service composition",
                        requirementDefinitionToDelete.getUniqueId());
                return Either.right(componentsUtils.getResponseFormat(ActionStatus
                                .CAPABILITY_DELETION_NOT_ALLOWED_USED_IN_COMPOSITION,
                        requirementDefinitionToDelete.getName()));
            }

            Either<List<RequirementDefinition>, StorageOperationStatus> result
                    = deleteRequirement(storedComponent, storedComponentRequirements, requirementDefinitionToDelete);
            if (result.isRight()) {
                janusGraphDao.rollback();
                LOGGER.error("Failed to delete requirement  from component {}. Response is {}",
                        storedComponent.getName(), result.right().value());
                return Either.right(componentsUtils.getResponseFormat(componentsUtils
                        .convertFromStorageResponse(result.right().value(), storedComponent.getComponentType())));
            }
            janusGraphDao.commit();
            return Either.left(requirementDefinitionToDelete);
        } catch (Exception e) {
            LOGGER.error(EXCEPTION_OCCURRED_DURING_REQUIREMENTS, "delete", e);
            janusGraphDao.rollback();
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.REQUIREMENT_NOT_FOUND));
        } finally {
            if (lockResult.isLeft() && lockResult.left().value()) {
                graphLockOperation.unlockComponent(storedComponent.getUniqueId(),
                        NodeTypeEnum.getByNameIgnoreCase(storedComponent.getComponentType().getValue()));
            }
        }
    }

    private Either<List<RequirementDefinition>, StorageOperationStatus> deleteRequirement(
            org.openecomp.sdc.be.model.Component storedComponent, Map<String,
            List<RequirementDefinition>> storedComponentRequirements,
            RequirementDefinition requirementDefinitionToDelete) {
        List<RequirementDefinition> requirementStoredInComponentByType =
                getRequirementStoredInComponentByType(requirementDefinitionToDelete.getCapability(),
                        storedComponentRequirements);
        if(requirementStoredInComponentByType == null) {
            return Either.right(StorageOperationStatus.BAD_REQUEST);
        }
        requirementStoredInComponentByType.removeIf(requirementDefinition ->
                requirementDefinition.getUniqueId().equals(requirementDefinitionToDelete.getUniqueId()));
        Either<List<RequirementDefinition>, StorageOperationStatus> result;
        if (requirementStoredInComponentByType.isEmpty()) {

            StorageOperationStatus operationStatus = requirementOperation.deleteRequirements(storedComponent,
                    requirementDefinitionToDelete.getCapability());
            if (operationStatus.equals(StorageOperationStatus.OK)) {
                result = Either.left(Collections.singletonList(requirementDefinitionToDelete));
            } else {
                result = Either.right(operationStatus);
            }
        } else {
            result = requirementOperation.updateRequirement(storedComponent.getUniqueId(),
                    requirementStoredInComponentByType);
        }
        return result;
    }

    private Either<org.openecomp.sdc.be.model.Component, ResponseFormat> getComponentDetails(String componentId) {
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreRequirements(false);
        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> componentStorageOperationStatusEither
                = toscaOperationFacade.getToscaElement(componentId, filter);
        if (componentStorageOperationStatusEither.isRight()) {
            StorageOperationStatus errorStatus = componentStorageOperationStatusEither.right().value();
            LOGGER.error("Failed to fetch component information by component id {}, Response is {}", componentId, errorStatus);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(errorStatus)));
        }
        return Either.left(componentStorageOperationStatusEither.left().value());
    }

    private Either<Boolean, ResponseFormat> lockComponentResult(boolean lock,
                                                                org.openecomp.sdc.be.model.Component component,
                                                                String action) {
        if (lock) {
            Either<Boolean, ResponseFormat> lockResult = lockComponent(component.getUniqueId(), component, action);
            if (lockResult.isRight()) {
                LOGGER.debug(FAILED_TO_LOCK_COMPONENT_RESPONSE_IS, component.getName(),
                        lockResult.right().value().getFormattedMessage());
                janusGraphDao.rollback();
                return Either.right(lockResult.right().value());
            }
        }
        return Either.left(true);
    }

    private List<RequirementDefinition> getRequirementStoredInComponentByType(
            String capabilityType, Map<String,
            List<RequirementDefinition>> requirements) {

        Optional<Map.Entry<String, List<RequirementDefinition>>> entryOptional
                = requirements.entrySet().stream().filter(map -> map.getKey().equals(capabilityType)).findFirst();
        return entryOptional.map(Map.Entry::getValue).orElse(null);

    }

    private RequirementDefinition initiateNewRequirement(org.openecomp.sdc.be.model.Component component,
                                                         RequirementDefinition requirementDefinition) {
        if (StringUtils.isEmpty(requirementDefinition.getUniqueId()))
            requirementDefinition.setUniqueId(UUID.randomUUID().toString());
        if (StringUtils.isEmpty(requirementDefinition.getOwnerId()))
            requirementDefinition.setOwnerId(component.getUniqueId());
        if (StringUtils.isEmpty(requirementDefinition.getOwnerName()))
            requirementDefinition.setOwnerName(component.getName());
        requirementDefinition.setLeftOccurrences(requirementDefinition.getMaxOccurrences());
        return requirementDefinition;
    }

    private RequirementDefinition updateRequirement(RequirementDefinition storedRequirement,
                                                    RequirementDefinition requirementToUpdate) {
        storedRequirement.setName(requirementToUpdate.getName());
        storedRequirement.setCapability(requirementToUpdate.getCapability());
        storedRequirement.setNode(requirementToUpdate.getNode());
        storedRequirement.setRelationship(requirementToUpdate.getRelationship());
        storedRequirement.setMinOccurrences(requirementToUpdate.getMinOccurrences());
        storedRequirement.setMaxOccurrences(requirementToUpdate.getMaxOccurrences());
        return storedRequirement;
    }

    private Boolean isRequirementUsedInServiceComposition(RequirementDefinition requirementDefinition,
                                                          org.openecomp.sdc.be.model.Component component) {
        Either<List<org.openecomp.sdc.be.model.Component>, StorageOperationStatus> componentList
                = toscaOperationFacade.getParentComponents(component.getUniqueId());
        if (componentList.isRight()) {
            return Boolean.FALSE;
        }
        return componentList.left().value().stream()
                .flatMap(parentComponent -> parentComponent.getComponentInstancesRelations()
                        .stream()).flatMap(requirementCapabilityRelDef -> requirementCapabilityRelDef.getRelationships().stream())
                .anyMatch(capabilityRequirementRelationship -> capabilityRequirementRelationship.getRelation()
                        .getRequirementUid().equals(requirementDefinition.getUniqueId()));
    }
}
