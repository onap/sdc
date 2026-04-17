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
package org.openecomp.sdc.itempermissions.dao.impl;

import java.util.Optional;
import java.util.Set;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionMapper;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionMapperBuilder;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDao;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.PagingIterable;

/**
 * Wrapper around the generated ItemPermissionsDao to provide business logic.
 */
public class ItemPermissionsDaoImpl implements ItemPermissionsDao {

    private final ItemPermissionsDao dao;

    public ItemPermissionsDaoImpl(CqlSession session) {
        ItemPermissionMapper mapper = new ItemPermissionMapperBuilder(session).build();
        this.dao = mapper.itemPermissionsDao();
    }

    public ItemPermissionsDaoImpl(ItemPermissionsDao dao) {
        this.dao = dao;
    }

    /**
     * List all permissions for an item.
     */
    public PagingIterable<ItemPermissionsEntity> listItemPermissions(String itemId) {
        return dao.getItemPermissions(itemId);
    }

    /**
     * Update item permissions by adding and removing users.
     */
    public void updateItemPermissions(String itemId, String permission,
                                      Set<String> addedUsersIds, Set<String> removedUsersIds) {
        // Add permissions
        addedUsersIds.forEach(userId -> {
            ItemPermissionsEntity entity = new ItemPermissionsEntity(itemId, userId, permission);
            dao.addPermission(entity);
        });

        // Remove permissions only if existing permission matches
        removedUsersIds.forEach(userId ->
            dao.getUserItemPermissionEntity(itemId, userId)
               .filter(e -> permission.equals(e.getPermission()))
               .ifPresent(dao::deletePermission)
        );
    }

    /**
     * Get a single user’s permission for an item.
     */
    public Optional<String> getUserItemPermission(String itemId, String userId) {
        return dao.getUserItemPermissionEntity(itemId, userId)
                  .map(ItemPermissionsEntity::getPermission);
    }

    /**
     * Delete all permissions for an item.
     */
    public void deleteItemPermissions(String itemId) {
        dao.deleteItemPermissions(itemId);
    }

    @Override
    public PagingIterable<ItemPermissionsEntity> getItemPermissions(String itemId) {
        return dao.getItemPermissions(itemId);
    }

    @Override
    public Optional<ItemPermissionsEntity> getUserItemPermissionEntity(String itemId, String userId) {
        return dao.getUserItemPermissionEntity(itemId, userId);
    }

    @Override
    public void addPermission(ItemPermissionsEntity entity) {
       dao.addPermission(entity);
    }

    @Override
    public void deletePermission(ItemPermissionsEntity entity) {
        dao.deletePermission(entity);
    }
}
