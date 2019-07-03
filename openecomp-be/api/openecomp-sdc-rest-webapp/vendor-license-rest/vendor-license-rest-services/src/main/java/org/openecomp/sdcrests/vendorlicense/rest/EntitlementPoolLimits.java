package org.openecomp.sdcrests.vendorlicense.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    "/v1.0/vendor-license-models/{vlmId}/versions/{versionId}/entitlement-pools/{entitlementPoolId}/limits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(info = @Info(title = "Vendor License Model - Entitlement Pool Limits"))
@Validated
public interface EntitlementPoolLimits {

  @POST
  @Path("/")
  @Operation(description = "Create vendor entitlement pool limits")
  Response createLimit(@Valid LimitRequestDto request,
                       @Parameter(description = "Vendor license model Id") @PathParam("vlmId")
                           String vlmId,
                       @Parameter(description = "Vendor license model version Id") @PathParam
                           ("versionId")
                           String versionId,
                       @Parameter(description = "Vendor license model Entitlement Pool Id")
                       @PathParam("entitlementPoolId")
                           String entitlementPoolId,
                       @NotNull(message = USER_MISSING_ERROR_MSG)
                       @HeaderParam(USER_ID_HEADER_PARAM) String user);


  @GET
  @Path("/")
  @Operation(description = "List vendor entitlement pool limits", responses = @ApiResponse(content = @Content(array = @ArraySchema( schema = @Schema(implementation = LimitRequestDto.class)))))
  Response listLimits(
      @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @Parameter(description = "Vendor license model Entitlement Pool Id") @PathParam("entitlementPoolId")
          String entitlementPoolId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{limitId}")
  @Operation(description = "Update vendor entitlement pool limit")
  Response updateLimit(@Valid LimitRequestDto request,
                       @Parameter(description = "Vendor license model Id") @PathParam("vlmId")
                           String vlmId,
                       @Parameter(description = "Vendor license model version Id") @PathParam
                           ("versionId")
                           String versionId,
                       @Parameter(description = "Vendor license model Entitlement Pool Id")
                       @PathParam("entitlementPoolId")
                           String entitlementPoolId,
                       @NotNull(message = USER_MISSING_ERROR_MSG)
                       @PathParam("limitId") String limitId,
                       @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{limitId}")
  @Operation(description = "Get vendor entitlement pool limit", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = LimitEntityDto.class))))
  Response getLimit(
      @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @Parameter(description = "Vendor license model Entitlement Pool Id") @PathParam
          ("entitlementPoolId") String entitlementPoolId,
      @Parameter(description = "Vendor license model Entitlement Pool Limit Id") @PathParam("limitId")
          String limitId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @DELETE
  @Path("/{limitId}")
  @Operation(description = "Delete vendor entitlement pool limit")
  Response deleteLimit(
      @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @Parameter(description = "Vendor license model Entitlement pool Id") @PathParam("entitlementPoolId")
          String entitlementPoolId,
      @PathParam("limitId") String limitId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

}
