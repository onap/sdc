/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdcrests.vsp.rest;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
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
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageRequestDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.validation.IsValidJson;
import org.springframework.validation.annotation.Validated;

@Path("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/components/{componentId}/images")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor Software Product Images")})
@Validated
public interface Images extends VspEntities {

    @GET
    @Path("/")
    @Operation(description = "List vendor software product component images", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ImageDto.class)))))
    Response list(@Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                  @Parameter(description = "Vendor software product component Id") @PathParam("componentId") String componentId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

    @POST
    @Path("/")
    @Operation(description = "Create a vendor software product component image")
    Response create(@Valid ImageRequestDto request, @Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                    @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                    @Parameter(description = "Vendor software product component Id") @PathParam("componentId") String componentId,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

    @GET
    @Path("/schema")
        //@Operation(description = "Get schema for vendor software product component Image" ,

        // response = QuestionnaireResponseDto.class)
    Response getImageSchema(@Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                            @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                            @Parameter(description = "Vendor software product component Id") @PathParam("componentId") String componentId,
                            @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

    /*@GET
    @Path("/{imageId}")
    @Operation(description = "Get vendor software product component Image",
        response = ImageDto.class,
        responseContainer = "ImageEntityResponse")
    Response get(@Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                 @Parameter(description = "Vendor software product component Id") @PathParam("componentId")
                     String componentId,
                 @Parameter(description = "Vendor software product image Id") @PathParam("imageId")
                     String imageId,
                 @Pattern(regexp = Version.VERSION_REGEX,
                     message = Version.VERSION_STRING_VIOLATION_MSG) @QueryParam("version")
                     String version,
                 @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                     String user);*/
    @GET
    @Path("/{imageId}")
    @Operation(description = "Get vendor software product component Image", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ImageDto.class))))
    Response get(@Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                 @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                 @Parameter(description = "Vendor software product component Id") @PathParam("componentId") String componentId,
                 @Parameter(description = "Vendor software product Image Id") @PathParam("imageId") String imageId,
                 @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

    @DELETE
    @Path("/{imageId}")
    @Operation(description = "Delete vendor software product Image")
    Response delete(@Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                    @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                    @Parameter(description = "Vendor software product component Id") @PathParam("componentId") String componentId,
                    @Parameter(description = "Vendor software product Image Id") @PathParam("imageId") String imageId,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

    @PUT
    @Path("/{imageId}")
    @Operation(description = "Update vendor software product Image")
    Response update(@Valid ImageRequestDto request, @Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                    @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                    @Parameter(description = "Vendor software product component Id") @PathParam("componentId") String componentId,
                    @Parameter(description = "Vendor software product Image Id") @PathParam("imageId") String imageId,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

    @PUT
    @Path("/{imageId}/questionnaire")
    @Operation(description = "Update vendor software product component image questionnaire")
    Response updateQuestionnaire(@NotNull @IsValidJson String questionnaireData,
                                 @Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                                 @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                                 @Parameter(description = "Vendor software product component Id") @PathParam("componentId") String componentId,
                                 @Parameter(description = "Vendor software product image Id") @PathParam("imageId") String imageId,
                                 @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

    @GET
    @Path("/{imageId}/questionnaire")
    @Operation(description = "Get vendor software product component image questionnaire", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionnaireResponseDto.class))))
    Response getQuestionnaire(@Parameter(description = "Vendor software product Id") @PathParam("vspId") String vspId,
                              @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                              @Parameter(description = "Vendor software product component Id") @PathParam("componentId") String componentId,
                              @Parameter(description = "Vendor software product image Id") @PathParam("imageId") String imageId,
                              @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);
}
