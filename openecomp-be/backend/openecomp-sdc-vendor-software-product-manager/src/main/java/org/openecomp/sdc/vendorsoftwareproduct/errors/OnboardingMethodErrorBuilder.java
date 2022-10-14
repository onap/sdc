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
package org.openecomp.sdc.vendorsoftwareproduct.errors;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_INVALID_ONBOARDING_METHOD;

import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

/**
 * The type Onboarding method error builder.
 */
public class OnboardingMethodErrorBuilder {

    private static final String VSP_INVALID_ONBOARDING_METHOD_MSG = "The onboardingMethod value doesn't meet the expected attribute value.";

    private OnboardingMethodErrorBuilder() {
    }

    /**
     * Get invalid onboarding method error builder error code.
     *
     * @return the error code
     */
    public static ErrorCode getInvalidOnboardingMethodErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VSP_INVALID_ONBOARDING_METHOD);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(VSP_INVALID_ONBOARDING_METHOD_MSG);
        return builder.build();
    }
}
