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

package org.openecomp.sdc.vendorsoftwareproduct.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;


public class ComponentErrorBuilder {

  private static final String VFC_INVALID_MISSING_IMAGE_MSG =
      "All VFC need to have atleast a single Image specified. Please fix the VFC Images and re-submit the VSP";

  private ComponentErrorBuilder(){

  }


  public static ErrorCode vfcMissingImageErrorBuilder() {
    ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
    builder.withId(VendorSoftwareProductErrorCodes.VFC_INVALID);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder
        .withMessage(String.format(VFC_INVALID_MISSING_IMAGE_MSG));
    return builder.build();
  }
}
