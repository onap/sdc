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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.datatypes.enums.PropertyFilterTargetType;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;

class PropertyFilterConstraintDataDefinitionJsonDeserializerTest {
    private static final Path TEST_RESOURCES_PATH = Path.of("src/test/resources/propertyFilterConstraintDataDefinitionDeserializer");

    @Test
    void testStaticPropertyFilter() throws IOException {
        //given
        final String propertyFilterAsString = Files.readString(TEST_RESOURCES_PATH.resolve("filter-constraint-static.json"));
        //when
        final PropertyFilterConstraintDataDefinition actualPropertyFilterConstraint = parseToscaFunction(propertyFilterAsString);
        //then
        assertEquals(FilterValueType.STATIC, actualPropertyFilterConstraint.getValueType());
        assertEquals(ConstraintType.EQUAL, actualPropertyFilterConstraint.getOperator());
        assertEquals(PropertyFilterTargetType.CAPABILITY, actualPropertyFilterConstraint.getTargetType());
        assertEquals("aCapability", actualPropertyFilterConstraint.getCapabilityName());
        assertEquals("aProperty", actualPropertyFilterConstraint.getPropertyName());
        assertEquals("aStaticValue", actualPropertyFilterConstraint.getValue());
    }

    @Test
    void testGetInputToscaFunction() throws IOException {
        //given
        final String toscaGetInputFunction = Files.readString(TEST_RESOURCES_PATH.resolve("filter-constraint-get-input.json"));
        //when
        final PropertyFilterConstraintDataDefinition actualPropertyFilterConstraint = parseToscaFunction(toscaGetInputFunction);
        //then
        assertEquals(FilterValueType.GET_INPUT, actualPropertyFilterConstraint.getValueType());
        assertEquals(ConstraintType.GREATER_THAN, actualPropertyFilterConstraint.getOperator());
        assertEquals(PropertyFilterTargetType.PROPERTY, actualPropertyFilterConstraint.getTargetType());
        assertNull(actualPropertyFilterConstraint.getCapabilityName());
        assertEquals("aProperty", actualPropertyFilterConstraint.getPropertyName());
        assertTrue(actualPropertyFilterConstraint.getValue() instanceof ToscaGetFunctionDataDefinition);
        final ToscaGetFunctionDataDefinition toscaGetFunction = (ToscaGetFunctionDataDefinition) actualPropertyFilterConstraint.getValue();
        assertEquals(ToscaFunctionType.GET_INPUT, toscaGetFunction.getType());
        assertEquals(ToscaGetFunctionType.GET_INPUT, toscaGetFunction.getFunctionType());
        assertEquals("aPropertyId", toscaGetFunction.getPropertyUniqueId());
        assertEquals("aProperty", toscaGetFunction.getPropertyName());
        assertEquals(PropertySource.SELF, toscaGetFunction.getPropertySource());
        assertEquals("aServiceId", toscaGetFunction.getSourceUniqueId());
        assertEquals("aService", toscaGetFunction.getSourceName());
        assertEquals(List.of("input", "subProperty"), toscaGetFunction.getPropertyPathFromSource());
    }

    @Test
    void testLegacyPropertyFilter() throws IOException {
        //given
        final String legacyPropertyFilter = Files.readString(TEST_RESOURCES_PATH.resolve("filter-constraint-legacy.txt"));
        //when
        final PropertyFilterConstraintDataDefinition actualPropertyFilterConstraint = parseToscaFunction(legacyPropertyFilter);
        //then
        assertEquals(FilterValueType.STATIC, actualPropertyFilterConstraint.getValueType());
        assertEquals(ConstraintType.EQUAL, actualPropertyFilterConstraint.getOperator());
        assertEquals(PropertyFilterTargetType.PROPERTY, actualPropertyFilterConstraint.getTargetType());
        assertNull(actualPropertyFilterConstraint.getCapabilityName());
        assertEquals("propertyName", actualPropertyFilterConstraint.getPropertyName());
        assertEquals("aValue", actualPropertyFilterConstraint.getValue());
    }

    private PropertyFilterConstraintDataDefinition parseToscaFunction(final String propertyFilterConstraintAsJson) throws JsonProcessingException {
        return new ObjectMapper().readValue(propertyFilterConstraintAsJson, PropertyFilterConstraintDataDefinition.class);
    }
}