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

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentRequestDto;

public class MapComponentRequestDtoToComponentEntity
    extends MappingBase<ComponentRequestDto, ComponentEntity> {
  @Override
  public void doMapping(ComponentRequestDto source, ComponentEntity target) {
    ComponentData component = new ComponentData();
    component.setName(source.getName());
    component.setDisplayName(source.getDisplayName());
    component.setVfcCode(source.getVfcCode());
    component.setNfcCode(source.getNfcCode());
    component.setNfcFunction(source.getNfcFunction());
    component.setDescription(source.getDescription());
    target.setComponentCompositionData(component);
  }
}
