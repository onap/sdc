/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.DEFAULT_VALUE;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.DEFINITION;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.DESCRIPTION;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.HIDDEN;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.IMMUTABLE;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.INPUT_ID;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.INPUT_PATH;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.INSTANCE_UNIQUE_ID;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.LABEL;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.NAME;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PARENT_PROPERTY_TYPE;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PASSWORD;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PROPERTY;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PROPERTY_CONSTRAINTS;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PROPERTY_ID;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.REQUIRED;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.SCHEMA;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.STATUS;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.SUB_PROPERTY_INPUT_PATH;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.TYPE;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.UNIQUE_ID;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.VALUE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;

class PropertyDataConverterTest {

    private PropertyDataConverter propertyDataConverter;
    @Mock
    private SchemaDefinitionConverter schemaDefinitionConverter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        propertyDataConverter = new PropertyDataConverter();
        propertyDataConverter.setSchemaDefinitionConverter(schemaDefinitionConverter);
    }

    @Test
    void parsePropertiesSuccessTest() {
        final List<Map<String, Object>> propertyDataList = new ArrayList<>();
        final HashMap<String, Object> propertyJsonMap1 = new HashMap<>();
        propertyJsonMap1.put("type", "string");
        propertyDataList.add(propertyJsonMap1);
        final HashMap<String, Object> propertyJsonMap2 = new HashMap<>();
        propertyJsonMap2.put("type", "boolean");
        propertyDataList.add(propertyJsonMap2);
        final HashMap<String, Object> propertyJsonMap3 = new HashMap<>();
        propertyJsonMap3.put("type", "a.tosca.type");
        propertyDataList.add(propertyJsonMap3);
        final List<PropertyDataDefinition> actualPropertyDataList = propertyDataConverter
            .parseProperties(propertyDataList);
        assertThat(actualPropertyDataList, is(notNullValue()));
        assertThat(actualPropertyDataList.size(), is(propertyDataList.size()));
    }

    @Test
    void parseEmptyPropertiesListTest() {
        List<PropertyDataDefinition> propertyDataDefinitions =
            propertyDataConverter.parseProperties(null);
        assertThat("Should not be null", propertyDataDefinitions, is(notNullValue()));
        assertThat("Should be empty", propertyDataDefinitions, is(empty()));
        propertyDataDefinitions =
            propertyDataConverter.parseProperties(Collections.emptyList());
        assertThat("Should not be null", propertyDataDefinitions, is(notNullValue()));
        assertThat("Should be empty", propertyDataDefinitions, is(empty()));
    }

    @Test
    void createPropertyDataSuccessTest() {
        final Map<String, Object> propertyJsonMap = new HashMap<>();
        final PropertyDataDefinition expectedPropertyData = new PropertyDataDefinition();

        final String uniqueId = "uniqueId";
        expectedPropertyData.setUniqueId(uniqueId);
        propertyJsonMap.put(UNIQUE_ID.getPresentation(), uniqueId);

        final String type = "type";
        expectedPropertyData.setType(type);
        propertyJsonMap.put(TYPE.getPresentation(), type);

        final Boolean required = Boolean.FALSE;
        expectedPropertyData.setRequired(required);
        propertyJsonMap.put(REQUIRED.getPresentation(), required);

        final String name = "name";
        expectedPropertyData.setName(name);
        propertyJsonMap.put(NAME.getPresentation(), name);

        final String label = "label";
        expectedPropertyData.setLabel(label);
        propertyJsonMap.put(LABEL.getPresentation(), label);

        final String value = "value";
        expectedPropertyData.setValue(value);
        propertyJsonMap.put(VALUE.getPresentation(), value);

        final Boolean hidden = Boolean.FALSE;
        expectedPropertyData.setHidden(hidden);
        propertyJsonMap.put(HIDDEN.getPresentation(), hidden);

        final Boolean immutable = Boolean.TRUE;
        expectedPropertyData.setImmutable(immutable);
        propertyJsonMap.put(IMMUTABLE.getPresentation(), immutable);

        final boolean password = false;
        expectedPropertyData.setPassword(password);
        propertyJsonMap.put(PASSWORD.getPresentation(), password);

        final boolean definition = false;
        expectedPropertyData.setDefinition(definition);
        propertyJsonMap.put(DEFINITION.getPresentation(), definition);

        final String description = "description";
        expectedPropertyData.setDescription(description);
        propertyJsonMap.put(DESCRIPTION.getPresentation(), description);

        final String defaultValue = "defaultValue";
        expectedPropertyData.setDefaultValue(defaultValue);
        propertyJsonMap.put(DEFAULT_VALUE.getPresentation(), defaultValue);

        final String inputPath = "inputPath";
        expectedPropertyData.setInputPath(inputPath);
        propertyJsonMap.put(INPUT_PATH.getPresentation(), inputPath);

        final String status = "status";
        expectedPropertyData.setStatus(status);
        propertyJsonMap.put(STATUS.getPresentation(), status);

        final String inputId = "inputId";
        expectedPropertyData.setInputId(inputId);
        propertyJsonMap.put(INPUT_ID.getPresentation(), inputId);

        final String instanceUniqueId = "instanceUniqueId";
        expectedPropertyData.setInstanceUniqueId(instanceUniqueId);
        propertyJsonMap.put(INSTANCE_UNIQUE_ID.getPresentation(), instanceUniqueId);

        final String propertyId = "propertyId";
        expectedPropertyData.setPropertyId(propertyId);
        propertyJsonMap.put(PROPERTY_ID.getPresentation(), propertyId);

        final String parentPropertyType = "parentPropertyType";
        expectedPropertyData.setParentPropertyType(parentPropertyType);
        propertyJsonMap.put(PARENT_PROPERTY_TYPE.getPresentation(), parentPropertyType);

        final String subPropertyType = "subPropertyType";
        expectedPropertyData.setSubPropertyInputPath(subPropertyType);
        propertyJsonMap.put(SUB_PROPERTY_INPUT_PATH.getPresentation(), subPropertyType);

        PropertyDataDefinition actualPropertyData = propertyDataConverter.createPropertyData(propertyJsonMap);
        assertThat(actualPropertyData.getUniqueId(), is(expectedPropertyData.getUniqueId()));
        assertThat(actualPropertyData.getRequired(), is(expectedPropertyData.getRequired()));
        assertThat(actualPropertyData.getName(), is(expectedPropertyData.getName()));
        assertThat(actualPropertyData.getLabel(), is(expectedPropertyData.getLabel()));
        assertThat(actualPropertyData.getValue(), is(expectedPropertyData.getValue()));
        assertThat(actualPropertyData.getHidden(), is(expectedPropertyData.getHidden()));
        assertThat(actualPropertyData.getImmutable(), is(expectedPropertyData.getImmutable()));
        assertThat(actualPropertyData.isPassword(), is(expectedPropertyData.isPassword()));
        assertThat(actualPropertyData.getDefinition(), is(expectedPropertyData.getDefinition()));
        assertThat(actualPropertyData.getDescription(), is(expectedPropertyData.getDescription()));
        assertThat(actualPropertyData.getDefaultValue(), is(expectedPropertyData.getDefaultValue()));
        assertThat(actualPropertyData.getInputPath(), is(expectedPropertyData.getInputPath()));
        assertThat(actualPropertyData.getStatus(), is(expectedPropertyData.getStatus()));
        assertThat(actualPropertyData.getInputId(), is(expectedPropertyData.getInputId()));
        assertThat(actualPropertyData.getInstanceUniqueId(), is(expectedPropertyData.getInstanceUniqueId()));
        assertThat(actualPropertyData.getPropertyId(), is(expectedPropertyData.getPropertyId()));
        assertThat(actualPropertyData.getParentPropertyType(), is(expectedPropertyData.getParentPropertyType()));
        assertThat(actualPropertyData.getSubPropertyInputPath(), is(expectedPropertyData.getSubPropertyInputPath()));
        assertThat(actualPropertyData.getSchema(), is(nullValue()));
        assertThat(actualPropertyData.getPropertyConstraints(), is(nullValue()));

        //schema
        final Map<String, Object> propertySchemaMap = new HashMap<>();
        propertySchemaMap.put(TYPE.getPresentation(), "string");
        final Map<String, Object> schemaMap = new HashMap<>();
        schemaMap.put(PROPERTY.getPresentation(), propertySchemaMap);
        propertyJsonMap.put(SCHEMA.getPresentation(), schemaMap);
        final SchemaDefinition schemaDefinition = new SchemaDefinition();
        final PropertyDataDefinition schemaPropertyData = new PropertyDataDefinition();
        schemaPropertyData.setType("string");
        schemaDefinition.setProperty(schemaPropertyData);
        expectedPropertyData.setSchema(schemaDefinition);
        when(schemaDefinitionConverter.parseTo(schemaMap)).thenReturn(Optional.of(schemaDefinition));
        //constraints
        final List<String> propertyConstraintList = new ArrayList<>();
        propertyConstraintList.add("constraint1");
        propertyConstraintList.add("constraint2");
        expectedPropertyData.setPropertyConstraints(propertyConstraintList);
        propertyJsonMap.put(PROPERTY_CONSTRAINTS.getPresentation(), propertyConstraintList);
        actualPropertyData = propertyDataConverter.createPropertyData(propertyJsonMap);
        assertThat(actualPropertyData.getSchema(), is(notNullValue()));
        assertThat(actualPropertyData.getSchema().getType(), is(expectedPropertyData.getSchema().getType()));
        assertThat(actualPropertyData.getPropertyConstraints(), is(notNullValue()));
        assertThat(actualPropertyData.getPropertyConstraints().size(), is(expectedPropertyData.getPropertyConstraints().size()));
        assertThat(actualPropertyData.getPropertyConstraints(), containsInAnyOrder(propertyConstraintList.toArray()));
    }

    @Test
    void createEmptyPropertyDataTest() {
        final Map<String, Object> propertyJsonMap = new HashMap<>();
        final PropertyDataDefinition propertyData = propertyDataConverter.createPropertyData(propertyJsonMap);
        final PropertyDataDefinition expectedPropertyData = new PropertyDataDefinition();
        assertThat("Should be the equal", propertyData, is(expectedPropertyData));
    }
}