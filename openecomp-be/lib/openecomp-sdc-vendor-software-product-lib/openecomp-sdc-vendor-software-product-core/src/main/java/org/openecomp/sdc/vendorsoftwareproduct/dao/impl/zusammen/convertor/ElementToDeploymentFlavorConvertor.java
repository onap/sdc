package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.types.ElementPropertyName;

public class ElementToDeploymentFlavorConvertor extends ElementConvertor <DeploymentFlavorEntity>{

  @Override
  public DeploymentFlavorEntity convert( Element element) {
    DeploymentFlavorEntity deploymentFlavorEntity = new DeploymentFlavorEntity();

    deploymentFlavorEntity.setId(element.getElementId().getValue());
    deploymentFlavorEntity.setCompositionData(new String(FileUtils.toByteArray(element.getData())));
    mapInfoToDeploymentFlavorEntity(deploymentFlavorEntity,element.getInfo());
    return deploymentFlavorEntity;
  }

  @Override
  public DeploymentFlavorEntity convert( ElementInfo elementInfo) {
    DeploymentFlavorEntity deploymentFlavorEntity = new DeploymentFlavorEntity();

    deploymentFlavorEntity.setId(elementInfo.getId().getValue());
    mapInfoToDeploymentFlavorEntity(deploymentFlavorEntity,elementInfo.getInfo());
    return deploymentFlavorEntity;
  }


  public void mapInfoToDeploymentFlavorEntity(DeploymentFlavorEntity deploymentFlavorEntity,Info info){


    deploymentFlavorEntity.setCompositionData(
        info.getProperty(ElementPropertyName.compositionData.name()));
  }

}
