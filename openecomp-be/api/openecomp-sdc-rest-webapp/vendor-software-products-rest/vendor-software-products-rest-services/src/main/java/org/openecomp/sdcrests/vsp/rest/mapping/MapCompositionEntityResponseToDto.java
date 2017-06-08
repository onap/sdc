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

import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionDataEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionDataEntityDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityResponseDto;

public class MapCompositionEntityResponseToDto<S extends CompositionDataEntity, T extends
    CompositionDataEntityDto, M extends MappingBase<S, T>>
    extends MappingBase<CompositionEntityResponse<S>, CompositionEntityResponseDto<T>> {
  private M dataMapper;
  private Class<T> targetDataClass;

  public MapCompositionEntityResponseToDto(M dataMapper, Class<T> targetDataClass) {
    this.dataMapper = dataMapper;
    this.targetDataClass = targetDataClass;
  }

  @Override
  public void doMapping(CompositionEntityResponse<S> source,
                        CompositionEntityResponseDto<T> target) {
    target.setId(source.getId());
    target.setSchema(source.getSchema());
    target.setData(dataMapper.applyMapping(source.getData(), targetDataClass));
  }
}
