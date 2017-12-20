package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.types.ElementPropertyName;

public class ElementToNetworkConvertor extends ElementConvertor <NetworkEntity>{

  @Override
  public NetworkEntity convert( Element element) {
    NetworkEntity networkEntity = new NetworkEntity();

    networkEntity.setId(element.getElementId().getValue());
    networkEntity.setCompositionData(new String(FileUtils.toByteArray(element.getData())));
    mapInfoToNetworkEntity(networkEntity,element.getInfo());
    return networkEntity;
  }

  @Override
  public NetworkEntity convert( ElementInfo elementInfo) {
    NetworkEntity networkEntity = new NetworkEntity();

    networkEntity.setId(elementInfo.getId().getValue());
    mapInfoToNetworkEntity(networkEntity,elementInfo.getInfo());
    return networkEntity;
  }


  public void mapInfoToNetworkEntity(NetworkEntity networkEntity,Info info){
    networkEntity.setCompositionData(
        info.getProperty(ElementPropertyName.compositionData.name()));
  }

}
