package org.openecomp.sdc.vendorsoftwareproduct.dao.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.text.MessageFormat;

public class VendorSoftwareProductCreationFailedBuilder {
  private static final String VSP_CREATION_FAILED = "Failed to create VSP; %s";
  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  /**
   * Instantiates a new Vendor software product creation failed error builder.
   *
   */
  public VendorSoftwareProductCreationFailedBuilder(String reason) {
    builder.withId(VendorSoftwareProductErrorCodes.FAILED_TO_CREATE_VSP);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder
        .withMessage(String.format(VSP_CREATION_FAILED, reason));
  }

  public ErrorCode build() {
    return builder.build();
  }
}
