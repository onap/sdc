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
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDao;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;

import java.util.Collection;
import java.util.Set;

/**
 * Created by ayalaben on 6/20/2017.
 */
public class ItemPermissionsDaoImpl implements ItemPermissionsDao {
  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static ItemPermissionsAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(ItemPermissionsAccessor.class);

  @Override
  public Collection<ItemPermissionsEntity> listItemPermissions(String itemId) {
    return accessor.getItemPermissions(itemId).all();
  }

  @Override
  public void updateItemPermissions(String itemId, String permission, Set<String> addedUsersIds,
                                    Set<String> removedUsersIds) {
    addedUsersIds.forEach(userId ->  accessor.addPermission(itemId,userId,permission));
    removedUsersIds.forEach(userId -> accessor.deletePermission(itemId,userId));
  }

  @Override
  public String getUserItemPermission(String itemId, String userId) {

    ResultSet result =  accessor.getUserItemPermission(itemId,userId);
    if (result.getAvailableWithoutFetching() < 1) {
      return null;
    }
    return result.one().getString(0);
  }

  @Override
  public void deleteItemPermissions(String itemId) {
   accessor.deleteItemPermissions(itemId);
  }


  @Accessor
  interface ItemPermissionsAccessor {
    @Query("select * from dox.item_permissions WHERE item_id = ?")
    Result<ItemPermissionsEntity> getItemPermissions(String itemId);

    @Query("select permission from dox.item_permissions WHERE item_id = ? AND user_id=?")
    ResultSet getUserItemPermission(String itemId,String userId);

    @Query("delete from dox.item_permissions where item_id = ? and user_id = ?")
    void deletePermission(String itemId, String userId);

    @Query("insert into dox.item_permissions (item_id,user_id,permission) values (?,?,?)")
    void addPermission(String itemId,String userId, String permission);

    @Query("delete from dox.item_permissions where item_id=?")
    void deleteItemPermissions(String itemId);

  }
}
