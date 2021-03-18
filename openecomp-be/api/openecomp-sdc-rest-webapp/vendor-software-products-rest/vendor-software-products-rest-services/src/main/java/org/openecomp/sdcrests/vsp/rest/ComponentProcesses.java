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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ProcessEntityDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ProcessRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/components/{componentId}/processes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor Software Product Component Processes")})
@Validated
public interface ComponentProcesses extends VspEntities {
  @GET
  @Path("/")
  @Operation(description = "List vendor software product component processes", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProcessEntityDto.class)))))
  Response list(@Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                @Parameter(description = "Vendor software product version Id") @PathParam("versionId") String versionId,
                @Parameter(description = "Vendor software product component Id") @PathParam("componentId")
                    String componentId,
                @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                    String user);

  @DELETE
  @Path("/")
  @Operation(description = "Delete vendor software product processes",responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
  Response deleteList(
      @Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
      @Parameter(description = "Vendor software product version Id") @PathParam("versionId") String versionId,
      @Parameter(description = "Vendor software product component Id") @PathParam("componentId")
          String componentId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @POST
  @Path("/")
  @Operation(description = "Create a vendor software product process")
  Response create(@Valid ProcessRequestDto request,
                  @Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @Parameter(description = "Vendor software product version Id") @PathParam("versionId") String versionId,
                  @Parameter(description = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/{processId}")
  @Operation(description = "Get vendor software product process",responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ProcessEntityDto.class))))
  Response get(@Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
               @Parameter(description = "Vendor software product version Id") @PathParam("versionId") String versionId,
               @Parameter(description = "Vendor software product component Id") @PathParam("componentId")
                   String componentId,
               @Parameter(description = "Vendor software product process Id") @PathParam("processId")
                   String processId,
               @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                   String user);

  @DELETE
  @Path("/{processId}")
  @Operation(description = "Delete vendor software product process")
  Response delete(@Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @Parameter(description = "Vendor software product version Id") @PathParam("versionId") String versionId,
                  @Parameter(description = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @Parameter(description = "Vendor software product process Id") @PathParam("processId")
                      String processId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @PUT
  @Path("/{processId}")
  @Operation(description = "Update vendor software product process")
  Response update(@Valid ProcessRequestDto request,
                  @Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @Parameter(description = "Vendor software product version Id") @PathParam("versionId") String versionId,
                  @Parameter(description = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @Parameter(description = "Vendor software product process Id") @PathParam("processId")
                      String processId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/{processId}/upload")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Operation(description = "Get vendor software product process uploaded file")
  Response getUploadedFile(
      @Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
      @Parameter(description = "Vendor software product version Id") @PathParam("versionId") String versionId,
      @Parameter(description = "Vendor software product component Id") @PathParam("componentId")
          String componentId,
      @Parameter(description = "Vendor software product process Id") @PathParam("processId")
          String processId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @DELETE
  @Path("/{processId}/upload")
  @Operation(description = "Delete vendor software product process uploaded file")
  Response deleteUploadedFile(
      @Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
      @Parameter(description = "Vendor software product version Id") @PathParam("versionId") String versionId,
      @Parameter(description = "Vendor software product component Id") @PathParam("componentId")
          String componentId,
      @Parameter(description = "Vendor software product process Id") @PathParam("processId")
          String processId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @POST
  @Path("/{processId}/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Operation(description = "Update vendor software product process upload")
  Response uploadFile(@Multipart("upload") Attachment attachment,
                      @Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                      @Parameter(description = "Vendor software product version Id") @PathParam("versionId") String versionId,
                      @Parameter(description = "Vendor software product component Id")
                      @PathParam("componentId") String componentId,
                      @Parameter(description = "Vendor software product process Id")
                      @PathParam("processId") String processId,
                      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                          String user);
}
