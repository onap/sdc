package org.openecomp.sdcrests.item.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.openecomp.sdcrests.item.types.ItemActionRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Items")
@Validated
public interface Items {

   @GET
   @Path("/{itemId}")
   @ApiOperation(value = "Get details of a item")
   Response getItem(@PathParam("itemId") String itemId,
                     @NotNull(message = USER_MISSING_ERROR_MSG)
                     @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{itemId}/actions")
  @ApiOperation(value = "Acts on item version")
  Response actOn(ItemActionRequestDto request,
                 @PathParam("itemId") String itemId,
                 @NotNull(message = USER_MISSING_ERROR_MSG)
                 @HeaderParam(USER_ID_HEADER_PARAM) String user);



}
