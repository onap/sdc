package org.openecomp.sdcrests.itempermissions.rest;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.openecomp.sdcrests.itempermissions.types.ItemPermissionsDto;
import org.openecomp.sdcrests.itempermissions.types.ItemPermissionsRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

/**
 * Created by ayalaben on 6/18/2017.
 */
@Path("/v1.0/items/{itemId}/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(info = @Info(title = "Item Permissions"))
@Validated
public interface ItemPermissions {

  @GET
  @Path("/")
  @Operation(description = "List users permissions assigned on item",responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ItemPermissionsDto.class)))))
  Response list(@PathParam("itemId") String itemId,
                @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{permission}")
  @Operation(description = "Update useres permission on item")
  Response updatePermissions(@Valid ItemPermissionsRequestDto request,
                             @PathParam("itemId") String itemId,
                             @PathParam("permission") String permission,
                             @NotNull(message = USER_MISSING_ERROR_MSG)
                             @HeaderParam(USER_ID_HEADER_PARAM) String user);

}
