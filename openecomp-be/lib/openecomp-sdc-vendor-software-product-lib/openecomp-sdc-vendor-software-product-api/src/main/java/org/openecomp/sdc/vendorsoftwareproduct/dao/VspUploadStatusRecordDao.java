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

package org.openecomp.sdc.vendorsoftwareproduct.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusRecord;

/**
 * Data Access Object for the package upload process status record.
 */
public interface VspUploadStatusRecordDao {

    /**
     * Creates an upload status record.
     *
     * @param vspUploadStatusRecord the upload status record to create
     */
    void create(final VspUploadStatusRecord vspUploadStatusRecord);

    /**
     * Updates an upload status record.
     *
     * @param vspUploadStatusRecord the upload status record to update
     */
    void update(final VspUploadStatusRecord vspUploadStatusRecord);

    /**
     * Finds all upload status record by Vendor Software Product id and its version id.
     *
     * @param vspId        the Vendor Software Product id
     * @param vspVersionId the Vendor Software Product version id
     * @return a list with all the status record found that matches the criteria
     */
    List<VspUploadStatusRecord> findAllByVspIdAndVersionId(final String vspId, final String vspVersionId);

    /**
     * Finds all upload status record by Vendor Software Product id and its version id.
     *
     * @param vspId        the Vendor Software Product id
     * @param vspVersionId the Vendor Software Product version id
     * @return a list with all the status record found that matches the criteria
     */
    Optional<VspUploadStatusRecord> findByVspIdAndVersionIdAndLockId(final String vspId, final String vspVersionId, final UUID lockId);

    /**
     * Finds all uploads in progress by Vendor Software Product id and its version id.
     *
     * @param vspId        the Vendor Software Product id
     * @param vspVersionId the Vendor Software Product version id
     * @return a list with all the status record found that matches the criteria
     */
    List<VspUploadStatusRecord> findAllInProgress(final String vspId, final String vspVersionId);

    /**
     * Finds the latest upload status record for the Vendor Software Product id and its version id.
     *
     * @param vspId        the Vendor Software Product id
     * @param vspVersionId the Vendor Software Product version id
     * @return the latest upload status record that matches the criteria
     */
    Optional<VspUploadStatusRecord> findLatest(final String vspId, final String vspVersionId);

}
