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

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.HEAT_PACKAGE_FILE_CREATION;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;

/**
 * The type File creation error builder.
 */
public class FileCreationErrorBuilder extends BaseErrorBuilder {
  private static final String HEAT_PKG_FILE_CREATION_ERROR_MSG =
      "Error while trying to create heat file from the package of vendor software product "
          + "with Id %s.";

  /**
   * Instantiates a new File creation error builder.
   *
   * @param vendorSoftwareProductId the vendor software product id
   */
  public FileCreationErrorBuilder(String vendorSoftwareProductId) {
    builder.withId(HEAT_PACKAGE_FILE_CREATION);
    builder.withCategory(ErrorCategory.SYSTEM);
    builder.withMessage(String.format(HEAT_PKG_FILE_CREATION_ERROR_MSG, vendorSoftwareProductId));
  }

}
