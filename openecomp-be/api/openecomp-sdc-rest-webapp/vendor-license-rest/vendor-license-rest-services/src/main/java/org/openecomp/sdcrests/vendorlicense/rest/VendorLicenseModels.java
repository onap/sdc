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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.openecomp.sdcrests.common.RestConstants;
import org.openecomp.sdcrests.item.types.ItemDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelActionRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/vendor-license-models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor License Models")})
@Validated
public interface VendorLicenseModels {

    @GET
    @Path("/")
    @Operation(description = "List vendor license models", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ItemDto.class)))))
    Response listLicenseModels(@Parameter(description = "Filter to return only Vendor License Models with at"
        + " least one version at this status. Currently supported values: 'Certified' , 'Draft'") @QueryParam("versionFilter") String versionStatus,
                               @Parameter(description = "Filter to only return Vendor License Models at this status."
                                   + "Currently supported values: 'ACTIVE' , 'ARCHIVED'."
                                   + "Default value = 'ACTIVE'.") @QueryParam("Status") String itemStatus,
                               @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user , @Context HttpServletRequest req);

    @POST
    @Path("/")
    @Operation(description = "Create vendor license model", responses = @ApiResponse(responseCode = "401", description = "Unauthorized Tenant"))
    Response createLicenseModel(@Valid VendorLicenseModelRequestDto request,
                                @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user, @Context HttpServletRequest req);

    @DELETE
    @Path("/{vlmId}")
    @Operation(description = "Delete vendor license model")
    Response deleteLicenseModel(@Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
                                @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);

    @PUT
    @Path("/{vlmId}/versions/{versionId}")
    @Operation(description = "Update vendor license model")
    Response updateLicenseModel(@Valid VendorLicenseModelRequestDto request,
                                @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
                                @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
                                @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);

    @GET
    @Path("/{vlmId}/versions/{versionId}")
    @Operation(description = "Get vendor license model", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = VendorLicenseModelEntityDto.class))))
    Response getLicenseModel(@Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
                             @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
                             @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);

    @PUT
    @Path("/{vlmId}/versions/{versionId}/actions")
    @Operation(description = "Update vendor license model")
    Response actOnLicenseModel(@Valid VendorLicenseModelActionRequestDto request,
                               @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
                               @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
                               @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);

}
