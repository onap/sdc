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
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDetailsDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.validation.IsValidJson;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/components/{componentId" + "}/compute-flavors")
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor Software Product Component Compute-flavors")})
@Validated
public interface Compute extends VspEntities {

    @GetMapping({ "", "/" })
    @Operation(description = "Get list of vendor software product component compute-flavors", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComputeDto.class)))))
    ResponseEntity list(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                        @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                        @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                        @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{computeFlavorId}")
    @Operation(description = "Get vendor software product component compute-flavor", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ComputeDetailsDto.class)))))
    ResponseEntity get(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                 @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                 @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                 @Parameter(description = "Vendor software product compute-flavor Id") @PathVariable("computeFlavorId") String computeId,
                 @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PostMapping({ "", "/" })
    @Operation(description = "Create a vendor software product component compute-flavor")
    ResponseEntity create(@Valid @RequestBody ComputeDetailsDto request, @Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                    @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                    @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{computeFlavorId}")
    @Operation(description = "Update vendor software product component compute-flavor")
    ResponseEntity update(@Valid @RequestBody ComputeDetailsDto request, @Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                    @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                    @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                    @Parameter(description = "Vendor software product compute-flavor Id") @PathVariable("computeFlavorId") String computeFlavorId,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{computeFlavorId}/questionnaire")
    @Operation(description = "Update vendor software product component compute-flavor questionnaire")
    ResponseEntity updateQuestionnaire(@RequestBody @NotNull @IsValidJson String questionnaireData,
                                 @Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                                 @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                                 @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                                 @Parameter(description = "Vendor software product compute-flavor Id") @PathVariable("computeFlavorId") String computeFlavorId,
                                 @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @DeleteMapping("/{computeFlavorId}")
    @Operation(description = "Delete vendor software product component compute-flavor")
    ResponseEntity delete(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                    @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                    @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                    @Parameter(description = "Vendor software product compute-flavor Id") @PathVariable("computeFlavorId") String computeFlavorId,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{computeFlavorId}/questionnaire")
    @Operation(description = "Get vendor software product component compute-flavor questionnaire", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionnaireResponseDto.class))))
    ResponseEntity getQuestionnaire(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                              @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                              @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                              @Parameter(description = "Vendor software product compute-flavor Id") @PathVariable("computeFlavorId") String computeId,
                              @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);
}
