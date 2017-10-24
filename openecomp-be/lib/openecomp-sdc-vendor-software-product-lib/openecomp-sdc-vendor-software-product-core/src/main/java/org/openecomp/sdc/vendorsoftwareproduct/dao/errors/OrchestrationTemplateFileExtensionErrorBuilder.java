package org.openecomp.sdc.vendorsoftwareproduct.dao.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.INVALID_EXTENSION;

public class OrchestrationTemplateFileExtensionErrorBuilder {
  private static final String INVALID_EXTENSION_MSG = "Invalid file extension. Valid extensions " +
      "are : zip, csar.";
  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  public OrchestrationTemplateFileExtensionErrorBuilder(){
    builder.withId(INVALID_EXTENSION);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(INVALID_EXTENSION_MSG));
  }

  public ErrorCode build() {
    return builder.build();
  }
}
