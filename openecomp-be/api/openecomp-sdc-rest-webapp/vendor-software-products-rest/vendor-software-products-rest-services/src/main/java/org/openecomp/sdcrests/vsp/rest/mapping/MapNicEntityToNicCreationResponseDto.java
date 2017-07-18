package org.openecomp.sdcrests.vsp.rest.mapping;


import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicCreationResponseDto;

public class MapNicEntityToNicCreationResponseDto extends MappingBase<NicEntity,
    NicCreationResponseDto> {


  @Override
  public void doMapping(NicEntity source, NicCreationResponseDto target) {
    target.setNicId(source.getId());
  }
}
