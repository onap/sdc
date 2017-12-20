package org.openecomp.core.tools.store.zusammen.datatypes;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;

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
@Table(
        keyspace = "zusammen_dox",
        name = "version"
)
public class VersionEntity {
    @Column(name = "space")
    @PartitionKey(0)
    private String space;

    @Column(name = "item_id")
    @PartitionKey(1)
    private String itemId;

    @Column(name = "version_id")
    @PartitionKey(2)
    private String versionId;

    @Column(name = "base_version_id")
    private String baseVersionId;

    @Column(name = "creation_time")
    private Date creationTime;

    @Column(name = "info")
    private String info;

    @Column(name = "modification_time")
    private Date modificationTime;

    @Column(name = "relations")
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

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public String getRelations() {
        return relations;
    }

    public void setRelations(String relations) {
        this.relations = relations;
    }
}
