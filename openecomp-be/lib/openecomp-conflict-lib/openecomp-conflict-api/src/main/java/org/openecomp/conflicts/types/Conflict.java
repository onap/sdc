package org.openecomp.conflicts.types;

import org.openecomp.sdc.datatypes.model.ElementType;

public class Conflict<T> extends ConflictInfo {
  private T yours;
  private T theirs;

  public Conflict(String id, ElementType type, String name) {
    super(id, type, name);
  }

  public T getYours() {
    return yours;
  }

  public void setYours(T yours) {
    this.yours = yours;
  }

  public T getTheirs() {
    return theirs;
  }

  public void setTheirs(T theirs) {
    this.theirs = theirs;
  }


}
