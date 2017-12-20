package org.openecomp.sdcrests.itempermissions.types;

/**
 * Created by ayalaben on 6/18/2017.
 */
public class ItemPermissionsDto {

  private String userId;
  private String permission;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String id) {
    this.userId = id;
  }

  public String getPermission() {
    return permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }

}
