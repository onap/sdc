/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.itempermissions.dao;

import java.util.Set;

import org.openecomp.sdc.itempermissions.type.UserPermissionItemsEntity;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.Select;

@Dao
public interface UserPermissionsDao {

    
    @Select
    UserPermissionItemsEntity getUserPermissionItems(String userId, String permission);

    @Insert
    void save(UserPermissionItemsEntity entity);

    @Query("update user_permission_items set item_list = item_list + :items where user_id = :userId and permission = :permission")
    void addItems(Set<String> items, String userId, String permission);

    @Query("update user_permission_items set item_list = item_list - :items where user_id = :userId and permission = :permission")
    void removeItems(Set<String> items, String userId, String permission);


    default Set<String> listUserPermittedItems(String userId, String permission) {
        UserPermissionItemsEntity entity = getUserPermissionItems(userId, permission);
        return entity == null ? Set.of() : entity.getItemList();
    }


    default void updatePermissions(String itemId, String permission, Set<String> addedUserIds, Set<String> removedUserIds) {
        if (addedUserIds != null && !addedUserIds.isEmpty()) {
            addItems(addedUserIds, itemId, permission);
        }
        if (removedUserIds != null && !removedUserIds.isEmpty()) {
            removeItems(removedUserIds, itemId, permission);
        }
    }
}
