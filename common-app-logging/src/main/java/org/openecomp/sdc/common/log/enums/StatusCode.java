package org.openecomp.sdc.common.log.enums;

public enum StatusCode {
  ERROR("ERROR"),
  STARTED("STARTED"),
  COMPLETE("COMPLETE"),
  INPROGRESS("INPROGRESS");

  String statusCode;

  StatusCode(String statusCode) {
    this.statusCode = statusCode;
  }

  public String getStatusCodeEnum() {
    return statusCode;
  }
}
