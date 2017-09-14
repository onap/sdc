package org.openecomp.sdcrests.vsp.rest;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModelRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/component-dependency-model")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor Software Product Component Dependency Model")
@Validated
public interface ComponentDependencyModels extends VspEntities {

  @POST
  @Path("/")
  @ApiOperation(value = "Create a vendor software product component dependency model")
  Response create(@Valid ComponentDependencyModelRequestDto request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/")
  @ApiOperation(value = "Get component dependency Model for vendor software product",
      response = ComponentDependencyModelRequestDto.class,
      responseContainer = "List")
  Response list(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                @ApiParam(value = "Vendor software product version Id") @PathParam("versionId")
                    String versionId,
                @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                    String user);
}
