/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterConstraintDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaConcatFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionParameter;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionType;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaStringParameter;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.datatypes.enums.PropertyFilterTargetType;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.openecomp.sdc.exception.InvalidArgumentException;
import org.yaml.snakeyaml.Yaml;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertyFilterConstraintDataDefinitionHelper {

    public static PropertyFilterConstraintDataDefinition convertLegacyConstraint(final String constraint) {
        final var propertyFilterConstraint = new PropertyFilterConstraintDataDefinition();
        final Map<String, Object> constraintYaml = new Yaml().load(constraint);
        final String propertyName = constraintYaml.keySet().iterator().next();
        propertyFilterConstraint.setPropertyName(propertyName);
        final Map<String, Object> operatorYaml = (Map<String, Object>) constraintYaml.get(propertyName);
        final String operator = operatorYaml.keySet().iterator().next();
        propertyFilterConstraint.setOperator(ConstraintType.findByType(operator).orElse(null));
        Object valueYaml = operatorYaml.get(operator);
        final Optional<ToscaFunction> toscaFunction = readToscaFunctionFromLegacyConstraintValue(valueYaml);
        if (toscaFunction.isPresent()) {
            propertyFilterConstraint.setValue(toscaFunction.get());
        } else {
            propertyFilterConstraint.setValue(valueYaml);
        }
        propertyFilterConstraint.setValueType(detectValueType(valueYaml));
        propertyFilterConstraint.setTargetType(PropertyFilterTargetType.PROPERTY);
        return propertyFilterConstraint;
    }

    public static Optional<ToscaFunction> readToscaFunctionFromLegacyConstraintValue(final Object filterValue) {
        if (!(filterValue instanceof Map)) {
            return Optional.empty();
        }
        final Map<?, ?> filterValueAsMap = (Map<?, ?>) filterValue;
        final Set<?> keys = filterValueAsMap.keySet();
        if (keys.size() != 1) {
            return Optional.empty();
        }
        final Object toscaFunctionTypeObject = keys.iterator().next();
        if (!(toscaFunctionTypeObject instanceof String)) {
            return Optional.empty();
        }
        final ToscaFunctionType toscaFunctionType = ToscaFunctionType.findType((String) toscaFunctionTypeObject).orElse(null);
        if (toscaFunctionType == null) {
            return Optional.empty();
        }
        switch (toscaFunctionType) {
            case GET_INPUT:
                return readLegacyGetInputConstraintValue(filterValueAsMap, toscaFunctionTypeObject);
            case GET_ATTRIBUTE:
            case GET_PROPERTY:
                return readLegacyGetPropertyConstraintValue(filterValueAsMap, toscaFunctionTypeObject, toscaFunctionType);
            case CONCAT:
                return readLegacyConcatConstraintValue(filterValueAsMap, toscaFunctionTypeObject);
            default:
                return Optional.empty();
        }
    }

    private static Optional<ToscaFunction> readLegacyConcatConstraintValue(Map<?, ?> filterValueAsMap, Object toscaFunctionType) {
        final List<Object> concatValue;
        try {
            concatValue = (List<Object>) filterValueAsMap.get(toscaFunctionType);
        } catch (final Exception ignored) {
            return Optional.empty();
        }
        if (concatValue.isEmpty()) {
            return Optional.empty();
        }
        final var toscaConcatFunction = new ToscaConcatFunction();
        for (Object parameter : concatValue) {
            if (parameter instanceof String) {
                final ToscaStringParameter toscaStringParameter = new ToscaStringParameter();
                toscaStringParameter.setValue((String) parameter);
                toscaConcatFunction.addParameter(toscaStringParameter);
            } else {
                readToscaFunctionFromLegacyConstraintValue(parameter)
                    .ifPresent(toscaFunction -> toscaConcatFunction.addParameter((ToscaFunctionParameter) toscaFunction));
            }
        }
        return Optional.of(toscaConcatFunction);
    }

    private static Optional<ToscaFunction> readLegacyGetPropertyConstraintValue(Map<?, ?> filterValueAsMap, Object toscaFunctionType,
                                                                                ToscaFunctionType toscaFunctionType1) {
        final var toscaGetFunction = new ToscaGetFunctionDataDefinition();
        toscaGetFunction.setFunctionType(ToscaGetFunctionType.fromToscaFunctionType(toscaFunctionType1)
            .orElseThrow(() -> new InvalidArgumentException("Could not convert a ToscaFunctionType to a ToscaGetFunctionType"))
        );
        final List<String> getFunctionValue;
        try {
            getFunctionValue = (List<String>) filterValueAsMap.get(toscaFunctionType);
        } catch (final Exception ignored) {
            return Optional.of(toscaGetFunction);
        }
        if (!getFunctionValue.isEmpty()) {
            final Optional<PropertySource> propertySource = PropertySource.findType(getFunctionValue.get(0));
            if (propertySource.isPresent()) {
                toscaGetFunction.setPropertySource(propertySource.get());
            } else {
                toscaGetFunction.setPropertySource(PropertySource.INSTANCE);
                toscaGetFunction.setSourceName(getFunctionValue.get(0));
            }
            toscaGetFunction.setPropertyName(getFunctionValue.get(1));
            toscaGetFunction.setPropertyPathFromSource(getFunctionValue.subList(1, getFunctionValue.size()));
        }
        return Optional.of(toscaGetFunction);
    }

    private static Optional<ToscaFunction> readLegacyGetInputConstraintValue(Map<?, ?> filterValueAsMap, Object toscaFunctionType) {
        final var toscaGetFunction = new ToscaGetFunctionDataDefinition();
        toscaGetFunction.setFunctionType(ToscaGetFunctionType.GET_INPUT);
        final List<String> getFunctionValue;
        final Object valueAsObject = filterValueAsMap.get(toscaFunctionType);
        if (valueAsObject instanceof String) {
            getFunctionValue = List.of((String) valueAsObject);
        } else if (valueAsObject instanceof List) {
            try {
                getFunctionValue = (List<String>) filterValueAsMap.get(toscaFunctionType);
            } catch (final Exception ignored) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }

        toscaGetFunction.setPropertyPathFromSource(getFunctionValue);
        if (!getFunctionValue.isEmpty()) {
            toscaGetFunction.setPropertyName(toscaGetFunction.getPropertyPathFromSource().get(0));
        }
        toscaGetFunction.setPropertySource(PropertySource.SELF);
        return Optional.of(toscaGetFunction);
    }

    private static FilterValueType detectValueType(final Object value) {
        if (value instanceof Map) {
            final Map<?, ?> valueAsMap = (Map<?, ?>) value;
            if (valueAsMap.containsKey(ToscaFunctionType.CONCAT.getName())) {
                return FilterValueType.CONCAT;
            }
            if (valueAsMap.containsKey(ToscaFunctionType.GET_ATTRIBUTE.getName())) {
                return FilterValueType.GET_ATTRIBUTE;
            }
            if (valueAsMap.containsKey(ToscaFunctionType.GET_PROPERTY.getName())) {
                return FilterValueType.GET_PROPERTY;
            }
            if (valueAsMap.containsKey(ToscaFunctionType.GET_INPUT.getName())) {
                return FilterValueType.GET_INPUT;
            }
        }

        return FilterValueType.STATIC;
    }

    public static Optional<FilterValueType> convertFromToscaFunctionType(final ToscaFunctionType toscaFunctionType) {
        return FilterValueType.findByName(toscaFunctionType.getName());
    }

}
