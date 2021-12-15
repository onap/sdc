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

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import java.util.UUID;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusRecord;

/**
 * Cassandra accessor for the {@link VspUploadStatusRecord}
 */
@Accessor
public interface VspUploadStatusRecordAccessor {

    @Query("SELECT * FROM vsp_upload_status WHERE vsp_id = ? AND vsp_version_id = ?")
    Result<VspUploadStatusRecord> findAllByVspIdAndVspVersionId(final String vspId, final String vspVersionId);

    @Query("SELECT * FROM vsp_upload_status WHERE vsp_id = ? AND vsp_version_id = ? AND is_complete = FALSE")
    Result<VspUploadStatusRecord> findAllIncomplete(final String vspId, final String vspVersionId);

    @Query("SELECT * FROM vsp_upload_status WHERE vsp_id = ? AND vsp_version_id = ? AND lock_id = ?")
    Result<VspUploadStatusRecord> findByVspIdAndVersionIdAndLockId(final String vspId, final String vspVersionId, final UUID lockId);
}
