package org.openecomp.sdc.versioning.types;

import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;

import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Item {
  private String id;
  private String type;
  private String name;
  private String owner;
  private ItemStatus status;
  private String description;
  private Map<String, Object> properties = new HashMap<>();
  private Map<VersionStatus, Integer> versionStatusCounters = new EnumMap<>(VersionStatus.class);
  private Date creationTime;
  private Date modificationTime;

  public void addProperty(String key, Object value) {
    properties.put(key, value);
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
}
