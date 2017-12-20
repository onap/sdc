package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;

public class ElementToImageQuestionnaireConvertor extends ElementConvertor <ImageEntity>{

  @Override
  public ImageEntity convert( Element element) {
    ImageEntity imageEntity = new ImageEntity();

    imageEntity.setId(element.getElementId().getValue());
    imageEntity.setQuestionnaireData(new String(FileUtils.toByteArray(element.getData())));
    return imageEntity;
  }

}
