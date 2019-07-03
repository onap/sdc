package org.openecomp.sdcrests.vsp.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@OpenAPIDefinition(info = @Info(title = "Vendor Software Product Component Dependencies"))
@Validated
public interface ComponentDependencies extends VspEntities {

  @POST
  @Path("/")
  @Operation( description= "Create a vendor software product component dependency")
  Response create(@Valid ComponentDependencyModel request,
                  @Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/")
  @Operation(description = "Get component dependencies for vendor software product", responses = @ApiResponse(content = @Content(array = @ArraySchema( schema = @Schema(implementation = ComponentDependencyResponseDto.class)))))
  Response list(@Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                @Parameter(description = "Vendor software product version Id") @PathParam("versionId")
                    String versionId,
                @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                    String user);

  @DELETE
  @Path("/{dependencyId}")
  @Operation(description = "Delete component dependency for vendor software product")
  Response delete(@Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @Parameter(description = "Vendor software product version Id")
                  @PathParam("versionId") String versionId,
                  @Parameter(description = "Vendor software product Component Dependency Id") @PathParam
                      ("dependencyId") String dependencyId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @PUT
  @Path("/{dependencyId}")
  @Operation(description = "Update component dependency for vendor software product")
  Response update(@Valid ComponentDependencyModel request,
                  @Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @Parameter(description = "Vendor software product version Id") @PathParam("versionId")
                      String versionId,
                  @Parameter(description = "Vendor software product Component Dependency Id") @PathParam
                      ("dependencyId") String dependencyId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/{dependencyId}")
  @Operation(description = "Get component dependency for vendor software product", responses = @ApiResponse(content = @Content(schema = @Schema(implementation =ComponentDependencyResponseDto.class))))
  Response get(@Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
               @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
               @Parameter(description = "Vendor software product Component Dependency Id") @PathParam
                   ("dependencyId") String dependencyId,
               @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                   String user);
}
