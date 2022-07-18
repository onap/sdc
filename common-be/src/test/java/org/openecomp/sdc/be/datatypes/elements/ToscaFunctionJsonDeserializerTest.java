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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.yaml.snakeyaml.Yaml;

class ToscaFunctionJsonDeserializerTest {

    private static final Path TEST_RESOURCES_PATH = Path.of("src/test/resources/toscaFunctionJsonDeserializer");

    @Test
    void testGetInputToscaFunction() throws IOException {
        //given
        final String toscaGetInputFunction = Files.readString(TEST_RESOURCES_PATH.resolve("getInput.json"));
        //when
        final ToscaFunction toscaFunction = parseToscaFunction(toscaGetInputFunction);
        //then
        assertTrue(toscaFunction instanceof ToscaGetFunctionDataDefinition);
        final ToscaGetFunctionDataDefinition toscaGetFunction = (ToscaGetFunctionDataDefinition) toscaFunction;
        assertEquals(ToscaFunctionType.GET_INPUT, toscaGetFunction.getType());
        assertEquals(ToscaGetFunctionType.GET_INPUT, toscaGetFunction.getFunctionType());
        assertEquals("e57525d7-2115-4934-9ba4-9cebfa22bad2.nf_naming", toscaGetFunction.getPropertyUniqueId());
        assertEquals(PropertySource.SELF, toscaGetFunction.getPropertySource());
        assertEquals("instance_name", toscaGetFunction.getPropertyName());
        assertEquals("ciResVFc26a0b30ec20", toscaGetFunction.getSourceName());
        assertEquals("aee643c9-6c8e-4a24-af7a-a9aff5c072c0", toscaGetFunction.getSourceUniqueId());
        assertEquals(List.of("nf_naming", "instance_name"), toscaGetFunction.getPropertyPathFromSource());
    }

    @Test
    void testGetInputToscaFunctionLegacyConversion() throws IOException {
        //given
        final String toscaGetInputFunction = Files.readString(TEST_RESOURCES_PATH.resolve("getInputLegacy.json"));
        //when
        final ToscaFunction toscaFunction = parseToscaFunction(toscaGetInputFunction);
        //then
        assertTrue(toscaFunction instanceof ToscaGetFunctionDataDefinition);
        final ToscaGetFunctionDataDefinition toscaGetFunction = (ToscaGetFunctionDataDefinition) toscaFunction;
        assertEquals(ToscaFunctionType.GET_INPUT, toscaGetFunction.getType());
        assertEquals(ToscaGetFunctionType.GET_INPUT, toscaGetFunction.getFunctionType());
    }

    @Test
    void testNoFunctionTypeProvided() throws IOException {
        //given
        final String toscaGetInputFunction = Files.readString(TEST_RESOURCES_PATH.resolve("getFunctionMissingType.json"));
        //when/then
        final ValueInstantiationException actualException =
            assertThrows(ValueInstantiationException.class, () -> parseToscaFunction(toscaGetInputFunction));
        assertTrue(actualException.getMessage().contains("Attribute type not provided"));
    }

    @Test
    void testConcatToscaFunction() throws IOException {
        //given
        final String toscaConcatFunction = Files.readString(TEST_RESOURCES_PATH.resolve("concatFunction.json"));
        //when
        final ToscaFunction toscaFunction = parseToscaFunction(toscaConcatFunction);
        //then
        assertTrue(toscaFunction instanceof ToscaConcatFunction);
        final Object yamlObject = new Yaml().load(toscaFunction.getValue());
        assertTrue(yamlObject instanceof Map);
        final Map<String, Object> yamlMap = (Map<String, Object>) yamlObject;
        final Object concatFunctionObj = yamlMap.get(ToscaFunctionType.CONCAT.getName());
        assertNotNull(concatFunctionObj);
        assertTrue(concatFunctionObj instanceof List);
        final List<Object> concatFunctionParameters = (List<Object>) concatFunctionObj;
        assertEquals(3, concatFunctionParameters.size(), "Expecting three parameters");
        assertTrue(concatFunctionParameters.get(0) instanceof Map);
        final Map<String, Object> parameter1Map = (Map<String, Object>) concatFunctionParameters.get(0);
        assertNotNull(parameter1Map.get(ToscaFunctionType.GET_INPUT.getName()));
        assertTrue(parameter1Map.get(ToscaFunctionType.GET_INPUT.getName()) instanceof List);
        List<String> getInputParameters = (List<String>) parameter1Map.get(ToscaFunctionType.GET_INPUT.getName());
        assertEquals(2, getInputParameters.size(), "Expecting two parameters in the get_input function");
        assertEquals("nf_naming", getInputParameters.get(0));
        assertEquals("instance_name", getInputParameters.get(1));

        assertEquals("my string", concatFunctionParameters.get(1));

        assertTrue(concatFunctionParameters.get(2) instanceof Map);
        final Map<String, Object> parameter2Map = (Map<String, Object>) concatFunctionParameters.get(2);
        assertNotNull(parameter2Map.get(ToscaFunctionType.CONCAT.getName()));
        assertTrue(parameter2Map.get(ToscaFunctionType.CONCAT.getName()) instanceof List);
        List<Object> concatParameters = (List<Object>) parameter2Map.get(ToscaFunctionType.CONCAT.getName());
        assertEquals(3, concatParameters.size(), "Expecting two parameters in the sub concat function");
        assertEquals("string1", concatParameters.get(0));
        assertEquals("string2", concatParameters.get(1));
        assertTrue(concatParameters.get(2) instanceof Map);
        Map<String, Object> yamlFunctionValueMap = (Map<String, Object>) concatParameters.get(2);
        assertTrue(yamlFunctionValueMap.get("myList") instanceof List);
        assertTrue(yamlFunctionValueMap.get("get_something") instanceof List);
        assertTrue(yamlFunctionValueMap.get("string") instanceof String);
    }

    @Test
    void testYamlFunction() throws IOException {
        //given
        final String yamlFunction = Files.readString(TEST_RESOURCES_PATH.resolve("yamlFunction.json"));
        //when
        final ToscaFunction toscaFunction = parseToscaFunction(yamlFunction);
        //then
        assertTrue(toscaFunction instanceof CustomYamlFunction);
        assertDoesNotThrow(() -> new Yaml().load(toscaFunction.getValue()));
    }

    private ToscaFunction parseToscaFunction(final String toscaFunctionJson) throws JsonProcessingException {
        return new ObjectMapper().readValue(toscaFunctionJson, ToscaFunction.class);
    }
}