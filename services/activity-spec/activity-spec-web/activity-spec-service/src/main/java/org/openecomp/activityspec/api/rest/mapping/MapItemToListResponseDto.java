/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.activityspec.api.rest.mapping;

import org.openecomp.activityspec.api.rest.types.ActivitySpecListResponseDto;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdcrests.mapping.MappingBase;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.openecomp.activityspec.utils.ActivitySpecConstant.CATEGORY_ATTRIBUTE_NAME;

public class MapItemToListResponseDto extends MappingBase<Item, ActivitySpecListResponseDto> {
  @Override
  public void doMapping(Item source, ActivitySpecListResponseDto target) {
    target.setId(source.getId());
    target.setName(source.getName());
    target.setCategoryList((List<String>) source.getProperties().get(
        CATEGORY_ATTRIBUTE_NAME));
    final Map<VersionStatus, Integer> versionStatusCounters = source.getVersionStatusCounters();
    if (Objects.nonNull(versionStatusCounters) && !versionStatusCounters.isEmpty()) {
      final Set<VersionStatus> versionStatuses = versionStatusCounters.keySet();
      target.setStatus(versionStatuses.stream().findFirst().isPresent()
          ? versionStatuses.stream().findFirst().get().name() : null);
    }
  }
}
