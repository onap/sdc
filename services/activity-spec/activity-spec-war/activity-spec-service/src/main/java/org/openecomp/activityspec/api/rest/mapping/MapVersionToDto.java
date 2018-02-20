package org.openecomp.activityspec.api.rest.mapping;

import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.item.types.VersionDto;
import org.openecomp.sdcrests.mapping.MappingBase;

public class MapVersionToDto extends MappingBase<Version, VersionDto> {
  @Override
  public void doMapping(Version source, VersionDto target) {
    target.setId(source.getId());
  }
}
