package org.openecomp.activityspec.api.rest.mapping;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.openecomp.activityspec.api.rest.types.ActivitySpecListResponseDto;
import org.openecomp.activityspec.be.datatypes.ActivitySpecConstant;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdcrests.mapping.MappingBase;

import java.util.List;

public class MapItemToListResponseDto extends MappingBase<Item, ActivitySpecListResponseDto> {
  @Override
  public void doMapping(Item source, ActivitySpecListResponseDto target) {
    target.setId(source.getId());
    target.setName(source.getName());
    target.setCategoryList((List<String>) source.getProperties().get(ActivitySpecConstant
        .CATEGORY_ATTRIBUTE_NAME));
    final Map<VersionStatus, Integer> versionStatusCounters = source.getVersionStatusCounters();
    if (Objects.nonNull(versionStatusCounters) && !versionStatusCounters.isEmpty()) {
      final Set<VersionStatus> versionStatuses = versionStatusCounters.keySet();
      target.setStatus(versionStatuses.stream().findFirst().isPresent()
          ? versionStatuses.stream().findFirst().get().name() : null);
    }
  }
}
