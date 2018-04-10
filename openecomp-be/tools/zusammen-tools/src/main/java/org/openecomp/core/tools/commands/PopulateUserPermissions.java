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

import org.openecomp.core.tools.store.PermissionHandler;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;

import java.util.*;


public class PopulateUserPermissions {

    private static PermissionHandler permissionHandler = new PermissionHandler();

    public static void execute(){

        List<ItemPermissionsEntity> permissions = permissionHandler.getAll();

        permissions.forEach(itemPermissionsEntity ->
            permissionHandler.addItem
                    (Collections.singleton(itemPermissionsEntity.getItemId()),
                            itemPermissionsEntity.getUserId(),itemPermissionsEntity.getPermission()));

        System.exit(0);

    }
}
