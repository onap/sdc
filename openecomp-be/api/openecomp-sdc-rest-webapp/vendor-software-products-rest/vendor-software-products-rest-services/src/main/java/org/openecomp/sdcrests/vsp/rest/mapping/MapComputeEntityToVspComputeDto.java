package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDescription;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspComputeDto;

public class MapComputeEntityToVspComputeDto extends MappingBase<ComputeEntity, VspComputeDto> {
  @Override
  public void doMapping(ComputeEntity source, VspComputeDto target) {
    target.setComputeFlavorId(source.getId());
    if (source.getCompositionData() != null) {
      ComputeDescription desc = JsonUtil.json2Object(source.getCompositionData(), ComputeDescription
          .class);
      target.setName(desc.getName());
      target.setComponentId(source.getComponentId());
    }
  }
}