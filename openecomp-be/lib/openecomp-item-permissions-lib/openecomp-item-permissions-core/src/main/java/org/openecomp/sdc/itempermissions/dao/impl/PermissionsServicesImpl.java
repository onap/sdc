package org.openecomp.sdc.itempermissions.dao.impl;

import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.itempermissions.PermissionsRules;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDao;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;

import java.util.Collection;
import java.util.Set;


/**
 * Created by ayalaben on 6/22/2017.
 */
public class PermissionsServicesImpl implements PermissionsServices {

  private ItemPermissionsDao permissionsDao;

  private PermissionsRules permissionsRules;

  private static final String CHANGE_PERMISSIONS = "Change_Item_Permissions";

  public PermissionsServicesImpl(PermissionsRules permissionsRules,
                                 ItemPermissionsDao permissionsDao) {
    this.permissionsDao = permissionsDao;
    this.permissionsRules = permissionsRules;
  }


  @Override
  public Collection<ItemPermissionsEntity> listItemPermissions(String itemId) {
    return permissionsDao.listItemPermissions(itemId);
  }

  @Override
  public void updateItemPermissions(String itemId, String permission, Set<String> addedUsersIds,
                                    Set<String> removedUsersIds) {

    String currentUserId = SessionContextProviderFactory.getInstance()
          .createInterface().get().getUser().getUserId();

    permissionsRules.executeAction(itemId,currentUserId,CHANGE_PERMISSIONS);

    permissionsRules.updatePermission(itemId,currentUserId,permission,addedUsersIds,
          removedUsersIds);

    permissionsDao.updateItemPermissions(itemId, permission,
          addedUsersIds, removedUsersIds);

  }

  @Override
  public boolean isAllowed(String itemId,String userId,String action) {

    String userPermission = permissionsDao.getUserItemPermiission(itemId,userId);
    return permissionsRules.isAllowed(userPermission,action);
  }

  @Override
  public void execute(String itemId,String userId,String action) {
    permissionsRules.executeAction(itemId, userId, action);
  }

  @Override
  public String getUserItemPermiission(String itemId, String userId) {
    return permissionsDao.getUserItemPermiission(itemId,userId);
  }

}
