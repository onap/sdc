/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.action.errors;

import org.openecomp.sdc.action.util.ActionUtil;

/**
 * Custom Exception class for handling Action Library error scenarios.
 */
public class ActionException extends RuntimeException {

  private String errorCode;
  private String description;
  private int logResponseCode;

  public ActionException() {

  }

  /**
   * Instantiates a new Action exception.
   *
   * @param errorCode   the error code
   * @param description the description
   */
  public ActionException(String errorCode, String description) {
    this.errorCode = errorCode;
    this.description = description;
    this.logResponseCode = ActionUtil.getLogResponseCode(this.errorCode);
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getLogResponseCode() {
    return logResponseCode;
  }

  public void setLogResponseCode(int logResponseCode) {
    this.logResponseCode = logResponseCode;
  }
}
