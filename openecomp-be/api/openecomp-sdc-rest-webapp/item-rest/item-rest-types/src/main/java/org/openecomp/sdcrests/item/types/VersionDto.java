package org.openecomp.sdcrests.item.types;

import org.openecomp.sdc.versioning.dao.types.VersionState;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;

import java.util.Date;
import java.util.Map;

public class VersionDto {
  private String id;
  private String name;
  private String description;
  private String baseId;
  private VersionStatus status;
  private VersionState state;
  private Date creationTime;
  private Date modificationTime;
  private Map<String,Object> additionalInfo;
  public VersionDto() {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getBaseId() {
    return baseId;
  }

  public void setBaseId(String baseId) {
    this.baseId = baseId;
  }

  public VersionStatus getStatus() {
    return status;
  }

  public void setStatus(VersionStatus status) {
    this.status = status;
  }

  public VersionState getState() {
    return state;
  }

  public void setState(VersionState state) {
    this.state = state;
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

  public Map<String, Object> getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(Map<String, Object> additionalInfo) {
    this.additionalInfo = additionalInfo;
  }
}
