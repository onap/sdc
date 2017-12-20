package org.openecomp.sdc.vendorsoftwareproduct.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.NETWORK_DESCRIPTION_NOT_ALLOWED_FOR_INTERNAL_NETWORK;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.NETWORK_TYPE_UPDATE_NOT_ALLOWED;

public class NicInternalNetworkErrorBuilder {
  private static final String NULL_NETWORKID_NOT_ALLOWED_MSG =
      "Internal Networks are currently not supported for VSP created Manually, so please fix all the NIC to be of Type External and re-submit the VSP.";

  private static final String NETWORK_DESCRIPTION_NOT_ALLOWED_FOR_INTERNAL_NETWORK_MSG =
      "Invalid request, Network Description not allowed for Internal Networks";
  private static final String NETWORK_TYPE_UPDATE_NOT_ALLOWED_MSG =
          "Invalid request, Network Type Update not allowed for a Nic";


  public static ErrorCode getNicNullNetworkIdInternalNetworkIdErrorBuilder(){
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(VendorSoftwareProductErrorCodes.NULL_NETWORKID_NOT_ALLOWED);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(NULL_NETWORKID_NOT_ALLOWED_MSG));
    return builder.build();
  }


  public static ErrorCode getNetworkDescriptionInternalNetworkErrorBuilder(){
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(NETWORK_DESCRIPTION_NOT_ALLOWED_FOR_INTERNAL_NETWORK);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(NETWORK_DESCRIPTION_NOT_ALLOWED_FOR_INTERNAL_NETWORK_MSG));
    return builder.build();
  }


  public static ErrorCode getNetworkTypeErrorBuilder(){
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(NETWORK_TYPE_UPDATE_NOT_ALLOWED);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(NETWORK_TYPE_UPDATE_NOT_ALLOWED_MSG));
    return builder.build();
  }


}
