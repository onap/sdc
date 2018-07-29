/*
 * Copyright © 2018 European Support Limited
 *
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
 */


package org.openecomp.sdcrests.vendorlicense.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.sdcrests.common.RestConstants;
import org.openecomp.sdcrests.item.types.ItemDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelActionRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;


@Path("/v1.0/vendor-license-models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor License Models")
@Validated
public interface VendorLicenseModels {

  @GET
  @Path("/")
  @ApiOperation(value = "List vendor license models",
      response = ItemDto.class,
      responseContainer = "List")
  Response listLicenseModels(@ApiParam(value = "Filter to return only Vendor License Models with at" +
                            " least one version at this status. Currently supported values: 'Certified' , 'Draft'")
                            @QueryParam("versionFilter") String versionStatus,
                            @ApiParam(value = "Filter to only return Vendor License Models at this status." +
                            "Currently supported values: 'ACTIVE' , 'ARCHIVED'." +
                            "Default value = 'ACTIVE'.")
                            @QueryParam("Status") String itemStatus,
                            @NotNull(message = USER_MISSING_ERROR_MSG)
                            @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);

  @POST
  @Path("/")
  @ApiOperation(value = "Create vendor license model")
  Response createLicenseModel(@Valid VendorLicenseModelRequestDto request,
                              @NotNull(message = USER_MISSING_ERROR_MSG)
                              @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);

  @DELETE
  @Path("/{vlmId}")
  @ApiOperation(value = "Delete vendor license model")
  Response deleteLicenseModel(
            @ApiParam(value = "Vendor license model Id") @PathParam("vlmId") String vlmId,
            @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(RestConstants.USER_ID_HEADER_PARAM)
                    String user);

  @PUT
  @Path("/{vlmId}/versions/{versionId}")
  @ApiOperation(value = "Update vendor license model")
  Response updateLicenseModel(@Valid VendorLicenseModelRequestDto request,
                              @ApiParam(value = "Vendor license model Id")
                              @PathParam("vlmId") String vlmId,
                              @ApiParam(value = "Vendor license model version Id")
                              @PathParam("versionId") String versionId,
                              @NotNull(message = USER_MISSING_ERROR_MSG)
                              @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{vlmId}/versions/{versionId}")
  @ApiOperation(value = "Get vendor license model",
      response = VendorLicenseModelEntityDto.class)
  Response getLicenseModel(
      @ApiParam(value = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @ApiParam(value = "Vendor license model version Id") @PathParam
          ("versionId") String versionId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(RestConstants.USER_ID_HEADER_PARAM)
          String user);


  @PUT
  @Path("/{vlmId}/versions/{versionId}/actions")
  @ApiOperation(value = "Update vendor license model")
  Response actOnLicenseModel(@Valid VendorLicenseModelActionRequestDto request,
                             @ApiParam(value = "Vendor license model Id") @PathParam("vlmId")
                                 String vlmId,
                             @ApiParam(value = "Vendor license model version Id") @PathParam
                                 ("versionId") String versionId,
                             @NotNull(message = USER_MISSING_ERROR_MSG)
                             @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);
}
