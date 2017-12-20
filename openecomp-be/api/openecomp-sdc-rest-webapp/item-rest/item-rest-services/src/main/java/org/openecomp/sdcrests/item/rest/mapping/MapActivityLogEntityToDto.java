/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdcrests.item.rest.mapping;

import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdcrests.item.types.ActivityLogDto;
import org.openecomp.sdcrests.item.types.ActivityStatus;
import org.openecomp.sdcrests.mapping.MappingBase;

public class MapActivityLogEntityToDto
    extends MappingBase<ActivityLogEntity, ActivityLogDto> {


  @Override
  public void doMapping(ActivityLogEntity source, ActivityLogDto target) {
    target.setId(source.getId());
    target.setTimestamp(source.getTimestamp());
    target.setType(source.getType().name());
    target.setComment(source.getComment());
    target.setUser(source.getUser());
    target.setStatus(new ActivityStatus(source.isSuccess(), source.getMessage()));
  }
}
