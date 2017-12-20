package org.openecomp.sdcrests.conflict.types;

import java.util.ArrayList;
import java.util.Collection;

public class ItemVersionConflictDto{
  private ConflictDto conflict;
  private Collection<ConflictInfoDto> conflictInfoList = new ArrayList<>();

  public ConflictDto getConflict() {
    return conflict;
  }

  public void setConflict(ConflictDto conflict) {
    this.conflict = conflict;
  }

  public Collection<ConflictInfoDto> getConflictInfoList() {
    return conflictInfoList;
  }

  public void setConflictInfoList(Collection<ConflictInfoDto> conflictInfoList) {
    this.conflictInfoList = conflictInfoList;
  }

  public void addConflictInfo(ConflictInfoDto conflictInfo){
    conflictInfoList.add(conflictInfo);
  }
}
