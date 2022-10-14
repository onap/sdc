/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.versioning.dao.types.Version;

public class CompositionEditNotAllowedErrorBuilder {

    private static final String VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG =
        "Composition entities may not be created / deleted for Vendor Software Product %s, version %s" + "whose entities were uploaded";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    /**
     * Instantiates a new error builder.
     *
     * @param vendorSoftwareProductId the vendor software product id
     * @param version                 the version
     */
    public CompositionEditNotAllowedErrorBuilder(String vendorSoftwareProductId, Version version) {
        builder.withId(VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder
            .withMessage(String.format(VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG, vendorSoftwareProductId, version == null ? null : version.toString()));
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
