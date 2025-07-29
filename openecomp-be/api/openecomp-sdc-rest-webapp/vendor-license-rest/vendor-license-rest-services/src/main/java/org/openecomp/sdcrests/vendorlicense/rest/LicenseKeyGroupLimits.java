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
package org.openecomp.sdcrests.vendorlicense.rest;

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

import org.openecomp.sdcrests.vendorlicense.types.LimitEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.LimitRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1.0/vendor-license-models/{vlmId}/versions/{versionId}/license-key-groups/{licenseKeyGroupId}/limits")
@RestController
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor License Model - License Key Group Limits")})
@Validated
public interface LicenseKeyGroupLimits {

    @PostMapping({ "", "/" })
    @Operation(description = "Create vendor license key group limit")
    ResponseEntity createLimit(@Valid @RequestBody LimitRequestDto request, @Parameter(description = "Vendor license model Id") @PathVariable("vlmId") String vlmId,
                               @Parameter(description = "Vendor license model version Id") @PathVariable("versionId") String versionId,
                               @Parameter(description = "Vendor license model License Key Group Id") @PathVariable("licenseKeyGroupId") String licenseKeyGroupId,
                               @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping({ "", "/" })
    @Operation(description = "List vendor license key group limits", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = LimitEntityDto.class)))))
    ResponseEntity listLimits(@Parameter(description = "Vendor license model Id") @PathVariable("vlmId") String vlmId,
                        @Parameter(description = "Vendor license model version Id") @PathVariable("versionId") String versionId,
                        @Parameter(description = "Vendor license model License Key Group Id") @PathVariable("licenseKeyGroupId") String licenseKeyGroupId,
                        @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{limitId}")
    @Operation(description = "Update vendor license key group limit")
    ResponseEntity updateLimit(@Valid @RequestBody LimitRequestDto request, @Parameter(description = "Vendor license model Id") @PathVariable("vlmId") String vlmId,
                         @Parameter(description = "Vendor license model version Id") @PathVariable("versionId") String versionId,
                         @Parameter(description = "Vendor license model License Key Group Id") @PathVariable("licenseKeyGroupId") String licenseKeyGroupId,
                         @NotNull(message = USER_MISSING_ERROR_MSG) @PathVariable("limitId") String limitId,
                         @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{limitId}")
    @Operation(description = "Get vendor entitlement pool limit", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = LimitEntityDto.class))))
    ResponseEntity getLimit(@Parameter(description = "Vendor license model Id") @PathVariable("vlmId") String vlmId,
                      @Parameter(description = "Vendor license model version Id") @PathVariable("versionId") String versionId,
                      @Parameter(description = "Vendor license model License Key Group") @PathVariable("licenseKeyGroupId") String entitlementPoolId,
                      @Parameter(description = "Vendor license model License Key Group Limit Id") @PathVariable("limitId") String limitId,
                      @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @DeleteMapping("/{limitId}")
    @Operation(description = "Delete vendor license key group limit")
    ResponseEntity deleteLimit(@Parameter(description = "Vendor license model Id") @PathVariable("vlmId") String vlmId,
                         @Parameter(description = "Vendor license model version Id") @PathVariable("versionId") String versionId,
                         @Parameter(description = "Vendor license model license key group Id") @PathVariable("licenseKeyGroupId") String licenseKeyGroupId,
                         @PathVariable("limitId") String limitId,
                         @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);
}
