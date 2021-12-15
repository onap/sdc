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

package org.openecomp.sdcrests.vsp.rest.exception;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_PROCESSING_IN_PROGRESS;

import java.util.function.Supplier;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.ErrorCode.ErrorCodeBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.dao.errors.VendorSoftwareProductNotFoundErrorBuilder;

public class PackageUploadManagerExceptionSupplier {

    private PackageUploadManagerExceptionSupplier() {
    }

    public static Supplier<CoreException> vspUploadAlreadyInProgress(final String vspId, final String vspVersionId) {
        final String errorMsg = String.format("There is a processing in progress for the VSP %s, version %s", vspId, vspVersionId);
        return () -> new CoreException(new ErrorCodeBuilder().withId(VSP_PROCESSING_IN_PROGRESS).withMessage(errorMsg).build());
    }

    public static Supplier<CoreException> couldNotCreateLock(final String vspId, final String vspVersionId, final Exception exception) {
        final String errorMsg = String.format("There is a processing in progress for the VSP %s, version %s", vspId, vspVersionId);
        final ErrorCode errorCode = new ErrorCodeBuilder().withId(VSP_PROCESSING_IN_PROGRESS).withMessage(errorMsg).build();
        return () -> new CoreException(errorCode, exception);
    }

    public static Supplier<CoreException> vspNotFound(final String vspId, final String vspVersionId) {
        return () -> new CoreException(new VendorSoftwareProductNotFoundErrorBuilder(vspId, vspVersionId).build());
    }
}
