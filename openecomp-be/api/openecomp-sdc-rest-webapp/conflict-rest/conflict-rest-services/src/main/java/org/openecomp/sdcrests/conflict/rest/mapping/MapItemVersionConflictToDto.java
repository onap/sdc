package org.openecomp.sdcrests.conflict.rest.mapping;


import org.openecomp.conflicts.types.ItemVersionConflict;
import org.openecomp.sdcrests.conflict.types.ConflictDto;
import org.openecomp.sdcrests.conflict.types.ConflictInfoDto;
import org.openecomp.sdcrests.conflict.types.ItemVersionConflictDto;
import org.openecomp.sdcrests.mapping.MappingBase;

public class MapItemVersionConflictToDto
    extends MappingBase<ItemVersionConflict, ItemVersionConflictDto> {

  @Override
  public void doMapping(ItemVersionConflict source, ItemVersionConflictDto target) {
    target
        .setConflict(new MapConflictToDto().applyMapping(source.getVersionConflict(), ConflictDto.class));

    MapConflictInfoToDto conflictInfoMapper = new MapConflictInfoToDto();
    source.getElementConflicts().forEach(conflictInfo -> target
        .addConflictInfo(conflictInfoMapper.applyMapping(conflictInfo, ConflictInfoDto.class)));
  }
}
