package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeCreationDto;

public class MapComputeEntityToComputeCreationDto extends
    MappingBase<ComputeEntity, ComputeCreationDto> {
  @Override
  public void doMapping(ComputeEntity source, ComputeCreationDto target) {
    target.setId(source.getId());
  }
}
