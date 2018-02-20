package org.openecomp.activityspec.api.rest.mapping;

import org.openecomp.activityspec.be.datatypes.ActivitySpecParameter;
import org.openecomp.activityspec.api.rest.types.ActivitySpecParameterDto;
import org.openecomp.sdcrests.mapping.MappingBase;

public class MapActivityParameterToDto extends MappingBase<ActivitySpecParameter,
    ActivitySpecParameterDto> {
  @Override
  public void doMapping(ActivitySpecParameter source, ActivitySpecParameterDto target) {
    target.setName(source.getName());
    target.setType(source.getType());
    target.setValue(source.getValue());
  }
}
