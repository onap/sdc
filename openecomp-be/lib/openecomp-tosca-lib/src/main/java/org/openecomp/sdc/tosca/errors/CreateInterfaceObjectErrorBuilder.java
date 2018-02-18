package org.openecomp.sdc.tosca.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class CreateInterfaceObjectErrorBuilder {

  private static final String COULD_NOT_CREATE_OBJECT_MSG =
      "Could not create %s from %s. Reason - %s";
  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  public CreateInterfaceObjectErrorBuilder(String interfaceClassName,
                                           String interfaceId,
                                           String reason) {
    builder.withId(ToscaErrorCodes.INVALID_INTERFACE_VALUE);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(
        String.format(COULD_NOT_CREATE_OBJECT_MSG, interfaceClassName, interfaceId, reason));
  }

  public ErrorCode build() {
    return builder.build();
  }
}
