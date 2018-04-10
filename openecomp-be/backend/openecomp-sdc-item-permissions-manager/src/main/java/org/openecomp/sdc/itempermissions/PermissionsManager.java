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
package org.openecomp.sdc.itempermissions;

import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;

import java.util.Collection;
import java.util.Set;

/**
 * Created by ayalaben on 6/18/2017.
 */
public interface PermissionsManager {

  Collection<ItemPermissionsEntity> listItemPermissions(String itemId);

  Set<String> listUserPermittedItems(String userId, String permission);

  void updateItemPermissions(String itemId, String permission, Set<String> addedUsersIds,
                             Set<String> removedUsersIds);

  boolean isAllowed(String itemId,String userId,String action);

  String getUserItemPermission(String itemId, String userId);

  void deleteItemPermissions(String itemId);


}
