package org.openecomp.sdc.versioning.types;

import org.openecomp.sdc.versioning.dao.types.VersionStatus;

import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Item {
  private String id;
  private String type;
  private String name;
  private String description;
  private Map<String, Object> properties = new HashMap<>();
  private Map<VersionStatus, Integer> versionStatusCounters = new EnumMap<>(VersionStatus.class);
  private Date creationTime;
  private Date modificationTime;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
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

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void addProperty(String key, Object value) {
    properties.put(key, value);
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  public Map<VersionStatus, Integer> getVersionStatusCounters() {
    return versionStatusCounters;
  }

  public void setVersionStatusCounters(Map<VersionStatus, Integer> versionStatusCounters) {
    this.versionStatusCounters = versionStatusCounters;
  }

  public void addVersionStatus(VersionStatus versionStatus) {
    Integer counter = versionStatusCounters.get(versionStatus);
    versionStatusCounters
        .put(versionStatus, counter == null ? 1 : counter + 1);
  }

  public void removeVersionStatus(VersionStatus versionStatus) {
    Integer counter = versionStatusCounters.get(versionStatus);
    if (counter != null) {
      if (counter == 1) {
        versionStatusCounters.remove(versionStatus);
      } else {
        versionStatusCounters.put(versionStatus, counter - 1);
      }
    }
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
