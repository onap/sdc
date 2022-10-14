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

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.INVALID_EXTENSION;

import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class OrchestrationTemplateFileExtensionErrorBuilder {

    private static final String INVALID_EXTENSION_MSG = "Invalid file extension. Valid extensions " + "are : zip, csar.";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    public OrchestrationTemplateFileExtensionErrorBuilder() {
        builder.withId(INVALID_EXTENSION);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(INVALID_EXTENSION_MSG));
    }

    public ErrorCode build() {
        return builder.build();
    }
}
