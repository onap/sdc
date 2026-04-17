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


import com.datastax.oss.driver.api.core.CqlSession;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openecomp.sdc.itempermissions.dao.UserPermissionsDao;
import org.openecomp.sdc.itempermissions.dao.UserPermissionsMapper;
import org.openecomp.sdc.itempermissions.dao.UserPermissionsMapperBuilder;
import org.openecomp.sdc.itempermissions.type.UserPermissionItemsEntity;

public class UserPermissionsDaoImpl implements UserPermissionsDao {

     private final UserPermissionsDao dao;

     public UserPermissionsDaoImpl(CqlSession session) {
        UserPermissionsMapper mapper = new UserPermissionsMapperBuilder(session).build();
        this.dao = mapper.userPermissionsDao();
    }

     @Override
    public Set<String> listUserPermittedItems(String userId, String permission) {
        UserPermissionItemsEntity entity = dao.getUserPermissionItems(userId, permission);
        if (entity == null || entity.getItemList() == null) {
            return new HashSet<>();
        }
        return entity.getItemList();
    }

    @Override
    public void updatePermissions(String itemId, String permission, Set<String> addedUsersIds, Set<String> removedUsersIds) {
        Set<String> itemSet = Collections.singleton(itemId);

        // Add item to each user
        addedUsersIds.forEach(userId -> {
            UserPermissionItemsEntity entity = dao.getUserPermissionItems(userId, permission);
            if (entity == null) {
                entity = new UserPermissionItemsEntity(userId, permission, itemSet);
            } else {
                Set<String> items = new HashSet<>(entity.getItemList());
                items.addAll(itemSet);
                entity.setItemList(items);
            }
            dao.save(entity);
        });

        // Remove item from each user
        removedUsersIds.forEach(userId -> {
            UserPermissionItemsEntity entity = dao.getUserPermissionItems(userId, permission);
            if (entity != null && entity.getItemList() != null) {
                Set<String> items = new HashSet<>(entity.getItemList());
                items.removeAll(itemSet);
                entity.setItemList(items);
                dao.save(entity);
            }
        });
    }

    @Override
    public UserPermissionItemsEntity getUserPermissionItems(String userId, String permission) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserPermissionItems'");
    }

    @Override
    public void save(UserPermissionItemsEntity entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public void addItems(Set<String> items, String userId, String permission) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addItems'");
    }

    @Override
    public void removeItems(Set<String> items, String userId, String permission) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeItems'");
    }
}
