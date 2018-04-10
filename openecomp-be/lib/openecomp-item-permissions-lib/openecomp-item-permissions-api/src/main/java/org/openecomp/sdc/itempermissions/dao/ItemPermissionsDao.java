package org.openecomp.sdc.itempermissions.dao;

import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;

import java.util.Collection;
import java.util.Set;

/**
 * Created by ayalaben on 6/18/2017.
 */
public interface ItemPermissionsDao {

  Collection<ItemPermissionsEntity> listItemPermissions(String itemId);

  void updateItemPermissions(String itemId, String permission, Set<String> addedUsersIds,
                             Set<String> removedUsersIds);

  String getUserItemPermission(String itemId, String userId);

  void deleteItemPermissions(String itemId);
}
