/*
 * Copyright © 2018 European Support Limited
 *
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
 */
package org.openecomp.sdcrests.item.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.openecomp.sdcrests.item.types.ItemActionRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Items")
@Validated
public interface Items {

   @GET
   @Path("/{itemId}")
   @ApiOperation(value = "Get details of a item")
   Response getItem(@PathParam("itemId") String itemId,
                     @NotNull(message = USER_MISSING_ERROR_MSG)
                     @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @PUT
  @Path("/{itemId}/actions")
  @ApiOperation(value = "Acts on item version")
  Response actOn(ItemActionRequestDto request,
                 @PathParam("itemId") String itemId,
                 @NotNull(message = USER_MISSING_ERROR_MSG)
                 @HeaderParam(USER_ID_HEADER_PARAM) String user);



}
