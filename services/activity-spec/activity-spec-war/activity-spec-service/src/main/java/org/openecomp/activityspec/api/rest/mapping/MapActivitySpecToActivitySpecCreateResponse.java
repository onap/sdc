package org.openecomp.activityspec.api.rest.mapping;

import java.util.Objects;
import org.openecomp.activityspec.be.dao.types.ActivitySpecEntity;
import org.openecomp.activityspec.api.rest.types.ActivitySpecCreateResponse;
import org.openecomp.sdcrests.mapping.MappingBase;

public class MapActivitySpecToActivitySpecCreateResponse extends MappingBase<ActivitySpecEntity,
    ActivitySpecCreateResponse > {

  @Override
  public void doMapping(ActivitySpecEntity source, ActivitySpecCreateResponse target) {
    target.setId(source.getId());
    target.setVersionId(Objects.nonNull(source.getVersion()) ? source.getVersion().getId() : null);
  }
}
