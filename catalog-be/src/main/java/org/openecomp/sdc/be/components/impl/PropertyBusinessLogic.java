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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.IComplexDefaultValue;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.model.tosca.validators.PropertyTypeValidator;
import org.openecomp.sdc.be.resources.data.EntryData;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.util.*;
import java.util.function.Supplier;

@org.springframework.stereotype.Component("propertyBusinessLogic")
public class PropertyBusinessLogic extends BaseBusinessLogic {

    private static final String CREATE_PROPERTY = "CreateProperty";

    private static final Logger log = Logger.getLogger(PropertyBusinessLogic.class);

    private static final String EMPTY_VALUE = null;

    protected static IElementOperation getElementDao(Class<IElementOperation> class1, ServletContext context) {
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);

        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);

        return webApplicationContext.getBean(class1);
    }

    public Either<Map<String, DataTypeDefinition>, ResponseFormat> getAllDataTypes() {
        return getAllDataTypes(applicationDataTypeCache);
    }

    /**
     * Create new property on component in graph
     *
     * @param componentId
     * @param propertyName
     * @param newPropertyDefinition
     * @param userId
     * @return either properties or response format
     */

    public Either<EntryData<String, PropertyDefinition>, ResponseFormat> addPropertyToComponent(String componentId,
                                                                                                String propertyName,
                                                                                                PropertyDefinition newPropertyDefinition,
                                                                                                String userId) {
        Either<EntryData<String, PropertyDefinition>, ResponseFormat> result = null;

        validateUserExists(userId, "create Property", false);

        Either<Component, StorageOperationStatus> serviceElement =
                toscaOperationFacade.getToscaElement(componentId);
        if (serviceElement.isRight()) {
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
            return result;
        }
        Component component = serviceElement.left().value();
        NodeTypeEnum nodeType = component.getComponentType().getNodeType();
        StorageOperationStatus lockResult = graphLockOperation.lockComponent(componentId, nodeType );
        if (!lockResult.equals(StorageOperationStatus.OK)) {
            BeEcompErrorManager.getInstance().logBeFailedLockObjectError(CREATE_PROPERTY, nodeType.name().toLowerCase(), componentId);
            log.info("Failed to lock component {}. Error - {}", componentId, lockResult);
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            return result;
        }

        try {
            if (!ComponentValidationUtils.canWorkOnComponent(component, userId)) {
                result = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
                return result;
            }

            List<PropertyDefinition> properties = component.getProperties();

            if(CollectionUtils.isEmpty(properties)) {
                properties = new ArrayList<>();
            }

            if(isPropertyExistInComponent(properties, propertyName)) {

                result =
                    Either.right(componentsUtils.getResponseFormat(ActionStatus
                        .PROPERTY_ALREADY_EXIST, propertyName));
                return result;

            } else {

                Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = getAllDataTypes(applicationDataTypeCache);
                if (allDataTypes.isRight()) {
                    result = Either.right(allDataTypes.right().value());
                    return result;
                }

                Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();

                // validate property default values
                Either<Boolean, ResponseFormat> defaultValuesValidation = validatePropertyDefaultValue(newPropertyDefinition, dataTypes);
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
                    if (newPropertyDefinition != null) {
                        SchemaDefinition schema = newPropertyDefinition.getSchema();
                        if (schema != null) {
                            PropertyDataDefinition prop = schema.getProperty();
                            if (prop != null) {
                                innerType = prop.getType();
                            }
                        }
                        String convertedValue = null;
                        if (newPropertyDefinition.getDefaultValue() != null) {
                            convertedValue = converter.convert(
                                newPropertyDefinition.getDefaultValue(), innerType, allDataTypes.left().value());
                            newPropertyDefinition.setDefaultValue(convertedValue);
                        }
                    }
                }
                Either<PropertyDefinition, StorageOperationStatus> addPropertyEither =
                    toscaOperationFacade
                        .addPropertyToComponent(propertyName, newPropertyDefinition, component);

                if (addPropertyEither.isRight()) {
                    log.info("Failed to add new property {}. Error - {}", componentId,
                        addPropertyEither.right().value());
                    result = Either.right(componentsUtils.getResponseFormat(ActionStatus
                        .GENERAL_ERROR));
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
     * Get property of component
     *
     * @param componentId
     * @param propertyId
     * @param userId
     * @return either properties or response format
     */

    public Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> getComponentProperty(String componentId, String propertyId, String userId) {

        validateUserExists(userId, "create Component Instance", false);
        // Get the resource from DB
        Either<Component, StorageOperationStatus> status =
            toscaOperationFacade.getToscaElement(componentId);
        if (status.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
        }
        Component component = status.left().value();
        List<PropertyDefinition> properties = component.getProperties();
        if(CollectionUtils.isEmpty(properties)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, ""));
        }

        for(PropertyDefinition property : properties) {
            if(property.getUniqueId().equals(propertyId)) {
                return Either.left(new EntryData<>(property.getName(), property));
            }
        }
        return Either.right(componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, ""));
    }


    public Either<List<PropertyDefinition>, ResponseFormat> getPropertiesList(String componentId,
                                                                              String userId) {
        validateUserExists(userId, "create Component Instance", false);

        // Get the resource from DB
        ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreProperties(false);
        Either<Component, StorageOperationStatus> status =
            toscaOperationFacade.getToscaElement(componentId);
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

    public Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> deletePropertyFromComponent(String componentId, String propertyId, String userId) {

        Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> result = null;

        validateUserExists(userId, "delete Property", false);

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
            BeEcompErrorManager.getInstance().logBeFailedLockObjectError(CREATE_PROPERTY, nodeType.name().toLowerCase(),
                    componentId);
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
            Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> statusGetProperty =
                getComponentProperty(componentId, propertyId, userId);
            if (statusGetProperty.isRight()) {
                result = Either.right(statusGetProperty.right().value());
                return result;
            }

            Map.Entry<String, PropertyDefinition> propertyDefinitionEntry = statusGetProperty.left().value();

            // verify that the property is not used by operation
            if (isPropertyUsedByOperation(component, propertyDefinitionEntry.getValue())) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus
                    .PROPERTY_USED_BY_OPERATION));
            }

            StorageOperationStatus status =
                toscaOperationFacade.deletePropertyOfComponent(component, propertyDefinitionEntry.getKey());
            if (status != StorageOperationStatus.OK) {
                result = Either.right(componentsUtils.getResponseFormat(componentsUtils
                    .convertFromStorageResponse(status), component.getName()));
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

    public boolean isPropertyUsedByOperation(Component component,
                                             PropertyDefinition propertyDefinitionEntry) {

        // Component's own interfaces
        Map<String, InterfaceDefinition> interfaces = component.getInterfaces();
        if(MapUtils.isNotEmpty(interfaces)){
          for(Map.Entry<String, InterfaceDefinition> interfaceEntry : interfaces.entrySet()) {
            if (isPropertyExistInOperationInterface(propertyDefinitionEntry, interfaceEntry.getValue())) {
              return true;
            }
          }
        }

        // Component's child's component interfaces
        if(isPropertyUsedInCIInterfaces(component.getComponentInstancesInterfaces(), propertyDefinitionEntry)){
            return true;
        }

        // Component's parent's component interfaces
        Either<List<Component>, StorageOperationStatus> componentList = toscaOperationFacade.getParentComponents(component.getUniqueId());
        if(componentList.isLeft()){
            for (Component parentComponent : componentList.left().value()) {
                if(isPropertyUsedInCIInterfaces(parentComponent.getComponentInstancesInterfaces(), propertyDefinitionEntry)){
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isPropertyUsedInCIInterfaces(Map<String, List<ComponentInstanceInterface>> componentInstanceInterfaces, PropertyDefinition propertyDefinitionEntry){
        Optional<ComponentInstanceInterface> isPropertyExistInOperationInterface = Optional.empty();
        if(MapUtils.isNotEmpty(componentInstanceInterfaces)){
            isPropertyExistInOperationInterface = componentInstanceInterfaces.entrySet().stream()
                    .flatMap(interfaceEntry -> interfaceEntry.getValue().stream())
                    .filter(instanceInterface -> isPropertyExistInOperationInterface(propertyDefinitionEntry, instanceInterface))
                    .findAny();
        }
        return isPropertyExistInOperationInterface.isPresent();
    }

    private boolean isPropertyExistInOperationInterface(PropertyDefinition propertyDefinition,
                                                        InterfaceDefinition interfaceDefinition) {
        Map<String, OperationDataDefinition> operations =
            interfaceDefinition.getOperations();
        for(Map.Entry<String, OperationDataDefinition> operationEntry : operations
            .entrySet()) {
            Optional<OperationInputDefinition> inputWithDeletedPropertyCandidate =
                getInputWithDeclaredProperty(propertyDefinition, operationEntry);

            if(inputWithDeletedPropertyCandidate.isPresent()) {
                return true;
            }
        }
        return false;
    }

    private Optional<OperationInputDefinition> getInputWithDeclaredProperty(PropertyDefinition propertyDefinition,
                                                                            Map.Entry<String, OperationDataDefinition> operationEntry) {
        ListDataDefinition<OperationInputDefinition> inputs =
            operationEntry.getValue().getInputs();
        List<OperationInputDefinition> operationInputsList =
            Objects.isNull(inputs) ? null : inputs.getListToscaDataDefinition();

        if(CollectionUtils.isEmpty(operationInputsList)) {
            return Optional.empty();
        }

        return operationInputsList.stream().filter(input -> input.getInputId().equals(propertyDefinition.getUniqueId())
                || (input.getSourceProperty() != null && input.getSourceProperty().equals(propertyDefinition.getUniqueId()))).findAny();
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

    public Either<EntryData<String, PropertyDefinition>, ResponseFormat> updateComponentProperty(String componentId,
                                                                        String propertyId,
                                                                        PropertyDefinition newPropertyDefinition,
                                                                        String userId) {

        Either<EntryData<String, PropertyDefinition>, ResponseFormat> result = null;

        Either<Component, StorageOperationStatus> status = toscaOperationFacade.getToscaElement(
                componentId);
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
            BeEcompErrorManager.getInstance().logBeFailedLockObjectError(CREATE_PROPERTY, nodeType.name().toLowerCase(),
                    componentId);
            result = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            return result;
        }

        try {
            Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> statusGetProperty =
                getComponentProperty(componentId, propertyId, userId);
            if (statusGetProperty.isRight()) {
                result = Either.right(statusGetProperty.right().value());
                return result;
            }
            String propertyName = statusGetProperty.left().value().getKey();

            Either<PropertyDefinition, StorageOperationStatus> either =
                toscaOperationFacade.updatePropertyOfComponent(component, newPropertyDefinition);
            if (either.isRight()) {
                result = Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(either.right().value()), component.getName()));
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

    private boolean isPropertyExistInComponent(List<PropertyDefinition> properties, String propertyName) {
        if(CollectionUtils.isEmpty(properties)) {
            return false;
        }

        Optional<PropertyDefinition> propertyCandidate =
            properties.stream().filter(property -> property.getName().equals(propertyName))
                .findAny();

        return propertyCandidate.isPresent();
    }

    private boolean isPropertyExist(List<PropertyDefinition> properties, String resourceUid, String propertyName, String propertyType) {
        boolean result = false;
        if (!CollectionUtils.isEmpty(properties)) {
            for (PropertyDefinition propertyDefinition : properties) {

                if ( propertyDefinition.getName().equals(propertyName) &&
                    (propertyDefinition.getParentUniqueId().equals(resourceUid) || !propertyDefinition.getType().equals(propertyType)) ) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private Either<PropertyDefinition, StorageOperationStatus> handleProperty(PropertyDefinition newPropertyDefinition, Map<String, DataTypeDefinition> dataTypes) {

        StorageOperationStatus validateAndUpdateProperty = validateAndUpdateProperty(newPropertyDefinition, dataTypes);
        if (validateAndUpdateProperty != StorageOperationStatus.OK) {
            return Either.right(validateAndUpdateProperty);
        }

        return Either.left(newPropertyDefinition);
    }

    private StorageOperationStatus validateAndUpdateProperty(IComplexDefaultValue propertyDefinition, Map<String, DataTypeDefinition> dataTypes) {

        log.trace("Going to validate property type and value. {}", propertyDefinition);

        String propertyType = propertyDefinition.getType();
        String value = propertyDefinition.getDefaultValue();

        ToscaPropertyType type = getType(propertyType);

        if (type == null) {
            DataTypeDefinition dataTypeDefinition = dataTypes.get(propertyType);
            if (dataTypeDefinition == null) {
                log.debug("The type {} of property cannot be found.", propertyType);
                return StorageOperationStatus.INVALID_TYPE;
            }
            return validateAndUpdateComplexValue(propertyDefinition, propertyType, value, dataTypeDefinition, dataTypes);
        }
        String innerType;

        Either<String, TitanOperationStatus> checkInnerType = getInnerType(type, propertyDefinition::getSchema);
        if (checkInnerType.isRight()) {
            return StorageOperationStatus.INVALID_TYPE;
        }
        innerType = checkInnerType.left().value();

        log.trace("After validating property type {}", propertyType);

        boolean isValidProperty = isValidValue(type, value, innerType, dataTypes);
        if (!isValidProperty) {
            log.info("The value {} of property from type {} is invalid", value, type);
            return StorageOperationStatus.INVALID_VALUE;
        }

        PropertyValueConverter converter = type.getConverter();

        if (isEmptyValue(value)) {
            log.debug("Default value was not sent for property {}. Set default value to {}", propertyDefinition.getName(), EMPTY_VALUE);
            propertyDefinition.setDefaultValue(EMPTY_VALUE);
        } else if (!isEmptyValue(value)) {
            String convertedValue = converter.convert(value, innerType, dataTypes);
            propertyDefinition.setDefaultValue(convertedValue);
        }
        return StorageOperationStatus.OK;
    }

    private StorageOperationStatus validateAndUpdateComplexValue(IComplexDefaultValue propertyDefinition, String propertyType,
                                                                 String value, DataTypeDefinition dataTypeDefinition, Map<String, DataTypeDefinition> dataTypes) {

        ImmutablePair<JsonElement, Boolean> validateResult = dataTypeValidatorConverter.validateAndUpdate(value, dataTypeDefinition, dataTypes);

        if (validateResult.right) {
            log.debug("The value {} of property from type {} is invalid", propertyType, propertyType);
            return StorageOperationStatus.INVALID_VALUE;
        }

        JsonElement jsonElement = validateResult.left;

        log.trace("Going to update value in property definition {} {}" , propertyDefinition.getName() , jsonElement);

        updateValue(propertyDefinition, jsonElement);

        return StorageOperationStatus.OK;
    }

    private void updateValue(IComplexDefaultValue propertyDefinition, JsonElement jsonElement) {

        propertyDefinition.setDefaultValue(getValueFromJsonElement(jsonElement));

    }

    @Override
    protected String getValueFromJsonElement(JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return EMPTY_VALUE;
        }
        if(jsonElement.toString().isEmpty()){
            return "";
        }
        return jsonElement.toString();
    }

    private Either<String, TitanOperationStatus> getInnerType(ToscaPropertyType type, Supplier<SchemaDefinition> schemeGen) {
        String innerType = null;
        if (type == ToscaPropertyType.LIST || type == ToscaPropertyType.MAP) {

            SchemaDefinition def = schemeGen.get();
            if (def == null) {
                log.debug("Schema doesn't exists for property of type {}", type);
                return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
            }
            PropertyDataDefinition propDef = def.getProperty();
            if (propDef == null) {
                log.debug("Property in Schema Definition inside property of type {} doesn't exist", type);
                return Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
            }
            innerType = propDef.getType();
        }
        return Either.left(innerType);
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
