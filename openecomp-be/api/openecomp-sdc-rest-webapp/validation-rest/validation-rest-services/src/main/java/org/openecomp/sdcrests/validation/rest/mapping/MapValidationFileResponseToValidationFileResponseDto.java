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

package org.openecomp.sdcrests.validation.rest.mapping;


import org.openecomp.sdc.validation.types.ValidationFileResponse;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.validation.types.ValidationFileResponseDto;

/**
 * Created by TALIO on 4/27/2016.
 */
public class MapValidationFileResponseToValidationFileResponseDto
    extends MappingBase<ValidationFileResponse, ValidationFileResponseDto> {
  @Override
  public void doMapping(ValidationFileResponse source, ValidationFileResponseDto target) {
    //target.setErrors(source.getErrors());
    //target.setFileNames(source.getFileNames());
    //target.setStatus(source.getStatus());
    target.setValidationData(source.getValidationData());
  }
}
