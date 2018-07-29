package org.openecomp.sdcrests.vsp.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDetailsDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.validation.IsValidJson;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/components/{componentId" +
    "}/compute-flavors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor Software Product Component Compute-flavors")
@Validated
public interface Compute extends VspEntities {

  @GET
  @Path("/")
  @ApiOperation(value = "Get list of vendor software product component compute-flavors",
      response = ComputeDto.class,
      responseContainer = "List")
  Response list(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
                @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
                    String componentId,
                @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                    String user);

  @GET
  @Path("/{computeFlavorId}")
  @ApiOperation(value = "Get vendor software product component compute-flavor",
      response = ComputeDetailsDto.class,
      responseContainer = "CompositionEntityResponse")
  Response get(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
               @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
               @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
                   String componentId,
               @ApiParam(value = "Vendor software product compute-flavor Id") @PathParam
                   ("computeFlavorId")
                   String computeId,
               @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                   String user);

  @POST
  @Path("/")
  @ApiOperation(value = "Create a vendor software product component compute-flavor")
  Response create(@Valid ComputeDetailsDto request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @PUT
  @Path("/{computeFlavorId}")
  @ApiOperation(value = "Update vendor software product component compute-flavor")
  Response update(@Valid ComputeDetailsDto request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @ApiParam(value = "Vendor software product compute-flavor Id") @PathParam
                      ("computeFlavorId")
                      String computeFlavorId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @PUT
  @Path("/{computeFlavorId}/questionnaire")
  @ApiOperation(value = "Update vendor software product component compute-flavor questionnaire")
  Response updateQuestionnaire(@NotNull @IsValidJson String questionnaireData,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @ApiParam(value = "Vendor software product compute-flavor Id") @PathParam
                      ("computeFlavorId")
                      String computeFlavorId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @DELETE
  @Path("/{computeFlavorId}")
  @ApiOperation(value = "Delete vendor software product component compute-flavor")
  Response delete(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @ApiParam(value = "Vendor software product compute-flavor Id") @PathParam
                      ("computeFlavorId")
                      String computeFlavorId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/{computeFlavorId}/questionnaire")
  @ApiOperation(value = "Get vendor software product component compute-flavor questionnaire",
      response = QuestionnaireResponseDto.class)
  Response getQuestionnaire(
      @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
      @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
      @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
          String componentId,
      @ApiParam(value = "Vendor software product compute-flavor Id") @PathParam
          ("computeFlavorId") String computeId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);
}
