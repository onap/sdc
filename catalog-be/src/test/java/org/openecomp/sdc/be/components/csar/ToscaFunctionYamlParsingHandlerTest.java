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

package org.openecomp.sdc.be.components.csar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.ToscaConcatFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionType;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaStringParameter;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;

class ToscaFunctionYamlParsingHandlerTest {

    final ToscaFunctionYamlParsingHandler toscaFunctionYamlParsingHandler = new ToscaFunctionYamlParsingHandler();

    @Test
    void buildToscaFunctionBasedOnPropertyValue_NotAToscaFunctionTest() {
        assertEquals(Optional.empty(), toscaFunctionYamlParsingHandler.buildToscaFunctionBasedOnPropertyValue(null));
    }

    @Test
    void buildToscaFunctionBasedOnPropertyValue_GetInputTest() {
        final List<String> getInputParameters = List.of("input", "subProperty");
        final Map<String, Object> getInput = Map.of(ToscaFunctionType.GET_INPUT.getName(), getInputParameters);
        final Optional<ToscaFunction> actualToscaFunctionOpt = toscaFunctionYamlParsingHandler.buildToscaFunctionBasedOnPropertyValue(getInput);
        assertTrue(actualToscaFunctionOpt.isPresent());
        final ToscaFunction actualToscaFunction = actualToscaFunctionOpt.get();
        assertGetInput(actualToscaFunction, getInputParameters);
    }

    @Test
    void buildToscaFunctionBasedOnPropertyValue_GetPropertyTest() {
        final List<String> getPropertyValue = List.of(PropertySource.SELF.getName(), "aProperty", "aSubProperty");
        final Map<String, Object> getProperty = Map.of(ToscaFunctionType.GET_PROPERTY.getName(), getPropertyValue);

        final Optional<ToscaFunction> actualToscaFunctionOpt = toscaFunctionYamlParsingHandler.buildToscaFunctionBasedOnPropertyValue(getProperty);
        assertTrue(actualToscaFunctionOpt.isPresent());
        final ToscaFunction actualToscaFunction = actualToscaFunctionOpt.get();
        assertEquals(ToscaFunctionType.GET_PROPERTY, actualToscaFunction.getType());
        assertTrue(actualToscaFunction instanceof ToscaGetFunctionDataDefinition);
        final ToscaGetFunctionDataDefinition toscaGetFunction = (ToscaGetFunctionDataDefinition) actualToscaFunction;
        assertEquals(ToscaGetFunctionType.GET_PROPERTY, toscaGetFunction.getFunctionType());
        assertEquals("aSubProperty", toscaGetFunction.getPropertyName());
        assertEquals(PropertySource.SELF, toscaGetFunction.getPropertySource());
        assertEquals(getPropertyValue.subList(1, getPropertyValue.size()), toscaGetFunction.getPropertyPathFromSource());
        assertNull(toscaGetFunction.getPropertyUniqueId());
        assertNull(toscaGetFunction.getSourceName());
    }

    @Test
    void buildToscaFunctionBasedOnPropertyValue_GetAttributeTest() {
        final List<String> getPropertyValue = List.of(PropertySource.INSTANCE.getName(), "anAttribute", "aSubAttribute");
        final Map<String, Object> getProperty = Map.of(ToscaFunctionType.GET_ATTRIBUTE.getName(), getPropertyValue);

        final Optional<ToscaFunction> actualToscaFunctionOpt = toscaFunctionYamlParsingHandler.buildToscaFunctionBasedOnPropertyValue(getProperty);
        assertTrue(actualToscaFunctionOpt.isPresent());
        final ToscaFunction actualToscaFunction = actualToscaFunctionOpt.get();
        assertEquals(ToscaFunctionType.GET_ATTRIBUTE, actualToscaFunction.getType());
        assertTrue(actualToscaFunction instanceof ToscaGetFunctionDataDefinition);
        final ToscaGetFunctionDataDefinition toscaGetFunction = (ToscaGetFunctionDataDefinition) actualToscaFunction;
        assertEquals(ToscaGetFunctionType.GET_ATTRIBUTE, toscaGetFunction.getFunctionType());
        assertEquals("aSubAttribute", toscaGetFunction.getPropertyName());
        assertEquals(PropertySource.INSTANCE, toscaGetFunction.getPropertySource());
        assertEquals(getPropertyValue.subList(1, getPropertyValue.size()), toscaGetFunction.getPropertyPathFromSource());
        assertEquals(getPropertyValue.get(0), toscaGetFunction.getSourceName());
        assertNull(toscaGetFunction.getPropertyUniqueId());
    }

    @Test
    void buildToscaFunctionBasedOnPropertyValue_ConcatTest() {
        final List<Object> concatValue = List.of("string1", "-", Map.of(ToscaFunctionType.GET_INPUT.getName(), "inputName"));
        final Map<String, Object> concatValueMap = Map.of(ToscaFunctionType.CONCAT.getName(), concatValue);

        final Optional<ToscaFunction> actualToscaFunctionOpt = toscaFunctionYamlParsingHandler.buildToscaFunctionBasedOnPropertyValue(concatValueMap);
        assertTrue(actualToscaFunctionOpt.isPresent());
        final ToscaFunction actualToscaFunction = actualToscaFunctionOpt.get();
        assertEquals(ToscaFunctionType.CONCAT, actualToscaFunction.getType());
        assertTrue(actualToscaFunction instanceof ToscaConcatFunction);
        final ToscaConcatFunction toscaConcatFunction = (ToscaConcatFunction) actualToscaFunction;
        assertEquals(3, toscaConcatFunction.getParameters().size());
        assertTrue(toscaConcatFunction.getParameters().get(0) instanceof ToscaStringParameter);
        final ToscaStringParameter parameter1 = (ToscaStringParameter) toscaConcatFunction.getParameters().get(0);
        assertEquals("string1", parameter1.getValue());
        assertTrue(toscaConcatFunction.getParameters().get(1) instanceof ToscaStringParameter);
        final ToscaStringParameter parameter2 = (ToscaStringParameter) toscaConcatFunction.getParameters().get(1);
        assertEquals("-", parameter2.getValue());
        assertTrue(toscaConcatFunction.getParameters().get(2) instanceof ToscaGetFunctionDataDefinition);
        final ToscaGetFunctionDataDefinition getFunction = (ToscaGetFunctionDataDefinition) toscaConcatFunction.getParameters().get(2);
        assertGetInput(getFunction, List.of("inputName"));
    }


    @Test
    void isPropertyValueToscaFunctionTest() {
        assertFalse(toscaFunctionYamlParsingHandler.isPropertyValueToscaFunction(ToscaFunctionType.GET_INPUT.getName()));
        assertFalse(toscaFunctionYamlParsingHandler.isPropertyValueToscaFunction(new HashMap<>()));
        assertFalse(toscaFunctionYamlParsingHandler.isPropertyValueToscaFunction(
                Map.of(ToscaFunctionType.GET_ATTRIBUTE.getName(), "", ToscaFunctionType.GET_INPUT.getName(), "")
            )
        );
        assertTrue(toscaFunctionYamlParsingHandler.isPropertyValueToscaFunction(Map.of(ToscaFunctionType.GET_ATTRIBUTE.getName(), "")));
        assertTrue(toscaFunctionYamlParsingHandler.isPropertyValueToscaFunction(Map.of(ToscaFunctionType.GET_INPUT.getName(), "")));
        assertTrue(toscaFunctionYamlParsingHandler.isPropertyValueToscaFunction(Map.of(ToscaFunctionType.GET_PROPERTY.getName(), "")));
        assertTrue(toscaFunctionYamlParsingHandler.isPropertyValueToscaFunction(Map.of(ToscaFunctionType.CONCAT.getName(), "")));
        assertFalse(toscaFunctionYamlParsingHandler.isPropertyValueToscaFunction(Map.of(ToscaFunctionType.YAML.getName(), "")));
        assertFalse(toscaFunctionYamlParsingHandler.isPropertyValueToscaFunction(Map.of(ToscaFunctionType.STRING.getName(), "")));
    }

    private static void assertGetInput(final ToscaFunction actualGetInputFunction, final List<String> expectedGetInputParameters) {
        assertEquals(ToscaFunctionType.GET_INPUT, actualGetInputFunction.getType());
        assertTrue(actualGetInputFunction instanceof ToscaGetFunctionDataDefinition);
        final ToscaGetFunctionDataDefinition toscaGetFunction = (ToscaGetFunctionDataDefinition) actualGetInputFunction;
        assertEquals(ToscaGetFunctionType.GET_INPUT, toscaGetFunction.getFunctionType());
        assertEquals(expectedGetInputParameters.get(expectedGetInputParameters.size() - 1), toscaGetFunction.getPropertyName());
        assertEquals(PropertySource.SELF, toscaGetFunction.getPropertySource());
        assertEquals(expectedGetInputParameters, toscaGetFunction.getPropertyPathFromSource());
        assertNull(toscaGetFunction.getPropertyUniqueId());
        assertNull(toscaGetFunction.getSourceName());
    }
}