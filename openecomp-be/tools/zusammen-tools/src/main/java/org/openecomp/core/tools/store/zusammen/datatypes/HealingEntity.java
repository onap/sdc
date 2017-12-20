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
  private String old_version;

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

  public HealingEntity(String space, String itemId, String versionId, boolean healingFlag,String
      oldVersion) {
    this.space = space;
    this.itemId = itemId;
    this.versionId = versionId;
    this.healingFlag = healingFlag;
    this.old_version = oldVersion;
  }

  public String getOldVersion() {
    return old_version;
  }

  public void setOldVersion(String old_version) {
    this.old_version = old_version;
  }
}