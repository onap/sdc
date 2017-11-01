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

package org.openecomp.sdc.common.errors;

import java.io.Serializable;

public class ErrorCode implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;
  private String message;
  private ErrorCategory category;

  protected ErrorCode() {
  }

  public ErrorCode(String id){
        this.id=id;
    }

  public void setMessage(String message) {
        this.message = message;
    }
  public String getMessage() {
        return message;
    }
  public String getId() {
        return id;
    }
  /*
    For backward compatibility only - will be removed soon
  */

  @Override
  public String toString() {
    return message;
  }

  public String id() {
    return id;
  }

  @Deprecated
  protected void id(String id) {
    this.id = id;
  }

  public String message() {
    return message;
  }

  @Deprecated
  protected void message(String message) {
    this.message = message;
  }

  public ErrorCategory category() {
    return category;
  }

  @Deprecated
  protected void category(ErrorCategory category) {
    this.category = category;
  }

  public static class ErrorCodeBuilder {

    private String id;
    private String message;
    private ErrorCategory category = ErrorCategory.APPLICATION;

    public ErrorCodeBuilder withId(String id) {
      this.id = id;
      return this;
    }

    //todo remove later

    public ErrorCodeBuilder withMessage(String message) {
      this.message = message;
      return this;
    }

    //todo remove later
    public ErrorCodeBuilder withCategory(ErrorCategory category) {
      this.category = category;
      return this;
    }

    /**
     * Build error code.
     *
     * @return the error code
     */
    public ErrorCode build() {
      ErrorCode inst = new ErrorCode();
      inst.id = id;
      inst.message = message;
      inst.category = category;
      return inst;
    }
  }
}
