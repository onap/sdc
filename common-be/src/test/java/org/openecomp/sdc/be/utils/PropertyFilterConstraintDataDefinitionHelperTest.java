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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterConstraintDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaConcatFunction;
import org.openecomp.sdc.be.datatypes.elements.ToscaFunctionType;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;

class PropertyFilterConstraintDataDefinitionHelperTest {

    private static final Path RESOURCE_PATH = Path.of("src", "test", "resources", "nodeFilter", "constraints");

    @Test
    void convertLegacyConstraintGetInputTest() throws IOException {
        final var propertyFilterConstraint =
            PropertyFilterConstraintDataDefinitionHelper.convertLegacyConstraint(readConstraintFile("legacy-get_input.yaml"));
        assertPropertyFilterConstraint(propertyFilterConstraint, "flavour_id", null, ConstraintType.GREATER_OR_EQUAL, FilterValueType.GET_INPUT);
        assertTrue(propertyFilterConstraint.getValue() instanceof ToscaGetFunctionDataDefinition);
        final var toscaGetFunction = (ToscaGetFunctionDataDefinition) propertyFilterConstraint.getValue();
        assertToscaGetFunction(toscaGetFunction, ToscaFunctionType.GET_INPUT, ToscaGetFunctionType.GET_INPUT, PropertySource.SELF,
            List.of("inputName"), "inputName", null);
    }

    @Test
    void convertLegacyConstraintGetInputSubPathTest() throws IOException {
        final PropertyFilterConstraintDataDefinition propertyFilterConstraint =
            PropertyFilterConstraintDataDefinitionHelper.convertLegacyConstraint(readConstraintFile("legacy-get_input-subProperty.yaml"));
        assertPropertyFilterConstraint(propertyFilterConstraint, "flavour_id", null, ConstraintType.EQUAL, FilterValueType.GET_INPUT);
        assertTrue(propertyFilterConstraint.getValue() instanceof ToscaGetFunctionDataDefinition);
        final var toscaGetFunction = (ToscaGetFunctionDataDefinition) propertyFilterConstraint.getValue();
        assertToscaGetFunction(toscaGetFunction, ToscaFunctionType.GET_INPUT, ToscaGetFunctionType.GET_INPUT, PropertySource.SELF,
            List.of("inputName", "inputSubProperty", "inputSubSubProperty"), "inputSubSubProperty", null);
    }

    @Test
    void convertLegacyConstraintGetPropertyFromInstanceTest() throws IOException {
        final PropertyFilterConstraintDataDefinition propertyFilterConstraint =
            PropertyFilterConstraintDataDefinitionHelper.convertLegacyConstraint(readConstraintFile("legacy-get_property-from-instance.yaml"));
        assertPropertyFilterConstraint(propertyFilterConstraint, "flavour_id", null, ConstraintType.EQUAL, FilterValueType.GET_PROPERTY);
        assertTrue(propertyFilterConstraint.getValue() instanceof ToscaGetFunctionDataDefinition);
        final var toscaGetFunction = (ToscaGetFunctionDataDefinition) propertyFilterConstraint.getValue();
        assertToscaGetFunction(toscaGetFunction, ToscaFunctionType.GET_PROPERTY, ToscaGetFunctionType.GET_PROPERTY, PropertySource.INSTANCE,
            List.of("property", "subProperty"), "subProperty", "Instance Name");
    }

    @Test
    void convertLegacyConstraintGetAttributeFromInstanceTest() throws IOException {
        final PropertyFilterConstraintDataDefinition propertyFilterConstraint =
            PropertyFilterConstraintDataDefinitionHelper.convertLegacyConstraint(readConstraintFile("legacy-get_attribute-from-instance.yaml"));
        assertPropertyFilterConstraint(propertyFilterConstraint, "flavour_id", null, ConstraintType.EQUAL, FilterValueType.GET_ATTRIBUTE);
        assertTrue(propertyFilterConstraint.getValue() instanceof ToscaGetFunctionDataDefinition);
        final var toscaGetFunction = (ToscaGetFunctionDataDefinition) propertyFilterConstraint.getValue();
        assertToscaGetFunction(toscaGetFunction, ToscaFunctionType.GET_ATTRIBUTE, ToscaGetFunctionType.GET_ATTRIBUTE, PropertySource.INSTANCE,
            List.of("property", "subProperty"), "subProperty", "Instance Name");
    }


    @Test
    void convertLegacyConstraintGetPropertyFromSelfTest() throws IOException {
        final PropertyFilterConstraintDataDefinition propertyFilterConstraint =
            PropertyFilterConstraintDataDefinitionHelper.convertLegacyConstraint(readConstraintFile("legacy-get_property-from-self.yaml"));
        assertPropertyFilterConstraint(propertyFilterConstraint, "flavour_id", null, ConstraintType.EQUAL, FilterValueType.GET_PROPERTY);
        assertTrue(propertyFilterConstraint.getValue() instanceof ToscaGetFunctionDataDefinition);
        final var toscaGetFunction = (ToscaGetFunctionDataDefinition) propertyFilterConstraint.getValue();
        assertToscaGetFunction(toscaGetFunction, ToscaFunctionType.GET_PROPERTY, ToscaGetFunctionType.GET_PROPERTY, PropertySource.SELF,
            List.of("property", "subProperty"), "subProperty", null);
    }

    @Test
    void convertLegacyConstraintGetAttributeFromSelfTest() throws IOException {
        final PropertyFilterConstraintDataDefinition propertyFilterConstraint =
            PropertyFilterConstraintDataDefinitionHelper.convertLegacyConstraint(readConstraintFile("legacy-get_attribute-from-self.yaml"));
        assertPropertyFilterConstraint(propertyFilterConstraint, "flavour_id", null, ConstraintType.EQUAL, FilterValueType.GET_ATTRIBUTE);
        assertTrue(propertyFilterConstraint.getValue() instanceof ToscaGetFunctionDataDefinition);
        final var toscaGetFunction = (ToscaGetFunctionDataDefinition) propertyFilterConstraint.getValue();
        assertToscaGetFunction(toscaGetFunction, ToscaFunctionType.GET_ATTRIBUTE, ToscaGetFunctionType.GET_ATTRIBUTE, PropertySource.SELF,
            List.of("property", "subProperty"), "subProperty", null);
    }

    @Test
    void convertLegacyConstraintStaticTest() throws IOException {
        final PropertyFilterConstraintDataDefinition propertyFilterConstraint =
            PropertyFilterConstraintDataDefinitionHelper.convertLegacyConstraint(readConstraintFile("legacy-static.yaml"));
        assertPropertyFilterConstraint(propertyFilterConstraint, "vnf_profile", null, ConstraintType.EQUAL, FilterValueType.STATIC);
        assertTrue(propertyFilterConstraint.getValue() instanceof Map);
        final Map<String, Object> value = (Map<String, Object>) propertyFilterConstraint.getValue();
        assertEquals("1", value.get("instantiation_level"));
        assertEquals(1, value.get("max_number_of_instances"));
        assertEquals(1, value.get("min_number_of_instances"));
    }

    @Test
    void convertLegacyConstraintConcatTest() throws IOException {
        final PropertyFilterConstraintDataDefinition propertyFilterConstraint =
            PropertyFilterConstraintDataDefinitionHelper.convertLegacyConstraint(readConstraintFile("concat.yaml"));
        assertPropertyFilterConstraint(propertyFilterConstraint, "descriptor_id", null, ConstraintType.EQUAL, FilterValueType.CONCAT);
        assertTrue(propertyFilterConstraint.getValue() instanceof ToscaConcatFunction);
        final ToscaConcatFunction toscaConcatFunction = (ToscaConcatFunction) propertyFilterConstraint.getValue();
        assertEquals(3, toscaConcatFunction.getParameters().size());
        assertEquals(ToscaFunctionType.STRING, toscaConcatFunction.getParameters().get(0).getType());
        assertEquals("aString", toscaConcatFunction.getParameters().get(0).getValue());
        assertEquals(ToscaFunctionType.GET_INPUT, toscaConcatFunction.getParameters().get(1).getType());
        assertEquals(ToscaFunctionType.STRING, toscaConcatFunction.getParameters().get(2).getType());
        assertEquals("anotherString", toscaConcatFunction.getParameters().get(2).getValue());
    }

    private static void assertPropertyFilterConstraint(final PropertyFilterConstraintDataDefinition propertyFilterConstraint,
                                                       final String propertyName, final String capabilityName, final ConstraintType constraintType,
                                                       final FilterValueType filterValueType) {
        assertEquals(propertyName, propertyFilterConstraint.getPropertyName());
        assertEquals(capabilityName, propertyFilterConstraint.getCapabilityName());
        assertEquals(constraintType, propertyFilterConstraint.getOperator());
        assertEquals(filterValueType, propertyFilterConstraint.getValueType());
    }

    private void assertToscaGetFunction(final ToscaGetFunctionDataDefinition actualToscaGetFunction,
                                        final ToscaFunctionType expectedToscaFunctionType, final ToscaGetFunctionType expectedToscaFunctionGetType,
                                        final PropertySource expectedPropertySource, final List<String> expectedPropertyPathFromSource,
                                        final String expectedPropertyName, final String expectedSourceName) {
        assertEquals(expectedToscaFunctionType, actualToscaGetFunction.getType());
        assertEquals(expectedToscaFunctionGetType, actualToscaGetFunction.getFunctionType());
        assertEquals(expectedPropertySource, actualToscaGetFunction.getPropertySource());
        assertEquals(expectedPropertyPathFromSource, actualToscaGetFunction.getPropertyPathFromSource());
        assertEquals(expectedPropertyName, actualToscaGetFunction.getPropertyName());
        assertEquals(expectedSourceName, actualToscaGetFunction.getSourceName());
        assertNull(actualToscaGetFunction.getPropertyUniqueId());
        assertNull(actualToscaGetFunction.getSourceUniqueId());
    }

    @Test
    void convertFromToscaFunctionTypeTest() {
        Optional<FilterValueType> filterValueType =
            PropertyFilterConstraintDataDefinitionHelper.convertFromToscaFunctionType(ToscaFunctionType.GET_PROPERTY);
        assertTrue(filterValueType.isPresent());
        assertEquals(FilterValueType.GET_PROPERTY, filterValueType.get());

        filterValueType =
            PropertyFilterConstraintDataDefinitionHelper.convertFromToscaFunctionType(ToscaFunctionType.GET_INPUT);
        assertTrue(filterValueType.isPresent());
        assertEquals(FilterValueType.GET_INPUT, filterValueType.get());

        filterValueType =
            PropertyFilterConstraintDataDefinitionHelper.convertFromToscaFunctionType(ToscaFunctionType.GET_ATTRIBUTE);
        assertTrue(filterValueType.isPresent());
        assertEquals(FilterValueType.GET_ATTRIBUTE, filterValueType.get());

        filterValueType =
            PropertyFilterConstraintDataDefinitionHelper.convertFromToscaFunctionType(ToscaFunctionType.YAML);
        assertTrue(filterValueType.isPresent());
        assertEquals(FilterValueType.YAML, filterValueType.get());

        filterValueType =
            PropertyFilterConstraintDataDefinitionHelper.convertFromToscaFunctionType(ToscaFunctionType.CONCAT);
        assertTrue(filterValueType.isPresent());
        assertEquals(FilterValueType.CONCAT, filterValueType.get());

        assertTrue(PropertyFilterConstraintDataDefinitionHelper.convertFromToscaFunctionType(ToscaFunctionType.STRING).isEmpty());
    }

    private String readConstraintFile(final String fileName) throws IOException {
        return Files.readString(RESOURCE_PATH.resolve(fileName));
    }
}