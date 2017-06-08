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

public class GeneralErrorBuilder {

  private static final String GENERAL_ERROR_REST_ID = "GENERAL_ERROR_REST_ID";
  private static final String GENERAL_ERROR_REST_MSG = "An error has occurred: %s";

  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  /**
   * Instantiates a new General error builder.
   *
   * @param detailedError the detailed error
   */
  public GeneralErrorBuilder(String detailedError) {
    builder.withId(GENERAL_ERROR_REST_ID);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(GENERAL_ERROR_REST_MSG, detailedError));
  }

  public ErrorCode build() {
    return builder.build();
  }

}
