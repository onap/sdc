package org.openecomp.core.utilities.json;

import org.openecomp.core.utilities.file.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JsonSchemaDataGeneratorTest {

  public static final String SCHEMA_WITHOUT_DEFAULTS = new String(
      FileUtils.toByteArray(FileUtils.getFileInputStream("jsonUtil/json_schema/aSchema.json")));
  public static final String SCHEMA_WITH_REFS_AND_DEFAULTS = new String(FileUtils.toByteArray(
      FileUtils.getFileInputStream("jsonUtil/json_schema/schemaWithRefsAndDefaults.json")));
  public static final String SCHEMA_WITH_INVALID_DEFAULT = new String(FileUtils.toByteArray(
      FileUtils.getFileInputStream("jsonUtil/json_schema/schemaWithInvalidDefault.json")));
  public static final String SCHEMA_NIC = new String(
      FileUtils.toByteArray(FileUtils.getFileInputStream("jsonUtil/json_schema/nicSchema.json")));

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
}