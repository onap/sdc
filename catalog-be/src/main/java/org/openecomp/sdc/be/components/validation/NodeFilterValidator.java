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

import com.google.gson.Gson;
import fj.data.Either;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.PropertyFilterTargetType;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.ToscaPropertyData;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.dto.FilterConstraintDto;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.validators.DataTypeValidatorConverter;
import org.openecomp.sdc.be.model.validation.FilterConstraintValidator;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("NodeFilterValidator")
public class NodeFilterValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFilterValidator.class);
    private static final String SOURCE = "Source";
    private static final String TARGET = "Target";
    private static final String INPUT_NOT_FOUND_LOG = "Input '{}' not found in parent component '{}', unique id '{}'";
    private static final Set<String> TYPES_WITH_SCHEMA = Set.of(ToscaPropertyType.MAP.getType(), ToscaPropertyType.LIST.getType());
    private static final Set<String> COMPARABLE_TYPES = Set
        .of(ToscaPropertyType.STRING.getType(), ToscaPropertyType.INTEGER.getType(), ToscaPropertyType.FLOAT.getType());
    private final ComponentsUtils componentsUtils;
    private final ApplicationDataTypeCache applicationDataTypeCache;
    private final FilterConstraintValidator filterConstraintValidator;

    @Autowired
    public NodeFilterValidator(final ComponentsUtils componentsUtils, final ApplicationDataTypeCache applicationDataTypeCache,
                               final FilterConstraintValidator filterConstraintValidator) {
        this.componentsUtils = componentsUtils;
        this.applicationDataTypeCache = applicationDataTypeCache;
        this.filterConstraintValidator = filterConstraintValidator;
    }

    public Either<Boolean, ResponseFormat> validateComponentInstanceExist(final Component component, final String componentInstanceId) {
        if (component == null || StringUtils.isEmpty(componentInstanceId)) {
            LOGGER.error("Expecting a component and a component instance id, given was '{}' and '{}'", component, componentInstanceId);
            final String componentName = component == null ? "?" : component.getName();
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND, componentName, componentInstanceId));
        }
        if (CollectionUtils.isEmpty(component.getComponentInstances()) || component.getComponentInstances().stream()
            .noneMatch(ci -> ci.getUniqueId().equals(componentInstanceId))) {
            LOGGER.error("Component '{}' node instance list is empty or component instance '{}' not found",
                component.getUniqueId(), componentInstanceId);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND,
                component.getName(), componentInstanceId));
        }
        return Either.left(Boolean.TRUE);
    }

    public Either<Boolean, ResponseFormat> validateFilter(final Component parentComponent, final String componentInstanceId,
                                                          final List<FilterConstraintDto> filterConstraint) {
        if (CollectionUtils.isEmpty(filterConstraint)) {
            return Either.left(true);
        }
        for (final FilterConstraintDto filterConstraintDto : filterConstraint) {
            final Either<Boolean, ResponseFormat> validationEither =
                validateFilter(parentComponent, componentInstanceId, filterConstraintDto);
            if (validationEither.isRight()) {
                return validationEither;
            }
        }
        return Either.left(true);
    }
    public Either<Boolean, ResponseFormat> validateFilter(final Component parentComponent, final String componentInstanceId,
                                                          final FilterConstraintDto filterConstraint) {
        validateFilterConstraint(filterConstraint);
        switch (filterConstraint.getValueType()) {
            case STATIC:
                if (filterConstraint.isCapabilityPropertyFilter()) {
                    return validateStaticValueAndOperatorOfCapabilityProperties(parentComponent, componentInstanceId, filterConstraint);
                } else {
                    return validateStaticValueAndOperator(parentComponent, componentInstanceId, filterConstraint);
                }
            case GET_PROPERTY:
                return validatePropertyConstraint(parentComponent, componentInstanceId, filterConstraint, filterConstraint.getCapabilityName());
            case GET_INPUT:
                return validateInputConstraint(parentComponent, componentInstanceId, filterConstraint);
            default:
                return Either.left(true);
        }
    }

    private void validateFilterConstraint(final FilterConstraintDto filterConstraint) {
        filterConstraintValidator.validate(filterConstraint);
    }

    private Either<Boolean, ResponseFormat> validatePropertyConstraint(final Component parentComponent, final String componentInstanceId,
                                                                       final FilterConstraintDto filterConstraint, final String capabilityName) {
        String source = SOURCE;
        final ToscaGetFunctionDataDefinition toscaGetFunction = filterConstraint.getAsToscaGetFunction().orElse(null);
        if (toscaGetFunction == null || !(filterConstraint.getValue() instanceof ToscaGetFunctionDataDefinition)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.TOSCA_FUNCTION_EXPECTED_ERROR));
        }
        final Optional<? extends ToscaPropertyData> sourceSelectedProperty = findPropertyFromGetFunction(parentComponent, toscaGetFunction);
        if (sourceSelectedProperty.isPresent()) {
            Optional<? extends PropertyDefinition> targetComponentInstanceProperty =
                getInstanceProperties(parentComponent, componentInstanceId, capabilityName, filterConstraint.getPropertyName());

            source = targetComponentInstanceProperty.isEmpty() ? TARGET : SOURCE;
            if (targetComponentInstanceProperty.isPresent()) {
                final ResponseFormat responseFormat = validatePropertyData(sourceSelectedProperty.get(), targetComponentInstanceProperty.get());
                if (responseFormat != null) {
                    return Either.right(responseFormat);
                }
                return Either.left(true);
            }
        }
        final String missingProperty = SOURCE.equals(source) ? filterConstraint.getValue().toString() : filterConstraint.getPropertyName();
        return Either.right(componentsUtils.getResponseFormat(ActionStatus.FILTER_PROPERTY_NOT_FOUND, source, missingProperty));
    }

    private Optional<? extends ToscaPropertyData> findPropertyFromGetFunction(final Component parentComponent,
                                                                              final ToscaGetFunctionDataDefinition toscaGetFunction) {
        List<? extends ToscaPropertyData> sourcePropertyDefinitions;
        if (PropertySource.SELF == toscaGetFunction.getPropertySource()) {
            sourcePropertyDefinitions = getSelfPropertyFromGetFunction(parentComponent, toscaGetFunction);
        } else {
            sourcePropertyDefinitions = getInstancePropertiesBasedOnGetFunctionSource(parentComponent, toscaGetFunction);
        }
        final List<String> propertyPath = toscaGetFunction.getPropertyPathFromSource();
        final Optional<? extends ToscaPropertyData> sourceProperty = sourcePropertyDefinitions.stream()
            .filter(propertyDefinition -> propertyDefinition.getName().equals(propertyPath.get(0))).findFirst();
        if (sourceProperty.isEmpty() || propertyPath.size() == 1) {
            return sourceProperty;
        }
        final Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypesEither =
            applicationDataTypeCache.getAll(parentComponent.getModel());
        if (allDataTypesEither.isRight()) {
            return Optional.empty();
        }
        return findSubProperty(propertyPath.subList(1, propertyPath.size()), sourceProperty.get().getType(), allDataTypesEither.left().value());
    }

    private List<? extends ToscaPropertyData> getInstancePropertiesBasedOnGetFunctionSource(final Component parentComponent,
                                                                                            final ToscaGetFunctionDataDefinition toscaGetFunction) {
        final ComponentInstance componentInstance = parentComponent.getComponentInstances().stream()
            .filter(componentInstance1 -> componentInstance1.getName().equals(toscaGetFunction.getSourceName()))
            .findFirst()
            .orElse(null);
        if (componentInstance == null) {
            return List.of();
        }
        final List<? extends ToscaPropertyData> instanceProperties;
        switch (toscaGetFunction.getFunctionType()) {
            case GET_PROPERTY:
                instanceProperties = parentComponent.getComponentInstancesProperties().get(componentInstance.getUniqueId());
                break;
            case GET_ATTRIBUTE:
                instanceProperties = parentComponent.getComponentInstancesAttributes().get(componentInstance.getUniqueId());
                break;
            default:
                instanceProperties = List.of();
        }
        if (instanceProperties == null) {
            return List.of();
        }
        return instanceProperties;
    }

    private static List<? extends ToscaPropertyData> getSelfPropertyFromGetFunction(final Component component,
                                                                                    final ToscaGetFunctionDataDefinition toscaGetFunction) {
        switch (toscaGetFunction.getFunctionType()) {
            case GET_INPUT:
                if (component.getInputs() != null) {
                    return component.getInputs();
                }
                break;
            case GET_PROPERTY:
                if (component.getProperties() != null) {
                    return component.getProperties();
                }
                break;
            case GET_ATTRIBUTE:
                if (component.getAttributes() != null) {
                    return component.getAttributes();
                }
                break;
        }
        return List.of();
    }

    private Optional<PropertyDefinition> findSubProperty(final List<String> propertyPath, final String parentPropertyType,
                                                         final Map<String, DataTypeDefinition> modelDataTypes) {
        final DataTypeDefinition dataTypeDefinition = modelDataTypes.get(parentPropertyType);
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
        return findSubProperty(propertyPath.subList(1, propertyPath.size()), propertyDefinition.getType(), modelDataTypes);
    }
    
    private Optional<ComponentInstanceProperty> getInstanceProperties(final Component parentComponent, final String componentInstanceId,
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
                                                                    final FilterConstraintDto filterConstraint) {
        final List<InputDefinition> sourceInputDefinition = parentComponent.getInputs();
        if (CollectionUtils.isEmpty(sourceInputDefinition)) {
            LOGGER.debug("Parent component '{}', unique id '{}', does not have inputs", parentComponent.getName(), parentComponent.getUniqueId());
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_DOES_NOT_HAVE_INPUTS, parentComponent.getName()));
        }
        if (!(filterConstraint.getValue() instanceof ToscaGetFunctionDataDefinition)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.TOSCA_FUNCTION_EXPECTED_ERROR));
        }
        final ToscaGetFunctionDataDefinition getFunction = (ToscaGetFunctionDataDefinition) filterConstraint.getValue();
        final List<String> propertyPathFromSource = getFunction.getPropertyPathFromSource();
        Optional<? extends PropertyDefinition> sourceSelectedProperty =
            sourceInputDefinition.stream().filter(input -> input.getName().equals(propertyPathFromSource.get(0))).findFirst();
        if (sourceSelectedProperty.isEmpty()) {
            LOGGER.debug(INPUT_NOT_FOUND_LOG,
                propertyPathFromSource.get(0), parentComponent.getName(), parentComponent.getUniqueId());
            return Either.right(
                componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INPUT_NOT_FOUND, propertyPathFromSource.get(0), parentComponent.getName())
            );
        }
        if (propertyPathFromSource.size() > 1) {
            final Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypesEither =
                applicationDataTypeCache.getAll(parentComponent.getModel());
            if (allDataTypesEither.isRight()) {
                LOGGER.error("Could not load data types for model {}", parentComponent.getModel());
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.DATA_TYPES_NOT_LOADED, parentComponent.getModel()));
            }
            sourceSelectedProperty =
                findSubProperty(propertyPathFromSource.subList(1, propertyPathFromSource.size()), sourceSelectedProperty.get().getType(),
                    allDataTypesEither.left().value());
        }
        final Optional<? extends PropertyDefinition> targetComponentInstanceProperty;
        if (PropertyFilterTargetType.CAPABILITY.equals(filterConstraint.getTargetType())) {
            final CapabilityDefinition capability = parentComponent.getComponentInstances().stream()
                .filter(componentInstance -> componentInstance.getUniqueId().equals(componentInstanceId))
                .map(componentInstance -> componentInstance.getCapabilities().values())
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .filter(capabilityDefinition -> capabilityDefinition.getName().equals(filterConstraint.getCapabilityName()))
                .findFirst().orElse(null);
            if (capability == null) {
                return Either.right(
                    componentsUtils.getResponseFormat(ActionStatus.CAPABILITY_NOT_FOUND_IN_COMPONENT,
                        filterConstraint.getCapabilityName(), parentComponent.getComponentType().getValue(), parentComponent.getName())
                );
            }
            targetComponentInstanceProperty = capability.getProperties().stream()
                .filter(property -> filterConstraint.getPropertyName().equals(property.getName()))
                .findFirst();
        } else {
            targetComponentInstanceProperty =
                parentComponent.getComponentInstancesProperties()
                    .get(componentInstanceId).stream()
                    .filter(property -> filterConstraint.getPropertyName().equals(property.getName()))
                    .findFirst();
        }
        if (sourceSelectedProperty.isPresent() && targetComponentInstanceProperty.isPresent()) {
            final ResponseFormat responseFormat = validatePropertyData(sourceSelectedProperty.get(), targetComponentInstanceProperty.get());
            if (responseFormat != null) {
                return Either.right(responseFormat);
            }
            return Either.left(true);
        }

        return Either.right(componentsUtils.getResponseFormat(ActionStatus.INPUTS_NOT_FOUND));
    }

    private <T extends ToscaPropertyData> ResponseFormat validatePropertyData(final T sourcePropDefinition,
                                                                              final T targetPropDefinition) {
        final String sourceType = sourcePropDefinition.getType();
        final String targetType = targetPropDefinition.getType();
        if (sourceType.equals(targetType)) {
            if (TYPES_WITH_SCHEMA.contains(sourceType)) {
                final String sourceSchemaType = sourcePropDefinition.getSchemaType();
                final String targetSchemaType = targetPropDefinition.getSchemaType();
                if (sourceSchemaType != null && !sourceSchemaType.equals(targetSchemaType)) {
                    return componentsUtils.getResponseFormat(ActionStatus.SOURCE_TARGET_SCHEMA_MISMATCH,
                        targetPropDefinition.getName(), targetSchemaType, sourcePropDefinition.getName(), sourceSchemaType);
                }
            }
            return null;
        }
        return componentsUtils.getResponseFormat(ActionStatus.SOURCE_TARGET_PROPERTY_TYPE_MISMATCH,
            sourcePropDefinition.getName(), sourcePropDefinition.getType(), targetPropDefinition.getName(), targetPropDefinition.getType());
    }

    private Either<Boolean, ResponseFormat> validateStaticValueAndOperator(final Component parentComponent, final String componentInstanceId,
                                                                           final FilterConstraintDto filterConstraint) {
        final ComponentInstanceProperty componentInstanceProperty = parentComponent.getComponentInstancesProperties()
            .get(componentInstanceId).stream().filter(property -> filterConstraint.getPropertyName().equals(property.getName()))
            .findFirst()
            .orElse(null);
        if (componentInstanceProperty == null) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.SELECTED_PROPERTY_NOT_PRESENT, filterConstraint.getPropertyName()));
        }
        if (filterConstraint.getOperator().isComparable() && !COMPARABLE_TYPES.contains(componentInstanceProperty.getType())) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_OPERATOR_PROVIDED, filterConstraint.getPropertyName(),
                filterConstraint.getOperator().getType()));
        }
        return isValidValueCheck(componentInstanceProperty.getType(), componentInstanceProperty.getSchemaType(), parentComponent.getModel(),
            filterConstraint.getValue(), filterConstraint.getPropertyName());
    }

    private Either<Boolean, ResponseFormat> validateStaticSubstitutionFilter(final Component component,
                                                                             final FilterConstraintDto filterConstraint) {

        final PropertyDefinition componentProperty = component.getProperties().stream()
            .filter(property -> property.getName().equals(filterConstraint.getPropertyName())).findFirst().orElse(null);
        if (componentProperty == null) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.SELECTED_PROPERTY_NOT_PRESENT, filterConstraint.getPropertyName()));
        }
        if (filterConstraint.getOperator().isComparable() && !COMPARABLE_TYPES.contains(componentProperty.getType())) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_OPERATOR_PROVIDED, filterConstraint.getPropertyName(),
                filterConstraint.getOperator().getType()));
        }
        return isValidValueCheck(componentProperty.getType(), componentProperty.getSchemaType(), component.getModel(),
            filterConstraint.getValue(), filterConstraint.getPropertyName());
    }

    private Either<Boolean, ResponseFormat> validateStaticValueAndOperatorOfCapabilityProperties(final Component parentComponent,
                                                                                                 final String componentInstanceId,
                                                                                                 final FilterConstraintDto filterConstraint) {
        ComponentInstanceProperty componentInstanceProperty = null;
        final Optional<ComponentInstance> optionalComponentInstances = parentComponent.getComponentInstances().stream()
            .filter(componentInstance -> componentInstanceId.equalsIgnoreCase(componentInstance.getUniqueId())).findFirst();
        if (optionalComponentInstances.isPresent()) {
            final Optional<List<CapabilityDefinition>> optionalCapabilityDefinitionList = optionalComponentInstances.get().getCapabilities().values()
                .stream().filter(capabilityDefinitions -> capabilityDefinitions.stream()
                    .allMatch(capabilityDefinition -> capabilityDefinition.getProperties() != null)).collect(Collectors.toList()).stream().filter(
                    capabilityDefinitions -> capabilityDefinitions.stream().allMatch(
                        capabilityDefinition -> capabilityDefinition.getProperties().stream().anyMatch(
                            componentInstanceProperty1 -> filterConstraint.getPropertyName()
                                .equalsIgnoreCase(componentInstanceProperty1.getName())))).findFirst();
            if (optionalCapabilityDefinitionList.isPresent() && !optionalCapabilityDefinitionList.get().isEmpty()) {
                componentInstanceProperty =
                    getComponentInstanceProperty(optionalCapabilityDefinitionList.get().get(0), filterConstraint.getPropertyName()).orElse(null);
            }
        }

        if (componentInstanceProperty == null) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.SELECTED_PROPERTY_NOT_PRESENT, filterConstraint.getPropertyName()));
        }
        if (filterConstraint.getOperator().isComparable() && !COMPARABLE_TYPES.contains(componentInstanceProperty.getType())) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_OPERATOR_PROVIDED, filterConstraint.getPropertyName(),
                filterConstraint.getOperator().getType()));
        }
        return isValidValueCheck(componentInstanceProperty.getType(), componentInstanceProperty.getSchemaType(), parentComponent.getModel(),
            filterConstraint.getValue(), filterConstraint.getPropertyName());
    }
    
    private Optional<ComponentInstanceProperty> getComponentInstanceProperty(CapabilityDefinition capabilityDefinition, final String propertyName){
    	return capabilityDefinition.getProperties().stream().filter(property -> property.getName().equals(propertyName)).findAny();
    }

    private Either<Boolean, ResponseFormat> isValidValueCheck(final String type, final String schemaType, final String model,
                                                              final Object value, final String propertyName) {
        final Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypesEither =
            applicationDataTypeCache.getAll(model);
        if (allDataTypesEither.isRight()) {
            LOGGER.error("Could not validate filter value. Could not load data types for model {}", model);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.DATA_TYPES_NOT_LOADED, model));
        }
        final Map<String, DataTypeDefinition> modelDataTypesMap = allDataTypesEither.left().value();
        final ToscaPropertyType toscaPropertyType = ToscaPropertyType.isValidType(type);
        if (toscaPropertyType == null && !modelDataTypesMap.containsKey(type)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_PROPERTY_TYPE, type, propertyName));
        }
        final String valueAsJsonString;
        try {
            valueAsJsonString = new Gson().toJson(value);
        } catch (final Exception e) {
            LOGGER.debug("Unsupported property filter value", e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_VALUE_PROVIDED, type, propertyName, String.valueOf(value)));
        }
        if (toscaPropertyType != null) {
            if (toscaPropertyType.getValidator().isValid(valueAsJsonString, schemaType, modelDataTypesMap)) {
                return Either.left(true);
            }
        } else {
            if (DataTypeValidatorConverter.getInstance().isValid(valueAsJsonString, modelDataTypesMap.get(type), modelDataTypesMap)) {
                return Either.left(true);
            }
        }

        return Either.right(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_VALUE_PROVIDED, type, propertyName, valueAsJsonString));
    }

    public Either<Boolean, ResponseFormat> validateSubstitutionFilter(final Component component, final List<FilterConstraintDto> filterConstraintList) {
        if (CollectionUtils.isEmpty(filterConstraintList)) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CONSTRAINT_FORMAT_INCORRECT));
        }
        for (final FilterConstraintDto filterConstraintDto : filterConstraintList) {
            final Either<Boolean, ResponseFormat> validationEither = validateSubstitutionFilter(component, filterConstraintDto);
            if (validationEither.isRight()) {
                return validationEither;
            }
        }
        return Either.left(true);
    }

    public Either<Boolean, ResponseFormat> validateSubstitutionFilter(final Component component, final FilterConstraintDto filterConstraint) {
        validateFilterConstraint(filterConstraint);
        switch (filterConstraint.getValueType()) {
            case STATIC:
                return validateStaticSubstitutionFilter(component, filterConstraint);
            case GET_PROPERTY:
            case GET_ATTRIBUTE:
            case GET_INPUT:
                return validateSubstitutionFilterGetFunctionConstraint(component, filterConstraint);
            default:
                return Either.left(true);
        }
    }

    private Either<Boolean, ResponseFormat> validateSubstitutionFilterGetFunctionConstraint(final Component component,
                                                                                            final FilterConstraintDto filterConstraint) {
        final ToscaGetFunctionDataDefinition toscaGetFunction = filterConstraint.getAsToscaGetFunction().orElse(null);
        if (toscaGetFunction == null) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.TOSCA_FUNCTION_EXPECTED_ERROR));
        }

        if (CollectionUtils.isEmpty(component.getProperties())) {
            return Either.right(
                componentsUtils.getResponseFormat(ActionStatus.FILTER_PROPERTY_NOT_FOUND, TARGET, getPropertyType(toscaGetFunction),
                    filterConstraint.getPropertyName())
            );
        }

        final Optional<? extends PropertyDefinition> targetComponentProperty = component.getProperties().stream()
            .filter(property -> property.getName().equals(filterConstraint.getPropertyName())).findFirst();
        if (targetComponentProperty.isEmpty()) {
            return Either.right(
                componentsUtils.getResponseFormat(ActionStatus.FILTER_PROPERTY_NOT_FOUND, TARGET, getPropertyType(toscaGetFunction),
                    filterConstraint.getPropertyName())
            );
        }

        final Optional<? extends ToscaPropertyData> sourceSelectedProperty = findPropertyFromGetFunction(component, toscaGetFunction);
        if (sourceSelectedProperty.isEmpty()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.FILTER_PROPERTY_NOT_FOUND, SOURCE, getPropertyType(toscaGetFunction),
                String.join("->", toscaGetFunction.getPropertyPathFromSource())));
        }

        final ResponseFormat responseFormat = validatePropertyData(sourceSelectedProperty.get(), targetComponentProperty.get());
        if (responseFormat != null) {
            return Either.right(responseFormat);
        }
        return Either.left(true);
    }

    private String getPropertyType(final ToscaGetFunctionDataDefinition toscaGetFunction) {
        switch (toscaGetFunction.getType()) {
            case GET_INPUT:
                return "input";
            case GET_PROPERTY:
                return "property";
            case GET_ATTRIBUTE:
                return "attribute";
            default:
                return "";
        }
    }

}
