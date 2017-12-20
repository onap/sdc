package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ImageData;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageDto;


public class MapImageDataToImageDto extends MappingBase<ImageData, ImageDto> {

  @Override
  public void doMapping(ImageData source, ImageDto target) {
    target.setFileName(source.getFileName());
    target.setDescription(source.getDescription());
  }
}






