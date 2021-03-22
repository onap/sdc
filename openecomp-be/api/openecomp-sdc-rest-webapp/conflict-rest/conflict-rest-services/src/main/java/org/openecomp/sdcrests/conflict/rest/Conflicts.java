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
package org.openecomp.sdcrests.conflict.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdcrests.common.RestConstants;
import org.openecomp.sdcrests.conflict.types.ConflictDto;
import org.openecomp.sdcrests.conflict.types.ConflictResolutionDto;
import org.openecomp.sdcrests.conflict.types.ItemVersionConflictDto;
import org.springframework.validation.annotation.Validated;

@Path("/v1.0/items/{itemId}/versions/{versionId}/conflicts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Item Version Conflicts")})
@Validated
public interface Conflicts {

    @GET
    @Path("/")
    @Operation(description = "item version conflicts", summary = "Item version private copy conflicts against its public copy", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ItemVersionConflictDto.class))))
    Response getConflict(@Parameter(description = "Item Id") @PathParam("itemId") String itemId,
                         @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                         @NotNull(message = RestConstants.USER_MISSING_ERROR_MSG) @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);

    @GET
    @Path("/{conflictId}")
    @Operation(description = "Gets item version conflict", summary = "Gets an item version private copy conflict against its public copy", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ConflictDto.class))))
    Response getConflict(@Parameter(description = "Item Id") @PathParam("itemId") String itemId,
                         @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                         @Parameter(description = "Version Id") @PathParam("conflictId") String conflictId,
                         @NotNull(message = RestConstants.USER_MISSING_ERROR_MSG) @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);

    @PUT
    @Path("/{conflictId}")
    @Operation(description = "Resolves item version conflict", summary = "Resolves an item version private copy conflict against its public copy")
    Response resolveConflict(ConflictResolutionDto conflictResolution, @Parameter(description = "Item Id") @PathParam("itemId") String itemId,
                             @Parameter(description = "Version Id") @PathParam("versionId") String versionId,
                             @Parameter(description = "Version Id") @PathParam("conflictId") String conflictId,
                             @NotNull(message = RestConstants.USER_MISSING_ERROR_MSG) @HeaderParam(RestConstants.USER_ID_HEADER_PARAM) String user);
}
