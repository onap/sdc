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

package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDto;

public class MapComponentDataToComponentDto extends MappingBase<ComponentData, ComponentDto> {
  @Override
  public void doMapping(ComponentData source, ComponentDto target) {
    target.setName(source.getName());
    target.setDisplayName(
        source.getDisplayName() == null ? source.getName() : source.getDisplayName());
    target.setDescription(source.getDescription());
  }
}
