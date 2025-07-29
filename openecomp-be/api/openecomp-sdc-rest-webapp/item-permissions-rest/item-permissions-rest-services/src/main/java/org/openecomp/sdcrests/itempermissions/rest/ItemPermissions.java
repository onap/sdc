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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.openecomp.sdcrests.itempermissions.types.ItemPermissionsDto;
import org.openecomp.sdcrests.itempermissions.types.ItemPermissionsRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.openecomp.sdcrests.common.RestConstants.USER_ID_HEADER_PARAM;
import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

/**
 * Created by ayalaben on 6/18/2017.
 */
@RequestMapping("/v1.0/items/{itemId}/permissions")
@RestController
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Item Permissions")})
@Validated
public interface ItemPermissions {

    @GetMapping("/")
    @Operation(description = "List users permissions assigned on item", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ItemPermissionsDto.class)))))
    ResponseEntity list(@PathVariable("itemId") String itemId, @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{permission}")
    @Operation(description = "Update useres permission on item")
    ResponseEntity updatePermissions(@Valid @RequestBody ItemPermissionsRequestDto request, @PathVariable("itemId") String itemId,
                               @PathVariable("permission") String permission,
                               @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);
}
