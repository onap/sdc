package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDescription;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDetailsDto;

public class MapComputeDetailsDtoToComputeEntity extends MappingBase<ComputeDetailsDto,
    ComputeEntity> {
  @Override
  public void doMapping(ComputeDetailsDto source, ComputeEntity target) {
    ComputeDescription computeDesc = new ComputeDescription(source.getName(), source
        .getDescription());
    target.setCompositionData(computeDesc == null ? null : JsonUtil.object2Json(computeDesc));
  }
}
