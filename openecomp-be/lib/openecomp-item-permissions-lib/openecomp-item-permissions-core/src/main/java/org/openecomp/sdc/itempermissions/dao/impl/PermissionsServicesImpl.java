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

import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.itempermissions.PermissionsRules;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDao;
import org.openecomp.sdc.itempermissions.dao.UserPermissionsDao;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;

import java.util.Collection;
import java.util.Set;


/**
 * Created by ayalaben on 6/22/2017.
 */
public class PermissionsServicesImpl implements PermissionsServices {

  private ItemPermissionsDao itemPermissionsDao;

  private UserPermissionsDao userPermissionsDao;

  private PermissionsRules permissionsRules;

  private static final String CHANGE_PERMISSIONS = "Change_Item_Permissions";

  public PermissionsServicesImpl(PermissionsRules permissionsRules,
                                 ItemPermissionsDao itemPermissionsDao,UserPermissionsDao userPermissionsDao) {
    this.itemPermissionsDao = itemPermissionsDao;
    this.permissionsRules = permissionsRules;
    this.userPermissionsDao = userPermissionsDao;
  }


  @Override
  public Collection<ItemPermissionsEntity> listItemPermissions(String itemId) {
    return itemPermissionsDao.listItemPermissions(itemId);
  }

  @Override
  public Set<String> listUserPermittedItems(String userId, String permission) {
    return userPermissionsDao.listUserPermittedItems(userId,permission);
  }

  @Override
  public void updateItemPermissions(String itemId, String permission, Set<String> addedUsersIds,
                                    Set<String> removedUsersIds) {

    String currentUserId = SessionContextProviderFactory.getInstance()
          .createInterface().get().getUser().getUserId();

    permissionsRules.executeAction(itemId,currentUserId,CHANGE_PERMISSIONS);

    permissionsRules.updatePermission(itemId,currentUserId,permission,addedUsersIds,
          removedUsersIds);

    itemPermissionsDao.updateItemPermissions(itemId, permission,
          addedUsersIds, removedUsersIds);

    userPermissionsDao.updatePermissions(itemId, permission,
            addedUsersIds, removedUsersIds);

  }

  @Override
  public boolean isAllowed(String itemId,String userId,String action) {

    String userPermission = itemPermissionsDao.getUserItemPermission(itemId,userId);
    return permissionsRules.isAllowed(userPermission,action);
  }

  @Override
  public void execute(String itemId,String userId,String action) {
    permissionsRules.executeAction(itemId, userId, action);
  }

  @Override
  public String getUserItemPermiission(String itemId, String userId) {
    return itemPermissionsDao.getUserItemPermission(itemId,userId);
  }

  @Override
  public void deleteItemPermissions(String itemId) {
    itemPermissionsDao.deleteItemPermissions(itemId);
  }

}
