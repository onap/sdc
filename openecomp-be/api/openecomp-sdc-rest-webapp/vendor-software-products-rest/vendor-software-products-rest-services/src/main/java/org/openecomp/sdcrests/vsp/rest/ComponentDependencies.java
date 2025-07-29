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
package org.openecomp.sdcrests.vsp.rest;

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
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModel;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/component-dependencies")
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor Software Product Component Dependencies")})
@Validated
public interface ComponentDependencies extends VspEntities {

    @PostMapping({ "", "/" })
    @Operation(description = "Create a vendor software product component dependency")
    ResponseEntity create(@Valid @RequestBody ComponentDependencyModel request, @Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                          @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                          @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping({ "", "/" })
    @Operation(description = "Get component dependencies for vendor software product", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComponentDependencyResponseDto.class)))))
    ResponseEntity list(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                  @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @DeleteMapping("/{dependencyId}")
    @Operation(description = "Delete component dependency for vendor software product")
    ResponseEntity delete(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                    @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                    @Parameter(description = "Vendor software product Component Dependency Id") @PathVariable("dependencyId") String dependencyId,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{dependencyId}")
    @Operation(description = "Update component dependency for vendor software product")
    ResponseEntity update(@Valid @RequestBody ComponentDependencyModel request, @Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                    @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                    @Parameter(description = "Vendor software product Component Dependency Id") @PathVariable("dependencyId") String dependencyId,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{dependencyId}")
    @Operation(description = "Get component dependency for vendor software product", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ComponentDependencyResponseDto.class))))
    ResponseEntity get(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                 @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                 @Parameter(description = "Vendor software product Component Dependency Id") @PathVariable("dependencyId") String dependencyId,
                 @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);
}
