package org.openecomp.sdc.itempermissions;

import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;

import java.util.Collection;
import java.util.Set;

/**
 * Created by ayalaben on 6/18/2017.
 */
public interface ItemPermissionsManager {

  Collection<ItemPermissionsEntity> listItemPermissions(String itemId);

  void updateItemPermissions(String itemId, String permission, Set<String> addedUsersIds,
                             Set<String> removedUsersIds);

  boolean isAllowed(String itemId,String userId,String action);

  String getUserItemPermiission(String itemId, String userId);


}
