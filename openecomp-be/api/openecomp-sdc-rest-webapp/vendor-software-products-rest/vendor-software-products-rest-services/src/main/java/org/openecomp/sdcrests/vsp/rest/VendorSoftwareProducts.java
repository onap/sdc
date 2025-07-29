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
import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.openecomp.sdcrests.item.types.ItemCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.PackageInfoDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VersionSoftwareProductActionRequestDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspComputeDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDescriptionDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDetailsDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspRequestDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.validation.IsValidJson;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor Software Products")})
@Validated
@RestController
@RequestMapping("/v1.0/vendor-software-products")
public interface VendorSoftwareProducts extends VspEntities {

    @PostMapping({ "", "/" })
    @Operation(description = "Create a new vendor software product", responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = ItemCreationDto.class)))
            , @ApiResponse(responseCode = "401", description = "Unauthorized Tenant")})
    ResponseEntity createVsp(@Valid @RequestBody VspRequestDto vspRequestDto, @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user, HttpServletRequest req);

    @GetMapping({ "", "/" })
    @Operation(description = "Get list of vendor software products and their description", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = VspDetailsDto.class)))))
    ResponseEntity listVsps(@Parameter(description = "Filter to return only Vendor Software Products with at"
            + " least one version at this status. Currently supported values: 'Certified' , 'Draft'") @RequestParam("versionFilter") String versionStatus,
                            @Parameter(description = "Filter to only return Vendor Software Products at this status."
                                    + "Currently supported values: 'ACTIVE' , 'ARCHIVED'."
                                    + "Default value = 'ACTIVE'.") @RequestParam("Status") String itemStatus,
                            @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user, HttpServletRequest req);

    @GetMapping("/{vspId}")
    @Parameter(description = "Get details of the latest certified vendor software product")
    ResponseEntity getLatestVsp(@PathVariable("vspId") String vspId,
                                @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{vspId}/versions/{versionId}")
    @Parameter(description = "Get details of a vendor software product")
    ResponseEntity getVsp(@PathVariable("vspId") String vspId, @PathVariable("versionId") String versionId,
                          @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{vspId}/versions/{versionId}")
    @Parameter(description = "Update an existing vendor software product")
    ResponseEntity updateVsp(@PathVariable("vspId") String vspId, @PathVariable("versionId") String versionId, @Valid @RequestBody VspDescriptionDto vspDescriptionDto,
                             @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @DeleteMapping("/{vspId}")
    @Parameter(description = "Deletes vendor software product by given id")
    ResponseEntity deleteVsp(@PathVariable("vspId") String vspId, @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/packages")
    @Operation(description = "Get list of translated CSAR files details", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = PackageInfoDto.class)))))
    ResponseEntity listPackages(@Parameter(description = "Vendor Software Product status filter. "
            + "Currently supported values: 'ACTIVE', 'ARCHIVED'") @RequestParam("Status") String status,
                                @Parameter(description = "Category") @RequestParam("category") String category,
                                @Parameter(description = "Sub-category") @RequestParam("subCategory") String subCategory,
                                @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{vspId}/versions/{versionId}/orchestration-template")
    @Operation(description = "Get Orchestration Template (HEAT) file", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = File.class))))
    ResponseEntity<byte[]> getOrchestrationTemplate(@PathVariable("vspId") String vspId, @PathVariable("versionId") String versionId,
                                                    @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/validation-vsp")
    ResponseEntity getValidationVsp(@NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user, HttpServletRequest hreq) throws Exception;

    @PutMapping("/{vspId}/versions/{versionId}/actions")
    @Operation(description = "Actions on a vendor software product", summary = "Performs one of the following actions on a vendor software product: |"
            + "Checkout: Locks it for edits by other users. Only the locking user sees the edited " + "version.|"
            + "Undo_Checkout: Unlocks it and deletes the edits that were done.|" + "Checkin: Unlocks it and activates the edited version to all users.| "
            + "Submit: Finalize its active version.|" + "Create_Package: Creates a CSAR zip file.|")
    ResponseEntity actOnVendorSoftwareProduct(@RequestBody VersionSoftwareProductActionRequestDto request, @PathVariable("vspId") String vspId,
                                              @PathVariable("versionId") String versionId,
                                              @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user) throws IOException;

    @GetMapping(value = "/packages/{vspId}")
    @Operation(description = "Get translated CSAR file", summary = "Exports translated file to a zip file", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = File.class))))
    ResponseEntity getTranslatedFile(@PathVariable("vspId") String vspId, @RequestParam("versionId") String versionId,
                                     @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{vspId}/versions/{versionId}/questionnaire")
    @Operation(description = "Get vendor software product questionnaire", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionnaireResponseDto.class))))
    ResponseEntity getQuestionnaire(@PathVariable("vspId") String vspId, @PathVariable("versionId") String versionId,
                                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{vspId}/versions/{versionId}/questionnaire")
    @Operation(description = "Update vendor software product questionnaire")
    ResponseEntity updateQuestionnaire(@NotNull @IsValidJson @RequestBody String questionnaireData, @PathVariable("vspId") String vspId,
                                       @PathVariable("versionId") String versionId,
                                       @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{vspId}/versions/{versionId}/heal")
    @Operation(description = "Checkout and heal vendor software product questionnaire", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionnaireResponseDto.class))))
    ResponseEntity heal(@PathVariable("vspId") String vspId, @PathVariable("versionId") String versionId,
                        @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping(value = "/{vspId}/versions/{versionId}/vspInformationArtifact", produces = "text/plain")
    @Operation(description = "Get vendor software product information artifact for specified version", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = File.class))))
    ResponseEntity getVspInformationArtifact(@PathVariable("vspId") String vspId, @PathVariable("versionId") String versionId,
                                             @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{vspId}/versions/{versionId}/compute-flavors")
    @Operation(description = "Get list of vendor software product compute-flavors", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = VspComputeDto.class)))))
    ResponseEntity listComputes(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                                @PathVariable("versionId") String versionId,
                                @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);
}
