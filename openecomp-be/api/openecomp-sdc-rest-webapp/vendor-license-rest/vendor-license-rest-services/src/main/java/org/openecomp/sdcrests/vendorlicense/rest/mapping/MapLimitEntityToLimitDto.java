package org.openecomp.sdcrests.vendorlicense.rest.mapping;

import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorlicense.types.LimitEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.LimitRequestDto;

public class MapLimitEntityToLimitDto extends MappingBase<LimitEntity, LimitEntityDto> {

  @Override
  public void doMapping(LimitEntity source, LimitEntityDto target) {
    target.setId(source.getId());
    target.setName(source.getName());
    target.setDescription(source.getDescription());
    target.setMetric(source.getMetric());
    target.setAggregationFunction(source.getAggregationFunction() != null ? source
        .getAggregationFunction().name() : null);
    target.setTime(source.getTime());
    target.setType(source.getType() != null ? source.getType().name() : null);
    target.setUnit(source.getUnit());
    target.setValue(source.getValue());
  }
}
