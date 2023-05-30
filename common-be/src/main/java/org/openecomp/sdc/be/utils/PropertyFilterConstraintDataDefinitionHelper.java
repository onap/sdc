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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
        final Object valueYaml = operatorYaml.get(operator);
        final Optional<ToscaFunction> toscaFunction = createToscaFunctionFromLegacyConstraintValue(valueYaml);
        if (toscaFunction.isPresent()) {
            propertyFilterConstraint.setValue(toscaFunction.get());
        } else {
            propertyFilterConstraint.setValue(valueYaml);
        }
        propertyFilterConstraint.setValueType(detectValueType(valueYaml));
        propertyFilterConstraint.setTargetType(PropertyFilterTargetType.PROPERTY);
        return propertyFilterConstraint;
    }

    public static Optional<ToscaFunction> createToscaFunctionFromLegacyConstraintValue(final Object filterValue) {
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

    public static Optional<FilterValueType> convertFromToscaFunctionType(final ToscaFunctionType toscaFunctionType) {
        return FilterValueType.findByName(toscaFunctionType.getName());
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
                createToscaFunctionFromLegacyConstraintValue(parameter)
                    .ifPresent(toscaFunction -> toscaConcatFunction.addParameter((ToscaFunctionParameter) toscaFunction));
            }
        }
        return Optional.of(toscaConcatFunction);
    }

    private static Optional<ToscaFunction> readLegacyGetPropertyConstraintValue(Map<?, ?> filterValueAsMap, Object toscaFunctionType,
                                                                                ToscaFunctionType toscaFunctionType1) {
        final ToscaGetFunctionDataDefinition toscaGetFunction = new ToscaGetFunctionDataDefinition();
        toscaGetFunction.setFunctionType(
            toscaFunctionType.toString().equalsIgnoreCase(ToscaFunctionType.GET_PROPERTY.getName()) ? ToscaGetFunctionType.GET_PROPERTY : ToscaGetFunctionType.GET_ATTRIBUTE
        );
        final Object functionValueObj = null != filterValueAsMap.get(toscaFunctionType1) ?
            filterValueAsMap.get(toscaFunctionType1) : filterValueAsMap.get(toscaFunctionType);
        if (!(functionValueObj instanceof List)) {
            return Optional.empty();
        }
        final List<String> functionParameters;
        try {
            functionParameters = ((List<Object>) functionValueObj).stream()
                .map(object -> Objects.toString(object, null))
                .collect(Collectors.toList());
        } catch (final ClassCastException ignored) {
            return Optional.empty();
        }
        if (functionParameters.size() < 2) {
            return Optional.empty();
        }
        final String propertySourceType = functionParameters.get(0);
        final PropertySource propertySource = PropertySource.findType(propertySourceType).orElse(null);
        if (propertySource == PropertySource.SELF) {
            toscaGetFunction.setPropertySource(propertySource);
        } else {
            toscaGetFunction.setPropertySource(PropertySource.INSTANCE);
            toscaGetFunction.setSourceName(propertySourceType);
        }
        List<String> propertySourceIndex = functionParameters.subList(1, functionParameters.size());
        List<String> propertySourcePath = new ArrayList<>();
        propertySourcePath.add((String)propertySourceIndex.get(0));
        if (propertySourceIndex.size() > 1 ) {
            List<Object> indexParsedList = new ArrayList<Object>();
            List<String> indexObjectList = propertySourceIndex.subList(1,propertySourceIndex.size());
            boolean loopFlag = true;
            for (String indexValue : indexObjectList) {
                if (!indexValue.equalsIgnoreCase("INDEX") && !StringUtils.isNumeric(indexValue) && loopFlag) {
                    propertySourcePath.add(indexValue);
                } else {
                    loopFlag = false;
                    if (StringUtils.isNumeric(indexValue)) {
                        indexParsedList.add(Integer.parseInt(indexValue));
                    } else {
                        indexParsedList.add(indexValue);
                    }
                }
            }
            toscaGetFunction.setToscaIndexList(indexParsedList);
        }
        toscaGetFunction.setPropertyPathFromSource(propertySourcePath);
        final String propertyName = toscaGetFunction.getPropertyPathFromSource().get(toscaGetFunction.getPropertyPathFromSource().size() - 1);
        toscaGetFunction.setPropertyName(propertyName);
        return Optional.of(toscaGetFunction);
    }

    private static Optional<ToscaFunction> readLegacyGetInputConstraintValue(Map<?, ?> filterValueAsMap, Object toscaFunctionType) {
        final ToscaGetFunctionDataDefinition toscaGetFunction = new ToscaGetFunctionDataDefinition();
        toscaGetFunction.setFunctionType(ToscaGetFunctionType.GET_INPUT);
        toscaGetFunction.setPropertySource(PropertySource.SELF);
        final Object functionValueObj = filterValueAsMap.get(toscaFunctionType);
        if (!(functionValueObj instanceof List) && !(functionValueObj instanceof String)) {
            return Optional.empty();
        }
        if (functionValueObj instanceof String) {
            toscaGetFunction.setPropertyPathFromSource(List.of((String) functionValueObj));
        } else {
            final List<String> functionParameters;
            try {
                functionParameters = ((List<Object>) functionValueObj).stream()
                    .map(object -> Objects.toString(object, null))
                    .collect(Collectors.toList());
            } catch (final ClassCastException ignored) {
                return Optional.empty();
            }
            List<String> propertySourcePath = new ArrayList<>();
            propertySourcePath.add((String)functionParameters.get(0));
            if (functionParameters.size() > 1 ) {
                List<Object> indexParsedList = new ArrayList<Object>();
                List<String> indexObjectList = functionParameters.subList(1,functionParameters.size());
                boolean loopFlag = true;
                for (String indexValue : indexObjectList) {
                    if (!indexValue.equalsIgnoreCase("INDEX") && !StringUtils.isNumeric(indexValue) && loopFlag) {
                        propertySourcePath.add(indexValue);
                    } else {
                        loopFlag = false;
                        if (StringUtils.isNumeric(indexValue)) {
                            indexParsedList.add(Integer.parseInt(indexValue));
                        } else {
                            indexParsedList.add(indexValue);
                        }
                    }
                }
                toscaGetFunction.setToscaIndexList(indexParsedList);
            }
            toscaGetFunction.setPropertyPathFromSource(propertySourcePath);
        }
        final String propertyName = toscaGetFunction.getPropertyPathFromSource().get(toscaGetFunction.getPropertyPathFromSource().size() - 1);
        toscaGetFunction.setPropertyName(propertyName);
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

}
