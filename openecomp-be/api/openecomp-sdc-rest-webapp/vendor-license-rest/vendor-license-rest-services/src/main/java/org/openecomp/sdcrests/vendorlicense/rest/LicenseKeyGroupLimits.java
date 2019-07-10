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

package org.openecomp.sdcrests.vendorlicense.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.sdcrests.vendorlicense.types.LimitEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.LimitRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path(
    "/v1.0/vendor-license-models/{vlmId}/versions/{versionId}/license-key-groups/{licenseKeyGroupId}/limits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor License Model - License Key Group Limits")
@Validated
public interface LicenseKeyGroupLimits {

  @POST
  @Path("/")
  @ApiOperation(value = "Create vendor license key group limit")
  Response createLimit(@Valid LimitRequestDto request,
                       @ApiParam(value = "Vendor license model Id") @PathParam("vlmId")
                           String vlmId,
                       @ApiParam(value = "Vendor license model version Id") @PathParam
                           ("versionId")
                           String versionId,
                       @ApiParam(value = "Vendor license model License Key Group Id")
                       @PathParam("licenseKeyGroupId")
                           String licenseKeyGroupId,
                       @NotNull(message = USER_MISSING_ERROR_MSG)
                       @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/")
  @ApiOperation(value = "List vendor license key group limits",
      response = LimitEntityDto.class,
      responseContainer = "List")
  Response listLimits(
      @ApiParam(value = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @ApiParam(value = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @ApiParam(value = "Vendor license model License Key Group Id") @PathParam("licenseKeyGroupId")
          String licenseKeyGroupId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{limitId}")
  @ApiOperation(value = "Update vendor license key group limit")
  Response updateLimit(@Valid LimitRequestDto request,
                       @ApiParam(value = "Vendor license model Id") @PathParam("vlmId")
                           String vlmId,
                       @ApiParam(value = "Vendor license model version Id") @PathParam
                           ("versionId")
                           String versionId,
                       @ApiParam(value = "Vendor license model License Key Group Id")
                       @PathParam("licenseKeyGroupId")
                           String licenseKeyGroupId,
                       @NotNull(message = USER_MISSING_ERROR_MSG)
                       @PathParam("limitId") String limitId,
                       @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{limitId}")
  @ApiOperation(value = "Get vendor entitlement pool limit",
      response = LimitEntityDto.class)
  Response getLimit(
      @ApiParam(value = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @ApiParam(value = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @ApiParam(value = "Vendor license model License Key Group") @PathParam
          ("licenseKeyGroupId") String entitlementPoolId,
      @ApiParam(value = "Vendor license model License Key Group Limit Id") @PathParam("limitId")
          String limitId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @DELETE
  @Path("/{limitId}")
  @ApiOperation(value = "Delete vendor license key group limit")
  Response deleteLimit(
      @ApiParam(value = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @ApiParam(value = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @ApiParam(value = "Vendor license model license key group Id") @PathParam("licenseKeyGroupId")
          String licenseKeyGroupId,
      @PathParam("limitId") String limitId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);
}
