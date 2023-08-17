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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
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

    @Test
    void testCustomToscaFunction() throws IOException {
        //given
        setDefaultCustomToscaFunctionOnConfiguration();
        final String toscaCustomFunction = Files.readString(TEST_RESOURCES_PATH.resolve("customFunction.json"));
        //when
        final ToscaFunction toscaFunction = parseToscaFunction(toscaCustomFunction);
        //then
        assertTrue(toscaFunction instanceof ToscaCustomFunction);
        final Object yamlObject = new Yaml().load(toscaFunction.getValue());
        assertTrue(yamlObject instanceof Map);
        final Map<String, Object> yamlMap = (Map<String, Object>) yamlObject;
        final Object customFunctionObj = yamlMap.get("$" + ((ToscaCustomFunction) toscaFunction).getName());
        assertNotNull(customFunctionObj);
        assertTrue(customFunctionObj instanceof List);
        final List<Object> customFunctionParameters = (List<Object>) customFunctionObj;
        assertEquals(3, customFunctionParameters.size(), "Expecting three parameters");
        assertEquals("string1", customFunctionParameters.get(0));

        assertTrue(customFunctionParameters.get(1) instanceof Map);
        final Map<String, Object> parameter1Map = (Map<String, Object>) customFunctionParameters.get(1);
        assertNotNull(parameter1Map.get(ToscaFunctionType.GET_ATTRIBUTE.getName()));
        assertTrue(parameter1Map.get(ToscaFunctionType.GET_ATTRIBUTE.getName()) instanceof List);
        List<String> getAttributeParameters = (List<String>) parameter1Map.get(ToscaFunctionType.GET_ATTRIBUTE.getName());
        assertEquals(2, getAttributeParameters.size(), "Expecting two parameters in the get_attribute function");
        assertEquals("SELF", getAttributeParameters.get(0));
        assertEquals("descriptor_id", getAttributeParameters.get(1));

        assertTrue(customFunctionParameters.get(2) instanceof Map);
        final Map<String, Object> parameter2Map = (Map<String, Object>) customFunctionParameters.get(2);
        Object customFunctionObj2 = parameter2Map.get(parameter2Map.keySet().stream().iterator().next());
        assertNotNull(customFunctionObj2);
        assertTrue(customFunctionObj2 instanceof List);
        List<Object> customParameters = (List<Object>) customFunctionObj2;
        assertEquals(2, customParameters.size(), "Expecting two parameters in the sub custom function");
        assertTrue(customParameters.get(0) instanceof Map);
        final Map<String, Object> concatFunctionValueMap = (Map<String, Object>) customParameters.get(0);
        assertNotNull(concatFunctionValueMap.get(ToscaFunctionType.CONCAT.getName()));
        assertTrue(concatFunctionValueMap.get(ToscaFunctionType.CONCAT.getName()) instanceof List);
        List<Object> concatParameters = (List<Object>) concatFunctionValueMap.get(ToscaFunctionType.CONCAT.getName());
        assertEquals(2, concatParameters.size(), "Expecting two parameters in the sub concat function");
        assertEquals("string2", concatParameters.get(0));
        assertTrue(concatParameters.get(1) instanceof Map);
        Map<String, Object> yamlFunctionValueMap = (Map<String, Object>) concatParameters.get(1);
        assertTrue(yamlFunctionValueMap.get("myList") instanceof List);
        assertTrue(yamlFunctionValueMap.get("get_something") instanceof List);
        assertTrue(yamlFunctionValueMap.get("string") instanceof String);

        assertEquals("string3", customParameters.get(1));
    }

    @Test
    void testCustomToscaFunctionGetInputType() throws IOException {
        //given
        setDefaultCustomToscaFunctionOnConfiguration();
        final String toscaCustomFunctionFile = Files.readString(TEST_RESOURCES_PATH.resolve("customFunctionGetInputType.json"));
        //when
        final ToscaFunction toscaFunction = parseToscaFunction(toscaCustomFunctionFile);
        //then
        assertTrue(toscaFunction instanceof ToscaCustomFunction);
        ToscaCustomFunction toscaCustomFunction = (ToscaCustomFunction) toscaFunction;
        final Object yamlObject = new Yaml().load(toscaFunction.getValue());
        assertTrue(yamlObject instanceof Map);
        final Map<String, Object> yamlMap = (Map<String, Object>) yamlObject;
        assertEquals(1, yamlMap.size());
        final Object customFunctionGetInputValue = yamlMap.get("$" + ((ToscaCustomFunction) toscaFunction).getName());
        assertTrue(customFunctionGetInputValue instanceof ArrayList);
        List<Object> customFunctionGetInputValueList = (ArrayList<Object>) customFunctionGetInputValue;
        assertEquals(4, customFunctionGetInputValueList.size());
        assertEquals("pLMNInfoList", customFunctionGetInputValueList.get(0));
        assertEquals(1, customFunctionGetInputValueList.get(1));
        assertEquals("snssai", customFunctionGetInputValueList.get(2));
        assertEquals("sd", customFunctionGetInputValueList.get(3));

        List<ToscaFunctionParameter> parameters = toscaCustomFunction.getParameters();
        assertEquals(1, parameters.size());
        ToscaFunctionParameter paramFunction = toscaCustomFunction.getParameters().get(0);
        assertTrue(paramFunction instanceof ToscaGetFunctionDataDefinition);

        final ToscaGetFunctionDataDefinition toscaGetFunction = (ToscaGetFunctionDataDefinition) paramFunction;
        assertEquals(ToscaFunctionType.GET_INPUT, toscaGetFunction.getType());
        assertEquals(ToscaGetFunctionType.GET_INPUT, toscaGetFunction.getFunctionType());
        assertEquals("dd0ec4d2-7e74-4d92-af2f-89c7436baa63.pLMNInfoList", toscaGetFunction.getPropertyUniqueId());
        assertEquals(PropertySource.SELF, toscaGetFunction.getPropertySource());
        assertEquals("pLMNInfoList", toscaGetFunction.getPropertyName());
        assertEquals("testService", toscaGetFunction.getSourceName());
        assertEquals("dd0ec4d2-7e74-4d92-af2f-89c7436baa63", toscaGetFunction.getSourceUniqueId());
        assertEquals(List.of("pLMNInfoList"), toscaGetFunction.getPropertyPathFromSource());
    }

    private void setDefaultCustomToscaFunctionOnConfiguration() {
        final var configurationManager = new ConfigurationManager();
        final var configuration = new Configuration();
        List<Configuration.CustomToscaFunction> defaultCustomToscaFunctions = new ArrayList<>();
        Configuration.CustomToscaFunction defaultCustomType = new Configuration.CustomToscaFunction();
        defaultCustomType.setName("custom_function_get_input_type");
        defaultCustomType.setType("get_input");
        defaultCustomToscaFunctions.add(defaultCustomType);
        configuration.setDefaultCustomToscaFunctions(defaultCustomToscaFunctions);
        configurationManager.setConfiguration(configuration);
    }

    private ToscaFunction parseToscaFunction(final String toscaFunctionJson) throws JsonProcessingException {
        return new ObjectMapper().readValue(toscaFunctionJson, ToscaFunction.class);
    }
}
