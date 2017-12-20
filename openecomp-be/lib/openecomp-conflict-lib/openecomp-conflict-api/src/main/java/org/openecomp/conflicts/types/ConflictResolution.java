package org.openecomp.conflicts.types;

import java.util.Map;

public class ConflictResolution {
  private Resolution resolution;
  // sits in lower level...
  private Map<String, Object> otherResolution;

  public ConflictResolution() {
  }

  public ConflictResolution(Resolution resolution) {
    this.resolution = resolution;
  }

  public Resolution getResolution() {
    return resolution;
  }

  public void setResolution(Resolution resolution) {
    this.resolution = resolution;
  }

  public Map<String, Object> getOtherResolution() {
    return otherResolution;
  }

  public void setOtherResolution(Map<String, Object> otherResolution) {
    this.otherResolution = otherResolution;
  }
}
