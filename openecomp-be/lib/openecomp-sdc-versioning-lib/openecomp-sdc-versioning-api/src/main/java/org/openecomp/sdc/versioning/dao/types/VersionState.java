package org.openecomp.sdc.versioning.dao.types;

public class VersionState {
  private SynchronizationState synchronizationState;
  private boolean dirty;

  public SynchronizationState getSynchronizationState() {
    return synchronizationState;
  }

  public void setSynchronizationState(
      SynchronizationState synchronizationState) {
    this.synchronizationState = synchronizationState;
  }

  public boolean isDirty() {
    return dirty;
  }

  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }
}
