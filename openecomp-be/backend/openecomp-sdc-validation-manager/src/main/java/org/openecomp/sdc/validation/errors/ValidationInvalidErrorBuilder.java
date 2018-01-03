/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.openecomp.sdc.validation.errors;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

import java.util.List;
import java.util.Map;

public class ValidationInvalidErrorBuilder extends BaseErrorBuilder {
  private static final String VALIDATION_INVALID_DETAILED_MSG = "File is invalid: %s";
  private static final String VALIDATION_INVALID_MSG = "Validated file is invalid";

  /**
   * Instantiates a new Validation invalid error builder.
   *
   * @param errors the errors
   */
  public ValidationInvalidErrorBuilder(Map<String, List<ErrorMessage>> errors) {
    getErrorCodeBuilder().withId(ValidationErrorCodes.VALIDATION_INVALID.getVal());
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder()
        .withMessage(String.format(VALIDATION_INVALID_DETAILED_MSG, toString(errors)));
  }

  /**
   * Instantiates a new Validation invalid error builder.
   */
  public ValidationInvalidErrorBuilder() {
    getErrorCodeBuilder().withId(ValidationErrorCodes.VALIDATION_INVALID.getVal());
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder().withMessage(VALIDATION_INVALID_MSG);
  }

  private String toString(Map<String, List<ErrorMessage>> errors) {
    StringBuilder sb = new StringBuilder();
    errors.entrySet().stream()
        .forEach(entry -> singleErrorToString(sb, entry.getKey(), entry.getValue()));
    return sb.toString();
  }

  private void singleErrorToString(StringBuilder sb, String fileName, List<ErrorMessage> errors) {
    sb.append(System.lineSeparator());
    sb.append(fileName);
    sb.append(sb.append(": "));
    errors.stream().forEach(
        error -> sb.append(error.getMessage()).append("[").append(error.getLevel()).append("], "));
  }

}
