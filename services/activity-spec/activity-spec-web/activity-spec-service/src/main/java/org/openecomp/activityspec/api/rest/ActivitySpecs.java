/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.activityspec.api.rest;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.activityspec.api.rest.types.ActivitySpecActionRequestDto;
import org.openecomp.activityspec.api.rest.types.ActivitySpecRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1.0/activity-spec/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Activity Specs")
@Validated
public interface ActivitySpecs {
  @POST
  @Path("/")
  @ApiOperation(value = "Create Activity Spec")
  Response createActivitySpec(@Valid ActivitySpecRequestDto request);

  @GET
  @Path("/{id}/versions/{versionId}")
  @ApiOperation(value = "Get Activity Spec")
  Response getActivitySpec(@ApiParam(value = "Activity Spec Id") @PathParam("id")
                                  String id,
                              @ApiParam(value = "Version Id") @PathParam("versionId")
                                  String versionId);

  @PUT
  @Path("/{id}/versions/{versionId}")
  @ApiOperation(value = "Update Activity Spec")
  Response updateActivitySpec(@Valid ActivitySpecRequestDto request,
                              @ApiParam(value = "Activity Spec Id") @PathParam("id")
                                  String id,
                              @ApiParam(value = "Version Id") @PathParam("versionId")
                                  String versionId);

  @PUT
  @Path("/{id}/versions/{versionId}/actions")
  @ApiOperation(value = "Actions on a activity spec",
      notes = "Performs one of the following actions on a activity spec: |"
          + "Submit: Finalize its active version.|"
          + "Deprecate: Deprecate activity spec.|")
  Response actOnActivitySpec(ActivitySpecActionRequestDto request,
      @ApiParam(value = "Activity Spec Id") @PathParam("id") String id,
      @ApiParam(value = "Version Id") @PathParam("versionId") String versionId);

  @GET
  @Path("/")
  @ApiOperation(value = "Get list of activity specs ",
      responseContainer = "List")
  Response list(@ApiParam(
      value = "Currently supported values: 'Certified' - only activity specs with Certified status")
                    @QueryParam("status") String versionStatus);
}
