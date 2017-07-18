package org.openecomp.sdc.vendorsoftwareproduct.errors;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes
    .VSP_INVALID_ONBOARDING_METHOD;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes
    .VSP_ONBOARD_METHOD_UPDATE_NOT_ALLOWED;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

/**
 * The type Onboarding method error builder.
 */
public class OnboardingMethodErrorBuilder {

  private static final String VSP_ONBOARD_METHOD_UPDATE_NOT_ALLOWED_MSG =
      "onboardingMethod update is not allowed.";
  private static final String VSP_INVALID_ONBOARDING_METHOD_MSG =
      "The onboardingMethod value doesn't meet the expected attribute value.";

  /**
   * Gets onboarding update error.
   *
   * @return the onboarding update error
   */
  public static ErrorCode getOnboardingUpdateError() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(VSP_ONBOARD_METHOD_UPDATE_NOT_ALLOWED);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(VSP_ONBOARD_METHOD_UPDATE_NOT_ALLOWED_MSG));
    return builder.build();
  }

  /**
   * Get invalid onboarding method error builder error code.
   *
   * @return the error code
   */
  public static ErrorCode getInvalidOnboardingMethodErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(VSP_INVALID_ONBOARDING_METHOD);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(VSP_INVALID_ONBOARDING_METHOD_MSG));
    return builder.build();
  }
}
