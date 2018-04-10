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

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.itempermissions.dao.UserPermissionsDao;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserPermissionsDaoImpl implements UserPermissionsDao {

    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static UserPermissionsAccessor accessor =
            noSqlDb.getMappingManager().createAccessor(UserPermissionsAccessor.class);


    @Override
    public Set<String> listUserPermittedItems(String userId, String permission) {
        ResultSet resultSet = accessor.getUserPermissionItems(userId,permission);
        if(resultSet.isExhausted()){
            return new HashSet<>();
        } else {
            return resultSet.one().getSet(0,String.class);
        }
    }

    @Override
    public void updatePermissions(String itemId, String permission, Set<String> addedUsersIds,
                                      Set<String> removedUsersIds) {
        addedUsersIds.forEach(userId ->
                accessor.addItem(Stream.of(itemId).collect(Collectors.toSet()), userId,permission));
        removedUsersIds.forEach(userId ->
                accessor.removeItem(Stream.of(itemId).collect(Collectors.toSet()),userId,permission));
    }

    @Accessor
    interface UserPermissionsAccessor {

        @Query("select item_list from dox.user_permission_items WHERE user_id = ? AND permission = ?")
        ResultSet getUserPermissionItems(String userId, String permission);

        @Query("select * from dox.user_permissions WHERE user_id = ?")
        ResultSet getUserPermissions(String userId);

        @Query("update dox.user_permission_items set item_list=item_list+? WHERE user_id = ? AND permission = ?")
        void addItem(Set<String> items, String userId, String permission);

        @Query("update dox.user_permission_items set item_list=item_list-? WHERE user_id = ? AND permission = ?")
        void removeItem(Set<String> items, String userId, String permission);

    }

}
