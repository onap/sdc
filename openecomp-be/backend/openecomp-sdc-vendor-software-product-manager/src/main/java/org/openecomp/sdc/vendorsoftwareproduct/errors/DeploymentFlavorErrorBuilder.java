package org.openecomp.sdc.vendorsoftwareproduct.errors;


import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.versioning.dao.types.Version;

public class DeploymentFlavorErrorBuilder {
    private static final String CREATE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG=
            "Deployment Flavor cannot be added for VSPs onboarded with HEAT.";
    private static final String FEATURE_GROUP_NOT_EXIST_FOR_VSP_MSG=
        "Invalid request,Feature Group with Id %s does not exist for Vsp with Id %s and version " +
            "%s.";
    private static final String INVALID_COMPONENT_COMPUTE_ASSOCIATION_MSG
        ="Invalid request,for valid association please provide ComponentId for Compute Flavor";
    private static final String SAME_VFC_ASSOCIATION_MORE_THAN_ONCE_NOT_ALLOWED_MSG=
        "Invalid Request,Same Vfc cannot be associated more than once.";
    private static final String DUPLICATE_DEPLOYMENT_FLAVOR_MODEL_NOT_ALLOWED_MSG =
        "Invalid request, Deployment Flavor with model %s already exists for Vsp with Id %s.";
    private static final String DEPLOYMENT_FLAVOUR_NAME_FORMAT_MSG = "Field does not conform to predefined criteria"
            + ": name : must match %s";
    private static final String INVALID_COMPUTE_FLAVOR_ID_MSG =
        "Invalid request, Compute Flavor with Id %s does not exist for VFC with Id %s.";
    private static final String INVALID_COMPONENT_COMPUTE_ASSOCIATION_ERROR_MSG="VSP cannot be " +
        "submitted with an invalid Deployment Flavor. All Deployment Flavor should have atleast a VFC included with it's required Compute needs. Please fix the Deployment Flavor and re-submit the VSP.";

    private static final String FEATUREGROUP_REQUIRED_IN_DEPLOYMENT_FLAVOR_MSG = "VSP cannot be " +
        "submitted with an invalid Deployment Flavor. All Deployment Flavor should have " +
        "FeatureGroup. Please fix the Deployment Flavor and re-submit the VSP.";

    public static ErrorCode getAddDeploymentNotSupportedHeatOnboardErrorBuilder(){
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.CREATE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(CREATE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING_MSG));
        return builder.build();
    }

    public static ErrorCode getFeatureGroupNotexistErrorBuilder( String featureGroupId, String
        VspId, Version activeVersion){
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.FEATURE_GROUP_NOT_EXIST_FOR_VSP);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(FEATURE_GROUP_NOT_EXIST_FOR_VSP_MSG,featureGroupId,
            VspId,activeVersion.toString()));
        return builder.build();
    }

    public static ErrorCode getDuplicateVfcAssociationErrorBuilder(){
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.SAME_VFC_ASSOCIATION_MORE_THAN_ONCE_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(SAME_VFC_ASSOCIATION_MORE_THAN_ONCE_NOT_ALLOWED_MSG));
        return builder.build();
    }

    public static ErrorCode getInvalidAssociationErrorBuilder(){
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.INVALID_COMPONENT_COMPUTE_ASSOCIATION);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(INVALID_COMPONENT_COMPUTE_ASSOCIATION_MSG));
        return builder.build();
    }

    public static ErrorCode getDuplicateDeploymentFlavorModelErrorBuilder(String name, String vspId){
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.DUPLICATE_DEPLOYMENT_FLAVOR_MODEL_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(DUPLICATE_DEPLOYMENT_FLAVOR_MODEL_NOT_ALLOWED_MSG,name,vspId));
        return builder.build();
    }
    public static ErrorCode getInvalidComputeIdErrorBuilder( String computeFlavorId, String
        vfcId){
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.INVALID_COMPUTE_FLAVOR_ID);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(INVALID_COMPUTE_FLAVOR_ID_MSG,computeFlavorId,
            vfcId));
        return builder.build();
    }

    public static ErrorCode getInvalidComponentComputeAssociationErrorBuilder(){
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.INVALID_COMPONENT_COMPUTE_ASSOCIATION);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(INVALID_COMPONENT_COMPUTE_ASSOCIATION_ERROR_MSG));
        return builder.build();
    }

    public static ErrorCode getFeatureGroupMandatoryErrorBuilder(){
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.FEATUREGROUP_REQUIRED_IN_DEPLOYMENT_FLAVOR);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(FEATUREGROUP_REQUIRED_IN_DEPLOYMENT_FLAVOR_MSG));
        return builder.build();
    }

    public static ErrorCode getDeploymentFlavorNameFormatErrorBuilder(String pattern){
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.DEPLOYMENT_FLAVOR_NAME_FORMAT_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(DEPLOYMENT_FLAVOUR_NAME_FORMAT_MSG, pattern));
        return builder.build();
    }
}
