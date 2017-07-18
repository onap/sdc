package org.openecomp.sdc.vendorsoftwareproduct.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class DuplicateNicInComponentErrorBuilder {

  private static final String DUPLICATE_NIC_NAME_NOT_ALLOWED_MSG =
      "Invalid request, NIC with name %s already exist for component with ID %s.";

  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  public DuplicateNicInComponentErrorBuilder(String nicName, String componentId ){
    builder.withId(VendorSoftwareProductErrorCodes.DUPLICATE_NIC_NAME_NOT_ALLOWED);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(DUPLICATE_NIC_NAME_NOT_ALLOWED_MSG,nicName,componentId));
  }
  public ErrorCode build() {
    return builder.build();
  }

}
