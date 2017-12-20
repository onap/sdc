package org.openecomp.sdcrests.item.rest.mapping;

import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdcrests.item.types.ItemDto;
import org.openecomp.sdcrests.mapping.MappingBase;

public class MapItemToDto extends MappingBase<Item, ItemDto> {
  @Override
  public void doMapping(Item source, ItemDto target) {
    target.setId(source.getId());
    target.setType(source.getType());
    target.setName(source.getName());
    target.setDescription(source.getDescription());
  }
}
