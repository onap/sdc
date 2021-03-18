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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.openecomp.sdcrests.vendorlicense.types.EntitlementPoolEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.EntitlementPoolRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/vendor-license-models/{vlmId}/versions/{versionId}/entitlement-pools")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor License Model - Entitlement Pools")})
@Validated
public interface EntitlementPools {
    @GET
    @Path("/")
    @Operation(description = "List vendor entitlement pools", responses = @ApiResponse(content = @Content(array = @ArraySchema( schema = @Schema(implementation = EntitlementPoolEntityDto.class)))))
    Response listEntitlementPools(
            @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
            @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,

            @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

    @POST
    @Path("/")
    @Operation(description = "Create vendor entitlement pool")
    Response createEntitlementPool(@Valid EntitlementPoolRequestDto request,
                                   @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
                                   @Parameter(description = "Vendor license model version Id") @PathParam ("versionId") String versionId,
                                   @NotNull(message = USER_MISSING_ERROR_MSG)
                                   @HeaderParam(USER_ID_HEADER_PARAM) String user);

    @PUT
    @Path("/{entitlementPoolId}")
    @Operation(description = "Update vendor entitlement pool")
    Response updateEntitlementPool(@Valid EntitlementPoolRequestDto request,
                                   @Parameter(description = "Vendor license model Id") @PathParam("vlmId")
                                           String vlmId,
                                   @Parameter(description = "Vendor license model version Id")
                                   @PathParam("versionId") String versionId,
                                   @NotNull(message = USER_MISSING_ERROR_MSG)
                                   @PathParam("entitlementPoolId") String entitlementPoolId,
                                   @HeaderParam(USER_ID_HEADER_PARAM) String user);

    @GET
    @Path("/{entitlementPoolId}")
    @Operation(description = "Get vendor entitlement pool", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntitlementPoolEntityDto.class))))
    Response getEntitlementPool(
            @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
            @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
            @PathParam("entitlementPoolId") String entitlementPoolId,
            @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

    @DELETE
    @Path("/{entitlementPoolId}")
    @Operation(description = "Delete vendor entitlement pool")
    Response deleteEntitlementPool(
            @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
            @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
            @PathParam("entitlementPoolId") String entitlementPoolId,
            @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

}
