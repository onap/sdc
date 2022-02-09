/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;


class ToscaValueBaseConverterTest {

    private final ToscaValueBaseConverter converter = new ToscaValueBaseConverter();

    @Test
    void testJson2JavaPrimitive() {
        JsonPrimitive prim1 = new JsonPrimitive(Boolean.FALSE);
        Object res1 = converter.json2JavaPrimitive(prim1);
        assertFalse((Boolean)res1);

        JsonPrimitive prim2 = new JsonPrimitive("Test");
        Object res2 = converter.json2JavaPrimitive(prim2);
        assertEquals("Test", res2);

        JsonPrimitive prim3 = new JsonPrimitive(3);
        Object res3 = converter.json2JavaPrimitive(prim3);
        assertEquals(3, (int) (Integer) res3);

        JsonPrimitive prim4 = new JsonPrimitive(3.6);
        Object res4 = converter.json2JavaPrimitive(prim4);
        assertEquals(3.6, (Double) res4);
    }

    @Test
    void testIsEmptyObjectValue() {
        boolean res1 = ToscaValueBaseConverter.isEmptyObjectValue(null);
        assertTrue(res1);

        boolean res2 = ToscaValueBaseConverter.isEmptyObjectValue("");
        assertTrue(res2);

        boolean res3 = ToscaValueBaseConverter.isEmptyObjectValue(new HashMap<>());
        assertTrue(res3);

        boolean res4 = ToscaValueBaseConverter.isEmptyObjectValue(new LinkedList<>());
        assertTrue(res4);

        boolean res5 = ToscaValueBaseConverter.isEmptyObjectValue("test");
        assertFalse(res5);
    }

    @Test
    void testHandleComplexJsonValue() {
        JsonObject innerObject = new JsonObject();
        innerObject.addProperty("string", "stringValue");
        innerObject.addProperty("int", 1);
        innerObject.addProperty("float", 1.1);
        innerObject.add("null", null);

        JsonArray jsonArray1 = new JsonArray();
        jsonArray1.add(innerObject);
        jsonArray1.add(innerObject);
        jsonArray1.add(innerObject);

        JsonArray jsonArray2 = new JsonArray();
        jsonArray2.add("value0");
        jsonArray2.add("value1");
        jsonArray2.add("value2");

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("objectProperty", innerObject);
        jsonObject.add("arrayProperty1", jsonArray1);
        jsonObject.add("arrayProperty2", jsonArray2);
        jsonObject.addProperty("stringProperty", "stringPropertyValue");

        final Object resultingObject = converter.handleComplexJsonValue(jsonObject);
        assertTrue(resultingObject instanceof Map);
        final Map<String, Object> jsonObjectAsMap = (Map<String, Object>) resultingObject;
        assertEquals(4, jsonObjectAsMap.keySet().size());
        assertTrue(jsonObjectAsMap.containsKey("objectProperty"));
        assertTrue(jsonObjectAsMap.containsKey("arrayProperty1"));
        assertTrue(jsonObjectAsMap.containsKey("arrayProperty2"));
        assertTrue(jsonObjectAsMap.containsKey("stringProperty"));

        final String stringProperty = (String) jsonObjectAsMap.get("stringProperty");
        assertEquals(jsonObject.get("stringProperty").getAsString(), stringProperty);

        final Object arrayProperty1 = jsonObjectAsMap.get("arrayProperty1");
        assertTrue(arrayProperty1 instanceof List);
        final List<Object> arrayProperty1AsList = (List<Object>) arrayProperty1;
        assertEquals(3, arrayProperty1AsList.size());
        for (int i = 0; i < arrayProperty1AsList.size(); i++) {
            final Object actualElement = arrayProperty1AsList.get(i);
            assertTrue(actualElement instanceof Map);
            final Map<String, Object> actualElementAsMap = (Map<String, Object>) actualElement;
            final JsonObject expectedJsonObject = jsonArray1.get(i).getAsJsonObject();
            assertEquals(expectedJsonObject.get("string").getAsString(), actualElementAsMap.get("string"));
            assertEquals(expectedJsonObject.get("int").getAsInt(), actualElementAsMap.get("int"));
            assertEquals(expectedJsonObject.get("float").getAsDouble(), actualElementAsMap.get("float"));
            assertNull(actualElementAsMap.get("null"));
        }

        final Object arrayProperty2 = jsonObjectAsMap.get("arrayProperty2");
        assertTrue(arrayProperty2 instanceof List);
        final List<String> arrayProperty2AsList = (List<String>) arrayProperty2;
        assertEquals(3, arrayProperty2AsList.size());
        for (int i = 0; i < arrayProperty2AsList.size(); i++) {
            assertEquals("value" + i, arrayProperty2AsList.get(i));
        }

        final Object objectProperty = jsonObjectAsMap.get("objectProperty");
        assertTrue(objectProperty instanceof Map);
        final Map<String, Object> objectPropertyAsMap = (Map<String, Object>) objectProperty;
        assertEquals(4, objectPropertyAsMap.keySet().size());
        assertTrue(objectPropertyAsMap.containsKey("string"));
        assertEquals(innerObject.get("string").getAsString(), objectPropertyAsMap.get("string"));
        assertEquals(innerObject.get("int").getAsInt(), objectPropertyAsMap.get("int"));
        assertEquals(innerObject.get("float").getAsDouble(), objectPropertyAsMap.get("float"));
        assertNull(objectPropertyAsMap.get("null"));
    }
}
