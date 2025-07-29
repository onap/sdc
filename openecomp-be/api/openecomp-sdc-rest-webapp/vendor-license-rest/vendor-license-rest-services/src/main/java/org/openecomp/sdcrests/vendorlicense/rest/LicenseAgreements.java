/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import org.openecomp.sdcrests.vendorlicense.types.LicenseAgreementEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.LicenseAgreementModelDto;
import org.openecomp.sdcrests.vendorlicense.types.LicenseAgreementRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.LicenseAgreementUpdateRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1.0/vendor-license-models/{vlmId}/versions/{versionId}/license-agreements")
@RestController
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor License Model - License Agreements")})
@Validated
public interface LicenseAgreements {

    @GetMapping({ "", "/" })
    @Operation(description = "List vendor license agreements", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = LicenseAgreementEntityDto.class)))))
    ResponseEntity listLicenseAgreements(@Parameter(description = "Vendor license model Id") @PathVariable("vlmId") String vlmId,
                                         @Parameter(description = "Vendor license model version Id") @PathVariable("versionId") String versionId,
                                         @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PostMapping({ "", "/" })
    @Operation(description = "Create vendor license agreement")
    ResponseEntity createLicenseAgreement(@RequestBody @Valid LicenseAgreementRequestDto request,
                                    @Parameter(description = "Vendor license model Id") @PathVariable("vlmId") String vlmId,
                                    @Parameter(description = "Vendor license model version Id") @PathVariable("versionId") String versionId,
                                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{licenseAgreementId}")
    @Operation(description = "Update vendor license agreement")
    ResponseEntity updateLicenseAgreement(@RequestBody @Valid LicenseAgreementUpdateRequestDto request,
                                    @Parameter(description = "Vendor license model Id") @PathVariable("vlmId") String vlmId,
                                    @Parameter(description = "Vendor license model version Id") @PathVariable("versionId") String versionId,
                                    @PathVariable("licenseAgreementId") String licenseAgreementId,
                                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{licenseAgreementId}")
    @Operation(description = "Get vendor license agreement", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = LicenseAgreementModelDto.class))))
    ResponseEntity getLicenseAgreement(@Parameter(description = "Vendor license model Id") @PathVariable("vlmId") String vlmId,
                                 @Parameter(description = "Vendor license model version Id") @PathVariable("versionId") String versionId,
                                 @PathVariable("licenseAgreementId") String licenseAgreementId,
                                 @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @DeleteMapping("/{licenseAgreementId}")
    @Operation(description = "Delete vendor license agreement")
    ResponseEntity deleteLicenseAgreement(@Parameter(description = "Vendor license model Id") @PathVariable("vlmId") String vlmId,
                                    @Parameter(description = "Vendor license model version Id") @PathVariable("versionId") String versionId,
                                    @PathVariable("licenseAgreementId") String licenseAgreementId,
                                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);
}
