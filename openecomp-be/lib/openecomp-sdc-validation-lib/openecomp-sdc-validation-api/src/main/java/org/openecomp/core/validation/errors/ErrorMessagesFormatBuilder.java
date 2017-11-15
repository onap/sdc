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

package org.openecomp.core.validation.errors;


import org.openecomp.core.validation.ErrorMessageCode;

public class ErrorMessagesFormatBuilder {
  private static final String messagePattern = "[%s]: %s";

  public static String getErrorWithParameters(String error, String... params) {
    return String.format(error, params);
  }
  /**
   * Formatted message with error code.
   *
   * @param messageCode error code.
   * @param errorMessage error message.
   * @param params parameters used in formatting message.
   * @return formatted message string.
   */
  public static String getErrorWithParameters(ErrorMessageCode messageCode,
                                              String errorMessage, String... params) {
    String message = getErrorWithParameters(errorMessage, params);

    return ( null != messageCode && null != messageCode.getMessageCode() )  ?
            String.format(messagePattern, messageCode.getMessageCode(), message) : message;
  }

}
