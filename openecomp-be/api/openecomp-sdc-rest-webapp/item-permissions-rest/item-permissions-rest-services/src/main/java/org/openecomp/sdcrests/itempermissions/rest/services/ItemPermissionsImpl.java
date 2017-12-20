//package org.openecomp.sdcrests.itempermissions.rest.mapping.mapping.services;
package org.openecomp.sdcrests.itempermissions.rest.services;


import org.openecomp.sdc.itempermissions.ItemPermissionsManager;
import org.openecomp.sdc.itempermissions.ItemPermissionsManagerFactory;
import org.openecomp.sdcrests.itempermissions.rest.ItemPermissions;
import org.openecomp.sdcrests.itempermissions.rest.mapping.MapItemPermissionsToItemPermissionsDto;
import org.openecomp.sdcrests.itempermissions.types.ItemPermissionsDto;
import org.openecomp.sdcrests.itempermissions.types.ItemPermissionsRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;

/**
 * Created by ayalaben on 6/18/2017.
 */

@Named
@Service("itemPermissions")
@Scope(value = "prototype")
public class ItemPermissionsImpl implements ItemPermissions {

  private ItemPermissionsManager itemPermissionsManager =
      ItemPermissionsManagerFactory.getInstance().createInterface();

  @Override
  public Response list(String itemId, String user) {

    GenericCollectionWrapper<ItemPermissionsDto> results = new GenericCollectionWrapper<>();
    MapItemPermissionsToItemPermissionsDto mapper = new MapItemPermissionsToItemPermissionsDto();

    itemPermissionsManager.listItemPermissions(itemId)
        .forEach(itemPermission -> results.add(mapper.applyMapping
            (itemPermission, ItemPermissionsDto.class)));

    return Response.ok(results).build();
  }

  @Override
  public Response updatePermissions(ItemPermissionsRequestDto request, String itemId,
                                    String permission, String user) {

    itemPermissionsManager.updateItemPermissions(itemId,permission,request.getAddedUsersIds(),
        request.getRemovedUsersIds());

    return Response.ok().build();
  }
}
