package org.openecomp.sdc.versioning.dao.types;

public enum SynchronizationState {
  UpToDate("Up to date"),
  OutOfSync("Out of sync"),
  Merging("Merging");

  private String displayName;

  SynchronizationState(String displayName) {
    this.displayName = displayName;
  }

  public String toString() {
    return this.displayName;
  }
}
