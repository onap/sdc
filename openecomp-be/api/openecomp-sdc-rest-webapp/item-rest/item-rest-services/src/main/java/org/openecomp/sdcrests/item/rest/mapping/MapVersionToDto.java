package org.openecomp.sdcrests.item.rest.mapping;

import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.item.types.VersionDto;
import org.openecomp.sdcrests.mapping.MappingBase;

public class MapVersionToDto extends MappingBase<Version, VersionDto> {
  @Override
  public void doMapping(Version source, VersionDto target) {
    target.setId(source.getId());
    target.setName(source.getName());
    target.setDescription(source.getDescription());
    target.setBaseId(source.getBaseId());
    target.setStatus(source.getStatus());
    target.setState(source.getState());
    target.setCreationTime(source.getCreationTime());
    target.setModificationTime(source.getModificationTime());
    target.setAdditionalInfo(source.getAdditionalInfo());
  }
}
