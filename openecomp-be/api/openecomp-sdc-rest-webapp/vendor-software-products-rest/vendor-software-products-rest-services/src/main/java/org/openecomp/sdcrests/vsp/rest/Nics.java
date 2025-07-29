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
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.NicRequestDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.validation.IsValidJson;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/components/{componentId}/nics")
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor Software Product Component NICs")})
@Validated
public interface Nics extends VspEntities {

    @GetMapping("/")
    @Operation(description = "List vendor software product component NICs", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = NicDto.class)))))
    ResponseEntity list(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                        @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                        @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                        @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PostMapping("/")
    @Operation(description = "Create a vendor software product NIC")
    ResponseEntity create(@Valid @RequestBody NicRequestDto request, @Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                    @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                    @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{nicId}")
    @Operation(description = "Get vendor software product NIC", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = NicDto.class))))
    ResponseEntity get(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                 @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                 @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                 @Parameter(description = "Vendor software product NIC Id") @PathVariable("nicId") String nicId,
                 @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @DeleteMapping("/{nicId}")
    @Operation(description = "Delete vendor software product NIC")
    ResponseEntity delete(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                    @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                    @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                    @Parameter(description = "Vendor software product NIC Id") @PathVariable("nicId") String nicId,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{nicId}")
    @Operation(description = "Update vendor software product NIC")
    ResponseEntity update(@Valid @RequestBody NicRequestDto request, @Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                    @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                    @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                    @Parameter(description = "Vendor software product NIC Id") @PathVariable("nicId") String nicId,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{nicId}/questionnaire")
    @Operation(description = "Get vendor software product component NIC questionnaire", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionnaireResponseDto.class))))
    ResponseEntity getQuestionnaire(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                              @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                              @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                              @Parameter(description = "Vendor software product NIC Id") @PathVariable("nicId") String nicId,
                              @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{nicId}/questionnaire")
    @Operation(description = "Update vendor software product component NIC questionnaire")
    ResponseEntity updateQuestionnaire(@RequestBody @NotNull @IsValidJson String questionnaireData,
                                 @Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                                 @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                                 @Parameter(description = "Vendor software product component Id") @PathVariable("componentId") String componentId,
                                 @Parameter(description = "Vendor software product NIC Id") @PathVariable("nicId") String nicId,
                                 @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);
}
