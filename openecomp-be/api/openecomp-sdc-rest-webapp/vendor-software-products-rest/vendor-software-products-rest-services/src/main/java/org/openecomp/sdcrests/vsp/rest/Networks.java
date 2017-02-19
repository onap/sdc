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
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NetworkDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NetworkRequestDto;
import org.springframework.validation.annotation.Validated;

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

@Path("/v1.0/vendor-software-products/{vspId}/networks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor Software Product Networks")
@Validated
public interface Networks {
  @GET
  @Path("/")
  @ApiOperation(value = "List vendor software product networks",
      response = NetworkDto.class,
      responseContainer = "List")
  Response list(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                @Pattern(regexp = Version.VERSION_REGEX,
                    message = Version.VERSION_STRING_VIOLATION_MSG) @QueryParam("version")
                    String version,
                @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_HEADER_PARAM)
                    String user);

  @POST
  @Path("/")
  @ApiOperation(value = "Create a vendor software product network")
  Response create(@Valid NetworkRequestDto request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_HEADER_PARAM)
                      String user);

  @GET
  @Path("/{networkId}")
  @ApiOperation(value = "Get vendor software product network",
      response = NetworkDto.class)
  Response get(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
               @ApiParam(value = "Vendor software product network Id") @PathParam("networkId")
                   String networkId,
               @Pattern(regexp = Version.VERSION_REGEX,
                   message = Version.VERSION_STRING_VIOLATION_MSG) @QueryParam("version")
                   String version,
               @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_HEADER_PARAM)
                   String user);

  @DELETE
  @Path("/{networkId}")
  @ApiOperation(value = "Delete vendor software product network")
  Response delete(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Vendor software product network Id") @PathParam("networkId")
                      String networkId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_HEADER_PARAM)
                      String user);

  @PUT
  @Path("/{networkId}")
  @ApiOperation(value = "Update vendor software product network")
  Response update(@Valid NetworkRequestDto request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Vendor software product network Id") @PathParam("networkId")
                      String networkId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_HEADER_PARAM)
                      String user);
}
