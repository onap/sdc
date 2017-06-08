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

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.CREATE_PACKAGE_FOR_NON_FINAL_VSP;


public class CreatePackageForNonFinalVendorSoftwareProductErrorBuilder {

  private static final String CREATE_PACKAGE_FOR_NON_FINAL_VSP_MSG =
      "Package creation for vendor software product with id %s and version %s is not allowed since "
          + "it is not final (submitted).";
  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  /**
   * Instantiates a new Create package for non final vendor software product error builder.
   *
   * @param vendorSoftwareProductId the vendor software product id
   * @param version                 the version
   */
  public CreatePackageForNonFinalVendorSoftwareProductErrorBuilder(String vendorSoftwareProductId,
                                                                   Version version) {
    builder.withId(CREATE_PACKAGE_FOR_NON_FINAL_VSP);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String
        .format(CREATE_PACKAGE_FOR_NON_FINAL_VSP_MSG, vendorSoftwareProductId, version == null ? null : version.toString()));
  }

  public ErrorCode build() {
    return builder.build();
  }
}
