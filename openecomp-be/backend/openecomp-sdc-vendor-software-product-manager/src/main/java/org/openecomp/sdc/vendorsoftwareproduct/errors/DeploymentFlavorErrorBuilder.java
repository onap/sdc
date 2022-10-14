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

import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class DeploymentFlavorErrorBuilder {

    private static final String CREATE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG = "Deployment Flavor cannot be added for VSPs onboarded with HEAT.";
    private static final String FEATURE_GROUP_NOT_EXIST_FOR_VSP_MSG = "Invalid request, Feature Group provided does not exist for this Vsp.";
    private static final String INVALID_COMPONENT_COMPUTE_ASSOCIATION_MSG = "Invalid request,for valid association please provide ComponentId for Compute Flavor";
    private static final String SAME_VFC_ASSOCIATION_MORE_THAN_ONCE_NOT_ALLOWED_MSG = "Invalid Request,Same Vfc cannot be associated more than once.";
    private static final String DUPLICATE_DEPLOYMENT_FLAVOR_MODEL_NOT_ALLOWED_MSG = "Invalid request, Deployment Flavor with model %s already exists for Vsp with Id %s.";
    private static final String DEPLOYMENT_FLAVOUR_NAME_FORMAT_MSG = "Field does not conform to predefined criteria" + ": name : must match %s";
    private static final String INVALID_COMPUTE_FLAVOR_ID_MSG = "Invalid request, Compute Flavor provided does not exist for this VFC.";
    private static final String INVALID_COMPONENT_ID_MSG = "Invalid request, Component provided does not exist for this VSP.";
    private static final String INVALID_COMPONENT_COMPUTE_ASSOCIATION_ERROR_MSG = "VSP cannot be "
        + "submitted with an invalid Deployment Flavor. All Deployment Flavor should have atleast a VFC included with it's required Compute needs. "
        + "Please fix the Deployment Flavor %s and re-submit the VSP.";
    private static final String FEATUREGROUP_REQUIRED_IN_DEPLOYMENT_FLAVOR_MSG =
        "VSP cannot be " + "submitted with an invalid Deployment Flavor. All Deployment Flavor should have "
            + "FeatureGroup. Please fix the Deployment Flavor %s and re-submit the VSP.";

    private DeploymentFlavorErrorBuilder() {
    }

    public static ErrorCode getAddDeploymentNotSupportedHeatOnboardErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.CREATE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(CREATE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG);
        return builder.build();
    }

    public static ErrorCode getFeatureGroupNotexistErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.FEATURE_GROUP_NOT_EXIST_FOR_VSP);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(FEATURE_GROUP_NOT_EXIST_FOR_VSP_MSG);
        return builder.build();
    }

    public static ErrorCode getDuplicateVfcAssociationErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.SAME_VFC_ASSOCIATION_MORE_THAN_ONCE_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(SAME_VFC_ASSOCIATION_MORE_THAN_ONCE_NOT_ALLOWED_MSG);
        return builder.build();
    }

    public static ErrorCode getInvalidAssociationErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.INVALID_COMPONENT_COMPUTE_ASSOCIATION);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(INVALID_COMPONENT_COMPUTE_ASSOCIATION_MSG);
        return builder.build();
    }

    public static ErrorCode getDuplicateDeploymentFlavorModelErrorBuilder(String name, String vspId) {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.DUPLICATE_DEPLOYMENT_FLAVOR_MODEL_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(DUPLICATE_DEPLOYMENT_FLAVOR_MODEL_NOT_ALLOWED_MSG, name, vspId));
        return builder.build();
    }

    public static ErrorCode getInvalidComputeIdErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.INVALID_COMPUTE_FLAVOR_ID);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(INVALID_COMPUTE_FLAVOR_ID_MSG);
        return builder.build();
    }

    public static ErrorCode getInvalidComponentIdErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.INVALID_COMPONENT_ID);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(INVALID_COMPONENT_ID_MSG);
        return builder.build();
    }

    public static ErrorCode getInvalidComponentComputeAssociationErrorBuilder(String model) {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.INVALID_COMPONENT_COMPUTE_ASSOCIATION);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(INVALID_COMPONENT_COMPUTE_ASSOCIATION_ERROR_MSG, model));
        return builder.build();
    }

    public static ErrorCode getFeatureGroupMandatoryErrorBuilder(String model) {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.FEATUREGROUP_REQUIRED_IN_DEPLOYMENT_FLAVOR);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(FEATUREGROUP_REQUIRED_IN_DEPLOYMENT_FLAVOR_MSG, model));
        return builder.build();
    }

    public static ErrorCode getDeploymentFlavorNameFormatErrorBuilder(String pattern) {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.DEPLOYMENT_FLAVOR_NAME_FORMAT_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(DEPLOYMENT_FLAVOUR_NAME_FORMAT_MSG, pattern));
        return builder.build();
    }
}
