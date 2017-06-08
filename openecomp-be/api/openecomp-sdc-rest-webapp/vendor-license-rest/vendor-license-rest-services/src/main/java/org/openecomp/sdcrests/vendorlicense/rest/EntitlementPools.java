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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.sdcrests.vendorlicense.types.EntitlementPoolEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.EntitlementPoolRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/vendor-license-models/{vlmId}/versions/{versionId}/entitlement-pools")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor License Model - Entitlement Pools")
@Validated
public interface EntitlementPools {
  @GET
  @Path("/")
  @ApiOperation(value = "List vendor entitlement pools",
      response = EntitlementPoolEntityDto.class,
      responseContainer = "List")
  Response listEntitlementPools(
      @ApiParam(value = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @ApiParam(value = "Vendor license model version Id") @PathParam("versionId") String versionId,

      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @POST
  @Path("/")
  @ApiOperation(value = "Create vendor entitlement pool")
  Response createEntitlementPool(@Valid EntitlementPoolRequestDto request,
                                 @ApiParam(value = "Vendor license model Id") @PathParam("vlmId")
                                     String vlmId,
                                 @ApiParam(value = "Vendor license model version Id") @PathParam
                                     ("versionId")
                                     String versionId,
                                 @NotNull(message = USER_MISSING_ERROR_MSG)
                                 @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{entitlementPoolId}")
  @ApiOperation(value = "Update vendor entitlement pool")
  Response updateEntitlementPool(@Valid EntitlementPoolRequestDto request,
                                 @ApiParam(value = "Vendor license model Id") @PathParam("vlmId")
                                     String vlmId,
                                 @ApiParam(value = "Vendor license model version Id")
                                 @PathParam("versionId") String versionId,
                                 @NotNull(message = USER_MISSING_ERROR_MSG)
                                 @PathParam("entitlementPoolId") String entitlementPoolId,
                                 @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{entitlementPoolId}")
  @ApiOperation(value = "Get vendor entitlement pool",
      response = EntitlementPoolEntityDto.class)
  Response getEntitlementPool(
      @ApiParam(value = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @ApiParam(value = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @PathParam("entitlementPoolId") String entitlementPoolId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @DELETE
  @Path("/{entitlementPoolId}")
  @ApiOperation(value = "Delete vendor entitlement pool")
  Response deleteEntitlementPool(
      @ApiParam(value = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @ApiParam(value = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @PathParam("entitlementPoolId") String entitlementPoolId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

}
