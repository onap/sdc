package org.openecomp.sdcrests.conflict.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.sdcrests.common.RestConstants;
import org.openecomp.sdcrests.conflict.types.ConflictDto;
import org.openecomp.sdcrests.conflict.types.ConflictResolutionDto;
import org.openecomp.sdcrests.conflict.types.ItemVersionConflictDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1.0/items/{itemId}/versions/{versionId}/conflicts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Item Version Conflicts")
@Validated
public interface Conflicts {

  @GET
  @Path("/")
  @ApiOperation(value = "item version conflicts",
      notes = "Item version private copy conflicts against its public copy",
      response = ItemVersionConflictDto.class)
  Response getConflict(@ApiParam("Item Id") @PathParam("itemId") String itemId,
                       @ApiParam("Version Id") @PathParam("versionId") String versionId,
                       @NotNull(message = RestConstants.USER_MISSING_ERROR_MSG)
                        @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{conflictId}")
  @ApiOperation(value = "Gets item version conflict",
      notes = "Gets an item version private copy conflict against its public copy",
      response = ConflictDto.class)
  Response getConflict(@ApiParam("Item Id") @PathParam("itemId") String itemId,
                       @ApiParam("Version Id") @PathParam("versionId") String versionId,
                       @ApiParam("Version Id") @PathParam("conflictId") String conflictId,
                       @NotNull(message = RestConstants.USER_MISSING_ERROR_MSG)
                       @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{conflictId}")
  @ApiOperation(value = "Resolves item version conflict",
      notes = "Resolves an item version private copy conflict against its public copy")
  Response resolveConflict(ConflictResolutionDto conflictResolution,
                           @ApiParam("Item Id") @PathParam("itemId") String itemId,
                           @ApiParam("Version Id") @PathParam("versionId") String versionId,
                           @ApiParam("Version Id") @PathParam("conflictId") String conflictId,
                           @NotNull(message = RestConstants.USER_MISSING_ERROR_MSG)
                           @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);
}
