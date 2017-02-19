package org.openecomp.sdc.validation.impl.validators;

import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.errors.Messages;
import org.junit.Test;
import org.testng.Assert;

public class ErrorMessagesTest {

  @Test
  public void testErrorFormatWithOneParam() {
    String error1 = ErrorMessagesFormatBuilder
        .getErrorWithParameters(Messages.MISSING_FILE_NAME_IN_MANIFEST.getErrorMessage(),
            "file.yaml");
    Assert.assertNotNull(error1);
  }

  @Test
  public void testErrorFormatWithTwoParams() {
    String error1 = ErrorMessagesFormatBuilder
        .getErrorWithParameters(Messages.REFERENCED_PARAMETER_NOT_FOUND.getErrorMessage(), "param",
            "res");
    Assert.assertNotNull(error1);
  }
}
