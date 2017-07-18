package org.openecomp.sdc.vendorsoftwareproduct.errors;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.DUPLICATE_IMAGE_NAME_NOT_ALLOWED;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.UPDATE_IMAGE_NOT_ALLOWED;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VFC_IMAGE_INVALID_FORMAT;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

/**
 * The Image error builder.
 */
public class ImageErrorBuilder {

  private static final String VFC_IMAGE_DUPLICATE_NAME_MSG = "Invalid request, Image with name %s"
      + " already exists for component with ID %s.";

  private static final String IMAGE_INVALID_FORMAT_MSG = "The format value doesn't meet the "
      + "expected attribute value.";

  private static final String IMAGE_HEAT_READONLY_ATTR_MSG = "Update of attribute %s not allowed "
      + "for VSP onboarded via HEAT.";


  /**
   * Gets duplicate image name error builder.
   *
   * @return the duplicate image name error builder
   */
  public static ErrorCode getDuplicateImageNameErrorBuilder(String imageName, String componenetId) {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(DUPLICATE_IMAGE_NAME_NOT_ALLOWED);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(VFC_IMAGE_DUPLICATE_NAME_MSG, imageName, componenetId ));
    return builder.build();
  }

  /**
   * Gets invalid image format error builder.
   *
   * @return the invalid image format error builder
   */
  public static ErrorCode getInvalidImageFormatErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(VFC_IMAGE_INVALID_FORMAT);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(IMAGE_INVALID_FORMAT_MSG));
    return builder.build();
  }

  public static ErrorCode getImageHeatReadOnlyErrorBuilder(String name) {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(UPDATE_IMAGE_NOT_ALLOWED);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(IMAGE_HEAT_READONLY_ATTR_MSG, name));
    return builder.build();
  }
}
