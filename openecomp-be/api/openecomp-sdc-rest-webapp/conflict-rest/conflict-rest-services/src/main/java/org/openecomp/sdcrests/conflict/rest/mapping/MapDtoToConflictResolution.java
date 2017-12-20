package org.openecomp.sdcrests.conflict.rest.mapping;

import org.openecomp.conflicts.types.ConflictResolution;
import org.openecomp.sdcrests.conflict.types.ConflictResolutionDto;
import org.openecomp.sdcrests.mapping.MappingBase;

public class MapDtoToConflictResolution
    extends MappingBase<ConflictResolutionDto, ConflictResolution> {

  @Override
  public void doMapping(ConflictResolutionDto source, ConflictResolution target) {
    target.setResolution(source.getResolution());
    target.setOtherResolution(source.getOtherResolution());
  }
}
