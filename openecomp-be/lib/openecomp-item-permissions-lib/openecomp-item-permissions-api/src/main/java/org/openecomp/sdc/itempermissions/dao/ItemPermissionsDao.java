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
package org.openecomp.sdc.itempermissions.dao;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;

/**
 * Created by ayalaben on 6/18/2017.
 */
public interface ItemPermissionsDao {

    Collection<ItemPermissionsEntity> listItemPermissions(String itemId);

    void updateItemPermissions(String itemId, String permission, Set<String> addedUsersIds, Set<String> removedUsersIds);

    Optional<String> getUserItemPermission(String itemId, String userId);

    void deleteItemPermissions(String itemId);
}
