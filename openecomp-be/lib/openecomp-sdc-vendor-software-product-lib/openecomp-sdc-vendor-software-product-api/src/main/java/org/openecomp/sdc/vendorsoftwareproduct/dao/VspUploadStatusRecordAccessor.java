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

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;

/**
 * Cassandra accessor for the {@link VspUploadStatusRecord}
 */
@Dao
public interface VspUploadStatusRecordAccessor {

    @Select(customWhereClause = "vsp_id = :vspId AND vsp_version_id = :vspVersionId")
    List<VspUploadStatusRecord> findAllByVspIdAndVspVersionId(String vspId, String vspVersionId);

    @Select(customWhereClause = "vsp_id = :vspId AND vsp_version_id = :vspVersionId AND is_complete = false")
    List<VspUploadStatusRecord> findAllIncomplete(String vspId, String vspVersionId);

    @Select(customWhereClause = "vsp_id = :vspId AND vsp_version_id = :vspVersionId AND lock_id = :lockId")
    Optional<VspUploadStatusRecord> findByVspIdAndVersionIdAndLockId(String vspId, String vspVersionId, UUID lockId);
}
