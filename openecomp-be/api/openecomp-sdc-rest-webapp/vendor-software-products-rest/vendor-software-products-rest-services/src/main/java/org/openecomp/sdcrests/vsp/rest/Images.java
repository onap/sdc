package org.openecomp.sdcrests.vsp.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageRequestDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.validation.IsValidJson;
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

@Path("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/components/{componentId}/images")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor Software Product Images")
@Validated
public interface Images extends VspEntities {

  @GET
  @Path("/")
  @ApiOperation(value = "List vendor software product component images",
      response = ImageDto.class,
      responseContainer = "List")
  Response list(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
                @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
                    String componentId,
                @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                    String user);

  @POST
  @Path("/")
  @ApiOperation(value = "Create a vendor software product component image")
  Response create(@Valid ImageRequestDto request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/schema")
  //@ApiOperation(value = "Get schema for vendor software product component Image" ,
  // response = QuestionnaireResponseDto.class)
  Response getImageSchema(@ApiParam(value = "Vendor software product Id") @PathParam("vspId")
                              String vspId,
                          @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
                          @ApiParam(value = "Vendor software product component Id")
                          @PathParam("componentId") String componentId,@NotNull
                              (message = USER_MISSING_ERROR_MSG) @HeaderParam
                              (USER_ID_HEADER_PARAM) String user);

  /*@GET
  @Path("/{imageId}")
  @ApiOperation(value = "Get vendor software product component Image",
      response = ImageDto.class,
      responseContainer = "ImageEntityResponse")
  Response get(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
               @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
                   String componentId,
               @ApiParam(value = "Vendor software product image Id") @PathParam("imageId")
                   String imageId,
               @Pattern(regexp = Version.VERSION_REGEX,
                   message = Version.VERSION_STRING_VIOLATION_MSG) @QueryParam("version")
                   String version,
               @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                   String user);*/

  @GET
  @Path("/{imageId}")
  @ApiOperation(value = "Get vendor software product component Image",
      response = ImageDto.class,
      responseContainer = "CompositionEntityResponse")
  Response get(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
               @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
               @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
                   String componentId,
               @ApiParam(value = "Vendor software product Image Id") @PathParam
                   ("imageId")
                   String imageId,
               @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                   String user);

  @DELETE
  @Path("/{imageId}")
  @ApiOperation(value = "Delete vendor software product Image")
  Response delete(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @ApiParam(value = "Vendor software product Image Id") @PathParam("imageId")
                      String imageId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @PUT
  @Path("/{imageId}")
  @ApiOperation(value = "Update vendor software product Image")
  Response update(@Valid ImageRequestDto request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @ApiParam(value = "Vendor software product Image Id") @PathParam("imageId")
                      String imageId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @PUT
  @Path("/{imageId}/questionnaire")
  @ApiOperation(value = "Update vendor software product component image questionnaire")
  Response updateQuestionnaire(@NotNull @IsValidJson String questionnaireData,
                               @ApiParam(value = "Vendor software product Id")
                               @PathParam("vspId") String vspId,
                               @ApiParam(value = "Version Id")
                               @PathParam("versionId") String versionId,
                               @ApiParam(value = "Vendor software product component Id")
                               @PathParam("componentId") String componentId,
                               @ApiParam(value = "Vendor software product image Id")
                               @PathParam ("imageId") String imageId,
                               @NotNull(message = USER_MISSING_ERROR_MSG)
                               @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{imageId}/questionnaire")
  @ApiOperation(value = "Get vendor software product component image questionnaire",
      response = QuestionnaireResponseDto.class)
  Response getQuestionnaire(
      @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
      @ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
      @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
          String componentId,
      @ApiParam(value = "Vendor software product image Id") @PathParam
          ("imageId") String imageId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);
}
