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

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.NETWORK_DESCRIPTION_NOT_ALLOWED_FOR_INTERNAL_NETWORK;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.NETWORK_TYPE_UPDATE_NOT_ALLOWED;

import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class NicInternalNetworkErrorBuilder {

    private static final String NULL_NETWORKID_NOT_ALLOWED_MSG = "Internal Networks are currently not supported for VSP created Manually, so please fix all the NIC to be of Type External and re-submit the VSP.";
    private static final String NETWORK_DESCRIPTION_NOT_ALLOWED_FOR_INTERNAL_NETWORK_MSG = "Invalid request, Network Description not allowed for Internal Networks";
    private static final String NETWORK_TYPE_UPDATE_NOT_ALLOWED_MSG = "Invalid request, Network Type Update not allowed for a Nic";

    private NicInternalNetworkErrorBuilder() {
    }

    public static ErrorCode getNicNullNetworkIdInternalNetworkIdErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(VendorSoftwareProductErrorCodes.NULL_NETWORKID_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(NULL_NETWORKID_NOT_ALLOWED_MSG);
        return builder.build();
    }

    public static ErrorCode getNetworkDescriptionInternalNetworkErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(NETWORK_DESCRIPTION_NOT_ALLOWED_FOR_INTERNAL_NETWORK);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(NETWORK_DESCRIPTION_NOT_ALLOWED_FOR_INTERNAL_NETWORK_MSG);
        return builder.build();
    }

    public static ErrorCode getNetworkTypeErrorBuilder() {
        ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();
        builder.withId(NETWORK_TYPE_UPDATE_NOT_ALLOWED);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(NETWORK_TYPE_UPDATE_NOT_ALLOWED_MSG);
        return builder.build();
    }
}
