package org.openecomp.sdcrests.itempermissions.rest.mapping;

import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;
import org.openecomp.sdcrests.itempermissions.types.ItemPermissionsDto;
import org.openecomp.sdcrests.mapping.MappingBase;

/**
 * Created by ayalaben on 6/18/2017.
 */

public class MapItemPermissionsToItemPermissionsDto  extends
    MappingBase<ItemPermissionsEntity, ItemPermissionsDto> {


  @Override
  public void doMapping(ItemPermissionsEntity source, ItemPermissionsDto target) {
    target.setUserId(source.getUserId());
    target.setPermission(source.getPermission());
  }
}
