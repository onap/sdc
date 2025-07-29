/*
 * Copyright © 2016-2018 European Support Limited
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.FileDataStructureDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1.0/vendor-software-products/{vspId}/versions/{versionId}/orchestration-template-candidate")
@Tags({@Tag(name = "SDCE-1 APIs"), @Tag(name = "Orchestration Template Candidate")})
@Validated
public interface OrchestrationTemplateCandidate extends VspEntities {

    @PostMapping("/")
    ResponseEntity upload(@PathVariable("vspId") String vspId, @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                    @RequestPart("upload") MultipartFile fileToUpload,
                    @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user);


    @GetMapping("/")
    @Operation(description = "Get uploaded Network Package file", summary = "Downloads in uploaded Network Package file", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = File.class))))
    ResponseEntity get(@PathVariable("vspId") String vspId, @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                       @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user) throws IOException;


    @DeleteMapping("/")
    @Operation(description = "Delete orchestration template candidate file and its files data structure")
    ResponseEntity abort(@PathVariable("vspId") String vspId, @Parameter(description = "Version Id") @PathVariable("versionId") String versionId)
        throws Exception;


    @PutMapping("/process")
    @Operation(description = "process Orchestration Template Candidate", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = UploadFileResponseDto.class))))
    ResponseEntity process(@PathVariable("vspId") String vspId, @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                     @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user)
        throws InvocationTargetException, IllegalAccessException;


    @PutMapping("/manifest")
    @Operation(description = "Update an existing vendor software product")
    ResponseEntity updateFilesDataStructure(@PathVariable("vspId") String vspId,
                                      @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                                      @Valid @RequestBody FileDataStructureDto fileDataStructureDto,
                                      @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user) throws Exception;


    @GetMapping("/manifest")
    @Operation(description = "Get uploaded HEAT file files data structure", summary = "Downloads the latest HEAT package", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = FileDataStructureDto.class))))
    ResponseEntity getFilesDataStructure(@PathVariable("vspId") String vspId, @Parameter(description = "Version Id") @PathVariable("versionId") String versionId,
                                   @NotNull(message = USER_MISSING_ERROR_MSG) @RequestHeader(USER_ID_HEADER_PARAM) String user) throws Exception;
}
