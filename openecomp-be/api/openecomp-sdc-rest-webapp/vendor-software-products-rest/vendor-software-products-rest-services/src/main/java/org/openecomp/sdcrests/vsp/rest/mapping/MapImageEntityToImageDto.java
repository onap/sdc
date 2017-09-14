package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ImageData;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageDto;

public class MapImageEntityToImageDto extends MappingBase<ImageEntity, ImageDto> {
  @Override
  public void doMapping(ImageEntity source, ImageDto target) {
    target.setId(source.getId());
    Image image = source.getImageCompositionData();

    if (image != null) {
      ImageData imageData = new ImageData(image.getFileName(), image.getDescription());
      new MapImageDataToImageDto().doMapping(imageData, target);
    }
  }
}
