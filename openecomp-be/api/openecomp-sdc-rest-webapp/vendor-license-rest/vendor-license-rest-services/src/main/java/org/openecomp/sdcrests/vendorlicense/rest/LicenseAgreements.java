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
import org.openecomp.sdcrests.vendorlicense.types.LicenseAgreementEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.LicenseAgreementModelDto;
import org.openecomp.sdcrests.vendorlicense.types.LicenseAgreementRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.LicenseAgreementUpdateRequestDto;
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


@Path("/v1.0/vendor-license-models/{vlmId}/versions/{versionId}/license-agreements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor License Model - License Agreements")
@Validated

public interface LicenseAgreements {

  @GET
  @Path("/")
  @ApiOperation(value = "List vendor license agreements",
      response = LicenseAgreementEntityDto.class,
      responseContainer = "List")
  Response listLicenseAgreements(
      @ApiParam(value = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @ApiParam(value = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @POST
  @Path("/")
  @ApiOperation(value = "Create vendor license agreement")
  Response createLicenseAgreement(@Valid LicenseAgreementRequestDto request,
                                  @ApiParam(value = "Vendor license model Id") @PathParam("vlmId")
                                      String vlmId,
                                  @ApiParam(value = "Vendor license model version Id")
                                  @PathParam("versionId") String versionId,
                                  @NotNull(message = USER_MISSING_ERROR_MSG)
                                  @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{licenseAgreementId}")
  @ApiOperation(value = "Update vendor license agreement")
  Response updateLicenseAgreement(@Valid LicenseAgreementUpdateRequestDto request,
                                  @ApiParam(value = "Vendor license model Id") @PathParam("vlmId")
                                      String vlmId,
                                  @ApiParam(value = "Vendor license model version Id")
                                  @PathParam("versionId") String versionId,
                                  @PathParam("licenseAgreementId") String licenseAgreementId,
                                  @NotNull(message = USER_MISSING_ERROR_MSG)
                                  @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{licenseAgreementId}")
  @ApiOperation(value = "Get vendor license agreement",
      response = LicenseAgreementModelDto.class)
  Response getLicenseAgreement(
      @ApiParam(value = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @ApiParam(value = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @PathParam("licenseAgreementId") String licenseAgreementId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @DELETE
  @Path("/{licenseAgreementId}")
  @ApiOperation(value = "Delete vendor license agreement")
  Response deleteLicenseAgreement(
      @ApiParam(value = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @ApiParam(value = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @PathParam("licenseAgreementId") String licenseAgreementId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

}
