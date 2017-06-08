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

package org.openecomp.sdcrests.vsp.rest;

import com.sun.jersey.multipart.FormDataParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/vendor-software-products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor Software Products")
@Validated
public interface VendorSoftwareProductsForSwaggerFileUpload {



  @POST
  @Path("/{vspId}/versions/{versionId}/orchestration-template-candidate")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ApiOperation(value = "Uploads a HEAT package to translate",
      response = UploadFileResponseDto.class)
  Response uploadOrchestrationTemplateCandidate(@PathParam("vspId") String vspId,
                                                @PathParam("versionId") String versionId,
                                                @FormDataParam("upload")
                                                    InputStream heatFileToUpload,
                                                @NotNull(message = USER_MISSING_ERROR_MSG)
                                                @HeaderParam(USER_ID_HEADER_PARAM) String user);

 }
