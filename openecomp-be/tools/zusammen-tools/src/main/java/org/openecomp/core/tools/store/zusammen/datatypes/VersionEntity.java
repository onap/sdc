/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */
package org.openecomp.core.tools.store.zusammen.datatypes;


import java.time.Instant;
import java.util.Date;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

/**
 * CREATE TABLE zusammen_dox.version (
 * space text,
 * item_id text,
 * version_id text,
 * base_version_id text,
 * creation_time timestamp,
 * info text,
 * modification_time timestamp,
 * relations text,
 * PRIMARY KEY ((space, item_id), version_id)
 * ) WITH CLUSTERING ORDER BY (version_id ASC)
 * AND bloom_filter_fp_chance = 0.01
 * AND caching = '{"keys":"ALL", "rows_per_partition":"NONE"}'
 * AND comment = ''
 * AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy'}
 * AND compression = {'sstable_compression': 'org.apache.cassandra.io.compress.LZ4Compressor'}
 * AND dclocal_read_repair_chance = 0.1
 * AND default_time_to_live = 0
 * AND gc_grace_seconds = 864000
 * AND max_index_interval = 2048
 * AND memtable_flush_period_in_ms = 0
 * AND min_index_interval = 128
 * AND read_repair_chance = 0.0
 * AND speculative_retry = '99.0PERCENTILE';
 */
@Entity
public class VersionEntity {

    @CqlName("space")
    @PartitionKey(0)
    private String space;
    @CqlName("item_id")
    @PartitionKey(1)
    private String itemId;
    @CqlName("version_id")
    @PartitionKey(2)
    private String versionId;
    @CqlName("base_version_id")
    private String baseVersionId;
    @CqlName("creation_time")
    private Instant creationTime;
    @CqlName("info")
    private String info;
    @CqlName("modification_time")
    private Instant modificationTime;
    @CqlName("relations")
    private String relations;

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getBaseVersionId() {
        return baseVersionId;
    }

    public void setBaseVersionId(String baseVersionId) {
        this.baseVersionId = baseVersionId;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Instant getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Instant modificationTime) {
        this.modificationTime = modificationTime;
    }

    public String getRelations() {
        return relations;
    }

    public void setRelations(String relations) {
        this.relations = relations;
    }
}
