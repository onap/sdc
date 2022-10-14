/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.vendorlicense.errors;

import java.util.List;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class LicensingDataInvalidErrorBuilder {

    private static final String LICENSING_DATA_INVALID_MSG = "Invalid licensing data: %s";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    /**
     * Instantiates a new Licensing data invalid error builder.
     *
     * @param licensingDataErrors the licensing data errors
     */
    public LicensingDataInvalidErrorBuilder(List<String> licensingDataErrors) {
        builder.withId(VendorLicenseErrorCodes.LICENSING_DATA_INVALID);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(LICENSING_DATA_INVALID_MSG, CommonMethods.listToSeparatedString(licensingDataErrors, ',')));
    }

    public ErrorCode build() {
        return builder.build();
    }
}
