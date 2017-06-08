package org.openecomp.sdc.vendorsoftwareproduct.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.versioning.dao.types.Version;


public class CompositionEditNotAllowedErrorBuilder {
  private static final String VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG =
      "Composition entities may not be created / deleted for Vendor Software Product %s, version %s"
          + "whose entities were uploaded";
  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  /**
   * Instantiates a new error builder.
   *
   * @param vendorSoftwareProductId the vendor software product id
   * @param version                 the version
   */
  public CompositionEditNotAllowedErrorBuilder(String vendorSoftwareProductId, Version version) {
    builder.withId(VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(
        String.format(VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG, vendorSoftwareProductId,
            version == null ? null : version.toString()));

  }

  /**
   * Build error code.
   *
   * @return the error code
   */
  public ErrorCode build() {
    return builder.build();
  }
}
