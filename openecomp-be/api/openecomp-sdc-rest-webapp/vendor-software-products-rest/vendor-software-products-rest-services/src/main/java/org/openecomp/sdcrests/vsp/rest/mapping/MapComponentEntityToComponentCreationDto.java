package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentCreationDto;

public class MapComponentEntityToComponentCreationDto extends MappingBase<ComponentEntity,
    ComponentCreationDto> {
  @Override
  public void doMapping(ComponentEntity source, ComponentCreationDto target) {
    target.setVfcId(source.getId());
  }
}
