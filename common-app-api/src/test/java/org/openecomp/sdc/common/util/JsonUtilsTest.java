/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.openecomp.sdc.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.junit.Test;

public class JsonUtilsTest {

    private String testProperty01 = "testProperty01";
    private String testValue01 = "testValue01";
    private String testProperty02 = "testProperty02";
    private String testValue02 = "testValue02";
    private String expectedJsonObject =
        "" + "{" + "\"" + testProperty01 + "\":\"" + testValue01 + "\"," + "\"" + testProperty02 + "\":\"" + testValue02 + "\"" + "}";

    @Test
    public void validateToStringConvertsJsonObjectToValidString() {
        String result = JsonUtils.toString(generateJsonObject());
        assertEquals(result, expectedJsonObject);
    }

    @Test
    public void validateToStringReturnsNullIfJsonElementIsNull() {
        String result = JsonUtils.toString(null);
        assertNull(result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void validateToStringFromJsonArrayThrowsUnsupportedOperationException() {
        JsonUtils.toString(generateJsonArray());
    }

    @Test
    public void validateToStringReturnsNullIfJsonElementIsJsonNull() {
        String result = JsonUtils.toString(new JsonNull());
        assertNull(result);
    }

    @Test
    public void validateContainsEntryReturnsTrueIfKeyIsPresentInJsonObject() {
        boolean result = JsonUtils.containsEntry(generateJsonObject(), testProperty01);
        assertTrue(result);
    }

    @Test
    public void validateContainsEntryReturnsFalseIfKeyIsNotPresentInJsonObject() {
        boolean result = JsonUtils.containsEntry(new JsonObject(), testProperty01);
        assertFalse(result);
    }

    @Test
    public void validateIsEmptyJsonReturnsTrueIfInputIsEmpty() {
        boolean result = JsonUtils.isEmptyJson(new JsonObject());
        assertTrue(result);
    }

    @Test
    public void validateIsEmptyJsonReturnsFalseIfInputIsNotEmpty() {
        boolean result = JsonUtils.isEmptyJson(generateJsonArray().get(0));
        assertFalse(result);
    }

    @Test
    public void validateIsNullOrEmptyReturnsTrueIfInputIsEmpty() {
        boolean result = JsonUtils.isJsonNullOrEmpty(new JsonObject());
        assertTrue(result);
    }

    @Test
    public void validateIsNullOrEmptyReturnsFalseIfInputIsNotNull() {
        boolean result = JsonUtils.isJsonNullOrEmpty(generateJsonObject());
        assertFalse(result);
    }

    private JsonObject generateJsonObject() {
        final JsonObject testObject = new JsonObject();
        testObject.addProperty(testProperty01, testValue01);
        testObject.addProperty(testProperty02, testValue02);
        return testObject;
    }

    private JsonArray generateJsonArray() {
        final JsonArray testArray = new JsonArray();
        testArray.add(generateJsonObject());
        return testArray;
    }
}
