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

import static org.openecomp.sdcrests.common.RestConstants.USER_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.PackageInfoDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VersionSoftwareProductActionRequestDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDescriptionDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.validation.IsValidJson;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/v1.0/vendor-software-products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor Software Products")
@Validated
public interface VendorSoftwareProducts {

  @POST
  @Path("/")
  @ApiOperation(value = "Create a new vendor software product",
      response = VspCreationDto.class)
  Response createNewVsp(@Valid VspDescriptionDto vspDescriptionDto,
                        @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_HEADER_PARAM)
                            String user);

  @GET
  @Path("/")
  @ApiOperation(value = "Get list of vendor software products and their description",
      responseContainer = "List")
  Response getVspList(@ApiParam(
      value = "Currently supported values: 'Final' - only vendor software products with final "
          + "version will be return - with their latest final version")
                      @QueryParam("versionFilter") String versionFilter,
                      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_HEADER_PARAM)
                          String user);

  @GET
  @Path("/packages")
  @ApiOperation(value = "Get list of translated CSAR files details",
      response = PackageInfoDto.class,
      responseContainer = "List")
  Response listPackages(@ApiParam("Category") @QueryParam("category") String category,
                        @ApiParam("Sub-category") @QueryParam("subCategory") String subCategory,
                        @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_HEADER_PARAM)
                            String user);

  @GET
  @Path("/{vspId}")
  @ApiOperation(value = "Get details of a vendor software product")
  Response getVspDetails(@PathParam("vspId") String vspId,
                         @Pattern(regexp = Version.VERSION_REGEX,
                             message = Version.VERSION_STRING_VIOLATION_MSG) @QueryParam("version")
                             String version,
                         @NotNull(message = USER_MISSING_ERROR_MSG)
                         @HeaderParam(USER_HEADER_PARAM) String user);

  @PUT
  @Path("/{vspId}")
  @ApiOperation(value = "Update an existing vendor software product")
  Response updateVsp(@PathParam("vspId") String vspId, @Valid VspDescriptionDto vspDescriptionDto,
                     @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_HEADER_PARAM)
                         String user);

  @DELETE
  @Path("/{vspId}")
  @ApiOperation(value = "Deletes vendor software product by given id")
  Response deleteVsp(@PathParam("vspId") String vspId,
                     @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_HEADER_PARAM)
                         String user);

  @POST
  @Path("{vspId}/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ApiOperation(value = "Uploads a HEAT package to translate",
      response = UploadFileResponseDto.class)
  Response uploadFile(@PathParam("vspId") String vspId,
                      @Multipart("upload") InputStream heatFileToUpload,
                      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_HEADER_PARAM)
                          String user);

  @GET
  @Path("/{vspId}/downloadHeat")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Get uploaded HEAT file",
      notes = "Downloads the latest HEAT package",
      response = File.class)
  Response getLatestHeatPackage(@PathParam("vspId") String vspId,
                                /*@NotNull(message = USER_MISSING_ERROR_MSG)*/
                                @HeaderParam(USER_HEADER_PARAM) String user);


  @PUT
  @Path("/{vspId}/actions")
  @ApiOperation(value = "Actions on a vendor software product",
      notes = "Performs one of the following actions on a vendor software product: |"
          + "Checkout: Locks it for edits by other users. Only the locking user sees the edited "
          + "version.|"
          + "Undo_Checkout: Unlocks it and deletes the edits that were done.|"
          + "Checkin: Unlocks it and activates the edited version to all users.| "
          + "Submit: Finalize its active version.|"
          + "Create_Package: Creates a CSAR zip file.|")
  /*@ApiResponses(value = {
            @ApiResponse(code = 200, message = "Action succeeded"),
            @ApiResponse(code = 417, message = "Validation before submit has failed",
            response = ValidationResponseDto.class)})*/
  Response actOnVendorSoftwareProduct(@PathParam("vspId") String vspId,
                                      VersionSoftwareProductActionRequestDto request,
                                      @NotNull(message = USER_MISSING_ERROR_MSG)
                                      @HeaderParam(USER_HEADER_PARAM) String user)
      throws IOException;

  @GET
  @Path("/packages/{vspId}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @ApiOperation(value = "Get translated CSAR file",
      notes = "Exports translated file to a zip file",
      response = File.class)
  Response getTranslatedFile(@PathParam("vspId") String vspId,
                             @Pattern(regexp = Version.VERSION_REGEX,
                                 message = Version.VERSION_STRING_VIOLATION_MSG)
                             @QueryParam("version") String version,
                             /*@NotNull(message = USER_MISSING_ERROR_MSG)*/
                             @HeaderParam(USER_HEADER_PARAM) String user);

  @GET
  @Path("/{vspId}/questionnaire")
  @ApiOperation(value = "Get vendor software product questionnaire",
      response = QuestionnaireResponseDto.class)
  Response getQuestionnaire(@PathParam("vspId") String vspId,
                            @Pattern(regexp = Version.VERSION_REGEX,
                                message = Version.VERSION_STRING_VIOLATION_MSG)
                            @QueryParam("version") String version,
                            @NotNull(message = USER_MISSING_ERROR_MSG)
                            @HeaderParam(USER_HEADER_PARAM) String user);

  @PUT
  @Path("/{vspId}/questionnaire")
  @ApiOperation(value = "Update vendor software product questionnaire")
  Response updateQuestionnaire(@NotNull @IsValidJson String questionnaireData,
                               @PathParam("vspId") String vspId,
                               @NotNull(message = USER_MISSING_ERROR_MSG)
                               @HeaderParam(USER_HEADER_PARAM) String user);
}
