package org.openecomp.sdc.vendorlicense.errors;

/**
 * Created by ayalaben on 5/8/2017
 */
public enum UncompletedVendorLicenseModelErrorType {

    SUBMIT_UNCOMPLETED_VLM_MSG_MISSING_LA("Uncompleted vendor license model - cannot be submitted. \n"
            + "It must contain a license agreement(s)."),

    SUBMIT_UNCOMPLETED_VLM_MSG_LA_MISSING_FG("Uncompleted vendor license model - cannot be submitted. \n"
            + "The license agreement(s) must contain at least one feature group."),

    SUBMIT_UNCOMPLETED_VLM_MSG_FG_MISSING_EP("Uncompleted vendor license model - cannot be submitted. \n"
        + "The feature group(s) must contain at least one entitlement pool.");


    private String errorMessage;

    UncompletedVendorLicenseModelErrorType(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

