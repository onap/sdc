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
package org.openecomp.core.tools.store.zusammen.datatypes;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

/**
 * Created by ayalaben on 10/15/2017
 */
@Table(keyspace = "dox", name = "healing")
public class HealingEntity {

  @Column(name = "space")
  @PartitionKey(0)
  private String space;

  @Column(name = "item_id")
  @PartitionKey(1)
  private String itemId;

  @Column(name = "version_id")
  @PartitionKey(2)
  private String versionId;

  @Column(name = "healing_needed")
  private boolean healingFlag;

  @Column(name = "old_version")
  private String oldVersion;

  /**
   * Every entity class must have a default constructor according to
   * <a href="http://docs.datastax.com/en/developer/java-driver/2.1/manual/object_mapper/creating/">
   * Definition of mapped classes</a>.
   */
  public HealingEntity() {
    // Don't delete! Default constructor is required by DataStax driver
  }

  public HealingEntity(String space, String itemId, String versionId, boolean healingFlag,
                       String oldVersion) {
    this.space = space;
    this.itemId = itemId;
    this.versionId = versionId;
    this.healingFlag = healingFlag;
    this.oldVersion = oldVersion;
  }

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

  public boolean getHealingFlag() {
    return healingFlag;
  }

  public void setHealingFlag(boolean healingFlag) {
    this.healingFlag = healingFlag;
  }

  public String getOldVersion() {
    return oldVersion;
  }

  public void setOldVersion(String oldVersion) {
    this.oldVersion = oldVersion;
  }
}