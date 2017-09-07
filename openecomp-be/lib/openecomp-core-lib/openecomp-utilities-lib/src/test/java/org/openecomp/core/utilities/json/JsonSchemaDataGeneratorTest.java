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

import org.json.JSONException;
import org.json.JSONObject;
import org.openecomp.core.utilities.file.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JsonSchemaDataGeneratorTest {

  public static final String SCHEMA_WITHOUT_DEFAULTS =
          readFromFile("jsonUtil/json_schema/aSchema.json");

  public static final String SCHEMA_WITH_REFS_AND_DEFAULTS =
          readFromFile("jsonUtil/json_schema/schemaWithRefsAndDefaults.json");

  public static final String SCHEMA_WITH_INVALID_DEFAULT =
          readFromFile("jsonUtil/json_schema/schemaWithInvalidDefault.json");

  public static final String SCHEMA_NIC =
          readFromFile("jsonUtil/json_schema/nicSchema.json");

  @Test
  public void testSchemaWithoutDefaults() {
    testGenerate(SCHEMA_WITHOUT_DEFAULTS, new JSONObject());
  }

  @Test
  public void testSchemaWithRefsAndDefaults() {
    testGenerate(SCHEMA_WITH_REFS_AND_DEFAULTS,
        new JSONObject(
            "{\"cityOfBirth\":\"Tel Aviv\",\"address\":{\"city\":\"Tel Aviv\"},\"phoneNumber\":[{\"code\":1,\"location\":\"Home\"},{\"code\":2,\"location\":\"Office\"}]}"));
  }

  @Test(expectedExceptions = JSONException.class)
  public void testSchemaWithInvalidDefault() {
    testGenerate(SCHEMA_WITH_INVALID_DEFAULT, null);
  }

  @Test
  public void testNicQuestionnaireSchema() {
    testGenerate(SCHEMA_NIC,
        new JSONObject("{\"ipConfiguration\":{\"ipv4Required\":true,\"ipv6Required\":false}}"));
  }

  private void testGenerate(String schema, JSONObject expectedData) {
    JsonSchemaDataGenerator jsonSchemaDataGenerator = new JsonSchemaDataGenerator(schema);
    String data = jsonSchemaDataGenerator.generateData();
    System.out.println(data);
    JSONObject dataJson = new JSONObject(data);
    Assert.assertTrue(expectedData.similar(dataJson));
  }

  private static String readFromFile(String filename) {
    return FileUtils.readViaInputStream(filename, stream -> new String(FileUtils.toByteArray(stream)));
  }
}
