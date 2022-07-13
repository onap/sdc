/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

package org.openecomp.sdc.be.tosca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.onap.sdc.tosca.datatypes.model.EntrySchema;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.tosca.exception.ToscaConversionException;
import org.openecomp.sdc.be.tosca.model.ToscaAttribute;
import org.openecomp.sdc.be.tosca.model.ToscaSchemaDefinition;

class AttributeConverterTest {

    @Test
    void testScalarTypeConversion() throws ToscaConversionException {
        //given
        final AttributeConverter attributeConverter = createTestSubject();
        final AttributeDefinition attributeDefinition = new AttributeDefinition();
        attributeDefinition.setType(ToscaPropertyType.STRING.getType());
        attributeDefinition.setDescription("aDescription");
        attributeDefinition.setStatus("aStatus");
        attributeDefinition.setDefaultValue("aDefaultValue");
        //when
        final ToscaAttribute actualToscaAttribute = attributeConverter.convert(attributeDefinition);
        //then
        assertAttribute(attributeDefinition, actualToscaAttribute);
    }

    @Test
    void testScalarNoDefaultValueConversion() throws ToscaConversionException {
        //given
        final AttributeConverter attributeConverter = createTestSubject();
        final AttributeDefinition attributeDefinition = new AttributeDefinition();
        attributeDefinition.setType(ToscaPropertyType.STRING.getType());
        attributeDefinition.setDescription("aDescription");
        attributeDefinition.setStatus("aStatus");
        //when
        final ToscaAttribute actualToscaAttribute = attributeConverter.convert(attributeDefinition);
        //then
        assertAttribute(attributeDefinition, null, actualToscaAttribute);
    }

    @Test
    void testListTypeConversion() throws JsonProcessingException, ToscaConversionException {
        //given
        final Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        final AttributeConverter attributeConverter = new AttributeConverter(dataTypes);
        final AttributeDefinition attributeDefinition = new AttributeDefinition();
        attributeDefinition.setType(ToscaPropertyType.LIST.getType());
        attributeDefinition.setDescription("aDescription");
        attributeDefinition.setStatus("aStatus");
        attributeDefinition.setSchema(new SchemaDefinition());
        attributeDefinition.getSchema().setProperty(new PropertyDataDefinition());
        attributeDefinition.getSchema().getProperty().setType(ToscaPropertyType.LIST.getType());
        attributeDefinition.getSchema().getProperty().setDescription("anEntrySchemaDescription");
        final List<String> defaultValueList = new ArrayList<>();
        defaultValueList.add("entry1");
        defaultValueList.add("entry2");
        defaultValueList.add("entry2");
        attributeDefinition.setDefaultValue(parseToJsonString(defaultValueList));
        //when
        final ToscaAttribute actualToscaAttribute = attributeConverter.convert(attributeDefinition);
        //then
        assertAttribute(attributeDefinition, defaultValueList, actualToscaAttribute);
    }

    @Test
    void testDataTypeTypeConversion() throws JsonProcessingException, ToscaConversionException {
        //given
        final Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        final DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
        final List<PropertyDefinition> propertyDefinitionList = new ArrayList<>();
        final PropertyDefinition integerProperty = new PropertyDefinition();
        integerProperty.setName("anIntegerProperty");
        integerProperty.setType(ToscaPropertyType.INTEGER.getType());
        propertyDefinitionList.add(integerProperty);
        final PropertyDefinition stringProperty = new PropertyDefinition();
        stringProperty.setName("aStringProperty");
        stringProperty.setType(ToscaPropertyType.STRING.getType());
        propertyDefinitionList.add(stringProperty);
        dataTypeDefinition.setProperties(propertyDefinitionList);
        final String myDataType = "com.data.type.my";
        dataTypeDefinition.setType(myDataType);
        dataTypes.put(myDataType, dataTypeDefinition);

        final AttributeConverter attributeConverter = new AttributeConverter(dataTypes);
        final AttributeDefinition attributeDefinition = new AttributeDefinition();
        attributeDefinition.setType(myDataType);
        attributeDefinition.setDescription("aDescription");
        attributeDefinition.setStatus("aStatus");
        final Map<String, Object> defaultValueMap = new HashMap<>();
        defaultValueMap.put("aStringProperty", "aStringPropertyValue");
        defaultValueMap.put("anIntegerProperty", 0);
        final String defaultValue = parseToJsonString(defaultValueMap);
        attributeDefinition.setDefaultValue(defaultValue);
        //when
        final ToscaAttribute actualToscaAttribute = attributeConverter.convert(attributeDefinition);
        //then
        assertAttribute(attributeDefinition, defaultValueMap, actualToscaAttribute);
    }

    @Test
    void testInvalidDefaultValueJsonConversion() {
        //given
        final AttributeConverter attributeConverter = createTestSubject();
        final AttributeDefinition attributeDefinition = new AttributeDefinition();
        attributeDefinition.setDefaultValue(": thisIsAnInvalidJson");
        //when, then
        final ToscaAttribute attribute = attributeConverter.convert(attributeDefinition);
        assertNotNull(attribute);
        assertNull(attribute.getDefault());
    }

    @Test
    void testConvertAndAddDefaultValue() throws ToscaConversionException {
        final AttributeConverter converter = createTestSubject();
        final AttributeDefinition attribute = new AttributeDefinition();
        attribute.setName("attrib");
        attribute.setDefaultValue("default");
        final Map<String, Object> attribs = new HashMap<>();
        converter.convertAndAddValue(attribs, attribute);
        assertEquals(0, attribs.size());
    }

    @Test
    void testConvertAndAddValue() throws ToscaConversionException {
        final AttributeConverter converter = createTestSubject();
        final AttributeDefinition attribute = new AttributeDefinition();
        attribute.setName("attrib");
        attribute.setValue("value");
        final Map<String, Object> attribs = new HashMap<>();
        converter.convertAndAddValue(attribs, attribute);
        assertEquals(1, attribs.size());
        assertEquals("value", attribs.get("attrib"));
    }

    private AttributeConverter createTestSubject() {
        return new AttributeConverter(Collections.emptyMap());
    }

    private void assertAttribute(final AttributeDefinition expectedAttributeDefinition, final ToscaAttribute actualToscaAttribute) {
        assertAttribute(expectedAttributeDefinition, expectedAttributeDefinition.get_default(), actualToscaAttribute);
    }

    private void assertAttribute(final AttributeDefinition expectedAttributeDefinition, final Object expectedDefault,
                                 final ToscaAttribute actualToscaAttribute) {
        assertEquals(expectedAttributeDefinition.getType(), actualToscaAttribute.getType());
        assertEquals(expectedAttributeDefinition.getDescription(), actualToscaAttribute.getDescription());
        assertEquals(expectedAttributeDefinition.getStatus(), actualToscaAttribute.getStatus());
        if (expectedAttributeDefinition.getSchema() == null) {
            assertNull(actualToscaAttribute.getEntrySchema());
        } else {
            assertEquals(expectedAttributeDefinition.getSchema().getProperty().getType(),
                actualToscaAttribute.getEntrySchema().getType());
            assertEquals(
                expectedAttributeDefinition.getSchema().getProperty().getDescription(),
                actualToscaAttribute.getEntrySchema().getDescription());
        }
        assertEquals(expectedDefault, actualToscaAttribute.getDefault());
    }

    private String parseToJsonString(final Object value) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(value);
    }

}
