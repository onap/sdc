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
import com.datastax.driver.mapping.annotations.Enumerated;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Date;

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
  @Enumerated
  private ActivityType type;
  private String user;
  private Date timestamp;
  private boolean success;
  private String message;
  private String comment;

  public ActivityLogEntity() {
  }

  public ActivityLogEntity(String itemId, Version version) {
    this.itemId = itemId;
    setVersion(version);
  }

  public ActivityLogEntity(String itemId, Version version, ActivityType type, String user,
                           boolean success, String message, String comment) {
    this(itemId, version);
    this.type = type;
    this.user = user;
    this.success = success;
    this.message = message;
    this.comment = comment;
    this.timestamp = new Date();
  }

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public Version getVersion() {
    return versionId == null ? null : new Version(versionId);
  }

  public void setVersion(Version version) {
    this.versionId = version == null ? null : version.getId();
  }

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(String versionId) {
    this.versionId = versionId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ActivityType getType() {
    return type;
  }

  public void setType(ActivityType type) {
    this.type = type;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
