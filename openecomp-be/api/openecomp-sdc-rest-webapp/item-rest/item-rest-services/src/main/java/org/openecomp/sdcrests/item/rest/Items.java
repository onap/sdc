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

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdcrests.item.types.ItemActionRequestDto;
import org.springframework.validation.annotation.Validated;

@Path("/v1.0/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Items")})
@Validated
public interface Items {

    @GET
    @Path("/")
    @Operation(description = "Get list of items according to desired filters", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Item.class)))))
    Response list(@Parameter(description = "Filter by item status", schema = @Schema(type = "string", allowableValues = {"ACTIVE", "ARCHIVED"}))
                  @QueryParam("itemStatus") String itemStatusFilter,
                  @Parameter(description = "Filter by version status", schema = @Schema(type = "string", allowableValues = {"Certified", "Draft"}))
                  @QueryParam("versionStatus") String versionStatusFilter,
                  @Parameter(description = "Filter by item type", schema = @Schema(type = "string", allowableValues = {"vsp", "vlm"}))
                  @QueryParam("itemType") String itemTypeFilter,
                  @Parameter(description = "Filter by user permission", schema = @Schema(type = "string", allowableValues = {"Owner", "Contributor"}))
                  @QueryParam("permission") String permissionFilter,
                  @Parameter(description = "Filter by onboarding method", schema = @Schema(type = "string", allowableValues = {"NetworkPackage", "manual"}))
                  @QueryParam("onboardingMethod") String onboardingMethodFilter,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user, @Context HttpServletRequest hreq);

    @GET
    @Path("/{itemId}")
    @Operation(description = "Get details of a item")
    Response getItem(@PathParam("itemId") String itemId, @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);

    @PUT
    @Path("/{itemId}/actions")
    @Operation(description = "Acts on item version")
    Response actOn(ItemActionRequestDto request, @PathParam("itemId") String itemId,
                   @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(USER_ID_HEADER_PARAM) String user);
}
