package org.openecomp.sdc.itempermissions.type;

import java.util.Set;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;

@Entity
@CqlName("user_permission_items") 
public class UserPermissionItemsEntity {

    @PartitionKey
    private String userId;

    @ClusteringColumn
    private String permission;

    @CqlName("item_list")
    private Set<String> itemList;

    public UserPermissionItemsEntity() { } // default constructor

    public UserPermissionItemsEntity(String userId, String permission, Set<String> itemList) {
        this.userId = userId;
        this.permission = permission;
        this.itemList = itemList;
    }

    // Getters & Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public Set<String> getItemList() { return itemList; }
    public void setItemList(Set<String> itemList) { this.itemList = itemList; }
}

