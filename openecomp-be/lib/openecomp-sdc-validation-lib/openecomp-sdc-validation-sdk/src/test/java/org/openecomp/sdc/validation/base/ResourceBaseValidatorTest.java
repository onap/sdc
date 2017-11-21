package org.openecomp.sdc.validation.base;

import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

@Test
public class ResourceBaseValidatorTest {

ResourceBaseValidator resourceBaseValidator=new ResourceBaseValidator();

  @Test
  public void testInvalidResourceType(){
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(
        resourceBaseValidator, "/InvalidResourceType");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [RBV1]: A resource has an invalid or unsupported type - null, " +
            "Resource ID [FSB2]");
  }

  @Test
  public void testInvalidHeatStructure(){
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(resourceBaseValidator,
        "/InvalidHeatStructure");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [RBV2]: Invalid HEAT format problem - [while scanning for the next " +
        "token\n" + "found character '\\t(TAB)' that cannot start any token. " +
        "(Do not use \\t(TAB) for indentation)\n" +
        " in 'reader', line 10, column 1:\n" +
        "    \t\t\tresources:\n" +
        "    ^\n" +
        "]");
  }
}
