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

package org.openecomp.sdc.be.model.tosca.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;


class DataTypePropertyConverterTest {

    private static final DataTypePropertyConverter dataTypePropertyConverter = DataTypePropertyConverter.getInstance();
    private static final String PROPERTY2_DEFAULT = "{\"prop1\":\"def1\",\"prop3\":\"def3\"}";

    private Map<String, DataTypeDefinition> dataTypes;
    private DataTypeDefinition noDefaultValue;
    private DataTypeDefinition dataType2;
    private DataTypeDefinition dataType3;
    private PropertyDefinition prop1;
    private PropertyDefinition prop2;
    private PropertyDefinition prop3;
    private PropertyDefinition noDefaultProp;

    @BeforeEach
    void setUp() {
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

        DataTypeDefinition dataType1 = new DataTypeDefinition();
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
    void testGetPropertyDefaultValuesRec_dataTypeNotExist() {
        String defaultValue = dataTypePropertyConverter.getDataTypePropertiesDefaultValuesRec("someType", dataTypes);
        assertNull(defaultValue);
    }

    @Test
    void testGetPropertyDefaultValuesRec_NoDefaultValue() {
        String defaultValue = dataTypePropertyConverter.getDataTypePropertiesDefaultValuesRec("noDefault", dataTypes);
        assertNull(defaultValue);
    }

    @Test
    void testGetPropertyDefaultValuesRec() {
        String defaultValue = dataTypePropertyConverter.getDataTypePropertiesDefaultValuesRec("dataType1", dataTypes);
        assertEquals(PROPERTY2_DEFAULT, defaultValue);
    }

    @Test
    void testGetPropertyDefaultValuesRec_defaultFromDerivedDataType_derivedDataTypeHasNoDefaults() {
        dataType2.setDerivedFrom(noDefaultValue);
        String defaultValue = dataTypePropertyConverter.getDataTypePropertiesDefaultValuesRec("dataType2", dataTypes);
        assertNull(defaultValue);
    }

    @Test
    void testGetPropertyDefaultValuesRec_defaultFromDerivedDataType() {
        String defaultValue = dataTypePropertyConverter.getDataTypePropertiesDefaultValuesRec("dataType2", dataTypes);
        assertEquals(PROPERTY2_DEFAULT, defaultValue);
    }

    @Test
    void testGetPropertyDefaultValuesRec_defaultFromDataTypesOfProperties_dataTypeOfPropertyHasNoDefault() {
        dataType3.getProperties().get(0).setType(noDefaultValue.getName());
        String defaultValue = dataTypePropertyConverter.getDataTypePropertiesDefaultValuesRec("dataType3", dataTypes);
        assertNull(defaultValue);
    }

    @Test
    void testGetPropertyDefaultValuesRec_defaultFromDataTypesOfProperties() {
        String defaultValue = dataTypePropertyConverter.getDataTypePropertiesDefaultValuesRec("dataType3", dataTypes);
        assertEquals("{\"prop2\":" + PROPERTY2_DEFAULT + "}", defaultValue);//data type 3 has property prop2 which has a data type with property prop1 which has a default value
    }

    @Test
    void testMergeDefaultValues_allDefaultValuesAreOverridden() {
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

        dataTypePropertyConverter.mergeDataTypeDefaultValuesWithPropertyValue(value, "dataType3", dataTypes);
        assertEquals(valBeforeMerge, value.toString());
    }

    @Test
    void testMergeDefaultValues() {
        JsonObject value = new JsonObject();
        value.addProperty(noDefaultProp.getName(), "override1");

        JsonObject prop1Val = new JsonObject();
        prop1Val.addProperty(prop1.getName(), "prop1Override");

        value.add(prop2.getName(), prop1Val);

        dataTypePropertyConverter.mergeDataTypeDefaultValuesWithPropertyValue(value, "dataType3", dataTypes);

        assertEquals("{\"noDefaultProp\":\"override1\",\"prop2\":{\"prop1\":\"prop1Override\",\"prop3\":\"def3\"}}",
                      value.toString());//expect to merge prop 3 default as it was not overridden
    }

    @Test
    void testMergeDefaultValues_mergeAll() {
        JsonObject value = new JsonObject();
        dataTypePropertyConverter.mergeDataTypeDefaultValuesWithPropertyValue(value, "dataType3", dataTypes);

        assertEquals("{\"prop2\":" + PROPERTY2_DEFAULT + "}",
                     value.toString());//expect to merge prop 3 default as it was not overridden
    }

    @Test
    void testMergeDefaultValues_doNotAddDefaultsForGetInputValues() {

        JsonObject getInputValue = new JsonObject();
        getInputValue.addProperty("get_input", "in1");

        JsonObject value = new JsonObject();
        value.add(prop2.getName(), getInputValue);

        dataTypePropertyConverter.mergeDataTypeDefaultValuesWithPropertyValue(value, "dataType3", dataTypes);

        assertEquals("{\"prop2\":{\"get_input\":\"in1\"}}", value.toString());
    }

    @Test
    void testMergeDefaultValues_doNotAddDefaultsForGetInputInnerValues() {
        JsonObject getInputValue = new JsonObject();
        getInputValue.addProperty("get_input", "in1");

        JsonObject prop1Val = new JsonObject();
        prop1Val.add(prop1.getName(), getInputValue);

        JsonObject value = new JsonObject();
        value.add(prop2.getName(), prop1Val);

        dataTypePropertyConverter.mergeDataTypeDefaultValuesWithPropertyValue(value, "dataType3", dataTypes);

        assertEquals("{\"prop2\":{\"prop1\":{\"get_input\":\"in1\"},\"prop3\":\"def3\"}}", value.toString());

    }
}
