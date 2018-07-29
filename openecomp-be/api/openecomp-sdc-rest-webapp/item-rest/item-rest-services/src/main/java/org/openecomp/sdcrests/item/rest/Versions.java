package org.openecomp.sdcrests.item.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.sdcrests.item.types.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/items/{itemId}/versions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Item Versions")
@Validated
public interface Versions {

  @GET
  @Path("/")
  @ApiOperation(value = "Lists item versions",
      response = VersionDto.class,
      responseContainer = "List")
  Response list(@PathParam("itemId") String itemId,
                @NotNull(message = USER_MISSING_ERROR_MSG)
                @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @POST
  @Path("/{versionId}")
  @ApiOperation(value = "Creates a new item version")
  Response create(VersionRequestDto request,
                  @PathParam("itemId") String itemId,
                  @PathParam("versionId") String versionId,
                  @NotNull(message = USER_MISSING_ERROR_MSG)
                  @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{versionId}")
  @ApiOperation(value = "Gets item version", response = VersionDto.class)
  Response get(@PathParam("itemId") String itemId,
               @PathParam("versionId") String versionId,
               @NotNull(message = USER_MISSING_ERROR_MSG)
               @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{versionId}/activity-logs")
  @ApiOperation(value = "Gets item version activity log",
      response = ActivityLogDto.class,
      responseContainer = "List")
  Response getActivityLog(@ApiParam("Item Id") @PathParam("itemId") String itemId,
                          @ApiParam("Version Id") @PathParam("versionId") String versionId,
                          @NotNull(message = USER_MISSING_ERROR_MSG)
                          @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{versionId}/revisions")
  @ApiOperation(value = "Gets item version revisions", response = RevisionDto.class,
      responseContainer = "List")
  Response listRevisions(@PathParam("itemId") String itemId,
                         @PathParam("versionId") String versionId,
                         @NotNull(message = USER_MISSING_ERROR_MSG)
               @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{versionId}/actions")
  @ApiOperation(value = "Acts on item version")
  Response actOn(VersionActionRequestDto request,
                 @PathParam("itemId") String itemId,
                 @PathParam("versionId") String versionId,
                 @NotNull(message = USER_MISSING_ERROR_MSG)
                 @HeaderParam(USER_ID_HEADER_PARAM) String user);
}
