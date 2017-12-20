package org.openecomp.core.zusammen.plugin.dao.types;

import com.amdocs.zusammen.datatypes.Id;

import java.util.Date;

public class VersionEntity {
  private Id id;
  private Id baseId;
  private Date creationTime;
  private Date modificationTime;

  public VersionEntity(Id id) {
    this.id = id;
  }

  public Id getId() {
    return id;
  }

  public Id getBaseId() {
    return baseId;
  }

  public void setBaseId(Id baseId) {
    this.baseId = baseId;
  }

  public Date getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(Date creationTime) {
    this.creationTime = creationTime;
  }

  public Date getModificationTime() {
    return modificationTime;
  }

  public void setModificationTime(Date modificationTime) {
    this.modificationTime = modificationTime;
  }

}
