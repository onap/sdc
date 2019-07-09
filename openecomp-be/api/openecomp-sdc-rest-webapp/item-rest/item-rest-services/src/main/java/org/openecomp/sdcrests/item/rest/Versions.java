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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@Api(value = "Item Versions")
@Validated
public interface Versions {

  @GET
  @Path("/")
  @ApiOperation(value = "Lists item versions",
      response = VersionDto.class,
      responseContainer = "List")
  Response list(@PathParam("itemId") String itemId,
                @NotNull(message = USER_MISSING_ERROR_MSG)
                @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @POST
  @Path("/{versionId}")
  @ApiOperation(value = "Creates a new item version")
  Response create(VersionRequestDto request,
                  @PathParam("itemId") String itemId,
                  @PathParam("versionId") String versionId,
                  @NotNull(message = USER_MISSING_ERROR_MSG)
                  @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{versionId}")
  @ApiOperation(value = "Gets item version", response = VersionDto.class)
  Response get(@PathParam("itemId") String itemId,
               @PathParam("versionId") String versionId,
               @NotNull(message = USER_MISSING_ERROR_MSG)
               @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{versionId}/activity-logs")
  @ApiOperation(value = "Gets item version activity log",
      response = ActivityLogDto.class,
      responseContainer = "List")
  Response getActivityLog(@ApiParam("Item Id") @PathParam("itemId") String itemId,
                          @ApiParam("Version Id") @PathParam("versionId") String versionId,
                          @NotNull(message = USER_MISSING_ERROR_MSG)
                          @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{versionId}/revisions")
  @ApiOperation(value = "Gets item version revisions", response = RevisionDto.class,
      responseContainer = "List")
  Response listRevisions(@PathParam("itemId") String itemId,
                         @PathParam("versionId") String versionId,
                         @NotNull(message = USER_MISSING_ERROR_MSG)
               @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{versionId}/actions")
  @ApiOperation(value = "Acts on item version")
  Response actOn(VersionActionRequestDto request,
                 @PathParam("itemId") String itemId,
                 @PathParam("versionId") String versionId,
                 @NotNull(message = USER_MISSING_ERROR_MSG)
                 @HeaderParam(USER_ID_HEADER_PARAM) String user);
}
