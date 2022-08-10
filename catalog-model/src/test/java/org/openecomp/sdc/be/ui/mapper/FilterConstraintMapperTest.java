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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterConstraintDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaConcatFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionType;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.datatypes.enums.PropertyFilterTargetType;
import org.openecomp.sdc.be.model.dto.FilterConstraintDto;
import org.openecomp.sdc.be.ui.model.UIConstraint;

class FilterConstraintMapperTest {

    private final FilterConstraintMapper filterConstraintMapper = new FilterConstraintMapper();

    @Test
    void mapFromUIConstraintTest() {
        //given
        final var uIConstraint = new UIConstraint();
        final FilterValueType filterValueType = FilterValueType.STATIC;
        uIConstraint.setSourceType(filterValueType.getName());
        final String capabilityName = "aCapability";
        uIConstraint.setCapabilityName(capabilityName);
        final String propertyName = "aProperty";
        uIConstraint.setServicePropertyName(propertyName);
        final ConstraintType operator = ConstraintType.GREATER_OR_EQUAL;
        uIConstraint.setConstraintOperator(operator.getType());
        final ToscaFunctionType expectedValueToscaFunctionType = ToscaFunctionType.GET_INPUT;
        uIConstraint.setValue(Map.of("type", expectedValueToscaFunctionType.getName()));
        //when
        final FilterConstraintDto filterConstraintDto = filterConstraintMapper.mapFrom(uIConstraint);
        //then
        assertEquals(PropertyFilterTargetType.CAPABILITY, filterConstraintDto.getTargetType());
        assertEquals(propertyName, filterConstraintDto.getPropertyName());
        assertEquals(capabilityName, filterConstraintDto.getCapabilityName());
        assertEquals(filterValueType, filterConstraintDto.getValueType());
        assertTrue(filterConstraintDto.getValue() instanceof ToscaGetFunctionDataDefinition);
        assertEquals(expectedValueToscaFunctionType, ((ToscaGetFunctionDataDefinition) filterConstraintDto.getValue()).getType());
        assertEquals(operator, filterConstraintDto.getOperator());
        //when
        final UIConstraint actualUiConstraint = filterConstraintMapper.mapToUiConstraint(filterConstraintDto);
        //then
        assertEquals(propertyName, actualUiConstraint.getServicePropertyName());
        assertEquals(capabilityName, actualUiConstraint.getCapabilityName());
        assertEquals(filterValueType.getName(), actualUiConstraint.getSourceType());
        assertTrue(actualUiConstraint.getValue() instanceof ToscaGetFunctionDataDefinition);
        assertEquals(expectedValueToscaFunctionType, ((ToscaGetFunctionDataDefinition) actualUiConstraint.getValue()).getType());
        assertEquals(operator.getType(), actualUiConstraint.getConstraintOperator());
        assertNull(actualUiConstraint.getSourceName());
    }

    @Test
    void mapFromPropertyFilterConstraintDataDefinitionTest() {
        //given
        final var propertyFilterConstraintDataDefinition = new PropertyFilterConstraintDataDefinition();
        propertyFilterConstraintDataDefinition.setTargetType(PropertyFilterTargetType.CAPABILITY);
        final String capabilityName = "aCapability";
        propertyFilterConstraintDataDefinition.setCapabilityName(capabilityName);
        final String propertyName = "aProperty";
        propertyFilterConstraintDataDefinition.setPropertyName(propertyName);
        final ConstraintType operator = ConstraintType.GREATER_OR_EQUAL;
        propertyFilterConstraintDataDefinition.setOperator(operator);
        final FilterValueType filterValueType = FilterValueType.STATIC;
        propertyFilterConstraintDataDefinition.setValueType(filterValueType);
        final String value = "aStaticValue";
        propertyFilterConstraintDataDefinition.setValue(value);
        //when
        final FilterConstraintDto filterConstraintDto = filterConstraintMapper.mapFrom(propertyFilterConstraintDataDefinition);
        //then
        assertEquals(PropertyFilterTargetType.CAPABILITY, filterConstraintDto.getTargetType());
        assertEquals(propertyName, filterConstraintDto.getPropertyName());
        assertEquals(capabilityName, filterConstraintDto.getCapabilityName());
        assertEquals(filterValueType, filterConstraintDto.getValueType());
        assertEquals(value, filterConstraintDto.getValue());
        assertEquals(operator, filterConstraintDto.getOperator());
        //when
        final PropertyFilterConstraintDataDefinition actualPropertyFilterConstraint =
            filterConstraintMapper.mapTo(filterConstraintDto);
        assertEquals(PropertyFilterTargetType.CAPABILITY, actualPropertyFilterConstraint.getTargetType());
        assertEquals(propertyName, actualPropertyFilterConstraint.getPropertyName());
        assertEquals(capabilityName, actualPropertyFilterConstraint.getCapabilityName());
        assertEquals(filterValueType, actualPropertyFilterConstraint.getValueType());
        assertEquals(value, actualPropertyFilterConstraint.getValue());
        assertEquals(operator, actualPropertyFilterConstraint.getOperator());
    }

    @Test
    void parseValueToToscaFunctionTest() {
        //given
        final ToscaConcatFunction expectedValue = new ToscaConcatFunction();
        //when
        Optional<ToscaFunction> actualToscaFunction = filterConstraintMapper.parseValueToToscaFunction(expectedValue);
        //then
        assertTrue(actualToscaFunction.isPresent());
        assertEquals(expectedValue, actualToscaFunction.get());
        //when
        actualToscaFunction = filterConstraintMapper.parseValueToToscaFunction("not a tosca function");
        //then
        assertTrue(actualToscaFunction.isEmpty());
        //given
        final Map<String, Object> value = Map.of("type", ToscaFunctionType.CONCAT.getName());
        //when
        actualToscaFunction = filterConstraintMapper.parseValueToToscaFunction(value);
        //then
        assertTrue(actualToscaFunction.isPresent());
        assertTrue(actualToscaFunction.get() instanceof ToscaConcatFunction);
        //when
        actualToscaFunction = filterConstraintMapper.parseValueToToscaFunction(Map.of("type", 1));
        //then
        assertTrue(actualToscaFunction.isEmpty());
    }
}