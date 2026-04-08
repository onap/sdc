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

package org.openecomp.sdc.vendorsoftwareproduct.dao.type;


import java.time.Instant;
import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.Transient;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Entity
@CqlName("vsp_upload_status")
public class VspUploadStatusRecord {

    @PartitionKey
    @CqlName("vsp_id")
    private String vspId;

    @PartitionKey(value = 1)
    @CqlName("vsp_version_id")
    private String vspVersionId;

    @ClusteringColumn
    @CqlName("lock_id")
    private UUID lockId;

    @CqlName("is_complete")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean isComplete;

    @CqlName("status")
    private String status;

    @ClusteringColumn(value = 1)
    @CqlName("created")
    private Instant created;

    @CqlName("updated")
    private Instant updated;

    public boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(boolean complete) {
        isComplete = complete;
    }

    @Transient
    public VspUploadStatus getStatusEnum() {
        return status == null ? null : VspUploadStatus.valueOf(status);
    }

    @Transient
    public void setStatusEnum(VspUploadStatus s) {
        status = (s == null ? null : s.name());
    }
}