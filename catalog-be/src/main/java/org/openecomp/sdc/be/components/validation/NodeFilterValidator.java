/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.components.validation;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.utils.NodeFilterConstraintAction;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionType;
import org.openecomp.sdc.be.datatypes.enums.NodeFilterConstraintType;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServiceFilterUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("NodeFilterValidator")
public class NodeFilterValidator {

    private static final String SOURCE = "Source";
    public static final Set<String> comparableTypes = Set
        .of(ToscaPropertyType.STRING.getType(), ToscaPropertyType.INTEGER.getType(), ToscaPropertyType.FLOAT.getType());
    public static final Set<String> schemableTypes = Set.of(ToscaPropertyType.MAP.getType(), ToscaPropertyType.LIST.getType());
    public static final Set<String> comparableConstraintsOperators = Set
        .of(ConstraintConvertor.GREATER_THAN_OPERATOR, ConstraintConvertor.LESS_THAN_OPERATOR);
    protected final ToscaOperationFacade toscaOperationFacade;
    protected final ComponentsUtils componentsUtils;
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFilterValidator.class);
    private final ApplicationDataTypeCache applicationDataTypeCache;

    @Autowired
    public NodeFilterValidator(final ToscaOperationFacade toscaOperationFacade, final ComponentsUtils componentsUtils,
                               final ApplicationDataTypeCache applicationDataTypeCache) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
        this.applicationDataTypeCache = applicationDataTypeCache;
    }

    public Either<Boolean, ResponseFormat> validateComponentInstanceExist(final Component component, final String componentInstanceId) {
        if (component == null || StringUtils.isEmpty(componentInstanceId)) {
            LOGGER.error("Input data cannot be empty");
            return getErrorResponse(ActionStatus.FILTER_NOT_FOUND);
        }
        if (CollectionUtils.isEmpty(component.getComponentInstances()) || component.getComponentInstances().stream()
            .noneMatch(ci -> ci.getUniqueId().equals(componentInstanceId))) {
            LOGGER.error("Component Instance list is empty");
            return getErrorResponse(ActionStatus.FILTER_NOT_FOUND);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> getErrorResponse(ActionStatus actionStatus, String... variables) {
        ResponseFormat errorResponse = ResponseFormatManager.getInstance().getResponseFormat(actionStatus, variables);
        return Either.right(errorResponse);
    }

    public Either<Boolean, ResponseFormat> validateFilter(final Component parentComponent, final String componentInstanceId,
                                                          final List<String> uiConstraints, final NodeFilterConstraintAction action,
                                                          final NodeFilterConstraintType nodeFilterConstraintType,
                                                          final String capabilityName) {
        if (NodeFilterConstraintAction.ADD != action && NodeFilterConstraintAction.UPDATE != action) {
            return Either.left(true);
        }
        if (CollectionUtils.isEmpty(uiConstraints)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CONSTRAINT_FORMAT_INCORRECT));
        }
        try {
            for (final String uiConstraint : uiConstraints) {
                final UIConstraint constraint = new ConstraintConvertor().convert(uiConstraint);
                if (constraint.getSourceType() == null) {
                    return Either.left(true);
                }
                switch (constraint.getSourceType()) {
                    case ConstraintConvertor.PROPERTY_CONSTRAINT: {
                        return validatePropertyConstraint(parentComponent, componentInstanceId, constraint, capabilityName);
                    }
                    case ConstraintConvertor.SERVICE_INPUT_CONSTRAINT: {
                        return validateInputConstraint(parentComponent, componentInstanceId, constraint);
                    }
                    case ConstraintConvertor.STATIC_CONSTRAINT: {
                        if (NodeFilterConstraintType.PROPERTIES.equals(nodeFilterConstraintType)) {
                            return isComponentPropertyFilterValid(parentComponent, componentInstanceId, constraint);
                        } else {
                            return isComponentCapabilityPropertyFilterValid(parentComponent, componentInstanceId, constraint);
                        }
                    }
                    default:
                        final ToscaFunctionType toscaFunctionType = ToscaFunctionType.findType(constraint.getSourceType()).orElse(null);
                        if (toscaFunctionType != null) {
                            if (toscaFunctionType == ToscaFunctionType.GET_INPUT) {
                                return validateInputConstraint(parentComponent, componentInstanceId, constraint);
                            }
                            if (toscaFunctionType == ToscaFunctionType.GET_PROPERTY) {
                                return validatePropertyConstraint(parentComponent, componentInstanceId, constraint, capabilityName);
                            }
                            return Either.left(true);
                        }

                        return Either.right(componentsUtils.getResponseFormat(ActionStatus.CONSTRAINT_FORMAT_INCORRECT));

                }
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Provided constraints '{}'", String.join(",\n", uiConstraints), e);
            }
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CONSTRAINT_FORMAT_INCORRECT));
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> isComponentCapabilityPropertyFilterValid(final Component parentComponent,
                                                                                     final String componentInstanceId,
                                                                                     final UIConstraint uiConstraint) {
        return validateStaticValueAndOperatorOfCapabilityProperties(parentComponent, componentInstanceId, uiConstraint);
    }

    private Either<Boolean, ResponseFormat> isComponentPropertyFilterValid(Component parentComponent, String componentInstanceId,
                                                                           UIConstraint constraint) {
        return validateStaticValueAndOperator(parentComponent, componentInstanceId, constraint);
    }

    private Either<Boolean, ResponseFormat> validatePropertyConstraint(final Component parentComponent, final String componentInstanceId,
                                                                       final UIConstraint uiConstraint, final String capabilityName) {
        String source = SOURCE;
        final Optional<? extends PropertyDefinition> sourceSelectedProperty = findSourceProperty(parentComponent, uiConstraint);
        if (sourceSelectedProperty.isPresent()) {
            Optional<? extends PropertyDefinition> targetComponentInstanceProperty =
                getProperty(parentComponent, componentInstanceId, capabilityName, uiConstraint.getServicePropertyName());

            source = targetComponentInstanceProperty.isEmpty() ? "Target" : SOURCE;
            if (targetComponentInstanceProperty.isPresent()) {
                return validatePropertyData(uiConstraint, sourceSelectedProperty, targetComponentInstanceProperty);
            }
        }
        final String missingProperty = SOURCE.equals(source) ? uiConstraint.getValue().toString() : uiConstraint.getServicePropertyName();
        return Either.right(componentsUtils.getResponseFormat(ActionStatus.MAPPED_PROPERTY_NOT_FOUND, source, missingProperty));
    }

    private Optional<? extends PropertyDefinition> findSourceProperty(final Component parentComponent, final UIConstraint uiConstraint) {
        final List<? extends PropertyDefinition> sourcePropertyDefinition;
        if ("SELF".equalsIgnoreCase(uiConstraint.getSourceName())) {
            sourcePropertyDefinition =
                parentComponent.getProperties() != null ? parentComponent.getProperties() : Collections.emptyList();
        } else {
            final ComponentInstance componentInstance = parentComponent.getComponentInstances().stream()
                .filter(componentInstance1 -> uiConstraint.getSourceName().equals(componentInstance1.getName())).findFirst().orElse(null);
            if (componentInstance == null) {
                sourcePropertyDefinition = Collections.emptyList();
            } else {
                final List<ComponentInstanceProperty> componentInstanceProperties =
                    parentComponent.getComponentInstancesProperties().get(componentInstance.getUniqueId());
                sourcePropertyDefinition = componentInstanceProperties == null ? Collections.emptyList() : componentInstanceProperties;
            }
        }
        final List<String> valuePath = ServiceFilterUtils.extractGetFunctionValue(uiConstraint);
        if (valuePath.size() == 1) {
            return sourcePropertyDefinition.stream()
                .filter(propertyDefinition -> propertyDefinition.getName().equals(valuePath.get(0))).findFirst();
        }
        if (valuePath.size() == 2) {
            return sourcePropertyDefinition.stream()
                .filter(propertyDefinition -> propertyDefinition.getName().equals(valuePath.get(1))).findFirst();
        }
        PropertyDefinition sourceProperty = sourcePropertyDefinition.stream()
            .filter(propertyDefinition -> propertyDefinition.getName().equals(valuePath.get(1))).findFirst().orElse(null);
        if (sourceProperty == null) {
            return Optional.empty();
        }
        final Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypesEither =
            applicationDataTypeCache.getAll(parentComponent.getModel());
        if (allDataTypesEither.isRight()) {
            return Optional.empty();
        }
        return findSourceProperty(valuePath.subList(2, valuePath.size()), sourceProperty, allDataTypesEither.left().value());
    }

    private Optional<PropertyDefinition> findSourceProperty(final List<String> propertyPath, final PropertyDefinition sourceProperty,
                                                            final Map<String, DataTypeDefinition> modelDataTypes) {
        final DataTypeDefinition dataTypeDefinition = modelDataTypes.get(sourceProperty.getType());
        if (CollectionUtils.isEmpty(dataTypeDefinition.getProperties())) {
            return Optional.empty();
        }
        final PropertyDefinition propertyDefinition = dataTypeDefinition.getProperties().stream()
            .filter(propertyDefinition1 -> propertyDefinition1.getName().equals(propertyPath.get(0))).findFirst().orElse(null);
        if (propertyDefinition == null) {
            return Optional.empty();
        }
        if (propertyPath.size() == 1) {
            return Optional.of(propertyDefinition);
        }
        return findSourceProperty(propertyPath.subList(1, propertyPath.size()), propertyDefinition, modelDataTypes);
    }
    
    private Optional<ComponentInstanceProperty> getProperty(final Component parentComponent, final String componentInstanceId,
            final String capabilityName, final String propertyName) {

        if (StringUtils.isEmpty(capabilityName)) {
            return parentComponent.getComponentInstancesProperties().get(componentInstanceId).stream()
                    .filter(property -> propertyName.equals(property.getName())).findFirst();
        } else {
            final Optional<ComponentInstance> componentInstanceOptional = parentComponent.getComponentInstances().stream()
                    .filter(componentInstance -> componentInstance.getUniqueId().equals(componentInstanceId)).findAny();
            if (componentInstanceOptional.isPresent()) {
                for (final List<CapabilityDefinition> listOfCaps : componentInstanceOptional.get().getCapabilities().values()) {
                    final Optional<CapabilityDefinition> capDef = listOfCaps.stream().filter(cap -> cap.getName().equals(capabilityName)).findAny();
                    if (capDef.isPresent()) {
                        return capDef.get().getProperties().stream().filter(property -> propertyName.equals(property.getName())).findFirst();
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Either<Boolean, ResponseFormat> validateInputConstraint(final Component parentComponent, final String componentInstanceId,
                                                                    final UIConstraint uiConstraint) {
        final List<InputDefinition> sourceInputDefinition = parentComponent.getInputs();
        if (CollectionUtils.isEmpty(sourceInputDefinition)) {
            LOGGER.debug("Parent component '{}', unique id '{}', does not have inputs", parentComponent.getName(), parentComponent.getUniqueId());
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INPUTS_NOT_FOUND));
        }

        final List<String> getInputValuePath = ServiceFilterUtils.extractGetFunctionValue(uiConstraint);
        if (getInputValuePath.isEmpty()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INPUTS_NOT_FOUND));
        }
        Optional<? extends PropertyDefinition> sourceSelectedProperty =
            sourceInputDefinition.stream().filter(input -> getInputValuePath.get(0).equals(input.getName())).findFirst();
        if (sourceSelectedProperty.isEmpty()) {
            LOGGER.debug("Input '{}' not found in parent component '{}', unique id '{}'",
                getInputValuePath.get(0), parentComponent.getName(), parentComponent.getUniqueId());
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INPUTS_NOT_FOUND));
        }
        if (getInputValuePath.size() > 1) {
            final Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypesEither =
                applicationDataTypeCache.getAll(parentComponent.getModel());
            if (allDataTypesEither.isRight()) {
                LOGGER.error("Could not load data types for model {}", parentComponent.getModel());
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INPUTS_NOT_FOUND));
            }
            sourceSelectedProperty =
                findSourceProperty(getInputValuePath.subList(1, getInputValuePath.size()), sourceSelectedProperty.get(),
                    allDataTypesEither.left().value());
        }
        final Optional<? extends PropertyDefinition> targetComponentInstanceProperty = parentComponent.getComponentInstancesProperties()
            .get(componentInstanceId).stream().filter(property -> uiConstraint.getServicePropertyName().equals(property.getName())).findFirst();
        if (sourceSelectedProperty.isPresent() && targetComponentInstanceProperty.isPresent()) {
            return validatePropertyData(uiConstraint, sourceSelectedProperty, targetComponentInstanceProperty);
        }

        return Either.right(componentsUtils.getResponseFormat(ActionStatus.INPUTS_NOT_FOUND));
    }

    private Either<Boolean, ResponseFormat> validatePropertyData(UIConstraint uiConstraint,
                                                                 Optional<? extends PropertyDefinition> sourceSelectedProperty,
                                                                 Optional<? extends PropertyDefinition> targetComponentInstanceProperty) {
        if (sourceSelectedProperty.isPresent() && targetComponentInstanceProperty.isPresent()) {
            final PropertyDefinition sourcePropDefinition = sourceSelectedProperty.get();
            final String sourceType = sourcePropDefinition.getType();
            final PropertyDefinition targetPropDefinition = targetComponentInstanceProperty.get();
            final String targetType = targetPropDefinition.getType();
            if (sourceType.equals(targetType)) {
                if (schemableTypes.contains(sourceType)) {
                    final String sourceSchemaType = sourcePropDefinition.getSchemaType();
                    final String targetSchemaType = targetPropDefinition.getSchemaType();
                    if (sourceSchemaType != null && !sourceSchemaType.equals(targetSchemaType)) {
                        return Either.right(componentsUtils
                            .getResponseFormat(ActionStatus.SOURCE_TARGET_SCHEMA_MISMATCH, targetPropDefinition.getName(), targetSchemaType,
                                sourcePropDefinition.getName(), sourceSchemaType));
                    }
                }
                return Either.left(Boolean.TRUE);
            } else {
                return Either.right(componentsUtils
                    .getResponseFormat(ActionStatus.SOURCE_TARGET_PROPERTY_TYPE_MISMATCH, uiConstraint.getServicePropertyName(),
                        uiConstraint.getValue().toString()));
            }
        } else {
            LOGGER.debug("Null value passed to `validatePropertyData` - sourceSelectedProperty: '{}' - targetComponentInstanceProperty: '{}'",
                sourceSelectedProperty, targetComponentInstanceProperty);
            return Either.right(componentsUtils
                .getResponseFormat(ActionStatus.GENERAL_ERROR, uiConstraint.getServicePropertyName(), uiConstraint.getValue().toString()));
        }
    }

    private Either<Boolean, ResponseFormat> validateStaticValueAndOperator(final Component parentComponent, final String componentInstanceId,
                                                                           final UIConstraint uiConstraint) {
        if (!(Objects.nonNull(uiConstraint) && uiConstraint.getValue() instanceof String)) {
            return Either.left(false);
        }
        //TODO: get capabilities properties when constraint type is capabilities
        final Optional<ComponentInstanceProperty> componentInstanceProperty = parentComponent.getComponentInstancesProperties()
            .get(componentInstanceId).stream().filter(property -> uiConstraint.getServicePropertyName().equals(property.getName())).findFirst();
        if (componentInstanceProperty.isEmpty()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.SELECTED_PROPERTY_NOT_PRESENT, uiConstraint.getServicePropertyName()));
        }
        if (comparableConstraintsOperators.contains(uiConstraint.getConstraintOperator()) && !comparableTypes
            .contains(componentInstanceProperty.get().getType())) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_OPERATOR_PROVIDED, uiConstraint.getServicePropertyName(),
                uiConstraint.getConstraintOperator()));
        }
        return isValidValueCheck(componentInstanceProperty.get().getType(), String.valueOf(uiConstraint.getValue()),
            uiConstraint.getServicePropertyName());
    }

    private Either<Boolean, ResponseFormat> validateStaticValueAndOperatorOfCapabilityProperties(final Component parentComponent,
                                                                                                 final String componentInstanceId,
                                                                                                 final UIConstraint uiConstraint) {
        if (!(Objects.nonNull(uiConstraint) && uiConstraint.getValue() instanceof String)) {
            return Either.left(false);
        }
        Optional<ComponentInstanceProperty> optionalComponentInstanceProperty = Optional.empty();
        final Optional<ComponentInstance> optionalComponentInstances = parentComponent.getComponentInstances().stream()
            .filter(componentInstance -> componentInstanceId.equalsIgnoreCase(componentInstance.getUniqueId())).findFirst();
        if (optionalComponentInstances.isPresent()) {
            final Optional<List<CapabilityDefinition>> optionalCapabilityDefinitionList = optionalComponentInstances.get().getCapabilities().values()
                .stream().filter(capabilityDefinitions -> capabilityDefinitions.stream()
                    .allMatch(capabilityDefinition -> capabilityDefinition.getProperties() != null)).collect(Collectors.toList()).stream().filter(
                    capabilityDefinitions -> capabilityDefinitions.stream().allMatch(
                        capabilityDefinition -> capabilityDefinition.getProperties().stream().anyMatch(
                            componentInstanceProperty -> uiConstraint.getServicePropertyName()
                                .equalsIgnoreCase(componentInstanceProperty.getName())))).findFirst();
            if (optionalCapabilityDefinitionList.isPresent() && !optionalCapabilityDefinitionList.get().isEmpty()) {
                optionalComponentInstanceProperty = getComponentInstanceProperty(optionalCapabilityDefinitionList.get().get(0), uiConstraint.getServicePropertyName());
            }
        }

        if (optionalComponentInstanceProperty.isEmpty()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.SELECTED_PROPERTY_NOT_PRESENT, uiConstraint.getServicePropertyName()));
        }
        if (comparableConstraintsOperators.contains(uiConstraint.getConstraintOperator()) && !comparableTypes
            .contains(optionalComponentInstanceProperty.get().getType())) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_OPERATOR_PROVIDED, uiConstraint.getServicePropertyName(),
                uiConstraint.getConstraintOperator()));
        }
        return isValidValueCheck(optionalComponentInstanceProperty.get().getType(), String.valueOf(uiConstraint.getValue()),
            uiConstraint.getServicePropertyName());
    }
    
    private Optional<ComponentInstanceProperty> getComponentInstanceProperty(CapabilityDefinition capabilityDefinition, final String propertyName){
    	return capabilityDefinition.getProperties().stream().filter(property -> property.getName().equals(propertyName)).findAny();
    }

    private Either<Boolean, ResponseFormat> isValidValueCheck(String type, String value, String propertyName) {
        ToscaPropertyType toscaPropertyType = ToscaPropertyType.isValidType(type);
        if (Objects.isNull(toscaPropertyType)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_PROPERTY_TYPE, type, propertyName));
        }
        if (toscaPropertyType.getValidator().isValid(value, null)) {
            return Either.left(Boolean.TRUE);
        }
        return Either.right(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_VALUE_PROVIDED, type, propertyName, value));
    }

    public Either<Boolean, ResponseFormat> validateComponentFilter(final Component component, final List<String> uiConstraints,
                                                                   final NodeFilterConstraintAction action) {
        if (NodeFilterConstraintAction.ADD != action && NodeFilterConstraintAction.UPDATE != action) {
            return Either.left(true);
        }
        if (CollectionUtils.isEmpty(uiConstraints)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CONSTRAINT_FORMAT_INCORRECT));
        }
        try {
            for (final String uiConstraint : uiConstraints) {
                final UIConstraint constraint = new ConstraintConvertor().convert(uiConstraint);
                if (constraint.getSourceType() == null) {
                    return Either.left(true);
                }
                switch (constraint.getSourceType()) {
                    case ConstraintConvertor.PROPERTY_CONSTRAINT:
                        return validateComponentPropertyConstraint(component, constraint);
                    case ConstraintConvertor.STATIC_CONSTRAINT:
                        return validateComponentStaticValueAndOperator(component, constraint);
                    default:
                        final ToscaFunctionType toscaFunctionType = ToscaFunctionType.findType(constraint.getSourceType()).orElse(null);
                        if (toscaFunctionType != null) {
                            if (toscaFunctionType == ToscaFunctionType.GET_PROPERTY) {
                                return validateComponentPropertyConstraint(component, constraint);
                            }
                            return Either.left(true);
                        }

                        return Either.right(componentsUtils.getResponseFormat(ActionStatus.CONSTRAINT_FORMAT_INCORRECT));
                }
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Provided constraints '{}'", String.join(",\n", uiConstraints), e);
            }
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CONSTRAINT_FORMAT_INCORRECT));
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateComponentPropertyConstraint(final Component component, final UIConstraint uiConstraint) {
        String source = SOURCE;
        final List<PropertyDefinition> componentProperties = component.getProperties();
        if (CollectionUtils.isNotEmpty(componentProperties)) {
            final Optional<? extends PropertyDefinition> sourceSelectedProperty = componentProperties.stream()
                .filter(property -> {
                    if (uiConstraint.getValue() instanceof Map) {
                        final Object getPropertyValue = ((Map<?, ?>) uiConstraint.getValue()).get(ToscaFunctionType.GET_PROPERTY.getName());
                        if (getPropertyValue instanceof List) {
                            final List<?> getPropertyValueList = (List<?>) getPropertyValue;
                            if (getPropertyValueList.size() > 1) {
                                return getPropertyValueList.get(1).equals(property.getName());
                            }
                        }
                        return false;
                    }

                    return uiConstraint.getValue().equals(property.getName());
                })
                .findFirst();
            final Optional<? extends PropertyDefinition> targetComponentProperty = componentProperties.stream()
                .filter(property -> uiConstraint.getServicePropertyName().equals(property.getName())).findFirst();
            source = targetComponentProperty.isEmpty() ? "Target" : SOURCE;
            if (sourceSelectedProperty.isPresent() && targetComponentProperty.isPresent()) {
                return validatePropertyData(uiConstraint, sourceSelectedProperty, targetComponentProperty);
            }
        }
        final String missingProperty = source.equals(SOURCE) ? uiConstraint.getValue().toString() : uiConstraint.getServicePropertyName();
        return Either.right(componentsUtils.getResponseFormat(ActionStatus.MAPPED_PROPERTY_NOT_FOUND, source, missingProperty));
    }

    private Either<Boolean, ResponseFormat> validateComponentStaticValueAndOperator(final Component component, final UIConstraint uiConstraint) {
        if (uiConstraint == null
            || (!(uiConstraint.getValue() instanceof String)
                && !(uiConstraint.getValue() instanceof Number)
                && !(uiConstraint.getValue() instanceof Boolean))
        ) {
            return Either.left(false);
        }
        final Optional<PropertyDefinition> componentProperty = component.getProperties().stream()
            .filter(property -> uiConstraint.getServicePropertyName().equals(property.getName())).findFirst();
        if (componentProperty.isEmpty()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.SELECTED_PROPERTY_NOT_PRESENT, uiConstraint.getServicePropertyName()));
        }
        if (comparableConstraintsOperators.contains(uiConstraint.getConstraintOperator()) && !comparableTypes
            .contains(componentProperty.get().getType())) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_OPERATOR_PROVIDED, uiConstraint.getServicePropertyName(),
                uiConstraint.getConstraintOperator()));
        }
        return isValidValueCheck(componentProperty.get().getType(), String.valueOf(uiConstraint.getValue()), uiConstraint.getServicePropertyName());
    }
}
