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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.springframework.validation.annotation.Validated;

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
import java.io.File;
import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/vnfrepository")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "VNF Repository packages")
@Validated
public interface VnfPackageRepository extends VspEntities {

	@GET
	@Path("/vnfpackages")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Get VNF packages from VNF Repository", notes = "Call VNF Repostory to get VNF package details", response = File.class)
	Response getVnfPackages(@PathParam("vspId") String vspId,
			@ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
			@NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user) throws Exception;

	@GET
	@Path("/vnfpackage/{csarId}/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Download VNF package from VNF Repository", notes = "Download VNF package from VNF repository and send to client", response = File.class)
	Response downloadVnfPackage(@PathParam("vspId") String vspId,
			@ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
			@PathParam("csarId") String csarId,
			@NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user) throws Exception;

	@POST
	@Path("/vnfpackage/{csarId}/import")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Import VNF package from VNF Repository", notes = "Call VNF Repostory to download VNF package, validate it and send the response", response = UploadFileResponseDto.class)
	Response importVnfPackage(@PathParam("vspId") String vspId,
			@ApiParam(value = "Version Id") @PathParam("versionId") String versionId,
			@PathParam("csarId") String csarId,
			@NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user) throws Exception;

}
