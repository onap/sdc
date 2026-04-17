/*
 * Copyright © 2016-2018 European Support Limited
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
package org.openecomp.core.tools.store.zusammen.datatypes;


import java.util.Map;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

/**
 * CREATE TABLE zusammen_dox.version_elements (
 * space text,
 * item_id text,
 * version_id text,
 * revision_id text,
 * conflict_element_ids set<text>,
 * dirty_element_ids set<text>,
 * element_ids map<text, text>,
 * message text,
 * publish_time timestamp,
 * stage_element_ids set<text>,
 * user text,
 * PRIMARY KEY ((space, item_id, version_id), revision_id))
 * WITH CLUSTERING ORDER BY (revision_id ASC)
 */
@Entity
public class VersionElementsEntity {

    @CqlName("space")
    @PartitionKey(0)
    private String space;
    @CqlName("item_id")
    @PartitionKey(1)
    private String itemId;
    @CqlName("version_id")
    @PartitionKey(2)
    private String versionId;
    @CqlName("revision_id")
    @ClusteringColumn
    private String revisionId;
    @CqlName("element_ids")
    private Map<String, String> elementIds;

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

    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    public Map<String, String> getElementIds() {
        return elementIds;
    }

    public void setElementIds(Map<String, String> elementIds) {
        this.elementIds = elementIds;
    }
}
