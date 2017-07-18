package org.openecomp.sdcrests.vendorlicense.rest.mapping;

import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdcrests.mapping.MappingBase;

public class MapLimitEntityToLimitCreationDto extends
    MappingBase<LimitEntity, LimitCreationDto> {
  @Override
  public void doMapping(LimitEntity source, LimitCreationDto target) {
    target.setLimitId(source.getId());
  }
}
