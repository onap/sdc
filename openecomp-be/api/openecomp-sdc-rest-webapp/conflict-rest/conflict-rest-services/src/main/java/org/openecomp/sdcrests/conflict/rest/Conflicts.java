package org.openecomp.sdcrests.conflict.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.openecomp.sdcrests.common.RestConstants;
import org.openecomp.sdcrests.conflict.types.ConflictDto;
import org.openecomp.sdcrests.conflict.types.ConflictResolutionDto;
import org.openecomp.sdcrests.conflict.types.ItemVersionConflictDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1.0/items/{itemId}/versions/{versionId}/conflicts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(info = @Info(title = "Item Version Conflicts"))
@Validated
public interface Conflicts {

  @GET
  @Path("/")
  @Operation(description = "item version conflicts",
      summary = "Item version private copy conflicts against its public copy",
          responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ItemVersionConflictDto.class))))
  Response getConflict(@Parameter(description = "Item Id") @PathParam("itemId") String itemId,
                       @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                       @NotNull(message = RestConstants.USER_MISSING_ERROR_MSG)
                        @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{conflictId}")
  @Operation(description = "Gets item version conflict",
      summary = "Gets an item version private copy conflict against its public copy",
          responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ConflictDto.class))))
  Response getConflict(@Parameter(description = "Item Id") @PathParam("itemId") String itemId,
                       @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                       @Parameter(description = "Version Id") @PathParam("conflictId") String conflictId,
                       @NotNull(message = RestConstants.USER_MISSING_ERROR_MSG)
                       @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{conflictId}")
  @Operation(description = "Resolves item version conflict",
      summary = "Resolves an item version private copy conflict against its public copy")
  Response resolveConflict(ConflictResolutionDto conflictResolution,
                           @Parameter(description = "Item Id") @PathParam("itemId") String itemId,
                           @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                           @Parameter(description = "Version Id") @PathParam("conflictId") String conflictId,
                           @NotNull(message = RestConstants.USER_MISSING_ERROR_MSG)
                           @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);
}
