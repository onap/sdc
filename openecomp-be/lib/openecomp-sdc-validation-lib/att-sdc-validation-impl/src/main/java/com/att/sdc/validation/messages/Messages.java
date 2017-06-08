package com.att.sdc.validation.messages;

/**
 * Created by TALIO on 5/17/2017.
 */
public enum Messages {

  UNEXPECTED_GROUP_TYPE_ATT_VALET(
      "Unexpected group_type for ATT::Valet::GroupAssignment, Resource ID [%s]");

  private String errorMessage;

  Messages(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
