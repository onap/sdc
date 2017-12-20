package org.openecomp.sdcrests.conflict.types;

import org.openecomp.sdc.datatypes.model.ElementType;

public class ConflictInfoDto {
  private String id;
  private ElementType type;
  private String name;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ElementType getType() {
    return type;
  }

  public void setType(ElementType type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
