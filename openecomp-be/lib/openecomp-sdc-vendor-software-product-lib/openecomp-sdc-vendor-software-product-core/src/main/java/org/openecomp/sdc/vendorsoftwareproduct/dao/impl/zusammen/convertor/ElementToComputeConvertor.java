package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.types.ElementPropertyName;

public class ElementToComputeConvertor extends ElementConvertor <ComputeEntity>{

  @Override
  public ComputeEntity convert( Element element) {
    ComputeEntity computeEntity = new ComputeEntity();

    computeEntity.setId(element.getElementId().getValue());
    computeEntity.setCompositionData(new String(FileUtils.toByteArray(element.getData())));
    mapInfoToComputeEntity(computeEntity,element.getInfo());
    return computeEntity;
  }

  @Override
  public ComputeEntity convert( ElementInfo elementInfo) {
    ComputeEntity computeEntity = new ComputeEntity();

    computeEntity.setId(elementInfo.getId().getValue());
    mapInfoToComputeEntity(computeEntity,elementInfo.getInfo());
    return computeEntity;
  }


  public void mapInfoToComputeEntity(ComputeEntity computeEntity,Info info){


    computeEntity.setCompositionData(
        info.getProperty(ElementPropertyName.compositionData.name()));
  }

}
