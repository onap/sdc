package org.openecomp.sdcrests.conflict.types;

import java.util.Map;

public class ConflictDto extends ConflictInfoDto {
  private Map<String, Object> yours;
  private Map<String, Object> theirs;

  public Map<String, Object> getYours() {
    return yours;
  }

  public void setYours(Map<String, Object> yours) {
    this.yours = yours;
  }

  public Map<String, Object> getTheirs() {
    return theirs;
  }

  public void setTheirs(Map<String, Object> theirs) {
    this.theirs = theirs;
  }
}
