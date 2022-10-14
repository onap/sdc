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

public class DuplicateNicInComponentErrorBuilder {

    private static final String DUPLICATE_NIC_NAME_NOT_ALLOWED_MSG = "Invalid request, NIC with name %s already exist for component with ID %s.";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    public DuplicateNicInComponentErrorBuilder(String nicName, String componentId) {
        builder.withId(VendorSoftwareProductErrorCodes.DUPLICATE_NIC_NAME_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(DUPLICATE_NIC_NAME_NOT_ALLOWED_MSG, nicName, componentId));
    }

    public ErrorCode build() {
        return builder.build();
    }
}
