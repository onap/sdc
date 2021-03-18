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

package org.openecomp.sdcrests.vendorlicense.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.openecomp.sdcrests.vendorlicense.types.FeatureGroupEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.FeatureGroupModelDto;
import org.openecomp.sdcrests.vendorlicense.types.FeatureGroupRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.FeatureGroupUpdateRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;


@Path("/v1.0/vendor-license-models/{vlmId}/versions/{versionId}/feature-groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor License Model - Feature Groups")})
@Validated
public interface FeatureGroups {

  @GET
  @Path("/")
  @Operation(description = "List vendor feature groups",responses = @ApiResponse(content = @Content(array = @ArraySchema( schema = @Schema(implementation =FeatureGroupEntityDto.class)))))
  Response listFeatureGroups(
      @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @POST
  @Path("/")
  @Operation(description = "Create vendor feature group")
  Response createFeatureGroup(@Valid FeatureGroupRequestDto request,
                              @Parameter(description = "Vendor license model Id") @PathParam("vlmId")
                                  String vlmId,
                              @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
                              @NotNull(message = USER_MISSING_ERROR_MSG)
                              @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{featureGroupId}")
  @Operation(description = "Update vendor feature group")
  Response updateFeatureGroup(@Valid FeatureGroupUpdateRequestDto request,
                              @Parameter(description = "Vendor license model Id") @PathParam("vlmId")
                                  String vlmId,
                              @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
                              @PathParam("featureGroupId") String featureGroupId,
                              @NotNull(message = USER_MISSING_ERROR_MSG)
                              @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{featureGroupId}")
  @Operation(description = "Get vendor feature group", responses = @ApiResponse(content = @Content(schema = @Schema(implementation =FeatureGroupModelDto.class))))
  Response getFeatureGroup(
      @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @PathParam("featureGroupId") String featureGroupId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @DELETE
  @Path("/{featureGroupId}")
  @Operation(description = "Delete vendor feature group")
  Response deleteFeatureGroup(
      @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @PathParam("featureGroupId") String featureGroupId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

}
