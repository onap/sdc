/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdcrests.vsp.rest;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

import java.io.File;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.springframework.validation.annotation.Validated;

@Path("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/vnfrepository")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "VNF Repository packages")})
@Validated
public interface VnfPackageRepository extends VspEntities {

    @GET
    @Path("/vnfpackages")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(description = "Get VNF packages from VNF Repository",
            summary = "Call VNF Repository to get VNF package details", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = File.class))))
    Response getVnfPackages(@PathParam("vspId") String vspId,
            @Parameter(description= "Version Id") @PathParam("versionId") String versionId,
            @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user) throws Exception;

    @GET
    @Path("/vnfpackage/{csarId}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(description = "Download VNF package from VNF Repository",
            summary = "Download VNF package from VNF repository and send to client", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = File.class))))
    Response downloadVnfPackage(@PathParam("vspId") String vspId,
            @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
            @PathParam("csarId") String csarId,
            @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user) throws Exception;

    @POST
    @Path("/vnfpackage/{csarId}/import")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Import VNF package from VNF Repository",
            summary = "Call VNF Repository to download VNF package, validate it and send the response",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = UploadFileResponseDto.class))))
    Response importVnfPackage(@PathParam("vspId") String vspId,
            @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
            @PathParam("csarId") String csarId,
            @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user) throws Exception;

}
