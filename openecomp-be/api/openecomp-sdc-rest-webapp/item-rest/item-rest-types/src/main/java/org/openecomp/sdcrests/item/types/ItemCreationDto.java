package org.openecomp.sdcrests.item.types;

public class ItemCreationDto {
  private String itemId;
  private VersionDto version;

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public VersionDto getVersion() {
    return version;
  }

  public void setVersion(VersionDto version) {
    this.version = version;
  }
}
