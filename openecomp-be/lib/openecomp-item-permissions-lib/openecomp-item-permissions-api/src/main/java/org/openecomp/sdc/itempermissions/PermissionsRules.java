package org.openecomp.sdc.itempermissions;

import java.util.Set;

/**
 * Created by ayalaben on 6/22/2017.
 */
public interface PermissionsRules {

  boolean isAllowed(String userId,String action);

  void executeAction(String itemId, String userId, String action);

  void updatePermission(String itemId,String currentUserId, String permission,Set<String>
      addedUsersIds, Set<String> removedUsersIds);

}
