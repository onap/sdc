/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import java.util.Date;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Table(keyspace = "dox", name = "vsp_upload_status")
public class VspUploadStatus {

    @PartitionKey
    @Column(name = "vsp_id")
    private String vspId;

    @PartitionKey(value = 1)
    @Column(name = "vsp_version_id")
    private String vspVersionId;

    @ClusteringColumn
    @Column(name = "lock_id")
    private UUID lockId;

    @Column(name = "is_complete")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean isComplete;

    @Column(name = "status")
    private VspUploadStatusType status;

    @ClusteringColumn(value = 1)
    @Column(name = "created")
    private Date created;

    @Column(name = "updated")
    private Date updated;

    public boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(boolean complete) {
        isComplete = complete;
    }
}