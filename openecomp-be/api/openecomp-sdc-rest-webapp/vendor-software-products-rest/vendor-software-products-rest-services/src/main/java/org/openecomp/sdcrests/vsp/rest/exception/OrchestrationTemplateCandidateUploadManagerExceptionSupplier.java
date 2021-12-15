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

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_CREATE_UPLOAD_LOCK_ERROR;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_PROCESSING_IN_PROGRESS;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_UPDATE_UPLOAD_LOCK_ERROR;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_UPLOAD_ALREADY_FINISHED_ERROR;

import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.ErrorCode.ErrorCodeBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.dao.errors.VendorSoftwareProductNotFoundErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusType;

public class OrchestrationTemplateCandidateUploadManagerExceptionSupplier {

    private OrchestrationTemplateCandidateUploadManagerExceptionSupplier() {
    }

    public static Supplier<CoreException> vspUploadAlreadyInProgress(final String vspId, final String vspVersionId) {
        final String errorMsg = String.format("There is a processing in progress for the VSP %s, version %s", vspId, vspVersionId);
        return () -> new CoreException(new ErrorCodeBuilder().withId(VSP_PROCESSING_IN_PROGRESS).withMessage(errorMsg).build());
    }

    public static Supplier<CoreException> couldNotCreateLock(final String vspId, final String vspVersionId, final Exception exception) {
        final String errorMsg = String.format("Could not create a lock for the VSP %s, version %s", vspId, vspVersionId);
        final ErrorCode errorCode = new ErrorCodeBuilder().withId(VSP_CREATE_UPLOAD_LOCK_ERROR).withMessage(errorMsg).build();
        return () -> new CoreException(errorCode, exception);
    }

    public static Supplier<CoreException> couldNotUpdateLock(final UUID lockId, final String vspId, final String vspVersionId, final Exception exception) {
        final String errorMsg = String.format("Could not update the lock %s for the VSP %s, version %s", lockId, vspId, vspVersionId);
        final ErrorCode errorCode = new ErrorCodeBuilder().withId(VSP_UPDATE_UPLOAD_LOCK_ERROR).withMessage(errorMsg).build();
        return () -> new CoreException(errorCode, exception);
    }

    public static Supplier<CoreException> couldNotFindLock(final UUID lockId, final String vspId, final String vspVersionId) {
        final String errorMsg = String.format("Could not find lock '%s' for the VSP %s, version %s", lockId, vspId, vspVersionId);
        final ErrorCode errorCode = new ErrorCodeBuilder().withId(VSP_CREATE_UPLOAD_LOCK_ERROR).withMessage(errorMsg).build();
        return () -> new CoreException(errorCode);
    }

    public static Supplier<CoreException> uploadAlreadyFinished(final UUID lockId, final String vspId, final String vspVersionId) {
        final String errorMsg = String.format("The upload was already finished for lock '%s', VSP '%s', version '%s'", lockId, vspId, vspVersionId);
        final ErrorCode errorCode = new ErrorCodeBuilder().withId(VSP_UPLOAD_ALREADY_FINISHED_ERROR).withMessage(errorMsg).build();
        return () -> new CoreException(errorCode);
    }

    public static Supplier<CoreException> vspNotFound(final String vspId, final String vspVersionId) {
        return () -> new CoreException(new VendorSoftwareProductNotFoundErrorBuilder(vspId, vspVersionId).build());
    }

    public static Supplier<IllegalArgumentException> invalidCompleteStatus(final VspUploadStatusType status) {
        String errorMsg = String.format("Invalid complete status '%s'. Expecting one of: %s",
            status,
            VspUploadStatusType.getCompleteStatus().stream().map(Enum::name).collect(Collectors.joining(", "))
        );
        return () -> new IllegalArgumentException(errorMsg);
    }
}
