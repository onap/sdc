package org.openecomp.sdcrests.vsp.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModel;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyResponseDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/component-dependencies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor Software Product Component Dependencies")
@Validated
public interface ComponentDependencies extends VspEntities {

  @POST
  @Path("/")
  @ApiOperation(value = "Create a vendor software product component dependency")
  Response create(@Valid ComponentDependencyModel request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/")
  @ApiOperation(value = "Get component dependencies for vendor software product",
      response = ComponentDependencyResponseDto.class,
      responseContainer = "List")
  Response list(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                @ApiParam(value = "Vendor software product version Id") @PathParam("versionId")
                    String versionId,
                @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                    String user);

  @DELETE
  @Path("/{dependencyId}")
  @ApiOperation(value = "Delete component dependency for vendor software product")
  Response delete(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Vendor software product version Id")
                  @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product Component Dependency Id") @PathParam
                      ("dependencyId") String dependencyId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @PUT
  @Path("/{dependencyId}")
  @ApiOperation(value = "Update component dependency for vendor software product")
  Response update(@Valid ComponentDependencyModel request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Vendor software product version Id") @PathParam("versionId")
                      String versionId,
                  @ApiParam(value = "Vendor software product Component Dependency Id") @PathParam
                      ("dependencyId") String dependencyId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/{dependencyId}")
  @ApiOperation(value = "Get component dependency for vendor software product",
      response = ComponentDependencyResponseDto.class)
  Response get(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
               @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
               @ApiParam(value = "Vendor software product Component Dependency Id") @PathParam
                   ("dependencyId") String dependencyId,
               @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                   String user);
}
