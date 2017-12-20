package org.openecomp.sdc.itempermissions.errors;

/**
 * Created by ayalaben on 6/28/2017
 */
public enum PermissionsErrorMessages {

  NO_PERMISSION_FOR_USER("The user is not permitted to edit this item"),
  USER_NOT_OWNER_SUBMIT("The user must be the owner to submit the item"),
  INVALID_PERMISSION_TYPE("Invalid permission type"),
  INVALID_ACTION_TYPE("Invalid action type");

  private String errorMessage;

  PermissionsErrorMessages(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

}
