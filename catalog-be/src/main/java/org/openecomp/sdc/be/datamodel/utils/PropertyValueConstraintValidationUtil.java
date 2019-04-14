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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fj.data.Either;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintUtil;
import org.openecomp.sdc.be.model.tosca.constraints.ValidValuesConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyValueConstraintValidationUtil {

    private static final String UNDERSCORE = "_";
    private static final String VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY =
            "%nValue provided in invalid format for %s property";
    private Map<String, DataTypeDefinition> dataTypeDefinitionCache;
    private static final Logger logger = LoggerFactory.getLogger(PropertyValueConstraintValidationUtil.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private List<String> errorMessages = new ArrayList<>();
    private StringBuilder completePropertyName;
    private String completeInputName;
    private static final String IGNORE_PROPERTY_VALUE_START_WITH = "{\"get_input\":";

    public static PropertyValueConstraintValidationUtil getInstance() {
        return new PropertyValueConstraintValidationUtil();
    }

    public Either<Boolean, ResponseFormat> validatePropertyConstraints(
            Collection<? extends PropertyDefinition> propertyDefinitionList,
            ApplicationDataTypeCache applicationDataTypeCache) {
        ResponseFormatManager responseFormatManager = getResponseFormatManager();
        dataTypeDefinitionCache = applicationDataTypeCache.getAll().left().value();
        CollectionUtils.emptyIfNull(propertyDefinitionList).stream()
                .filter(this::isValuePresent)
                .forEach(this::evaluatePropertyTypeForConstraintValidation);

        if (CollectionUtils.isNotEmpty(errorMessages)) {
            logger.error("Properties with Invalid Data:", errorMessages);
            ResponseFormat inputResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .INVALID_PROPERTY_VALUES, String.join(",", errorMessages));
            return Either.right(inputResponse);
        }

        return Either.left(Boolean.TRUE);
    }

    private boolean isValuePresent(PropertyDefinition propertyDefinition) {
        if (propertyDefinition instanceof InputDefinition) {
            return StringUtils.isNotEmpty(propertyDefinition.getDefaultValue());
        }

        return StringUtils.isNotEmpty(propertyDefinition.getValue());
    }

    private void evaluatePropertyTypeForConstraintValidation(PropertyDefinition propertyDefinition) {
        if (Objects.nonNull(propertyDefinition.getType())
                && dataTypeDefinitionCache.containsKey(propertyDefinition.getType())) {

            completeInputName = "";
            completePropertyName = new StringBuilder();
            if (propertyDefinition instanceof InputDefinition) {
                completeInputName = propertyDefinition.getName();
                propertyDefinition = getPropertyDefinitionObjectFromInputs(propertyDefinition);
            }

            if (Objects.nonNull(propertyDefinition)) {
				if (ToscaType.isPrimitiveType(propertyDefinition.getType())) {
					propertyDefinition.setConstraints(
							org.openecomp.sdc.be.dao.utils.CollectionUtils.merge(propertyDefinition.getConstraints(),
									dataTypeDefinitionCache.get(propertyDefinition.getType()).getConstraints()));
					evaluateConstraintsOnProperty(propertyDefinition);
				} else if (ToscaType.isCollectionType(propertyDefinition.getType())) {
					propertyDefinition.setConstraints(
							org.openecomp.sdc.be.dao.utils.CollectionUtils.merge(propertyDefinition.getConstraints(),
									dataTypeDefinitionCache.get(propertyDefinition.getType()).getConstraints()));
					evaluateConstraintsOnProperty(propertyDefinition);
					evaluateCollectionTypeProperties(propertyDefinition);
				} else {
					setCompletePropertyName(propertyDefinition);
					evaluateComplexTypeProperties(propertyDefinition);
				}
			}
        } else {
            errorMessages.add("\nUnsupported datatype found for property " + getCompletePropertyName(propertyDefinition));
        }
    }

    private void setCompletePropertyName(PropertyDefinition propertyDefinition) {
        if(StringUtils.isNotBlank(propertyDefinition.getUniqueId())) {
            completePropertyName.append(
                    propertyDefinition.getUniqueId().substring(propertyDefinition.getUniqueId().lastIndexOf('.') + 1));
        }
    }

    private void evaluateConstraintsOnProperty(PropertyDefinition propertyDefinition) {
        ToscaType toscaType = ToscaType.isValidType(propertyDefinition.getType());
        if (isPropertyNotMappedAsInput(propertyDefinition)
                && CollectionUtils.isNotEmpty(propertyDefinition.getConstraints())
                && isValidValueConstraintPresent(propertyDefinition.getConstraints())) {
            for (PropertyConstraint propertyConstraint : propertyDefinition.getConstraints()) {
                try {
                    propertyConstraint.initialize(toscaType);
                    propertyConstraint.validate(toscaType, propertyDefinition.getValue());
                } catch (ConstraintValueDoNotMatchPropertyTypeException | ConstraintViolationException exception) {
                    errorMessages.add("\n" + propertyConstraint.getErrorMessage(
                            toscaType, exception, getCompletePropertyName(propertyDefinition)));
                }
            }
        } else if (isPropertyNotMappedAsInput(propertyDefinition)
                && ToscaType.isPrimitiveType(propertyDefinition.getType())
                && !toscaType.isValidValue(propertyDefinition.getValue())) {
            errorMessages.add(String.format("\nUnsupported value provided for %s property supported value "
                    + "type is %s.", getCompletePropertyName(propertyDefinition), toscaType.getType()));
        }
    }

    private boolean isPropertyNotMappedAsInput(PropertyDefinition propertyDefinition) {
        return !propertyDefinition.getValue().startsWith(IGNORE_PROPERTY_VALUE_START_WITH);
    }

    private void checkAndEvaluatePrimitiveProperty(PropertyDefinition propertyDefinition,
												   DataTypeDefinition dataTypeDefinition) {
        if (ToscaType.isPrimitiveType(dataTypeDefinition.getName())
                && CollectionUtils.isNotEmpty(dataTypeDefinition.getConstraints())) {

            PropertyDefinition definition = new PropertyDefinition();
            definition.setValue(propertyDefinition.getValue());
            definition.setType(dataTypeDefinition.getName());
            definition.setConstraints(dataTypeDefinition.getConstraints());

            evaluateConstraintsOnProperty(propertyDefinition);
        }
    }

    private void evaluateComplexTypeProperties(PropertyDefinition propertyDefinition) {
        List<PropertyDefinition> propertyDefinitions =
                dataTypeDefinitionCache.get(propertyDefinition.getType()).getProperties();
		try {
			Map<String, Object> valueMap =
					MapUtils.emptyIfNull(ConstraintUtil.parseToCollection(propertyDefinition.getValue(),
							new TypeReference<Map<String, Object>>() {}));

			if (CollectionUtils.isEmpty(propertyDefinitions)) {
				checkAndEvaluatePrimitiveProperty(propertyDefinition,
						dataTypeDefinitionCache.get(propertyDefinition.getType()));
			} else {
				ListUtils.emptyIfNull(propertyDefinitions)
						.forEach(prop -> evaluateRegularComplexType(propertyDefinition, prop, valueMap));
			}
		} catch (ConstraintValueDoNotMatchPropertyTypeException e) {
			logger.debug(e.getMessage(), e);
			errorMessages.add(String.format(VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY,
					getCompletePropertyName(propertyDefinition)));
		}
    }

    private void evaluateRegularComplexType(PropertyDefinition propertyDefinition,
											PropertyDefinition prop,
											Map<String, Object> valueMap) {
        try {
            if (valueMap.containsKey(prop.getName())) {
                if (ToscaType.isPrimitiveType(prop.getType())) {
                    evaluateConstraintsOnProperty(createPropertyDefinition(prop,
                            String.valueOf(valueMap.get(prop.getName()))));
                } else if (ToscaType.isCollectionType(prop.getType())) {

                    evaluateCollectionTypeProperties(createPropertyDefinition(prop,
                            objectMapper.writeValueAsString(valueMap.get(prop.getName()))));
                } else {
                    completePropertyName.append(UNDERSCORE);
                    completePropertyName.append(prop.getName());
                    evaluateComplexTypeProperties(
                            createPropertyDefinition(prop, objectMapper.writeValueAsString(
                                    valueMap.get(prop.getName()))));
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            errorMessages.add(String.format(VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY,
                    getCompletePropertyName(propertyDefinition)));
        }
    }

    private void evaluateCollectionTypeProperties(PropertyDefinition propertyDefinition) {
        ToscaType toscaPropertyType = ToscaType.isValidType(propertyDefinition.getType());
        if (ToscaType.LIST == toscaPropertyType) {
            evaluateListType(propertyDefinition);
        } else if (ToscaType.MAP == toscaPropertyType) {
            evaluateMapType(propertyDefinition);
        }
    }

    private void evaluateListType(PropertyDefinition propertyDefinition) {
        try {
            String schemaType = propertyDefinition.getSchemaType();
            List list = ConstraintUtil.parseToCollection(propertyDefinition.getValue(),
                    new TypeReference<List<Object>>() {});
            evaluateCollectionType(propertyDefinition, list, schemaType);
        } catch (ConstraintValueDoNotMatchPropertyTypeException e) {
            logger.debug(e.getMessage(), e);
            errorMessages.add(String.format(VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY,
                    getCompletePropertyName(propertyDefinition)));
        }
    }

    private void evaluateMapType(PropertyDefinition propertyDefinition) {
        try {
            String schemaType = propertyDefinition.getSchemaType();
            Map map = ConstraintUtil.parseToCollection(propertyDefinition.getValue(),
                    new TypeReference<Map<String, Object>>() {
            });
            evaluateCollectionType(propertyDefinition, map.values(), schemaType);
        } catch (ConstraintValueDoNotMatchPropertyTypeException e) {
            logger.debug(e.getMessage(), e);
            errorMessages.add(String.format(VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY,
                    getCompletePropertyName(propertyDefinition)));
        }
    }

    private void evaluateCollectionPrimitiveSchemaType(PropertyDefinition propertyDefinition,
													   Object value,
													   String schemaType) {
        if (Objects.nonNull(propertyDefinition.getSchema())
                && propertyDefinition.getSchema().getProperty() instanceof PropertyDefinition) {
            propertyDefinition.setConstraints(((PropertyDefinition) propertyDefinition.getSchema()
                    .getProperty()).getConstraints());
            propertyDefinition.setValue(String.valueOf(value));
            propertyDefinition.setType(schemaType);
            evaluateConstraintsOnProperty(propertyDefinition);
        }
    }
    private void evaluateCollectionType(PropertyDefinition propertyDefinition,
										Collection valueList,
										String schemaType) {
        for (Object value : valueList) {
            try {
                if (ToscaType.isPrimitiveType(schemaType)) {
                    evaluateCollectionPrimitiveSchemaType(propertyDefinition, value, schemaType);
                } else if (ToscaType.isCollectionType(schemaType)) {
                    propertyDefinition.setValue(objectMapper.writeValueAsString(value));
                    propertyDefinition.setType(schemaType);
                    evaluateCollectionTypeProperties(propertyDefinition);
                } else {
                    propertyDefinition.setValue(objectMapper.writeValueAsString(value));
                    propertyDefinition.setType(schemaType);
                    completePropertyName.append(UNDERSCORE);
                    completePropertyName.append(propertyDefinition.getName());
                    evaluateComplexTypeProperties(propertyDefinition);
                }
            } catch (IOException e) {
                logger.debug(e.getMessage(), e);
                errorMessages.add(String.format(VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY,
                        getCompletePropertyName(propertyDefinition)));
            }
        }
    }

    private String getCompletePropertyName(PropertyDefinition propertyDefinition) {
        return StringUtils.isNotBlank(completeInputName) ? completeInputName :
                StringUtils.isBlank(completePropertyName) ?
                        propertyDefinition.getName() : completePropertyName + UNDERSCORE + propertyDefinition.getName();
    }

    private PropertyDefinition createPropertyDefinition(PropertyDefinition prop, String value) {
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName(prop.getName());
        propertyDefinition.setValue(value);
        propertyDefinition.setType(prop.getType());
        propertyDefinition.setConstraints(prop.getConstraints());
        propertyDefinition.setSchema(prop.getSchema());

        return propertyDefinition;
    }

    private boolean isValidValueConstraintPresent(List<PropertyConstraint> propertyConstraints) {
        return propertyConstraints.stream()
                .anyMatch(propertyConstraint -> propertyConstraint instanceof ValidValuesConstraint);
    }

    private PropertyDefinition getPropertyDefinitionObjectFromInputs(
            PropertyDefinition property) {
        InputDefinition inputDefinition = (InputDefinition) property;
        PropertyDefinition propertyDefinition = null;

        if (CollectionUtils.isEmpty(inputDefinition.getProperties())
                || ToscaType.isPrimitiveType(inputDefinition.getProperties().get(0).getType())) {
        	propertyDefinition  = new PropertyDefinition();
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
            for (int i = 0; i < inputPathArr.length ; i++) {
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
            errorMessages.add(String.format(VALUE_PROVIDED_IN_INVALID_FORMAT_FOR_PROPERTY,
                    inputDefinition.getName()));
        }

        return propertyDefinition;
    }

    protected ResponseFormatManager getResponseFormatManager() {
        return ResponseFormatManager.getInstance();
    }
}
