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

package org.openecomp.sdc.vendorlicense.errors;


import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class LimitErrorBuilder {

  private static final String LIMIT_INVALID_ATTR_VALUE_MSG = "The %s value doesn't meet the "
      + "expected attribute value.";

  private static final String DUPLICATE_LIMIT_NAME_NOT_ALLOWED_MSG =
      "Invalid request, Limit with name %s already exists for type %s.";

  private LimitErrorBuilder(){

  }

  public static ErrorCode getInvalidValueErrorBuilder(String attribute, String errorCode) {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(errorCode);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(LIMIT_INVALID_ATTR_VALUE_MSG, attribute));
    return builder.build();
  }

  public static ErrorCode getDuplicateNameErrorbuilder(String name, String type) {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(VendorLicenseErrorCodes.DUPLICATE_LIMIT_NAME_NOT_ALLOWED);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format (DUPLICATE_LIMIT_NAME_NOT_ALLOWED_MSG, name, type ));
    return builder.build();
  }
}
