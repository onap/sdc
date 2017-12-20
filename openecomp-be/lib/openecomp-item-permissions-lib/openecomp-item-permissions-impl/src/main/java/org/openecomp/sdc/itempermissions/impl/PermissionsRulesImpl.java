package org.openecomp.sdc.itempermissions.impl;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.itempermissions.PermissionsRules;
import org.openecomp.sdc.itempermissions.PermissionsServicesFactory;
import org.openecomp.sdc.itempermissions.errors.PermissionsErrorMessagesBuilder;
import org.openecomp.sdc.itempermissions.impl.types.PermissionActionTypes;
import org.openecomp.sdc.itempermissions.impl.types.PermissionTypes;

import java.util.HashSet;
import java.util.Set;

import static org.openecomp.sdc.itempermissions.errors.PermissionsErrorMessages.INVALID_ACTION_TYPE;
import static org.openecomp.sdc.itempermissions.errors.PermissionsErrorMessages.INVALID_PERMISSION_TYPE;

/**
 * Created by ayalaben on 6/26/2017.
 */
public class PermissionsRulesImpl implements PermissionsRules {


  @Override
  public boolean isAllowed(String permission, String action) {

    if (permission == null) {
      return false;
    }
    try {
      PermissionTypes.valueOf(permission);
    } catch (IllegalArgumentException ex) {
      throw new CoreException(new PermissionsErrorMessagesBuilder(INVALID_PERMISSION_TYPE).build());
    }

    try {
      switch (PermissionActionTypes.valueOf(action)) {
        case Create_Item:
          return true;

        case Edit_Item:
          if (permission.equals(PermissionTypes.Contributor.name()) || permission.equals
              (PermissionTypes.Owner.name())) {
            return true;
          }
          break;
        case Commit_Item:
          if (permission.equals(PermissionTypes.Contributor.name()) ||  permission.equals
              (PermissionTypes.Owner.name())) {
          return true;
        }
          break;

        case Change_Item_Permissions:
          if (permission.equals(PermissionTypes.Owner.name())) {
            return true;
          }
          break;

        case Submit_Item:
          if (permission.equals(PermissionTypes.Owner.name())) {
            return true;
          }
          break;

        default:
          return false;
      }
    } catch (IllegalArgumentException ex) {
      throw new CoreException(new PermissionsErrorMessagesBuilder(INVALID_ACTION_TYPE).build());
    }

    return false;
  }

  @Override
  public void executeAction(String itemId, String userId, String action) {
    try {
      switch (PermissionActionTypes.valueOf(action)) {
        case Create_Item:
          caseCreateItem(userId,itemId);
          break;

        case Change_Item_Permissions:
          break;

        case Edit_Item:
          break;

        case Submit_Item:
          break;

        default:
      }
    } catch (IllegalArgumentException ex) {
      throw new CoreException(new PermissionsErrorMessagesBuilder(INVALID_ACTION_TYPE).build());
    }
  }

  @Override
  public void updatePermission(String itemId,String currentUserId, String permission, Set<String>
      addedUsersIds,Set<String> removedUsersIds) {
    try {
      PermissionTypes.valueOf(permission);
    } catch (IllegalArgumentException ex) {
      throw new CoreException(new PermissionsErrorMessagesBuilder(INVALID_PERMISSION_TYPE).build());
    }

    if (permission.equals(PermissionTypes.Owner.name())) {

      HashSet<String> currentOwner = new HashSet<String>();
      currentOwner.add(currentUserId);

      PermissionsServicesFactory.getInstance().createInterface()
          .updateItemPermissions(itemId,PermissionTypes.Contributor.name(),
          currentOwner,new HashSet<String>());
    }
  }

  protected void caseCreateItem(String userId,String itemId) {
    HashSet<String> ownerId = new HashSet<String>();
    ownerId.add(userId);
    PermissionsServicesFactory.getInstance().createInterface()
        .updateItemPermissions(itemId, PermissionTypes.Owner.name(), ownerId,
            new HashSet<String>());
  }

}
