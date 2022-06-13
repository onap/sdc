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

import static org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.utils.CINodeFilterUtils;
import org.openecomp.sdc.be.components.impl.utils.NodeFilterConstraintAction;
import org.openecomp.sdc.be.components.validation.NodeFilterValidator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeFilterConstraintType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.UploadNodeFilterInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeFilterOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("componentNodeFilterBusinessLogic")
public class ComponentNodeFilterBusinessLogic extends BaseBusinessLogic {

    private static final Logger LOGGER = Logger.getLogger(ComponentNodeFilterBusinessLogic.class.getName());
    private final NodeFilterOperation nodeFilterOperation;
    private final NodeFilterValidator nodeFilterValidator;

    @Autowired
    public ComponentNodeFilterBusinessLogic(final IElementOperation elementDao, final IGroupOperation groupOperation,
                                            final IGroupInstanceOperation groupInstanceOperation, final IGroupTypeOperation groupTypeOperation,
                                            final InterfaceOperation interfaceOperation,
                                            final InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                            final ArtifactsOperations artifactToscaOperation, final NodeFilterOperation nodeFilterOperation,
                                            final NodeFilterValidator nodeFilterValidator) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
        this.nodeFilterOperation = nodeFilterOperation;
        this.nodeFilterValidator = nodeFilterValidator;
    }

    public Optional<CINodeFilterDataDefinition> createNodeFilterIfNotExist(final String componentId, final String componentInstanceId,
                                                                           final boolean shouldLock, final ComponentTypeEnum componentTypeEnum)
        throws BusinessLogicException {
        final Component component = getComponent(componentId);
        final Optional<ComponentInstance> componentInstance = getComponentInstance(componentInstanceId, component);
        Optional<CINodeFilterDataDefinition> filterDataDefinition = getCiNodeFilterDataDefinition(componentInstance);
        if (filterDataDefinition.isPresent()) {
            return filterDataDefinition;
        }
        final Either<CINodeFilterDataDefinition, StorageOperationStatus> result;
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(component.getUniqueId(), component, "Create Node Filter on component");
                wasLocked = true;
            }
            result = nodeFilterOperation.createNodeFilter(componentId, componentInstanceId);
            if (result.isRight()) {
                janusGraphDao.rollback();
                LOGGER.error(BUSINESS_PROCESS_ERROR, "Failed to Create Node filter on component with id {}", componentId);
                throw new BusinessLogicException(componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(result.right().value()), component.getSystemName()));
            } else {
                filterDataDefinition = Optional.ofNullable(result.left().value());
            }
            janusGraphDao.commit();
            LOGGER.debug("Node filter successfully created in component {} . ", component.getSystemName());
        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(BUSINESS_PROCESS_ERROR, "Exception occurred during add Component node filter property values: {}", e.getMessage(), e);
            throw new BusinessLogicException(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }
        return filterDataDefinition;
    }

    public Optional<String> deleteNodeFilterIfExists(final String componentId, final String componentInstanceId, final boolean shouldLock,
                                                     final ComponentTypeEnum componentTypeEnum) throws BusinessLogicException {
        final Component component = getComponent(componentId);
        final Optional<CINodeFilterDataDefinition> nodeFilterDataDefinition = getCiNodeFilterDataDefinition(componentInstanceId, component);
        if (!nodeFilterDataDefinition.isPresent()) {
            return Optional.ofNullable(componentInstanceId);
        }
        final Either<String, StorageOperationStatus> result;
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(component.getUniqueId(), component, "Delete Node Filter from component");
                wasLocked = true;
            }
            result = nodeFilterOperation.deleteNodeFilter(component, componentInstanceId);
            if (result.isRight()) {
                LOGGER.error(BUSINESS_PROCESS_ERROR, "Failed to delete node filter in component {}. Response is {}. ", component.getName(),
                    result.right().value());
                janusGraphDao.rollback();
                throw new BusinessLogicException(componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(result.right().value()), component.getSystemName()));
            }
            janusGraphDao.commit();
            LOGGER.debug("Node filter successfully deleted in component {} . ", component.getSystemName());
        } catch (final Exception e) {
            LOGGER.error(BUSINESS_PROCESS_ERROR, "Exception occurred during delete deleting node filter: {}", e.getMessage(), e);
            janusGraphDao.rollback();
            throw new BusinessLogicException(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }
        return Optional.ofNullable(result.left().value());
    }

    private String getComponentInstancePropertyType(Component component, String componentInstanceId, String propertyName) {
        List<ComponentInstanceProperty> componentInstanceProperties = component.getComponentInstancesProperties().get(componentInstanceId);
        if (!componentInstanceProperties.isEmpty()) {
            for (ComponentInstanceProperty componentInstanceProperty : componentInstanceProperties) {
                if (componentInstanceProperty.getName().equals(propertyName)) {
                    return componentInstanceProperty.getType();
                }
            }
        }
        return null;
    }

    public Optional<CINodeFilterDataDefinition> addNodeFilter(final String componentId, final String componentInstanceId,
                                                              final NodeFilterConstraintAction action, final String propertyName,
                                                              final String constraint, final boolean shouldLock,
                                                              final ComponentTypeEnum componentTypeEnum,
                                                              final NodeFilterConstraintType nodeFilterConstraintType, final String capabilityName)
        throws BusinessLogicException {
        final Component component = getComponent(componentId);
        CINodeFilterDataDefinition nodeFilterDataDefinition = validateAndReturnNodeFilterDefinition(componentInstanceId, action, constraint,
            component, nodeFilterConstraintType, capabilityName);
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(component.getUniqueId(), component, "Add Node Filter on Component");
                wasLocked = true;
            }
            final RequirementNodeFilterPropertyDataDefinition requirementNodeFilterPropertyDataDefinition = new RequirementNodeFilterPropertyDataDefinition();
            requirementNodeFilterPropertyDataDefinition.setName(propertyName);
            requirementNodeFilterPropertyDataDefinition.setConstraints(Collections.singletonList(constraint));
            requirementNodeFilterPropertyDataDefinition.setType(getComponentInstancePropertyType(component, componentInstanceId, propertyName));
            final Either<CINodeFilterDataDefinition, StorageOperationStatus> result = addNewNodeFilter(componentId, componentInstanceId,
                nodeFilterConstraintType, nodeFilterDataDefinition, requirementNodeFilterPropertyDataDefinition, capabilityName);
            if (result.isRight()) {
                janusGraphDao.rollback();
                throw new BusinessLogicException(componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(result.right().value()), component.getSystemName()));
            } else {
                nodeFilterDataDefinition = result.left().value();
            }
            janusGraphDao.commit();
            LOGGER.debug("Node filter successfully created in component {} . ", component.getSystemName());
        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(BUSINESS_PROCESS_ERROR, "Exception occurred during add component node filter property values: {}", e.getMessage(), e);
            throw new BusinessLogicException(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }
        return Optional.ofNullable(nodeFilterDataDefinition);
    }

    public Optional<CINodeFilterDataDefinition> deleteNodeFilter(final String componentId, final String componentInstanceId,
                                                                 final NodeFilterConstraintAction action, final String constraint, final int position,
                                                                 final boolean shouldLock, final ComponentTypeEnum componentTypeEnum,
                                                                 final NodeFilterConstraintType nodeFilterConstraintType)
        throws BusinessLogicException {
        final Component component = getComponent(componentId);
        CINodeFilterDataDefinition nodeFilterDataDefinition = validateAndReturnNodeFilterDefinition(componentInstanceId, action, constraint,
            component, nodeFilterConstraintType, "");
        boolean wasLocked = false;
        try {
            if (shouldLock) {
                lockComponent(component.getUniqueId(), component, "Add Node Filter on Component");
                wasLocked = true;
            }
            final Either<CINodeFilterDataDefinition, StorageOperationStatus> result = nodeFilterOperation
                .deleteConstraint(componentId, componentInstanceId, nodeFilterDataDefinition, position, nodeFilterConstraintType);
            if (result.isRight()) {
                janusGraphDao.rollback();
                throw new BusinessLogicException(componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(result.right().value()), component.getSystemName()));
            } else {
                nodeFilterDataDefinition = result.left().value();
            }
            janusGraphDao.commit();
            LOGGER.debug("Node filter successfully deleted in component {} . ", component.getSystemName());
        } catch (final Exception e) {
            janusGraphDao.rollback();
            LOGGER.error(BUSINESS_PROCESS_ERROR, "Exception occurred during delete component node filter property values: {}", e.getMessage(), e);
            throw new BusinessLogicException(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        } finally {
            if (wasLocked) {
                unlockComponent(component.getUniqueId(), componentTypeEnum);
            }
        }
        return Optional.ofNullable(nodeFilterDataDefinition);
    }

    private Either<CINodeFilterDataDefinition, StorageOperationStatus> addNewNodeFilter(final String componentId, final String componentInstanceId,
                                                                                        final NodeFilterConstraintType nodeFilterConstraintType,
                                                                                        final CINodeFilterDataDefinition nodeFilterDataDefinition,
                                                                                        final RequirementNodeFilterPropertyDataDefinition requirementNodeFilterPropertyDataDefinition,
                                                                                        final String capabilityName) {
        if (NodeFilterConstraintType.PROPERTIES.equals(nodeFilterConstraintType)) {
            return nodeFilterOperation
                .addNewProperty(componentId, componentInstanceId, nodeFilterDataDefinition, requirementNodeFilterPropertyDataDefinition);
        }
        final RequirementNodeFilterCapabilityDataDefinition requirementNodeFilterCapabilityDataDefinition = new RequirementNodeFilterCapabilityDataDefinition();
        requirementNodeFilterCapabilityDataDefinition.setName(capabilityName);
        final ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> propertyDataDefinitionListDataDefinition = new ListDataDefinition<>();
        propertyDataDefinitionListDataDefinition.getListToscaDataDefinition()
            .addAll(Collections.singleton(requirementNodeFilterPropertyDataDefinition));
        requirementNodeFilterCapabilityDataDefinition.setProperties(propertyDataDefinitionListDataDefinition);
        return nodeFilterOperation
            .addNewCapabilities(componentId, componentInstanceId, nodeFilterDataDefinition, requirementNodeFilterCapabilityDataDefinition);
    }

    private void unlockComponent(final String componentUniqueId, final ComponentTypeEnum componentType) {
        graphLockOperation.unlockComponent(componentUniqueId, componentType.getNodeType());
    }

    public User validateUser(final String userId) {
        final User user = userValidations.validateUserExists(userId);
        userValidations.validateUserRole(user, Arrays.asList(Role.DESIGNER, Role.ADMIN));
        return user;
    }

    private Optional<ComponentInstance> getComponentInstance(final String componentInstanceId, final Component component) {
        return component.getComponentInstanceById(componentInstanceId);
    }

    private Optional<CINodeFilterDataDefinition> getCiNodeFilterDataDefinition(final Optional<ComponentInstance> componentInstance) {
        if (componentInstance.isPresent()) {
            return Optional.ofNullable(componentInstance.get().getNodeFilter());
        }
        return Optional.empty();
    }

    private Optional<CINodeFilterDataDefinition> getCiNodeFilterDataDefinition(final String componentInstanceId, final Component component)
        throws BusinessLogicException {
        final Either<Boolean, ResponseFormat> response = nodeFilterValidator.validateComponentInstanceExist(component, componentInstanceId);
        if (response.isRight()) {
            throw new BusinessLogicException(
                componentsUtils.getResponseFormat(ActionStatus.NODE_FILTER_NOT_FOUND, response.right().value().getFormattedMessage()));
        }
        final Optional<ComponentInstance> componentInstance = getComponentInstance(componentInstanceId, component);
        if (componentInstance.isPresent()) {
            return Optional.ofNullable(componentInstance.get().getNodeFilter());
        }
        return Optional.empty();
    }

    private CINodeFilterDataDefinition validateAndReturnNodeFilterDefinition(final String componentInstanceId,
                                                                             final NodeFilterConstraintAction action, final String constraint,
                                                                             final Component component,
                                                                             final NodeFilterConstraintType nodeFilterConstraintType,
                                                                             final String capabilityName)
        throws BusinessLogicException {
        validateNodeFilter(component, componentInstanceId, action, constraint, nodeFilterConstraintType, capabilityName);
        final Optional<CINodeFilterDataDefinition> cINodeFilterDataDefinition = getCiNodeFilterDataDefinition(componentInstanceId, component);
        if (!cINodeFilterDataDefinition.isPresent()) {
            throw new BusinessLogicException(componentsUtils.getResponseFormat(ActionStatus.NODE_FILTER_NOT_FOUND));
        }
        return cINodeFilterDataDefinition.get();
    }

    private void validateNodeFilter(final Component component, final String componentInstanceId, final NodeFilterConstraintAction action,
                                    final String constraint, final NodeFilterConstraintType nodeFilterConstraintType, final String capabilityName) throws BusinessLogicException {
        final Either<Boolean, ResponseFormat> response = nodeFilterValidator
            .validateFilter(component, componentInstanceId, Collections.singletonList(constraint), action, nodeFilterConstraintType, capabilityName);
        if (response.isRight()) {
            throw new BusinessLogicException(
                componentsUtils.getResponseFormat(ActionStatus.NODE_FILTER_NOT_FOUND, response.right().value().getFormattedMessage()));
        }
    }

    public Optional<CINodeFilterDataDefinition> updateNodeFilter(final String componentId, final String componentInstanceId,
                                                                 final UIConstraint uiConstraint, final ComponentTypeEnum componentTypeEnum,
                                                                 final NodeFilterConstraintType nodeFilterConstraintType, final int index)
        throws BusinessLogicException {
        final Optional<CINodeFilterDataDefinition> deleteActionResponse = deleteNodeFilter(componentId, componentInstanceId,
            NodeFilterConstraintAction.DELETE, null, index, true, componentTypeEnum, nodeFilterConstraintType);
        if (!deleteActionResponse.isPresent()) {
            throw new BusinessLogicException(
                componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR, "Failed to delete node filter capabilities"));
        }
        return addNodeFilter(componentId.toLowerCase(), componentInstanceId, NodeFilterConstraintAction.ADD, uiConstraint.getServicePropertyName(),
            new ConstraintConvertor().convert(uiConstraint), true, componentTypeEnum, nodeFilterConstraintType,
            uiConstraint.getCapabilityName());
    }

    public StorageOperationStatus associateNodeFilterToComponentInstance(final String componentId,
                                                                         final Map<String, UploadNodeFilterInfo> instNodeFilter) {
        for (Entry<String, UploadNodeFilterInfo> filter : instNodeFilter.entrySet()) {
            String componentInstanceId = filter.getKey();
            CINodeFilterDataDefinition ciNodeFilterDataDefinition = new CINodeFilterUtils()
                .getNodeFilterDataDefinition(filter.getValue(), componentInstanceId);
            Either<CINodeFilterDataDefinition, StorageOperationStatus> nodeFilter = nodeFilterOperation.createNodeFilter(componentId,
                componentInstanceId);
            if (nodeFilter.isRight()) {
                LOGGER.error(BUSINESS_PROCESS_ERROR, "Failed to Create Node filter on component instance with id {}",
                    filter.getKey());
                return nodeFilter.right().value();
            }

            //associate node filter properties to component instance
            List<RequirementNodeFilterPropertyDataDefinition> properties = ciNodeFilterDataDefinition.getProperties()
                .getListToscaDataDefinition();
            if (!properties.isEmpty()) {
                final Component component;
                try {
                    component = getComponent(componentId);
                } catch (BusinessLogicException e) {
                    throw new ComponentException(e.getResponseFormat());
                }
                properties.forEach(property -> {
                    String propertyType = getComponentInstancePropertyType(component, componentInstanceId, property.getName());
                    RequirementNodeFilterPropertyDataDefinition requirementNodeFilterPropertyDataDefinition =
                        getRequirementNodeFilterPropertyDataDefinition(property);
                    requirementNodeFilterPropertyDataDefinition.setType(propertyType);
                    Either<CINodeFilterDataDefinition, StorageOperationStatus> nodeFilterProperty = nodeFilterOperation
                        .addNewProperty(componentId, componentInstanceId, nodeFilter.left().value(), requirementNodeFilterPropertyDataDefinition);
                    if (nodeFilterProperty.isRight()) {
                        throw new ComponentException(
                            componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(nodeFilterProperty.right().value()),
                                componentInstanceId));
                    }
                });
            }

            //associate node filter capabilities to component instance
            List<RequirementNodeFilterCapabilityDataDefinition> capabilities = ciNodeFilterDataDefinition.getCapabilities()
                .getListToscaDataDefinition();
            if (!capabilities.isEmpty()) {
                capabilities.forEach(capability -> {
                    RequirementNodeFilterCapabilityDataDefinition requirementNodeFilterCapabilityDataDefinition =
                        new RequirementNodeFilterCapabilityDataDefinition();
                    requirementNodeFilterCapabilityDataDefinition.setName(capability.getName());
                    requirementNodeFilterCapabilityDataDefinition.setProperties(getProperties(capability.getProperties()));
                    Either<CINodeFilterDataDefinition, StorageOperationStatus> nodeFilterCapability = nodeFilterOperation
                        .addNewCapabilities(componentId, componentInstanceId, nodeFilter.left().value(),
                            requirementNodeFilterCapabilityDataDefinition);
                    if (nodeFilterCapability.isRight()) {
                        throw new ComponentException(
                            componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(nodeFilterCapability.right().value()),
                                componentInstanceId));
                    }
                });
            }
        }
        return StorageOperationStatus.OK;
    }

    private List<String> getNodeFilterConstraints(String name, List<String> value) {
        List<String> constraints = new ArrayList<>();
        constraints.add(name + ":\n " + value.get(0));
        return constraints;
    }

    private ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> getProperties(ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> properties) {
        ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> updatedProperties = new ListDataDefinition<>();
        properties.getListToscaDataDefinition().forEach(property -> {
            RequirementNodeFilterPropertyDataDefinition requirementNodeFilterPropertyDataDefinition = getRequirementNodeFilterPropertyDataDefinition(
                property);
            updatedProperties.add(requirementNodeFilterPropertyDataDefinition);
        });
        return updatedProperties;
    }

    private RequirementNodeFilterPropertyDataDefinition getRequirementNodeFilterPropertyDataDefinition(
        RequirementNodeFilterPropertyDataDefinition property) {
        RequirementNodeFilterPropertyDataDefinition requirementNodeFilterPropertyDataDefinition = new RequirementNodeFilterPropertyDataDefinition();
        requirementNodeFilterPropertyDataDefinition.setName(property.getName());
        requirementNodeFilterPropertyDataDefinition.setConstraints(getNodeFilterConstraints(property.getName(), property.getConstraints()));
        return requirementNodeFilterPropertyDataDefinition;
    }
}
