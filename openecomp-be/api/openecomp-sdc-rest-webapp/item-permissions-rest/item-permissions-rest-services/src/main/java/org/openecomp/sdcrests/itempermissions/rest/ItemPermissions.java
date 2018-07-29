package org.openecomp.sdcrests.itempermissions.rest;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(value = "Item Permissions")
@Validated
public interface ItemPermissions {

  @GET
  @Path("/")
  @ApiOperation(value = "List users permissions assigned on item",
      response = ItemPermissionsDto.class,
      responseContainer = "List")

  Response list(@PathParam("itemId") String itemId,
                @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{permission}")
  @ApiOperation(value = "Update useres permission on item")
  Response updatePermissions(@Valid ItemPermissionsRequestDto request,
                             @PathParam("itemId") String itemId,
                             @PathParam("permission") String permission,
                             @NotNull(message = USER_MISSING_ERROR_MSG)
                             @HeaderParam(USER_ID_HEADER_PARAM) String user);

}
