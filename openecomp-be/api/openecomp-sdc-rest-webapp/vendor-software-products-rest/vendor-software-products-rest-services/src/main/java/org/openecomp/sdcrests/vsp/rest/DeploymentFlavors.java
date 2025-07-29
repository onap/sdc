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
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorListResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/deployment-flavors")
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor Software Product deployment-flavors")})
@Validated
public interface DeploymentFlavors extends VspEntities {

    @PostMapping({ "", "/" })
    @Operation(description = "Create a vendor software product Deployment Flavor")
    ResponseEntity create(@Valid @RequestBody DeploymentFlavorRequestDto request,
                          @Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                          @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                          @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping({ "", "/" })
    @Operation(description = "List vendor software product Deployment Flavor", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeploymentFlavorListResponseDto.class)))))
    ResponseEntity list(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                  @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                  @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{deploymentFlavorId}")
    @Operation(description = "Get vendor software product Deployment Flavor", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = DeploymentFlavorDto.class))))
    ResponseEntity get(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                 @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                 @Parameter(description = "Vendor software product Deployment Flavor Id") @PathVariable("deploymentFlavorId") String deploymentFlavorId,
                 @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/schema")
    ResponseEntity getSchema(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                             @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                       @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @DeleteMapping("/{deploymentFlavorId}")
    @Operation(description = "Delete vendor software product Deployment Flavor")
    ResponseEntity delete(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                    @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                    @Parameter(description = "Vendor software product Deployment Flavor Id") @PathVariable("deploymentFlavorId") String deploymentFlavorId,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{deploymentFlavorId}")
    @Operation(description = "Update vendor software product Deployment Flavor")
    ResponseEntity update(@Valid @RequestBody DeploymentFlavorRequestDto request,
                    @Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                    @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                    @Parameter(description = "Vendor software product Deployment Flavor Id") @PathVariable("deploymentFlavorId") String deploymentFlavorId,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);
}
