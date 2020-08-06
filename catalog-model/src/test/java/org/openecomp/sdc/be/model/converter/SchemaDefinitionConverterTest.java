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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.PROPERTY;
import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.TYPE;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;

class SchemaDefinitionConverterTest {

    private SchemaDefinitionConverter schemaDefinitionConverter;
    @Mock
    private PropertyDataConverter propertyDataConverter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        schemaDefinitionConverter = new SchemaDefinitionConverter();
        schemaDefinitionConverter.setPropertyDataConverter(propertyDataConverter);
    }

    @Test
    void parseToSuccessTest() {
        //given
        final Map<String, Object> propertySchemaMap = new HashMap<>();
        propertySchemaMap.put(TYPE.getPresentation(), "string");
        final Map<String, Object> schemaMap = new HashMap<>();
        schemaMap.put(PROPERTY.getPresentation(), propertySchemaMap);
        final PropertyDataDefinition schemaPropertyData = new PropertyDataDefinition();
        schemaPropertyData.setType("string");
        //when
        when(propertyDataConverter.createPropertyData(propertySchemaMap)).thenReturn(schemaPropertyData);
        final SchemaDefinition actualSchemaDefinition = schemaDefinitionConverter.parseTo(schemaMap).orElse(null);
        //then
        assertThat(actualSchemaDefinition, is(notNullValue()));
        assertThat(actualSchemaDefinition.getProperty(), is(notNullValue()));
        assertThat(actualSchemaDefinition.getProperty().getType(), is(schemaPropertyData.getType()));
    }

    @Test
    void parseToEmptyJsonMap() {
        //given
        final Map<String, Object> schemaMap = new HashMap<>();
        //when
        final Optional<SchemaDefinition> actualSchemaDefinition = schemaDefinitionConverter.parseTo(schemaMap);
        //then
        assertThat(actualSchemaDefinition.isPresent(), is(false));
    }
}