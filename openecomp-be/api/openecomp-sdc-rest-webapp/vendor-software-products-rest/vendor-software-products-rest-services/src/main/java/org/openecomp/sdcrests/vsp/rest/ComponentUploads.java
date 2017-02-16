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

import static org.openecomp.sdcrests.common.RestConstants.USER_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.MibUploadStatusDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/v1.0/vendor-software-products/{vspId}/components/{componentId}/monitors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor Software Product Component MIB Uploads")
@Validated
public interface ComponentUploads {
  @POST
  @Path("/snmp-trap/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ApiOperation(value = "Upload vendor software product MIB Trap Definitions file")
  Response uploadTrapMibFile(@Multipart("upload") Attachment attachment,
                             @ApiParam(value = "Vendor software product Id") @PathParam("vspId")
                                 String vspId,
                             @ApiParam(value = "Vendor software product component Id")
                             @PathParam("componentId") String componentId,
                             @NotNull(message = USER_MISSING_ERROR_MSG)
                             @HeaderParam(USER_HEADER_PARAM) String user);

  @DELETE
  @Path("/snmp-trap")
  @ApiOperation(value = "Delete vendor software product MIB Trap Definitions file")
  Response deleteTrapMibFile(
      @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
      @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
          String componentId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_HEADER_PARAM) String user);

  @POST
  @Path("/snmp/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ApiOperation(value = "Upload vendor software product MIB Poll Definitions file")
  Response uploadPollMibFile(@Multipart("upload") Attachment attachment,
                             @ApiParam(value = "Vendor software product Id") @PathParam("vspId")
                                 String vspId,
                             @ApiParam(value = "Vendor software product component Id")
                             @PathParam("componentId") String componentId,
                             @NotNull(message = USER_MISSING_ERROR_MSG)
                             @HeaderParam(USER_HEADER_PARAM) String user);

  @DELETE
  @Path("/snmp")
  @ApiOperation(value = "Delete vendor software product MIB Poll Definitions file")
  Response deletePollMibFile(
      @ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
      @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
          String componentId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_HEADER_PARAM) String user);

  @GET
  @Path("/snmp/")
  @ApiOperation(value = "Get the filenames of uploaded MIB definitions",
      response = MibUploadStatusDto.class)
  Response list(@ApiParam(value = "Vendor software product Id") @PathParam("vspId") String vspId,
                @ApiParam(value = "Vendor software product component Id") @PathParam("componentId")
                    String componentId,
                @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_HEADER_PARAM)
                    String user);
}
