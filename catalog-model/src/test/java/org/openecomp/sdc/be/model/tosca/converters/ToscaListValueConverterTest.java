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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.model.tosca.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;

public class ToscaListValueConverterTest {

    private static final String DOUBLE = "double";
    private static final String STRING = "string";
    private static final String TEST_1 = "test1";
    private static final String FLOAT = "float";
    private static final String INTEGER = "integer";

    private ToscaListValueConverter createTestSubject() {
        return ToscaListValueConverter.getInstance();
    }

    @Test
    public void testGetInstance() {
        ToscaListValueConverter result = createTestSubject();
        assertNotNull(result);
    }

    @Test
    public void shouldConvertToNullWhenValueIsNull() {
        ToscaListValueConverter converter = createTestSubject();
        String innerType = "";
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        Object result = converter.convertToToscaValue(null, innerType, dataTypes);
        assertNull(result);
    }

    @Test
    public void shouldConvertToNullWhenValueIsEmpty() {
        ToscaListValueConverter converter = createTestSubject();
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        Object result = converter.convertToToscaValue("", STRING, dataTypes);
        assertNull(result);
    }

    @Test
    public void shouldConvertToNullWhenValueIsInvalid() {
        ToscaListValueConverter converter = createTestSubject();
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        Object result = converter.convertToToscaValue("{ \" not a json \" ", STRING, dataTypes);
        assertNull(result);
    }

    @Test
    public void shouldConvertToNullWithNonStandardScalarType() {
        ToscaListValueConverter converter = createTestSubject();
        String value = "";
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
        dataTypeDefinition.setName(FLOAT);
        dataTypes.put(DOUBLE, dataTypeDefinition);
        Object result = converter.convertToToscaValue(value, DOUBLE, dataTypes);
        assertNull(result);
    }

    @Test
    public void shouldConvertToNullWithNonStandardNonScalarType() {
        ToscaListValueConverter converter = createTestSubject();
        String value = "";
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        dataTypes.put(DOUBLE, new DataTypeDefinition());
        Object result = converter.convertToToscaValue(value, DOUBLE, dataTypes);
        assertNull(result);
    }

    @Test
    public void shouldConvertToValueWithNullDataTypeDefinition() {
        ToscaListValueConverter converter = createTestSubject();
        String value = "VALUE";
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        Object result = converter.convertToToscaValue(value, DOUBLE, dataTypes);
        assertEquals(result, value);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConvertComplexJsonArray() {
        ToscaListValueConverter converter = createTestSubject();
        String value = "{ \"test1\": [1, 2] }";
        Object result = converter.convertToToscaValue(value, INTEGER, new HashMap<>());
        HashMap<String, ArrayList<Integer>> mappedResult = (HashMap<String, ArrayList<Integer>>) result;
        assertEquals(mappedResult.get(TEST_1).get(0), Integer.valueOf(1));
        assertEquals(mappedResult.get(TEST_1).get(1), Integer.valueOf(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConvertSimpleJsonArray() {
        ToscaListValueConverter converter = createTestSubject();
        String value = "[1, 2]";
        Object result = converter.convertToToscaValue(value, INTEGER, new HashMap<>());
        ArrayList<Integer> mappedResult = (ArrayList<Integer>) result;
        assertEquals(mappedResult.get(0), Integer.valueOf(1));
        assertEquals(mappedResult.get(1), Integer.valueOf(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConvertSimpleJsonArrayWithComplexChild() {
        ToscaListValueConverter converter = createTestSubject();
        String value = "[{\"test1\": 1}, {\"test1\": 2}]";
        Object result = converter.convertToToscaValue(value, INTEGER, new HashMap<>());
        ArrayList<HashMap<String, Integer>> mappedResult = (ArrayList<HashMap<String, Integer>>) result;
        assertEquals(mappedResult.get(0).get(TEST_1), Integer.valueOf(1));
        assertEquals(mappedResult.get(1).get(TEST_1), Integer.valueOf(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConvertSimpleJsonArrayWithComplexChildNonScalarTypeAndNullPropertyType() {
        ToscaListValueConverter converter = createTestSubject();
        String value = "[{\"test1\": 1}, {\"test1\": 2}]";
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
        List<PropertyDefinition> props = new ArrayList<>();
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        props.add(propertyDefinition);
        propertyDefinition.setName(TEST_1);
        dataTypeDefinition.setProperties(props);
        dataTypes.put(DOUBLE, dataTypeDefinition);
        Object result = converter.convertToToscaValue(value, DOUBLE, dataTypes);
        ArrayList<HashMap<String, String>> mappedResult = (ArrayList<HashMap<String, String>>) result;
        assertEquals(mappedResult.get(0).get(TEST_1), "1");
        assertEquals(mappedResult.get(1).get(TEST_1), "2");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConvertSimpleJsonArrayWithComplexChildNonScalarTypeAndNonNullPropertyType() {
        ToscaListValueConverter converter = createTestSubject();
        String value = "[{\"test1\": 1}, {\"test1\": 2}]";
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
        List<PropertyDefinition> props = new ArrayList<>();
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        props.add(propertyDefinition);
        propertyDefinition.setName(TEST_1);
        propertyDefinition.setType(FLOAT);
        dataTypeDefinition.setProperties(props);
        dataTypes.put(DOUBLE, dataTypeDefinition);
        Object result = converter.convertToToscaValue(value, DOUBLE, dataTypes);
        ArrayList<HashMap<String, Double>> mappedResult = (ArrayList<HashMap<String, Double>>) result;
        assertEquals(mappedResult.get(0).get(TEST_1), Double.valueOf(1.0));
        assertEquals(mappedResult.get(1).get(TEST_1), Double.valueOf(2.0));
    }

}
