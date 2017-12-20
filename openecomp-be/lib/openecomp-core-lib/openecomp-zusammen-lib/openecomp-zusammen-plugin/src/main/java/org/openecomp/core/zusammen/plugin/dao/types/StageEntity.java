package org.openecomp.core.zusammen.plugin.dao.types;

import com.amdocs.zusammen.datatypes.item.Action;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

public class StageEntity<E> {
  private E entity;
  private Date publishTime;
  private Action action = Action.IGNORE;
  private boolean conflicted;
  private Set<E> conflictDependents = Collections.emptySet();

  // used by sync on stage creation
  public StageEntity(E entity, Date publishTime) {
    this.entity = entity;
    this.publishTime = publishTime;
  }

  public StageEntity(E entity, Date publishTime, Action action, boolean conflicted) {
    this.entity = entity;
    this.publishTime = publishTime;
    this.action = action;
    this.conflicted = conflicted;
  }

  public E getEntity() {
    return entity;
  }

  public Date getPublishTime() {
    return publishTime;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public boolean isConflicted() {
    return conflicted;
  }

  public void setConflicted(boolean conflicted) {
    this.conflicted = conflicted;
  }

  public Set<E> getConflictDependents() {
    return conflictDependents;
  }

  public void setConflictDependents(Set<E> conflictDependents) {
    this.conflictDependents = conflictDependents;
  }
}
