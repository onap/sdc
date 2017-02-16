package org.openecomp.core.utilities.json;


import org.openecomp.core.utilities.file.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class JsonUtilTest {

  @Test
  public void testValidJsonValidate() throws Exception {
    String json =
        new String(FileUtils.toByteArray(FileUtils.getFileInputStream("jsonUtil/json/a.json")));
    String jsonSchema = new String(
        FileUtils.toByteArray(FileUtils.getFileInputStream("jsonUtil/json_schema/aSchema.json")));

    List<String> validationErrors = JsonUtil.validate(json, jsonSchema);
    Assert.assertNull(validationErrors);
  }

  @Test
  public void testInValidJsonValidate() throws Exception {
    String json = new String(
        FileUtils.toByteArray(FileUtils.getFileInputStream("jsonUtil/json/a_invalid.json")));
    String jsonSchema = new String(
        FileUtils.toByteArray(FileUtils.getFileInputStream("jsonUtil/json_schema/aSchema.json")));

    List<String> validationErrors = JsonUtil.validate(json, jsonSchema);
    Assert.assertNotNull(validationErrors);
    Assert.assertEquals(validationErrors.size(), 3);
    Assert.assertEquals(validationErrors.get(0),
        "#/cityOfBirth: Paris is not a valid value. Possible values: New York,Tel Aviv,London");
    Assert.assertEquals(validationErrors.get(1),
        "#/address: {\"streetAddress\":\"21 2nd Street\",\"city\":\"Paris\"} is not a valid value. {\"streetAddress\":\"21 2nd Street\",\"city\":\"New York\"} is the only possible value for this field");
    Assert.assertEquals(validationErrors.get(2),
        "#/phoneNumber/0/code: expected type: Number, found: String");
  }
}