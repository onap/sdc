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
    "/v1.0/vendor-license-models/{vlmId}/versions/{versionId}/license-key-groups/{licenseKeyGroupId}/limits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(info = @Info(title = "Vendor License Model - License Key Group Limits"))
@Validated
public interface LicenseKeyGroupLimits {

  @POST
  @Path("/")
  @Operation(description = "Create vendor license key group limit")
  Response createLimit(@Valid LimitRequestDto request,
                       @Parameter(description = "Vendor license model Id") @PathParam("vlmId")
                           String vlmId,
                       @Parameter(description = "Vendor license model version Id") @PathParam
                           ("versionId")
                           String versionId,
                       @Parameter(description = "Vendor license model License Key Group Id")
                       @PathParam("licenseKeyGroupId")
                           String licenseKeyGroupId,
                       @NotNull(message = USER_MISSING_ERROR_MSG)
                       @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/")
  @Operation(description = "List vendor license key group limits", responses = @ApiResponse(content = @Content(array = @ArraySchema( schema = @Schema(implementation = LimitEntityDto.class)))))
  Response listLimits(
      @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @Parameter(description = "Vendor license model License Key Group Id") @PathParam("licenseKeyGroupId")
          String licenseKeyGroupId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{limitId}")
  @Operation(description = "Update vendor license key group limit")
  Response updateLimit(@Valid LimitRequestDto request,
                       @Parameter(description = "Vendor license model Id") @PathParam("vlmId")
                           String vlmId,
                       @Parameter(description = "Vendor license model version Id") @PathParam
                           ("versionId")
                           String versionId,
                       @Parameter(description = "Vendor license model License Key Group Id")
                       @PathParam("licenseKeyGroupId")
                           String licenseKeyGroupId,
                       @NotNull(message = USER_MISSING_ERROR_MSG)
                       @PathParam("limitId") String limitId,
                       @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{limitId}")
  @Operation(description = "Get vendor entitlement pool limit", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = LimitEntityDto.class))))
  Response getLimit(
      @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @Parameter(description = "Vendor license model License Key Group") @PathParam
          ("licenseKeyGroupId") String entitlementPoolId,
      @Parameter(description = "Vendor license model License Key Group Limit Id") @PathParam("limitId")
          String limitId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @DELETE
  @Path("/{limitId}")
  @Operation(description = "Delete vendor license key group limit")
  Response deleteLimit(
      @Parameter(description = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @Parameter(description = "Vendor license model version Id") @PathParam("versionId") String versionId,
      @Parameter(description = "Vendor license model license key group Id") @PathParam("licenseKeyGroupId")
          String licenseKeyGroupId,
      @PathParam("limitId") String limitId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);
}
