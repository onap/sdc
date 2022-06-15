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

package org.openecomp.sdc.be.datatypes.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;

class ToscaGetFunctionDataDefinitionTest {

    @Test
    void isSubPropertyTest() {
        final var toscaGetFunction = new ToscaGetFunctionDataDefinition();
        assertFalse(toscaGetFunction.isSubProperty());
        toscaGetFunction.setPropertyPathFromSource(List.of("property1"));
        assertFalse(toscaGetFunction.isSubProperty());
        toscaGetFunction.setPropertyPathFromSource(List.of("property1", "subProperty1"));
        assertTrue(toscaGetFunction.isSubProperty());
    }

    @Test
    void generateGetInputSinglePropertyValueTest() {
        //given
        final String propertyName = "property";
        final var toscaGetFunction = createGetFunction(ToscaGetFunctionType.GET_INPUT, null, List.of(propertyName), null);
        //when
        final String actualValue = toscaGetFunction.generatePropertyValue();
        //then
        final Map<?, ?> getInputJsonAsMap = convertJsonStringToMap(actualValue);
        assertTrue(getInputJsonAsMap.containsKey(ToscaGetFunctionType.GET_INPUT.getFunctionName()));
        final Object value = getInputJsonAsMap.get(ToscaGetFunctionType.GET_INPUT.getFunctionName());
        assertTrue(value instanceof String);
        assertEquals(value, propertyName);
    }

    @Test
    void generateGetInputMultiplePropertyValueTest() {
        //given
        final var toscaGetFunction = createGetFunction(
            ToscaGetFunctionType.GET_INPUT,
            null,
            List.of("property", "subProperty", "subSubProperty"),
            null
        );
        //when
        final String actualValue = toscaGetFunction.generatePropertyValue();
        //then
        final Map<?, ?> getInputJsonAsMap = convertJsonStringToMap(actualValue);
        assertTrue(getInputJsonAsMap.containsKey(ToscaGetFunctionType.GET_INPUT.getFunctionName()));
        final Object value = getInputJsonAsMap.get(ToscaGetFunctionType.GET_INPUT.getFunctionName());
        assertTrue(value instanceof List);
        assertEquals(value, toscaGetFunction.getPropertyPathFromSource());
    }

    @ParameterizedTest
    @EnumSource(value =  ToscaGetFunctionType.class, names = {"GET_ATTRIBUTE", "GET_PROPERTY"})
    void generateValueForGetFunctionWithSelfAsSourceTest(final ToscaGetFunctionType toscaFunction) {
        //given
        final var toscaGetFunction = createGetFunction(toscaFunction, PropertySource.SELF, List.of("property"), null);
        //when
        String actualValue = toscaGetFunction.generatePropertyValue();
        //then
        Map<?, ?> getInputJsonAsMap = convertJsonStringToMap(actualValue);
        assertTrue(getInputJsonAsMap.containsKey(toscaFunction.getFunctionName()));
        Object actualGetPropertyValue = getInputJsonAsMap.get(toscaFunction.getFunctionName());
        List<String> expectedGetPropertyValue = Stream.concat(
                Stream.of(PropertySource.SELF.getName()),
                toscaGetFunction.getPropertyPathFromSource().stream())
            .collect(Collectors.toList());
        assertEquals(expectedGetPropertyValue, actualGetPropertyValue);

        //given a sub property path
        toscaGetFunction.setPropertyPathFromSource(List.of("property", "subProperty", "subSubProperty"));
        //when
        actualValue = toscaGetFunction.generatePropertyValue();
        //then
        getInputJsonAsMap = convertJsonStringToMap(actualValue);
        assertTrue(getInputJsonAsMap.containsKey(toscaFunction.getFunctionName()));
        actualGetPropertyValue = getInputJsonAsMap.get(toscaFunction.getFunctionName());
        expectedGetPropertyValue = Stream.concat(
                Stream.of(PropertySource.SELF.getName()),
                toscaGetFunction.getPropertyPathFromSource().stream())
            .collect(Collectors.toList());
        assertEquals(expectedGetPropertyValue, actualGetPropertyValue);
    }

    @ParameterizedTest
    @EnumSource(value =  ToscaGetFunctionType.class, names = {"GET_ATTRIBUTE", "GET_PROPERTY"})
    void generateValueForGetFunctionWithInstanceAsSourceTest(final ToscaGetFunctionType toscaFunction) {
        //given
        final var toscaGetFunction = createGetFunction(toscaFunction, PropertySource.INSTANCE, List.of("property"), "sourceName");
        //when
        String actualValue = toscaGetFunction.generatePropertyValue();
        //then
        Map<?, ?> getInputJsonAsMap = convertJsonStringToMap(actualValue);
        assertTrue(getInputJsonAsMap.containsKey(toscaFunction.getFunctionName()));
        Object actualGetPropertyValue = getInputJsonAsMap.get(toscaFunction.getFunctionName());
        List<String> expectedGetPropertyValue = Stream.concat(
                Stream.of(toscaGetFunction.getSourceName()),
                toscaGetFunction.getPropertyPathFromSource().stream())
            .collect(Collectors.toList());
        assertEquals(expectedGetPropertyValue, actualGetPropertyValue);

        //given a sub property path
        toscaGetFunction.setPropertyPathFromSource(List.of("property", "subProperty", "subSubProperty"));
        //when
        actualValue = toscaGetFunction.generatePropertyValue();
        //then
        getInputJsonAsMap = convertJsonStringToMap(actualValue);
        assertTrue(getInputJsonAsMap.containsKey(toscaFunction.getFunctionName()));
        actualGetPropertyValue = getInputJsonAsMap.get(toscaFunction.getFunctionName());
        expectedGetPropertyValue = Stream.concat(
                Stream.of(toscaGetFunction.getSourceName()),
                toscaGetFunction.getPropertyPathFromSource().stream())
            .collect(Collectors.toList());
        assertEquals(expectedGetPropertyValue, actualGetPropertyValue);
    }

    @Test
    void generateValueFunctionTypeIsRequiredTest() {
        final var toscaGetFunction = createGetFunction(null, null, List.of("property"), null);
        toscaGetFunction.setPropertyPathFromSource(List.of("property"));
        final IllegalStateException actualException = assertThrows(IllegalStateException.class, toscaGetFunction::generatePropertyValue);
        assertEquals("functionType is required in order to generate the get function value", actualException.getMessage());
    }

    @Test
    void generateValuePropertyPathIsRequiredTest() {
        final var toscaGetFunction = createGetFunction(ToscaGetFunctionType.GET_INPUT, null, null, null);
        final IllegalStateException actualException = assertThrows(IllegalStateException.class, toscaGetFunction::generatePropertyValue);
        assertEquals("propertyPathFromSource is required in order to generate the get function value", actualException.getMessage());
    }

    @Test
    void generateValuePropertySourceIsRequiredForGetPropertyTest() {
        final var toscaGetFunction = createGetFunction(
            ToscaGetFunctionType.GET_PROPERTY,
            null,
            List.of("property"),
            null);
        final IllegalStateException actualException = assertThrows(IllegalStateException.class, toscaGetFunction::generatePropertyValue);
        assertEquals("propertySource is required in order to generate the get_property value", actualException.getMessage());
    }

    @Test
    void generateValueSourceNameIsRequiredForGetInstancePropertyTest() {
        final ToscaGetFunctionDataDefinition toscaGetFunction = createGetFunction(
            ToscaGetFunctionType.GET_PROPERTY,
            PropertySource.INSTANCE,
            List.of("property"),
            null);
        final IllegalStateException actualException = assertThrows(IllegalStateException.class, toscaGetFunction::generatePropertyValue);

        assertEquals("sourceName is required in order to generate the get_property from INSTANCE value", actualException.getMessage());
    }

    private ToscaGetFunctionDataDefinition createGetFunction(final ToscaGetFunctionType toscaGetFunctionType,
                                                             final PropertySource propertySource,
                                                             final List<String> propertyPath, String sourceName) {
        final var toscaGetFunction = new ToscaGetFunctionDataDefinition();
        toscaGetFunction.setFunctionType(toscaGetFunctionType);
        toscaGetFunction.setPropertySource(propertySource);
        toscaGetFunction.setPropertyPathFromSource(propertyPath);
        toscaGetFunction.setSourceName(sourceName);
        return toscaGetFunction;
    }

    private Map<?, ?> convertJsonStringToMap(final String actualValue) {
        final Gson gson = new Gson();
        return gson.fromJson(actualValue, Map.class);
    }
}