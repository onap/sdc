package org.openecomp.sdc.validation.impl.validators;

import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class HeatResourceValidatorTest {

  Validator validator=new HeatResourceValidator();

  @Test
  public void testParseException(){
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/guideLineValidator/baseHeatDoesNotExposeNetwork/parseException/");
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [RBV2]: Invalid HEAT format problem - [while scanning for the next token\n" +
            "found character '\\t(TAB)' that cannot start any token. (Do not use \\t(TAB) for indentation)\n" +
            " in 'reader', line 5, column 1:\n" +
            "    \t\t\tresources:\n" +
            "    ^\n" +
            "]");
  }
}
