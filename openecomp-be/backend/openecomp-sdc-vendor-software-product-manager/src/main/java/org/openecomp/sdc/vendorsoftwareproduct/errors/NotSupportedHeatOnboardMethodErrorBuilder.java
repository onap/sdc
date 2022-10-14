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

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.ADD_COMPUTE_NOT_ALLOWED_IN_HEAT_ONBOARDING;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.ADD_IMAGE_NOT_ALLOWED_IN_HEAT_ONBOARDING;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.ADD_NIC_NOT_ALLOWED_IN_HEAT_ONBOARDING;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.DELETE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.DELETE_IMAGE_NOT_ALLOWED;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.EDIT_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING;

import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class NotSupportedHeatOnboardMethodErrorBuilder {

    private static final String ADD_NIC_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG = "NIC cannot be added for VSPs onboarded with HEAT.";
    private static final String ADD_COMPUTE_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG = "Compute flavor cannot be added for VSPs onboarded with HEAT.";
    private static final String IMAGE_ADD_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG = "Image cannot be added for VSPs onboarded with HEAT.";
    private static final String DELETE_IMAGE_NOT_ALLOWED_MSG = "Image cannot be deleted for VSPs onboarded with HEAT.";
    private static final String DELETE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_MSG = "Deployment Flavor cannot be deleted for VSPs onboarded with HEAT.";
    private static final String EDIT_DEPLOYMENT_FLAVOR_NOT_ALLOWED_MSG = "Deployment Flavor cannot be edited for VSPs onboarded with HEAT.";

    private NotSupportedHeatOnboardMethodErrorBuilder() {
    }

    public static ErrorCode getAddNicNotSupportedHeatOnboardMethodErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(ADD_NIC_NOT_ALLOWED_IN_HEAT_ONBOARDING);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(ADD_NIC_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG);
        return builder.build();
    }

    public static ErrorCode getAddComputeNotSupportedHeatOnboardMethodErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(ADD_COMPUTE_NOT_ALLOWED_IN_HEAT_ONBOARDING);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(ADD_COMPUTE_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG);
        return builder.build();
    }

    public static ErrorCode getAddImageNotSupportedHeatOnboardMethodErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(ADD_IMAGE_NOT_ALLOWED_IN_HEAT_ONBOARDING);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(IMAGE_ADD_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG);
        return builder.build();
    }

    public static ErrorCode getDelImageNotSupportedHeatOnboardMethodErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(DELETE_IMAGE_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(DELETE_IMAGE_NOT_ALLOWED_MSG);
        return builder.build();
    }

    public static ErrorCode getDelDeploymentFlavorNotSupportedHeatOnboardMethodErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(DELETE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(DELETE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_MSG);
        return builder.build();
    }

    public static ErrorCode getUpdateDfNotSupportedHeatOnboardMethodErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(EDIT_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(EDIT_DEPLOYMENT_FLAVOR_NOT_ALLOWED_MSG);
        return builder.build();
    }
}
