/*
 * Copyright © 2018 European Support Limited
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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.openecomp.sdcrests.item.types.ItemCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.*;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.validation.IsValidJson;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/vendor-software-products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor Software Products")})
@Validated
public interface VendorSoftwareProducts extends VspEntities {

  @POST
  @Path("/")
  @Operation(description = "Create a new vendor software product",responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ItemCreationDto.class))))
  Response createVsp(@Valid VspRequestDto vspRequestDto,
                     @NotNull(message = USER_MISSING_ERROR_MSG)
                     @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/")
  @Operation(description = "Get list of vendor software products and their description",responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = VspDetailsDto.class)))))
  Response listVsps(@Parameter(description = "Filter to return only Vendor Software Products with at" +
                    " least one version at this status. Currently supported values: 'Certified' , 'Draft'")
                    @QueryParam("versionFilter") String versionStatus,
                    @Parameter(description = "Filter to only return Vendor Software Products at this status." +
                    "Currently supported values: 'ACTIVE' , 'ARCHIVED'." +
                    "Default value = 'ACTIVE'.")
                    @QueryParam("Status") String itemStatus,
                    @NotNull(message = USER_MISSING_ERROR_MSG)
                    @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{vspId}/versions/{versionId}")
  @Parameter(description = "Get details of a vendor software product")
  Response getVsp(@PathParam("vspId") String vspId,
                  @PathParam("versionId") String versionId,
                  @NotNull(message = USER_MISSING_ERROR_MSG)
                  @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{vspId}/versions/{versionId}")
  @Parameter(description = "Update an existing vendor software product")
  Response updateVsp(@PathParam("vspId") String vspId,
                     @PathParam("versionId") String versionId,
                     @Valid VspDescriptionDto vspDescriptionDto,
                     @NotNull(message = USER_MISSING_ERROR_MSG)
                     @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @DELETE
  @Path("/{vspId}")
  @Parameter(description = "Deletes vendor software product by given id")
  Response deleteVsp(@PathParam("vspId") String vspId,
                     @NotNull(message = USER_MISSING_ERROR_MSG)
                     @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/packages")
  @Operation(description = "Get list of translated CSAR files details", responses = @ApiResponse(content = @Content(array = @ArraySchema( schema = @Schema(implementation=PackageInfoDto.class)))))
  Response listPackages(@Parameter(description = "Vendor Software Product status filter. " +
                        "Currently supported values: 'ACTIVE', 'ARCHIVED'")
                        @QueryParam("Status") String status,
                        @Parameter(description = "Category") @QueryParam("category") String category,
                        @Parameter(description = "Sub-category") @QueryParam("subCategory") String subCategory,
                        @NotNull(message = USER_MISSING_ERROR_MSG)
                        @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{vspId}/versions/{versionId}/orchestration-template")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Operation(description = "Get Orchestration Template (HEAT) file", responses = @ApiResponse(content = @Content(schema = @Schema(implementation=File.class))))
  Response getOrchestrationTemplate(
      @PathParam("vspId") String vspId,
      @PathParam("versionId") String versionId,
      @HeaderParam(USER_ID_HEADER_PARAM) String user);


  @GET
  @Path("/validation-vsp")
  Response getValidationVsp(
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user)
      throws Exception;

  @PUT
  @Path("/{vspId}/versions/{versionId}/actions")
  @Operation(description = "Actions on a vendor software product",
      summary = "Performs one of the following actions on a vendor software product: |"
          + "Checkout: Locks it for edits by other users. Only the locking user sees the edited "
          + "version.|"
          + "Undo_Checkout: Unlocks it and deletes the edits that were done.|"
          + "Checkin: Unlocks it and activates the edited version to all users.| "
          + "Submit: Finalize its active version.|"
          + "Create_Package: Creates a CSAR zip file.|")
  Response actOnVendorSoftwareProduct(VersionSoftwareProductActionRequestDto request,
                                      @PathParam("vspId") String vspId,
                                      @PathParam("versionId") String versionId,
                                      @NotNull(message = USER_MISSING_ERROR_MSG)
                                      @HeaderParam(USER_ID_HEADER_PARAM) String user)
      throws IOException;

  @GET
  @Path("/packages/{vspId}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Operation(description = "Get translated CSAR file",
      summary = "Exports translated file to a zip file", responses = @ApiResponse(content = @Content(schema = @Schema(implementation=File.class))))
  Response getTranslatedFile(@PathParam("vspId") String vspId,
                             @QueryParam("versionId") String versionId,
                             @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{vspId}/versions/{versionId}/questionnaire")
  @Operation(description = "Get vendor software product questionnaire", responses = @ApiResponse(content = @Content(schema = @Schema(implementation=QuestionnaireResponseDto.class))))
  Response getQuestionnaire(@PathParam("vspId") String vspId,
                            @PathParam("versionId") String versionId,
                            @NotNull(message = USER_MISSING_ERROR_MSG)
                            @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{vspId}/versions/{versionId}/questionnaire")
  @Operation(description = "Update vendor software product questionnaire")
  Response updateQuestionnaire(@NotNull @IsValidJson String questionnaireData,
                               @PathParam("vspId") String vspId,
                               @PathParam("versionId") String versionId,
                               @NotNull(message = USER_MISSING_ERROR_MSG)
                               @HeaderParam(USER_ID_HEADER_PARAM) String user);


  @PUT
  @Path("/{vspId}/versions/{versionId}/heal")
  @Operation(description = "Checkout and heal vendor software product questionnaire",responses = @ApiResponse(content = @Content(schema = @Schema(implementation=QuestionnaireResponseDto.class))))
  Response heal(@PathParam("vspId") String vspId,
                @PathParam("versionId") String versionId,
                @NotNull(message = USER_MISSING_ERROR_MSG)
                @HeaderParam(USER_ID_HEADER_PARAM) String user);


  @GET
  @Path("/{vspId}/versions/{versionId}/vspInformationArtifact")
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(description = "Get vendor software product information artifact for specified version",responses = @ApiResponse(content = @Content(schema = @Schema(implementation=File.class))))
  Response getVspInformationArtifact(@PathParam("vspId") String vspId,
                                     @PathParam("versionId") String versionId,
                                     @NotNull(message = USER_MISSING_ERROR_MSG)
                                     @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{vspId}/versions/{versionId}/compute-flavors")
  @Operation(description = "Get list of vendor software product compute-flavors", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation=VspComputeDto.class)))))
  Response listComputes(@Parameter(description = "Vendor software product Id")
                        @PathParam("vspId") String vspId,
                        @PathParam("versionId") String versionId,
                        @NotNull(message = USER_MISSING_ERROR_MSG)
                        @HeaderParam(USER_ID_HEADER_PARAM) String user);
}
