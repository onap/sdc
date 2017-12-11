/*-
 *
 * Copyright © 2016-2017 European Support Limited *
 *
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

package org.openecomp.sdcrests.action.rest.mapping;

import org.openecomp.sdc.action.types.Action;
import org.openecomp.sdcrests.action.types.ActionResponseDto;
import org.openecomp.sdcrests.mapping.MappingBase;

/**
 * Maps Source Action Object To Action Response DTO.
 */
public class MapActionToActionResponseDto extends MappingBase<Action, ActionResponseDto> {

  @Override
  public void doMapping(Action source, ActionResponseDto target) {
    target.setActionUuId(source.getActionUuId());
    target.setActionInvariantUuId(source.getActionInvariantUuId());
    target.setVersion(source.getVersion());
    if (source.getStatus() != null) {
      target.setStatus(source.getStatus().name());
    }
  }
}
