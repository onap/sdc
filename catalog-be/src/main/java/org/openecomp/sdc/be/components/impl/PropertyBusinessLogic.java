/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import com.google.gson.JsonElement;
import fj.data.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.ToscaOperationException;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.model.tosca.validators.PropertyTypeValidator;
import org.openecomp.sdc.be.resources.data.EntryData;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("propertyBusinessLogic")
public class PropertyBusinessLogic extends BaseBusinessLogic {

    private static final String CREATE_PROPERTY = "CreateProperty";
    private static final Logger log = Logger.getLogger(PropertyBusinessLogic.class);
    private static final String EMPTY_VALUE = null;

    @Autowired
    public PropertyBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation, IGroupInstanceOperation groupInstanceOperation,
                                 IGroupTypeOperation groupTypeOperation, InterfaceOperation interfaceOperation,
                                 InterfaceLifecycleOperation interfaceLifecycleTypeOperation, ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
    }

    /**
     * Create new property on component in graph
     *
     * @param componentId
     * @param newPropertyDefinition
     * @param userId
     * @return either properties or response format
     */
    public Either<EntryData<String, PropertyDefinition>, ResponseFormat> addPropertyToComponent(String componentId,
                                                                                                PropertyDefinition newPropertyDefinition,
                                                                                                String userId) {
        Either<EntryData<String, PropertyDefinition>, ResponseFormat> result = null;
        validateUserExists(userId);
        Either<Component, StorageOperationStatus> serviceElement = toscaOperationFacade.getToscaElement(componentId);
        if (serviceElement.isRight()) {
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
            return result;
        }
        Component component = serviceElement.left().value();
        NodeTypeEnum nodeType = component.getComponentType().getNodeType();
        StorageOperationStatus lockResult = graphLockOperation.lockComponent(componentId, nodeType);
        if (!lockResult.equals(StorageOperationStatus.OK)) {
            BeEcompErrorManager.getInstance().logBeFailedLockObjectError(CREATE_PROPERTY, nodeType.name().toLowerCase(), componentId);
            log.info("Failed to lock component {}. Error - {}", componentId, lockResult);
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            return result;
        }
        try {
            final String propertyName = newPropertyDefinition.getName();
            if (!ComponentValidationUtils.canWorkOnComponent(component, userId)) {
                result = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
                return result;
            }
            List<PropertyDefinition> properties = component.getProperties();
            if (CollectionUtils.isEmpty(properties)) {
                properties = new ArrayList<>();
            }
            if (isPropertyExistInComponent(properties, propertyName)) {
                result = Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_ALREADY_EXIST, propertyName));
                return result;
            } else {
                Map<String, DataTypeDefinition> allDataTypes = componentsUtils.getAllDataTypes(applicationDataTypeCache, component.getModel());
                // validate property default values
                Either<Boolean, ResponseFormat> defaultValuesValidation = validatePropertyDefaultValue(newPropertyDefinition, allDataTypes);
                if (defaultValuesValidation.isRight()) {
                    result = Either.right(defaultValuesValidation.right().value());
                    return result;
                }
                // convert property
                ToscaPropertyType type = getType(newPropertyDefinition.getType());
                if (type != null) {
                    PropertyValueConverter converter = type.getConverter();
                    // get inner type
                    String innerType = null;
                    SchemaDefinition schema = newPropertyDefinition.getSchema();
                    if (schema != null) {
                        PropertyDataDefinition prop = schema.getProperty();
                        if (prop != null) {
                            innerType = prop.getType();
                        }
                    }
                    if (newPropertyDefinition.getDefaultValue() != null) {
                        final String convertedValue = converter.convert(newPropertyDefinition.getDefaultValue(), innerType, allDataTypes);
                        newPropertyDefinition.setDefaultValue(convertedValue);
                    }
                }
                Either<PropertyDefinition, StorageOperationStatus> addPropertyEither = toscaOperationFacade
                    .addPropertyToComponent(newPropertyDefinition, component);
                if (addPropertyEither.isRight()) {
                    log.info("Failed to add new property {}. Error - {}", componentId, addPropertyEither.right().value());
                    result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
                    return result;
                }
            }
            result = Either.left(new EntryData<>(propertyName, newPropertyDefinition));
            return result;
        } finally {
            commitOrRollback(result);
            // unlock component
            graphLockOperation.unlockComponent(componentId, nodeType);
        }
    }

    /**
     * Copies a list of properties to a component.
     *
     * @param component            the component to add the copied properties
     * @param propertiesToCopyList the properties to be copied
     * @return the updated component with the copied properties.
     * @throws ToscaOperationException when a problem happens during the copy operation
     */
    public Component copyPropertyToComponent(final Component component, final List<PropertyDefinition> propertiesToCopyList)
        throws ToscaOperationException {
        return copyPropertyToComponent(component, propertiesToCopyList, true);
    }

    /**
     * Copies a list of properties to a component.
     *
     * @param component            the component to add the copied properties
     * @param propertiesToCopyList the properties to be copied
     * @param refreshComponent     refresh the component from database after update
     * @return the component refreshed from database if refreshComponent is {@code true}, the same component reference otherwise
     * @throws ToscaOperationException when a problem happens during the copy operation
     */
    public Component copyPropertyToComponent(final Component component, final List<PropertyDefinition> propertiesToCopyList,
                                             final boolean refreshComponent) throws ToscaOperationException {
        if (CollectionUtils.isEmpty(propertiesToCopyList)) {
            return component;
        }
        for (final PropertyDefinition propertyDefinition : propertiesToCopyList) {
            copyPropertyToComponent(component, propertyDefinition);
        }
        if (refreshComponent) {
            return toscaOperationFacade.getToscaElement(component.getUniqueId()).left().value();
        }
        return component;
    }

    /**
     * Copies one property to a component.
     *
     * @param component          the component to add the copied property
     * @param propertyDefinition the property to be copied
     * @throws ToscaOperationException when a problem happens during the copy operation
     */
    private void copyPropertyToComponent(final Component component, final PropertyDefinition propertyDefinition) throws ToscaOperationException {
        final PropertyDefinition copiedPropertyDefinition = new PropertyDefinition(propertyDefinition);
        final String componentId = component.getUniqueId();
        final String propertyName = copiedPropertyDefinition.getName();
        copiedPropertyDefinition.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(componentId, propertyName));
        copiedPropertyDefinition.setParentUniqueId(componentId);
        final Either<PropertyDefinition, StorageOperationStatus> operationResult = toscaOperationFacade
            .addPropertyToComponent(copiedPropertyDefinition, component);
        if (operationResult.isRight()) {
            final String error = String
                .format("Failed to add copied property '%s' to component '%s'. Operation status: '%s'", propertyDefinition.getUniqueId(), componentId,
                    operationResult.right().value());
            log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, PropertyBusinessLogic.class.getName(), "catalog-be", error);
            throw new ToscaOperationException(error, operationResult.right().value());
        }
    }

    /**
     * Get property of component
     *
     * @param componentId
     * @param propertyId
     * @param userId
     * @return either properties or response format
     */
    public Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> getComponentProperty(String componentId, String propertyId, String userId) {
        validateUserExists(userId);
        // Get the resource from DB
        Either<Component, StorageOperationStatus> status = toscaOperationFacade.getToscaElement(componentId);
        if (status.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
        }
        Component component = status.left().value();
        List<PropertyDefinition> properties = component.getProperties();
        if (CollectionUtils.isEmpty(properties)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, ""));
        }
        for (PropertyDefinition property : properties) {
            if (property.getUniqueId().equals(propertyId)) {
                return Either.left(new EntryData<>(property.getName(), property));
            }
        }
        return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, ""));
    }

    public Either<List<PropertyDefinition>, ResponseFormat> getPropertiesList(String componentId, String userId) {
        validateUserExists(userId);
        // Get the resource from DB
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreProperties(false);
        Either<Component, StorageOperationStatus> status = toscaOperationFacade.getToscaElement(componentId);
        if (status.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
        }
        Component component = status.left().value();
        List<PropertyDefinition> properties = component.getProperties();
        return Either.left(properties);
    }

    /**
     * delete property of component from graph
     *
     * @param componentId
     * @param propertyId
     * @param userId
     * @return either properties or response format
     */
    public Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> deletePropertyFromComponent(String componentId, String propertyId,
                                                                                                     String userId) {
        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> result = null;
        validateUserExists(userId);
        // Get the resource from DB
        Either<Component, StorageOperationStatus> getComponentRes = toscaOperationFacade.getToscaElement(componentId);
        if (getComponentRes.isRight()) {
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
            return result;
        }
        Component component = getComponentRes.left().value();
        NodeTypeEnum nodeType = component.getComponentType().getNodeType();
        StorageOperationStatus lockResult = graphLockOperation.lockComponent(componentId, nodeType);
        if (!lockResult.equals(StorageOperationStatus.OK)) {
            BeEcompErrorManager.getInstance().logBeFailedLockObjectError(CREATE_PROPERTY, nodeType.name().toLowerCase(), componentId);
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            return result;
        }
        try {
            // verify that resource is checked-out and the user is the last

            // updater
            if (!ComponentValidationUtils.canWorkOnComponent(component, userId)) {
                result = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
                return result;
            }
            // verify property exist in resource
            Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> statusGetProperty = getComponentProperty(componentId, propertyId, userId);
            if (statusGetProperty.isRight()) {
                result = Either.right(statusGetProperty.right().value());
                return result;
            }
            Map.Entry<String, PropertyDefinition> propertyDefinitionEntry = statusGetProperty.left().value();
            // verify that the property is not used by operation
            if (isPropertyUsedByOperation(component, propertyDefinitionEntry.getValue())) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_USED_BY_OPERATION));
            }
            StorageOperationStatus status = toscaOperationFacade.deletePropertyOfComponent(component, propertyDefinitionEntry.getKey());
            if (status != StorageOperationStatus.OK) {
                result = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(status), component.getName()));
                return result;
            }
            result = Either.left(propertyDefinitionEntry);
            return result;
        } finally {
            commitOrRollback(result);
            // unlock component
            graphLockOperation.unlockComponent(componentId, nodeType);
        }
    }

    public boolean isPropertyUsedByOperation(Component component, PropertyDefinition propertyDefinitionEntry) {
        // Component's own interfaces
        Map<String, InterfaceDefinition> interfaces = component.getInterfaces();
        if (MapUtils.isNotEmpty(interfaces)) {
            for (Map.Entry<String, InterfaceDefinition> interfaceEntry : interfaces.entrySet()) {
                if (isPropertyExistInOperationInterface(propertyDefinitionEntry, interfaceEntry.getValue())) {
                    return true;
                }
            }
        }
        // Component's child's component interfaces
        if (isPropertyUsedInCIInterfaces(component.getComponentInstancesInterfaces(), propertyDefinitionEntry)) {
            return true;
        }
        // Component's parent's component interfaces
        Either<List<Component>, StorageOperationStatus> componentList = toscaOperationFacade.getParentComponents(component.getUniqueId());
        if (componentList.isLeft()) {
            for (Component parentComponent : componentList.left().value()) {
                if (isPropertyUsedInCIInterfaces(parentComponent.getComponentInstancesInterfaces(), propertyDefinitionEntry)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPropertyUsedInCIInterfaces(Map<String, List<ComponentInstanceInterface>> componentInstanceInterfaces,
                                                 PropertyDefinition propertyDefinitionEntry) {
        Optional<ComponentInstanceInterface> isPropertyExistInOperationInterface = Optional.empty();
        if (MapUtils.isNotEmpty(componentInstanceInterfaces)) {
            isPropertyExistInOperationInterface = componentInstanceInterfaces.entrySet().stream()
                .flatMap(interfaceEntry -> interfaceEntry.getValue().stream())
                .filter(instanceInterface -> isPropertyExistInOperationInterface(propertyDefinitionEntry, instanceInterface)).findAny();
        }
        return isPropertyExistInOperationInterface.isPresent();
    }

    private boolean isPropertyExistInOperationInterface(PropertyDefinition propertyDefinition, InterfaceDefinition interfaceDefinition) {
        Map<String, OperationDataDefinition> operations = interfaceDefinition.getOperations();
        for (Map.Entry<String, OperationDataDefinition> operationEntry : operations.entrySet()) {
            Optional<OperationInputDefinition> inputWithDeletedPropertyCandidate = getInputWithDeclaredProperty(propertyDefinition, operationEntry);
            if (inputWithDeletedPropertyCandidate.isPresent()) {
                return true;
            }
        }
        return false;
    }

    private Optional<OperationInputDefinition> getInputWithDeclaredProperty(PropertyDefinition propertyDefinition,
                                                                            Map.Entry<String, OperationDataDefinition> operationEntry) {
        ListDataDefinition<OperationInputDefinition> inputs = operationEntry.getValue().getInputs();
        List<OperationInputDefinition> operationInputsList = Objects.isNull(inputs) ? null : inputs.getListToscaDataDefinition();
        if (CollectionUtils.isEmpty(operationInputsList)) {
            return Optional.empty();
        }
        return operationInputsList.stream().filter(
            input -> input.getInputId().equals(propertyDefinition.getUniqueId()) || (input.getSourceProperty() != null && input.getSourceProperty()
                .equals(propertyDefinition.getUniqueId()))).findAny();
    }

    /**
     * update property
     *
     * @param componentId
     * @param propertyId
     * @param newPropertyDefinition
     * @param userId
     * @return either properties or response format
     */
    public Either<EntryData<String, PropertyDefinition>, ResponseFormat> updateComponentProperty(String componentId, String propertyId,
                                                                                                 PropertyDefinition newPropertyDefinition,
                                                                                                 String userId) {
        Either<EntryData<String, PropertyDefinition>, ResponseFormat> result = null;
        Either<Component, StorageOperationStatus> status = toscaOperationFacade.getToscaElement(componentId);
        if (status.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
        }
        Component component = status.left().value();
        NodeTypeEnum nodeType = component.getComponentType().getNodeType();
        if (!ComponentValidationUtils.canWorkOnComponent(component, userId)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
        }
        StorageOperationStatus lockResult = graphLockOperation.lockComponent(componentId, nodeType);
        if (!lockResult.equals(StorageOperationStatus.OK)) {
            BeEcompErrorManager.getInstance().logBeFailedLockObjectError(CREATE_PROPERTY, nodeType.name().toLowerCase(), componentId);
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            return result;
        }
        try {
            Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> statusGetProperty = getComponentProperty(componentId, propertyId, userId);
            if (statusGetProperty.isRight()) {
                result = Either.right(statusGetProperty.right().value());
                return result;
            }
            String propertyName = statusGetProperty.left().value().getKey();
            Either<PropertyDefinition, StorageOperationStatus> either = toscaOperationFacade
                .updatePropertyOfComponent(component, newPropertyDefinition);
            if (either.isRight()) {
                result = Either.right(componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(either.right().value()), component.getName()));
                return result;
            }
            EntryData<String, PropertyDefinition> property = new EntryData<>(propertyName, either.left().value());
            result = Either.left(property);
            return result;
        } finally {
            commitOrRollback(result);
            graphLockOperation.unlockComponent(componentId, nodeType);
        }
    }

    /**
     * Finds a component by id,
     *
     * @param componentId the component id to find
     * @return an Optional<Component> if the component with given id was found, otherwise Optional.empty()
     * @throws BusinessLogicException when a problem happens during the find operation
     */
    public Optional<Component> findComponentById(final String componentId) throws BusinessLogicException {
        final Either<Component, StorageOperationStatus> status = toscaOperationFacade.getToscaElement(componentId);
        if (status.isRight()) {
            final StorageOperationStatus operationStatus = status.right().value();
            if (operationStatus == StorageOperationStatus.NOT_FOUND) {
                return Optional.empty();
            }
            final ResponseFormat responseFormat = componentsUtils.getResponseFormat(operationStatus);
            throw new BusinessLogicException(responseFormat);
        }
        return Optional.ofNullable(status.left().value());
    }

    /**
     * Updates a component property.
     *
     * @param componentId        the component id that owns the property
     * @param propertyDefinition the existing property to update
     * @return the updated property
     * @throws BusinessLogicException if the component was not found or if there was a problem during the update operation.
     */
    public PropertyDefinition updateComponentProperty(final String componentId, final PropertyDefinition propertyDefinition)
        throws BusinessLogicException {
        final Component component = findComponentById(componentId).orElse(null);
        if (component == null) {
            throw new BusinessLogicException(componentsUtils.getResponseFormatByResource(ActionStatus.RESOURCE_NOT_FOUND, componentId));
        }
        final Either<PropertyDefinition, StorageOperationStatus> updateResultEither = toscaOperationFacade
            .updatePropertyOfComponent(component, propertyDefinition);
        if (updateResultEither.isRight()) {
            final ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(updateResultEither.right().value()), component.getName());
            throw new BusinessLogicException(responseFormat);
        }
        return updateResultEither.left().value();
    }

    private boolean isPropertyExistInComponent(List<PropertyDefinition> properties, String propertyName) {
        if (CollectionUtils.isEmpty(properties)) {
            return false;
        }
        Optional<PropertyDefinition> propertyCandidate = properties.stream().filter(property -> property.getName().equals(propertyName)).findAny();
        return propertyCandidate.isPresent();
    }

    @Override
    protected String getValueFromJsonElement(JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return EMPTY_VALUE;
        }
        if (jsonElement.toString().isEmpty()) {
            return "";
        }
        return jsonElement.toString();
    }

    @Override
    protected boolean isValidValue(ToscaPropertyType type, String value, String innerType, Map<String, DataTypeDefinition> dataTypes) {
        if (isEmptyValue(value)) {
            return true;
        }
        PropertyTypeValidator validator = type.getValidator();
        return validator.isValid(value, innerType, dataTypes);
    }

    @Override
    public boolean isEmptyValue(String value) {
        return value == null;
    }
}
