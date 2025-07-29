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
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ProcessEntityDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ProcessRequestDto;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/processes")
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Vendor Software Product Processes")})
@Validated
public interface Processes {

    @GetMapping({ "", "/" })
    @Operation(description = "List vendor software product processes", responses = @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProcessEntityDto.class)))))
    ResponseEntity list(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                        @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                        @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @DeleteMapping({ "", "/" })
    @Operation(description = "Delete vendor software product processes", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
    ResponseEntity deleteList(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                              @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                              @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PostMapping({ "", "/" })
    @Operation(description = "Create a vendor software product process")
    ResponseEntity create(@Valid @RequestBody ProcessRequestDto request, @Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                          @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                          @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping("/{processId}")
    @Operation(description = "Get vendor software product process", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ProcessEntityDto.class))))
    ResponseEntity get(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                       @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                       @Parameter(description = "Vendor software product process Id") @PathVariable("processId") String processId,
                       @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @DeleteMapping("/{processId}")
    @Operation(description = "Delete vendor software product process")
    ResponseEntity delete(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                          @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                          @Parameter(description = "Vendor software product process Id") @PathVariable("processId") String processId,
                          @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PutMapping("/{processId}")
    @Operation(description = "Update vendor software product process")
    ResponseEntity update(@Valid @RequestBody ProcessRequestDto request, @Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                          @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                          @Parameter(description = "Vendor software product process Id") @PathVariable("processId") String processId,
                          @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @GetMapping(value = "/{processId}/upload",
    produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(description = "Get vendor software product process uploaded file")
    ResponseEntity<Resource> getUploadedFile(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                                           @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                                           @Parameter(description = "Vendor software product process Id") @PathVariable("processId") String processId,
                                           @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @DeleteMapping("/{processId}/upload")
    @Operation(description = "Delete vendor software product process uploaded file")
    ResponseEntity deleteUploadedFile(@Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                                      @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                                      @Parameter(description = "Vendor software product process Id") @PathVariable("processId") String processId,
                                      @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);

    @PostMapping(value = "/{processId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(description = "Update vendor software product process upload")
    ResponseEntity uploadFile(@RequestPart("upload") MultipartFile multipartFile,
                              @Parameter(description = "Vendor software product Id") @PathVariable("vspId") String vspId,
                              @Parameter(description = "Vendor software product version Id") @PathVariable("versionId") String versionId,
                              @Parameter(description = "Vendor software product process Id") @PathVariable("processId") String processId,
                              @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);
}
