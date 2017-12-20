package org.openecomp.sdcrests.vsp.rest.mapping;


import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyCreationDto;

public class MapComponentDependencyEntityToCreationDto extends MappingBase
    <ComponentDependencyModelEntity, ComponentDependencyCreationDto> {

  @Override
  public void doMapping(ComponentDependencyModelEntity source,
                        ComponentDependencyCreationDto target) {
    target.setId(source.getId());
  }
}
