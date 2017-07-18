package org.openecomp.sdc.vendorsoftwareproduct.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class DeleteNicErrorBuilder {
    private static final String DELETE_NIC_NOT_ALLOWED_MSG =
            "NIC cannot be deleted for VSPs onboarded with HEAT.";

    public static ErrorCode getDeleteNicForHeatOnboardedVspErrorBuilder(){
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.DELETE_NIC_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(DELETE_NIC_NOT_ALLOWED_MSG));
        return builder.build();
    }

}
