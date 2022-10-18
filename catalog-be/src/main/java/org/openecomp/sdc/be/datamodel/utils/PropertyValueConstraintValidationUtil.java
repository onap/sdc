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
package org.openecomp.sdc.be.datamodel.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fj.data.Either;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintUtil;
import org.openecomp.sdc.be.model.tosca.constraints.LengthConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.MaxLengthConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.MinLengthConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyValueConstraintValidationUtil {

    private static final String UNDERSCORE = "_";
    private static final String VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY = "%nValue provided in invalid format for %s property";
    private static final Logger logger = LoggerFactory.getLogger(PropertyValueConstraintValidationUtil.class);
    private static final String IGNORE_PROPERTY_VALUE_START_WITH = "{\"get_input\":";
    private Map<String, DataTypeDefinition> dataTypeDefinitionCache;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<String> errorMessages = new ArrayList<>();
    private StringBuilder completePropertyName;
    private String completeInputName;

    public Either<Boolean, ResponseFormat> validatePropertyConstraints(final Collection<? extends PropertyDefinition> propertyDefinitionList,
                                                                       final ApplicationDataTypeCache applicationDataTypeCache,
                                                                       final String model) {

        dataTypeDefinitionCache = applicationDataTypeCache.getAll(model).left().value();
        CollectionUtils.emptyIfNull(propertyDefinitionList).stream()
            .filter(this::isValuePresent)
            .forEach(this::evaluatePropertyTypeForConstraintValidation);
        if (CollectionUtils.isNotEmpty(errorMessages)) {
            final String errorMsgAsString = String.join(",", errorMessages);
            logger.debug("Properties with Invalid Data: {}", errorMsgAsString);
            return Either.right(getResponseFormatManager().getResponseFormat(ActionStatus.INVALID_PROPERTY_VALUES, errorMsgAsString));
        }
        return Either.left(Boolean.TRUE);
    }

    private boolean isValuePresent(PropertyDefinition propertyDefinition) {
        if (propertyDefinition instanceof ComponentInstanceInput) {
            return StringUtils.isNotEmpty(propertyDefinition.getValue());
        }
        if (propertyDefinition instanceof InputDefinition) {
            return StringUtils.isNotEmpty(propertyDefinition.getDefaultValue());
        }
        return StringUtils.isNotEmpty(propertyDefinition.getValue());
    }

    private void evaluatePropertyTypeForConstraintValidation(PropertyDefinition propertyDefinition) {
        if (propertyDefinition == null || propertyDefinition.getType() == null || !dataTypeDefinitionCache.containsKey(propertyDefinition.getType())) {
            errorMessages.add("\nUnsupported datatype found for property " + getCompletePropertyName(propertyDefinition));
            return;
        }
        completeInputName = "";
        completePropertyName = new StringBuilder();
        if (propertyDefinition instanceof ComponentInstanceInput) {
            setCompletePropertyName(propertyDefinition);
            evaluateComplexTypeProperties(propertyDefinition);
            return;
        }
        if (propertyDefinition instanceof InputDefinition) {
            completeInputName = propertyDefinition.getName();
            propertyDefinition = getPropertyDefinitionObjectFromInputs(propertyDefinition);
        }
        if (propertyDefinition != null) {
            List<PropertyConstraint> propertyConstraints =
                dataTypeDefinitionCache.get(propertyDefinition.getType()).safeGetConstraints();
            if (ToscaType.isPrimitiveType(propertyDefinition.getType())) {
                propertyDefinition.setConstraints(org.openecomp.sdc.be.dao.utils.CollectionUtils.merge(propertyDefinition.safeGetConstraints(),
                    propertyConstraints.isEmpty() ? new ArrayList<>() : propertyConstraints));
                evaluateConstraintsOnProperty(propertyDefinition);
            } else if (ToscaType.isCollectionType(propertyDefinition.getType())) {
                propertyDefinition.setConstraints(org.openecomp.sdc.be.dao.utils.CollectionUtils.merge(propertyDefinition.safeGetConstraints(),
                    propertyConstraints.isEmpty() ? new ArrayList<>() : propertyConstraints));
                evaluateConstraintsOnProperty(propertyDefinition);
                evaluateCollectionTypeProperties(propertyDefinition);
            } else {
                setCompletePropertyName(propertyDefinition);
                evaluateComplexTypeProperties(propertyDefinition);
            }
        }
    }

    private void setCompletePropertyName(PropertyDefinition propertyDefinition) {
        if (StringUtils.isNotBlank(propertyDefinition.getUniqueId())) {
            completePropertyName.append(propertyDefinition.getUniqueId().substring(propertyDefinition.getUniqueId().lastIndexOf('.') + 1));
        }
    }

    private void evaluateConstraintsOnProperty(PropertyDefinition propertyDefinition) {
        ToscaType toscaType = ToscaType.isValidType(propertyDefinition.getType());
        if (isPropertyNotMappedAsInput(propertyDefinition) && CollectionUtils.isNotEmpty(propertyDefinition.getConstraints())) {
            for (PropertyConstraint propertyConstraint : propertyDefinition.getConstraints()) {
                try {
                    propertyConstraint.initialize(toscaType);
                    propertyConstraint.validate(toscaType, propertyDefinition.getValue());
                } catch (ConstraintValueDoNotMatchPropertyTypeException | ConstraintViolationException exception) {
                    errorMessages.add("\n" + propertyConstraint.getErrorMessage(toscaType, exception, getCompletePropertyName(propertyDefinition)));
                }
            }
        } else if (isPropertyNotMappedAsInput(propertyDefinition) && ToscaType.isPrimitiveType(propertyDefinition.getType()) && !toscaType
            .isValidValue(propertyDefinition.getValue())) {
            errorMessages.add(String
                .format("\nUnsupported value provided for %s property supported value " + "type is %s.", getCompletePropertyName(propertyDefinition),
                    toscaType.getType()));
        }
    }

    private boolean isPropertyNotMappedAsInput(PropertyDefinition propertyDefinition) {
        return !propertyDefinition.getValue().startsWith(IGNORE_PROPERTY_VALUE_START_WITH);
    }

    private void checkAndEvaluatePrimitiveProperty(PropertyDefinition propertyDefinition, DataTypeDefinition dataTypeDefinition) {
        if (ToscaType.isPrimitiveType(dataTypeDefinition.getName()) && CollectionUtils.isNotEmpty(dataTypeDefinition.getConstraints())) {
            PropertyDefinition definition = new PropertyDefinition();
            definition.setValue(propertyDefinition.getValue());
            definition.setType(dataTypeDefinition.getName());
            definition.setConstraints(dataTypeDefinition.getConstraints());
            evaluateConstraintsOnProperty(propertyDefinition);
        }
    }

    private void evaluateComplexTypeProperties(PropertyDefinition propertyDefinition) {
        List<PropertyDefinition> propertyDefinitions = dataTypeDefinitionCache.get(propertyDefinition.getType()).getProperties();
        try {
            Map<String, Object> valueMap = MapUtils
                .emptyIfNull(ConstraintUtil.parseToCollection(propertyDefinition.getValue(), new TypeReference<>() {
                }));
            if (CollectionUtils.isEmpty(propertyDefinitions)) {
                checkAndEvaluatePrimitiveProperty(propertyDefinition, dataTypeDefinitionCache.get(propertyDefinition.getType()));
            } else {
                ListUtils.emptyIfNull(propertyDefinitions).forEach(prop -> evaluateRegularComplexType(propertyDefinition, prop, valueMap));
            }
        } catch (ConstraintValueDoNotMatchPropertyTypeException e) {
            logger.debug(e.getMessage(), e);
            errorMessages.add(String.format(VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY, getCompletePropertyName(propertyDefinition)));
        }
    }

    private void evaluateRegularComplexType(PropertyDefinition propertyDefinition, PropertyDefinition prop, Map<String, Object> valueMap) {
        try {
            completePropertyName = new StringBuilder();
            PropertyDefinition newPropertyWithValue = copyPropertyWithNewValue(prop, String.valueOf(valueMap.get(prop.getName())));
            if (valueMap.containsKey(prop.getName()) ) {
                if (ToscaType.isPrimitiveType(prop.getType())) {
                    if (isPropertyToEvaluate(newPropertyWithValue)) {
                        evaluateConstraintsOnProperty(newPropertyWithValue);
                    }
                } else if (ToscaType.isCollectionType(prop.getType())) {
                    newPropertyWithValue =
                        copyPropertyWithNewValue(prop,
                            objectMapper.writeValueAsString(valueMap.get(prop.getName())));
                    if (isPropertyToEvaluate(newPropertyWithValue)) {
                        evaluateCollectionTypeProperties(newPropertyWithValue);
                    }
                } else {
                    newPropertyWithValue =
                        copyPropertyWithNewValue(prop,
                            objectMapper.writeValueAsString(valueMap.get(prop.getName())));
                    if (isPropertyToEvaluate(newPropertyWithValue)) {
                        completePropertyName.append(UNDERSCORE);
                        completePropertyName.append(prop.getName());
                        evaluateComplexTypeProperties(newPropertyWithValue);
                    }
                }
            }
        } catch (IOException | ConstraintValueDoNotMatchPropertyTypeException e) {
            logger.error(e.getMessage(), e);
            errorMessages.add(String.format(VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY, getCompletePropertyName(propertyDefinition)));
        }
    }

    private Boolean isPropertyToEvaluate (PropertyDefinition propertyDefinition)
        throws ConstraintValueDoNotMatchPropertyTypeException {
        if (Boolean.FALSE.equals(propertyDefinition.isRequired())) {
            if (!ToscaType.isCollectionType(propertyDefinition.getType())) {
                return StringUtils.isNotEmpty(propertyDefinition.getValue()) &&
                    !"null".equals(propertyDefinition.getValue());
            }
            else {
                if (ToscaType.LIST == ToscaType.isValidType(propertyDefinition.getType())) {
                    Collection<Object> list = ConstraintUtil.parseToCollection(propertyDefinition.getValue(), new TypeReference<>() {});
                    return CollectionUtils.isNotEmpty(Collections.singleton(list)) &&
                        list.size() > 0 &&
                        !"null".equals(list);
                }
                Map<String, Object> valueMap = MapUtils
                    .emptyIfNull(ConstraintUtil.parseToCollection(propertyDefinition.getValue(), new TypeReference<>() {
                    }));
                return CollectionUtils.isNotEmpty(Collections.singleton(valueMap)) &&
                    valueMap.size() > 0 &&
                    !"null".equals(valueMap);
            }
        } else {
            return true;
        }
    }

    private void evaluateCollectionTypeProperties(PropertyDefinition propertyDefinition) {
        ToscaType toscaPropertyType = ToscaType.isValidType(propertyDefinition.getType());
        try {
            if (isPropertyToEvaluate(propertyDefinition)) {
                evaluateCollectionConstraints(propertyDefinition, toscaPropertyType);
            }
        } catch (ConstraintValueDoNotMatchPropertyTypeException e) {
            logger.error(e.getMessage(), e);
            errorMessages.add(String.format(VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY, getCompletePropertyName(propertyDefinition)));
        }
        if (ToscaType.LIST == toscaPropertyType) {
            evaluateListType(propertyDefinition);
        } else if (ToscaType.MAP == toscaPropertyType) {
            evaluateMapType(propertyDefinition);
        }
    }

    private void evaluateCollectionConstraints (PropertyDefinition propertyDefinition, ToscaType toscaPropertyType) {
        if (null == toscaPropertyType) {
            toscaPropertyType = ToscaType.isValidType(propertyDefinition.getType());
        }
        List<PropertyConstraint> constraintsList = propertyDefinition.getConstraints();
        if (CollectionUtils.isNotEmpty(constraintsList)) {
            for (PropertyConstraint propertyConstraint : constraintsList) {
                if (isACollectionConstraint(propertyConstraint)) {
                    try {
                        if (ToscaType.LIST == toscaPropertyType) {
                            Collection<Object> list = ConstraintUtil.parseToCollection(propertyDefinition.getValue(), new TypeReference<>() {});
                            propertyConstraint.validate(list);
                        } else if (ToscaType.MAP == toscaPropertyType) {
                            final Map<String, Object> map = ConstraintUtil.parseToCollection(propertyDefinition.getValue(), new TypeReference<>() {});
                            propertyConstraint.validate(map);
                        }
                    } catch (ConstraintValueDoNotMatchPropertyTypeException | ConstraintViolationException exception) {
                        errorMessages.add("\n" + propertyConstraint.getErrorMessage(toscaPropertyType, exception,
                            getCompletePropertyName(propertyDefinition)));
                    }
                }
            }
        }
    }

    private boolean isACollectionConstraint(PropertyConstraint constraint) {
        if (constraint instanceof MaxLengthConstraint){
            return true;
        }
        if (constraint instanceof MinLengthConstraint) {
            return true;
        }
        return constraint instanceof LengthConstraint;
    }

    private void evaluateListType(PropertyDefinition propertyDefinition) {
        try {
            if (propertyDefinition.getSchemaType() == null) {
                propertyDefinition.setSchema(createStringSchema());
            }
            Collection<Object> list = ConstraintUtil.parseToCollection(propertyDefinition.getValue(), new TypeReference<>() {});
            evaluateCollectionType(propertyDefinition, list);
        } catch (ConstraintValueDoNotMatchPropertyTypeException e) {
            logger.debug(e.getMessage(), e);
            errorMessages.add(String.format(VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY, getCompletePropertyName(propertyDefinition)));
        }
    }

    private SchemaDefinition createStringSchema() {
        final SchemaDefinition schemaDefinition = new SchemaDefinition();
        final PropertyDefinition schemaStringProperty = new PropertyDefinition();
        schemaStringProperty.setType(ToscaType.STRING.getType());
        schemaDefinition.setProperty(schemaStringProperty);
        return schemaDefinition;
    }

    private void evaluateMapType(final PropertyDefinition propertyDefinition) {
        try {
            if (propertyDefinition.getSchemaType() == null) {
                propertyDefinition.setSchema(createStringSchema());
            }
            final Map<String, Object> map = ConstraintUtil.parseToCollection(propertyDefinition.getValue(), new TypeReference<>() {});
            evaluateCollectionType(propertyDefinition, map.values());
        } catch (ConstraintValueDoNotMatchPropertyTypeException e) {
            logger.debug(e.getMessage(), e);
            errorMessages.add(String.format(VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY, getCompletePropertyName(propertyDefinition)));
        }
    }

    private void evaluateCollectionPrimitiveSchemaType(final PropertyDefinition propertyDefinition,
                                                       final String schemaType) throws JsonProcessingException {
        if (propertyDefinition.getSchema() != null && propertyDefinition.getSchema().getProperty() instanceof PropertyDefinition) {
            propertyDefinition.setConstraints(((PropertyDefinition) propertyDefinition.getSchema().getProperty()).getConstraints());
            propertyDefinition.setValue(objectMapper.readValue(propertyDefinition.getValue(), String.class));
            propertyDefinition.setType(schemaType);
            evaluateConstraintsOnProperty(propertyDefinition);
        }
    }

    private void evaluateCollectionType(final PropertyDefinition propertyDefinition, final Collection<Object> valueList) {
        final String schemaType = propertyDefinition.getSchemaType();
        for (final Object value : valueList) {
            try {
                final PropertyDefinition propertyCopyWithNewValue = copyPropertyWithNewValue(propertyDefinition, objectMapper.writeValueAsString(value));
                if (ToscaType.isPrimitiveType(schemaType)) {
                    evaluateCollectionPrimitiveSchemaType(propertyCopyWithNewValue, schemaType);
                } else if (ToscaType.isCollectionType(schemaType)) {
                    propertyCopyWithNewValue.setType(schemaType);
                    propertyCopyWithNewValue.setSchemaType(propertyDefinition.getSchemaProperty().getSchemaType());
                    evaluateCollectionTypeProperties(propertyCopyWithNewValue);
                } else {
                    propertyCopyWithNewValue.setType(schemaType);
                    completePropertyName.append(UNDERSCORE);
                    completePropertyName.append(propertyCopyWithNewValue.getName());
                    evaluateComplexTypeProperties(propertyCopyWithNewValue);
                }
            } catch (final Exception e) {
                logger.debug(e.getMessage(), e);
                errorMessages.add(String.format(VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY, getCompletePropertyName(propertyDefinition)));
            }
        }
    }

    private String getCompletePropertyName(final PropertyDefinition propertyDefinition) {
        if (StringUtils.isNotBlank(completeInputName)) {
            return completeInputName;
        }

        final String propertyName = propertyDefinition == null ? "" : propertyDefinition.getUniqueId();
        if (StringUtils.isNotBlank(completePropertyName)) {
            return completePropertyName + UNDERSCORE + propertyName;
        }

        return propertyName;
    }

    private PropertyDefinition copyPropertyWithNewValue(final PropertyDefinition propertyToCopy, final String value) {
        final var propertyDefinition = new PropertyDefinition(propertyToCopy);
        propertyDefinition.setValue(value);
        return propertyDefinition;
    }

    private PropertyDefinition getPropertyDefinitionObjectFromInputs(PropertyDefinition property) {
        InputDefinition inputDefinition = (InputDefinition) property;
        PropertyDefinition propertyDefinition = null;
        if (CollectionUtils.isEmpty(inputDefinition.getProperties()) || ToscaType.isPrimitiveType(inputDefinition.getProperties().get(0).getType())) {
            propertyDefinition = new PropertyDefinition();
            propertyDefinition.setType(inputDefinition.getType());
            propertyDefinition.setValue(inputDefinition.getDefaultValue());
            propertyDefinition.setName(inputDefinition.getName());
        } else if (Objects.nonNull(inputDefinition.getInputPath())) {
            propertyDefinition = evaluateComplexTypeInputs(inputDefinition);
        }
        return propertyDefinition;
    }

    private PropertyDefinition evaluateComplexTypeInputs(InputDefinition inputDefinition) {
        Map<String, Object> inputMap = new HashMap<>();
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        String[] inputPathArr = inputDefinition.getInputPath().split("#");
        if (inputPathArr.length > 1) {
            inputPathArr = ArrayUtils.remove(inputPathArr, 0);
        }
        try {
            Map<String, Object> presentMap = inputMap;
            for (int i = 0; i < inputPathArr.length; i++) {
                if (i == inputPathArr.length - 1) {
                    presentMap.computeIfAbsent(inputPathArr[i], k -> inputDefinition.getDefaultValue());
                } else {
                    presentMap.computeIfAbsent(inputPathArr[i], k -> new HashMap<String, Object>());
                    presentMap = (Map<String, Object>) presentMap.get(inputPathArr[i]);
                }
            }
            if (CollectionUtils.isNotEmpty(inputDefinition.getProperties())) {
                propertyDefinition.setType(inputDefinition.getProperties().get(0).getType());
            }
            propertyDefinition.setName(inputDefinition.getName());
            propertyDefinition.setValue(objectMapper.writeValueAsString(inputMap));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            errorMessages.add(String.format(VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY, inputDefinition.getName()));
        }
        return propertyDefinition;
    }

    protected ResponseFormatManager getResponseFormatManager() {
        return ResponseFormatManager.getInstance();
    }
}
