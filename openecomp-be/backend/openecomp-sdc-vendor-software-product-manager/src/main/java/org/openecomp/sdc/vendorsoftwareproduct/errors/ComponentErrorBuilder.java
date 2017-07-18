package org.openecomp.sdc.vendorsoftwareproduct.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;


public class ComponentErrorBuilder {

  private static final String VFC_INVALID_MISSING_IMAGE_MSG =
      "All VFC need to have atleast a single Image specified. Please fix the VFC Images and re-submit the VSP";


  public static ErrorCode VfcMissingImageErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(VendorSoftwareProductErrorCodes.VFC_INVALID);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder
        .withMessage(String.format(VFC_INVALID_MISSING_IMAGE_MSG));
    return builder.build();
  }
}
