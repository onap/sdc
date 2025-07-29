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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.openecomp.sdcrests.item.types.ActivityLogDto;
import org.openecomp.sdcrests.item.types.VersionActionRequestDto;
import org.openecomp.sdcrests.item.types.VersionDto;
import org.openecomp.sdcrests.item.types.VersionRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1.0/items/{itemId}/versions")
@RestController
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Item Versions")})
@Validated
public interface Versions {

    @GetMapping("/")
    @Operation(description = "Lists item versions", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = VersionDto.class)))))
    ResponseEntity list(@PathVariable("itemId") String itemId, @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PostMapping("/{versionId}")
    @Operation(description = "Creates a new item version")
    ResponseEntity create(@RequestBody VersionRequestDto request, @PathVariable("itemId") String itemId, @PathVariable("versionId") String versionId,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{versionId}")
    @Operation(description = "Gets item version", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = VersionDto.class))))
    ResponseEntity get(@PathVariable("itemId") String itemId, @PathVariable("versionId") String versionId,
                 @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{versionId}/activity-logs")
    @Operation(description = "Gets item version activity log", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ActivityLogDto.class)))))
    ResponseEntity getActivityLog(@Parameter(description = "Item Id") @PathVariable("itemId") String itemId,
                            @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                            @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{versionId}/revisions")
    @Operation(description = "Gets item version revisions", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ActivityLogDto.class)))))
    ResponseEntity listRevisions(@PathVariable("itemId") String itemId, @PathVariable("versionId") String versionId,
                           @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{versionId}/actions")
    @Operation(description = "Acts on item version")
    ResponseEntity actOn(@RequestBody @Valid VersionActionRequestDto request, @PathVariable("itemId") String itemId, @PathVariable("versionId") String versionId,
                         @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);
}
