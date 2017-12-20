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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ProcessEntityDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ProcessRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/components/{componentId}/processes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor Software Product Component Processes")
@Validated
public interface ComponentProcesses extends VspEntities {
  @GET
  @Path("/")
  @ApiOperation(value = "List vendor software product component processes",
      response = ProcessEntityDto.class,
      responseContainer = "List")
  Response list(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
                @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
                    String componentId,
                @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                    String user);

  @DELETE
  @Path("/")
  @ApiOperation(value = "Delete vendor software product processes",
      responseContainer = "List")
  Response deleteList(
      @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
      @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
      @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
          String componentId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @POST
  @Path("/")
  @ApiOperation(value = "Create a vendor software product process")
  Response create(@Valid ProcessRequestDto request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/{processId}")
  @ApiOperation(value = "Get vendor software product process",
      response = ProcessEntityDto.class)
  Response get(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
               @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
               @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
                   String componentId,
               @ApiParam(value = "Vendor software product process Id") @PathParam("processId")
                   String processId,
               @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                   String user);

  @DELETE
  @Path("/{processId}")
  @ApiOperation(value = "Delete vendor software product process")
  Response delete(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @ApiParam(value = "Vendor software product process Id") @PathParam("processId")
                      String processId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @PUT
  @Path("/{processId}")
  @ApiOperation(value = "Update vendor software product process")
  Response update(@Valid ProcessRequestDto request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @ApiParam(value = "Vendor software product process Id") @PathParam("processId")
                      String processId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/{processId}/upload")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Get vendor software product process uploaded file")
  Response getUploadedFile(
      @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
      @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
      @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
          String componentId,
      @ApiParam(value = "Vendor software product process Id") @PathParam("processId")
          String processId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @DELETE
  @Path("/{processId}/upload")
  @ApiOperation(value = "Delete vendor software product process uploaded file")
  Response deleteUploadedFile(
      @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
      @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
      @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
          String componentId,
      @ApiParam(value = "Vendor software product process Id") @PathParam("processId")
          String processId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @POST
  @Path("/{processId}/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ApiOperation(value = "Update vendor software product process upload")
  Response uploadFile(@Multipart("upload") Attachment attachment,
                      @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                      @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
                      @ApiParam(value = "Vendor software product component Id")
                      @PathParam("componentId") String componentId,
                      @ApiParam(value = "Vendor software product process Id")
                      @PathParam("processId") String processId,
                      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                          String user);
}
