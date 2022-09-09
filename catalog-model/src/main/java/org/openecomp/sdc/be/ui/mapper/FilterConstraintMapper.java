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

package org.openecomp.sdc.be.ui.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterConstraintDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionType;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.datatypes.enums.PropertyFilterTargetType;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.openecomp.sdc.be.model.dto.FilterConstraintDto;
import org.openecomp.sdc.be.ui.model.UIConstraint;

public class FilterConstraintMapper {

    public FilterConstraintDto mapFrom(final UIConstraint uiConstraint) {
        final var filterConstraint = new FilterConstraintDto();
        ConstraintType.findByType(uiConstraint.getConstraintOperator()).ifPresent(filterConstraint::setOperator);
        filterConstraint.setCapabilityName(uiConstraint.getCapabilityName());
        filterConstraint.setPropertyName(uiConstraint.getServicePropertyName());
        filterConstraint.setTargetType(StringUtils.isEmpty(uiConstraint.getCapabilityName()) ? PropertyFilterTargetType.PROPERTY : PropertyFilterTargetType.CAPABILITY);
        FilterValueType.findByName(uiConstraint.getSourceType()).ifPresent(filterConstraint::setValueType);
        filterConstraint.setValue(mapValueFrom(uiConstraint));
        return filterConstraint;
    }

    private Object mapValueFrom(final UIConstraint uiConstraint) {
        if (FilterValueType.GET_INPUT.getLegacyName().equals(uiConstraint.getSourceType())) {
            final ToscaGetFunctionDataDefinition toscaGetFunctionDataDefinition = new ToscaGetFunctionDataDefinition();
            toscaGetFunctionDataDefinition.setPropertySource(PropertySource.SELF);
            final String value = (String) uiConstraint.getValue();
            toscaGetFunctionDataDefinition.setPropertyName(value);
            toscaGetFunctionDataDefinition.setFunctionType(ToscaGetFunctionType.GET_INPUT);
            toscaGetFunctionDataDefinition.setPropertyPathFromSource(List.of(value));
            return toscaGetFunctionDataDefinition;
        }

        if (FilterValueType.GET_PROPERTY.getLegacyName().equals(uiConstraint.getSourceType())) {
            final ToscaGetFunctionDataDefinition toscaGetFunctionDataDefinition = new ToscaGetFunctionDataDefinition();
            toscaGetFunctionDataDefinition.setPropertySource(PropertySource.SELF);
            final String value = (String) uiConstraint.getValue();
            toscaGetFunctionDataDefinition.setPropertyName(value);
            toscaGetFunctionDataDefinition.setFunctionType(ToscaGetFunctionType.GET_PROPERTY);
            toscaGetFunctionDataDefinition.setPropertyPathFromSource(List.of(value));
            return toscaGetFunctionDataDefinition;
        }

        return parseValueFromUiConstraint(uiConstraint.getValue());
    }

    public FilterConstraintDto mapFrom(final PropertyFilterConstraintDataDefinition propertyFilterConstraint) {
        var filterConstraintDto = new FilterConstraintDto();
        filterConstraintDto.setTargetType(propertyFilterConstraint.getTargetType());
        filterConstraintDto.setPropertyName(propertyFilterConstraint.getPropertyName());
        filterConstraintDto.setCapabilityName(propertyFilterConstraint.getCapabilityName());
        filterConstraintDto.setOperator(propertyFilterConstraint.getOperator());
        filterConstraintDto.setValueType(propertyFilterConstraint.getValueType());
        filterConstraintDto.setValue(propertyFilterConstraint.getValue());
        return filterConstraintDto;
    }

    public PropertyFilterConstraintDataDefinition mapTo(final FilterConstraintDto filterConstraintDto) {
        var propertyFilterConstraint = new PropertyFilterConstraintDataDefinition();
        propertyFilterConstraint.setTargetType(filterConstraintDto.getTargetType());
        propertyFilterConstraint.setPropertyName(filterConstraintDto.getPropertyName());
        propertyFilterConstraint.setCapabilityName(filterConstraintDto.getCapabilityName());
        propertyFilterConstraint.setOperator(filterConstraintDto.getOperator());
        propertyFilterConstraint.setValueType(filterConstraintDto.getValueType());
        propertyFilterConstraint.setValue(filterConstraintDto.getValue());
        return propertyFilterConstraint;
    }

    public UIConstraint mapToUiConstraint(final FilterConstraintDto filterConstraintDto) {
        final var uiConstraint = new UIConstraint();
        uiConstraint.setConstraintOperator(filterConstraintDto.getOperator().getType());
        uiConstraint.setValue(filterConstraintDto.getValue());
        uiConstraint.setCapabilityName(filterConstraintDto.getCapabilityName());
        uiConstraint.setServicePropertyName(filterConstraintDto.getPropertyName());
        uiConstraint.setSourceType(filterConstraintDto.getValueType().getName());
        return uiConstraint;
    }

    private Object parseValueFromUiConstraint(final Object value) {
        if (!(value instanceof Map || value instanceof String)) {
            return value;
        }
        final Map<?, ?> valueAsMap;
        if (value instanceof String) {
            try {
                valueAsMap = new Gson().fromJson((String) value, Map.class);
            } catch (final Exception ignored) {
                return value;
            }
        } else {
            valueAsMap = (Map<?, ?>) value;
        }

        final Optional<ToscaFunction> toscaFunction = parseValueToToscaFunction(valueAsMap);
        if (toscaFunction.isPresent()) {
            return toscaFunction.get();
        }

        return valueAsMap;
    }

    public Optional<ToscaFunction> parseValueToToscaFunction(final Object value) {
        if (value instanceof ToscaFunction) {
            return Optional.of((ToscaFunction) value);
        }
        return readToscaFunctionType(value).map(toscaFunctionType -> new ObjectMapper().convertValue(value, ToscaFunction.class));
    }

    private Optional<ToscaFunctionType> readToscaFunctionType(final Object toscaFunction) {
        if (!(toscaFunction instanceof Map)) {
            return Optional.empty();
        }
        final Object type = ((Map<?, ?>) toscaFunction).get("type");
        if (type instanceof String) {
            return ToscaFunctionType.findType((String) type);
        }
        return Optional.empty();
    }

}
