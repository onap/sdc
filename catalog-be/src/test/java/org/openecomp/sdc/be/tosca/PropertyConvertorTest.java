/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.tosca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;

class PropertyConvertorTest {

    private PropertyDefinition property;
    private Map<String, DataTypeDefinition> dataTypes;

    @InjectMocks
    private PropertyConvertor propertyConvertor;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        property = new PropertyDefinition();
        property.setName("myProperty");
        property.setType(ToscaPropertyType.INTEGER.getType());
        dataTypes = new HashMap<>();
        dataTypes.put(property.getName(), new DataTypeDefinition());
    }

    @Test
    void testConvertProperty() {
    	SchemaDefinition schema = new SchemaDefinition();
    	schema.setProperty(property);
    	
    	property.setSchema(schema);
    	
    	propertyConvertor.convertProperty(dataTypes, property, PropertyConvertor.PropertyType.PROPERTY);
    }

    @Test
    void convertPropertyWhenValueAndDefaultNull() {
        ToscaProperty prop = propertyConvertor.convertProperty(dataTypes, property, PropertyConvertor.PropertyType.PROPERTY);
        assertNotNull(prop);
        assertNull(prop.getDefaultp());
    }

    @Test
    void convertPropertyWhenValueNullAndDefaultNotEmpty() {
        final String def = "1";
        property.setDefaultValue(def);
        ToscaProperty result = propertyConvertor.convertProperty(dataTypes, property, PropertyConvertor.PropertyType.PROPERTY);
        assertNotNull(result);
        assertEquals(Integer.valueOf(def), result.getDefaultp());
    }
    
    @Test
    void convertPropertyWithMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "value");
        property.setMetadata(metadata);
        ToscaProperty result = propertyConvertor.convertProperty(dataTypes, property, PropertyConvertor.PropertyType.PROPERTY);
        assertNotNull(result);
        assertEquals(metadata, result.getMetadata());
    }

    @Test
    void convertPropertiesWhenValueAndDefaultNullInOne() {
        PropertyDefinition property1 = new PropertyDefinition();
        property1.setName("otherProperty");
        property1.setType(ToscaPropertyType.INTEGER.getType());
        property1.setDefaultValue("2");
        dataTypes.put(property1.getName(), new DataTypeDefinition());
        Resource resource = new Resource();
        List<PropertyDefinition> properties = new ArrayList<>();
        properties.add(property);
        properties.add(property1);
        resource.setProperties(properties);
        Either<ToscaNodeType, ToscaError> result = propertyConvertor.convertProperties(resource, new ToscaNodeType(), dataTypes);
        assertTrue(result.isLeft());
        assertEquals(2, result.left().value().getProperties().size());
        int cnt = 0;
        for (ToscaProperty prop : result.left().value().getProperties().values()) {
            if (prop.getDefaultp() == null) {
                cnt++;
            }
        }
        assertEquals(1, cnt);
    }

    @Test
    void convertPropertiesWhenValueAndDefaultExist() {
        PropertyDefinition property1 = new PropertyDefinition();
        property1.setName("otherProperty");
        property1.setType(ToscaPropertyType.INTEGER.getType());
        property1.setDefaultValue("2");
        property.setDefaultValue("1");
        dataTypes.put(property1.getName(), new DataTypeDefinition());
        Resource resource = new Resource();
        List<PropertyDefinition> properties = new ArrayList<>();
        properties.add(property);
        properties.add(property1);
        resource.setProperties(properties);
        Either<ToscaNodeType, ToscaError> result = propertyConvertor.convertProperties(resource, new ToscaNodeType(), dataTypes);
        assertTrue(result.isLeft());
        assertEquals(2, result.left().value().getProperties().size());
        for (ToscaProperty prop : result.left().value().getProperties().values()) {
            assertNotNull(prop.getDefaultp());
        }
    }

    @Test
    void convertPropertiesWhenValueAndDefaultNullInAll() {
        PropertyDefinition property1 = new PropertyDefinition();
        property1.setName("otherProperty");
        property1.setType(ToscaPropertyType.INTEGER.getType());
        dataTypes.put(property1.getName(), new DataTypeDefinition());
        Resource resource = new Resource();
        List<PropertyDefinition> properties = new ArrayList<>();
        properties.add(property);
        properties.add(property1);
        resource.setProperties(properties);
        Either<ToscaNodeType, ToscaError> result = propertyConvertor.convertProperties(resource, new ToscaNodeType(), dataTypes);
        assertTrue(result.isLeft());
        assertEquals(2, result.left().value().getProperties().size());
        for (ToscaProperty prop : result.left().value().getProperties().values()) {
            assertNull(prop.getDefaultp());
        }
     }

    @Test
    void convertPropertyWhichStartsWithSemiColon() {
        final PropertyDefinition property = new PropertyDataDefinitionBuilder()
            .setDefaultValue("::")
            .setType(ToscaPropertyType.STRING.getType())
            .build();
        final ToscaProperty toscaProperty =
            propertyConvertor.convertProperty(Collections.emptyMap(), property, PropertyConvertor.PropertyType.PROPERTY);
        assertEquals("::", toscaProperty.getDefaultp());
    }

    @Test
    void convertPropertyWhichStartsWithSlash() {
        final PropertyDefinition property = new PropertyDataDefinitionBuilder()
            .setDefaultValue("/")
            .setType(ToscaPropertyType.STRING.getType())
            .build();
        final ToscaProperty toscaProperty =
            propertyConvertor.convertProperty(Collections.emptyMap(), property, PropertyConvertor.PropertyType.PROPERTY);
        assertEquals("/", toscaProperty.getDefaultp());
    }
    
    @Test
    void convertPropertyWithYamlValue() {
        final PropertyDefinition property = new PropertyDataDefinitionBuilder()
            .setDefaultValue("{concat: [ get_input: service_name, '--', 'WirelessService']}")
            .setType(ToscaPropertyType.STRING.getType())
            .build();
        final ToscaProperty toscaProperty =
            propertyConvertor.convertProperty(Collections.emptyMap(), property, PropertyConvertor.PropertyType.PROPERTY);
        assertTrue(toscaProperty.getDefaultp() instanceof Map);
        assertTrue(((Map)toscaProperty.getDefaultp()).get("concat") instanceof List);
        assertEquals(3, ((List)((Map)toscaProperty.getDefaultp()).get("concat")).size());
    }

}
