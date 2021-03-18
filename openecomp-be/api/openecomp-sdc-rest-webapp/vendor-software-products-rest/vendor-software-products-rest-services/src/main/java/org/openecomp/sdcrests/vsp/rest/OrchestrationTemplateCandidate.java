/*
 * Copyright © 2016-2018 European Support Limited
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
 */

package org.openecomp.sdcrests.vsp.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.FileDataStructureDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/orchestration-template-candidate")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Orchestration Template Candidate")})
@Validated
public interface OrchestrationTemplateCandidate extends VspEntities {

  @POST
  @Path("/")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  Response upload(
      @PathParam("vspId") String vspId,
      @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
      @Multipart("upload") Attachment fileToUpload,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Operation(description = "Get uploaded Network Package file",
      summary = "Downloads in uploaded Network Package file", responses = @ApiResponse(content = @Content(schema = @Schema(implementation =File.class))))
  Response get(
      @PathParam("vspId") String vspId,
      @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user)
      throws IOException;

  @DELETE
  @Path("/")
  @Operation(description = "Delete orchestration template candidate file and its files data structure")
  Response abort(
      @PathParam("vspId") String vspId,
      @Parameter(description = "Version Id") @PathParam("versionId") String versionId)
      throws Exception;

  @PUT
  @Path("/process")
  @Operation(description = "process Orchestration Template Candidate",responses = @ApiResponse(content = @Content(schema = @Schema(implementation =UploadFileResponseDto.class))))
  Response process(
      @PathParam("vspId") String vspId,
      @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user)
      throws InvocationTargetException, IllegalAccessException;

  @PUT
  @Path("/manifest")
  @Operation(description = "Update an existing vendor software product")
  Response updateFilesDataStructure(
      @PathParam("vspId") String vspId,
      @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
      @Valid FileDataStructureDto fileDataStructureDto,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user)
      throws Exception;

  @GET
  @Path("/manifest")
  @Operation(description = "Get uploaded HEAT file files data structure",
      summary = "Downloads the latest HEAT package",responses = @ApiResponse(content = @Content(schema = @Schema(implementation =FileDataStructureDto.class))))
  Response getFilesDataStructure(
      @PathParam("vspId") String vspId,
      @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user)
      throws Exception;
}
