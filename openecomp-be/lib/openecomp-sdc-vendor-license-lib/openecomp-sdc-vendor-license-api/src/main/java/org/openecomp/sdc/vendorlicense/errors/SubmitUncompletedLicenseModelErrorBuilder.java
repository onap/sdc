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
package org.openecomp.sdc.vendorlicense.errors;

import org.openecomp.sdc.errors.ErrorCode;

public class SubmitUncompletedLicenseModelErrorBuilder {

    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    /**
     * Instantiates a new Submit uncompleted license model error builder.
     */
    public SubmitUncompletedLicenseModelErrorBuilder(UncompletedVendorLicenseModelErrorType error) {
        builder.withId(VendorLicenseErrorCodes.SUBMIT_UNCOMPLETED_LICENSE_MODEL);
        builder.withMessage(error.getErrorMessage());
    }

    public ErrorCode build() {
        return builder.build();
    }
}
