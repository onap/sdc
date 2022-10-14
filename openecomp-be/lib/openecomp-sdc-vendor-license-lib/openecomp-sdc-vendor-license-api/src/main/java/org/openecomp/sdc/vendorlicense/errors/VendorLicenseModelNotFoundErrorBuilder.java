/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */
package org.openecomp.sdc.vendorlicense.errors;

import org.openecomp.sdc.errors.ErrorCode;

public class VendorLicenseModelNotFoundErrorBuilder {

    private static final String VENDOR_LICENSE_MODEL_NOT_FOUND_MSG = "Vendor license model with id %s not found.";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    /**
     * Instantiates a new Vendor license model not found error builder.
     *
     * @param vendorLicenseModelId the vendor license model id
     */
    public VendorLicenseModelNotFoundErrorBuilder(String vendorLicenseModelId) {
        builder.withId(VendorLicenseErrorCodes.VENDOR_LICENSE_MODEL_NOT_FOUND);
        builder.withMessage(String.format(VENDOR_LICENSE_MODEL_NOT_FOUND_MSG, vendorLicenseModelId));
    }

    public ErrorCode build() {
        return builder.build();
    }
}
