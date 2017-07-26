package org.openecomp.sdc.vendorsoftwareproduct.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.NIC_NAME_FORMAT_NOT_ALLOWED;

/**
 * The NIC error builder.
 */
public class NicErrorBuilder {
    private static final String NIC_NAME_FORMAT_MSG = "Field does not conform to predefined criteria"
            + ": name : must match %s";

    /**
     * Gets image name format error builder.
     *
     * @return the image name format error builder
     */
    public static ErrorCode getNicNameFormatErrorBuilder(String imageName, String pattern) {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(NIC_NAME_FORMAT_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(NIC_NAME_FORMAT_MSG, pattern));
        return builder.build();
    }
}
