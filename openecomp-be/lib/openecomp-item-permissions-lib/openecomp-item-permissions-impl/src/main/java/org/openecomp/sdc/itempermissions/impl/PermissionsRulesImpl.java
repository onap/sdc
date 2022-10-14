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
package org.openecomp.sdc.itempermissions.impl;

import static org.openecomp.sdc.itempermissions.errors.PermissionsErrorMessages.INVALID_ACTION_TYPE;
import static org.openecomp.sdc.itempermissions.errors.PermissionsErrorMessages.INVALID_PERMISSION_TYPE;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.itempermissions.PermissionsRules;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.itempermissions.PermissionsServicesFactory;
import org.openecomp.sdc.itempermissions.errors.PermissionsErrorMessagesBuilder;
import org.openecomp.sdc.itempermissions.impl.types.PermissionActionTypes;
import org.openecomp.sdc.itempermissions.impl.types.PermissionTypes;

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
                case Commit_Item:
                case Delete_Item:
                case Submit_Item:
                    if (permission.equals(PermissionTypes.Contributor.name()) || permission.equals(PermissionTypes.Owner.name())) {
                        return true;
                    }
                    break;
                case Change_Item_Permissions:
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
                    caseCreateItem(userId, itemId);
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
    public void updatePermission(String itemId, String currentUserId, String permission, Set<String> addedUsersIds, Set<String> removedUsersIds) {
        try {
            PermissionTypes.valueOf(permission);
        } catch (IllegalArgumentException ex) {
            throw new CoreException(new PermissionsErrorMessagesBuilder(INVALID_PERMISSION_TYPE).build());
        }
        if (isOwnerAdded(permission, addedUsersIds)) {
            handleCurrentOwner(itemId, currentUserId);
        }
    }

    private boolean isOwnerAdded(String permission, Set<String> addedUsersIds) {
        return permission.equals(PermissionTypes.Owner.name()) && CollectionUtils.isNotEmpty(addedUsersIds);
    }

    private void handleCurrentOwner(String itemId, String currentUserId) {
        PermissionsServices permissionsServices = PermissionsServicesFactory.getInstance().createInterface();
        if (!permissionsServices.getUserItemPermission(itemId, currentUserId).isPresent()) {
            return; // no current owner - first owner addition
        }
        Set<String> currentUserSet = Collections.singleton(currentUserId);
        permissionsServices.updateItemPermissions(itemId, PermissionTypes.Contributor.name(), currentUserSet, new HashSet<>());
        permissionsServices.updateItemPermissions(itemId, PermissionTypes.Owner.name(), new HashSet<>(), currentUserSet);
    }

    private void caseCreateItem(String userId, String itemId) {
        HashSet<String> ownerId = new HashSet<>();
        ownerId.add(userId);
        PermissionsServicesFactory.getInstance().createInterface()
            .updateItemPermissions(itemId, PermissionTypes.Owner.name(), ownerId, new HashSet<>());
    }
}
