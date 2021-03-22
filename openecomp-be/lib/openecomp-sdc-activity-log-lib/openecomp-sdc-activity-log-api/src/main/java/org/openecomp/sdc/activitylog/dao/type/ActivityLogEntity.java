/*
 * Copyright © 2016-2017 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.activitylog.dao.type;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.versioning.dao.types.Version;

@Getter
@Setter
@NoArgsConstructor
@Table(keyspace = "dox", name = "activity_log")
public class ActivityLogEntity {

    @PartitionKey
    @Column(name = "item_id")
    private String itemId;
    @ClusteringColumn(value = 1)
    @Column(name = "version_id")
    private String versionId;
    @ClusteringColumn
    @Column(name = "activity_id")
    private String id;
    private ActivityType type;
    private String user;
    private Date timestamp;
    private boolean success;
    private String message;
    private String comment;

    public ActivityLogEntity(String itemId, Version version) {
        this.itemId = itemId;
        this.versionId = version == null ? null : version.getId();
    }

    public ActivityLogEntity(String itemId, Version version, ActivityType type, String user, boolean success, String message, String comment) {
        this(itemId, version);
        this.type = type;
        this.user = user;
        this.success = success;
        this.message = message;
        this.comment = comment;
        this.timestamp = new Date();
    }
}
