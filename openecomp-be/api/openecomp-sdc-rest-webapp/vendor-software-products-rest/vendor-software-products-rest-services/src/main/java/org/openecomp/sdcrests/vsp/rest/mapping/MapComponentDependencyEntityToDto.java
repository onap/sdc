package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyResponseDto;

public class MapComponentDependencyEntityToDto extends
    MappingBase<ComponentDependencyModelEntity, ComponentDependencyResponseDto> {

  @Override
  public void doMapping(ComponentDependencyModelEntity source,
                        ComponentDependencyResponseDto target) {
    target.setSourceId(source.getSourceComponentId());
    target.setTargetId(source.getTargetComponentId());
    target.setRelationType(source.getRelation());
    target.setId(source.getId());
  }
}
