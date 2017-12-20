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
    addedUsersIds.forEach(userId -> accessor.addPermission(itemId,userId,permission));
    removedUsersIds.forEach(userId -> accessor.deletePermission(itemId,userId));
  }

  @Override
  public void addUserPermission(String itemId, String userId, String permission){
    accessor.addPermission(itemId,userId,permission);
  }

  @Override
  public String getUserItemPermiission(String itemId, String userId) {

    ResultSet result =  accessor.getUserItemPermission(itemId,userId);
    if (result.getAvailableWithoutFetching() < 1) {
      return null;
    }
    return result.one().getString(0);
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

  }
}
