package org.openecomp.activityspec.api.rest.types;

public class ActivitySpecCreateResponse {
  private String id;
  private String versionId;

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(String versionId) {
    this.versionId = versionId;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

}

