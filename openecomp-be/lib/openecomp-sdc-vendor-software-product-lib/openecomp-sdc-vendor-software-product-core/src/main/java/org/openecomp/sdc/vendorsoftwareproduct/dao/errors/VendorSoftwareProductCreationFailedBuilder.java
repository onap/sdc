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
package org.openecomp.sdc.vendorsoftwareproduct.dao.errors;

import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;

public class VendorSoftwareProductCreationFailedBuilder {

    private static final String VSP_CREATION_FAILED = "Failed to create VSP; %s";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    /**
     * Instantiates a new Vendor software product creation failed error builder.
     */
    public VendorSoftwareProductCreationFailedBuilder(String reason) {
        builder.withId(VendorSoftwareProductErrorCodes.FAILED_TO_CREATE_VSP);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(VSP_CREATION_FAILED, reason));
    }

    public ErrorCode build() {
        return builder.build();
    }
}
