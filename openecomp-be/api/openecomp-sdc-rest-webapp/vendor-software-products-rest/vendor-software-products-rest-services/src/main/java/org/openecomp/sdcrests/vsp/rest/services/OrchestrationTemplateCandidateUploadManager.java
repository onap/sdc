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

package org.openecomp.sdcrests.vsp.rest.services;

import java.util.Optional;
import java.util.UUID;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusType;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspUploadStatusDto;

public interface OrchestrationTemplateCandidateUploadManager {

    /**
     * Creates a lock to start uploading a package.
     *
     * @param vspId        the Vendor Software Product id
     * @param vspVersionId the Vendor Software Product version id
     * @param user         the current user
     * @return a new upload status containing the lock
     */
    VspUploadStatusDto startUpload(String vspId, String vspVersionId, String user);

    /**
     * Finishes the upload process, applying the given VspUploadStatusType completion status.
     *
     * @param vspId          the Vendor Software Product id
     * @param vspVersionId   the Vendor Software Product version id
     * @param lockId         the upload lock id
     * @param completeStatus any complete status
     * @param user           the current user
     * @return the updated status
     */
    VspUploadStatusDto finishUpload(final String vspId, final String vspVersionId, final UUID lockId, final VspUploadStatusType completeStatus,
                                    final String user);

    /**
     * Finds the latest upload status for a given Vendor Software Product version.
     *
     * @param vspId        the Vendor Software Product id
     * @param vspVersionId the Vendor Software Product version id
     * @param user         the current user
     * @return the latest upload status for the requested Vendor Software Product version
     */
    Optional<VspUploadStatusDto> findLatestStatus(String vspId, String vspVersionId, String user);
}
