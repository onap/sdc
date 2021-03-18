/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdcrests.item.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.openecomp.sdcrests.item.types.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/items/{itemId}/versions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Item Versions")})
@Validated
public interface Versions {

  @GET
  @Path("/")
  @Operation(description = "Lists item versions", responses = @ApiResponse(content = @Content(array = @ArraySchema( schema = @Schema(implementation = VersionDto.class)))))
  Response list(@PathParam("itemId") String itemId,
                @NotNull(message = USER_MISSING_ERROR_MSG)
                @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @POST
  @Path("/{versionId}")
  @Operation(description = "Creates a new item version")
  Response create(VersionRequestDto request,
                  @PathParam("itemId") String itemId,
                  @PathParam("versionId") String versionId,
                  @NotNull(message = USER_MISSING_ERROR_MSG)
                  @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{versionId}")
  @Operation(description = "Gets item version", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = VersionDto.class))))
  Response get(@PathParam("itemId") String itemId,
               @PathParam("versionId") String versionId,
               @NotNull(message = USER_MISSING_ERROR_MSG)
               @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{versionId}/activity-logs")
  @Operation(description = "Gets item version activity log", responses = @ApiResponse(content = @Content(array = @ArraySchema( schema = @Schema(implementation = ActivityLogDto.class)))))
  Response getActivityLog(@Parameter(description = "Item Id") @PathParam("itemId") String itemId,
                          @Parameter( description = "Version Id") @PathParam("versionId") String versionId,
                          @NotNull(message = USER_MISSING_ERROR_MSG)
                          @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{versionId}/revisions")
  @Operation(description = "Gets item version revisions", responses = @ApiResponse(content = @Content(array = @ArraySchema( schema = @Schema(implementation = ActivityLogDto.class)))))
  Response listRevisions(@PathParam("itemId") String itemId,
                         @PathParam("versionId") String versionId,
                         @NotNull(message = USER_MISSING_ERROR_MSG)
               @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{versionId}/actions")
  @Operation(description = "Acts on item version")
  Response actOn(VersionActionRequestDto request,
                 @PathParam("itemId") String itemId,
                 @PathParam("versionId") String versionId,
                 @NotNull(message = USER_MISSING_ERROR_MSG)
                 @HeaderParam(USER_ID_HEADER_PARAM) String user);
}
