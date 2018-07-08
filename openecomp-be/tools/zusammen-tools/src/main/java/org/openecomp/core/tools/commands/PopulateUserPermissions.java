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

package org.openecomp.core.tools.commands;

import static org.openecomp.core.tools.commands.CommandName.POPULATE_USER_PERMISSIONS;

import java.util.Collections;
import java.util.List;
import org.openecomp.core.tools.store.PermissionHandler;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;


public class PopulateUserPermissions extends Command {

    @Override
    public boolean execute(String[] args) {
        PermissionHandler permissionHandler = new PermissionHandler();
        List<ItemPermissionsEntity> permissions = permissionHandler.getAll();

        permissions.forEach(itemPermissionsEntity -> {
            if (!itemPermissionsEntity.getUserId().isEmpty() && !itemPermissionsEntity.getPermission().isEmpty()) {
                permissionHandler.addItem(Collections.singleton(itemPermissionsEntity.getItemId()),
                        itemPermissionsEntity.getUserId(), itemPermissionsEntity.getPermission());
            }
        });

        return true;
    }

    @Override
    public CommandName getCommandName() {
        return POPULATE_USER_PERMISSIONS;
    }
}
