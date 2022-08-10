/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.components.impl;

import static org.openecomp.sdc.be.dao.api.ActionStatus.SUBSTITUTION_FILTER_NOT_FOUND;
import static org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR;

import fj.data.Either;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.validation.NodeFilterValidator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.dto.FilterConstraintDto;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.SubstitutionFilterOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.ui.mapper.FilterConstraintMapper;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("componentSubstitutionFilterBusinessLogic")
public class ComponentSubstitutionFilterBusinessLogic extends BaseBusinessLogic {

    private static final Logger LOGGER = Logger.getLogger(ComponentSubstitutionFilterBusinessLogic.class);
    private final SubstitutionFilterOperation substitutionFilterOperation;
    private final NodeFilterValidator nodeFilterValidator;

    @Autowired
    public ComponentSubstitutionFilterBusinessLogic(final IElementOperation elementDao, final IGroupOperation groupOperation,
                                                    final IGroupInstanceOperation groupInstanceOperation,
                                                    final IGroupTypeOperation groupTypeOperation, final InterfaceOperation interfaceOperation,
                                                    final InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                                    final ArtifactsOperations artifactToscaOperation,
                                                    final SubstitutionFilterOperation substitutionFilterOperation,
                                                    final NodeFilterValidator nodeFilterValidator) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
        this.substitutionFilterOperation = substitutionFilterOperation;
        this.nodeFilterValidator = nodeFilterValidator;
    }

    public Optional<SubstitutionFilterDataDefinition> createSubstitutionFilterIfNotExist(final String componentId, final boolean shouldLock,
                                                                                         final ComponentTypeEnum componentTypeEnum)
        throws BusinessLogicException {
        final Component component = getComponent(componentId);
        Optional<SubstitutionFilterDataDefinition> substitutionFilterDataDefinition = Optional.ofNullable(component.getSubstitutionFilter());
        if (substitutionFilterDataDefinition.isPresent()) {
            return substitutionFilterDataDefinition;
        }
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(component.getUniqueId(), component, "Create Substitution Filter on component");
                wasLocked = true;
            }
            final Either<SubstitutionFilterDataDefinition, StorageOperationStatus> result = substitutionFilterOperation
                .createSubstitutionFilter(componentId);
            if (result.isRight()) {
                janusGraphDao.rollback();
                LOGGER.error(BUSINESS_PROCESS_ERROR, "Failed to Create Substitution filter on component with id {}", componentId);
                throw new BusinessLogicException(componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(result.right().value()), component.getSystemName()));
            }
            substitutionFilterDataDefinition = Optional.ofNullable(result.left().value());
            component.setSubstitutionFilter(substitutionFilterDataDefinition.get());
            janusGraphDao.commit();
            LOGGER.debug("Substitution filter successfully created in component {} . ", component.getSystemName());
        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER
                .error(BUSINESS_PROCESS_ERROR, "Exception occurred during add Component Substitution filter property values: {}", e.getMessage(), e);
            throw new BusinessLogicException(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }
        return substitutionFilterDataDefinition;
    }

    public Optional<SubstitutionFilterDataDefinition> addSubstitutionFilter(final String componentId,
                                                                            final FilterConstraintDto filterConstraint, final boolean shouldLock,
                                                                            final ComponentTypeEnum componentTypeEnum) throws BusinessLogicException {
        final Component component = getComponent(componentId);
        final Either<Boolean, ResponseFormat> response = nodeFilterValidator.validateSubstitutionFilter(component, filterConstraint);
        if (response.isRight()) {
            throw new BusinessLogicException(
                componentsUtils.getResponseFormat(ActionStatus.SUBSTITUTION_FILTER_NOT_FOUND, response.right().value().getFormattedMessage()));
        }
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(component.getUniqueId(), component, "Add Substitution Filter on Component");
                wasLocked = true;
            }
            final SubstitutionFilterPropertyDataDefinition newProperty = new SubstitutionFilterPropertyDataDefinition();
            newProperty.setName(filterConstraint.getPropertyName());
            newProperty.setConstraints(List.of(new FilterConstraintMapper().mapTo(filterConstraint)));
            final Either<SubstitutionFilterDataDefinition, StorageOperationStatus> resultEither = substitutionFilterOperation
                .addPropertyFilter(componentId, component.getSubstitutionFilter(), newProperty);
            if (resultEither.isRight()) {
                janusGraphDao.rollback();
                throw new BusinessLogicException(componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(resultEither.right().value()),
                        component.getSystemName()));
            }
            janusGraphDao.commit();
            LOGGER.debug("Substitution filter successfully created in component {} . ", component.getSystemName());
            return Optional.ofNullable(resultEither.left().value());
        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER
                .error(BUSINESS_PROCESS_ERROR, "Exception occurred during add component substitution filter property values: {}", e.getMessage(), e);
            throw new BusinessLogicException(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }
    }

    public Optional<SubstitutionFilterDataDefinition> updateSubstitutionFilter(final String componentId, final List<FilterConstraintDto> constraints,
                                                                               final boolean shouldLock,
                                                                               final ComponentTypeEnum componentTypeEnum) throws BusinessLogicException {
        final Component component = getComponent(componentId);
        final Either<Boolean, ResponseFormat> response = nodeFilterValidator.validateSubstitutionFilter(component, constraints);
        if (response.isRight()) {
            throw new BusinessLogicException(
                componentsUtils.getResponseFormat(ActionStatus.SUBSTITUTION_FILTER_NOT_FOUND, response.right().value().getFormattedMessage()));
        }
        SubstitutionFilterDataDefinition substitutionFilterDataDefinition = component.getSubstitutionFilter();
        if (substitutionFilterDataDefinition == null) {
            throw new BusinessLogicException(componentsUtils.getResponseFormat(SUBSTITUTION_FILTER_NOT_FOUND));
        }
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(component.getUniqueId(), component, "Update Substitution Filter on Component");
                wasLocked = true;
            }
            final List<SubstitutionFilterPropertyDataDefinition> properties = constraints.stream()
                .map(this::buildSubstitutionFilterPropertyDataDefinition).collect(Collectors.toList());
            final Either<SubstitutionFilterDataDefinition, StorageOperationStatus> result = substitutionFilterOperation
                .updatePropertyFilters(componentId, substitutionFilterDataDefinition, properties);
            if (result.isRight()) {
                janusGraphDao.rollback();
                throw new BusinessLogicException(componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(result.right().value()), component.getSystemName()));
            } else {
                substitutionFilterDataDefinition = result.left().value();
            }
            janusGraphDao.commit();
            LOGGER.debug("Substitution filter successfully updated in component {} . ", component.getSystemName());
        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(BUSINESS_PROCESS_ERROR, this.getClass().getName(),
                "Exception occurred during update component substitution filter property values: {}", e);
            throw new BusinessLogicException(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }
        return Optional.ofNullable(substitutionFilterDataDefinition);
    }

    public Optional<SubstitutionFilterDataDefinition> updateSubstitutionFilter(final String componentId, final FilterConstraintDto filterConstraint,
                                                                               final int index,
                                                                               final boolean shouldLock) throws BusinessLogicException {
        final Component component = getComponent(componentId);
        final Either<Boolean, ResponseFormat> validationResponse = nodeFilterValidator.validateSubstitutionFilter(component, filterConstraint);
        if (validationResponse.isRight()) {
            throw new BusinessLogicException(validationResponse.right().value());
        }
        final SubstitutionFilterDataDefinition substitutionFilterDataDefinition = component.getSubstitutionFilter();
        if (substitutionFilterDataDefinition == null) {
            throw new BusinessLogicException(componentsUtils.getResponseFormat(SUBSTITUTION_FILTER_NOT_FOUND, component.getName()));
        }
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(component.getUniqueId(), component, "Update Substitution Filter on Component");
                wasLocked = true;
            }
            final SubstitutionFilterPropertyDataDefinition substitutionFilterProperty =
                buildSubstitutionFilterPropertyDataDefinition(filterConstraint);
            final Either<SubstitutionFilterDataDefinition, StorageOperationStatus> result =
                substitutionFilterOperation.updatePropertyFilter(componentId, substitutionFilterDataDefinition, substitutionFilterProperty, index);
            if (result.isRight()) {
                janusGraphDao.rollback();
                throw new BusinessLogicException(
                    componentsUtils.getResponseFormatByResource(
                        componentsUtils.convertFromStorageResponse(result.right().value()), component.getSystemName()
                    )
                );
            }
            janusGraphDao.commit();
            LOGGER.debug("Substitution filter successfully updated in component {} . ", component.getSystemName());
            return Optional.ofNullable(result.left().value());
        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(BUSINESS_PROCESS_ERROR, this.getClass().getName(),
                "Exception occurred during update component substitution filter property values: {}", e);
            throw e;
        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), component.getComponentType());
            }
        }
    }

    public Optional<SubstitutionFilterDataDefinition> deleteSubstitutionFilter(final String componentId, final int position, final boolean shouldLock,
                                                                               final ComponentTypeEnum componentTypeEnum)
        throws BusinessLogicException {
        final Component component = getComponent(componentId);
        SubstitutionFilterDataDefinition substitutionFilterDataDefinition = component.getSubstitutionFilter();
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(component.getUniqueId(), component, "Delete substitution Filter on Component");
                wasLocked = true;
            }
            final Either<SubstitutionFilterDataDefinition, StorageOperationStatus> result = substitutionFilterOperation
                .deleteConstraint(componentId, substitutionFilterDataDefinition, position);
            if (result.isRight()) {
                janusGraphDao.rollback();
                throw new BusinessLogicException(componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(result.right().value()), component.getSystemName()));
            } else {
                substitutionFilterDataDefinition = result.left().value();
            }
            janusGraphDao.commit();
            LOGGER.debug("Substitution filter successfully deleted in component {} . ", component.getSystemName());
        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(BUSINESS_PROCESS_ERROR, "Exception occurred during delete component substitution filter property values: {}", e.getMessage(),
                e);
            throw new BusinessLogicException(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }
        return Optional.ofNullable(substitutionFilterDataDefinition);
    }

    private void unlockComponent(final String componentUniqueId, final ComponentTypeEnum componentType) {
        graphLockOperation.unlockComponent(componentUniqueId, componentType.getNodeType());
    }

    public User validateUser(final String userId) {
        final User user = userValidations.validateUserExists(userId);
        userValidations.validateUserRole(user, Arrays.asList(Role.DESIGNER, Role.ADMIN));
        return user;
    }

    private SubstitutionFilterPropertyDataDefinition buildSubstitutionFilterPropertyDataDefinition(final FilterConstraintDto filterConstraint) {
        final var substitutionFilterProperty = new SubstitutionFilterPropertyDataDefinition();
        substitutionFilterProperty.setName(filterConstraint.getPropertyName());
        substitutionFilterProperty.setConstraints(List.of(new FilterConstraintMapper().mapTo(filterConstraint)));
        return substitutionFilterProperty;
    }

    public void addSubstitutionFilterInGraph(String componentId,
                                             ListDataDefinition<SubstitutionFilterPropertyDataDefinition> substitutionFilterProperties)
        throws BusinessLogicException {
        Either<SubstitutionFilterDataDefinition, StorageOperationStatus> updateSubstitutionFilter;
        Optional<SubstitutionFilterDataDefinition> substitutionFilter = createSubstitutionFilterIfNotExist(componentId, true,
            ComponentTypeEnum.SERVICE);
        if (substitutionFilter.isPresent()) {
            for (SubstitutionFilterPropertyDataDefinition filter : substitutionFilterProperties.getListToscaDataDefinition()) {
                updateSubstitutionFilter = substitutionFilterOperation.addPropertyFilter(componentId, substitutionFilter.get(), filter);
                if (updateSubstitutionFilter.isRight()) {
                    throw new BusinessLogicException(componentsUtils
                        .getResponseFormat(componentsUtils.convertFromStorageResponse(updateSubstitutionFilter.right().value())));
                }
            }
        }
    }

}
