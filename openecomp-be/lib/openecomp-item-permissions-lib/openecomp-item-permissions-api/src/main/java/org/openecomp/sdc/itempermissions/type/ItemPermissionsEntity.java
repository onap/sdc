package org.openecomp.sdc.itempermissions.type;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

/**
 * Created by ayalaben on 6/18/2017.
 */

@Table(keyspace = "dox", name = "item_permissions")
public class ItemPermissionsEntity {

  @PartitionKey
  @Column(name = "item_id")
  private String itemId;

  @ClusteringColumn
  @Column(name = "user_id")
  private String userId;

  @Column(name = "permission")
  private String permission;


  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getPermission() {
    return permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }
}
