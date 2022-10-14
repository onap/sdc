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

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_DELETE_ALREADY_IN_USE_BY_VF;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_DELETE_FROM_DATABASE_ERROR;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_DELETE_FROM_STORAGE_ERROR;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_DELETE_GENERIC_ERROR;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_DELETE_NOT_ARCHIVED;

import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode.ErrorCodeBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.dao.errors.VendorSoftwareProductNotFoundErrorBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VendorSoftwareProductsExceptionSupplier {

    public static Supplier<CoreException> vspNotFound(final String vspId) {
        final VendorSoftwareProductNotFoundErrorBuilder errorBuilder = new VendorSoftwareProductNotFoundErrorBuilder(vspId);
        return () -> new CoreException((errorBuilder.build()));
    }

    public static Supplier<CoreException> vspInUseByVf(final String vfName) {
        final String errorMsg = Messages.DELETE_VSP_ERROR_USED_BY_VF.formatMessage(vfName, vfName);
        final ErrorCodeBuilder errorBuilder =
            new ErrorCodeBuilder().withId(VSP_DELETE_ALREADY_IN_USE_BY_VF)
                .withCategory(ErrorCategory.USER)
                .withMessage(errorMsg);
        return () -> new CoreException(errorBuilder.build());
    }

    public static Supplier<CoreException> deleteGenericError(final String vspId) {
        final String errorMsg = String.format("An error has occurred while trying to delete the VSP '%s'.", vspId);
        final ErrorCodeBuilder errorBuilder =
            new ErrorCodeBuilder().withId(VSP_DELETE_GENERIC_ERROR)
                .withCategory(ErrorCategory.SYSTEM)
                .withMessage(errorMsg);
        return () -> new CoreException(errorBuilder.build());
    }

    public static Supplier<CoreException> deleteNotArchivedVsp(final String vspId) {
        final String errorMsg = Messages.DELETE_NOT_ARCHIVED_VSP_ERROR.formatMessage(vspId);
        final ErrorCodeBuilder errorBuilder =
            new ErrorCodeBuilder().withId(VSP_DELETE_NOT_ARCHIVED)
                .withCategory(ErrorCategory.USER)
                .withMessage(errorMsg);
        return () -> new CoreException(errorBuilder.build());
    }

    public static Supplier<CoreException> deleteVspFromStorageFailure(final String vspId) {
        final String errorMsg = Messages.DELETE_VSP_FROM_STORAGE_ERROR.formatMessage(vspId);
        final ErrorCodeBuilder errorBuilder =
            new ErrorCodeBuilder().withId(VSP_DELETE_FROM_STORAGE_ERROR)
                .withCategory(ErrorCategory.SYSTEM)
                .withMessage(errorMsg);
        return () -> new CoreException(errorBuilder.build());
    }

    public static Supplier<CoreException> deleteVspFromDatabaseFailure(final String vspId) {
        final String errorMsg = Messages.DELETE_VSP_ERROR.formatMessage(vspId);
        final ErrorCodeBuilder errorBuilder =
            new ErrorCodeBuilder().withId(VSP_DELETE_FROM_DATABASE_ERROR)
                .withCategory(ErrorCategory.SYSTEM)
                .withMessage(errorMsg);
        return () -> new CoreException(errorBuilder.build());
    }
}
