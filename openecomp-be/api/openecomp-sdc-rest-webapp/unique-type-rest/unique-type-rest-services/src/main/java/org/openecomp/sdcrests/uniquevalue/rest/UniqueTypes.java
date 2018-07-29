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
package org.openecomp.sdcrests.uniquevalue.rest;

import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@Path("/v1.0/unique-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Unique Types")
@Validated
public interface UniqueTypes {

  @GET
  @Path("/")
  @ApiOperation(value = "Lists unique value types")
  Response listUniqueTypes(@NotNull(message = USER_MISSING_ERROR_MSG)
                           @HeaderParam(USER_ID_HEADER_PARAM) String user);

  @GET
  @Path("/{type}/values/{value}")
  @ApiOperation(value = "Gets unique value")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indication whether the unique value is occupied"),
      @ApiResponse(code = 404, message = "Unsupported unique type")})
  Response getUniqueValue(
      @ApiParam("The unique value type, for example: 'VlmName'") @PathParam("type") String type,
      @ApiParam("The unique value") @PathParam("value") String value,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);
}
