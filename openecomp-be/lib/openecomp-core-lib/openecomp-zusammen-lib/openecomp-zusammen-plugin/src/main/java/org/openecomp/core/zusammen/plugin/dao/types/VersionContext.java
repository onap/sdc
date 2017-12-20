package org.openecomp.core.zusammen.plugin.dao.types;

import com.amdocs.zusammen.datatypes.Id;

public class VersionContext {
  private String space;
  private Id itemId;


  public VersionContext(String space, Id itemId) {
    this.space = space;
    this.itemId = itemId;
  }

  public String getSpace() {
    return space;
  }

  public Id getItemId() {
    return itemId;
  }

}
