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

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.NIC_NAME_FORMAT_NOT_ALLOWED;

/**
 * The NIC error builder.
 */
public class NicErrorBuilder {
    private static final String NIC_NAME_FORMAT_MSG = "Field does not conform to predefined criteria"
            + ": name : must match %s";
private NicErrorBuilder() {

}
    /**
     * Gets image name format error builder.
     *
     * @return the image name format error builder
     */
    public static ErrorCode getNicNameFormatErrorBuilder(String pattern) {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(NIC_NAME_FORMAT_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(NIC_NAME_FORMAT_MSG, pattern));
        return builder.build();
    }
}
