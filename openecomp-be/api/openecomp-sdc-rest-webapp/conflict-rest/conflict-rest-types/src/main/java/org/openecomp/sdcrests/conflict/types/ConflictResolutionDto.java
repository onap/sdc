package org.openecomp.sdcrests.conflict.types;

import org.openecomp.conflicts.types.Resolution;

import java.util.Map;

public class ConflictResolutionDto {
  private Resolution resolution;
  // sits in lower level...
  private Map<String, Object> otherResolution;

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
