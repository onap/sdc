package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageCreationDto;

public class MapImageEntityToImageCreationDto extends MappingBase<ImageEntity,
    ImageCreationDto> {

  @Override
  public void doMapping(ImageEntity source, ImageCreationDto target) {
    target.setId(source.getId());
  }
}
