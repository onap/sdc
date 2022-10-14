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

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.ORCHESTRATION_NOT_FOUND;

import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class OrchestrationTemplateNotFoundErrorBuilder {

    private static final String ORCHESTRATION_TEMPLATE_NOT_FOUND_MESSAGE = "Failed to get orchestration template for VSP with id %s";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    /**
     * Instantiates a new Orchestration template not found error builder.
     *
     * @param vspId the vsp id
     */
    public OrchestrationTemplateNotFoundErrorBuilder(String vspId) {
        builder.withId(ORCHESTRATION_NOT_FOUND).withCategory(ErrorCategory.APPLICATION)
            .withMessage(String.format(ORCHESTRATION_TEMPLATE_NOT_FOUND_MESSAGE, vspId));
    }

    public ErrorCode build() {
        return builder.build();
    }
}
