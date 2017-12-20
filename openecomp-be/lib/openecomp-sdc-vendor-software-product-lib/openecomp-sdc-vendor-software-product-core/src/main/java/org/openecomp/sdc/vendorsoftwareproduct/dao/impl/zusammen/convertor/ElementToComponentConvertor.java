package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.types.ElementPropertyName;

public class ElementToComponentConvertor extends ElementConvertor<ComponentEntity> {

  @Override
  public ComponentEntity convert(Element element) {
    ComponentEntity componentEntity = new ComponentEntity();
    componentEntity.setId(element.getElementId().getValue());
    componentEntity.setCompositionData(new String(FileUtils.toByteArray(element.getData())));
    mapInfoToComponentEntity(componentEntity,element.getInfo());
    return componentEntity;
  }

  @Override
  public ComponentEntity convert( ElementInfo elementInfo) {
    ComponentEntity componentEntity = new ComponentEntity();
    componentEntity.setId(elementInfo.getId().getValue());
    mapInfoToComponentEntity(componentEntity,elementInfo.getInfo());
    return componentEntity;
  }


  public void mapInfoToComponentEntity(ComponentEntity componentEntity,Info info){


    componentEntity.setCompositionData(
        info.getProperty(ElementPropertyName.compositionData.name()));
  }

}
