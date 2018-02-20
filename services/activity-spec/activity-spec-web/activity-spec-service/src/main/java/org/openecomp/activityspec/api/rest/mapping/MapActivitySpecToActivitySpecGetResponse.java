package org.openecomp.activityspec.api.rest.mapping;

import org.openecomp.activityspec.be.dao.types.ActivitySpecEntity;
import org.openecomp.activityspec.api.rest.types.ActivitySpecGetResponse;
import org.openecomp.activityspec.api.rest.types.ActivitySpecParameterDto;
import org.openecomp.sdcrests.mapping.MappingBase;

import java.util.Objects;
import java.util.stream.Collectors;

public class MapActivitySpecToActivitySpecGetResponse extends MappingBase<ActivitySpecEntity,
    ActivitySpecGetResponse> {

  @Override
  public void doMapping(ActivitySpecEntity source, ActivitySpecGetResponse target) {
    target.setName(source.getName());
    target.setDescription(source.getDescription());
    target.setCategoryList(source.getCategoryList());
    if (Objects.nonNull(source.getInputParameters())) {
      target.setInputParameters(source.getInputParameters().stream().map(
          activitySpecParameter -> new MapActivityParameterToDto()
              .applyMapping(activitySpecParameter, ActivitySpecParameterDto
                  .class)).collect(Collectors.toList()));
    }
    if (Objects.nonNull(source.getOutputParameters())) {
      target.setOutputParameters(source.getOutputParameters().stream().map(
          activitySpecParameter -> new MapActivityParameterToDto()
              .applyMapping(activitySpecParameter, ActivitySpecParameterDto
                  .class)).collect(Collectors.toList()));
    }
    target.setStatus(source.getStatus());
  }
}
