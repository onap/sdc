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

package org.openecomp.sdcrests.common.mapping;

import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdcrests.common.types.ErrorMessageDto;
import org.openecomp.sdcrests.mapping.MappingBase;

public class MapErrorMessageToDto extends MappingBase<ErrorMessage, ErrorMessageDto> {
  @Override
  public void doMapping(ErrorMessage source, ErrorMessageDto target) {
    target.setMessage(source.getMessage());
    target.setLevel(source.getLevel());
  }
}
