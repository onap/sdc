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

package org.onap.sdc.activityspec.api.rest;

import io.swagger.annotations.*;
import org.onap.sdc.activityspec.api.rest.types.ActivitySpecActionRequestDto;
import org.onap.sdc.activityspec.api.rest.types.ActivitySpecRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.onap.sdc.activityspec.utils.ActivitySpecConstant.USER_ID_HEADER_PARAM;

@Path("/v1.0/activity-spec/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Activity Specs")
@Validated
public interface ActivitySpecs {

    @POST
    @Path("/")
    @ApiOperation(value = "Create Activity Spec")
    @ApiImplicitParams({@ApiImplicitParam(name = USER_ID_HEADER_PARAM, required = true, dataType = "string",
            paramType = "header")})
    Response createActivitySpec(@Valid ActivitySpecRequestDto request);

    @GET
    @Path("/{id}/versions/{versionId}")
    @ApiOperation(value = "Get Activity Spec")
    @ApiImplicitParams({@ApiImplicitParam(name = USER_ID_HEADER_PARAM, required = true, dataType = "string",
            paramType = "header")})
    Response getActivitySpec(@ApiParam(value = "Activity Spec Id") @PathParam("id") String id,
                                    @ApiParam(value = "Version Id") @PathParam("versionId") String versionId);

    @PUT
    @Path("/{id}/versions/{versionId}")
    @ApiOperation(value = "Update Activity Spec")
    @ApiImplicitParams({@ApiImplicitParam(name = USER_ID_HEADER_PARAM, required = true, dataType = "string",
            paramType = "header")})
    Response updateActivitySpec(@Valid ActivitySpecRequestDto request,
                                       @ApiParam(value = "Activity Spec Id") @PathParam("id") String id,
                                       @ApiParam(value = "Version Id") @PathParam("versionId") String versionId);

    @PUT
    @Path("/{id}/versions/{versionId}/actions")
    @ApiOperation(value = "Actions on a activity spec",
            notes = "Performs one of the following actions on a activity spec: |" + "CERTIFY: Certifies activity spec.|"
                            + "DEPRECATE: Deprecates activity spec.|" + "DELETE: Deletes activity spec.")
    @ApiImplicitParams({@ApiImplicitParam(name = USER_ID_HEADER_PARAM, required = true, dataType = "string",
            paramType = "header")})
    Response actOnActivitySpec(ActivitySpecActionRequestDto request,
                                      @ApiParam(value = "Activity Spec Id") @PathParam("id") String id,
                                      @ApiParam(value = "Version Id") @PathParam("versionId") String versionId);

    @GET
    @Path("/")
    @ApiOperation(value = "Get list of activity specs ", responseContainer = "List")
    @ApiImplicitParams({@ApiImplicitParam(name = USER_ID_HEADER_PARAM, required = true, dataType = "string",
            paramType = "header")})
    Response list(@ApiParam(value = "List activity specs based on status filter") @QueryParam("status")
                          String versionStatus);
}
