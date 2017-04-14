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

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_NOT_FOUND;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;

/**
 * The type Vendor software product not found error builder.
 */
public class VendorSoftwareProductNotFoundErrorBuilder extends BaseErrorBuilder {
  private static final String VSP_FOUND_MSG = "Vendor software product with Id %s not found.";

  /**
   * Instantiates a new Vendor software product not found error builder.
   *
   * @param vendorSoftwareProductId the vendor software product id
   */
  public VendorSoftwareProductNotFoundErrorBuilder(String vendorSoftwareProductId) {
    builder.withId(VSP_NOT_FOUND);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(VSP_FOUND_MSG, vendorSoftwareProductId));
  }

}
