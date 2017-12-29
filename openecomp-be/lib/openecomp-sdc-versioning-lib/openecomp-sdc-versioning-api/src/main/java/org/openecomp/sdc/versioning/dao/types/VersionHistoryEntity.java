/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.versioning.dao.types;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "dox", name = "version_history")
public class VersionHistoryEntity {

  @PartitionKey
  @Column(name = "entity_id")
  @Frozen
  private VersionableEntityId entityId;

  @Column(name = "active_version")
  @Frozen
  private Version version;

  private String user;
  private String description;
  private VersionType type;

  /**
   * Every entity class must have a default constructor according to
   * <a href="http://docs.datastax.com/en/developer/java-driver/2.1/manual/object_mapper/creating/">
   * Definition of mapped classes</a>.
   */
  public VersionHistoryEntity() {
    // Don't delete! Default constructor is required by DataStax driver
  }

  public VersionHistoryEntity(VersionableEntityId entityId) {
    this.entityId = entityId;
  }

  public VersionableEntityId getEntityId() {
    return entityId;
  }

  public void setEntityId(VersionableEntityId entityId) {
    this.entityId = entityId;
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public VersionType getType() {
    return type;
  }

  public void setType(VersionType type) {
    this.type = type;
  }
}
