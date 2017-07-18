package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComputeData;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDetailsDto;

public class MapComputeDataToComputeDetailsDto extends MappingBase<ComputeData, ComputeDetailsDto> {

  @Override
  public void doMapping(ComputeData source, ComputeDetailsDto target) {

    target.setName(source.getName());
    target.setDescription(source.getDescription());
  }
}
