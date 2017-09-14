package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageRequestDto;

public class MapImageRequestDtoToImageEntity extends MappingBase<ImageRequestDto, ImageEntity> {

  @Override
  public void doMapping(ImageRequestDto source, ImageEntity target) {
    Image image = new Image();
    image.setFileName(source.getFileName());
    image.setDescription(source.getDescription());
    /*try {
      if (source.getFormat() != null) {
        final ImageFormat imageFormat = ImageFormat.valueOf(source.getFormat());
        image.setFormat(source.getFormat());
      }
    } catch (IllegalArgumentException exception) {
      ErrorCode errorCode = ImageErrorBuilder.getInvalidImageFormatErrorBuilder();
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_IMAGE, ErrorLevel.ERROR.name(),
          errorCode.id(), errorCode.message() );
      throw new CoreException(errorCode);
    }
    image.setMd5(source.getMd5());
    image.setVersion(source.getVersion());
    //image.setProvidedBy(source.getProvidedBy());*/
    target.setImageCompositionData(image);
  }

}
