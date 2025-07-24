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
package org.openecomp.sdcrests.itempermissions.rest.services;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.itempermissions.PermissionsManager;
import org.openecomp.sdc.itempermissions.PermissionsManagerFactory;
import org.openecomp.sdcrests.itempermissions.rest.ItemPermissions;
import org.openecomp.sdcrests.itempermissions.rest.mapping.MapItemPermissionsToItemPermissionsDto;
import org.openecomp.sdcrests.itempermissions.types.ItemPermissionsDto;
import org.openecomp.sdcrests.itempermissions.types.ItemPermissionsRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Created by ayalaben on 6/18/2017.
 */
@Named
@Service("itemPermissions")
@Scope(value = "prototype")
public class ItemPermissionsImpl implements ItemPermissions {

    private PermissionsManager permissionsManager = PermissionsManagerFactory.getInstance().createInterface();

    @Override
    public ResponseEntity list(String itemId, String user) {
        GenericCollectionWrapper<ItemPermissionsDto> results = new GenericCollectionWrapper<>();
        MapItemPermissionsToItemPermissionsDto mapper = new MapItemPermissionsToItemPermissionsDto();
        permissionsManager.listItemPermissions(itemId)
            .forEach(itemPermission -> results.add(mapper.applyMapping(itemPermission, ItemPermissionsDto.class)));
        return ResponseEntity.ok(results);
    }

    @Override
    public ResponseEntity updatePermissions(ItemPermissionsRequestDto request, String itemId, String permission, String user) {
        permissionsManager.updateItemPermissions(itemId, permission, request.getAddedUsersIds(), request.getRemovedUsersIds());
        return ResponseEntity.ok().build();
    }
}
