/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.core.utilities.json;


import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openecomp.core.utilities.file.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JsonUtilTest {

    @Test
    public void testValidJsonValidate() {


        String json = FileUtils.readViaInputStream("jsonUtil/json/a.json",
                stream -> new String(FileUtils.toByteArray(stream)));

        String jsonSchema = FileUtils.readViaInputStream("jsonUtil/json_schema/aSchema.json",
                stream -> new String(FileUtils.toByteArray(stream)));

        List<String> validationErrors = JsonUtil.validate(json, jsonSchema);
        Assert.assertNull(validationErrors);
    }

    @Test
    public void testInValidJsonValidate() {

        String json = FileUtils.readViaInputStream("jsonUtil/json/a_invalid.json",
                stream -> new String(FileUtils.toByteArray(stream)));
        String jsonSchema = FileUtils.readViaInputStream("jsonUtil/json_schema/aSchema.json",
                stream -> new String(FileUtils.toByteArray(stream)));

        List<String> validationErrors = JsonUtil.validate(json, jsonSchema);
        Assert.assertNotNull(validationErrors);
        Assert.assertEquals(validationErrors.size(), 5);
        Assert.assertEquals(validationErrors.get(0),
                "#/cityOfBirth: Paris is not a valid value. Possible values: New York,Tel Aviv,London");
        Assert.assertEquals(validationErrors.get(1),
                "#/address: {\"streetAddress\":\"21 2nd Street\",\"city\":\"Paris\"} is not a valid value. {\"streetAddress\":\"21 2nd Street\",\"city\":\"New York\"} is the only possible value for this field");
        Assert.assertEquals(validationErrors.get(2),
                "#/phoneNumber/0/code: expected type: Number, found: String");
        Assert.assertEquals(validationErrors.get(3),
                "#/gender: expected type: String, found: Integer");
        Assert.assertEquals(validationErrors.get(4), "#/dateOfBirth: [20101988] is not valid value. "
                + "It does not match pattern (0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInValidJsonValidateNullJson() {
        JsonUtil.validate(null, null);
    }

    @Test
    public void testObject2Json() {
        List<String> list = Stream.of("Json", "Util", "Test").collect(Collectors.toList());

        String resultStr = JsonUtil.object2Json(list);
        Assert.assertNotNull(resultStr);
        Assert.assertTrue(resultStr.contains("Json") && resultStr.contains("Util"));
    }

    @Test
    public void testSbObject2Json() {
        List<String> list = Stream.of("Json", "Util", "Test").collect(Collectors.toList());

        StringBuilder resultStr = JsonUtil.sbObject2Json(list);
        Assert.assertNotNull(resultStr);
        Assert.assertTrue(resultStr.toString().contains("Json")
                && resultStr.toString().contains("Util"));
    }

    @Test
    public void testJson2Object() {
        String inputStr = "[Json, Util, Test]";
        List list = JsonUtil.json2Object(inputStr, ArrayList.class);
        Assert.assertNotNull(list);
        Assert.assertEquals(list.size(), 3);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testJson2ObjectIncorrectJson() {
        String inputStr = "{[Json, Util, Test]}";
        List list = JsonUtil.json2Object(inputStr, ArrayList.class);
        Assert.assertNull(list);
    }

    @Test
    public void testJson2ObjectInputStream() {
        String inputStr = "[Json, Util, Test]";
        List list = JsonUtil.json2Object(new ByteArrayInputStream(inputStr.getBytes()), ArrayList.class);
        Assert.assertNotNull(list);
        Assert.assertEquals(list.size(), 3);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testJson2ObjectIncorrectJsonInputStream() {
        String inputStr = "{[Json, Util, Test]}";
        List list = JsonUtil.json2Object(new ByteArrayInputStream(inputStr.getBytes()), ArrayList.class);
        Assert.assertNull(list);
    }

    @Test
    public void testIsValidJson() {
        String inputStr = "{\n"
                + "\t\"obj\": [\"Json\", \"Util\", \"Test\"]\n"
                + "}";
        Assert.assertTrue(JsonUtil.isValidJson(inputStr));
    }
    @Test
    public void testIsValidJsonFail() {
        String inputStr = "{[Json, Util, Test]}";
        Assert.assertFalse(JsonUtil.isValidJson(inputStr));
    }

}
