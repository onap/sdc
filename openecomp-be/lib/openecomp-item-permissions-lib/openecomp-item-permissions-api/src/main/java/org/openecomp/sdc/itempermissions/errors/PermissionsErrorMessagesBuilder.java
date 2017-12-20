package org.openecomp.sdc.itempermissions.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

/**
 * Created by ayalaben on 6/28/2017
 */
public class PermissionsErrorMessagesBuilder {
  public static final String PERMISSIONS_ERROR= "PERMISSIONS_ERROR";
  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  /**
   * Instantiates a new Submit uncompleted license model error builder.
   *
   * @param error
   */
  public PermissionsErrorMessagesBuilder(PermissionsErrorMessages error) {
    builder.withId(PERMISSIONS_ERROR);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(error.getErrorMessage());
  }

  public ErrorCode build() {
    return builder.build();
  }


}
