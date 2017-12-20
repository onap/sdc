package org.openecomp.sdcrests.itempermissions.types;

import io.swagger.annotations.ApiModel;

import java.util.Set;

/**
 * Created by ayalaben on 6/20/2017.
 */

@ApiModel(value = "ItemPermissionsRequest")
public class ItemPermissionsRequestDto {

  private Set<String> addedUsersIds;
  private Set<String> removedUsersIds;

  public Set<String> getAddedUsersIds() {
    return addedUsersIds;
  }

  public void setAddedUsersIds(Set<String> addedUsersIds) {
    this.addedUsersIds = addedUsersIds;
  }

  public Set<String> getRemovedUsersIds() {
    return removedUsersIds;
  }

  public void setRemovedUsersIds(Set<String> removedUsersIds) {
    this.removedUsersIds = removedUsersIds;
  }
}
