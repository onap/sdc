package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModel;

public class MapComponentDependencyModelEntityToDto extends
    MappingBase<ComponentDependencyModelEntity, ComponentDependencyModel> {

  @Override
  public void doMapping(ComponentDependencyModelEntity source,
                        ComponentDependencyModel target) {
   target.setSourceId(source.getSourceComponentId());
    target.setTargetId(source.getTargetComponentId());
    target.setRelationType(source.getRelation());
  }
}
