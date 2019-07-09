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

package org.openecomp.sdcrests.vsp.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModel;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyResponseDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/component-dependencies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor Software Product Component Dependencies")
@Validated
public interface ComponentDependencies extends VspEntities {

  @POST
  @Path("/")
  @ApiOperation(value = "Create a vendor software product component dependency")
  Response create(@Valid ComponentDependencyModel request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/")
  @ApiOperation(value = "Get component dependencies for vendor software product",
      response = ComponentDependencyResponseDto.class,
      responseContainer = "List")
  Response list(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                @ApiParam(value = "Vendor software product version Id") @PathParam("versionId")
                    String versionId,
                @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                    String user);

  @DELETE
  @Path("/{dependencyId}")
  @ApiOperation(value = "Delete component dependency for vendor software product")
  Response delete(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Vendor software product version Id")
                  @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product Component Dependency Id") @PathParam
                      ("dependencyId") String dependencyId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @PUT
  @Path("/{dependencyId}")
  @ApiOperation(value = "Update component dependency for vendor software product")
  Response update(@Valid ComponentDependencyModel request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Vendor software product version Id") @PathParam("versionId")
                      String versionId,
                  @ApiParam(value = "Vendor software product Component Dependency Id") @PathParam
                      ("dependencyId") String dependencyId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/{dependencyId}")
  @ApiOperation(value = "Get component dependency for vendor software product",
      response = ComponentDependencyResponseDto.class)
  Response get(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
               @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
               @ApiParam(value = "Vendor software product Component Dependency Id") @PathParam
                   ("dependencyId") String dependencyId,
               @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                   String user);
}
