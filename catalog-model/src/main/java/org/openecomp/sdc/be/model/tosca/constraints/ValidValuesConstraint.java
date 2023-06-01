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
package org.openecomp.sdc.be.model.tosca.constraints;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintFunctionalException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.PropertyConstraintException;

@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ValidValuesConstraint extends AbstractPropertyConstraint {

    private static final String PROPERTY_TYPE_IS = "> property type is <";
    @Getter
    @Setter
    @NotNull
    @EqualsAndHashCode.Include
    private List<Object> validValues;
    private Set<Object> validValuesTyped;

    public ValidValuesConstraint(List<Object> validValues) {
        this.validValues = validValues;
    }
    
    @Override
    public void initialize(ToscaType propertyType, SchemaDefinition schema) throws ConstraintValueDoNotMatchPropertyTypeException {
        ToscaType toscaType = getValuesType(propertyType, schema);
        validValuesTyped = Sets.newHashSet();
        if (validValues == null) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "validValues constraint has invalid value <> property type is <" + propertyType.toString() + ">");
        }
        for (Object value : validValues) {
            if (!toscaType.isValidValue(String.valueOf(value))) {
                throw new ConstraintValueDoNotMatchPropertyTypeException(
                    "validValues constraint has invalid value <" + value + PROPERTY_TYPE_IS + propertyType.toString() + ">");
            } else {
                validValuesTyped.add(toscaType.convert(String.valueOf(value)));
            }
        }
    }

    public void validateType(String propertyType, SchemaDefinition schema) throws ConstraintValueDoNotMatchPropertyTypeException {
        ToscaType toscaType = getValuesType(ToscaType.getToscaType(propertyType), schema);
        if (toscaType == null) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "validValues constraint has invalid values <" + validValues.toString() + PROPERTY_TYPE_IS + propertyType + ">");
        }
        if (validValues == null) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                "validValues constraint has invalid value <> property type is <" + propertyType + ">");
        }
        for (Object value : validValues) {
            if (!toscaType.isValidValue(String.valueOf(value))) {
                throw new ConstraintValueDoNotMatchPropertyTypeException(
                    "validValues constraint has invalid value <" + value + PROPERTY_TYPE_IS + propertyType + ">");
            }
        }
    }
    
    private ToscaType getValuesType(ToscaType propertyType, SchemaDefinition schema) {
        return ToscaType.isCollectionType(propertyType.getType()) ? ToscaType.getToscaType(schema.getProperty().getType()) : propertyType;
    }

    @Override
    public void validateValueOnUpdate(PropertyConstraint newConstraint) throws PropertyConstraintException {
        if (newConstraint.getConstraintType() == getConstraintType()) {
            if (!((ValidValuesConstraint) newConstraint).getValidValues().containsAll(validValues)) {
                throw new PropertyConstraintException("Deletion of exists value is not permitted", null, null,
                    ActionStatus.CANNOT_DELETE_VALID_VALUES, getConstraintType().name(),
                    validValues.stream().filter(v -> !((ValidValuesConstraint) newConstraint).getValidValues().contains(v)).collect(toList())
                        .toString());
            }
        }
    }
    
    @Override
    public void validate(PropertyDefinition propertyDefinition) throws ConstraintViolationException {
        ToscaType toscaType = ToscaType.isValidType(propertyDefinition.getType());
        try {
            Collection<Object> valuesToValidate;
            if (ToscaType.LIST == toscaType) {
                if (propertyDefinition.getValue() != null) {
                    valuesToValidate = ConstraintUtil.parseToCollection(propertyDefinition.getValue(), new TypeReference<>() {});
                } else {
                    valuesToValidate = ConstraintUtil.parseToCollection(propertyDefinition.getDefaultValue(), new TypeReference<>() {});
                }
            } else if (ToscaType.MAP == toscaType) {
                Map<String, Object> map;

                if (propertyDefinition.getValue() != null) {
                    map = ConstraintUtil.parseToCollection(propertyDefinition.getValue(), new TypeReference<>() {});
                } else {
                    map = ConstraintUtil.parseToCollection(propertyDefinition.getDefaultValue(), new TypeReference<>() {});
                }
                valuesToValidate = map.values();
            } else {
                if (propertyDefinition.getValue() != null) {
                    valuesToValidate = Collections.singleton(propertyDefinition.getValue());
                } else {
                    valuesToValidate = Collections.singleton(propertyDefinition.getDefaultValue());
                }
            }
            if (propertyDefinition.getSubPropertyToscaFunctions() != null) {
                propertyDefinition.getSubPropertyToscaFunctions().forEach(subPropToscaFunction -> {
                    valuesToValidate.remove(subPropToscaFunction.getToscaFunction().getJsonObjectValue());
                });
            }
            ToscaType valuesType = getValuesType(toscaType, propertyDefinition.getSchema());
            for (final Object value: valuesToValidate) {
                if (value != null) {
                    validate(valuesType, value.toString());
                }
            }
        } catch (ConstraintValueDoNotMatchPropertyTypeException exception) {
            throw new ConstraintViolationException("Value cannot be parsed to a list", exception);
        }
    }

    @Override
    public void validate(Object propertyValue) throws ConstraintViolationException {
        if (propertyValue == null) {
            throw new ConstraintViolationException("Value to validate is null");
        }
        if (!validValuesTyped.contains(propertyValue)) {
            throw new ConstraintViolationException("The value is not in the list of valid values");
        }
    }

    @Override
    public ConstraintType getConstraintType() {
        return ConstraintType.VALID_VALUES;
    }

    @Override
    public String getErrorMessage(ToscaType toscaType, ConstraintFunctionalException e, String propertyName) {
        return getErrorMessage(toscaType, e, propertyName, "'%s' value must be one of the following: [%s]", String.join(",", String.valueOf(validValues)));
    }

    public boolean validateValueType(String propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        ToscaType toscaType = ToscaType.getToscaType(propertyType);
        if (toscaType == null) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                    "validValues constraint has invalid values <" + validValues + "> property type is <" + propertyType + ">");
        }
        if (validValues == null) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                    "validValues constraint has invalid value <> property type is <" + propertyType + ">");
        }
        for (Object value : validValues) {
            if (!toscaType.isValueTypeValid(value)) {
                return false;
            }
        }
        return true;
    }

    public void changeConstraintValueTypeTo(String propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
        ToscaType toscaType = ToscaType.getToscaType(propertyType);
        try {
            validValues.replaceAll(obj -> toscaType.convert(String.valueOf(obj)));
        } catch (Exception e) {
            throw new ConstraintValueDoNotMatchPropertyTypeException(
                    "validValues constraint has invalid values <" + validValues + "> property type is <" + propertyType + ">");
        }
    }
}
