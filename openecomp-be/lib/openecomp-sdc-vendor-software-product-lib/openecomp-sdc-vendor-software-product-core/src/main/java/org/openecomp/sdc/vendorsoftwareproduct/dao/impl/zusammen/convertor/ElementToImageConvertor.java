package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.types.ElementPropertyName;

public class ElementToImageConvertor extends ElementConvertor <ImageEntity>{

  @Override
  public ImageEntity convert( Element element) {
    ImageEntity imageEntity = new ImageEntity();

    imageEntity.setId(element.getElementId().getValue());
    imageEntity.setCompositionData(new String(FileUtils.toByteArray(element.getData())));
    mapInfoToImageEntity(imageEntity,element.getInfo());
    return imageEntity;
  }

  @Override
  public ImageEntity convert( ElementInfo elementInfo) {
    ImageEntity imageEntity = new ImageEntity();

    imageEntity.setId(elementInfo.getId().getValue());
    mapInfoToImageEntity(imageEntity,elementInfo.getInfo());
    return imageEntity;
  }


  public void mapInfoToImageEntity(ImageEntity imageEntity,Info info){


    imageEntity.setCompositionData(
        info.getProperty(ElementPropertyName.compositionData.name()));
  }

}
