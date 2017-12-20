package org.openecomp.sdcrests.conflict.rest.mapping;

import org.openecomp.conflicts.types.ConflictInfo;
import org.openecomp.sdcrests.conflict.types.ConflictInfoDto;
import org.openecomp.sdcrests.mapping.MappingBase;

public class MapConflictInfoToDto extends MappingBase<ConflictInfo, ConflictInfoDto> {
  @Override
  public void doMapping(ConflictInfo source, ConflictInfoDto target) {
    target.setId(source.getId());
    target.setType(source.getType());
    target.setName(source.getName());
  }
}
