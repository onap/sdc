package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.types.ElementPropertyName;

public class ElementToNicConvertor extends ElementConvertor <NicEntity>{

  @Override
  public NicEntity convert( Element element) {
    NicEntity nicEntity = new NicEntity();

    nicEntity.setId(element.getElementId().getValue());
    nicEntity.setCompositionData(new String(FileUtils.toByteArray(element.getData())));
    mapInfoToNicEntity(nicEntity,element.getInfo());
    return nicEntity;
  }

  @Override
  public NicEntity convert( ElementInfo elementInfo) {
    NicEntity nicEntity = new NicEntity();

    nicEntity.setId(elementInfo.getId().getValue());
    mapInfoToNicEntity(nicEntity,elementInfo.getInfo());
    return nicEntity;
  }


  public void mapInfoToNicEntity(NicEntity nicEntity,Info info){

      nicEntity.setCompositionData(
          info.getProperty(ElementPropertyName.compositionData.name()));
  }

}
