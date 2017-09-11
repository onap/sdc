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


import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;

/**
 * Created by TALIO on 4/27/2016.
 */
public class MapUploadFileResponseToUploadFileResponseDto
    extends MappingBase<UploadFileResponse, UploadFileResponseDto> {
  @Override
  public void doMapping(UploadFileResponse source, UploadFileResponseDto target) {
    target.setStatus(source.getStatus());
    target.setErrors(source.getErrors());
    target.setOnboardingOrigin(source.getOnboardingType().toString());
    target.setNetworkPackageName(source.getNetworkPackageName());
  }
}
