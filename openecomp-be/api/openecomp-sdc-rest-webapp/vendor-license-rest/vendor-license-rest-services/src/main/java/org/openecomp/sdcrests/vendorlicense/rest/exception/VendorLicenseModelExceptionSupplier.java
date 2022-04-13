/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdcrests.vendorlicense.rest.exception;

import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.vendorlicense.errors.VendorLicenseModelNotFoundErrorBuilder;

/**
 * Supplies exceptions happened for a Vendor License Model operation .
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VendorLicenseModelExceptionSupplier {

    /**
     * Provides a could not find Vendor License Model exception.
     *
     * @param vmlId the Vendor License Model id
     * @return a Supplier for the exception
     */
    public static Supplier<CoreException> couldNotFindVlm(final String vmlId) {
        final ErrorCode errorCode = new VendorLicenseModelNotFoundErrorBuilder(vmlId).build();
        return () -> new CoreException((errorCode));
    }


}
