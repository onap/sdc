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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdcrests.item.types.ItemActionRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@RequestMapping("/v1.0/items")
@RestController
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Items")})
@Validated
public interface Items {

    @GetMapping({ "", "/" })
    @Operation(description = "Get list of items according to desired filters", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Item.class)))))
    ResponseEntity list(@Parameter(description = "Filter by item status", schema = @Schema(type = "string", allowableValues = {"ACTIVE", "ARCHIVED"}))
                  @RequestParam("itemStatus") String itemStatusFilter,
                        @Parameter(description = "Filter by version status", schema = @Schema(type = "string", allowableValues = {"Certified", "Draft"}))
                  @RequestParam("versionStatus") String versionStatusFilter,
                        @Parameter(description = "Filter by item type", schema = @Schema(type = "string", allowableValues = {"vsp", "vlm"}))
                  @RequestParam("itemType") String itemTypeFilter,
                        @Parameter(description = "Filter by user permission", schema = @Schema(type = "string", allowableValues = {"Owner", "Contributor"}))
                  @RequestParam("permission") String permissionFilter,
                        @Parameter(description = "Filter by onboarding method", schema = @Schema(type = "string", allowableValues = {"NetworkPackage", "manual"}))
                  @RequestParam("onboardingMethod") String onboardingMethodFilter,
                        @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user, @Autowired HttpServletRequest hreq);

    @GetMapping("/{itemId}")
    @Operation(description = "Get details of a item")
    ResponseEntity getItem(@PathVariable("itemId") String itemId, @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{itemId}/actions")
    @Operation(description = "Acts on item version")
    ResponseEntity actOn(@RequestBody ItemActionRequestDto request, @PathVariable("itemId") String itemId,
                   @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);
}
