package org.openecomp.sdc.vendorsoftwareproduct.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class NicNetworkIdNotAllowedExternalNetworkErrorBuilder {
  private static final String NETWORKID_NOT_ALLOWED_FOR_EXTERNAL_NETWORK_MSG =
      "Invalid request,NetworkId not allowed for External Networks";
  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
  public NicNetworkIdNotAllowedExternalNetworkErrorBuilder(){
    builder.withId(VendorSoftwareProductErrorCodes.NETWORKID_NOT_ALLOWED_FOR_EXTERNAL_NETWORK);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(NETWORKID_NOT_ALLOWED_FOR_EXTERNAL_NETWORK_MSG));
  }

  public ErrorCode build() {
    return builder.build();
  }
}
