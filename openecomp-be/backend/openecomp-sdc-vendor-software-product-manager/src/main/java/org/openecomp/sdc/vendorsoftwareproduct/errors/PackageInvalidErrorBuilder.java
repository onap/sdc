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

package org.openecomp.sdc.vendorsoftwareproduct.errors;


import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.versioning.dao.types.Version;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.PACKAGE_INVALID;

/**
 * The type Package invalid error builder.
 */
public class PackageInvalidErrorBuilder {
  private static final String PACKAGE_INVALID_MSG =
      "Package for vendor software product with Id %s and version %s is invalid "
          + "(does not contain translated data).";
  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  /**
   * Instantiates a new Package invalid error builder.
   *
   * @param vendorSoftwareProductId the vendor software product id
   * @param version                 the version
   */
  public PackageInvalidErrorBuilder(String vendorSoftwareProductId, Version version) {
    builder.withId(PACKAGE_INVALID);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(PACKAGE_INVALID_MSG,
        vendorSoftwareProductId, version == null ? null : version.toString()));
  }

  /**
   * Build error code.
   *
   * @return the error code
   */
  public ErrorCode build() {
    return builder.build();
  }
}
