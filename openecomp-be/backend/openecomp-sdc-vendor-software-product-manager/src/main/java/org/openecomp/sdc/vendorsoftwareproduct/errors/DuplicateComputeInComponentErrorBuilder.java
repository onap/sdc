package org.openecomp.sdc.vendorsoftwareproduct.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.UPDATE_COMPUTE_NOT_ALLOWED;


public class DuplicateComputeInComponentErrorBuilder {

  private static final String DUPLICATE_COMPUTE_NAME_NOT_ALLOWED_MSG =
      "Invalid request, Compute with name %s already exists for component with ID %s.";
  private static final String COMPUTE_HEAT_READONLY_ATTR_MSG = "Update of attribute %s not allowed "
          + "for VSP onboarded via HEAT.";
  private static final String COMPUTE_MANUAL_READONLY_ATTR_MSG = "Update of attribute %s not allowed "
          + "for VSP onboarded manually.";


  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  public DuplicateComputeInComponentErrorBuilder(String computeName, String componentId ){
    builder.withId(VendorSoftwareProductErrorCodes.DUPLICATE_COMPUTE_NAME_NOT_ALLOWED);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(DUPLICATE_COMPUTE_NAME_NOT_ALLOWED_MSG,computeName,
        componentId));
  }

  /**
   * Gets duplicate compute name error builder.
   *
   * @return the duplicate compute name error builder
   */
  public static ErrorCode getDuplicateComputeNameErrorBuilder(String computeName, String componenetId) {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(VendorSoftwareProductErrorCodes.DUPLICATE_COMPUTE_NAME_NOT_ALLOWED);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(DUPLICATE_COMPUTE_NAME_NOT_ALLOWED_MSG, computeName, componenetId ));
    return builder.build();
  }

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
