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

package org.openecomp.sdc.vendorlicense.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class SubmitUncompletedLicenseModelErrorBuilder {

  private static final String SUBMIT_UNCOMPLETED_LICENSE_MODEL_MSG =
      "Uncompleted vendor license model %s cannot be submitted. "
              + "It must contain license_agreement(s) that all feature groups "
              + "contain at least one entitlement pool.";
  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  /**
   * Instantiates a new Submit uncompleted license model error builder.
   *
   * @param vlmId the vlm id
   */
  public SubmitUncompletedLicenseModelErrorBuilder(String vlmId) {
    builder.withId(VendorLicenseErrorCodes.SUBMIT_UNCOMPLETED_LICENSE_MODEL);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(SUBMIT_UNCOMPLETED_LICENSE_MODEL_MSG, vlmId));
  }

  public ErrorCode build() {
    return builder.build();
  }


}
