package org.openecomp.sdc.vendorsoftwareproduct.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.UPDATE_COMPUTE_NOT_ALLOWED;


public class DuplicateComputeInComponentErrorBuilder {
  private static final String COMPUTE_HEAT_READONLY_ATTR_MSG =
      "Update of attribute %s not allowed for VSP onboarded via HEAT.";

  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  public static ErrorCode getComputeHeatReadOnlyErrorBuilder(String name) {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(UPDATE_COMPUTE_NOT_ALLOWED);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(COMPUTE_HEAT_READONLY_ATTR_MSG, name));
    return builder.build();
  }


  public ErrorCode build() {
    return builder.build();
  }
}
