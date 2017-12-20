package org.openecomp.sdcrests.item.rest.mapping;

import org.openecomp.sdc.versioning.dao.types.Revision;
import org.openecomp.sdcrests.item.types.RevisionDto;
import org.openecomp.sdcrests.mapping.MappingBase;

public class MapRevisionToDto extends MappingBase<Revision, RevisionDto> {
  @Override
  public void doMapping(Revision source, RevisionDto target) {
    target.setId(source.getId());
    target.setMessage(source.getMessage());
    target.setUser(source.getUser());
    target.setTime(source.getTime());
  }
}
