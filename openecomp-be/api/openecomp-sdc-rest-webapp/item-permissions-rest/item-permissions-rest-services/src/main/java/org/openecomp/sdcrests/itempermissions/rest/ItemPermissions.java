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

package org.openecomp.sdcrests.itempermissions.rest;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.openecomp.sdcrests.itempermissions.types.ItemPermissionsDto;
import org.openecomp.sdcrests.itempermissions.types.ItemPermissionsRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

/**
 * Created by ayalaben on 6/18/2017.
 */
@Path("/v1.0/items/{itemId}/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Item Permissions")
@Validated
public interface ItemPermissions {

  @GET
  @Path("/")
  @ApiOperation(value = "List users permissions assigned on item",
      response = ItemPermissionsDto.class,
      responseContainer = "List")

  Response list(@PathParam("itemId") String itemId,
                @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{permission}")
  @ApiOperation(value = "Update useres permission on item")
  Response updatePermissions(@Valid ItemPermissionsRequestDto request,
                             @PathParam("itemId") String itemId,
                             @PathParam("permission") String permission,
                             @NotNull(message = USER_MISSING_ERROR_MSG)
                             @HeaderParam(USER_ID_HEADER_PARAM) String user);

}
