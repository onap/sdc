package org.openecomp.activityspec.api.rest.mapping;

import java.util.ArrayList;
import org.openecomp.activityspec.be.dao.types.ActivitySpecEntity;
import org.openecomp.activityspec.be.datatypes.ActivitySpecParameter;
import org.openecomp.activityspec.api.rest.types.ActivitySpecRequestDto;
import org.openecomp.sdcrests.mapping.MappingBase;

import java.util.Objects;
import java.util.stream.Collectors;

public class MapActivitySpecRequestDtoToActivitySpecEntity
    extends MappingBase<ActivitySpecRequestDto,
    ActivitySpecEntity> {

  @Override
  public void doMapping(ActivitySpecRequestDto source, ActivitySpecEntity target) {
    target.setName(source.getName());
    target.setDescription(source.getDescription());
    target.setCategoryList(source.getCategoryList() == null ? new ArrayList<String>()
        : source.getCategoryList());
    if (Objects.nonNull(source.getInputParameters())) {
      target.setInputParameters(source.getInputParameters().stream()
          .map(activitySpecParameterDto -> new MapDtoToActivityParameter()
              .applyMapping(activitySpecParameterDto, ActivitySpecParameter.class))
                .collect(Collectors.toList()));
    }
    if (Objects.nonNull(source.getOutputParameters())) {
      target.setOutputParameters(source.getOutputParameters().stream()
          .map(activitySpecParameterDto -> new MapDtoToActivityParameter()
              .applyMapping(activitySpecParameterDto, ActivitySpecParameter.class))
          .collect(Collectors.toList()));
    }
  }
}
