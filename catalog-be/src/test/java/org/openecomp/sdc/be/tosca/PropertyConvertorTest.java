package org.openecomp.sdc.be.tosca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;

import fj.data.Either;

public class PropertyConvertorTest {
    private PropertyDefinition property;
    Map<String, DataTypeDefinition> dataTypes;

    @Before
    public void setUp(){
        property = new PropertyDefinition();
        property.setName("myProperty");
        property.setType(ToscaPropertyType.INTEGER.getType());
        dataTypes = new HashMap<String, DataTypeDefinition>();
        dataTypes.put(property.getName(), new DataTypeDefinition());
    }


    @Test
    public void convertPropertyWhenValueAndDefaultNull() {
        ToscaProperty prop = PropertyConvertor.getInstance().convertProperty(dataTypes, property, false);
        assertNotNull(prop);
        assertNull(prop.getDefaultp());
    }

    @Test
    public void convertPropertyWhenValueNullAndDefaultNotEmpty() {
        final String def = "1";
        property.setDefaultValue(def);
        ToscaProperty result = PropertyConvertor.getInstance().convertProperty(dataTypes, property, false);
        assertNotNull(result);
        assertEquals(Integer.valueOf(def).intValue(), result.getDefaultp());
    }

    @Test
    public void convertPropertiesWhenValueAndDefaultNullInOne() {
        PropertyDefinition property1 = new PropertyDefinition();
        property1.setName("otherProperty");
        property1.setType(ToscaPropertyType.INTEGER.getType());
        property1.setDefaultValue("2");
        dataTypes.put(property1.getName(), new DataTypeDefinition());
        Resource resource = new Resource();
        List<PropertyDefinition> properties = new ArrayList<PropertyDefinition>();
        properties.add(property);
        properties.add(property1);
        resource.setProperties(properties);
        Either<ToscaNodeType, ToscaError> result = PropertyConvertor.getInstance().convertProperties(resource, new ToscaNodeType(), dataTypes);
        assertTrue(result.isLeft());
        assertEquals(2, result.left().value().getProperties().size());
        int cnt = 0;
        for (Iterator<ToscaProperty> it = result.left().value().getProperties().values().iterator(); it.hasNext(); ) {
            ToscaProperty prop = it.next();
            if (prop.getDefaultp() == null) {
                cnt++;
            }
        }
        assertEquals(1, cnt);
    }

    @Test
    public void convertPropertiesWhenValueAndDefaultExist() {
        PropertyDefinition property1 = new PropertyDefinition();
        property1.setName("otherProperty");
        property1.setType(ToscaPropertyType.INTEGER.getType());
        property1.setDefaultValue("2");
        property.setDefaultValue("1");
        dataTypes.put(property1.getName(), new DataTypeDefinition());
        Resource resource = new Resource();
        List<PropertyDefinition> properties = new ArrayList<PropertyDefinition>();
        properties.add(property);
        properties.add(property1);
        resource.setProperties(properties);
        Either<ToscaNodeType, ToscaError> result = PropertyConvertor.getInstance().convertProperties(resource, new ToscaNodeType(), dataTypes);
        assertTrue(result.isLeft());
        assertEquals(2, result.left().value().getProperties().size());
        for (Iterator<ToscaProperty> it = result.left().value().getProperties().values().iterator(); it.hasNext(); ) {
            ToscaProperty prop = it.next();
            assertNotNull(prop.getDefaultp());
        }
    }

    @Test
    public void convertPropertiesWhenValueAndDefaultNullInAll() {
        PropertyDefinition property1 = new PropertyDefinition();
        property1.setName("otherProperty");
        property1.setType(ToscaPropertyType.INTEGER.getType());
        dataTypes.put(property1.getName(), new DataTypeDefinition());
        Resource resource = new Resource();
        List<PropertyDefinition> properties = new ArrayList<PropertyDefinition>();
        properties.add(property);
        properties.add(property1);
        resource.setProperties(properties);
        Either<ToscaNodeType, ToscaError> result = PropertyConvertor.getInstance().convertProperties(resource, new ToscaNodeType(), dataTypes);
        assertTrue(result.isLeft());
        assertEquals(2, result.left().value().getProperties().size());
        for (Iterator<ToscaProperty> it = result.left().value().getProperties().values().iterator(); it.hasNext(); ) {
            ToscaProperty prop = it.next();
            assertNull(prop.getDefaultp());
        }
     }
}
