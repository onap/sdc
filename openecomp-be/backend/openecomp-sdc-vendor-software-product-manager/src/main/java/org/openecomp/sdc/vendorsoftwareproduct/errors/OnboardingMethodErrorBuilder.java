package org.openecomp.sdc.vendorsoftwareproduct.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_INVALID_ONBOARDING_METHOD;

/**
 * The type Onboarding method error builder.
 */
public class OnboardingMethodErrorBuilder {

  private static final String VSP_INVALID_ONBOARDING_METHOD_MSG =
      "The onboardingMethod value doesn't meet the expected attribute value.";

  /**
   * Get invalid onboarding method error builder error code.
   *
   * @return the error code
   */
  public static ErrorCode getInvalidOnboardingMethodErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(VSP_INVALID_ONBOARDING_METHOD);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(VSP_INVALID_ONBOARDING_METHOD_MSG);
    return builder.build();
  }
}
