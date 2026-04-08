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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.Select;

/**
 * Created by ayalaben on 6/18/2017.
 */
@Dao
public interface ItemPermissionsDao {

    // Fetch all permissions for an item
    @Select(customWhereClause = "item_id = :itemId")
    PagingIterable<ItemPermissionsEntity> getItemPermissions(String itemId);

    // Fetch a single user’s permission on an item
    @Select
    Optional<ItemPermissionsEntity> getUserItemPermissionEntity(String itemId,
                                                                String userId);

    // Insert or update a permission
    @Insert
    void addPermission(ItemPermissionsEntity entity);

    // Delete a specific permission
    @Delete(entityClass = ItemPermissionsEntity.class)
    void deletePermission(ItemPermissionsEntity entity);

    // Delete all permissions for an item
    @Query("DELETE FROM item_permissions WHERE item_id = :itemId")
    void deleteItemPermissions(String itemId);
    
}
