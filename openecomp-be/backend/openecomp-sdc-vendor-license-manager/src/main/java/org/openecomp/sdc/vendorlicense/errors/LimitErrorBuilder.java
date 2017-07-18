package org.openecomp.sdc.vendorlicense.errors;


import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class LimitErrorBuilder {

  private static final String LIMIT_INVALID_ATTR_VALUE_MSG = "The %s value doesn't meet the "
      + "expected attribute value.";

  private static final String DUPLICATE_LIMIT_NAME_NOT_ALLOWED_MSG =
      "Invalid request, Limit with name %s already exists for type %s.";

  public static ErrorCode getInvalidValueErrorBuilder(String attribute, String errorCode) {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(errorCode);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(LIMIT_INVALID_ATTR_VALUE_MSG, attribute));
    return builder.build();
  }

  public static ErrorCode getDuplicateNameErrorbuilder(String name, String type) {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(VendorLicenseErrorCodes.DUPLICATE_LIMIT_NAME_NOT_ALLOWED);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(DUPLICATE_LIMIT_NAME_NOT_ALLOWED_MSG, name, type ));
    return builder.build();
  }
}
