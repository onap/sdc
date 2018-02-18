package org.openecomp.sdc.tosca.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class ToscaInvalidInterfaceValueErrorBuilder {

  private static final String INVALID_INTERFACE_MSG =
      "Cannot create interface object. reason - %s";
  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  public ToscaInvalidInterfaceValueErrorBuilder(String reason) {
    builder.withId(ToscaErrorCodes.INVALID_INTERFACE_VALUE);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(INVALID_INTERFACE_MSG, reason));
  }

  public ErrorCode build() {
    return builder.build();
  }
}
