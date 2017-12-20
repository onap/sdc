package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.ComponentDependencyModelPropertyName;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;

public class ElementToComponentDependencyModelConvertor extends ElementConvertor <ComponentDependencyModelEntity>{

  @Override
  public ComponentDependencyModelEntity convert( Element element) {
    ComponentDependencyModelEntity componentDependencyModelEntity = new ComponentDependencyModelEntity();
    componentDependencyModelEntity.setId(element.getElementId().getValue());
    mapInfoToComponentDependencyModelEntity(componentDependencyModelEntity,element.getInfo());
    return componentDependencyModelEntity;
  }

  @Override
  public ComponentDependencyModelEntity convert(ElementInfo elementInfo) {
    ComponentDependencyModelEntity componentDependencyModelEntity = new ComponentDependencyModelEntity();

    componentDependencyModelEntity.setId(elementInfo.getId().getValue());
    mapInfoToComponentDependencyModelEntity(componentDependencyModelEntity,elementInfo.getInfo());
    return componentDependencyModelEntity;
  }


  public void mapInfoToComponentDependencyModelEntity(ComponentDependencyModelEntity componentDependencyModelEntity,Info info){

    componentDependencyModelEntity.setSourceComponentId(info
        .getProperty(ComponentDependencyModelPropertyName.sourcecomponent_id.name()));
    componentDependencyModelEntity.setTargetComponentId(info
        .getProperty(ComponentDependencyModelPropertyName.targetcomponent_id.name()));
    componentDependencyModelEntity.setRelation(info
        .getProperty(ComponentDependencyModelPropertyName.relation.name()));

  }

}
