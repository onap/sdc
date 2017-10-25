package org.openecomp.sdc.be.model.tosca.converters;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;

import javax.json.Json;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DataTypePropertyConverterTest {

    private static final String EMPTY_JSON_STR = "{}";
    public static final String PROPERTY2_DEFAULT = "{\"prop1\":\"def1\",\"prop3\":\"def3\"}";
    private DataTypePropertyConverter testInstance = DataTypePropertyConverter.getInstance();
    private Map<String, DataTypeDefinition> dataTypes;
    private DataTypeDefinition noDefaultValue, dataType1, dataType2, dataType3;
    private PropertyDefinition prop1, prop2, prop3, noDefaultProp;

    @Before
    public void setUp() throws Exception {
        dataTypes = new HashMap<>();

        prop1 = new PropertyDefinition();
        prop1.setDefaultValue("def1");
        prop1.setName("prop1");

        prop2 = new PropertyDefinition();
        prop2.setType("dataType1");
        prop2.setName("prop2");

        prop3 = new PropertyDefinition();
        prop3.setDefaultValue("def3");
        prop3.setName("prop3");

        noDefaultProp = new PropertyDefinition();
        noDefaultProp.setName("noDefaultProp");

        noDefaultValue = new DataTypeDefinition();
        noDefaultValue.setProperties(Collections.singletonList(noDefaultProp));

        dataType1 = new DataTypeDefinition();
        dataType1.setProperties(Arrays.asList(prop1, prop3));

        dataType2 = new DataTypeDefinition();
        dataType2.setDerivedFrom(dataType1);

        dataType3 = new DataTypeDefinition();
        dataType3.setProperties(Collections.singletonList(prop2));
        dataType3.setDerivedFrom(noDefaultValue);

        dataTypes.put("noDefault", noDefaultValue);
        dataTypes.put("dataType1", dataType1);
        dataTypes.put("dataType2", dataType2);
        dataTypes.put("dataType3", dataType3);
    }

    @Test
    public void testGetPropertyDefaultValuesRec_dataTypeNotExist() throws Exception {
        String defaultValue = testInstance.getDataTypePropertiesDefaultValuesRec("someType", dataTypes);
        assertNull(defaultValue);
    }

    @Test
    public void testGetPropertyDefaultValuesRec_NoDefaultValue() throws Exception {
        String defaultValue = testInstance.getDataTypePropertiesDefaultValuesRec("noDefault", dataTypes);
        assertNull(defaultValue);
    }

    @Test
    public void testGetPropertyDefaultValuesRec() throws Exception {
        String defaultValue = testInstance.getDataTypePropertiesDefaultValuesRec("dataType1", dataTypes);
        assertEquals(PROPERTY2_DEFAULT, defaultValue);
    }

    @Test
    public void testGetPropertyDefaultValuesRec_defaultFromDerivedDataType_derivedDataTypeHasNoDefaults() throws Exception {
        dataType2.setDerivedFrom(noDefaultValue);
        String defaultValue = testInstance.getDataTypePropertiesDefaultValuesRec("dataType2", dataTypes);
        assertNull(defaultValue);
    }

    @Test
    public void testGetPropertyDefaultValuesRec_defaultFromDerivedDataType() throws Exception {
        String defaultValue = testInstance.getDataTypePropertiesDefaultValuesRec("dataType2", dataTypes);
        assertEquals(PROPERTY2_DEFAULT, defaultValue);
    }

    @Test
    public void testGetPropertyDefaultValuesRec_defaultFromDataTypesOfProperties_dataTypeOfPropertyHasNoDefault() throws Exception {
        dataType3.getProperties().get(0).setType(noDefaultValue.getName());
        String defaultValue = testInstance.getDataTypePropertiesDefaultValuesRec("dataType3", dataTypes);
        assertNull(defaultValue);
    }

    @Test
    public void testGetPropertyDefaultValuesRec_defaultFromDataTypesOfProperties() throws Exception {
        String defaultValue = testInstance.getDataTypePropertiesDefaultValuesRec("dataType3", dataTypes);
        assertEquals("{\"prop2\":" + PROPERTY2_DEFAULT + "}", defaultValue);//data type 3 has property prop2 which has a data type with property prop1 which has a default value
    }

    @Test
    public void testMergeDefaultValues_allDefaultValuesAreOverridden() throws Exception {
        JsonObject value = new JsonObject();
        value.addProperty(noDefaultProp.getName(), "override1");

        JsonObject prop1Val = new JsonObject();
        prop1Val.addProperty(prop1.getName(), "prop1Override");

        JsonObject prop3Val = new JsonObject();
        prop3Val.addProperty(prop3.getName(), "prop3Override");

        JsonObject prop2Value = new JsonObject();
        prop2Value.add(prop3.getName(), prop3Val);
        prop2Value.add(prop1.getName(), prop1Val);

        value.add(prop2.getName(), prop2Value);

        String valBeforeMerge = value.toString();

        testInstance.mergeDataTypeDefaultValuesWithPropertyValue(value, "dataType3", dataTypes);
        assertEquals(valBeforeMerge, value.toString());
    }

    @Test
    public void testMergeDefaultValues() throws Exception {
        JsonObject value = new JsonObject();
        value.addProperty(noDefaultProp.getName(), "override1");

        JsonObject prop1Val = new JsonObject();
        prop1Val.addProperty(prop1.getName(), "prop1Override");

        value.add(prop2.getName(), prop1Val);

        testInstance.mergeDataTypeDefaultValuesWithPropertyValue(value, "dataType3", dataTypes);

        assertEquals("{\"noDefaultProp\":\"override1\",\"prop2\":{\"prop1\":\"prop1Override\",\"prop3\":\"def3\"}}",
                      value.toString());//expect to merge prop 3 default as it was not overridden
    }

    @Test
    public void testMergeDefaultValues_mergeAll() throws Exception {
        JsonObject value = new JsonObject();
        testInstance.mergeDataTypeDefaultValuesWithPropertyValue(value, "dataType3", dataTypes);

        assertEquals("{\"prop2\":" + PROPERTY2_DEFAULT + "}",
                     value.toString());//expect to merge prop 3 default as it was not overridden
    }

    @Test
    public void testMergeDefaultValues_doNotAddDefaultsForGetInputValues() throws Exception {

        JsonObject getInputValue = new JsonObject();
        getInputValue.addProperty("get_input", "in1");

        JsonObject value = new JsonObject();
        value.add(prop2.getName(), getInputValue);

        testInstance.mergeDataTypeDefaultValuesWithPropertyValue(value, "dataType3", dataTypes);

        assertEquals("{\"prop2\":{\"get_input\":\"in1\"}}", value.toString());
    }

    @Test
    public void testMergeDefaultValues_doNotAddDefaultsForGetInputInnerValues() throws Exception {
        JsonObject getInputValue = new JsonObject();
        getInputValue.addProperty("get_input", "in1");

        JsonObject prop1Val = new JsonObject();
        prop1Val.add(prop1.getName(), getInputValue);

        JsonObject value = new JsonObject();
        value.add(prop2.getName(), prop1Val);

        testInstance.mergeDataTypeDefaultValuesWithPropertyValue(value, "dataType3", dataTypes);

        assertEquals("{\"prop2\":{\"prop1\":{\"get_input\":\"in1\"},\"prop3\":\"def3\"}}", value.toString());

    }
}
