package org.openecomp.sdc.vendorsoftwareproduct.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.ADD_COMPUTE_NOT_ALLOWED_IN_HEAT_ONBOARDING;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.ADD_IMAGE_NOT_ALLOWED_IN_HEAT_ONBOARDING;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.ADD_NIC_NOT_ALLOWED_IN_HEAT_ONBOARDING;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.DELETE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.DELETE_IMAGE_NOT_ALLOWED;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.EDIT_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING;

public class NotSupportedHeatOnboardMethodErrorBuilder {
  private static final String ADD_NIC_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG =
      "NIC cannot be added for VSPs onboarded with HEAT.";
  private static final String ADD_COMPUTE_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG =
      "Compute flavor cannot be added for VSPs onboarded with HEAT.";
  private static final String IMAGE_ADD_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG =
      "Image cannot be added for VSPs onboarded with HEAT.";
  private static final String DELETE_IMAGE_NOT_ALLOWED_MSG =
      "Image cannot be deleted for VSPs onboarded with HEAT.";
  private static final String DELETE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_MSG =
          "Deployment Flavor cannot be deleted for VSPs onboarded with HEAT.";
  private static final String EDIT_DEPLOYMENT_FLAVOR_NOT_ALLOWED_MSG =
      "Deployment Flavor cannot be edited for VSPs onboarded with HEAT.";


  public static ErrorCode getAddNicNotSupportedHeatOnboardMethodErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(ADD_NIC_NOT_ALLOWED_IN_HEAT_ONBOARDING);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(ADD_NIC_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG));
    return builder.build();
  }

  public static ErrorCode getAddComputeNotSupportedHeatOnboardMethodErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(ADD_COMPUTE_NOT_ALLOWED_IN_HEAT_ONBOARDING);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(ADD_COMPUTE_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG));
    return builder.build();
  }

  public static ErrorCode getAddImageNotSupportedHeatOnboardMethodErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(ADD_IMAGE_NOT_ALLOWED_IN_HEAT_ONBOARDING);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(IMAGE_ADD_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG));
    return builder.build();
  }

  public static ErrorCode getDelImageNotSupportedHeatOnboardMethodErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(DELETE_IMAGE_NOT_ALLOWED);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(DELETE_IMAGE_NOT_ALLOWED_MSG));
    return builder.build();
  }

  public static ErrorCode getDelDeploymentFlavorNotSupportedHeatOnboardMethodErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(DELETE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(DELETE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_MSG));
    return builder.build();
  }

  public static ErrorCode getUpdateDfNotSupportedHeatOnboardMethodErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(EDIT_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(EDIT_DEPLOYMENT_FLAVOR_NOT_ALLOWED_MSG));
    return builder.build();
  }

}
