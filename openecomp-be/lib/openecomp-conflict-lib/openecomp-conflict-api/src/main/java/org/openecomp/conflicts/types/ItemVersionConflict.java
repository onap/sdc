package org.openecomp.conflicts.types;

import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.ArrayList;
import java.util.Collection;

public class ItemVersionConflict {
  private Conflict<Version> versionConflict;
  private Collection<ConflictInfo> elementConflicts = new ArrayList<>();

  public Conflict<Version> getVersionConflict() {
    return versionConflict;
  }

  public void setVersionConflict(Conflict<Version> versionConflict) {
    this.versionConflict = versionConflict;
  }

  public Collection<ConflictInfo> getElementConflicts() {
    return elementConflicts;
  }

  public void setElementConflicts(Collection<ConflictInfo> elementConflicts) {
    this.elementConflicts = elementConflicts;
  }

  public void addElementConflictInfo(ConflictInfo conflictInfo) {
    elementConflicts.add(conflictInfo);
  }
}
