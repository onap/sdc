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
  private static final String BRACKET_LEFT = "[";
  private static final String BRACKET_RIGHT = "]";
  private static final String SEPARATOR = ": ";

  public static String getErrorWithParameters(String error, String... params) {
    return String.format(error, params);
  }
  /**
   * Formatted message with error code.
   *
   * @param messageCode error code.
   * @param errorMessage error message.
   * @param params paramters used in formatting meswage.
   * @return formatted message string.
   */
  public static String getErrorWithParameters(final ErrorMessageCode messageCode,
                                              String errorMessage, String... params) {
    StringBuffer message = new StringBuffer(BRACKET_LEFT);
    message.append(messageCode.getMessageCode()).append(BRACKET_RIGHT).append(SEPARATOR)
            .append(errorMessage);

    return getErrorWithParameters(message.toString(), params);
  }

}
