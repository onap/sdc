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

public class PackageNotFoundErrorBuilder {
  private static final String PACKAGE_VERSION_NOT_FOUND_MSG =
      "Package for vendor software product with Id %s and version %s does not exist.";
  private static final String PACKAGE_NOT_FOUND_MSG =
      "Package for vendor software product with Id %s does not exist.";
  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  /**
   * Instantiates a new Package not found error builder.
   *
   * @param vendorSoftwareProductId the vendor software product id
   * @param version                 the version
   */
  public PackageNotFoundErrorBuilder(String vendorSoftwareProductId, Version version) {
    builder.withId(VendorSoftwareProductErrorCodes.PACKAGE_NOT_FOUND);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(PACKAGE_VERSION_NOT_FOUND_MSG,
        vendorSoftwareProductId, version == null ? null : version.toString()));
  }

  /**
   * Instantiates a new Package not found error builder.
   *
   * @param vendorSoftwareProductId the vendor software product id
   */
  public PackageNotFoundErrorBuilder(String vendorSoftwareProductId) {
    builder.withId(VendorSoftwareProductErrorCodes.PACKAGE_NOT_FOUND);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(PACKAGE_NOT_FOUND_MSG, vendorSoftwareProductId));
  }

  public ErrorCode build() {
    return builder.build();
  }
}
