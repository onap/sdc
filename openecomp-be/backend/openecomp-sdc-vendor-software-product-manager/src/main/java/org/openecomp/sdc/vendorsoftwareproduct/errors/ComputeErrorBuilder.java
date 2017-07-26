package org.openecomp.sdc.vendorsoftwareproduct.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

/**
 * The Compute error builder.
 */
public class ComputeErrorBuilder {
    private static final String COMPUTE_NAME_FORMAT_MSG = "Field does not conform to predefined criteria"
            + ": name : must match %s";

    /**
     * Gets image name format error builder.
     *
     * @return the image name format error builder
     */
    public static ErrorCode getComputeNameFormatErrorBuilder(String pattern) {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.COMPUTE_NAME_FORMAT_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(COMPUTE_NAME_FORMAT_MSG, pattern));
        return builder.build();
    }
}
