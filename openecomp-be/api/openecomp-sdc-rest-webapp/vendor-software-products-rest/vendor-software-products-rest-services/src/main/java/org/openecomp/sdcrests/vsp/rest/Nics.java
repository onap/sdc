/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicRequestDto;
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

@Path("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/components/{componentId}/nics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor Software Product Component NICs")
@Validated
public interface Nics extends VspEntities {
  @GET
  @Path("/")
  @ApiOperation(value = "List vendor software product component NICs",
      response = NicDto.class,
      responseContainer = "List")
  Response list(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
                @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
                    String componentId,
                @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                    String user);

  @POST
  @Path("/")
  @ApiOperation(value = "Create a vendor software product NIC")
  Response create(@Valid NicRequestDto request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/{nicId}")
  @ApiOperation(value = "Get vendor software product NIC",
      response = NicDto.class)
  Response get(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
               @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
               @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
                   String componentId,
               @ApiParam(value = "Vendor software product NIC Id") @PathParam("nicId") String nicId,
               @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                   String user);

  @DELETE
  @Path("/{nicId}")
  @ApiOperation(value = "Delete vendor software product NIC")
  Response delete(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @ApiParam(value = "Vendor software product NIC Id") @PathParam("nicId")
                      String nicId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @PUT
  @Path("/{nicId}")
  @ApiOperation(value = "Update vendor software product NIC")
  Response update(@Valid NicRequestDto request,
                  @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                  @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
                  @ApiParam(value = "Vendor software product component Id")
                  @PathParam("componentId") String componentId,
                  @ApiParam(value = "Vendor software product NIC Id") @PathParam("nicId")
                      String nicId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM)
                      String user);

  @GET
  @Path("/{nicId}/questionnaire")
  @ApiOperation(value = "Get vendor software product component NIC questionnaire",
      response = QuestionnaireResponseDto.class)
  Response getQuestionnaire(
      @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
      @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
      @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
          String componentId,
      @ApiParam(value = "Vendor software product NIC Id") @PathParam("nicId") String nicId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{nicId}/questionnaire")
  @ApiOperation(value = "Update vendor software product component NIC questionnaire")
  Response updateQuestionnaire(@NotNull @IsValidJson String questionnaireData,
                               @ApiParam(value = "Vendor software product Id") @PathParam("vspId")
                                   String vspId,
                               @ApiParam(value = "Vendor software product version Id") @PathParam("versionId") String versionId,
                               @ApiParam(value = "Vendor software product component Id")
                               @PathParam("componentId") String componentId,
                               @ApiParam(value = "Vendor software product NIC Id")
                               @PathParam("nicId") String nicId,
                               @NotNull(message = USER_MISSING_ERROR_MSG)
                               @HeaderParam(USER_ID_HEADER_PARAM) String user);
}
