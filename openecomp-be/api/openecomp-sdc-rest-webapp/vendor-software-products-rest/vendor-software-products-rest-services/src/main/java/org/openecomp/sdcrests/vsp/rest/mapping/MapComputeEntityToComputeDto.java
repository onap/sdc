package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.types.ListComputeResponse;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDescription;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDto;

public class MapComputeEntityToComputeDto extends MappingBase<ListComputeResponse, ComputeDto> {
  @Override
  public void doMapping(ListComputeResponse source, ComputeDto target) {
    target.setId(source.getComputeEntity().getId());
    if (source.getComputeEntity().getCompositionData() != null) {
      ComputeDescription desc = JsonUtil.json2Object(source.getComputeEntity().getCompositionData
          (), ComputeDescription.class);
      target.setName(desc.getName());
      target.setDescription(desc.getDescription());
      target.setAssociatedToDeploymentFlavor(source.isAssociatedWithDeploymentFlavor());
    }
  }
}
