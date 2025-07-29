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
package org.openecomp.sdcrests.vendorlicense.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.openecomp.sdcrests.common.RestConstants;
import org.openecomp.sdcrests.item.types.ItemDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelActionRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

@RequestMapping("/v1.0/vendor-license-models")
@RestController
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor License Models")})
@Validated
public interface VendorLicenseModels {

    @GetMapping({ "", "/" })
    @Operation(description = "List vendor license models", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ItemDto.class)))))
    ResponseEntity listLicenseModels(@Parameter(description = "Filter to return only Vendor License Models with at"
        + " least one version at this status. Currently supported values: 'Certified' , 'Draft'") @RequestParam("versionFilter") String versionStatus,
                                     @Parameter(description = "Filter to only return Vendor License Models at this status."
                                   + "Currently supported values: 'ACTIVE' , 'ARCHIVED'."
                                   + "Default value = 'ACTIVE'.") @RequestParam("Status") String itemStatus,
                                     @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(RestConstants.USER_ID_HEADER_PARAM) String user , @Autowired HttpServletRequest req);

    @PostMapping({ "", "/" })
    @Operation(description = "Create vendor license model", responses = @ApiResponse(responseCode = "401", description = "Unauthorized Tenant"))
    ResponseEntity createLicenseModel(@Valid @RequestBody VendorLicenseModelRequestDto request,
                                @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(RestConstants.USER_ID_HEADER_PARAM) String user, @Autowired HttpServletRequest req);

    @DeleteMapping("/{vlmId}")
    @Operation(description = "Delete vendor license model")
    ResponseEntity deleteLicenseModel(@Parameter(description = "Vendor license model Id") @PathVariable("vlmId") String vlmId,
                                @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(RestConstants.USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{vlmId}/versions/{versionId}")
    @Operation(description = "Update vendor license model")
    ResponseEntity updateLicenseModel(@Valid @RequestBody VendorLicenseModelRequestDto request,
                                @Parameter(description = "Vendor license model Id") @PathVariable("vlmId") String vlmId,
                                @Parameter(description = "Vendor license model version Id") @PathVariable("versionId") String versionId,
                                @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(RestConstants.USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{vlmId}/versions/{versionId}")
    @Operation(description = "Get vendor license model", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = VendorLicenseModelEntityDto.class))))
    ResponseEntity getLicenseModel(@Parameter(description = "Vendor license model Id") @PathVariable("vlmId") String vlmId,
                             @Parameter(description = "Vendor license model version Id") @PathVariable("versionId") String versionId,
                             @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(RestConstants.USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{vlmId}/versions/{versionId}/actions")
    @Operation(description = "Update vendor license model")
    ResponseEntity actOnLicenseModel(@Valid @RequestBody VendorLicenseModelActionRequestDto request,
                               @Parameter(description = "Vendor license model Id") @PathVariable("vlmId") String vlmId,
                               @Parameter(description = "Vendor license model version Id") @PathVariable("versionId") String versionId,
                               @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(RestConstants.USER_ID_HEADER_PARAM) String user);
}
